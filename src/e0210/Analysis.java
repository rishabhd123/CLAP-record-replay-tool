package e0210;


import java.io.FileInputStream;
/*
 * @author Sridhar Gopinath		-		g.sridhar53@gmail.com
 * 
 * Course project,
 * Principles of Programming Course, Fall - 2016,
 * Computer Science and Automation (CSA),
 * Indian Institute of Science (IISc),
 * Bangalore
 */
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import soot.Body;
import soot.BodyTransformer;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.*;
import soot.toolkits.graph.*;

public class Analysis extends BodyTransformer {
	
	int base=0;
	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
		
		//1.Declaration-S
		ExceptionalBlockGraph graph;
		DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> mjGraph;
		Map<Vertex, Block> vertexMap;
		Map<Vertex, Integer> numPaths;
		List<DefaultWeightedEdge> delEdge=null;
		//1.Declaration-E
		
		graph=new ExceptionalBlockGraph(b);		//soot graph
		System.out.println(graph);
		mjGraph=new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);	//jGraph
		
		numPaths =new HashMap<Vertex,Integer>();		//This MAP contains total number of possible paths from each vertex
		
		
		Iterator<Block> gIterator=graph.iterator();
		vertexMap=new HashMap<Vertex, Block>();
				
		Essentials obj=new Essentials();
		//2.Populating jGraph-S	
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
		
		
		
		CycleDetector<Vertex, DefaultWeightedEdge> cDetect=new CycleDetector<>(mjGraph);
		if(cDetect.detectCycles())
		{	
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
			obj.removeCycles(mjGraph,delEdge);				//Remove Cycles
		}
		
			int nPath= obj.BL(mjGraph,numPaths);	//apply Ball-Larus algorithm and returns number of paths in current method 
			synchronized (this) {
				obj.instrumentation(b, mjGraph, vertexMap,base);
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
				FileInputStream f_g_in=new FileInputStream("/home/rishabh/workspace/e0210-project/Testcases/project-2/sootOutput/gr.ser");
				
				if(f_g_in.available()>0)				{
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
				FileOutputStream f_g_out=new FileOutputStream("/home/rishabh/workspace/e0210-project/Testcases/project-2/sootOutput/gr.ser");
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
			
			}
		
		//Print Edges and Edge-Weights
			/*	
		Set<DefaultWeightedEdge>edges=mjGraph.edgeSet();
		Iterator<DefaultWeightedEdge> edgeIt=edges.iterator();
		while(edgeIt.hasNext()){
			DefaultWeightedEdge e1=edgeIt.next();
			System.out.println(mjGraph.getEdgeSource(e1).node+"-"+mjGraph.getEdgeTarget(e1).node+"--"+mjGraph.getEdgeWeight(e1));
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