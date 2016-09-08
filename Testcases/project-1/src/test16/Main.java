package test16;

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
 * Test 6: Inheritance
 */

public class Main {

	public static void main(String[] args) {

		int a = args[0].length();

		Subclass obj = new Subclass();
		obj.bar(a);

		System.err.println(a);

		return;
	}

	public void foo(int a) {
		if (a == 2)
			return;
		return;
	}

}