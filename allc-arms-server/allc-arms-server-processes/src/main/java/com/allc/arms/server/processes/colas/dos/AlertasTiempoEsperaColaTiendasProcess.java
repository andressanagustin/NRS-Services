/*
 *  ***************************************************************
 * NUO - 2020
 * Creado por:      Ing. Joel Jalon Gomez 
 * Motivo:          verificar y notificar cada n minutos tiempo de espera en cola por cajas
 * Fecha Creacion:  2020-11-20
 *  ***************************************************************
 */
package com.allc.arms.server.processes.colas.dos;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.processes.colas.DevSuRequest;
import com.allc.arms.server.processes.colas.QueueAlert;
import com.allc.arms.server.processes.colas.ResponseService;
import com.allc.arms.server.processes.colas.queue_list;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;
import com.google.gson.Gson;




public class AlertasTiempoEsperaColaTiendasProcess extends AbstractProcess {
	
	public static void main(String args[]) {
		log.info("prueba clase joel!!");
		AlertasTiempoEsperaColaTiendasProcess obj = new AlertasTiempoEsperaColaTiendasProcess();
		try {
			String jsonData = "{\"timeSpan\":\"10:15 - 10:30\", \"queuesAlert\":[{\"store_id\":\"117\", \"queue_list\":[{\"queue_id\":\"1171\", \"quantity\":210, \"alert_level\":3, \"description\":\"CAJA NORMAL\"}]}, {\"store_id\":\"328\", \"queue_list\":[{\"queue_id\":\"3281\", \"quantity\":190, \"alert_level\":3, \"description\":\"CAJA NORMAL\"}, {\"queue_id\":\"3284\", \"quantity\":306, \"alert_level\":2, \"description\":\"CAJA EXPRESS\"}]}]}";
			obj.consumoServicio(jsonData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected static Logger log = Logger.getLogger(AlertasTiempoEsperaColaTiendasProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	protected Session session = null;
	protected boolean finished = false;

	
	@Override
	public void run() {
		while (!isEnd) {
			try {
				verificarTecs();
				Thread.sleep(properties.getLong("colas.alertas.tec.timesleep.2"));
			} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
			}
		}
		finished = true;
	}
	
	public void verificarTecs() {
		Integer minutosIntervalo =  properties.getInt("colas.alertas.tec.minutos.intervalo.2");
		String spBd = "select * from colas_ec.fn_alerta_tec_tienda_2(:intervalo)";
		
		String queryCajasTec = "select \r\n" + 
				"		cast(avg(tc.tiempo_espera) as numeric(12,2)) prom_tec,\r\n" +  
				"		right('0000'||ee.des_clave,4) caja,\r\n" + 
				"		t.de_str_rt tienda,\r\n" + 
				"		t.cd_str_rt codigo_tienda,\r\n" + 
				"		t.id_bsn_un id_tienda,\r\n" + 
				"		tt.descripcion tipo_terminal\r\n" + 
				"		from eyes_ec.etc_tiempo_calculado tc\r\n" + 
				"		inner join eyes_ec.fm_equipo ee on ee.id_equipo = tc.id_equipo\r\n" + 
				"		inner join arts_ec.pa_str_rtl t on t.cd_str_rt = ee.id_local \r\n" + 
				"		inner join eyes_ec.etc_tipo_terminal tt on tt.id_tipo_terminal = ee.id_tipo_terminal\r\n" + 
				"		where tc.fecha >= to_timestamp(:fechaIni,'YYYY-MM-DD HH24:MI:SS')\r\n" + 
				"		and tc.fecha < to_timestamp(:fechaFin,'YYYY-MM-DD HH24:MI:SS')\r\n" +  
				"			and t.cd_str_rt = :codigoTienda \r\n" + 
				"		group by \r\n" + 
				"		right('0000'||ee.des_clave,4),\r\n" + 
				"		t.de_str_rt,\r\n" + 
				"		t.cd_str_rt,\r\n" + 
				"		t.id_bsn_un,\r\n" + 
				"		tt.descripcion\r\n" + 
				"		having cast(avg(tc.tiempo_espera) as numeric(12,2)) >= :tiempoLimite";
		try {
		log.info("***************** Inicio Verificar tiempos de espera en cola ***********************");
		iniciarSesion();
		session.beginTransaction();
		
		SQLQuery query = session.createSQLQuery(spBd);
		query.setInteger("intervalo", minutosIntervalo);
		List<Object[]> rows = query.list();
		session.getTransaction().commit();
		
		if (rows == null || rows.isEmpty())
		{
			log.info("No hay tiempos de espera que notificar...");
			return;
		}
		String horaIniFormat = new SimpleDateFormat("HH:mm").format( (Date) rows.get(0)[11] );
		String horaFinFormat = new SimpleDateFormat("HH:mm").format( (Date) rows.get(0)[12] );
		
		String fechaIniFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( (Date) rows.get(0)[11] );
		String fechaFinFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( (Date) rows.get(0)[12] );
		
		DevSuRequest dsRequest = new DevSuRequest();
		List<QueueAlert> listaAlert = new ArrayList<QueueAlert>();
		
		dsRequest.setTimeSpan(horaIniFormat + " - "+ horaFinFormat );
		
		//por cada tiempo de espera, setear atributos con registros
    	for(Object[] row : rows){
    		QueueAlert alert = new QueueAlert();
    		alert.setStore_id( ((BigInteger)row[3]).intValue() );
    		List<queue_list> listaAlertaCajas = new ArrayList<queue_list>();
    		
    		
    		log.info("tienda --> "+row[4].toString() + " -----------------------------");
    		log.info("codigo tienda --> "+row[3].toString());
    		log.info("total cajas --> "+row[5].toString());
    		log.info("total cajas tec --> "+row[6].toString());
    		log.info("% tec --> "+row[7].toString());
    		log.info("Fecha Ini ref --> "+row[11]);
    		log.info("fecha Fin ref --> "+row[12]);
    		log.info("Minutos intervalo --> "+row[10]);
    		log.info("tiempoLimite --> "+row[13]);


    		//por cada tienda, consultar cajas y notificar a las pda's...
    		log.info("consultando detalle de cajas tec's...");
    		query = session.createSQLQuery(queryCajasTec);
    		query.setString("fechaIni", fechaIniFormat);
    		query.setString("fechaFin", fechaFinFormat);
    		query.setInteger("codigoTienda", ((BigInteger)row[3]).intValue());
    		query.setInteger("tiempoLimite", (Integer)row[13]);
    		
    		List<Object[]> listaCajas = query.list();
    		
    		String jsonData = "";
    		for(Object[] caja : listaCajas){
    			Integer cont = 1;
    			String queueId = ((BigInteger)row[3]).toString() + Integer.valueOf(caja[1].toString());
    			log.info("   queueId --> "+queueId);
    			log.info("   caja --> "+caja[1]);
    			log.info("   tipo --> "+caja[5]);
    			log.info("   promedio tec --> "+((BigDecimal)caja[0]).intValue());
    			
    			queue_list term = new queue_list();
    			term.setAlert_level(1);
    			term.setDescription(caja[1].toString() +" - "+caja[5].toString());
    			term.setQuantity(((BigDecimal)caja[0]).intValue());
    			term.setQueue_id(Integer.valueOf(queueId));
    			
    			
    			cont++;
    			listaAlertaCajas.add(term);
    		}
    		
    		alert.setQueue_list(listaAlertaCajas);
    		listaAlert.add(alert);
    		
    		//query.setDate("fechaIni", date)
    	}
    	dsRequest.setQueuesAlert(listaAlert);
    	
    	log.info("to json..");
    	Gson gson = new Gson();
    	String strJson = gson.toJson(dsRequest);
    	log.info(strJson);
    	
    	
    	
    	//enviar notificacion
    	consumoServicio(strJson);
    		
    	
    	
		} catch (Exception ex) {
			log.error("Error al Verificar tiempos de espera en cola:",ex);
			if (session != null && session.getTransaction() != null && session.getTransaction().isActive())
			{
				session.getTransaction().rollback();
			}
		}
		finally {
			log.info("***************** Fin Verificar tiempos de espera en cola ***********************");
			if (session != null && session.getTransaction() != null && session.getTransaction().isActive())
			{
				session.close();
			}
			session = null;
		}
			
	}

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		//closeConnection();
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo Alertas Tec...");
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
		log.info("Finalizó el Proceso de alertas tec's.");
		return true;
	}
	
	
	protected void iniciarSesion() {
		while (session == null && !isEnd) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
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
	
	public void consumoServicio(String jsonRequest) throws Exception {
		
		try {
			
		
		    //String u = "https://jboss.devsu.us/vcount/exposed/queues/alertTEC";
		    String u =  (String) properties.getObject("colas.alertas.tec.webservice.notificar");
		    //String u = "http://10.122.5.148:8080/WS_NUO_ECOMMERCE/carritos/consultar";
		    //
		    log.info("url consumo --> "+u);
		    
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
	
		    /*
		    jsonRequest = "{\r\n" + 
		    		"  \"idCliente\": 12,\r\n" + 
		    		"  \"idLocal\": 1\r\n" + 
		    		"}";
		    */
		    
		    //enviar el post
		    try(OutputStream os = conn.getOutputStream()) {
		        byte[] input = jsonRequest.getBytes("utf-8");
		        os.write(input, 0, input.length);	
		        os.close();
		    }
		    
		    int respCode = conn.getResponseCode();
		    
		    log.info("http status code --> "+respCode);
		    
		    
		    
		    //leer respuesta
		    StringBuilder response = new StringBuilder();
		    try(BufferedReader br = new BufferedReader(
		    		  new InputStreamReader(
		    				  (respCode != 200 && respCode != 201)
		    				  ? conn.getErrorStream()
		    				  : conn.getInputStream()
		    				  , "utf-8"
		    				  )
		    		  )
		    	) {
		    		    
		    		    String responseLine = null;
		    		    while ((responseLine = br.readLine()) != null) {
		    		        response.append(responseLine.trim());
		    		    }
		    		    log.info(response.toString());
		    		}
		    
		    if (respCode != 200 && respCode != 201)
		    {
		    	log.info("error inesperado al notificar la alerta");
		    	//conn.getErrorStream();
		    	return;
		    }
		    
		    ResponseService jsonRespuesta = new Gson().fromJson(response.toString(), ResponseService.class);
		    
		    
		    if (!jsonRespuesta.getResponse().isStatus())
		    {
		    	log.info("Error al notificar alerta:");
		    	log.info("mensaje --> "+jsonRespuesta.getResponse().getErrorMessage());
		    	return;
		    }
		    log.info("alerta enviada!");
	    
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Error inesperado al consultar servicio:",ex);
		}
	    		
	}
	
	
	

}
