package uk.ac.soton.ecs.mobilesensors;

import uk.ac.soton.ecs.mobilesensors.layout.Location;

public interface EnvironmentalParameters {

	double getLengthScale(Location location, double time);

	double getTimeScale(Location location, double time);


}


