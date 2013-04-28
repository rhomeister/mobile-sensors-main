package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorLocationHistory;

public class Evader {

	private Log log = LogFactory.getLog(Evader.class);

	private Location position;
	private EvaderMovementModel movementModel;
	private SensorLocationHistory locationHistory;
	private double time = 0.0;

	public Evader(EvaderMovementModel movementModel) {
		this.movementModel = movementModel;
	}

	public Location getPosition() {
		return position;
	}

	public EvaderMovementModel getMovementModel() {
		return movementModel;
	}

	public void move() {
		if (locationHistory == null)
			locationHistory = new SensorLocationHistory(new SensorID(9999),
					position);

		List<Move> moveOptions = position.getMoveOptions();

		Location newPosition = getMovementModel().selectMovement(moveOptions,
				getPosition());

		log.info("Evader moving from " + position + " to " + newPosition);

		locationHistory.addLocation(newPosition, time++);

		position = newPosition;
	}

	public void setLocation(Location location) {
		this.position = location;
	}

	public SensorLocationHistory getLocationHistory() {
		return locationHistory;
	}
}
