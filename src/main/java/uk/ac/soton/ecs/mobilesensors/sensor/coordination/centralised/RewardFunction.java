package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

public interface RewardFunction<S extends State> {

	public abstract double getReward(S state, MultiSensorAction actions);

}