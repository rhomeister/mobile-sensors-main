package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastSet;

import org.apache.commons.collections15.BidiMap;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

public abstract class MinimiseMetricCut implements Splitter {

	public final List<Set<Location>> getClusters(DoubleMatrix1D secondEigV,
			BidiMap<Location, Integer> indexer, AccessibilityGraphImpl graph,
			DoubleMatrix2D weightMatrix) {

		List<Integer> ordering = getVertexOrdering(secondEigV);
		List<Set<Location>> result = new FastList<Set<Location>>();

		int bestSplit = 0;
		double minValue = Double.MAX_VALUE;

		for (int j = 1; j < secondEigV.size(); j++) {
			List<Integer> cluster1 = ordering.subList(0, j);
			List<Integer> cluster2 = ordering.subList(j, ordering.size());

			double value = getValue(cluster1, cluster2, graph, weightMatrix);

			if (value < minValue) {
				minValue = value;
				bestSplit = j;
			}
		}

		result.add(toLocations(ordering.subList(0, bestSplit), indexer));
		result.add(toLocations(ordering.subList(bestSplit, ordering.size()),
				indexer));

		return result;
	}

	protected abstract double getValue(List<Integer> cluster1,
			List<Integer> cluster2, AccessibilityGraphImpl graph,
			DoubleMatrix2D weightMatrix);

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
