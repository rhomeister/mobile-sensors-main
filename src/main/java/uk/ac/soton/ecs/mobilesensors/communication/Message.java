package uk.ac.soton.ecs.mobilesensors.communication;

import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;

public class Message<T extends Object> {
	private final SensorID recipient;

	private final SensorID sender;

	private final T content;

	protected Message(SensorID recipient, SensorID sender, T content) {
		super();
		this.recipient = recipient;
		this.sender = sender;
		this.content = content;
	}

	public SensorID getRecipient() {
		return recipient;
	}

	public SensorID getSender() {
		return sender;
	}

	public T getContent() {
		return content;
	}

	public Message<T> createReply(T content) {
		return new Message<T>(sender, recipient, content);
	}

	public static <T extends Object> Message<T> createMessage(Sensor sender,
			SensorID recipient, T content) {
		return new Message<T>(recipient, sender.getID(), content);
	}
}
