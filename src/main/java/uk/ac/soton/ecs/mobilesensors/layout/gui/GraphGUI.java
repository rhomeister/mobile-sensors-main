package uk.ac.soton.ecs.mobilesensors.layout.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.TransformerUtils;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;

public class GraphGUI {

	private final VisualizationViewer<Location, AccessibilityRelation> vv;
	private Map<Set<Location>, Color> colorMap;
	private HashSet<Location> startPoints;
	private HashSet<Location> endPoints;
	private HashSet<AccessibilityRelation> pathEdges;
	private AccessibilityGraphImpl graph;
	private List<Set<Location>> clusters;

	public GraphGUI(AccessibilityGraphImpl graph,
			ClusteredGraph<Location, AccessibilityRelation> clusteredGraph,
			final List<Location> path) {
		Map<Location, Point2D> coordinates = new HashMap<Location, Point2D>();
		this.graph = graph;

		this.clusters = new ArrayList<Set<Location>>();

		if (clusteredGraph != null) {
			for (Cluster<Location> cluster : clusteredGraph.getClusters()) {
				this.clusters.add(cluster.getVertices());
			}

			colorMap = createColorMap(clusters);

			for (Location location : graph) {
				Point2D point = location.getCoordinates();
				point = new Point2D.Double(point.getX() * .5, point.getY() * .5);
				coordinates.put(location, point);
			}
		}

		JFrame jf = new JFrame();

		StaticLayout<Location, AccessibilityRelation> layout = new StaticLayout<Location, AccessibilityRelation>(
				graph, TransformerUtils.mapTransformer(coordinates));

		vv = new VisualizationViewer<Location, AccessibilityRelation>(layout);

		vv.setBackground(Color.WHITE);

		setClusters(clusters);

		setPath(path);

		vv.getRenderContext().setVertexShapeTransformer(
				new ConstantTransformer(new Ellipse2D.Double(-3.0, -3.0, 6.0,
						6.0)));

		vv.setVertexToolTipTransformer(new Transformer<Location, String>() {
			public String transform(Location input) {
				return input.getCoordinates().toString();
			}
		});

		// vv.getRenderContext().setEdgeFillPaintTransformer(edgePaintTransformer);

		vv.getRenderContext().setEdgeShapeTransformer(
				new EdgeShape.Line<Location, AccessibilityRelation>());

		vv.setPreferredSize(new Dimension(1400, 1400));

		jf.setPreferredSize(new Dimension(1400, 1400));
		jf.getContentPane().add(vv);
		// jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
		jf.setTitle("Layout Graph");

		if (clusteredGraph != null) {
			// ClusterGraphGUI.show(clusteredGraph, colorMap);
		}

	}

	public void setPath(final List<Location> path) {
		if (path != null)
			setPaths(Collections.singleton(path));
	}

	public void setClusters(final Collection<Set<Location>> clusters) {
		if (clusters != null) {
			vv.getRenderContext().setVertexFillPaintTransformer(
					new Transformer<Location, Paint>() {
						public Paint transform(Location input) {
							for (Set<Location> set : clusters) {
								if (set.contains(input)) {
									return colorMap.get(set);
								}
							}

							return Color.lightGray;
						}
					});
		}
	}

	protected boolean isPathEdge(AccessibilityRelation input,
			List<Location> path) {
		if (path == null)
			return false;

		for (int i = 0; i < path.size() - 1; i++) {
			if (path.get(i).equals(input.getOther(path.get(i + 1))))
				return true;
		}

		return false;
	}

	private Map<Set<Location>, Color> createColorMap(
			Collection<Set<Location>> clusters) {
		HashMap<Set<Location>, Color> colorMap = new HashMap<Set<Location>, Color>();

		if (clusters != null) {

			Color[] colors = { Color.blue, Color.red, Color.yellow, Color.cyan,
					Color.gray, Color.green, Color.orange, Color.lightGray,
					Color.pink, Color.magenta, Color.darkGray };

			int i = 0;
			for (Set<Location> set : clusters) {
				colorMap.put(set, colors[i++ % colors.length]);
			}
		}
		return colorMap;

	}

	public GraphGUI(AccessibilityGraphImpl graph) {
		this(graph, null, null);
	}

	public GraphGUI(AccessibilityGraphImpl graph,
			ClusteredGraph<Location, AccessibilityRelation> clusteredGraph) {
		this(graph, clusteredGraph, null);
	}

	public Map<Set<Location>, Color> getClusterColorMap() {
		return colorMap;
	}

	public void setPaths(final Collection<List<Location>> paths) {
		startPoints = new HashSet<Location>();
		endPoints = new HashSet<Location>();
		pathEdges = new HashSet<AccessibilityRelation>();

		for (List<Location> list : paths) {
			startPoints.add(list.get(0));
			endPoints.add(list.get(list.size() - 1));

			for (int i = 0; i < list.size() - 1; i++) {
				pathEdges.add(graph.findEdge(list.get(i), list.get(i + 1)));
			}
		}

		vv.getRenderContext().setVertexDrawPaintTransformer(
				new Transformer<Location, Paint>() {
					public Paint transform(Location input) {
						if (startPoints.contains(input)) {
							return Color.green;
						}
						if (endPoints.contains(input)) {
							return Color.red;
						}

						return Color.black;
					}
				});

		vv.getRenderContext().setVertexStrokeTransformer(
				new Transformer<Location, Stroke>() {
					public Stroke transform(Location input) {
						if (startPoints.contains(input)
								|| endPoints.contains(input))
							return new BasicStroke(3.0f);

						return new BasicStroke(1.0f);
					}
				});

		vv.getRenderContext().setEdgeStrokeTransformer(
				new Transformer<AccessibilityRelation, Stroke>() {
					public Stroke transform(AccessibilityRelation input) {
						if (pathEdges.contains(input))
							return new BasicStroke(5.0f);

						return new BasicStroke(1.0f);
					}
				});

		vv.repaint();
	}

	public void saveToSVG() {
		// Get a DOMImplementation
		DOMImplementation domImpl = GenericDOMImplementation
				.getDOMImplementation();
		String svgNamespaceURI = "http://www.w3.org/2000/svg";

		// Create an instance of org.w3c.dom.Document
		Document document = domImpl
				.createDocument(svgNamespaceURI, "svg", null);

		// Create an instance of the SVG Generator
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		// Render into the SVG Graphics2D implementation
		// vv.repaint();

		// vv.paint(svgGenerator);
		// vv.paint(svgGenerator);
		vv.paint(svgGenerator);

		// Finally, stream out SVG to the standard output using UTF-8
		// character to byte encoding
		boolean useCSS = true; // we want to use CSS style attribute

		OutputStream printWriter;
		try {
			String filename = "test_" + System.currentTimeMillis() + ".svg";
			printWriter = new FileOutputStream(filename);

			System.out.println("SAVING TO file " + filename);

			Writer out = new OutputStreamWriter(printWriter, "UTF-8");
			svgGenerator.stream(out, useCSS);
			printWriter.flush();
			printWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
