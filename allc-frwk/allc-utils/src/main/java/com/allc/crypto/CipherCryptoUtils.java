/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 *
 * @author Tyrone López
 */
public class CipherCryptoUtils {

    private static final Logger LOGGER = Logger.getLogger(CipherCryptoUtils.class);

    private final String SECRETKEY = "s~e!r@v#i$c%i^o&d*e(a)c_r-e=d+i,t.a@c#i$o%n^e&c*u`a`~t`or$i$a$n$a"; //llave para desenciptar datos

    public CipherCryptoUtils() {
        LOGGER.info("Crea --- archivo cyper");
    }

    public String cifra(String texto) throws Exception {
        String base64EncryptedString = "";

        try {

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(SECRETKEY.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);

            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            Cipher cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] plainTextBytes = texto.getBytes("utf-8");
            byte[] buf = cipher.doFinal(plainTextBytes);
            byte[] base64Bytes = Base64.encodeBase64(buf);
            base64EncryptedString = new String(base64Bytes);

        } catch (UnsupportedEncodingException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (InvalidKeyException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (BadPaddingException ex) {
            LOGGER.error(ex.getMessage(), ex);

        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error(ex.getMessage(), ex);

        } catch (IllegalBlockSizeException ex) {
            LOGGER.error(ex.getMessage(), ex);

        } catch (NoSuchPaddingException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return base64EncryptedString;
    }

    public String descifra(String textoEncriptado) throws Exception {

        String base64EncryptedString = "";

        try {
            byte[] message = Base64.decodeBase64(textoEncriptado.getBytes("utf-8"));
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(SECRETKEY.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            SecretKey key = new SecretKeySpec(keyBytes, "DESede");

            Cipher decipher = Cipher.getInstance("DESede");
            decipher.init(Cipher.DECRYPT_MODE, key);

            byte[] plainText = decipher.doFinal(message);

            base64EncryptedString = new String(plainText, "UTF-8");

        } catch (UnsupportedEncodingException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (InvalidKeyException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (BadPaddingException ex) {
            LOGGER.error(ex.getMessage(), ex);

        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error(ex.getMessage(), ex);

        } catch (IllegalBlockSizeException ex) {
            LOGGER.error(ex.getMessage(), ex);

        } catch (NoSuchPaddingException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return base64EncryptedString;
    }
}
