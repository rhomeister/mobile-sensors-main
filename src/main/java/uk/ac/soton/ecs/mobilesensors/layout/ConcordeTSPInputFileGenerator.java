package uk.ac.soton.ecs.mobilesensors.layout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;

public class ConcordeTSPInputFileGenerator {

	public static void create(AccessibilityGraphImpl graph, File output) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("this should not happend");
		}

		try {
			writer.println("NAME:TEST");
			writer.println("TYPE:TSP");
			writer.println("DIMENSION:" + graph.getVertexCount());
			writer.println("EDGE_WEIGHT_TYPE:EXPLICIT");
			writer.println("EDGE_WEIGHT_FORMAT:LOWER_DIAG_ROW");
			writer.println("NODE_COORD_TYPE: NO_COORDS");
			writer.println("EDGE_WEIGHT_SECTION:");

			List<Location> vertices = getSortedVertices(graph);

			for (int row = 0; row < vertices.size(); row++) {
				for (int col = 0; col < row; col++) {
					Location location1 = vertices.get(row);
					Location location2 = vertices.get(col);

					double shortestPathLength = graph.getShortestPathLength(
							location1, location2);

					writer.println((int) shortestPathLength);
				}
				writer.println(0);
			}

			writer.write("EOF");
		} finally {
			writer.flush();
			writer.close();
		}
	}

	public static List<Location> getSortedVertices(AccessibilityGraphImpl graph) {
		List<Location> vertices = new ArrayList<Location>(graph.getVertices());

		Comparator<Location> comparator = new Comparator<Location>() {
			public int compare(Location o1, Location o2) {
				int xCompare = Double.compare(o1.getX(), o2.getX());

				if (xCompare != 0)
					return xCompare;

				return Double.compare(o1.getY(), o1.getY());
			}
		};

		// sort the vertices such that we can read the solution in the same
		// order
		Collections.sort(vertices, comparator);
		return vertices;
	}

	public static List<Location> readSolution(File file,
			AccessibilityGraphImpl graph) throws IOException {
		List<Location> vertices = getSortedVertices(graph);

		List<String> lines = FileUtils.readLines(file);

		Validate.isTrue(Integer.parseInt(lines.get(0)) == graph
				.getVertexCount());

		List<Location> destinations = new ArrayList<Location>();

		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			String[] indices = line.split(" ");

			for (String index : indices) {
				destinations.add(vertices.get(Integer.parseInt(index)));
			}
		}

		Location current = null;
		List<Location> path = new ArrayList<Location>();
		// compute all intermediate locations
		for (Location location : destinations) {
			if (current == null) {
				current = location;
				continue;
			}

			path.addAll(graph
					.getShortestPathLocations(current, location, false));
			current = location;
		}

		return destinations;
	}

	public static void main(String[] args) throws IOException {
		AccessibilityGraphImpl graph = AccessibilityGraphImpl
				.readGraph(new File(
						"/home/rs06r/workspace/experiments/src/main/resources/graphs/building32.txt"));

		create(graph, new File("large-room-tsp.txt"));

		// List<Location> tsp = readSolution(new File("test.sol"), graph);
		// System.out.println(tsp.size());
		// System.out.println(tsp);
	}
}
