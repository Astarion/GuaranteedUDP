package Server;

import CommonUtils.Channel;
import CommonUtils.InitPackage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by 14Malgavka on 05.05.2017.
 */
public class Server {
    private static int serverPort;
    private static final int PACKAGE_SIZE = 2048;

    public static void main(String[] args) {
        try {
            serverPort = Integer.parseInt(args[0]);
            int clientPort = Integer.parseInt(args[1]);
            final int CHANNEL_THROUGHPUT = Integer.parseInt(args[2]);

            DatagramSocket datagramSocket = new DatagramSocket(serverPort);
            byte[] array = new byte[PACKAGE_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(array, PACKAGE_SIZE);
            datagramSocket.receive(datagramPacket);

            FileOutputStream fileOutputStream = new FileOutputStream("Received Package");
            fileOutputStream.write(array);
            fileOutputStream.close();

            InitPackage initPackage;
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("Received Package"));
            initPackage = (InitPackage) objectInputStream.readObject();
//            System.out.println(initPackage);
            objectInputStream.close();

            Channel<byte[]> writerChannel = new Channel<>(CHANNEL_THROUGHPUT);
            Channel<byte[]> senderChannel = new Channel<>(CHANNEL_THROUGHPUT);
            Sender sender = new Sender(datagramSocket, new InetSocketAddress("localhost", clientPort), senderChannel);
            FileWriter fileWriter = new FileWriter(initPackage.getFileName(), writerChannel, initPackage.getLastPackageSize());
            Receiver receiver = new Receiver(datagramSocket, PACKAGE_SIZE, initPackage.getLastPackageSize(), writerChannel, senderChannel, initPackage.getPackageCount(), sender, fileWriter);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                receiver.stop();
                sender.stop();
                fileWriter.stop();
            }));

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
