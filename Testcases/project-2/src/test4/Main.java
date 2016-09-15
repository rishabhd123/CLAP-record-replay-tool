package test4;

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
 * Test 4: Multiple exit points
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();
		int y = 0;

		if (x > 3) {
			if (x > 5)
				System.exit(0);
			else {
				if (x == 4)
					y = y + 2;
			}
		} else
			return;

		y = y + 3;

		if (x == 3)
			y = y + 4;

		if (x == 4)
			System.exit(0);
		else
			y += 3;

		y = y + 5;

		System.err.println(y);

		return;
	}

}