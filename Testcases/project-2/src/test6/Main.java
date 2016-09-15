package test6;

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
 * Test 6: while loop
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		while (x < 7)
			x++;

		System.err.println(x);

		return;
	}

}