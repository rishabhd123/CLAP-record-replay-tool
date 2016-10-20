package test6;
import java.util.concurrent.locks.ReentrantLock;

import popUtil.PoP_Util;

import java.util.concurrent.locks.Lock;

/* Test - 6:
    Thread Fork Recursion
*/
public class Main {

    static Lock lock = new ReentrantLock();
    static Integer shared_int_a = 3;
    static class MyThread extends Thread
    {
        @Override
        public void run() 
        {
            PoP_Util.randomDelay();
            
            Boolean forkNew = false;
            lock.lock(); 
            
            shared_int_a--;
            if(shared_int_a <= 0)
            { 
                forkNew = false;    
            }
            lock.unlock(); 
            if (forkNew)
            {
                MyThread t3 = new MyThread();
                System.err.println("Forked new thread");
                t3.start();
                
            }
            else
            {
                System.err.println("shared_int_a exhausted");
            }
            
        }
    }

	public static void main(String[] args) throws InterruptedException {

		MyThread t1 = new MyThread();

        t1.start();

        t1.join();
        
		return;
	}

}
