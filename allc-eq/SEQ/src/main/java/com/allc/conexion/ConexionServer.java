package com.allc.conexion;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;


public class ConexionServer {
	//static Logger log = Logger.getLogger(ConexionServer.class);
	
	private Socket client;
	private DataOutputStream dos;
	private DataInputStream dis;
	private int cantidadBytesLongitud;
	private String encode;
	private String data;
	private long timeOutConexion;
	private long timeOutSleep;
	private String error;
	

	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
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
	

	/**
	 * Constructor que inicializa la clase
	 * @param cliente					Socket que se utiliza para el cliente en particular
	 * @param cantidadBytesLongitud		Cantidad de bytes usados para indicar la longitud de la trama ( primeros X bytes )
	 * @param timeOutConexion			Tiempo usado como limite para esperar por los datos en el socket
	 * @param timeOutSleep				Tiempo de respiro utilizado entre los loops de las lecturas
	 * @param encode					Si es null escribe en el charset por defecto utilizando bytes, caso contrario con el charset indicado utilizando outputStreamWriter
	 */
	
	public ConexionServer(Socket cliente, int cantidadBytesLongitud, long timeOutConexion, long timeOutSleep, String encode){
		try{
			setClient(cliente);
			setCantidadBytesLongitud(cantidadBytesLongitud);
			setEncode(encode);
			setDos(new DataOutputStream(getClient().getOutputStream()));
			setDis(new DataInputStream(getClient().getInputStream()));
			setTimeOutConexion(timeOutConexion);
			setTimeOutSleep(timeOutSleep);
			setError("");
		} catch (Exception e) {
			setError("ConexionServer: " + e.fillInStackTrace());
			setClient(null);
			setDis(null);
			setDos(null);
		}
	}
	
	public void closeConnectionServer(){
		try {
			if(dos != null )
				dos.close();
			if(dis != null )
				dis.close();
			if(!client.isClosed())
				client.close();
			setError("");
			//log.info("closeClienteServer: Cerro las conexiones");
		} catch (Exception e) {
			setError("closeConnectionServer: " + e);
		}

	}

	/**
	 * Lee la longitud de una trama 
	 * @return  la cantidad de bytes que tiene la trama.
	 *			-1 si ocurrio un error o si ocurrio timeout al leer la longitud de la trama
	 */
	public synchronized int leeLongitudDataSocket(){
		int cantBytesAleer = getCantidadBytesLongitud();
		byte[] cbufHeader;
		int totbytesaleer = 0;
		try{
			cbufHeader = new byte[cantBytesAleer];
			if(!timeOutSocket(cantBytesAleer)){
				getDis().read(cbufHeader, 0, cantBytesAleer);
				totbytesaleer = Integer.parseInt(new String(cbufHeader));
				setError("");
			}else{
				totbytesaleer = -1;
				setError("leeLongitudDataSocket: Time out passed " + getTimeOutConexion() );
			}
		}catch(Exception e){
			setError("leeLongitudDataSocket: " + e);
			totbytesaleer = -1;
		}
		return totbytesaleer;
	}
	
/*	public synchronized int leeLongitudDataSocket(){
		int cantBytesAleer = getCantidadBytesLongitud();
		byte[] cbufHeader;
		int totbytesaleer = 0;
		try{
			cbufHeader = new byte[cantBytesAleer];
			
			if(getDis().available()< getCantidadBytesLongitud()){
				//wait
			}
			getDis().read(cbufHeader, 0, cantBytesAleer);
			totbytesaleer = Integer.parseInt(new String(cbufHeader));

		}catch(Exception e){
			log.error("leeLongitudDataSocket: " + e.fillInStackTrace());
			totbytesaleer = -1;
		}
		return totbytesaleer;
	}*/
	/**
	 * Funcion 	que lee una cantidad de bytes = totbytesaleer 
	 * @param 	totbytesaleer
	 * @return	""	  	si ocurrio timeout al leer del socket 
	 * 			null 	si ocurrio un error al leer del socket
	 */
	public synchronized String leeDataSocket(int totbytesaleer){
		String dato;
		try{
			byte[] bufbyte = new byte[totbytesaleer];
			int totalleido = 0;
			int totalleidos = 0;
			
			if(!timeOutSocket(totbytesaleer)){
				while ((totbytesaleer - totalleidos) > 0){
					totalleido = getDis().read(bufbyte, totalleidos, totbytesaleer - totalleidos);
					if (totalleido == -1){
						setError("leeDataSocket: Error reading socket");
						return null;
					}
					totalleidos = totalleidos + totalleido;
				}
				dato = new String(bufbyte);
				setData(dato);
				setError("");
			}else //si ingreso aqui la funcion timeOutSocket ya registro el error con setError
				dato = "";
			return dato;
		}catch(Exception e){
			setError("leeDataSocket: " + e);
			return null;
		}
	}
	
	
	public boolean timeOutSocket(int cantidadBytesEspera){
		boolean tiempoAgotado = false;
		
		long tiempoInicial = Calendar.getInstance().getTimeInMillis();
		try {
			while (getDis().available() < cantidadBytesEspera){
				if(( Calendar.getInstance().getTimeInMillis() - tiempoInicial) >= getTimeOutConexion() ){
					tiempoAgotado = true;
					break;
				}

				Thread.sleep(getTimeOutSleep());
			}
		}catch (Exception e) {
			setError("timeOutSocket: " + e);
		}
		if (tiempoAgotado){
			setError("TimeOutSocket: process contempt by time out. Time limit: " + timeOutConexion + " time passed by : " + String.valueOf(Calendar.getInstance().getTimeInMillis() - tiempoInicial));
			return true;
		}else{
			setError("");
			return false;
		}
	}
	
	public boolean testSocketConectado(){
		try {
			getDis().available();
			setError("");
			return true;
		}catch (Exception e) {
			setError("testSocketConectado: " + e);
			return false;
		}

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
			//log.info("data enviada al cliente Socket: " + data);
			setError("");
			return true;
		} catch (Exception e) {
			setError("escribeDataSocketDOS: " + e);
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
			//log.info("data enviada al cliente Socket: " + data);
			setError("");
			return true;
		} catch (IOException e) {
			setError("escribeDataSocketOSW: " + e);
			return false;
		}
	}

	
		
}
