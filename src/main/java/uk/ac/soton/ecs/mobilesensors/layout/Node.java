package uk.ac.soton.ecs.mobilesensors.layout;

import java.util.Set;

public interface Node<V> extends Iterable<V> {
	public Set<V> getVertices();

	public int size();

	public int getId();

}
