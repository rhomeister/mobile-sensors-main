package uk.ac.soton.ecs.mobilesensors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;

import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class Move {

	private final Location targetLocation;

	private Integer hashCode;

	public Move(Location targetLocation) {
		this.targetLocation = targetLocation;
	}

	public Location getTargetLocation() {
		return targetLocation;
	}

	public double getX() {
		return targetLocation.getX();
	}

	public double getY() {
		return targetLocation.getY();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Move) {
			Move move = (Move) obj;
			return getX() == move.getX() && getY() == move.getY();
		}

		return false;
	}

	@Override
	public int hashCode() {
		if (hashCode == null) {
			hashCode = new HashCodeBuilder().append(getX()).append(getY())
					.toHashCode();
		}

		return hashCode;
	}

	@Override
	public String toString() {
		return targetLocation.toString();
	}

	public static List<Move> convertToMoves(List<Location> shortestPath) {
		List<Move> result = new ArrayList<Move>();

		for (Location location : shortestPath) {
			result.add(new Move(location));
		}

		return result;
	}
}
