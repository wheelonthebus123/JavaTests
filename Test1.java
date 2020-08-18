import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Test1 {
    public static void main(String[] args) {
        final BoundedBuffer bb = new BoundedBuffer();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("t1 run");
                for (int i = 0; i < 1; ++i) {
                    try {
                        System.out.println("putting..");
                        bb.put(Integer.valueOf(i));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("t2 run");
                for (int i = 0; i < 1; ++i) {
                    try {
                        System.out.println("taking..");
                        Object val = bb.take();
                        System.out.println(val);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        t1.start();
        t2.start();
    }

    static class BoundedBuffer {
        final ReentrantLock lock = new ReentrantLock();
        final Condition notFull = lock.newCondition();
        final Condition notEmpty = lock.newCondition();

        final Object[] items = new Object[1];
        int putptr, takeptr, count;

        public void put(Object x) throws InterruptedException {
            System.out.println("put wait lock...");
            lock.lock();
            System.out.println("put get lock" + ":lock count = " + lock.getHoldCount());
            try {
                while (count == items.length) {
                    System.out.println("buffer full, please wait");
                    notFull.await();
                }

                items[putptr] = x;
                if (++putptr == items.length) {
                    putptr = 0;
                }
                ++count;
                notEmpty.signal();
            } finally {
                lock.unlock();
                System.out.println("put release lock " + ":lock count = " + lock.getHoldCount());
            }
        }

        public Object take() throws InterruptedException {
            System.out.println("take wait lock...");
            lock.lock();
            System.out.println("take get lock" + ":lock count = " + lock.getHoldCount());
            try {
                while (count == 0) {
                    System.out.println("no elements, please wait");
                    for(int i=0; i<10; ++i){
                        System.out.println("take sleep "+i);
                        Thread.sleep(1000);
                    }
                    System.out.println("take waiting...");
                    notEmpty.await();//
                    System.out.println("lock count " + lock.getHoldCount());
                }
                Object x = items[takeptr];
                if (++takeptr == items.length) {
                    takeptr = 0;
                }
                --count;
                notFull.signal();
                return x;
            } finally {
                lock.unlock();
                System.out.println("take release lock " + ":lock count = " + lock.getHoldCount());
            }
        }
    }
}