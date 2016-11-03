package e0210;

import java.io.File;

/*
 * @author Sridhar Gopinath		-		g.sridhar53@gmail.com
 * 
 * Course project,
 * Principles of Programming Course, Fall - 2016,
 * Computer Science and Automation (CSA),
 * Indian Institute of Science (IISc),
 * Bangalore
 */

import java.util.ArrayList;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;

public class Main {
	
	public static void configure(String classpath){
		Options.v().set_verbose(false);
		Options.v().set_keep_line_number(true);
		Options.v().set_src_prec(Options.src_prec_class);
		Options.v().set_soot_classpath(classpath);
		Options.v().set_prepend_classpath(true);
	}

	public static void main(String[] args) {

		String project = args[0];
		String testcase = args[1];
		File f=null,dir=null;
		try {
			
			dir=new File("sootOutput");
			if(dir.exists()) dir.delete();
			dir.mkdir();
			
			f=new File("sootOutput/gr.ser");
			if(f.exists()) f.delete();
			
			f.createNewFile();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<String> base_args = new ArrayList<String>();

		// This is done so that SOOT can find java.lang.Object
		base_args.add("-prepend-classpath");
		
		//base_args.add("-w");

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
		
				
		
		configure("Testcases/"+project+ "/"+project+".jar:bin/");
		
		// The Main classs for the application
		base_args.add("-main-class");
		base_args.add(testcase + ".Main");

		// Class to analyze
		base_args.add(testcase + ".Main");

		base_args.add("-output-dir");
		base_args.add("Testcases/" + project + "/sootBin/");
		

		Analysis obj = new Analysis();
		

		PackManager.v().getPack("jtp").add(new Transform("jtp.MyAnalysis", obj));
		


		Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
	    Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);
		soot.Main.main(base_args.toArray(new String[base_args.size()]));

		//obj.finish(testcase);
		
		return;
	}
	
}