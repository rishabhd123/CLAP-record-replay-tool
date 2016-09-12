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
import soot.jimple.toolkits.callgraph.TopologicalOrderer;
import soot.jimple.toolkits.typing.Util;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.jgrapht.*;

import soot.toolkits.graph.*;

public class Analysis extends BodyTransformer {
	
	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
		
		ExceptionalBlockGraph graph=new ExceptionalBlockGraph(b);
		//System.out.println(graph.toString()); //Print the whole graph
		List<Block> gBlocks= graph.getBlocks();
		//System.out.println(gBlocks); // Print the list of node(Blocks)
		SimpleDirectedWeightedGraph<Block, DefaultEdge> mjGraph=new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
		Iterator<Block> gIterator=graph.iterator();
		Map<Integer, Block> gMap=new HashMap<Integer, Block>();
		while(gIterator.hasNext()){
			Block block = gIterator.next();
			mjGraph.addVertex(block);
			gMap.put(block.getIndexInMethod(), block);
		}//Directed graph created but doesn't contain any edges
		gIterator=graph.iterator();
		while(gIterator.hasNext()){
			Block block = gIterator.next();
			List<Block> succL=block.getSuccs();
			Iterator<Block> sIterator=succL.iterator();
			System.out.println(block.getIndexInMethod());
			while(sIterator.hasNext()){
				Block succ=sIterator.next();
				mjGraph.addEdge(block, succ);
				
			}
		}
		TopologicalOrderIterator<Block, DefaultEdge> tIterator=new TopologicalOrderIterator<>(mjGraph);
		List<Block> rTopologicalOrder=new ArrayList<Block>();
		while(tIterator.hasNext()){rTopologicalOrder.add(tIterator.next());}
		
		Collections.reverse(rTopologicalOrder);
		//System.out.println(rTopologicalOrder.toString()); //print Reverse_topological order 
		
		
		//System.out.println(b.toString()); //prints soot ByteCode
		
		return;
	}
}