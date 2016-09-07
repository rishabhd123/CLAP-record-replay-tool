package test3;

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
 * Test 3: Nested if-else
 */

public class Main {

	public static void main(String[] args) {

		int a = args[0].length();

		if (a > 5) {
			if (a > 10)
				a++;
			else
				a--;
		} else {
			if (a == 0)
				a--;
		}

		System.err.println(a);

		return;
	}

}