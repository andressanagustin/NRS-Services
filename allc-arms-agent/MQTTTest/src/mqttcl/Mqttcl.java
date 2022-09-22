package mqttcl;

import org.eclipse.paho.client.mqttv3.MqttException;


public class Mqttcl {

   static String protocolo = "tcp://";
   static String brokerURL, topic, clienteId;

   /**
    * parametros:
    * URL del broker
    * tema
    * identificacion del cliente
    * qos
    */
   public static void main(String[] args) {
      int accion = 1;
      String mensaje = "mensaje de prueba   de visores 4690     ";

      if(args.length < 3) {
         brokerURL = protocolo + "172.20.10.14:1883";
         topic = "prueba12";
         clienteId = "pos4690";
      } else {
         brokerURL = protocolo + args[0];
         topic = args[1];
         clienteId = args[2];
      }

      if (args.length > 3) {
         if (args[3].equalsIgnoreCase("-s")) {
            accion = 1;
         }
         if (args[3].equalsIgnoreCase("-p")) {
            accion = 2;
         }
         if (args[3].equalsIgnoreCase("-h")) {
            accion = 4;
         }
      }

      if (args.length > 4) {
         mensaje = args[4];
      }

      if ((accion & 2) == 2) {
         envia(brokerURL, topic, clienteId, mensaje);
      }

      if ((accion & 1) == 1) {
         recibe(brokerURL, topic, clienteId);
      }

      if (accion == 4) {
         System.out.println(
            "Sintaxis:\n" +
            "para suscribir a un tema\n" +
            "brokerURL:port tema Id_cliente -s\n" +
            "172.20.10.14:1883 visorAmarillo/estado pos4201 -s\n\n" +
            "para publicar un mensaje\n" +
            "brokerURL:port tema Id_cliente -p mensaje\n" +
            "172.20.10.14:1883 visorAmarillo/mensaje pos4202 -p \"mensaje\"\n"
         );
      }

      System.exit(0);
   }

   private static void recibe(String br, String to, String cl) {
      Suscribir cliente = new Suscribir(br, to, cl);
      cliente.start();
      while (true) {
    	  try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  if(!cliente.isConnected())
    		  cliente.start();
      }
   }

   private static void envia(String br, String to, String cl, String msg) {

      // Suscripciones no duraderas
      boolean cleanSession = false;
      boolean quietMode = false;
      int qos = 2;
      String password = null;
      String userName = "";

      try {
         // Crea una instancia de la clase publicar
         Publicar p = new Publicar(br, cl, cleanSession, quietMode, userName, password);

         p.publish(to, qos, msg.getBytes());

	} catch(MqttException me) {
         // Mostrar detalles de cualquier excepción que ocurra
         System.out.println("reason "+me.getReasonCode());
         System.out.println("msg "+me.getMessage());
         System.out.println("loc "+me.getLocalizedMessage());
         System.out.println("cause "+me.getCause());
         System.out.println("excep "+me);
         me.printStackTrace();
	}

   }
}

