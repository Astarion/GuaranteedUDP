package Client;

import CommonUtils.*;


import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by 14Malgavka on 05.05.2017.
 */
public class Client implements ClientCallBack, Runnable, Stoppable {

    private Channel<byte[]> readerChannel;
    private Channel<byte[]> senderChannel;

    private String address;
    private int clientPort;
    private int serverPort;
    private String fileName;
    private static final int PACKAGE_SIZE = 2048;
    private static final int CHANNEL_THROUGHPUT = 10240;
    private int packageCounter = 0;
    private long packageNumber;
    private SlidingWindow window;
    private FileReader fileReader;
    private ClientSender clientSender;
    private Receiver receiver;
    private Thread thread;

    private final static Object lock = new Object();


    public Client(String address, int clientPort, int serverPort, String fileName, int slidingWindowSize) {

        readerChannel = new Channel<>(CHANNEL_THROUGHPUT);
        senderChannel = new Channel<>(CHANNEL_THROUGHPUT);
//        System.out.println("Reader Channel: " + readerChannel.getSize() + "\nSender Channel: " + senderChannel.getSize());
        this.address = address;
        this.clientPort = clientPort;
        this.serverPort = serverPort;
        this.fileName = fileName;
        window = new SlidingWindow(slidingWindowSize);
        this.thread = new Thread(this);
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
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("InitPackage"));
            File fileToTransfer = new File(fileName);
            long fileSize = fileToTransfer.length();
            fileReader = new FileReader(fileName, PACKAGE_SIZE - 4, readerChannel);

            packageNumber = (long) Math.ceil((double) fileSize / (PACKAGE_SIZE - Integer.BYTES));
            int lastPackageSize = (int) (fileSize % (PACKAGE_SIZE - Integer.BYTES));
            InitPackage initPackage = new InitPackage(fileSize, lastPackageSize, fileName, packageNumber);

            objectOutputStream.writeObject(initPackage);
            objectOutputStream.flush();
            objectOutputStream.close();
            FileInputStream fileInputStream = new FileInputStream("InitPackage");
            byte[] bytePacket = new byte[PACKAGE_SIZE];
            byte[] byteReader;
            fileInputStream.read(bytePacket);
            fileInputStream.close();
            DatagramSocket datagramSocket = new DatagramSocket(clientPort);
            InetSocketAddress serverAddress = new InetSocketAddress(address, serverPort);
            DatagramPacket datagramPacket = new DatagramPacket(bytePacket, bytePacket.length, serverAddress);
            datagramSocket.send(datagramPacket);


            receiver = new Receiver(datagramSocket, this);
            clientSender = new ClientSender(senderChannel, datagramSocket, serverAddress);

            while (packageCounter < packageNumber) {
                byteReader = readerChannel.take();
                if (byteReader.length < (PACKAGE_SIZE - 4))
                    bytePacket = new byte[byteReader.length + 4];
                System.arraycopy(toByte(packageCounter), 0, bytePacket, 0, 4);
                System.arraycopy(byteReader, 0, bytePacket, 4, byteReader.length);
                PartOfFile partOfFile = new PartOfFile(byteReader, packageCounter, this);
                window.addPacket(partOfFile);
//                System.out.println("Отправлен " + packageCounter + " пакет");
                packageCounter++;
                senderChannel.put(bytePacket.clone());
            }
        } catch (InterruptedException e) {
            System.out.println("readerChannel waits too long");
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            System.exit(-1);
        } catch (IllegalArgumentException e) {
            System.out.println("Exception: " + e.getMessage());
            System.exit(-2);
        }

        stop();
//        senderChannel.put(new byte[0]);
//        //TODO преобразование в поток байтов
//        //TODO с помощью сериализации перевод в байты и обратно
//        //Читает из буффера
//
//        //Преобразовывает(добавить номер пакета...)
//        //Превращаем в DataGram
//        //Отправляет ClientSender'у
//
//        //TODO Интерфейс CallBack лдя клиента или клиентридера, непонятно ничего. Почитать
//        /*
//        Interface CallBack
//        {
//            void onRead(byte[])
//         }
//         FileReader(CallBack callBack)
//         {
//            read();
//            callBack.onRead(byte[]);
//         }
//
//
//         Client:
//         FileReader(this::onRead)
//         void onRead(byte[])
//         */
//        //TODO Класс Timer реализовать TimeOut для переотправки пакетов, если сервер не сказал, что получил пакет.
    }

    @Override
    public void onRead(byte[] numbOfDatagram) {
//        if (packageCounter >= packageNumber)
//            return;
        int numberOfPacket = toInt(numbOfDatagram);
//        System.out.println("Подтверждена отправка " + numberOfPacket);
        for (int i = 0; i < window.size(); i++) {
            if (window.get(i) != null &&
                    window.get(i).getPackageNumber() == numberOfPacket) {
                window.get(i).setReceived();
                break;
            }
        }

        while (window.getFirst() != null && window.getFirst().isReceived())
            window.removeFirst();

    }

    @Override
    public void resend(PartOfFile partOfFile) {
//        System.out.println("Переотправляю пакет номер: " + partOfFile.getPackageNumber());
        byte[] bytePacket;
        if (partOfFile.getData().length < PACKAGE_SIZE - 4)
            bytePacket = new byte[partOfFile.getData().length + 4];
        else
            bytePacket = new byte[PACKAGE_SIZE];
        System.arraycopy(toByte(partOfFile.getPackageNumber()), 0, bytePacket, 0, 4);
        System.arraycopy(partOfFile.getData(), 0, bytePacket, 4, partOfFile.getData().length);
        clientSender.resend(bytePacket);
    }

    @Override
    public void stop() {
        try {
            thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        packageNumber = -1;
        fileReader.stop();
        clientSender.stop();
    }
}
