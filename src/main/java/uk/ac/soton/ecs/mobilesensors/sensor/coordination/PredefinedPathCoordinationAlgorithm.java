package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.ConcordeTSPInputFileGenerator;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.patrolling.PatrollingInformativenessFunction;
import uk.ac.soton.ecs.mobilesensors.worldmodel.probpe.AbstractProbabilisticPursuitEvaderModel;
import uk.ac.soton.ecs.mobilesensors.worldmodel.probpe.Observation;

public class PredefinedPathCoordinationAlgorithm extends
		AbstractAdjacentCoordinationMechanism {

	private List<Location> path;

	// pointer to current element in path
	private int currentIndex = -1;

	private String pathIndices;

	private Location overrideDestination;

	@Required
	public void setPathIndices(String pathIndices) {
		this.pathIndices = pathIndices;
	}

	@Override
	protected Move determineBestContiguousMove(double time) {
		// if we have detected an attacker / evader -> move to that location
		if (getInformativenessFunction() instanceof PatrollingInformativenessFunction
				|| getInformativenessFunction() instanceof AbstractProbabilisticPursuitEvaderModel) {
			Collection<Observation> observations = (Collection<Observation>) getObservations();

			for (Observation observation : observations) {
				if (observation.isEvaderDetected()) {

					System.err
							.println("evader detected " + getSensor().getID());

					Location location = getGraph().getNearestLocation(
							observation.getSensedCoordinates());

					overrideDestination = location;
					break;
				}
			}
		}

		if (getCurrentLocation().equals(overrideDestination)
				|| currentIndex == -1) {
			overrideDestination = null;
			currentIndex = path.indexOf(getCurrentLocation());
		}

		if (overrideDestination != null) {
			List<AccessibilityRelation> shortestPath = getGraph()
					.getShortestPath(getCurrentLocation(), overrideDestination);
			return new Move(shortestPath.get(0).getOther(getCurrentLocation()));
		} else {
			int nextIndex = (++currentIndex) % path.size();
			return new Move(path.get(nextIndex));
		}
	}

	public void initialize(Simulation simulation) {
		AccessibilityGraphImpl graph = simulation.getEnvironment()
				.getAccessibilityGraph();

		pathIndices = pathIndices.replaceAll("\\n", " ");
		pathIndices = pathIndices.replaceAll("\\t", " ");
		pathIndices = pathIndices.replaceAll(" +", " ");
		pathIndices = pathIndices.trim();

		String[] split = pathIndices.split(" ");
		path = new ArrayList<Location>(split.length);

		Validate.isTrue(split.length == graph.getVertexCount());

		List<Location> sortedVertices = ConcordeTSPInputFileGenerator
				.getSortedVertices(graph);

		List<Location> destinations = new ArrayList<Location>();

		for (String string : split) {
			int index = Integer.parseInt(string.trim());
			destinations.add(sortedVertices.get(index));
		}

		Validate.isTrue(destinations.size() == graph.getVertexCount());

		// Location current = null;
		path = new ArrayList<Location>();
		// // compute all intermediate locations
		// for (Location location : destinations) {
		// if (current == null) {
		// current = location;
		// continue;
		// }
		//
		// path.addAll(graph
		// .getShortestPathLocations(current, location, false));
		// current = location;
		// }

		for (int i = 0; i < destinations.size(); i++) {
			Location current = destinations.get(i);
			Location next = destinations.get((i + 1) % destinations.size());

			path.addAll(graph.getShortestPathLocations(current, next, false));
		}

		checkPath(path);

		// try {
		// path = ConcordeTSPInputFileGenerator.readSolution(pathFile
		// .getFile(), graph);
		// } catch (IOException e) {
		// throw new IllegalStateException(e);
		// }
	}

	private void checkPath(List<Location> path2) {
		for (int i = 0; i < path2.size() - 1; i++) {
			Location location = path2.get(i);
			Location location2 = path2.get(i + 1);

			Validate.isTrue(!location.equals(location2));
			Validate.isTrue(location.getNeighbours().contains(location2));
		}

		Location location = path2.get(0);
		Location location2 = path2.get(path2.size() - 1);
		Validate.isTrue(!location.equals(location2));
		Validate.isTrue(location.getNeighbours().contains(location2));
	}
}
