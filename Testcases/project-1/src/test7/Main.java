package test7;

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
 * Test 7: Do-while loop with a **very large** input
 */

public class Main {

	public static void main(String[] args) {

		int a = args[0].length();

		long i = 0, j = 0;
		do {
			if (i % 10 == 0)
				j++;
			i++;
		} while (i < 5000000000L);

		System.err.println(a + j);

		return;
	}

}