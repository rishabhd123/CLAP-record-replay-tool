package pop_replay;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;

public class PoP_Replay_Instrumentor extends SceneTransformer
{	
	
	static SootClass PoP_Replay_Util;
	static SootMethod initialize,calls,printTID,countMethod,concat,generateTuple;
	static{	
		PoP_Replay_Util=Scene.v().loadClassAndSupport("pop_replay.PoP_Replay_Util");
		calls=PoP_Replay_Util.getMethod("void calls(long)");
		initialize=PoP_Replay_Util.getMethod("void initialize()");
		countMethod=PoP_Replay_Util.getMethod("void countMethod(java.lang.String)");
		concat=PoP_Replay_Util.getMethod("java.lang.String concat(java.lang.String,long)");
		generateTuple=PoP_Replay_Util.getMethod("void generateTuple(java.lang.String,java.lang.String)");
		
	}
    
	/*
	static SootClass MyCounter;
	static SootMethod initialize,calls,printTID,countMethod,concat,generateTuple;
	static{	//always reflect changes in SootMethodList and in String dntPerformInternalTransform
		MyCounter=Scene.v().loadClassAndSupport("e0210.MyCounter");
		calls=MyCounter.getMethod("void calls(long)");
		initialize=MyCounter.getMethod("void initialize()");
		countMethod=MyCounter.getMethod("void countMethod(java.lang.String)");
		concat=MyCounter.getMethod("java.lang.String concat(java.lang.String,long)");
		generateTuple=MyCounter.getMethod("void generateTuple(java.lang.String,java.lang.String)");
		
	}*/
	
    @Override
    protected void internalTransform(String arg0, Map<String, String> arg1) {
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
            	{		System.out.println(curMethod.getSignature()+"----------------------");
            			PatchingChain<Unit> byteU=curMethod.getActiveBody().getUnits();
            			Iterator<Unit> stmtIt=byteU.snapshotIterator();
            			while(stmtIt.hasNext()){
            				Stmt s=(Stmt)stmtIt.next();
            				if(s.containsFieldRef()){
            					System.out.println(s.toString());
            				}
            			}
            		
            	}
            } 
          
        }  
    }
}
