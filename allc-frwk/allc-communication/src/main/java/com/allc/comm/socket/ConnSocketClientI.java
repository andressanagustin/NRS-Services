package com.allc.comm.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public interface ConnSocketClientI {

	public boolean writeDataSocket(String data);

	public boolean timeOutSocket();
	
	public int readLengthDataSocket();
	
	public String readDataSocket(int quantityBytesToRead);
	
	//public boolean connectSocket();
	
	public boolean connectSocketUsingRetries();
	
	public void closeConnection(); 
	
	//public boolean writeDataSocketDOS(String data);
	//public synchronized boolean writeByteArraySocket(byte[] data);
	//private synchronized boolean writeByteArraySocketDOS(byte[] data);
	//private boolean timeOutSocket(long quantityBytesToWait);
	//public synchronized int bytesToInt(byte[] bytes);
	//public synchronized int leeLongitudDataHexaSocket();
	//private void closeDos();
	//private void closeDis();
	//private void closeClient();
	
	public Socket getClient();
	
	public void setClient(Socket client);
	
	public DataOutputStream getDos();
	
	public void setDos(DataOutputStream dos);
	
	public DataInputStream getDis();

	public void setDis(DataInputStream dis);

	public int getRetries();

	public void setRetries(int retries);

	public String getIpServer();

	public void setIpServer(String ipServer);

	public int getPortServer();

	public void setPortServer(int portServer);

	public boolean isConnected();

	public void setConnected(boolean connected);

	public long getTimeOutConnection();

	public void setTimeOutConnection(long timeOutConnection);

	public int getQuantityBytesLength();

	public void setQuantityBytesLength(int quantityBytesLength);

	public long getTimeOutSleep();

	public void setTimeOutSleep(long timeOutSleep);


}
