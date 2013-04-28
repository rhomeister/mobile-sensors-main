package uk.ac.soton.ecs.mobilesensors.layout;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class Cluster<V> implements Node<V>, Serializable {

	private final Set<V> set;
	private final Set<V> boundaryVertices = new HashSet<V>();
	private Map<V, Set<V>> otherClusterBoundaryVertices = new FastMap<V, Set<V>>();
	private final int id;

	public Cluster(Set<V> set, UndirectedGraph<V, ?> graph, int id) {
		this.set = set;
		this.id = id;

		for (V v : set) {
			Collection<V> neighbors = graph.getNeighbors(v);

			for (V v2 : neighbors) {
				if (!set.contains(v2)) {
					if (boundaryVertices.add(v))
						otherClusterBoundaryVertices.put(v, new HashSet<V>());

					otherClusterBoundaryVertices.get(v).add(v2);
				}
			}
		}
	}

	public Iterator<V> iterator() {
		return set.iterator();
	}

	public int size() {
		return set.size();
	}

	public Set<V> getVertices() {
		return set;
	}

	public Set<V> getBoundaryVertices() {
		return boundaryVertices;
	}

	public boolean contains(V v) {
		return set.contains(v);
	}

	/**
	 * 
	 * @param sensorLocation
	 * @return a list of boundary vertices of *other* clusters that can be
	 *         reached from a boundary vertex
	 */
	public Set<V> getNeighbouringClusters(V v) {
		return otherClusterBoundaryVertices.get(v);
	}

	public int getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return set.hashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof Cluster) {
			Cluster other = (Cluster) arg0;
			return other.set.equals(set);
		}

		return false;
	}

	@Override
	public String toString() {
		return "C " + getId();
	}

}
