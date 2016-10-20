package test3;
import java.util.concurrent.locks.ReentrantLock;

import popUtil.PoP_Util;

import java.util.concurrent.locks.Lock;

/* Test - 3:
    Two threads competing to enter into an 'if' block
    Includes lock, fork, join
    No symbolic writes
    Includes local variables and arithmetic/boolean operations
    Multiple shared variables
    
    Note: You may need to handle the XOR operation separately
*/
public class Main {

    static Lock lock1 = new ReentrantLock(); 
    static Lock lock2 = new ReentrantLock(); 
    static Integer shared_int_a = 1, shared_int_b = 1;
    static class MyThread extends Thread
    {

        @Override
        public void run() 
        {
            PoP_Util.randomDelay();
            
            Boolean modified = false;
            lock1.lock(); 
            int local_1 = shared_int_a + 2;
            if(local_1%2==1)
            /* Only one thread will be able to enter this */
            { 
                modified = true;
                shared_int_a = 2;
                System.err.println("Wrote shared_int_a");
            }
            else
            {
                System.err.println("Coudln't write shared_int_a");
            }
            lock1.unlock(); 
            
            if (!modified)
            {
                lock2.lock();
                int local_2 = shared_int_b;
                if((local_2^1) == 0)
                /* Only one thread will be able to enter this */
                {
                    modified = true;
                    shared_int_b = 2;
                    System.err.println("Wrote shared_int_b");
                }
                else
                {
                    System.err.println("Couldn't write shared_int_b");
                }
                lock2.unlock();
            }
            
        }
    }

	public static void main(String[] args) throws InterruptedException {

		MyThread t1 = new MyThread();
        MyThread t2 = new MyThread();
        MyThread t3 = new MyThread();

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();
		return;
	}

}
