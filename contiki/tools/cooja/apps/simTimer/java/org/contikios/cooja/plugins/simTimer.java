/*
 * Copyright (c) 2009, Swedish Institute of Computer Science.
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

package org.contikios.cooja.plugins;

import java.util.Collection;
import java.util.Vector;

import org.contikios.cooja.*;
import org.jdom.Element;


@ClassDescription("simTimer")
@PluginType(PluginType.SIM_PLUGIN)
public class simTimer extends VisPlugin
{
	private static simTimerServer _simTimerServer = null;

	public simTimer(Simulation simulation, Cooja gui)
	{
		super("simTimer", gui, false);

		// start simulation timer server when first serial port is added.
		if (_simTimerServer == null)
		{
			_simTimerServer = new simTimerServer(simulation);
			_simTimerServer.start();
		}
		// Else, update the simulation object of the simulation timer server
		else
		{
			_simTimerServer.simulation = simulation;
		}
	}

	public void closePlugin()
	{
	}

	public Collection<Element> getConfigXML()
	{
		return new Vector<Element>();
	}

	public boolean setConfigXML(Collection<Element> configXML, boolean visAvailable)
	{
		return true;
	}
}
