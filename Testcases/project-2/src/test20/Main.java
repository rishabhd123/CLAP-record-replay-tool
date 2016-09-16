package test20;

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
 * Test 20: Multiple constructors
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		Main obj = null;
		if (x < 5)
			obj = new Main();
		else if (x > 5)
			obj = new Main(x);
		else
			obj = new Main(x, 10);

		obj.foo();

		return;
	}

	int a, b;

	public Main() {
		a = b = 10;
		return;
	}

	public Main(int x) {
		if (x == 5)
			a = b = 10;
		else
			a = b = x;
		return;
	}

	public Main(int x, int y) {
		for (int i = 0; i < y; i++) {
			a += x;
			b += x;
		}
		return;
	}

	public void foo() {
		System.err.println(a);
		System.err.println(b);
		return;
	}

}