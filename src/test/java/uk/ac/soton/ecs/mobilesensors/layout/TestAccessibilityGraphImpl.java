package uk.ac.soton.ecs.mobilesensors.layout;

import junit.framework.TestCase;

public class TestAccessibilityGraphImpl extends TestCase {
	private AccessibilityGraphImpl graphA1;
	private AccessibilityGraphImpl graphA2;
	private AccessibilityGraphImpl graphB;
	private AccessibilityGraphImpl graphC;

	@Override
	protected void setUp() throws Exception {
		graphA1 = createGraphA();
		graphA2 = createGraphA();
		graphB = createGraphB();
		graphC = createGraphC();
	}

	private AccessibilityGraphImpl createGraphA() {
		AccessibilityGraphImpl graph = new AccessibilityGraphImpl();

		graph = new AccessibilityGraphImpl();

		Location location1 = graph.addAccessibleLocation(10, 40);
		Location location2 = graph.addAccessibleLocation(20, 90);
		Location location3 = graph.addAccessibleLocation(0, 100);

		graph.addAccessibilityRelation(location1, location2);
		graph.addAccessibilityRelation(location1, location3);

		return graph;
	}

	private AccessibilityGraphImpl createGraphB() {
		AccessibilityGraphImpl graph = new AccessibilityGraphImpl();

		graph = new AccessibilityGraphImpl();

		Location location1 = graph.addAccessibleLocation(10, 40);
		Location location2 = graph.addAccessibleLocation(20, 90);
		Location location3 = graph.addAccessibleLocation(0, 100);

		graph.addAccessibilityRelation(location1, location2);

		return graph;
	}

	private AccessibilityGraphImpl createGraphC() {
		AccessibilityGraphImpl graph = new AccessibilityGraphImpl();

		graph = new AccessibilityGraphImpl();

		Location location1 = graph.addAccessibleLocation(10, 40);
		Location location2 = graph.addAccessibleLocation(20, 90);
		Location location3 = graph.addAccessibleLocation(20, 100);

		graph.addAccessibilityRelation(location1, location2);
		graph.addAccessibilityRelation(location1, location3);

		return graph;
	}

	public void testEquals() throws Exception {
		assertEquals(graphA1, graphA1);
		assertEquals(graphA2, graphA2);

		assertEquals(graphA1, graphA2);
		assertEquals(graphA2, graphA1);
		assertEquals(graphB, graphB);
		assertEquals(graphC, graphC);

		assertFalse(graphA1.equals(graphB));
		assertFalse(graphA1.equals(graphC));
		assertFalse(graphA2.equals(graphB));
		assertFalse(graphA2.equals(graphC));

		assertFalse(graphB.equals(graphC));
	}
}
