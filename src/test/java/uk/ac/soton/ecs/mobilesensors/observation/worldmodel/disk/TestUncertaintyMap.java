package uk.ac.soton.ecs.mobilesensors.observation.worldmodel.disk;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;

import junit.framework.TestCase;
import uk.ac.soton.ecs.mobilesensors.layout.RectangularGrid;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;
import uk.ac.soton.ecs.mobilesensors.worldmodel.disk.UncertaintyMap;

public class TestUncertaintyMap extends TestCase {

	private UncertaintyMap map;
	private RectangularGrid rectangularGrid;

	@Override
	protected void setUp() throws Exception {
		rectangularGrid = new RectangularGrid(10, 10, 10);

		map = new UncertaintyMap(rectangularGrid, .1);
	}

	public void testInitialise() throws Exception {
		assertTrue(!map.getValues().keySet().isEmpty());

		Map<Point2D, Double> values = map.getValues();

		for (Point2D coordinate : values.keySet()) {
			assertEquals(values.get(coordinate), UncertaintyMap.MAX_UNCERTAINTY);
		}
	}

	public void testObserve() throws Exception {
		ObservationCoordinates coordinates = new ObservationCoordinates(5.0,
				5.0, 0.0);

		Collection<Point2D> gridPoints = rectangularGrid.getGridPoints(
				coordinates.getLocation(), 3.0);

		map.observe(gridPoints);

		Map<Point2D, Double> values = map.getValues();

		for (Point2D coordinate : values.keySet()) {
			if (coordinate.distance(coordinates.getLocation().getCoordinates()) <= 3.0) {
				assertEquals(values.get(coordinate), 0.0);
			} else {
				assertEquals(values.get(coordinate),
						UncertaintyMap.MAX_UNCERTAINTY);
			}
		}
	}

	public void testIncrement() throws Exception {
		map.increaseUncertainty(20);

		Map<Point2D, Double> values = map.getValues();

		for (Point2D coordinate : values.keySet()) {
			assertEquals(values.get(coordinate), UncertaintyMap.MAX_UNCERTAINTY);
		}

		ObservationCoordinates coordinates = new ObservationCoordinates(5.0,
				5.0, 0.0);

		Collection<Point2D> gridPoints = rectangularGrid.getGridPoints(
				coordinates.getLocation(), 3.0);

		map.observe(gridPoints);

		map.increaseUncertainty(1.0);

		values = map.getValues();

		for (Point2D coordinate : values.keySet()) {
			if (coordinate.distance(coordinates.getLocation().getCoordinates()) <= 3.0) {
				assertEquals(values.get(coordinate), map
						.getUncertaintyIncrement());
			} else {
				assertEquals(values.get(coordinate),
						UncertaintyMap.MAX_UNCERTAINTY);
			}
		}
	}
}
