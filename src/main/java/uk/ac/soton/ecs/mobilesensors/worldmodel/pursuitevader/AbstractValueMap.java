package uk.ac.soton.ecs.mobilesensors.worldmodel.pursuitevader;

import java.awt.geom.Point2D;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.Grid;

public abstract class AbstractValueMap {
	protected FastMap<Point2D, Double> uncertaintyMap;
	private double value;

	private final Grid grid;

	public AbstractValueMap(Grid grid, double value) {
		setValue(value);
		this.grid = grid;
		initialise();
	}

	public AbstractValueMap(Grid grid, FastMap<Point2D, Double> map) {
		this.grid = grid;
		this.uncertaintyMap = map;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public Grid getGrid() {
		return grid;
	}

	protected void initialise() {
		uncertaintyMap = new FastMap<Point2D, Double>(getGrid()
				.getGridPointCount() * 4 / 3);
		Validate.isTrue(!getGrid().getGridPoints().isEmpty());
		reset(value);
	}

	protected void reset(double value) {
		for (Point2D point : getGrid().getGridPoints()) {
			uncertaintyMap.put(point, value);
		}
	}

	public final Map<Point2D, Double> getValues() {
		return uncertaintyMap;
	}

	protected void addToUncertaintyMap(double constant) {
		for (Point2D point : uncertaintyMap.keySet()) {
			Double double1 = uncertaintyMap.get(point);
			uncertaintyMap.put(point, double1 + constant);
		}
	}
}
