package test25;

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
 * Test 25: Static class initialization block
 */

public class Main {

	static int a, b;

	static {
		a = 10;
		if (b < 10)
			b++;
		else
			b--;
		System.exit(0);
	}

	public static void main(String[] args) {

		int x = args[0].length();

		try {
			x++;
		} catch (RuntimeException e) {
			x--;
		} finally {
			x += 10;
		}

		System.err.println(x);

		return;
	}

}
