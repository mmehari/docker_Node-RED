package de.fau.cooja.plugins;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class SerialPortDiscoveryServer extends Thread {
    private static Logger _logger = Logger.getLogger(SerialPortDiscoveryServer.class);

    private ServerSocket serverSocket = null;

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(0);
            _logger.info("Creating server for serial2pty serial port discovery. Listening on port " + serverSocket.getLocalPort());
        } catch (IOException e) {
            _logger.fatal("No free port found for serial2pty service");
	    return;
        }

        while(true) {
            try {
                Socket client = serverSocket.accept();
                _logger.info("new Connection " + client + " established.");

                DataOutputStream cos = new DataOutputStream(client.getOutputStream());

                Set<Map.Entry<String, Integer>> serialPorts = Serial2PtyRegistry.getAllSerialPorts();
                for (Map.Entry<String, Integer> port : serialPorts) {
                    cos.writeBytes(port.getValue() + "\t" + port.getKey() + "\n");
                }
                cos.flush();

                client.shutdownOutput();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
