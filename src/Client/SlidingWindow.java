package Client;

import CommonUtils.CyclicBuffer;
import CommonUtils.PartOfFile;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by 14Malgavka on 05.05.2017.
 */

public class SlidingWindow {

    private CyclicBuffer<PartOfFile> window;

    public SlidingWindow(int size) {
        window = new CyclicBuffer<>(size);
    }

    public int size(){
        return window.size;
    }

    public boolean isFull() {
        return window.isFull();
    }

    public boolean isEmpty() {
        return window.isEmpty();
    }

    public void addPacket(PartOfFile partOfFile) {
        window.put(partOfFile);
//      System.out.println("Timer для " + partOfFile.getPackageNumber() + " запущен в SlidingWindow");
        partOfFile.startTimer();
    }

    //Возвращает первый элемент, но не удаляет его из очереди
    public PartOfFile getFirst() {
        if (window.elementAt(window.getTakeIndex()) == null)
            return null;
        return window.getFirst();
    }

    //Возвращает первый элемент и удаляет его из очереди
    public PartOfFile removeFirst() {
        return window.removeFirst();
    }

    public PartOfFile get(int index){
        return window.elementAt(index);
    }

    @Override
    public String toString() {
        return "SlidingWindow{" +
                "window=" + window +
                '}';
    }
}
