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
 * Test 16: Inheritance
 */

public class Subclass extends Main {

	public void bar(int a) {
		if (a % 2 == 0)
			foo(a);
		foo(a);
		return;
	}

}