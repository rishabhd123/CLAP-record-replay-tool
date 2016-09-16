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
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

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
		SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> mjGraph=null;
		try{
			FileInputStream fi=new FileInputStream("/home/rishabh/workspace/e0210-project/Testcases/project-2/sootOutput/gr.ser");
			ObjectInputStream objin=new ObjectInputStream(fi);
			mjGraph=(SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge>) objin.readObject();
			objin.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		
		int bl=Integer.parseInt(in);	//applicable only when there is exactly one method
		
		in=obj1.regeneratePath(mjGraph, bl);
		//System.out.println(mjGraph.toString());

		// Write the contents of the string to the output file
		PrintWriter out = new PrintWriter(outPath);
		out.print(in);
		
		out.close();

		return;
	}

}