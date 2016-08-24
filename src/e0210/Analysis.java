package e0210;

import java.util.*;

/*
 * @author Sridhar Gopinath		-		g.sridhar53@gmail.com
 * 
 * Course project,
 * Principles of Programming Course, Fall - 2016,
 * Computer Science and Automation (CSA),
 * Indian Institute of Science (IISc),
 * Bangalore
 */

import java.util.Map;
import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.options.Options;
import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.util.*;
import soot.jimple.Jimple;
import java.io.*;


public class Analysis extends BodyTransformer {

	
	/* 
	  static SootClass counterClass;
	  static SootMethod increaseCounter, reportCounter;

	  static {
	    counterClass    = Scene.v().loadClassAndSupport("MyCounter");
	    increaseCounter = counterClass.getMethod("void increase(int)");
	    reportCounter   = counterClass.getMethod("void report()");
	  }
	*/
	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
		
		
		
		
		PatchingChain<Unit> byteU=b.getUnits();
		Local dynBranch,disp;
		
				
		
		
		dynBranch=Jimple.v().newLocal("dynBranch", LongType.v());
		b.getLocals().add(dynBranch);
		
		disp=Jimple.v().newLocal("disp", RefType.v("java.io.PrintStream"));
		b.getLocals().add(disp);
		
		byteU.insertAfter(Jimple.v().newAssignStmt(dynBranch, LongConstant.v(0)), byteU.getFirst());
		
		
		Iterator iter=byteU.snapshotIterator();
			
			while(iter.hasNext()){
				Stmt s=(Stmt)iter.next();
				if(s instanceof IfStmt){
					byteU.insertBefore(Jimple.v().newAssignStmt(dynBranch, Jimple.v().newAddExpr(dynBranch, LongConstant.v(1))), s);
					// Here i have to insert increment statement to count Dyn branching
				}
				else if(s instanceof ReturnVoidStmt || s instanceof ReturnStmt ){		//InvokeStmt to handle System.exit(0) "|| s instanceof InvokeStmt"
					byteU.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),s);

					
					SootMethod toCall = Scene.v().getMethod("<java.io.PrintStream: void println(long)>");
		            byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, toCall.makeRef(), dynBranch)),s);
		            
		            
		            
		            SootMethod toCall1 = Scene.v().getMethod("<java.io.PrintStream: void print(long)>");
		            byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, toCall1.makeRef(), dynBranch)),s);
					
				}
				else if (s.containsInvokeExpr()){
					InvokeExpr exp=(InvokeExpr)s.getInvokeExpr();
					String str=exp.toString();
					if(str.contains("staticinvoke <java.lang.System: void exit(")){			//In this implementation there cant be any conflict in matching func. name
						
						byteU.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),s);

						
						SootMethod toCall = Scene.v().getMethod("<java.io.PrintStream: void println(long)>");
			            byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, toCall.makeRef(), dynBranch)),s);
			            
			            
			            SootMethod toCall1 = Scene.v().getMethod("<java.io.PrintStream: void print(long)>");
			            byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, toCall1.makeRef(), dynBranch)),s);
					}
					
						
				}
				
			}
			
			
			/*
			byteU.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),byteU.getLast());

			
			SootMethod toCall = Scene.v().getMethod("<java.io.PrintStream: void println(long)>");
            byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, toCall.makeRef(), dynBranch)),byteU.getLast());
            
            
            SootMethod toCall1 = Scene.v().getMethod("<java.io.PrintStream: void print(long)>");
            byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, toCall1.makeRef(), dynBranch)),byteU.getLast());
		
			*/
		
		//----------------------------------------
		
		/*
		
		PatchingChain<Unit> units = b.getUnits();
        Local arg, tmpRef,cnt;
        
        
        cnt=Jimple.v().newLocal("cnt", LongType.v());
        b.getLocals().add(cnt);
        Stmt countInit=Jimple.v().newAssignStmt(cnt, LongConstant.v(0));	//Initializing new count variable
        units.insertAfter(countInit, units.getFirst());		//inserting count(cnt) variable
       
        
        Stmt ass= Jimple.v().newAssignStmt(cnt, Jimple.v().newAddExpr(cnt, LongConstant.v(1)));
        units.insertBefore(ass, units.getLast());
        
        
        // Add some locals, java.lang.String l0   It's a String Object
            arg = Jimple.v().newLocal("l0", ArrayType.v(RefType.v("java.lang.String"), 1));
            b.getLocals().add(arg);
        
        // Add locals, java.io.printStream tmpRef
            tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
            b.getLocals().add(tmpRef);
            
        // add "l0 = @parameter0"   It can't be inserted into "main" because there is another string variable accesing @parameter0
            units.insertBefore(Jimple.v().newIdentityStmt(arg, 
                 Jimple.v().newParameterRef(ArrayType.v(RefType.v("java.lang.String"), 1), 0)),units.getLast());
        	
        // add "tmpRef = java.lang.System.out"
            units.insertBefore(Jimple.v().newAssignStmt(tmpRef, Jimple.v().newStaticFieldRef(
                Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),units.getLast());
        
        // insert "tmpRef.println("Hello world!")"
        
            SootMethod toCall = Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>");
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), StringConstant.v("Hello world!"))),units.getLast());
            
           
            Iterator stmtIt = b.getUnits().snapshotIterator();
            		while (stmtIt.hasNext())
    				{	Stmt s = (Stmt) stmtIt.next();
            			if (s instanceof IfStmt){
            			Stmt newas=Jimple.v().newAssignStmt(cnt, LongConstant.v(1111111));	
            			units.insertAfter(newas, s);
    				    
            			}
            		
    				    
    				    
    				} 
    		System.out.println(b.toString());
		
	*/	
			
			
		
		return;
	}

	
	

}