package uk.ac.soton.ecs.mobilesensors.worldmodel.disk;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map.Entry;

import javolution.util.FastMap;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.worldmodel.pursuitevader.AbstractValueMap;

public class UncertaintyMap extends AbstractValueMap {

	public static final Double MAX_UNCERTAINTY = 1.0;
	private final double uncertaintyIncrement;

	public UncertaintyMap(Grid grid, double uncertaintyIncrement) {
		super(grid, MAX_UNCERTAINTY);

		this.uncertaintyIncrement = uncertaintyIncrement;
	}

	public void increaseUncertainty(double time) {
		Validate.isTrue(time >= 0.0, "received time " + time);
		if (time == 0)
			return;

		for (Entry<Point2D, Double> entry : uncertaintyMap.entrySet()) {
			double uncertainty = entry.getValue();
			uncertainty = Math.min(uncertainty + uncertaintyIncrement * time,
					MAX_UNCERTAINTY);
			entry.setValue(uncertainty);
		}
	}

	public void observe(Collection<Point2D> gridPointsInRange) {
		// once observed uncertainty drops to 0
		for (Point2D point : gridPointsInRange) {
			uncertaintyMap.put(point, 0.0);
		}
	}

	public UncertaintyMap copy() {
		UncertaintyMap copy = new UncertaintyMap(getGrid(),
				uncertaintyIncrement);
		copy.uncertaintyMap = new FastMap<Point2D, Double>(uncertaintyMap);
		return copy;
	}

	public double getUncertaintyReduction(Collection<Point2D> gridPointsInRange) {
		double reduction = 0.0;
		for (Point2D point : gridPointsInRange) {
			reduction += uncertaintyMap.get(point);
		}
		return reduction;
	}

	public double getUncertaintyIncrement() {
		return uncertaintyIncrement;
	}

	public void reset() {
		reset(MAX_UNCERTAINTY);
	}
}
