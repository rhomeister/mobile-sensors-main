package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.gui.GraphGUI;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.matrix.GraphMatrixOperations;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest;
import edu.uci.ics.jung.algorithms.util.Indexer;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.Pair;

public class Clusterer {

	public static void main(String[] args) throws IOException {
		File file = new File(
				"../experiments/src/main/resources/graphs/building32.txt");
		Validate.isTrue(file.exists());

		AccessibilityGraphImpl graph = AccessibilityGraphImpl.readGraph(file);

		SparseDoubleMatrix2D graphToSparseMatrix = GraphMatrixOperations
				.graphToSparseMatrix(graph);

		// System.out.println(graphToSparseMatrix);

		EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(
				graphToSparseMatrix);
		DoubleMatrix2D eigenvalues = eigenvalueDecomposition.getD();

		Validate.isTrue(eigenvalues.rows() == eigenvalues.columns());

		int smallerThan1Count = 0;
		int d = 2;

		int[] eigenValueIndices = new int[d];

		for (int i = eigenvalues.rows() - 1; i >= 0; i--) {
			if (eigenvalues.get(i, i) < 1) {
				eigenValueIndices[smallerThan1Count++] = i;
			}

			if (smallerThan1Count >= 2) {
				break;
			}
		}

		System.out.println(eigenValueIndices[0]);
		System.out.println(eigenValueIndices[1]);

		// columns are eigenvectors

		DoubleMatrix2D eigenVectors = eigenvalueDecomposition.getV();

		BidiMap<Location, Integer> indexer = Indexer.<Location> create(graph
				.getVertices());

		Map<AccessibilityRelation, Double> weights = new HashMap<AccessibilityRelation, Double>();

		for (AccessibilityRelation edge : graph.getEdges()) {
			Pair<Location> endpoints = graph.getEndpoints(edge);
			Location first = endpoints.getFirst();
			Location second = endpoints.getSecond();

			int firstIndex = indexer.get(first);
			int secondIndex = indexer.get(second);

			double edgeWeight = 0.0;

			for (int i = 0; i < eigenValueIndices.length; i++) {
				edgeWeight += Math.abs(eigenVectors.get(i, firstIndex)
						- eigenVectors.get(i, secondIndex));
			}

			weights.put(edge, edgeWeight);
		}

		DelegateForest<Location, AccessibilityRelation> forest = new DelegateForest<Location, AccessibilityRelation>();
		new MinimumSpanningForest<Location, AccessibilityRelation>(graph,
				forest, null, weights);

		AccessibilityGraphImpl accessibilityGraphImpl = new AccessibilityGraphImpl();
		for (Location location : forest.getVertices()) {
			accessibilityGraphImpl.addVertex(location);
		}

		for (AccessibilityRelation edge : forest.getEdges()) {
			accessibilityGraphImpl.addEdge(edge, edge.getLocation1(),
					edge.getLocation2());
		}

		new GraphGUI(accessibilityGraphImpl);

		WeakComponentClusterer<Location, AccessibilityRelation> clusterer = new WeakComponentClusterer<Location, AccessibilityRelation>();

		List<Double> sortedValues = new ArrayList<Double>(weights.values());
		Collections.sort(sortedValues);
		Collections.reverse(sortedValues);

		double bestValue = Double.MIN_VALUE;
		Set<Set<Location>> bestClustering = null;

		for (Double weight : sortedValues) {
			Collection<AccessibilityRelation> oldEdges = new ArrayList<AccessibilityRelation>(
					forest.getEdges());

			for (AccessibilityRelation edge : oldEdges) {
				if (weights.get(edge) >= weight) {
					int edgeCount = forest.getEdgeCount();
					forest.removeEdge(edge, false);
					Validate.isTrue(edgeCount - 1 == forest.getEdgeCount(),
							edgeCount + " " + forest.getEdgeCount());
				}
			}

			Collection<Tree<Location, AccessibilityRelation>> trees = forest
					.getTrees();

			int intraClusterEdges = 0;

			int sum = 0;

			for (Tree<Location, AccessibilityRelation> tree : trees) {
				sum += tree.getVertexCount() * (tree.getVertexCount() - 1);

				for (Location vertex : tree.getVertices()) {
					Collection<Location> neighbors = graph.getNeighbors(vertex);

					for (Location location : neighbors) {
						if (tree.containsVertex(location)) {
							intraClusterEdges++;
						}
					}
				}
			}

			double coverage = intraClusterEdges / 2.0 / graph.getEdgeCount();

			int n = graph.getVertexCount();

			double value = 1
					- (2 * graph.getEdgeCount() * (1 - coverage) + sum)
					/ (n * (n - 1));

			// intraClusterEdges /= 2;
			//
			// int count = 0;
			//
			// for (Tree<Location, AccessibilityRelation> cluster1 : trees) {
			// for (Tree<Location, AccessibilityRelation> cluster2 : trees) {
			// if (cluster1 == cluster2)
			// continue;
			//
			// for (Location vertex1 : cluster1.getVertices()) {
			// for (Location vertex2 : cluster2.getVertices()) {
			// if (!graph.isNeighbor(vertex1, vertex2)) {
			// count++;
			// }
			// }
			// }
			// }
			// }
			//
			// double value = intraClusterEdges + count;

			System.out.println(value);

			if (value > bestValue) {
				bestClustering = clusterer.transform(forest);
				bestValue = value;
			}
		}

		System.out.println(bestValue);

		// d.forEachNonZero(new IntIntDoubleFunction() {
		//
		// public double apply(int arg0, int arg1, double arg2) {
		// System.out.println(arg0 + " " + arg1 + " " + arg2);
		// return arg2;
		// }
		// });
		//
		GraphGUI.show(graph, bestClustering);
	}
}
