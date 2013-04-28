package uk.ac.soton.ecs.mobilesensors.sensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class UtilityHistory {

	List<Location> currentLocations = new ArrayList<Location>();
	List<Double> timestamps = new ArrayList<Double>();
	List<List<Move>> consideredMoves = new ArrayList<List<Move>>();
	List<List<Double>> consideredMoveUtility = new ArrayList<List<Double>>();
	private final Sensor sensor;
	private final List<Move> selectedMoves = new ArrayList<Move>();

	public UtilityHistory(Sensor sensor) {
		this.sensor = sensor;
	}

	public void add(Location location, double time, List<Move> moves,
			double[] utilities, Move selectedMove) {
		currentLocations.add(location);
		timestamps.add(time);
		consideredMoves.add(moves);
		consideredMoveUtility
				.add(Arrays.asList(ArrayUtils.toObject(utilities)));
		selectedMoves.add(selectedMove);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		String tableHeader = "sensorID time currentX currentY consideredX "
				+ "consideredX utility";

		buffer.append("% " + tableHeader + "\n");

		for (int i = 0; i < currentLocations.size(); i++) {
			Double timestamp = timestamps.get(i);
			Location currentLocation = currentLocations.get(i);
			Move selectedMove = selectedMoves.get(i);

			for (int j = 0; j < consideredMoves.get(i).size(); j++) {
				Double utility = consideredMoveUtility.get(i).get(j);
				Move consideredMove = consideredMoves.get(i).get(j);
				int isChosen = consideredMove.equals(selectedMove) ? 1 : 0;

				buffer.append(String.format(Locale.US,
						"%5s %15.6f %15.6f %15.6f %15.6f %15.6f %15.6f %10d\n",
						sensor.getID(), timestamp, currentLocation.getX(),
						currentLocation.getY(), consideredMove.getX(),
						consideredMove.getY(), utility, isChosen));
			}
		}

		return buffer.toString();
	}
}
