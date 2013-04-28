package uk.ac.soton.ecs.mobilesensors.configuration;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.GraphFactory;

public class TestAccessibilityGraphIO extends TestCase {

	private File file;
	private AccessibilityGraphImpl graph;

	@Override
	protected void setUp() throws Exception {

		graph = GraphFactory.createRectangularGridGraph(10, 50, 5, 9, 3, 8);

		file = File.createTempFile("temp", "tmp");

		graph.write(file.getAbsolutePath());
	}

	public void testRead() throws Exception {
		AccessibilityGraphImpl readGraph = AccessibilityGraphIO.readGraph(file);
		assertEquals(graph, readGraph);

		file = File.createTempFile("temp", "tmp");
		readGraph.write(file);

		AccessibilityGraphImpl readGraph2 = AccessibilityGraphIO
				.readGraph(file);

		assertEquals(readGraph, readGraph2);
	}

	@Override
	protected void tearDown() throws Exception {
		file.delete();
	}

	public static void main(String[] args) throws IOException {
		AccessibilityGraphImpl readGraph = AccessibilityGraphIO
				.readGraph(new File("rectgraph10-10-11.txt"));

		readGraph.write(new File("bla.txt"));
	}
}
