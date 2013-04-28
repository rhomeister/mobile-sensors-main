package uk.ac.soton.ecs.mobilesensors.communication;


public class InstanceOfFilter implements MessageFilter {

	private Class clazz;

	public InstanceOfFilter(Class clazz) {
		this.clazz = clazz;
	}

	public boolean accept(Message<?> message) {
		return clazz.isInstance(message);
	}

}
