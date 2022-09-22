package com.allc.arms.server.processes.cer.syscard;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.syscard.SyscardFrame;
import com.allc.arms.server.persistence.syscard.SyscardFrameDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class RegisterTramaFromServerProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(RegisterTramaFromServerProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected RandomAccessFile randSeekRead = null;
	protected RandomAccessFile randFileRead = null;
	protected String tramasSeekFileName;
	protected long timeSleep;
	protected String valorEnCero;
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected boolean endProcess = false;
	protected boolean finished = false;
	protected String storeNumber = "";
	protected String tramasFileName = null;
	protected String actualDateDay;
	private Session sesion;
	protected SyscardFrameDAO syscardFrameDAO = new SyscardFrameDAO();

	protected boolean init() {
		boolean result = false;
		try {
			storeNumber = properties.getObject("eyes.store.code");
			while (storeNumber.length() < 3)
				storeNumber = "0" + storeNumber;
			Files.creaEscribeDataArchivo(getEyesFileName(),
					"REG_FRM_SRVR_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber
							+ "|STR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando proceso de registro de tramas syscard desde Server.\n",
					true);
			valorEnCero = Util.rpad(ArmsServerConstants.Communication.CERO, ArmsServerConstants.Communication.SPACE, 20)
					+ ArmsServerConstants.Communication.CRLF;
			tramasSeekFileName = "tramaFromServerSeek.dat";
			timeSleep = 15000;
			actualDateDay = getFechaActual();
			openSession();
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public void run() {
		try {
			if (init()) {
				while (!endProcess) {
					if (readTramas()) {
						Thread.sleep(timeSleep);
					}
				}
				Files.creaEscribeDataArchivo(getEyesFileName(),
						"REG_FRM_SRVR_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|END|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Finaliza el proceso de registro de tramas syscard desde Server.\n",
						true);
			} else
				Files.creaEscribeDataArchivo(getEyesFileName(),
						"REG_FRM_SRVR_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al iniciar el proceso de registro de tramas syscard desde Server.\n",
						true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo(getEyesFileName(),
						"REG_FRM_SRVR_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error en el proceso de registro de tramas syscard desde Server.\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
			log.error(e.getMessage(), e);
		} finally {
			sesion.close();
			sesion = null;
		}
		finished = true;
	}

	public boolean shutdown(long timeToWait) {
		endProcess = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo RegisterTramaFromServerProcess...");
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

	protected boolean readTramas() {
		boolean isStoreClosed = false;
		long tmp = 0;
		String valorPosicion;
		long punteroFile = 0;
		String linea = "";

		try {
			tramasFileName = "tramaFromServer"+actualDateDay+".dat";
			if (tramasFileName == null || tramasFileName.equals(""))
				return false;
			if (!Files.fileExists(tramasSeekFileName)) {
				Files.creaEscribeDataArchivo(tramasSeekFileName, valorEnCero, false);
				Files.creaEscribeDataArchivo(tramasSeekFileName,
						tramasFileName + ArmsServerConstants.Communication.CRLF, true);
			} else {
				tramasFileName = Files.readSpecifictLineOfFile(tramasSeekFileName, 2);
				actualDateDay = tramasFileName.substring(15, 19);
			}

			randSeekRead = new RandomAccessFile(tramasSeekFileName, "r");
			punteroFile = obtieneOffsetSeek(tramasSeekFileName);
			log.info("puntero:" + punteroFile);
			
			if (punteroFile >= 0) {
				File file = new File(tramasFileName);
				if(!file.exists())
					file.createNewFile();
				randFileRead = new RandomAccessFile(tramasFileName, "r");
				randFileRead.seek(punteroFile);
				//utilizamos este contador para que si no avanza el puntero loguee cada 10 ciclos
				int logguerCount = 10;
				while (!endProcess) {
					if(logguerCount == 10) {
						log.info("Leyendo puntosFile:" + (punteroFile + 1));
					} else if(logguerCount == 0) {
						logguerCount = 10;
					}
					if (!Files.fileExists(tramasFileName))
						Files.creaEscribeDataArchivo(tramasFileName, "", true);
					linea = randFileRead.readLine();
					if (null != linea && !linea.trim().equals("")) {
						String[] campos =linea.split(ArmsServerConstants.Communication.MEM_SEP_CHR);
						updateFrame(campos);
						punteroFile = randFileRead.getFilePointer();
						valorPosicion = Util.rpad(String.valueOf(punteroFile), ArmsServerConstants.Communication.SPACE, 20)
								+ ArmsServerConstants.Communication.CRLF;
						Files.creaEscribeDataArchivo(tramasSeekFileName, valorPosicion, false);
						Files.creaEscribeDataArchivo(tramasSeekFileName,
								tramasFileName + ArmsServerConstants.Communication.CRLF, true);
					} else {
						if (!actualDateDay.equals(getFechaActual())) {
							Iterator itFechas = obtenerFechasIntermedias(actualDateDay, getFechaActual()).iterator();
							while (itFechas.hasNext()) {
								actualDateDay = (String) itFechas.next();
								tramasFileName = "tramaFromServer"+actualDateDay+".dat";
								file = new File(tramasFileName);
								if(file.exists()){
									Files.creaEscribeDataArchivo(tramasSeekFileName, valorEnCero, false);
									Files.creaEscribeDataArchivo(tramasSeekFileName,
											tramasFileName + ArmsServerConstants.Communication.CRLF, true);
									randFileRead = new RandomAccessFile(tramasFileName, "r");
									randFileRead.seek(punteroFile);
									break;
								}
							}
						} else{
							logguerCount--;
							try {
								Thread.sleep(timeSleep);
							} catch (InterruptedException e) {
								log.error(e.getMessage(), e);
							}
						}
					}
				}
				try {
					randSeekRead.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				try {
					randFileRead.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else {
				try {
					randSeekRead.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				try {
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return isStoreClosed;
	}
	

	private void updateFrame(String[] data) throws Exception {
		try {
			SyscardFrame syscardFrame =  new SyscardFrame();
			for(int i = 0; i < data.length; i++){
				String campo = (String) data[i];
				
				if(campo.startsWith("MT="))
					syscardFrame.setMessageType(campo.substring(3));
				else if(campo.startsWith("BM1="))
					syscardFrame.setBitMap1(campo.substring(4));
				else if(campo.startsWith("BM2="))
					syscardFrame.setBitMap2(campo.substring(4));
				else if(campo.startsWith("PAC="))
					syscardFrame.setPrimaryAccNum(campo.substring(4));
				else if(campo.startsWith("PC="))
					syscardFrame.setProcessingCode(campo.substring(3));
				else if(campo.startsWith("AM="))
						syscardFrame.setTrxAmount(campo.substring(3));
				else if(campo.startsWith("STAN="))
					syscardFrame.setSystemTraceAuditNum(campo.substring(5));
				else if(campo.startsWith("LTT="))
					syscardFrame.setLocalTrxTime(campo.substring(4));
				else if(campo.startsWith("LTD="))
					syscardFrame.setLocalTrxDate(campo.substring(4));
				else if(campo.startsWith("ED="))
					syscardFrame.setExpirationDate(campo.substring(3));
				else if(campo.startsWith("PEM="))
					syscardFrame.setPosEntryMode(campo.substring(4));
				else if(campo.startsWith("NII="))
					syscardFrame.setNetworkIntID(campo.substring(4));
				else if(campo.startsWith("PCC="))
					syscardFrame.setPosConditionCode(campo.substring(4));
				else if(campo.startsWith("T2D="))
					syscardFrame.setTrack2(campo.substring(4));
				else if(campo.startsWith("RRN="))
					syscardFrame.setRetrievalRefNum(campo.substring(4));
				else if(campo.startsWith("AN="))
					syscardFrame.setAutorizationNum(campo.substring(3));
				else if(campo.startsWith("RC="))
					syscardFrame.setResponseCode(campo.substring(3));
				else if(campo.startsWith("TI="))
					syscardFrame.setTerminalID(campo.length() > 11 ? campo.substring(3, 11) : campo.substring(3));
				else if(campo.startsWith("MI="))
					syscardFrame.setMerchantID(campo.length() > 18 ? campo.substring(3, 18) : campo.substring(3));
				else if(campo.startsWith("T1D="))
					syscardFrame.setTrack1(campo.length() > 84 ? campo.substring(4, 84) : campo.substring(4));
				else if(campo.startsWith("NLA="))
					syscardFrame.setNumLoteAct(campo.length() > 10 ? campo.substring(4, 10) : campo.substring(4));
				else if(campo.startsWith("AD="))
					syscardFrame.setAdditionalData105(campo.length() > 21 ? campo.substring(3, 21) : campo.substring(3));
				else if(campo.startsWith("AD2="))
					syscardFrame.setAdditionalData112(campo.length() > 504 ? campo.substring(4, 504) : campo.substring(4));
				else if(campo.startsWith("AD3="))
					syscardFrame.setAdditionalData114(campo.length() > 6 ? campo.substring(4, 6) : campo.substring(4));
				else if(campo.startsWith("RFNU="))
					syscardFrame.setReservedNatUse(campo.length() > 13 ? campo.substring(5, 13) : campo.substring(5));
				else if(campo.startsWith("AD4="))
					syscardFrame.setAdditionalData120(campo.length() > 504 ? campo.substring(4, 504) : campo.substring(4));
				else if(campo.startsWith("AD5="))
					syscardFrame.setAdditionalData122(campo.length() > 704 ? campo.substring(4, 704) : campo.substring(4));
				else if(campo.startsWith("RFPU="))
					syscardFrame.setReservedPrivUse(campo.length() > 505 ? campo.substring(5, 505) : campo.substring(5));
				else if(campo.startsWith("RV="))
					syscardFrame.setReserved(campo.length() > 503 ? campo.substring(3, 503) : campo.substring(3));
			}
			log.info("SyscardFrame:"+syscardFrame.toString());
			syscardFrameDAO.insertSyscardFrame(sesion, syscardFrame);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void openSession() {
		while (sesion == null) {
			try {
				sesion = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sesion = null;
			}
			if (sesion == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA SESION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	private String getFechaActual() {
		return Util.convertDateToString(new Date(), "MMdd");
	}
	
	private List obtenerFechasIntermedias(String fechaIni, String fechaFin) {
		int mes1 = (new Integer(fechaIni.substring(0, 2))).intValue();
		int mes2 = (new Integer(fechaFin.substring(0, 2))).intValue();
		int dia1 = (new Integer(fechaIni.substring(2, 4))).intValue();
		int dia2 = (new Integer(fechaFin.substring(2, 4)).intValue());
		List fechas = new ArrayList();
		int diaMax = 31;
		int mesMax = 12;
		while (mes1 <= mesMax && mes1 <= mes2) {
			while (dia1 <= diaMax) {
				if (dia1 == diaMax) {
					dia1++;
				} else {
					dia1++;
					String dia = (new Integer(dia1).toString());
					String mes = (new Integer(mes1).toString());
					fechas.add((mes.length() < 2 ? "0" + mes : mes) + (dia.length() < 2 ? "0" + dia : dia));
					if(dia1 == dia2 && mes1 == mes2)
						break;
				}
			}
			if(dia1 == dia2 && mes1 == mes2)
				break;

			dia1 = 0;
			mes1++;
			if(mes1 > mesMax)
				mes1 = 1;
		}
		fechas.add(fechaFin);
		return fechas;
	}
	
	protected long obtieneOffsetSeek(String nombreFileSeek) {
		long punteroFile;
		String data;
		try {
			data = randSeekRead.readLine();
			randSeekRead.seek(0);
			if (null == data)
				punteroFile = 0;
			else
				punteroFile = Long.parseLong(data.replaceAll(" ", ""));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			punteroFile = -1;
		}
		return punteroFile;
	}

}
