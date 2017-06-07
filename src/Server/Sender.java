package Server;

import CommonUtils.Channel;
import CommonUtils.Stoppable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Created by 14Malgavka on 05.05.2017.
 */
public class Sender implements Runnable, Stoppable {

    private DatagramSocket serverSocket;
    private InetSocketAddress clientAddress;
    private Channel<byte[]> senderChannel;
    private Thread thread;
    private volatile boolean isActive;

    public Sender(DatagramSocket serverSocket, InetSocketAddress clientAddress, Channel<byte[]> senderChannel) {
        this.serverSocket = serverSocket;
        this.clientAddress = clientAddress;
        this.senderChannel = senderChannel;
        isActive = true;
        thread = new Thread(this);
        thread.start();

    }

    @Override
    public void run() {
        while (isActive) {
            try {
                byte[] numberOfPackage = senderChannel.take();
                if (numberOfPackage.length == 0)
                    break;
                DatagramPacket packet = new DatagramPacket(numberOfPackage, numberOfPackage.length, clientAddress);
                serverSocket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println("Sender has stopped" + e.getMessage());
            }
        }

    }

    @Override
    public void stop() {
        if (isActive) {
            senderChannel.put(new byte[0]);
            isActive = false;
        }
    }
}
