/**
 * 
 */
package com.allc.arms.server.operations.cer.devolucion;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class ConsultaDevolucionOperation extends AbstractOperation {
	private Logger logger = Logger.getLogger(ConsultaDevolucionOperation.class);
	private Session session = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.operation.AbstractOperation#shutdown(long)
	 */
	@Override
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.allc.core.operation.AbstractOperation#process(com.allc.comm.socket.
	 * ConnSocketServer, com.allc.comm.frame.Frame,
	 * com.allc.properties.PropFile)
	 */
	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		try {
			String nroNC = (String) frame.getBody().get(0);
			String fecha = (String) frame.getBody().get(1);
			String nroTienda = (String) frame.getBody().get(2);
			String nroTerminal = (String) frame.getBody().get(3);

			StringBuffer message = new StringBuffer();
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"CONS_DEV_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Consultando Nota de Crédito: " + nroNC + ".\n",
					true);

			iniciarSesionArts("Arts");

			if (existNotaCredito(nroNC)) {

				logger.info("Inicia carga de datos dentro del mensaje.");
				message.append("0").append(ArmsServerConstants.Communication.FRAME_SEP).append(nroNC)
						.append(ArmsServerConstants.Communication.FRAME_SEP);

				List<Object[]> dataNC = getNCData(nroNC, Integer.valueOf(nroTienda).toString());
				if (dataNC != null && !dataNC.isEmpty()) {

					for (int i = 0; i < dataNC.size(); i++) {
						Object[] row = dataNC.get(i);
						String monto = row[1].toString();
						message.append(monto.substring(0, monto.length()-4)).append(ArmsServerConstants.Communication.FRAME_SEP)
								.append(row[2].toString()).append(ArmsServerConstants.Communication.FRAME_SEP);
						if (row[0].toString().trim().length() == 0) {
							if("55".equals(row[5].toString()))
								message.append("1").append(ArmsServerConstants.Communication.FRAME_SEP).append(row[4].toString()).append(ArmsServerConstants.Communication.FRAME_SEP).append("0");

						} else {
							String factura = row[3].toString();
							List<Object[]> dataTarjNC = getNCDataTarj(factura, monto.substring(0, monto.length()-4), row[0].toString());

							if (dataTarjNC != null && !dataTarjNC.isEmpty()) {
								for (int j = 0; j < dataTarjNC.size(); j++) {
									Object[] rowT = dataTarjNC.get(j);
									message.append("2").append(ArmsServerConstants.Communication.FRAME_SEP).append(row[4].toString()).append(ArmsServerConstants.Communication.FRAME_SEP)
									.append(rowT[0].toString()).append(";")
											.append(rowT[1].toString()).append(";");
											String plazo = rowT[2].toString();
											message.append(plazo.trim().equals("00") ? "  " : plazo).append(";")
											.append(rowT[3].toString()).append(";");
											String monto1 = rowT[4].toString();
											message.append(monto1.substring(0, monto1.length()-4)).append(";");
											String monto2 = rowT[5].toString();
											message.append(monto2.substring(0, monto2.length()-4)).append(";");
											String monto3 = rowT[6].toString();
											message.append(monto3.substring(0, monto3.length()-4)).append(";")
											.append(rowT[7].toString()).append(";")
											.append(rowT[8].toString()).append(";")
											.append(rowT[9].toString()).append(";")
											.append(rowT[10].toString()).append(";")
											.append(rowT[11].toString()).append(";")
											.append(rowT[12].toString()).append(";")
											.append(rowT[13].toString()).append(";")
											.append(rowT[14].toString()).append(";")
											.append(rowT[15].toString()).append(";")
											.append(rowT[16].toString()).append(";")
											.append(rowT[17] != null ? rowT[17].toString() : "").append(";")
											.append(rowT[18] != null ? rowT[18].toString() : "").append(";")
											.append(rowT[19] != null ? rowT[19].toString() : "").append(";")
											.append(rowT[20] != null ? rowT[20].toString() : "");

									if (j < dataTarjNC.size() - 1)
										message.append(ArmsServerConstants.Communication.FRAME_SEP);

								}
							}
						}

						if (i < dataNC.size() - 1)
							message.append(ArmsServerConstants.Communication.FRAME_SEP);

					}

				}
			} else {
				logger.info("La nota de credito: " + nroNC + " no existe.");
				message.append("1");
			}
			String sb = Util.addLengthStartOfString(
					frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message.toString(),
			//si el ultimo numero es menor a 5 suponemos que es efectivo y si no es tarjeta
//			if(Integer.valueOf(nroNC.substring(nroNC.length()-1)).intValue() < 5){
//				message.append("0").append(ArmsServerConstants.Communication.FRAME_SEP).append(nroNC).append(ArmsServerConstants.Communication.FRAME_SEP).append("1000").append(ArmsServerConstants.Communication.FRAME_SEP).append("0")
//				.append(ArmsServerConstants.Communication.FRAME_SEP).append("1");
//			} else {
//				message.append("0").append(ArmsServerConstants.Communication.FRAME_SEP).append(nroNC).append(ArmsServerConstants.Communication.FRAME_SEP).append("39").append(ArmsServerConstants.Communication.FRAME_SEP).append("0")
//				.append(ArmsServerConstants.Communication.FRAME_SEP).append("2").append(ArmsServerConstants.Communication.FRAME_SEP).append("1").append(ArmsServerConstants.Communication.FRAME_SEP).append("00").append(ArmsServerConstants.Communication.FRAME_SEP)
//				.append("  ").append(ArmsServerConstants.Communication.FRAME_SEP).append("  ").append(ArmsServerConstants.Communication.FRAME_SEP).append("34").append(ArmsServerConstants.Communication.FRAME_SEP).append("0").append(ArmsServerConstants.Communication.FRAME_SEP)
//				.append("5").append(ArmsServerConstants.Communication.FRAME_SEP).append("000026").append(ArmsServerConstants.Communication.FRAME_SEP).append("233920").append(ArmsServerConstants.Communication.FRAME_SEP).append("20161115").append(ArmsServerConstants.Communication.FRAME_SEP)
//				.append("239699").append(ArmsServerConstants.Communication.FRAME_SEP).append("8100000009").append(ArmsServerConstants.Communication.FRAME_SEP).append("JP100411").append(ArmsServerConstants.Communication.FRAME_SEP).append("103");	
//			}
			
					properties.getInt("serverSocket.quantityBytesLength"));
			logger.info("Respuesta a enviar: " + sb);
			if (socket.writeDataSocket(sb)) {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"CONS_DEV_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|END|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Devolución: " + frame.getBody().get(0) + " encontrado.\n",
						true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"CONS_DEV_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|WAR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|No se pudo enviar la respuesta.\n",
						true);
		} catch (

		Exception e) {
			logger.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"CONS_DEV_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al buscar la Devolución: " + frame.getBody().get(0) + ".\n",
						true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		return false;
	}

	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected void iniciarSesion(Session session, String name) {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					logger.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.allc.core.operation.AbstractOperation#process(com.allc.comm.pipe.
	 * ConnPipeServer, com.allc.comm.frame.Frame, com.allc.properties.PropFile)
	 */
	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

	protected void iniciarSesionArts(String name) {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}

	private List<Object[]> getNCData(String notaCredito, String tienda) {

		try {
			SQLQuery query = session.createSQLQuery(
					"select TR_LTM_TND.ID_ACNT_TND, TR_LTM_TND.MO_ITM_LN_TND, TR_LTM_TND_RTN.STS, TR_RTN.ORGL_INVC_NMB, TR_LTM_TND.AI_LN_ITM, AS_TND.TY_TND from TR_TRN, TR_RTN, TR_LTM_TND_RTN, TR_LTM_TND, PA_STR_RTL, AS_TND where PA_STR_RTL.ID_BSN_UN = TR_TRN.ID_BSN_UN AND TR_LTM_TND.ID_TND = AS_TND.ID_TND and TR_TRN.ID_TRN = TR_RTN.ID_TRN and TR_RTN.ID_TRN = TR_LTM_TND_RTN.ID_TRN and TR_LTM_TND_RTN.ID_TRN = TR_LTM_TND.ID_TRN and TR_LTM_TND_RTN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM AND TR_TRN.FL_CNCL<> 1 AND TR_LTM_TND.FL_IS_CHNG=1 and PA_STR_RTL.CD_STR_RT = "
							+ tienda + " and TR_RTN.RTN_NMB = '" + notaCredito + "' ");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getNCDataTarj(String factura, String monto, String accountNumber) {

		try {
			logger.info("Consulta: SELECT CO_TND_PINPAD.COD_ADQ, CO_TND_PINPAD.COD_DIF, CO_TND_PINPAD.PLZ_DIF, CO_TND_PINPAD.MSM_GRACIA, CO_TND_PINPAD.MONTO_B_GRAB_IVA, CO_TND_PINPAD.MONTO_B_NO_GRAB_IVA, CO_TND_PINPAD.IVA_TRX, CO_TND_PINPAD.SEQ_TRX, CO_TND_PINPAD.HORA_TRX, CO_TND_PINPAD.FECHA_TRX, CO_TND_PINPAD.NUM_AUTO, CO_TND_PINPAD.MID, CO_TND_PINPAD.TID, CO_TND_PINPAD.CID, AS_TND.TY_TND, AS_TND.DE_TND, TR_LTM_TND.ID_ACNT_TND, CO_TND_PINPAD.ARQC, CO_TND_PINPAD.AID, CO_TND_PINPAD.BNK_NM, CO_TND_PINPAD.BRND_NM FROM TR_TRN, CO_TND_PINPAD, TR_INVC, TR_LTM_TND, AS_TND WHERE TR_TRN.ID_TRN = TR_INVC.ID_TRN AND TR_INVC.ID_TRN = CO_TND_PINPAD.ID_TRN and CO_TND_PINPAD.ID_TRN = TR_LTM_TND.ID_TRN and CO_TND_PINPAD.SQ_NBR = TR_LTM_TND.AI_LN_ITM and TR_LTM_TND.ID_TND = AS_TND.ID_TND AND TR_TRN.FL_CNCL<> 1 and TR_LTM_TND.MO_ITM_LN_TND = "
							+ monto + " and TR_INVC.INVC_NMB = '" + factura + "' ");
			SQLQuery query = session.createSQLQuery(
					"SELECT CO_TND_PINPAD.COD_ADQ, CO_TND_PINPAD.COD_DIF, CO_TND_PINPAD.PLZ_DIF, CO_TND_PINPAD.MSM_GRACIA, CO_TND_PINPAD.MONTO_B_GRAB_IVA, CO_TND_PINPAD.MONTO_B_NO_GRAB_IVA, CO_TND_PINPAD.IVA_TRX, CO_TND_PINPAD.SEQ_TRX, CO_TND_PINPAD.HORA_TRX, CO_TND_PINPAD.FECHA_TRX, CO_TND_PINPAD.NUM_AUTO, CO_TND_PINPAD.MID, CO_TND_PINPAD.TID, CO_TND_PINPAD.CID, AS_TND.TY_TND, AS_TND.DE_TND, TR_LTM_TND.ID_ACNT_TND, CO_TND_PINPAD.ARQC, CO_TND_PINPAD.AID, CO_TND_PINPAD.BNK_NM, CO_TND_PINPAD.BRND_NM FROM TR_TRN, CO_TND_PINPAD, TR_INVC, TR_LTM_TND, AS_TND WHERE TR_TRN.ID_TRN = TR_INVC.ID_TRN AND TR_INVC.ID_TRN = CO_TND_PINPAD.ID_TRN and CO_TND_PINPAD.ID_TRN = TR_LTM_TND.ID_TRN and CO_TND_PINPAD.SQ_NBR = TR_LTM_TND.AI_LN_ITM and TR_LTM_TND.ID_TND = AS_TND.ID_TND AND TR_TRN.FL_CNCL<> 1 and TR_LTM_TND.MO_ITM_LN_TND = "
							+ monto + " and TR_INVC.INVC_NMB = '" + factura + "' ");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public boolean existNotaCredito(String nroNotaCredito) {
		try {
			Query query = session.createSQLQuery(
					"SELECT R.ID_TRN FROM TR_RTN R WHERE R.RTN_NMB = '" + nroNotaCredito + "'");
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

}
