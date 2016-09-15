package test17;

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
 * Test 17: Indirect recursion
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		foo(x);

		System.err.println(x);

		return;
	}

	public static void foo(int x) {

		if (x < 8)
			bar(x + 1);

		return;
	}

	public static void bar(int x) {

		if (x < 7)
			foo(x + 1);

		return;
	}

}