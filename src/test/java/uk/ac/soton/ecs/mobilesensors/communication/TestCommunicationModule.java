package uk.ac.soton.ecs.mobilesensors.communication;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Ignore;

import uk.ac.soton.ecs.mobilesensors.Environment;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.initialplacement.SimpleInitialPlacement;
import uk.ac.soton.ecs.mobilesensors.layout.GraphFactory;
import uk.ac.soton.ecs.mobilesensors.layout.LocationImpl;
import uk.ac.soton.ecs.mobilesensors.layout.RectangularGrid;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;

@Ignore
public class TestCommunicationModule extends TestCase {

	private CommunicationModule communicationModule1;

	private CommunicationModule communicationModule2;

	private Sensor sensor1;

	private Sensor sensor2;

	private Sensor sensor3;

	private CommunicationModule communicationModule3;

	private Simulation simulation;

	private RangeRestrictedMessageInterchange interchange;

	@Override
	protected void setUp() throws Exception {
		interchange = new RangeRestrictedMessageInterchange();

		simulation = new Simulation();

		Environment environment = new Environment();

		RectangularGrid rectangularGrid = new RectangularGrid(10, 10, 10);

		environment.setGrid(rectangularGrid);

		environment.setAccessibilityGraph(GraphFactory
				.createRectangularGridGraph(10, 10, 1, 0, 0));

		simulation.setEnvironment(environment);
		simulation
				.setMessageInterchange(new RangeRestrictedMessageInterchange());
		simulation.setInitialPlacement(new SimpleInitialPlacement(4));

		sensor1 = new Sensor();
		sensor2 = new Sensor();
		sensor3 = new Sensor();

		simulation.addSensor(sensor1);
		simulation.addSensor(sensor2);
		simulation.addSensor(sensor3);
		simulation.getGlobalMetrics().clear();
		interchange.setSimulation(simulation);

		simulation.initialize();

		communicationModule1 = new CommunicationModule(sensor1, simulation,
				interchange);

		communicationModule2 = new CommunicationModule(sensor2, simulation,
				interchange);
		communicationModule3 = new CommunicationModule(sensor3, simulation,
				interchange);
	}

	public void testname() throws Exception {
		sensor1.setInitialLocation(new LocationImpl(0, 0));
		sensor2.setInitialLocation(new LocationImpl(5, 5));
		sensor3.setInitialLocation(new LocationImpl(10, 10));

		interchange.setCommunicationRange(8);
		interchange.handleStartOfRound(null, 1, 5.0);

		List<SensorID> expected = Arrays.asList(sensor2.getID(), sensor3
				.getID());

		Collection<SensorID> reachableSensors = communicationModule1
				.getReachableSensors(sensor2.getID());

		assertTrue(expected.containsAll(reachableSensors));
		assertTrue(reachableSensors.containsAll(expected));

		expected = Arrays.asList(sensor3.getID());

		reachableSensors = communicationModule2.getReachableSensors(sensor3
				.getID());
		assertTrue(expected.containsAll(reachableSensors));
		assertTrue(reachableSensors.containsAll(expected));

		sensor3.setInitialLocation(new LocationImpl(5, 0));
		interchange.handleStartOfRound(null, 1, 5.0);

		expected = Arrays.asList(sensor2.getID(), sensor3.getID());
		reachableSensors = communicationModule1.getReachableSensors(sensor2
				.getID());

		// System.out.println(reachableSensors);

		assertTrue(expected.containsAll(reachableSensors));
		assertTrue(reachableSensors.containsAll(expected));
	}
}
