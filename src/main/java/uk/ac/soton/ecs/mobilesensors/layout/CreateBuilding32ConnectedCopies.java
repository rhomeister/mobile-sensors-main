package uk.ac.soton.ecs.mobilesensors.layout;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import uk.ac.soton.ecs.mobilesensors.layout.gui.GraphGUI;

public class CreateBuilding32ConnectedCopies {
	public static void main(String[] args) throws IOException {
		int xCopies = 4;
		int yCopies = 4;
		double xOffset = 701.0;
		double yOffset = 640.0;
		double connectDistance = 45;

		AccessibilityGraphImpl result = createConnectedCopies(xCopies, yCopies,
				xOffset, yOffset, connectDistance);

		new GraphGUI(result);

		// result.write("../experiments/src/main/resources/graphs/building32-4x4.txt");

		// AccessibilityGraphImpl graph = AccessibilityGraphImpl
		// .readGraph(new File(
		// "../experiments/src/main/resources/graphs/building32.txt"));
		// new GraphGUI(graph);
		//
		// MinimiseMetricClusterer clusterer = new MinimiseMetricClusterer();
		// ClusterResult<Location> result = clusterer.clusterBiggest(graph, 10);
		// ClusteredGraph<Location, AccessibilityRelation> clusteredGraph = new
		// ClusteredGraph<Location, AccessibilityRelation>(
		// graph, result);
		//
		// GraphGUI graphGUI = new GraphGUI(graph, clusteredGraph, null);
		// ClusterGraphGUI.show(clusteredGraph, graphGUI.getClusterColorMap());
	}

	public static AccessibilityGraphImpl createConnectedCopies(int xCopies,
			int yCopies, double xOffset, double yOffset, double connectDistance)
			throws IOException {
		AccessibilityGraphImpl[][] graphCopies = new AccessibilityGraphImpl[xCopies][yCopies];
		Collection<Location>[][] connectLocations = new Collection[xCopies][yCopies];

		AccessibilityGraphImpl result = null;

		for (int i = 0; i < xCopies; i++) {
			for (int j = 0; j < yCopies; j++) {
				AccessibilityGraphImpl graph = graphCopies[i][j] = AccessibilityGraphImpl
						.readGraph(new File(
								"../experiments/src/main/resources/graphs/building32.txt"));
				connectLocations[i][j] = getConnectionLocations(graph);

				graph.translate(xOffset * i, yOffset * j);

				if (result == null) {
					result = graph;
				} else {
					Collection<Location> resultConnectLocations = new ArrayList<Location>();

					if (i > 0)
						resultConnectLocations
								.addAll(connectLocations[i - 1][j]);
					if (j > 0)
						resultConnectLocations
								.addAll(connectLocations[i][j - 1]);

					result = GraphFactory.connect(result, graph,
							connectDistance, resultConnectLocations,
							connectLocations[i][j]);
				}
			}
		}

		return result;

	}

	public static Collection<Location> getConnectionLocations(
			AccessibilityGraphImpl graph) {
		Collection<Location> result = new HashSet<Location>();

		result.add(graph.getLocation(new Point2D.Double(76, 14)));
		result.add(graph.getLocation(new Point2D.Double(286, 14)));
		result.add(graph.getLocation(new Point2D.Double(321, 14)));
		result.add(graph.getLocation(new Point2D.Double(461, 14)));
		result.add(graph.getLocation(new Point2D.Double(496, 14)));
		result.add(graph.getLocation(new Point2D.Double(601, 14)));

		result.add(graph.getLocation(new Point2D.Double(82.5, 617)));
		result.add(graph.getLocation(new Point2D.Double(308, 615)));
		result.add(graph.getLocation(new Point2D.Double(465.5, 613)));
		result.add(graph.getLocation(new Point2D.Double(597.5, 614)));

		result.add(graph.getLocation(new Point2D.Double(6, 14)));
		result.add(graph.getLocation(new Point2D.Double(670.5, 424)));
		result.add(graph.getLocation(new Point2D.Double(670.5, 459)));
		result.add(graph.getLocation(new Point2D.Double(8.5, 426.5)));
		result.add(graph.getLocation(new Point2D.Double(8.5, 461.5)));
		result.add(graph.getLocation(new Point2D.Double(671, 14)));

		return result;
	}
}
