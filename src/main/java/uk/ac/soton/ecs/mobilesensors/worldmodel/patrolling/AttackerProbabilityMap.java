package uk.ac.soton.ecs.mobilesensors.worldmodel.patrolling;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.worldmodel.probpe.Observation;
import uk.ac.soton.ecs.mobilesensors.worldmodel.probpe.SensingModel;

public class AttackerProbabilityMap implements InitializingBean {

	private Grid grid;

	private Map<Point2D, CellModel> cells = new HashMap<Point2D, CellModel>();

	private double attackProbability;

	private int attackDuration;

	private SensingModel sensingModel;

	@Required
	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	@Required
	public void setSensingModel(SensingModel sensingModel) {
		this.sensingModel = sensingModel;
	}

	@Required
	public void setAttackDuration(int attackDuration) {
		this.attackDuration = attackDuration;
	}

	@Required
	public void setAttackProbability(double attackProbability) {
		this.attackProbability = attackProbability;
	}

	public void afterPropertiesSet() throws Exception {
		for (Point2D point2d : grid) {
			cells
					.put(point2d, new CellModel(attackDuration,
							attackProbability));
		}
	}

	public double[] getStateProbabilityVector(Point2D point) {
		return cells.get(point).getStateProbabilityVector();
	}

	public void update() {
		for (CellModel model : cells.values()) {
			model.update();
		}
	}

	public void observe(Collection<Observation> observations) {
		for (Observation observation : observations) {
			Point2D observationCoordinates = observation
					.getSensedCoordinates();

			CellModel cellModel = cells.get(observationCoordinates);

			if (observation.isEvaderDetected()) {
				cellModel.updateForPositiveObservation(sensingModel
						.getFalsePositiveProbability(observation));
			} else {
				cellModel.updateForNegativeObservation(sensingModel
						.getFalseNegativeProbability(observation));
			}
		}
	}

	public Grid getGrid() {
		return grid;
	}

	public CellModel getCell(Point2D point) {
		return cells.get(point);
	}

	public AttackerProbabilityMap copy() {
		AttackerProbabilityMap result = new AttackerProbabilityMap();
		result.attackDuration = attackDuration;
		result.attackProbability = attackProbability;
		result.sensingModel = sensingModel;
		result.grid = grid;

		for (Point2D point2d : grid) {
			CellModel cellModel = cells.get(point2d);
			result.cells.put(point2d, cellModel.copy());
		}

		return result;
	}
}
