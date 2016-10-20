package test1;

import java.util.concurrent.locks.ReentrantLock;

import popUtil.PoP_Util;

import java.util.concurrent.locks.Lock;
import java.lang.Thread;

/* Test - 1:
    Two threads competing to enter into an 'if' block
    Includes lock, fork, join
    No symbolic writes
*/
public class Main {

	static Lock lock = new ReentrantLock();
	static Integer shared_int_a = 1;

	static class MyThread extends Thread {
		@Override
		public void run() {
			PoP_Util.randomDelay(); /*
									 * You need to skip over this in your
									 * analysis
									 */

			lock.lock();
			if (shared_int_a == 1)
			/* Only one thread will be able to enter this */
			{
				System.err.println(Thread.currentThread().getId());
				shared_int_a = 2;
				System.err.println("Wrote shared_int_a");
			} else {
				System.err.println("Couldn't write shared_int_a");
			}

			lock.unlock();
		}
	}

	public static void main(String[] args) throws InterruptedException {

		MyThread t1 = new MyThread();
		MyThread t2 = new MyThread();

		t1.start();
		t2.start();

		t1.join();
		t2.join();

		return;
	}

}
