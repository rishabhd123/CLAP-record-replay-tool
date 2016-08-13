package test8;

/*
 * @author Sridhar Gopinath		-		g.sridhar53@gmail.com
 * 
 * Course project,
 * Principles of Programming Course, Fall - 2016,
 * Computer Science and Automation (CSA),
 * Indian Institute of Science (IISc),
 * Bangalore
 */

public class Main {

	public static void main(String[] args) {

		int a = args[0].length();

		String str;
		switch (a) {
		case 1:
			str = "January";
			break;
		case 2:
			str = "February";
			break;
		case 3:
			str = "March";
			break;
		case 4:
			str = "April";
			break;
		case 5:
			str = "May";
			break;
		case 6:
			str = "June";
			break;
		case 7:
			str = "July";
			break;
		case 8:
			str = "August";
			break;
		case 9:
			str = "September";
			break;
		case 10:
			str = "October";
			break;
		case 11:
			str = "November";
			break;
		case 12:
			str = "December";
			break;
		default:
			str = "Invalid month";
			break;
		}
		System.err.println(str);

		return;
	}

}