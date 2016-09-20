package test15;

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
 * Test 15: Creating objects (should take care of constructors)
 */

public class Main {

	public static void main(String[] args) {

		int a = args[0].length();

		Main obj = new Main();
		obj.foo();

		System.err.println(a);

		return;
	}

	public void foo() {

		bar();

		return;
	}

	private void bar() {
		return;
	}

}
