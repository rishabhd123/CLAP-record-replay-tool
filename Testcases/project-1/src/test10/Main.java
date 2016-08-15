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

public class Main {

	public static void main(String[] args) {

		int a = args[0].length();

		for (int i = 1; i < 10; i++) {
			if (i % 5 == 0)
				System.exit(0);
			a++;
		}

		System.err.println(a);

		return;
	}

}