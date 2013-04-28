package uk.ac.soton.ecs.mobilesensors.layout;

import java.awt.geom.Point2D;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.utils.ArrayUtils;

public class RectangularGrid extends GeneralGrid {
	private double lengthX;
	private double lengthY;
	private double originX;
	private double originY;
	private int precisionX;
	private int precisionY;

	public RectangularGrid(double lengthX, double lengthY, int precisionX,
			int precisionY, double originX, double originY) {
		this.lengthX = lengthX;
		this.lengthY = lengthY;
		this.precisionX = precisionX;
		this.precisionY = precisionY;
		this.originX = originX;
		this.originY = originY;

		initialize();
	}

	public RectangularGrid(double lengthX, double lengthY, int precision) {
		this(lengthX, lengthY, precision, 0, 0);
	}

	public RectangularGrid() {

	}

	public RectangularGrid(double lengthX, double lengthY, int precision,
			double originX, double originY) {
		this(lengthX, lengthY, precision, precision, originX, originY);
	}

	@Override
	public void initialize() {
		Validate.isTrue(precisionX > 0, "Precision should be greater than 0");
		Validate.isTrue(precisionY > 0, "Precision should be greater than 0");

		double[] rangeX = ArrayUtils.linspace(originX, originY + lengthX,
				precisionX);
		double[] rangeY = ArrayUtils.linspace(originX, originY + lengthY,
				precisionY);

		for (double x : rangeX) {
			for (double y : rangeY) {
				gridPoints.add(new Point2D.Double(x, y));
			}
		}

		super.initialize();
	}

	public void setLengthX(double lengthX) {
		this.lengthX = lengthX;
	}

	public void setLengthY(double lengthY) {
		this.lengthY = lengthY;
	}

	public void setOriginX(double originX) {
		this.originX = originX;
	}

	public void setOriginY(double originY) {
		this.originY = originY;
	}

	public void setPrecision(int precision) {
		setPrecisionX(precision);
		setPrecisionY(precision);
	}

	public void setPrecisionX(int precision) {
		this.precisionX = precision;
	}

	public void setPrecisionY(int precision) {
		this.precisionY = precision;
	}

	public static void main(String[] args) {
		for (int i = 0; i <= 670; i += 10) {
			for (int j = 0; j <= 620; j += 10) {
				System.out.println(i + " " + j);
			}
		}
	}
}