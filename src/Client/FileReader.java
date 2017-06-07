package Client;

import CommonUtils.Channel;
import CommonUtils.Stoppable;

import java.io.*;

/**
 * Created by 14Malgavka on 05.05.2017.
 */
//Просто читает файл и складывает его в буффер, чтобы потом передать сендеру
public class FileReader implements Stoppable {

    private Thread thread;
    private Channel<byte[]> channel;
    private boolean isActive;

    public FileReader(String fileName, int packageSize, Channel<byte[]> channel) {
        isActive = true;
        this.channel = channel;
        thread = new Thread(() -> {
            try {
                byte[] data = new byte[packageSize];
                int readLength;

                BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(fileName));
                while (isActive) {
                    readLength = fileInputStream.read(data);

                    if (readLength == -1) {
                        stop();
                        break;
                    }

                    if (readLength < packageSize) {
                        byte[] finalData = new byte[readLength];
                        System.arraycopy(data, 0, finalData, 0, finalData.length);
                        this.channel.put(finalData.clone());
                    } else {
                        this.channel.put(data.clone());
                    }
                }
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                System.out.println("Wrong file name");
                System.exit(-1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    @Override
    public void stop() {
        if (isActive)
            isActive = false;
    }
}
