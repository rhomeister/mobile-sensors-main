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

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.gui.GraphGUI;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import edu.uci.ics.jung.algorithms.matrix.GraphMatrixOperations;
import edu.uci.ics.jung.algorithms.util.Indexer;

public class KannanClusterer {

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

		for (Location location1 : graph) {
			for (Location location2 : graph) {
				// if (location1 == location2)
				// continue;

				int first = indexer.get(location1);
				int second = indexer.get(location1);

				double shortestPathLength = graph.getShortestPathLength(
						location1, location2);

				double value = shortestPathLength < 5 ? 1 : 0;

				matrixA.set(first, second, value);
			}
		}

		// normalizeAMatrix(matrixA);

		List<List<Integer>> clusters = new ArrayList<List<Integer>>();
		clusters.add(toIndices(graph.getLocations(), indexer));

		int k = 3;

		for (int i = 0; i < k; i++) {
			List<Integer> bestCluster = clusters.get(i); // getBestCluster(clusters);

			int[] indices = ArrayUtils.toPrimitive(bestCluster
					.toArray(new Integer[0]));

			DoubleMatrix2D matrixB = matrixA.viewSelection(indices, indices)
					.copy();

			// normaliseMatrix(matrixB);

			// eigenvector corresponding to second highest eigenvalue
			DoubleMatrix1D eigenVector = new EigenvalueDecomposition(matrixB)
					.getV().viewColumn(matrixB.rows() - 2);

			// order indices of cluster according to their appearance in the
			// eigenvector
			List<Location> vertexOrdering = getVertexOrdering(bestCluster,
					eigenVector, indexer);

			int bestSplit = 0;
			double bestValue = Double.MAX_VALUE;

			for (int j = 1; j < vertexOrdering.size(); j++) {
				List<Location> cluster1 = vertexOrdering.subList(0, j);
				List<Location> cluster2 = vertexOrdering.subList(j,
						vertexOrdering.size());

				double value = getValue(toIndices(cluster1, indexer),
						toIndices(cluster2, indexer), graph, matrixA);

				if (value < bestValue) {
					bestValue = value;
					bestSplit = j;
				}
			}

			clusters.remove(bestCluster);
			clusters.add(toIndices(vertexOrdering.subList(0, bestSplit),
					indexer));
			clusters.add(toIndices(vertexOrdering.subList(bestSplit,
					vertexOrdering.size()), indexer));

		}

		GraphGUI.show(graph, toLocations(clusters, indexer));
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

	private static double getValue(List<Integer> cluster1,
			List<Integer> cluster2, AccessibilityGraphImpl graph,
			DoubleMatrix2D weightMatrix) {

		double edgeSum = 0.0;

		for (Integer location1 : cluster1) {
			for (Integer location2 : cluster2) {
				edgeSum += weightMatrix.get(location1, location2);
			}
		}

		double aCluster1 = 0;
		double aCluster2 = 0;

		for (Integer i : cluster1) {
			aCluster1 += weightMatrix.viewRow(i).zSum();
		}

		for (Integer i : cluster2) {
			aCluster2 += weightMatrix.viewRow(i).zSum();
		}

		return edgeSum / Math.min(aCluster1, aCluster2);
	}

	public static List<Location> getVertexOrdering(
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
