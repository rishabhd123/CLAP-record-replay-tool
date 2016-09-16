package test15;

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
 * Test 15: Function overloading
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		if (x > 1)
			foo();

		for (int i = 0; i < 10; i++) {
			if (i % 2 == 0)
				foo(x);
			else
				foo(x, i);
		}

		return;
	}

	public static void foo() {
		return;
	}

	public static void foo(int x) {
		if (x < 10)
			return;
		else
			return;
	}

	public static void foo(int x, int y) {
		if (x < y)
			return;
		else
			return;
	}

}