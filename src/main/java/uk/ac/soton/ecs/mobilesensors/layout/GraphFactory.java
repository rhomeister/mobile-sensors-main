package uk.ac.soton.ecs.mobilesensors.layout;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.soton.ecs.utils.ArrayUtils;

public class GraphFactory {

	public static AccessibilityGraphImpl createRectangularGridGraph(
			double lengthX, double lengthY, double gridWidth, double originX,
			double originY) {
		return createRectangularGridGraph(lengthX, lengthY,
				(int) (lengthX / gridWidth) + 1,
				(int) (lengthY / gridWidth) + 1, originX, originY);
	}

	public static void removeLocations(AccessibilityGraphImpl graph,
			Rectangle2D rectangle) {
		List<Location> locations = new ArrayList<Location>(graph.getLocations());

		for (Location location : locations) {
			if (rectangle.contains(location.getCoordinates())) {
				graph.removeLocation(location);
			}
		}
	}

	public static AccessibilityGraphImpl createRectangularGridGraph(
			double lengthX, double lengthY, int precisionX, int precisionY,
			double originX, double originY) {
		AccessibilityGraphImpl graph = new AccessibilityGraphImpl();

		Location[] lastRow = new LocationImpl[precisionY];
		for (double x : ArrayUtils.linspace(originX, originX + lengthX,
				precisionX)) {
			Location[] currentRow = new LocationImpl[precisionY];
			Location previousLocation = null;
			int yi = 0;
			for (double y : ArrayUtils.linspace(originY, originY + lengthY,
					precisionY)) {

				Location location = graph.addAccessibleLocation(x, y);

				currentRow[yi] = location;

				if (previousLocation != null)
					graph.addAccessibilityRelation(previousLocation, location);
				if (lastRow[yi] != null)
					graph.addAccessibilityRelation(lastRow[yi], location);

				previousLocation = location;
				yi++;
			}
			lastRow = currentRow;
		}

		return graph;
	}

	public static void main(String[] args) throws IOException {
		AccessibilityGraphImpl graph = createRectangularGridGraph(300, 300, 3,
				3, 10, 10.0);

	}

	public static AccessibilityGraphImpl connect(AccessibilityGraphImpl graph1,
			AccessibilityGraphImpl graph2, double connectionDistance,
			Collection<Location> connectLocations1,
			Collection<Location> connectLocations2) {
		AccessibilityGraphImpl graph = new AccessibilityGraphImpl();

		graph.copyFrom(graph1);
		graph.copyFrom(graph2);

		for (Location location1 : connectLocations1) {
			for (Location location2 : connectLocations2) {
				if (location1.directDistance(location2) <= connectionDistance) {
					graph.addAccessibilityRelation(
							graph.getLocation(location1), graph
									.getLocation(location2));
				}
			}
		}

		return graph;
	}

	public static AccessibilityGraphImpl connect(AccessibilityGraphImpl graph1,
			AccessibilityGraphImpl graph2, double connectionDistance) {
		return connect(graph1, graph2, connectionDistance, graph1
				.getLocations(), graph2.getLocations());
	}
}
