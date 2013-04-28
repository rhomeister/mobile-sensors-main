package uk.ac.soton.ecs.mobilesensors.worldmodel;

import java.awt.geom.Point2D;
import java.util.Collection;

import uk.ac.soton.ecs.mobilesensors.layout.Location;

public interface SimpleObservationInformativenessFunction {

	Collection<? extends Observation> observe(Collection<Location> coordinates);

	Collection<? extends Observation> observe(Location location);

	double getInformativeness(Location location);

	Point2D getMaximumInformativeLocation();

	boolean hasEventOccurred();

	double getObservationRange();

	SimpleObservationInformativenessFunction copy();

	void clearHistory();

	void progressTime(int time);

}
