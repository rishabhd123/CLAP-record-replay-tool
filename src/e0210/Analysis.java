package e0210;

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