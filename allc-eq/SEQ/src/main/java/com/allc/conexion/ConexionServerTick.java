package com.allc.conexion;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;


import org.apache.log4j.Logger;


public class ConexionServerTick {
	static Logger log = Logger.getLogger(ConexionServerTick.class);
	
	private Socket client;
	private DataOutputStream dos;
	private DataInputStream dis;
	private int cantidadBytesLongitud;
	private String encode;
	private String data;
	private long timeOutConexion;
	private long timeOutSleep;
	
	

	public Socket getClient() {
		return client;
	}
	public DataOutputStream getDos() {
		return dos;
	}
	public DataInputStream getDis() {
		return dis;
	}
	public int getCantidadBytesLongitud() {
		return cantidadBytesLongitud;
	}
	public String getEncode() {
		return encode;
	}
	public String getData() {
		return data;
	}
	public long getTimeOutConexion() {
		return timeOutConexion;
	}	
	public long getTimeOutSleep() {
		return timeOutSleep;
	}
	
	private void setClient(Socket client) {
		this.client = client;
	}
	private void setDos(DataOutputStream dos) {
		this.dos = dos;
	}
	private void setDis(DataInputStream dis) {
		this.dis = dis;
	}
	private void setCantidadBytesLongitud(int cantidadBytesLongitud) {
		this.cantidadBytesLongitud = cantidadBytesLongitud;
	}
	private void setEncode(String encode) {
		this.encode = encode;
	}
	private void setData(String data) {
		this.data = data;
	}
	private void setTimeOutConexion(long timeOutConexion) {
		this.timeOutConexion = timeOutConexion;
	}
	private void setTimeOutSleep(long timeOutSleep) {
		this.timeOutSleep = timeOutSleep;
	}
	


	
	public ConexionServerTick(Socket cliente, int cantidadBytesLongitud, long timeOutConexion, long timeOutSleep, String encode){
		try{
			setClient(cliente);
			setCantidadBytesLongitud(cantidadBytesLongitud);
			setEncode(encode);
			setDos(new DataOutputStream(getClient().getOutputStream()));
			setDis(new DataInputStream(getClient().getInputStream()));
			setTimeOutConexion(timeOutConexion);
			setTimeOutSleep(timeOutSleep);

		} catch (Exception e) {
			log.error("ConexionClienteServer: " + e.fillInStackTrace());
			setClient(null);
			setDis(null);
			setDos(null);
		}
	}
	
	public void closeConnectionServerTick(){
		try {
			if(dos != null )
				dos.close();
			if(dis != null )
				dis.close();
			if(!client.isClosed())
				client.close();
			log.info("closeClienteServer: Cerro las conexiones");
		} catch (Exception e) {
			log.error("closeClienteServer: " + e.fillInStackTrace());
		}

	}
	
	public synchronized int leeLongitudDataSocket(){
		int cantBytesAleer = getCantidadBytesLongitud();
		byte[] cbufHeader;
		int totbytesaleer = 0;
		try{
			cbufHeader = new byte[cantBytesAleer];
			
			getDis().read(cbufHeader, 0, cantBytesAleer);
			totbytesaleer = Integer.parseInt(new String(cbufHeader));
		}catch(Exception e){
			log.error("leeLongitudDataSocket: " + e.fillInStackTrace());
			totbytesaleer = -1;
		}
		return totbytesaleer;
	}
	
	public synchronized String leeDataSocket(int totbytesaleer){
		String dato;
		try{
/*			log.info("Total Bytes a leer: "	+ totbytesaleer);
			if(totbytesaleer==0){
				dato = Util2.agregaLongitudInicioCadena("",5);

			}else{*/

				byte[] bufbyte = new byte[totbytesaleer];
				int totalleido = 0;
				int totalleidos = 0;
				
				while ((totbytesaleer - totalleidos) > 0){
					totalleido = getDis().read(bufbyte, totalleidos, totbytesaleer - totalleidos);
					if (totalleido == -1){
						log.error("leeDataSocket: Error al leer del socket");
						return null;
					}
					totalleidos = totalleidos + totalleido;
				}
				dato = new String(bufbyte);
				setData(dato);
				//log.info("data leida: " + dato);
			//}
			return dato;
		}catch(Exception e){
			log.error("leeDataSocket: " + e);
			return null;
		}
	}
	
	
	public boolean timeOutSocket(){
		boolean tiempoAgotado = false;
		
		long tiempoInicial = Calendar.getInstance().getTimeInMillis();
		try {
			while (getDis().available() == 0){
				if(( Calendar.getInstance().getTimeInMillis() - tiempoInicial) >= getTimeOutConexion() ){
					tiempoAgotado = true;
					break;
				}

				Thread.sleep(getTimeOutSleep());
			}
		}catch (Exception e) {
			log.error("TimeOutSocket: " + e.fillInStackTrace());
		}
		if (tiempoAgotado){
			log.info("TimeOutSocket: Proceso despreciado por TimeOut. Tiempo Maximo: " + timeOutConexion + " Tiempo Transcurrido: " + String.valueOf(Calendar.getInstance().getTimeInMillis() - tiempoInicial));
			return true;
		}else
			return false;
	}
	
	
	/**
	 * Funcion Utilizada para escribir data a un Socket
	 * @param data  Dato que se envia por socket
	 * @return		true si se logro escribir la data al socket
	 * 				false si no se logro escribir la data al socket
	 */
	public synchronized boolean escribeDataSocket(String data){
		
		if(null == getEncode())
			return escribeDataSocketDOS(data);
		else
			return escribeDataSocketOSW(data);

	}
	
	/**
	 * Funcion utilizada para escribir data a un socket
	 * UTILIZA DataOutputStream
	 * @param data	Dato que se envia por socket
	 * @return		true si se logro escribir la data al socket
	 * 				false si no se logro escribir la data al socket
	 */
	private synchronized boolean escribeDataSocketDOS(String data){
		try {
			
			getDos().writeBytes(data);
			getDos().flush();
			log.info("data enviada al cliente Socket: " + data);
			return true;
		} catch (Exception e) {
			log.error("escribeDataSocketDOS: " + e.fillInStackTrace());
			return false;
		}
	}

	/**
	 * Funcion utilizada para escribir data a un socket
	 * UTILIZA OutputStreamWriter
	 * @param data	Dato que se envia por socket
	 * @return		true si se logro escribir la data al socket
	 * 				false si no se logro escribir la data al socket
	 */
	private synchronized boolean escribeDataSocketOSW(String data){
		try {
			OutputStreamWriter osw = new OutputStreamWriter(getDos(), getEncode());
			osw.write(data);
			osw.flush();
			log.info("data enviada al cliente Socket: " + data);
			return true;
		} catch (IOException e) {
			log.error("escribeDataSocketOSW: " + e.fillInStackTrace());
			return false;
		}
	}

	
		
}
