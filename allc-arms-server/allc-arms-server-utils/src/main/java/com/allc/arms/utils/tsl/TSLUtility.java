package com.allc.arms.utils.tsl;

import com.allc.arms.utils.ArmsServerConstants;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.OS4690.FileInputStream4690;

public class TSLUtility {

    private static final Logger LOGGER = Logger.getLogger(TSLUtility.class);

    /**
     * Lee data de un archivo de texto plano.
     *
     * @param nombreArchivo
     * @return
     */
    public static String leerArchivo(String nombreArchivo) {
        String respuesta = "";
        try {

            FileReader fr = new FileReader(nombreArchivo);
            BufferedReader entrada = new BufferedReader(fr);
            String linea;

            while (null != (linea = entrada.readLine())) {
                respuesta = respuesta.concat(linea);
            }
            entrada.close();
            fr.close();

        } catch (Exception e) {

            respuesta = "";
        }
        return respuesta;
    }

    /**
     * Lee data de un archivo de texto plano.
     *
     * @param nombreArchivo
     * @return
     */
    public static String leerArchivo4690(String nombreArchivo) {
        String respuesta = "";
        FileInputStream4690 fstream = null;
        DataInputStream entrada = null;
        BufferedReader buffer = null;
        try {
            fstream = new FileInputStream4690(nombreArchivo);
            entrada = new DataInputStream(fstream);
            buffer = new BufferedReader(new InputStreamReader(entrada));
            String strLinea;
            while ((strLinea = buffer.readLine()) != null) {
                respuesta = respuesta.concat(strLinea);
            }
            entrada.close();
            fstream.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            respuesta = "";
        } finally {

            try {
                buffer.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
            try {
                entrada.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
            try {
                fstream.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return respuesta;
    }

    /**
     * Lee cantBytes bytes de un archivo de texto plano desde la posición pos.
     *
     * @param nombreArchivo
     * @return
     */
    public static String leerArchivo4690(String nombreArchivo, int cantBytes, int pos) {
        String respuesta = "";
        try {
            FileInputStream4690 fstream = new FileInputStream4690(nombreArchivo);
            DataInputStream entrada = new DataInputStream(fstream);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
            char[] cBuff = new char[cantBytes];
            int res = buffer.read(cBuff, pos, cantBytes);
            if (res != -1) {
                respuesta = new String(cBuff);
            }
            entrada.close();
        } catch (IndexOutOfBoundsException e) {
            //no logueamos error porque esta excepción es porque no hay 512 bytes para leer
            respuesta = "";
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            respuesta = "";
        }
        return respuesta;
    }

    /*
	 * @param Array the byte array to which the <code>String</code> will be inserted.
	 * 
	 * @param Offset the position in the byte array from where insertion will begin.
	 * 
	 * @param Value the string to be inserted (this must be only digits).
     */
    public static void packUPD(byte[] Array, int Offset, String Value) {
        // If the string is an odd length then pack with an F
        if (Value.length() % 2 == 1) {
            Value = (char) 0xFF + Value;
        }
        // Now pack each pair of bytes into the array
        for (int i = 0, j = Offset; i < Value.length(); i += 2, j++) {
            Array[j] = (byte) (((Value.charAt(i) & 0x0F) << 4) + (Value.charAt(i + 1) & 0x0F));
        }
    }

    public static String convertBCDToString(byte abyte0[]) {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < abyte0.length; i++) {
            byte j = (new Byte(abyte0[i])).byteValue();
            s.append(j >> 4 & 0xf);
            s.append(j & 0xf);
        }
        return s.toString();
    }

    public static String fechaFormato(Date fecha) {
        SimpleDateFormat formateador = new SimpleDateFormat("yyyyMMddHHmmss"/* , new Locale("ES_ES") */);
        return formateador.format(fecha).toString();
    }

    public static String unpack(byte abyte0[]) { // System.setProperty( "file.encoding", "UTF-8" );
        String salida = "";
        StringBuffer stringbuffer = new StringBuffer();
        for (int i = 0; i < abyte0.length; i++) {
            char c1 = mapToChar((byte) ((abyte0[i] & 0xf0) >> 4));
            char c3 = mapToChar((byte) (abyte0[i] & 0xf));
            stringbuffer.append(c1);
            stringbuffer.append(c3);

        }
        salida = stringbuffer.toString();
        salida = salida.replaceAll("F", "0");
        salida = salida.replaceAll("D", "-");
        return salida;
    }

    protected static char mapToChar(byte byte0) {
        char c;
        if (byte0 < 10) {
            c = (char) (48 + byte0);
        } else {
            c = (char) (65 + (byte0 - 10));
        }
        return c;
    }

    public static String obtiene(List entrada, int pos, int opc) throws UnsupportedEncodingException {
        // System.setProperty( "file.encoding", "UTF-8" );
        String salida = "";
        // String salida = new String("".getBytes(), TSLReader.charset);
        byte[] arregloB;

        if (opc == 1) {
            salida = entrada.get(pos).toString();
            if (salida.length() > 0) {
                arregloB = salida.getBytes("ISO-8859-1");
                salida = unpack(arregloB);
                // salida=Util4690.unpack(salida);
                // salida=Util4690.unpackNum(salida);
                // salida = BCD.unpack(arregloB, false);
            } else {
                salida = "";
            }
        } else {
            // salida= new String(entrada.get(pos).toString().getBytes(), TSLReader.charset);
            salida = entrada.get(pos).toString();
        }
        return salida;
    }

    public static String campos(List entrada, String accion) throws UnsupportedEncodingException {
        // System.setProperty( "file.encoding", "UTF-8" );
        // String campossal= new String("".getBytes(), TSLReader.charset);
        String campossal = "";
        while (entrada.size() > accion.length()) {
            accion = accion + "0";
        }
        int max = entrada.size() <= accion.length() ? entrada.size() : accion.length();
        for (int ind = 0; ind < max; ind++) {
            // System.out.println("ind"+ind);
            int acc = (new Integer(accion.substring(ind, ind + 1))).intValue();
            campossal = campossal + obtiene(entrada, ind, acc) + "|";
        }
        return campossal;
    }

    public static String parseaexecpt(List reg, String ident) throws UnsupportedEncodingException {

        int pp = (new Integer(ident)).intValue();
        // String salida = new String("".getBytes(), TSLReader.charset);
        String salida = "";

        switch (pp) {
            case 1:
                // salida = campos(reg,"1111111000001111101111111111111111111");
                salida = campos(reg, "1111111000001100111010");
                break;
            case 2:
                // salida = campos(reg,"111110011110");
                salida = campos(reg, "1111100111100100");
                break;
            case 3:
                // salida = campos(reg,"1111100100110111111111111111111111111");
                salida = campos(reg, "1111100100110010");
                break;
            case 4:
                // salida = campos(reg,"1111101101111111111111111111111111111");
                salida = campos(reg, "111110110101");
                break;
            case 5:
                // salida = campos(reg,"11111000110");
                salida = campos(reg, "1111100011010");
                break;
            case 6:
                // salida = campos(reg,"111110011011111111111111111111111111");
                salida = campos(reg, "111110011010");
                break;
            case 7:
                // salida = campos(reg,"111110000");
                salida = campos(reg, "1111100000");
                break;
            case 8:
                // salida = campos(reg,"1111110011111111111111111111111111111");
                salida = campos(reg, "111111000");
                break;
            case 9:
                // salida = campos(reg,"111111111110");
                salida = campos(reg, "111111111110");
                break;
            case 10:
                salida = campos(reg, "111110010");
                break;
            case 11:
                // salida = campos(reg,"1111100011011111111111111111111111111");
                salida = campos(reg, "1111100011010");
                break;
            case 12:
                // salida = campos(reg,"11111000");
                salida = campos(reg, "111110001110");
                break;
            case 13:
                // salida = campos(reg,"11111000000000110");
                salida = campos(reg, "11111000000000011010");
                break;
            case 14:
                // salida = campos(reg,"11111000000000110");
                salida = campos(reg, "11111000000000011010");
                break;
            case 15:
                // salida = campos(reg,"11111010");
                salida = campos(reg, "11111010");
                break;
            case 16:
                // salida = campos(reg,"1111100");
                salida = campos(reg, "1111100");
                break;
            case 17:
                // salida = campos(reg,"1111100111111111111111111111111111111111");
                salida = campos(reg, "11111001110");
                break;
            case 18:
                // salida = campos(reg,"111110000000000");
                salida = campos(reg, "1111100000000000");
                break;
            case 19:
                // salida = campos(reg,"111110");
                salida = campos(reg, "1111100000");
                break;
            case 20:
                // salida = campos(reg,"011111000000000");
                salida = campos(reg, "01111100000000000");
                break;
            case 21:
                // salida = campos(reg,"011111000000000");
                salida = campos(reg, "01111100000000000");
                break;
            case 22:
                // salida = campos(reg,"011111000000000");
                salida = campos(reg, "01111100000000000");
                break;
            case 23:
                // salida = campos(reg,"011111000000000");
                salida = campos(reg, "0111110000000000");
                break;
            case 24:
                // salida = campos(reg,"0111100000000");
                salida = campos(reg, "01111000000000");
                break;
            case 25:
                // salida = campos(reg,"011111000011");
                salida = campos(reg, "0111110000110");
                break;
            case 26:
                // salida = campos(reg,"01111010");
                salida = campos(reg, "011110100");
                break;
            case 30:
                // salida = campos(reg,"011110100");
                salida = campos(reg, "0111101000");
                break;
            case 31:
                // salida = campos(reg,"011110100");
                salida = campos(reg, "0111101000");
                break;
            case 32:
                // salida = campos(reg,"011110100");
                salida = campos(reg, "0111101000");
                break;
            case 33:
                // salida = campos(reg,"0111101");
                salida = campos(reg, "0111101");
                break;
            case 34:
                salida = campos(reg, "0111101");
                break;
            case 35:
                salida = campos(reg, "011110");
                break;
            case 40:
                // salida = campos(reg,"011110100");
                salida = campos(reg, "011110100");
                break;
            case 41:
                // salida = campos(reg,"0111110000");
                salida = campos(reg, "01111100000");
                break;
            case 42:
                salida = campos(reg, "0111110");
                break;
            case 43:
                // salida = campos(reg,"0111110");
                salida = campos(reg, "0111110");
                break;
            case 44:
                // salida = campos(reg,"0111110100");
                salida = campos(reg, "01111101000");
                break;
            case 45:
                // salida = campos(reg,"0111110100");
                salida = campos(reg, "01111101000");
                break;
            case 46:
                // salida = campos(reg,"0111110100");
                salida = campos(reg, "01111101000");
                break;
            case 47:
                // salida = campos(reg,"011110100");
                salida = campos(reg, "01111101000");
                break;
            case 48:
                salida = campos(reg, "111110000");
                break;
            case 50:
                salida = campos(reg, "011111");
                break;
            case 51:
                // salida = campos(reg,"0111100");
                salida = campos(reg, "01111000");
                break;
            case 52:
                // salida = campos(reg,"01111011");
                salida = campos(reg, "01111011");
                break;
            case 55:
                salida = campos(reg, "011110000");
                break;
            case 80:
                salida = campos(reg, "11111011");
                break;
            case 81:
                salida = campos(reg, "111111011001");
                break;
            case 82:
                salida = campos(reg, "11111110100");
                break;
            case 83:
                salida = campos(reg, "1111111100");
                break;
            case 89:
                // salida = campos(reg,"111110");
                salida = campos(reg, "11111110000000");
                break;
            case 99:
                salida = campos(reg, "111110000");
                break;

            default:
                // log.info("no encontrado");
                System.out.println("ExcLog no encontrado  ident: " + ident + " reg " + reg.toString());
                break;
        }
        return salida;
    }

    public static String parseatlog(List reg, int ident) throws UnsupportedEncodingException {

        // System.setProperty( "file.encoding", "UTF-8" );
        String salida = "";
        // String salida = new String("".getBytes(), TSLReader.charset);

        switch (ident) {
            case 0:
                salida = campos(reg, "111111111111111");
                break;
            case 1:
                salida = campos(reg, "111111111");
                break;
            case 2:
                salida = campos(reg, "11111111");
                break;
            case 3:
                salida = campos(reg, "11111");
                break;
            case 4:
                salida = campos(reg, "11111");
                break;
            case 5:
                salida = campos(reg, "111111");
                break;
            case 6:
                salida = campos(reg, "111111");
                break;
            case 7:
                salida = campos(reg, "111111111");
                break;
            case 8:
                salida = campos(reg, "111111111");
                break;
            case 9:
                salida = campos(reg, "111");
                break;
            case 10:
                salida = campos(reg, "111");
                break;
            case 11:
                salida = campos(reg, "1111111");
                break;
            // case 12:
            // salida = campos(reg,"111111");
            // break;
            case 13:
                salida = campos(reg, "11111111111111");
                break;
            // case 14:
            // salida = campos(reg,"100");
            // break;
            // case 15:
            // salida = campos(reg,"11");
            // break;
//			case 16:
//				salida = campos(reg, "1011000110011100000000000000111110");
//				break;
            case 20:
                salida = campos(reg, "10");
                break;
            case 21:
                salida = campos(reg, "1111");
                break;
//			case 80:
//				salida = campos(reg, "110111111111111101");
//				break;
//			case 97:
//				salida = campos(reg, "11111111111111111111111111111111111");
//				break;
            case 99:
                salida = parsearString99(reg);
                break;

            default:
                // log.info("no encontrado");
                System.out.println("Tlog no encontrado ident: " + ident + " reg " + reg.toString());

                break;
        }
        if (salida.length() > 0) {
            salida = salida.substring(0, salida.length() - 1);
        }
        return salida;
    }

    protected static String parsearString99(List reg) throws UnsupportedEncodingException {
        String campo1 = unpack(reg.get(1).toString().getBytes("ISO-8859-1"));
        if (ArmsServerConstants.Tsl.VALIDACION_AFILIADO.equals(campo1)) {
            return campos(reg, "1111110111");
        } else if (ArmsServerConstants.Tsl.COMPRA_TARJETA.equals(campo1)) {
            return campos(reg, "1111011111111111111");
        } else if (ArmsServerConstants.Tsl.ACTUALIZACION_CUPO.equals(campo1)) {
            return campos(reg, "1111110111");
        } else if (ArmsServerConstants.Tsl.PROMOTION_DISCOUNT_DATA.equals(campo1)) {
            return campos(reg, "110000");
        } else if (ArmsServerConstants.Tsl.RENOVACIONES.equals(campo1)) {
            return campos(reg, "11111");
        } else if (ArmsServerConstants.Tsl.OPERADORA_CELULARES.equals(campo1)) {
            return campos(reg, "1101011111");
        } else if (ArmsServerConstants.Tsl.DONACIONES.equals(campo1)) {
            return campos(reg, "1101");
        } else if (ArmsServerConstants.Tsl.DESCUENTO_TRX.equals(campo1)) {
            return campos(reg, "1111");
        } else if (ArmsServerConstants.Tsl.DATOS_ADICIONALES_TRX.equals(campo1)) {
            return campos(reg, "111111");
        } else if (ArmsServerConstants.Tsl.PROMO_DOLAR_VIRTUAL.equals(campo1)) {
            return campos(reg, "111111");
        } else if (ArmsServerConstants.Tsl.DESCUENTO_DIAS_D.equals(campo1)) {
            return campos(reg, "11111");
        } else if (ArmsServerConstants.Tsl.DEDUCIBLES.equals(campo1)) {
            return campos(reg, "111111");
        } else if (ArmsServerConstants.Tsl.CUPON_DESC_EMPRESARIAL.equals(campo1)) {
            return campos(reg, "11111111");
        } else if (ArmsServerConstants.Tsl.NUEVA_RESERVA_SISPE.equals(campo1)) {
            return campos(reg, "11111111");
        } else if (ArmsServerConstants.Tsl.CUPON_DESC_ESPECIAL.equals(campo1)) {
            return campos(reg, "11111111");
        } else if (ArmsServerConstants.Tsl.REIMPRESION_DOC_FICAL_PDV.equals(campo1)) {
            return campos(reg, "11111110");
        } else if (ArmsServerConstants.Tsl.COMPROBANTE_EGRESO.equals(campo1)) {
            return campos(reg, "11111101");
        } else if (ArmsServerConstants.Tsl.COMPROBANTE_INGRESO.equals(campo1)) {
            return campos(reg, "11110");
        } else if (ArmsServerConstants.Tsl.VENTA_BONO_CONSULTA_CYCA.equals(campo1)) {
            return campos(reg, "1111");
        } else if (ArmsServerConstants.Tsl.ENCUESTA_TABLET.equals(campo1)) {
            return campos(reg, "1111");
        } else if (ArmsServerConstants.Tsl.AUT_SUPERV_VIA_REMOTA.equals(campo1)) {
            return campos(reg, "11111111");
        } else if (ArmsServerConstants.Tsl.BENEF_EMPRESARIAL_CONTROLADO.equals(campo1)) {
            return campos(reg, "11111");
        } else if (ArmsServerConstants.Tsl.NOTA_CREDITO_ELEC.equals(campo1)) {
            return campos(reg, "11111101100");
        } else if (ArmsServerConstants.Tsl.FACTURA_ELEC.equals(campo1)) {
            return campos(reg, "1111000");
        } else if (ArmsServerConstants.Tsl.LLAMADO_ASISTENTE.equals(campo1)) {
            return campos(reg, "1111101");
        } else if (ArmsServerConstants.Tsl.SUPERV_AUT_CANTIDAD.equals(campo1)) {
            return campos(reg, "111");
        } else if (ArmsServerConstants.Tsl.TARJETA_CREDITO.equals(campo1)) {
            return campos(reg, "1111100110111011110");
        } else if (ArmsServerConstants.Tsl.CONTROL_FUNDA.equals(campo1)) {
            return campos(reg, "111111");
        } else if (ArmsServerConstants.Tsl.TOTALES_TRX.equals(campo1)) {
            return campos(reg, "111111111111111");
        } else if (ArmsServerConstants.Tsl.SEGURO_GARANT_TOTAL_COMPRA_SEG.equals(campo1)) {
            return campos(reg, "11001111110110");
        } else if (ArmsServerConstants.Tsl.CONTRATO_GARANT_TOTAL.equals(campo1)) {
            return campos(reg, "11111");
        } else if (ArmsServerConstants.Tsl.INGRESO_VOUCHER_CERRADO_EFEC.equals(campo1)) {
            return campos(reg, "1111111");
        } else if (ArmsServerConstants.Tsl.BASE_DESC_LOYALTY_ERRADA.equals(campo1)) {
            return campos(reg, "111111");
        } else if (ArmsServerConstants.Tsl.PRODUCTO_DIA.equals(campo1)) {
            return campos(reg, "1111");
        } else if (ArmsServerConstants.Tsl.REG_CAMBIO_DATO_FACTURA.equals(campo1)) {
            return campos(reg, "110111");
        } else if (ArmsServerConstants.Tsl.CRED_PROPIO_FACTURACION.equals(campo1)) {
            return campos(reg, "1111111110010");
        } else if (ArmsServerConstants.Tsl.NOTA_CRED_VALE_ELEC.equals(campo1)) {
            return campos(reg, "1111110110000100");
        } else if (ArmsServerConstants.Tsl.REG_GENERICO_VENDEDOR.equals(campo1)) {
            return campos(reg, "11110");
        } else if (ArmsServerConstants.Tsl.VALE_CAJA_CAMB_EFEC.equals(campo1)) {
            return campos(reg, "1111111");
        } else if (ArmsServerConstants.Tsl.REPO_CAJA_CHICA_EFEC.equals(campo1)) {
            return campos(reg, "1111111");
        } else if (ArmsServerConstants.Tsl.CASHBACK_ALFALFA_RECURRENCIA.equals(campo1)) {
            return campos(reg, "1110");
        } else if (ArmsServerConstants.Tsl.ACUM_PROMO_ALBUM_VIRTUAL.equals(campo1)) {
            return campos(reg, "111001");
        } else if (ArmsServerConstants.Tsl.DATOS_ADIC_TRX2.equals(campo1)) {
            return campos(reg, "111000");
        } else if (ArmsServerConstants.Tsl.ACUM_CASHBACK_CABECERA.equals(campo1)) {
            return campos(reg, "1110100");
        } else if (ArmsServerConstants.Tsl.ACUM_CASHBACK_DETALLE.equals(campo1)) {
            return campos(reg, "110001");
        } else if (ArmsServerConstants.Tsl.PAGO_DIVIDENDOS_ACCION_EFECT.equals(campo1)) {
            return campos(reg, "1101111");
        } else if (ArmsServerConstants.Tsl.TRANS_DELIVERY_PLATF_WEB.equals(campo1)) {
            return campos(reg, "1100");
        } else if (ArmsServerConstants.Tsl.DESC_DIRECTO_PROD.equals(campo1)) {
            return campos(reg, "1111");
        } else if (ArmsServerConstants.Tsl.PROMO_LOYALTY.equals(campo1)) {
            return campos(reg, "11111");
        } else if (ArmsServerConstants.Tsl.DESC_ART_LOYALTY.equals(campo1)) {
            return campos(reg, "1111");
        } else if (ArmsServerConstants.Tsl.DESC_ART_PROMO_LOYALTY.equals(campo1)) {
            return campos(reg, "11101");
        } else if (ArmsServerConstants.Tsl.DESC_EMP_DATA.equals(campo1)) {
            return campos(reg, "1111111111111110");
        }
        return campos(reg, "110");
    }

}
