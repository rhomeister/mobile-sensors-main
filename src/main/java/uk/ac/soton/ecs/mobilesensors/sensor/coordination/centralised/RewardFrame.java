package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class RewardFrame<S extends State> {

	private final RewardFunction<S> function;
	private RewardTableModel tableModel;
	private S state;
	private List<MultiSensorAction> actions;
	private Planner<S> planner;
	private TransitionFunction<S> transitionFunction;

	public RewardFrame(TransitionFunction<S> transitionFunction,
			RewardFunction<S> function, S currentState, Planner<S> planner) {
		JFrame frame = new JFrame();
		this.function = function;
		this.planner = planner;
		this.transitionFunction = transitionFunction;

		frame.getContentPane().setLayout(new BorderLayout());

		tableModel = new RewardTableModel();
		JTable table = new JTable(tableModel);
		table.getColumnModel().getColumn(0).setPreferredWidth(250);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.getColumnModel().getColumn(2).setPreferredWidth(150);
		table.getColumnModel().getColumn(3).setPreferredWidth(150);

		table.getColumnModel().getColumn(1).setCellRenderer(
				new DoubleRenderer());
		table.getColumnModel().getColumn(2).setCellRenderer(
				new DoubleRenderer());
		table.getColumnModel().getColumn(3).setCellRenderer(
				new DoubleRenderer());

		table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
		frame.getContentPane().add(table.getTableHeader(),
				BorderLayout.PAGE_START);
		frame.getContentPane().add(table, BorderLayout.CENTER);

		setState(currentState);

		frame.setTitle("Rewards");
		frame.pack();
		frame.setVisible(true);

	}

	public void setState(S state) {
		this.state = state;
		this.actions = new ArrayList<MultiSensorAction>(transitionFunction
				.getActions(state));

		tableModel.fireTableDataChanged();
	}

	private class RewardTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private final String[] columnNames = { "Action", "Reward",
				"Value of Next State", "Total (Discounted)" };

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			MultiSensorAction action = actions.get(rowIndex);

			double reward = function.getReward(state, action);
			S transition = transitionFunction.transition(state, action)
					.keySet().iterator().next();
			double stateValue = planner.getValue(transition);

			if (columnIndex == 0) {
				return action.toString();
			}
			if (columnIndex == 1) {
				return reward;
			}
			if (columnIndex == 2) {
				return stateValue;
			}
			if (columnIndex == 3) {
				return stateValue
						* Math.pow(planner.getGamma(), action.getDuration())
						+ reward;
			}

			return null;
		}

		public int getRowCount() {
			return actions.size();
		}

		public int getColumnCount() {
			return 4;
		}
	}

	public class CustomTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component cell = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);

			MultiSensorAction action = actions.get(row);
			MultiSensorAction nextAction = planner.nextAction(state);

			if (action.equals(nextAction)) {
				cell.setBackground(Color.green.brighter());
			} else {
				cell.setBackground(Color.white);
			}

			return cell;
		}
	}

	static class DoubleRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		NumberFormat formatter;

		public DoubleRenderer() {
			super();
			setHorizontalAlignment(JLabel.RIGHT);
		}

		public void setValue(Object value) {
			if (formatter == null) {
				formatter = new DecimalFormat("0.00");
			}

			setText((value == null) ? "" : formatter.format(value));
		}
	}

}
