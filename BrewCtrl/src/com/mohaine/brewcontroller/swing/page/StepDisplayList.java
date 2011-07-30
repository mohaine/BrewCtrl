/*
    Copyright 2009-2011 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

package com.mohaine.brewcontroller.swing.page;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.Controller;
import com.mohaine.brewcontroller.UnitConversion;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEvent;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEventHandler;
import com.mohaine.brewcontroller.event.StepModifyEvent;
import com.mohaine.brewcontroller.event.StepModifyEventHandler;
import com.mohaine.brewcontroller.event.StepsModifyEvent;
import com.mohaine.brewcontroller.event.StepsModifyEventHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.bus.EventBus;

public class StepDisplayList extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final NumberFormat nf = new DecimalFormat("0.#");

	private Controller controller;
	private JTable table = new JTable();
	private StepListTableModel tableModel = new StepListTableModel();
	private EventBus eventBus;
	private ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
	private UnitConversion conversion;

	@Inject
	public StepDisplayList(Controller controllerp, EventBus eventBusp, UnitConversion conversion) {
		super();
		this.eventBus = eventBusp;
		this.controller = controllerp;
		this.conversion = conversion;

		setPreferredSize(new Dimension(600, 200));

		setLayout(new BorderLayout());
		table.setModel(tableModel);
		table.setRowHeight(25);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);

		add(new JScrollPane(table), BorderLayout.CENTER);
		updateSteps();

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int[] selectedRows = table.getSelectedRows();
					if (selectedRows.length > 0) {
						int row = selectedRows[0];
						if (row > -1 && row < tableModel.steps.size()) {
							HeaterStep newSelection = tableModel.steps.get(row);
							if (newSelection != controller.getSelectedStep()) {
								controller.setSelectedStep(newSelection);
							}
						}
					}
				}

			}
		});

	}

	@Override
	public void addNotify() {
		super.addNotify();

		removeHandlers();
		handlers.add(eventBus.addHandler(StepsModifyEvent.getType(), new StepsModifyEventHandler() {

			@Override
			public void onStepsChange() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateSteps();
					}
				});

			}
		}));

		handlers.add(eventBus.addHandler(StepModifyEvent.getType(), new StepModifyEventHandler() {
			@Override
			public void onStepChange(final HeaterStep step) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						modifedStep(step);
					}
				});
			}
		}));
		handlers.add(eventBus.addHandler(ChangeSelectedStepEvent.getType(), new ChangeSelectedStepEventHandler() {
			@Override
			public void onStepChange(final HeaterStep step) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateSelectedRow(step);
					}
				});
			}
		}));
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		removeHandlers();
	}

	private void removeHandlers() {
		for (HandlerRegistration reg : handlers) {
			reg.removeHandler();
		}
		handlers.clear();
	}

	private void modifedStep(HeaterStep modifedStep) {

		List<HeaterStep> steps = tableModel.steps;
		int row = 0;
		for (HeaterStep heaterStep : steps) {
			if (modifedStep == heaterStep) {
				tableModel.fireTableRowsUpdated(row, row);
				break;
			}
			row++;
		}

	}

	private void updateSteps() {
		List<HeaterStep> steps = controller.getSteps();
		tableModel.steps = new ArrayList<HeaterStep>(steps);
		tableModel.fireTableDataChanged();

		HeaterStep selectedStep = controller.getSelectedStep();
		updateSelectedRow(selectedStep);

	}

	private void updateSelectedRow(HeaterStep selectedStep) {
		int row = 0;
		for (HeaterStep heaterStep : tableModel.steps) {
			if (heaterStep == selectedStep) {
				table.getSelectionModel().setSelectionInterval(row, row);

			}
			row++;
		}
	}

	class StepListTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		List<HeaterStep> steps;

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return steps != null ? steps.size() : 0;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Name";
			case 1:
				return "HLT Temp";
			case 2:
				return "Tun Temp";
			case 3:
				return "Time Remaining";
			case 4:
				return "Time";
			}

			return null;
		}

		@Override
		public Object getValueAt(int row, int column) {

			if (steps != null && row < steps.size()) {
				HeaterStep step = steps.get(row);
				switch (column) {
				case 0:
					return step.getName();
				case 1:
					return nf.format(conversion.getTempDisplayConveter().convertFrom(step.getHltTemp()));
				case 2:
					return nf.format(conversion.getTempDisplayConveter().convertFrom(step.getTunTemp()));
				case 3:
					return HeaterStep.timeToMinutes(step.getTimeRemaining(),"forever");
				case 4:
					return HeaterStep.timeToMinutes(step.getTotalCompletedTime(),"");
				}

			}

			return null;
		}

	}

}
