package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import maxSumController.discrete.DiscreteVariableDomainImpl;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.util.KMeansClusterer;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;
import uk.ac.soton.ecs.utils.TopNFilter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter.EdgeType;

public class LocalKMeansPartitionedDomain extends
		DiscreteVariableDomainImpl<MultiStepMove> {
	public LocalKMeansPartitionedDomain(Location location, int length,
			AccessibilityGraphImpl graph, int clusterCount, int domainSize,
			ObservationInformativenessFunction spatialField) {

		Validate.isTrue(clusterCount >= domainSize);
		Validate.notNull(spatialField);

		// find subgraph reachable in 'length' steps
		KNeighborhoodFilter<Location, AccessibilityRelation> filter = new KNeighborhoodFilter<Location, AccessibilityRelation>(
				location, length, EdgeType.IN_OUT);

		AccessibilityGraphImpl neighbourHood = new AccessibilityGraphImpl(
				filter.transform(graph));

		// // partition subgraph into 'clusterCount' clusters
		KMeansClusterer cluster = new KMeansClusterer(clusterCount);
		Collection<Set<Location>> clusters = cluster.transform(neighbourHood,
				spatialField.getValues());
		//
		// MinimiseMetricClusterer clusterer = new MinimiseMetricClusterer();
		// List<Set<Location>> clusters =
		// clusterer.clusterBiggest(neighbourHood,
		// clusterCount);

		// find the top 'domainSize' vertices
		TopNFilter<Point2D, Double> destinations = new TopNFilter<Point2D, Double>(
				domainSize);

		// find max vertex in each cluster
		for (Set<Location> set : clusters) {
			double maxValue = Double.NEGATIVE_INFINITY;
			Location bestDestination = null;

			for (Location potentialDestination : set) {
				double value = spatialField.getValues().get(
						potentialDestination.getCoordinates());
				if (value > maxValue) {
					maxValue = value;
					bestDestination = potentialDestination;
				}
			}

			destinations.add(bestDestination.getCoordinates(), maxValue);
		}

		// create paths to those locations
		for (Point2D point2d : destinations) {
			List<Location> shortestPath = graph.getShortestPathLocations(
					location, graph.getLocation(point2d), false);

			add(new MultiStepMove(location, Move.convertToMoves(shortestPath)));
		}
	}
}
