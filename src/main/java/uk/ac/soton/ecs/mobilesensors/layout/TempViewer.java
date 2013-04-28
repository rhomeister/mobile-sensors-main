package uk.ac.soton.ecs.mobilesensors.layout;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;

public class TempViewer {

	private final VisualizationViewer<ClusteredGraph<Location, AccessibilityRelation>, Integer> vv;

	public TempViewer(
			HierarchicalClusteredGraph<Location, AccessibilityRelation> hierarchicalClusteredGraph) {
		JFrame jf = new JFrame();
		ClusteredGraph<Location, AccessibilityRelation> root = hierarchicalClusteredGraph
				.getRoot();

		TreeLayout<ClusteredGraph<Location, AccessibilityRelation>, Integer> layout = new TreeLayout<ClusteredGraph<Location, AccessibilityRelation>, Integer>(
				hierarchicalClusteredGraph);

		vv = new VisualizationViewer<ClusteredGraph<Location, AccessibilityRelation>, Integer>(
				layout);

		vv.setBackground(Color.WHITE);

		// vv.getRenderContext().setVertexShapeTransformer(
		// new ConstantTransformer(new Ellipse2D.Double(-3.0, -3.0, 6.0,
		// 6.0)));
		//
		// vv.setVertexToolTipTransformer(new Transformer<Location, String>() {
		// public String transform(Location input) {
		// return input.getCoordinates().toString();
		// }
		// });

		vv.getRenderContext()
				.setVertexLabelTransformer(
						new Transformer<ClusteredGraph<Location, AccessibilityRelation>, String>() {

							public String transform(
									ClusteredGraph<Location, AccessibilityRelation> input) {
								return input.getLabel();
							}
						});

		// vv.getRenderContext().setEdgeFillPaintTransformer(edgePaintTransformer);

		vv.getRenderContext()
				.setEdgeShapeTransformer(
						new EdgeShape.Line<ClusteredGraph<Location, AccessibilityRelation>, Integer>());

		vv.setPreferredSize(new Dimension(1400, 1400));

		jf.setPreferredSize(new Dimension(1400, 1400));
		jf.getContentPane().add(vv);
		// jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
		jf.setTitle("Layout Graph");

	}
}
