package ru.yandex.fixcolor.skillbox.hw2;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MainClass {
    private final String TOKEN = "WC8AFF8My1AAAAAAAAAAAXAZd3Y4brEuiz88I7ZgI-BAFpyJLoC2HmDInnkzw_a6";
    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

    private Thread objDelay = null;
    private Thread objSender = null;

    public static void main(String[] args) {
        new MainClass().start();
    }
    private  void start() {
        objDelay = new DelayClass(5_000);
        objSender = new SenderClass();
        objSender.start();
        objDelay.start();
        try {
            while (true) Thread.sleep(1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class DelayClass extends Thread {
        private long timeDelay;
        private Thread lifeParent = null;

        public DelayClass(long timeDelay) {
            this.timeDelay = timeDelay;
            lifeParent = Thread.currentThread();
        }

        @Override
        public void run() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            try {
                while (lifeParent.isAlive()) {
                    Thread.sleep(timeDelay);
                    queue.add(dateFormat.format(new Date()));
                }
            } catch (InterruptedException e) {
                System.out.println("crash class delay: " + e.getMessage());
            }
        }
    }

    class SenderClass extends Thread {
        private Thread lifeParent = null;
        public SenderClass() {
            lifeParent = Thread.currentThread();
        }

        @Override
        public void run() {
            BufferedImage image = null;
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            DbxClientV2 client = new DbxClientV2(config, TOKEN);

            ByteArrayOutputStream outputStream = null;
            InputStream inputStream = null;
            String data;
            try {
                while (lifeParent.isAlive()) {
                    data = queue.poll(1, TimeUnit.SECONDS);
                    if (data == null) continue;

                    image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                    outputStream = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", outputStream);
                    inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                    FileMetadata metadata = client.files().uploadBuilder("/" + data + ".png").uploadAndFinish(inputStream);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (AWTException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UploadErrorException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (java.lang.Throwable e) {
                System.out.println("упс " + e.getMessage());
            }
            System.out.println("stop");
        }
    }
}


