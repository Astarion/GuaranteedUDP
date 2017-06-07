package Client;

import CommonUtils.Channel;
import CommonUtils.Stoppable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Created by 14Malgavka on 05.05.2017.
 */

public class ClientSender implements Stoppable {

    private Channel<byte[]> channel;
    private final DatagramSocket clientSocket;
    private final InetSocketAddress serverAddress;
    private final Thread thread;
    private boolean isActive;

    public ClientSender(Channel<byte[]> channel, DatagramSocket clientSocket, InetSocketAddress serverAddress) {
        this.channel = channel;
        this.clientSocket = clientSocket;
        this.serverAddress = serverAddress;
        isActive = true;
        thread = new Thread(() -> {
            while (isActive) {
                try {
                    byte[] pieceOfData = this.channel.take();
                    if (pieceOfData.length == 0) {
                        break;
                    }
                    DatagramPacket datagramPacket = new DatagramPacket(pieceOfData, pieceOfData.length, this.serverAddress);
                    this.clientSocket.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    System.out.println("Interrupted exception in clientSender");
                }
            }
        });
        thread.start();
    }

//    @Override
//    public void run() {
//        while (true) {
//            try {
//                byte[] pieceOfData = channel.take();
////            window.addPacket(pieceOfData);
//                DatagramPacket datagramPacket = new DatagramPacket(pieceOfData, pieceOfData.length, serverAddress);
//                clientSocket.send(datagramPacket);
//                System.out.println(pieceOfData.length);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                System.out.println("Interrupted exception in clientSender");
//            }
//        }
//    }

    public void resend(byte[] pieceOfData) {
        try {
            if (isActive) {
                DatagramPacket datagramPacket = new DatagramPacket(pieceOfData, pieceOfData.length, serverAddress);
                clientSocket.send(datagramPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (isActive) {
            //Если поток ожидает элемента в канале
            this.channel.put(new byte[0]);
            isActive = false;
        }

    }

    //Только отправляет готовые пакеты!

    //Читает из буффера- NO

    //Преобразовывает(добавить номер пакета...)
    //Превращаем в DataGram
    //Отправляем DataGram серверу
    //Channel(<PartOfFile>)
}
