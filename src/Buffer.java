import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Buffer<object> {
    private int CAPACITY;
    Object object;
    Buffer(int capacity){
        this.CAPACITY = capacity;
    }

    public LinkedList<object> queue = new LinkedList<>();


    // Create a new lock
    private static Lock lock = new ReentrantLock();

    // Create two conditions
    private static Condition notEmpty = lock.newCondition();
    private static Condition notFull = lock.newCondition();

    public void write(object value) {
        lock.lock(); // Acquire the lock
        try {
            while (queue.size() >= CAPACITY) {
               // System.out.println("Wait for notFull condition");
                notFull.await();
            }

            queue.add(value);
            notEmpty.signal(); // Signal notEmpty condition
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        finally {
            lock.unlock(); // Release the lock
        }
    }

    public object read() {
        object value = null;
        lock.lock(); // Acquire the lock
        try {
            while (queue.isEmpty()) {
                //System.out.println("\t\t\tWait for notEmpty condition");
                notEmpty.await();
            }

            value = queue.remove();
            notFull.signal(); // Signal notFull condition
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        finally {
            lock.unlock(); // Release the lock
            return value;
        }
    }
}