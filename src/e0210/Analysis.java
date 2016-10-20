package e0210;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

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

import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;

import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalBlockGraph;

public class Analysis extends BodyTransformer {

	DirectedPseudograph<Block, DefaultEdge> graph = new DirectedPseudograph<Block, DefaultEdge>(DefaultEdge.class);

	@Override
	protected synchronized void internalTransform(Body b, String phaseName, Map<String, String> options) {

		ExceptionalBlockGraph cfg = new ExceptionalBlockGraph(b);

		for (Block block : cfg.getBlocks()) {
			graph.addVertex(block);
		}

		for (Block block : cfg.getBlocks()) {
			for (Block succ : cfg.getSuccsOf(block))
				graph.addEdge(block, succ);
		}

		System.out.println(b.toString());

		return;
	}

	public void finish(String testcase) {

		VertexNameProvider<Block> id = new VertexNameProvider<Block>() {

			@Override
			public String getVertexName(Block b) {
				return String.valueOf("\"" + b.getBody().getMethod().getNumber() + " " + b.getIndexInMethod() + "\"");
			}
		};

		VertexNameProvider<Block> name = new VertexNameProvider<Block>() {

			@Override
			public String getVertexName(Block b) {
				String body = b.toString().replace("\'", "").replace("\"", "");
				return body;
			}
		};

		new File("sootOutput").mkdir();

		DOTExporter<Block, DefaultEdge> exporter = new DOTExporter<Block, DefaultEdge>(id, name, null);
		try {
			exporter.export(new PrintWriter("sootOutput/" + testcase + ".dot"), graph);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return;
	}

}