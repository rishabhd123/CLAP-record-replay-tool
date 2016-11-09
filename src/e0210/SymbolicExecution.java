package e0210;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import soot.Body;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.util.Chain;
import com.microsoft.z3.*;



public class SymbolicExecution extends SceneTransformer {
	String project,testcase;
	String[] tupleList=null;
	
	Hashtable<String,Hashtable<String,Integer> > hashThreadMethodCount= new Hashtable<String,Hashtable<String,Integer> >();
	Hashtable<String,String> forkjoinMap;
	Hashtable<String,String> lockObjMap;
	Hashtable<String,Integer> readCountForSymVal=new Hashtable<String,Integer>();
	Hashtable<String,Integer> writeCountForSymVal=new Hashtable<String,Integer>();
	Hashtable<String,Hashtable<String,String> > programOrderConst=new Hashtable<String,Hashtable<String,String>>();
	Hashtable<String,Integer> stmtCountOfThread=new Hashtable<String,Integer>();		//contains cnt of nos of stmt executed in particular thread
	BoolExpr[]  constraints=new BoolExpr[100000];		//contains all constraints
	int c=0;
	
	
	Context ctx=new Context(new HashMap<String,String>());		//solver specific
	Solver solver=ctx.mkSolver();
	
	
	
	String varName,stmtToPrint;		//stmtToPrint::it holds the intrathread stmts which is to be printed on the console
	int forksByThread,cntProgramOrder;		//Sequence number of stmt which is currently executing
	
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
				String thread=tupleComponents[1];
				programOrderConst.put(thread, new Hashtable<String,String>());
				
				stmtToPrint= thread+", Begin";
				programOrderConst.get(thread).put("O_"+thread+"_1", stmtToPrint);
				System.out.println(stmtToPrint);
				
				
				getTrace(tupleComponents[0], tupleComponents[1], tupleComponents[3],true);
				
				
				stmtToPrint= thread+", End";
				programOrderConst.get(thread).put("O_"+thread+"_"+cntProgramOrder, stmtToPrint);
				System.out.println(stmtToPrint+"\n\n");				
				stmtCountOfThread.put(thread, cntProgramOrder);

				// inserting program order constraints of "thread" in array of constraints.
					for(int p=1;p<cntProgramOrder;p++){
						IntExpr innum1=ctx.mkIntConst("O_"+thread+"_"+p);
						IntExpr innum2=ctx.mkIntConst("O_"+thread+"_"+(p+1));
						BoolExpr lt=ctx.mkLt(innum1, innum2);
						constraints[c++]=lt;
					}
					
					
					
					
				
				//System.out.println(solver.check());		//solver specific
				//System.out.println(solver.toString());	
				//System.out.println(cntProgramOrder);
				
			}
						
		}
		BoolExpr[] constraints1= new BoolExpr[c];
		java.lang.System.arraycopy(constraints, 0, constraints1,0 ,c);
				
		System.out.println(constraints1.length);
		solver.add(ctx.mkAnd(constraints1));
		System.out.println(solver.check());		//solver specific
		//System.out.println(solver.toString());
		solver.check();
		Model model=solver.getModel();
		//System.out.println(model.toString());
			
		
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
			cntProgramOrder=2;
			
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
				
				String leftOp=((AssignStmt) s).getLeftOp().toString();
				lockObjMap.put(leftOp,s.getFieldRef().getField().getName());
				
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
			
			else if(s.containsFieldRef() && ( stmtStr.contains("Integer") || stmtStr.contains("Double") || stmtStr.contains("Long") || stmtStr.contains("Short") || stmtStr.contains("Byte") || stmtStr.contains("int ") || stmtStr.contains("double ") || stmtStr.contains("long ") || stmtStr.contains("float "))){
				Value rightop= ((AssignStmt) s).getRightOp();
				Value leftop=  ((AssignStmt) s).getLeftOp();
				//System.out.println(s.getFieldRef().getField().getName());
				int r=rightop.toString().length();
				int l=leftop.toString().length();
				varName=s.getFieldRef().getField().getName();
							
				if(l>r){
					if(writeCountForSymVal.containsKey(varName))	writeCountForSymVal.put(varName,writeCountForSymVal.get(varName)+1);
					else	writeCountForSymVal.put(varName,1);
					
					stmtToPrint= thread+", Write, "+varName+", Sym_Val_"+varName+"_W"+writeCountForSymVal.get(varName);
					programOrderConst.get(thread).put("O_"+thread+"_"+cntProgramOrder, stmtToPrint);
					cntProgramOrder++;
					System.out.println(stmtToPrint);
					
				}
				else{
					if(readCountForSymVal.containsKey(varName))		readCountForSymVal.put(varName,readCountForSymVal.get(varName)+1);
					else	readCountForSymVal.put(varName,1);
					
					stmtToPrint= thread+", Read, "+varName+", Sym_Val_"+varName+"_R"+readCountForSymVal.get(varName);
					programOrderConst.get(thread).put("O_"+thread+"_"+cntProgramOrder, stmtToPrint);
					cntProgramOrder++;
					System.out.println(stmtToPrint);
				}
				
				
				
			}
			
			
		}
		else if(s instanceof InvokeStmt){
			String stmtStr=s.toString();
			if(stmtStr.contains("start(")){
				
				stmtToPrint=thread+", Fork, "+thread+"."+forksByThread;
				programOrderConst.get(thread).put("O_"+thread+"_"+cntProgramOrder, stmtToPrint);
				
				System.out.println(stmtToPrint);
				
				AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)s.getInvokeExpr();
				String baseObj=expr1.getBase().toString();
				forkjoinMap.put(baseObj, thread+", Join, "+thread+"."+forksByThread);
				
				//fork constraints: instead of printing, give these to solver
				System.out.println("O_"+thread+"_"+cntProgramOrder+" < "+ "O_"+thread+"."+forksByThread+"_"+1 );
				//
					IntExpr innum1=ctx.mkIntConst("O_"+thread+"_"+cntProgramOrder);		//solver specific
					IntExpr innum2=ctx.mkIntConst("O_"+thread+"."+forksByThread+"_"+1);
					BoolExpr lt=ctx.mkLt(innum1, innum2);
					constraints[c++]=lt;
					//solver.add(lt);
					
				//
				
				
				cntProgramOrder++;
				forksByThread++;				
			}
			else if(stmtStr.contains("join(")){
				AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)s.getInvokeExpr();
				String baseObj=expr1.getBase().toString();
				
				stmtToPrint=forkjoinMap.get(baseObj);
				programOrderConst.get(thread).put("O_"+thread+"_"+cntProgramOrder, stmtToPrint);
				
				System.out.println(stmtToPrint);
				
				String temp=forkjoinMap.get(baseObj).split(" ")[2];
				
				//join constraints: instead of printing, give these to solver
				System.out.println("O_"+thread+"_"+cntProgramOrder+" > "+ "O_"+temp+"_"+stmtCountOfThread.get(temp));
				//
					IntExpr innum1=ctx.mkIntConst("O_"+thread+"_"+cntProgramOrder);			//solver specific
					IntExpr innum2=ctx.mkIntConst("O_"+temp+"_"+stmtCountOfThread.get(temp));
					BoolExpr gt=ctx.mkGt(innum1, innum2);
					constraints[c++]=gt;
					//solver.add(lt);
				//
				
				cntProgramOrder++;
				
			}
			else if(stmtStr.contains("void lock()") ){
				AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)s.getInvokeExpr();
				
				stmtToPrint= thread+", Lock, "+lockObjMap.get(expr1.getBase().toString());
				programOrderConst.get(thread).put("O_"+thread+"_"+cntProgramOrder, stmtToPrint);
				cntProgramOrder++;
				System.out.println(stmtToPrint);
				
			}
			else if(stmtStr.contains("void unlock()")){
				AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)s.getInvokeExpr();
				
				stmtToPrint= thread+", Unlock, "+lockObjMap.get(expr1.getBase().toString());
				programOrderConst.get(thread).put("O_"+thread+"_"+cntProgramOrder, stmtToPrint);
				cntProgramOrder++;
				System.out.println(stmtToPrint);
				
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
			
		
		}
		
	return;	
	}

}
