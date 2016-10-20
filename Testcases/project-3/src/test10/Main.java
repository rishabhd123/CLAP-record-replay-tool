package test10;
import java.util.concurrent.locks.ReentrantLock;

import popUtil.PoP_Util;

import java.util.concurrent.locks.Lock;

/* Test - 10:
    Two threads competing to enter into an 'if' block
    Includes lock, fork
    Symbolic writes
    
    Multiple methods
    Method will also take arguments and return a value
    
*/
public class Main {

    static Lock lock = new ReentrantLock();
    static Integer shared_int_a = 1;
    static Integer shared_int_b = 2;
    static void incrementShared (int incrementCount)
    {
        lock.lock(); 
        if(shared_int_a == 1)
        /* Only one thread will be able to enter this */
        { 
            
            System.err.println("Wrote shared_int_a");
            shared_int_a += incrementCount;
        }
        else
        {
            System.err.println("Couldn't write shared_int_a");
        }
        lock.unlock(); 
    }
    static Integer getIncrementCount (int a)
    {
        return a*2 + 10;
    }
    static class MyThread extends Thread
    {
        
        @Override
        public void run() 
        {
            PoP_Util.randomDelay();
            
            int local_int_a = shared_int_b + 4;
            incrementShared(getIncrementCount(local_int_a));
        }
    }

	public static void main(String[] args) throws InterruptedException {

		MyThread t1 = new MyThread();
        MyThread t2 = new MyThread();

        t1.start();
        t2.start();

        incrementShared(getIncrementCount(4));
        
		return;
	}

}
