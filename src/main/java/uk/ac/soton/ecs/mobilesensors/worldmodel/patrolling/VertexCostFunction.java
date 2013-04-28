package uk.ac.soton.ecs.mobilesensors.worldmodel.patrolling;

import java.awt.geom.Point2D;

public interface VertexCostFunction {

	double getCost(Point2D location);

}
