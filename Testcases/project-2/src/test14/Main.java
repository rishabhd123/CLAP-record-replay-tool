package test14;

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
 * Test 14: Multiple function calls inside loop
 * Example taken from Casper paper:
 * Wu, Rongxin, et al. "Casper: an efficient approach to call trace collection." ACM SIGPLAN Notices 51.1 (2016): 678-690.
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		A();

		int i = 0;
		do {
			if (i % 2 == 0)
				B(i);
			else
				E(i);
			i++;
		} while (i < 10);

		H();
		
		System.err.println(x);

		return;
	}

	public static void A() {
		return;
	}

	public static void B(int x) {
		if (x > 5)
			C();
		else
			D();
		return;
	}

	public static void C() {
		return;
	}

	public static void D() {
		return;
	}

	public static void E(int x) {
		if (x > 5)
			C();
		else
			D();
		return;
	}

	public static void H() {
		return;
	}

}