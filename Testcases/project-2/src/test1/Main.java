package test1;

/*public class Main {

	public static void main(String[] args) {

		System.out.println("Hello world");

		return;
	}

}*/


public class Main {

	public static void main(String[] args) {
		int a=args[0].length();
		boolean b=true;
		System.out.println("Hello world");
		if(a<4){
			if(a<4)
			System.out.println("Inside IF IF");
			else System.out.println("Inside IF Else");

		System.out.println("Inside IF");
		}
		else if(a==4){
		System.out.println("Inside Else If");
		}
		else{
		System.err.println("Inside Else");		
		}
		return;
	}

}
