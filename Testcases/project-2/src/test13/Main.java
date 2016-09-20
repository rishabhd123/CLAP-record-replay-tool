package test13;

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
 * Test 13: Function call inside loop
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		for (int i = x; i < 10; i++) {
			if (i % 2 == 0)
				foo();
			else
				System.err.println(x);
		}

		System.err.println(x);

		return;
	}

	public static void foo() {
		return;
	}

}