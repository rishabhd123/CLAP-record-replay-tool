package test11;

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
 * Test 11: Simple function call
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		if (x > 3)
			bar();
		else
			bar();

		System.err.println(x);

		return;
	}

	private static void bar() {
		return;
	}

}