package uk.ac.soton.ecs.mobilesensors.communication;

public interface MessageFilter {

	boolean accept(Message<?> message);

}
