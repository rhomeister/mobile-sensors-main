package uk.ac.soton.ecs.mobilesensors.worldmodel;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public interface ObservationInformativenessFunction {

	Grid getGrid();

	/**
	 * Get the values of the spatial field for the current situation/timestep
	 * 
	 * @return
	 */
	Map<Point2D, Double> getValues();

	void setGrid(Grid grid);

	Point2D getMaximumInformativeLocation();

	boolean hasEventOccurred();

	double getObservationRange();

	void progressTime(int time);

	ObservationInformativenessFunction copy();

	void clearHistory();

	Collection<Observation> observe(Location location);

	double getInformativeness(Location location);

	Collection<Observation> observe(Collection<Location> locations);

	void initialise();

	// get informativeness only on specified locations
	double getInformativeness(Location location, Set<Location> locations);

	int getTau();

}
