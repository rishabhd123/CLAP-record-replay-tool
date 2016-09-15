package test9;

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
 * Test 9: Nested while loops
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();
		int y = 0;

		while (x < 10) {
			y = 0;
			while (y < 5)
				y++;
			x++;
		}

		System.err.println(x);

		return;
	}

}