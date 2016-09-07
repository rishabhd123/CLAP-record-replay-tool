package test25;

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
 * Hidden test 5: Anonymous classes
 * 
 * Command line args:
 * 4 5
 */

public class Main {

	static interface Class1 {
		public int someFunction(int a);
	}

	public static void main(String[] args) {

		Class1 obj = new Class1() {
			@Override
			public int someFunction(int a) {
				if (a == 4) {
					return 4;
				}
				return 0;
			}
		};
		System.err.println(obj.someFunction(Integer.parseInt(args[0])));
		System.err.println(obj.someFunction(Integer.parseInt(args[1])));
	}

}