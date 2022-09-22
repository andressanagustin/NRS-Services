/**
 *
 */
package com.allc.arms.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author gustavo
 *
 */
public final class ArmsAgentConstants {

    public static final String PROP_FILE_NAME = "ArmsAgentConf.properties";
    public static final String LOG4J_PROP_FILE_NAME = "log4jArmsAgentConf.properties";
    public static final String LOG4J_PROP_FILE_NAME_PRO = "log4jArmsAgentProConf.properties";
    public static final String LOG4J_PROP_FILE_NAME_OPE = "log4jArmsAgentOpeConf.properties";
    public static final String VERSION_COMPILADO = "18/03/2022";

    public static class Process {

        /**
         * Codigo que indica el proceso saf *
         */
        public static final int SAF_PROCESS = 999;
        /**
         * Codigo que indica el proceso que verificara que los otros threads
         * esten corriendo *
         */
        // public static final int IS_ALIVE_PROCESS = 0;
        /**
         * Codigo que indica el envio del HeartBeat *
         */
        public static final int HEART_BEAT_PROCESS = 998;
        /**
         * Codigo que indica el proceso que realizara la obtenecion de los
         * eventos de la cola de mensajes del SO Windows
         */
        public static final int GET_FILE_PROCESS = 4;
        /**
         * *
         */
        public static final int GET_CUSTOMER_FROM_DB = 5;

        /**
         * *
         */
        public static final int DISCOVERER_PROCESS = 6;

        public static final int UPD_OPERATOR = 7;

        public static final int FTP_FILE_PROCESS = 8;

        public static final int TSL_PROCESS = 10;

        public static final int EL_PROCESS = 11;

        public static final int RETENCION_PROCESS = 12;

        public static final int CONSULTA_CED_PAD_RUC_PROCESS = 13;

        public static final int CONSULTA_RETENCION_PROCESS = 14;

        public static final int UPDATE_ITEM_DATA_PROCESS = 15;

        public static final int GENERATE_SUSP_TRANS_PROCESS = 16;

        public static final int UPDATE_MOTO_DATA_PROCESS = 17;

        public static final int CONSULTA_CUPON_REDIMIBLE_PROCESS = 18;

        public static final int FILE_RECEIVER_OPERATION = 19;

        public static final int CONSULTA_DEVOLUCION_PROCESS = 41;

        public static final int UPDATE_DEVOLUCION_PROCESS = 20;

        public static final int CONSULTA_CUSTOMER_DATA_PROCESS = 21;

        public static final int CONSULTA_MOTO_PROCESS = 22;

        public static final int CONSULTA_GERENTE_PROCESS = 23;

        public static final int LOAD_PARAMS_PROCESS = 24;

        public static final int CONSULTA_CLAVE_SOCIEDAD = 25;

        public static final int OPER_SUP_UPDATE = 26;

        public static final int CONSULTA_STORE_PROCESS = 27;

        public static final int CONSULTA_ARQUEO_TND = 28;

        public static final int UPDATE_COD_OPERA = 29;

        public static final int EPS_LOG_PROCESS = 30;

        public static final int WRITE_TRAMA_SYSCARD = 31;

        public static final int UPDATE_EBIL_FILE_PROCESS = 32;

        public static final int CONSULTA_RESERVA = 33;

        public static final int CONSULTA_ULT_NUM_FACTURA = 34;

        public static final int FILE_UPDATER_DOWN_PROCESS = 35;

        public static final int CONSULTA_SEQ_SYSCARD = 36;

        public static final int CONSULTA_PINPAD = 37;

        public static final int STORE_STATUS_UPDATE_PROCESS = 38;

        public static final int PINPAD_CLOSE_OPERATION = 40;

        public static final int RECEIVE_RESP_SYS_OPERATION = 44;

        public static final int SEND_PUNTOS_OPERATION = 47;

        public static final int GEN_SYS_CNL_OPERATION = 48;

        public static final int SEND_TRAMA_POS_OPERATION = 49;

        public static final int EBIL_MASTER_RECEIVER_OPERATION = 50;

        public static final int REPLACE_FILE_OPERATION = 51;

        public static final int FILE_CMD_UPDATER_DOWN_PROCESS = 55;

        public static final int GET_TRX_FROM_TLOG_OPERATION = 62;

        public static final int OPE_REGISTRO_PERSONAS_EN_COLA = 63;

        public static final int LOAD_INIT_OPERATOR_OPERATION = 64;

        public static final int OPERATOR_JETSON_UPLOAD = 66;

        public static final int OPERATOR_JETSON_PING = 67;

        public static final int UPDATE_RESERVAS_OERATION = 68;

        public static final int PUBLISH_JETSON_OPERATION = 71;

        public static final int DELETE_ALERTS_FILES_OPERATION = 73;

        public static final int STATUS_AGENT = 75;
		
		public static final int UPDATE_TIME = 77;

    }

    public static class Tsl {

        public static final int TYPE = 0;

        public static final String STORE_CLOSING = "21";
        public static final String VALIDACION_AFILIADO = "0001";
        public static final String COMPRA_TARJETA = "0002";
        public static final String ACTUALIZACION_CUPO = "0003";
        public static final String RENOVACIONES = "0004";
        public static final String OPERADORA_CELULARES = "0006";
        public static final String DONACIONES = "0007";
        public static final String DESCUENTO_TRX = "0011";
        public static final String DATOS_ADICIONALES_TRX = "0012";
        public static final String PROMO_DOLAR_VIRTUAL = "0014";
        public static final String DESCUENTO_DIAS_D = "0015";
        public static final String DEDUCIBLES = "0016";
        public static final String CUPON_DESC_EMPRESARIAL = "0017";
        public static final String NUEVA_RESERVA_SISPE = "0019";
        public static final String CUPON_DESC_ESPECIAL = "0020";
        public static final String REIMPRESION_DOC_FICAL_PDV = "0021";
        public static final String NOTA_CREDITO = "0022";
        public static final String COMPROBANTE_EGRESO = "0022";
        public static final String COMPROBANTE_INGRESO = "0023";
        public static final String VENTA_BONO_CONSULTA_CYCA = "0024";
        public static final String ENCUESTA_TABLET = "0025";
        public static final String AUT_SUPERV_VIA_REMOTA = "0026";
        public static final String BENEF_EMPRESARIAL_CONTROLADO = "0027";
        public static final String NOTA_CREDITO_ELEC = "0028";
        public static final String FACTURA_ELEC = "0029";
        public static final String LLAMADO_ASISTENTE = "0030";
        public static final String SUPERV_AUT_CANTIDAD = "0031";
        public static final String TARJETA_CREDITO = "0032";
        public static final String CONTROL_FUNDA = "0033";
        public static final String TOTALES_TRX = "0034";
        public static final String SEGURO_GARANT_TOTAL_COMPRA_SEG = "0035";
        public static final String CONTRATO_GARANT_TOTAL = "0036";
        public static final String INGRESO_VOUCHER_CERRADO_EFEC = "0037";
        public static final String BASE_DESC_LOYALTY_ERRADA = "0038";
        public static final String PRODUCTO_DIA = "0039";
        public static final String REG_CAMBIO_DATO_FACTURA = "0040";
        public static final String CRED_PROPIO_FACTURACION = "0041";
        public static final String NOTA_CRED_VALE_ELEC = "0045";
        public static final String REG_GENERICO_VENDEDOR = "0046";
        public static final String VALE_CAJA_CAMB_EFEC = "0047";
        public static final String REPO_CAJA_CHICA_EFEC = "0048";
        public static final String CASHBACK_ALFALFA_RECURRENCIA = "0083";
        public static final String ACUM_PROMO_ALBUM_VIRTUAL = "0201";
        public static final String DATOS_ADIC_TRX2 = "0203";
        public static final String ACUM_CASHBACK_CABECERA = "0204";
        public static final String ACUM_CASHBACK_DETALLE = "0205";
        public static final String DESC_DIRECTO_PROD = "0206";
        public static final String PAGO_DIVIDENDOS_ACCION_EFECT = "0207";
        public static final String TRANS_DELIVERY_PLATF_WEB = "0208";
        public static final String PROMO_LOYALTY = "189015";
        public static final String DESC_ART_LOYALTY = "189001";
        public static final String DESC_ART_PROMO_LOYALTY = "9021";

        public static final String PROMOTION_DISCOUNT_DATA = "53";
        public static final String DESC_EMP_DATA = "98";
    }

    public static class Communication {

        public static final String FRAME_SEP = "'|'";
        /**
         * Cantidad de bytes para la longitud de la trama *
         */
        public static final int QTY_BYTES_LENGTH_FRAME = 5;
        /**
         * cantidad de datos del header *
         */
        public static final int QTY_MEMBERS_HEADER = 6;
        /**
         * Caracter que separa cada dato de la trama *
         */
        public static final String MEM_SEP_CHR = "\\'\\|\\'";
        /**
         * Caracter que se usara para separar datos de la trama en 2do nivel *
         */
        public static final String MEM_SEP_CHR2 = ",";
        /**
         * Caracter para expresiones regulares *
         */
        public static final String REGEX = MEM_SEP_CHR;
        /**
         * Caracter para expresiones regulares *
         */
        public static final String REGEX2 = "\",\"";
        /**
         * Cadena utilizada para fin de linea *
         */
        public static final String CRLF = "\r\n";
        /**
         * Cadena Espacio *
         */
        public static final String SPACE = " ";
        /**
         * cadena vacia *
         */
        public static final String EMPTY_STR = "";
        /**
         * Canal de comunicacion via socket *
         */
        public static final String SOCKET_CHANNEL = "S";
        /**
         * Canal de comunicacion via pipe *
         */
        public static final String PIPE_CHANNEL = "P";
        /**
         * Constante que indica que la conexión es permanente *
         */
        public static final String PERM_CONN = "1";
        /**
         * Constante que indica que la conexión no es permanente *
         */
        public static final String TEMP_CONN = "0";

        public static final String CERO = "0";
    }

    public static class Component {

        public static final String RUTA_FILE_IP = "c:/adx_sdt1/";

        public static final String COMMENT_CHAR = "REM";
    }

    public static class DateFormatters {

        public static final SimpleDateFormat ddMMyy_format = new SimpleDateFormat("ddMMyy", new Locale("ES_ES"));

        public static final SimpleDateFormat yyyyMMddHHmmss_format = new SimpleDateFormat("yyyyMMddHHmmss", new Locale("ES_ES"));
        
        public static final SimpleDateFormat HHmmss_yyMMdd_format = new SimpleDateFormat("HHmmss yyMMdd", new Locale("ES_ES"));
        public static final SimpleDateFormat yyMMddHHmmss_format = new SimpleDateFormat("yyMMddHHmmss", new Locale("ES_ES"));
        public static final SimpleDateFormat HHmmss_format = new SimpleDateFormat("HHmmss", new Locale("ES_ES"));

    }

    public static class AmbitoParams {

        public static final String ARMS_AGENT_PARAMS = "3";

    }
}
