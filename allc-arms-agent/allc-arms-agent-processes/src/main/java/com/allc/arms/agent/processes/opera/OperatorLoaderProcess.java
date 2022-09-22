package com.allc.arms.agent.processes.opera;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.agent.operations.operator.OperatorKeyed;
import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.ParamsUtils;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.FileInputStream4690;
import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.security.AuthorizationRecord;
import com.ibm.OS4690.security.EnhancedSecurity;
import com.ibm.OS4690.security.EnhancedSecuritySession;

/**
 * Proceso encargado de generar dos archivos con los datos de personales y de permisos de los operadores y modelos 
 * que se encuentran en el controlador en los archivos EAMOPERA y ADXCSOUF
 * 
 * @author gustavo
 * @modificado andres
 *
 */
public class OperatorLoaderProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(OperatorLoaderProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected boolean endTSLProcess = false;
	protected boolean finished = false;
	protected String storeNumber = "";
	private OperatorKeyed operatorKeyed = new OperatorKeyed();
	
	protected boolean init() {
		boolean result = false;
		try {
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			storeNumber = (new Integer(controllerStatusData.getStoreNumber())).toString();
			while(storeNumber.length() < 3)
				storeNumber = "0" + storeNumber;
			Files.creaEscribeDataArchivo4690(getEyesFileName(), "OP_LOAD_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando proceso de carga de Operadores.\n", true);
			
			operatorKeyed.init(properties);
			
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public void run() {
		try {
			if (init()) {
				log.info("Leyendo operadores");
				List codes = readAllOperatorCodes(properties); // busca en ADXCSOUF.DAT
				List codesIgnore = new ArrayList();
				log.info("Cantidad de codigos de operador: " + codes);
				HashMap codeNivAuto = new HashMap();
				
				for(int i = 0; i < codes.size(); i++){
					String reg = operatorKeyed.readOperatorData((String)codes.get(i));  // busca en EAMOPERA.DAT
					if(reg != null)
					{
						Files.creaEscribeDataArchivo4690("F:/ALLC/"+storeNumber+"-1-OperaLoadData", reg +"\n", true);
						codeNivAuto.put((String)codes.get(i), reg.substring(18, 20));
					}else {
						String nuevoCod = ((String)codes.get(i)).replace("*", "999999999");
						log.info("operador busqueda segundo metodo codigo: " + nuevoCod);
						String reg2 = operatorKeyed.readOperatorData(nuevoCod);  // busca en EAMOPERA.DAT
						if(reg2 != null)
						{
							Files.creaEscribeDataArchivo4690("F:/ALLC/"+storeNumber+"-1-OperaLoadData", reg2 +"\n", true);
							codeNivAuto.put((String)codes.get(i), reg2.substring(18, 20));
						}else {
							codesIgnore.add(codes.get(i));
							log.info("Borro codigo:" + (String)codes.get(i) + ", porque no esta en keyed.");
						}	
					}
				}
				// le saco los codigos ignorados que no estan en el archivo 1
				log.info("Codigos de operador Ignorados por no ser encontrados en el archivo operatorKeyed: " + codesIgnore);
				codes.removeAll(codesIgnore);
				log.info("Cantidad de codigos de operador para buscar Authorizations level: " + codes);
				readAllLevelAuthorizations(codes, properties,codeNivAuto);
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "OP_LOAD_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Finalizó el proceso de carga de operadores.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "OP_LOAD_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al iniciar el proceso de carga de operadores.\n", true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "OP_LOAD_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error en el proceso de carga de operadores.\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
			log.error(e.getMessage(), e);
		} finally {
			try {
				if(operatorKeyed != null)
					operatorKeyed.closure();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		finished = true;
	}

	public boolean shutdown(long timeToWait) {
		operatorKeyed.closure();
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo TSLReader...");
		while (!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					return false;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		return true;
	}

	private List readAllOperatorCodes(PropFile properties){
		List codes = new ArrayList();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream4690(properties.getObject("operator.levelAuthorization.filename"))));
			char[] reg = new char[34];
			int offset = 0;
			while(br.read(reg, offset, 34) > 0){
				String regStr = new String(reg);
				String code = regStr.substring(0, 9).trim();
				if(!codes.contains(code)){
					codes.add(code);
					log.info("Operator: " + code + " Added");
				}
			}
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (IOException e) {
			}
		}
		
		return codes;
	}
	

	private boolean readAllLevelAuthorizations(List codes, PropFile properties, HashMap codeNivAuto) {
		EnhancedSecuritySession ess = null;
		try {
			EnhancedSecurity ass = new EnhancedSecurity();
			if (ass.isAvailable()) {
				ess = ass.startSession(properties.getObject("operator.master.user"), ParamsUtils.desencriptar(properties.getObject("operator.master.password")));
				for(int i = 0; i < codes.size(); i++){
					AuthorizationRecord ar = null;
					String reg = null;
					try {

						// si tiene autorizacion usamos el metodo de toshiba ass
						try {
							ar = ess.getRecord((String) codes.get(i));
							reg = ar.getAttributes();
						}catch (FlexosException fe){
							//si da alguna exception por el metodo de  ess busco manual
							if(fe.getReturnCode() == -2132344831 || fe.getReturnCode() == -2132344812)
							{	
								log.info("Exception metodo ess, buscamos manual. Code:"+(String) codes.get(i));
								reg = readLevelAuthorizations((String) codes.get(i),properties);
							}
							else
							{
								throw fe;	
							}
						}
						
						if(reg != null) {
							log.info("Attributes:" + reg);
							String codeD10 = StringUtils.leftPad(((String) codes.get(i)) , 10, ArmsAgentConstants.Communication.CERO);
							Files.creaEscribeDataArchivo4690("F:/ALLC/"+storeNumber+"-2-OperaLoadData", codeD10 + reg +"\n", true);
						}else {
							log.info("Los metodos de ess y manual en la busqueda de archivo de 'level authorizations' retornaron vacios. Reg:" + reg);
						}
						
					} catch (FlexosException fe) {
						log.info("Error Code:"+fe.getReturnCode());
						log.error(fe.getMessage(), fe);
					}
				}
				//verifico si quedo abierta la session y la cierro.
                if (ess != null) {
                    try {
                        log.info("cerrando session");
                        ess.cancel();
                        ess = null;
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
			} else
				log.error("EnhancedSecurity No Available");
		} catch (FlexosException e) {
			log.error(e.getMessage(), e);
			if (ess != null) {
				try {
					log.info("Cancelando session.");
					ess.cancel();
					ess = null;
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
			return false;
		}

		return true;
	}
		
	private String readLevelAuthorizations(String paramCode,PropFile properties) {
		String regLevelAuto = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream4690(properties.getObject("operator.levelAuthorization.filename"))));
			char[] reg = new char[34];
			int offset = 0;
			while(br.read(reg, offset, 34) > 0){
				String regStr = new String(reg);
				String code = regStr.substring(0, 9).trim();
				if(paramCode.equals(code)){
					//codes.add(code);
					//log.info("Operator Autorization(nuestro): " + code + " Added");
					byte[] c = new byte[2];
					c[0] = Util.shortToBytes((short)0)[0];
					c[1] = regStr.substring(18, 19).getBytes()[0];
					//String grupo = StringUtils.leftPad(Integer.toString(Short.valueOf(Util.bytesToShort(c)).intValue()), 3, ArmsAgentConstants.Communication.CERO);
					String user = StringUtils.leftPad(Integer.toString(Short.valueOf(Util.bytesToShort(c)).intValue()), 3, ArmsAgentConstants.Communication.CERO);
					
					byte[] d = new byte[2];
					d[0] = Util.shortToBytes((short)0)[0];
					d[1] = regStr.substring(19, 20).getBytes()[0];
					//String user = StringUtils.leftPad(Integer.toString(Short.valueOf(Util.bytesToShort(d)).intValue()), 3, ArmsAgentConstants.Communication.CERO);
					String grupo = StringUtils.leftPad(Integer.toString(Short.valueOf(Util.bytesToShort(d)).intValue()), 3, ArmsAgentConstants.Communication.CERO);
					
					//log.info("Gupo:" + grupo + "-User:" + user);
					List bitesAuto = new ArrayList();
					//int i=0;
					for(int j=20; j<=32;j++)
					{
						byte[] b = new byte[2];
						b[0] = Util.shortToBytes((short)0)[0];
						b[1] = regStr.substring(j, j+1).getBytes()[0];
						String biteAuto = StringUtils.leftPad(Integer.toBinaryString(Short.valueOf(Util.bytesToShort(b)).intValue()) , 8, ArmsAgentConstants.Communication.CERO);
						bitesAuto.add(biteAuto);
						//log.info("regStr:-" + biteAuto + "-Bites- posicion: " + j + ", pos list: " + i);
						//i++;
					}
					
					char reserved = 'N';
					String bite20 = (String)bitesAuto.get(0);
					String bite21 = (String)bitesAuto.get(1);
					String bite22 = (String)bitesAuto.get(2);
					String bite23 = (String)bitesAuto.get(3);
					String bite24 = (String)bitesAuto.get(4);
					String bite25 = (String)bitesAuto.get(5);
					String bite26 = (String)bitesAuto.get(6);
					String bite27 = (String)bitesAuto.get(7);
					String bite28 = (String)bitesAuto.get(8);
					String bite29 = (String)bitesAuto.get(9);
					String bite30 = (String)bitesAuto.get(10);
					String bite31 = (String)bitesAuto.get(11);
					String bite32 = (String)bitesAuto.get(12);
					
					/*************************** 20 *********************************/
					//log.info("digito 7 pos 20:-" + bite20.substring(0,1));
					regLevelAuto += bite20.substring(0,1).equals("1") ? "Y" : "N" ; //7  id:1
					regLevelAuto += bite20.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:2
					regLevelAuto += bite20.substring(2,3).equals("1") ? "Y" : "N" ; //5  id:3
					regLevelAuto += bite20.substring(3,4).equals("1") ? "Y" : "N" ; //4  id:4
					regLevelAuto += bite20.substring(4,5).equals("1") ? "Y" : "N" ; //3  id:5
					regLevelAuto += bite20.substring(5,6).equals("1") ? "Y" : "N" ; //2  id:6
					/*************************** R *********************************/
					regLevelAuto += reserved;									//  id:7
					regLevelAuto += reserved;									//  id:8
					/*************************** 32 *********************************/
					regLevelAuto += bite32.substring(0,1).equals("1") ? "Y" : "N" ; //7  id:9
					regLevelAuto += bite32.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:10
					regLevelAuto += bite32.substring(2,3).equals("1") ? "Y" : "N" ; //5  id:11
					regLevelAuto += bite32.substring(3,4).equals("1") ? "Y" : "N" ; //4  id:12
					regLevelAuto += bite32.substring(4,5).equals("1") ? "Y" : "N" ; //3  id:13
					regLevelAuto += bite32.substring(5,6).equals("1") ? "Y" : "N" ; //2  id:14
					regLevelAuto += bite32.substring(6,7).equals("1") ? "Y" : "N" ; //1  id:15
					/*************************** R *********************************/
					regLevelAuto += reserved;									//  id:16
					regLevelAuto += reserved;									//  id:17
					/*************************** 21 *********************************/
					regLevelAuto += bite21.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:18
					regLevelAuto += bite21.substring(2,3).equals("1") ? "Y" : "N" ; //5  id:19
					regLevelAuto += bite21.substring(3,4).equals("1") ? "Y" : "N" ; //4  id:20
					regLevelAuto += bite21.substring(4,5).equals("1") ? "Y" : "N" ; //3  id:21
					regLevelAuto += bite21.substring(5,6).equals("1") ? "Y" : "N" ; //2  id:22
					/*************************** R *********************************/
					regLevelAuto += reserved;									//  id:23
					regLevelAuto += reserved;									//  id:24
					/*************************** 31 *********************************/
					regLevelAuto += bite31.substring(0,1).equals("1") ? "Y" : "N" ; //7  id:25
					regLevelAuto += bite31.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:26
					/*************************** R *********************************/
					regLevelAuto += reserved;									//  id:27
					regLevelAuto += reserved;									//  id:28
					regLevelAuto += reserved;									//  id:29
					regLevelAuto += reserved;									//  id:30
					regLevelAuto += reserved;									//  id:31
					regLevelAuto += reserved;									//  id:32
					/*************************** 30 *********************************/
					regLevelAuto += bite30.substring(0,1).equals("1") ? "Y" : "N" ; //7  id:33
					regLevelAuto += bite30.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:34
					regLevelAuto += bite30.substring(2,3).equals("1") ? "Y" : "N" ; //5  id:35
					regLevelAuto += bite30.substring(3,4).equals("1") ? "Y" : "N" ; //4  id:36
					regLevelAuto += bite30.substring(4,5).equals("1") ? "Y" : "N" ; //3  id:37
					regLevelAuto += bite30.substring(5,6).equals("1") ? "Y" : "N" ; //2  id:38
					regLevelAuto += bite30.substring(6,7).equals("1") ? "Y" : "N" ; //1  id:39
					regLevelAuto += bite30.substring(7,8).equals("1") ? "Y" : "N" ; //0  id:40
					/*************************** 22 *********************************/
					regLevelAuto += bite22.substring(0,1).equals("1") ? "Y" : "N" ; //7  id:41
					regLevelAuto += bite22.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:42
					regLevelAuto += bite22.substring(2,3).equals("1") ? "Y" : "N" ; //5  id:43
					regLevelAuto += bite22.substring(3,4).equals("1") ? "Y" : "N" ; //4  id:44
					regLevelAuto += bite22.substring(4,5).equals("1") ? "Y" : "N" ; //3  id:45
					regLevelAuto += bite22.substring(5,6).equals("1") ? "Y" : "N" ; //2  id:46
					regLevelAuto += bite22.substring(6,7).equals("1") ? "Y" : "N" ; //1  id:47
					regLevelAuto += bite22.substring(7,8).equals("1") ? "Y" : "N" ; //0  id:48
					/*************************** R *********************************/
					regLevelAuto += reserved;									//  id:49
					/*************************** 29 *********************************/
					regLevelAuto += bite29.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:50
					regLevelAuto += bite29.substring(2,3).equals("1") ? "Y" : "N" ; //5  id:51
					regLevelAuto += bite29.substring(3,4).equals("1") ? "Y" : "N" ; //4  id:52
					regLevelAuto += bite29.substring(4,5).equals("1") ? "Y" : "N" ; //3  id:53
					regLevelAuto += bite29.substring(5,6).equals("1") ? "Y" : "N" ; //2  id:54
					regLevelAuto += bite29.substring(6,7).equals("1") ? "Y" : "N" ; //1  id:55
					regLevelAuto += bite29.substring(7,8).equals("1") ? "Y" : "N" ; //0  id:56
					/*************************** 23 *********************************/
					regLevelAuto += bite23.substring(0,1).equals("1") ? "Y" : "N" ; //7  id:57
					regLevelAuto += bite23.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:58
					regLevelAuto += bite23.substring(2,3).equals("1") ? "Y" : "N" ; //5  id:59
					regLevelAuto += bite23.substring(3,4).equals("1") ? "Y" : "N" ; //4  id:60
					/*************************** R *********************************/
					regLevelAuto += reserved;									//  id:61
					regLevelAuto += reserved;									//  id:62
					regLevelAuto += reserved;									//  id:63
					regLevelAuto += reserved;									//  id:64
					/*************************** 24 *********************************/
					regLevelAuto += bite24.substring(0,1).equals("1") ? "Y" : "N" ; //7  id:65
					regLevelAuto += bite24.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:66
					/*************************** R *********************************/
					regLevelAuto += reserved;									//  id:67
					regLevelAuto += reserved;									//  id:68
					/*************************** 24 *********************************/
					regLevelAuto += bite24.substring(4,5).equals("1") ? "Y" : "N" ; //3  id:69
					regLevelAuto += bite24.substring(5,6).equals("1") ? "Y" : "N" ; //2  id:70
					regLevelAuto += bite24.substring(6,7).equals("1") ? "Y" : "N" ; //1  id:71
					regLevelAuto += bite24.substring(7,8).equals("1") ? "Y" : "N" ; //0  id:72
					/*************************** R *********************************/
					regLevelAuto += reserved;									//  id:73
					regLevelAuto += reserved;									//  id:74
					regLevelAuto += reserved;									//  id:75
					regLevelAuto += reserved;									//  id:76
					regLevelAuto += reserved;									//  id:77
					regLevelAuto += reserved;									//  id:78
					regLevelAuto += reserved;									//  id:79
					regLevelAuto += reserved;									//  id:80
					/*************************** 25 *********************************/
					regLevelAuto += bite25.substring(0,1).equals("1") ? "Y" : "N" ; //7  id:81
					regLevelAuto += bite25.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:82
					regLevelAuto += bite25.substring(2,3).equals("1") ? "Y" : "N" ; //5  id:83
					regLevelAuto += bite25.substring(3,4).equals("1") ? "Y" : "N" ; //4  id:84
					regLevelAuto += bite25.substring(4,5).equals("1") ? "Y" : "N" ; //3  id:85
					regLevelAuto += bite25.substring(5,6).equals("1") ? "Y" : "N" ; //2  id:86
					regLevelAuto += bite25.substring(6,7).equals("1") ? "Y" : "N" ; //1  id:87
					/*************************** R *********************************/
					regLevelAuto += reserved;									//  id:88
					/*************************** 27 *********************************/
					regLevelAuto += bite27.substring(0,1).equals("1") ? "Y" : "N" ; //7  id:89
					regLevelAuto += bite27.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:90
					regLevelAuto += bite27.substring(2,3).equals("1") ? "Y" : "N" ; //5  id:91
					regLevelAuto += bite27.substring(3,4).equals("1") ? "Y" : "N" ; //4  id:92
					regLevelAuto += bite27.substring(4,5).equals("1") ? "Y" : "N" ; //3  id:93
					/*************************** R *********************************/
					regLevelAuto += reserved;									//  id:94
					regLevelAuto += reserved;									//  id:95
					regLevelAuto += reserved;									//  id:96
					/*************************** 26 *********************************/
					regLevelAuto += bite26.substring(0,1).equals("1") ? "Y" : "N" ; //7  id:97
					regLevelAuto += bite26.substring(1,2).equals("1") ? "Y" : "N" ; //6  id:98
					regLevelAuto += bite26.substring(2,3).equals("1") ? "Y" : "N" ; //5  id:99
					regLevelAuto += bite26.substring(3,4).equals("1") ? "Y" : "N" ; //4  id:100
					/*************************** R *********************************/
					regLevelAuto += reserved;									//  id:101
					regLevelAuto += reserved;									//  id:102
					regLevelAuto += reserved;									//  id:103
					regLevelAuto += reserved;									//  id:104
					
					regLevelAuto += grupo;
					regLevelAuto += user;
				}
			}
		
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (IOException e) {
			}
		}
		
		return regLevelAuto;
	}
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

}
