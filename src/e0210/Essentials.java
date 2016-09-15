package e0210;

import java.util.*;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.typing.Util;
import soot.toolkits.graph.Block;
import soot.util.HashChain;
import soot.jimple.Jimple;


public class Essentials {
	
	private PatchingChain<Unit> bEdgeChain;


	public synchronized void BL(SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> mjGraph,Map<Integer, Integer> numPaths ){
		List<Integer> rTopologicalOrder;
		//3.Generating Reverse Topological order-S
				TopologicalOrderIterator<Integer, DefaultWeightedEdge> tIterator=new TopologicalOrderIterator<>(mjGraph);
				rTopologicalOrder=new ArrayList<Integer>();
				while(tIterator.hasNext()){rTopologicalOrder.add(tIterator.next());}
				Collections.reverse(rTopologicalOrder);
				//3.Generating Reverse Topological order-E
		
		
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
	
		
	public synchronized void removeCycles(SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> mjGraph,List<DefaultWeightedEdge> delEdge){
		
		delEdge=new ArrayList<DefaultWeightedEdge>();
		//List<DefaultWeightedEdge> addEddge=new ArrayList<DefaultWeightedEdge>();
		Iterator<DefaultWeightedEdge> edgeIt=mjGraph.edgeSet().iterator();
		while(edgeIt.hasNext()){					//Recording which edges to delete
			DefaultWeightedEdge e1=edgeIt.next();
			if((mjGraph.getEdgeSource(e1)>mjGraph.getEdgeTarget(e1)) && mjGraph.getEdgeTarget(e1)!=-2){
				delEdge.add(e1);
			}
		}
		//Deleting back-edges and adding dummy edges
		edgeIt=delEdge.iterator();
		while(edgeIt.hasNext()){
			DefaultWeightedEdge e1=edgeIt.next();
			
			mjGraph.addEdge(-1, mjGraph.getEdgeTarget(e1));
			mjGraph.addEdge(mjGraph.getEdgeSource(e1), -2);
			mjGraph.removeEdge(e1);
		}		
		
	}
	
		
	
	public synchronized void instrumentation(Body b,SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> mjGraph,Map<Integer, Block> vertexMap)
	{	
		bEdgeChain = new PatchingChain<Unit>(new HashChain<Unit>());
		Unit u1,u2,u3;
		PatchingChain<Unit> bUnits=b.getUnits();
		Local pathSum,disp;
		pathSum=Jimple.v().newLocal("pathSum", LongType.v());
		disp=Jimple.v().newLocal("disp", RefType.v("java.io.PrintStream"));
		b.getLocals().add(pathSum);
		b.getLocals().add(disp);
		
		bUnits.insertBefore(Jimple.v().newAssignStmt(pathSum, LongConstant.v(0)), Util.findFirstNonIdentityUnit(b, (Stmt)bUnits.getFirst()));//initialization
		bUnits.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),Util.findFirstNonIdentityUnit(b, (Stmt)bUnits.getFirst()));
		
		
		Iterator<Integer> vertIt=mjGraph.vertexSet().iterator();
		Integer v,weight,i,j;
		Double aj,ai,d;
		while(vertIt.hasNext()){
			v=vertIt.next();		//ID of source(i.e integer value)
			if(v!=-1 && v!=-2){
				Block sb=vertexMap.get(v); //source block
				Iterator<Block> succ=sb.getSuccs().iterator();
				while(succ.hasNext()){
					Block db=succ.next();	//destination block
					int w=db.getIndexInMethod();	//ID of destination(i.e integer value)
					if(v>w){
						aj=new Double(mjGraph.getEdgeWeight(mjGraph.getEdge(-1,w)));
						ai=new Double(mjGraph.getEdgeWeight(mjGraph.getEdge(v,-2)));
						j=aj.intValue();
						i=ai.intValue();
						u1=Jimple.v().newAssignStmt(pathSum, Jimple.v().newAddExpr(pathSum, LongConstant.v(i)));	//pathsum=pathsum+i
						bEdgeChain.addLast(u1);
						u2=Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void println(long)>").makeRef(), pathSum)); //print pathsum
						bEdgeChain.addLast(u2);
						u3=Jimple.v().newAssignStmt(pathSum, LongConstant.v(j));	//pathsum=j
						bEdgeChain.addLast(u3);
						bUnits.insertOnEdge(bEdgeChain, sb.getTail(),db.getHead());						
					}
					else{
						d=new Double(mjGraph.getEdgeWeight(mjGraph.getEdge(v,w)));
						weight=d.intValue();
						bUnits.insertOnEdge(Jimple.v().newAssignStmt(pathSum, Jimple.v().newAddExpr(pathSum, LongConstant.v(weight))), sb.getTail(), db.getHead());
					}
									
				}
			}
			
		}
		Iterator<Unit> unitIt=bUnits.snapshotIterator();
		while(unitIt.hasNext()){
			Stmt s=(Stmt)unitIt.next();
			if(s instanceof ReturnVoidStmt || s instanceof ReturnStmt){
				bUnits.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void println(long)>").makeRef(), pathSum)),s);
				}
		}
				
	}

}
