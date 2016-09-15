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
import soot.Body;
import soot.BodyTransformer;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.*;
import soot.toolkits.graph.*;

public class Analysis extends BodyTransformer {
	
	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
		
		//1.Declaration-S
		ExceptionalBlockGraph graph;
		SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> mjGraph;
		Map<Integer, Block> vertexMap;
		Map<Integer, Integer> numPaths;
		List<DefaultWeightedEdge> delEdge=null;
		//1.Declaration-E
		
		graph=new ExceptionalBlockGraph(b);		//soot graph
		mjGraph=new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);	//jGraph
		numPaths =new HashMap<Integer,Integer>();		//This MAP contains total number of possible paths from each vertex
		
		//2.Populating jGraph-S
		Iterator<Block> gIterator=graph.iterator();
		vertexMap=new HashMap<Integer, Block>();
		
			
		while(gIterator.hasNext()){
			Block block = gIterator.next();
			//mjGra//4.Instrumentation-Sph.addVertex(block);
			vertexMap.put(block.getIndexInMethod(), block);
			mjGraph.addVertex(block.getIndexInMethod());
		}//Directed graph created but doesn't contain any edges
				
		gIterator=graph.iterator();
		while(gIterator.hasNext()){
			Block block = gIterator.next();
			List<Block> succL=block.getSuccs();
			Iterator<Block> sIterator=succL.iterator();
			while(sIterator.hasNext()){
				Block succ=sIterator.next();
				mjGraph.addEdge(block.getIndexInMethod(), succ.getIndexInMethod());
				
			}
		}
		Essentials obj=new Essentials();
		
		CycleDetector<Integer, DefaultWeightedEdge> cDetect=new CycleDetector<>(mjGraph);
		if(cDetect.detectCycles())
		{
			mjGraph.addVertex(-1);
			mjGraph.addVertex(-2);
			mjGraph.addEdge(-1, 0);
			Iterator<Integer> vertIt=mjGraph.vertexSet().iterator();
			while(vertIt.hasNext()){
				Integer v=vertIt.next();
				if(mjGraph.outDegreeOf(v)==0 && v!=-2) mjGraph.addEdge(v, -2);
			}
			obj.removeCycles(mjGraph,delEdge);				//Remove Cycles
		}
		
			obj.BL(mjGraph,numPaths);	//apply Ball-Larus algorithm
				
		
		obj.instrumentation(b, mjGraph, vertexMap);		//apply instrumentation
		
		/*
		//Print Edges and Edge-Weights
		
		Set<DefaultWeightedEdge>edges=mjGraph.edgeSet();
		Iterator<DefaultWeightedEdge> edgeIt=edges.iterator();
		while(edgeIt.hasNext()){
			DefaultWeightedEdge e1=edgeIt.next();
			System.out.println(e1+"--"+mjGraph.getEdgeWeight(e1));
			}
		
		System.out.println("-----------------------------------------------------------------------------------");	
		// Print Vertices
		Set<Integer>vert=mjGraph.vertexSet();
		Iterator<Integer> vertIt=vert.iterator();
		while(vertIt.hasNext()){
			int v1=vertIt.next();
			System.out.println(v1);
			}	
		*/
		
		
		
		//System.out.println(mjGraph.toString());
		//System.out.println(graph.toString());
		//System.out.println(b.toString()); //prints soot ByteCode
		return;
	}
				
}