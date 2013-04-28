package uk.ac.soton.ecs.mobilesensors.worldmodel.pursuitevader;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.Observation;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public abstract class AbstractObservationInformativenessFunction implements
		ObservationInformativenessFunction {

	private Grid grid;

	public final Grid getGrid() {
		return grid;
	}

	public final void setGrid(Grid grid) {
		this.grid = grid;
	}

	public final Point2D getMaximumInformativeLocation() {
		return getMaximumInformativenessLocation(getValues());
	}

	public final static Point2D getMaximumInformativenessLocation(
			Map<Point2D, Double> values) {
		double maxValue = Double.NEGATIVE_INFINITY;
		Point2D bestPoint = null;

		for (Point2D point : values.keySet()) {
			if (values.get(point) > maxValue) {
				bestPoint = point;
				maxValue = values.get(point);
			}
		}

		return bestPoint;
	}

	public Collection<Observation> observe(Collection<Location> locations) {
		Collection<Observation> observations = new ArrayList<Observation>();
		for (Location location : locations) {
			observations.addAll(observe(location));
		}
		return observations;
	}
}
