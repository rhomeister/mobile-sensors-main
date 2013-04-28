package uk.ac.soton.ecs.mobilesensors.configuration;

import java.io.File;

import junit.framework.TestCase;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class TestAccessibilityGraphIORectangularGraph extends TestCase {

	private File file;
	private AccessibilityGraphImpl graph;

	@Override
	protected void setUp() throws Exception {
		graph = new AccessibilityGraphImpl();

		Location location1 = graph.addAccessibleLocation(10, 40);
		Location location2 = graph.addAccessibleLocation(20, 90);
		Location location3 = graph.addAccessibleLocation(0, 100);
		graph.addAccessibilityRelation(location1, location2);
		graph.addAccessibilityRelation(location1, location3);

		file = File.createTempFile("temp", "tmp");

		graph.write(file.getAbsolutePath());
	}

	public void testRead() throws Exception {
		AccessibilityGraphImpl readGraph = AccessibilityGraphIO.readGraph(file);

		assertEquals(graph, readGraph);
	}

	@Override
	protected void tearDown() throws Exception {
		file.delete();
	}
}
