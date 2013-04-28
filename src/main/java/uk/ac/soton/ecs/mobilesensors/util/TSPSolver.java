package uk.ac.soton.ecs.mobilesensors.util;

import java.util.List;

import uk.ac.soton.ecs.mobilesensors.layout.Location;

public interface TSPSolver {

	List<Location> computeTSP(Location start, Location finish,
			List<Location> path, double maxBudget);

}
