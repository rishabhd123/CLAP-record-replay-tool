package test22;

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
 * Hidden test 2: If-else-if ladder 
 *
 * Command Line args: 
 * 3
 */

public class Main {

	public static void main(String[] args) {

		int a = Integer.parseInt(args[0]);
		if (a < 1) {
			System.err.println("one");
		} else if (a < 2) {
			System.err.println("two");

		} else if (a < 3) {
			System.err.println("three");

		} else if (a < 4) {
			System.err.println("four");

		} else {
			System.err.println("five");
		}
	}

}