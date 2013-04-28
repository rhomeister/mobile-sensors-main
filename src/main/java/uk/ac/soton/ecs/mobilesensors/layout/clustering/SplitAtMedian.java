package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javolution.util.FastSet;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

public class SplitAtMedian implements Splitter {

	public List<Set<Location>> getClusters(DoubleMatrix1D secondEigV,
			BidiMap<Location, Integer> indexer, AccessibilityGraphImpl graph,
			DoubleMatrix2D weightMatrix) {
		List<Integer> vertexOrdering = getVertexOrdering(secondEigV);

		for (int i = 0; i < vertexOrdering.size() - 1; i++) {
			Validate.isTrue(secondEigV.get(vertexOrdering.get(i)) <= secondEigV
					.get(vertexOrdering.get(i + 1)));
		}

		List<Set<Location>> result = new ArrayList<Set<Location>>();

		int n = vertexOrdering.size();
		int half = n / 2;

		result.add(toLocations(vertexOrdering.subList(0, half), indexer));
		result.add(toLocations(vertexOrdering.subList(half, n), indexer));

		return result;
	}

	private Set<Location> toLocations(List<Integer> subList,
			BidiMap<Location, Integer> indexer) {
		Set<Location> result = new FastSet<Location>();
		for (Integer integer : subList) {
			result.add(indexer.getKey(integer));
		}

		return result;
	}

	private List<Integer> getVertexOrdering(final DoubleMatrix1D eigenVector) {
		List<Integer> sorted = new ArrayList<Integer>();

		for (int i = 0; i < eigenVector.size(); i++) {
			sorted.add(i);
		}

		Collections.sort(sorted, new Comparator<Integer>() {

			public int compare(Integer o1, Integer o2) {
				double i1 = eigenVector.get(o1);
				double i2 = eigenVector.get(o2);

				return Double.compare(i1, i2);
			}
		});

		return sorted;
	}

}
