package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.awt.geom.Point2D;

import junit.framework.TestCase;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.GraphFactory;
import uk.ac.soton.ecs.mobilesensors.layout.GraphGridAdaptor;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;

public class TestProbabilisticPursuitEvaderModel extends TestCase {

	private ProbabilisticPursuitEvaderModel model;
	private AccessibilityGraphImpl graph;
	private GraphGridAdaptor grid;

	@Override
	protected void setUp() throws Exception {
		graph = GraphFactory.createRectangularGridGraph(2, 2, 3, 3, 0, 0);
		grid = new GraphGridAdaptor();
		grid.setGraph(graph);
		grid.afterPropertiesSet();

		SimpleRandomMovementModel movementModel = new SimpleRandomMovementModel();
		movementModel.setGraph(graph);

		SensingModel sensingModel = new SimpleCellSensingModel(11, 0.1, 0.2,
				grid);
		model = new ProbabilisticPursuitEvaderModel(sensingModel, movementModel);
		model.initialise(grid, graph);
	}

	public void testObserveEvaderLocation() throws Exception {
		Location evaderLocation = graph
				.getLocation(new Point2D.Double(0.0, 0.0));
		model.setEvaderLocation(evaderLocation);

		for (Double value : model.getValues().values()) {
			assertEquals(1.0 / model.getValues().size(), value);
		}

		ObservationCoordinates coordinates = new ObservationCoordinates(
				evaderLocation, 0.0);

		Validate.isTrue(false);

		// FIXME
		// model.observe(coordinates);

		// model.update(true);

		for (Point2D point : model.getValues().keySet()) {
			Double value = model.getValues().get(point);

			if (point.equals(new Point2D.Double(0, 0))) {
				assertEquals(1.0 / 3.0, value);
			} else if (point.equals(new Point2D.Double(0, 1))) {
				assertEquals(1.0 / 3.0, value);
			} else if (point.equals(new Point2D.Double(1, 0))) {
				assertEquals(1.0 / 3.0, value);
			} else {
				assertEquals(0.0, value);
			}
		}
	}

}
