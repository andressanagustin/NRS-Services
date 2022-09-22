package com.allc.arms.agent.operations.operator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.ParamsUtils;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileInputStream4690;
import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.security.AuthorizationRecord;
import com.ibm.OS4690.security.EnhancedSecurity;
import com.ibm.OS4690.security.EnhancedSecuritySession;

/**
 * Operacion encargado de generar dos archivos con los datos de personales y de
 * permisos de los operadores y modelos que se encuentran en el controlador en
 * los archivos EAMOPERA y ADXCSOUF
 * 
 * @author andres s
 *
 */

public class OperatorLoaderOperation extends AbstractOperation {

	protected static Logger log = Logger.getLogger(OperatorLoaderOperation.class);
	protected String storeNumber = "";
	private String archivo1Name = "";
	private String archivo2Name = "";
	private String carpeta = "";
	private String outFolder = "";
	private OperatorKeyed operatorKeyed = new OperatorKeyed();
	private PropFile properties;
	protected static Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	protected ConnSocketClient socketClient;
	protected String descriptorProceso = "OP_LOAD_O";

	protected boolean init(PropFile pProperties) {
		boolean result = false;
		try {
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			this.properties = pProperties;
			storeNumber = (new Integer(controllerStatusData.getStoreNumber())).toString();
			while (storeNumber.length() < 3)
				storeNumber = "0" + storeNumber;
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
					descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
							+ storeNumber + "|STR|"
							+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando operacion de carga inicial de Operadores.\n",
					true);
			carpeta = properties.getObject("fileUpdaterUp.out.folder.path");
                        if(!(new File4690(carpeta).exists()))
                        {
                            log.info("Creando carpeta ");
                            new File4690(carpeta).mkdirs();
                        }
			outFolder = "/usr/local/NRS/WWW/allc_dat/out/" + storeNumber + "/iniOpe"; // properties.getObject("SUITE_ROOT")
																						// +
																						// properties.getObject("SYNC_OUT");
			// outFolder =
			// "C:\\Users\\tyrone.lopez\\Downloads\\ArmsServer\\ArmsServer\\allc_dat\\out\\"+storeNumber+"\\iniOpe";
			// // properties.getObject("SUITE_ROOT") + properties.getObject("SYNC_OUT");
			archivo1Name = storeNumber + "-1-OperaLoadData"; // EAMOPERA.DAT
			archivo2Name = storeNumber + "-2-OperaLoadData"; // ADXCSOUF.DAT

			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Operator Update Operation iniciada.");
		try {
			operatorKeyed.init(properties);
			// responder ok
			String message = "0";
			StringBuffer sb = new StringBuffer(
					frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + message);
			socket.writeDataSocket(
					Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")));

			if (init(properties)) {
				File4690 archivo1ANT = new File4690(carpeta + File.separator + archivo1Name);
				File4690 archivo2ANT = new File4690(carpeta + File.separator + archivo2Name);
				// anter de terminar la operacion borramos los archivos
				if (archivo1ANT.exists()) {
					log.info("Borramos archivo 1 anterior");
					archivo1ANT.delete();
				}
				if (archivo2ANT.exists()) {
					log.info("Borramos archivo 2 anterior");
					archivo2ANT.delete();
				}

				log.info("Leyendo operadores");
				List codes = readAllOperatorCodes(); // busca en ADXCSOUF.DAT
				List codesIgnore = new ArrayList();
				log.info("Cantidad de codigos de operador: " + codes);
				HashMap codeNivAuto = new HashMap();

				for (int i = 0; i < codes.size(); i++) {
					String reg = operatorKeyed.readOperatorData((String) codes.get(i)); // busca en EAMOPERA.DAT
					if (reg != null) {
						Files.creaEscribeDataArchivo4690(carpeta + File.separator + archivo1Name, reg + "\n", true);
						codeNivAuto.put((String) codes.get(i), reg.substring(18, 20));
					} else {
						String nuevoCod = ((String) codes.get(i)).replace("*", "999999999");
						log.info("operador busqueda segundo metodo codigo: " + nuevoCod);
						String reg2 = operatorKeyed.readOperatorData(nuevoCod); // busca en EAMOPERA.DAT
						if (reg2 != null) {
							Files.creaEscribeDataArchivo4690(carpeta + File.separator + archivo1Name, reg2 + "\n",
									true);
							codeNivAuto.put((String) codes.get(i), reg2.substring(18, 20));
						} else {
							codesIgnore.add(codes.get(i));
							// log.info("Borro codigo:" + (String)codes.get(i) + ", porque no esta en
							// keyed.");
						}
					}
				}
				// le saco los codigos ignorados que no estan en el archivo 1
				// ESTOS CODIGOS QUE ESTAN EN EL ARCHIVO DOS Y NO EN EL UNO SON SOLO USUARIOS DE
				// SISTEMA OPERATIVO
				// ver como sacar datos de usuarios de estos codigos que solo tendrian permisos
				// de sistema operativo
				log.info("Codigos de operador Ignorados por no ser encontrados en el archivo operatorKeyed: "
						+ codesIgnore);
				// codes.removeAll(codesIgnore);
				log.info("Cantidad de codigos de operador para buscar Authorizations level: " + codes);
				boolean resp = readAllLevelAuthorizations(codes, codeNivAuto);

				// finalizamos llamando operacion del server para recibir archivos.
				File4690 archivo1 = new File4690(carpeta + File4690.separator + archivo1Name);
				File4690 archivo2 = new File4690(carpeta + File4690.separator + archivo2Name);
				if (resp && archivo1.exists() && archivo2.exists()) {
					fileSender(archivo1);
					fileSender(archivo2);
					log.info("Operator Update Operation FINALIZO DE FORMA CORRECTA.");

				} else {
					// se podria agregar una respuesta a una operacion para que actualice el estado
					log.info("Operator Update Operation DE FORMA INCORRECTA Y NO SUBIO LOS ARCHIVOS.");
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
							descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress()
									+ "|" + storeNumber + "|ERR|"
									+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Error en el proceso de carga de operadores.\n",
							true);
				}

				Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
						descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|END|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Finalizó el proceso de carga de operadores.\n",
						true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
						descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al iniciar el proceso de carga de operadores.\n",
						true);
			operatorKeyed.closure();
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
						descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error en el proceso de carga de operadores.\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
			log.error(e.getMessage(), e);
			operatorKeyed.closure();
		}
		return false;
	}

	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

	private List readAllOperatorCodes() {
		List codes = new ArrayList();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream4690(properties.getObject("operator.levelAuthorization.filename"))));
			char[] reg = new char[34];
			int offset = 0;
			while (br.read(reg, offset, 34) > 0) {
				String regStr = new String(reg);
				String code = regStr.substring(0, 9).trim();
				if (!codes.contains(code)) {
					codes.add(code);
					log.info("Operator: " + code + " Added");
				}
			}
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}

		return codes;
	}

	private boolean readAllLevelAuthorizations(List codes, HashMap codeNivAuto) {
		EnhancedSecuritySession ess = null;
		int errorStartSession = 0;
		boolean resp = false;
		try {
			EnhancedSecurity ass = new EnhancedSecurity();
			if (ass.isAvailable()) {
				// log.info("Usuario: "+properties.getObject("operator.master.user")+", Clave:
				// "+
				// ParamsUtils.desencriptar(properties.getObject("operator.master.password")));

				/*
				 * try { String currentUser = ass.getCurrentUserID();
				 * log.info("Usuario EnhancedSecurity: "+currentUser); }catch (FlexosException
				 * e) { log.info("error  ass.getCurrentUserID()"); log.error(e.getMessage(), e);
				 * }
				 * 
				 * try { ess = ass.startSessionCurrentUser(); }catch (FlexosException e) {
				 * log.info("ass.startSessionCurrentUser()"); log.error(e.getMessage(), e); }
				 */

				try {
					ess = ass.startSession(properties.getObject("operator.master.user"),
							ParamsUtils.desencriptar(properties.getObject("operator.master.password")));
				} catch (FlexosException e1) {
					log.info("Error Code:" + e1.getReturnCode());
					log.error(e1.getMessage(), e1);
					errorStartSession = 1;				
					// SI NO PUEDE LOGEAR NO DEBERIAMOS CONTINUAR EL PROCESO POR LO QUE COMENTO ESA
					// PARTE
					// GENERAR UN MSJ DE ERROR Y DEVOLVERLO PARA MOSTRAR EN LA SUITE DE CON ERROR DE
					// DATOS DE LOGEO PARA SEGURIDAD MEJORADA
					// com.ibm.OS4690.FlexosException: FLEXOSEXCEPTION (rc=0x80e70003)
				}

				// si es 0 es porque pudimos iniciar session
				if (errorStartSession == 0) {
					log.info("LOGEAMOS EN ESS ess, BUSCAMOS CON LA CLASE.");

					// NUEVO PARA PROBAR METODOS DE CLASES //
					String[] idModel = ess.getModelIds();
					log.info("idModels:" + idModel.toString());
					int sizeModel = ess.getModelRecordCount();
					for (int i = 0; i < sizeModel; i++) {
						try {
							String reg = null;
							AuthorizationRecord mo = null;
							mo = ess.getRecord((String) idModel[i]);
							log.info("idModel:" + (String) idModel[i]);
							log.info("RecordModel(Attributes):" + mo.getAttributes());
							log.info("RecordModel(AuthoriztionLevel):" + mo.getAuthorizationLevel());
							log.info("RecordModel(UserAttributes):" + mo.getUserAttributes());
							reg = mo.getAttributes() + StringUtils.leftPad(mo.getAuthorizationLevel() + "", 2,
									ArmsAgentConstants.Communication.CERO); // agregamos authorizationLevel al final
							reg = reg.concat("MOD");
							if (reg != null) {
								String codeD10 = StringUtils.leftPad(((String) idModel[i]), 10,
										ArmsAgentConstants.Communication.CERO);
								// Files.creaEscribeDataArchivo4690("F:/ALLC/"+archivo2Name, codeD10 + reg
								// +"\n", true);
								Files.creaEscribeDataArchivo4690(carpeta + File.separator + archivo2Name,
										codeD10 + reg + "\n", true);
							} else {
								log.info(
										"Los metodos de ess y manual en la busqueda de archivo de 'level authorizations' retornaron vacios. Reg:"
												+ reg);
							}

						} catch (FlexosException fe) {
							log.info("Error Code:" + fe.getReturnCode());
							log.error(fe.getMessage(), fe);
						}
					}

					String[] idUser = ess.getIds();
					log.info("idUser:" + Arrays.toString(idUser));
					int sizeUser = ess.getRecordCount();
					for (int i = 0; i < sizeUser; i++) {
						try {
							String reg = null;
							AuthorizationRecord us = null;
							us = ess.getRecord((String) idUser[i]);
							log.info("idUser:" + (String) idUser[i]);
							log.info("RecordUser(Attributes):" + us.getAttributes());
							log.info("RecordUser(AuthoriztionLevel):" + us.getAuthorizationLevel());
							log.info("RecordUser(UserAttributes):" + us.getUserAttributes());
							reg = us.getAttributes() + StringUtils.leftPad(us.getAuthorizationLevel() + "", 2,
									ArmsAgentConstants.Communication.CERO); // agregamos authorizationLevel al final
							reg = reg.concat("OPE");

							if (reg != null) {
								String codeD10 = StringUtils.leftPad(((String) idUser[i]), 10,
										ArmsAgentConstants.Communication.CERO);
								// Files.creaEscribeDataArchivo4690("F:/ALLC/"+archivo2Name, codeD10 + reg
								// +"\n", true);
                                                                log.info("code10 " + codeD10 + " reg " + " " +carpeta +" " + archivo2Name);
								Files.creaEscribeDataArchivo4690(carpeta + File4690.separator + archivo2Name,
										codeD10 + reg + "\n", true);
							} else {
								log.info(
										"Los metodos de ess y manual en la busqueda de archivo de 'level authorizations' retornaron vacios. Reg:"
												+ reg);
							}

						} catch (FlexosException fe) {
							log.info("Error Code:" + fe.getReturnCode());
							log.error(fe.getMessage(), fe);
						}
					}
					/////////////////////////////////////////////

					// * METODO VIEJO *//
					/*
					 * for(int i = 0; i < codes.size(); i++){ AuthorizationRecord ar = null; String
					 * reg = null; try { // si tiene autorizacion usamos el metodo de toshiba ass
					 * //try { ar = ess.getRecord((String) codes.get(i)); reg = ar.getAttributes();
					 * //ar.getAuthorizationLevel(); //}catch (FlexosException fe){ //si da alguna
					 * exception por el metodo de ess busco manual // PREGUNTAR A RODRIGO PERO SI DA
					 * EXCEPTION ACA NO DEBERIAMOS AGREGAR MANUAL PERO CREO QUE NO
					 * 
					 * if(fe.getReturnCode() == -2132344831 || fe.getReturnCode() == -2132344812) {
					 * log.info("Exception metodo ess, buscamos manual. Code:"+(String)
					 * codes.get(i)); reg = readLevelAuthorizations((String)
					 * codes.get(i),properties); } else { throw fe; } //}
					 * 
					 * if(reg != null) { log.info("Attributes:" + reg); String codeD10 =
					 * StringUtils.leftPad(((String) codes.get(i)) , 10,
					 * ArmsAgentConstants.Communication.CERO);
					 * //Files.creaEscribeDataArchivo4690("F:/ALLC/"+archivo2Name, codeD10 + reg
					 * +"\n", true); Files.creaEscribeDataArchivo4690(carpeta
					 * +File.separator+archivo2Name, codeD10 + reg +"\n", true); }else { log.
					 * info("Los metodos de ess y manual en la busqueda de archivo de 'level authorizations' retornaron vacios. Reg:"
					 * + reg); }
					 * 
					 * } catch (FlexosException fe) { log.info("Error Code:"+fe.getReturnCode());
					 * log.error(fe.getMessage(), fe); } }
					 */
				} else {
					/*
					 * // si es 1 es porque no pudimos iniciar sesion y buscamos datos a mano.
					 * log.info("Exception metodo ess, buscamos manual."); for(int i = 0; i <
					 * codes.size(); i++){ String reg = null; reg = readLevelAuthorizations((String)
					 * codes.get(i),properties);
					 * 
					 * if(reg != null) { log.info("Attributes:" + reg); String codeD10 =
					 * StringUtils.leftPad(((String) codes.get(i)) , 10,
					 * ArmsAgentConstants.Communication.CERO);
					 * //Files.creaEscribeDataArchivo4690("F:/ALLC/"+archivo2Name, codeD10 + reg
					 * +"\n", true); Files.creaEscribeDataArchivo4690(carpeta
					 * +File.separator+archivo2Name, codeD10 + reg +"\n", true); }else { log.
					 * info("El metodo manual en la busqueda de archivo de 'level authorizations' retornaron vacios. Reg:"
					 * + reg); } }
					 */

				}
			} else
				log.error("EnhancedSecurity No Available");
			// ess.cancel();
			resp = true;
		} catch (Exception e) {
			if (e instanceof FlexosException)
				log.info("FlexosException");
			log.error(e.getMessage(), e);
			resp = false;
		} finally {
			if (ess != null) {
				try {
					log.info("Cerrando session.");
					ess.cancel();
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}

		return resp;
	}

	private String readLevelAuthorizations(String paramCode, PropFile properties) {
		String regLevelAuto = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream4690(properties.getObject("operator.levelAuthorization.filename"))));
			char[] reg = new char[34];
			int offset = 0;
			while (br.read(reg, offset, 34) > 0) {
				String regStr = new String(reg);
				String code = regStr.substring(0, 9).trim();
				if (paramCode.equals(code)) {
					// codes.add(code);
					// log.info("Operator Autorization(nuestro): " + code + " Added");
					byte[] c = new byte[2];
					c[0] = Util.shortToBytes((short) 0)[0];
					c[1] = regStr.substring(18, 19).getBytes()[0];
					// String grupo =
					// StringUtils.leftPad(Integer.toString(Short.valueOf(Util.bytesToShort(c)).intValue()),
					// 3, ArmsAgentConstants.Communication.CERO);
					String user = StringUtils.leftPad(Integer.toString(Short.valueOf(Util.bytesToShort(c)).intValue()),
							3, ArmsAgentConstants.Communication.CERO);

					byte[] d = new byte[2];
					d[0] = Util.shortToBytes((short) 0)[0];
					d[1] = regStr.substring(19, 20).getBytes()[0];
					// String user =
					// StringUtils.leftPad(Integer.toString(Short.valueOf(Util.bytesToShort(d)).intValue()),
					// 3, ArmsAgentConstants.Communication.CERO);
					String grupo = StringUtils.leftPad(Integer.toString(Short.valueOf(Util.bytesToShort(d)).intValue()),
							3, ArmsAgentConstants.Communication.CERO);

					// log.info("Gupo:" + grupo + "-User:" + user);
					List bitesAuto = new ArrayList();
					// int i=0;
					for (int j = 20; j <= 32; j++) {
						byte[] b = new byte[2];
						b[0] = Util.shortToBytes((short) 0)[0];
						b[1] = regStr.substring(j, j + 1).getBytes()[0];
						String biteAuto = StringUtils.leftPad(
								Integer.toBinaryString(Short.valueOf(Util.bytesToShort(b)).intValue()), 8,
								ArmsAgentConstants.Communication.CERO);
						bitesAuto.add(biteAuto);
						// log.info("regStr:-" + biteAuto + "-Bites- posicion: " + j + ", pos list: " +
						// i);
						// i++;
					}

					char reserved = 'N';
					String bite20 = (String) bitesAuto.get(0);
					String bite21 = (String) bitesAuto.get(1);
					String bite22 = (String) bitesAuto.get(2);
					String bite23 = (String) bitesAuto.get(3);
					String bite24 = (String) bitesAuto.get(4);
					String bite25 = (String) bitesAuto.get(5);
					String bite26 = (String) bitesAuto.get(6);
					String bite27 = (String) bitesAuto.get(7);
					String bite28 = (String) bitesAuto.get(8);
					String bite29 = (String) bitesAuto.get(9);
					String bite30 = (String) bitesAuto.get(10);
					String bite31 = (String) bitesAuto.get(11);
					String bite32 = (String) bitesAuto.get(12);

					/*************************** 20 *********************************/
					// log.info("digito 7 pos 20:-" + bite20.substring(0,1));
					regLevelAuto += bite20.substring(0, 1).equals("1") ? "Y" : "N"; // 7 id:1
					regLevelAuto += bite20.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:2
					regLevelAuto += bite20.substring(2, 3).equals("1") ? "Y" : "N"; // 5 id:3
					regLevelAuto += bite20.substring(3, 4).equals("1") ? "Y" : "N"; // 4 id:4
					regLevelAuto += bite20.substring(4, 5).equals("1") ? "Y" : "N"; // 3 id:5
					regLevelAuto += bite20.substring(5, 6).equals("1") ? "Y" : "N"; // 2 id:6
					/*************************** R *********************************/
					regLevelAuto += reserved; // id:7
					regLevelAuto += reserved; // id:8
					/*************************** 32 *********************************/
					regLevelAuto += bite32.substring(0, 1).equals("1") ? "Y" : "N"; // 7 id:9
					regLevelAuto += bite32.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:10
					regLevelAuto += bite32.substring(2, 3).equals("1") ? "Y" : "N"; // 5 id:11
					regLevelAuto += bite32.substring(3, 4).equals("1") ? "Y" : "N"; // 4 id:12
					regLevelAuto += bite32.substring(4, 5).equals("1") ? "Y" : "N"; // 3 id:13
					regLevelAuto += bite32.substring(5, 6).equals("1") ? "Y" : "N"; // 2 id:14
					regLevelAuto += bite32.substring(6, 7).equals("1") ? "Y" : "N"; // 1 id:15
					/*************************** R *********************************/
					regLevelAuto += reserved; // id:16
					regLevelAuto += reserved; // id:17
					/*************************** 21 *********************************/
					regLevelAuto += bite21.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:18
					regLevelAuto += bite21.substring(2, 3).equals("1") ? "Y" : "N"; // 5 id:19
					regLevelAuto += bite21.substring(3, 4).equals("1") ? "Y" : "N"; // 4 id:20
					regLevelAuto += bite21.substring(4, 5).equals("1") ? "Y" : "N"; // 3 id:21
					regLevelAuto += bite21.substring(5, 6).equals("1") ? "Y" : "N"; // 2 id:22
					/*************************** R *********************************/
					regLevelAuto += reserved; // id:23
					regLevelAuto += reserved; // id:24
					/*************************** 31 *********************************/
					regLevelAuto += bite31.substring(0, 1).equals("1") ? "Y" : "N"; // 7 id:25
					regLevelAuto += bite31.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:26
					/*************************** R *********************************/
					regLevelAuto += reserved; // id:27
					regLevelAuto += reserved; // id:28
					regLevelAuto += reserved; // id:29
					regLevelAuto += reserved; // id:30
					regLevelAuto += reserved; // id:31
					regLevelAuto += reserved; // id:32
					/*************************** 30 *********************************/
					regLevelAuto += bite30.substring(0, 1).equals("1") ? "Y" : "N"; // 7 id:33
					regLevelAuto += bite30.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:34
					regLevelAuto += bite30.substring(2, 3).equals("1") ? "Y" : "N"; // 5 id:35
					regLevelAuto += bite30.substring(3, 4).equals("1") ? "Y" : "N"; // 4 id:36
					regLevelAuto += bite30.substring(4, 5).equals("1") ? "Y" : "N"; // 3 id:37
					regLevelAuto += bite30.substring(5, 6).equals("1") ? "Y" : "N"; // 2 id:38
					regLevelAuto += bite30.substring(6, 7).equals("1") ? "Y" : "N"; // 1 id:39
					regLevelAuto += bite30.substring(7, 8).equals("1") ? "Y" : "N"; // 0 id:40
					/*************************** 22 *********************************/
					regLevelAuto += bite22.substring(0, 1).equals("1") ? "Y" : "N"; // 7 id:41
					regLevelAuto += bite22.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:42
					regLevelAuto += bite22.substring(2, 3).equals("1") ? "Y" : "N"; // 5 id:43
					regLevelAuto += bite22.substring(3, 4).equals("1") ? "Y" : "N"; // 4 id:44
					regLevelAuto += bite22.substring(4, 5).equals("1") ? "Y" : "N"; // 3 id:45
					regLevelAuto += bite22.substring(5, 6).equals("1") ? "Y" : "N"; // 2 id:46
					regLevelAuto += bite22.substring(6, 7).equals("1") ? "Y" : "N"; // 1 id:47
					regLevelAuto += bite22.substring(7, 8).equals("1") ? "Y" : "N"; // 0 id:48
					/*************************** R *********************************/
					regLevelAuto += reserved; // id:49
					/*************************** 29 *********************************/
					regLevelAuto += bite29.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:50
					regLevelAuto += bite29.substring(2, 3).equals("1") ? "Y" : "N"; // 5 id:51
					regLevelAuto += bite29.substring(3, 4).equals("1") ? "Y" : "N"; // 4 id:52
					regLevelAuto += bite29.substring(4, 5).equals("1") ? "Y" : "N"; // 3 id:53
					regLevelAuto += bite29.substring(5, 6).equals("1") ? "Y" : "N"; // 2 id:54
					regLevelAuto += bite29.substring(6, 7).equals("1") ? "Y" : "N"; // 1 id:55
					regLevelAuto += bite29.substring(7, 8).equals("1") ? "Y" : "N"; // 0 id:56
					/*************************** 23 *********************************/
					regLevelAuto += bite23.substring(0, 1).equals("1") ? "Y" : "N"; // 7 id:57
					regLevelAuto += bite23.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:58
					regLevelAuto += bite23.substring(2, 3).equals("1") ? "Y" : "N"; // 5 id:59
					regLevelAuto += bite23.substring(3, 4).equals("1") ? "Y" : "N"; // 4 id:60
					/*************************** R *********************************/
					regLevelAuto += reserved; // id:61
					regLevelAuto += reserved; // id:62
					regLevelAuto += reserved; // id:63
					regLevelAuto += reserved; // id:64
					/*************************** 24 *********************************/
					regLevelAuto += bite24.substring(0, 1).equals("1") ? "Y" : "N"; // 7 id:65
					regLevelAuto += bite24.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:66
					/*************************** R *********************************/
					regLevelAuto += reserved; // id:67
					regLevelAuto += reserved; // id:68
					/*************************** 24 *********************************/
					regLevelAuto += bite24.substring(4, 5).equals("1") ? "Y" : "N"; // 3 id:69
					regLevelAuto += bite24.substring(5, 6).equals("1") ? "Y" : "N"; // 2 id:70
					regLevelAuto += bite24.substring(6, 7).equals("1") ? "Y" : "N"; // 1 id:71
					regLevelAuto += bite24.substring(7, 8).equals("1") ? "Y" : "N"; // 0 id:72
					/*************************** R *********************************/
					regLevelAuto += reserved; // id:73
					regLevelAuto += reserved; // id:74
					regLevelAuto += reserved; // id:75
					regLevelAuto += reserved; // id:76
					regLevelAuto += reserved; // id:77
					regLevelAuto += reserved; // id:78
					regLevelAuto += reserved; // id:79
					regLevelAuto += reserved; // id:80
					/*************************** 25 *********************************/
					regLevelAuto += bite25.substring(0, 1).equals("1") ? "Y" : "N"; // 7 id:81
					regLevelAuto += bite25.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:82
					regLevelAuto += bite25.substring(2, 3).equals("1") ? "Y" : "N"; // 5 id:83
					regLevelAuto += bite25.substring(3, 4).equals("1") ? "Y" : "N"; // 4 id:84
					regLevelAuto += bite25.substring(4, 5).equals("1") ? "Y" : "N"; // 3 id:85
					regLevelAuto += bite25.substring(5, 6).equals("1") ? "Y" : "N"; // 2 id:86
					regLevelAuto += bite25.substring(6, 7).equals("1") ? "Y" : "N"; // 1 id:87
					/*************************** R *********************************/
					regLevelAuto += reserved; // id:88
					/*************************** 27 *********************************/
					regLevelAuto += bite27.substring(0, 1).equals("1") ? "Y" : "N"; // 7 id:89
					regLevelAuto += bite27.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:90
					regLevelAuto += bite27.substring(2, 3).equals("1") ? "Y" : "N"; // 5 id:91
					regLevelAuto += bite27.substring(3, 4).equals("1") ? "Y" : "N"; // 4 id:92
					regLevelAuto += bite27.substring(4, 5).equals("1") ? "Y" : "N"; // 3 id:93
					/*************************** R *********************************/
					regLevelAuto += reserved; // id:94
					regLevelAuto += reserved; // id:95
					regLevelAuto += reserved; // id:96
					/*************************** 26 *********************************/
					regLevelAuto += bite26.substring(0, 1).equals("1") ? "Y" : "N"; // 7 id:97
					regLevelAuto += bite26.substring(1, 2).equals("1") ? "Y" : "N"; // 6 id:98
					regLevelAuto += bite26.substring(2, 3).equals("1") ? "Y" : "N"; // 5 id:99
					regLevelAuto += bite26.substring(3, 4).equals("1") ? "Y" : "N"; // 4 id:100
					/*************************** R *********************************/
					regLevelAuto += reserved; // id:101
					regLevelAuto += reserved; // id:102
					regLevelAuto += reserved; // id:103
					regLevelAuto += reserved; // id:104

					regLevelAuto += grupo;
					regLevelAuto += user;
				}
			}

		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}

		return regLevelAuto;
	}

	private void fileSender(File4690 fileToSend) {
		String filename = fileToSend.getName();
		if (filename != null) {

			log.info("Archivo a enviar: " + filename);
			// obtenemos las subcarpetas si existen
			// String subdirs =
			// fileToSend.getAbsolutePath().substring(inFolder.getAbsolutePath().length() +
			// 1, fileToSend.getAbsolutePath().length() - filename.length());

			StringBuffer data = getFrameHeader();
			data.append(ArmsAgentConstants.Communication.FRAME_SEP).append(outFolder + File.separator + filename)
					.append(ArmsAgentConstants.Communication.FRAME_SEP).append(fileToSend.length());
			List list = Arrays.asList(p.split(data.toString()));

			Frame frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
					ArmsAgentConstants.Communication.FRAME_SEP);
			if (frame.loadData()) {
				boolean send = sendFileHeader(frame) && sendFileBytes(fileToSend);

				if (send) {
					log.info("Archivo enviado correctamente.");
					Files.creaEscribeDataArchivo4690(getEyesFileName(),
							descriptorProceso + "|" + properties.getHostName() + "|3|" + properties.getHostAddress()
									+ "|" + storeNumber + "|PRC|"
									+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Archivo enviado: " + filename + ".\n",
							true);
					closeClient();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				} else {
					log.error("Error al enviar el archivo.");
					Files.creaEscribeDataArchivo4690(getEyesFileName(),
							descriptorProceso + "|" + properties.getHostName() + "|3|" + properties.getHostAddress()
									+ "|" + storeNumber + "|ERR|"
									+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Error al enviar el archivo: " + filename + ".\n",
							true);
				}
			}
		}
	}

	protected StringBuffer getFrameHeader() {
		StringBuffer data = new StringBuffer();
		data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
				.append(ArmsAgentConstants.Process.FILE_RECEIVER_OPERATION)
				// .append("19")
				.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000")
				.append(ArmsAgentConstants.Communication.FRAME_SEP).append(storeNumber)
				.append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Communication.TEMP_CONN)
				.append(ArmsAgentConstants.Communication.FRAME_SEP)
				.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
		return data;
	}

	protected boolean connectClient() {
		// log.info("Ip: "+properties.getObject("clientSocket.ipCentral")+", port:
		// "+properties.getInt("clientSocket.port"));
		if (socketClient == null) {
			socketClient = new ConnSocketClient();
			// socketClient.setIpServer("10.160.2.70");
			// String ip = properties.getObject("clientSocket.ipCentral") != null ?
			// properties.getObject("clientSocket.ipCentral") :
			// properties.getObject("clientSocket.ip");
			// socketClient.setIpServer(ip);
			socketClient.setIpServer(properties.getObject("clientSocket.ipCentral"));
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeClient() {
		if (socketClient != null)
			socketClient.closeConnection();
	}

	protected String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected boolean sendFileHeader(Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {

			if (socketClient == null || !socketClient.isConnected())
				connectClient();
			// else
			// log.info("SocketClient: " + socketClient +", isConnected: "+
			// socketClient.isConnected());

			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
			if (socketClient.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0) {
					numberOfBytes = socketClient.readLengthDataSocket();
					if (timeOutCycles == 5) {
						// cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket
						// esté activo
						String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
						if (!socketClient.writeDataSocket(mje)) {
							socketClient.setConnected(false);
							return false;
						}
						timeOutCycles = 0;
					}
					timeOutCycles++;
				}
				if (numberOfBytes > 0) {
					str = socketClient.readDataSocket(numberOfBytes);
					if (StringUtils.isNotBlank(str)) {
						list = Arrays.asList(p.split(str));
						frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsAgentConstants.Communication.FRAME_SEP);
						log.info("Respuesta recibida: " + frameRpta.toString());
						if (frameRpta.getStatusTrama() == 0) {
							return true;
						}
					}
				}
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
	}

	protected boolean sendFileBytes(File4690 fileToSend) {

		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");

		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream4690(fileToSend));
			long totalBytesToRead = fileToSend.length();
			byte[] byteArray = new byte[(int) (totalBytesToRead < 8192 ? totalBytesToRead : 8192)];

			while (bis.read(byteArray) != -1 && totalBytesToRead > 0) {
				socketClient.writeByteArraySocket(byteArray);
				totalBytesToRead = totalBytesToRead - 8192;
				if (totalBytesToRead < 8192 && totalBytesToRead > 0)
					byteArray = new byte[(int) totalBytesToRead];
			}
			bis.close();
			int numberOfBytes = 0;
			int timeOutCycles = 0;
			while (numberOfBytes == 0) {
				numberOfBytes = socketClient.readLengthDataSocket();
				if (timeOutCycles == 5) {
					// cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket
					// esté activo
					String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
					if (!socketClient.writeDataSocket(mje)) {
						socketClient.setConnected(false);
						return false;
					}
					timeOutCycles = 0;
				}
				timeOutCycles++;
			}
			if (numberOfBytes > 0) {
				String str = socketClient.readDataSocket(numberOfBytes);
				if (StringUtils.isNotBlank(str)) {
					List list = Arrays.asList(p.split(str));
					Frame frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
							ArmsAgentConstants.Communication.FRAME_SEP);
					log.info("Respuesta recibida: " + frameRpta.toString());
					if (frameRpta.getStatusTrama() == 0) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
	}

}
