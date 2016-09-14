package e0210;

import java.util.*;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.typing.Util;
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
				
		List<DefaultWeightedEdge> delEdge=new ArrayList<>();	//to handle deletion of edges
		
		Set<DefaultWeightedEdge> edges= mjGraph.edgeSet();
		Iterator<DefaultWeightedEdge> edgesIt=edges.iterator();
		while(edgesIt.hasNext()){
			DefaultWeightedEdge e=edgesIt.next();
			bs=mjGraph.getEdgeSource(e);
			bd=mjGraph.getEdgeTarget(e);
			if( bs> bd){
				delEdge.add(e);
				mjGraph.addEdge(sour, bd);
				mjGraph.addEdge(bs, dest);
			}
			
		}
		
	}
	public synchronized void instrumentation(Body b,SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> mjGraph,Map<Integer, Block> vertexMap)
	{
		PatchingChain<Unit> bUnits=b.getUnits();
		Local pathSum,disp;
		pathSum=Jimple.v().newLocal("pathSum", LongType.v());
		disp=Jimple.v().newLocal("disp", RefType.v("java.io.PrintStream"));
		b.getLocals().add(pathSum);
		b.getLocals().add(disp);
		
		bUnits.insertBefore(Jimple.v().newAssignStmt(pathSum, LongConstant.v(0)), Util.findFirstNonIdentityUnit(b, (Stmt)bUnits.getFirst()));//initialization
		
		Iterator<Integer> vertIt=mjGraph.vertexSet().iterator();
		Integer v,weight;
		Double d;
		while(vertIt.hasNext()){
			v=vertIt.next();
			Block sb=vertexMap.get(v); //source block
			Iterator<Block> succ=sb.getSuccs().iterator();
			while(succ.hasNext()){
				Block db=succ.next();	//destination block
				d=new Double(mjGraph.getEdgeWeight(mjGraph.getEdge(v, db.getIndexInMethod())));
				weight=d.intValue();
				bUnits.insertOnEdge(Jimple.v().newAssignStmt(pathSum, Jimple.v().newAddExpr(pathSum, LongConstant.v(weight))), sb.getTail(), db.getHead());
			}
		}
		bUnits.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),bUnits.getLast());
		bUnits.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void println(long)>").makeRef(), pathSum)),bUnits.getLast());
		
	}

}
