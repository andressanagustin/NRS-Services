/*
 *  ***************************************************************
 * NUO - 2021
 * Creado por:      Ing. Joel Jalon Gomez 
 * Motivo:          evaluar cada trx para notificar si se ingreso una cantidad de clientes en cola superior a la establecida.
 * Fecha Creacion:  2021-04-05
 *  ***************************************************************
 */
package com.allc.arms.server.operations.colas;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.allc.arms.server.operations.utils.ClasificacionAlertas;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.properties.PropFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;


import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;


public class AlertaClientesColaSuperadoOperation{

	protected static Logger log;
	protected PropFile propFile = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private Map<String,String> prop;
	private Session session;
	
	
	public AlertaClientesColaSuperadoOperation(Session session){
		log = Logger.getLogger(AlertaClientesColaSuperadoOperation.class);
		//log.info("AlertaClientesColaSuperadoOperation, session:"+session.toString());
		this.session = session;
	}
	
	public void lanzarAlertaColaExcedida(Integer idBsnUn, Integer idCaja, Integer secuencialTrx, Integer personasCola, String cdOpr) {
		
		String CODIGO_PROCESO = "3";
		String TITULO_ALERTA = "Cantidad de personas en fila";
		try {
			if (Integer.parseInt(propFile.getObject("proceso.alertas.supervisor.habilitado")) == 0)
			{
				return;
			}
			
			if (!conexionDbLink())
			{
				return;
			}
			
			log.info("************ Inicio lanzarAlertaColaExcedida *************************");
			// CONSULTA CENTRAL
			//SQLQuery query = session.createSQLQuery(SqlPostgres.OBTENER_TITULO_ALERTA);
			SQLQuery query = session.createSQLQuery(SqlPostgres.OBTENER_TITULO_ALERTA_LOCAL);
			query.setString(0, CODIGO_PROCESO);
			List<Object[]> rows = query.list();
			TITULO_ALERTA = rows.get(0)+"";
			log.info("Antes 1, titulo: "+TITULO_ALERTA);
			
			// CONSULTA CENTRAL
			//query = session.createSQLQuery(SqlPostgres.CONSULTAR_TRX_CON_CLIENTES_COLA_SUPERADO);
                        if (cdOpr == null || cdOpr.isEmpty())
                            query = session.createSQLQuery(SqlPostgres.CONSULTAR_TRX_CON_CLIENTES_COLA_SUPERADO_LOCAL);
                        else
                            query = session.createSQLQuery(SqlPostgres.CONSULTAR_TRX_CON_CLIENTES_COLA_SUPERADO_LOCAL_V4);
                        
			query.setInteger("idBsnUn", idBsnUn);
			query.setInteger("aiTrn", secuencialTrx);
			query.setInteger("idWs", idCaja);
			//log.info("Query CONSULTAR_TRX_CON_CLIENTES_COLA_SUPERADO_LOCAL: "+query.getQueryString());
                        rows = query.list();
			log.info("Despues 1, rows: "+rows.size());
			if (rows.size() == 0)
			{
				log.info("No hay alerta que notificar...");
				return;
			}
			
			
			Object[] row = rows.get(0);
			
			String ipWsTiendaSupervisor = row[1].toString();
			String codigoCaja = "000" + row[7].toString();
	        codigoCaja = codigoCaja.substring(codigoCaja.length() - 3);
	        //Integer cantidadClientesColaDisparador =Integer.parseInt(prop.get("alertas.cantidad.clientes.cola.excedido"));
	        boolean notificarSupervisor = ((Integer) row[13]) == 1;
	        Integer cantidadClientesColaDisparador = (Integer) row[15];

			
			log.info("tienda --> "+row[0].toString() + " -----------------------------");
     		log.info("codigo tienda --> "+(Integer) row[3]);
     		log.info("COD_OPERADOR --> "+(Integer) row[11]);
     		log.info("Operador --> "+row[12].toString());
     		log.info("Caja --> "+ codigoCaja);
     		log.info("Id tipo Caja --> "+ (Integer) row[14]);
     		log.info("Cantidad Clientes en cola --> "+personasCola);
     		log.info("Disparador cantidad de clientes en cola >? --> "+cantidadClientesColaDisparador);
     		log.info("----------------------------------------------------------------");
     		
     		//AutorizacionRequest alert = new AutorizacionRequest();
     		AlertTecRequest alert = new AlertTecRequest();
     		
        	AutorizacionRequestIp alertIp = new AutorizacionRequestIp();
        	LocalDateTime  fechaInicio = LocalDateTime .now();
        	
        	if (row[9] == null ) row[9]  = 1;
        	//
        	
        	alert.setCaja(Integer.parseInt(codigoCaja));
        	alert.setIdLocal((Integer) row[3]);
        	
        	alert.setDescripcionAlerta(prop.get("proceso.cantidad.personas.fila.msj.notif")
					.replace("**CANT_COLA**", personasCola+"")
					.replace("**MAX_COLA**", cantidadClientesColaDisparador+"")
					.replace("**OPERADOR**", (Integer) row[11]	+" - "+row[12].toString())
			);
        	
        	alert.setTituloAlerta(TITULO_ALERTA);
        	alert.setTipoAlerta(ClasificacionAlertas.CANTIDAD_PERSONAS);
        	alert.setValor(personasCola+"");
        	
        	
        	
        	alertIp.setIp(ipWsTiendaSupervisor);
        	alertIp.setAutorizacionRequest(alert);
        	
        	String auditInsert ="("+(Integer) row[3]+"," //codigo tienda
    	     		+"'"+row[0].toString()+"'," //tienda
    	     		+"'"+CODIGO_PROCESO+"'," //codigo alerta
    	     		+"'"+TITULO_ALERTA+"',"//nombre alerta
    	     		+"'"+row[7].toString()+"',"  //codigo caja
    	     		+secuencialTrx+"," //secuencial trx
    	     		+(Integer) row[11]+"," //codigo operador
    	     		+"100007,"
    	     		+"'"+toStringJson(alert)+"',"// json envio
    	     		+"null,"
    	     		+"null,"
    	     		+"'{"+
    	     			"\"clientesColaDisparador\" : "+cantidadClientesColaDisparador+"}'," //json parametros
    	     		+"now()"	
    	     		+")";
        	
        	//registrar alertas detectadas
	        registroAlertaNotifSupervAudit(auditInsert);
	        
        	log.info("to json..");
        	String strJson = toStringJson(alert);
	     	log.info(strJson);

	     	if (notificarSupervisor)
	     	{
	     		notificaSupervisorTienda(strJson,ipWsTiendaSupervisor);
	     	}
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
			// TODO: handle exception
		}
		finally {
			log.info("************ Fin lanzarAlertaColaExcedida *************************");
		}
	}

	
	public void notificaSupervisorTienda(String jsonRequest, String host) throws Exception {
		
		try {
			
			//String u = "http://pos-favorita.devsu.us/favorita-pos-server/api/exposed/sendAlertTEC";
		    String u =  "http://"+host+":8080"+ (String) propFile.getObject("notificacion.supervisor.tienda.webservice.url");
		    //
		    log.info("url consumo --> "+u);
		    log.info("json envio:");
		    log.info(jsonRequest);
		    
		    URL url = new URL(u);


		    //HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		    conn.setDoOutput(true);   
		    conn.setRequestMethod("POST");  
		    conn.setUseCaches(false);  
		    conn.setConnectTimeout(10000);  
		    conn.setReadTimeout(10000);  
		    conn.setRequestProperty("Content-Type", "application/json; utf-8");
		    conn.setRequestProperty("Accept", "application/json");
		    	String userpass = "supermaxi-admin:SUPERmaxi1";
		    String auth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
		    //String auth = (String) properties.getObject("colas.alertas.tec.webservice.notificar.autorizacion");
		    log.info("auth --> "+auth);
		    conn.setRequestProperty ("Authorization", auth);

		    //enviar el post
		    
		    	OutputStream os = conn.getOutputStream();
		        byte[] input = jsonRequest.getBytes("utf-8");
		        os.write(input, 0, input.length);	
		        os.close();
		    
		    int respCode = conn.getResponseCode();
		    
		    log.info("http status code --> "+respCode);
		    
		    
		    
		    //leer respuesta
		    StringBuilder response = new StringBuilder();
		    
		    InputStreamReader isr = new InputStreamReader(
					  (respCode != 200 && respCode != 201)
					  ? conn.getErrorStream()
					  : conn.getInputStream()
					  , "utf-8"
					  );
		    BufferedReader br = new BufferedReader(isr);

		    String responseLine = null;
		    while ((responseLine = br.readLine()) != null) {
		        response.append(responseLine.trim());
		    }
		    br.close();
		    log.info(response.toString());
		    if (respCode != 200 && respCode != 201)
		    {
		    	
		    	log.info("error inesperado al notificar supervisor");
		    	//conn.getErrorStream();
		    	return;
		    }
		    
		    ResponseAutorizacionService jsonRespuesta = new Gson().fromJson(response.toString(), ResponseAutorizacionService.class);
		    
		    
		    if (!jsonRespuesta.getRespuesta().isEstado())
		    {
		    	log.info("Error al notificar alerta:");
		    	log.info("mensaje --> "+jsonRespuesta.getRespuesta().getError().getMensaje());
		    	return;
		    }
		    log.info("alerta enviada!");
	    
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Error inesperado al consultar servicio:",ex);
		}
	    		
	}
	
	private boolean validarEnvioAlertaSupervisorLocal(Integer codigoTienda, String codigoAlerta) {
		
		try {
			Integer result=0;
			SQLQuery query = session.createSQLQuery(SqlPostgres.VALIDA_NOTIFICAR_ALERTA_PROCESO_TIENDA);
			query.setInteger(0, codigoTienda);
			query.setString(1, codigoAlerta);
			result = ((BigInteger) query.list().get(0)).intValue();

	        return result == 1;
	        
		} catch (Exception ex) {
			log.error("Error al verificar validacion envio de notificacion al supervisor",ex);
			return false;
		}
		
	}
	
	private void registroAlertaNotifSupervAudit(String queryInsert) {
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createSQLQuery(SqlPostgres.AL_NS_REGISTRO_AUDIT + queryInsert + ";");
			query.executeUpdate();

			tx.commit();
		} catch (Exception ex) {
			tx.rollback();
			log.error("Error al registrar alerta procesada",ex);
		}
	}
	
	private String toStringJson (Object obj) throws JsonProcessingException
    {
    	//Creating the ObjectMapper object
        ObjectMapper mapper = new ObjectMapper();
        //Converting the Object to JSONString
        String jsonString = mapper.writeValueAsString(obj);
        return jsonString;
    }
	
	public boolean conexionDbLink () {
    	if (Integer.parseInt(propFile.getObject("eyes.store.code.group")) == 0)
        {
        	return true;
        }
    	
    	log.info("conexion a la central");
    	try {

    		SQLQuery query = session.createSQLQuery(SqlPostgres.CHECK_DBLINK);
    		List<Object[]> rows = query.list();
    		if (rows != null && !rows.isEmpty())
    		{
    			log.info("ya existe una conexion a la central");
    			return true;
    		}
    			
    		
            
    		/*log.info("Host: "+propFile.getObject("central.db.host")+
    				", DBNAME:"+propFile.getObject("central.db.name")+
    				", USUARIO:"+propFile.getObject("central.db.usuario")+
    				", PASSWORD:"+propFile.getObject("central.db.password"));*/
    		
            query = session.createSQLQuery(SqlPostgres.CONEXION_DB_LINK
            							.replace("**HOST**", propFile.getObject("central.db.host"))
            							.replace("**DBNAME**", propFile.getObject("central.db.name"))
            							.replace("**USUARIO**", propFile.getObject("central.db.usuario"))
            							.replace("**PASSWORD**", propFile.getObject("central.db.password"))				
            );
            
            rows = query.list();
            
		} catch (Exception ex) {
			ex.printStackTrace();
			log.fatal("No se pudo establecer conexion a BD central!",ex);
			return false;
			// TODO: handle exception
		}
        log.info("conexion establecida");
        return true;
    	
    }

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public static Logger getLog() {
		return log;
	}

	public static void setLog(Logger log) {
		AlertaClientesColaSuperadoOperation.log = log;
	}

	public Map<String, String> getProp() {
		return prop;
	}

	public void setProp(Map<String, String> prop) {
		this.prop = prop;
	}
	
	

}
