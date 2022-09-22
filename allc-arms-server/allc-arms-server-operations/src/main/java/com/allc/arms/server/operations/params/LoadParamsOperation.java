/**
 * 
 */
package com.allc.arms.server.operations.params;

import java.math.BigInteger;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
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
 * Operación encargada de obtener los parámetros de la tienda y ámbito consultados.
 * 
 * @author gustavo
 *
 */
public class LoadParamsOperation extends AbstractOperation {
	private static Logger log = Logger.getLogger(LoadParamsOperation.class);
	private Session session = null;


	@Override
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Iniciando LoadParamsOperation...");
		try {
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "LOAD_PARAMS_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Obteniendo parámetros de tienda: "+frame.getHeader().get(Frame.POS_SOURCE)+", ámbito: "+frame.getBody().get(0)+".\n", true);
			StringBuilder message = new StringBuilder();
			iniciarSesion();

			Integer codTienda = Integer.valueOf((String) frame.getHeader().get(Frame.POS_SOURCE));
			Integer ambito = Integer.valueOf((String) frame.getBody().get(0));
			Integer idBsnUnGp = -1;
			//validar el body si tiene +1, viene el id group
			if (frame.getBody().size() > 1)
			{
				log.info("carga de paramtros a nivel regional");
				idBsnUnGp = Integer.valueOf((String) frame.getBody().get(1));
			}
			
			if (codTienda == 0)
			{
				codTienda=-1; //-1 para que traiga los parametros sin considerar la tienda
			}
			
			if (idBsnUnGp == -1 && codTienda != -1) {
				log.info("BUSCA REGIONAL SEGUN LA TIENDA");
				idBsnUnGp = getRegional(codTienda);
			}
			 
			log.info("tienda establecida --> "+codTienda);
			log.info("id region establecida --> "+idBsnUnGp);
			log.info("ambito establecida --> "+ambito);
			
			ParamsDAO paramsDAO = new ParamsDAO();
			List params = paramsDAO.getParamsListStoreGroup(session, idBsnUnGp, codTienda, ambito);
			
			if(params != null && !params.isEmpty()) {
				Iterator itParams = params.iterator();
				while(itParams.hasNext()){
					ParamValue paramValue = (ParamValue) itParams.next();
					message.append(frame.getSeparator()).append(paramValue.getParam().getClave()).append("=").append(paramValue.getValor());
				}
			}
			session.close();

			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + ambito + message);
			log.info("Respuesta a enviar: " + sb.toString());
			if(socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")))){
				if(params != null && !params.isEmpty())
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "LOAD_PARAMS_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Parámetros enviados.\n", true);
				else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "LOAD_PARAMS_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se encontraron parámetros.\n", true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "LOAD_PARAMS_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "LOAD_PARAMS_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al obtener los parámetros de la tienda: "+frame.getHeader().get(Frame.POS_SOURCE)+", ámbito: "+frame.getBody().get(0)+".\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected void iniciarSesion() {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Integer getRegional(Integer codTienda) {
        try {
            Query query = this.session.createSQLQuery("select sr.id_bsn_un_gp from mn_srv_str sl join mn_srv_prn_chl ph on sl.id_srv_prn_chl = ph.id_srv_prn_chl join mn_srv sr on sr.id_srv_prn_chl = ph.id_srv_prn_chl where sl.estado = 1 and ph.estado = 1 and sr.estado=1 and sl.id_local = " + codTienda);
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
            	log.info("Obtiene codigo " + rows.get(0));
            	if (rows.get(0) == null) return -1;
            	else return (Integer) rows.get(0);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return -1;

    }

}
