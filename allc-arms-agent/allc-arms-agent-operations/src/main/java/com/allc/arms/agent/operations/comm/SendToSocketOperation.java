package com.allc.arms.agent.operations.comm;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;

import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.core.receiver.ReceiverPipe;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class SendToSocketOperation extends AbstractOperation{
	
	private Logger logger = Logger.getLogger(SendToSocketOperation.class);
	protected String storeCode;

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		return false;
	}
	
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		//logger.info("Iniciando SendToSocketOperation...");
		try {
			//liberamos pipeServer
			ReceiverPipe.waitAvailable = false;
			String msg = (String) frame.getBody().get(0);
			//logger.info("Mensaje recibido por pipe: " + msg);
			String trama = Util.addLengthStartOfString(msg, properties.getInt("serverSocket.quantityBytesLength"))
					.toString();
			//logger.info("Trama a enviar: "+trama);
			
			DatagramSocket clientSocket = new DatagramSocket();
			List ips = properties.getList("serverSocketUDP.ip");
			List ports = properties.getList("serverSocketUDP.port");
			if(ips != null && ports != null && ips.size() == ports.size()){
				//logger.info("UDP cantidad conexiones:" + ips.size());
				for(int i = 0; i < ips.size(); i++){
					String ip = (String) ips.get(i);
					int port = Integer.valueOf((String) ports.get(i)).intValue();
					InetAddress ipAddress = InetAddress.getByName(ip);
					byte[] datosCatalina = msg.getBytes();	
					//logger.info("Conectando UDP a IP: "+ipAddress+" - Port:"+ port);
					DatagramPacket sendPacket = new DatagramPacket(datosCatalina, datosCatalina.length, ipAddress, port);
					//logger.info("Enviando mensaje UDP");
					clientSocket.send(sendPacket);
					//logger.info("Mensaje UDP enviado con éxito");
					
				}
			}			
			clientSocket.close();
//			pipe.sendData("F", "CC", trama);
		} catch (Exception e){
			logger.error(e.getMessage(), e);
		}
		
		return false;
	}

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
