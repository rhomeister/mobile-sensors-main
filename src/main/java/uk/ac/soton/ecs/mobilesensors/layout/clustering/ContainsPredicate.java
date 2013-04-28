package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.util.Collection;

import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;

public class ContainsPredicate<T1, T2> extends VertexPredicateFilter<T1, T2> {

	public ContainsPredicate(final Collection<T1> set) {
		super(new org.apache.commons.collections15.Predicate<T1>() {
			public boolean evaluate(T1 object) {
				return set.contains(object);
			};
		});

	}
}
