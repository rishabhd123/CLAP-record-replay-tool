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

import soot.PackManager;
import soot.Transform;

public class TraceMaker {

	public static void main(String[] args) throws IOException {

		String project = args[0];
		String testcase = args[1];

		// Input given to the executable
		String argsFile = "Testcases/" + project + "/input/" + testcase;
		String testcaseArgs = new String(Files.readAllBytes(Paths.get(argsFile)));

		String inPath = "Testcases/" + project + "/output/" + testcase;

		String globalTraceOutPath = "Testcases/" + project + "/processed-output/" + testcase + "-global-trace";
		String tupleOutPath = "Testcases/" + project + "/processed-output/" + testcase + "-tuples";

		System.out.println("Processing " + testcase + " of " + project);

		// Read the contents of the output file into a string
		String in = new String(Files.readAllBytes(Paths.get(inPath)));

		/*
		 * 
		 * Write your algorithm which does the post-processing of the output
		 * 
		 */

		// Call to the symbolic execution.
		// You can add any arguments that you want
		// Example: basic block tuples
		sootMain(project, testcase);

		// Write the contents of the string to the output file
		PrintWriter globalTraceWriter = new PrintWriter(globalTraceOutPath);
		globalTraceWriter.print(in);

		globalTraceWriter.close();

		// Write the contents of the string to the output file
		PrintWriter tupleWriter = new PrintWriter(tupleOutPath);
		tupleWriter.print(in);

		tupleWriter.close();

		return;
	}

	public static void sootMain(String project, String testcase) {

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

		SymbolicExecution obj = new SymbolicExecution();

		PackManager.v().getPack("jtp").add(new Transform("jtp.MyAnalysis", obj));

		soot.Main.main(base_args.toArray(new String[base_args.size()]));

	}

}