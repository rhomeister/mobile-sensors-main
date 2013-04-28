package uk.ac.soton.ecs.mobilesensors.layout;

public interface AccessibilityRelation {

	Location getLocation1();

	Location getLocation2();

	Location getOther(Location location);

	double getLength();

}