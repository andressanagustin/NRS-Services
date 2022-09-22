/**
 * 
 */
package com.allc.main.task;

import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ServiceEQtoNagios.RecibeTramas;

import com.allc.conexion.Conexion;
import com.allc.conexion.ConexionCliente;
import com.allc.conexion.ConexionServer;
import com.allc.conexion.Trama;
import com.allc.dao.EQDAO;
import com.allc.dao.EQADMINDAO;
import com.allc.entities.Ejecucion;
import com.allc.entities.Tienda;
import com.allc.entities.Equipo;
import com.allc.entities.Message;
import com.allc.entities.Proceso;
import com.allc.entities.TipoEstado;
import com.allc.files.Files;
import com.allc.main.constants.Constants;
import com.allc.main.properties.PropertiesSEQ;
import com.allc.util.Util2;

/**
 * @author Alexander Padilla
 *
 */
public class Task implements Runnable {
	static Logger log = Logger.getLogger(Task.class);
	Pattern p = Pattern.compile(Constants.Comunicacion.REGEX);
	protected Pattern parms = Pattern.compile(Constants.ArmsServerCommunication.REGEX);
	private static SimpleDateFormat formateador = new SimpleDateFormat("yyyyMMddHHmmss", new Locale("ES_ES"));
	/**
	 * 
	 */
	private Socket cliente;
	private RecibeTramas recibeTramas;
	private int regInBD;
	
	public Socket getCliente() {
		return cliente;
	}

	public void setCliente(Socket cliente) {
		this.cliente = cliente;
	}

	public Task(Socket cliente, RecibeTramas recibeTramas, int registerInBD) {
		this.cliente = cliente;
		this.recibeTramas = recibeTramas;
		this.regInBD = registerInBD;
	}


	public void run() {
		int totbytesaleer;
		int tipoOperacion;
		String data = "";
		boolean esFinal = false;
		try {
			cliente.setSoLinger(true, 0);
		} catch (SocketException e) {
			log.error(e.getMessage(), e);
		}
		
		String cli = cliente.getInetAddress().getHostAddress();
		log.info("connected client: " + cli);
		
		ConexionServer cnx = new ConexionServer(cliente, Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD, PropertiesSEQ.Comunication.TIME_OUT_SERVER, PropertiesSEQ.Comunication.TIME_OUT_SLEEP, null);
		try {
			String message = null;
			while(!esFinal){
				 totbytesaleer = cnx.leeLongitudDataSocket();
				 /**Si se recibe iformacion con data**/
				 if(totbytesaleer > 0){
						data = cnx.leeDataSocket(totbytesaleer);
						log.info("DATA RECIBIDA: "+data);
						/**Si lo que se lee es igual a "" entonces ocurrio timeOut, ya que aqui solo se ingresa si totbytesaleer > 0 **/
						if(!data.equals("")){
							//log.info("data leida del Cliente Socket: " + data);
						    List<String> list = Arrays.asList(p.split(data));
						    /**Instanciamos a la clase trama**/
						    Trama trama = new Trama(list, Constants.Comunicacion.CANTIDAD_DATOS_HEADER, Constants.Comunicacion.CAR);
						    /**realizamos la carga para validar la trama**/
							if(trama.loadData()){
								if(log.isDebugEnabled())
									log.debug(trama.toString());
							
								//canal = (String)trama.getHeader().get(0);
								tipoOperacion = Integer.parseInt((String)trama.getHeader().get(1));
								//fechaHora = (String)trama.getHeader().get(4);
								//socket = Integer.parseInt((String)trama.getHeader().get(2));
								
		
								switch(tipoOperacion){
									// *************************************************
									// * PROCESO SAF
									// ************************************************
									case Constants.ProcessConstants.SAF_PROCESS:{  //SAF
										//storeAndForward();
										break;
									}
									// *************************************************
									// * REGISTRO DE EVENTOS 
									// ************************************************//*
									/**Registro de eventos de la cola de mensajes del 4690**/
									case Constants.ProcessConstants.SAVE_4690_EVENT_QUEUE_PROCESS:
									/**Registro de eventos del exception log del 4690**/
									case Constants.ProcessConstants.SAVE_4690_EXCEPTION_LOG_PROCESS:
									/**Registro de eventos del SO windows**/
									case Constants.ProcessConstants.SAVE_WIN_EVENT_LOG_PROCESS:{
										storedEvents(cnx, trama);
										try{
											recibeTramas.RecibeTramas(trama.getBodyStr());
										}catch(Exception e){
											log.error("trying to send to Nagios " + e);
										}
										message = "";
										break;
									}
									case Constants.ProcessConstants.LAUNCH_UPS:{
										storedUPS(cnx, trama);
										message = "";
										break;
									}
									default: {
										message = "run: Operation not defined";
										break;
									}
								}
							}else
								message = trama.getError();
									
						}else{
							message = cnx.getError();
						}
								
				 }else
					 /**Si lo que se recibe es un Tick de vida**/
					 if(totbytesaleer == 0){
						 message = "I'm still alive...";
					 }else
						 /**Si ocurrio un error**/
						 if(totbytesaleer == -1){
							 message = cnx.getError();
							 esFinal = true;
					     }
				 
				 if(!message.equals(""))
					 log.info(message);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage(), e);
		} finally{
			try {
				log.info("Cierra conection");
				cnx.closeConnectionServer();
				Thread.sleep(PropertiesSEQ.Comunication.TIME_OUT_SLEEP);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
			//cnx.closeConnectionServer();
		}		
		
		
		
		
	}

	/**
	 * Registra un evento respetando la estructura que le corresponde al mensaje
	 * DescripcionCadena/Codigo de Local/A�o/Mes/dia.dat
	 * @param conxSocket
	 * @param trama
	 */
	private void storedEvents(final ConexionServer conxSocket, Trama trama) {
		String message;
		String codLocal;
		String desCadena;
		String fechaHoraEvento;
		String nodoCtrl;
		String terminalID;
		String messageGroup;
		String messageNumber;
		String sourceNumber;
		String eventNumber;
		String severity;
		String uniqueData;
		String progSource;
		String ip = null;
		try{
			desCadena = (String)trama.getHeader().get(3);
			codLocal = (String)trama.getHeader().get(4);
			
			
			fechaHoraEvento = (String)trama.getBody().get(0);
			progSource=(String)trama.getBody().get(1);
			nodoCtrl = (String)trama.getBody().get(3);
			terminalID = (String)trama.getBody().get(4);
			messageGroup = (String)trama.getBody().get(5);
			messageNumber = (String)trama.getBody().get(6);
			sourceNumber = (String)trama.getBody().get(7);
			eventNumber = (String)trama.getBody().get(8);
			severity = (String)trama.getBody().get(9);
			uniqueData = (String)trama.getBody().get(10);
			
			//ip=(String)trama.getBody().get(2);
			//LA IP VENIA SIN NATEAR Y NECESITAMOS QUE SEA NATEADA, POR ESO AHORA LA TOMAMOS DESDE MN_TIENDA
			int terminal = Integer.valueOf(terminalID).intValue();
			int tipo = terminal > 0 ? 2 : 1;
			String desClave = tipo == 2 ? terminalID : nodoCtrl;
			EQADMINDAO eqAdminDAO = new EQADMINDAO();
			Tienda tienda = eqAdminDAO.getTienda(codLocal);
			if(tienda != null){						
				ip= tienda.getIp();	
			}

			Date fecha = Util2.convierteStringADate(fechaHoraEvento, "yyyyMMddHHmmss");
			
			SimpleDateFormat dateFormatAno = new SimpleDateFormat("yyyy");
			String ano = String.valueOf(Integer.parseInt(dateFormatAno.format(fecha)));
			
			SimpleDateFormat dateFormatMes = new SimpleDateFormat("MM");
			String mes = String.valueOf(Integer.parseInt(dateFormatMes.format(fecha)));
					
			SimpleDateFormat dateFormatDia = new SimpleDateFormat("dd");
			String dia = String.valueOf(Integer.parseInt(dateFormatDia.format(fecha)));
			log.info("Entro al metodo storedEvents, regInDb: " + regInBD);
			if(regInBD==1){
				EQDAO eqDAO = new EQDAO();
				Equipo equipo = eqDAO.getEquipo(desClave, codLocal);
				
				if(equipo == null){
					equipo = new Equipo();
					equipo.setDesClave(desClave);
					equipo.setIdTipo(tipo);
					equipo.setIdLocal(Integer.valueOf(codLocal));
					equipo.setIndActivo(1);
					equipo.setIndOnline(1);
					equipo.setIp(ip);
					log.info("No Encontro el Equipo, lo crea con los siguientes datos: " + equipo.toString());
				}else {
					log.info("Encontro el Equipo, " + equipo.toString());
					//if(equipo.getIdLocal()  == Integer.valueOf(codLocal))
					equipo.setIp(ip);
					log.info("Con el cambio de Ip quedo --> " + ip);	
				}
				Message message2 = new Message();
				message2.setData(uniqueData);
				message2.setEquipo(equipo);
				message2.setEventDate(fecha);
				message2.setMessageGroup(messageGroup);
				message2.setMessageNumber(new Integer(messageNumber));
				message2.setPmSource(progSource);
				message2.setSeverity(new Integer(severity));
				message2.setSourceNumber(sourceNumber);
				message2.setCodLocal(Integer.valueOf(codLocal));
//				MessageSource messageSource = new MessageSource();
//				messageSource.setEquipo(equipo);
//				messageSource.setEventDate(fecha);
//				messageSource.setIdLocal(codLocal);
//				
//				message2.setMessageSource(messageSource);
				eqDAO.saveMessage(message2);
				eqDAO.closeSession();
			}
			if("X".equalsIgnoreCase(messageGroup.trim()) && "223".equalsIgnoreCase(messageNumber.trim())){
				log.info("FIN DE CIERRE ENCONTRADO.");
				informarFinCierre(codLocal, fecha);
			}
			//data a registrar 
			//String data = fechaHoraEvento + Main.getCar() + nodoCtrl + Main.getCar() + terminalID + Main.getCar() + messageGroup + Main.getCar() + messageNumber + Main.getCar() + sourceNumber + Main.getCar() + eventNumber + Main.getCar() + severity + Main.getCar() + uniqueData;
			String data = trama.getBodyStr();
			/**se guarda Cadena/Local/A�o/Mes/dia.dat**/
			String ruta = PropertiesSEQ.Parametros.RUTA_REGISTRO_INFO + Constants.Sistema.FS + desCadena + Constants.Sistema.FS + codLocal + Constants.Sistema.FS + ano + Constants.Sistema.FS + mes ;
			log.info("RUTA: "+ruta);
			/**creamos la ruta del directorio**/
			if(Files.creaDirectorio(ruta))
				if(Files.creaEscribeDataArchivo(ruta + Constants.Sistema.FS + dia + ".dat", data + Constants.Comunicacion.CRLF, true))
					message = String.valueOf(Constants.ControlMensajes.RESULT_MS_OK);
				else
					message = Constants.ControlMensajes.RESULT_ERROR_NO_SE_REGISTRO_DATA_EN_FILE + trama.getSeparationCar() + Constants.DescriptorMensajes.DES_ERROR_NO_SE_REGISTRO_DATA_EN_FILE;
			else
				message = Constants.ControlMensajes.RESULT_ERROR_NO_SE_CREO_DIRECTORIO + trama.getSeparationCar() + Constants.DescriptorMensajes.DES_ERROR_NO_SE_CREO_DIRECTORIO;
			
			String tmp = Util2.agregaLongitudInicioCadena((String)trama.getHeaderStr() + trama.getSeparationCar() + message , Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);	
			/**Enviamos la data**/
			conxSocket.escribeDataSocket(tmp);
			log.info("Data Sent: " + tmp);
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
	}			
	
	protected void informarFinCierre(String codLocal, Date fecha){
		Conexion cx = new Conexion();
		/** Set the attributes **/
		cx.setIp(PropertiesSEQ.ComunicationArmsServer.IP);
		cx.setPuerto(PropertiesSEQ.ComunicationArmsServer.PORT);
		cx.setReintentos(PropertiesSEQ.ComunicationArmsServer.RETRIES);
		cx.setTimeOutConexion(PropertiesSEQ.ComunicationArmsServer.TIMEOUT);
		cx.setTimeOutSleep(PropertiesSEQ.ComunicationArmsServer.TIMESLEEP);
		cx.setCantidadBytesLongitud(Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);
		ConexionCliente cnxion = null;
		cnxion = new ConexionCliente(null, cx.getIp(), cx.getPuerto(), cx.getReintentos(), cx.getTimeOutConexion(),
				cx.getTimeOutSleep(), cx.getCantidadBytesLongitud(), null);
		if (cnxion.ConectaSocket()) {
			log.info(Thread.currentThread().getName() + ": Connected.");
			// si hay data se envia.
			send(cnxion, fecha, codLocal);
			cnxion.closeConexion();
		} else {
			 log.info(Thread.currentThread().getName() + ": Not Connected.");
		}
	}
	

	private boolean send(ConexionCliente conexionCliente, Date fecha, String storeNumber) {
		String data;
		List list = null;
		StringBuffer sb = new StringBuffer();
		try {
			while(storeNumber.length() < 4){
				storeNumber = "0"+storeNumber;
			}
			String today = formateador.format(new Date()).toString();
			sb.append(Constants.ArmsServerCommunication.SOCKET_CHANNEL).append(Constants.ArmsServerCommunication.FRAME_SEP).append("39")
			.append(Constants.ArmsServerCommunication.FRAME_SEP).append("000").append(Constants.ArmsServerCommunication.FRAME_SEP)
			.append(storeNumber).append(Constants.ArmsServerCommunication.FRAME_SEP).append(today).append(Constants.ArmsServerCommunication.FRAME_SEP)
			.append(Constants.ArmsServerCommunication.PERM_CONN).append(Constants.ArmsServerCommunication.FRAME_SEP).append(formateador.format(fecha));
			
			String linea = Util2.agregaLongitudInicioCadena(sb.toString(), Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);

			if (conexionCliente.escribeDataSocket(linea)) {
				if (!conexionCliente.timeOutSocket()) {
					data = conexionCliente.leeDataSocket(conexionCliente.leeLongitudDataSocket());
					list = Arrays.asList(parms.split(data));
					if (list.get(Constants.Comunicacion.CANTIDAD_DATOS_HEADER).toString().equals(Constants.ArmsServerCommunication.CERO)) {
						return true;
					}

				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);

		}
		return false;
	}
	/**
	 * Registra una actualización de Estado de Procesp.
	 * 
	 * @param conxSocket
	 * @param trama
	 */
	private void storedUPS(final ConexionServer conxSocket, Trama trama) {
		String codLocal;
		String nodoCtrl;
		String descriptorPro;
		Date fechaExec;
		String abrevTipoEstado;
		String data;
		String tipoEq;
		String ip = null;
		try{
			nodoCtrl = (String)trama.getHeader().get(2);
			codLocal = (String)trama.getBody().get(4);
			if (codLocal==null || codLocal.equals("") || codLocal.equals("null")) {
				codLocal = "000";
				log.info("cambia codLocal a " + codLocal);
			}
			
			tipoEq = (String)trama.getBody().get(2);
			//ip=(String)trama.getBody().get(3);
			
			descriptorPro = (String)trama.getBody().get(0);
			fechaExec = Util2.convierteStringADate((String)trama.getBody().get(6), "yyyyMMddHHmmss");
			abrevTipoEstado = (String)trama.getBody().get(5);
			if (trama.getBody().size() > 7) {
				data = (String)trama.getBody().get(7);
			}
			else data="";
			//ip=(String)trama.getBody().get(2);
			//LA IP VENIA SIN NATEAR Y NECESITAMOS QUE SEA NATEADA, POR ESO AHORA LA TOMAMOS DESDE MN_TIENDA
			EQADMINDAO eqAdminDAO = new EQADMINDAO();
			Tienda tienda = eqAdminDAO.getTienda(codLocal);
			if(tienda != null){						
				ip= tienda.getIp();	
			}
			if(regInBD==1){
				EQDAO eqDAO = new EQDAO();
				Equipo equipo = eqDAO.getEquipo(nodoCtrl, codLocal);
				if(equipo == null){
					equipo = new Equipo();
					equipo.setDesClave(nodoCtrl);
					equipo.setIdTipo(Integer.valueOf(tipoEq));
					equipo.setIdLocal(Integer.valueOf(codLocal));
					equipo.setIndActivo(1);
					equipo.setIndOnline(1);
					equipo.setIp(ip);
				}
				Proceso proceso = eqDAO.getProceso(descriptorPro);
				if(proceso == null){
					proceso = new Proceso();
					proceso.setClave(descriptorPro);
				}
				TipoEstado tipoEstado = eqDAO.getTipoEstado(abrevTipoEstado);
				if(tipoEstado == null){
					tipoEstado = new TipoEstado();
					tipoEstado.setAbreviatura(abrevTipoEstado);
				}
				
				Ejecucion ejecucion = new Ejecucion();
				ejecucion.setProceso(proceso);
				ejecucion.setEquipo(equipo);
				ejecucion.setExecDate(fechaExec);
				ejecucion.setTipoEstado(tipoEstado);
				ejecucion.setData(data);
				ejecucion.setCodLocal(Integer.valueOf(codLocal));
				
				eqDAO.saveEjecucion(ejecucion);
				eqDAO.closeSession();
			}
			String tmp = Util2.agregaLongitudInicioCadena((String)trama.getHeaderStr() + trama.getSeparationCar() + String.valueOf(Constants.ControlMensajes.RESULT_MS_OK) , Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);	
			/**Enviamos la data**/
			conxSocket.escribeDataSocket(tmp);
			log.info("Termina");
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
	}			

}
