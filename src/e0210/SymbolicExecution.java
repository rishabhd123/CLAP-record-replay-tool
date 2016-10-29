package e0210;

import java.util.Map;

import soot.SceneTransformer;

public class SymbolicExecution extends SceneTransformer {

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {

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

}
