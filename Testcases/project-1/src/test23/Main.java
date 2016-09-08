package test23;

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
 * Test 23 (Hidden test 3): Recursion
 * 
 * Command line args:
 * 0 5
 */

public class Main {

	static int recur(int a) {
		if (a == 0) {
			return a;
		}
		return recur(a - 1);
	}

	public static void main(String[] args) {
		System.err.println(recur(Integer.parseInt(args[0])));
		System.err.println(recur(Integer.parseInt(args[1])));
	}

}