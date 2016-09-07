package test19;

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
 * Test 19: Break and Continue inside a loop
 */

public class Main {

	public static void main(String[] args) {

		int a = args[0].length();

		for (int i = 0; i < a; i++) {
			if (i % 2 == 0)
				continue;
			if (i % 3 == 0)
				break;
		}

		System.err.println(a);

		return;
	}

}