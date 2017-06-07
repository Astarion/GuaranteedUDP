package Server;

import CommonUtils.CyclicBuffer;

/**
 * Created by Alex on 31.05.2017.
 */
public class ServerSlidingWindow {

    private CyclicBuffer<byte[]> writeWindow;
    private static final Object lock = new Object();

    public ServerSlidingWindow(int size) {
        writeWindow = new CyclicBuffer<>(size);
    }

    public void set(int index, byte[] data) {
        synchronized (lock) {
            writeWindow.set(index % writeWindow.size, data);
        }
    }

    public byte[] get(int index) {
        synchronized (lock) {
            return writeWindow.elementAt(index % writeWindow.size);
        }
    }

    public byte[] getFirst() {
        synchronized (lock) {
            if (writeWindow.elementAt(writeWindow.getTakeIndex()) == null)
                return null;
            return writeWindow.getFirst();
        }
    }

    public byte[] removeFirst() {
        synchronized (lock) {
            return writeWindow.removeFirst();
        }
    }

}
