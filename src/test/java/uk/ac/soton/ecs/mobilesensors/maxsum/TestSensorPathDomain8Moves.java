package uk.ac.soton.ecs.mobilesensors.maxsum;

import junit.framework.TestCase;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.GraphFactory;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.SensorPathDomain8Moves;

public class TestSensorPathDomain8Moves extends TestCase {

	private AccessibilityGraphImpl graph;

	@Override
	protected void setUp() throws Exception {
		graph = GraphFactory.createRectangularGridGraph(11, 11, 1, 0, 0);
	}

	public void testname() throws Exception {
		SensorPathDomain8Moves sensorPathDomain8Moves = new SensorPathDomain8Moves(
				graph.getNearestLocation(5, 5), 1, graph);
		
		assertEquals(4, sensorPathDomain8Moves.getStates().size());
	}
	
	public void testname1() throws Exception {
		SensorPathDomain8Moves sensorPathDomain8Moves = new SensorPathDomain8Moves(
				graph.getNearestLocation(0, 0), 1, graph);
		
		assertEquals(3, sensorPathDomain8Moves.getStates().size());
	}
	
	public void testname3() throws Exception {
		SensorPathDomain8Moves sensorPathDomain8Moves = new SensorPathDomain8Moves(
				graph.getNearestLocation(0, 0), 2, graph);
		
		assertEquals(4, sensorPathDomain8Moves.getStates().size());
	}
	
	
	public void testname4() throws Exception {
		SensorPathDomain8Moves sensorPathDomain8Moves = new SensorPathDomain8Moves(
				graph.getNearestLocation(1, 1), 2, graph);
		
		assertEquals(8, sensorPathDomain8Moves.getStates().size());
	}

}
