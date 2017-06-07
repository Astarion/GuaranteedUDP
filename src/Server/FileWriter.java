package Server;

import Client.FileReader;
import CommonUtils.Channel;
import CommonUtils.Stoppable;

import java.io.*;

/**
 * Created by 14Malgavka on 05.05.2017.
 */


public class FileWriter implements Stoppable {


    private Thread thread;
    private volatile boolean isActive;
    private Channel<byte[]> writerChannel;

    public FileWriter(String fileName, Channel<byte[]> writerChannel, int lastPackageSize) {
        isActive = true;
        this.writerChannel = writerChannel;
        try {
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream("D://Download//" + fileName));
//            FileOutputStream fileOutputStream = new FileOutputStream("D://papka//tranfer - " + fileName);
            thread = new Thread(() -> {
                byte[] dataToWrite;
//                int counter = 0;
                while (isActive) {
                    try {
                        dataToWrite = this.writerChannel.take();
                        if (dataToWrite.length == 0)
                            break;
                        fileOutputStream.write(dataToWrite);
                        fileOutputStream.flush();
//                        System.out.println("Записано байт: " + dataToWrite.length);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        System.out.println("File writer has stopped");
                    }
                }
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    System.out.println("Cought " + e.getMessage());
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        thread.start();


    }

    @Override
    public void stop() {
        if (isActive) {
            this.writerChannel.put(new byte[0]);
            isActive = false;
        }
    }
}
//Закрывать по интерраптед эксепшену