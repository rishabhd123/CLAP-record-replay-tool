package test5;

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

		for (int i = 0; i < a; i++) {
			if (i % 2 == 0)
				i++;
			else
				a--;
		}
		System.err.println(a);

		return;
	}

}