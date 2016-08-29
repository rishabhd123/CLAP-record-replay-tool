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

import javax.management.BadBinaryOpValueExpException;

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
import soot.jimple.toolkits.typing.Util;

public class Analysis extends BodyTransformer {
	
	static SootClass sootcountClass;
	static SootMethod incrC, printC;
	static{
		sootcountClass=Scene.v().loadClassAndSupport("e0210.MyCounter");
		incrC=sootcountClass.getMethod("void increment(long)");
		printC=sootcountClass.getMethod("void printG()");
	}
	
	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
			
		PatchingChain<Unit> byteU=b.getUnits();
		Local dynBranch,disp;
				
		dynBranch=Jimple.v().newLocal("dynBranch", LongType.v());		//local variable
		b.getLocals().add(dynBranch);
		
		disp=Jimple.v().newLocal("disp", RefType.v("java.io.PrintStream"));	// for printing
		b.getLocals().add(disp);
		
		byteU.insertBefore(Jimple.v().newAssignStmt(dynBranch, LongConstant.v(0)), Util.findFirstNonIdentityUnit(b, (Stmt)byteU.getFirst()));
				
		Iterator iter=byteU.snapshotIterator();				
			
			while(iter.hasNext()){
				Stmt s=(Stmt)iter.next();
				if(s instanceof IfStmt){
					byteU.insertBefore(Jimple.v().newAssignStmt(dynBranch, Jimple.v().newAddExpr(dynBranch, LongConstant.v(1))), s);
					byteU.insertBefore( Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(incrC.makeRef(),LongConstant.v(1))),s);
					// Here i have to insert increment statement to count Dyn branching
				}
				else if(s instanceof ReturnVoidStmt || s instanceof ReturnStmt ){
					if(!(b.getMethod().getSubSignature().equals("void increment(long)") || b.getMethod().getSubSignature().equals("void printG()") || b.getMethod().getSubSignature().equals("void <clinit>()") )  ){
						byteU.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),s);
						
			            byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void println(long)>").makeRef(), dynBranch)),s);
					}
					
		            
		            if(b.getMethod().getSubSignature().equals("void main(java.lang.String[])"))            	
		            	byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(printC.makeRef())),s);
		           				
				}	// handled all types of returns
				
				else if (s.containsInvokeExpr()){
					InvokeExpr exp=(InvokeExpr)s.getInvokeExpr();
					String str=exp.toString();
					if(str.contains("staticinvoke <java.lang.System: void exit(")){			//In this implementation there cant be any conflict in matching func. name
						
						byteU.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),s);
						
						byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void println(long)>").makeRef(), dynBranch)),s);
			            
						byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(printC.makeRef())),s);
					}    	//handled exit(0)
					
										
				}
				
			}	//instrumentation done
			
			System.out.println(b.toString());
					
		return;
	}
}