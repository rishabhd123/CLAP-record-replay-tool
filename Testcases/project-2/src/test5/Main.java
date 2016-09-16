package test5;

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
 * Test 5: Single block, multiple exits
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		System.exit(0);

		System.err.println(x);

		return;
	}

}