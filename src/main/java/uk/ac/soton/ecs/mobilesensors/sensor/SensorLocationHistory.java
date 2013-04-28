package uk.ac.soton.ecs.mobilesensors.sensor;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.LocationImpl;

public class SensorLocationHistory {

	private final String sensorIDString;
	private final List<Location> locations;
	private final List<Double> timeStamps;
	private SensorID sensorId;

	public SensorLocationHistory(SensorID sensorId, Location initialLocation) {
		locations = new ArrayList<Location>();
		timeStamps = new ArrayList<Double>();
		this.sensorIDString = sensorId.toString();
		this.sensorId = sensorId;
		addLocation(initialLocation, 0.0);
	}

	public void addLocation(Location location, double timeStep) {
		locations.add(location);
		timeStamps.add(timeStep);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		String tableHeader = "sensorID time X Y";

		buffer.append("% " + tableHeader + "\n");

		for (int i = 0; i < locations.size(); i++) {
			Point2D coords = locations.get(i).getCoordinates();

			buffer.append(String.format(Locale.US,
					"%5s %25.10f %25.10f %25.10f\n", sensorIDString, timeStamps
							.get(i), coords.getX(), coords.getY()));
		}

		return buffer.toString();
	}

	public static SensorLocationHistory readFromFile(File file)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));

		Validate.isTrue(reader.readLine().startsWith("%"));

		String line;

		SensorLocationHistory history = null;
		double timeStep = 0.0;

		while ((line = reader.readLine()) != null) {
			Scanner scanner = new Scanner(line);

			int sensorID = scanner.nextInt();
			double ts = scanner.nextDouble();
			double x = scanner.nextDouble();
			double y = scanner.nextDouble();
			Location location = new LocationImpl(x, y);

			if (history == null) {
				history = new SensorLocationHistory(new SensorID(sensorID),
						location);
			} else {
				history.addLocation(location, timeStep);
			}

			timeStep++;
		}

		return history;
	}

	public static void main(String[] args) throws IOException {
		// SensorLocationHistory history = new SensorLocationHistory(new
		// SensorID(
		// 4), new LocationImpl(0, 30));
		//
		// history.addLocation(new LocationImpl(421.3, 4293.1), 32.2);
		// history.addLocation(new LocationImpl(42144.3, 423.1), 32.2);
		// history.addLocation(new LocationImpl(421.3, 4293.1), 35.2);
		//
		// System.out.println(history);

		SensorLocationHistory readFromFile = SensorLocationHistory
				.readFromFile(new File(
						"/tmp/test_221_1263899059703/sensor0/sensor_locations"));

		System.out.println(readFromFile);
	}

	public Location get(double currentRound) {
		int index = timeStamps.indexOf(currentRound);

		if (index == -1) {
			System.out.println(timeStamps);
			System.out.println(currentRound);
		}

		return locations.get(index);
	}

	public SensorID getID() {
		return sensorId;
	}
}
