package uk.ac.soton.ecs.mobilesensors.comparison;

import java.util.Collection;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public interface SensorPlacementAlgorithm {

	void setGrid(Grid grid);

	void setAccessibleLocationGraph(AccessibilityGraphImpl graph);

	void setSensorCount(int sensorCount);

	Grid getGrid();

	// void setCovarianceFunction(CovarianceFunction covarianceFunction);

	Collection<Location> getPlacement();

	void setInformativenessFunction(
			ObservationInformativenessFunction informativenessFunction);

}