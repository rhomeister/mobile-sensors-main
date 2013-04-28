package uk.ac.soton.ecs.mobilesensors;

public class Timer {

	private double currentTime = 0.0;

	private double tickDuration = 1.0;

	public double getTime() {
		return currentTime;
	}

	public void nextTick() {
		currentTime += tickDuration;
	}

	public double getTickDuration() {
		return tickDuration;
	}

	public double getTimeAtRound(int round) {
		return round * tickDuration;
	}
}
