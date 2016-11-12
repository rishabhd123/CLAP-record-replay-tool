package e0210;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.Constant;
import soot.jimple.DivExpr;
import soot.jimple.EqExpr;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.MulExpr;
import soot.jimple.RemExpr;
import soot.jimple.Stmt;
import soot.jimple.SubExpr;
import soot.jimple.XorExpr;
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
	Hashtable<String,Integer> readCountForSymVal=new Hashtable<String,Integer>();		//number of reads performed on variables
	Hashtable<String,Integer> writeCountForSymVal=new Hashtable<String,Integer>();		//number of writes performed on variables
	Hashtable<String,Hashtable<String,String> > programOrderConst=new Hashtable<String,Hashtable<String,String>>();
	Hashtable<String,Integer> stmtCountOfThread=new Hashtable<String,Integer>();		//contains cnt of nos of stmt executed in particular thread
	Hashtable<String, LinkedList<MyLock> > lockListMap=new Hashtable<String, LinkedList<MyLock>>();		//Mapping of lock-name and corresponding objects of MyLock
	Hashtable<String,Integer> localReadCountForSymval;
	Hashtable<String,Integer> localWriteCountForSymval;
	List<String> clinitStmt=new ArrayList<>();
	
	BoolExpr[]  constraints=new BoolExpr[100000];		//contains all constraints
	int c=0, blockId,nextBlockId;
	
	
	Context ctx=new Context(new HashMap<String,String>());		//solver specific
	Solver solver=ctx.mkSolver();
	
	
	
	String varName,stmtToPrint;		//stmtToPrint::it holds the intrathread stmts which is to be printed on the console
	int forksByThread,cntProgramOrder,cntProOrder;		//Sequence number of stmt which is currently executing
	
	Chain<SootClass> classes=Scene.v().getApplicationClasses();
	
	
	//Constructor for SymbolicExecution
	public SymbolicExecution(String pro,String test) {
		project=pro;
		testcase=test;
	}
	
	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {	
		
		
		//Handling clinit<>
		Iterator<SootClass> sootClsIt=classes.iterator();
		while(sootClsIt.hasNext()){
			SootClass crntCls=sootClsIt.next();
			Iterator<SootMethod> sootMthdIt=crntCls.getMethods().iterator();
			while(sootMthdIt.hasNext()){
				SootMethod crntMethod=sootMthdIt.next();
				String nameOfMeth=crntMethod.getSignature();
				if(nameOfMeth.contains("test") && nameOfMeth.contains("<clinit>()")){
					handleclinit(crntMethod.getActiveBody());
				}
			}
		}
		
		//^Handling clinit<>
		
		
		
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
				if(!tuple.contains("void main(") )	programOrderConst.put(thread, new Hashtable<String,String>());
				
				stmtToPrint= thread+", Begin";
				programOrderConst.get(thread).put("O_"+thread+"_1", stmtToPrint);
				System.out.println(stmtToPrint);
				if(tuple.contains("void main(") ){
					for(int i=2,j=0;i<cntProOrder;i++,j++){
						programOrderConst.get("0").put("O_0_"+i, clinitStmt.get(j));
						System.out.println(clinitStmt.get(j));
					}
					
				}
				
				
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
		
		generateLockUnlockConstr(); 			//Generating Lock_unlock constraints
		
		BoolExpr[] constraints1= new BoolExpr[c];
		java.lang.System.arraycopy(constraints, 0, constraints1,0 ,c);
				
		System.out.println(constraints1.length);
		solver.add(ctx.mkAnd(constraints1));
		System.out.println(solver.check());			//solver specific
		System.out.println(solver.toString());
		//solver.check();
		//Model model=solver.getModel();
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
			if(thread.equals("0")) cntProgramOrder=cntProOrder;
			else cntProgramOrder=2;
			
			localWriteCountForSymval=new Hashtable<String,Integer>();
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
		int sizeblBlocks=blBlocks.length;
		for(int i=0;i<sizeblBlocks;i++)					//for(String blockStr:blBlocks)
		{	
			String blockStr=blBlocks[i];
			blockId=Integer.parseInt(blockStr);					
			if(i<sizeblBlocks-1) 	nextBlockId=Integer.parseInt(blBlocks[i+1]);					//successor of current block: will be used in "IfStmt"
			
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
			BinopExpr binaryExpOfIf=(BinopExpr)((IfStmt) s).getCondition();
			Value op1=binaryExpOfIf.getOp1();
			Value op2=binaryExpOfIf.getOp2();
			String operation=binaryExpOfIf.getSymbol();			
			String type=binaryExpOfIf.getType().toString();
			String lhs="SV_"+thread+"_"+op1.toString()+"_W"+localWriteCountForSymval.get(op1.toString());
			if((blockId+1)==nextBlockId){		//False according to jimple
				switch (operation){
				case " != ": operation=" == "; break;
				case " == ": operation=" != "; break;
				case " >= ": operation=" < "; break;
				case " <= ": operation=" > "; break;
				case " < ": operation=" >= "; break;
				case " > ": operation=" <= "; break;
								
				}	
					
				
			}
			
			
			if(op2 instanceof Constant){			//when rhs of Ifstmt condition in constant
				switch (type){
				
				case "java.lang.Integer":
					IntExpr lhs1=ctx.mkIntConst(lhs);
					IntNum  rhs1=ctx.mkInt(Integer.parseInt(op2.toString()));
					
						switch (operation){
							case " != ": 
								constraints[c++]= ctx.mkNot(ctx.mkEq(lhs1, rhs1));
								break;
							case " == ": 
								constraints[c++]= ctx.mkEq(lhs1, rhs1);
								break;
							case " >= ": 
								constraints[c++]=ctx.mkGe(lhs1, rhs1);
								break;
							case " <= ": 
								constraints[c++]=ctx.mkLe(lhs1, rhs1);
								break;
							case " < ": 
								constraints[c++]=ctx.mkLt(lhs1, rhs1);
								break;
							case " > ": 
								constraints[c++]=ctx.mkGt(lhs1, rhs1);
								break;
						
						}
					
					break;
					
				case "int":
					IntExpr lhs2=ctx.mkIntConst(lhs);
					IntNum  rhs2=ctx.mkInt(Integer.parseInt(op2.toString()));
					
						switch (operation){
							case " != ": 
								constraints[c++]= ctx.mkNot(ctx.mkEq(lhs2, rhs2));
								break;
							case " == ": 
								constraints[c++]= ctx.mkEq(lhs2, rhs2);
								break;
							case " >= ": 
								constraints[c++]=ctx.mkGe(lhs2, rhs2);
								break;
							case " <= ": 
								constraints[c++]=ctx.mkLe(lhs2, rhs2);
								break;
							case " < ": 
								constraints[c++]=ctx.mkLt(lhs2, rhs2);
								break;
							case " > ": 
								constraints[c++]=ctx.mkGt(lhs2, rhs2);
								break;
						
						}
					
					break;	
				
				}
				
			}
			else{
				String rhs="SV_"+thread+"_"+op2.toString()+"_W"+localWriteCountForSymval.get(op2.toString());
				switch (type){
				
				case "java.lang.Integer":
					IntExpr lhs1=ctx.mkIntConst(lhs);
					IntExpr  rhs1=ctx.mkIntConst(rhs);
					
						switch (operation){
							case " != ": 
								constraints[c++]= ctx.mkNot(ctx.mkEq(lhs1, rhs1));
								break;
							case " == ": 
								constraints[c++]= ctx.mkEq(lhs1, rhs1);
								break;
							case " >= ": 
								constraints[c++]=ctx.mkGe(lhs1, rhs1);
								break;
							case " <= ": 
								constraints[c++]=ctx.mkLe(lhs1, rhs1);
								break;
							case " < ": 
								constraints[c++]=ctx.mkLt(lhs1, rhs1);
								break;
							case " > ": 
								constraints[c++]=ctx.mkGt(lhs1, rhs1);
								break;
						
						}
					
					break;
					
				case "int":
					IntExpr lhs2=ctx.mkIntConst(lhs);
					IntExpr  rhs2=ctx.mkIntConst(rhs);
					
						switch (operation){
							case " != ": 
								constraints[c++]= ctx.mkNot(ctx.mkEq(lhs2, rhs2));
								break;
							case " == ": 
								constraints[c++]= ctx.mkEq(lhs2, rhs2);
								break;
							case " >= ": 
								constraints[c++]=ctx.mkGe(lhs2, rhs2);
								break;
							case " <= ": 
								constraints[c++]=ctx.mkLe(lhs2, rhs2);
								break;
							case " < ": 
								constraints[c++]=ctx.mkLt(lhs2, rhs2);
								break;
							case " > ": 
								constraints[c++]=ctx.mkGt(lhs2, rhs2);
								break;
						
						}
					
					break;	
				
				}
				
				
			}
			
			
			//System.out.println(type+"---"+op1+"---"+operation+"---"+op2);
			
			//System.out.println("------------------------------------");
			//System.out.println(((IfStmt) s).getCondition().getUseBoxes().get(0).getValue());
			//System.out.println("------------------------------------");
			
		}
		
		else if(s instanceof AssignStmt) {
			//System.out.println(s);
			Value rightop= ((AssignStmt) s).getRightOp();
			Value leftop=  ((AssignStmt) s).getLeftOp();
			
			String leftOp=((AssignStmt) s).getLeftOp().toString();
			String rightOp=((AssignStmt) s).getRightOp().toString();
			String stmtStr=s.toString();
			
						
			if(s.containsInvokeExpr()){
				if(stmtStr.contains("Value()")){
					AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)s.getInvokeExpr();
					String baseObj=expr1.getBase().toString();
					String rhs="SV_"+thread+"_"+baseObj+"_W"+localWriteCountForSymval.get(baseObj);
					
					if(localWriteCountForSymval.containsKey(leftOp)) 	localWriteCountForSymval.put(leftOp, localWriteCountForSymval.get(leftOp)+1);
					else		localWriteCountForSymval.put(leftOp, 1);
					
					String lhs="SV_"+thread+"_"+leftOp+"_W"+localWriteCountForSymval.get(leftOp);
					global_eq_local(s.getInvokeExpr().getType().toString(), lhs, rhs);
					
					
				}
				else if(stmtStr.contains("valueOf(")){
					
					if(localWriteCountForSymval.containsKey(leftOp)) 	localWriteCountForSymval.put(leftOp, localWriteCountForSymval.get(leftOp)+1);
					else		localWriteCountForSymval.put(leftOp, 1);
					
					String lhs;
					Value arg=s.getInvokeExpr().getArg(0);
					String arg_s=arg.toString();
					String type=s.getInvokeExpr().getType().toString();
					
					lhs="SV_"+thread+"_"+leftOp+"_W"+localWriteCountForSymval.get(leftOp);
					
					if(arg instanceof Constant){
						
						switch (type){
						case "java.lang.Integer": 
							IntExpr lhs0=ctx.mkIntConst(lhs);
							IntNum rhs0=ctx.mkInt(Integer.parseInt(arg_s));
							constraints[c++]=ctx.mkEq(lhs0, rhs0);
							break;
							
						case "int":
							IntExpr lhs1=ctx.mkIntConst(lhs);
							IntNum rhs1=ctx.mkInt(Integer.parseInt(arg_s));
							constraints[c++]=ctx.mkEq(lhs1, rhs1);
							break;
							
						case "java.lang.Double": 
							RealExpr lhs2=ctx.mkRealConst(lhs);
							RatNum rhs2=ctx.mkReal(arg_s);
							constraints[c++]=ctx.mkEq(lhs2, rhs2);
							break;
							
						case "double": 
							RealExpr lhs3=ctx.mkRealConst(lhs);
							RatNum rhs3=ctx.mkReal(arg_s);
							constraints[c++]=ctx.mkEq(lhs3, rhs3);
							break;
							
						case "java.lang.Character": 
							
							break;
							
						case "char": 
							
							break;
							
						case "java.lang.Boolean": 
							IntExpr lhs6=ctx.mkIntConst(lhs);
							IntNum rhs6=ctx.mkInt(Integer.parseInt(arg_s));
							constraints[c++]=ctx.mkEq(lhs6, rhs6);
							break;
							
						case "boolean":
							IntExpr lhs7=ctx.mkIntConst(lhs);
							IntNum rhs7=ctx.mkInt(Integer.parseInt(arg_s));
							constraints[c++]=ctx.mkEq(lhs7, rhs7);
							break;
							
						}
						
						
					}
					
					else{
												
						String rhs="SV_"+thread+"_"+arg_s+"_W"+localWriteCountForSymval.get(arg_s);
						global_eq_local(type, lhs, rhs);
					}
					//System.out.println(s.getInvokeExpr().getType().toString()+"--------------------------");
					
				}
			}
			
			else if( !(s.containsFieldRef()) && !(stmtStr.contains("$r")) ){	//IMP :: enter here only when ass stmt doesn't contain InvokeExpr,Static global field. :: here "$r" condition is added to eliminate--	
				//System.out.println(stmtStr+"-----a-------------------");		//-- ass stmt which contains thread object initialization line t1=$r0 or t2=$r1
				
				
				String type=leftop.getType().toString();	
					if(rightop instanceof BinopExpr){
						Value op1=((BinopExpr) rightop).getOp1();
						Value op2=((BinopExpr) rightop).getOp2();
						
						Integer op1_toString=localWriteCountForSymval.get(op1.toString());
						Integer op2_toString=localWriteCountForSymval.get(op2.toString());
						
						//To handle a=a+1 type of situation i am placing the if-else just below earlier it was at the beginning of this block(else-if block)
						if(localWriteCountForSymval.containsKey(leftOp)) 		localWriteCountForSymval.put(leftOp, localWriteCountForSymval.get(leftOp)+1);
						else localWriteCountForSymval.put(leftOp, 1);
						//System.out.println(leftop.getType().toString());
						String lhs="SV_"+thread+"_"+leftOp+"_W"+localWriteCountForSymval.get(leftOp);
						
						if(rightop instanceof AddExpr){
							
							if(op1 instanceof Constant && op2 instanceof Constant){
								
							}
							else if(op1 instanceof Constant){
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								CV(type, lhs, op1.toString(), sym_op2, "Add");
								//System.out.println(sym_op2);
							}
							else if(op2 instanceof Constant){
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								VC(type, lhs, sym_op1, op2.toString(), "Add");
								//System.out.println(sym_op1+"-------"+op1.toString());
							}
							else{
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								VV(type, lhs, sym_op1, sym_op2, "Add");
								//System.out.println(sym_op1+"-------"+op1.toString());
								
							}
							
							//System.out.println(((BinopExpr) rightop).getOp1()+"---------------"+((BinopExpr) rightop).getOp2());
							
							
						}
						else if(rightop instanceof MulExpr){
							
							if(op1 instanceof Constant && op2 instanceof Constant){
								
							}
							else if(op1 instanceof Constant){
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								CV(type, lhs, op1.toString(), sym_op2, "Mult");
								//System.out.println(sym_op2);
							}
							else if(op2 instanceof Constant){
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								VC(type, lhs, sym_op1, op2.toString(), "Mult");
								//System.out.println(sym_op1+"-------"+op1.toString());
							}
							else{
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								VV(type, lhs, sym_op1, sym_op2, "Mult");
								//System.out.println(sym_op1+"-------"+op1.toString());

							}
							
							//System.out.println(((BinopExpr) rightop).getOp1()+"---------------"+((BinopExpr) rightop).getOp2());
						
						}
						else if(rightop instanceof DivExpr){
							
							if(op1 instanceof Constant && op2 instanceof Constant){
								
							}
							else if(op1 instanceof Constant){
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								CV(type, lhs, op1.toString(), sym_op2, "Div");
								//System.out.println(sym_op2);
							}
							else if(op2 instanceof Constant){
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								VC(type, lhs, sym_op1, op2.toString(), "Div");
								//System.out.println(sym_op1+"-------"+op1.toString());
							}
							else{
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								VV(type, lhs, sym_op1, sym_op2, "Div");
								//System.out.println(sym_op1+"-------"+op1.toString());
								
							}
							
							//System.out.println(((BinopExpr) rightop).getOp1()+"---------------"+((BinopExpr) rightop).getOp2());
							
						}
						else if(rightop instanceof RemExpr){
							
							if(op1 instanceof Constant && op2 instanceof Constant){
								
							}
							else if(op1 instanceof Constant){
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								CV(type, lhs, op1.toString(), sym_op2, "Rem");
								//System.out.println(sym_op2);
							}
							else if(op2 instanceof Constant){
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								VC(type, lhs, sym_op1, op2.toString(), "Rem");
								//System.out.println(sym_op1+"-------"+op1.toString());
							}
							else{
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								VV(type, lhs, sym_op1, sym_op2, "Rem");
								//System.out.println(sym_op1+"-------"+op1.toString());
								
							}
							
							//System.out.println(((BinopExpr) rightop).getOp1()+"---------------"+((BinopExpr) rightop).getOp2());
							
						}
						else if(rightop instanceof SubExpr){
							
							if(op1 instanceof Constant && op2 instanceof Constant){
								
							}
							else if(op1 instanceof Constant){
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								CV(type, lhs, op1.toString(), sym_op2, "Sub");
								//System.out.println(sym_op2);
							}
							else if(op2 instanceof Constant){
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								VC(type, lhs, sym_op1, op2.toString(), "Sub");
								//System.out.println(sym_op1+"-------"+op1.toString());
							}
							else{
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								VV(type, lhs, sym_op1, sym_op2, "Sub");
								//System.out.println(sym_op1+"-------"+op1.toString());
								
							}
							
							//System.out.println(((BinopExpr) rightop).getOp1()+"---------------"+((BinopExpr) rightop).getOp2());
							
							
						}
						else if(rightop instanceof XorExpr){
							
							if(op1 instanceof Constant && op2 instanceof Constant){
								
							}
							else if(op1 instanceof Constant){
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								CV(type, lhs, op1.toString(), sym_op2, "Xor");
								//System.out.println(sym_op2);
							}
							else if(op2 instanceof Constant){
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								VC(type, lhs, sym_op1, op2.toString(), "Xor");
								//System.out.println(sym_op1+"-------"+op1.toString());
							}
							else{
								String sym_op1="SV_"+thread+"_"+op1.toString()+"_W"+op1_toString;				//localWriteCountForSymval.get(op1.toString());
								String sym_op2="SV_"+thread+"_"+op2.toString()+"_W"+op2_toString;				//localWriteCountForSymval.get(op2.toString());
								VV(type, lhs, sym_op1, sym_op2, "Xor");
								//System.out.println(sym_op1+"-------"+op1.toString());
								
							}
							
							//System.out.println(((BinopExpr) rightop).getOp1()+"---------------"+((BinopExpr) rightop).getOp2());
							
							
						}
						
					}
					
					else{
																		
						if(localWriteCountForSymval.containsKey(leftOp)) localWriteCountForSymval.put(leftOp, localWriteCountForSymval.get(leftOp)+1);
						else localWriteCountForSymval.put(leftOp, 1);
						String SV_lhs="SV_"+thread+"_"+leftOp+"_W"+localWriteCountForSymval.get(leftOp);
						
						if(rightop instanceof Constant){
							switch (type){
							case "java.lang.Integer": 
								IntExpr lhs=ctx.mkIntConst(SV_lhs);
								IntNum rhs=ctx.mkInt(Integer.parseInt(rightOp));
								constraints[c++]=ctx.mkEq(lhs, rhs);
								break;
								
							case "int":
								IntExpr lhs1=ctx.mkIntConst(SV_lhs);
								IntNum rhs1=ctx.mkInt(Integer.parseInt(rightOp));
								constraints[c++]=ctx.mkEq(lhs1, rhs1);
								break;
								
							case "java.lang.Double": 
								RealExpr lhs2=ctx.mkRealConst(SV_lhs);
								RatNum rhs2=ctx.mkReal(rightOp);
								constraints[c++]=ctx.mkEq(lhs2, rhs2);
								break;
								
							case "double": 
								RealExpr lhs3=ctx.mkRealConst(SV_lhs);
								RatNum rhs3=ctx.mkReal(rightOp);
								constraints[c++]=ctx.mkEq(lhs3, rhs3);
								break;
								
							case "java.lang.Character": 
								
								break;
								
							case "char": 
								break;
								
							case "java.lang.Boolean": 
								IntExpr lhs6=ctx.mkIntConst(SV_lhs);
								IntNum rhs6=ctx.mkInt(Integer.parseInt(rightOp));
								constraints[c++]=ctx.mkEq(lhs6, rhs6);
								break;
								
							case "boolean":
								IntExpr lhs7=ctx.mkIntConst(SV_lhs);
								IntNum rhs7=ctx.mkInt(Integer.parseInt(rightOp));
								constraints[c++]=ctx.mkEq(lhs7, rhs7);
								break;
								
							}
							
							
							
						}
						
						
					}
					
					
			}
			
			
			if(rightOp.contains("locks.Lock")){
								
				lockObjMap.put(leftOp,s.getFieldRef().getField().toString());
				
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
			
			else if(s.containsFieldRef() && ( stmtStr.contains("Integer") || stmtStr.contains("Double") || stmtStr.contains("Character") || stmtStr.contains("Boolean") ||  stmtStr.contains("int ") || stmtStr.contains("double ") || stmtStr.contains("char ") || stmtStr.contains("boolean "))){
				
				
				//System.out.println(s.getFieldRef().getField().getName());
				int r=rightop.toString().length();
				int l=leftop.toString().length();
				varName=s.getFieldRef().getField().toString();
				
							
				if(l>r){
					if(writeCountForSymVal.containsKey(varName))	writeCountForSymVal.put(varName,writeCountForSymVal.get(varName)+1);
					else	writeCountForSymVal.put(varName,1);
					
					stmtToPrint= thread+", Write, "+varName+", SV_"+varName+"_W"+writeCountForSymVal.get(varName);
					programOrderConst.get(thread).put("O_"+thread+"_"+cntProgramOrder, stmtToPrint);
					
					//creating constraints that SV of lhs(static shared var) = SV of rhs(local variable)
					global_eq_local( s.getFieldRef().getType().toString(), "SV_"+varName+"_W"+writeCountForSymVal.get(varName), "SV_"+thread+"_"+rightOp+"_W"+localWriteCountForSymval.get(rightOp));
					
					
					cntProgramOrder++;
					System.out.println(stmtToPrint);
					
				}
				else{
					if(readCountForSymVal.containsKey(varName))		readCountForSymVal.put(varName,readCountForSymVal.get(varName)+1);
					else	readCountForSymVal.put(varName,1);
					
					stmtToPrint= thread+", Read, "+varName+", SV_"+varName+"_R"+readCountForSymVal.get(varName);
					programOrderConst.get(thread).put("O_"+thread+"_"+cntProgramOrder, stmtToPrint);
					
					localWriteCountForSymval.put(leftOp, 1);
					//creating constraints that SV of lhs(local) = SV of rhs(static shared var)		
					//i am not creating a new function for this, coz this is just opposite of above case
					global_eq_local(s.getFieldRef().getType().toString(), "SV_"+thread+"_"+leftOp+"_W1" , "SV_"+varName+"_R"+readCountForSymVal.get(varName));
					
					
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
				String lockName=lockObjMap.get(expr1.getBase().toString());
				
				stmtToPrint= thread+", Lock, "+lockName;
				programOrderConst.get(thread).put("O_"+thread+"_"+cntProgramOrder, stmtToPrint);
				if(!lockListMap.containsKey(lockName)) lockListMap.put(lockName, new LinkedList<MyLock>());
				
				MyLock loc=new MyLock("O_"+thread+"_"+cntProgramOrder, "");				
				lockListMap.get(lockName).addLast(loc);
				
				cntProgramOrder++;
				System.out.println(stmtToPrint);
				
			}
			else if(stmtStr.contains("void unlock()")){
				AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)s.getInvokeExpr();
				String lockName=lockObjMap.get(expr1.getBase().toString());
				stmtToPrint= thread+", Unlock, "+lockName;
				programOrderConst.get(thread).put("O_"+thread+"_"+cntProgramOrder, stmtToPrint);
				
				MyLock temp=lockListMap.get(lockName).removeLast();
				MyLock loc=new MyLock(temp.lockO, "O_"+thread+"_"+cntProgramOrder);
				lockListMap.get(lockName).addLast(loc);
				
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
	
	public void generateLockUnlockConstr(){
		Iterator<String>lockIt= lockListMap.keySet().iterator();		//iterator of list of locks(lock1, lock2)
		while(lockIt.hasNext()){
			
			String loc=lockIt.next();					//get the object list of lock into loc.     Onject list=MyLock Object which contains 'O'(order) variables-
			LinkedList<MyLock> a=lockListMap.get(loc);	//- of lock-unlock,which will be required in contraint generation
			int size=a.size();
				for(int i=0;i<size-1;i++){
			
					MyLock ob1=a.get(i);				//
					String lock1=ob1.lockO;
					String unlock1=ob1.unlockO;
					IntExpr l1=ctx.mkIntConst(lock1);
					IntExpr u1=ctx.mkIntConst(unlock1);
					for(int j=i+1;j<size;j++){
			
						MyLock ob2=a.get(j);
						String lock2=ob2.lockO;
						String unlock2=ob2.unlockO;
						IntExpr l2=ctx.mkIntConst(lock2);
						IntExpr u2=ctx.mkIntConst(unlock2);
											
						BoolExpr[] lt_OR_gt=new BoolExpr[2];
						lt_OR_gt[0]=ctx.mkLt(u1, l2);	//less than
						lt_OR_gt[1]=ctx.mkGt(l1, u2);	//greater than
						
						
						constraints[c++]=ctx.mkOr(lt_OR_gt);
						
					}
				}
		}
	}
	
	
	
	public void global_eq_local(String type,String SV_lhs,String SV_rhs){
		
		switch (type){
		case "java.lang.Integer": 
			IntExpr lhs=ctx.mkIntConst(SV_lhs);
			IntExpr rhs=ctx.mkIntConst(SV_rhs);
			constraints[c++]=ctx.mkEq(lhs, rhs);
			break;
			
		case "int":
			IntExpr lhs1=ctx.mkIntConst(SV_lhs);
			IntExpr rhs1=ctx.mkIntConst(SV_rhs);
			constraints[c++]=ctx.mkEq(lhs1, rhs1);
			break;
			
		case "java.lang.Double": 
			RealExpr lhs2=ctx.mkRealConst(SV_lhs);
			RealExpr rhs2=ctx.mkRealConst(SV_rhs);
			constraints[c++]=ctx.mkEq(lhs2, rhs2);
			break;
			
		case "double": 
			RealExpr lhs3=ctx.mkRealConst(SV_lhs);
			RealExpr rhs3=ctx.mkRealConst(SV_rhs);
			constraints[c++]=ctx.mkEq(lhs3, rhs3);
			break;
			
		case "java.lang.Character": 
			
			break;
			
		case "char": 
			break;
			
		case "java.lang.Boolean": 
			IntExpr lhs6=ctx.mkIntConst(SV_lhs);
			IntExpr rhs6=ctx.mkIntConst(SV_rhs);
			constraints[c++]=ctx.mkEq(lhs6, rhs6);
			break;
			
		case "boolean":
			IntExpr lhs7=ctx.mkIntConst(SV_lhs);
			IntExpr rhs7=ctx.mkIntConst(SV_rhs);
			constraints[c++]=ctx.mkEq(lhs7, rhs7);
			break;
			
		}
		
	}
	
	public void CC(String type,String SV_lhs,String op1,String op2,String operation){
		ArithExpr rhs=null;
		
		switch (type){
		case "java.lang.Integer": 
			IntNum[] rightExpr1=new IntNum[2];
			IntExpr lhs1=ctx.mkIntConst(SV_lhs);
			IntNum f_op1=ctx.mkInt(Integer.parseInt(op1));
			IntNum s_op1=ctx.mkInt(Integer.parseInt(op2));
			rightExpr1[0]=f_op1;
			rightExpr1[1]=s_op1;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr1);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr1);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op1, s_op1);
				break;
			case "Rem":
				rhs=ctx.mkMod(f_op1, s_op1);
				break;			
			}
			
			
			constraints[c++]=ctx.mkEq(lhs1, rhs);
			break;
			
		case "int":
			IntNum[] rightExpr2=new IntNum[2];
			IntExpr lhs2=ctx.mkIntConst(SV_lhs);
			IntNum f_op2=ctx.mkInt(Integer.parseInt(op1));
			IntNum s_op2=ctx.mkInt(Integer.parseInt(op2));
			rightExpr2[0]=f_op2;
			rightExpr2[1]=s_op2;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr2);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr2);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op2, s_op2);
				break;
			case "Rem":
				rhs=ctx.mkMod(f_op2, s_op2);
				break;			
			}
			
			
			constraints[c++]=ctx.mkEq(lhs2, rhs);
			break;
			
		case "java.lang.Double": 
			RatNum[] rightExpr3=new RatNum[2];
			RealExpr lhs3=ctx.mkRealConst(SV_lhs);
			RatNum f_op3=ctx.mkReal(op1);
			RatNum s_op3=ctx.mkReal(op2);
			rightExpr3[0]=f_op3;
			rightExpr3[1]=s_op3;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr3);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr3);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op3, s_op3);
				break;
					
			}
			
			constraints[c++]=ctx.mkEq(lhs3, rhs);
			break;
			
		case "double": 
			RatNum[] rightExpr4=new RatNum[2];
			RealExpr lhs4=ctx.mkRealConst(SV_lhs);
			RatNum f_op4=ctx.mkReal(op1);
			RatNum s_op4=ctx.mkReal(op2);
			rightExpr4[0]=f_op4;
			rightExpr4[1]=s_op4;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr4);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr4);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op4, s_op4);
				break;
					
			}
			
			constraints[c++]=ctx.mkEq(lhs4, rhs);
			break;
			
		case "java.lang.Character": 
			//TODO
			break;
			
		case "char": 
			//TODO
			break;
			
		case "java.lang.Boolean": 
			//TODO
			break;
			
		case "boolean":
			//TODO
			break;
			
		}
		
	}
	
	public void VC(String type,String SV_lhs,String op1,String op2,String operation){
		ArithExpr rhs=null;
		
		switch (type){
		case "java.lang.Integer": 
			IntExpr[] rightExpr1=new IntExpr[2];
			IntExpr lhs1=ctx.mkIntConst(SV_lhs);
			IntExpr f_op1=ctx.mkIntConst(op1);
			IntNum s_op1=ctx.mkInt(Integer.parseInt(op2));
			rightExpr1[0]=f_op1;
			rightExpr1[1]=s_op1;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr1);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr1);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op1, s_op1);
				break;
			case "Rem":
				rhs=ctx.mkMod(f_op1, s_op1);
				break;
			case "Sub":
				rhs=ctx.mkSub(rightExpr1);
				break;
			case "Xor":
				rhs=ctx.mkBV2Int(ctx.mkBVXOR(ctx.mkInt2BV(32, f_op1), ctx.mkInt2BV(32,s_op1)), true);
				break;	
			}
			
			
			constraints[c++]=ctx.mkEq(lhs1, rhs);
			break;
			
		case "int":
			IntExpr[] rightExpr2=new IntExpr[2];
			IntExpr lhs2=ctx.mkIntConst(SV_lhs);
			IntExpr f_op2=ctx.mkIntConst(op1);
			IntNum s_op2=ctx.mkInt(Integer.parseInt(op2));
			rightExpr2[0]=f_op2;
			rightExpr2[1]=s_op2;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr2);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr2);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op2, s_op2);
				break;
			case "Rem":
				rhs=ctx.mkMod(f_op2, s_op2);
				break;
			case "Sub":
				rhs=ctx.mkSub(rightExpr2);
				break;
			case "Xor":
				rhs=ctx.mkBV2Int(ctx.mkBVXOR(ctx.mkInt2BV(32, f_op2), ctx.mkInt2BV(32,s_op2)), true);
				break;	
			}
			
			constraints[c++]=ctx.mkEq(lhs2, rhs);
			break;
			
		case "java.lang.Double": 
			RealExpr[] rightExpr3=new RealExpr[2];
			RealExpr lhs3=ctx.mkRealConst(SV_lhs);
			RealExpr f_op3=ctx.mkRealConst(op1);
			RatNum s_op3=ctx.mkReal(op2);
			rightExpr3[0]=f_op3;
			rightExpr3[1]=s_op3;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr3);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr3);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op3, s_op3);
				break;
			case "Sub":
				rhs=ctx.mkSub(rightExpr3);
				break;	
					
			}
			
			constraints[c++]=ctx.mkEq(lhs3, rhs);
			break;
			
		case "double": 
			RealExpr[] rightExpr4=new RealExpr[2];
			RealExpr lhs4=ctx.mkRealConst(SV_lhs);
			RealExpr f_op4=ctx.mkRealConst(op1);
			RatNum s_op4=ctx.mkReal(op2);
			rightExpr4[0]=f_op4;
			rightExpr4[1]=s_op4;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr4);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr4);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op4, s_op4);
				break;
			case "Sub":
				rhs=ctx.mkSub(rightExpr4);
				break;	
					
			}
			
			constraints[c++]=ctx.mkEq(lhs4, rhs);
			break;
			
		case "java.lang.Character": 
			//TODO
			break;
			
		case "char": 
			//TODO
			break;
			
		case "java.lang.Boolean": 
			//TODO
			break;
			
		case "boolean":
			//TODO
			break;
			
		}
		
	}
	
	public void CV(String type,String SV_lhs,String op1,String op2,String operation){
		ArithExpr rhs=null;
		
		switch (type){
		case "java.lang.Integer": 
			IntExpr[] rightExpr1=new IntExpr[2];
			IntExpr lhs1=ctx.mkIntConst(SV_lhs);
			IntNum f_op1=	ctx.mkInt(Integer.parseInt(op1));							
			IntExpr s_op1=	ctx.mkIntConst(op2);
			rightExpr1[0]=f_op1;
			rightExpr1[1]=s_op1;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr1);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr1);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op1, s_op1);
				break;
			case "Rem":
				rhs=ctx.mkMod(f_op1, s_op1);
				break;
			case "Sub":
				rhs=ctx.mkSub(rightExpr1);
				break;
				
			}
			
			
			constraints[c++]=ctx.mkEq(lhs1, rhs);
			break;
			
		case "int":
			IntExpr[] rightExpr2=new IntExpr[2];
			IntExpr lhs2=ctx.mkIntConst(SV_lhs);
			IntNum f_op2=ctx.mkInt(Integer.parseInt(op1));								
			IntExpr s_op2=ctx.mkIntConst(op2);
			rightExpr2[0]=f_op2;
			rightExpr2[1]=s_op2;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr2);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr2);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op2, s_op2);
				break;
			case "Rem":
				rhs=ctx.mkMod(f_op2, s_op2);
				break;
			case "Sub":
				rhs=ctx.mkSub(rightExpr2);
				break;	
			}
			
			
			constraints[c++]=ctx.mkEq(lhs2, rhs);
			break;
			
		case "java.lang.Double": 
			RealExpr[] rightExpr3=new RealExpr[2];
			RealExpr lhs3=ctx.mkRealConst(SV_lhs);
			RatNum f_op3=	ctx.mkReal(op1);				
			RealExpr s_op3=ctx.mkRealConst(op2);
			rightExpr3[0]=f_op3;
			rightExpr3[1]=s_op3;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr3);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr3);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op3, s_op3);
				break;
			case "Sub":
				rhs=ctx.mkSub(rightExpr3);
				break;	
					
			}
			
			constraints[c++]=ctx.mkEq(lhs3, rhs);
			break;
			
		case "double": 
			RealExpr[] rightExpr4=new RealExpr[2];
			RealExpr lhs4=ctx.mkRealConst(SV_lhs);
			RatNum f_op4=	ctx.mkReal(op1);					
			RealExpr s_op4=ctx.mkRealConst(op2);
			rightExpr4[0]=f_op4;
			rightExpr4[1]=s_op4;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr4);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr4);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op4, s_op4);
				break;
			case "Sub":
				rhs=ctx.mkSub(rightExpr4);
				break;	
					
			}
			
			constraints[c++]=ctx.mkEq(lhs4, rhs);
			break;
			
		case "java.lang.Character": 
			//TODO
			break;
			
		case "char": 
			//TODO
			break;
			
		case "java.lang.Boolean": 
			//TODO
			break;
			
		case "boolean":
			//TODO
			break;
			
		}
		
	}
	
	public void VV(String type,String SV_lhs,String op1,String op2,String operation){
		ArithExpr rhs=null;
		
		switch (type){
		case "java.lang.Integer": 
			IntExpr[] rightExpr1=new IntExpr[2];
			IntExpr lhs1=ctx.mkIntConst(SV_lhs);
			IntExpr f_op1=	ctx.mkIntConst(op1);			
			IntExpr s_op1=	ctx.mkIntConst(op2);
			rightExpr1[0]=f_op1;
			rightExpr1[1]=s_op1;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr1);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr1);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op1, s_op1);
				break;
			case "Rem":
				rhs=ctx.mkMod(f_op1, s_op1);
				break;	
			case "Sub":
				rhs=ctx.mkSub(rightExpr1);
				break;
			}
			
			
			constraints[c++]=ctx.mkEq(lhs1, rhs);
			break;
			
		case "int":
			IntExpr[] rightExpr2=new IntExpr[2];
			IntExpr lhs2=ctx.mkIntConst(SV_lhs);
			IntExpr f_op2=ctx.mkIntConst(op1);								
			IntExpr s_op2=ctx.mkIntConst(op2);
			rightExpr2[0]=f_op2;
			rightExpr2[1]=s_op2;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr2);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr2);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op2, s_op2);
				break;
			case "Rem":
				rhs=ctx.mkMod(f_op2, s_op2);
				break;	
			case "Sub":
				rhs=ctx.mkSub(rightExpr2);
				break;	
			}
			
			
			constraints[c++]=ctx.mkEq(lhs2, rhs);
			break;
			
		case "java.lang.Double": 
			RealExpr[] rightExpr3=new RealExpr[2];
			RealExpr lhs3=ctx.mkRealConst(SV_lhs);
			RealExpr f_op3=	ctx.mkRealConst(op1);				
			RealExpr s_op3=ctx.mkRealConst(op2);
			rightExpr3[0]=f_op3;
			rightExpr3[1]=s_op3;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr3);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr3);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op3, s_op3);
				break;
			case "Sub":
				rhs=ctx.mkSub(rightExpr3);
				break;	
					
			}
			
			constraints[c++]=ctx.mkEq(lhs3, rhs);
			break;
			
		case "double": 
			RealExpr[] rightExpr4=new RealExpr[2];
			RealExpr lhs4=ctx.mkRealConst(SV_lhs);
			RealExpr f_op4=	ctx.mkRealConst(op1);					
			RealExpr s_op4=ctx.mkRealConst(op2);
			rightExpr4[0]=f_op4;
			rightExpr4[1]=s_op4;
			switch (operation){
			case "Add":
				rhs=ctx.mkAdd(rightExpr4);
				break;
			case "Mult":
				rhs=ctx.mkMul(rightExpr4);
				break;
			case "Div":
				rhs=ctx.mkDiv(f_op4, s_op4);
				break;
			case "Sub":
				rhs=ctx.mkSub(rightExpr4);
				break;	
					
			}
			
			constraints[c++]=ctx.mkEq(lhs4, rhs);
			break;
			
		case "java.lang.Character": 
			//TODO
			break;
			
		case "char": 
			//TODO
			break;
			
		case "java.lang.Boolean": 
			//TODO
			break;
			
		case "boolean":
			//TODO
			break;
			
		}
		
	}
	
	public void handleclinit(Body b){
		programOrderConst.put("0",new Hashtable<String,String>());
		cntProOrder=2;
		PatchingChain<Unit> byteU=b.getUnits();
		Iterator<Unit> iter=byteU.iterator();
		String arg_s=null;
		while(iter.hasNext()){
			Stmt s=(Stmt)iter.next();
			String stmtStr=s.toString();
			if(s instanceof AssignStmt){
				
				if(stmtStr.contains("valueOf(")){
					arg_s=s.getInvokeExpr().getArg(0).toString();					
				}
				else if( s.containsFieldRef() && stmtStr.contains("Integer")){					
					String var_Name=s.getFieldRef().getField().toString();
					writeCountForSymVal.put(var_Name, 1);
					stmtToPrint="0, Write, "+var_Name+", SV_"+var_Name+"_W1";
					programOrderConst.get("0").put("O_0_"+cntProOrder, stmtToPrint);
					clinitStmt.add(stmtToPrint);
					
					IntExpr lhs=ctx.mkIntConst("SV_"+var_Name+"_W1");
					IntNum  rhs=ctx.mkInt(Integer.parseInt(arg_s));
					constraints[c++]=ctx.mkEq(lhs, rhs);
					
					cntProOrder++;
					//syso
				}
			}
			
		}
		
	}
}
	