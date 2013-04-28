package uk.ac.soton.ecs.mobilesensors.worldmodel.patrolling;

import java.awt.geom.Point2D;
import java.util.Map;

public interface LossFunction {

	Map<Point2D, Double> getLossMap(AttackerProbabilityMap map);
}
