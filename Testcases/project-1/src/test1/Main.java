package test1;

public class Main {

	public static void main(String[] args) {

		int a = args[0].length();

		if (a > 5)
			a++;
		else
			a--;
		System.err.println(a);

		return;
	}

}