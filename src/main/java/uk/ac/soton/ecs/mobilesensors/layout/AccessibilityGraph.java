package uk.ac.soton.ecs.mobilesensors.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public interface AccessibilityGraph<T extends Location> extends Iterable<T> {
	T addAccessibleLocation(double x, double y);

	void addAccessibilityRelation(T location1, T location2);

	int getLocationCount();

	List<T> getLocations();

	T getNearestLocation(double locationX, double locationY);

	Rectangle2D getBoundingBox();

	Location getLocation(Point2D point);

}
