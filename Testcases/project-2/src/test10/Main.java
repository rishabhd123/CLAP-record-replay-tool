package test10;

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
 * Test 10: Complicated loops and conditionals
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();
		int y = 0;

		if (x < 3) {
			if (x == 2)
				y = y + 1;
			else {
				if (x == 1)
					y = y + 2;
			}
		}

		while (x < 10) {
			if (x == 5) {
				x++;
				continue;
			}

			if (x == 9)
				break;

			x++;
		}

		if (x < 5) {
			while (x < 10) {
				if (x == 6)
					System.err.println(x);
				else if (x == 7)
					System.err.println(x + 1);
				else
					System.err.println(x - 1);
			}
			System.exit(0);
		}

		System.err.println(x);

		return;
	}

}