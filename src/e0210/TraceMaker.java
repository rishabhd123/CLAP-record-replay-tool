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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;

import soot.PackManager;
import soot.Transform;

public class TraceMaker {

	public static void main(String[] args) throws IOException {

		String project = args[0];
		String testcase = args[1];
		
		try{
		File f=new File("Testcases/"+project+"/tuples");
		if(f.exists()) f.delete();
		f.mkdir();
		}catch(Exception e){}
		
		// The raw output from instrumented code. You'll use this to construct the tuple for each method call
		String inPath = "Testcases/" + project + "/output/" + testcase;
		// The output files for global trace and tuples. You'll output the results to these files
		String globalTraceOutPath = "Testcases/" + project + "/processed-output/" + testcase + "-global-trace";
		String tupleOutPath = "Testcases/" + project + "/tuples/"+ testcase;

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
				catch (Exception e) {/**e.printStackTrace();*/}
				
				f_g_in.close();
			}
			catch(Exception e){/*e.printStackTrace();*/}
		
		
		DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge> tempGraph=null;
		List<String> pathList=new ArrayList<>();
		Iterator<DirectedWeightedPseudograph<Vertex, DefaultWeightedEdge>> graphListIt;
		
		String finalResult="";					//contains the final stream of complete tuples
		for(String s:blFile){
			String[] tupleStr=s.split(",");		//split tuple into four string by ","
			String blStr=tupleStr[3];			//retrieve last component(bl id) from tuple
			String[] blId=blStr.split("&");		// split bl id string of each tuple into singleton bl-id using "&"
			result="";							//contains block id's corresponding to bl id's of each tuple  
			boolean isFirst=true;
			for(String bls:blId){				//for each bl-id retrieve the full path
				
				int bl=Integer.parseInt(bls);
			graphListIt=listOfGraph.iterator();
			//BufferedReader br=new BufferedReader(new StringReader(s));
			
			int checkBl= Integer.parseInt(bls);		//extract BLid for checking purpose
			while(graphListIt.hasNext()){
				tempGraph=graphListIt.next();
						
				Vertex checkVert = obj1.getVertexfromInt(tempGraph,0);
				
				
				//Vertex checkVert=mjGraph.vertexSet().iterator().next();		//Extract first vertex to check sBL and eBL
				if(checkVert.sBL<=checkBl && checkVert.eBl>=checkBl){
					path=obj1.regeneratePath(tempGraph, bl,checkVert.sBL);	//block id path corresponding to each bl id(single number)
					if(isFirst){
						result=result+path;						
						isFirst=false;
					}
					else{
						result=result+"&"+path;						
					}
					
					break;
					
				}
				
			}
			
			
		}
			finalResult=finalResult+tupleStr[0]+","+tupleStr[1]+","+tupleStr[2]+","+result+"\n";
			
		}	
		// Write the tuples of each method call to the output file
		PrintWriter out = new PrintWriter(tupleOutPath);
		out.print(finalResult);		
		out.close();
		
		//----------Tuple Written Successfully--------//
		sootMainSymbolicExecution(project, testcase);
        /*
            You should have the intra-thread trace for each thread and the
            path constraints by now.
            Assign an order variable of the form O_i_j to the jth trace entry
            of the ith thread.
            Use the intra-thread traces to construct read-write constraints,
            locking constraints, program order constraints, must-happen-before
            constraints. These constraints will be in terms of the order 
            variables.
            Put all these constaints together into one big equation:
            All_constraints = (Read-write constraints) ^ (Path constraints) ^
                (Program order constraints) ^ (Must happen before constraints)
                ^ (Locking constraints)
            
        */
        
        /* Solve the constraints using Z3 solver
           The solver will provide you a feasible assignment of order variables
           Using these values, construct your global trace 
           To construct the global trace, you just need to put the intra-thread
           trace entries in ascending order of their order variables.
        */

		// Output the global trace 
		PrintWriter globalTraceWriter = new PrintWriter(globalTraceOutPath);
		globalTraceWriter.print("");

		globalTraceWriter.close();

		// Output the tuples
		PrintWriter tupleWriter = new PrintWriter(tupleOutPath);
		tupleWriter.print("");

		tupleWriter.close();


		return;
	}

	
	public static void sootMainSymbolicExecution(String project, String testcase) {

		ArrayList<String> base_args = new ArrayList<String>();

		// This is done so that SOOT can find java.lang.Object
		base_args.add("-prepend-classpath");

		base_args.add("-w");

		// Consider the Main Class as an application and not as a library
		base_args.add("-app");

		// Validate the Jimple IR at the end of the analysis
		base_args.add("-validate");

		// Exclude these classes and do not construct call graph for them
		base_args.add("-exclude");
		base_args.add("jdk.net");
		base_args.add("-exclude");
		base_args.add("java.lang");
		base_args.add("-exclude");
        base_args.add("jdk.internal.*"); 
		base_args.add("-no-bodies-for-excluded");

		// Retain variable names from the bytecode
		base_args.add("-p");
		base_args.add("jb");
		base_args.add("use-original-names:true");

		// Output the file as .class (Java Bytecode)
		base_args.add("-f");
		base_args.add("class");

		// Add the class path i.e. path to the JAR file
		base_args.add("-soot-class-path");
		base_args.add("Testcases/" + project + "/" + project + ".jar");

		// The Main class for the application
		base_args.add("-main-class");
		base_args.add(testcase + ".Main");

		// Class to analyze
		base_args.add(testcase + ".Main");

		base_args.add("-output-dir");
		base_args.add("Testcases/" + project + "/sootBin/");

		SymbolicExecution obj = new SymbolicExecution(project,testcase);

		PackManager.v().getPack("wjtp").add(new Transform("wjtp.MyAnalysis", obj));

		soot.Main.main(base_args.toArray(new String[base_args.size()]));

	}

}
