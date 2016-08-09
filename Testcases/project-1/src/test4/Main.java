package test4;

public class Main {

	public static void main(String[] args) {
		int a = args[0].length();

		if (a > 0)
			foo(a);
		else
			bar(a);
		System.err.println(a);

		return;
	}

	public static int foo(int a) {
		if (a > 0)
			if (a == 1)
				return a + 1;
		return a;
	}

	public static int bar(int a) {
		if (a > 5)
			if (a == 10)
				return a + 1;
		return a;
	}

}