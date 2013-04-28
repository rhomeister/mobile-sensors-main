package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.util.Pair;

public class ClusterResult<V> extends
		DelegateTree<ClusterResultVertex<V>, Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -735937177340292836L;

	private List<ClusterResultVertex<V>> order = new ArrayList<ClusterResultVertex<V>>();

	public ClusterResult(Collection<V> root) {
		setRoot(new ClusterResultVertex<V>(root));
		order.add(getRoot());
	}

	public ClusterResult() {
	}

	/**
	 * Creates a subtree from given root
	 * 
	 * @param result
	 * @param root
	 */
	public ClusterResult(ClusterResult<V> result, ClusterResultVertex<V> root) {
		setRoot(root);
		addChildren(root, result);
	}

	public ClusterResult<V> getSubtree(ClusterResultVertex<V> root) {
		return new ClusterResult<V>(this, root);
	}

	private void addChildren(ClusterResultVertex<V> parent,
			ClusterResult<V> result) {
		for (ClusterResultVertex<V> child : result.getChildren(parent)) {
			addChild(getEdgeCount() + 1, parent, child);
			addChildren(child, result);
		}
	}

	public List<Set<V>> getClusters() {
		List<Set<V>> result = new ArrayList<Set<V>>();

		for (ClusterResultVertex<V> vertex : getVertices()) {
			if (getChildCount(vertex) == 0) {
				result.add(vertex.getElements());
			}
		}

		return result;
	}

	@Override
	public boolean addChild(Integer edge, ClusterResultVertex<V> parent,
			ClusterResultVertex<V> child) {
		order.add(child);
		return super.addChild(edge, parent, child);
	}

	public void split(Set<V> parent, Set<V>[] children, Set<V> child2) {
		for (Set<V> set : children) {
			addChild(getEdgeCount() + 1, new ClusterResultVertex<V>(parent),
					new ClusterResultVertex<V>(set));
		}
	}

	public void split(Set<V> parent, List<Set<V>> split) {
		for (Set<V> set : split) {

			addChild(getEdgeCount() + 1, new ClusterResultVertex<V>(parent),
					new ClusterResultVertex<V>(set));
		}
	}

	// public static void main(String[] args) throws FileNotFoundException,
	// IOException, ClassNotFoundException {
	// DelegateTree<String, String> delegateTree = new DelegateTree<String,
	// String>();
	// delegateTree.setRoot("hoi");
	//
	// ObjectOutputStream stream = new ObjectOutputStream(
	// new FileOutputStream("test"));
	// stream.writeObject(delegateTree);
	// stream.close();
	//
	// ObjectInputStream stream1 = new ObjectInputStream(new FileInputStream(
	// "test"));
	// DelegateTree<String, String> o = (DelegateTree<String, String>) stream1
	// .readObject();
	//
	// System.out.println(o.getRoot());
	//
	// }

	/**
	 * Custom deserialization is needed.
	 */
	public static <V> ClusterResult<V> read(File file) throws IOException,
			ClassNotFoundException {
		ObjectInputStream stream = new ObjectInputStream(new FileInputStream(
				file));

		ClusterResult<V> result = new ClusterResult<V>();

		ClusterResultVertex<V> root = (ClusterResultVertex<V>) stream
				.readObject();
		Map<Integer, Pair<ClusterResultVertex<V>>> map = (Map<Integer, Pair<ClusterResultVertex<V>>>) stream
				.readObject();

		result.setRoot(root);

		boolean changed = true;

		while (changed) {
			changed = false;
			for (Entry<Integer, Pair<ClusterResultVertex<V>>> entry : map
					.entrySet()) {
				ClusterResultVertex<V> parent = entry.getValue().getFirst();
				ClusterResultVertex<V> child = entry.getValue().getSecond();

				if (!result.getVertices().contains(parent)) {
					continue;
				}

				if (!result.getEdges().contains(entry.getKey())) {
					result.addEdge(entry.getKey(), parent, child);
					changed = true;
				}
			}
		}

		result.order = (List<ClusterResultVertex<V>>) stream.readObject();

		return result;
	}

	/**
	 * Custom serialization is needed.
	 */
	public static <V> void write(File file, ClusterResult<V> result)
			throws IOException {
		Map<Integer, Pair<ClusterResultVertex<V>>> map = new HashMap<Integer, Pair<ClusterResultVertex<V>>>();

		for (Integer edge : result.getEdges()) {
			map.put(edge, result.getEndpoints(edge));
		}
		ObjectOutputStream stream = new ObjectOutputStream(
				new FileOutputStream(file));

		stream.writeObject(result.getRoot());
		stream.writeObject(map);
		stream.writeObject(result.getOrder());
	}

	public List<ClusterResultVertex<V>> getOrder() {
		return order;
	}

	public void labelVertices(List<Cluster<V>> clusters) {
		boolean changed;

		do {
			changed = false;
			for (ClusterResultVertex<V> node : getVertices()) {
				if (node.isUnlabeled() && isLeaf(node)) {
					for (Cluster<V> cluster : clusters) {
						if (cluster.getVertices().equals(node.getElements())) {
							node.setClusters(Collections.singletonList(cluster));
							break;
						}
					}
					if (node.isUnlabeled()) {
						throw new IllegalStateException();
					}

					changed = true;
				} else if (node.isUnlabeled() && allChildrenLabeled(node)) {
					List<Cluster<V>> childClusters = new ArrayList<Cluster<V>>();

					for (ClusterResultVertex<V> child : getChildren(node)) {
						childClusters.addAll(child.getClusters());
					}

					node.setClusters(childClusters);
					changed = true;
				}
			}
		} while (changed);

		Validate.isTrue(allVerticesLabeled());
	}

	private boolean allVerticesLabeled() {
		Collection<ClusterResultVertex<V>> vertices = getVertices();

		for (ClusterResultVertex<V> vertex : vertices) {
			if (vertex.isUnlabeled())
				return false;
		}

		return true;
	}

	private boolean allChildrenLabeled(ClusterResultVertex<V> node) {
		for (ClusterResultVertex<V> child : getChildren(node)) {
			if (child.getClusters() == null) {
				return false;
			}
		}
		return true;
	}

	public void printBreadthFirst() {
		Queue<ClusterResultVertex<V>> queue = new LinkedBlockingQueue<ClusterResultVertex<V>>();
		queue.add(getRoot());

		int currentDepth = 0;
		while (!queue.isEmpty()) {
			ClusterResultVertex<V> poll = queue.poll();
			if (getDepth(poll) != currentDepth) {
				System.out.println();
				currentDepth++;
			}
			System.out.print(poll + " ");

			queue.addAll(getChildren(poll));
		}
		System.out.println();
	}

	public int getLeafCount() {
		int count = 0;
		for (ClusterResultVertex<V> vertex : getVertices()) {
			if (isLeaf(vertex)) {
				count++;
			}
		}
		return count;
	}

	public List<ClusterResultVertex<V>> getLeafs() {
		List<ClusterResultVertex<V>> leafs = new ArrayList<ClusterResultVertex<V>>();

		for (ClusterResultVertex<V> vertex : getVertices()) {
			if (isLeaf(vertex))
				leafs.add(vertex);
		}

		return leafs;
	}
}