package uk.ac.soton.ecs.mobilesensors.metric;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public class ValueHistory2 {

	private File file;

	private PrintWriter writer;

	public ValueHistory2(String[] fieldNames, File outputDirectory,
			String filename) throws IOException {
		Validate.notNull(outputDirectory);

		file = new File(outputDirectory, filename + ".gz");
		writer = new PrintWriter(new GZIPOutputStream(
				new FileOutputStream(file)));

		writer.write("%" + StringUtils.join(fieldNames, " ") + "\n");
	}

	public void addLine(Object... objects) {
		String formatString = "";

		for (Object object : objects) {
			if (object instanceof Float || object instanceof Double)
				formatString += "%15.5f ";
			else if (object instanceof Integer)
				formatString += "%15d ";
			else
				formatString += "%15s ";
		}
		formatString += "\n";

		String line = String.format(Locale.US, formatString, objects);
		writer.write(line);
		writer.flush();
	}

	public void close() throws IOException {
		writer.flush();
		writer.close();
	}
}
