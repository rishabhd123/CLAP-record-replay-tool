package test14;

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

		foo(a);

		System.err.println(a);

		return;
	}

	public static void foo(int a) {

		if (a % 2 == 0)
			System.exit(1);

		return;
	}

}