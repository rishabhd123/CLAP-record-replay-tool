package e0210;

public class MyCounter {
  
  private static long  c = 0;
  
  
  public static synchronized void increment(long x) {
    c =c+ x;
  }

    public static synchronized void printG() {
    System.out.print(c);
  }
}
