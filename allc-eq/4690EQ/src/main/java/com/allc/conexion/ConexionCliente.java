package com.allc.conexion;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.allc.util.Util2;





public class ConexionCliente {
	static Logger log = Logger.getLogger(ConexionCliente.class);
	private Socket client;
	private DataOutputStream dos;
	private DataInputStream dis;
	private boolean conectado = false;
	private int reintentos;
	private int timeOutConexion;
	private int timeOutSleep;
	//private String nombre;
	private String ipServer;
	private int portServer;
	private int cantidadBytesLongitud;
	private String encode;

	/**
	 * Constructor de la clase, si se va a usar DataOutputStream para escribir por el socket colocar la variable
	 * encode en null, caso contrario, indicarle el encode con el que se trabajara (UTF-8, ASCII, etc) y se trabajara
	 * con OutputStreamWriter
	 * 
	 * @param nombre
	 * @param ipServer
	 * @param portServer
	 * @param reintentos
	 * @param timeOutConexion
	 * @param timeOutSleep
	 * @param cantidadBytesLongitud
	 * @param encode
	 */

	public ConexionCliente(String ipServer, int portServer, int reintentos, int timeOutConexion, int timeOutSleep, int cantidadBytesLongitud, String encode){
		setDos(null);
		setDis(null);
		setClient(null);
		setConectado(false);
		setReintentos(reintentos);
		//setNombre(nombre);
		setIpServer(ipServer);
		setPortServer(portServer);
		setTimeOutConexion(timeOutConexion);
		setTimeOutSleep(timeOutSleep);
		setCantidadBytesLongitud(cantidadBytesLongitud);
		setEncode(encode);
	}

	public int getCantidadBytesLongitud() {
		return cantidadBytesLongitud;
	}
	public String getIpServer() {
		return ipServer;
	}
	public int getPortServer() {
		return portServer;
	}
/*	public String getNombre() {
		return nombre;
	}*/
	public int getTimeOutConexion() {
		return timeOutConexion;
	}
	public int getTimeOutSleep() {
		return timeOutSleep;
	}
	public String getEncode() {
		return encode;
	}
	public int getReintentos() {
		return reintentos;
	}
	public DataOutputStream getDos() {
		return dos;
	}
	public DataInputStream getDis() {
		return dis;
	}
	public Socket getClient() {
		return client;
	}
	public boolean getConectado() {
		return conectado;
	}
	
	
	private void setPortServer(int portServerPoolDb) {
		this.portServer = portServerPoolDb;
	}
	private void setIpServer(String ipServerPoolDb) {
		this.ipServer = ipServerPoolDb;
	}
/*	private void setNombre(String nombre) {
		this.nombre = nombre;
	}*/
	private void setCantidadBytesLongitud(int cantidadBytesLongitud) {
		this.cantidadBytesLongitud = cantidadBytesLongitud;
	}
	private void setTimeOutConexion(int timeOutConexion) {
		this.timeOutConexion = timeOutConexion;
	}
	private void setTimeOutSleep(int timeOutSleep) {
		this.timeOutSleep = timeOutSleep;
	}
	private void setReintentos(int reintentos) {
		this.reintentos = reintentos;
	}
	private void setDos(DataOutputStream dos) {
		this.dos = dos;
	}
	private void setDis(DataInputStream dis) {
		this.dis = dis;
	}
	private void setClient(Socket client) {
		this.client = client;
	}	
	private void setConectado(boolean conectado) {
		this.conectado = conectado;
	}
	private void setEncode(String encode) {
		this.encode = encode;
	}

	public boolean limpiaDataSocket(){
		int totbytesaleer;
		String datoSucio;
		
		try {
			//while (getDis().available() == 0){
			
			if(getDis().available() > 0){
				totbytesaleer = leeLongitudDataSocket();
				if(totbytesaleer > 0){
					datoSucio = leeDataSocket(totbytesaleer);
					log.info("forward: data limpiada del socket: " + datoSucio);
				}
			}
			
			return true;
		//}catch (IOException e) {
			//conectado = false;
			//log.error("TimeOutSocket: " + e.fillInStackTrace());
		}catch (Exception e) {
			log.error("TimeOutSocket: " + e.fillInStackTrace());
			return false;
		}
	}
	
	public boolean testSocketConectado(){
		try {
			getDis().available();
			//log.info("getDis().available: " + tot );
			return true;
		}catch (Exception e) {
			log.error("testSocketConectado: " + e.fillInStackTrace());
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
			log.info("data enviada al socket: " + data);
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
			log.info("data enviada al socket: " + data);
			return true;
		} catch (IOException e) {
			log.error("escribeDataSocketOSW: " + e.fillInStackTrace());
			return false;
		}
	}
	
	public synchronized int leeLongitudDataSocket(){
		int cantBytesAleer = getCantidadBytesLongitud();
		byte[] cbufHeader;
		int totbytesaleer = 0;
		try{
			cbufHeader = new byte[cantBytesAleer];
			//while(getDis().available()< getCantidadBytesLongitud()){
				//wait
				
			//}
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
		boolean tiempoAgotado = false;
		try{
			log.info("Total Bytes a leer: "	+ totbytesaleer);
			if(totbytesaleer==0){
				dato = Util2.agregaLongitudInicioCadena("",5);

			}else{
				long tiempoInicial = Calendar.getInstance().getTimeInMillis();
				try {
					//while (getDis().available() == 0){
					while (getDis().available() < totbytesaleer){
						if(( Calendar.getInstance().getTimeInMillis() - tiempoInicial) >= timeOutConexion ){
							tiempoAgotado = true;
							break;
						}

						Thread.sleep(timeOutSleep);
					}
				}catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				if (tiempoAgotado){
					log.info("leeDataSocket: Proceso despreciado por TimeOut. Tiempo Maximo: " + timeOutConexion + " Tiempo Transcurrido: " + String.valueOf(Calendar.getInstance().getTimeInMillis() - tiempoInicial));
					dato = null;
				}else{
					
					byte[] bufbyte = new byte[totbytesaleer];
					int totalleido = 0;
					int totalleidos = 0;
					while ((totbytesaleer - totalleidos) > 0){
						totalleido = getDis().read(bufbyte, totalleidos, totbytesaleer - totalleidos);
						if (totalleido == -1){
							log.error("Error al leer del socket del Server");
							return null;
						}
						totalleidos = totalleidos + totalleido;
					}
					dato = new String(bufbyte);
					log.info("data leida: " + dato);
				}
				
			}
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			return null;
		}
		return dato;
	}
		
	public boolean timeOutSocket(){
		boolean tiempoAgotado = false;
		
		long tiempoInicial = Calendar.getInstance().getTimeInMillis();
		try {
			while (getDis().available() == 0){
			//while (getDis().available() < getCantidadBytesLongitud()){
				if(( Calendar.getInstance().getTimeInMillis() - tiempoInicial) >= timeOutConexion ){
					tiempoAgotado = true;
					break;
				}

				Thread.sleep(timeOutSleep);
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

	public String timeOutSocket(int totBytes){
		boolean tiempoAgotado = false;
		int totalBytesLeidos = 0;
		long tiempoInicial = Calendar.getInstance().getTimeInMillis();
		String data = null;
		long tiempoTranscurrido = 0;
		try {
			byte[] bufbyte = new byte[totBytes];
			while(totalBytesLeidos < totBytes){
				while (getDis().available() == 0){
				//while (getDis().available() < getCantidadBytesLongitud()){
					if(  (tiempoTranscurrido = ( Calendar.getInstance().getTimeInMillis() - tiempoInicial)) >= timeOutConexion ){
						log.info("TimeOutSocket: Proceso despreciado por TimeOut. Tiempo Maximo: " + timeOutConexion + " Tiempo Transcurrido: " + String.valueOf(tiempoTranscurrido));
						tiempoAgotado = true;
						break;
					}
	
					Thread.sleep(timeOutSleep);
				}
				if(!tiempoAgotado){
					getDis().read(bufbyte, totalBytesLeidos++, 1);
				}else{
					break;
				}
			}
			if(!tiempoAgotado)
				data = new String(bufbyte);
		}catch (Exception e) {
			log.error("TimeOutSocket: " + e.fillInStackTrace());
		}
		return data;
	}	
	
	public boolean ConectaSocket(){
		try{
			setClient(new Socket(ipServer, portServer));
			setDos(new DataOutputStream(getClient().getOutputStream()));
			setDis(new DataInputStream(getClient().getInputStream()));
			setConectado(true);
			return true;
		} catch (Exception e) {
			log.error("ConectaSocket: " + e.fillInStackTrace());
			setClient(null);
			setDis(null);
			setDos(null);
			setConectado(false);
			return false;
		}
	}

	public boolean ConectaSocketReintentos(){
		try{
			for(int i=0;i<getReintentos();i++){
				try{
					setClient(new Socket(ipServer, portServer));
					setDos(new DataOutputStream(getClient().getOutputStream()));
					setDis(new DataInputStream(getClient().getInputStream()));
					setConectado(true);
					return true;
				} catch (Exception e) {
					log.error("ConectaSocket: " + e.fillInStackTrace());
					setClient(null);
					setDis(null);
					setDos(null);
					setConectado(false);
					
				}
				Thread.sleep(1000);
			}
			
		}catch (Exception e){
			log.error(e.fillInStackTrace());
		}
		return false;
	}
	
	public void closeConexion(){
		try {
			if(dos != null )
				dos.close();
			if(dis != null )
				dis.close();
			if(!client.isClosed())
				client.close();
			
		} catch (Exception e) {
			log.error("closeConexion: " + e.fillInStackTrace());
		}
		setConectado(false);
	}

	public String toString() {
		return "ConexionCliente ["
				+ (client != null ? "client=" + client + ", " : "")
				+ (dos != null ? "dos=" + dos + ", " : "")
				+ (dis != null ? "dis=" + dis + ", " : "") + "conectado="
				+ conectado + ", reintentos=" + reintentos
				+ ", timeOutConexion=" + timeOutConexion + ", timeOutSleep="
				+ timeOutSleep + ", "
				+ (ipServer != null ? "ipServer=" + ipServer + ", " : "")
				+ "portServer=" + portServer + ", cantidadBytesLongitud="
				+ cantidadBytesLongitud + ", "
				+ (encode != null ? "encode=" + encode : "") + "]";
	}
	
	
}
