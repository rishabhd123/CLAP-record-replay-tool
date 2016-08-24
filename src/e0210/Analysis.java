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


public class Analysis extends BodyTransformer {

	
	SootField globalC;
	boolean added=false;
	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
		
		if (added)
            globalC = Scene.v().getMainClass().getFieldByName("globalC");
        else
        {
            // Add gotoCounter field
        	globalC = new SootField("globalC", LongType.v(),Modifier.STATIC);
            Scene.v().getMainClass().addField(globalC);
            added=true;
        }
		
		
		PatchingChain<Unit> byteU=b.getUnits();
		Local dynBranch,disp,gL;
		
		
		
		gL=Jimple.v().newLocal("gL", LongType.v());			//local, that will hold global value in each method
		b.getLocals().add(gL);
		
		dynBranch=Jimple.v().newLocal("dynBranch", LongType.v());		//local variable
		b.getLocals().add(dynBranch);
		
		disp=Jimple.v().newLocal("disp", RefType.v("java.io.PrintStream"));	// for printing
		b.getLocals().add(disp);
		
		byteU.insertAfter(Jimple.v().newAssignStmt(dynBranch, LongConstant.v(0)), byteU.getFirst());
		
		byteU.insertAfter(Jimple.v().newAssignStmt(gL, Jimple.v().newStaticFieldRef(globalC.makeRef())), byteU.getFirst());  
		
		Iterator iter=byteU.snapshotIterator();				
			
			while(iter.hasNext()){
				Stmt s=(Stmt)iter.next();
				if(s instanceof IfStmt){
					byteU.insertBefore(Jimple.v().newAssignStmt(dynBranch, Jimple.v().newAddExpr(dynBranch, LongConstant.v(1))), s);
					byteU.insertBefore(Jimple.v().newAssignStmt(gL, Jimple.v().newAddExpr(gL, LongConstant.v(1))), s);
					// Here i have to insert increment statement to count Dyn branching
				}
				else if(s instanceof ReturnVoidStmt || s instanceof ReturnStmt ){		
					byteU.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),s);
							
		            byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void println(long)>").makeRef(), dynBranch)),s);
		            
		            byteU.insertBefore(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(globalC.makeRef()),gL), s); //on return assign the value of gL to globalC
		            
		            if(b.getMethod().getSubSignature().equals("void main(java.lang.String[])"))            	
		            	byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void print(long)>").makeRef(),gL )),s);
		           				
				}	// handled all types of returns
				
				else if (s.containsInvokeExpr()){
					InvokeExpr exp=(InvokeExpr)s.getInvokeExpr();
					String str=exp.toString();
					if(str.contains("staticinvoke <java.lang.System: void exit(")){			//In this implementation there cant be any conflict in matching func. name
						
						byteU.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),s);
						
						byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void println(long)>").makeRef(), dynBranch)),s);
			            
			            byteU.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void print(long)>").makeRef(), gL)),s);
					}    	//handled exit(0) and
					
					else if(str.contains("staticinvoke <")){
						byteU.insertBefore(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(globalC.makeRef()),gL), s);
						byteU.insertAfter(Jimple.v().newAssignStmt(gL,Jimple.v().newStaticFieldRef(globalC.makeRef())), s);
											
					}	//handled static function calls					
						
				}
				
			}	//instrumentation done
			
			System.out.println(b.toString());
			
		
		return;
	}
}