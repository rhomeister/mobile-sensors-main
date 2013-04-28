package uk.ac.soton.ecs.mobilesensors.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;
import javolution.util.FastSet;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.clustering.ClusterResult;
import uk.ac.soton.ecs.mobilesensors.util.InducedGraphFilter;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class ClusteredGraph<V, E> extends
		UndirectedSparseGraph<Node<V>, ClusterEdge<V>> {

	private static final long serialVersionUID = -692021098618652453L;
	private final UndirectedGraph<V, E> graph;
	private final List<Cluster<V>> clusters;
	private final Map<V, Cluster<V>> containedIn = new FastMap<V, Cluster<V>>();
	private final Map<V, TransitNode<V>> containedInTransitNodes = new FastMap<V, TransitNode<V>>();
	private final Set<Set<V>> transitNodeVertices;
	private final Set<TransitNode<V>> transitNodes = new FastSet<TransitNode<V>>();
	private final Set<TransitNode<V>> externalTransitNodes = new FastSet<TransitNode<V>>();
	private String label;

	public ClusteredGraph(UndirectedGraph<V, E> graph, ClusterResult<V> tree) {
		this.graph = graph;
		this.clusters = new ArrayList<Cluster<V>>();

		int index = 0;

		for (Set<V> set : tree.getClusters()) {
			Cluster<V> cluster = new Cluster<V>(set, graph, index++);
			clusters.add(cluster);

			for (V v : set) {
				containedIn.put(v, cluster);
			}
		}

		Validate.isTrue(containedIn.keySet().containsAll(graph.getVertices()));

		Graph<V, E> subgraph;

		if (clusters.size() > 1) {
			// compute subgraph containing only boundary vertices
			subgraph = new InducedGraphFilter<V, E>(getBoundaryVertices())
					.transform(graph);
		} else {
			subgraph = new UndirectedSparseGraph<V, E>();
			subgraph.addVertex(containedIn.keySet().iterator().next());
		}

		transitNodeVertices = new WeakComponentClusterer<V, E>()
				.transform(subgraph);

		buildGraph();
		tree.labelVertices(clusters);
	}

	public Cluster<V> getCluster(Set<V> elements) {
		for (Cluster<V> cluster : clusters) {
			if (cluster.getVertices().equals(elements)) {
				return cluster;
			}
		}

		throw new IllegalArgumentException(
				"No cluster found with these vertices");
	}

	public Set<TransitNode<V>> getExternalTransitNodes() {
		return externalTransitNodes;
	}

	public void addExternalTransitNodes(Set<TransitNode<V>> nodes) {
		for (TransitNode<V> transitNode : nodes) {

			for (TransitNode<V> node : transitNodes) {
				if (transitNode.getVertices().containsAll(node.getVertices())
						&& node.getVertices().containsAll(
								transitNode.getVertices())) {
					System.err.println("already exists!");
					continue;
				}
			}

			if (getExternalTransitionNode(transitNode, false) != null) {
				System.err.println("already exists!");
				continue;
			}

			Collection<Cluster<V>> adjacentClusters = new ArrayList<Cluster<V>>();

			for (V v : transitNode) {
				Cluster<V> cluster = containedIn.get(v);

				if (cluster != null) {
					adjacentClusters.add(cluster);
				}
			}

			if (adjacentClusters.isEmpty()) {
				continue;
			}

			Validate.notEmpty(adjacentClusters);

			TransitNode<V> transitNodeCopy = new TransitNode<V>(
					transitNode.getVertices(), transitNodes.size() + 1, true,
					this);
			addTransitNode(transitNodeCopy);
			externalTransitNodes.add(transitNodeCopy);

			for (Cluster<V> cluster : adjacentClusters) {
				addEdge(new ClusterEdge<V>(), cluster, transitNodeCopy);
			}

		}
	}

	private void buildGraph() {
		for (Cluster<V> cluster : clusters) {
			addVertex(cluster);
		}

		for (Set<V> transitNodeSet : transitNodeVertices) {
			TransitNode<V> transitNode = new TransitNode<V>(transitNodeSet,
					transitNodes.size() + 1, false, this);
			addTransitNode(transitNode);

			for (V v1 : transitNode) {
				Cluster<V> cluster1 = containedIn.get(v1);
				addEdge(new ClusterEdge<V>(), cluster1, transitNode);
			}
		}
	}

	private void addTransitNode(TransitNode<V> transitNode) {
		transitNodes.add(transitNode);

		for (V v : transitNode) {
			containedInTransitNodes.put(v, transitNode);
		}
		addVertex(transitNode);
	}

	@SuppressWarnings("unchecked")
	public Set<List<Node<V>>> getTransitMoves(TransitNode<V> transitNode) {
		Set<List<Node<V>>> transitMoves = new HashSet<List<Node<V>>>();

		Validate.notNull(transitNode);
		Validate.isTrue(containsVertex(transitNode));

		for (Node<V> node : getNeighbors(transitNode)) {
			Cluster<V> cluster = (Cluster<V>) node;

			for (Node<V> node1 : getNeighbors(cluster)) {
				TransitNode<V> transitNode2 = (TransitNode<V>) node1;

				transitMoves.add(Arrays.asList(transitNode, cluster,
						transitNode2));
			}
		}

		return transitMoves;
	}

	public List<Cluster<V>> getClusters() {
		return clusters;
	}

	public UndirectedGraph<V, E> getGraph() {
		return graph;
	}

	public Cluster<V> getCluster(V v) {
		return containedIn.get(v);
	}

	public Collection<V> getBoundaryVertices() {
		Collection<V> result = new ArrayList<V>();

		for (Cluster<V> cluster : clusters) {
			result.addAll(cluster.getBoundaryVertices());
		}

		return result;
	}

	public Set<TransitNode<V>> getTransitNodes() {
		return transitNodes;
	}

	public TransitNode<V> getTransitNode(V v) {
		return containedInTransitNodes.get(v);
	}

	public int getClusterCount() {
		return clusters.size();
	}

	public TransitNode<V> getExternalTransitionNode(TransitNode<V> startNode) {
		return getExternalTransitionNode(startNode, true);
	}

	public TransitNode<V> getExternalTransitionNode(TransitNode<V> startNode,
			boolean throwExceptionIfNotExists) {
		for (TransitNode<V> externalNode : externalTransitNodes) {
			if (externalNode.getVertices().containsAll(startNode.getVertices())
					&& startNode.getVertices().containsAll(
							externalNode.getVertices())) {
				return externalNode;
			}
		}

		if (throwExceptionIfNotExists)
			throw new IllegalArgumentException();
		else
			return null;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void checkConsistency() {
		for (Cluster<V> cluster : clusters) {
			for (Cluster<V> cluster2 : clusters) {
				if (cluster.getId() == cluster2.getId()) {
					Validate.isTrue(cluster.getVertices().containsAll(
							cluster2.getVertices()));
					Validate.isTrue(cluster2.getVertices().containsAll(
							cluster.getVertices()));
				}
			}
		}

		Validate.isTrue(transitNodes.containsAll(externalTransitNodes));

		for (TransitNode<V> cluster : transitNodes) {
			for (TransitNode<V> cluster2 : transitNodes) {
				if (cluster.getId() == cluster2.getId()) {
					Validate.isTrue(cluster.getVertices().containsAll(
							cluster2.getVertices()));
					Validate.isTrue(cluster2.getVertices().containsAll(
							cluster.getVertices()));
				}
			}
		}

	}
}
