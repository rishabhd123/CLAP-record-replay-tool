package pop_replay;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import soot.Body;
import soot.Local;
import soot.LongType;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.NullConstant;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.jimple.toolkits.typing.Util;
import soot.util.Chain;

public class PoP_Replay_Instrumentor extends SceneTransformer
{	
	
	static SootClass PoP_Replay_Util;
	static SootMethod initialize,calls,printTID,countMethod,concat,criticalBefore,criticalAfter;
    
		
    @Override
    protected void internalTransform(String arg0, Map<String, String> arg1) {
    	
     		PoP_Replay_Util=Scene.v().loadClassAndSupport("pop_replay.PoP_Replay_Util");
    		calls=PoP_Replay_Util.getMethod("void calls(long)");
    		initialize=PoP_Replay_Util.getMethod("void initialize()");
    		countMethod=PoP_Replay_Util.getMethod("void countMethod(java.lang.String)");
    		concat=PoP_Replay_Util.getMethod("java.lang.String concat(java.lang.String,long)");
    		criticalBefore=PoP_Replay_Util.getMethod("void criticalBefore(java.lang.String,java.lang.String,long)");
    		criticalAfter=PoP_Replay_Util.getMethod("void criticalAfter()");
    		

    	
        Chain<SootClass> allClasses = Scene.v().getApplicationClasses();
        for (SootClass curClass: allClasses) {
        
            /* These classes must be skipped */
            if (curClass.getName().contains("pop_replay.PoP_Replay_Util")
             || curClass.getName().contains("popUtil.PoP_Util")) {
                continue;
            }
           
            List<SootMethod> allMethods = curClass.getMethods();
          
            for (SootMethod curMethod: allMethods) {  
            	if(curMethod.getSignature().contains("test") && !curMethod.getSignature().contains("<init>()"))
            	{	//Instrumenting method 
            		Body b=curMethod.getActiveBody();
            			
            		Local threadId,methodName;
            		threadId=Jimple.v().newLocal("threadId", LongType.v());
            		b.getLocals().add(threadId);
            		methodName=Jimple.v().newLocal("methodName",RefType.v("java.lang.String"));
            		b.getLocals().add(methodName);
            		
            		PatchingChain<Unit> byteChain=b.getUnits();
            		Iterator<Unit> byteChainIt=byteChain.snapshotIterator();
            		
            		if(b.getMethod().getSubSignature().equals("void main(java.lang.String[])")){		//initializes various Data structure
            			Stmt initMain=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(initialize.makeRef()));	
            			byteChain.insertBefore(initMain, Util.findFirstNonIdentityUnit(b, (Stmt)byteChain.getFirst()));
            		}
            		
            				
            		while(byteChainIt.hasNext()){
            			Stmt s=(Stmt)byteChainIt.next();
            			String stmtStr=s.toString();
            			if(s instanceof InvokeStmt){
            				InvokeExpr expr=s.getInvokeExpr();
            				if(expr.getMethod().getSubSignature().equals("void start()")){	//when a thread is forked
            					AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)expr;
            					Local baseObj=(Local)expr1.getBase();
            					
            					Stmt childId= Jimple.v().newAssignStmt(threadId, Jimple.v().newVirtualInvokeExpr(baseObj,Scene.v().getMethod("<java.lang.Thread: long getId()>").makeRef()));
            					byteChain.insertBefore(childId, s);
            					
            					Stmt callsInvoke=	Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(calls.makeRef(), threadId));
            					byteChain.insertBefore(callsInvoke, s);
            										 
            				}
            				
            				//
            				else if(stmtStr.contains("staticinvoke") && expr.getMethod().getSignature().contains("test") ){		//can add "test" type of syntax to avoid mismatching
            					Stmt methodNameAissgn= Jimple.v().newAssignStmt(methodName, StringConstant.v(expr.getMethod().getSubSignature())); //assign subSignature of method to Jimple local methodName
            					byteChain.insertBefore(methodNameAissgn, s);
            					
            					Stmt countMethodStmt=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(countMethod.makeRef(), methodName));
            					byteChain.insertBefore(countMethodStmt, s);
            					
            					
            				}
            				
            			}
            			
            		}
            		
            		//Re-Instrumentation for replay
            		
            		Local childIntId;
            		childIntId=Jimple.v().newLocal("childIntId", LongType.v());
            		b.getLocals().add(childIntId);
            		
            		Stmt previousStmt=null;
            		String tuple=null;
            		byteChainIt=byteChain.snapshotIterator();
            		while(byteChainIt.hasNext()){
            			Stmt s=(Stmt)byteChainIt.next();
            			String stmtStr=s.toString();
            			if(s instanceof AssignStmt){		//AssignStmt
            				
            				Value leftop=((AssignStmt) s).getLeftOp();
            				Value rightop=((AssignStmt) s).getRightOp();
            				String leftOp=leftop.toString();
            				String rightOp=rightop.toString();
            				if(s.containsFieldRef() && (stmtStr.contains("Integer") || stmtStr.contains("Double") )){
            					int l=leftOp.length();
            					int r=rightOp.length();
            					if(l<r){
            						Stmt critBefCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalBefore.makeRef(),StringConstant.v("Read"),StringConstant.v(rightOp),LongConstant.v(0)));
                					byteChain.insertBefore(critBefCall, s);
                					
                					Stmt critAftCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalAfter.makeRef()));
                					byteChain.insertAfter(critAftCall, s);
            					}
            					else{
            						Stmt critBefCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalBefore.makeRef(),StringConstant.v("Write"),StringConstant.v(leftOp),LongConstant.v(0)));
                					byteChain.insertBefore(critBefCall, s);
                					
                					Stmt critAftCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalAfter.makeRef()));
                					byteChain.insertAfter(critAftCall, s);
            					}
            				}
            				
            			}
            			else if(s instanceof InvokeStmt){
            				InvokeExpr invExpr=s.getInvokeExpr();
            				if(stmtStr.contains("void lock()")){
            					String lockName=((AssignStmt) previousStmt).getRightOp().toString();
            					Stmt critBefCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalBefore.makeRef(),StringConstant.v("Lock"),StringConstant.v(lockName),LongConstant.v(0)));
            					byteChain.insertBefore(critBefCall, s);
            					
            					Stmt critAftCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalAfter.makeRef()));
            					byteChain.insertAfter(critAftCall, s);
            					
            				}
            				else if(stmtStr.contains("void unlock()")){
            					String lockName=((AssignStmt) previousStmt).getRightOp().toString();
            					Stmt critBefCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalBefore.makeRef(),StringConstant.v("Unlock"),StringConstant.v(lockName),LongConstant.v(0)));
            					byteChain.insertBefore(critBefCall, s);
            					
            					Stmt critAftCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalAfter.makeRef()));
            					byteChain.insertAfter(critAftCall, s);
            				}
            				else if(stmtStr.contains("void start()")){
            					AbstractInstanceInvokeExpr expr=(AbstractInstanceInvokeExpr)invExpr;
            					Value base= expr.getBase();
            					Stmt getChildId= Jimple.v().newAssignStmt(childIntId,Jimple.v().newVirtualInvokeExpr((Local)base, Scene.v().getMethod("<java.lang.Thread: long getId()>").makeRef()) );
            					byteChain.insertBefore(getChildId, s);
            					
            					Stmt critBefCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalBefore.makeRef(),StringConstant.v("Fork"),StringConstant.v(""),childIntId));
            					byteChain.insertBefore(critBefCall, s);
            					
            					Stmt critAftCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalAfter.makeRef()));
            					byteChain.insertAfter(critAftCall, s);
            					
            					      					
            					
            					
            				}
            				else if(stmtStr.contains("void join()")){
            					AbstractInstanceInvokeExpr expr=(AbstractInstanceInvokeExpr)invExpr;
            					Value base= expr.getBase();
            					Stmt getChildId= Jimple.v().newAssignStmt(childIntId,Jimple.v().newVirtualInvokeExpr((Local)base, Scene.v().getMethod("<java.lang.Thread: long getId()>").makeRef()) );
            					byteChain.insertBefore(getChildId, s);
            					
            					Stmt critBefCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalBefore.makeRef(),StringConstant.v("Join"),StringConstant.v(""),childIntId));
            					byteChain.insertBefore(critBefCall, s);
            					
            					Stmt critAftCall=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(criticalAfter.makeRef()));
            					byteChain.insertAfter(critAftCall, s);
            					
            					
            				}
            				
            				
            			}
            		
            		
            		previousStmt=s;
            		}
            		
            		//System.out.println(b.toString());	
            	}// End IF
            } 
          
        }  
    }
}
