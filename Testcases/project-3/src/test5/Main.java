package test5;
import java.util.concurrent.locks.ReentrantLock;

import popUtil.PoP_Util;

import java.util.concurrent.locks.Lock;

/* Test - 5:
    Two threads competing to enter into an 'if' block
    Includes fork, join
    No symbolic writes
    Without lock
*/
public class Main {

    static Integer shared_int_a = 1;
    static class MyThread extends Thread
    {
        @Override
        public void run() 
        {
            PoP_Util.randomDelay();
            
            if(shared_int_a == 1)
            /* Only one thread should be able to enter this, but as there are no locks, all thread can */
            { 
                System.err.println("Incremented shared_int_a");
                shared_int_a++;
            }
            else
            {
                System.err.println("Coudldn't increment shared_int_a");
            }
            
        }
    }

	public static void main(String[] args) throws InterruptedException {

		MyThread t1 = new MyThread();
        MyThread t2 = new MyThread();

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        
        if(shared_int_a == 3)
        {
            System.err.println("Both threads incremented the shared int");
        }
        
		return;
	}

}
