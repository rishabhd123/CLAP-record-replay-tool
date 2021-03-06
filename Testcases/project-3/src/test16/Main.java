package test16;

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
 * Test 12: Multiple function calls
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		if (x > 3) {
			if (x > 5)
				bar();
			foo(x);
		}

		System.err.println(x);

		return;
	}

	public static void foo(int x) {
		if (x == 0)
			return;
		else {
			bar();
			return;
		}
	}

	public static void bar() {
		return;
	}

}
