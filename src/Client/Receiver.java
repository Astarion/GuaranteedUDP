package Client;

import CommonUtils.ClientCallBack;
import CommonUtils.Stoppable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by 14Malgavka on 05.05.2017.
 */
public class Receiver implements Stoppable {

    private final DatagramSocket clientSocket;
    private final ClientCallBack callBack;
    private Thread thread;
    private boolean isActive;

    public Receiver(DatagramSocket clientSocket, ClientCallBack callBack) {
        this.clientSocket = clientSocket;
        this.callBack = callBack;
        isActive = true;
        thread = new Thread(() -> {
//            try {
//                this.clientSocket.setSoTimeout(5000);
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
            while (isActive) {
                byte[] numbOfDatagram = new byte[Integer.BYTES];
                DatagramPacket receivedPacket = new DatagramPacket(numbOfDatagram, numbOfDatagram.length);
                try {
                    this.clientSocket.setSoTimeout(2500);
                    this.clientSocket.receive(receivedPacket);
                    this.callBack.onRead(numbOfDatagram);
                } catch (IOException e) {
                    stop();
                    break;
                }

            }
        });
        thread.start();
    }

    @Override
    public void stop() {
        if(isActive) {
            clientSocket.close();
            isActive = false;
        }
    }

    //Принимает от сервера дейтаграмы с номерами принятых дейтаграм
    //Sliding Window с помощью ресивера будем двигаться дальше, общий доступ с рисевера и с клиента.

}
