package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.awt.geom.Point2D;

import junit.framework.TestCase;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.GraphFactory;
import uk.ac.soton.ecs.mobilesensors.layout.GraphGridAdaptor;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.probpe.ProbabilityMap;
import uk.ac.soton.ecs.mobilesensors.worldmodel.probpe.SimpleRandomMovementModel;

public class TestSimpleRandomMovementModel extends TestCase {

	private SimpleRandomMovementModel movementModel;
	private GraphGridAdaptor grid;
	private AccessibilityGraphImpl graph;

	@Override
	protected void setUp() throws Exception {
		graph = GraphFactory.createRectangularGridGraph(100, 100, 11, 11, 0, 0);
		grid = new GraphGridAdaptor();
		grid.setGraph(graph);
		grid.afterPropertiesSet();

		movementModel = new SimpleRandomMovementModel();
		movementModel.setGraph(graph);
	}

	public void testFlatPrior() throws Exception {
		ProbabilityMap probabilityMap = new ProbabilityMap(grid);
		probabilityMap.createFlatPrior();
		probabilityMap.checkValidity();

		movementModel.updateProbabilityMap(probabilityMap);

	}

	public void testSinglePeak() throws Exception {
		ProbabilityMap probabilityMap = new ProbabilityMap(grid);
		Point2D.Float x = new Point2D.Float(0, 0);
		probabilityMap.put(x, 1.0);
		probabilityMap.checkValidity();

		ProbabilityMap newMap = movementModel
				.updateProbabilityMap(probabilityMap);

		Location location = graph.getLocation(x);

		for (Location neighbour : location.getNeighbours()) {
			assertEquals(1.0 / location.getNeighbours().size(), newMap
					.getValue(neighbour.getCoordinates()));
		}
	}
}
