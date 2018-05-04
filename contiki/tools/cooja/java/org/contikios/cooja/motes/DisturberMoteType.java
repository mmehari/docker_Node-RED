/*
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
 *
 */

package org.contikios.cooja.motes;

import java.awt.Container;
import java.util.Vector;
import java.util.Arrays;
import java.util.Base64;

import org.apache.log4j.Logger;

import org.contikios.cooja.AbstractionLevelDescription;
import org.contikios.cooja.COOJARadioPacket;
import org.contikios.cooja.ClassDescription;
import org.contikios.cooja.Mote;
import org.contikios.cooja.MoteTimeEvent;
import org.contikios.cooja.MoteType;
import org.contikios.cooja.RadioPacket;
import org.contikios.cooja.Simulation;
import org.contikios.cooja.MoteInterface;
import org.contikios.cooja.MoteType.MoteTypeCreationException;
import org.contikios.cooja.interfaces.ApplicationRadio;
import org.contikios.cooja.interfaces.Radio.RadioEvent;
import org.contikios.cooja.dialogs.SerialUI;

/**
 * Simple application-level mote that periodically transmits dummy radio packets
 * on all radio channels (-1), interfering all surrounding radio communication.
 * 
 * This mote type also implements the mote functionality ("mote software"),
 * and can be used as an example of implementing application-level mote.
 *
 * @see DisturberMote
 * @author Fredrik Osterlind, Thiemo Voigt
 */
@ClassDescription("Disturber mote")
@AbstractionLevelDescription("Application level")
public class DisturberMoteType extends AbstractApplicationMoteType {
  private static Logger logger = Logger.getLogger(DisturberMoteType.class);

  public DisturberMoteType() {
    super();
  }

  public DisturberMoteType(String identifier) {
    super(identifier);
    setDescription("Disturber Mote Type #" + identifier);
  }

  public boolean configureAndInit(Container parentContainer,
      Simulation simulation, boolean visAvailable) 
  throws MoteTypeCreationException {
    if (!super.configureAndInit(parentContainer, simulation, visAvailable)) {
      return false;
    }
    setDescription("Disturber Mote Type #" + getIdentifier());
    return true;
  }
  
  public Mote generateMote(Simulation simulation) {
    return new DisturberMote(this, simulation);
  }

  public static class DisturberMote extends AbstractApplicationMote {
    private ApplicationRadio radio = null;
    private SerialUI serialport = null;
    
    private final RadioPacket radioPacket = new COOJARadioPacket(new byte[] {
        0x01, 0x02, 0x03, 0x04, 0x05
    });
    private static long DELAY = 10 * Simulation.MILLISECOND;
    private static long DURATION = Simulation.MILLISECOND / 5;
    private static long PERIOD = DELAY + DURATION;
    private static byte DUTYCYCLE = (byte)((100 * DURATION) / PERIOD);
    
    private byte[] serialDataIN = new byte[256];
    private byte[] serialDataOUT = new byte[256];
    private byte i = 0, j;
    
    private final static byte PARAM_GET = 0;
    private final static byte PARAM_SET = 1;
    
    private final static short uid_PERIOD = (short) 45200;
    private final static short uid_DUTYCYCLE = (short) 45201;
    private final static short uid_RELOAD_MAC = (short) 28170;
    
    
    public DisturberMote() {
      super();
      serialport = getSerialPort();
    }
    public DisturberMote(MoteType moteType, Simulation simulation) {
      super(moteType, simulation);
      serialport = getSerialPort();
    }
    
    public SerialUI getSerialPort() {
        for (MoteInterface intf: getInterfaces().getInterfaces()) {
            if (intf instanceof SerialUI)
                return (SerialUI) intf;
        }
        return null;
    }
    
    public void execute(long time) {
      if (radio == null) {
        radio = (ApplicationRadio) getInterfaces().getRadio();
      }
      
      /* Start sending interfering traffic */
      /*logger.info("Sending radio packet on channel: " + radio.getChannel());*/
      radio.startTransmittingPacket(radioPacket, DURATION);
    }
    
    public void receivedPacket(RadioPacket p) {
      /* Ignore */
    }
    public void sentPacket(RadioPacket p) {
      /* Send another packet after a small pause */
      getSimulation().scheduleEvent(new MoteTimeEvent(this, 0) {
        public void execute(long t) {
          /*logger.info("Sending another radio packet on channel: " + radio.getChannel());*/
          radio.startTransmittingPacket(radioPacket, DURATION);
        }
      }, getSimulation().getSimulationTime() + DELAY);
    }
    
    public String toString() {
      return "Disturber " + getID();
    }
    
    @Override
    public void writeByte(byte b) {
      if (b != '\n') {
        serialDataIN[i] = b;
        i++;
      } else {
        // Check serial header
        if (check_serial_header(serialDataIN)) {
          byte[] encDataIN = Arrays.copyOfRange(serialDataIN, 10, serialDataIN[1] - 1);
          byte[] decDataIN = Base64.getDecoder().decode(encDataIN);
          
          byte opcode = decDataIN[0];
          if (opcode == PARAM_GET || opcode == PARAM_SET) {
            byte num_param = decDataIN[1];
            int seq_nr = (int)(decDataIN[2] & 0xFF | (decDataIN[3] & 0xFF) << 8 | (decDataIN[4] & 0xFF) << 16 | (decDataIN[5] & 0xFF) << 24);
          
            // Calculate decoded data out length
            byte decDataOUT_len = 6;
            for (j = 0; j < num_param; j++) {
              if (opcode == PARAM_GET) {
                byte len = decDataIN[6 + 4 * j + 3];
                decDataOUT_len = (byte)(decDataOUT_len + 4 + len);
              } else if (opcode == PARAM_SET) {
                decDataOUT_len = (byte)(decDataOUT_len + 4 + 1);
              }
            }
            
            // Create decoded data out buffer and start filling data
            byte[] decDataOUT = new byte[decDataOUT_len];
            System.arraycopy(decDataIN, 0, decDataOUT, 0, 6);
            
            if (opcode == PARAM_GET) {
              byte offset_IN = 6;
              byte offset_OUT = 6;
              for (j = 0; j < num_param; j++) {
                short uid = (short)((decDataIN[offset_IN] & 0xFF) | (decDataIN[offset_IN + 1] & 0xFF) << 8);
                byte type = decDataIN[offset_IN + 2];
                byte len = decDataIN[offset_IN + 3];
                
                // Store uid, type and len parameters
                System.arraycopy(decDataIN, offset_IN, decDataOUT, offset_OUT, 3);
                decDataOUT[offset_OUT + 3] = len;
                
                // Interference PERIOD
                if (uid == uid_PERIOD) {
                  decDataOUT[offset_OUT + 4] = (byte)(PERIOD & 0xFF);
                  decDataOUT[offset_OUT + 5] = (byte)((PERIOD >> 8) & 0xFF);
                }
                // Interference DUTYCYCLE
                else if (uid == uid_DUTYCYCLE) {
                  decDataOUT[offset_OUT + 4] = DUTYCYCLE;
                } else {
                  send_error_message(String.format("GET PARAM %d:0x%04X not found", j + 1, uid));
                  break;
                }
                
                offset_IN = (byte)(offset_IN + 4);
                offset_OUT = (byte)(offset_OUT + 4 + len);
              }
              
              // PARAM_GET is successful. Send a positive response
              if (j == num_param)
                send_serial_response(decDataOUT);
            } else if (opcode == PARAM_SET) {
              byte offset_IN = 6;
              byte offset_OUT = 6;
              for (j = 0; j < num_param; j++) {
                short uid = (short)((decDataIN[offset_IN] & 0xFF) | (decDataIN[offset_IN + 1] & 0xFF) << 8);
                byte type = decDataIN[offset_IN + 2];
                byte len = decDataIN[offset_IN + 3];
                
                // Interference PERIOD
                if (uid == uid_PERIOD) {
                  PERIOD = (long)((decDataIN[offset_IN + 4] & 0xFF) | (decDataIN[offset_IN + 5] & 0xFF) << 8);
                }
                // Interference DUTYCYCLE
                else if (uid == uid_DUTYCYCLE) {
                  DUTYCYCLE = decDataIN[offset_IN + 4];
                }
                // Reloading mac request
                else if (uid == uid_RELOAD_MAC);
                else {
                  send_error_message(String.format("SET PARAM %d:0x%04X not found", j + 1, uid));
                  break;
                }
                
                // Store uid, type, len and value parameters
                System.arraycopy(decDataIN, offset_IN, decDataOUT, offset_OUT, 3);
                decDataOUT[offset_OUT + 3] = 1;
                decDataOUT[offset_OUT + 4] = 0x00;
                
                // Update offset parameters for next iteration
                offset_IN = (byte)(offset_IN + 4 + len);
                offset_OUT = (byte)(offset_OUT + 4 + 1);
              }
              
              // PARAM_SET is successful. Update disturber parameters and send a positive response
              if (j == num_param) {
                // Update DELAY and DURATION parameters
                DURATION = (long)(DUTYCYCLE * PERIOD) / 100;
                DELAY = PERIOD - DURATION;
                
                // Send response message
                send_serial_response(decDataOUT);
              }
            }
          } else
            send_error_message(String.format("ERROR: wrong opcode %d", opcode));
        } else
          send_error_message("ERROR: wrong serial header");
	
        // Reset serial data IN buffer
        Arrays.fill(serialDataIN, (byte) 0);
        i = 0;
      }
    }
    
    public boolean check_serial_header(byte[] data) {
      int j;
      for (j = 2; j < 10; j++) {
        if (data[j] != 'F')
          return false;
      }
      return true;
    }
    
    public void send_error_message(String error_msg) {
      byte[] error_msg_byte_array = error_msg.getBytes();
      byte[] decDataOUT = new byte[10 + error_msg_byte_array.length];
      
      decDataOUT[0] = 127;
      decDataOUT[1] = 1;
      decDataOUT[2] = 0;
      decDataOUT[3] = 0;
      decDataOUT[4] = 0;
      decDataOUT[5] = 0;
      decDataOUT[6] = (byte) 255;
      decDataOUT[7] = (byte) 255;
      decDataOUT[8] = (byte) 255;
      decDataOUT[9] = (byte)(error_msg_byte_array.length);
      System.arraycopy(error_msg_byte_array, 0, decDataOUT, 10, error_msg_byte_array.length);
      
      send_serial_response(decDataOUT);
    }
    
    public void send_serial_response(byte[] decDataOUT) {
      // Encode data out
      byte[] encDataOUT = Base64.getEncoder().encode(decDataOUT);
      
      // Construct serial data out packet [Decoded data length, Encoded data length, Serial header byte, Encoded data out]
      serialDataOUT[0] = (byte)(decDataOUT.length + 10);
      serialDataOUT[1] = (byte)(encDataOUT.length + 10);
      System.arraycopy(serialDataIN, 2, serialDataOUT, 2, 8);
      System.arraycopy(encDataOUT, 0, serialDataOUT, 10, encDataOUT.length);
      
      int j;
      for (j = 0; j < encDataOUT.length + 10; j++) {
        serialport.dataReceived(serialDataOUT[j]);
      }
    }
  }
}
