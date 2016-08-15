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

public class Main {

	public static void main(String[] args) {

		int a = args[0].length();

		int i = 0;
		while (a < 100) {
			a++;
			if (a % 2 == 0)
				i++;
		}

		System.err.println(a + i);

		return;
	}

}