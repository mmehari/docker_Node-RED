/*
 * Copyright (c) 2011, Swedish Institute of Computer Science.
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
 */

package org.contikios.whipp;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.jdom.Element;

import org.contikios.cooja.Simulation;
import org.contikios.cooja.interfaces.DirectionalAntennaRadio;
import org.contikios.cooja.interfaces.Radio;
import org.contikios.cooja.radiomediums.AbstractRadioMedium;
import org.contikios.cooja.util.ScnObservable;

import statistics.GaussianWrapper;

import wsdl.Michael;
import wsdl.MichaelService;

/**
 * This is a modification of the MRM radio model for the WHIPP radio model
 *
 * @Modified Michael Tetemke Mehari
 */
public class ChannelModel
{
	private static Logger logger = Logger.getLogger(ChannelModel.class);

	private static final double C = 299792458; /* m/s */

	private Hashtable<Parameter,Object> parametersDefaults = new Hashtable<Parameter,Object>();
	private Hashtable<Parameter,Object> parameters = new Hashtable<Parameter,Object>();

	/* Log mode: visualize signal components */
	private boolean logMode = false;
	private StringBuilder logInfo = null;

	private Simulation simulation;

	MichaelService service = new MichaelService();
	Michael server = service.getMichaelPort();

	/**
	 * Notifies observers when this channel model has changed settings.
	 */
	private ScnObservable settingsObservable = new ScnObservable();
	public enum Parameter
	{
		snr_threshold,
		bg_noise_mean,
		bg_noise_var,
		system_gain_mean,
		system_gain_var,
		frequency,
		tx_power,
		tx_with_gain,
		rx_sensitivity,
		rx_with_gain,
		obstacles_path;

		public static Object getDefaultValue(Parameter p)
		{
			switch (p)
			{
			case snr_threshold:
				return new Double(6);
			case bg_noise_mean:
				return new Double(AbstractRadioMedium.SS_NOTHING);
			case bg_noise_var:
				return new Double(1);
			case system_gain_mean:
				return new Double(0);
			case system_gain_var:
				return new Double(4);
			case frequency: /* MHz */
				return new Double(2400);
			case tx_power:
				return new Double(1.5);
			case tx_with_gain:
				return new Boolean(false);
			case rx_sensitivity:
				return new Double(-100);
			case rx_with_gain:
				return new Boolean(false);
			case obstacles_path:
				return new String("/path/to/obstacles/file");
			}
			throw new RuntimeException("Unknown default value: " + p);
		}

		public static Parameter fromString(String name)
		{
			/* Backwards compatability */
			if (name.equals("snr_threshold"))
			{
				return snr_threshold;
			}
			else if (name.equals("bg_noise_mean"))
			{
				return bg_noise_mean;
			}
			else if (name.equals("bg_noise_var"))
			{
				return bg_noise_var;
			}
			else if (name.equals("system_gain_mean"))
			{
				return system_gain_mean;
			}
			else if (name.equals("system_gain_var"))
			{
				return system_gain_var;
			}
			else if (name.equals("tx_power"))
			{
				return tx_power;
			}
			else if (name.equals("rx_sensitivity"))
			{
				return rx_sensitivity;
			}
			else if (name.equals("obstacles_path"))
			{
				return obstacles_path;
			}
			return null;
		}

		public static String getDescription(Parameter p)
		{
			switch (p)
			{
			case snr_threshold:
				return "SNR reception threshold (dB)";
			case bg_noise_mean:
				return "Background noise mean (dBm)";
			case bg_noise_var:
				return "Background noise variance (dB)";
			case system_gain_mean:
				return "Extra system gain mean (dB)";
			case system_gain_var:
				return "Extra system gain variance (dB)";
			case frequency:
				return "Frequency (MHz)";
			case tx_power:
				return "Default transmitter output power (dBm)";
			case tx_with_gain:
				return "Directional antennas: with TX gain";
			case rx_sensitivity:
				return "Receiver sensitivity (dBm)";
			case rx_with_gain:
				return "Directional antennas: with RX gain";
			case obstacles_path:
				return "Obstacles file path";
			}
			throw new RuntimeException("Unknown decrption: " + p);
		}
	}

	public ChannelModel(Simulation simulation)
	{
		this.simulation = simulation;

		/* Default values */
		for (Parameter p: Parameter.values())
		{
			parameters.put(p, Parameter.getDefaultValue(p));
		}

		parametersDefaults = (Hashtable<Parameter,Object>) parameters.clone();
	}

	/**
	 * Adds a settings observer to this channel model.
	 * Every time the settings are changed all observers
	 * will be notified.
	 *
	 * @param obs New observer
	 */
	public void addSettingsObserver(Observer obs)
	{
		settingsObservable.addObserver(obs);
	}

	/**
	 * Deletes an earlier registered setting observer.
	 *
	 * @param osb
	 *          Earlier registered observer
	 */
	public void deleteSettingsObserver(Observer obs)
	{
		settingsObservable.deleteObserver(obs);
	}

	/**
	 * Returns a parameter value
	 *
	 * @param identifier Parameter identifier
	 * @return Current parameter value
	 */
	public Object getParameterValue(Parameter id)
	{
		Object value = parameters.get(id);
		if (value == null)
		{
			logger.fatal("No parameter with id:" + id + ", aborting");
			return null;
		}
		return value;
	}

	/**
	 * Returns a double parameter value
	 *
	 * @param identifier Parameter identifier
	 * @return Current parameter value
	 */
	public double getParameterDoubleValue(Parameter id)
	{
		return ((Double) getParameterValue(id)).doubleValue();
	}

	/**
	 * Returns an integer parameter value
	 *
	 * @param identifier Parameter identifier
	 * @return Current parameter value
	 */
	public int getParameterIntegerValue(Parameter id)
	{
		return ((Integer) getParameterValue(id)).intValue();
	}

	/**
	 * Returns a boolean parameter value
	 *
	 * @param identifier Parameter identifier
	 * @return Current parameter value
	 */
	public boolean getParameterBooleanValue(Parameter id)
	{
		return ((Boolean) getParameterValue(id)).booleanValue();
	}

	/**
	 * Returns a string parameter value
	 *
	 * @param identifier Parameter identifier
	 * @return Current parameter value
	 */
	public String getParameterStringValue(Parameter id)
	{
		return ((String) getParameterValue(id)).toString();
	}

	/**
	 * Saves a new parameter value
	 *
	 * @param id Parameter identifier
	 * @param newValue New parameter value
	 */
	public void setParameterValue(Parameter id, Object newValue)
	{
		if (!parameters.containsKey(id))
		{
			logger.fatal("No parameter with id:" + id + ", aborting");
			return;
		}
		parameters.put(id, newValue);

		settingsObservable.setChangedAndNotify();
	}

	/**
	 * Calculates and returns the received signal strength (dBm) of a signal sent
	 * from the given source position to the given destination position as a
	 * random variable. This method uses current parameters such as transmitted
	 * power, obstacles, overall system loss etc.
	 *
	 * @param sourceX
	 *          Source position X
	 * @param sourceY
	 *          Source position Y
	 * @param destX
	 *          Destination position X
	 * @param destY
	 *          Destination position Y
	 * @return Received signal strength (dBm) random variable. The first value is
	 *         the random variable mean, and the second is the variance.
	 */
	public double[] getReceivedSignalStrength(TxPair txPair)
	{
		int srcX = (int) (txPair.getFromX() * 100);
		int srcY = (int) (txPair.getFromY() * 100);
		int srcZ = 300;
		int dstX = (int) (txPair.getToX() * 100);
		int dstY = (int) (txPair.getToY() * 100);
		int dstZ = 300;
		int outputPower = (int) txPair.getTxPower();	// EIRP (emitted power)of transmitter in dBm
		double PL_exp = 2.0;				// path loss exponent of path loss model
		double PL_intrpt = 40.0;			// path loss intercept of path loss model (in dB) (i.e., the path loss at a distance of 1m in line of sight conditions)
		double drywall_loss = 2.0;			// loss of 'layered drywall' (in dB)
		double concrete_loss = 8.0;			// loss of 'concrete wall' (in dB)
		String obstacles_str = getParameterStringValue(Parameter.obstacles_path);

		double RSSI = server.calculate(dstX, dstY, dstZ, srcX, srcY, srcZ, outputPower, PL_exp, PL_intrpt, drywall_loss, concrete_loss, obstacles_str);
		double systemGain = getParameterDoubleValue(Parameter.system_gain_mean);

		double transmitterGain = 0;
		if (getParameterBooleanValue(Parameter.tx_with_gain))
		{
			transmitterGain = txPair.getTxGain();
		}

		double receivedPower = RSSI + systemGain + transmitterGain;
		double accumulatedVariance = getParameterDoubleValue(Parameter.system_gain_var);

		return new double[] {receivedPower, accumulatedVariance};
	}

	/**
	 * Calculates and returns the signal to noise ratio (dB) of a signal sent from
	 * the given source position to the given destination position as a random
	 * variable. This method uses current parameters such as transmitted power,
	 * obstacles, overall system loss etc.
	 *
	 * @param sourceX Source position X
	 * @param sourceY Source position Y
	 * @param destX Destination position X
	 * @param destY Destination position Y
	 * @return Received SNR (dB) random variable:
	 * The first value in the array is the random variable mean.
	 * The second is the variance.
	 * The third value is the received signal strength which may be used in comparison with interference etc.
	 */
	public double[] getSINR(TxPair txPair, double interference)
	{
		/* TODO Cache values: called repeatedly with noise sources. */

		// Calculate received signal strength
		double[] signalStrength = getReceivedSignalStrength(txPair);
		double[] snrData = new double[] { signalStrength[0], signalStrength[1], signalStrength[0] };

		// Add antenna gain
		if (getParameterBooleanValue(Parameter.rx_with_gain))
		{
			snrData[0] += txPair.getRxGain();
		}

		double noiseVariance = getParameterDoubleValue(Parameter.bg_noise_var);
		double noiseMean = getParameterDoubleValue(Parameter.bg_noise_mean);

		if (interference > noiseMean)
		{
			noiseMean = interference;
		}

		// Applying noise to calculate SNR
		snrData[0] -= noiseMean;
		snrData[1] += noiseVariance;

		if (logMode)
		{
			logInfo.append("\nReceived SNR: " + String.format("%2.3f", snrData[0]) + " dB (variance " + snrData[1] + ")\n");
		}
		return snrData;
	}


	/**
	 * Calculates probability that a receiver at given destination receives
	 * a packet from a transmitter at given source.
	 * This method uses current parameters such as transmitted power,
	 * obstacles, overall system loss, packet size etc.
	 *
	 * TODO Packet size
	 * TODO External interference/Background noise
	 *
	 * @param sourceX Source position X
	 * @param sourceY Source position Y
	 * @param destX Destination position X
	 * @param destY Destination position Y
	 * @param interference Current interference at destination (dBm)
	 * @return [Probability of reception, signal strength at destination]
	 */
	public double[] getProbability(TxPair txPair, double interference)
	{
		double[] snrData = getSINR(txPair, interference);
		double snrMean = snrData[0];
		double snrVariance = snrData[1];
		double signalStrength = snrData[2];
		double threshold = getParameterDoubleValue(Parameter.snr_threshold);
		double rxSensitivity = getParameterDoubleValue(Parameter.rx_sensitivity);

		// Check signal strength against receiver sensitivity and interference
		if (rxSensitivity > signalStrength - snrMean && threshold < rxSensitivity + snrMean - signalStrength)
		{
			if (logMode)
			{
				logInfo.append("Weak signal: increasing threshold\n");
			}

			// Keeping snr variance but increasing theshold to sensitivity
			threshold = rxSensitivity + snrMean - signalStrength;
		}

		// If not random varianble, probability is either 1 or 0
		if (snrVariance == 0)
		{
			return new double[]
			       {
			           threshold - snrMean > 0 ? 0:1, signalStrength
			       };
		}
		double snrStdDev = Math.sqrt(snrVariance);


		// "Missing" signal strength in order to receive packet is probability that
		// random variable with mean snrMean and standard deviance snrStdDev is above
		// current threshold.

		// (Using error algorithm method, much faster than taylor approximation!)
		double probReception = 1 - GaussianWrapper.cdfErrorAlgo(threshold, snrMean, snrStdDev);

		if (logMode)
		{
			logInfo.append("Reception probability: " + String.format("%1.1f%%", 100*probReception) + "\n");
		}

		// Returns probabilities
		return new double[] { probReception, signalStrength };
	}

	/**
	 * Returns XML elements representing the current configuration.
	 *
	 * @see #setConfigXML(Collection)
	 * @return XML element collection
	 */
	public Collection<Element> getConfigXML()
	{
		ArrayList<Element> config = new ArrayList<Element>();
		Element element;

		Enumeration<Parameter> paramEnum = parameters.keys();
		while (paramEnum.hasMoreElements())
		{
			Parameter p = (Parameter) paramEnum.nextElement();
			element = new Element(p.toString());
			if (parametersDefaults.get(p).equals(parameters.get(p)))
			{
				/* Default value */
				continue;
			}
			element.setAttribute("value", parameters.get(p).toString());
			config.add(element);
		}

		return config;
	}

	/**
	 * Sets the configuration depending on the given XML elements.
	 *
	 * @see #getConfigXML()
	 * @param configXML
	 *          Config XML elements
	 * @return True if config was set successfully, false otherwise
	 */
	public boolean setConfigXML(Collection<Element> configXML)
	{
		for (Element element : configXML)
		{
			/* Parameter values */
			String name = element.getName();
			String value;
			Parameter param = null;

			if (name.equals("wavelength"))
			{
				/* Backwards compatability: ignored parameters */
				value = element.getAttributeValue("value");
				if (value == null)
				{
					value = element.getText();
				}
//				private static final double C = 299792458; /* m/s */
				double frequency = C/Double.parseDouble(value);
				frequency /= 1000000.0; /* mhz */
				parameters.put(Parameter.frequency, frequency); /* mhz */

				logger.warn("WHIPP parameter converted from wavelength to frequency: " + String.format("%1.1f MHz", frequency));
				continue;
			}
			else if (name.equals("tx_antenna_gain") || name.equals("rx_antenna_gain"))
			{
				logger.warn("WHIPP parameter \"" + name + "\" was removed");
				continue;
			}
			else if (Parameter.fromString(name) != null)
			{
				/* Backwards compatability: renamed parameters */
				param = Parameter.fromString(name);
			}
			else
			{
				param = Parameter.valueOf(name);
			}

			value = element.getAttributeValue("value");
			if (value == null || value.isEmpty())
			{
				/* Backwards compatability: renamed parameters */
				value = element.getText();
			}

			Class<?> paramClass = parameters.get(param).getClass();
			if (paramClass == Double.class)
			{
				parameters.put(param, new Double(Double.parseDouble(value)));
			}
			else if (paramClass == Boolean.class)
			{
				parameters.put(param, Boolean.parseBoolean(value));
			}
			else if (paramClass == Integer.class)
			{
				parameters.put(param, Integer.parseInt(value));
			}
			else if (name.equals("obstacles_path") && paramClass == String.class)
			{
				String obstacles_str = "";
				try
				{
					obstacles_str = new String(Files.readAllBytes(Paths.get(value)));
				}
				catch (IOException e)
				{
					System.out.println(e);
				}
				parameters.put(param, obstacles_str);
			}
			else
			{
				logger.fatal("Unsupported class type: " + paramClass);
			}
		}
		settingsObservable.setChangedAndNotify();
		return true;
	}

	public static abstract class TxPair
	{
		public abstract double getFromX();
		public abstract double getFromY();
		public abstract double getToX();
		public abstract double getToY();
		public abstract double getTxPower();

		public double getDistance()
		{
			double w = getFromX() - getToX();
			double h = getFromY() - getToY();
			return Math.sqrt(w*w+h*h);
		}

		/**
		 * @return Radians
		 */
		public double getAngle()
		{
			return Math.atan2(getToY()-getFromY(), getToX()-getFromX());
		}
		public Point2D getFrom()
		{
			return new Point2D.Double(getFromX(), getFromY());
		}
		public Point2D getTo()
		{
			return new Point2D.Double(getToX(), getToY());
		}

		/**
		 * @return Relative transmitter gain (zero for omnidirectional radios)
		 */
		public abstract double getTxGain();

		/**
		 * @return Relative receiver gain (zero for omnidirectional radios)
		 */
		public abstract double getRxGain();
	}
	public static abstract class RadioPair extends TxPair
	{
		public abstract Radio getFromRadio();
		public abstract Radio getToRadio();

		public double getDistance()
		{
			double w = getFromX() - getToX();
			double h = getFromY() - getToY();
			return Math.sqrt(w*w+h*h);
		}
		public double getFromX()
		{
			return getFromRadio().getPosition().getXCoordinate();
		}
		public double getFromY()
		{
			return getFromRadio().getPosition().getYCoordinate();
		}
		public double getToX()
		{
			return getToRadio().getPosition().getXCoordinate();
		}
		public double getToY()
		{
			return getToRadio().getPosition().getYCoordinate();
		}
		public double getTxPower()
		{
			return getFromRadio().getCurrentOutputPower();
		}
		public double getTxGain()
		{
			if (!(getFromRadio() instanceof DirectionalAntennaRadio))
			{
				return 0;
			}
			DirectionalAntennaRadio r = (DirectionalAntennaRadio)getFromRadio();
			double txGain = r.getRelativeGain(r.getDirection() + getAngle(), getAngle());
			//logger.debug("tx gain: " + txGain + " (angle " + String.format("%1.1f", Math.toDegrees(r.getDirection() + getAngle())) + ")");
			return txGain;
		}
		public double getRxGain()
		{
			if (!(getToRadio() instanceof DirectionalAntennaRadio))
			{
				return 0;
			}
			DirectionalAntennaRadio r = (DirectionalAntennaRadio)getFromRadio();
			double txGain = r.getRelativeGain(r.getDirection() + getAngle() + Math.PI, getDistance());
			//logger.debug("rx gain: " + txGain + " (angle " + String.format("%1.1f", Math.toDegrees(r.getDirection() + getAngle() + Math.PI)) + ")");
			return txGain;
		}
	}

}
