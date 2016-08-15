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

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;

public class Analysis extends BodyTransformer {

	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {

		System.out.println(b.toString());

		return;
	}

}