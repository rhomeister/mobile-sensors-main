package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class StateFrame<S extends State> {

	private final Map<Cluster<Location>, VisitComponent> visitationLabels = new HashMap<Cluster<Location>, VisitComponent>();
	private ClusteredGraph<Location, AccessibilityRelation> clusteredGraph;

	public StateFrame(S initialState,
			Map<Set<Location>, Color> clusterColorMap,
			ClusteredGraph<Location, AccessibilityRelation> clusteredGraph,
			int tau) {
		Validate.notNull(initialState);
		JFrame frame = new JFrame();
		frame.setTitle("Cluster States");
		this.clusteredGraph = clusteredGraph;

		frame.getContentPane().setLayout(
				new GridLayout(clusteredGraph.getClusterCount(), 2));

		for (Cluster<Location> cluster : clusteredGraph.getClusters()) {
			ClusterCanvas clusterCanvas = new ClusterCanvas(clusterColorMap
					.get(cluster.getVertices()));
			clusterCanvas.setPreferredSize(new Dimension(100, 40));
			frame.getContentPane().add(clusterCanvas);
			VisitComponent label = new VisitComponent(tau);
			frame.getContentPane().add(label);
			visitationLabels.put(cluster, label);
		}

		setClusterVisitationStates(initialState);

		frame.pack();
		frame.setVisible(true);
	}

	public void setClusterVisitationStates(State state) {
		for (Cluster<Location> cluster : clusteredGraph.getClusters()) {
			visitationLabels.get(cluster).setVisitationTime(
					state.getLastVisitTime(cluster));
		}
	}

	private static class VisitComponent extends JLabel {

		private static final long serialVersionUID = 1L;
		private int visitTime;
		private double tau;

		public VisitComponent(int tau) {
			setOpaque(false);
			setHorizontalAlignment(SwingConstants.CENTER);
			this.tau = tau;
			setPreferredSize(new Dimension(100, 40));
			setVisitationTime(tau);
		}

		public void setVisitationTime(int lastVisitTime) {
			this.visitTime = lastVisitTime;
			setText(visitTime + "");
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.clearRect(0, 0, getWidth(), getHeight());

			double fraction = visitTime / tau;
			int boxHeight = (int) (getHeight() * fraction);

			g.setColor(Color.lightGray);
			g.fillRect(0, getHeight() - boxHeight, getWidth(), boxHeight);
			g.setColor(Color.black);
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

			setOpaque(false);
			super.paintComponent(g);
			setOpaque(true);
		}
	}

	private class ClusterCanvas extends Canvas {

		private static final long serialVersionUID = 1L;
		private final Color paint;

		public ClusterCanvas(Color paint) {
			this.paint = paint;
		}

		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setColor(paint);
			int diameter = Math.min(getWidth(), getHeight()) - 10;

			int startX = (getWidth() - diameter) / 2;
			int startY = 5;

			g2.fillOval(startX, startY, diameter, diameter);
			g2.setColor(Color.black);

			g2.setStroke(new BasicStroke(2));
			g2.drawOval(startX, startY, diameter, diameter);
		}
	}

}
