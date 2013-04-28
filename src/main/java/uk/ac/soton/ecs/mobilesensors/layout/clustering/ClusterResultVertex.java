package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;

public class ClusterResultVertex<V> implements Serializable {

	private static final long serialVersionUID = -4563442048584987647l;
	private Set<V> elements;
	private transient List<Cluster<V>> clusters;

	public ClusterResultVertex(Collection<V> cluster) {
		this.elements = new HashSet<V>(cluster);
	}

	public Set<V> getElements() {
		return elements;
	}

	@Override
	public int hashCode() {
		return elements.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClusterResultVertex<?>) {
			ClusterResultVertex<V> other = (ClusterResultVertex<V>) obj;
			return other.elements.equals(elements);
		}

		return false;
	}

	public List<Cluster<V>> getClusters() {
		return clusters;
	}

	public void setClusters(List<Cluster<V>> clusters) {
		this.clusters = clusters;
	}

	public boolean isUnlabeled() {
		return clusters == null;
	}

	@Override
	public String toString() {
		if (!isUnlabeled()) {
			return clusters.toString();
		} else {
			return "not labeled";
		}
	}
}
