package e0210;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {

		ArrayList<String> base_args = new ArrayList<String>();

		base_args.add("-whole-program");

		// This is done so that SOOT can find java.lang.Object
		base_args.add("-prepend-classpath");

		// Consider the Main Class as an application and not as a library
		base_args.add("-app");

		// Validate the Jimple IR at the end of the analysis
		base_args.add("-validate");

		// Do SPARK analysis which is used to de-virtualize virtual method calls
		base_args.add("-p");
		base_args.add("cg.spark");
		base_args.add("enabled");

		// Exclude these classes and do not construct call graph for them
		base_args.add("-exclude");
		base_args.add("jdk.net");
		base_args.add("-exclude");
		base_args.add("java.lang");
		base_args.add("-no-bodies-for-excluded");

		// Output the file as .class (Java Bytecode)
		base_args.add("-f");
		base_args.add("class");

		// Add the class path i.e. path to the JAR file
		base_args.add("-soot-class-path");
		// base_args.add(jarPath);

		// The Main class for the application
		base_args.add("-main-class");
		base_args.add(args[0]);

		// Class to analyze
		base_args.add(args[0]);

		base_args.add("-output-dir");
		// base_args.add(outputDir + "bin/");

		// Analysis obj = new Analysis();

		// PackManager.v().getPack("wjtp").add(new Transform("wjtp.MyAnalysis",
		// obj));

		soot.Main.main(base_args.toArray(new String[base_args.size()]));

		return;
	}

}