package uk.ac.soton.ecs.mobilesensors.sensor;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class SensorID implements Comparable<SensorID> {

	private final int id;

	public SensorID(int id) {
		this.id = id;
	}

	public String getID() {
		return toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SensorID) {
			SensorID other = (SensorID) obj;
			return other.getID().equals(getID());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}

	@Override
	public String toString() {
		return "" + id;
	}

	public int compareTo(SensorID o) {
		return getID().compareTo(o.getID());
	}
}
