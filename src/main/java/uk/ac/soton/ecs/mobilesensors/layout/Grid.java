package uk.ac.soton.ecs.mobilesensors.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;

import Jama.Matrix;

public interface Grid extends Iterable<Point2D> {
	List<Point2D> getGridPoints();

	Matrix getGrid();

	int getGridPointCount();

	Collection<Point2D> getGridPoints(Point2D location, double distance);

	Collection<Point2D> getGridPoints(Location location, double distance);

	Rectangle2D getBoundingRectangle();

	List<Location> getLocations();

	List<Location> getLocations(Location consideredSensorLocation,
			double evaluationRange);
}
