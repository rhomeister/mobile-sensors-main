package uk.ac.soton.ecs.mobilesensors.worldmodel.disk;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.Observation;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;
import uk.ac.soton.ecs.mobilesensors.worldmodel.pursuitevader.AbstractObservationInformativenessFunction;

/**
 * Every sensor has a disk with certain radius. Every grid cell has a maximum
 * uncertainty level, which drops to 0 if it is covered by a sensor. Uncertainty
 * grows with a certain growth rate until it reaches maximum uncertainty
 * Informativeness is defined as the uncertainty reduction achieved by a sensor
 * 
 * @author rs06r
 * 
 */
public class DiskInformativenessFunction extends
		AbstractObservationInformativenessFunction {

	private double radius;

	private UncertaintyMap uncertaintyMap;

	public double uncertaintyIncrement;

	private int currentTime;

	public DiskInformativenessFunction(Grid grid, double radius,
			double uncertaintyIncrement) {
		this.radius = radius;
		this.uncertaintyIncrement = uncertaintyIncrement;
		setGrid(grid);
		initialise();
	}

	public DiskInformativenessFunction() {
	}

	/**
	 * Copy constructor
	 * 
	 * @param grid
	 * @param radius2
	 * @param uncertaintyIncrement2
	 * @param uncertaintyMap2
	 */
	private DiskInformativenessFunction(Grid grid, double radius2,
			double uncertaintyIncrement2, UncertaintyMap uncertaintyMap2) {
		setGrid(grid);
		this.radius = radius2;
		this.uncertaintyIncrement = uncertaintyIncrement2;
		this.uncertaintyMap = uncertaintyMap2;
	}

	@Required
	public void setUncertaintyIncrement(double uncertaintyIncrement) {
		this.uncertaintyIncrement = uncertaintyIncrement;
	}

	public Map<Point2D, Double> getValues() {
		return uncertaintyMap.getValues();
	}

	@Required
	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getInformativeness(Location location) {
		Collection<Point2D> gridPointsInRange = getGrid().getGridPoints(
				location, radius);

		return uncertaintyMap.getUncertaintyReduction(gridPointsInRange);
	}

	public ObservationInformativenessFunction copy() {
		return new DiskInformativenessFunction(getGrid(), radius,
				uncertaintyIncrement, uncertaintyMap.copy());
	}

	public void clearHistory() {
		initialise();
	}

	public Collection<Observation> observe(Location location) {
		Collection<Point2D> gridPointsInRange = getGrid().getGridPoints(
				location, radius);

		uncertaintyMap.observe(gridPointsInRange);

		return new ArrayList<Observation>();
	}

	public void initialise() {
		uncertaintyMap = new UncertaintyMap(getGrid(), uncertaintyIncrement);
	}

	public boolean hasEventOccurred() {
		return false;
	}

	public double getObservationRange() {
		return radius;
	}

	public void progressTime(int time) {
		currentTime += time;
		uncertaintyMap.increaseUncertainty(time);
	}

	public double getInformativeness(Location location, Set<Location> locations) {
		Collection<Point2D> gridPointsInRange = new ArrayList<Point2D>();

		for (Location gridLocation : locations) {
			if (gridLocation.directDistanceSq(location) < radius * radius) {
				gridPointsInRange.add(gridLocation.getCoordinates());
			}
		}

		return uncertaintyMap.getUncertaintyReduction(gridPointsInRange);
	}

	public int getTau() {
		return (int) (1.0 / uncertaintyIncrement);
	}
}
