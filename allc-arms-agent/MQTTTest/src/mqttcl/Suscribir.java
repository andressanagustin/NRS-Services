/*******************************************************************************
 * Clase para suscribirse a un tema MQTT
 *
 * @author Alejandro Farre P.
 * 5 de Abril de 2020
 * Barcelona
 */
package mqttcl;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.paho.client.mqttv3.*;

public class Suscribir {
	protected static Logger logger;
   private String broker, topic, clienteId;
   private MqttClient cliente;

   public Suscribir(String br, String to, String cl) {
	   PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(Suscribir.class);
      broker = br;
      topic = to;
      clienteId = cl;

      try {
         cliente = new MqttClient(broker, clienteId);
      } catch (MqttException e) {
    	  logger.error(e.getMessage(), e);
         //System.exit(1);
      }
   }

   public void start() {
      try {
         cliente.setCallback(new Retorno());
         MqttConnectOptions conOpt = new MqttConnectOptions();
         conOpt.setAutomaticReconnect(true);
         conOpt.setCleanSession(false);
         cliente.connect(conOpt);
         cliente.subscribe(topic);
      }catch (MqttException e) {
    	  logger.error(e.getMessage(), e);
         //System.exit(1);
      }
   }
   
   public boolean isConnected(){
	   return cliente != null && cliente.isConnected();
   }

}
