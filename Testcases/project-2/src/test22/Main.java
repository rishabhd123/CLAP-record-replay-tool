package test22;

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
 * Test 22: Inner classes, method overloading and overriding methods
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		Class1 obj1 = new Class1();
		obj1.wrapper(x);

		System.err.println(x);

		return;
	}

	static class Class1 {

		static class Class2 {

			public int someMethod(int a) {
				Integer b = (Integer) a;
				if (b != a)
					return 0;
				else
					return 1;
			}

			public int someMethod(int a, int b) {
				if (a < b)
					return -1;
				else if (a > b)
					return 1;
				else
					return 0;
			}

		}

		static class Class3 extends Class2 {

			@Override
			public int someMethod(int a) {
				int b = 0;
				for (int i = 0; i < a; i++)
					b += 1;
				return b;
			}

		}

		Integer wrapper(int a) {

			Class3 obj2 = new Class3();

			Integer ret = 0;
			ret += obj2.someMethod(a, a);
			ret += obj2.someMethod(a);

			Class2 o = obj2;
			ret += o.someMethod(a);

			return ret;
		}

	}

}