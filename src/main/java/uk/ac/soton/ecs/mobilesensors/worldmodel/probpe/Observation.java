package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

public class Observation extends
		uk.ac.soton.ecs.mobilesensors.worldmodel.Observation {

	private boolean detected;

	public void setDetected(boolean detected) {
		this.detected = detected;
	}

	public boolean isEvaderDetected() {
		return detected;
	}

	@Override
	public String toString() {
		return getSensedCoordinates() + " " + detected;
	}

}
