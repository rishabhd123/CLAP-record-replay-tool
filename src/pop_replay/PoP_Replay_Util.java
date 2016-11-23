package pop_replay;


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;

public class PoP_Replay_Util 
{	
	static int cnt=0;
	static ArrayList<String> gtraceArr = null;
    static {
          
            /* The global_trace will be copied to the current directory before running the test case */
    	
    	try {
			String in = new String(Files.readAllBytes(Paths.get("global_trace")));
			String[] gtrace=in.split("\n");
			gtraceArr=new ArrayList<String>();
			for(String s:gtrace){
				if(!( s.contains("Begin")||s.contains("End") )) gtraceArr.add(s);					
				
			}
			
		} catch (IOException e) {e.printStackTrace();}
    	
    	            
    }
    ////////////////////////
    static Object obj1=new Object();
    static Object obj2=new Object();
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
    
    public static void criticalBefore (String mid,String last,long cid) throws InterruptedException {
    	
    	int parent=(int)Thread.currentThread().getId();
    	String first=threadMapping[parent];
    	
    	if(first==null) first="0";
    	
    	String tuple=first+", "+mid+", ";
    	if(mid.contains("Fork")||mid.contains("Join")){
    		tuple=tuple+threadMapping[(int)cid];    		
    	}
    	else tuple=tuple+last;   //got the tuple
    	
    	while(true){
    		synchronized (obj1) {
    			if(tuple.equals(gtraceArr.get(cnt))) {break;}
    			obj1.wait();
			}
    		 		  		
    		
    	}
    	   	
    		
    }
    
    /* You can modify criticalAfter() to receive the required arguments */
    public static void criticalAfter () {
       synchronized (obj1) {
    	   cnt++;
    	   obj1.notifyAll();		
       }
    }
       
}
