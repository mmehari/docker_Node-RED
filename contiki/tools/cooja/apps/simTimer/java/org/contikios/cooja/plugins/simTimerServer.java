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

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.contikios.cooja.*;

public class simTimerServer extends Thread
{
	private static Logger logger = Logger.getLogger(simTimer.class);

	public Simulation simulation;
	private ServerSocket serverSocket = null;
	private long simDelay_us;

	public simTimerServer(Simulation simulation)
	{
		this.simulation = simulation;
	}

	@Override
	public void run()
	{
		try
		{
			serverSocket = new ServerSocket(0);
			logger.info("Creating server for simTimer service. Listening on port " + serverSocket.getLocalPort());
		}
		catch (IOException e)
		{
			logger.fatal("No free port found for simTimer service");
			return;
		}

		while(true)
		{
			try
			{
				Socket client = serverSocket.accept();
				logger.info("new simTimer Connection " + client + " established");

				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter   out = new PrintWriter(client.getOutputStream(), true);

				simDelay_us = Long.parseLong(in.readLine(), 10);
				TimeEvent simTimerEvent = new TimeEvent(0)
				{
					public void execute(long t)
					{
						logger.info("simTimer Finished @ " + t + " usec");
						out.println("\n");

						// Close connection
						try
						{
							client.shutdownOutput();
							client.close();
						}
						catch (IOException e)
						{
							logger.info("simTimer shutdown exception");
							e.printStackTrace();
						}
					}
				};

				if (simTimerEvent.isScheduled())
				{
					logger.info("Removing previously scheduled simTimer");
					simTimerEvent.remove();
				}
				simulation.scheduleEvent(simTimerEvent, simulation.getSimulationTime() + simDelay_us);
			}
			catch (IOException e)
			{
				logger.info("simTimer accept exception");
				e.printStackTrace();
			}
		}
	}
}
