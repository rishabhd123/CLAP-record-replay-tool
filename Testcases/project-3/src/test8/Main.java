package test8;
import java.util.concurrent.locks.ReentrantLock;

import popUtil.PoP_Util;

import java.util.concurrent.locks.Lock;

/* Test - 8:
    More Thread Fork Recursion
*/
public class Main {

    static Lock lock = new ReentrantLock();
    static Integer shared_int_a = 10; /* Total 10 forks are possible */
    static class MyThread extends Thread
    {
        @Override
        public void run() 
        {
        
            for (int i = 0; i < 2; i++)
            {
                PoP_Util.randomDelay();
                
                Boolean forkNew = true;
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
                    System.err.println(Thread.currentThread().getName()+" Starting new thread");
                    t3.start();
                }
                else
                {
                    System.err.println(Thread.currentThread().getName()+" shared_int_a exhausted");
                }
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

        
		return;
	}

}
