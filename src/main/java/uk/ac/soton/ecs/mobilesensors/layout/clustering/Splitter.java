package uk.ac.soton.ecs.mobilesensors.layout.clustering;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

public interface Splitter {

	List<Set<Location>> getClusters(DoubleMatrix1D secondEigV,
			BidiMap<Location, Integer> indexer, AccessibilityGraphImpl graph,
			DoubleMatrix2D weightMatrix);

}
