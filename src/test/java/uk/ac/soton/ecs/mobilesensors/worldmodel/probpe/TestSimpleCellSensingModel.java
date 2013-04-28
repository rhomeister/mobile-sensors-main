package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.awt.geom.Point2D;

import junit.framework.TestCase;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.GraphFactory;
import uk.ac.soton.ecs.mobilesensors.layout.GraphGridAdaptor;
import uk.ac.soton.ecs.mobilesensors.worldmodel.probpe.Observation;
import uk.ac.soton.ecs.mobilesensors.worldmodel.probpe.SimpleCellSensingModel;

public class TestSimpleCellSensingModel extends TestCase {

	private SimpleCellSensingModel sensingModel;
	private AccessibilityGraphImpl graph;

	@Override
	protected void setUp() throws Exception {
		graph = GraphFactory.createRectangularGridGraph(100, 100, 11, 11, 0, 0);
		GraphGridAdaptor grid = new GraphGridAdaptor();
		grid.setGraph(graph);
		grid.afterPropertiesSet();

		sensingModel = new SimpleCellSensingModel(11, 0.1, 0.2, grid);
	}

	public void testObservationAtPursuerLocation() throws Exception {
		Observation observation = new Observation();

		Point2D.Float pursuerLocation = new Point2D.Float(0, 0);
		observation.setSensorLocation(graph.getLocation(pursuerLocation));
		observation.setSensedCoordinates(pursuerLocation);

		assertEquals(0.0, sensingModel.getFalseNegativeProbability(observation));
		assertEquals(0.0, sensingModel.getFalsePositiveProbability(observation));
	}

	public void testObservationNextPursuerLocation() throws Exception {
		Observation observation = new Observation();

		Point2D.Float pursuerLocation = new Point2D.Float(0, 0);
		Point2D.Float observationLocation = new Point2D.Float(0, 10);

		observation.setSensorLocation(graph.getLocation(pursuerLocation));
		observation.setSensedCoordinates(observationLocation);

		assertEquals(0.1, sensingModel.getFalseNegativeProbability(observation));
		assertEquals(0.2, sensingModel.getFalsePositiveProbability(observation));
	}

	public void testObservationOutOfRange() throws Exception {
		Observation observation = new Observation();

		Point2D.Float pursuerLocation = new Point2D.Float(0, 0);
		Point2D.Float observationLocation = new Point2D.Float(100, 100);

		observation.setSensorLocation(graph.getLocation(pursuerLocation));
		observation.setSensedCoordinates(observationLocation);

		assertEquals(0.5, sensingModel.getFalseNegativeProbability(observation));
		assertEquals(0.5, sensingModel.getFalsePositiveProbability(observation));
	}
}
