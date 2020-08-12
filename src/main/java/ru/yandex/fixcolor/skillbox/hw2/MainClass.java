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

public class MainClass {
    private final String TOKEN = "qD5dXi3UxZAAAAAAAAAAAcf2t0uqRthz7juDp4kyYnXeHlMEW8DY2DPENPz92hju";

    private Thread objDelay = null;
    private Thread objSender = null;
    private boolean puskSend = false;

    public static void main(String[] args) {
        new MainClass().start();
    }
    private  void start() {
        objDelay = new DelayClass(5000);
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
            try {
                while (lifeParent.isAlive()) {
                    Thread.sleep(timeDelay);
                    puskSend = true;
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            try {
                while (lifeParent.isAlive()) {
                    if (!puskSend) {
                        Thread.sleep(100);
                        continue;
                    }
                    puskSend = false;
                    image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                    String data = dateFormat.format(new Date());

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


