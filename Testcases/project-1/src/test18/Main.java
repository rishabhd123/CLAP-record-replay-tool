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

public class Main {

	public static void main(String[] args) {

		int a = args[0].length();

		for (int i = 0; i < 3; i++)
			foo(a);

		System.err.println(a);

		return;
	}

	public static void foo(int a) {
		for (int i = 0; i < 3; i++)
			bar(a);
		return;
	}

	public static void bar(int a) {
		for (int i = 0; i < 3; i++)
			a++;
		return;
	}

}