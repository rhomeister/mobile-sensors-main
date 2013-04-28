package uk.ac.soton.ecs.mobilesensors.metric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.LocationImpl;

public class ValueHistory {

	private final List<Location> locations = new ArrayList<Location>();

	private final List<Double> timestamps = new ArrayList<Double>();

	private final List<Double> values = new ArrayList<Double>();

	private PrintWriter writer;

	private File file;

	private Map<Double, Double> valuesForTimestampAverage = new HashMap<Double, Double>();

	private Map<Double, Integer> valuesForTimestampCount = new HashMap<Double, Integer>();

	private boolean writeToFile = true;

	public ValueHistory() {
		writeToFile = false;
	}

	public ValueHistory(String header, File outputDirectory, String filename) {
		Validate.notNull(outputDirectory);
		Validate.isTrue(outputDirectory.exists());

		file = new File(outputDirectory, filename + ".gz");
		try {
			writer = new PrintWriter(new GZIPOutputStream(new FileOutputStream(
					file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
		writer.write(header);
	}

	public void addValue(Location location, double time, double value) {
		locations.add(location);
		timestamps.add(time);
		this.values.add(value);

		if (valuesForTimestampCount.get(time) == null) {
			valuesForTimestampCount.put(time, 1);
			valuesForTimestampAverage.put(time, value);
		} else {
			int count = valuesForTimestampCount.get(time);
			valuesForTimestampCount.put(time, count++);
			double previousAverage = valuesForTimestampAverage.get(time);
			valuesForTimestampAverage.put(time,
					(previousAverage * count + value) / (count + 1));
		}

		if (writeToFile) {
			String line = String.format(Locale.US,
					"%15.4f %25.15f %25.15f %25.15f\n", time, location.getX(),
					location.getY(), value);

			writer.write(line);
			writer.flush();
		}
	}

	public void writeValueFile() throws IOException {
		if (writeToFile) {
			writer.flush();
			writer.close();
		}

	}

	public List<Double> getTimestamps() {
		return timestamps;
	}

	public List<Double> getValuesForTimestamp(double timestamp) {
		List<Double> result = new ArrayList<Double>();

		for (int i = 0; i < timestamps.size(); i++) {
			if (timestamps.get(i) == timestamp) {
				result.add(values.get(i));
			}
		}

		return result;
	}

	public void addValues(List<Location> locations, double timestep,
			List<Double> values) {
		for (int i = 0; i < locations.size(); i++) {
			addValue(locations.get(i), timestep, values.get(i));
		}
	}

	public static ValueHistory readFromFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line;

		while ((line = reader.readLine()).startsWith("%"))
			;

		ValueHistory history = new ValueHistory();

		while ((line = reader.readLine()) != null) {
			Scanner scanner = new Scanner(line);

			double timeStep = scanner.nextDouble();
			double x = scanner.nextDouble();
			double y = scanner.nextDouble();
			double value = scanner.nextDouble();

			history.addValue(new LocationImpl(x, y), timeStep, value);
		}

		return history;
	}
}
