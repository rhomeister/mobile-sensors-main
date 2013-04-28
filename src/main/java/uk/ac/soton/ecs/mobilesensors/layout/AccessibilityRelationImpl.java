package uk.ac.soton.ecs.mobilesensors.layout;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class AccessibilityRelationImpl implements AccessibilityRelation,
		Serializable {

	private Location location1;
	private Location location2;
	private Double length;

	public AccessibilityRelationImpl(Location location1, Location location2) {
		this.location1 = location1;
		this.location2 = location2;
	}

	public AccessibilityRelationImpl() {

	}

	public Location getLocation1() {
		return location1;
	}

	public double getLength() {
		if (length == null)
			length = location1.directDistance(location2);

		return length;
	}

	public Location getLocation2() {
		return location2;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AccessibilityRelation) {
			AccessibilityRelation relation = (AccessibilityRelation) o;

			if (getLocation1() == null || getLocation2() == null)
				return false;

			return getLocation1().equals(relation.getLocation1())
					&& getLocation2().equals(relation.getLocation2());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(location1).append(location2)
				.toHashCode();
	}

	public Location getOther(Location location) {
		if (location.equals(location1)) {
			return location2;
		} else if (location.equals(location2)) {
			return location1;
		}

		return null;
	}

}
