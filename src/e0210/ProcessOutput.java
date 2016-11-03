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

public class ProcessOutput {

	public static void main(String[] args) throws IOException {

		String project = args[0];
		String testcase = args[1];

		String inPath = "Testcases/" + project + "/output/" + testcase;
		String outPath = "Testcases/" + project + "/processed-output/" + testcase;

		System.out.println("Processing " + testcase + " of " + project);

		// Read the contents of the output file into a string
		String in = new String(Files.readAllBytes(Paths.get(inPath)));

		/*
		 * 
		 * Write your algorithm which does the post-processing of the output
		 * 
		 */

		// Write the contents of the string to the output file
		PrintWriter out = new PrintWriter(outPath);
		out.print(in);

		out.close();

		return;
	}

}