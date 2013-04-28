package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javolution.util.FastSet;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.gui.GraphGUI;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import edu.uci.ics.jung.algorithms.matrix.GraphMatrixOperations;
import edu.uci.ics.jung.algorithms.util.Indexer;

public class ShiMalikClusterer {

	public static void main(String[] args) throws IOException {
		File file = new File(
				"../experiments/src/main/resources/graphs/building32.txt");
		Validate.isTrue(file.exists());

		AccessibilityGraphImpl graph = AccessibilityGraphImpl.readGraph(file);

		SparseDoubleMatrix2D matrixA = GraphMatrixOperations
				.graphToSparseMatrix(graph);

		BidiMap<Location, Integer> indexer = Indexer.<Location> create(graph
				.getVertices());

		double diameter = Double.NEGATIVE_INFINITY;

		for (Location location1 : graph) {
			for (Location location2 : graph) {
				diameter = Math.max(diameter, graph.getShortestPathLength(
						location1, location2));
			}
		}

		System.out.println(diameter);

		for (Location location1 : graph) {
			for (Location location2 : graph) {
				// if (location1 == location2)
				// continue;

				int first = indexer.get(location1);
				int second = indexer.get(location2);

				double shortestPathLength = graph.getShortestPathLength(
						location1, location2);

				double value = shortestPathLength < 5 ? 1 : 0;

				matrixA.set(first, second, diameter - shortestPathLength);
			}
		}

		System.out.println(matrixA);

		normalizeAMatrix(matrixA);

		List<Set<Location>> clusters = new ArrayList<Set<Location>>();
		// clusters.add(toIndices(graph.getLocations(), indexer));

		int[] indices = { 2, 4, 5, 7, 12, 15, 16, 21, 23, 24, 27, 29, 30, 31,
				33, 38, 39, 40, 41, 42, 45, 47, 53, 54, 56, 58, 60, 61, 62, 63,
				64, 67, 68, 70, 73, 74, 76, 77, 78, 80, 81, 83, 84, 85, 86, 87,
				88, 90, 92, 94, 95, 96, 98, 99, 100, 103, 105, 107, 109, 110,
				111, 116, 118, 123, 124, 125, 126, 127, 129, 130, 131, 134,
				142, 143, 145, 148, 149, 150, 152, 153, 157, 163, 164, 165,
				166, 167, 169, 170, 171, 173, 174, 177, 178, 183, 185, 190,
				196, 197, 199, 200, 201, 209, 210, 211, 213, 215, 222, 229,
				232, 234, 236, 238, 239, 240, 245, 246, 248, 250, 251, 252,
				254, 256, 257, 258, 259, 260, 263, 265, 266, 271, 274, 275,
				276, 281, 283, 285, 286, 290, 291, 292, 293, 294, 296, 297,
				298, 301, 302, 303, 304, 306, 311, 317, 319, 327, 328, 331,
				333, 337, 338, 343, 344, 345, 346 };

		Set<Location> cluster1 = new FastSet<Location>();

		for (int i : indices) {
			cluster1.add(indexer.getKey(i));
		}

		Set<Location> cluster2 = new FastSet<Location>();

		for (Location location : graph.getVertices()) {
			if (!cluster1.contains(location)) {
				cluster2.add(location);
			}
		}

		clusters.clear();
		clusters.add(cluster1);
		clusters.add(cluster2);

		GraphGUI.show(graph, clusters);
	}

	private static void normalizeAMatrix(SparseDoubleMatrix2D matrixA) {
		for (int i = 0; i < matrixA.rows(); i++) {
			double rowSum = matrixA.viewRow(i).zSum();

			for (int j = 0; j < matrixA.columns(); j++) {
				matrixA.set(i, j, matrixA.get(i, j) / rowSum);
			}
		}
	}

	private static Collection<Set<Location>> toLocations(
			List<List<Integer>> clusters, BidiMap<Location, Integer> indexer) {
		Collection<Set<Location>> result = new ArrayList<Set<Location>>();

		for (List<Integer> list : clusters) {
			Set<Location> locationList = new HashSet<Location>();

			for (Integer index : list) {
				locationList.add(indexer.getKey(index));
			}

			result.add(locationList);
		}

		return result;
	}

	private static List<Integer> toIndices(List<Location> subList,
			BidiMap<Location, Integer> indexer) {
		List<Integer> result = new ArrayList<Integer>();

		for (Location location : subList) {
			result.add(indexer.get(location));
		}

		return result;

	}

	private static double getValue(List<Location> cluster1,
			List<Location> cluster2, AccessibilityGraphImpl graph) {

		double edgeSum = 0.0;

		for (Location location1 : cluster1) {
			for (Location location2 : cluster2) {
				if (graph.isNeighbor(location1, location2))
					edgeSum++;
			}
		}

		double aCluster1 = 0;
		double aCluster2 = 0;
		for (Location location1 : graph) {
			for (Location location2 : cluster1) {
				aCluster1++;
			}

			for (Location location2 : cluster2) {
				aCluster2++;
			}
		}

		return edgeSum / Math.min(aCluster1, aCluster2);

	}

	private static List<Location> getVertexOrdering(
			final List<Integer> bestCluster, final DoubleMatrix1D eigenVector,
			BidiMap<Location, Integer> indexer) {

		List<Integer> sorted = new ArrayList<Integer>(bestCluster);

		Collections.sort(sorted, new Comparator<Integer>() {

			public int compare(Integer o1, Integer o2) {
				int i1 = bestCluster.indexOf(o1);
				int i2 = bestCluster.indexOf(o2);

				return Double.compare(eigenVector.get(i2), eigenVector.get(i1));
			}
		});

		List<Location> result = new ArrayList<Location>();

		for (Integer index : sorted) {
			result.add(indexer.getKey(index));
		}

		return result;
	}

	private static void normaliseMatrix(DoubleMatrix2D matrixB) {
		for (int i = 0; i < matrixB.rows(); i++) {
			double rowSum = matrixB.viewRow(i).zSum();
			rowSum -= matrixB.get(i, i);
			Validate.isTrue(rowSum <= 1.0);
			matrixB.set(i, i, 1 - rowSum);
		}
	}
}
