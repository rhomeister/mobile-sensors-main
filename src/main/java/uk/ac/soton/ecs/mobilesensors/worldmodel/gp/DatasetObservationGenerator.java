package uk.ac.soton.ecs.mobilesensors.worldmodel.gp;


public class DatasetObservationGenerator {
	// extends
	// KnownHyperparametersObservationGenerator implements InitializingBean {
	//
	// private static Log log = LogFactory
	// .getLog(DatasetObservationGenerator.class);
	//
	// private String filename;
	//
	// private final Map<Point2D, Integer> sensorLocations = new
	// HashMap<Point2D, Integer>();
	// private final Map<Integer, TreeSet<Double>> timestamps = new
	// HashMap<Integer, TreeSet<Double>>();
	// private final Map<Integer, HashMap<Double, Double>> measurements = new
	// HashMap<Integer, HashMap<Double, Double>>();
	// private double lengthScale;
	// private double timeScale;
	// private double noise;
	// private double signalVariance;
	//
	// public void setLengthScale(double lengthScale) {
	// this.lengthScale = lengthScale;
	// }
	//
	// public void setTimeScale(double timeScale) {
	// this.timeScale = timeScale;
	// }
	//
	// public void setNoise(double noise) {
	// this.noise = noise;
	// }
	//
	// public void setSignalVariance(double signalVariance) {
	// this.signalVariance = signalVariance;
	// }
	//
	// public void setFilename(String filename) {
	// this.filename = filename;
	// }
	//
	// @Override
	// protected double generateObservation(double x, double y, double time) {
	// Point2D coordinates = new Point2D.Double(x, y);
	// Integer sensorID = sensorLocations.get(coordinates);
	// if (sensorID == null) {
	// log.warn("No sensordata for requested location (" + x + ", " + y
	// + ")");
	//
	// double minDistance = Double.POSITIVE_INFINITY;
	// Point2D bestCoord = null;
	//
	// for (Point2D coords : sensorLocations.keySet()) {
	// if (coordinates.distance(coords) < minDistance) {
	// minDistance = coordinates.distance(coords);
	// bestCoord = coords;
	// }
	// }
	//
	// sensorID = sensorLocations.get(bestCoord);
	// }
	//
	// Double timestamp = timestamps.get(sensorID).lower(time);
	// if (timestamp == null)
	// timestamp = timestamps.get(sensorID).higher(time);
	//
	// return measurements.get(sensorID).get(timestamp);
	// }
	//
	// @Override
	// public void afterPropertiesSet() throws Exception {
	// Validate.isTrue(lengthScale > 0);
	// Validate.isTrue(timeScale > 0);
	// Validate.isTrue(noise > 0);
	// Validate.isTrue(signalVariance > 0);
	//
	// setHyperParameters(new double[] { lengthScale, timeScale,
	// signalVariance, noise });
	//
	// BufferedReader reader = new BufferedReader(new FileReader(new File(
	// filename)));
	//
	// String line;
	//
	// do {
	// line = reader.readLine();
	// } while (line.startsWith("%"));
	//
	// while (line != null) {
	// line = line.trim();
	// String[] split = line.split(" +");
	//
	// double x = Double.parseDouble(split[0]);
	// double y = Double.parseDouble(split[1]);
	// Point2D coordinates = new Point2D.Double(x, y);
	// double time = Double.parseDouble(split[2]);
	// double temperature = Double.parseDouble(split[3]);
	// int sensorID = (int) Double.parseDouble(split[4]);
	//
	// if (!sensorLocations.containsKey(coordinates)) {
	// sensorLocations.put(coordinates, sensorID);
	// timestamps.put(sensorID, new TreeSet<Double>());
	// measurements.put(sensorID, new HashMap<Double, Double>());
	// }
	//
	// timestamps.get(sensorID).add(time);
	// measurements.get(sensorID).put(time, temperature);
	//
	// line = reader.readLine();
	// }
	//
	// super.afterPropertiesSet();
	// }
	//
	// public static void main(String[] args) throws Exception {
	// DatasetObservationGenerator generator = new
	// DatasetObservationGenerator();
	// generator.setFilename("/mnt/data/berkeley-dataset/datasets/"
	// + "berkeley-temperature-2004-03-01-rescaled.txt");
	//
	// generator.setLengthScale(1.0);
	// generator.setNoise(1.0);
	// generator.setSignalVariance(1.0);
	// generator.setHyperParameters(new double[] {});
	// generator.setTimeScale(1.0);
	//
	// generator.afterPropertiesSet();
	// generator.setPhenomenon(new SpatialPhenomenon("temperature"));
	//
	// System.out.println(generator.sensorLocations);
	//
	// System.out.println(generator.generateObservation(21.5, 23, 0));
	//
	// }
	//	
	// public EnvironmentalParameters getEnvironmentalParameters() {
	// throw new NotImplementedException();
	// }
}
