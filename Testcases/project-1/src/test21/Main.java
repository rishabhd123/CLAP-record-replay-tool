package test21;

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
 * Hidden test 1: Inner classes 
 * Checks whether all classes are being instrumented
 * Checks whether overloaded functions are printed 
 * Checks whether overridden functions are printed
 * 
 * Command line args:
 * 4
 */

public class Main {

	static class Class1 {
		static class Class2 {
			public int someMethod(int a)
			/* Never Invoked */
			{
				Integer b = (Integer) a;
				if (b != a) {
					return 0;
				} else {
					return 1;
				}
			}

			public int someMethod(int a, int b) {
				if (a < b) {
					return -1;
				} else if (a > b) {
					return 1;
				} else {
					return 0;
				}
			}
		}

		static class Class3 extends Class2 {
			@Override
			public int someMethod(int a) {
				int b = 0;
				for (int i = 0; i < a; i++) {
					b += 1;
				}
				return b;
			}
		}

		static Class3 obj2 = new Class3();

		Integer wrapper(int a) {
			Integer ret = 0;
			ret += obj2.someMethod(a, a);
			ret += obj2.someMethod(a + 1);
			Class2 o = obj2;
			ret += o.someMethod(a + 1);
			return ret;
		}
	}

	public static void main(String[] args) {
		int ctr = 0;
		Class1 obj1 = new Class1();
		System.err.println(obj1.wrapper(Integer.parseInt(args[0])));
		System.err.println(ctr);
	}

}