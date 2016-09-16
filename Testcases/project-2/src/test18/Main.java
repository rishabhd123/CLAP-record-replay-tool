package test18;

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
 * Test 18: Exceptions
 */

public class Main {

	public static void main(String args[]) {

		int x = args[0].length();

		try {
			try {
				int b = x / 0;
				System.err.println(b);
			} catch (ArithmeticException e1) {
				if (x < 5)
					x++;
				else
					x--;
				System.err.println("Exception: e1");
			}
			try {
				int b = args[x].length();
				System.err.println(b);
			} catch (ArrayIndexOutOfBoundsException e2) {
				System.err.println("Exception: e2");
			}
		} catch (ArithmeticException e3) {
			x++;
		} catch (ArrayIndexOutOfBoundsException e4) {
			x--;
		} catch (Exception e5) {
			x += 10;
		} finally {
			System.err.println("Finally");
		}

		System.err.println(x);

		return;
	}

}