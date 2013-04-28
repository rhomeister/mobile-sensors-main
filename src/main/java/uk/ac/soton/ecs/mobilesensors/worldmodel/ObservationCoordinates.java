package uk.ac.soton.ecs.mobilesensors.worldmodel;

import org.apache.commons.lang.builder.HashCodeBuilder;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.LocationImpl;

public class ObservationCoordinates {

	// these are the coordinates the sensor observes *from*, it might see many
	// more locations
	// from this vantage point, depending on the model
	private double observeX;
	private double observeY;
	private double time;

	public ObservationCoordinates(Location observation, double time) {
		this(observation.getX(), observation.getY(), time);
	}

	public ObservationCoordinates(double x, double y, double time) {
		this.observeX = x;
		this.observeY = y;
		this.time = time;
	}

	public double getTime() {
		return time;
	}

	public double getX() {
		return observeX;
	}

	public void setX(double x) {
		this.observeX = x;
	}

	public double getY() {
		return observeY;
	}

	public void setY(double y) {
		this.observeY = y;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public Location getLocation() {
		return new LocationImpl(observeX, observeY);
	}

	public void setLocation(Location location) {
		this.observeX = location.getX();
		this.observeY = location.getY();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(observeY).append(observeY)
				.toHashCode();
	}
}
