package e0210;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import soot.toolkits.graph.Block;

public class Essentials {
	
	public synchronized void BL(SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> mjGraph,List<Integer> rTopologicalOrder,Map<Integer, Integer> numPaths ){
		for(int i:rTopologicalOrder){
			if((mjGraph.outDegreeOf(i))==0){
				numPaths.put(i, 1);
			}
			else{
				numPaths.put(i,0);
				Set<DefaultWeightedEdge> adjE= mjGraph.outgoingEdgesOf(i);
				Iterator<DefaultWeightedEdge> edgeIt=adjE.iterator();
				while(edgeIt.hasNext()) {
					DefaultWeightedEdge t=edgeIt.next();
					mjGraph.setEdgeWeight(t,numPaths.get(i));	//Assign edge Value into edges
					numPaths.put(i,numPaths.get(i)+numPaths.get(mjGraph.getEdgeTarget(t)));
					
				}
			}
			
		}
	}
	
	public synchronized void removeCycles(SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> mjGraph){
		
		int sour=0,dest=0,temp,bs,bd;
		Iterator<Integer> vertIt=mjGraph.vertexSet().iterator();
		while(vertIt.hasNext()){
			temp=vertIt.next();
			if(mjGraph.outDegreeOf(temp)==0){
				dest=temp;
				break;
			}
		}
				
		List<DefaultWeightedEdge> delEdge=new ArrayList<>();
		
		Set<DefaultWeightedEdge> edges= mjGraph.edgeSet();
		Iterator<DefaultWeightedEdge> edgesIt=edges.iterator();
		while(edgesIt.hasNext()){
			DefaultWeightedEdge e=edgesIt.next();
			bs=mjGraph.getEdgeSource(e);
			bd=mjGraph.getEdgeTarget(e);
			if( bs> bd){
				mjGraph.removeAllEdges(bs, bd);
				mjGraph.addEdge(sour, bd);
				mjGraph.addEdge(bs, dest);
			}
			
		}
		
	}
	

}
