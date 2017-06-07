package CommonUtils;

/**
 * Created by Alex on 30.05.2017.
 */
public class CyclicBuffer<E> {

    private Object[] buffer;

    private int takeIndex;
    private int putIndex;
    private int count;
    public final int size;

    private final Object lock = new Object();

    public CyclicBuffer(int size) {
        this.size = size;
        buffer = new Object[this.size];
        putIndex = 0;
        takeIndex = 0;
    }

    public boolean isFull() {
        synchronized (lock) {
            return (size == count);
        }
    }

    public boolean isEmpty() {
        return (count == 0);
    }

    public int getTakeIndex(){
        return takeIndex;
    }

    public void put(E element) {
        synchronized (lock) {
            try {
                while (isFull())
                    lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            buffer[putIndex] = element;
            count++;
            if ((++putIndex) == size)
                putIndex = 0;
            lock.notifyAll();
        }
    }

    public E removeFirst() {
        synchronized (lock) {
            try {
                while (isEmpty())
                    lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            @SuppressWarnings("unchecked")
            E element = (E) buffer[takeIndex];
            buffer[takeIndex] = null;
            if (++takeIndex == buffer.length)
                takeIndex = 0;
            count--;
            lock.notifyAll();
            return element;
        }
    }

    public E getFirst() {
        synchronized (lock) {
            try {
                while (isEmpty())
                    lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return (E) buffer[takeIndex];
        }
    }

    public void set(int index, E element) {
        synchronized (lock) {
            if (index >= buffer.length)
                throw new IndexOutOfBoundsException();
            if (buffer[index] == null)
                count++;
            buffer[index] = element;
        }
    }

    public E elementAt(int index) {
        synchronized (lock) {
            return (E) buffer[index];
        }
    }


}
