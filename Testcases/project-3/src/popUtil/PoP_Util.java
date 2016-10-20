package popUtil;

/* 
    Util class for auxillary methods    
*/

import java.util.Random;

public class PoP_Util {

	static Random randGen = new Random();

	/* Delay the calling thread for a random amount of time */
	public static void randomDelay(){
	    randomDelay(100);
	}
	public static void randomDelay(int maxDelay) {

		try {
			int delay = randGen.nextInt(maxDelay);
			Thread.sleep(delay);
		} catch (Exception e) {
			System.err.println("Exception in PoP_Util class. Please contact the TA");
		}
	}

}
