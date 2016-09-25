package test24;

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
 * Test 24: **Large** loop
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		int b = 0;
		while (b < 1000)
			b++;

		System.err.println(x);

		return;
	}

}