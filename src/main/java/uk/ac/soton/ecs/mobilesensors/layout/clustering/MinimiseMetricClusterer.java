package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;
import uk.ac.soton.ecs.mobilesensors.layout.gui.GraphGUI;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import edu.uci.ics.jung.algorithms.matrix.GraphMatrixOperations;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.util.Indexer;

public class MinimiseMetricClusterer {

	private final Splitter splitter;

	private final static Log log = LogFactory
			.getLog(MinimiseMetricClusterer.class);

	public static void main(String[] args) throws IOException {
		File file = new File(
				"../experiments/src/main/resources/graphs/building32-3x3.txt");
		Validate.isTrue(file.exists());

		AccessibilityGraphImpl graph = AccessibilityGraphImpl.readGraph(file);

		MinimiseMetricClusterer clusterer = new MinimiseMetricClusterer();
		ClusterResult<Location> tree = clusterer.clusterBiggest(graph, 64);

		for (Set<Location> set : tree.getClusters()) {
			System.out.println(set.size());
		}

		ClusteredGraph<Location, AccessibilityRelation> clusteredGraph = new ClusteredGraph<Location, AccessibilityRelation>(
				graph, tree);

		Collection<Location> boundaryVertices = clusteredGraph
				.getBoundaryVertices();

		AccessibilityGraphImpl subgraph = graph.getSubgraph(boundaryVertices);
		new GraphGUI(subgraph);
		new GraphGUI(graph, clusteredGraph);

		Set<Set<Location>> components = subgraph.getComponents();
		// System.out.println(components);

		for (Set<Location> set : components) {
			System.out.println(set);
		}

		Set<TransitNode<Location>> transitNodes = clusteredGraph
				.getTransitNodes();

		for (TransitNode<Location> transitNode : transitNodes) {
			System.out.println(clusteredGraph.getTransitMoves(transitNode));
		}
	}

	public MinimiseMetricClusterer(Splitter splitter) {
		this.splitter = splitter;
	}

	public MinimiseMetricClusterer() {
		this(new MinimiseConductance());
	}

	public ClusterResult<Location> clusterRecursively(
			AccessibilityGraphImpl graph, int times) {
		File file = new File("cluster-" + graph.hashCode() + "-recursive-"
				+ times);
		ClusterResult<Location> result = loadCachedResult(file);

		if (result == null) {
			result = new ClusterResult<Location>(new HashSet<Location>(
					graph.getVertices()));

			for (int i = 0; i < times; i++)
				splitAll(result, graph);

			saveCachedResult(result, file);
		}
		return result;
	}

	private void saveCachedResult(ClusterResult<Location> result, File file) {
		try {
			ClusterResult.write(file, result);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ClusterResult<Location> loadCachedResult(File file) {
		if (file.exists()) {
			log.info("Loading cached cluster result from " + file);
			try {
				return ClusterResult.read(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("No cached cluster result for this graph found "
					+ file);
		}
		return null;
	}

	private void splitAll(ClusterResult<Location> result,
			AccessibilityGraphImpl graph) {
		for (Set<Location> cluster : result.getClusters()) {
			result.split(cluster, split(graph.getSubgraph(cluster)));
		}
	}

	private void splitBiggest(ClusterResult<Location> result,
			AccessibilityGraphImpl graph) {
		Set<Location> biggest = Collections.max(result.getClusters(),
				new Comparator<Set<Location>>() {
					public int compare(Set<Location> o1, Set<Location> o2) {
						return o1.size() - o2.size();
					}
				});

		result.split(biggest, split(graph.getSubgraph(biggest)));
	}

	private void splitBiggestDiameter(ClusterResult<Location> result,
			final AccessibilityGraphImpl graph) {
		Set<Location> biggest = Collections.max(result.getClusters(),
				new Comparator<Set<Location>>() {
					public int compare(Set<Location> o1, Set<Location> o2) {
						return (int) (DistanceStatistics.diameter(graph
								.getSubgraph(o1)) - DistanceStatistics
								.diameter(graph.getSubgraph(o2)));
					}
				});

		result.split(biggest, split(graph.getSubgraph(biggest)));
	}

	private List<Set<Location>> split(AccessibilityGraphImpl graph) {
		BidiMap<Location, Integer> indexer = Indexer.<Location> create(graph
				.getVertices());

		int n = graph.getVertexCount();

		SparseDoubleMatrix2D weightMatrix = getMatrix(graph);

		SparseDoubleMatrix2D degreeMatrix = new SparseDoubleMatrix2D(n, n);

		for (int i = 0; i < n; i++) {
			degreeMatrix.set(i, i, weightMatrix.viewRow(i).zSum());
		}

		SparseDoubleMatrix2D laplacian = new SparseDoubleMatrix2D(n, n);

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				laplacian.set(i, j,
						degreeMatrix.get(i, j) - weightMatrix.get(i, j));
			}

		}

		EigenvalueDecomposition eigValueDecomposition = new EigenvalueDecomposition(
				laplacian);

		DoubleMatrix1D secondEigV = eigValueDecomposition.getV().viewColumn(1);

		return splitter.getClusters(secondEigV, indexer, graph, weightMatrix);

	}

	private SparseDoubleMatrix2D getMatrix(AccessibilityGraphImpl graph) {
		return GraphMatrixOperations.graphToSparseMatrix(graph);

		// // normalizeRows(graphToSparseMatrix);
		//
		// double diameter = Double.NEGATIVE_INFINITY;
		//
		// for (Location location1 : graph) {
		// for (Location location2 : graph) {
		// diameter = Math.max(diameter, graph.getShortestPathLength(
		// location1, location2));
		// }
		// }
		//
		// for (Location location1 : graph) {
		// for (Location location2 : graph) {
		// if (location1 == location2)
		// continue;
		//
		// int first = indexer.get(location1);
		// int second = indexer.get(location2);
		//
		// double shortestPathLength = graph.getShortestPathLength(
		// location1, location2);
		// graphToSparseMatrix.set(first, second, diameter
		// - shortestPathLength);
		// }
		// }
	}

	// private static void normalizeRows(SparseDoubleMatrix2D matrixA) {
	// for (int i = 0; i < matrixA.rows(); i++) {
	// double rowSum = matrixA.viewRow(i).zSum();
	//
	// for (int j = 0; j < matrixA.columns(); j++) {
	// matrixA.set(i, j, matrixA.get(i, j) / rowSum);
	// }
	// }
	//
	// // for (int i = 0; i < matrixA.rows(); i++) {
	// // double rowSum = matrixA.viewRow(i).zSum();
	// // System.out.println(rowSum);
	// // }
	//
	// }

	private static AccessibilityGraphImpl getGraph1() {
		AccessibilityGraphImpl graph = new AccessibilityGraphImpl();
		Location location1 = graph.addAccessibleLocation(10, 10);
		Location location2 = graph.addAccessibleLocation(100, 10);
		Location location3 = graph.addAccessibleLocation(10, 100);
		Location location4 = graph.addAccessibleLocation(100, 100);
		Location location5 = graph.addAccessibleLocation(200, 100);

		graph.addAccessibilityRelation(location1, location2);
		graph.addAccessibilityRelation(location2, location3);
		graph.addAccessibilityRelation(location3, location4);
		graph.addAccessibilityRelation(location1, location5);
		graph.addAccessibilityRelation(location2, location5);
		return graph;
	}

	public ClusterResult<Location> clusterBiggest(AccessibilityGraphImpl graph,
			int count) {
		File file = new File("cluster-" + graph.hashCode() + "-biggest-"
				+ count);
		ClusterResult<Location> result = loadCachedResult(file);

		if (result == null) {
			result = new ClusterResult<Location>(graph.getVertices());
			for (int i = 0; i < count - 1; i++) {
				System.err.println(i);
				splitBiggest(result, graph);
			}
			saveCachedResult(result, file);
		}

		return result;
	}

	public ClusterResult<Location> clusterBiggestDiameter(
			AccessibilityGraphImpl graph, int count) {
		File file = new File("cluster-" + graph.hashCode()
				+ "-biggest-diameter-" + count);
		ClusterResult<Location> result = loadCachedResult(file);

		if (result == null) {
			result = new ClusterResult<Location>(graph.getVertices());
			for (int i = 0; i < count - 1; i++) {
				System.err.println(i);
				splitBiggestDiameter(result, graph);
			}
			saveCachedResult(result, file);
		}

		return result;
	}
}
