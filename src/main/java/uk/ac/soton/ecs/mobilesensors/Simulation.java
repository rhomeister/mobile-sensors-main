package uk.ac.soton.ecs.mobilesensors;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.communication.CommunicationModule;
import uk.ac.soton.ecs.mobilesensors.communication.MessageInterchange;
import uk.ac.soton.ecs.mobilesensors.communication.UlimitedRangeMessageInterchange;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.LocationImpl;
import uk.ac.soton.ecs.mobilesensors.metric.BBMaxSumStatistics;
import uk.ac.soton.ecs.mobilesensors.metric.GlobalMetric;
import uk.ac.soton.ecs.mobilesensors.metric.LogWriter;
import uk.ac.soton.ecs.mobilesensors.metric.SpatialFieldValueMetric;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;

public class Simulation {

	protected final Collection<Sensor> sensors = new ArrayList<Sensor>();

	protected Log log = LogFactory.getLog(Simulation.class);

	private Environment environment;

	private InitialPlacement initialPlacement;

	private MessageInterchange messageInterchange = new UlimitedRangeMessageInterchange();

	private final Timer timer = new Timer();

	private String simpleName;

	private String description;

	private int lastID = 0;

	private Collection<GlobalMetric> globalMetrics = new ArrayList<GlobalMetric>();

	private int round;

	protected final Map<SensorID, Sensor> sensorIDs = new HashMap<SensorID, Sensor>();

	private final Collection<SimulationEventListener> eventListeners = new ArrayList<SimulationEventListener>();

	protected File outputDirectory;

	private final Collection<LogWriter> logWriters = new ArrayList<LogWriter>();

	private boolean initialized;

	private TerminationPolicy terminationPolicy;

	public Simulation() {
		// addGlobalMetric(new VarianceMetric());
		// addGlobalMetric(new VarianceTimeMetric());
		// addGlobalMetric(new GuestrinEntropyMetric());

		addGlobalMetric(new SpatialFieldValueMetric());
		addGlobalMetric(new RMSEMetric());

		// addGlobalMetric(new PredictionQualityMetric());
		// addGlobalMetric(new GuestrinMutualInformationMetric());

		addLogWriter(new BBMaxSumStatistics());
	}

	public double getTime() {
		return timer.getTime();
	}

	public void addGlobalMetric(GlobalMetric metric) {
		globalMetrics.add(metric);
		logWriters.add(metric);
	}

	@Required
	public void setTerminationPolicy(TerminationPolicy terminationPolicy) {
		this.terminationPolicy = terminationPolicy;
	}

	public void addSensor(Sensor sensor) {
		Validate.isTrue(sensor.getID() == null,
				"Sensor has already been registered");
		sensor.setSensorID(new SensorID(lastID++));
		sensor.setEnvironment(environment);
		sensor.setCommunicationModule(new CommunicationModule(sensor, this,
				messageInterchange));
		sensors.add(sensor);
		sensorIDs.put(sensor.getID(), sensor);
	}

	public void runUntilFinished() throws Exception {
		initialize();

		while (!isSimulationEnded()) {
			runSingleRound();
		}
	}

	private void finishSimulation() throws Exception {
		for (SimulationEventListener listener : eventListeners) {
			listener.handleEndOfSimulation(this);
		}

		for (LogWriter writer : logWriters) {
			writer.finaliseLogs();
		}
	}

	public boolean isSimulationEnded() {
		return terminationPolicy.isSimulationEnded();
	}

	public void runSingleRound() throws Exception {
		if (!initialized)
			initialize();

		if (isSimulationEnded())
			return;

		for (SimulationEventListener listener : eventListeners)
			listener.handleStartOfRound(this, round, timer.getTime());

		log.info("Starting round " + round + ". Time " + timer.getTime());
		runSensorRound();

		environment.update();

		nextTimeTick();

		synchronized (eventListeners) {
			for (SimulationEventListener listener : eventListeners)
				listener.handleEndOfRound(this, round, timer.getTime());
		}

		for (LogWriter writer : logWriters)
			writer.handleEndOfRound(round, timer.getTime());

		round++;

		if (isSimulationEnded()) {
			finishSimulation();
		}
	}

	public void initialize() {
		if (initialized)
			return;

		// the sensors can override the initialplacement of the simulation
		InitialPlacement initialPlacement = sensors.iterator().next()
				.getInitialPlacement();

		if (initialPlacement == null) {
			// no initialplacement defined in the sensors, use the one supplied
			// by the simulation by default
			initialPlacement = this.initialPlacement;
			log.info("No InitialPlacement found in sensor definition, "
					+ "using IP supplied in simulation definition");
		} else {
			log.info("Using InitialPlacement found in sensor definition");
		}

		Validate.notNull(environment.getAccessibilityGraph());

		initialPlacement.setInitialLocations(sensors, environment
				.getAccessibilityGraph(), this);

		initializeSensors();

		round = 0;
		for (LogWriter logWriter : logWriters) {
			logWriter.setOutputDirectory(outputDirectory);
		}

		for (GlobalMetric metric : globalMetrics) {
			metric.initialize(this);
		}

		initialized = true;

		log.info("Simulation initialized with " + sensors.size() + " sensors");

		for (SimulationEventListener listener : eventListeners)
			listener.handleStartOfSimulation(this, timer.getTime());
	}

	private void nextTimeTick() {
		timer.nextTick();

		environment.handleTimerEvent(timer.getTime());

		for (Sensor sensor : sensors) {
			sensor.handleTimerEvent(timer.getTime());
		}

		for (GlobalMetric metric : globalMetrics)
			metric.handleEndOfRound(sensors, timer, round);
	}

	public Map<SensorID, Location> getNeighbourLocations(final Sensor sensor) {
		Map<SensorID, Location> result = new HashMap<SensorID, Location>();

		for (Sensor otherSensor : getNeighboursInternal(sensor)) {
			if (!otherSensor.equals(sensor))
				result.put(otherSensor.getID(), otherSensor.getLocation());
		}

		return result;
	}

	private List<Sensor> getNeighboursInternal(Sensor sensor) {
		List<Sensor> result = new ArrayList<Sensor>();

		for (Sensor otherSensor : sensors) {
			if (!otherSensor.equals(sensor))
				result.add(otherSensor);
		}

		return result;
	}

	public List<SensorID> getNeighbours(Sensor sensor) {
		List<SensorID> result = new ArrayList<SensorID>();

		for (Sensor otherSensor : getNeighboursInternal(sensor)) {
			result.add(otherSensor.getID());
		}

		return result;
	}

	public String getDescription() {
		return description;
	}

	@Required
	public String getSimpleName() {
		return simpleName;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setSimpleName(String unixName) {
		this.simpleName = unixName;
	}

	@Required
	public void setDescription(String description) {
		this.description = description;
	}

	@Required
	public void setEnvironment(Environment environment) {
		this.environment = environment;
		addLogWriter(environment);
	}

	public void setGlobalMetrics(Collection<GlobalMetric> globalMetrics) {
		this.globalMetrics = globalMetrics;
	}

	public Collection<GlobalMetric> getGlobalMetrics() {
		return globalMetrics;
	}

	public Collection<Sensor> getSensors() {
		return sensors;
	}

	public Map<SensorID, Location> getSensorLocationsMap() {
		Map<SensorID, Location> locations = new HashMap<SensorID, Location>();

		for (Sensor sensor : sensors) {
			if (sensor.getLocation() != null)
				locations.put(sensor.getID(), sensor.getLocation());
		}

		return locations;
	}

	public Collection<Location> getSensorLocations() {
		return getSensorLocationsMap().values();
	}

	public Sensor getSensorByID(SensorID sensorID) {
		Sensor sensor = sensorIDs.get(sensorID);
		if (sensor == null)
			throw new IllegalArgumentException("Sensor with ID " + sensorID
					+ " does not exist");

		return sensor;
	}

	public MessageInterchange getMessageInterchange() {
		return messageInterchange;
	}

	public void setMessageInterchange(MessageInterchange messageInterchange) {
		this.messageInterchange = messageInterchange;
	}

	public void addEventListener(SimulationEventListener eventListener) {
		synchronized (eventListeners) {
			eventListeners.add(eventListener);
		}
	}

	public void setInitialPlacement(InitialPlacement initialPlacement) {
		this.initialPlacement = initialPlacement;
	}

	public InitialPlacement getInitialPlacement() {
		return initialPlacement;
	}

	public void setOutputDirectory(File file) {
		this.outputDirectory = file;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void addLogWriter(LogWriter writer) {
		Validate.isTrue(!logWriters.contains(writer));
		logWriters.add(writer);
	}

	public Collection<Sensor> getSensorsAtLocation(LocationImpl location) {
		Map<SensorID, Location> sensorLocationsMap = getSensorLocationsMap();
		Collection<Sensor> result = new ArrayList<Sensor>();

		for (SensorID sensorID : sensorLocationsMap.keySet()) {
			if (sensorLocationsMap.get(sensorID).equals(location)) {
				result.add(getSensorByID(sensorID));
			}
		}

		return result;
	}

	public int getCurrentRound() {
		return round;
	}

	public double getTimeAtRound(int round) {
		return timer.getTimeAtRound(round);
	}

	protected void runSensorRound() {
		Queue<SimulationEvent> events = new LinkedList<SimulationEvent>();

		for (Sensor sensor : sensors) {
			List<? extends SimulationEvent> initialSensorEvents = sensor
					.startRound();

			events.addAll(initialSensorEvents);
		}

		while (!events.isEmpty()) {
			SimulationEvent event = events.remove();
			events.addAll(event.run());
		}

	}

	public void initializeSensors() {
		for (Sensor sensor : sensors) {
			sensor.initialize(this);
		}
	}

	public Set<SensorID> getSensorIDs() {
		return new HashSet<SensorID>(sensorIDs.keySet());
	}
}
