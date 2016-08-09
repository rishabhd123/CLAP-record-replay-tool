package test3;

public class Main {

	public static void main(String[] args) {
		int a = args[0].length();

		for (int i = 0; i < 5; i++) {
			if (i == 3)
				a++;
		}
		System.err.println(a);

		return;
	}

}