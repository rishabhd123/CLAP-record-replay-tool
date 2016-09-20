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
		
		String path;
		DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> mjGraph=null;
		Map<Integer, Boolean> exit_Map=null;
		
		try{
			FileInputStream f_g=new FileInputStream("/home/rishabh/workspace/e0210-project/Testcases/project-2/sootOutput/gr.ser");
			ObjectInputStream ob_g=new ObjectInputStream(f_g);
			mjGraph=(DirectedWeightedPseudograph<Integer, DefaultWeightedEdge>) ob_g.readObject();
			ob_g.close();
			
			FileInputStream f_vm=new FileInputStream("/home/rishabh/workspace/e0210-project/Testcases/project-2/sootOutput/vm.ser");
			ObjectInputStream ob_vm=new ObjectInputStream(f_vm);
			exit_Map=(Map<Integer, Boolean>) ob_vm.readObject();
			ob_vm.close();
			
			}
			catch(Exception e){
				e.printStackTrace();
			}
		
		path=obj1.regeneratePath(mjGraph, in,exit_Map);
		//System.out.println(mjGraph.toString());

		// Write the contents of the string to the output file
		PrintWriter out = new PrintWriter(outPath);
		out.print(path);
		//out.print(in);
		
		out.close();

		return;
	}

}