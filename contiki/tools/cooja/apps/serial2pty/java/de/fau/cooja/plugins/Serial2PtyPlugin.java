/*
 * Changed for use with Contiki 2.3, sky motes and binary data
 * transmission
 *
 * 2009-10-26
 * Niko Pollner, Chair for Computer Science 6, FAU Erlangen-Nuremberg
 *
 * 2012-03-01
 * Andre Frimberger:
 *   * Implemented pseudo terminals as a real serial device wrapper.
 *   * Allows using standard serial tools to communicate with virtual mote
 *
 *
 * Copyright (c) 2006, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * $Id: Serial2Pipe.java,v 1.1 2009/05/18 14:48:10 fros4943 Exp $
 */

package de.fau.cooja.plugins;

import java.awt.BorderLayout;
import java.io.*;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.ArrayList;

import javax.swing.*;

import org.apache.log4j.Logger;
import org.contikios.cooja.*;
import org.contikios.cooja.interfaces.SerialPort;
import org.jdom.Element;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

@ClassDescription("Serial 2 Pty")
@PluginType(PluginType.SIM_PLUGIN)
public class Serial2PtyPlugin extends VisPlugin {

	private static Logger logger = Logger.getLogger(Serial2PtyPlugin.class);
	private static final long serialVersionUID = 1L;
	private ArrayList<Serial2Pty> serialNodes = new ArrayList<>();
	private JTable motesTable = null;
	private JComboBox<Number> combo = new JComboBox<Number>();
	
	
	private final static String[] COLUMN_NAMES = new String[] { "Mote",
			"Port" };
  
	private final static int IDX_Mote = 0;
	private final static int IDX_Port = 1;

	public Serial2PtyPlugin(Simulation sim, Cooja gui) {
		super("Serial 2 Pty", gui, false);
		
		// Initialisation:
		if(serialNodes.size() != 0){
			serialNodes = new ArrayList<>();
		}
		for(Mote mote : sim.getMotes()){
			serialNodes.add(new Serial2Pty(mote,sim,gui));
		}
		
		motesTable = new JTable(model) {
			private static final long serialVersionUID = -4680013510092815210L;


		};
		motesTable.setFillsViewportHeight(true);
		combo.setEditable(true);

		motesTable.getColumnModel().getColumn(IDX_Mote)
				.setCellRenderer(new DefaultTableCellRenderer() { // TODO ????
							private static final long serialVersionUID = 4470088575039698508L;

							public void setValue(Object value) {
								setText(value.toString());
								return;
							}
						});
		motesTable.getColumnModel().getColumn(IDX_Port)
				.setCellRenderer(new DefaultTableCellRenderer() {
					private static final long serialVersionUID = -7170745293267593460L;

					public void setValue(Object value) {
						setText(value.toString());
						return;
					}
				});

		motesTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		motesTable.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);

		
		add(BorderLayout.CENTER, new JScrollPane(motesTable));

		model.fireTableDataChanged();
		setSize(400, 300);
	}


	final AbstractTableModel model = new AbstractTableModel() {
		private static final long serialVersionUID = 9101118401527171218L;

		public String getColumnName(int column) {
			if (column < 0 || column >= COLUMN_NAMES.length) {
				return "";
			}
			return COLUMN_NAMES[column];
		}

		public int getRowCount() {
			return serialNodes.size();
		}

		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		public Object getValueAt(int row, int column) {
			if (row < 0 || row >= serialNodes.size()) {
				return "";
			}
			if (column < 0 || column >= COLUMN_NAMES.length) {
				return "";
			}
			Serial2Pty mote = serialNodes.get(row);
			if (column == IDX_Mote) {
				return mote.getMoteID();
			}
			if (column == IDX_Port) {
				return mote.getSerialDeviceName();
			}
			return "";
		}

		public void setValueAt(Object value, int row, int column) {
		
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

		public Class<? extends Object> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	};

	public void closePlugin() {

		for(Serial2Pty pty : serialNodes){
			pty.closePlugin();
		}
	}

	public boolean setConfigXML(Collection<Element> configXML, boolean visAvailable) {
		return true;
	}

	public Collection<Element> getConfigXML() {
		return new Vector<Element>();
	}
}
