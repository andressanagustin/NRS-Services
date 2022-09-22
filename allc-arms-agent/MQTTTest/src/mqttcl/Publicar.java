/*******************************************************************************
 * Clase para publicar mensajes MQTT
 *
 * @author Alejandro Farre P.
 * 5 de Abril de 2020
 * Barcelona
 */
package mqttcl;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import java.sql.Timestamp;

public class Publicar implements MqttCallback {

   // Private instance variables
   private MqttClient client;
   private String brokerUrl;
   private boolean quietMode;
   private MqttConnectOptions conOpt;
   private boolean clean;
   String password;
   String userName;

    /**
     * Construye una instancia del contenedor de cliente de muestra
     * @param brokerUrl the url of the server to connect to
     * @param clientId the client id to connect with
     * @param cleanSession clear state at end of connection or not (durable or non-durable subscriptions)
     * @param quietMode whether debug should be printed to standard out
     * @param userName the username to connect with
     * @param password the password for the user
     * @throws MqttException
     */
   public Publicar(String brokerUrl, String clientId, boolean cleanSession, boolean quietMode, String userName, String password) throws MqttException {
      this.brokerUrl = brokerUrl;
      this.quietMode = quietMode;
      this.clean 	   = cleanSession;
      this.password = password;
      this.userName = userName;

      //El mensaje es temporalmente almacenado hasta que sea enviado 
      // al broker
      String tmpDir = System.getProperty("java.io.tmpdir");
      MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

      try {
            conOpt = new MqttConnectOptions();
            conOpt.setCleanSession(clean);
            if(password != null ) {
               conOpt.setPassword(this.password.toCharArray());
            }
            if(userName != null) {
               conOpt.setUserName(this.userName);
            }

            // Construye un cliente MQTT modo bloqueante
            client = new MqttClient(this.brokerUrl, clientId, dataStore);

            // Establece este contenedor como el controlador
            // de devolucion de llamadas
            client.setCallback(this);

      } catch (MqttException e) {
         e.printStackTrace();
         System.out.println("No se puede configurar el cliente: "+e.toString());
         System.exit(1);
      }
   }

   /**
    * Publica un mensaje MQTT
    * @param topicName el nombre del tema
    * @param qos la calidad de servicio (0,1,2)
    * @param payload el arreglo de bytes a enviar
    * @throws MqttException
    */
   public void publish(String topicName, int qos, byte[] payload) throws MqttException {

      // Connecta al servidor MQTT
      System.out.println("Conectando a "+ brokerUrl + " con cliente " + client.getClientId());
      client.connect(conOpt);
      System.out.println("Conectado");

      String time = new Timestamp(System.currentTimeMillis()).toString();
      System.out.println("Publicando en: "+ time + " al tema \"" + topicName + "\" qos " + qos);

      // Crea y configura un mensaje
      MqttMessage message = new MqttMessage(payload);
      message.setQos(qos);

      // Envia el mansaje al servidor, el control no se devuelve hasta
      // que se haya entregado al servidor, dependiendo de la calidad
      // de servicio
      client.publish(topicName, message);

      // Desconecta el cliente
      client.disconnect();
      System.out.println("Desconectado");
   }

   /****************************************************************/
   /* Metodo para implementar la interface MqttCallback            */
   /****************************************************************/
   /**
    * @see MqttCallback#connectionLost(Throwable)
    */
   // Llamada cuando la coneccion al servidor se pierde.
   // An application may choose to implement reconnection
   // logic at this point. This sample simply exits.
   public void connectionLost(Throwable cause) {
      System.out.println("Coneccion a " + brokerUrl + " perdida!" + cause);
      System.exit(1);
   }

   /**
    * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
    */
   public void deliveryComplete(IMqttDeliveryToken token) {
      // Called when a message has been delivered to the
      // server. The token passed in here is the same one
      // that was passed to or returned from the original call to publish.
      // This allows applications to perform asynchronous
      // delivery without blocking until delivery completes.
      //
      // This sample demonstrates asynchronous deliver and
      // uses the token.waitForCompletion() call in the main thread which
      // blocks until the delivery has completed.
      // Additionally the deliveryComplete method will be called if
      // the callback is set on the client
      //
      // If the connection to the server breaks before delivery has completed
      // delivery of a message will complete after the client has re-connected.
      // The getPendingTokens method will provide tokens for any messages
      // that are still to be delivered.
   }

   // Llamada cuando llega un mensaje
   /**
    * @see MqttCallback#messageArrived(String, MqttMessage)
    */
   public void messageArrived(String topic, MqttMessage message) throws MqttException {
      String time = new Timestamp(System.currentTimeMillis()).toString();
      System.out.println("Time:\t" +time +
                         "  Topic:\t" + topic +
                         "  Message:\t" + new String(message.getPayload()) +
                         "  QoS:\t" + message.getQos());
   }

   static void printHelp() {
      System.out.println(
         "Syntax:\n\n" +
         "    Sample [-h] [-a publish|subscribe] [-t <topic>] [-m <message text>]\n" +
         "            [-s 0|1|2] -b <hostname|IP address>] [-p <brokerport>] [-i <clientID>]\n\n" +
         "    -h  Print this help text and quit\n" +
         "    -q  Quiet mode (default is false)\n" +
         "    -a  Perform the relevant action (default is publish)\n" +
         "    -t  Publish/subscribe to <topic> instead of the default\n" +
         "            (publish: \"Sample/Java/v3\", subscribe: \"Sample/#\")\n" +
         "    -m  Use <message text> instead of the default\n" +
         "            (\"Message from MQTTv3 Java client\")\n" +
         "    -s  Use this QoS instead of the default (2)\n" +
         "    -b  Use this name/IP address instead of the default (m2m.eclipse.org)\n" +
         "    -p  Use this port instead of the default (1883)\n\n" +
         "    -i  Use this client ID instead of SampleJavaV3_<action>\n" +
         "    -c  Connect to the server with a clean session (default is false)\n" +
         "     \n\n Security Options \n" +
         "     -u Username \n" +
         "     -z Password \n" +
         "     \n\n SSL Options \n" +
         "    -v  SSL enabled; true - (default is false) " +
         "    -k  Use this JKS format key store to verify the client\n" +
         "    -w  Passpharse to verify certificates in the keys store\n" +
         "    -r  Use this JKS format keystore to verify the server\n" +
         " If javax.net.ssl properties have been set only the -v flag needs to be set\n" +
         "Delimit strings containing spaces with \"\"\n\n" +
         "Publishers transmit a single message then disconnect from the server.\n" +
         "Subscribers remain connected to the server and receive appropriate\n" +
         "messages until <enter> is pressed.\n\n"
      );
   }
}
