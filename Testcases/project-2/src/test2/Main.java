package test2;

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
 * Test 2: Complex if-else conditionals
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();
		int y = 0;

		if (x > 3) {
			if (x > 5)
				y = y + 1;
			else {
				if (x == 4)
					y = y + 2;
			}
		} else
			y = y + 1;

		y = y + 3;

		if (x == 3)
			y = y + 4;

		if (x == 4)
			y += 2;
		else
			y += 3;

		y = y + 5;

		System.err.println(y);

		return;
	}

}