package uk.ac.soton.ecs.mobilesensors.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public class PathUtils {
	public static Map<Sensor, List<Move>> makeSameLength(
			Map<Sensor, List<Location>> paths) {
		int maxSize = 1;

		for (List<Location> path : paths.values()) {
			maxSize = Math.max(maxSize, path.size());
		}

		Map<Sensor, List<Move>> result = new HashMap<Sensor, List<Move>>();
		for (Sensor sensor : paths.keySet()) {
			List<Location> path = paths.get(sensor);

			Location last;
			if (!path.isEmpty()) {
				last = path.get(path.size() - 1);
				path = path.subList(1, path.size());
			} else
				last = sensor.getLocation();

			int currentSize = path.size();
			if (path.isEmpty())
				currentSize--;

			for (int i = 0; i < maxSize - currentSize - 1; i++) {
				path.add(last);
			}

			result.put(sensor, Move.convertToMoves(path));
		}

		for (List<Location> path : paths.values()) {
			Validate.isTrue(path.size() == maxSize);
		}

		return result;
	}
}
