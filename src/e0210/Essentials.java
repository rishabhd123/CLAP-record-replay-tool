package e0210;


import java.io.IOException;
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


	public synchronized int BL(DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> mjGraph,Map<Vertex, Integer> numPaths){
		List<Vertex> rTopologicalOrder;
		//3.Generating Reverse Topological order-S
				TopologicalOrderIterator<Vertex, DefaultWeightedEdge> tIterator=new TopologicalOrderIterator<>(mjGraph);	//topological iteratot
				rTopologicalOrder=new ArrayList<Vertex>();		//List that will hold reverse topological sequence
				while(tIterator.hasNext()){rTopologicalOrder.add(tIterator.next());}
				Collections.reverse(rTopologicalOrder);
				//3.Generating Reverse Topological order-E
		
		
		for(Vertex i:rTopologicalOrder){
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
		int z=numPaths.get(rTopologicalOrder.get(rTopologicalOrder.size()-1));
		return z;
	}
	
		
	public synchronized void removeCycles(DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> mjGraph,List<DefaultWeightedEdge> delEdge){
		
		delEdge=new ArrayList<DefaultWeightedEdge>();
		//List<DefaultWeightedEdge> addEddge=new ArrayList<DefaultWeightedEdge>();
		Iterator<DefaultWeightedEdge> edgeIt=mjGraph.edgeSet().iterator();
		while(edgeIt.hasNext()){					//Recording which edges to delete
			DefaultWeightedEdge e1=edgeIt.next();
			if((mjGraph.getEdgeSource(e1).node>=mjGraph.getEdgeTarget(e1).node) && mjGraph.getEdgeTarget(e1).node!=-2){	// "==" (">=" more specifically) for self-loops
				delEdge.add(e1);
			}
		}
		//Deleting back-edges and adding dummy edges
		edgeIt=delEdge.iterator();
		Vertex vs=getVertexfromInt(mjGraph,-1);
		Vertex vd=getVertexfromInt(mjGraph,-2);
		while(edgeIt.hasNext()){
			DefaultWeightedEdge e1=edgeIt.next();
			
			mjGraph.addEdge(vs, mjGraph.getEdgeTarget(e1));
			mjGraph.addEdge(mjGraph.getEdgeSource(e1), vd);
			mjGraph.removeEdge(e1);
		}		
		
	}
	
		
	
	public synchronized void instrumentation(Body b,DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> mjGraph,Map<Vertex, Block> vertexMap,long base,SootMethod concat,SootMethod generateTuple)
	{	
		
		Unit u1,u2,u3;
		PatchingChain<Unit> bUnits=b.getUnits();
		Local pathSum,disp,blString;
		blString=Jimple.v().newLocal("blString", RefType.v("java.lang.String"));
		b.getLocals().add(blString);
		pathSum=Jimple.v().newLocal("pathSum", LongType.v());
		disp=Jimple.v().newLocal("disp", RefType.v("java.io.PrintStream"));
		b.getLocals().add(pathSum);
		b.getLocals().add(disp);
		
		bUnits.insertBefore(Jimple.v().newAssignStmt(pathSum, LongConstant.v(base)), Util.findFirstNonIdentityUnit(b, (Stmt)bUnits.getFirst()));//initialization
		bUnits.insertBefore(Jimple.v().newAssignStmt(disp, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),Util.findFirstNonIdentityUnit(b, (Stmt)bUnits.getFirst()));
		bUnits.insertBefore(Jimple.v().newAssignStmt(blString, StringConstant.v("")), Util.findFirstNonIdentityUnit(b, (Stmt)bUnits.getFirst()));
		
		Iterator<Vertex> vertIt=mjGraph.vertexSet().iterator();
		Vertex v;
		Integer weight,i,j;
		Double aj,ai,d;
		while(vertIt.hasNext()){
			v=vertIt.next();		//ID of source(i.e integer value)
			if(v.node!=-1 && v.node!=-2){
				Block sb=vertexMap.get(v); //source block
				Iterator<Block> succ=sb.getSuccs().iterator();
				while(succ.hasNext()){
					Block db=succ.next();	//destination block
					Vertex w=getVertexfromInt(mjGraph, db.getIndexInMethod());	//ID of destination(i.e integer value)
					if(v.node>=w.node){		// "==" for self-loops
						bEdgeChain = new PatchingChain<Unit>(new HashChain<Unit>());
						aj=new Double(mjGraph.getEdgeWeight(mjGraph.getEdge(getVertexfromInt(mjGraph,-1),w)));
						ai=new Double(mjGraph.getEdgeWeight(mjGraph.getEdge(v,getVertexfromInt(mjGraph,-2))));
						j=aj.intValue();
						i=ai.intValue();
						u1=Jimple.v().newAssignStmt(pathSum, Jimple.v().newAddExpr(pathSum, LongConstant.v(i)));	//pathsum=pathsum+i
						bEdgeChain.addLast(u1);
						
						
						StaticInvokeExpr concatInvExpr= Jimple.v().newStaticInvokeExpr(concat.makeRef(),blString,pathSum);
						u2=Jimple.v().newAssignStmt(blString, concatInvExpr);						
						
						bEdgeChain.addLast(u2);
						
						u3=Jimple.v().newAssignStmt(pathSum, LongConstant.v(j+base));	//pathsum=j
						bEdgeChain.addLast(u3);
						
						try{
							bUnits.insertOnEdge(bEdgeChain, sb.getTail(),db.getHead());
						}catch(Exception e){}
						
					}
					else{
						d=new Double(mjGraph.getEdgeWeight(mjGraph.getEdge(v,w)));
						weight=d.intValue();
						try{
							bUnits.insertOnEdge(Jimple.v().newAssignStmt(pathSum, Jimple.v().newAddExpr(pathSum, LongConstant.v(weight))), sb.getTail(), db.getHead());
						}catch(Exception e){}
						
					}
									
				}
			}
			
		}
		Iterator<Unit> unitIt=bUnits.snapshotIterator();
		while(unitIt.hasNext()){
			Stmt s=(Stmt)unitIt.next();
			if(s instanceof ReturnVoidStmt || s instanceof ReturnStmt){
				StaticInvokeExpr concatInvExpr= Jimple.v().newStaticInvokeExpr(concat.makeRef(),blString,pathSum);
				bUnits.insertBefore(Jimple.v().newAssignStmt(blString, concatInvExpr), s);
				
				InvokeStmt printTupleStmt= Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(generateTuple.makeRef(),blString,StringConstant.v(b.getMethod().getSubSignature())));
				bUnits.insertBefore(printTupleStmt, s);
				
				
				}
			else if(s.containsInvokeExpr()){
				String exp=s.getInvokeExpr().toString();
				if(exp.contains("staticinvoke <java.lang.System: void exit(")){

					StaticInvokeExpr concatInvExpr= Jimple.v().newStaticInvokeExpr(concat.makeRef(),blString,pathSum);
					bUnits.insertBefore(Jimple.v().newAssignStmt(blString, concatInvExpr), s);
					
					InvokeStmt printTupleStmt= Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(generateTuple.makeRef(),blString,StringConstant.v(b.getMethod().getSubSignature())));
					bUnits.insertBefore(printTupleStmt, s);
					
					//bUnits.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(disp, Scene.v().getMethod("<java.io.PrintStream: void println(long)>").makeRef(), pathSum)),s);
					
				}
					
			}
		}
				
	}
	
	public synchronized String regeneratePath(DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> mjGraph,int in,int sBL) throws IOException{
		
		//BufferedReader br=new BufferedReader(new StringReader(in));
			DirectedNeighborIndex<Vertex, DefaultWeightedEdge> dNI=new DirectedNeighborIndex<>(mjGraph);
			//String bl_str=br.readLine();	//bl_id in string form
			boolean flag=true;
			int bl;
			Vertex entry=getVertexfromInt(mjGraph, -1);
			String path="";
			bl=in-sBL;
			
				int weight,w;				//w-weight of temp chosen edge, chosenV-edge which is chosen temporarily, weight-weight of final chosen edge
				Vertex next,chosenV=null;
				while(mjGraph.outDegreeOf(entry)!=0){
					Iterator<Vertex> vIt=dNI.successorsOf(entry).iterator();
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
					
					if(chosenV.node==-2) break;
						
						if(flag) {
							path=path+chosenV.node;
							flag=false;
						}
						else
						path=path+"&"+chosenV.node;
						
						if(chosenV.exit) break;	
						
						entry=chosenV;			
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
	
	public Vertex getVertexfromInt(DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> mjGraph,int blockid) {
		Iterator<Vertex> vertIt=mjGraph.vertexSet().iterator();
		while(vertIt.hasNext()){
			Vertex v=vertIt.next();
			if(v.node==blockid){
				return v;
			}
		}
		return null;		
		
		
	}
	public Vertex createVert(int vid) {
		Vertex v=new Vertex();
		v.node=vid;
		return v;
		
	}
	
	

}