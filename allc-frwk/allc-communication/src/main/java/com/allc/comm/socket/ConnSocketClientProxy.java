package com.allc.comm.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

public class ConnSocketClientProxy implements ConnSocketClientI{
	private Logger logger = Logger.getLogger(ConnSocketClientProxy.class);
	private static int selectServer = 2; 
	private String ipServer2;	
	private int portServer2;
	private ConnSocketClient connSocketClient;

	public ConnSocketClientProxy() {
		connSocketClient = new ConnSocketClient();
	}
	
	/*************************************************************************************/
	
	public boolean writeDataSocket(String data) {
		// TODO Auto-generated method stub
		return connSocketClient.writeDataSocket(data);
	}
        
        public synchronized boolean writeByteArraySocket(byte[] data) {
		return connSocketClient.writeByteArraySocket(data);
	}

	public boolean timeOutSocket() {
		// TODO Auto-generated method stub
		return connSocketClient.timeOutSocket();
	}

	public int readLengthDataSocket() {
		// TODO Auto-generated method stub
		return connSocketClient.readLengthDataSocket();
	}

	public String readDataSocket(int quantityBytesToRead) {
		// TODO Auto-generated method stub
		return connSocketClient.readDataSocket(quantityBytesToRead);
	}

	public boolean connectSocket() {
		try {
			logger.info("Entro a conectSocket proxyClass, selectServer:"+selectServer);
			if(selectServer == 2) {
				logger.info("Entro a Server 2, ip:"+getIpServer2());
				selectServer = 1;
				setClient(new Socket(getIpServer2(), getPortServer2()));
			}else{
				logger.info("Entro a Server 1, ip:"+getIpServer());
				selectServer = 2;
				setClient(new Socket(getIpServer(), getPortServer()));
			}
			setDos(new DataOutputStream(getClient().getOutputStream()));
			setDis(new DataInputStream(getClient().getInputStream()));
			setConnected(true);
			return true;
		} catch (Exception e) {
			logger.info("ERROR ConnectSocket.");
			logger.error(e.getMessage(), e);
			setClient(null);
			setDis(null);
			setDos(null);
			setConnected(false);
			return false;
		}
	}

	public boolean connectSocketUsingRetries() {
		boolean result = false;
		try {
			for (int i = 0; i < getRetries(); i++) {
				if (connectSocket()) {
					logger.info("connectSocketUsingRetries CONECTO, intento i:"+i);
					result = true;
					break;
				} else {
					Thread.sleep(3000);
					logger.info("connectSocketUsingRetries NO CONECTO, intento i:"+i);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	public void closeConnection() {
		// TODO Auto-generated method stub
		connSocketClient.closeConnection();
	}

	public void setQuantityBytesLength(int quantityBytesLength) {
		// TODO Auto-generated method stub
		connSocketClient.setQuantityBytesLength(quantityBytesLength);
	}
	/*************************************************************************************/
		
	public String getIpServer2(){
		return this.ipServer2;
	}

	public void setIpServer2(String ipServer){
		this.ipServer2 = ipServer.trim();
	}
	public int getPortServer2() {
		return portServer2;
	}
	
	public void setPortServer2(int portServer) {
		this.portServer2 = portServer;
	}

	public Socket getClient(){
		return connSocketClient.getClient();
	}
	
	public void setClient(Socket client){
		connSocketClient.setClient(client);
	}
	
	public DataOutputStream getDos(){
		return connSocketClient.getDos();
	}
	
	public void setDos(DataOutputStream dos){
		connSocketClient.setDos(dos);
	}
	
	public DataInputStream getDis(){
		return connSocketClient.getDis();
	}

	public void setDis(DataInputStream dis){
		connSocketClient.setDis(dis);
	}

	public int getRetries(){
		return connSocketClient.getRetries();
	}

	public void setRetries(int retries){
		connSocketClient.setRetries(retries);
	}

	public String getIpServer(){
		return connSocketClient.getIpServer();
	}

	public void setIpServer(String ipServer){
		connSocketClient.setIpServer(ipServer.trim());
	}

	public int getPortServer(){
		return connSocketClient.getPortServer();
	}

	public void setPortServer(int portServer){
		connSocketClient.setPortServer(portServer);
	}

	public boolean isConnected(){
		return connSocketClient.isConnected();
	}

	public void setConnected(boolean connected){
		connSocketClient.setConnected(connected);
	}

	public long getTimeOutConnection(){
		return connSocketClient.getTimeOutConnection();
	}

	public void setTimeOutConnection(long timeOutConnection){
		connSocketClient.setTimeOutConnection(timeOutConnection);
	}

	public int getQuantityBytesLength(){
		return connSocketClient.getQuantityBytesLength();
	}

	public long getTimeOutSleep(){
		return connSocketClient.getTimeOutSleep();
	}

	public void setTimeOutSleep(long timeOutSleep){
		connSocketClient.setTimeOutSleep(timeOutSleep);
	}	
	
}
