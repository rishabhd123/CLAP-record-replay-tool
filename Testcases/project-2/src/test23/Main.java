package test23;

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
 * Test 23: Explicit throw statement
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		if (x < 5)
			throw new RuntimeException("throw");

		System.err.println(x);

		return;
	}

}