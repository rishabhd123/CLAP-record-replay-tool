package test19;

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
 * Test 19: Anonymous classes
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		new Abstract() {

			@Override
			public void foo(int x) {

				for (int i = 0; i < x; i++) {
					if (i % 2 == 0)
						bar(x);
					else
						System.err.println(x);
				}
			}

		}.foo(x);

		System.err.println(x);

		return;
	}

}