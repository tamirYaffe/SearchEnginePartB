package SearchEngineTools;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * A template class for Concurrent FIFO Buffer for producer-consumer design pattern.
 * @param <T> - the buffer objects type.
 */
public class ConcurrentBuffer<T> {
    /**
     * semaphoreEmpty- semaphore for empty queue.
     * semaphoreFull- semaphore for maxSize queue.
     * concurrentLinkedQueue- a Concurrent queue for handling objects.
     */
    private Semaphore semaphoreEmpty;
    private Semaphore semaphoreFull;
    private ConcurrentLinkedQueue<T> concurrentLinkedQueue;

    /**
     * a constructor that initialize the max size of objects in the buffer.
     * @param maxSize- the input max size of objects in the buffer.
     */
    public ConcurrentBuffer(int maxSize) {
        semaphoreEmpty =new Semaphore(0);
        semaphoreFull=new Semaphore(maxSize);
        concurrentLinkedQueue=new ConcurrentLinkedQueue<>();
    }

    /**
     * Adds the input object to the buffer if the buffer is not full, otherwise go to sleep until add will be possible.
     * @param object- object to add to the buffer.
     */
    public void add(T object){
        try {
            semaphoreFull.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        concurrentLinkedQueue.add(object);
        semaphoreEmpty.release();
    }

    /**
     * Get and remove next object from the buffer(FIFO method) if exists, otherwise sleep until get will be possible..
     * @return the next object from the buffer.
     */
    public T get(){
        try {
            semaphoreEmpty.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        T pooled= concurrentLinkedQueue.poll();
        semaphoreFull.release();
        return pooled;
    }
}
