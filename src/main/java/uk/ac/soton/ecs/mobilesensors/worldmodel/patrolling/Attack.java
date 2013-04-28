package uk.ac.soton.ecs.mobilesensors.worldmodel.patrolling;

import java.awt.geom.Point2D;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class Attack {

	private int duration;
	private Point2D location;

	public Attack(Point2D point) {
		this.location = point;
	}

	public void update() {
		duration++;
	}

	public int getDuration() {
		return duration;
	}

	public Point2D getLocation() {
		return location;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(location).append(duration)
				.toHashCode();
	}
}
