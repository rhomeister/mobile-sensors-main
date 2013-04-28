package uk.ac.soton.ecs.mobilesensors.layout;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.clustering.ClusterResult;
import uk.ac.soton.ecs.mobilesensors.layout.clustering.ClusterResultVertex;
import uk.ac.soton.ecs.mobilesensors.layout.clustering.MinimiseMetricClusterer;
import uk.ac.soton.ecs.mobilesensors.layout.gui.GraphGUI;
import uk.ac.soton.ecs.utils.GraphUtils;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class HierarchicalClusteredGraph<V, E> extends
		DelegateTree<ClusteredGraph<V, E>, Integer> {
	private final int maxClustersPerLevel;

	private class ClusterSizeComparator implements
			Comparator<ClusterResultVertex<V>> {
		public int compare(ClusterResultVertex<V> o1, ClusterResultVertex<V> o2) {
			return o2.getElements().size() - o1.getElements().size();
		}
	};

	private class ClusterDiameterComparator implements
			Comparator<ClusterResultVertex<V>> {
		public int compare(ClusterResultVertex<V> o1, ClusterResultVertex<V> o2) {
			return Double.compare(computeSizeMetric(o2), computeSizeMetric(o1));
		}
	};

	private final Map<ClusterResultVertex<V>, Double> distance = new HashMap<ClusterResultVertex<V>, Double>();

	private final Map<Cluster<V>, ClusteredGraph<V, E>> subGraphs = new HashMap<Cluster<V>, ClusteredGraph<V, E>>();

	private final int maxClusterDiameter;

	private final Comparator<ClusterResultVertex<V>> comparator = new ClusterDiameterComparator();

	private final UndirectedGraph<V, E> graph;

	public HierarchicalClusteredGraph(UndirectedGraph<V, E> graph,
			ClusterResult<V> result, int maxClustersPerLevel,
			int maxClusterDiameter) {
		this.graph = graph;
		this.maxClustersPerLevel = maxClustersPerLevel;
		this.maxClusterDiameter = maxClusterDiameter;

		removeClustersOverSatisfyingClusterDiameter(result);

		cluster(graph, result, null);

		initialiseExternalTransitNodes(getRoot());
	}

	private void initialiseExternalTransitNodes(ClusteredGraph<V, E> parent) {
		for (ClusteredGraph<V, E> child : getChildren(parent)) {
			child.addExternalTransitNodes(parent.getTransitNodes());
			initialiseExternalTransitNodes(child);
		}
	}

	private void removeClustersOverSatisfyingClusterDiameter(
			ClusterResult<V> result) {
		List<ClusterResultVertex<V>> unprocessedLeafs = result.getLeafs();

		while (!unprocessedLeafs.isEmpty()) {
			ClusterResultVertex<V> leaf = unprocessedLeafs
					.remove(unprocessedLeafs.size() - 1);

			if (result.containsVertex(leaf)) {
				ClusterResultVertex<V> parent = result.getParent(leaf);
				while (parent != null
						&& computeSizeMetric(parent) < maxClusterDiameter) {
					leaf = parent;
					parent = result.getParent(parent);
				}

				for (ClusterResultVertex<V> child : result.getChildren(leaf)) {
					result.removeChild(child);
					unprocessedLeafs.remove(child);
				}
			}
		}
	}

	private double computeSizeMetric(ClusterResultVertex<V> vertex) {
		Double result = distance.get(vertex);

		if (result == null) {
			result = (double) DistanceStatistics.diameter(GraphUtils
					.getSubgraph(graph, vertex.getElements()));
			distance.put(vertex, result);
		}

		return result;
	}

	public ClusteredGraph<V, E> getContainingGraph(Cluster<Location> cluster) {
		for (ClusteredGraph<V, E> vertex : getVertices()) {
			if (vertex.getClusters().contains(cluster))
				return vertex;
		}

		return null;
	}

	public ClusteredGraph<V, E> getSubGraph(Cluster<Location> cluster) {
		return subGraphs.get(cluster);
	}

	private void cluster(UndirectedGraph<V, E> graph, ClusterResult<V> result,
			ClusteredGraph<V, E> parent) {
		PriorityQueue<ClusterResultVertex<V>> queue = new PriorityQueue<ClusterResultVertex<V>>(
				maxClustersPerLevel, comparator);

		queue.add(result.getRoot());

		while (queue.size() < Math.min(maxClustersPerLevel,
				result.getLeafCount())) {
			Validate.isTrue(!queue.isEmpty());

			ClusterResultVertex<V> poll = queue.poll();

			System.out.println(queue.size());
			System.out.println(result.getLeafCount());

			if (result.isLeaf(poll))
				queue.add(poll);
			else {
				queue.addAll(result.getChildren(poll));
			}
		}

		ClusterResult<V> clusterResult = new ClusterResult<V>(result.getRoot()
				.getElements());

		int edgeCount = 0;
		for (ClusterResultVertex<V> clusterResultVertex : queue) {
			if (!clusterResultVertex.equals(clusterResult.getRoot()))
				clusterResult.addChild(edgeCount++, clusterResult.getRoot(),
						clusterResultVertex);
		}

		ClusteredGraph<V, E> clusteredGraph = new ClusteredGraph<V, E>(graph,
				clusterResult);

		if (parent == null) {
			setRoot(clusteredGraph);
			clusteredGraph.setLabel("root");
		} else {
			Cluster<V> superCluster = parent.getCluster(clusterResult.getRoot()
					.getElements());
			clusteredGraph.setLabel(parent.getLabel() + " "
					+ superCluster.getId());
			subGraphs.put(superCluster, clusteredGraph);
			addChild(getEdgeCount() + 1, parent, clusteredGraph);
		}

		for (ClusterResultVertex<V> clusterResultVertex : queue) {
			ClusterResult<V> subtree = result.getSubtree(clusterResultVertex);
			if (subtree.getVertexCount() == 1)
				continue;

			cluster(GraphUtils.getSubgraph(graph,
					clusterResultVertex.getElements()), subtree, clusteredGraph);
		}
	}

	@Override
	public ClusteredGraph<V, E> getRoot() {
		return root;
	}

	public static void main(String[] args) throws IOException {
		File file = new File(
				"../experiments/src/main/resources/graphs/building32-4x4.txt");
		Validate.isTrue(file.exists());

		AccessibilityGraphImpl graph = AccessibilityGraphImpl.readGraph(file);

		System.out.println(graph.getVertexCount());
		System.out.println(graph.getEdgeCount());

		MinimiseMetricClusterer clusterer = new MinimiseMetricClusterer();
		ClusterResult<Location> tree = clusterer.clusterBiggestDiameter(graph,
				128);

		file = new File(
				"../experiments/src/main/resources/graphs/building32-3x3.txt");
		Validate.isTrue(file.exists());

		graph = AccessibilityGraphImpl.readGraph(file);

		System.out.println(graph.getVertexCount());
		System.out.println(graph.getEdgeCount());

		clusterer = new MinimiseMetricClusterer();
		tree = clusterer.clusterBiggestDiameter(graph, 128);

		file = new File(
				"../experiments/src/main/resources/graphs/building32-2x2.txt");
		Validate.isTrue(file.exists());

		graph = AccessibilityGraphImpl.readGraph(file);

		System.out.println(graph.getVertexCount());
		System.out.println(graph.getEdgeCount());

		clusterer = new MinimiseMetricClusterer();
		tree = clusterer.clusterBiggestDiameter(graph, 128);
	}

	public static void main1(String[] args) throws IOException,
			InterruptedException {
		File file = new File(
				"../experiments/src/main/resources/graphs/building32-4x4.txt");
		Validate.isTrue(file.exists());

		AccessibilityGraphImpl graph = AccessibilityGraphImpl.readGraph(file);

		System.out.println(graph.getVertexCount());
		System.out.println(graph.getEdgeCount());

		MinimiseMetricClusterer clusterer = new MinimiseMetricClusterer();
		ClusterResult<Location> tree = clusterer.clusterBiggestDiameter(graph,
				128);

		// HierarchicalClusteredGraph<Location, AccessibilityRelation> fullGraph
		// = new HierarchicalClusteredGraph<Location, AccessibilityRelation>(
		// graph, tree, 70, 10000);

		// GraphGUI graphGUI = new GraphGUI(
		// graph,
		// new ClusteredGraph<Location, AccessibilityRelation>(graph, tree));

		// Thread.sleep(10040);
		//
		// graphGUI.saveToSVG();

		// graphGUI.saveToSVG();

		int maxDiam = 10;
		int maxClusters = 6;

		HierarchicalClusteredGraph<Location, AccessibilityRelation> hierarchicalClusteredGraph = new HierarchicalClusteredGraph<Location, AccessibilityRelation>(
				graph, tree, maxClusters, maxDiam);

		System.out.println("dfjkldfjkl "
				+ hierarchicalClusteredGraph.getTotalClusterCount());

		Collection<ClusteredGraph<Location, AccessibilityRelation>> vertices = hierarchicalClusteredGraph
				.getVertices();

		ClusteredGraph<Location, AccessibilityRelation> root = hierarchicalClusteredGraph
				.getRoot();

		new GraphGUI(new AccessibilityGraphImpl(root.getGraph()), root);

		// new TempViewer(hierarchicalClusteredGraph);

		//
		for (ClusteredGraph<Location, AccessibilityRelation> clusteredGraph : vertices) {
			// if (hierarchicalClusteredGraph.isLeaf(clusteredGraph)) {
			// new GraphGUI(new
			// AccessibilityGraphImpl(clusteredGraph.getGraph()),
			// clusteredGraph);
			ClusterGraphGUI.show(clusteredGraph);
			// break;
			// }
		}
		//
		// for (Cluster<Location> cluster : root.getClusters()) {
		// System.out.println(hierarchicalClusteredGraph.getSubGraph(cluster));
		// }
	}

	public int getTotalClusterCount() {
		int result = 0;
		for (ClusteredGraph<V, E> vertex : getVertices()) {
			if (isLeaf(vertex)) {
				result += vertex.getClusterCount();
			}
		}

		return result;
	}

	public int getLeafCount(ClusteredGraph<V, E> subGraph) {
		if (isLeaf(subGraph)) {
			return subGraph.getClusterCount();
		}

		int count = 0;
		Collection<ClusteredGraph<V, E>> children = getChildren(subGraph);
		for (ClusteredGraph<V, E> clusteredGraph : children) {
			count += getLeafCount(clusteredGraph);
		}
		return count;
	}
}