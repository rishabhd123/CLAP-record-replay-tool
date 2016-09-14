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

import soot.*;
import soot.jimple.*;
import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.toolkits.typing.Util;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.jgrapht.*;

import soot.toolkits.graph.*;

public class Analysis extends BodyTransformer {
	/*
	ExceptionalBlockGraph graph;
	List<Block> gBlocks;
	SimpleDirectedWeightedGraph<Integer, DefaultEdge> mjGraph;
	Map<Integer, Block> gMap;
	Map<DefaultEdge, Integer> edgeVal;
	Map<Integer, Integer> numPaths;
	List<Integer> rTopologicalOrder;
	*/
	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
		
		//Declaration
		ExceptionalBlockGraph graph;
		List<Block> gBlocks;
		SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> mjGraph;
		Map<Integer, Block> vertexMap;
		Map<Integer, Integer> numPaths;
		List<Integer> rTopologicalOrder;	
		
		
		graph=new ExceptionalBlockGraph(b);
		//System.out.println(graph.toString()); //Print the whole graph
		gBlocks= graph.getBlocks();
		//System.out.println(gBlocks); // Print the list of node(Blocks)
		mjGraph=new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
		
		Iterator<Block> gIterator=graph.iterator();
		vertexMap=new HashMap<Integer, Block>();
		while(gIterator.hasNext()){
			Block block = gIterator.next();
			//mjGraph.addVertex(block);
			vertexMap.put(block.getIndexInMethod(), block);
			mjGraph.addVertex(block.getIndexInMethod());
		}//Directed graph created but doesn't contain any edges
				
		gIterator=graph.iterator();
		while(gIterator.hasNext()){
			Block block = gIterator.next();
			List<Block> succL=block.getSuccs();
			Iterator<Block> sIterator=succL.iterator();
			//System.out.println(block.getIndexInMethod());
			//System.out.println(block.getTail());
			//System.out.println("Rishabh");
			while(sIterator.hasNext()){
				Block succ=sIterator.next();
				mjGraph.addEdge(block.getIndexInMethod(), succ.getIndexInMethod());
				
			}
		}
		
		
		TopologicalOrderIterator<Integer, DefaultWeightedEdge> tIterator=new TopologicalOrderIterator<>(mjGraph);
		rTopologicalOrder=new ArrayList<Integer>();
		while(tIterator.hasNext()){rTopologicalOrder.add(tIterator.next());}
		
		Collections.reverse(rTopologicalOrder);
		
		
		numPaths =new HashMap<Integer,Integer>();		//This MAP contains total number of possible paths from each vertex
		Essentials obj=new Essentials();
		obj.BL(mjGraph, rTopologicalOrder, numPaths);	//apply Ball-Larus algorithm
		
		Iterator<DefaultWeightedEdge> edIt=mjGraph.edgeSet().iterator();
		while (edIt.hasNext()) {
			DefaultWeightedEdge e1=edIt.next();
			System.out.println(e1+"--"+mjGraph.getEdgeWeight(e1));
			
		}
		//System.out.println(numPaths.toString());
		
		/*	
		//Print Edges and Edge-Weights
		 
		 Set<DefaultWeightedEdge>edges=mjGraph.edgeSet();
		Iterator<DefaultWeightedEdge> edgeIt=edges.iterator();
		while(edgeIt.hasNext()){
			DefaultWeightedEdge e1=edgeIt.next();
			System.out.println(e1+"--"+mjGraph.getEdgeWeight(e1));
			}
		
		// Print Vertices
		Set<Integer>vert=mjGraph.vertexSet();
		Iterator<Integer> vertIt=vert.iterator();
		while(vertIt.hasNext()){
			int v1=vertIt.next();
			System.out.println(v1);
			}	
		*/
		
		
		
		
		//System.out.println(graph.toString());
		//System.out.println(b.toString()); //prints soot ByteCode
		return;
	}
	
	
	
	/*
	 * EdgeValue Alternative-------Use MAP Instead of Edge Weights
	 * Map<DefaultEdge, Integer> edgeVal;
	 * 
	 * 0.SimpleDirectedWeightedGraph<Integer, DefaultEdge> mjGraph;
	 * 	 mjGraph=new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
	 * 	
	 * 1.edgeVal=new HashMap<DefaultEdge, Integer>();	//This MAP contains edge-value assignments
	 * 
	 * 2.BL(mjGraph,rTopologicalOrder,numPaths,edgeVal);		//apply Ball-Larus algorithm
	 * 
	 * 3.public synchronized void BL(SimpleDirectedWeightedGraph<Integer, DefaultEdge> mjGraph,List<Integer> rTopologicalOrder,Map<Integer, Integer> numPaths,Map<DefaultEdge, Integer> edgeVal ){
			for(int i:rTopologicalOrder){
				if((mjGraph.outDegreeOf(i))==0){
				numPaths.put(i, 1);
				}
				else{
				numPaths.put(i,0);
				Set<DefaultEdge> adjE= mjGraph.outgoingEdgesOf(i);
				Iterator<DefaultEdge> edgeIt=adjE.iterator();
				while(edgeIt.hasNext()) {
					DefaultEdge t=edgeIt.next();
					edgeVal.put(t, numPaths.get(i));		//Assign edge Value into MAP
					numPaths.put(i,numPaths.get(i)+numPaths.get(mjGraph.getEdgeTarget(t)));
					
					}
				}
			
			}
		}
		
	*
	* 4.// Print Edge-value pairs
		Set<DefaultEdge> de=edgeVal.keySet();
		Iterator<DefaultEdge> edgeIt= de.iterator();
		while(edgeIt.hasNext()){
			DefaultEdge e1=edgeIt.next();
			System.out.println(e1+"--"+edgeVal.get(e1));
	* */
	
	
	
	
}