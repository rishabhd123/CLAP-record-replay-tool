package pop_replay;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PoP_Replay_Util 
{
    static {
           
            /* The global_trace will be copied to the current directory before running the test case */
          //  BufferedReader br = new BufferedReader(new FileReader("global_trace"));
            
    }
    ////////////////////////
    
    static int[] forks=new int[10000];					//current TID--->number of forks
    static String[] threadMapping=new String[10000];		//current TID--->TID(0.0.1 like)
    static Hashtable<String,Integer>[] threadMethodCount = (Hashtable<String,Integer>[])new Hashtable<?,?>[10000];

   
    public static synchronized void countMethod(String methodSign){		//Count Number of times "methodSign" is called by current thread
  	  long  tid=Thread.currentThread().getId();
  	  
  	  if(threadMethodCount[(int)tid]==null )	threadMethodCount[(int)tid]=new Hashtable<String,Integer>();
  	  
  	  Hashtable<String,Integer> hashTableFortid=threadMethodCount[(int)tid];
  	  
  	  if(hashTableFortid.containsKey(methodSign))
  		  hashTableFortid.put(methodSign, hashTableFortid.get(methodSign)+1);
  	  else
  		  hashTableFortid.put(methodSign,0);
  	 
    }
    
    public static synchronized void generateTuple(String blString,String methodSign){
  	  int threadId=(int)Thread.currentThread().getId();
  	  if(methodSign.contains("void <clinit>()"))		System.out.println(methodSign+","+threadMapping[threadId]+","+ 0 +","+blString);
  	  else if(methodSign.contains("void run()"))	System.out.println(methodSign+","+threadMapping[threadId]+","+ 0 +","+blString);
  	  else							        System.out.println(methodSign+","+threadMapping[threadId]+","+threadMethodCount[threadId].get(methodSign)+","+blString);
    }
    
    public static synchronized String concat(String original,long toConcat){
  	  if(original.equals("")) return toConcat+"";
  	  original=original+"&"+toConcat;
  	  return original;
    }
      
    public static synchronized void initialize(){
  	  int mainid=(int)Thread.currentThread().getId();
  	  forks[mainid]= 0;
  	  threadMapping[mainid]="0";
  	  threadMethodCount[mainid]=new Hashtable<String,Integer>();
  	  threadMethodCount[mainid].put("void main(java.lang.String[])", 0);
    }
    
    public static synchronized void calls(long child){
  	  long parent=Thread.currentThread().getId();
  	  long forksByParent=forks[(int)parent];
  	  String parentTID=threadMapping[(int)parent];
  	  String childTID=parentTID+"."+Long.toString(forksByParent);
  	  threadMapping[(int)child]=childTID;
  	  forks[(int)parent]++; //increment no. of forks of parent by 1
  	  forks[(int)child]=0;	//set no. of forks of child to 0
   }
    
   ////////////////////////////// 
    
    
  
    /* You can modify criticalBefore() to receive the required arguments */
    public static void criticalBefore () {
   
    }
    
    /* You can modify criticalAfter() to receive the required arguments */
    public static void criticalAfter () {
       
    }
       
}
