package uk.ac.soton.ecs.mobilesensors.metric;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.mutable.MutableDouble;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.utils.Entropy;

public class ValueHistoryLight {

	private PrintWriter writer;

	private File file;

	private Map<Double, Entropy> averages = new TreeMap<Double, Entropy>();

	private Map<Double, MutableDouble> max = new TreeMap<Double, MutableDouble>();

	private Map<Double, MutableDouble> min = new TreeMap<Double, MutableDouble>();

	private File outputDirectory;

	private String filename;

	private boolean writeFullFile = false;

	public ValueHistoryLight(String header, File outputDirectory,
			String filename) {
		Validate.notNull(outputDirectory);
		Validate.isTrue(outputDirectory.exists());
		this.outputDirectory = outputDirectory;
		this.filename = filename;

		if (writeFullFile) {
			file = new File(outputDirectory, filename + ".gz");
			try {
				writer = new PrintWriter(new GZIPOutputStream(
						new FileOutputStream(file)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(e);
			}
			writer.write(header);
		}
	}

	public void addValue(Location location, double time, double value) {
		addValue(location.getCoordinates(), time, value);
	}

	public void addValue(Point2D coordinates, double time, double value) {
		if (writeFullFile) {
			String line = String.format(Locale.US,
					"%15.4f %25.15f %25.15f %25.15f\n", time, coordinates
							.getX(), coordinates.getY(), value);

			writer.write(line);
			writer.flush();
		}

		if (!averages.containsKey(time)) {
			averages.put(time, new Entropy());
			min.put(time, new MutableDouble(Double.MAX_VALUE));
			max.put(time, new MutableDouble(Double.MIN_VALUE));
		}

		averages.get(time).add(value);
		MutableDouble minDouble = min.get(time);
		minDouble.setValue(Math.min(minDouble.doubleValue(), value));
		MutableDouble maxDouble = max.get(time);
		maxDouble.setValue(Math.max(maxDouble.doubleValue(), value));
	}

	public void writeValueFile() throws IOException {
		if (writeFullFile) {
			writer.flush();
			writer.close();
		}

		PrintWriter averageWriter = new PrintWriter(new File(outputDirectory,
				filename + "_avg"));

		averageWriter.println("%Time    Average_Value");
		for (Double time : averages.keySet()) {
			double value = averages.get(time).getAverage();
			String line = String.format(Locale.US, "%15.4f  %25.15f", time,
					value);
			averageWriter.println(line);
		}

		averageWriter.flush();
		averageWriter.close();

		PrintWriter maxWriter = new PrintWriter(new File(outputDirectory,
				filename + "_max"));

		averageWriter.println("%Time    Max_Value");
		for (Double time : max.keySet()) {
			double value = max.get(time).doubleValue();
			String line = String.format(Locale.US, "%15.4f  %25.15f", time,
					value);
			maxWriter.println(line);
		}
		maxWriter.flush();
		maxWriter.close();

		PrintWriter minWriter = new PrintWriter(new File(outputDirectory,
				filename + "_min"));

		averageWriter.println("%Time    Min_Value");
		for (Double time : min.keySet()) {
			double value = min.get(time).doubleValue();
			String line = String.format(Locale.US, "%15.4f  %25.15f", time,
					value);
			minWriter.println(line);
		}
		minWriter.flush();
		minWriter.close();

		File entropyFile = new File(outputDirectory, filename + "_entropy");
		PrintWriter entropyWriter = new PrintWriter(entropyFile);

		try {
			averageWriter.println("%Time    Entropy");
			for (Double time : averages.keySet()) {
				double value = averages.get(time).getEntropy();
				String line = String.format(Locale.US, "%15.4f  %25.15f", time,
						value);
				entropyWriter.println(line);
			}
		} finally {
			entropyWriter.flush();
			entropyWriter.close();
		}
	}
}
