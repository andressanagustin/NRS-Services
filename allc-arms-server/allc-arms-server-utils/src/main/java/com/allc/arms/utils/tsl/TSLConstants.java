package com.allc.arms.utils.tsl;

import java.text.SimpleDateFormat;

public class TSLConstants {

	public static final int StringType = 0;

	public static final String TLOG_SUFIX_DATE_TIME_FORMAT_ACE = "yyMMddHHmm";
	public static final String TLOG_SUFIX_DATE_TIME_FORMAT_ACE_2 = "yyyyMMdd";
	public static final String TLOG_SUFIX_DATE_TIME_FORMAT_SM = "yyMMdd";
	public static final int TSL_RECORD_POSITION_BODY = 0;
	public static final int BUSINESS_DATE_DAY_POSITION_BODY = 1;

	public static class Formatters {
		public static final SimpleDateFormat TLOG_SUFIX_DATE_TIME_FORMATTER_ACE = new SimpleDateFormat(TLOG_SUFIX_DATE_TIME_FORMAT_ACE);
		public static final SimpleDateFormat TLOG_SUFIX_DATE_TIME_FORMATTER_ACE_2 = new SimpleDateFormat(TLOG_SUFIX_DATE_TIME_FORMAT_ACE_2);
		public static final SimpleDateFormat TLOG_SUFIX_DATE_TIME_FORMATTER_SM = new SimpleDateFormat(TLOG_SUFIX_DATE_TIME_FORMAT_SM);
	}

	/** String Types of the Transaction summary log **/
	public static class StringTypeCode {
		public static final int TRANSACTION_HEADER = 0;
		public static final int ITEM_ENTRY = 1;
		public static final int ITEM_ENTRY_EXTENSION = 2;
		public static final int DISCOUNT = 3;
		public static final int VOIDED_DISCOUNT = 4;
		public static final int TENDER = 5;
		public static final int TENDER_CORRECTION = 6;
		public static final int TAX = 7;
		public static final int TAX_REFUND = 8;
		public static final int CHANGE = 9;
		public static final int MANAGER_OVERRIDE = 10;
		public static final int DATA_ENTRY = 11;
		public static final int TILL_CHANGE = 13;
		public static final int SUREPOS_ACE_EPS_TENDER = 16;
		public static final int EXCEPTION_LOG = 20;
		public static final int STORE_CLOSING = 21;
		public static final int WIC_EBT_DATA = 80;
		public static final int EXTRA_DATA = 97;
		public static final int USER_DATA = 99;

	}

	public static class TransactionHeaderTranType {

		public static final String CHECKOUT_TRANSACTION = "00";
		public static final String TENDER_CASHING = "01";
		public static final String TENDER_EXCHANGE = "02";
		public static final String CASHIER_LOAN = "03";
		public static final String CASHIER_PICKUP = "04";
		public static final String TENDER_LISTING = "05";
		public static final String PRICE_VERIFY_CHANGE = "06";
		public static final String TRAINING_SESSION = "07";
		public static final String TERMINAL_TRANSFER = "08";
		public static final String TERMINAL_MONITOR = "09";
		public static final String TENDER_COUNT = "10";
		public static final String RESERVED1 = "11";
		public static final String RETURN_ITEM_TRANSACTION = "12";
		public static final String WIC_TRANSACTION = "13";
		public static final String RESERVED2 = "14";
		public static final String REPRINT_TENDER_RECEIPT = "15";
		public static final String VOIDED_CHECKOUT_TRANSACTION = "16";
		public static final String OPERATOR_SIGN_OFF = "17";
		public static final String STANDALONE_SESSION = "18";
		public static final String EBT_BALANCE_INQUIRY = "20";
		public static final String VALUE_CARD_BALANCE_INQUIRY = "21";
		public static final String WIC_EBT_BALANCE_INQUIRY = "22";
		public static final String DEPARTMENT_TOTALS_REPORT = "80";

	}

	/** Detail of the String Type 00 = TRANSACTION_HEADER **/
	public static class Type00 {
		public static final int TYPE = 0;
		public static final int TERMINAL = 1;
		public static final int TRANSNUM = 2;
		public static final int DATETIME = 3;
		public static final int TRANTYPE = 4;
		public static final int NUMSTRING = 5;
		public static final int OPERATOR = 6;
		public static final int PASSWORD = 7;
		public static final int GROSSPOS = 8;
		public static final int GROSSNEG = 9;
		public static final int RINGTIME = 10;
		public static final int TENDERTI = 11;
		public static final int SPECIAL = 12;
		public static final int INACTIVE = 13;
		public static final int INDICAT1 = 14;

		/** Positions **/
		public static final int INDICAT1_SIGN_OFF_IS_FALSE_BIT_POSITION = 4;
		public static final int INDICAT1_OPERATOR_SIGN_ON_BIT_POSITION = 6;
		public static final int INDICAT1_GROSS_POSITIVE_IS_NEGATIVE_BIT_POSITION = 21;
		public static final int INDICAT1_GROSS_NEGATIVE_IS_NEGATIVE_BIT_POSITION = 20;

		/** Totals **/
		public static final String TRANSACTION_GROSS_POSITIVE_AMOUNT = "TransactionGrossPositiveAmount";
		public static final String TRANSACTION_GROSS_NEGATIVE_AMOUNT = "TransactionGrossNegativeAmount";
		public static final String TRANSACTION_TAX_AMOUNT = "TransactionTaxAmount";
		public static final String TRANSACTION_POSITIVE_DISCOUNT = "TransactionPositiveDiscount";
		public static final String TRANSACTION_NEGATIVE_DISCOUNT = "TransactionNegativeDiscount";
		public static final String TRANSACTION_PROMO_POSITIVE_DISCOUNT = "TransactionPromoPositiveDiscount";
		public static final String TRANSACTION_PROMO_NEGATIVE_DISCOUNT = "TransactionPromoNegativeDiscount";
	}

	/** Detail of the String Type 01 = ITEM_ENTRY **/
	public static class Type01 {
		/** Fields **/
		public static final int TYPE = 0;
		public static final int ITEMCODE = 1;
		public static final int XPRICE = 2;
		public static final int DEPARTME = 3;
		public static final int FAMILYNU = 4;
		public static final int INDICAT1 = 5;
		public static final int INDICAT2 = 6;
		public static final int INDICAT3 = 7;
		/** Positions **/
		public static final int INDICAT1_WEIGHT_ITEM_BIT_POSITION = 14;
		public static final int INDICAT1_PRICE_ENTERED_BIT_POSITION = 13;
		public static final int INDICAT1_TAXABLE_A_BIT_POSITION = 7;
		public static final int INDICAT1_TAXABLE_B_BIT_POSITION = 6;
		public static final int INDICAT1_TAXABLE_C_BIT_POSITION = 5;
		public static final int INDICAT1_TAXABLE_D_BIT_POSITION = 4;

		public static final int INDICAT2_EXTENSION_FOLLOWS_THIS_STRING_BIT_POSITION = 1;
		public static final int INDICAT2_NEGATIVE_PRICE_DUE_TO_DEAL_BIT_POSITION = 2;
		public static final int INDICAT2_REFUND_KEY_PRESSED_BIT_POSITION = 6;
		public static final int INDICAT2_CANCEL_KEY_PRESSED_BIT_POSITION = 7;

		/** NOT USED **/
		public static final int INDICAT2_DATA_ENTRY_WITH_ITEM_BIT_POSITION = 13;

		/** Values T (ITEM TYPE) **/
		public static final String INDICAT3_T_NORMAL_ITEM_SALE_VALUE = "0";
		public static final String INDICAT3_T_DEPOSIT_VALUE = "1";
		public static final String INDICAT3_T_REFUND_VALUE = "2";
		public static final String INDICAT3_T_DEPOSIT_RETURN_VALUE = "3";
		public static final String INDICAT3_T_MISC_TRANS_RECEIPT_SALE_VALUE = "4";
		public static final String INDICAT3_T_MISC_TRANS_PAYOUT_REFUND_VALUE = "5";
		public static final String INDICAT3_T_MANUFACTURER_COUPON_VALUE = "6";
		public static final String INDICAT3_T_STORE_COUPON_VALUE = "7";
		public static final String INDICAT3_T_ITEM_SALE_CANCEL_VALUE = "8";
		public static final String INDICAT3_T_DEPOSIT_CANCEL_VALUE = "9";
		/** Values O (ENTRY METHOD) **/
		public static final String INDICAT3_O_SCANNED_ITEM_CODE_VALUE = "0";
		public static final String INDICAT3_O_KEYED_ITEM_CODE_VALUE = "1";
		public static final String INDICAT3_O_ITEM_LOOKUP_KEY_USED_VALUE = "2";
		public static final String INDICAT3_O_ITEM_CODE_LINKED_TO_VALUE = "3";
		public static final String INDICAT3_O_RESERVED1_VALUE = "4";
		public static final String INDICAT3_O_ITEM_CREATED_BY_SERVICE_VALUE = "5";
		public static final String INDICAT3_O_RESERVED2_VALUE = "6";
		public static final String INDICAT3_O_REEDEMPTION_OF_POINTS_VALUE = "8";
		public static final String INDICAT3_O_BONUS_POINTS_VALUE = "9";
		/** Names O (ENTRY METHOD) NAMES **/
		public static final String INDICAT3_O_SCANNED_ITEM_CODE_NAME = "SCANNED";
		public static final String INDICAT3_O_KEYED_ITEM_CODE_NAME = "KEYED";
		public static final String INDICAT3_O_ITEM_LOOKUP_KEYED_USED_NAME = "LOOKUP KEYED";
		public static final String INDICAT3_O_ITEM_CODE_LINKED_TO_NAME = "LINKED";
		public static final String INDICAT3_O_RESERVED1_NAME = "RESERVED1";
		public static final String INDICAT3_O_ITEM_CREATED_BY_SERVICE_NAME = "SERVICE";
		public static final String INDICAT3_O_RESERVED2_NAME = "RESERVED2";
		public static final String INDICAT3_O_REEDEMPTION_POINTS_NAME = "REEDEMPTION POINTS";
		public static final String INDICAT3_O_BONUS_POINTS_NAME = "BONUS POINTS";

	}

	/** Detail of the String Type 02 = ITEM_ENTRY_EXTENSION **/
	public static class Type02 {
		/** Fields **/
		public static final int TYPE = 0;
		public static final int MPGROUP = 1;
		public static final int DEALQUAN = 2;
		public static final int METHOD = 3;
		public static final int SALEQUAN = 4;
		public static final int SALEPRIC = 5;
		public static final int QTYORWGTORVOL = 6;
		public static final int INDICAT1 = 7;
		/** Positions **/
		public static final int INDICAT1_SCALE_WEIGHT_BIT_POSITION = 3;
		public static final int INDICAT1_WEIGHT_OR_VOLUME_KEY_BIT_POSITION = 6;

	}

	/** Detail of the String Type 03 = DISCOUNT, 04 = VOIDED_DISCOUNT **/
	public static class Type0304 {
		/** Fields **/
		public static final int TYPE = 0;
		public static final int DISGROUP = 1;
		public static final int DISRATE = 2;
		public static final int AMOUNT = 3;
		public static final int TAXEXEMP = 4;
		public static final int TAXABLEEXEMPTAMOUNT = 5;
		public static final int TAXEXEMPTID = 6;
		public static final int TAXEXEMPTAMOUNTA = 7;
		public static final int TAXEXEMPTAMOUNTB = 8;
		public static final int TAXEXEMPTAMOUNTC = 9;
		public static final int TAXEXEMPTAMOUNTD = 10;
		public static final int TAXABLEEXEMPTAMOUNTA = 11;
		public static final int TAXABLEEXEMPTAMOUNTB = 12;
		public static final int TAXABLEEXEMPTAMOUNTC = 13;
		public static final int TAXABLEEXEMPTAMOUNTD = 14;
		public static final int TAXEXEMPTAMOUNTE = 15;
		public static final int TAXEXEMPTAMOUNTF = 16;
		public static final int TAXEXEMPTAMOUNTG = 17;
		public static final int TAXEXEMPTAMOUNTH = 18;
		public static final int TAXABLEEXEMPTAMOUNTE = 19;
		public static final int TAXABLEEXEMPTAMOUNTF = 20;
		public static final int TAXABLEEXEMPTAMOUNTG = 21;
		public static final int TAXABLEEXEMPTAMOUNTH = 22;

	}

	/** Detail of the String Type 05 = TENDER, 06 = TENDER_CORRECTION **/
	public static class Type0506 {
		/** Fields **/
		public static final int TYPE = 0;
		public static final int TENDTYPE = 1;
		public static final int AMTTENDE = 2;
		public static final int AMTTNFEE = 3;
		public static final int CUSTOMER = 4;
		public static final int STATUS = 5;

		/** Values (ACCOUNT_STATUS_CODE) **/
		public static final String TENDER_05 = "05";
		public static final String TENDER_CORRECTION_06 = "06";

		/*
		 * public static final int ACCOUNT_STATUS_CODE_00_NONE_VALUE = 0; public static final int ACCOUNT_STATUS_CODE_50_ACCEPT_TENDER_VALUE
		 * = 50; public static final int ACCOUNT_STATUS_CODE_51_REJECT_TENDER_RISK_1_VALUE = 51; public static final int
		 * ACCOUNT_STATUS_CODE_52_REJECT_TENDER_RISK_2_VALUE = 52; public static final int ACCOUNT_STATUS_CODE_53_REJECT_TENDER_RISK_3_VALUE
		 * = 53; public static final int ACCOUNT_STATUS_CODE_54_REJECT_TENDER_RISK_4_VALUE = 54; public static final int
		 * ACCOUNT_STATUS_CODE_60_ACCEPT_TENDER_NO_FEE_VALUE = 60;
		 */
	}

	/** Detail of the String Type 07 = TAX, 08 = TAX_REFUND **/
	public static class Type0708 {
		/** Fields **/
		public static final int TYPE = 0;
		public static final int AMTTAXA = 1;
		public static final int AMTTAXB = 2;
		public static final int AMTTAXC = 3;
		public static final int AMTTAXD = 4;
		public static final int AMTSALEA = 5;
		public static final int AMTSALEB = 6;
		public static final int AMTSALEC = 7;
		public static final int AMTSALED = 8;

		public static final String A = "A";
		public static final String B = "B";
		public static final String C = "C";
		public static final String D = "D";

		public static final int SALE = 1;
		public static final int RETURN = 2;
	}

	/** Detail of the String Type 09 = CHANGE **/
	public static class Type09 {
		/** Fields **/
		public static final int TYPE = 0;
		public static final int TENDTYPE = 1;
		public static final int AMTCHANGE = 2;
	}

	/** Detail of the String Type 10 = MANAGER OVERRIDE **/
	public static class Type10 {
		/** Fields **/
		public static final int TYPE = 0;
		public static final int OVERRIDE = 1;
		public static final int REASON = 2;
	}

	/** Detail of the String Type 11 = DATA ENTRY **/
	public static class Type11 {
		/** Fields **/
		public static final int TYPE = 0;
		public static final int DATA1 = 1;
		public static final int DATA2 = 2;
		public static final int DATA3 = 3;
		public static final int DATA4 = 4;
		public static final int DATA5 = 5;
		public static final int DATA6 = 6;

		public static final String IDENTIFIER_VALUE = "EE";

		public static final int IDENTIFIER = 1;
		public static final int CUSTOMERACCOUNTID = 2;
		public static final int POINTS = 3;
		public static final int COUPONAMOUNT = 4;
		public static final int COUPONCOUNT = 5;
		public static final int MESSAGECOUNT = 6;
		public static final int TRANSFERREDTRANSCOUNT = 7;
		public static final int TRANSFERREDTRANSAMOUNT = 8;
		public static final int BONUSPOINTS = 9;
		public static final int REEDEMEDPOINTS = 10;
		public static final int ENTRYMETHOD = 11;

	}

	/** Detail of the String Type 13 = TILL CHANGE **/
	public static class Type13 {
		/** Fields **/
		public static final int TYPE = 0;
		public static final int NUMLOANS = 1;
		public static final int AMTLOANS = 2;
		public static final int NUMPKUPS = 3;
		public static final int AMTPKUPS = 4;
		public static final int TENDTYP1POS = 5;
		public static final int AMTTEND1POS = 6;
		public static final int TENDTYP2POS = 7;
		public static final int AMTTEND2POS = 8;
		public static final int TENDTYP3POS = 9;
		public static final int AMTTEND3POS = 10;
		public static final int TENDTYP4POS = 11;
		public static final int AMTTEND4POS = 12;
		public static final int TENDTYP5POS = 13;
		public static final int AMTTEND5POS = 14;
		public static final int TENDTYP6POS = 15;
		public static final int AMTTEND6POS = 16;
		public static final int TENDTYP7POS = 17;
		public static final int AMTTEND7POS = 18;
		public static final int TENDTYP8POS = 19;
		public static final int AMTTEND8POS = 20;
		public static final int NEGATIVE = 21;
		public static final int TENDTYP1NEG = 22;
		public static final int AMTTEND1NEG = 23;
		public static final int TENDTYP2NEG = 24;
		public static final int AMTTEND2NEG = 25;
		public static final int TENDTYP3NEG = 26;
		public static final int AMTTEND3NEG = 27;
		public static final int TENDTYP4NEG = 28;
		public static final int AMTTEND4NEG = 29;
		public static final int TENDTYP5NEG = 30;
		public static final int AMTTEND5NEG = 31;
		public static final int TENDTYP6NEG = 32;
		public static final int AMTTEND6NEG = 33;
		public static final int TENDTYP7NEG = 34;
		public static final int AMTTEND7NEG = 35;
		public static final int TENDTYP8NEG = 36;
		public static final int AMTTEND8NEG = 37;

		public static final String LOAN = "0";
		public static final String PICKUP = "1";
		public static final String GASTO_EFE = "2";
		public static final String VALE_EMP = "3";
		public static final String DEV_NC = "4";
	}

	/** Detail of the String Type 20 = EXCEPTION LOG **/
	public static class Type20 {
		/** Fields **/
		public static final int TYPE = 0;
		public static final int LOGDATA = 1;

	}

	/** Detail of the String Type21 = STORE CLOSING **/
	public static class Type21 {
		/** Fields **/
		public static final int TYPE = 0;
		public static final int DATETIME = 1;
		public static final int INDICAT1 = 2;
		public static final int INDICAT2 = 3;

		public static final String STORE_CLOSING = "1";

	}

	/** Detail of the String Type99 = USER DATA **/
	public static class Type99 {
		/** Fields **/
		public static final int TYPE = 1;
		public static final int USRDATA = 2;

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
		public static final String NOTA_CREDITO = "0022"; // no se usa mas
		public static final String COMPROBANTE_EGRESO = "0022";
		public static final String COMPROBANTE_INGRESO = "0023";
		public static final String VENTA_BONO_CONSULTA_CYCA = "0024";
		public static final String ENCUESTA_TABLET = "0025";
		public static final String AUT_SUPERV_VIA_REMOTA = "0026";
		public static final String BENEF_EMPRESARIAL_CONTROLADO = "0027";
		public static final String NOTA_CREDITO_ELEC = "0028"; // nota de credito electronida, vale de papel NO SE USA MAS!!
		public static final String FACTURA_ELEC = "0029";
		public static final String LLAMADO_ASISTENTE = "0030";
		public static final String SUPERV_AUT_CANTIDAD = "0031";
		public static final String TARJETA_CREDITO = "0032"; // pagos con medios electronicos.
		public static final String CONTROL_FUNDA = "0033";
		public static final String TOTALES_TRX = "0034";
		public static final String SEGURO_GARANT_TOTAL_COMPRA_SEG = "0035";
		public static final String CONTRATO_GARANT_TOTAL = "0036";
		public static final String INGRESO_VOUCHER_CERRADO_EFEC = "0037";
		public static final String BASE_DESC_LOYALTY_ERRADA = "0038";
		public static final String PRODUCTO_DIA = "0039";
		public static final String REG_CAMBIO_DATO_FACTURA = "0040";
		public static final String CRED_PROPIO_FACTURACION = "0041";
		public static final String NOTA_CRED_VALE_ELEC = "0045"; // igual a la nota de credito pero el vale de caja es electronico
		public static final String REG_GENERICO_VENDEDOR = "0046";
		public static final String VALE_CAJA_CAMB_EFEC = "0047";
		public static final String REPO_CAJA_CHICA_EFEC= "0048";
		public static final String CASHBACK_ALFALFA_RECURRENCIA = "0083";
		public static final String ACUM_PROMO_ALBUM_VIRTUAL = "0201";
		public static final String DATOS_ADIC_TRX2 = "0203";
		public static final String ACUM_CASHBACK_CABECERA = "0204";
		public static final String ACUM_CASHBACK_DETALLE = "0205";
		public static final String DESC_DIRECTO_PROD = "0206";
		public static final String PROMO_LOYALTY = "189015";
		public static final String DESC_ART_LOYALTY = "189001";
		public static final String DESC_ART_PROMO_LOYALTY = "9021";
		public static final String PROMOTION_DISCOUNT_DATA = "53";
		public static final String DESC_EMP_DATA = "98";
		
		public static class SubType0029 {
			/** Fields **/
			public static final int SUBTIPO = 2;
			public static final int NUM_TRX_CAJA = 3;
			public static final int RUC_CLIENTE = 4;
			public static final int NOMBRE_CLIENTE = 5;
			public static final int CLAVE = 6;
		}
		
		public static class SubType0028 {
			/** Fields **/
			public static final int SUBTIPO = 2;
			public static final int NUM_NOTA = 3;
			public static final int MONTO = 4;
			public static final int NUM_TRX_CAJA = 5;
			public static final int RUC_CLIENTE = 6;
			public static final int NRO_TRX_ORIGINAL = 7;
			public static final int FECHA_TRX_ORIGINAL = 8;
			public static final int NOMBRE_CLIENTE = 9;
			public static final int CLAVE = 10;
		}
		
		public static class SubType0034 {
			/** Fields **/
			public static final int SUBTOTAL1 = 2;
			public static final int DESCUENTOS = 3;
			public static final int SUBTOTAL2 = 4;
			public static final int BASE_TARIFA_0 = 5;
			public static final int BASE_TARIFA_IVA = 6;
			public static final int IVA = 7;
			public static final int TOTAL = 8;
		}
		
		
		public static class SubType98 {
			/** Fields **/
			public static final int TIPO_PROM = 2;
			public static final int COD_PROM = 3;
			public static final int COD_PROM_2 = 4;
			public static final int COD_PROM_3 = 5;
			public static final int EAN = 6;
			public static final int COD_ITEM = 7;
			public static final int PRECIO_MAESTRO = 8;
			public static final int PRECIO_PROM = 9;
			public static final int DESC_TOTAL = 10;
			public static final int SIGNO = 11;
			public static final int CANT_COMB = 12;
			public static final int CANT_PROD = 13;
			public static final int PORC_DESC = 14;
			public static final int LETRA_EMP = 15;
		}

		public static class SubType9021 {
			/** Fields **/
			public static final int BARCODE = 2;
			public static final int REGISTRY_NUMBER = 3;
			public static final int DETAILS = 4;
		}

		public static class SubType53 {
			/** Fields **/
			public static final int PROMO_CODE = 2;
			public static final int ORDINAL_NUMBER = 3;
			public static final int AMOUNT = 4;
			public static final int STATE = 5;
		}
		
		public static class SubType0203 {
			/** Fields **/
			public static final int SUBTYPE = 2;
			public static final int DATA_1 = 3;
			public static final int DATA_2 = 4;
			public static final int DATA_3 = 5;
		}
		
	}

}
