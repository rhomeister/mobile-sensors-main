package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import cern.colt.matrix.DoubleMatrix2D;

public class MinimiseRatioCut extends MinimiseMetricCut {

	protected double getValue(List<Integer> cluster1, List<Integer> cluster2,
			AccessibilityGraphImpl graph, DoubleMatrix2D weightMatrix) {

		Validate.isTrue(CollectionUtils.intersection(cluster1, cluster2)
				.isEmpty());

		double edgeSum = 0.0;

		for (Integer location1 : cluster1) {
			for (Integer location2 : cluster2) {
				edgeSum += weightMatrix.get(location1, location2);
			}
		}

		return edgeSum / (cluster1.size() * cluster2.size());
	}

}
