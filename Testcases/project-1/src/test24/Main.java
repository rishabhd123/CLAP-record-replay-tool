package test24;

/*
 * @author Abhinav Anil Sharma	-		abhinav.sharma@csa.iisc.ernet.in
 * 
 * Course project,
 * Principles of Programming Course, Fall - 2016,
 * Computer Science and Automation (CSA),
 * Indian Institute of Science (IISc),
 * Bangalore
 */

/*
 * Test 24 (Hidden test 4): Nested loops
 * 
 * Command line args:
 * 3
 */

public class Main {

	public static void main(String[] args) {
		int a = Integer.parseInt(args[0]);
		int b = 0;
		for (int i = 0; i < a; i++) {

			for (int j = 0; j < a; j++) {
				b += i;
			}
		}
		System.err.println(b);
	}

}
