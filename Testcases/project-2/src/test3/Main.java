package test3;

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
 * Test 3: Switch-case
 */

public class Main {

	public static void main(String[] args) {

		int x = args[0].length();

		String output = null;
		if (x < 5) {
			switch (x) {
			case 1:
				output = "January";
				break;
			case 2:
				output = "February";
				break;
			case 3:
				output = "March";
				break;
			default:
				output = "Vacation";
				break;
			}
		}

		System.err.println(output);

		return;
	}

}