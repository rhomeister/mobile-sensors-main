package uk.ac.soton.ecs.mobilesensors.layout;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uk.ac.soton.ecs.mobilesensors.Move;

public class LocationImpl extends Point2D implements Location, Serializable {

	private Point2D coordinates;

	private transient AccessibilityGraphImpl parent;

	protected LocationImpl(double x, double y, AccessibilityGraphImpl parent) {
		coordinates = new Point2D.Double(x, y);
		this.parent = parent;
	}

	public LocationImpl(double x, double y) {
		this(x, y, null);
	}

	protected LocationImpl() {
	}

	public LocationImpl(Point2D point2d) {
		this(point2d.getX(), point2d.getY());
	}

	public Point2D getCoordinates() {
		return coordinates;
	}

	public double directDistance(Location other) {
		return directDistance(other.getCoordinates());
	}

	public double directDistanceSq(Location other) {
		return directDistanceSq(other.getCoordinates());
	}

	public List<Move> getMoveOptions() {
		List<Move> moveOptions = new ArrayList<Move>();

		for (Location neighbour : getNeighbours())
			moveOptions.add(new Move(neighbour));

		return moveOptions;
	}

	public List<Location> getNeighbours() {
		ArrayList<Location> neighbours = new ArrayList<Location>();
		neighbours.add(this);
		neighbours.addAll(parent.getNeighbors(this));
		return neighbours;
	}

	public void setCoordinates(Point2D coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("(" + coordinates.getX() + ", "
				+ coordinates.getY() + ")");

		return buffer.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Location) {
			Location location = (Location) o;
			return coordinates.equals(location.getCoordinates());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return coordinates.hashCode();
	}

	public double getX() {
		return coordinates.getX();
	}

	public double getY() {
		return coordinates.getY();
	}

	public double directDistanceSq(Point2D coordinates2) {
		return coordinates.distanceSq(coordinates2);
	}

	public double directDistance(Point2D coordinates2) {
		return coordinates.distance(coordinates2);
	}

	@Override
	public void setLocation(double x, double y) {
		coordinates.setLocation(x, y);
	}

	public void translate(double x, double y) {
		setLocation(getX() + x, getY() + y);
	}
}
