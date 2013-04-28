package uk.ac.soton.ecs.mobilesensors;

import java.util.List;

public interface SimulationEvent {

	List<? extends SimulationEvent>  run();
	
}
