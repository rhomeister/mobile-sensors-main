package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.util.List;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import cern.colt.matrix.DoubleMatrix2D;

public class MinimiseConductance extends MinimiseMetricCut {

	protected double getValue(List<Integer> cluster1, List<Integer> cluster2,
			AccessibilityGraphImpl graph, DoubleMatrix2D weightMatrix) {

		double edgeSum = 0.0;

		for (Integer location1 : cluster1) {
			for (Integer location2 : cluster2) {
				edgeSum += weightMatrix.get(location1, location2);
			}
		}

		double aCluster1 = 0;
		double aCluster2 = 0;

		for (Integer i : cluster1) {
			aCluster1 += weightMatrix.viewRow(i).zSum();
		}

		for (Integer i : cluster2) {
			aCluster2 += weightMatrix.viewRow(i).zSum();
		}

		return edgeSum / Math.min(aCluster1, aCluster2);
	}
}
