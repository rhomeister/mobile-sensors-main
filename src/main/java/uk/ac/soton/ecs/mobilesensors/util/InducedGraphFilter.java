package uk.ac.soton.ecs.mobilesensors.util;

import java.util.Collection;

import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;

public class InducedGraphFilter<V, E> extends VertexPredicateFilter<V, E> {

	public InducedGraphFilter(final Collection<V> vertices) {
		super(new Predicate<V>() {
			public boolean evaluate(V object) {
				return vertices.contains(object);
			};
		});
	}

}
