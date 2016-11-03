/*package test4;
import java.util.concurrent.locks.ReentrantLock;

import popUtil.PoP_Util;

import java.util.concurrent.locks.Lock;
*/
/* Test - 4:
    Threads competing to enter into an 'if' block
    Includes lock, fork, join
    Includes symbolic writes
*/
/*
public class Main {

    static Lock lock = new ReentrantLock();
    static Integer shared_int_a = 1;
    static class MyThread extends Thread
    {

        @Override
        public void run() 
        {
            PoP_Util.randomDelay();
            
            lock.lock(); 
            if(shared_int_a <= 2)
            // Only two threads will be able to enter this 
            { 
                PoP_Util.registerEvent (1);
                System.err.println(Thread.currentThread().getName()+" Incremented shared_int_a");
                shared_int_a += 1; // Need a symbolic write here 
            }
            else
            {
                PoP_Util.registerEvent (2);
                System.err.println(Thread.currentThread().getName()+" Couldn't increment shared_int_a");
            }
            lock.unlock(); 
        }
    }

	public static void main(String[] args) throws InterruptedException {

		MyThread t1 = new MyThread();
        MyThread t2 = new MyThread();
        MyThread t3 = new MyThread();
        
        PoP_Util.registerFork(t1);
		t1.start();
		
		PoP_Util.registerFork(t2);
		t2.start();
		
		PoP_Util.registerFork(t3);
		t3.start();

        t1.join();
        t2.join();
        t3.join();
        
		return;
	}

}*/

package test4;

/*
 * @author Sridhar Gopinath		-		g.sridhar53@gmail.com
 * 
 * Course project,
 * Principles of Programming Course, Fall - 2016,
 * Computer Science and Automation (CSA),
 * Indian Institute of Science (IISc),
 * Bangalore
 */

/*
 * Test 14: Multiple function calls inside loop
 * Example taken from Casper paper:
 * Wu, Rongxin, et al. "Casper: an efficient approach to call trace collection." ACM SIGPLAN Notices 51.1 (2016): 678-690.
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		A();

		int i = 0;
		do {
			if (i % 2 == 0)
				B(i);
			else
				E(i);
			i++;
		} while (i < 10);

		H();
		
		System.err.println(x);

		return;
	}

	public static void A() {
		return;
	}

	public static void B(int x) {
		if (x > 5)
			C();
		else
			D();
		return;
	}

	public static void C() {
		return;
	}

	public static void D() {
		return;
	}

	public static void E(int x) {
		if (x > 5)
			C();
		else
			D();
		return;
	}

	public static void H() {
		return;
	}

}
