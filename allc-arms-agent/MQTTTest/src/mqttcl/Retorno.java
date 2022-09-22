/*******************************************************************************
 * Clase para recibir eventos MQTT
 *
 * @author Alejandro Farre P.
 * 5 de Abril de 2020
 * Barcelona
 */
package mqttcl;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileOutputStream4690;

public class Retorno implements MqttCallback {

	protected static Logger logger;
   //cuando la conexion se cierra de forma inesperada
   public void connectionLost(Throwable cause) {
      System.out.println("perdio la comunicacion: " + cause.toString());
   }

   //cada vez que se recibe un mensaje
   public void messageArrived(String topic, MqttMessage message) {
	   PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(Retorno.class);
		logger.info(Retorno.class.getName() + " messageArrived");
      System.out.println(topic + ": " + message.toString());
      try{
    	  	Properties props = new Properties();
			FileInputStream is = new FileInputStream("config.properties");
			props.load(is);
			boolean	is4690 = "4690".equalsIgnoreCase(props.getProperty("so"));
			String msg = message.toString();
			logger.info("msg: "+ msg);
			String lineToWrite = "";
			String filenameToWrite = "";
			if(msg.indexOf("|") > 0){
				String itemCode = msg.substring(0, msg.indexOf("|"));
				System.out.print("itemCode: " + itemCode);
				lineToWrite = "SEQ" + itemCode + "<80>" + "\n";
			} else if(msg.startsWith("SXARAC")){
				if(is4690){
					boolean	encriptEnabled = "1".equalsIgnoreCase(props.getProperty("encryptEnabled"));
					if(encriptEnabled)
						msg = desencriptar(msg.substring(6), "l4F4v0r1T4");
					logger.info("Data: "+ msg);
					String numControl = "0" + msg.substring(35, 45);
					logger.info("Archivo a crear: "+ "C:/RESERVAS/"+numControl.substring(0, 8) + "." + numControl.substring(8));
					File4690 file = new File4690("C:/RESERVAS/"+numControl.substring(0, 8) + "." + numControl.substring(8));
					if (!file.exists())
						file.createNewFile();
					else{
						file.delete();
						file.createNewFile();
					}
					String sxaracContent = msg.substring(6, msg.lastIndexOf("SXARAC"));
					String[] lines = sxaracContent.split("&");
					filenameToWrite = numControl.substring(0, 8) + "." + numControl.substring(8);
					FileOutputStream4690 fos = new FileOutputStream4690("C:/RESERVAS/"+filenameToWrite, true);
					for(int i = 0; i < lines.length; i++){
						String line = lines[i] + "\r\n";
						fos.write(line.getBytes(), 0, line.length());						
					}					
					fos.close();
					logger.info("Archivo creado: "+ "C:/RESERVAS/"+filenameToWrite);
					lineToWrite = msg.substring(msg.lastIndexOf("SXARAC")+6);
				}
			} else {
				filenameToWrite = "1111";
				lineToWrite = msg;
			}
			if(is4690){
				String path = props.getProperty("inFolder.path")+"/";
				File4690 file = new File4690(path + filenameToWrite);
				int seq = 0;
				while(file.exists()){
					seq++;
					file = new File4690(path + filenameToWrite + "-" + Integer.valueOf(seq).toString());
				}
				if (!file.exists())
					file.createNewFile();
				
				FileOutputStream4690 fos = new FileOutputStream4690(file, true);
				fos.write(lineToWrite.getBytes(), 0, lineToWrite.length());
				
				fos.close();
			} else {
				PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(props.getProperty("inFolder.path")+"/"+Integer.valueOf(11111).toString(), true)));
				fileaPos.write(lineToWrite, 0, lineToWrite.length());
				fileaPos.close();
			}
      } catch (Exception e){
    	  logger.error(e.getMessage(), e);
    	  System.out.print(e.getMessage());
      }
   }
   
   /**
	 * Metodo para desencriptar el mensaje
	 * @param token (mensaje encriptado)
	 * @param topic
	 * @return mensaje
	 */
	public static String desencriptar(String token, String topic) {	
           try 
           {
               String key = "0000000000" + topic +".*/@..";
               key = key.substring(key.length() - 16, key.length());
               // Crear key y cipher
               Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
               Cipher cipher = Cipher.getInstance("AES");
               // decrypt
               byte[] decodedBytes = Base64.getDecoder().decode(token);
               cipher.init(Cipher.DECRYPT_MODE, aesKey);
               String decrypted = new String(cipher.doFinal(decodedBytes));
               return decrypted;
           }
           catch(Exception e) 
           {
               return e.toString();
           }	
	}

   //Cuando un mensaje  con QoS 1 o 2 llega al broker
   public void deliveryComplete(IMqttDeliveryToken token) {
      String msg;
      try {
         msg = token.getMessage().getPayload().toString();
         System.out.println("Mensaje enviado " + token.getTopics() + ":" + msg);
      } catch (MqttException me) {
         System.out.println(me.getMessage());
      }
   }

}
