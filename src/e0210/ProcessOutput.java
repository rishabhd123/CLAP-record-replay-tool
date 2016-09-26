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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;

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
		
		String[] blFile=in.split(System.getProperty("line.separator"));		//System.getProperty("line.separator")
		String path=null,result="";
		DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> mjGraph=null;
		List<DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>> listOfGraph=new ArrayList<DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>>();
				
		try{	
			FileInputStream f_g_in=new FileInputStream("sootOutput/gr.ser");
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
		int p=0;
		boolean isFirst=true;
		for(String s:blFile){
			int bl=Integer.parseInt(s);
			graphListIt=listOfGraph.iterator();
			//BufferedReader br=new BufferedReader(new StringReader(s));
			
			int checkBl= Integer.parseInt(s);		//extract BLid for checking purpose
			while(graphListIt.hasNext()){
				tempGraph=graphListIt.next();
				int gid= listOfGraph.indexOf(tempGraph);			
				Vertex checkVert = obj1.getVertexfromInt(tempGraph,0);
				
				
				//Vertex checkVert=mjGraph.vertexSet().iterator().next();		//Extract first vertex to check sBL and eBL
				if(checkVert.sBL<=checkBl && checkVert.eBl>=checkBl){
					path=obj1.regeneratePath(tempGraph, bl,checkVert.sBL);
					if(isFirst){
						result=result+path;
						p=gid;
						isFirst=false;
					}
					else{
						if(p==gid){
							result=result+"\n"+path;
						}
						else{
							p=gid;
							result=result+"\n\n"+path;
						}
					}
					
					break;
					
				}
				
			}
			
			
		}
		
		
		

		// Write the contents of the string to the output file
		PrintWriter out = new PrintWriter(outPath);
		out.print(result);
		
		
		out.close();

		return;
	}

}