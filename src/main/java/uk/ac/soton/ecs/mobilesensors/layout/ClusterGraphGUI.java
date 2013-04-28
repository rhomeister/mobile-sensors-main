package uk.ac.soton.ecs.mobilesensors.layout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.TransformerUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class ClusterGraphGUI {

	public final static boolean test = false;

	public static void show(
			ClusteredGraph<Location, AccessibilityRelation> graph) {
		show(graph, new HashMap<Set<Location>, Color>());
	}

	public static void show(
			ClusteredGraph<Location, AccessibilityRelation> graph,
			final Map<Set<Location>, Color> colorMap) {
		Map<Node<Location>, Point2D> coordinates = new HashMap<Node<Location>, Point2D>();

		for (Node<Location> cluster : graph.getVertices()) {
			double averageX = 0;
			double averageY = 0;

			for (Location location : cluster) {
				averageX += location.getX() / 2;
				averageY += location.getY() / 2;
			}

			averageX /= cluster.size();
			averageY /= cluster.size();

			coordinates.put(cluster, new Point2D.Double(averageX, averageY));
		}

		JFrame jf = new JFrame();

		StaticLayout<Node<Location>, ClusterEdge<Location>> layout = new StaticLayout<Node<Location>, ClusterEdge<Location>>(
				graph, TransformerUtils.mapTransformer(coordinates));

		VisualizationViewer<Node<Location>, ClusterEdge<Location>> vv = new VisualizationViewer<Node<Location>, ClusterEdge<Location>>(
				layout);

		vv.getRenderContext().setVertexLabelTransformer(
				new Transformer<Node<Location>, String>() {
					public String transform(Node<Location> input) {
						return input.getId() + "";
					}
				});

		vv.getRenderContext().setVertexFillPaintTransformer(
				new Transformer<Node<Location>, Paint>() {
					public Paint transform(Node<Location> input) {
						if (input instanceof TransitNode) {
							TransitNode<?> node = (TransitNode<?>) input;
							if (node.isExternal()) {
								return Color.black;
							} else {
								return Color.white;
							}
						} else {
							return colorMap.get(input.getVertices());
						}
					}
				});

		vv.setBackground(Color.WHITE);

		vv.setPreferredSize(new Dimension(1400, 1400));

		vv.getRenderContext().setVertexShapeTransformer(
				new Transformer<Node<Location>, Shape>() {
					public Shape transform(Node<Location> input) {
						if (input instanceof TransitNode) {
							return new Rectangle2D.Double(-8.0, -8.0, 16.0,
									16.0);
						}

						return new Ellipse2D.Double(-8.0, -8.0, 16.0, 16.0);
					}
				});

		// vv.getRenderContext().setEdgeShapeTransformer(
		// new EdgeShape.Line<Set<Location>, AccessibilityRelation>());

		jf.setTitle("Clustered Layout Graph " + graph.getLabel());
		jf.getContentPane().add(vv);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);

		saveToSVG(vv, graph);

	}

	public static void saveToSVG(
			VisualizationViewer<Node<Location>, ClusterEdge<Location>> vv,
			ClusteredGraph<Location, AccessibilityRelation> graph) {
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
		vv.paint(svgGenerator);
		vv.paint(svgGenerator);

		// Finally, stream out SVG to the standard output using UTF-8
		// character to byte encoding
		boolean useCSS = true; // we want to use CSS style attribute

		OutputStream printWriter;
		try {
			String filename = "test_" + graph.getLabel() + "_"
					+ System.currentTimeMillis() + ".svg";
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
