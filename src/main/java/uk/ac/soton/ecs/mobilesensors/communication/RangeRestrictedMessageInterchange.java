package uk.ac.soton.ecs.mobilesensors.communication;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.SimulationEventListener;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.metric.LogWriter;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import uk.ac.soton.ecs.utils.GraphUtils;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * This message interchange module allows for communication between sensors that
 * are within a certain range
 * 
 * @author rs06r
 * 
 */
public class RangeRestrictedMessageInterchange extends
		AbstractMessageInterchange implements InitializingBean,
		SimulationEventListener, LogWriter {

	private static final String SENSOR_KEY = "SENSOR";

	private static Log log = LogFactory
			.getLog(RangeRestrictedMessageInterchange.class);

	private UndirectedGraph<Sensor, Pair<Sensor>> sensorGraph;

	private List<Double> timeSteps = new ArrayList<Double>();

	private List<Integer> sensorPairsInRangeCount = new ArrayList<Integer>();

	private int successFullyDeliveredMessages = 0;

	private double communicationRange;

	private double communicationProbability = 1.0;

	private Random random = new Random();

	// private Map<Sensor, UndirectedSparseVertex> sensorToVertexMap = new
	// HashMap<Sensor, UndirectedSparseVertex>();

	// cached results for the reachableSensors function
	private Map<Sensor, Map<SensorID, Collection<SensorID>>> cachedReachableSensors;

	private File outputDirectory;

	private int unconnectedRounds;

	public void setCommunicationRange(double communicationRange) {
		this.communicationRange = communicationRange;
	}

	public double getCommunicationRange() {
		return communicationRange;
	}

	public void processMessage(Message<?> message) {
		Sensor recipient = simulation.getSensorByID(message.getRecipient());
		Sensor sender = simulation.getSensorByID(message.getSender());

		if (inRange(sender, recipient)
				&& random.nextDouble() < communicationProbability) {
			endpoints.get(message.getRecipient()).deliver(message);
			successFullyDeliveredMessages++;

			log.debug("Forwarding message");
		} else {
			log.info("Dropping message: out of range / communication failure ("
					+ sender.getID() + " -> " + recipient.getID() + ")");
		}
	}

	public void afterPropertiesSet() throws Exception {
		Validate.isTrue(communicationRange >= 0);
		Validate.isTrue(communicationProbability >= 0
				&& communicationProbability <= 1.0);
		Validate.notNull(simulation,
				"Simulation is null, add reference to simulation");

		simulation.addEventListener(this);
		simulation.addLogWriter(this);
	}

	public void setCommunicationProbability(
			double successfulCommunicationProbability) {
		this.communicationProbability = successfulCommunicationProbability;
	}

	public double getCommunicationProbability() {
		return communicationProbability;
	}

	public void handleEndOfRound(int round, double timestep) {
		handleEndOfRound(simulation, round, timestep);
	}

	public void handleEndOfRound(Simulation source, int round, double timestep) {

		Collection<Sensor> sensorLocations = this.simulation.getSensors();
		int inRange = 0;

		for (Sensor sensor1 : sensorLocations) {
			for (Sensor sensor2 : sensorLocations) {
				if (!sensor1.equals(sensor2) && inRange(sensor1, sensor2))
					inRange++;

			}
		}

		// compensate for double counting
		inRange /= 2;

		Validate.isTrue(inRange >= 0);

		timeSteps.add(timestep);
		sensorPairsInRangeCount.add(inRange);
	}

	public Map<SensorID, Location> getNeighbourLocations(Sensor sensor) {
		Map<SensorID, Location> neigbours = simulation
				.getNeighbourLocations(sensor);
		Map<SensorID, Location> neigboursInRange = new HashMap<SensorID, Location>();

		for (SensorID neighbourID : neigbours.keySet()) {
			Sensor sensorByID = simulation.getSensorByID(neighbourID);
			if (inRange(sensor, simulation.getSensorByID(neighbourID))) {
				neigboursInRange.put(neighbourID, neigbours.get(neighbourID));
			}
		}

		return neigboursInRange;
	}

	public boolean inRange(Sensor sensor1, Sensor sensor2) {
		double directDistance = sensor1.getLocation().directDistance(
				sensor2.getLocation());
		return directDistance <= communicationRange;
	}

	public List<SensorID> getNeighbours(Sensor sensor) {
		return new ArrayList<SensorID>(getNeighbourLocations(sensor).keySet());
	}

	/**
	 * Returns the set of reachable sensors through neighbourSensorID. The
	 * sensor 'sensor' itself is not part of the set
	 */
	public Collection<SensorID> getReachableSensors(Sensor sensor,
			SensorID neighbourSensorID) {
		Sensor neighbour = simulation.getSensorByID(neighbourSensorID);

		Collection<SensorID> cachedResult = cachedReachableSensors.get(sensor)
				.get(neighbourSensorID);

		if (cachedResult != null) {
			return cachedResult;
		}

		// temporarily remove edge
		Pair<Sensor> edge = sensorGraph.findEdge(sensor, neighbour);
		sensorGraph.removeEdge(edge);

		BFSDistanceLabeler<Sensor, Pair<Sensor>> labeler = new BFSDistanceLabeler<Sensor, Pair<Sensor>>();
		labeler.labelDistances(sensorGraph, neighbour);

		Set<SensorID> sensorIDs = simulation.getSensorIDs();

		for (Sensor unreachableSensor : labeler.getUnvisitedVertices()) {
			sensorIDs.remove(unreachableSensor.getID());
		}

		sensorIDs.remove(sensor.getID());

		sensorGraph.addEdge(edge, sensor, neighbour);

		cachedReachableSensors.get(sensor).put(neighbourSensorID, sensorIDs);

		return sensorIDs;
	}

	public void handleStartOfRound(Simulation source, int round, double timestep) {
		buildSensorGraph();
		cachedReachableSensors = new HashMap<Sensor, Map<SensorID, Collection<SensorID>>>();

		for (Sensor sensor : simulation.getSensors()) {
			cachedReachableSensors.put(sensor,
					new HashMap<SensorID, Collection<SensorID>>());
		}
	}

	private void buildSensorGraph() {
		sensorGraph = new UndirectedSparseGraph<Sensor, Pair<Sensor>>();

		// create vertices
		for (Sensor sensor : simulation.getSensors()) {
			sensorGraph.addVertex(sensor);
		}

		for (Sensor sensor1 : simulation.getSensors()) {
			for (Sensor sensor2 : simulation.getSensors()) {
				if (!sensor1.equals(sensor2) && inRange(sensor1, sensor2)) {
					sensorGraph.addEdge(new Pair<Sensor>(sensor1, sensor2),
							sensor1, sensor2);
				}
			}
		}

		if (!GraphUtils.isConnected(sensorGraph)) {
			log.warn("Sensorgraph is not connected");
			unconnectedRounds++;
		}
	}

	public void handleEndOfSimulation(Simulation simulation) throws Exception {
		// TODO Auto-generated method stub

	}

	public void finaliseLogs() throws Exception {
		FileUtils.writeStringToFile(new File(outputDirectory,
				"message_count.txt"), successFullyDeliveredMessages + "\n");

		FileUtils.writeStringToFile(new File(outputDirectory,
				"communicationRange.txt"), communicationRange + "\n");

		FileUtils.writeStringToFile(new File(outputDirectory,
				"unconnectedRounds.txt"), unconnectedRounds + "\n");

		// write file with information about how many sensor pairs are in
		// communication range
		File outputFile = new File(outputDirectory, "sensor_pairs_in_range.txt");

		StringBuffer buffer = new StringBuffer();
		buffer.append("% timestamp sensor_pairs_in_communication_range\n");

		for (int i = 0; i < timeSteps.size(); i++) {
			buffer.append(String.format(Locale.US, "%15.4f %15d\n", timeSteps
					.get(i), sensorPairsInRangeCount.get(i)));
		}

		FileUtils.writeStringToFile(outputFile, buffer.toString());
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void handleStartOfSimulation(Simulation simulation, double time) {
		// TODO Auto-generated method stub

	}

}
