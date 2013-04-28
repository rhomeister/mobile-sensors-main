package uk.ac.soton.ecs.mobilesensors.comparison;


public class GuestrinMutualInformationAlgorithm {
	// extends
	// AbstractSensorPlacementAlgorithm {

	// private static Log log = LogFactory
	// .getLog(GuestrinMutualInformationAlgorithm.class);
	//
	// public void calculate() {
	// // regression = createRegression();
	//
	// for (int i = 0; i < sensorCount; i++) {
	// log.info("Adding sensor " + (i + 1) + " of " + sensorCount);
	// addBestLocation(getBestLocation());
	// }
	// }
	//
	// public Location getBestLocation() {
	// // See algorithm 1 in Krause 2005
	//
	// double maxRatio = Double.NEGATIVE_INFINITY;
	// Location bestLocation = null;
	//
	// for (Location location : graph.getLocations()) {
	// double numerator = calculateNumerator(location);
	//
	// double denominator = calculateDenominator(location);
	//
	// double ratio = numerator / denominator;
	//
	// if (ratio > maxRatio) {
	// maxRatio = ratio;
	// bestLocation = location;
	// }
	// }
	//
	// return bestLocation;
	// }
	//
	// private double calculateDenominator(Location consideredSensorLocation) {
	// GaussianRegression<GaussianProcess> regression = createRegression();
	//
	// Collection<Point2D> aPrime = new ArrayList<Point2D>();
	// Collection<Point2D> neighbourCoordinates = ObservationUtils
	// .toCoordinates(locations);
	//
	// // aPrime = grid \ (neighbours U consideredLocation)
	// for (Point2D point : grid.getGridPoints()) {
	// if (!point.equals(consideredSensorLocation.getCoordinates())
	// && !neighbourCoordinates.contains(point)) {
	// aPrime.add(point);
	// }
	// }
	//
	// DoubleMatrix aPrimeMatrix = ObservationUtils.toMatrix(aPrime, 0.0);
	//
	// DoubleMatrix consideredCoordinateMatrix = ObservationUtils.toMatrix(
	// consideredSensorLocation, 0.0);
	//
	// GaussianProcessPrediction prediction = regression.calculateRegression(
	// aPrimeMatrix, new DoubleMatrix(aPrimeMatrix.getRows(), 1))
	// .calculatePrediction(consideredCoordinateMatrix);
	//
	// return prediction.getVariance().get(0, 0);
	// }
	//
	// private double calculateNumerator(Location consideredSensorLocation) {
	// DoubleMatrix neighbourCoordinateMatrix = ObservationUtils
	// .toCoordinateMatrix(locations, 0.0);
	// DoubleMatrix consideredCoordinateMatrix = ObservationUtils.toMatrix(
	// consideredSensorLocation, 0.0);
	//
	// GaussianRegression<GaussianProcess> regression = createRegression();
	// GaussianProcessPrediction prediction = regression.calculateRegression(
	// neighbourCoordinateMatrix,
	// new DoubleMatrix(locations.size(), 1)).calculatePrediction(
	// consideredCoordinateMatrix);
	//
	// return prediction.getVariance().get(0, 0);
	// }
}
