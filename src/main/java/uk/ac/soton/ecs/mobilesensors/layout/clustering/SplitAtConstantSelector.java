package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javolution.util.FastList;

import org.apache.commons.collections15.BidiMap;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

public class SplitAtConstantSelector implements Splitter {

	private double c;

	public SplitAtConstantSelector(double c) {
		this.c = c;
	}

	public List<Set<Location>> getClusters(DoubleMatrix1D secondEigV,
			BidiMap<Location, Integer> indexer, AccessibilityGraphImpl graph,
			DoubleMatrix2D weightMatrix) {
		List<Set<Location>> clusters = new FastList<Set<Location>>();
		clusters.add(new HashSet<Location>());
		clusters.add(new HashSet<Location>());

		for (int i = 0; i < secondEigV.size(); i++) {
			if (secondEigV.get(i) < c) {
				clusters.get(0).add(indexer.getKey(i));
			} else {
				clusters.get(1).add(indexer.getKey(i));
			}
		}

		return clusters;
	}

}
