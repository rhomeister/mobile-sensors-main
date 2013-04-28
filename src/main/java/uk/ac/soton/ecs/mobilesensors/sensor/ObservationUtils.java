package uk.ac.soton.ecs.mobilesensors.sensor;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;
import uk.ac.soton.ecs.utils.CollectionUtils;
import Jama.Matrix;

public class ObservationUtils {

	// public static DoubleMatrix getXMatrix(
	// Collection<Observation> lastObservations) {
	// if (lastObservations.isEmpty())
	// return new DoubleMatrix(0, 3);
	//
	// double[][] matrix = new double[lastObservations.size()][3];
	//
	// int i = 0;
	// for (Observation observation : lastObservations) {
	// Location location = observation.getLocation();
	// matrix[i][0] = location.getX();
	// matrix[i][1] = location.getY();
	// matrix[i][2] = observation.getTimestamp();
	// i++;
	// }
	//
	// return new DoubleMatrix(matrix);
	// }

	// public static DoubleMatrix getYMatrix(
	// Collection<Observation> lastObservations) {
	// if (lastObservations.isEmpty())
	// return new DoubleMatrix(0, 1);
	//
	// double[][] matrix = new double[lastObservations.size()][1];
	//
	// int i = 0;
	// for (Observation observation : lastObservations) {
	// matrix[i][0] = observation.getValue();
	// i++;
	// }
	//
	// return new DoubleMatrix(matrix);
	// }

	public static Matrix toMatrix(Point2D coordinates, double time) {
		return new Matrix(new double[][] { { coordinates.getX(),
				coordinates.getY(), time } });
	}

	public static Matrix toMatrixJAMA(Point2D coordinates, double time) {
		return new Matrix(new double[][] { { coordinates.getX(),
				coordinates.getY(), time } });
	}

	public static Matrix toMatrix(Collection<Point2D> coordinates, double time) {
		if (coordinates.isEmpty())
			return new Matrix(0, 3);

		double[][] matrix = new double[coordinates.size()][3];

		int i = 0;
		for (Point2D point : coordinates) {
			matrix[i][0] = point.getX();
			matrix[i][1] = point.getY();
			matrix[i][2] = time;
			i++;
		}

		return new Matrix(matrix);
	}

	public static Matrix toMatrixJAMA(Collection<Point2D> coordinates,
			double time) {
		if (coordinates.isEmpty())
			return new Matrix(0, 3);

		double[][] matrix = new double[coordinates.size()][3];

		int i = 0;
		for (Point2D point : coordinates) {
			matrix[i][0] = point.getX();
			matrix[i][1] = point.getY();
			matrix[i][2] = time;
			i++;
		}

		return new Matrix(matrix);
	}

	public static <T extends Location> Matrix toCoordinateMatrix(
			Collection<T> locations, double time) {
		return toMatrix(toCoordinates(locations), time);
	}

	public static Matrix toMatrix(Location location, double time) {
		return toMatrix(location.getCoordinates(), time);
	}

	public static Matrix toMatrixJAMA(Location location, double time) {
		return toMatrixJAMA(location.getCoordinates(), time);
	}

	// public static Matrix toMatrixJAMA(Location location, double time) {
	// return toMatrix(location.getCoordinates(), time);
	// }

	public static <T extends Location> Collection<Point2D> toCoordinates(
			Collection<T> locations) {
		Collection<Point2D> coordinates = new ArrayList<Point2D>();

		for (Location location : locations) {
			coordinates.add(location.getCoordinates());
		}

		return coordinates;
	}

	public static Matrix getXMatrix1(
			Collection<ObservationCoordinates> observationCoordinates) {
		if (observationCoordinates.isEmpty())
			return new Matrix(0, 3);

		double[][] matrix = new double[observationCoordinates.size()][3];

		int i = 0;
		for (ObservationCoordinates observation : observationCoordinates) {
			Location location = observation.getLocation();
			matrix[i][0] = location.getX();
			matrix[i][1] = location.getY();
			matrix[i][2] = observation.getTime();
			i++;
		}

		return new Matrix(matrix);
	}

	public static Map<Double, Collection<ObservationCoordinates>> sortByTimestep(
			Collection<ObservationCoordinates> coordinates) {
		return CollectionUtils.sort(coordinates,
				new Transformer<ObservationCoordinates, Double>() {

					public Double transform(ObservationCoordinates input) {
						return input.getTime();
					}
				});
	}

	public static List<ObservationCoordinates> getObservationsAlongPath(
			List<Move> path, double currentTimestep) {
		List<ObservationCoordinates> result = new ArrayList<ObservationCoordinates>();

		for (int i = 0; i < path.size(); i++) {
			Move move = path.get(i);

			result.add(new ObservationCoordinates(move.getX(), move.getY(),
					++currentTimestep));
		}

		return result;

	}

	public static Matrix toCoordinateMatrixJAMA(Collection<Location> locations,
			double timestep) {
		return toMatrixJAMA(toCoordinates(locations), timestep);
	}

	public static Matrix getXMatrix1JAMA(
			Collection<ObservationCoordinates> observationCoordinates) {
		if (observationCoordinates.isEmpty())
			return new Matrix(0, 3);

		double[][] matrix = new double[observationCoordinates.size()][3];

		int i = 0;
		for (ObservationCoordinates observation : observationCoordinates) {
			Location location = observation.getLocation();
			matrix[i][0] = location.getX();
			matrix[i][1] = location.getY();
			matrix[i][2] = observation.getTime();
			i++;
		}

		return new Matrix(matrix);
	}
}
