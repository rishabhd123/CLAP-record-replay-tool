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
import soot.*;
import soot.jimple.*;
import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.toolkits.typing.Util;

public class Analysis extends BodyTransformer {
	
	static SootClass sootcountClass;
	static SootMethod incrC, printC;
	static{
		sootcountClass=Scene.v().loadClassAndSupport("e0210.MyCounter");		
		incrC=sootcountClass.getMethod("void increment(long)");
		printC=sootcountClass.getMethod("void printG(long)");
	}
	SootField gl;
	//boolean added=false;
	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
		
		
		PatchingChain<Unit> byteU=b.getUnits();
		Local dynBranch,disp,globe=null;
		
		
		if(b.getMethod().getSubSignature().equals("void main(java.lang.String[])"))
		{	
			gl=new SootField("gl",LongType.v(),Modifier.STATIC);
			Scene.v().getMainClass().addField(gl);
			
			globe=Jimple.v().newLocal("globe", LongType.v());
			b.getLocals().add(globe);
			
			Stmt global=Jimple.v().newAssignStmt(globe, Jimple.v().newStaticFieldRef(gl.makeRef()));
			
			
			//gl=Scene.v().getMainClass().getFieldByName("gl");
			byteU.insertBefore(global, Util.findFirstNonIdentityUnit(b, (Stmt)byteU.getFirst()));
		}				
		
		
		dynBranch=Jimple.v().newLocal("dynBranch", LongType.v());		//local variable
		b.getLocals().add(dynBranch); 		//get all the locals of current method i.e to which 'b' is refering
		
		disp=Jimple.v().newLocal("disp", RefType.v("java.io.PrintStream"));	// for printing
		b.getLocals().add(disp);
		
		byteU.insertBefore(Jimple.v().newAssignStmt(dynBranch, LongConstant.v(0)), Util.findFirstNonIdentityUnit(b, (Stmt)byteU.getFirst()));
		
				
		Iterator<Unit> iter=byteU.snapshotIterator();				
			
			while(iter.hasNext()){
				Stmt s=(Stmt)iter.next();
				if(s instanceof IfStmt && !(b.getMethod().getSubSignature().equals("void printG(long)"))){
					byteU.insertBefore(Jimple.v().newAssignStmt(dynBranch, Jimple.v().newAddExpr(dynBranch, LongConstant.v(1))), s);
					//byteU.insertBefore( Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(incrC.makeRef(),LongConstant.v(1))),s);
					// Here i have to insert increment statement to count Dyn branching
				}
				else if(s instanceof ReturnVoidStmt || s instanceof ReturnStmt ){
					//byteU.insertBefore( Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(incrC.makeRef(),dynBranch)),s);
					if(!(b.getMethod().getSubSignature().equals("void increment(long)") || b.getMethod().getSubSignature().equals("void printG(long)") || b.getMethod().getSubSignature().equals("void <clinit>()") )  ){
						byteU.insertBefore( Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(incrC.makeRef(),dynBranch)),s);
						byteU.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),s);
						
			            byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void println(long)>").makeRef(), dynBranch)),s);
					}
					
		            
		            if(b.getMethod().getSubSignature().equals("void main(java.lang.String[])"))
		            {
		            	
		            	byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(printC.makeRef(),globe)),s);// we need to modify
		            }
		           				
				}	// handled all types of returns
				
				else if (s.containsInvokeExpr()){
					InvokeExpr exp=(InvokeExpr)s.getInvokeExpr();		//  (InvokeExpr)s.getInvokeExpr();
					String str=exp.toString();
					if(str.contains("staticinvoke <java.lang.System: void exit(")){			//In this implementation there cant be any conflict in matching func. name
						
						byteU.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),s);
						
						byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void println(long)>").makeRef(), dynBranch)),s);
						byteU.insertBefore( Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(incrC.makeRef(),dynBranch)),s);
						byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(printC.makeRef(),LongConstant.v(0))),s);
					}    	//handled exit(0)
					else if(str.contains("void main(java.lang.String[])>(")){
						byteU.insertBefore(Jimple.v().newAssignStmt(globe,Jimple.v().newStaticFieldRef(gl.makeRef())), s);
						byteU.insertBefore(Jimple.v().newAssignStmt(globe, Jimple.v().newAddExpr(globe, LongConstant.v(1))), s);
						byteU.insertBefore(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(gl.makeRef()),globe), s);
						//after
						
						byteU.insertAfter(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(gl.makeRef()),globe), s);
						byteU.insertAfter(Jimple.v().newAssignStmt(globe, Jimple.v().newAddExpr(globe, LongConstant.v(-1))), s);
						byteU.insertAfter(Jimple.v().newAssignStmt(globe,Jimple.v().newStaticFieldRef(gl.makeRef())), s);
						
					}
					
										
				}
				
			}	//instrumentation done
			
			System.out.println(b.toString());
					
		return;
	}
}