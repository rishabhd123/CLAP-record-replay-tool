package test2;
/*
public class Main {

	public static void main(String[] args) {

		System.out.println("Hello world");

		return;
	}

}*/

public class Main {

        public static void main(String[] args) {

                int i, j;
                i = args[0].length();
                j = args[1].length();
                if(i>10){
                        i++;
                        if(j>4){
                                j++;
                        }else{
                                j--;
                        }
                }else{
                        i--;
                }
                if(i!=10){
                        if(j!=4){
                                System.out.println("abcd");
                        }
                }
                i = 1000;
                return;
        }

}
