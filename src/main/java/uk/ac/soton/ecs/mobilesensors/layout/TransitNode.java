package uk.ac.soton.ecs.mobilesensors.layout;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TransitNode<V> implements Node<V> {

	private final Set<V> set;
	private final int id;
	private final boolean external;
	private final ClusteredGraph<V, ?> graph;
	private final static TransitNode<Location> fake;

	static {
		// (531.0, 654.0), (391.0, 654.0), (465.5, 613.0), (496.0, 654.0),
		// (461.0, 654.0), (426.0, 654.0)

		Set<Location> set1 = new HashSet<Location>();
		set1.add(new LocationImpl(531.0, 654.0));
		set1.add(new LocationImpl(531.0, 654.0));
		set1.add(new LocationImpl(391.0, 654.0));
		set1.add(new LocationImpl(465.5, 613.0));
		set1.add(new LocationImpl(496.0, 654.0));
		set1.add(new LocationImpl(461.0, 654.0));
		set1.add(new LocationImpl(426.0, 654.0));

		fake = null; // new TransitNode<Location>(set1, 4, true);
	}

	public TransitNode(Set<V> set, int index, boolean external,
			ClusteredGraph<V, ?> graph) {
		this.set = set;
		this.id = index;
		this.external = external;
		this.graph = graph;

		if (equals(fake)) {
			// throw new IllegalArgumentException();
		}
	}

	public int getId() {
		return id;
	}

	public Set<V> getVertices() {
		return Collections.unmodifiableSet(set);
	}

	public int size() {
		return set.size();
	}

	public Iterator<V> iterator() {
		return set.iterator();
	}

	public boolean contains(V vertex) {
		return set.contains(vertex);
	}

	@Override
	public int hashCode() {
		return set.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TransitNode<?>) {
			TransitNode<?> node = (TransitNode<?>) obj;

			if (node.set.equals(set)) {
				if (node.getId() != getId()) {
					throw new IllegalArgumentException(
							"Why is this happening??");
				}
			}

			return node.set.equals(set);
		}
		return false;
	}

	@Override
	public String toString() {
		return "TN " + getId();
	}

	public ClusteredGraph<V, ?> getGraph() {
		return graph;
	}

	public V getRepresentativeVertex(Cluster<V> cluster) {
		for (V vertex : set) {
			if (cluster.contains(vertex))
				return vertex;
		}

		throw new IllegalArgumentException(
				"Cluster not connected to this TransitNode");
	}

	public boolean isExternal() {
		return external;
	}

}
