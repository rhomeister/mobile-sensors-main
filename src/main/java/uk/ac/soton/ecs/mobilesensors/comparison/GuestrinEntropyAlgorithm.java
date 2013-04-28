package uk.ac.soton.ecs.mobilesensors.comparison;

public class GuestrinEntropyAlgorithm {
	//
	// extends AbstractSensorPlacementAlgorithm {
	//
	// public void calculate() {
	// regression = createRegression();
	//
	// GaussianProcess predictor = regression.calculateRegression(
	// new DoubleMatrix(0, 3), new DoubleMatrix(0, 1));
	//
	// for (int i = 0; i < sensorCount; i++) {
	// GaussianProcessPrediction prediction = predictor
	// .calculatePrediction(ObservationUtils.toCoordinateMatrix(
	// graph.getLocations(), 0.0));
	//
	// DoubleMatrix variance = prediction.getVariance();
	//
	// double maxVariance = Double.NEGATIVE_INFINITY;
	// Location bestLocation = null;
	//
	// for (int j = 0; j < variance.getRows(); j++) {
	// if (variance.get(j, 0) > maxVariance) {
	// maxVariance = variance.get(j, 0);
	// double x = prediction.getTestX().get(j, 0);
	// double y = prediction.getTestX().get(j, 1);
	// bestLocation = new LocationImpl(x, y);
	// }
	// }
	//
	// predictor = regression.updateRegression(ObservationUtils.toMatrix(
	// bestLocation, 0.0), new DoubleMatrix(1, 1));
	//
	// addBestLocation(bestLocation);
	// }
	// }
}
