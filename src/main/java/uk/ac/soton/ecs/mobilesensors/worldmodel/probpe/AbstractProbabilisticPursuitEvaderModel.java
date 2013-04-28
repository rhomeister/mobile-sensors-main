package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.awt.geom.Point2D;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.InitialPlacement;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.SimulationEventListener;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.metric.LogWriter;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;
import uk.ac.soton.ecs.mobilesensors.worldmodel.pursuitevader.AbstractObservationInformativenessFunction;

public abstract class AbstractProbabilisticPursuitEvaderModel extends
		AbstractObservationInformativenessFunction implements ObservationInformativenessFunction,
		SimulationEventListener, LogWriter {
	protected double captureRange;

	protected Simulation simulation;

	protected InitialPlacement initialEvaderPlacement;

	protected AccessibilityGraphImpl graph;

	protected SensingModel sensingModel;

	protected double currentTime;

	public AbstractProbabilisticPursuitEvaderModel(SensingModel model) {
		this.sensingModel = model;
	}

	public AbstractProbabilisticPursuitEvaderModel() {
	}

	@Required
	public void setSensingModel(SensingModel sensingModel) {
		this.sensingModel = sensingModel;
	}

	@Required
	public void setEvaderInitialLocation(InitialPlacement placement) {
		initialEvaderPlacement = placement;
	}

	@Required
	public final void setCaptureRange(double captureRange) {
		this.captureRange = captureRange;
	}

	@Required
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
		this.simulation.addEventListener(this);
		this.simulation.addLogWriter(this);
	}

	protected void initialise(Grid grid, AccessibilityGraphImpl graph) {
		this.graph = graph;
	}

	public final double getObservationRange() {
		return sensingModel.getRange();
	}

	public abstract boolean isEvaderCapured();

	public final void handleEndOfSimulation(Simulation source) throws Exception {
	}

	public final void handleStartOfRound(Simulation source, int round,
			double timestep) {
		currentTime = timestep;
	}

	public final void handleStartOfSimulation(Simulation source, double time) {
		initialise(simulation.getEnvironment().getGrid(), simulation
				.getEnvironment().getAccessibilityGraph());
	}

	public final void handleEndOfRound(Simulation source, int round,
			double timestep) {
		moveEvader();
	}

	protected abstract void moveEvader();

	protected abstract ProbabilityMap getProbabilityMap();

	public final Map<Point2D, Double> getValues() {
		return getProbabilityMap().getValues();
	}

	public abstract Location getEvaderLocation();
}
