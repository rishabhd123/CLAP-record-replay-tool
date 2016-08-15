package test16;

public class Subclass extends Main {

	public void bar(int a) {
		if (a % 2 == 0)
			foo(a);
		foo(a);
		return;
	}

}