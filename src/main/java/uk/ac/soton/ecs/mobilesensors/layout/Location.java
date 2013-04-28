package uk.ac.soton.ecs.mobilesensors.layout;

import java.awt.geom.Point2D;
import java.util.List;

import uk.ac.soton.ecs.mobilesensors.Move;

public interface Location {

	List<Location> getNeighbours();

	List<Move> getMoveOptions();

	Point2D getCoordinates();

	double directDistance(Location other);

	double directDistanceSq(Location other);

	double directDistance(Point2D coordinates);

	double getX();

	double getY();

	void setCoordinates(Point2D output);

	void translate(double x, double y);

}
