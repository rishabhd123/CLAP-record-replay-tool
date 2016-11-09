 package e0210;

/*
 * @author Sridhar Gopinath		-		g.sridhar53@gmail.com
 * 
 * Course project,
 * Principles of Programming Course, Fall - 2016,
 * Computer Science and Automation (CSA),
 * Indian Institute of Science (IISc),
 * Bangalore
 */

import java.util.*;
import java.io.*;
import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.jimple.toolkits.typing.Util;
import soot.toolkits.graph.*;
import soot.toolkits.graph.ExceptionalBlockGraph;
import org.jgrapht.graph.*;

import org.jgrapht.alg.CycleDetector;


public class Analysis extends BodyTransformer {
	
	static SootClass MyCounter;
	static SootMethod initialize,calls,printTID,countMethod,concat,generateTuple;
	static{	//always reflect changes in SootMethodList and in String dntPerformInternalTransform
		MyCounter=Scene.v().loadClassAndSupport("e0210.MyCounter");
		calls=MyCounter.getMethod("void calls(long)");
		initialize=MyCounter.getMethod("void initialize()");
		countMethod=MyCounter.getMethod("void countMethod(java.lang.String)");
		concat=MyCounter.getMethod("java.lang.String concat(java.lang.String,long)");
		generateTuple=MyCounter.getMethod("void generateTuple(java.lang.String,java.lang.String)");
		
	}
	
	
	String dntPerformInternalTransform ="void initialize() void calls(long) void increment(long) void printG(long) void printTID() void countMethod(java.lang.String)"+
			" java.lang.String concat(java.lang.String,long) void generateTuple(java.lang.String,java.lang.String) void randomDelay() void randomDelay(int)";
	int base=0;												//void <init>() void <clinit>()
	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
		
		
		
		String currentMethod=b.getMethod().getSignature();
		
		
		if(currentMethod.contains("MyCounter") || currentMethod.contains("PoP_Util")) return;
		//System.out.println(b.getMethod().getSubSignature());
		
		
		System.out.println(b.toString());
		
		/*
		// Create List of SootMethod-Start
		List<SootMethod> sootMethodList=new ArrayList<SootMethod>();
		sootMethodList.add(countMethod);
		sootMethodList.add(calls);
		sootMethodList.add(initialize);
		*/
		// Create List of SootMethod-Start
		
		/*Things Related to graph*/
		
		
		
		
		
		
	//--------------------------------------------------------Instrumentation-------------------------------------------------------------------------	
	// Motive::- get first 3 elements of Tuple <Method Signature, Thread ID, # times this method was called by this thread before this invocation>
	
	
			
	Local threadId,methodName;
	threadId=Jimple.v().newLocal("threadId", LongType.v());
	b.getLocals().add(threadId);
	methodName=Jimple.v().newLocal("methodName",RefType.v("java.lang.String"));
	b.getLocals().add(methodName);
		
		PatchingChain<Unit> byteChain=b.getUnits();
		Iterator<Unit> byteChainIt=byteChain.snapshotIterator();
		
		if(b.getMethod().getSubSignature().equals("void main(java.lang.String[])")){		//initializes various Data struct
			Stmt initMain=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(initialize.makeRef()));	
			byteChain.insertBefore(initMain, Util.findFirstNonIdentityUnit(b, (Stmt)byteChain.getFirst()));
		}
		
				
		while(byteChainIt.hasNext()){
			Stmt s=(Stmt)byteChainIt.next();
			if(s instanceof InvokeStmt){
				InvokeExpr expr=s.getInvokeExpr();
				if(expr.getMethod().getSubSignature().equals("void start()")){
					AbstractInstanceInvokeExpr expr1=(AbstractInstanceInvokeExpr)expr;
					Local baseObj=(Local)expr1.getBase();
					
					Stmt childId= Jimple.v().newAssignStmt(threadId, Jimple.v().newVirtualInvokeExpr(baseObj,Scene.v().getMethod("<java.lang.Thread: long getId()>").makeRef()));
					byteChain.insertBefore(childId, s);
					
					Stmt callsInvoke=	Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(calls.makeRef(), threadId));
					byteChain.insertBefore(callsInvoke, s);
										 
				}
				
				else if(s.toString().contains("staticinvoke")){		//can add test1 type of syntax to avoid mismatching
					Stmt methodNameAissgn= Jimple.v().newAssignStmt(methodName, StringConstant.v(expr.getMethod().getSubSignature())); //assign subSignature of method to Jimple local methodName
					byteChain.insertBefore(methodNameAissgn, s);
					
					Stmt countMethodStmt=Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(countMethod.makeRef(), methodName));
					byteChain.insertBefore(countMethodStmt, s);
					
					
				}
				
			}			
		}
		// Done <Method Signature, Thread ID, # times this method was called by this thread before this invocation>
		//------------------------------------------------------------------Instrumentation Done------------------------------------------------------------------
		
		//Ball-Larus Implementation-Start
		ExceptionalBlockGraph graph;
		graph=new ExceptionalBlockGraph(b); //Creating graph here so that instrumentation regarding threads will not be reflected in graph
		DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> mjGraph;
		Map<Vertex, Block> vertexMap;
		Map<Vertex, Integer> numPaths;
		List<DefaultWeightedEdge> delEdge=null;
		
		Essentials obj=new Essentials();
		
		mjGraph=new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);		//Create empty jgraph
		vertexMap=new HashMap<Vertex,Block>();			//Vertex-Block Map
		numPaths=new HashMap<Vertex,Integer>();			//Vertex-numberOfPaths map
		Iterator<Block> gIterator=graph.iterator();
		
		while(gIterator.hasNext()){
			Block block = gIterator.next();
			int block_index=block.getIndexInMethod();
			Vertex v=new Vertex();
			v.node=block_index;
			v.exit=obj.isExit(block);
			vertexMap.put(v, block);
			mjGraph.addVertex(v);
		}//Directed graph created but doesn't contain any edges
		
				
		gIterator=graph.iterator();
		while(gIterator.hasNext()){
			Block block = gIterator.next();
			List<Block> succL=block.getSuccs();
			Iterator<Block> sIterator=succL.iterator();
			while(sIterator.hasNext()){
				Block succ=sIterator.next();
				Vertex src=obj.getVertexfromInt(mjGraph, block.getIndexInMethod());
				Vertex dest=obj.getVertexfromInt(mjGraph, succ.getIndexInMethod());
				mjGraph.addEdge(src,dest);
				
			}
		}	//Edges added to mjGraph
		//2.Populating jGraph-E
		
		
		
		Vertex vs=obj.createVert(-1);	//initial dummy i.e. -1
		Vertex vd=obj.createVert(-2);	//final dummy i.e. -2
		mjGraph.addVertex(vs);
		mjGraph.addVertex(vd);
		mjGraph.addEdge(vs, obj.getVertexfromInt(mjGraph,0));
		Iterator<Vertex> vertIt=mjGraph.vertexSet().iterator();
		while(vertIt.hasNext()){
			Vertex v=vertIt.next();
			if(mjGraph.outDegreeOf(v)==0 && v.node!=-2) mjGraph.addEdge(v, vd);
		}
		
		CycleDetector<Vertex, DefaultWeightedEdge> cDetect=new CycleDetector<>(mjGraph);
		if(cDetect.detectCycles())
		{	
			obj.removeCycles(mjGraph,delEdge);				//Remove Cycles
		}
		
			int nPath= obj.BL(mjGraph,numPaths);	//apply Ball-Larus algorithm and returns number of paths in current method 
			synchronized (this) {
				obj.instrumentation(b, mjGraph, vertexMap,base,concat,generateTuple);
				Iterator<Vertex> vIt=mjGraph.vertexSet().iterator();
				while(vIt.hasNext()){
					Vertex v=vIt.next();
					v.sBL=base;
					v.eBl=base+nPath-1;
				}
				base+=nPath;
			}
			
			
			//Graph Exporter
			DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> gr=null; //auxillary graph object
			List<DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>> listOfGraph=new ArrayList<>();
			
			synchronized (this) {
				try{					
				//First Read all objects from file
					
				FileInputStream f_g_in=new FileInputStream("sootOutput/gr.ser");
				if(f_g_in.available()==0){listOfGraph.add(mjGraph);}
				
				else if(f_g_in.available()>0){
						
					ObjectInputStream ob_g_in=new ObjectInputStream(f_g_in);					
					try {
						
						gr=(DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>) ob_g_in.readObject();
						while(gr!=null){
							listOfGraph.add(gr);
							gr= (DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>) ob_g_in.readObject();
						}
					} catch (Exception e) {
						//e.printStackTrace();
					}
					listOfGraph.add(mjGraph);
					ob_g_in.close();
				}
				f_g_in.close();
					
					
				//Write Object of Graph
				FileOutputStream f_g_out=new FileOutputStream("sootOutput/gr.ser");
				ObjectOutputStream ob_g_out=new ObjectOutputStream(f_g_out);
				Iterator<DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>> graphListIt=listOfGraph.iterator();
				while(graphListIt.hasNext()){
					gr=graphListIt.next();
					ob_g_out.writeObject(gr);
				}
				
				
				ob_g_out.close();
				f_g_out.close();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			
			}	//Graph Exporter
			
		

			
		
		
		
		//Ball-Larus Implementation-End
		/*
		Iterator<ValueBox> boxIt=b.getUseBoxes().iterator();
		while(boxIt.hasNext()) System.out.println(boxIt.next());
		 */
		//System.out.println(graph);				
		//System.out.println(b);
		return;
	}
	
				
}
 
 
 
 
 
 
 
 
 
 
 
 // Modified Analysis for Project 3
 /*
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/*
 * @author Sridhar Gopinath		-		g.sridhar53@gmail.com
 * 
 * Course project,
 * Principles of Programming Course, Fall - 2016,
 * Computer Science and Automation (CSA),
 * Indian Institute of Science (IISc),
 * Bangalore
 */
/*
import java.util.Map;

import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;

import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalBlockGraph;

public class Analysis extends BodyTransformer {

	DirectedPseudograph<Block, DefaultEdge> graph = new DirectedPseudograph<Block, DefaultEdge>(DefaultEdge.class);

	@Override
	protected synchronized void internalTransform(Body b, String phaseName, Map<String, String> options) {

		ExceptionalBlockGraph cfg = new ExceptionalBlockGraph(b);

		for (Block block : cfg.getBlocks()) {
			graph.addVertex(block);
		}

		for (Block block : cfg.getBlocks()) {
			for (Block succ : cfg.getSuccsOf(block))
				graph.addEdge(block, succ);
		}

		System.out.println(b.toString());

		return;
	}

	public void finish(String testcase) {

		VertexNameProvider<Block> id = new VertexNameProvider<Block>() {

			@Override
			public String getVertexName(Block b) {
				return String.valueOf("\"" + b.getBody().getMethod().getNumber() + " " + b.getIndexInMethod() + "\"");
			}
		};

		VertexNameProvider<Block> name = new VertexNameProvider<Block>() {

			@Override
			public String getVertexName(Block b) {
				String body = b.toString().replace("\'", "").replace("\"", "");
				return body;
			}
		};

		new File("sootOutput").mkdir();

		DOTExporter<Block, DefaultEdge> exporter = new DOTExporter<Block, DefaultEdge>(id, name, null);
		try {
			exporter.export(new PrintWriter("sootOutput/" + testcase + ".dot"), graph);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return;
	}

}*/