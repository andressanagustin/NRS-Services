/*
 *  ***************************************************************
 * NUO - 2020
 * Creado por:      Ing. Joel Jalon Gomez 
 * Motivo:          proceso para actualizar archivo de promociones mediante "vector_donload"
 * Fecha Creacion:  2020-11-05
 *  ***************************************************************
 */
package com.allc.arms.server.processes.vector;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Socket;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLType;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;

import oracle.jdbc.OracleTypes;
import tr.com.pos.genius.component.cli.ApplicationAdaptor.Request;
import tr.com.pos.util.ByteUtil;


public class VectorCampaignUpdateProcess extends AbstractProcess {
	
	public static void main(String args[]) {
		System.out.println("prueba clase joel!!");
		VectorCampaignUpdateProcess obj = new VectorCampaignUpdateProcess();
		try {
			obj.updateCampaigns();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected static Logger log = Logger.getLogger(VectorCampaignUpdateProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	protected Session session = null;
	protected boolean finished = false;
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	private boolean actualizarPromocionesEcommerce = false;

	
	@Override
	public void run() {
		log.info("::::::::::: Inicia hilo vector download! :::::::::::::::::");
		while (!isEnd) {
			log.info("dentro de while proceso");
			try {
				updateCampaigns();
				if (actualizarPromocionesEcommerce)
				{
					actualizarArticulosPromocionesOc();
				}
					Thread.sleep(properties.getLong("vector.download.timesleep"));
			} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
			}
		}
		finished = true;
	}
	
	public void updateCampaigns() {
		try {
		log.info("***************** Inicio Actualizar Campaigns ***********************");
		actualizarPromocionesEcommerce = false;
		log.info("consultando tiendas para vector download...");
		iniciarSesion();
		session.beginTransaction();
		SQLQuery query = session.createSQLQuery("SELECT vt.id as id_local,\r\n" + 
				"		vt.num as codigo_local,\r\n" + 
				"		st.ip\r\n" + 
				"FROM GENIUS3.STORE VT\r\n" + 
				"inner join saadmin.mn_tienda st on st.des_clave = vt.num\r\n" + 
				"where vt.is_vector_download = 1 and st.ind_activo = 1");
		List<Object[]> rows = query.list();
		
		
		if (rows == null || rows.isEmpty())
		{
			log.info("No hay tiendas configuradas para Vector Download...");
			return;
		}
    	for(Object[] row : rows){
    		log.info("actualizar campaigns para tienda --> "+row[1]+" host["+row[2]+"]...");
    		//od.setPasillo(obj[10] == null ? null : (String) obj[10]);
    		if (socketToVector((String)row[2],9191)) {
    			query = session.createSQLQuery("update GENIUS3.STORE\r\n" + 
    					"set fecha_vector_download = :fecha,\r\n" +
    					"is_vector_download = 0\r\n" +
    					"where id = :id");
    			
    			query.setParameter("fecha", new Date());
    			query.setParameter("id", (BigDecimal)row[0]);
    			Integer rowsUpdated = query.executeUpdate();
    			log.info("registros actualizados --> "+rowsUpdated);
    			
    			if (((BigInteger)row[1]).intValue() == 900) {
    				actualizarPromocionesEcommerce = true;
    			}
    		}
    		//System.out.println(row.toString());
    	}
    	session.getTransaction().commit();
    	
		} catch (Exception ex) {
			log.error("Error al actualizar campaigns:",ex);
			if (session != null && session.getTransaction() != null && session.getTransaction().isActive())
			{
				session.getTransaction().rollback();
			}
			// TODO: handle exception
		}
		finally {
			log.info("***************** Fin Actualizar Campaigns ***********************");
			if (session != null)
			{
				session.close();
			}
			session = null;
		}
			
	}
	
	public boolean actualizarArticulosPromocionesOc(){
		log.info("***************** Inicio Actualizar Articulos/Catálogos Promociones Oracle ***********************");
		Integer codigoError;
		String msjError;
		try {
			iniciarSesion();
			log.info("actualizando articulos promocionales...");

			//jjg - utilizado ya que hibernate no soporta llamadas a sp's complejos...
			SessionImpl sesionImpl = (SessionImpl) session;
			Connection cnx = sesionImpl.connection();
			CallableStatement cs = cnx.prepareCall("{CALL SP_EC_PRODUCTOS_PROMOCIONES_AL_DIA(?,?)}");
			cs.registerOutParameter(1, OracleTypes.NUMERIC);
			cs.registerOutParameter(2, OracleTypes.VARCHAR);
			cs.execute();
			codigoError = cs.getInt(1);
			msjError = cs.getString(2);
			cs.close();
			
			log.info("resultado sp --> "+ msjError);
			if (codigoError != 0)
			{
				log.error("error actualizando articulos promocionales:");
				log.error(msjError);
				return false;
			}
			log.info("articulos promocionales actualizados!");
			
			log.info("actualizando catálogos de promociones...");
			cs = cnx.prepareCall("{CALL SP_EC_CATALOGOS_PROMOCIONES_AL_DIA(?,?)}");
			cs.registerOutParameter(1, OracleTypes.NUMERIC);
			cs.registerOutParameter(2, OracleTypes.VARCHAR);
			cs.execute();
			codigoError = cs.getInt(1);
			msjError = cs.getString(2);
			cs.close();

			log.info("resultado sp --> "+ msjError);
			if (codigoError != 0)
			{
				log.error("error actualizando catálogos de promociones:");
				log.error(msjError);
				return false;
			}
			log.info("catálogos de promociones actualizados!");
			
			//cnx.close();
			return true;
		} catch (Exception ex) {
			log.error("Error al actualizar artículos/catálogos promocionales en Oracle:",ex);
			return false;
		}
		finally {
			log.info("***************** Fin Actualizar Articulos/Catálogos Promociones Oracle ***********************");
			session.close();
			session = null;
		}
		
	}

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		//closeConnection();
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo VectorCampaignUpdate...");
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
		log.info("Finalizó el Proceso de Actualizar Campaign.");
		return true;
	}
	
	
	protected void iniciarSesion() {
		while (session == null && !isEnd) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Vector").openSession();
			} catch (Exception e) {
				e.printStackTrace();
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
	
	
	private boolean socketToVector(String ipMotorPromo, Integer puertoSocket)
	{
		try {
			Socket socket = new Socket(ipMotorPromo, puertoSocket);//9191
			Request request = new Request();
			request.longOptionName = "force";
			request.parameter = "download-campaign";
			 
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(baos);
			out.writeObject(request);
			out.close();
			 
			byte[] send = new byte[baos.size() + 4];
			byte[] len = ByteUtil.intToByteArray(baos.size());
			System.arraycopy(len, 0, send, 0, len.length);
			System.arraycopy(baos.toByteArray(), 0, send, 4, baos.size());
			socket.getOutputStream().write(send);
			socket.close();
			return true;
			} catch (Exception e){
				log.error("Error al enviar mensaje por socket al host :"+ipMotorPromo,e);
				return false;
			}
		
	}

}
