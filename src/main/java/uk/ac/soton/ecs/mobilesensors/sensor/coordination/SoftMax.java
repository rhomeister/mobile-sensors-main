package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.math.stat.StatUtils;

import uk.ac.soton.ecs.utils.ArrayUtils;

public class SoftMax {

	private static SoftMax instance;

	public static SoftMax getInstance() {
		if (instance == null) {
			instance = new SoftMax();
		}

		return instance;
	}

	public <T extends Object> T select(Map<T, Double> utilities,
			double temperature) {
		List<T> objects = new ArrayList<T>();
		double[] utility = new double[utilities.size()];

		int i = 0;
		for (T object : utilities.keySet()) {
			objects.add(object);
			utility[i++] = utilities.get(object);
		}

		return select(objects, utility, temperature);
	}

	public <T extends Object> T select(List<T> objects, final double[] utility,
			double temperature) {
		if (temperature == 0.0) {
			T bestObject = null;
			double maxUtility = Double.NEGATIVE_INFINITY;

			for (int i = 0; i < utility.length; i++) {
				if (utility[i] > maxUtility) {
					maxUtility = utility[i];
					bestObject = objects.get(i);
				}
			}

			return bestObject;
		} else {
			double[] normalizedUtility = ArrayUtils.normalize(utility);
			double[] expUtils = new double[normalizedUtility.length];

			for (int i = 0; i < normalizedUtility.length; i++)
				expUtils[i] = Math.exp(1 / temperature * normalizedUtility[i]);

			double[] selectionProbability = ArrayUtils.divide(expUtils,
					StatUtils.sum(expUtils));

			double rand = RandomUtils.nextDouble();

			System.out.println(Arrays.toString(normalizedUtility));
			System.out.println(Arrays.toString(selectionProbability));

			double sum = 0.0;
			int i = 0;
			do {
				sum += selectionProbability[i++];
			} while (rand > sum);

			return objects.get(i - 1);
		}
	}
}
