package CommonUtils;

import java.util.LinkedList;

public class Channel<E> {
    private final int maxCounter;
    private final LinkedList<E> queue = new LinkedList<>();
    private final Object lock = new Object();

    public Channel(int maxCounter) {
        this.maxCounter = maxCounter;
    }

    public void put(E x) {
        synchronized (lock) {
            try {
                while (queue.size() >= maxCounter)
                    lock.wait();
                queue.addLast(x);
                lock.notifyAll();
            } catch (InterruptedException e) {
                System.out.println("Interrupted \n" + e.getMessage());
            }
        }
    }

    public E take() throws InterruptedException {
        synchronized (lock) {
            while (queue.isEmpty())
                lock.wait();
            lock.notifyAll();
            return queue.removeFirst();
        }
    }

    public Integer getSize() {
        synchronized (lock) {
            return queue.size();
        }
    }
}
