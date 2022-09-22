import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 */

/**
 * @author gustavo
 *
 */
public class Encrypt {

    /**
     * Metodo que genera encriptacion del mensaje
     * @param mensaje
     * @param topic
     * @return mensaje encriptado
     */
    public static String encriptar(String msg, String topic) {	
        try 
        {
            String key = "0000000000" + topic +".*/@..";
            key = key.substring(key.length() - 16, key.length());
            // Crear key y cipher
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            // encrypt
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(msg.getBytes());
            byte[] encodedBytes = Base64.getEncoder().encode(encrypted);
            //System.err.println(new String(encodedBytes));
            return (new String(encodedBytes));
        }
        catch(Exception e) 
        {
            return e.toString();
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
}
