package uk.ac.soton.ecs.mobilesensors.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.soton.ecs.mobilesensors.configuration.AccessibilityGraphReader;
import uk.ac.soton.ecs.utils.PajekNetWriterFixed;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.PajekNetWriter;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;

public class AccessibilityGraphImpl extends
		UndirectedSparseGraph<Location, AccessibilityRelation> implements
		AccessibilityGraph<Location> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5870536777022442685L;

	private static Log log = LogFactory.getLog(AccessibilityGraphImpl.class);

	private Map<Point2D, Location> locationToCoordinateMap;

	private transient DijkstraShortestPath<Location, AccessibilityRelation> dijkstra;

	private transient DistanceTable distances;

	private final Integer hashcode = null;

	public AccessibilityGraphImpl() {
	}

	public AccessibilityGraphImpl(
			Graph<Location, AccessibilityRelation> transform) {
		for (AccessibilityRelation relation : transform.getEdges()) {
			addEdge(relation, relation.getLocation1(), relation.getLocation2());
		}
	}

	public void addAccessibilityRelation(Location location1, Location location2) {
		super.addEdge(new AccessibilityRelationImpl(location1, location2),
				location1, location2);
	}

	public Location addAccessibleLocation(double x, double y) {
		LocationImpl newLocation = new LocationImpl(x, y, this);
		addVertex(newLocation);
		return newLocation;
	}

	public Iterator<Location> iterator() {
		return getVertices().iterator();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("AccessibilityGraphImpl\n");

		for (Location location : this) {
			buffer.append(location.toString() + "\n");
		}

		for (AccessibilityRelation relation : getAccessibilityRelations()) {
			buffer.append(relation.toString() + "\n");
		}

		return buffer.toString();
	}

	public int getLocationCount() {
		return getVertices().size();
	}

	public List<Location> getLocations() {
		return new ArrayList<Location>(getVertices());
	}

	// @Override
	// public Edge addEdge(Edge e) {
	// Edge result = null;
	//
	// if (e instanceof AccessibilityRelationImpl)
	// result = super.addEdge(e);
	// else {
	// Pair endpoints = e.getEndpoints();
	// result = addEdge(new AccessibilityRelationImpl(
	// (LocationImpl) endpoints.getFirst(),
	// (LocationImpl) endpoints.getSecond()));
	// }
	//
	// return result;
	// }

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AccessibilityGraphImpl) {
			AccessibilityGraphImpl graph = (AccessibilityGraphImpl) obj;

			if (graph.getLocations().containsAll(getLocations())
					&& getLocations().containsAll(graph.getLocations())) {
				return graph.getAccessibilityRelations().containsAll(
						getAccessibilityRelations());
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		List<Location> sortedVertices = new ArrayList<Location>(getVertices());
		Collections.sort(sortedVertices, new Comparator<Location>() {
			public int compare(Location arg0, Location arg1) {
				int compare = Double.compare(arg0.getX(), arg0.getX());
				if (compare == 0) {
					return Double.compare(arg0.getY(), arg0.getY());
				} else {
					return compare;
				}
			}
		});

		return sortedVertices.hashCode();
	}

	public Collection<AccessibilityRelation> getAccessibilityRelations() {
		return getEdges();
	}

	public Location getLocation(Location location) {
		for (Location locationLocal : this) {
			if (locationLocal.equals(location))
				return locationLocal;
		}

		return null;
	}

	public void addAccessibilityRelations(double maxDistance) {
		List<Location> locations = getLocations();

		for (int i = 0; i < locations.size(); i++) {
			for (int j = i + 1; j < locations.size(); j++) {
				Location location1 = locations.get(i);
				Location location2 = locations.get(j);

				if (!location1.equals(location2)
						&& location1.directDistance(location2) < maxDistance) {
					addAccessibilityRelation(location1, location2);
				}
			}
		}
	}

	public void copyFrom(AccessibilityGraphImpl graph) {
		for (Location location : graph) {
			addAccessibleLocation(location.getX(), location.getY());
		}

		for (AccessibilityRelation relation : graph.getAccessibilityRelations()) {
			Location loc1 = getLocation(relation.getLocation1());
			Location loc2 = getLocation(relation.getLocation2());
			addAccessibilityRelation(loc1, loc2);
		}
	}

	public void removeLocation(Location location) {
		removeVertex(location);
	}

	public Location getNearestLocation(double x, double y) {
		Point2D coordinates = new Point2D.Double(x, y);
		double shortestDistance = Double.POSITIVE_INFINITY;
		Location nearestLocation = null;

		for (Location location : getLocations()) {
			double distance = location.directDistance(coordinates);
			if (distance < shortestDistance) {
				shortestDistance = distance;
				nearestLocation = location;
			}
		}

		return nearestLocation;
	}

	public Rectangle2D getBoundingBox() {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (Location location : this) {
			minX = Math.min(minX, location.getX());
			minY = Math.min(minY, location.getY());
			maxX = Math.max(maxX, location.getX());
			maxY = Math.max(maxY, location.getY());
		}

		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}

	public void write(String filename) throws IOException {
		log.info("Writing grid to " + filename);
		PajekNetWriter<Location, AccessibilityRelation> writer = new PajekNetWriterFixed<Location, AccessibilityRelation>();

		writer.save(this, filename, null, null,
				new Transformer<Location, Point2D>() {
					public Point2D transform(Location input) {
						return input.getCoordinates();
					}
				});
	}

	public void write(File file) throws IOException {
		write(file.getAbsolutePath());
	}

	public static AccessibilityGraphImpl readGraph(File file)
			throws IOException {
		return readGraph(file, 1.0);
	}

	public Location getLocation(Point2D point) {
		if (locationToCoordinateMap == null) {
			locationToCoordinateMap = new HashMap<Point2D, Location>();

			for (Location location : getVertices()) {
				locationToCoordinateMap
						.put(location.getCoordinates(), location);
			}
		}

		Location location = locationToCoordinateMap.get(point);

		Validate.notNull(location);
		return location;

	}

	public List<AccessibilityRelation> getShortestPath(Location location1,
			Location location2) {
		return getDijkstraAlgorithm().getPath(location1, location2);
	}

	private DijkstraShortestPath<Location, AccessibilityRelation> getDijkstraAlgorithm() {
		if (dijkstra == null) {
			dijkstra = new DijkstraShortestPath<Location, AccessibilityRelation>(
					this);
			dijkstra.enableCaching(true);
		}

		return dijkstra;
	}

	public List<Location> getShortestPathLocations(Location location1,
			Location location2, boolean includeSource) {
		List<AccessibilityRelation> shortestPath = getShortestPath(location1,
				location2);

		List<Location> path = new ArrayList<Location>();
		path.add(location1);
		Location current = location1;

		for (AccessibilityRelation edge : shortestPath) {
			path.add(current = edge.getOther(current));
		}

		if (!includeSource)
			return path.subList(1, path.size());
		else
			return path;
	}

	public double getShortestPathLength(Location location1, Location location2) {
		if (distances == null)
			distances = new DistanceTable(this);

		Validate.notNull(location1);
		Validate.notNull(location2);

		return distances.getShortestPathLength(location1, location2);
	}

	public Location getNearestLocation(Point2D point) {
		return getNearestLocation(point.getX(), point.getY());
	}

	public AccessibilityGraphImpl getSubgraph(Collection<Location> set) {
		AccessibilityGraphImpl accessibilityGraphImpl = new AccessibilityGraphImpl();

		for (Location location : set) {
			accessibilityGraphImpl.addVertex(location);
		}

		for (AccessibilityRelation relation : getEdges()) {
			if (set.contains(relation.getLocation1())
					&& set.contains(relation.getLocation2()))
				accessibilityGraphImpl.addAccessibilityRelation(
						relation.getLocation1(), relation.getLocation2());
		}

		return accessibilityGraphImpl;
	}

	public Set<Set<Location>> getComponents() {
		return new WeakComponentClusterer<Location, AccessibilityRelation>()
				.transform(this);
	}

	public static void main(String[] args) throws GraphIOException, IOException {
		AccessibilityGraphImpl graph = readGraph(new File(
				"/home/rs06r/workspace/experiments/src/main/resources/graphs/building32.txt"));

		System.out.println(graph.hashCode());

		// GraphGUI.show(graph);

		// graph.write("ship_2");
	}

	public static AccessibilityGraphImpl readGraphML(String filename)
			throws GraphIOException, IOException {
		GraphMLReader2<UndirectedSparseGraph<LocationImpl, AccessibilityRelationImpl>, LocationImpl, AccessibilityRelationImpl> reader;

		Reader fileReader = new FileReader(filename);

		Transformer<GraphMetadata, UndirectedSparseGraph<LocationImpl, AccessibilityRelationImpl>> graphTransformer = new Transformer<GraphMetadata, UndirectedSparseGraph<LocationImpl, AccessibilityRelationImpl>>() {

			public UndirectedSparseGraph<LocationImpl, AccessibilityRelationImpl> transform(
					GraphMetadata input) {
				return new UndirectedSparseGraph<LocationImpl, AccessibilityRelationImpl>();
			}
		};

		Transformer<NodeMetadata, LocationImpl> vertexTransformer = new Transformer<NodeMetadata, LocationImpl>() {

			public LocationImpl transform(NodeMetadata input) {
				Double x = Double.parseDouble(input.getProperty("x"));
				Double y = Double.parseDouble(input.getProperty("y"));

				return new LocationImpl(x, y);
			}
		};

		Transformer<EdgeMetadata, AccessibilityRelationImpl> edgeTransformer = new Transformer<EdgeMetadata, AccessibilityRelationImpl>() {
			public AccessibilityRelationImpl transform(EdgeMetadata input) {
				return new AccessibilityRelationImpl();
			}
		};

		Transformer<HyperEdgeMetadata, AccessibilityRelationImpl> hyperEdgeTransformer = new Transformer<HyperEdgeMetadata, AccessibilityRelationImpl>() {
			public AccessibilityRelationImpl transform(HyperEdgeMetadata input) {
				return null;
			}
		};

		reader = new GraphMLReader2<UndirectedSparseGraph<LocationImpl, AccessibilityRelationImpl>, LocationImpl, AccessibilityRelationImpl>(
				fileReader, graphTransformer, vertexTransformer,
				edgeTransformer, hyperEdgeTransformer);

		UndirectedSparseGraph<LocationImpl, AccessibilityRelationImpl> readGraph = reader
				.readGraph();

		AccessibilityGraphImpl accessibilityGraphImpl = new AccessibilityGraphImpl();

		for (Location location : readGraph.getVertices()) {
			accessibilityGraphImpl.addAccessibleLocation(location.getX(),
					location.getY());
		}

		for (LocationImpl location : readGraph.getVertices()) {
			for (LocationImpl neighbor : readGraph.getNeighbors(location)) {
				Location location1 = accessibilityGraphImpl
						.getLocation((Location) location);
				Location location2 = accessibilityGraphImpl
						.getLocation((Location) neighbor);

				accessibilityGraphImpl.addAccessibilityRelation(location1,
						location2);
			}
		}

		return accessibilityGraphImpl;
	}

	public static AccessibilityGraphImpl readGraph(File file, double scale)
			throws FileNotFoundException, IOException {
		log.info("Reading AccessibilityGraph from " + file);

		AccessibilityGraphReader reader = new AccessibilityGraphReader();

		AccessibilityGraphImpl graph = reader.load(new FileReader(file), scale);

		return graph;
	}

	/**
	 * Translate all locations of a graph by the given amount
	 * 
	 * @param x
	 * @param y
	 */
	public void translate(double x, double y) {
		for (Location location : getVertices()) {
			location.translate(x, y);
		}
	}

	public void writeGrid(String string) throws Exception {
		GraphGridAdaptor graphGridAdaptor = new GraphGridAdaptor();
		graphGridAdaptor.setGraph(this);
		graphGridAdaptor.afterPropertiesSet();
		graphGridAdaptor.write(new File(string));
	}
}
