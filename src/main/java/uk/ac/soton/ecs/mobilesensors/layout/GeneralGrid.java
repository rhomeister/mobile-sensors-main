package uk.ac.soton.ecs.mobilesensors.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Jama.Matrix;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class GeneralGrid implements Grid {

	protected final List<Point2D> gridPoints = new ArrayList<Point2D>();
	private Matrix grid;
	private Rectangle2D boundingRectangle;
	private List<Location> locations;
	private boolean sorted = false;
	private Quadtree quadTree;
	private boolean quadTreeEnabled;

	public List<Point2D> getGridPoints() {
		if (sorted == false) {
			Collections.sort(gridPoints, new Comparator<Point2D>() {
				public int compare(Point2D o1, Point2D o2) {
					if (o1.getX() == o2.getX()) {
						return Double.compare(o1.getY(), o2.getY());
					}

					return Double.compare(o1.getX(), o2.getX());
				}
			});
			sorted = true;
		}

		return gridPoints;
	}

	public Iterator<Point2D> iterator() {
		return gridPoints.iterator();
	}

	public void addGridPoint(Point2D gridPoint) {
		sorted = false;
		gridPoints.add(gridPoint);
	}

	public Matrix getGrid() {
		return grid;
	}

	public int getGridPointCount() {
		return gridPoints.size();
	}

	public void setQuadTreeEnabled(boolean quadTreeEnabled) {
		this.quadTreeEnabled = quadTreeEnabled;
	}

	@SuppressWarnings("unchecked")
	public Collection<Point2D> getGridPoints(final Point2D location,
			final double distance) {
		if (quadTree == null && quadTreeEnabled) {
			initQuadTree();
		}

		List<Point2D> queryResult;

		if (quadTreeEnabled) {
			double minX = location.getX() - distance;
			double maxX = location.getX() + distance;
			double minY = location.getY() - distance;
			double maxY = location.getY() + distance;

			Envelope envelope = new Envelope(minX, maxX, minY, maxY);
			queryResult = quadTree.query(envelope);
		} else {
			queryResult = gridPoints;
		}

		List<Point2D> result = new ArrayList<Point2D>();

		for (Point2D point2D : queryResult) {
			if (point2D.distanceSq(location) <= distance * distance) {
				result.add(point2D);
			}
		}

		return result;
	}

	private void initQuadTree() {
		quadTree = new Quadtree();

		for (Point2D point : gridPoints) {
			Envelope envelope = new Envelope(new Coordinate(point.getX(),
					point.getY()));
			quadTree.insert(envelope, point);
		}
	}

	public Collection<Point2D> getGridPoints(Location location, double distance) {
		return getGridPoints(location.getCoordinates(), distance);
	}

	protected void initialize() {
		double[][] grid = new double[gridPoints.size()][2];

		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		int i = 0;
		for (Point2D gridPoint : gridPoints) {
			grid[i][0] = gridPoint.getX();
			grid[i++][1] = gridPoint.getY();

			minX = Math.min(minX, gridPoint.getX());
			minY = Math.min(minY, gridPoint.getY());
			maxX = Math.max(maxX, gridPoint.getX());
			maxY = Math.max(maxY, gridPoint.getY());
		}

		this.grid = new Matrix(grid);
		boundingRectangle = new Rectangle2D.Double(minX, minY, maxX - minX,
				maxY - minY);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Grid) {
			Grid grid = (Grid) obj;

			return Arrays.deepEquals(grid.getGrid().getArray(),
					this.grid.getArray());
		}

		return false;
	}

	private static Log log = LogFactory.getLog(GeneralGrid.class);

	public void write(File file) throws IOException {
		write(this, file);
	}

	public static Grid read(File file) throws Exception {
		log.info("Reading Grid from " + file.getPath());

		GeneralGrid grid = new GeneralGrid();
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line = reader.readLine();

		try {
			while (line != null) {
				Scanner scanner = new Scanner(line);
				grid.addGridPoint(new Point2D.Double(scanner.nextDouble(),
						scanner.nextDouble()));

				line = reader.readLine();
			}
		} catch (Exception e) {
			throw new Exception(
					"Error while parsing " + file.getAbsolutePath(), e);
		}

		grid.initialize();

		return grid;
	}

	public static void write(Grid grid, File file) throws IOException {
		log.info("Writing grid to " + file.getAbsolutePath());

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		for (Point2D point : grid) {
			writer.write(String.format(Locale.US, "%10.5f %10.5f\n",
					point.getX(), point.getY()));
		}

		writer.close();
	}

	public Rectangle2D getBoundingRectangle() {
		return boundingRectangle;
	}

	public List<Location> getLocations() {
		if (locations == null) {
			locations = convert(getGridPoints());
		}

		return locations;
	}

	private List<Location> convert(Collection<Point2D> gridPoints2) {
		ArrayList<Location> result = new ArrayList<Location>();

		for (Point2D point : getGridPoints()) {
			LocationImpl location = new LocationImpl(point.getX(), point.getY());
			result.add(location);
		}
		return result;
	}

	public List<Location> getLocations(Location location, double evaluationRange) {
		return convert(getGridPoints(location, evaluationRange));
	}

	public static void main(String[] args) {
		RectangularGrid grid = new RectangularGrid(100, 100, 100);

		StopWatch watch = new StopWatch();
		watch.start();

		for (int i = 0; i < 200; i++) {
			grid.getGridPoints(new Point2D.Double(0, 0), 19);
		}

		watch.stop();
		System.out.println(watch.getTime());

		grid.setQuadTreeEnabled(true);
		grid.getGridPoints(new Point2D.Double(0, 0), 19);
		watch.reset();
		watch.start();

		for (int i = 0; i < 200; i++) {
			grid.getGridPoints(new Point2D.Double(0, 0), 4);
		}

		watch.stop();
		System.out.println(watch.getTime());
	}
}
