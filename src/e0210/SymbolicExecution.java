package e0210;

import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.util.Chain;

public class SymbolicExecution extends SceneTransformer {
	String project,testcase;
	String[] tupleList=null;
	Hashtable<String,Hashtable<String,Integer> > hashThreadMethodCount= new Hashtable<String,Hashtable<String,Integer> >();
	Hashtable<String,String> forkjoinMap;
	Hashtable<String,String> lockObjMap;
	int forksByThread;
	
	Chain<SootClass> classes=Scene.v().getApplicationClasses();
	
	
	
	//Constructor for SymbolicExecution
	public SymbolicExecution(String pro,String test) {
		project=pro;
		testcase=test;
	}
	
	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		
		String in=null;
		String inPath="Testcases/"+project+"/tuples/"+testcase;
		try {
			 in = new String(Files.readAllBytes(Paths.get(inPath)));
		} catch (IOException e) {/*e.printStackTrace();*/}
		
		tupleList=in.split(System.getProperty("line.separator"));
		for(String tuple:tupleList){
			if(tuple.contains("void main(") || tuple.contains("void run()")){
				String[] tupleComponents=tuple.split(",");
				System.out.println("Begin Intrathread Trace of Thread: "+tupleComponents[1]);
				getTrace(tupleComponents[0], tupleComponents[1], tupleComponents[3],true);
				System.out.println("End Intrathread Trace of Thread: "+tupleComponents[1]);
			}
						
		}
		
		
		/* 
        SceneTransformer vs BodyTransformer
        ===================================
        BodyTransformer is applied on all methods separately. It uses multiple 
        worker threads to process the methods in parallel.
        SceneTransformer is applied on the whole program in one go. 
        SceneTransformer uses only one worker thread.
        
        It is better to use SceneTransformer for this part of the project 
        because:
        1. During symbolic execution you may need to 'jump' from one method
        to another when you encounter a call instruction. This is easily 
        done in SceneTransformer (see below)
        2. There is only one worker thread in SceneTransformer which saves 
        you from having to synchronize accesses to any global data structures
        that you'll use, (eg data structures to store constraints, trace, etc)
        
        How to get method body?
        =======================
        Use Scene.v().getApplicationClasses() to get all classes and
        SootClass::getMethods() to get all methods in a particular class. You
        can search these lists to find the body of any method.
        
        */
        
        /* 
        Perform the following for each thread T:
        1. Start with the first method of thread T. If T is the main thread,
        the first method is main(), else it is the run() method of the thread's
        class
        2. Using (only) the tuples for thread T, walk through the code to 
        reproduce the path followed by T. If thread T performs method calls
        then, during this walk you'll need to jump from one method's body to 
        another to imitate the flow of control (as discussed during the Project session)
        3. While walking through the code, collect the intra-thread trace for 
        T and the path constraints.
        Don't forget that you need to renumber each dynamic instance of a static
        variable (i.e. SSA)
        */
	    return;
	}
	
	
	public void pseudoGetTrace(String method,String thread,String numInvoke ){		//numInvoke=number of invocation of"method" by "thread"	
		
		//System.out.println("New Invocation  "+method+"  "+thread+" "+numInvoke);
		for(String tuple:tupleList ){
			String[] tupleComponent=tuple.split(",");
						
			if(tupleComponent[0].equals(method) && tupleComponent[1].equals(thread) && tupleComponent[2].equals(numInvoke)){
				getTrace(method, thread, tupleComponent[3],false);
			}
		}
		
		return;
	}
	

	public void getTrace(String method,String thread,String blString,boolean newThread){	
		if(newThread) {
			forksByThread=0;
			forkjoinMap=new Hashtable<String,String>();
			lockObjMap=new Hashtable<String,String>();
		}
		
		boolean flag=false;
		String[] blBlocks=blString.split("&");
		Iterator<SootClass> classesIt=classes.iterator();
		while(classesIt.hasNext()){
			SootClass nextClass=classesIt.next();
			Iterator<SootMethod> methodIt= nextClass.getMethods().iterator();
			while(methodIt.hasNext()){
				SootMethod nextMethod=methodIt.next();
				if(nextMethod.getSubSignature().contains(method)){
					//System.out.println("Trace of  Method "+method+"Executed by thread"+thread);
					printTrace(nextMethod.getActiveBody(),thread,blBlocks);
					flag=true;
					break;
					
				}
			}
			if(flag==true) break;
			
		}
		
		return;	
	}
	
	
	public void printTrace(Body b,String thread,String[] blBlocks){
		ExceptionalBlockGraph graph=new ExceptionalBlockGraph(b);
		
		List<Block> blockList=graph.getBlocks();
		for(String blockStr:blBlocks){
			int blockId=Integer.parseInt(blockStr);
		
			Iterator<Block> blockListIt=blockList.iterator();
			while(blockListIt.hasNext()){
				Block bloc=blockListIt.next();
				if(bloc.getIndexInMethod()==blockId){
					 Unit head=bloc.getHead();
					 Unit tail=bloc.getTail();
					 Stmt s=(Stmt)head;
					 printStatement(s,thread);			 
					 
					 while(!head.equals(tail)){
						 head=bloc.getSuccOf(head);
						 s=(Stmt)head;
						 printStatement(s,thread);
					 }
					
					break;
				}
			}
			
			
		}
		return;
		
	}
	public void printStatement(Stmt s,String thread){				//Print statement of Unit if it is assign or if stmt
		if(s instanceof IfStmt) { //System.out.println(s);
		}
		
		else if(s instanceof AssignStmt) {
			//System.out.println(s);
			String rightOp=((AssignStmt) s).getRightOp().toString();
			
			String stmtStr=s.toString();
			if(rightOp.contains("locks.Lock")){
				String[] rightOpStr=rightOp.split(" ");
				String leftOp=((AssignStmt) s).getLeftOp().toString();
				lockObjMap.put(leftOp,rightOpStr[2]);
				
			}
			else if(s.containsInvokeExpr() && !( stmtStr.contains("start(") || stmtStr.contains("<init>(") || stmtStr.contains("join(") ) && stmtStr.contains("test") ){
				String methodInvoked=s.getInvokeExpr().getMethod().toString();
				if(!hashThreadMethodCount.containsKey(thread)){
					pseudoGetTrace(methodInvoked, thread, "0");
					hashThreadMethodCount.put(thread, new Hashtable<String,Integer>());
					hashThreadMethodCount.get(thread).put(methodInvoked, 1);
				}
				else if(!hashThreadMethodCount.get(thread).containsKey(methodInvoked)){
					pseudoGetTrace(methodInvoked, thread, "0");
					hashThreadMethodCount.get(thread).put(methodInvoked, 1);
					
				}
				else{
					pseudoGetTrace(methodInvoked, thread, hashThreadMethodCount.get(thread).get(methodInvoked).toString());
					hashThreadMethodCount.get(thread).put(methodInvoked, hashThreadMethodCount.get(thread).get(methodInvoked)+1);
				}
			}
			return;
			
		}
		else if(s instanceof InvokeStmt){
			String stmtStr=s.toString();
			if(stmtStr.contains("start(")){
				System.out.println("<"+thread+", Fork, "+thread+"."+forksByThread+">");
				AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)s.getInvokeExpr();
				String baseObj=expr1.getBase().toString();
				forkjoinMap.put(baseObj, "<"+thread+", Join, "+thread+"."+forksByThread+">");
				forksByThread++;				
			}
			else if(stmtStr.contains("join(")){
				AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)s.getInvokeExpr();
				String baseObj=expr1.getBase().toString();
				System.out.println(forkjoinMap.get(baseObj));
				
			}
			else if(stmtStr.contains("void lock()") ){
				AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)s.getInvokeExpr();
				System.out.println("<"+thread+" Lock, "+lockObjMap.get(expr1.getBase().toString()));
				
			}
			else if(stmtStr.contains("void unlock()")){
				AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)s.getInvokeExpr();
				System.out.println("<"+thread+" Unlock, "+lockObjMap.get(expr1.getBase().toString()));
				
			}
			
			
			else if( !(  stmtStr.contains("<init>(") ) && stmtStr.contains("test") ){
				String methodInvoked=s.getInvokeExpr().getMethod().getSubSignature();
				if(!hashThreadMethodCount.containsKey(thread)){
					pseudoGetTrace(methodInvoked, thread, "0");
					hashThreadMethodCount.put(thread, new Hashtable<String,Integer>());
					hashThreadMethodCount.get(thread).put(methodInvoked, 1);
					
				}
				else if(!hashThreadMethodCount.get(thread).containsKey(methodInvoked)){
					pseudoGetTrace(methodInvoked, thread, "0");
					hashThreadMethodCount.get(thread).put(methodInvoked, 1);
					
					
				}
				else{
					pseudoGetTrace(methodInvoked, thread, hashThreadMethodCount.get(thread).get(methodInvoked).toString());
					hashThreadMethodCount.get(thread).put(methodInvoked, hashThreadMethodCount.get(thread).get(methodInvoked)+1);
					
				}
			}
			
		return;
		}
		
		
	}

}
