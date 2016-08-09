package test2;

public class Main {

	public static void main(String[] args) {

		int a = args[0].length();

		if (a > 5) {
			if (a > 10)
				a++;
			else
				a--;
		} else {
			if (a == 0)
				a--;
		}
		System.err.println(a);

		return;
	}

}