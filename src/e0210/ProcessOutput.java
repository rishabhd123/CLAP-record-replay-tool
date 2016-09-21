package e0210;

import java.io.BufferedReader;
/*
 * @author Sridhar Gopinath		-		g.sridhar53@gmail.com
 * 
 * Course project,
 * Principles of Programming Course, Fall - 2016,
 * Computer Science and Automation (CSA),
 * Indian Institute of Science (IISc),
 * Bangalore
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import soot.toolkits.graph.Block;

public class ProcessOutput {

	public static void main(String[] args) throws IOException {

		String project = args[0];
		String testcase = args[1];

		String inPath = "Testcases/" + project + "/output/" + testcase;
		String outPath = "Testcases/" + project + "/processed-output/" + testcase;

		System.out.println("Processing " + testcase + " of " + project);

		// Read the contents of the output file into a string
		String in = new String(Files.readAllBytes(Paths.get(inPath)));
		Essentials obj1=new Essentials();
		
		String[] blFile=in.split(System.getProperty("line.separator")+System.getProperty("line.separator"));		//System.getProperty("line.separator")
		System.out.println("-----------------");
		for(String s:blFile) System.out.println(s);
		System.out.println("-----------------");
		String path=null,result="";
		DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> mjGraph=null;
		List<DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>> listOfGraph=new ArrayList<DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>>();
				
		try{	
			FileInputStream f_g_in=new FileInputStream("/home/rishabh/workspace/e0210-project/Testcases/project-2/sootOutput/gr.ser");
			ObjectInputStream o_g_in=new ObjectInputStream(f_g_in);
			
			try {
				mjGraph=(DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>) o_g_in.readObject();			
				while(mjGraph!=null){
					listOfGraph.add(mjGraph);
					mjGraph=(DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>) o_g_in.readObject();				
					}		
				o_g_in.close();
				} 
				catch (Exception e) {/*e.printStackTrace();*/}
				
				f_g_in.close();
			}
			catch(Exception e){/*e.printStackTrace();*/}
		
		
		DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> tempGraph=null;
		List<String> pathList=new ArrayList<>();
		Iterator<DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>> graphListIt;
		for(String s:blFile){
			graphListIt=listOfGraph.iterator();
			BufferedReader br=new BufferedReader(new StringReader(s));
			int checkBl= Integer.parseInt(br.readLine());		//extract BLid for checking purpose
			while(graphListIt.hasNext()){
				tempGraph=graphListIt.next();
								
				Vertex checkVert = obj1.getVertexfromInt(tempGraph,0);
				
				
				//Vertex checkVert=mjGraph.vertexSet().iterator().next();		//Extract first vertex to check sBL and eBL
				if(checkVert.sBL<=checkBl && checkVert.eBl>=checkBl){
					path=obj1.regeneratePath(tempGraph, s,checkVert.sBL);
					pathList.add(path);
					break;
					
				}
				
			}
			
			
		}
		Iterator<String> pathIt=pathList.iterator();
		while(pathIt.hasNext()){
			path=pathIt.next();
			result+=path;
			if(pathIt.hasNext()) result+="\n\n";
		}
		
		

		// Write the contents of the string to the output file
		PrintWriter out = new PrintWriter(outPath);
		out.print(result);
		//out.print(in);
		
		out.close();

		return;
	}

}