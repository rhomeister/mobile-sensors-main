package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelationImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.LocationImpl;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;

/**
 * A sensor with the StaticPatrollingCoordinationMechanism patrols between two
 * locations in the environment. It follows the shortest path between these
 * locations.
 * 
 * @author rs06r
 * 
 */
public class StaticPatrollingCoordinationMechanism extends
		AbstractCoordinationMechanism {

	private boolean initialized;
	private Location patrollingFrom;
	private Location patrollingTo;
	private Iterator<AccessibilityRelationImpl> currentPlan;

	public Move determineBestMove(double time) {
		// swap locations when destination has been reached
		if (!currentPlan.hasNext()) {
			Location temp = patrollingFrom;
			patrollingFrom = patrollingTo;
			patrollingTo = temp;

			DijkstraShortestPath shortestPath = new DijkstraShortestPath(
					getGraph());
			currentPlan = shortestPath.getPath(patrollingFrom, patrollingTo)
					.iterator();
		}

		return new Move(currentPlan.next().getLocation2());
	}

	public void initialize() {
		initialized = true;

		List<SensorID> ids = new ArrayList<SensorID>(getNeighbourIDs());
		ids.add(sensor.getID());
		Collections.sort(ids);
		int rank = ids.indexOf(sensor.getID());

		Rectangle2D boundingRectangle = getGrid().getBoundingRectangle();

		double startX = boundingRectangle.getMinX();
		double startY = (rank + 0.1) * boundingRectangle.getHeight()
				/ ids.size();
		double endX = boundingRectangle.getMaxX();
		double endY = (rank + 0.9) * boundingRectangle.getHeight() / ids.size();

		patrollingFrom = getGraph().getNearestLocation(startX, startY);
		patrollingTo = getGraph().getNearestLocation(endX, endY);

		DijkstraShortestPath shortestPath = new DijkstraShortestPath(getGraph());
		currentPlan = shortestPath.getPath((LocationImpl) getCurrentLocation(),
				patrollingTo).iterator();
	}

	public void initialize(Simulation simulation) {
		initialize();
	}

}
