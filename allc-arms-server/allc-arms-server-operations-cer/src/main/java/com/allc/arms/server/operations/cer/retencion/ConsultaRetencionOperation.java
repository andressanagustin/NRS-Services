/**
 * 
 */
package com.allc.arms.server.operations.cer.retencion;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.cedpadruc.CedPadRucDAO;
import com.allc.arms.server.persistence.devolucion.DevolucionDAO;
import com.allc.arms.server.persistence.retencion.RetencionDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.entities.CedRuc;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class ConsultaRetencionOperation extends AbstractOperation {
	static Logger log = Logger.getLogger(ConsultaRetencionOperation.class);
	static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	private Session session = null;
	private Session sessionDevs = null;
	
	protected void iniciarSesionArts(String name) {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	protected void iniciarSesionDevs(String name) {
		while (sessionDevs == null) {
			try {
				sessionDevs = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionDevs == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Consulta Retenci�n Operation iniciada...");
		try {
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_RET_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando Factura: "+frame.getBody().get(0)+".\n", true);
			String nroFactura = (String) frame.getBody().get(0);
			//nroFactura = nroFactura.substring(0,3)+"-"+nroFactura.substring(3,6)+"-"+nroFactura.substring(6);
			String fecha = formatDate((String) frame.getBody().get(1));
			String tienda = (String) frame.getBody().get(2);
			String terminal = (String) frame.getBody().get(3);
			while(terminal.length() < 4)
				terminal = "0"+terminal;

			if (nroFactura != null && fecha != null) {
				String message;
				log.info("Nro. de Factura: " + nroFactura + " Fecha: " + fecha);
				
				iniciarSesionArts("Arts");
				RetencionDAO dao = new RetencionDAO();
				CedPadRucDAO cedPadRucDAO = new CedPadRucDAO();
				String esAnul = "";
				String tarjPay = "";
				String retenPay = "";
				String baseImp = "0.0";
				String hayServicio = "0";
				String codCliente = "";
				String nombre = "";
				String totalImp = "";
				Integer codResp = 1;
				CedRuc cedRuc = null;
				boolean isValid = false;
				if(dao.existRetencion(session, (String)frame.getBody().get(0))){
					// si ya se utilizo la factura para retencion
					codResp = 10;
				} else {
					List<String> data = dao.getTrxData(session, nroFactura, fecha);
					if (data == null) {
						codResp = 2;
					} else if(Integer.valueOf(data.get(7).toString()).compareTo(Integer.valueOf(tienda)) != 0){
						codResp = 4;
					} else {
						cedRuc = cedPadRucDAO.getCedRucById(session, data.get(5));
						if (data.get(5) == null || (cedRuc != null && !"R".equals(cedRuc.getTipo().trim())))
							codResp = 5;
						else if (existeDevolucion(data.get(4))) {
							codResp = 7;
						} else {
							if (dao.existeItemServicio(session, data.get(4)))
								hayServicio = "1";
							esAnul = data.get(0);
							log.info("Es Anul:"+esAnul);
							if (!esAnul.equalsIgnoreCase("false")) {
								codResp = 6;
							} else {
								retenPay = data.get(2);
								if (retenPay.compareTo("0") != 0) {
									codResp = 8;
								} else {
									tarjPay = data.get(1);
									if (tarjPay.compareTo("0") != 0) {
										codResp = 9;
									} else {
										isValid = true;
										//le quitamos el .000000 que trae por el tipo de dato en SQL
										baseImp = data.get(3).substring(0,data.get(3).length()-7);
										codCliente = cedRuc.getCodigo();
										nombre = cedRuc.getNombre();
										totalImp = data.get(6).substring(0,data.get(6).length()-7);
									}
								}
							}
						}
					}
				}
				message = codResp + ArmsServerConstants.Communication.FRAME_SEP + baseImp + ArmsServerConstants.Communication.FRAME_SEP + hayServicio + ArmsServerConstants.Communication.FRAME_SEP + codCliente + ArmsServerConstants.Communication.FRAME_SEP + totalImp + ArmsServerConstants.Communication.FRAME_SEP + nombre;
				StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message);
				log.info("Respuesta a enviar:"+sb.toString());
				if(socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")))){
					if(isValid)
						UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_RET_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Factura: "+frame.getBody().get(0)+" válida.\n", true);
					else
						UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_RET_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Factura: "+frame.getBody().get(0)+" no válida.\n", true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_RET_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
			}  else
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_RET_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Requerimiento inválido.\n", true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_RET_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar la Factura: "+frame.getBody().get(0)+".\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private boolean existeDevolucion(String idTrx) {
		iniciarSesionDevs("Devs");
		DevolucionDAO devolucionDAO = new DevolucionDAO();
		return devolucionDAO.existeDevolucion(sessionDevs, idTrx);
	}

	private String formatDate(String date) {
		String ano = date.substring(0, 4);
		String mes = date.substring(4, 6);
		String dia = date.substring(6, 8);
		return dia + "/" + mes + "/" + ano;
	}

	@Override
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
