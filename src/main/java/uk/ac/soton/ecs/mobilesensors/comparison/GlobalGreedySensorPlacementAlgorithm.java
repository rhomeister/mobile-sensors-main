package uk.ac.soton.ecs.mobilesensors.comparison;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;

import uk.ac.soton.ecs.mobilesensors.RMSEMetric;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public class GlobalGreedySensorPlacementAlgorithm extends
		AbstractSensorPlacementAlgorithm {

	@Override
	protected void calculate() {
		StopWatch watch = new StopWatch();
		watch.start();

		List<Long> time = new ArrayList<Long>();

		RMSEMetric rmseMetric = new RMSEMetric();
		rmseMetric.initialize(simulation);

		for (int i = 0; i < sensorCount; i++) {
			Set<Location> selected = new HashSet<Location>();

			double bestValue = Double.NEGATIVE_INFINITY;
			Location bestLocation = null;

			for (final Location location : graph.getLocations()) {
				Collection<Point2D> affectedGridPoints = grid.getGridPoints(
						location, 20);

				double before = 0.0;

				for (Point2D point2d : affectedGridPoints) {
					Location pointLocation = graph.getLocation(point2d);
					before += informativenessFunction
							.getInformativeness(pointLocation);
				}

				ObservationInformativenessFunction copy = informativenessFunction
						.copy();

				copy.observe(location);

				double after = 0.0;

				for (Point2D point2d : affectedGridPoints) {
					Location pointLocation = graph.getLocation(point2d);
					after += copy.getInformativeness(pointLocation);
				}

				double value = before - after;

				if (value > bestValue) {
					bestLocation = location;
					bestValue = value;
				}
			}

			// rmseMetric.handleEndOfRound(null, null, 0);

			time.add(watch.getTime());

			selected.add(bestLocation);
			addBestLocation(bestLocation);
			informativenessFunction.observe(bestLocation);

		}

		watch.stop();

		System.out.println(time);

		// System.exit(0);

	}

}
