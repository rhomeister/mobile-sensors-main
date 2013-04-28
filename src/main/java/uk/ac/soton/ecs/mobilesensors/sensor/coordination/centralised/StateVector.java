package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class StateVector<S extends State> extends HashMap<S, Double> {

	private static final long serialVersionUID = 1L;

	public void normalise() {

		double probabilitySum = 0.0;
		for (Double d : values()) {
			probabilitySum += d;
		}

		for (Entry<S, Double> entry : entrySet()) {
			entry.setValue(entry.getValue() / probabilitySum);
		}
	}

	public void addMultiply(Map<S, Double> other, double scalar) {
		Set<Entry<S, Double>> entrySet = other.entrySet();

		for (Entry<S, Double> entry : entrySet) {
			S key = entry.getKey();
			Double previousProbability = get(key);

			if (previousProbability == null) {
				previousProbability = 0.0;
			}

			put(key, previousProbability + scalar * entry.getValue());
		}
	}

}
