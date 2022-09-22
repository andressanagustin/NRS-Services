package com.test;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.DefaultListModel;

import mqttcl.Publicar;
import mqttcl.Suscribir;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileFilter4690;
import com.ibm.OS4690.FileInputStream4690;
import com.ibm.OS4690.FileOutputStream4690;
import com.ibm.retail.AEF.automation.Item;
import com.ibm.retail.AEF.automation.ItemIdentifier;
import com.ibm.retail.AEF.automation.ItemIdentifierImpl;
import com.ibm.retail.AEF.automation.POSAutomationProvider;
import com.ibm.retail.AEF.automation.SalesTransaction;
import com.ibm.retail.AEF.client.AEFPropertyListenerProxy;
import com.ibm.retail.AEF.client.AEFPropertyListenerProxyImpl;
import com.ibm.retail.AEF.client.CashReceiptListenerProxy;
import com.ibm.retail.AEF.data.POSDataProvider;
import com.ibm.retail.AEF.event.AEFPropertyChangeEvent;
import com.ibm.retail.AEF.event.AEFPropertyChangeListener;
import com.ibm.retail.AEF.event.CashReceiptEvent;
import com.ibm.retail.AEF.event.CashReceiptListener;
import com.ibm.retail.AEF.factory.AEFSessionFactory;
import com.ibm.retail.AEF.server.AEFBase;
import com.ibm.retail.AEF.server.SessionServer;
import com.ibm.retail.AEF.session.AEFSession;
import com.ibm.retail.AEF.session.SessionParameters;
import com.ibm.retail.AEF.workstation.Workstation;
import com.ibm.retail.si.util.AEFException;

/**
 * 
 */

/**
 * @author Gustavo Kiener
 *
 */
public class MainSaleTest  implements CashReceiptListener, AEFPropertyChangeListener {
	protected SessionServer server;
	protected AEFSession session;
	protected POSDataProvider data;
	protected DefaultListModel receiptList;
	protected CashReceiptListenerProxy receiptListenerProxy;
	protected AEFPropertyListenerProxy propertyListenerProxy;
	protected boolean createNew;
	protected int fileNumber = 0;
	protected String fileSxaracName = "";
	protected String orden = "";
	protected String path;
	private Iterator filesToProcess = null;
	private File4690 inFolder4690;
	private File inFolder;
	private File4690 outFolder4690;
	private File4690 errorFolder4690;
	private File outFolder;
	private File errorFolder;
	private boolean is4690;
	private int sleepTime;
	protected int posState;
	protected int posSubstate;
	boolean borrarArchivo = false;
	boolean transEnCurso = false;
	private String brokerMqtt;
	private String topicMqtt;
	private String hostName;
	private String hostAddress;
	private String storeNumber = "000";
	private String terminalNumber = null;
	protected static Properties props = null;
	protected static Logger logger;
	protected static SimpleDateFormat ddMMyy_format = new SimpleDateFormat("ddMMyy", new Locale("ES_ES"));
	protected static SimpleDateFormat yyyyMMddHHmmss_format = new SimpleDateFormat("yyyyMMddHHmmss", new Locale("ES_ES"));
	private static final int SUBSTATES_DESCONECTADO = 11042; //CAJERO DESCONECTADO - TERMINAL ACTIVO
	private static final int SUBSTATES_ASEGURADO = 11043; //CAJERO CONECTADO - TERMINAL ASEGURADO (falta contraseña)
	private static final int SUBSTATES_CAMBIO_CAJA = 10415; //CAMBIO DE CAJA
	private static final List<Integer> subStatesToProcess = new ArrayList<>(Arrays.asList(
																SUBSTATES_DESCONECTADO, 
																SUBSTATES_ASEGURADO
															));
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
		MainSaleTest listener = new MainSaleTest();
		props = new Properties();
		FileInputStream is = new FileInputStream("config.properties");
		props.load(is);
		listener.init(props);
//		listener.process(props, props.getProperty("terminalNumber"));
		listener.process(props, props.getProperty("terminalsUserPass"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void init(Properties props){
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(MainSaleTest.class);
		logger.info(MainSaleTest.class.getName() + " starting service");
		this.receiptList = new DefaultListModel();
		this.receiptList.addElement("Waiting for events...");
		createNew = false;
		try {
			sleepTime = 3000;
			
			brokerMqtt = props.getProperty("brokerMQTT");
			topicMqtt = props.getProperty("topicMQTT");
			String userMqtt = topicMqtt.substring(6);
			logger.info("User Mqtt:"+userMqtt);
			Suscribir cliente = new Suscribir(brokerMqtt, topicMqtt, userMqtt);
		    cliente.start();
		    
			is4690 = "4690".equalsIgnoreCase(props.getProperty("so"));
			path = props.getProperty("filesPath");
			logger.info(props.getProperty("inFolder.path"));
			Thread.sleep(2000);
			if(is4690){
				logger.info("Es 4690");
				File4690 pathFile = new File4690(path);
				pathFile.mkdirs();
				inFolder4690 = new File4690(props.getProperty("inFolder.path"));
				logger.info("Creando folder: " + props.getProperty("inFolder.path"));
				inFolder4690.mkdirs();
				outFolder4690 = new File4690(props.getProperty("outFolder.path"));
				logger.info("Creando folder: " + props.getProperty("outFolder.path"));
				outFolder4690.mkdirs();

				errorFolder4690 = new File4690(props.getProperty("outFolder.path") + "/ERR/");
				logger.info("Creando folder: " + props.getProperty("outFolder.path") + "/ERR/") ;
				errorFolder4690.mkdirs();
				ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
				storeNumber = (new Integer(controllerStatusData.getStoreNumber())).toString();
				while(storeNumber.length() < 3)
					storeNumber = "0" + storeNumber;
			} else {
				inFolder = new File(props.getProperty("inFolder.path"));
				inFolder.mkdirs();
				outFolder = new File(props.getProperty("outFolder.path"));
				outFolder.mkdirs();
				errorFolder = new File(props.getProperty("outFolder.path") + "/ERR/");
				errorFolder.mkdirs();
			}
			hostName = InetAddress.getLocalHost().getHostName();
			hostAddress = InetAddress.getLocalHost().getHostAddress();
			initFileNumber();
		} catch (Exception re) {
			logger.error(re.getMessage(), re);
		}
	}
	
	private void loadSessionServer(Properties props) throws Exception {
		if(server == null) {
			server = AEFBase.getInstance().getRemoteServerFromURI(props.getProperty("serverURI"));
			if(server == null) 
				logger.info("session server no obtenido");
			else
				logger.info("session server obtenido");
		}
	}
	
	private void loadSession(String terminalNumber, boolean isFisico) throws Exception {
		if(session == null){
			logger.info("Get an available session");
			if(isFisico){
				List servers=server.getFactoryIDs();
				for(int i = 0; i < servers.size(); i++){
					AEFSessionFactory factory = server.getFactory(i);
					if(factory.getFactoryInfo().getID().endsWith(String.valueOf(terminalNumber))){
						logger.info("Factory: "+ factory.getFactoryInfo().getID());
						this.session = factory.getSession(terminalNumber, new SessionParameters());
						i = servers.size();
					}
				}
			} else {						
				this.session = server.getSession(terminalNumber);
			}
			if(session != null){
				logger.info("Get an available session finished");
				logger.info("Obtained session. Terminal number is "
						+ this.session.getTerminalNumber());
				
				this.data = this.session.getPOSDataProvider();
				logger.info("getPOSDataProvider" + data);
				logger.info("Terminal number:" + terminalNumber);
				posSubstate = Integer.valueOf(this.data.getPropertyValue("POS_DEVICE", "subState").toString().trim());
				logger.info("Subestado inicial: "+ posSubstate);
				posState = Integer.valueOf(this.data.getPropertyValue("POS_DEVICE", "POS_STATE").toString().trim());
				logger.info("Estado inicial: "+ posState);
				String[] linea = new String[2];
				linea[0] = "POS State: " + posState + "\r\n";
				linea[1] = "POS Substate: " + posSubstate + "\r\n";
				
				if(is4690){
					File4690 pathSTSFile = new File4690(path+"/" + getTerminalNumber() + "/STS");
					pathSTSFile.mkdirs();
					File4690 file = new File4690(path+"/" + getTerminalNumber() +"/STS/00000000.000");
					if(!file.exists()){
						file.createNewFile();
					}
					FileOutputStream4690 fos = new FileOutputStream4690(file, true);
					fos.write(linea[0].getBytes(), 0, linea[0].length());
					fos.write(linea[1].getBytes(), 0, linea[1].length());
					fos.close();
				} 
				String linea1 = this.data.getPropertyValue("POS_DEVICE", "ANPROMPT_LINE1").toString().trim();
				logger.info("Linea 1: "+ linea1);
				String linea2 = this.data.getPropertyValue("POS_DEVICE", "ANPROMPT_LINE2").toString().trim();
				logger.info("Linea 2: "+ linea2);
				
				linea[0] = "Line 1: : " + linea1 + "\r\n";
				linea[1] = "Line 2: " + linea2 + "\r\n";
				
				if(is4690){
					File4690 pathPPTFile = new File4690(path+"/" + getTerminalNumber() + "/PPT");
					pathPPTFile.mkdirs();
					File4690 file = new File4690(path+"/" + getTerminalNumber() +"/PPT/00000000.000");
					if(!file.exists()){
						file.createNewFile();
					}
					FileOutputStream4690 fos = new FileOutputStream4690(file, true);
					fos.write(linea[0].getBytes(), 0, linea[0].length());
					fos.write(linea[1].getBytes(), 0, linea[1].length());
					fos.close();
				} 
				
				logger.info("Register as cash receipt listener using proxy");
				this.receiptListenerProxy = new CashReceiptListenerProxy(this.data, this);

				logger.info("Register as listener for property value changes");
				this.propertyListenerProxy = new AEFPropertyListenerProxyImpl(this.data, this);
				this.propertyListenerProxy.addAEFPropertyChangeListener("POS_DEVICE", "CASH_DRAWER_OPEN");

				this.propertyListenerProxy.addAEFPropertyChangeListener("POS_DEVICE", "POS_STATE");
				this.propertyListenerProxy.addAEFPropertyChangeListener("POS_DEVICE", "subState");
				this.propertyListenerProxy.addAEFPropertyChangeListener("POS_DEVICE", "ANPROMPT_LINE1");
				this.propertyListenerProxy.addAEFPropertyChangeListener("POS_DEVICE", "ANPROMPT_LINE2");
			}
		}
	}

	private String getTerminalNumber(){
		String tn = terminalNumber;
		while(tn.length() < 3)
			tn = "0" + tn;
		return tn;
	}
	public void process(Properties props, String terminalsData) {
		try {				
			String linea = null;
			int pos = 1;
			File4690 actionFile4690 = null;
			File actionFile = null;
			List terminals = new ArrayList();
			logger.info("Esperando: 15 seg");
			Thread.sleep(15000);
			logger.info("Fin de espera");
			if(terminalsData != null && !terminalsData.isEmpty() && terminalsData.contains(":")){
				String[] data = terminalsData.split(",");
				for(int i = 0; i < data.length; i++){
					String[] terminalData = data[i].split(":");
					terminals.add(terminalData);
					terminalNumber = terminalData[0];
					if(!terminalNumber.endsWith("F")){
						logger.info("Inicializando caja virtual: " + terminalNumber);
						try {
							loadSessionServer(props);
							if (server != null) {
								try {
									loadSession(terminalNumber, false);
								} catch (Exception e){
									//es normal que de exception porque al iniciar la caja demora en subir y se va por timeout el aef
//									logger.error(e.getMessage(), e);
								} finally {
									releaseSession();
									session = null;
								}
							}
						} catch (Exception e){
							logger.error(e.getMessage(), e);
							server = null;
						}
					}
				}				 
			} else {
				String[] terminalData = new String[3];
				//datos de prueba
				terminalNumber = props.getProperty("terminalNumber");
				terminalData[0] = props.getProperty("terminalNumber");
				terminalData[1] = "8181";
				terminalData[2] = "1234";
				terminals.add(terminalData);
			}
			int indexTerminal = 0;
			terminalNumber = ((String[]) terminals.get(indexTerminal))[0];
			logger.info("Terminal: " + terminalNumber);
			boolean isFisico = false;
			if(terminalNumber.endsWith("F")){
				terminalNumber = terminalNumber.substring(0, terminalNumber.length()-1);
				isFisico = true;
				logger.info("Es terminal fï¿½sica.");
			}
			loadSessionServer(props);
			if (server != null) {
				try {
					loadSession(terminalNumber, isFisico);
				} catch (Exception e){
					logger.error(e.getMessage(), e);
					releaseSession();
					session = null;
					server = null;
				}
			}
			while(true){
				try {
					pos = 1;
					borrarArchivo = false;
					String filename = "";
					if(is4690){
						logger.info("Buscando archivos");
						actionFile4690 = getNextActionFile4690();
						filename = actionFile4690.getName();
						logger.info("archivo encontrado: "+actionFile4690.getName());
						if(filename.contains("."))//si tiene extension es para sxarac
							fileSxaracName = filename;
						else
							fileSxaracName = "";
						orden = "";
						creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|STR|"+yyyyMMddHHmmss_format.format(new Date())+"|Iniciando proceso de archivo: " + filename + ".\n", true);
						linea = readSpecifictLineOfFile4690(actionFile4690, pos);
					} else {
						logger.info("Buscando archivos");
						actionFile = getNextActionFile();
						filename = actionFile.getName();
						if(filename.contains("."))//si tiene extension es para sxarac
							fileSxaracName = filename;
						else
							fileSxaracName = "";
						orden = "";
						logger.info("archivo encontrado: "+actionFile.getName());
						linea = readSpecifictLineOfFile(actionFile, pos);
					}
					loadSessionServer(props);
					if (server != null) {
						if(session == null){
							try {
								loadSession(terminalNumber, isFisico);
							} catch (Exception e){
								logger.error(e.getMessage(), e);
								if(indexTerminal+1 == terminals.size())
									indexTerminal = 0;
								else
									indexTerminal++;
								terminalNumber = ((String[]) terminals.get(indexTerminal))[0];
								logger.info("Terminal modificada a: " + terminalNumber);
								creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|WAR|"+yyyyMMddHHmmss_format.format(new Date())+"|Archivo: " + fileSxaracName + " - Terminal modificada a: " + terminalNumber + ".\n", true);
								if(terminalNumber.endsWith("F")){
									terminalNumber = terminalNumber.substring(0, terminalNumber.length()-1);
									isFisico = true;
									logger.info("Es terminal fï¿½sica.");
								} else 
									isFisico = false;
								releaseSession();
								session = null;
								server = null;
								Thread.sleep(1000);
							}
						}
						
						if (this.session != null) {

							POSAutomationProvider automation = session.getPOSAutomationProvider();
							Workstation workstation = this.session.getWorkstation();
							logger.info("Linea completa:"+linea);
							String[] lineas = linea.contains("&") ? linea.split("&") : null;
							int indexLineas = 0;
							if(lineas != null){
								linea = lineas[indexLineas];
							}
							transEnCurso = false;
							if(!subStatesToProcess.contains(posSubstate)){
								//si el estado es diferente a 11042, la caja no esta lista para iniciar una transaccion
								//por lo que debemos cambiar de terminal

								if(indexTerminal+1 == terminals.size())
									indexTerminal = 0;
								else
									indexTerminal++;
								terminalNumber = ((String[]) terminals.get(indexTerminal))[0];
								logger.info("Terminal modificada a: " + terminalNumber);
								creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|WAR|"+yyyyMMddHHmmss_format.format(new Date())+"|Archivo: " + fileSxaracName + " - Terminal modificada a: " + terminalNumber + ".\n", true);
								if(terminalNumber.endsWith("F")){
									terminalNumber = terminalNumber.substring(0, terminalNumber.length()-1);
									isFisico = true;
									logger.info("Es terminal fï¿½sica.");
								} else 
									isFisico = false;
								releaseSession();
								session = null;
								server = null;
								
							} else {
								boolean error = false;
								while(linea != null && !linea.isEmpty() && !error){
									logger.info("linea: "+linea);
									if(linea.startsWith("LGON")){
										if(automation.getOperator() != null)
											automation.forceLogoff();
										int indexSep = linea.indexOf(",");
										String user = linea.substring(4, indexSep);
										String pass = linea.substring(indexSep+1, linea.length());
										automation.logon(user, pass);
									} else if(linea.startsWith("LGOFF")){
										automation.logoff();
									} else if(linea.startsWith("MGRON")){
										workstation.setKeyLockPosition(3);
									} else if(linea.startsWith("MGROFF")){
										workstation.setKeyLockPosition(2);
									} else if(linea.startsWith("SLP")){
										Thread.sleep(Integer.valueOf(linea.substring(3)).intValue());
									} else if(linea.startsWith("ITM")){
										SalesTransaction trans = (SalesTransaction) automation.getTransaction();
										if(trans == null)
											trans = automation.startTransaction();
										if(trans != null){					
											ItemIdentifier itemID = new ItemIdentifierImpl();
											itemID.setItemCode(linea.substring(3, 15));
											if(linea.length() > 15){
												if(linea.substring(15, 16).equalsIgnoreCase("Q"))
													itemID.setQuantity(Integer.valueOf(linea.substring(16, 20)).toString());
												else if(linea.substring(15, 16).equalsIgnoreCase("W"))
													itemID.setWeight(linea.substring(16, 20));
											}
											ArrayList itemList = trans.addItem(itemID);
								             if (itemList != null && !itemList.isEmpty()) {
								               Iterator it = itemList.iterator();
								               while (it.hasNext()) {
								                 
								                 Item item = (Item)it.next();
								                 logger.info("Item added.  Description = " + item.getInfo().getDescription());
								               } 
								             } 	
										}
									} else if(linea.startsWith("TERM")){		
										String termNum = linea.substring(4);
										creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|PRC|"+yyyyMMddHHmmss_format.format(new Date())+"|Modificacion de terminal: " + orden + ".\n", true);
										releaseSession();
										session = null;
										for(int i = 0; i < terminals.size(); i++){
											if(((String[]) terminals.get(i))[0].equalsIgnoreCase(termNum)){
												indexTerminal = i;
											}
										}
										terminalNumber = ((String[]) terminals.get(indexTerminal))[0];
										logger.info("Terminal modificada a: " + terminalNumber);
										creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|WAR|"+yyyyMMddHHmmss_format.format(new Date())+"|Archivo: " + fileSxaracName + " - Terminal modificada a: " + terminalNumber + ".\n", true);
										if(terminalNumber.endsWith("F")){
											terminalNumber = terminalNumber.substring(0, terminalNumber.length()-1);
											isFisico = true;
											logger.info("Es terminal fï¿½sica.");
										} else 
											isFisico = false;
										loadSession(terminalNumber, isFisico);
									} else if(linea.startsWith("OEC")){		
										String orden = linea.substring(3);
										creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|PRC|"+yyyyMMddHHmmss_format.format(new Date())+"|Archivo: " + fileSxaracName + " - Orden: " + orden + ".\n", true);
									} else if(linea.startsWith("SEQ")){		
										logger.info("Procesando: "+ linea);
										if(linea.equalsIgnoreCase("SEQ<61>")){
											logger.info("Procesando desconexion");
											transEnCurso = false;
										} 
										
										//para ambos usamos el mismo formato
										//RR45R3R8RRR71R26
										if(linea.contains("@user")){
											logger.info("Procesando login");
											String user = ((String[]) terminals.get(indexTerminal))[1];
											logger.info("User: " + user);
											user = user.substring(12, 13) + user.substring(14, 15) + user.substring(5, 6) + user.substring(2, 3) + 
													user.substring(3, 4) + user.substring(15, 16) + user.substring(11, 12) + user.substring(7, 8);
											linea = linea.replace("@user", user);
											logger.info("linea" + linea);
											if (posSubstate == SUBSTATES_ASEGURADO) {
												linea = linea.replace(user + "<78>", "");
												logger.info("linea sin users " + linea);
											}
											transEnCurso = true;
										}
										if(linea.contains("@pass")){
											String pass = ((String[]) terminals.get(indexTerminal))[2];
											logger.info("Pass: " + pass);
											pass = pass.substring(12, 13) + pass.substring(14, 15) + pass.substring(5, 6) + pass.substring(2, 3) + 
													pass.substring(3, 4) + pass.substring(15, 16) + pass.substring(11, 12) + pass.substring(7, 8);
											linea = linea.replace("@pass", pass);
											logger.info("linea" + linea);
										}
										
										workstation.sendKeySequence(linea.substring(3));
									} else if(linea.startsWith("CHKST")){	
										//3 digitos estado y lo que viene despues es el tiempo
										int state = Integer.valueOf(linea.substring(5, 8));
										long startTime = Calendar.getInstance().getTimeInMillis();
										int endTimeout = linea.indexOf("DO") > 0 ? linea.indexOf("DO") : linea.length();
										long timeout = Long.valueOf(linea.substring(8, endTimeout));
										boolean timeoutExpired = false;
										while(!timeoutExpired){
											logger.info("Estado actual: "+posState + " - Estado a chequear: "+ state);										
											if(posState == state){
												timeoutExpired = true;
												if(endTimeout < linea.length()){
													logger.info("Procesando Do: "+ linea.substring(endTimeout+2));
													workstation.sendKeySequence(linea.substring(endTimeout+5));
												}
											} else {
												long dif = Calendar.getInstance().getTimeInMillis() - startTime;
												logger.info("Timeout: " + timeout + " - Dif: " + dif);
												if(dif > timeout){
													timeoutExpired = true;
													logger.info("Supero el timeout");
												} else {
													Thread.sleep(300);
												}
											}
										}
										if(posState != state){
											error = true;
											if(is4690)
												moveToError4690(actionFile4690);
											else
												moveToError(actionFile);
										}
									} else if(linea.startsWith("CHKSBST")){	
										//5 digitos estado y lo que viene despues es el tiempo
										int substate = Integer.valueOf(linea.substring(7, 12));
										//Busca que este tanto el subestado que viene como el subestado que se encuentra
										//Puede ser que el subestado del pos sea 11042 y siempre viene 11043
										if (posSubstate == SUBSTATES_DESCONECTADO && substate == SUBSTATES_ASEGURADO) {
											substate = SUBSTATES_DESCONECTADO;
										}
										long startTime = Calendar.getInstance().getTimeInMillis();
										int endTimeout = linea.indexOf("DO") > 0 ? linea.indexOf("DO") : linea.length();
										long timeout = Long.valueOf(linea.substring(12, endTimeout));
										boolean timeoutExpired = false;
										while(!timeoutExpired){
											logger.info("Subestado actual: "+posSubstate + " - Subestado a chequear: "+ substate);
											if(posSubstate == substate){
												timeoutExpired = true;
												if(endTimeout < linea.length()){
//													MANDA EL COMANDO <61><61> QUE SI SE EJECUTA NO CONECTA BIEN
//													if (posSubstate == SUBSTATES_DESCONECTADO) {
//														logger.info("Procesando Do: "+ linea.substring(endTimeout+2));
//														workstation.sendKeySequence(linea.substring(endTimeout+2));	
//													}
												}
											}
											else {
												long dif = Calendar.getInstance().getTimeInMillis() - startTime;
												logger.info("Timeout: " + timeout + " - Dif: " + dif);
												if(dif > timeout){
													timeoutExpired = true;
													logger.info("Supero el timeout");
												} else {
													Thread.sleep(300);
												}
											}
										}
										if(posSubstate != substate){
											error = true;
											if(is4690)
												moveToError4690(actionFile4690);
											else
												moveToError(actionFile);
										}
									}
									if(!error){
										indexLineas++;
										if(lineas != null && indexLineas < lineas.length){									
											linea = lineas[indexLineas];
										} else {
											pos++;
											linea = is4690 ? readSpecifictLineOfFile4690(actionFile4690, pos) : readSpecifictLineOfFile(actionFile, pos);
										}										
									}
								}
								logger.info("termino archivo");
								if(!error){
									if(is4690){
										creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|END|"+yyyyMMddHHmmss_format.format(new Date())+"|Archivo procesado exitosamente: " + fileSxaracName + ".\n", true);
										File4690 newFile = new File4690(outFolder4690, actionFile4690.getName());
										if(newFile.exists())
											newFile.delete();
										actionFile4690.renameTo(new File4690(outFolder4690, actionFile4690.getName()));								
									} else {
										File newFile = new File(outFolder, actionFile.getName());
										if(newFile.exists())
											newFile.delete();
										actionFile.renameTo(new File(outFolder, actionFile.getName()));
									}									
								}
							}	
						} else {
							Thread.sleep(5000);
							logger.info("Unable to obtain AEFSession.  ");
							if(indexTerminal+1 == terminals.size())
								indexTerminal = 0;
							else
								indexTerminal++;
							terminalNumber = ((String[]) terminals.get(indexTerminal))[0];
							logger.info("Terminal modificada a: " + terminalNumber);
							creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|WAR|"+yyyyMMddHHmmss_format.format(new Date())+"|Archivo: " + fileSxaracName + " - Terminal modificada a: " + terminalNumber + ".\n", true);
							if(terminalNumber.endsWith("F")){
								terminalNumber = terminalNumber.substring(0, terminalNumber.length()-1);
								isFisico = true;
								logger.info("Es terminal fï¿½sica.");
							} else 
								isFisico = false;
							server = null;
						}
					} else {
						Thread.sleep(2000);
						logger.info("Unable to connect to AEF SessionServer.  ");
					}
					
				} catch (Exception e) {
					Thread.sleep(1000);
					if(borrarArchivo){
						try {
							logger.info("Termino el procesamiento de Sxarac, forzamos el borrado de archivo");
							if(is4690){
								creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|WAR|"+yyyyMMddHHmmss_format.format(new Date())+"|Terminï¿½ con incidencia el proceso de archivo: " + fileSxaracName + " - Orden: " + orden + ".\n", true);
								File4690 newFile = new File4690(outFolder4690, actionFile4690.getName());
								if(newFile.exists())
									newFile.delete();
								actionFile4690.renameTo(new File4690(outFolder4690, actionFile4690.getName()));								
							} else {
								File newFile = new File(outFolder, actionFile.getName());
								if(newFile.exists())
									newFile.delete();
								actionFile.renameTo(new File(outFolder, actionFile.getName()));
							}
						} catch (Exception ex){
							logger.error(ex.getMessage(), ex);
						}
					} else {
						creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|ERR|"+yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar archivo: " + fileSxaracName + " - Orden: " + orden + ".\n", true);
					}
					logger.error(e.getMessage(), e);

					if(indexTerminal+1 == terminals.size())
						indexTerminal = 0;
					else
						indexTerminal++;
					terminalNumber = ((String[]) terminals.get(indexTerminal))[0];
					logger.info("Terminal modificada a: " + terminalNumber);
					creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|WAR|"+yyyyMMddHHmmss_format.format(new Date())+"|Archivo: " + fileSxaracName + " - Terminal modificada a: " + terminalNumber + ".\n", true);
					if(terminalNumber.endsWith("F")){
						terminalNumber = terminalNumber.substring(0, terminalNumber.length()-1);
						isFisico = true;
						logger.info("Es terminal fï¿½sica.");
					} else 
						isFisico = false;
					releaseSession();
					session = null;
					server = null;
				}
				finally {
					Thread.sleep(1000);
					
				}
			}
			

		} catch (Exception re) {
			creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|ERR|"+yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar archivo: " + fileSxaracName + " - Orden: " + orden + ".\n", true);
			logger.error(re.getMessage(), re);
		}

		releaseSession();
		
	}
	
	private String getEyesFileName(){
		return "F:/allc_pgm/EYES_AGENT_"+ddMMyy_format.format(new Date());
	}
	
	private boolean moveToError(File actionFile){
		logger.info("Moviendo archivo a error");
		File newFile = new File(errorFolder, actionFile.getName());
		if(newFile.exists())
			newFile.delete();
		return actionFile.renameTo(new File(errorFolder, actionFile.getName()));
	}
	
	private boolean moveToError4690(File4690 actionFile4690){
		logger.info("Moviendo archivo a error");
		creaEscribeDataArchivo4690(getEyesFileName(), "NUO_AEF_P|"+hostName+"|1|"+hostAddress+"|"+storeNumber+"|ERR|"+yyyyMMddHHmmss_format.format(new Date())+"|Error procesando archivo: " + actionFile4690.getName() + " - Orden: " + orden + ".\n", true);
		File4690 newFile = new File4690(errorFolder4690, actionFile4690.getName());
		if(newFile.exists())
			newFile.delete();
		return actionFile4690.renameTo(new File4690(errorFolder4690, actionFile4690.getName()));								
	}
	
	private String readSpecifictLineOfFile(File fileName, int row) {

		BufferedReader br = null;
		String linea = "";
		long cont = 0;
		try {

			br = new BufferedReader(new FileReader(fileName));

			while (null != (linea = br.readLine())) {
				cont++;
				if (cont == row) {
					break;
				}
			}
		} catch (Exception e1) {
			System.err.println("A exception occurred: "
					+ e1.getMessage());
			linea = null;
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
		return linea;
	}
	
	private String readSpecifictLineOfFile4690(File4690 file, long row) {

		BufferedReader br = null;
		String linea = "";
		long cont = 0;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream4690(file)));
			while (null != (linea = br.readLine())) {
				cont++;
				if (cont == row) {
					break;
				}
			}
		} catch (Exception e1) {
			System.err.println("A exception occurred: "
					+ e1.getMessage());
			linea = null;
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
		return linea;
	}
	
	/**
	 * 
	 * @param nombreArchivo
	 *            nombreArchivo
	 * @param data
	 *            data
	 * @param append
	 *            true adiciona, false = como si creara el archivo y guardara la
	 *            data
	 */
	private boolean creaEscribeDataArchivo4690(String nombreArchivo, String data, boolean append) {
		FileOutputStream4690 fos = null;
		try {
			File4690 file = new File4690(nombreArchivo);
			if (!file.exists())
				file.createNewFile();
			fos = new FileOutputStream4690(nombreArchivo, append);
			fos.write(data.getBytes(), 0, data.length());
			fos.close();
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		} finally {
			if(fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}
	
	private File4690 getNextActionFile4690() {
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				File4690[] files = inFolder4690.listFiles(new FileFilter4690() {
					public boolean accept(File4690 pathname) {
						return pathname.isFile();
					}
				});
				if (files.length == 0) {
					try {
						
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						System.err.println("A exception occurred: "
								+ e.getMessage());
					}
				} else {
					Arrays.sort(files, new Comparator() {
						public int compare(Object obj1, Object obj2) {
							String name1 = ((File4690) obj1).getName().toUpperCase();
							if(name1.contains("."))
								name1 = name1.split("\\.")[0];
							long sequence1 = 0;
							String name2 = ((File4690) obj2).getName().toUpperCase();
							if(name2.contains("."))
								name2 = name2.split("\\.")[0];
							long sequence2 = 0;
							sequence1 = Long.parseLong(name1);
							sequence2 = Long.parseLong(name2);

							if (sequence1 == sequence2) {
								return 0;
							}
							if (sequence1 < sequence2) {
								return -1;
							}
							return 1;
						}
					});
					this.filesToProcess = Arrays.asList(files).iterator();
				}
			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File4690) this.filesToProcess.next();
	}
	
	private File getNextActionFile() {
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile();
					}
				});
				if (files.length == 0) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						System.err.println("A exception occurred: "
								+ e.getMessage());
					}
				} else {
					Arrays.sort(files, new Comparator() {
						public int compare(Object obj1, Object obj2) {
							String name1 = ((File) obj1).getName().toUpperCase();
							long sequence1 = 0;
							String name2 = ((File) obj2).getName().toUpperCase();
							long sequence2 = 0;
							sequence1 = Long.parseLong(name1);
							sequence2 = Long.parseLong(name2);

							if (sequence1 == sequence2) {
								return 0;
							}
							if (sequence1 < sequence2) {
								return -1;
							}
							return 1;
						}
					});
					this.filesToProcess = Arrays.asList(files).iterator();
				}
			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File) this.filesToProcess.next();
	}
	
	public void initFileNumber(){
		fileNumber = 0;
		while(true){
			fileNumber++;
			if(is4690){
				if(!(new File4690(path+"/"+Integer.valueOf(fileNumber).toString())).exists())
					break;
			} else {
				if(!(new File(path+"/"+Integer.valueOf(fileNumber).toString())).exists())
					break;
			}
		}
	}
	
	
	public void linePrinted(CashReceiptEvent evt) throws RemoteException {
	     logger.info("Cash receipt event received");
	     Collection lines = evt.getRawPrintLines();
	     if (lines != null && lines.size() > 0) {
	       
	       Iterator it = lines.iterator();
	       while (it.hasNext())
	       {
	    	   String line = it.next().toString() + "\r\n";
	    	   if(createNew){
	    		   fileNumber++;
	    		   createNew = false;
	    	   }
	    	   
			try {
				String filename = fileSxaracName != null && !fileSxaracName.isEmpty() ? fileSxaracName : Integer.valueOf(fileNumber).toString();
				if(is4690){
					File4690 pathPRTFile = new File4690(path+"/" + getTerminalNumber() + "/PRT");
					pathPRTFile.mkdirs();
					File4690 file = new File4690(path+"/" + getTerminalNumber() + "/" +"/PRT/"+filename);
					if(!file.exists()){
						file = new File4690(path+"/" + getTerminalNumber() +"/PRT/"+"/"+Integer.valueOf(fileNumber).toString());
						if(file.exists()){
							file.renameTo(new File4690(path+"/" + getTerminalNumber() +"/PRT/"+filename));
							file = new File4690(path+"/" + getTerminalNumber() +"/PRT/"+filename);
						} else {
							file.createNewFile();
						}
					}
					FileOutputStream4690 fos = new FileOutputStream4690(file, true);
					fos.write(line.getBytes(), 0, line.length());
					fos.close();
				} else {
					PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(path+"/" + getTerminalNumber() +"/"+Integer.valueOf(fileNumber).toString(), true)));
					String lineToWrite = line;
					fileaPos.write(lineToWrite, 0, lineToWrite.length());
					fileaPos.close();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
	       
			try {
				String filename = "00000000.000";
				if(is4690){
					File4690 pathPRTFile = new File4690(path+"/" + getTerminalNumber() + "/PRT");
					pathPRTFile.mkdirs();
					File4690 file = new File4690(path+"/" + getTerminalNumber() + "/PRT/"+filename);
					if(!file.exists()){
						file.createNewFile();
					}
					FileOutputStream4690 fos = new FileOutputStream4690(file, true);
					fos.write(line.getBytes(), 0, line.length());
					fos.close();
				} else {
					PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(path+"/"+filename, true)));
					String lineToWrite = line;
					fileaPos.write(lineToWrite, 0, lineToWrite.length());
					fileaPos.close();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
	    	   logger.info("[CashReceipt] " + line);
	       }
	     }  else {
	    	 if("true".equals(evt.getProperty("paperCut"))){
	    		 logger.info("[CashReceipt] CUT PAPER");
	    		 createNew = true;
	    	 }
	     }
	}
	
	public void propertyChanged(AEFPropertyChangeEvent evt)
			throws RemoteException {

		logger.info("propertyChanged: " + evt.getPropertyName());
		if (evt.getCategoryName().equals("POS_DEVICE")
				&& evt.getPropertyName().equals("CASH_DRAWER_OPEN")) {
			if (Boolean.valueOf(evt.getNewValue().toString()).booleanValue()) {
				this.receiptList.addElement("[***  Cash Drawer is OPEN  ***]");
			} else {
				this.receiptList.addElement("[*** Cash Drawer is CLOSED ***]");
			}
		}
		
		String linea = "";		
		String fileExt = "";
		if (evt.getPropertyName().equals("ANPROMPT_LINE1")) {
			logger.info("[Prompt Line1] " + evt.getNewValue().toString().trim());
			linea = "Line 1: " + evt.getNewValue().toString().trim() + "\r\n";
			fileExt = "PPT";
		} else if (evt.getPropertyName().equals("ANPROMPT_LINE2")) {
			logger.info("[Prompt Line2] " + evt.getNewValue().toString().trim());
			linea = "Line 2: " + evt.getNewValue().toString().trim() + "\r\n";
			fileExt = "PPT";
		} else if (evt.getPropertyName().equals("POS_STATE")) {
			logger.info("[POS State] " + evt.getNewValue().toString().trim());
			linea =  "POS State: " + evt.getNewValue().toString().trim() + "\r\n";
			posState = Integer.valueOf(evt.getNewValue().toString().trim());
			fileExt = "STS";
		} else if (evt.getPropertyName().equals("subState")) {
			logger.info("[POS SubState] " + evt.getNewValue().toString().trim() + "\r\n");
			linea = "POS SubState: " + evt.getNewValue().toString().trim();
			posSubstate = Integer.valueOf(evt.getNewValue().toString().trim());
			fileExt = "STS";
			if(posSubstate == 80399){
				//si llega este estado ya Sxarac termino de procesar, por lo que debemos forzar el borrado
				borrarArchivo = true;
			/*}else if(posSubstate == SUBSTATES_ASEGURADO){
				//Terminal asegurado: state: 5, substate: 11043
				// enviar secuencia para desconectar el cajero 
				try {
					logger.info("Terminal asegurado.");
					workstation.sendKeySequence("<61><61>");
				} catch (Exception e){
					logger.error(e.getMessage(), e);
				}*/
			} else if(posSubstate == SUBSTATES_CAMBIO_CAJA){
				//Cambio de caja: state: 1, substate: 10415
				//enviar un correo electronico para que realicen el proceso operativo en el punto de venta.
				try {
					logger.info("Cambio de caja." );
					// mandar mail. usar cierre de tienda.
					EmailSender.sendMail(
							props.getProperty("smtp.user"),
							props.getProperty("smtp.password"),
							props.getProperty("smtp.server"),
							props.getProperty("smtp.user"),
							props.getProperty("nuoAef.CambioCaja.mail.to"),
                            "Alerta NUOAEF, cambio de caja.",
                            "Debe realizar el proceso operativo en el punto de venta.");
				} catch (Exception e){
					logger.error(e.getMessage(), e);
				}
			}
//			if(posSubstate == SUBSTATES_DESCONECTADO){
//				try {
//					logger.info("Escribiendo Mqtt: " + "RojaON");
//					String userMqtt = topicMqtt.substring(6)+"P";
//					Publicar p = new Publicar(brokerMqtt, userMqtt, true, 
//				            false, "", null);
//					p.publish("topic/smx02", 0, "VerdeOFF".getBytes());
//					p.publish("topic/smx02", 0, "AzulOFF".getBytes());
//					p.publish("topic/smx02", 0, "RojaON".getBytes());
//				} catch (Exception e){
//					logger.error(e.getMessage(), e);
//				}
//			} else if(posSubstate == 1008){
//				try {
//					logger.info("Escribiendo Mqtt: " + "AzulON");
//					String userMqtt = topicMqtt.substring(6)+"P";
//					Publicar p = new Publicar(brokerMqtt, userMqtt, true, 
//				            false, "", null);
//					p.publish("topic/smx02", 0, "VerdeOFF".getBytes());
//					p.publish("topic/smx02", 0, "AzulON".getBytes());
//					p.publish("topic/smx02", 0, "RojaOFF".getBytes());
//				} catch (Exception e){
//					logger.error(e.getMessage(), e);
//				}
//			} else if(posSubstate == SUBSTATES_ASEGURADO){
//				try {
//					logger.info("Escribiendo Mqtt: " + "VerdeON");
//					String userMqtt = topicMqtt.substring(6)+"P";
//					Publicar p = new Publicar(brokerMqtt, userMqtt, true, 
//				            false, "", null);
//					p.publish("topic/smx02", 0, "VerdeON".getBytes());
//					p.publish("topic/smx02", 0, "AzulOFF".getBytes());
//					p.publish("topic/smx02", 0, "RojaOFF".getBytes());
//				} catch (Exception e){
//					logger.error(e.getMessage(), e);
//				}
//			}
		}
		if (createNew) {
			fileNumber++;
			createNew = false;
		}

		try {
			String filename = fileSxaracName != null && !fileSxaracName.isEmpty() ? fileSxaracName : Integer.valueOf(fileNumber).toString();
			if(is4690){
				File4690 pathFile = new File4690(path+"/" + getTerminalNumber() + "/" + fileExt);
				pathFile.mkdirs();
				File4690 file = new File4690(path+"/" + getTerminalNumber() + "/" + fileExt + "/"+filename);
				if(!file.exists()){
					file = new File4690(path+"/" + getTerminalNumber() + "/"  + fileExt + "/"+Integer.valueOf(fileNumber).toString());
					if(file.exists()){
						file.renameTo(new File4690(path+"/" + getTerminalNumber() + "/" + fileExt + "/"+filename));
						file = new File4690(path+"/" + getTerminalNumber() + "/" + fileExt + "/"+filename);
					} else {
						file.createNewFile();
					}
				}
				FileOutputStream4690 fos = new FileOutputStream4690(file, true);
				fos.write(linea.getBytes(), 0, linea.length());
				fos.close();
			} else {
				PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(path+"/" + getTerminalNumber() + "/"+Integer.valueOf(fileNumber).toString(), true)));
				String lineToWrite = linea;
				fileaPos.write(lineToWrite, 0, lineToWrite.length());
				fileaPos.close();
			}
//			if (is4690) {
//
//				logger.info("create file: " + path + "/"
//						+ fileExt + ".TXT");
//				File4690 file = new File4690(path + "/"
//						+ fileExt + ".TXT");
//				if (!file.exists())
//					file.createNewFile();
//				FileOutputStream4690 fos = new FileOutputStream4690(path + "/"
//						+ fileExt + ".TXT", true);
//				fos.write(linea.getBytes(), 0, linea.length());
//				fos.close();
//			} else {
//				PrintWriter fileaPos = new PrintWriter(
//						new BufferedWriter(new FileWriter(path + "/"
//								+ fileExt + ".TXT", true)));
//				String lineToWrite = linea + "\n";
//				fileaPos.write(lineToWrite, 0, lineToWrite.length());
//				fileaPos.close();
//			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		try {
			String filename = "00000000.000";
			if(is4690){
				File4690 pathFile = new File4690(path+"/" + getTerminalNumber() + "/" + fileExt);
				pathFile.mkdirs();
				File4690 file = new File4690(path+"/" + getTerminalNumber() + "/" + fileExt +"/" + filename);
				if(!file.exists()){
					file.createNewFile();
				}
				FileOutputStream4690 fos = new FileOutputStream4690(file, true);
				fos.write(linea.getBytes(), 0, linea.length());
				fos.close();
			} else {
				PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(path+"/" + getTerminalNumber() + "/" + filename, true)));
				String lineToWrite = linea;
				fileaPos.write(lineToWrite, 0, lineToWrite.length());
				fileaPos.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void releaseSession() {
		try {
			if (this.session != null) {

				logger.info("Remove all listeners.");
				this.receiptListenerProxy.removeListener();
				this.propertyListenerProxy.removeAEFPropertyChangeListener(
						"POS_DEVICE", "CASH_DRAWER_OPEN");

				this.propertyListenerProxy.removeAEFPropertyChangeListener(
						"POS_DEVICE", "POS_STATE");

				this.propertyListenerProxy.removeAEFPropertyChangeListener(
						"POS_DEVICE", "subState");

				this.propertyListenerProxy.removeAEFPropertyChangeListener(
						"POS_DEVICE", "ANPROMPT_LINE1");

				this.propertyListenerProxy.removeAEFPropertyChangeListener(
						"POS_DEVICE", "ANPROMPT_LINE2");

				logger.info("Release the session.");
				this.session.release();
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
