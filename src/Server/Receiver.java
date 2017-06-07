package Server;

import CommonUtils.Channel;
import CommonUtils.Stoppable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

/**
 * Created by 14Malgavka on 05.05.2017.
 */
public class Receiver implements Runnable, Stoppable {

    private DatagramSocket serverSocket;
    private final int PACKAGE_SIZE;
    private int lastPackageSize;
    private Channel<byte[]> writerChannel;
    private Channel<byte[]> senderChannel;
    private Thread thread;
    private long packageNumber;
    private Sender serverSender;
    private FileWriter fileWriter;
    private int currentPackageNumber = 0;
    private final static Object lock = new Object();
    private ServerSlidingWindow window;



    public Receiver(DatagramSocket serverSocket, int packageSize, int lastPackageSize, Channel<byte[]> writerChannel, Channel<byte[]> senderChannel,
                    long packageNumber, Sender serverSender, FileWriter fileWriter) {
        this.serverSocket = serverSocket;
        PACKAGE_SIZE = packageSize;
        this.lastPackageSize = lastPackageSize;
        this.writerChannel = writerChannel;
        this.senderChannel = senderChannel;
        this.packageNumber = packageNumber;
        this.serverSender = serverSender;
        this.fileWriter = fileWriter;
        window = new ServerSlidingWindow(10);
        thread = new Thread(this);
        thread.start();
    }

    public static byte[] toByte(int number) {
        return ByteBuffer.allocate(4).putInt(number).array();
    }

    public static int toInt(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).getInt();
    }


    @Override
    public void run() {

        byte[] receivedData = new byte[PACKAGE_SIZE];
        byte[] writerData = new byte[PACKAGE_SIZE - Integer.BYTES];
        byte[] byteNumberOfPackage;
        DatagramPacket packet = new DatagramPacket(receivedData, PACKAGE_SIZE);
        int intNumberOfPackage;
        try {
            synchronized (lock) {
                while (currentPackageNumber < packageNumber) {
                    serverSocket.receive(packet);
                    byteNumberOfPackage = new byte[Integer.BYTES];
                    System.arraycopy(receivedData, 0, byteNumberOfPackage, 0, byteNumberOfPackage.length);

                    intNumberOfPackage = toInt(byteNumberOfPackage);
//                    System.out.println("Принят " + intNumberOfPackage);
//                    System.out.println("Текущий  номер пакета: " + currentPackageNumber);

                    if (intNumberOfPackage == packageNumber - 1) {
                        senderChannel.put(byteNumberOfPackage);
                        byte[] lastPackageData = new byte[lastPackageSize];
                        System.arraycopy(receivedData, 4, lastPackageData, 0, lastPackageData.length);
                        window.set(intNumberOfPackage, lastPackageData.clone());
                        tryToWrite();
                        continue;
                    }
                    System.arraycopy(receivedData, 4, writerData, 0, writerData.length);

                    if (window.get(intNumberOfPackage) != null || intNumberOfPackage < currentPackageNumber)
                        continue;

                    window.set(intNumberOfPackage, writerData.clone());
                    tryToWrite();
                    senderChannel.put(byteNumberOfPackage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Receiver has stopped");
        }

        try {
            thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stop();
//        senderChannel.put(new byte[0]);
    }

    private void tryToWrite(){
        synchronized (lock){
            while (window.getFirst() != null) {
                writerChannel.put(window.removeFirst().clone());
                currentPackageNumber++;
            }
        }
    }

    @Override
    public void stop() {
        serverSocket.close();
        serverSender.stop();
        fileWriter.stop();
    }
}