package com.allc.arms.server.operations.cer.conciliacion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.files.helper.FilesHelper;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;

public class GenerateSyscardConciliacionOperation extends AbstractOperation {
	private Session sesion;
	private Session sesionDevs;
	private Session sessionSaadmin;
	private Session sessionOperador;
	public boolean isEnd = false;
	protected boolean finished = false;
	static Logger log = Logger.getLogger(GenerateSyscardConciliacionOperation.class);
	protected ConnSocketClient socketClient;
	private File outFolderConci;
	private File inFolderConciSyscard;
	private File bkpFolderConciSyscard;
	private File bkpFolderFactElect;
	private int retries;
	private String ipFacElectA = null;
	private String ipFacElectC = null;
	private String usrFacElectA = null;
	private String pswFacElectA = null;
	private String usrFacElectC = null;
	private String pswFacElectC = null;
	private String pathFacElectA = null;
	private String pathFacElectB = null;
	private String pathFacElectC = null;
	private String ipSyscard = null;
	private String usrSyscard = null;
	private String pswSyscard = null;
	private String pathSyscardA = null;

	public void initialize(PropFile properties) {
		try {
			retries = properties.getInt("conciliacion.retries");
			outFolderConci = new File(properties.getObject("conciliacion.out.folder.facelect"));
			outFolderConci.mkdirs();
			inFolderConciSyscard = new File(properties.getObject("conciliacion.in.folder.syscard"));
			inFolderConciSyscard.mkdirs();
			bkpFolderConciSyscard = new File(properties.getObject("conciliacion.bkp.folder.syscard"));
			bkpFolderConciSyscard.mkdirs();
			bkpFolderFactElect = new File(properties.getObject("conciliacion.bkp.folder.facelect"));
			bkpFolderFactElect.mkdirs();

			ipSyscard = properties.getObject("sendFilesSyscard.server.serverFTP");
			usrSyscard = properties.getObject("sendFilesSyscard.username");
			pswSyscard = properties.getObject("sendFilesSyscard.password");
			pathSyscardA = properties.getObject("sendFilesSyscard.ftp.folder.pathA");

			ipFacElectA = properties.getObject("sendFilesFactElect.server.serverFTP");
			usrFacElectA = properties.getObject("sendFilesFactElect.username");
			pswFacElectA = properties.getObject("sendFilesFactElect.password");
			pathFacElectA = properties.getObject("sendFilesFactElect.ftp.folder.path");

			pathFacElectB = properties.getObject("sendFilesFactElect.ftp.folder.pathB");

			ipFacElectC = properties.getObject("sendFilesFactElect.server.serverFTPC");
			usrFacElectC = properties.getObject("sendFilesFactElect.usernameC");
			pswFacElectC = properties.getObject("sendFilesFactElect.passwordC");
			pathFacElectC = properties.getObject("sendFilesFactElect.ftp.folder.pathC");

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		StringBuilder msg = new StringBuilder(frame.getHeaderStr());
		try {
			initialize(properties);
			String storeCode = (String) frame.getBody().get(0);
			String fechaCont = (String) frame.getBody().get(1);
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"GEN_SYS_CON_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando Conciliación de Syscard, Tienda: " + storeCode + ".\n",
					true);
			openSession();
			openSessionDevs();
			openSessionSaAdmin();
			openSessionOperador();
			crearArchivoPuntosExtra(storeCode, fechaCont);
			crearArchivoPagosTrjCER(storeCode, fechaCont);
			crearArchivoRedencionPuntos(storeCode, fechaCont);
			crearArchivoConciFactElect(storeCode, fechaCont);
			crearArchivoConciIlimitadas(storeCode, fechaCont);
			if (crearArchivoGiftcard(storeCode, fechaCont))
				msg.append(frame.getSeparator()).append("0");
			else
				msg.append(frame.getSeparator()).append("1");
			String tmp = Util.addLengthStartOfString(msg.toString(),
					properties.getInt("serverSocket.quantityBytesLength"));
			log.info(tmp);
			if (socket.writeDataSocket(tmp)) {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"GEN_SYS_CON_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|END|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Conciliación de Syscard generada.\n",
						true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"GEN_SYS_CON_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|WAR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|No se pudo enviar la respuesta.\n",
						true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("-1");
				String tmp = Util.addLengthStartOfString(msg.toString(),
						properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				socket.writeDataSocket(tmp);
			} catch (Exception ex) {
				log.error(e.getMessage(), e);
			}
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"GEN_SYS_CON_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al procesar la respuesta.\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		} finally {
			sesion.close();
			sesion = null;
			sesionDevs.close();
			sesionDevs = null;
			sessionOperador.close();
			sessionOperador = null;
			
		}
		finished = true;
		return false;
	}

	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public void openSession() {
		while (sesion == null) {
			try {
				sesion = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
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

	public void openSessionDevs() {
		while (sesionDevs == null) {
			try {
				sesionDevs = HibernateSessionFactoryContainer.getSessionFactory("Devs").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sesionDevs = null;
			}
			if (sesionDevs == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA SESION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	public void openSessionSaAdmin() {
		while (sessionSaadmin == null) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sessionSaadmin = null;
			}
			if (sessionSaadmin == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA SESION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	public void openSessionOperador() {
		while (sessionOperador == null) {
			try {
				sessionOperador = HibernateSessionFactoryContainer.getSessionFactory("Opera").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sessionOperador = null;
			}
			if (sessionOperador == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA SESION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	public boolean crearArchivoGiftcard(String tienda, String fechaCont) {

		boolean fileSend = false;

		log.info("Proceso de conciliación de Giftcard para tienda: " + tienda);

		try {
			String dia = fechaCont.substring(6, 8);
			String mes = fechaCont.substring(4, 6);
			String ano = fechaCont.substring(0, 4);
			
			while(tienda.length()<3)
				tienda = "0" + tienda;

			String fileName = "sg" + tienda + mes + dia + ".txt";

			File giftcardFileDel = new File(inFolderConciSyscard, fileName);
			if (giftcardFileDel.exists())
				giftcardFileDel.delete();

			List<Object[]> data = getGiftcardData(dia + "/" + mes + "/" + ano, Integer.valueOf(tienda));
			File giftcardFile = null;
			BufferedWriter bwr = null;
			if (data != null && !data.isEmpty()) {
				giftcardFile = new File(inFolderConciSyscard, fileName);
				bwr = new BufferedWriter(new FileWriter(giftcardFile, true));
				String codAlm = StringUtils.leftPad(tienda, 4, "0");
				String fechaFormateada = ano + "/" + mes + "/" + dia;
				for (int i = 0; i < data.size(); i++) {
					Object[] row = data.get(i);

					String nombreAlm = row[0].toString().trim();
					String hora = row[1].toString();
					hora = hora.substring(0, 2) + ":" + hora.substring(2, 4) + ":" + hora.substring(4, 6);
					String cedCajero = StringUtils.leftPad(row[2].toString(), 10, "0");
					// TODO: revisar que es esto
					String cajero = row[2].toString().trim();
					while (cajero.length() < 3)
						cajero = "0" + cajero;
					String codCajero = cajero.substring(cajero.length() - 3);
					String estacionAux = row[3].toString();
					String codEstacion = StringUtils.leftPad(estacionAux.substring(estacionAux.length() - 3), 3, "0");
					String ticket = StringUtils.leftPad(row[4].toString(), 6, "0");
					String numTarjeta = StringUtils.leftPad(row[5].toString(), 16, "0");
					String monto = row[6].toString().trim();
					int montoAux = Integer.valueOf(monto);
					String valorMonto = null;
					if (montoAux < 100)
						valorMonto = "0" + String.valueOf(montoAux);
					else
						valorMonto = String.valueOf(montoAux);
					String valorFinalMonto = valorMonto.substring(0, valorMonto.length() - 2) + "."
							+ valorMonto.substring(valorMonto.length() - 2, valorMonto.length());
					String referencia = StringUtils.leftPad(row[7].toString(), 6, "0");
					String autorizacion = StringUtils.leftPad(row[8].toString(), 6, "0");
					String codResp = StringUtils.leftPad(row[9].toString(), 2, "0");
					String estado = row[10].toString();

					StringBuffer sb = new StringBuffer();
					sb.append(codAlm).append(",").append(nombreAlm).append(",").append(fechaFormateada).append(",")
							.append(hora).append(",").append(cedCajero).append(",").append(codCajero).append(",")
							.append(codEstacion).append(",").append(ticket).append(",")
							.append(numTarjeta.substring(0, 6)).append(",").append(numTarjeta.substring(6, 16))
							.append(",").append("").append(",").append(valorFinalMonto).append(",").append(0)
							.append(",").append(referencia).append(",").append(estado.equals("1") ? autorizacion : "")
							.append(",").append(estado.equals("2") ? autorizacion : "").append(",").append(codResp)
							.append(",").append(estado);
					bwr.write(sb.toString());
					bwr.newLine();
				}
			}

			List<Object[]> dataNC = getGiftcardNCData(dia + "/" + mes + "/" + ano, Integer.valueOf(tienda));
			if (dataNC != null && !dataNC.isEmpty()) {
				if (giftcardFile == null) {
					giftcardFile = new File(inFolderConciSyscard, fileName);
					bwr = new BufferedWriter(new FileWriter(giftcardFile, true));
				}
				String codAlm = StringUtils.leftPad(tienda, 4, "0");
				String fechaFormateada = ano + "/" + mes + "/" + dia;
				for (int i = 0; i < dataNC.size(); i++) {
					Object[] row = dataNC.get(i);
					List<Object[]> devsData = getGiftcardDevsData(row[4].toString());
					Object[] rowDevs = devsData.get(0);
					String nombreAlm = row[0].toString().trim();
					String hora = rowDevs[0].toString();
					hora = hora.substring(0, 2) + ":" + hora.substring(2, 4) + ":" + hora.substring(4, 6);
					String cedCajero = StringUtils.leftPad(row[1].toString(), 10, "0");
					// TODO: revisar que es esto
					String cajero = row[1].toString().trim();
					while (cajero.length() < 3)
						cajero = "0" + cajero;
					String codCajero = cajero.substring(cajero.length() - 3);
					String codEstacion = StringUtils.leftPad(row[2].toString(), 3, "0");
					String ticket = StringUtils.leftPad(row[3].toString(), 6, "0");
					String numTarjeta = StringUtils.leftPad(rowDevs[1].toString(), 16, "0");
					String monto = rowDevs[2].toString();
					int montoAux = Integer.valueOf(monto);
					String valorMonto = null;
					if (montoAux < 100)
						valorMonto = "0" + String.valueOf(montoAux);
					else
						valorMonto = String.valueOf(montoAux);
					String valorFinalMonto = valorMonto.substring(0, valorMonto.length() - 2) + "."
							+ valorMonto.substring(valorMonto.length() - 2, valorMonto.length());
					String referencia = StringUtils.leftPad(rowDevs[3].toString(), 6, "0");
					String autorizacion = StringUtils.leftPad(rowDevs[4].toString(), 6, "0");
					String codResp = StringUtils.leftPad(rowDevs[5].toString(), 2, "0");
					String estado = "1";

					StringBuffer sb = new StringBuffer();
					sb.append(codAlm).append(",").append(nombreAlm).append(",").append(fechaFormateada).append(",")
							.append(hora).append(",").append(cedCajero).append(",").append(codCajero).append(",")
							.append(codEstacion).append(",").append(ticket).append(",")
							.append(numTarjeta.substring(0, 6)).append(",").append(numTarjeta.substring(6, 16))
							.append(",").append("").append(",").append(valorFinalMonto).append(",").append(0)
							.append(",").append(referencia).append(",").append(estado.equals("1") ? autorizacion : "")
							.append(",").append(estado.equals("2") ? autorizacion : "").append(",").append(codResp)
							.append(",").append(estado);
					bwr.write(sb.toString());
					bwr.newLine();
				}
			}

			if (bwr != null) {
				bwr.close();
			}
			if (giftcardFile != null && giftcardFile.exists())
				fileSend = sendFilesForConciliacion(ipSyscard, usrSyscard, pswSyscard, pathSyscardA, fileName,
						inFolderConciSyscard);
			if (fileSend && giftcardFile != null && giftcardFile.exists()) {
				// String absolutePathGftOri =
				// inFolderConciSyscard.getAbsolutePath();
				// String absolutePathGftDes =
				// bkpFolderConciSyscard.getAbsolutePath();
				// FilesHelper.copyFile(absolutePathGftOri, absolutePathGftDes,
				// fileName, fileName);
				// giftcardFile.delete();
				File out = new File(bkpFolderConciSyscard, fileName);
				giftcardFile.renameTo(out);
				log.info("Archivo " + fileName + " enviado con exito.");
			} else
				log.info("Archivo " + fileName + " no pudo ser enviado con exito.");

			return true;

		} catch (Exception e) {
			log.info("Error durante el proceso de creacion del archivo Giftcard para la tienda: " + tienda);
			log.error(e.getMessage(), e);
		}

		return false;
	}

	public boolean crearArchivoConciIlimitadas(String tienda, String fechaCont) {

		boolean fileSend = false;

		log.info("Proceso de conciliación de Ilimitadas para tienda: " + tienda);

		try {
			String dia = fechaCont.substring(6, 8);
			String mes = fechaCont.substring(4, 6);
			String ano = fechaCont.substring(0, 4);
			
			while(tienda.length()<3)
				tienda = "0" + tienda;

			String fileName = "si" + tienda + mes + dia + ".txt";

			File ilimitadasFileDel = new File(inFolderConciSyscard, fileName);
			if (ilimitadasFileDel.exists())
				ilimitadasFileDel.delete();

			List<Object[]> data = getIlimitadasData(dia + "/" + mes + "/" + ano, Integer.valueOf(tienda));
			File ilimitadasFile = null;
			BufferedWriter bwr = null;
			if (data != null && !data.isEmpty()) {
				ilimitadasFile = new File(inFolderConciSyscard, fileName);
				bwr = new BufferedWriter(new FileWriter(ilimitadasFile, true));
				String codAlm = StringUtils.leftPad(tienda, 4, "0");
				String fechaFormateada = ano + "/" + mes + "/" + dia;
				for (int i = 0; i < data.size(); i++) {
					Object[] row = data.get(i);

					String nombreAlm = row[0].toString().trim();
					String hora = row[1].toString();
					hora = hora.substring(0, 2) + ":" + hora.substring(2, 4) + ":" + hora.substring(4, 6);
					String cajero = row[2].toString().trim();
					while (cajero.length() < 3)
						cajero = "0" + cajero;
					
					String nameOpr = getNameOperadorByCode(Integer.valueOf(cajero));
					
					String codCajero = cajero.substring(cajero.length() - 3);
					String nombreCajero = nameOpr.trim();
					String tramaSyscard = row[3].toString();
					String identificacionCliente = "";
					String nombreCliente = "";
					String portador = "";
					String nombrePortador = "";

					String ruc = tramaSyscard.substring(115, 130).trim();
					String motivo = tramaSyscard.substring(196, 197).trim();
					String formaPago = tramaSyscard.substring(197, 198).trim();

					if (!ruc.equals("")) {
						identificacionCliente = StringUtils.leftPad(ruc, 16, "0");
						nombreCliente = tramaSyscard.substring(130, 190).trim();
						nombreCliente = nombreCliente.replace(",", " ");
						nombreCliente = nombreCliente.replace("|", " ");
						portador = tramaSyscard.substring(45, 55).trim();
						nombrePortador = tramaSyscard.substring(55, 115).trim();
						nombrePortador = nombrePortador.replace(",", "");
					} else {
						identificacionCliente = StringUtils.leftPad(tramaSyscard.substring(40, 55).trim(), 16, "0");
						nombreCliente = tramaSyscard.substring(55, 115).trim();
						nombreCliente = nombreCliente.replace(",", " ");
						nombreCliente = nombreCliente.replace("|", " ");
						portador = "0000000000";
					}

					String tipoEmision = "";
					String tipoPago = "";

					if (motivo.equals("B") || motivo.equals("D") || motivo.equals("P"))
						tipoEmision = "REPOSICION";
					else if (motivo.equals("C"))
						tipoEmision = "CAMBIO CLTE.";
					else if (motivo.equals("N"))
						tipoEmision = "NUEVA";
					else if (motivo.equals("R"))
						tipoEmision = "RENOVACION";

					if (formaPago.equals("0"))
						tipoPago = "NORMAL";
					else if (formaPago.equals("1"))
						tipoPago = "ORD.COMPRA1";
					else if (formaPago.equals("2"))
						tipoPago = "ORD.COMPRA2";

					String estadoBd = row[4].toString();
					String estado = "";
					if(estadoBd.equals("0"))
						estado = "VENDIDA";
					else
						estado = "DEV/ANUL";
					
					String factura = row[5].toString();
					

					StringBuffer sb = new StringBuffer();
					sb.append(codAlm).append(",").append(nombreAlm).append(",").append(fechaFormateada).append(",")
							.append(codCajero).append(",").append(nombreCajero)
							.append(",").append(identificacionCliente).append(",").append(nombreCliente).append(",")
							.append(portador).append(",").append(nombrePortador).append(",").append(tipoEmision)
							.append(",").append(tipoPago).append(",").append(factura).append(",").append(hora)
							.append(",").append(estado);
					bwr.write(sb.toString());
					bwr.newLine();
				}
			}

			if (bwr != null) {
				bwr.close();
			}
			if (ilimitadasFile != null && ilimitadasFile.exists())
				fileSend = sendFilesForConciliacion(ipSyscard, usrSyscard, pswSyscard, pathSyscardA, fileName,
						inFolderConciSyscard);
			if (fileSend && ilimitadasFile != null && ilimitadasFile.exists()) {
				File out = new File(bkpFolderConciSyscard, fileName);
				ilimitadasFile.renameTo(out);
				log.info("Archivo " + fileName + " enviado con exito.");
			} else
				log.info("Archivo " + fileName + " no pudo ser enviado con exito.");

			return true;

		} catch (Exception e) {
			log.info("Error durante el proceso de creacion del archivo Ilimitadas para la tienda: " + tienda);
			log.error(e.getMessage(), e);
		}

		return false;
	}

	public boolean crearArchivoPuntosExtra(String tienda, String fechaCont) {

		boolean fileSend = false;

		log.info("Proceso de conciliación de Puntos Extras para tienda: " + tienda);

		try {
			String dia = fechaCont.substring(6, 8);
			String mes = fechaCont.substring(4, 6);
			String ano = fechaCont.substring(0, 4);
			List<Object[]> data = getPuntosExtrasData(dia + "/" + mes + "/" + ano, Integer.valueOf(tienda));

			if (data != null && !data.isEmpty()) {
				log.info("Inicia creacion archivo conciliacion Puntos Extras.");
				while(tienda.length()<3)
					tienda = "0" + tienda;
				String fileName = "sa" + tienda	+ ano.substring(2) + mes + dia + ".txt";

				File puntosExFileDel = new File(inFolderConciSyscard, fileName);
				if (puntosExFileDel.exists())
					puntosExFileDel.delete();

				File puntosExFile = new File(inFolderConciSyscard, fileName);
				BufferedWriter bwr = new BufferedWriter(new FileWriter(puntosExFile, true));
				String codAlm = StringUtils.leftPad(tienda, 4, "0");
				String fechaFormateada = ano + "/" + mes + "/" + dia;
				for (int i = 0; i < data.size(); i++) {
					Object[] row = data.get(i);

					String nombreAlm = row[0].toString().trim();
					String codEmpresa = StringUtils.leftPad(row[1] != null ? row[1].toString().substring(4) : "", 6,
							"0");
					String numPromo = StringUtils.leftPad(row[2].toString(), 10, "0");
					String puntos = row[3].toString();

					StringBuffer sb = new StringBuffer();
					sb.append(codAlm).append(",").append(nombreAlm).append(",").append(fechaFormateada).append(",")
							.append(codEmpresa).append(",").append(numPromo).append(",").append(puntos);
					bwr.write(sb.toString());
					bwr.newLine();
				}
				if (bwr != null) {
					bwr.close();
				}

				fileSend = sendFilesForConciliacion(ipSyscard, usrSyscard, pswSyscard, pathSyscardA, fileName,
						inFolderConciSyscard);
				if (fileSend) {
					// String absolutePathPtsExOri =
					// inFolderConciSyscard.getAbsolutePath();
					// String absolutePathPtsExDes =
					// bkpFolderConciSyscard.getAbsolutePath();
					// FilesHelper.copyFile(absolutePathPtsExOri,
					// absolutePathPtsExDes, fileName, fileName);
					// puntosExFile.delete();
					File out = new File(bkpFolderConciSyscard, fileName);
					puntosExFile.renameTo(out);
					log.info("Archivo " + fileName + " enviado con exito.");
				} else
					log.info("Archivo " + fileName + " no pudo ser enviado con exito.");
			}

			return true;

		} catch (Exception e) {
			log.info("Error durante el proceso de creacion del archivo Puntos Extras para la tienda: " + tienda);
			log.error(e.getMessage(), e);
		}

		return false;
	}

	public boolean crearArchivoPagosTrjCER(String tienda, String fechaCont) {

		boolean fileSend = false;

		log.info("Proceso de conciliación de Pagos con tarjeta CER para tienda: " + tienda);

		try {
			String dia = fechaCont.substring(6, 8);
			String mes = fechaCont.substring(4, 6);
			String ano = fechaCont.substring(0, 4);
			List<Object[]> data = getPagoCERData(dia + "/" + mes + "/" + ano, Integer.valueOf(tienda));

			if (data != null && !data.isEmpty()) {
				log.info("Inicia creacion archivo conciliacion de Pagos con tarjeta CER.");
				while(tienda.length()<3)
					tienda = "0" + tienda;
				String fileName = "st" + tienda	+ mes + dia + ".txt";

				File pagosCerFileDel = new File(inFolderConciSyscard, fileName);
				if (pagosCerFileDel.exists())
					pagosCerFileDel.delete();

				File pagosCerFile = new File(inFolderConciSyscard, fileName);
				BufferedWriter bwr = new BufferedWriter(new FileWriter(pagosCerFile, true));
				String codAlm = StringUtils.leftPad(tienda, 4, "0");
				String fechaFormateada = ano + "/" + mes + "/" + dia;
				for (int i = 0; i < data.size(); i++) {
					Object[] row = data.get(i);

					String nombreAlm = row[0].toString().trim();
					String hora = row[1].toString();
					hora = hora.substring(0, 2) + ":" + hora.substring(2, 4) + ":" + hora.substring(4, 6);
					String cedCajero = StringUtils.leftPad(row[2].toString(), 10, "0");
					// TODO: revisar que es esto
					String cajero = row[2].toString().trim();
					while (cajero.length() < 3)
						cajero = "0" + cajero;
					String codCajero = cajero.substring(cajero.length() - 3);
					String estacionAux = row[3].toString();
					String codEstacion = StringUtils.leftPad(estacionAux.substring(estacionAux.length() - 3), 3, "0");
					String ticket = StringUtils.leftPad(row[4].toString(), 6, "0");
					String numTarjeta = StringUtils.leftPad(row[5].toString(), 16, "0");
					String cedTrjHabiente = row[6].toString().trim();
					String monto = row[7].toString();
					Double newMonto = Double.valueOf(monto);
					int montoAux = newMonto.intValue();
					String valorMonto = null;
					if (montoAux < 100)
						valorMonto = "0" + String.valueOf(montoAux);
					else
						valorMonto = String.valueOf(montoAux);
					String valorFinalMonto = valorMonto.substring(0, valorMonto.length() - 2) + "."
							+ valorMonto.substring(valorMonto.length() - 2, valorMonto.length());
					String cupo = row[8].toString();
					Double newCupo = Double.valueOf(cupo);
					int cupoAux = newCupo.intValue();
					String valorCupo = null;
					if (cupoAux < 100)
						valorCupo = "0" + String.valueOf(cupoAux);
					else
						valorCupo = String.valueOf(cupoAux);
					String valorFinalCupo = valorCupo.substring(0, valorCupo.length() - 2) + "."
							+ valorCupo.substring(valorCupo.length() - 2, valorCupo.length());
					String referencia = StringUtils.leftPad(row[9].toString(), 6, "0");
					String autorizacion = StringUtils.leftPad(row[10].toString(), 6, "0");
					String codResp = StringUtils.leftPad(row[11].toString(), 2, "0");
					String estado = row[12].toString();

					StringBuffer sb = new StringBuffer();
					sb.append(codAlm).append(",").append(nombreAlm).append(",").append(fechaFormateada).append(",")
							.append(hora).append(",").append(cedCajero).append(",").append(codCajero).append(",")
							.append(codEstacion).append(",").append(ticket).append(",")
							.append(numTarjeta.substring(0, 6)).append(",").append(numTarjeta.substring(6, 16))
							.append(",").append(cedTrjHabiente).append(",").append(valorFinalMonto).append(",")
							.append(valorFinalCupo).append(",").append(referencia).append(",").append(autorizacion)
							.append(",").append(estado.equals("2") ? autorizacion : "").append(",").append(codResp)
							.append(",").append(estado);
					bwr.write(sb.toString());
					bwr.newLine();
				}
				if (bwr != null) {
					bwr.close();
				}

				fileSend = sendFilesForConciliacion(ipSyscard, usrSyscard, pswSyscard, pathSyscardA, fileName,
						inFolderConciSyscard);
				if (fileSend) {
					// String absolutePathPCerOri =
					// inFolderConciSyscard.getAbsolutePath();
					// String absolutePathPCerDes =
					// bkpFolderConciSyscard.getAbsolutePath();
					// FilesHelper.copyFile(absolutePathPCerOri,
					// absolutePathPCerDes, fileName, fileName);
					// pagosCerFile.delete();
					File out = new File(bkpFolderConciSyscard, fileName);
					pagosCerFile.renameTo(out);
					log.info("Archivo " + fileName + " enviado con exito.");
				} else
					log.info("Archivo " + fileName + " no pudo ser enviado con exito.");
			}

			return true;

		} catch (Exception e) {
			log.info("Error durante el proceso de creacion del archivo de Pagos con tarjeta CER para la tienda: "
					+ tienda);
			log.error(e.getMessage(), e);
		}

		return false;
	}

	public boolean crearArchivoRedencionPuntos(String tienda, String fechaCont) {

		boolean fileSend = false;

		log.info("Proceso de conciliación de Redencion de Puntos para tienda: " + tienda);

		try {
			String dia = fechaCont.substring(6, 8);
			String mes = fechaCont.substring(4, 6);
			String ano = fechaCont.substring(0, 4);

			log.info("Inicia creacion archivo conciliacion de Redencion de Puntos.");
			while(tienda.length()<3)
				tienda = "0" + tienda;
			String fileName = "sr" + tienda + ano.substring(2) + mes + dia + ".txt";

			File rdPtsFileDel = new File(inFolderConciSyscard, fileName);
			if (rdPtsFileDel.exists())
				rdPtsFileDel.delete();

			File rdmPuntosFile = null;
			BufferedWriter bwr = null;

			List<Object[]> data = getRedencionPuntosData(dia + "/" + mes + "/" + ano, Integer.valueOf(tienda));

			if (data != null && !data.isEmpty()) {

				rdmPuntosFile = new File(inFolderConciSyscard, fileName);
				bwr = new BufferedWriter(new FileWriter(rdmPuntosFile, true));
				String codAlm = StringUtils.leftPad(tienda, 4, "0");
				String lastCodNeg = null;
				String desNegocio = null;
				String codNegocio = null;
				String fechaFormateada = ano + "/" + mes + "/" + dia;
				for (int i = 0; i < data.size(); i++) {
					Object[] row = data.get(i);

					String nombreAlm = row[0].toString().trim();
					String hora = row[1].toString();
					hora = hora.substring(0, 2) + ":" + hora.substring(2, 4) + ":" + hora.substring(4, 6);
					String cedCajero = StringUtils.leftPad(row[2].toString(), 10, "0");
					// TODO: revisar que es esto
					String cajero = row[2].toString().trim();
					while (cajero.length() < 3)
						cajero = "0" + cajero;
					String codCajero = cajero.substring(cajero.length() - 3);
					String estacion = row[3].toString().trim();
					String codEstacion = StringUtils.leftPad(estacion.substring(estacion.length() - 3), 3, "0");
					String ticket = StringUtils.leftPad(row[4].toString(), 6, "0");
					String numFactura = StringUtils.leftPad(row[5].toString(), 15, "0");
					String codItem = StringUtils.leftPad(row[6].toString(), 6, "0");
					String desItem = row[7].toString().trim();
					String dsc = row[8].toString();
					String negocio = row[9].toString().trim();

					if (!negocio.equals(lastCodNeg)) {

						List<String> infoNeg = getInfoNegocio(negocio);
						desNegocio = infoNeg.get(1);
						codNegocio = infoNeg.get(0);
					}
					String refNumber = StringUtils.leftPad(row[10].toString(), 6, "0");
					String autorizacion = StringUtils.leftPad(row[11].toString(), 6, "0");
					String tipo = row[12].toString();

					StringBuffer sb = new StringBuffer();
					sb.append(codAlm).append(",").append(nombreAlm).append(",").append(fechaFormateada).append(",")
							.append(hora).append(",").append(cedCajero).append(",").append(codCajero).append(",")
							.append(codEstacion).append(",").append(ticket).append(",").append(numFactura).append(",")
							.append(codItem).append(",").append(desItem).append(",").append(dsc).append(",")
							.append(codNegocio).append(",").append(desNegocio).append(",").append(refNumber).append(",")
							.append(autorizacion).append(",").append(tipo);
					bwr.write(sb.toString());
					bwr.newLine();
					lastCodNeg = negocio;
				}

			}

			List<Object[]> dataNC = getRedencionPuntosNCData(dia + "/" + mes + "/" + ano, Integer.valueOf(tienda));
			if (dataNC != null && !dataNC.isEmpty()) {
				if (rdmPuntosFile == null) {
					rdmPuntosFile = new File(inFolderConciSyscard, fileName);
					bwr = new BufferedWriter(new FileWriter(rdmPuntosFile, true));
				}
				String codAlm = StringUtils.leftPad(tienda, 4, "0");
				String lastCodNeg = null;
				String desNegocio = null;
				String codNegocio = null;
				String fechaFormateada = ano + "/" + mes + "/" + dia;
				for (int i = 0; i < dataNC.size(); i++) {
					Object[] row = dataNC.get(i);

					String nombreAlm = row[0].toString();
					String hora = row[1].toString();
					hora = hora.substring(0, 2) + ":" + hora.substring(2, 4) + ":" + hora.substring(4, 6);
					String cedCajero = StringUtils.leftPad(row[2].toString(), 10, "0");
					// TODO: revisar que es esto
					String cajero = row[2].toString().trim();
					while (cajero.length() < 3)
						cajero = "0" + cajero;
					String codCajero = cajero.substring(cajero.length() - 3);
					String estacion = row[3].toString().trim();
					String codEstacion = StringUtils.leftPad(estacion.substring(estacion.length() - 3), 3, "0");
					String ticket = StringUtils.leftPad(row[5].toString(), 3, "0");
					String numFacturaOri = StringUtils.leftPad(row[6].toString(), 15, "0");
					String codItem = StringUtils.leftPad(row[7].toString(), 6, "0");
					String desItem = row[8].toString();
					String dsc = row[9].toString();
					String negocio = row[9].toString().trim();

					if (!negocio.equals(lastCodNeg)) {

						List<String> infoNeg = getInfoNegocio(negocio);
						desNegocio = infoNeg.get(1);
						codNegocio = infoNeg.get(0);
					}
					String refNumber = StringUtils.leftPad(row[11].toString(), 6, "0");
					String autorizacion = StringUtils.leftPad(row[12].toString(), 6, "0");
					String tipo = row[13].toString();

					StringBuffer sb = new StringBuffer();
					sb.append(codAlm).append(",").append(nombreAlm).append(",").append(fechaFormateada).append(",")
							.append(hora).append(",").append(cedCajero).append(",").append(codCajero).append(",")
							.append(codEstacion).append(",").append(ticket).append(",").append(numFacturaOri)
							.append(",").append(codItem).append(",").append(desItem).append(",").append(dsc).append(",")
							.append(codNegocio).append(",").append(desNegocio).append(",").append(refNumber).append(",")
							.append(autorizacion).append(",").append(tipo);
					bwr.write(sb.toString());
					bwr.newLine();
					lastCodNeg = codNegocio;
				}
			}
			if (bwr != null) {
				bwr.close();
			}

			if (rdmPuntosFile.exists())
				fileSend = sendFilesForConciliacion(ipSyscard, usrSyscard, pswSyscard, pathSyscardA, fileName,
						inFolderConciSyscard);
			if (fileSend && rdmPuntosFile.exists()) {
				// String absolutePathRdmPtsOri =
				// inFolderConciSyscard.getAbsolutePath();
				// String absolutePathRdmPtsDes =
				// bkpFolderConciSyscard.getAbsolutePath();
				// FilesHelper.copyFile(absolutePathRdmPtsOri,
				// absolutePathRdmPtsDes, fileName, fileName);
				// rdmPuntosFile.delete();
				File out = new File(bkpFolderConciSyscard, fileName);
				rdmPuntosFile.renameTo(out);
				log.info("Archivo " + fileName + " enviado con exito.");
			} else
				log.info("Archivo " + fileName + " no pudo ser enviado con exito.");

			return true;

		} catch (Exception e) {
			log.info("Error durante el proceso de creacion del archivo de Redencion de Puntos para la tienda: "
					+ tienda);
			log.error(e.getMessage(), e);
		}

		return false;
	}

	public boolean crearArchivoConciFactElect(String tienda, String fechaCont) {

		boolean fileMoSend = false;
		boolean fileFeSend = false;
		boolean fileDoSend = false;
		// boolean fileMoSendBkp = false;
		// boolean fileFeSendBkp = false;
		// boolean fileDoSendBkp = false;

		log.info("Proceso de conciliación de Factura Electrónica para tienda: " + tienda);

		try {
			String dia = fechaCont.substring(6, 8);
			String mes = fechaCont.substring(4, 6);
			String ano = fechaCont.substring(0, 4);
			
			while(tienda.length()<3)
				tienda = "0" + tienda;

			String fileName = "mo" + tienda + mes + dia + ".txt";

			File factElectFileDel = new File(outFolderConci, fileName);
			if (factElectFileDel.exists())
				factElectFileDel.delete();

			File factElectFile = new File(outFolderConci, fileName);
			BufferedWriter bwr = new BufferedWriter(new FileWriter(factElectFile, true));

			String fechaMo = ano + mes + dia;

			String rucTienda = getRucByTienda(Integer.valueOf(tienda));
			String ruc = "0990004196001";
			String establecimiento = rucTienda.substring(rucTienda.length() - 3);

			Double montoInvoice = getMontoFactPorFecha(fechaCont, Integer.valueOf(tienda));
			Double montoNc = getMontoNcPorFecha(fechaCont, Integer.valueOf(tienda));
			int valorAux = montoInvoice.intValue();
			String valor = null;
			if (valorAux < 100)
				valor = "0" + String.valueOf(valorAux);
			else
				valor = String.valueOf(valorAux);
			String valorFinalInvoice = valor.substring(0, valor.length() - 2) + "."
					+ valor.substring(valor.length() - 2, valor.length());
			int valorAux2 = montoNc.intValue();
			String valor2 = null;
			if (valorAux2 < 100)
				valor2 = "0" + String.valueOf(valorAux2);
			else
				valor2 = String.valueOf(valorAux2);
			String valorFinalNc = valor2.substring(0, valor2.length() - 2) + "."
					+ valor2.substring(valor2.length() - 2, valor2.length());
			bwr.write(ruc + "," + establecimiento + "," + fechaMo + "," + valorFinalInvoice + "," + valorFinalNc);
			bwr.newLine();

			if (bwr != null) {
				bwr.close();
			}

			String doFileName = "do" + tienda + mes + dia + ".txt";
			String doZipFileName = "do" + tienda + mes + dia;
			String zipDir = outFolderConci.getAbsolutePath() + "\\";
			comprimirDoFactElect(zipDir, doFileName, doZipFileName);

			fileMoSend = sendFilesForConciliacion(ipFacElectA, usrFacElectA, pswFacElectA, pathFacElectA, fileName,
					outFolderConci);

			String feFileName = "fe" + tienda + mes + dia + ".txt";

			fileFeSend = sendFilesForConciliacion(ipFacElectA, usrFacElectA, pswFacElectA, pathFacElectA, feFileName,
					outFolderConci);

			fileDoSend = sendFilesForConciliacion(ipFacElectA, usrFacElectA, pswFacElectA, pathFacElectB,
					doZipFileName + ".zip", outFolderConci);

			// fileFeSendBkp = sendFilesForConciliacion(ipFacElectC,
			// usrFacElectC, pswFacElectC, pathFacElectC, feFileName,
			// outFolderConci);
			//
			// fileDoSendBkp = sendFilesForConciliacion(ipFacElectC,
			// usrFacElectC, pswFacElectC, pathFacElectC, doZipFileName+".zip",
			// outFolderConci);
			//
			// fileMoSendBkp = sendFilesForConciliacion(ipFacElectC,
			// usrFacElectC, pswFacElectC, pathFacElectC, fileName,
			// outFolderConci);

			// if (fileMoSend && fileFeSend && fileDoSend && fileMoSendBkp &&
			// fileFeSendBkp && fileDoSendBkp) {
			if (fileMoSend && fileFeSend && fileDoSend) {
				// String absolutePathFactElectOri =
				// outFolderConci.getAbsolutePath();
				// String absolutePathFactElectDes =
				// bkpFolderConciSyscard.getAbsolutePath();
				//
				// FilesHelper.copyFile(absolutePathFactElectOri,
				// absolutePathFactElectDes, fileName, fileName);
				// factElectFile.delete();
				File outMo = new File(bkpFolderFactElect, fileName);
				factElectFile.renameTo(outMo);

				File feFile = new File(outFolderConci, feFileName);
				// FilesHelper.copyFile(absolutePathFactElectOri,
				// absolutePathFactElectDes, feFileName, feFileName);
				// feFile.delete();
				File outFe = new File(bkpFolderFactElect, fileName);
				feFile.renameTo(outFe);

				File doFile = new File(outFolderConci, doFileName + ".txt");
				File doFileZip = new File(outFolderConci, doZipFileName + ".zip");
				// FilesHelper.copyFile(absolutePathFactElectOri,
				// absolutePathFactElectDes, doFileName+".txt",
				// doFileName+".txt");
				// FilesHelper.copyFile(absolutePathFactElectOri,
				// absolutePathFactElectDes, doFileName+".zip",
				// doFileName+".zip");
				// doFile.delete();
				// doFileZip.delete();

				File outDoZip = new File(bkpFolderFactElect, doZipFileName + ".zip");
				File outDoTxt = new File(bkpFolderFactElect, doZipFileName + ".txt");

				doFile.renameTo(outDoTxt);
				doFileZip.renameTo(outDoZip);

				log.info("Archivos enviados con exito.");
			} else {
				// if(!fileMoSend || !fileMoSendBkp)
				if (!fileMoSend)
					log.info("Error al enviar el archivo " + fileName + " hacia algun destino.");
				// if(!fileFeSend || !fileFeSendBkp)
				if (!fileFeSend)
					log.info("Error al enviar el archivo " + feFileName + " hacia algun destino.");
				// if(!fileDoSend || !fileDoSendBkp)
				if (!fileDoSend)
					log.info("Error al enviar el archivo " + doFileName + " hacia algun destino.");
			}

			return true;

		} catch (Exception e) {
			log.info("Error durante el proceso de creacion del archivo de Factura Electronica para la tienda: "
					+ tienda);
			log.error(e.getMessage(), e);
		}

		return false;
	}

	protected void comprimirDoFactElect(String zipDir, String fileName, String zipName) {

		try {
			Files.zippear(zipDir, fileName, zipName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Double getMontoFactPorFecha(String fechaContable, Integer store) {
		try {

			log.info("Fecha para la consulta FACT: " + fechaContable);
			SQLQuery query = sesion.createSQLQuery(
					"SELECT sum(CAST(TR_INVC.TOTAL AS DECIMAL(14,3))) as total FROM TR_INVC, TR_TRN, PA_STR_RTL WHERE TR_INVC.ID_TRN = TR_TRN.ID_TRN and TR_TRN.FL_CNCL <> 1 and TR_TRN.ID_BSN_UN=PA_STR_RTL.ID_BSN_UN and PA_STR_RTL.CD_STR_RT =  "
							+ store + " and CAST(TR_TRN.DC_DY_BSN AS DATE) = CAST(convert(datetime,'" + fechaContable
							+ "',103) AS DATE)");
			List rows = query.list();
			log.info("rows: " + rows.get(0));
			if (rows != null && !rows.isEmpty())
				return Double.valueOf(rows.get(0).toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public Double getMontoNcPorFecha(String fechaContable, Integer store) {
		try {

			log.info("Fecha para la consulta NC: " + fechaContable);
			SQLQuery query = sesion.createSQLQuery(
					"SELECT case when sum(case when TR_LTM_TND.FL_IS_CHNG = 1 then TR_LTM_TND.MO_ITM_LN_TND end) is null then 0 else sum(case when TR_LTM_TND.FL_IS_CHNG = 1 then TR_LTM_TND.MO_ITM_LN_TND end) end as total FROM TR_RTN, TR_LTM_TND, TR_TRN, PA_STR_RTL,  TR_LTM_RTL_TRN WHERE TR_RTN.ID_TRN = TR_LTM_TND.ID_TRN and TR_LTM_TND.ID_TRN = TR_TRN.ID_TRN and TR_LTM_RTL_TRN.ID_TRN = TR_LTM_TND.ID_TRN and TR_LTM_RTL_TRN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM and TR_TRN.ID_BSN_UN=PA_STR_RTL.ID_BSN_UN and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and PA_STR_RTL.CD_STR_RT =  "
							+ store + " and CAST(TR_TRN.DC_DY_BSN AS DATE) = CAST(convert(datetime,'" + fechaContable
							+ "',103) AS DATE)");
			List rows = query.list();
			log.info("rows: " + rows.get(0));
			if (rows != null && !rows.isEmpty())
				return Double.valueOf(rows.get(0).toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getRedencionPuntosData(String fecha, Integer store) {
		try {
			SQLQuery query = sesion.createSQLQuery(
					"SELECT S.DE_STR_RT, C.TM, O.CD_OPR, W.CD_WS, T.AI_TRN, F.INVC_NMB, C.CD_ITM, I.DE_ITM, C.DSC, C.CD_NGC, C.REF_NBR, C.AUT_NBR, C.TYP FROM TR_TRN T, CO_PTS_RDM_DT C, PA_OPR O, AS_WS W, PA_STR_RTL S, TR_INVC F, AS_ITM I WHERE T.ID_TRN = C.ID_TRN and  C.ID_TRN = F.ID_TRN AND T.ID_OPR = O.ID_OPR AND T.ID_WS = W.ID_WS AND T.ID_BSN_UN = S.ID_BSN_UN AND C.CD_ITM = I.CD_ITM AND T.FL_CNCL <> 1 AND S.CD_STR_RT = "
							+ store + " AND CAST(T.DC_DY_BSN AS DATE) = CAST(convert(datetime,'" + fecha
							+ "',103) AS DATE) Group by S.DE_STR_RT, C.TM, O.CD_OPR, W.CD_WS, T.AI_TRN, F.INVC_NMB, C.CD_ITM, I.DE_ITM, C.DSC, C.CD_NGC, C.REF_NBR, C.AUT_NBR, C.TYP");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getRedencionPuntosNCData(String fecha, Integer store) {
		try {
			SQLQuery query = sesion.createSQLQuery(
					"SELECT S.DE_STR_RT, C.TM, O.CD_OPR, W.CD_WS, T.AI_TRN, NC.ORGL_INVC_NMB, C.CD_ITM, I.DE_ITM, C.DSC, C.CD_NGC, C.REF_NBR, C.AUT_NBR, C.TYP FROM TR_TRN T, CO_PTS_RDM_DT C, PA_OPR O, AS_WS W, PA_STR_RTL S, TR_RTN NC, AS_ITM I  WHERE T.ID_TRN = C.ID_TRN and  C.ID_TRN = NC.ID_TRN AND T.ID_OPR = O.ID_OPR AND T.ID_WS = W.ID_WS AND T.ID_BSN_UN = S.ID_BSN_UN AND C.CD_ITM = I.CD_ITM AND T.FL_CNCL = 0 AND S.CD_STR_RT = "
							+ store + " AND CAST(T.DC_DY_BSN AS DATE) = CAST(convert(datetime,'" + fecha
							+ "',103) AS DATE) Group by S.DE_STR_RT, C.TM, O.CD_OPR, W.CD_WS, T.AI_TRN, F.INVC_NMB, C.CD_ITM, I.DE_ITM, C.DSC, C.CD_NGC, C.REF_NBR, C.AUT_NBR, C.TYP");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getPagoCERData(String fecha, Integer store) {
		try {
			log.info(
					"SELECT S.DE_STR_RT, MIN(CD.TM), O.CD_OPR, W.CD_WS, T.AI_TRN, CD.CRD_NBR, CD.CD_CST, MAX(CD.AMNT) AS AMNT, MIN(CD.CP_DSP) AS CP_DSP, MIN(CAST(CD.SEQ_TRX AS INT)) AS SEQ_TRX, MAX(CD.ACT_CD) AS ACT_CD, MAX(CD.CD_RSP) AS CD_RSP, MAX(CD.STS_CRD) AS STS_CRD FROM TR_TRN T, CO_CRD_DT CD, PA_OPR O, AS_WS W, PA_STR_RTL S, TR_LTM_TND LTM, AS_TND TND WHERE T.ID_TRN = CD.ID_TRN AND T.ID_OPR = O.ID_OPR AND T.ID_WS = W.ID_WS AND T.ID_BSN_UN = S.ID_BSN_UN AND T.ID_TRN = LTM.ID_TRN AND CD.SQ_NBR = LTM.AI_LN_ITM AND LTM.ID_TND = TND.ID_TND AND TND.TY_TND < 55 AND TND.TY_TND > 50"
							+ " AND S.CD_STR_RT = " + store + " AND CAST(T.DC_DY_BSN AS DATE) = CAST(convert(datetime,'"
							+ fecha
							+ "',103) AS DATE)  GROUP BY CD.ACT_CD, S.DE_STR_RT, O.CD_OPR, W.CD_WS, T.AI_TRN, CD.CRD_NBR, CD.CD_CST ");
			SQLQuery query = sesion.createSQLQuery(
					"SELECT S.DE_STR_RT, MIN(CD.TM), O.CD_OPR, W.CD_WS, T.AI_TRN, CD.CRD_NBR, CD.CD_CST, MAX(CD.AMNT) AS AMNT, MIN(CD.CP_DSP) AS CP_DSP, MIN(CAST(CD.SEQ_TRX AS INT)) AS SEQ_TRX, MAX(CD.ACT_CD) AS ACT_CD, MAX(CD.CD_RSP) AS CD_RSP, MAX(CD.STS_CRD) AS STS_CRD FROM TR_TRN T, CO_CRD_DT CD, PA_OPR O, AS_WS W, PA_STR_RTL S, TR_LTM_TND LTM, AS_TND TND WHERE T.ID_TRN = CD.ID_TRN AND T.ID_OPR = O.ID_OPR AND T.ID_WS = W.ID_WS AND T.ID_BSN_UN = S.ID_BSN_UN AND T.ID_TRN = LTM.ID_TRN AND CD.SQ_NBR = LTM.AI_LN_ITM AND LTM.ID_TND = TND.ID_TND AND TND.TY_TND < 55 AND TND.TY_TND > 50"
							+ " AND T.FL_CNCL = 0 AND S.CD_STR_RT = " + store + " AND CAST(T.DC_DY_BSN AS DATE) = CAST(convert(datetime,'"
							+ fecha
							+ "',103) AS DATE)  GROUP BY CD.ACT_CD, S.DE_STR_RT, O.CD_OPR, W.CD_WS, T.AI_TRN, CD.CRD_NBR, CD.CD_CST ");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getGiftcardData(String fecha, Integer store) {
		try {
			SQLQuery query = sesion.createSQLQuery(
					"SELECT S.DE_STR_RT, G.TM, O.CD_OPR, W.CD_WS, T.AI_TRN, G.CRD_NBR, G.AMNT, G.REF_NBR, G.AUT_NBR, G.RSP_CD, G.STS FROM TR_TRN T, CO_GFTCRD_DT G, PA_OPR O, AS_WS W, PA_STR_RTL S WHERE T.ID_TRN = G.ID_TRN AND T.ID_OPR = O.ID_OPR AND T.ID_WS = W.ID_WS AND T.ID_BSN_UN = S.ID_BSN_UN"
							+ " AND T.FL_CNCL = 0 AND S.CD_STR_RT = " + store
							+ " AND CAST(T.DC_DY_BSN AS DATE) = CAST(convert(datetime,'" + fecha + "',103) AS DATE)");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getGiftcardNCData(String fecha, Integer store) {
		try {
			SQLQuery query = sesion.createSQLQuery(
					"SELECT S.DE_STR_RT, O.CD_OPR, W.CD_WS, T.AI_TRN, R.RTN_NMB FROM TR_TRN T, PA_OPR O, AS_WS W, PA_STR_RTL S, TR_RTN R, TR_LTM_TND LT, AS_TND TND WHERE T.ID_OPR = O.ID_OPR AND T.ID_WS = W.ID_WS AND T.ID_BSN_UN = S.ID_BSN_UN AND T.ID_TRN = R.ID_TRN AND T.ID_TRN = LT.ID_TRN AND LT.ID_TND = TND.ID_TND AND T.FL_CNCL = 0 AND TND.TY_TND = '11' AND LT.ID_ACNT_TND IS NOT NULL AND LT.FL_IS_CHNG = 1 AND R.TY_RTN = 10 AND S.CD_STR_RT = "
							+ store + " AND CAST(T.DC_DY_BSN AS DATE) = CAST(convert(datetime,'" + fecha
							+ "',103) AS DATE)");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getGiftcardDevsData(String notaCred) {
		try {
			SQLQuery query = sesionDevs.createSQLQuery(
					"Select DV_GFCD.TM, DV_GFCD.CRD_NBR, DV_GFCD.AMNT, DV_GFCD.REF_NBR, DV_GFCD.AUT_NBR, DV_GFCD.RESP_CODE From DV_GFCD, DV_TICKET Where DV_TICKET.ID_DEV = DV_GFCD.ID_DEV and DV_TICKET.NOTA = '"
							+ notaCred + "'");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getIlimitadasData(String fecha, Integer store) {
		try {
			SQLQuery query = sesion.createSQLQuery(
					"SELECT S.DE_STR_RT,I.TM,O.CD_OPR,I.ILIM_DT,I.FL_VD,CASE WHEN F.INVC_NMB IS NULL THEN '000000000000000' ELSE F.INVC_NMB END FROM PA_OPR O, PA_STR_RTL S , CO_ILIM_DT I, TR_TRN T LEFT JOIN TR_INVC F ON T.ID_TRN = F.ID_TRN WHERE I.ID_TRN = T.ID_TRN AND T.ID_OPR = O.ID_OPR AND T.ID_BSN_UN = S.ID_BSN_UN AND T.FL_CNCL = 0 AND S.CD_STR_RT = "
							+ store + " AND CAST(T.DC_DY_BSN AS DATE) = CAST(convert(datetime,'" + fecha
							+ "',103) AS DATE)");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getPuntosExtrasData(String fecha, Integer store) {
		try {
			SQLQuery query = sesion.createSQLQuery(
					"SELECT S.DE_STR_RT, V.DES_PRM, SUBSTRING (P.CD_PRM, 1, 10), SUM(CASE WHEN FL_RV = 0 THEN P.PTS ELSE -P.PTS END) AS PTS FROM PA_STR_RTL S, CO_PTS_DT P LEFT JOIN CO_PRM_PRV V ON SUBSTRING (P.CD_PRM, 1, 10)  = V.CD_PRM WHERE S.CD_STR_RT = P.CD_STR_RT"
							+ " AND P.CD_STR_RT = " + store + " AND CAST(P.DC_DY_BSN AS DATE) = CAST(convert(datetime,'"
							+ fecha
							+ "',103) AS DATE) GROUP BY S.DE_STR_RT, P.DC_DY_BSN, SUBSTRING (P.CD_PRM, 1, 10), V.DES_PRM");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public List<String> getInfoNegocio(String negocio) {
		try {

			SQLQuery query = sessionSaadmin
					.createSQLQuery("Select COD_LINEA, DES_NEGOCIO From MN_NEGOCIO Where COD_SAP  = '" + negocio + "'");
			List<Object[]> rows = query.list();
			for (Object[] row : rows) {
				List<String> retorno = new ArrayList<String>();
				retorno.add(row[0] != null ? row[0].toString() : null);
				retorno.add(row[1] != null ? row[1].toString() : null);
				return retorno;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public String getRucByTienda(Integer tienda) {
		try {

			SQLQuery query = sessionSaadmin
					.createSQLQuery("Select RUC_TIENDA From MN_TIENDA Where DES_CLAVE  = " + tienda);
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (String) rows.get(0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public String getNameOperadorByCode(Integer operador) {
		try {

			SQLQuery query = sessionOperador
					.createSQLQuery("SELECT concat(NOMBRE,' ',APELLIDO_M,' ',APELLIDO_P) FROM OP_OPERADOR WHERE CC_OPERADOR = " + operador);
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (String) rows.get(0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo ReceiveResponseSyscardOperation...");
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
		log.info("Finalizo la Operacion de Recepción de Respuestas de Syscard.");
		return true;
	}

	public boolean sendFilesForConciliacion(String server, String usuario, String password, String dir,
			String nombreFileToSend, File outFolder) {

		com.jcraft.jsch.Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;
		int reintentos = 0;
		String ip = server;
		String usr = usuario;
		String psw = password;
		String path = dir;
		int SFTPPORT = 22;

		log.info("Inicio de transferencia de archivo: " + nombreFileToSend);

		while (reintentos < retries) {
			log.info("Archivo a transferir: " + nombreFileToSend);
			try {
				if (ip != null && usr != null && psw != null && path != null) {
					JSch jsch = new JSch();
					session = jsch.getSession(usr, ip, SFTPPORT);
					session.setPassword(psw);
					java.util.Properties config = new java.util.Properties();
					config.put("StrictHostKeyChecking", "no");
					session.setConfig(config);
					session.connect();
					channel = session.openChannel("sftp");
					channel.connect();
					channelSftp = (ChannelSftp) channel;
					channelSftp.cd(path);
					File f = new File(outFolder, nombreFileToSend);
					FileInputStream inputStream = new FileInputStream(f);
					channelSftp.put(inputStream, f.getName());
					log.info("Archivo " + nombreFileToSend + " transferido con exito.");

					try {
						inputStream.close();
						channelSftp.exit();
						channel.disconnect();
						session.disconnect();
					} catch (Exception ex) {
						log.error(ex.getMessage(), ex);
					}
					return true;
				} else
					reintentos = retries;
			} catch (Exception e) {
				log.error("Se produjo un error durante el intento de transferencia.");
				log.error(e.getMessage(), e);
				reintentos++;

			}
		}
		return false;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
