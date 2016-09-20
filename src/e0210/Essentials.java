package e0210;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.typing.Util;
import soot.toolkits.graph.Block;
import soot.util.HashChain;
import soot.jimple.Jimple;


public class Essentials {
	
	private PatchingChain<Unit> bEdgeChain;


	public synchronized void BL(DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> mjGraph,Map<Integer, Integer> numPaths ){
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
	
		
	public synchronized void removeCycles(DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> mjGraph,List<DefaultWeightedEdge> delEdge){
		
		delEdge=new ArrayList<DefaultWeightedEdge>();
		//List<DefaultWeightedEdge> addEddge=new ArrayList<DefaultWeightedEdge>();
		Iterator<DefaultWeightedEdge> edgeIt=mjGraph.edgeSet().iterator();
		while(edgeIt.hasNext()){					//Recording which edges to delete
			DefaultWeightedEdge e1=edgeIt.next();
			if((mjGraph.getEdgeSource(e1)>=mjGraph.getEdgeTarget(e1)) && mjGraph.getEdgeTarget(e1)!=-2){	// "==" (">=" more specifically) for self-loops
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
	
		
	
	public synchronized void instrumentation(Body b,DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> mjGraph,Map<Integer, Block> vertexMap)
	{	
		
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
					if(v>=w){		// "==" for self-loops
						bEdgeChain = new PatchingChain<Unit>(new HashChain<Unit>());
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
				bUnits.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void print(long)>").makeRef(), pathSum)),s);		//println
				}
			else if(s.containsInvokeExpr()){
				String exp=s.getInvokeExpr().toString();
				if(exp.contains("staticinvoke <java.lang.System: void exit("))
					bUnits.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void print(long)>").makeRef(), pathSum)),s);
			}
		}
				
	}
	
	public synchronized String regeneratePath(DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> mjGraph,String in,Map<Integer, Boolean> exit_Map) throws IOException{//only for acyclic graph and single method
			BufferedReader br=new BufferedReader(new StringReader(in));
			DirectedNeighborIndex<Integer, DefaultWeightedEdge> dNI=new DirectedNeighborIndex<>(mjGraph);
			String bl_str=br.readLine();	//bl_id in string form
			int bl,entry=0;
			String path="0";
			bl=Integer.parseInt(bl_str);
			while(true){
				int weight,next,w,chosenV=0;	//w-weight of temp chosen edge.chosenV-edge which is chosen temporarily
												//weight-weight of final chosen edge
				while(mjGraph.outDegreeOf(entry)!=0){
					Iterator<Integer> vIt=dNI.successorsOf(entry).iterator();
					weight=0;
					while(vIt.hasNext()){
						next=vIt.next();
						w=(new Double(mjGraph.getEdgeWeight(mjGraph.getEdge(entry, next)))).intValue();	//edge-weight of "entry->next" edge
						if(w>=weight &&  w<=bl){
							weight=w;
							chosenV=next;						
						}
						
					}
					bl-=weight;
					
					if(chosenV==-2) break;
					
						path=path+"\n"+chosenV;
						if(exit_Map.get(chosenV)) break;					
						entry=chosenV;
						
					
					
			}
				
				bl_str=br.readLine();
				if(bl_str==null)
					break;
				
				
				bl=Integer.parseInt(bl_str);
				
				Iterator<Integer> adjVert=dNI.successorListOf(-1).iterator(); //adjacent vertices to -1
				int next1,w1,weight1=0,chosenV1=0;
				while(adjVert.hasNext()){
					next1=adjVert.next();
					w1=(new Double(mjGraph.getEdgeWeight(mjGraph.getEdge(-1, next1)))).intValue();
					if(w1>=weight1 &&  w1<=bl){
						weight1=w1;
						chosenV1=next1;						
					}
					
				}
				bl-=weight1;
				path=path+"\n"+chosenV1;
				entry=chosenV1;
				
		}
		
	return path;
	}
	
	public boolean isExit(Block block){
	
		Iterator<Unit> blockIt=block.iterator();
		while(blockIt.hasNext()){
			Stmt s=(Stmt)blockIt.next();
			if(s.containsInvokeExpr()){
				String exp=s.getInvokeExpr().toString();
				if(exp.contains("staticinvoke <java.lang.System: void exit("))
					return true;
			}
		}
		return false;
	
	}

}
