package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class MultiStepMove implements MobileSensorMove {

	private Location destination;

	private Location source;

	private List<Move> path;

	public MultiStepMove(Location source, List<Move> path) {
		this.path = path;
		this.source = source;

		if (path.isEmpty()) {
			this.path = new ArrayList<Move>();
			this.path.add(new Move(source));
			this.destination = source;
		} else {
			this.destination = path.get(path.size() - 1).getTargetLocation();

			Validate.isTrue(source.getNeighbours().contains(
					path.get(0).getTargetLocation()));

			for (int i = 1; i < path.size(); i++) {
				Validate.isTrue(path.get(i - 1).getTargetLocation()
						.getNeighbours().contains(
								path.get(i).getTargetLocation()));
			}
		}
	}

	public Location getDestination() {
		return destination;
	}

	public Location getSource() {
		return source;
	}

	public List<Move> getPath() {
		return path;
	}

	public int getLength() {
		return getPath().size();
	}

	public Location getLocation(int i) {
		return getPath().get(i).getTargetLocation();
	}

	@Override
	public String toString() {
		return getPath().toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MultiStepMove) {
			MultiStepMove state = (MultiStepMove) obj;
			return state.getPath().equals(getPath());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	public Collection<Location> getLocations() {
		List<Location> result = new ArrayList<Location>();
		for (Move move : path) {
			result.add(move.getTargetLocation());
		}

		return result;
	}
}
