package uk.ac.soton.ecs.mobilesensors.configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class AccessibilityGraphReader {

	public AccessibilityGraphImpl load(FileReader fileReader, double scale)
			throws IOException {
		BufferedReader reader = new BufferedReader(fileReader);

		String line = reader.readLine();

		AccessibilityGraphImpl graph = new AccessibilityGraphImpl();

		Map<Integer, Location> locationIndices = new HashMap<Integer, Location>();

		int vertices = Integer.parseInt(line.substring(10));

		for (int i = 0; i < vertices; i++) {
			line = reader.readLine();
			Scanner scanner = new Scanner(line);
			int index = scanner.nextInt();

			double locationX = scanner.nextDouble() * scale;
			double locationY = scanner.nextDouble() * scale;

			locationIndices.put(index, graph.addAccessibleLocation(locationX,
					locationY));
		}

		line = reader.readLine();

		Validate.isTrue(line.startsWith("*Edges"));

		line = reader.readLine();

		while (line != null) {
			Scanner scanner = new Scanner(line);
			int vertexIndex1 = scanner.nextInt();
			int vertexIndex2 = scanner.nextInt();

			Location location1 = locationIndices.get(vertexIndex1);
			Location location2 = locationIndices.get(vertexIndex2);

			graph.addAccessibilityRelation(location1, location2);

			line = reader.readLine();
		}

		return graph;

	}

	public AccessibilityGraphImpl load(FileReader fileReader)
			throws IOException {
		return load(fileReader, 1.0);
	}

}
