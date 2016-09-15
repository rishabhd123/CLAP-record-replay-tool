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
 * Test 16: Direct recursion
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		recursion(x);

		System.err.println(x);

		return;
	}

	public static int recursion(int x) {

		if (x == 0)
			return 0;

		int b = x * x;
		int a = recursion(x - 1);

		return a + b;
	}

}