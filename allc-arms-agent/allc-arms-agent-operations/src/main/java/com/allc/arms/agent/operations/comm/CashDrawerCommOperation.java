package com.allc.arms.agent.operations.comm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.core.receiver.ReceiverPipe;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class CashDrawerCommOperation extends AbstractOperation{
	
	private Logger logger = Logger.getLogger(CashDrawerCommOperation.class);
	protected String storeCode;
	protected String responseData = "";
	
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		return false;
	}
	
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		logger.info("Iniciando CashDrawerCommOperation...");
		try {
			//liberamos pipeServer
			ReceiverPipe.waitAvailable = false;
			String posResponseChannel = (String) frame.getHeader().get(Frame.POS_SOURCE);
			String pipeResponseChannel = (String) frame.getBody().get(0);
			String cashCancel = (String) frame.getBody().get(1);
			//logger.info("Pipe de Respuesta: " + pipeResponseChannel);
			logger.info("Cash/Cancel: " + cashCancel);
			pipe.setPipeOutputStream(null);
			TrustManager[] trustAllCerts = new TrustManager[] {
					new X509TrustManager() {
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return null;
						}
						public void checkClientTrusted(X509Certificate[] certs, String authType) {  }
						public void checkServerTrusted(X509Certificate[] certs, String authType) {  }
					}
			};

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			
			if("CASH".equalsIgnoreCase(cashCancel)){
				String posId = (String) frame.getBody().get(2);
				String posUser = (String) frame.getBody().get(3);
				String amount = (String) frame.getBody().get(4);
				//logger.info("Pos ID: " + posId);
				//logger.info("Pos User: " + posUser);
				//logger.info("Amount: " + amount);
				if(cash(posId, posUser, amount, properties, pipe, pipeResponseChannel, posResponseChannel)){
					String trama = Util.addLengthStartOfString("1"+responseData, properties.getInt("serverPipe.quantityBytesLength"))
							.toString();
					logger.info("Trama a enviar por pipe: "+trama);
					pipe.sendData(pipeResponseChannel, posResponseChannel, trama);	
				} else {
					String trama = Util.addLengthStartOfString("0", properties.getInt("serverPipe.quantityBytesLength"))
							.toString();
					logger.info("Trama a enviar por pipe: "+trama);
					pipe.sendData(pipeResponseChannel, posResponseChannel, trama);	
				}
			} else if("CANCEL".equalsIgnoreCase(cashCancel)){
				String code = (String) frame.getBody().get(2);
				if(cancel(code, properties, posResponseChannel)){
					String trama = Util.addLengthStartOfString("1", properties.getInt("serverPipe.quantityBytesLength"))
							.toString();
					logger.info("Trama a enviar por pipe: "+trama);
					pipe.sendData(pipeResponseChannel, posResponseChannel, trama);	
				} else {
					String trama = Util.addLengthStartOfString("0", properties.getInt("serverPipe.quantityBytesLength"))
							.toString();
					logger.info("Trama a enviar por pipe: "+trama);
					pipe.sendData(pipeResponseChannel, posResponseChannel, trama);	
				}
			}
			
			logger.info("Finaliza CashDrawerCommOperation...");
		} catch (Exception e){
			logger.error(e.getMessage(), e);
		}
		
		return false;
	}
	
	public boolean cancel(String code, PropFile properties, String posResponseChannel) {
		boolean retorno = true;
		try {
			String ip = (String) properties.getMap("serverCashDrawer.ip").get(posResponseChannel);
			String name = (String) properties.getMap("serverCashDrawer.name").get(posResponseChannel);
			String password = (String) properties.getMap("serverCashDrawer.password").get(posResponseChannel);
			logger.info("Iniciando Cancel");
			//logger.info("URL: "+ "https://"+ip+"/Cashdro3WS/index.php?operation=finishOperation&name="+name+"&password="+password+"&operationId="+code+"&type=3");
			URL url = new URL("https://"+ip+"/Cashdro3WS/index.php?operation=finishOperation&name="+name+"&password="+password+"&operationId="+code+"&type=3");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP Error code : "
						+ conn.getResponseCode());
			}
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			String output;
			String respuesta ="";
			logger.info("Leyendo respuesta");
			long startTime = Calendar.getInstance().getTimeInMillis();
			while ((output = br.readLine()) != null) {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= 600000) {
					retorno = false;
					logger.info("Se superó el timeout de espera");
					break;
				}
				respuesta += output;
				Thread.sleep(500);
			}
			respuesta = respuesta.trim();
			logger.info("Respuesta leida: "+ respuesta);
			logger.info("Operación: " + code + " cancelada.");
			conn.disconnect();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			retorno = false;
		}
		logger.info("Finaliza Cancel");
		return retorno;
	}
	
	private boolean cash(String posid, String posuser, String amount, PropFile properties, ConnPipeServer pipe, String pipeResponseChannel, String posResponseChannel){
		boolean retorno = true;
		try {
			String ip = (String) properties.getMap("serverCashDrawer.ip").get(posResponseChannel);
			String name = (String) properties.getMap("serverCashDrawer.name").get(posResponseChannel);
			String password = (String) properties.getMap("serverCashDrawer.password").get(posResponseChannel);
			logger.info("Iniciando Cash");
			URL url = new URL("https://"+ip+"/Cashdro3WS/index.php?operation=startOperation&name="+name +"&password="+password+"&type=4&posid="+posid+"&posuser="+posuser+"&parameters={\"amount\":\""+amount+"\"}");

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP Error code : "
						+ conn.getResponseCode());
			}
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			String output;
			String respuesta ="";
			logger.info("Leyendo respuesta");
			long startTime = Calendar.getInstance().getTimeInMillis();
			while ((output = br.readLine()) != null) {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= 600000) {
					retorno = false;
					logger.info("Se superó el timeout de espera");
					break;
				}
				respuesta += output;
				Thread.sleep(500);
			}
			respuesta = respuesta.trim();
			logger.info("Respuesta leida: "+ respuesta);
			//PARSEA JSON
			JSONObject json = new JSONObject(respuesta);
			int code= json.getInt("code");
			if(code == 1 ) {
				int operationId = json.getInt("data");
				logger.info("Code leido: "+ operationId);
				String trama = Util.addLengthStartOfString("OID="+operationId, properties.getInt("serverPipe.quantityBytesLength"))
						.toString();
				logger.info("Trama a enviar por pipe: "+trama);
				pipe.sendData(pipeResponseChannel, posResponseChannel, trama);
				logger.info("Enviar segunda trama");
				enviarSegundaTrama(ip, name, password, operationId, properties);

			}else {
				String strdata = json.getString("data");
				logger.error("Codigo: "+code + " Mensaje: "+strdata);
				logger.error("Fin del programa 1 con error ");
				retorno = false;
			}
			conn.disconnect();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			retorno = false;
		}
		logger.info("Finaliza cash");
		return retorno;
	}
	
	public boolean enviarSegundaTrama(String ip, String name, String password,int code, PropFile properties) {
		int number =0;
		boolean retorno = true;
		try {
			logger.info("Iniciando envío de segunda trama.");
			URL url = new URL("https://"+ip+"/Cashdro3WS/index.php?operation=acknowledgeOperationId&name="+name+"&password="+password+"&operationId="+code);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP Error code : "
						+ conn.getResponseCode());
			}
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			String output;
			String respuesta ="";
			logger.info("Leyendo respuesta");
			long startTime = Calendar.getInstance().getTimeInMillis();
			while ((output = br.readLine()) != null) {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= 600000) {
					retorno = false;
					logger.info("Se superó el timeout de espera");
					break;
				}
				respuesta += output;
				Thread.sleep(500);
			}
			respuesta = respuesta.trim();
			logger.info("Respuesta leida: "+ respuesta);

			//PARSEA JSON
			JSONObject json = new JSONObject(respuesta);
			number= json.getInt("code");
			if(number == 1 ) {
				logger.info("Enviar tercera trama");
				enviarTerceraTrama(ip, name, password, code, properties);
			}else {
				String strdata = json.getString("data");
				logger.error("Codigo: "+code + " Mensaje: "+strdata);
				logger.error("Fin del programa 2 con error");
				retorno = false;
			}

			conn.disconnect();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			retorno = false;
		}
		logger.info("Finaliza enviarSegundaTrama");
		return retorno;
	}
	
	public boolean enviarTerceraTrama(String ip, String name, String password,int operationId, PropFile properties) {
		int code =0;
		boolean retorno = true;
		try {
			logger.info("Iniciando envío de tercera trama.");
			URL url = new URL("https://"+ip+"/Cashdro3WS/index.php?operation=askOperation&operationId="+operationId+"&name="+name+"&password="+password);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP Error code : "
						+ conn.getResponseCode());
			}
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			String output;
			String respuesta ="";
			logger.info("Leyendo respuesta");
			long startTime = Calendar.getInstance().getTimeInMillis();
			while ((output = br.readLine()) != null) {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= 600000) {
					retorno = false;
					logger.info("Se superó el timeout de espera");
					break;
				}
				respuesta += output;
				Thread.sleep(500);
			}
			respuesta = respuesta.trim();
			logger.info("Respuesta leida: "+ respuesta);
			//PARSEA JSON
			JSONObject json = new JSONObject(respuesta);
			code= json.getInt("code");
			if(code == 1 ) {
				String strdata = json.getString("data");
				//logger.info("Data leída: " + strdata);
				strdata = strdata.replace("\\", "");
				JSONObject obj = new JSONObject(strdata);
				String operationid = obj.getJSONObject("operation").getString("state");
				logger.info("operationid: " + operationid);
				//Valida el estado de la transacción (Lo que hacía ENUM)
				int caseEstado =0;
				if(operationid.equals("I")) {
					caseEstado = 1;
				}else if (operationid.equals("Q")) {
					caseEstado = 2;
				}else if (operationid.equals("E")) {
					caseEstado = 3;
				}else if (operationid.equals("F")) {
					caseEstado = 4;
				}

				switch(caseEstado) {
				case 1:
					logger.info("La operación no existe. Fin de la transacción 3, estado: " +caseEstado);
					break;
				case 2:
					logger.info("La operación está en cola ");
					Thread.sleep(3*1000);
					enviarTerceraTrama(ip, name, password, operationId, properties);
					break;
				case 3:
					logger.info("La operación está en ejecución");
					Thread.sleep(3*1000);
					enviarTerceraTrama(ip, name, password, operationId, properties);
					break;
				case 4:
					String Fintotal = obj.getJSONObject("operation").getString("total");
					String Fintotalin = obj.getJSONObject("operation").getString("totalin");
					String Fintotalout = obj.getJSONObject("operation").getString("totalout");
					//logger.info(Fintotal+" "+ Fintotalin +" "+Fintotalout );
					logger.info("La operación ha Finalizado. Enviar la cuarta transacción");
					String resumenString = Fintotal+","+ Fintotalin +","+Fintotalout ;
					logger.info("resumenString: " + resumenString);
					responseData = resumenString;
					enviarCuartaTrama(ip, name, password, operationId, properties);
					break;
				}
			}else {
				String strdata = json.getString("data");
				logger.info("Codigo: "+code + " Mensaje: "+strdata);
				logger.info("Fin del programa 3 con error");
				retorno = false;
			}
			conn.disconnect();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			retorno = false;
		}
		logger.info("Finaliza enviarTerceraTrama");
		return retorno;
	}
	
	public boolean enviarCuartaTrama(String ip, String name, String password,int code, PropFile properties) {
		int number =0;
		boolean retorno = true;
		try {
			logger.info("Iniciando envío de cuarta trama.");
			URL url = new URL("https://"+ip+"/Cashdro3WS/index.php?operation=setOperationImported&name="+name+"&password="+password+"&operationId="+code);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP Error code : "
						+ conn.getResponseCode());
			}
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			String output;
			String respuesta ="";
			logger.info("Leyendo respuesta");
			long startTime = Calendar.getInstance().getTimeInMillis();
			while ((output = br.readLine()) != null) {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= 600000) {
					retorno = false;
					logger.info("Se superó el timeout de espera");
					break;
				}
				respuesta += output;
				Thread.sleep(500);
			}
			respuesta = respuesta.trim();
			logger.info("Respuesta leida: "+ respuesta);
			//PARSEA JSON
			JSONObject json = new JSONObject(respuesta);
			number= json.getInt("code");
			if(number == 1 ) {
				logger.info("Transacción Importada Correctamente. FIN DE LA OPERACION");
			}else {
				String strdata = json.getString("data");
				logger.info("Codigo: "+code + " Mensaje: "+strdata);
				logger.info("Fin del programa 4 con error");
				retorno = false;
			}
			conn.disconnect();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			retorno = false;
		}
		logger.info("Finaliza enviarCuartaTrama");
		return retorno;
	}

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
