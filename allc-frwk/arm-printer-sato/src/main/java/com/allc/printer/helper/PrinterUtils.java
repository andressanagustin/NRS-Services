package com.allc.printer.helper;

import org.apache.log4j.Logger;

/**
 * @author gustavo
 *
 */
public class PrinterUtils {
	private static Logger log = Logger.getLogger(PrinterUtils.class);

	public static String FONT_M = "M";
	public static String FONT_WB = "WB";
	public static String FONT_WL = "WL";
	// TODO: averiguar valores
	public static int ETQ_X_ROLLO = 3;
	public static int SIZE_DESC = 16;
	public static char ENT = 0x13;
	public static char STX = 0x02;
	public static char ESC = 0x1B;
	public static char ETX = 0x03;

	public String nombreArticulo(String nombre) {
		int i;
		String retorna = "";
		nombre = nombre.trim();
		for (i = 0; i < nombre.length(); i++) {
			if ((nombre.charAt(i) != ',') && (nombre.charAt(i) != '-') && (nombre.charAt(i) != '?') && (nombre.charAt(i) != ':'))
				retorna = retorna + nombre.charAt(i);
		}
		return retorna;
	}

	public int checkDigitoEan(String ean) {
		return (int) (Long.valueOf(ean).longValue() % 10);
	}

	public boolean esAlfabetico(String cadena) {
		// TODO: AVERIGUAR QUE DEBE HACER ESTA FUNCION
		return true;
	}

	public boolean prn_tty_ok(String puerto, int x, char a, char b, char c, char d) {
		// TODO: AVERIGUAR QUE DEBE HACER ESTA FUNCION
		return true;
	}

	public double roundUp(double n, int width) {
		return Math.rint(n*width)/width;
	}

	public String imprimeBarraSato(String articulo, String ean, String descripcion, String tamano, String marca, String referencia,
			String indImpto, String proveedor, String uxc, double precio, int cantEtq, char tipoEtiq, double porcRecargo, double porcIva,
			String modelBarra, boolean flMsjNoAfiliado) {
		String msjNoAfiliado = "", pvpNoAfiliado = "", leyendaIva = "", des1 = "", des2 = "", cadena = "", sPrecio = "", cod = "", cadena2 = "";
		double nValorRecargo;
		double nprecioNoAfi;
		String aTipo = "OB";

		// boolean bPuertoSerial = false;
		// if (printPar.substring(0, 3) != "tty")
		// bPuertoSerial = true;

		int nimpre;
		FONT_M = "M";
		FONT_WB = "WB";
		FONT_WL = "WL";
		if (modelBarra == "RV") {
			FONT_M = "XM";
			FONT_WB = "XB";
			FONT_WL = "XL";
		}
		// ***** INICIALIZA ARREGLO *****
		char sEtq[] = new char[1024];
		for (int I = 0; I < 1024; I++)
			sEtq[I] = ' ';

		descripcion = descripcion.replaceAll("%", "");

		// ***** OPCIONES DE ETIQUETAS *****
		if (tipoEtiq != 'G' && tipoEtiq != 'D' && tipoEtiq != 'N') {
			descripcion = descripcion + ' ' + marca;
			if (tamano.length() == 0)
				descripcion = nombreArticulo(descripcion);
			else {
				descripcion = (descripcion.length() >= 2 * SIZE_DESC ? descripcion.substring(0, 32) : descripcion) + ' ' + tamano.trim();
				descripcion = nombreArticulo(descripcion);
			}
			if (tipoEtiq == 'P' || tipoEtiq == 'M' || tipoEtiq == 'F') // para
																		// estas
																		// etiquetas
																		// la
																		// descripcion
																		// es
																		// mas
																		// pequena
			{
				if (precio >= 1000) {
					des1 = descripcion.substring(0, descripcion.length() >= SIZE_DESC - 4 ? SIZE_DESC - 4 : descripcion.length());
					if (descripcion.length() < 2 * (SIZE_DESC - 4))
						des2 = descripcion.length() < SIZE_DESC - 4 ? "" : descripcion.substring(SIZE_DESC - 4);
					else
						des2 = descripcion.substring(SIZE_DESC - 4, 2 * (SIZE_DESC - 4));
				} else {
					des1 = descripcion.substring(0, descripcion.length() >= SIZE_DESC - 2 ? SIZE_DESC - 2 : descripcion.length());
					if (descripcion.length() < 2 * (SIZE_DESC - 2))
						des2 = descripcion.length() < SIZE_DESC - 2 ? "" : descripcion.substring(SIZE_DESC - 2);
					else
						des2 = descripcion.substring(SIZE_DESC - 2, 2 * (SIZE_DESC - 2));
				}
				if (des2.length() == 1)
					des2 = des2 + ".";
				if (des2.length() == 0)
					des2 = des2 + "";
			} else {
				des1 = descripcion.substring(0, descripcion.length() >= SIZE_DESC ? SIZE_DESC : descripcion.length());
				if (descripcion.length() < 2 * SIZE_DESC)
					des2 = descripcion.length() < SIZE_DESC ? "" : descripcion.substring(SIZE_DESC);
				else
					des2 = descripcion.substring(SIZE_DESC, 2 * SIZE_DESC);

				if (des1.startsWith("l") || des1.startsWith("L"))
					des1 = "." + des1;
				if (des2.startsWith("l") || des2.startsWith("L"))
					des2 = "." + des2;
			}
			ean = Long.valueOf(ean).toString().trim();
			cod = ean + "/" + checkDigitoEan(ean);
			referencia = referencia.trim();
			if (esAlfabetico(referencia))
				referencia = "-" + referencia;

			nValorRecargo = roundUp(precio * porcRecargo, 2);
			nprecioNoAfi = precio + nValorRecargo;
			msjNoAfiliado = "NO AFILIADO US.$ ";
			if (indImpto == "0" || indImpto == "3") {
				nprecioNoAfi = nprecioNoAfi * (1 + (porcIva / 100));
				if (tipoEtiq != 'M' && tipoEtiq != 'F')
					precio = precio * (1 + (porcIva / 100));
			}
			pvpNoAfiliado = "" + Double.valueOf(nprecioNoAfi).toString();

			sPrecio = Double.valueOf(precio).toString();
			if (tipoEtiq != 'P' && tipoEtiq != 'M' && tipoEtiq != 'F') {
				if (sPrecio.length() >= 8)
					sPrecio = "$" + sPrecio;
				else
					sPrecio = "$ " + sPrecio;
			}

			if (indImpto == "0" || indImpto == "3")
				leyendaIva = "INCLUIDO IVA " + Double.valueOf(porcIva) + "%";
			else
				leyendaIva = "EXENTO DE IVA";
			// **** BORRA MENSAJES PARA NO MOSTRAR MENSAJES DE NO AFILIADOS ****
			if (!flMsjNoAfiliado) {
				msjNoAfiliado = "";
				pvpNoAfiliado = "";
			}
		}

		switch (tipoEtiq) {
			case 'I': /*
					 * Etiquetas Individuales, aqui no se imprime precio por eso se envia el parametro del precio en blanco
					 */
				cadena = imprimeIndivSato(ean, cod, " ", des1, des2, referencia, cantEtq);
				break;
			case 'R': /* Etiquetas de Ropa */
				cadena = imprimeRopaSato(ean, cod, referencia, proveedor, des1, des2, msjNoAfiliado, pvpNoAfiliado, sPrecio, leyendaIva,
						tamano, cantEtq);
				break;
			case 'M': /* Etiquetas de Mayorista */
				cadena = imprimePorMayor(ean, articulo, sPrecio, des1, des2, uxc, proveedor, leyendaIva, cantEtq);
				break;
			case 'P': /* Etiquetas de Percha */
				cadena = imprimePerchaSato(ean, articulo, sPrecio, msjNoAfiliado, pvpNoAfiliado, des1, des2, leyendaIva, cantEtq);
				break;
			case 'F': /* Etiquetas de Diferido */
				cadena = imprimeDiferido(ean, articulo, sPrecio, msjNoAfiliado, pvpNoAfiliado, des1, des2, uxc, referencia, proveedor,
						tamano, marca, leyendaIva, cantEtq);
				break;
			case 'V': /* Etiquetas de Cajas */
				cadena = imprimeCajaSato(des1, ean, referencia, cantEtq);
				break;
			case 'H': /* Etiquetas de Habladores */
				// **** RECIEN EMPIEZA EL HABLADOR ***
				cadena = imprimeHabladorSato(des1 + des2, sPrecio, msjNoAfiliado + pvpNoAfiliado, leyendaIva, cantEtq);
				break;
			case 'G': /* Etiquetas Grandes para Perchas */
				cadena = imprimeGrandePercha(descripcion, referencia, cantEtq);
				break;
			case 'D': /* Etiquetas Redondas pequenas para ropa */
				cadena = imprimeRedondaPequena(descripcion, cantEtq);
				break;
			case 'N': /* Etiquetas de Inversas para Cajas */
				cadena = imprimeInversaCaja(descripcion, cantEtq);
				break;
		}
		log.info("cadena: " + cadena);
		cadena = cadena.replace('|', STX).replace(',', ESC).replace('~', ETX);
		log.info("cadena modificada: " + cadena);
		// cadena = sEtq.toString();
		return cadena;
	}


	public String imprimeBarraZebra(String articulo, String ean, String descripcion, String tamano, String marca, String referencia,
			String indImpto, String proveedor, String uxc, double precio, int cantEtq, char tipoEtiq, double porcRecargo, double porcIva,
			String modelBarra, boolean flMsjNoAfiliado) {
		String msjNoAfiliado = "", pvpNoAfiliado = "", leyendaIva = "", des1 = "", des2 = "", cadena = "", sPrecio = "", cod = "", cadena2 = "";
		double nValorRecargo;
		double nprecioNoAfi;
		String aTipo = "OB";

		// boolean bPuertoSerial = false;
		// if (printPar.substring(0, 3) != "tty")
		// bPuertoSerial = true;

		int nimpre;
		FONT_M = "M";
		FONT_WB = "WB";
		FONT_WL = "WL";
		if (modelBarra == "RV") {
			FONT_M = "XM";
			FONT_WB = "XB";
			FONT_WL = "XL";
		}
		// ***** INICIALIZA ARREGLO *****
		char sEtq[] = new char[1024];
		for (int I = 0; I < 1024; I++)
			sEtq[I] = ' ';

		descripcion = descripcion.replaceAll("%", "");

		// ***** OPCIONES DE ETIQUETAS *****
		if (tipoEtiq != 'G' && tipoEtiq != 'F' && tipoEtiq != 'N') {
			descripcion = descripcion + ' ' + marca;
			if (tamano.length() == 0)
				descripcion = nombreArticulo(descripcion);
			else {
				descripcion = (descripcion.length() >= 2 * SIZE_DESC ? descripcion.substring(0, 32) : descripcion) + ' ' + tamano.trim();
				descripcion = nombreArticulo(descripcion);
			}
			if (tipoEtiq == 'P' || tipoEtiq == 'M' || tipoEtiq == 'D') // para
																		// estas
																		// etiquetas
																		// la
																		// descripcion
																		// es
																		// mas
																		// pequena
			{
				if (precio >= 1000) {
					des1 = descripcion.substring(0, descripcion.length() >= SIZE_DESC - 4 ? SIZE_DESC - 4 : descripcion.length());
					if (descripcion.length() < 2 * (SIZE_DESC - 4))
						des2 = descripcion.length() < SIZE_DESC - 4 ? "" : descripcion.substring(SIZE_DESC - 4);
					else
						des2 = descripcion.substring(SIZE_DESC - 4, 2 * (SIZE_DESC - 4));
				} else {
					des1 = descripcion.substring(0, descripcion.length() >= SIZE_DESC - 2 ? SIZE_DESC - 2 : descripcion.length());
					if (descripcion.length() < 2 * (SIZE_DESC - 2))
						des2 = descripcion.length() < SIZE_DESC - 2 ? "" : descripcion.substring(SIZE_DESC - 2);
					else
						des2 = descripcion.substring(SIZE_DESC - 2, 2 * (SIZE_DESC - 2));
				}
				if (des2.length() == 1)
					des2 = des2 + ".";
				if (des2.length() == 0)
					des2 = des2 + "";
			} else {
				des1 = descripcion.substring(0, descripcion.length() >= SIZE_DESC ? SIZE_DESC : descripcion.length());
				if (descripcion.length() < 2 * SIZE_DESC)
					des2 = descripcion.length() < SIZE_DESC ? "" : descripcion.substring(SIZE_DESC);
				else
					des2 = descripcion.substring(SIZE_DESC, 2 * SIZE_DESC);

				if (des1.startsWith("l") || des1.startsWith("L"))
					des1 = "." + des1;
				if (des2.startsWith("l") || des2.startsWith("L"))
					des2 = "." + des2;
			}
			ean = Long.valueOf(ean).toString().trim();
			cod = ean + "/" + checkDigitoEan(ean);
			referencia = referencia.trim();
			if (esAlfabetico(referencia))
				referencia = "-" + referencia;
			log.info("Porc Recargo: "+porcRecargo);
			nValorRecargo = roundUp(precio * porcRecargo/100, 100);
			nprecioNoAfi = precio + nValorRecargo;
			msjNoAfiliado = "NO AFILIADO US.$ ";
			if (indImpto.equals("0") || indImpto.equals("3")) {
				nprecioNoAfi = roundUp(nprecioNoAfi * (1 + (porcIva / 100)), 100);
				log.info("Precio no afil:"+nprecioNoAfi);
//				if (tipoEtiq != 'D')
					precio = precio * (1 + (porcIva / 100));
			}
			pvpNoAfiliado = "" + Double.valueOf(nprecioNoAfi).toString();
			//si solo tenemos parte entera le agregamos los dos decimales
			if(pvpNoAfiliado.length() == 1)
				pvpNoAfiliado = pvpNoAfiliado+".00";
			sPrecio = Double.valueOf(roundUp(precio,100)).toString();
			log.info("Precio:"+sPrecio);
			if (tipoEtiq != 'P' && tipoEtiq != 'M' && tipoEtiq != 'D') {
				if (sPrecio.length() >= 8)
					sPrecio = "$" + sPrecio;
				else
					sPrecio = "$ " + sPrecio;
			}

			if (indImpto.equals("0") || indImpto.equals("3")){
				String iva = Double.valueOf(porcIva).toString();
				leyendaIva = "INCLUIDO IVA " + iva.split("\\.")[0] + "%";
			} else
				leyendaIva = "EXENTO DE IVA";
			// **** BORRA MENSAJES PARA NO MOSTRAR MENSAJES DE NO AFILIADOS ****
			if (!flMsjNoAfiliado) {
				msjNoAfiliado = "";
				pvpNoAfiliado = "";
			}
		}

		switch (tipoEtiq) {
			case 'I': /*
					 * Etiquetas Individuales, aqui no se imprime precio por eso se envia el parametro del precio en blanco
					 */
				cadena = imprimeIndivZebra(ean, cod, " ", des1, des2, referencia, cantEtq);
				break;
			case 'R': /* Etiquetas de Ropa */
				cadena = imprimeRopaZebra(ean, cod, referencia, proveedor, des1, des2, msjNoAfiliado, pvpNoAfiliado, sPrecio, leyendaIva,
						tamano, cantEtq);
				break;
			case 'M': /* Etiquetas de Mayorista */
				cadena = imprimePorMayorZebra(ean, articulo, sPrecio, des1, des2, uxc, proveedor, leyendaIva, cantEtq);
				break;
			case 'P': /* Etiquetas de Percha */
				cadena = imprimePerchaZebra(ean, articulo, sPrecio, msjNoAfiliado, pvpNoAfiliado, des1, des2, leyendaIva, cantEtq);
				break;
			case 'D': /* Etiquetas de Diferido */
				cadena = imprimeDiferidoZebra(ean, articulo, sPrecio, msjNoAfiliado, pvpNoAfiliado, des1, des2, uxc, referencia, proveedor,
						tamano, marca, leyendaIva, cantEtq);
				break;
			case 'V': /* Etiquetas de Cajas */
				cadena = imprimeCajaZebra(des1, ean, referencia, cantEtq);
				break;
			case 'H': /* Etiquetas de Habladores */
				// **** RECIEN EMPIEZA EL HABLADOR ***
				cadena = imprimeHabladorZebra(des1 + des2, sPrecio, msjNoAfiliado + pvpNoAfiliado, leyendaIva, cantEtq);
				break;
			case 'G': /* Etiquetas Grandes para Perchas */
				cadena = imprimeGrandePerchaZebra(descripcion, referencia, cantEtq);
				break;
			case 'F': /* Etiquetas Redondas pequenas para ropa */
				cadena = imprimeRedondaPequenaZebra(descripcion, cantEtq);
				break;
			case 'N': /* Etiquetas de Inversas para Cajas */
				cadena = imprimeInversaCaja(descripcion, cantEtq);
				break;
		}
		log.info("cadena: " + cadena);
		cadena = cadena.replace('|', STX).replace(',', ESC).replace('~', ETX);
		log.info("cadena modificada: " + cadena);
		// cadena = sEtq.toString();
		return cadena;
	}

	/**
	 * Función que imprime etiquetas del tipo individuales, en formato etiquetas mi comisariato de 3 adhesivas.
	 * 
	 * @param ean
	 * @param cod
	 * @param precio
	 * @param des1
	 * @param des2
	 * @param referencia
	 * @param cantEtq
	 * @return
	 */
	public String imprimeIndivSato(String ean, String cod, String precio, String des1, String des2, String referencia, int cantEtq) {
		int residuo;
		String linea = "";
		if (cantEtq > 0) {
			// ---El residuo es desperdicio se imprimen de 3 en 3
			if (cantEtq < ETQ_X_ROLLO)
				cantEtq = ETQ_X_ROLLO;
			residuo = cantEtq % ETQ_X_ROLLO;
			cantEtq = cantEtq / ETQ_X_ROLLO;
			if (cantEtq >= 1) {
				linea = "|,A,%2,H775,V153,B302067" + ean + ",%2,P5,H769,V082,S" + cod + ",%2,H775,V066,P1,OB" + precio
						+ ",%2,H770,V036,P1,S" + des1 + ",%2,H770,V018,P1,S" + des2 + ",%3,H786,V008,P1,S" + referencia
						+ ",%2,H542,V153,B302067" + ean + ",%2,P5,H532,V082,S" + cod + ",%2,H542,V066,P1,OB" + precio
						+ ",%2,H537,V036,P1,S" + des1 + ",%2,H537,V018,P1,S" + des2 + ",%3,H553,V008,P1,S" + referencia
						+ ",%2,H302,V153,B302067" + ean + ",%2,P5,H296,V082,S" + cod + ",%2,H302,V066,P1,OB" + precio
						+ ",%2,H297,V036,P1,S" + des1 + ",%2,H297,V018,P1,S" + des2 + ",%3,H313,V008,P1,S" + referencia + ",Q"
						+ Integer.valueOf(cantEtq) + ",Z~";
			}
			if (residuo == 2) {
				linea = linea + "|,A,%2,H775,V153,B302067" + ean + ",%2,P5,H769,V082,S" + cod + ",%2,H775,V066,P1,OB" + precio
						+ ",%2,H770,V036,P1,S" + des1 + ",%2,H770,V018,P1,S" + des2 + ",%3,H786,V008,P1,S" + referencia
						+ ",%2,H542,V153,B302067" + ean + ",%2,P5,H532,V082,S" + cod + ",%2,H542,V066,P1,OB" + precio
						+ ",%2,H537,V036,P1,S" + des1 + ",%2,H537,V018,P1,S" + des2 + ",%3,H553,V008,P1,S" + referencia + ",Q1,Z~";
			}
			if (residuo == 1) {
				linea = linea + "|,A,%2,H775,V153,B302067" + ean + ",%2,P5,H769,V082,S" + cod + ",%2,H775,V066,P1,OB" + precio
						+ ",%2,H770,V036,P1,S" + des1 + ",%2,H770,V018,P1,S" + des2 + ",%3,H786,V008,P1,S" + referencia + ",Q1,Z~";
			}
		}
		return linea;
	}
	
	/**
	 * Función que imprime etiquetas del tipo individuales, en formato etiquetas mi comisariato de 3 adhesivas.
	 * 
	 * @param ean
	 * @param cod
	 * @param precio
	 * @param des1
	 * @param des2
	 * @param referencia
	 * @param cantEtq
	 * @return
	 */
	public String imprimeIndivZebra(String ean, String cod, String precio, String des1, String des2, String referencia, int cantEtq) {
		int residuo;
		//^XA^FO24,25^A0N,20,20^BY2^BEN,80,Y,N^FD786115589520^FS^FO52,130^A0N,20,14^FDSARDINA EN TOMAT^FS^FO52,155^A0N,20,14^FDE OVAL LA PORTUG^FS^FWB^FO0,40^A0,20,14^FD007601^FS^FO262,25^A0N,20,20^BY2^BEN,80,Y,N^FD786115589520^FS^FO295,130^A0N,20,14^FDSARDINA EN TOMAT^FS^FO295,155^A0N,20,14^FDE OVAL LA PORTUG^FS^FWB^FO236,40^A0,20,14^FD007601^FS^FO500,25^A0N,20,20^BY2^BEN,80,Y,N^FD786115589520^FS^FO536,130^A0N,20,14^FDSARDINA EN TOMAT^FS^FO536,155^A0N,20,14^FDE OVAL LA PORTUG^FS^FWB^FO471,40^A0,20,14^FD007601^FS^PQ2,0,0,N,Y^XZ
		String linea = "";
		if (cantEtq > 0) {
			// ---El residuo es desperdicio se imprimen de 3 en 3
			if (cantEtq < ETQ_X_ROLLO)
				cantEtq = ETQ_X_ROLLO;
			residuo = cantEtq % ETQ_X_ROLLO;
			cantEtq = cantEtq / ETQ_X_ROLLO;
			if (cantEtq >= 1) {
				linea = "^XA^FO24,25^A0N,20,20^BY2^BEN,80,Y,N^FD" + ean 
						//+ "^FS^FO10,80^A0N,14,14^FD" + precio
						+ "^FS^FO52,130^A0N,20,14^FD" + des1 + "^FS^FO52,155^A0N,20,14^FD" + des2 + "^FS^FWB^FO0,40^A0,20,14^FD" + referencia
						+ "^FS^FO262,25^A0N,20,20^BY2^BEN,80,Y,N^FD" + ean 
						//+ "^FS^FO200,80^A0N,14,14^FD" + precio
						+ "^FS^FO295,130^A0N,20,14^FD" + des1 + "^FS^FO295,155^A0N,20,14^FD" + des2 + "^FS^FWB^FO236,40^A0,20,14^FD" + referencia
						+ "^FS^FO500,30^A0N,20,20^BY2^BEN,80,Y,N^FD" + ean 
						//+ "^FS^FO390,80^A0N,14,14^" + precio
						+ "^FS^FO536,130^A0N,20,14^FD" + des1 + "^FS^FO536,155^A0N,20,14^FD" + des2 + "^FS^FWB^FO471,40^A0,20,14^FD" + referencia 
						+ "^FS^PQ" + Integer.valueOf(cantEtq).toString() + ",0,0,N,Y^XZ";
			}
			if (residuo == 2) {
				linea = linea + "^XA^FO24,25^A0N,20,20^BY2^BEN,80,Y,N^FD" + ean 
						//+ "^FS^FO10,80^A0N,14,14^FD" + precio
						+ "^FS^FO52,130^A0N,20,14^FD" + des1 + "^FS^FO52,155^A0N,20,14^FD" + des2 + "^FS^FWB^FO0,40^A0,20,14^FD" + referencia
						+ "^FS^FO262,25^A0N,20,20^BY2^BEN,80,Y,N^FD" + ean 
						//+ "^FS^FO390,80^A0N,14,14^" + precio
						+ "^FS^FO295,130^A0N,20,14^FD" + des1 + "^FS^FO295,155^A0N,20,14^FD" + des2 + "^FS^FWB^FO236,40^A0,20,14^FD" + referencia
						+ "^FS^PQ1,0,0,N,Y^XZ";
			}
			if (residuo == 1) {
				linea = linea + "^XA^FO24,25^A0N,20,20^BY2^BEN,80,Y,N^FD" + ean 
						//+ "^FS^FO10,80^A0N,14,14^FD" + precio
						+ "^FS^FO52,130^A0N,20,14^FD" + des1 + "^FS^FO52,155^A0N,20,14^FD" + des2 + "^FS^FWB^FO0,40^A0,20,14^FD" + referencia
						+ "^FS^PQ1,0,0,N,Y^XZ";
			}
		}
		return linea;
	}

	/**
	 * Función que imprime etiquetas para la ropa para colgar con plastiflecha, en formato etiquetas de cartón mi comisariato troqueladas de
	 * 1.
	 * 
	 * @param ean
	 * @param cod
	 * @param referencia
	 * @param proveedor
	 * @param des1
	 * @param des2
	 * @param noAfiliado
	 * @param precio
	 * @param leyendaIva
	 * @param tamano
	 * @param cantEtq
	 * @return
	 */
	public String imprimeRopaSato(String ean, String cod, String referencia, String proveedor, String des1, String des2, String noAfiliado,
			String pvpNoAfiliado, String precio, String leyendaIva, String tamano, int cantEtq) {
		String linea = "";
		if (cantEtq > 0) {
			int pos = precio.indexOf(".");
			String enteros = precio.substring(0, pos);
			String decimal = "00";
			if (precio.length() > pos + 2)
				decimal = precio.substring(pos + 1, pos + 3);
			else if (precio.length() > pos + 1)
				decimal = precio.substring(pos + 1, pos + 2) + "0";

			int posNoAfi = pvpNoAfiliado.indexOf(".");
			String enterosNoAfi = pvpNoAfiliado.substring(0, posNoAfi);
			String decimalNoAfi = "00";
			if (pvpNoAfiliado.length() > pos + 2)
				decimalNoAfi = pvpNoAfiliado.substring(pos + 1, pos + 3);
			else if (pvpNoAfiliado.length() > pos + 1)
				decimalNoAfi = pvpNoAfiliado.substring(pos + 1, pos + 2) + "0";

			linea = "|,A,%2,H720,V160,B302130" + ean + ",%2,P5,H713,V026,S" + cod + ",%2,P1,H500,V170,S" + proveedor + "-" + referencia
					+ ",%2,H500,V145,S" + des1 + ",%2,H500,V125,S" + des2 + ",%2,H520,V100,S" + noAfiliado + enterosNoAfi + "."
					+ decimalNoAfi + ",%2,H500,V080,OB" + enteros + "." + decimal + ",%2,H500,V048,S" + leyendaIva + ",%2,H510,V030,S"
					+ "TAMAÑO:" + tamano + ",Q" + Integer.valueOf(cantEtq) + ",Z~";
		}
		return linea;
	}
	
	/**
	 * Función que imprime etiquetas para la ropa para colgar con plastiflecha, en formato etiquetas de cartón mi comisariato troqueladas de
	 * 1.
	 * 
	 * @param ean
	 * @param cod
	 * @param referencia
	 * @param proveedor
	 * @param des1
	 * @param des2
	 * @param noAfiliado
	 * @param precio
	 * @param leyendaIva
	 * @param tamano
	 * @param cantEtq
	 * @return
	 */
	public String imprimeRopaZebra(String ean, String cod, String referencia, String proveedor, String des1, String des2, String noAfiliado,
			String pvpNoAfiliado, String precio, String leyendaIva, String tamano, int cantEtq) {
		//^XA^FO20,10^ADN,20,14^BY2^BEN,130,N,N^FD750023110100^FS^FO20,155^ADN,14,14^FD750023110100-0^FS^FO250,20^ADN,14,14^FD06334-REFERENCIA^FS^FO250,40^ADN,14,14^FDESPONJA BADO EXF^FS^FO250,60^ADN,14,14^FDOLIANTE NEXCAR^FS^FO215,85^ADN,12,14^FDNO AFILIADO US.$ 9998.57^FS^FO250,110^ADN,40,15^FD$4.14^FS^FO250,150^ADN,14,14^FDINCLUIDO IVA 19.0%^FS^FO250,170^ADN,14,14^FDTAMAÑO: 2^FS^PQ1,0,0,N,Y^XZ
		String linea = "";
		if (cantEtq > 0) {
			int pos = precio.indexOf(".");
			String enteros = precio.substring(0, pos);
			String decimal = "00";
			if (precio.length() > pos + 2)
				decimal = precio.substring(pos + 1, pos + 3);
			else if (precio.length() > pos + 1)
				decimal = precio.substring(pos + 1, pos + 2) + "0";

			proveedor = proveedor.trim();
			if(proveedor.length() > 6)
				proveedor = proveedor.substring(proveedor.length()-6, proveedor.length());
			
			String leyendaNoAfil = "";
			if(pvpNoAfiliado!=null && !pvpNoAfiliado.isEmpty()){
				int posNoAfi = pvpNoAfiliado.indexOf(".");
				
				String enterosNoAfi = pvpNoAfiliado.substring(0, posNoAfi);
				String decimalNoAfi = "00";
				if (pvpNoAfiliado.length() > posNoAfi + 2)
					decimalNoAfi = pvpNoAfiliado.substring(posNoAfi + 1, posNoAfi + 3);
				else if (pvpNoAfiliado.length() > posNoAfi + 1)
					decimalNoAfi = pvpNoAfiliado.substring(posNoAfi + 1, posNoAfi + 2) + "0";
				leyendaNoAfil = "^FO215,75^A0N,16,20^FD" + noAfiliado + enterosNoAfi + "." + decimalNoAfi + "^FS";
			}

			linea = "^XA^FO20,10^ADN,20,14^BY2^BEN,130,N,N^FD" + ean + "^FS^FO20,155^ADN,14,14^FD"+cod+"^FS^FO250,10^A0N,16,20^FD" + proveedor + referencia.trim()
					+ "^FS^FO250,30^A0N,16,20^FD" + des1 + "^FS^FO250,50^A0N,16,20^FD" + des2 + "^FS"+ leyendaNoAfil+"^FO250,100^A0N,32,45^FD" + enteros + "." + decimal + "^FS^FO250,135^A0N,16,20^FD" + leyendaIva + "^FS^FO250,155^A0N,16,20^FD"
					+ "TAMAÑO: " + tamano.trim() + "^FS^PQ" + Integer.valueOf(cantEtq).toString() + ",0,0,N,Y^XZ";
		}
		return linea;
	}

	/**
	 * Función que imprime etiquetas para percha.
	 * 
	 * @param ean
	 * @param articulo
	 * @param precio
	 * @param msjNoAfiliado
	 * @param pvpNoAfiliado
	 * @param des1
	 * @param des2
	 * @param leyendaIva
	 * @param cantEtq
	 * @return
	 */
	public String imprimePerchaSato(String ean, String articulo, String precio, String msjNoAfiliado, String pvpNoAfiliado, String des1,
			String des2, String leyendaIva, int cantEtq) {
		String linea = "";
		if (cantEtq > 0) {

			linea = "|,A,Q" + Integer.valueOf(cantEtq).toString() + ",%2,H783,V200,L0102," + FONT_M + des1;
			if (des2 != null && !des2.trim().isEmpty())
				linea = linea + ",%2,H783,V150," + FONT_M + des2.trim();

			int pos = precio.indexOf(".");
			String enteros = precio.substring(0, pos);
			String decimal = "00";
			if (precio.length() > pos + 2)
				decimal = precio.substring(pos + 1, pos + 3);
			else if (precio.length() > pos + 1)
				decimal = precio.substring(pos + 1, pos + 2) + "0";

			linea = linea + ",%2,H750,V100,L0101,S" + leyendaIva + ",%2,H750,V080,L0101,S" + ean + "-" + articulo.substring(0, 4)
					+ ",%2,H480,V210,L0101,SAFILIADO" + ",%2,H390,V210,PS,P2,L0102,S" + "US.$,%2,H";

			if (enteros.length() == 1)
				linea = linea + "410";
			else if (enteros.length() == 2)
				linea = linea + "450";
			else if (enteros.length() == 3)
				linea = linea + "500";
			else
				linea = linea + "540";

			int posNoAfi = pvpNoAfiliado.indexOf(".");
			String enterosNoAfi = pvpNoAfiliado.substring(0, posNoAfi);
			String decimalNoAfi = "00";
			if (pvpNoAfiliado.length() > pos + 2)
				decimalNoAfi = pvpNoAfiliado.substring(pos + 1, pos + 3);
			else if (pvpNoAfiliado.length() > pos + 1)
				decimalNoAfi = pvpNoAfiliado.substring(pos + 1, pos + 2) + "0";

			linea = linea + ",V180,PS,L0203," + FONT_WB + "1" + enteros + "." + ",%2,H350,V180,PS,L0102," + FONT_WB + "1" + decimal
					+ ",%2,H480,V050,L0101,S" + msjNoAfiliado + ",%2,H420,V035,PS,P2,L0102,S" + enterosNoAfi + "." + decimalNoAfi + ",Z~";

		}
		return linea;
	}
	
	
	/**
	 * Función que imprime etiquetas para percha.
	 * 
	 * @param ean
	 * @param articulo
	 * @param precio
	 * @param msjNoAfiliado
	 * @param pvpNoAfiliado
	 * @param des1
	 * @param des2
	 * @param leyendaIva
	 * @param cantEtq
	 * @return
	 */
	public String imprimePerchaZebra(String ean, String articulo, String precio, String msjNoAfiliado, String pvpNoAfiliado, String des1,
			String des2, String leyendaIva, int cantEtq) {
		String linea = "";
		if (cantEtq > 0) {
			// ^XA^FWN^FO10,25^A0,50,35^FDESPONJA BANO E^FS^FO10,75^A0,50,35^FDXFOLIANTE NEXC^FS^FO10,125^A0N,20,20^FDINCLUIDO IVA 19.0%^FS^FO10,145^A0N,20,20^FD750102311010-0034^FS^FO340,0^A0N,20,20^FDAFILIADO^FS^FO430,0^A0N,30,20^FDUS.$^FS^FO260,25^A0N,130,90^FD6464.^FS^FO450,25^A0N,100,45^FD14^FS^FO360,133^A0N,20,14^FDNO AFILIADO^FS^FO390,153^A0N,50,25^FDUS.$ 4.14^FS^PQ1,0,0,N,Y^XZ
			linea = "^XA^FWN^FO10,25^A0,55,37^FD" + des1 +"^FS";
			if (des2 != null && !des2.trim().isEmpty())
				linea = linea + "^FO10,75^A0,55,37^FD" + des2.trim() +"^FS";
			int pos = precio.indexOf(".");
			String enteros = precio.substring(0, pos);
			String decimal = "00";
			if (precio.length() > pos + 2)
				decimal = precio.substring(pos + 1, pos + 3);
			else if (precio.length() > pos + 1)
				decimal = precio.substring(pos + 1, pos + 2) + "0";

			linea = linea + "^FO10,125^A0N,18,18^FD" + leyendaIva + "^FS^FO10,145^A0N,18,18^FD" + ean + "-" + articulo.substring(0, 4)
					+ "^FS^FO340,0^A0N,17,17^FD"+"AFILIADO" + "^FS^FO420,0^A0N,30,20^FD" + "US.$";
			linea = linea + "^FS^FO";
			if (enteros.length() == 1)
				linea = linea + "390";
			else if (enteros.length() == 2)
				linea = linea + "350";
			else if (enteros.length() == 3)
				linea = linea + "310";
			else if (enteros.length() == 4)
				linea = linea + "260";
			else
				linea = linea + "250";
			
			String enterosNoAfi = "";
			String decimalNoAfi = "";
			if(!pvpNoAfiliado.trim().isEmpty()){
				int posNoAfi = pvpNoAfiliado.indexOf(".");
				enterosNoAfi = pvpNoAfiliado.substring(0, posNoAfi);
				decimalNoAfi = "00";
				if (pvpNoAfiliado.length() > posNoAfi + 2)
					decimalNoAfi = pvpNoAfiliado.substring(posNoAfi + 1, posNoAfi + 3);
				else if (pvpNoAfiliado.length() > posNoAfi + 1)
					decimalNoAfi = pvpNoAfiliado.substring(posNoAfi + 1, posNoAfi + 2) + "0";
			}
			linea = linea + ",25^A0N,140,90^FD" + enteros + "." + "^FS^FO450,25^A0N,100,45^FD" + decimal
					+ "^FS";
			if(!pvpNoAfiliado.trim().isEmpty())
				linea = linea + "^FO340,140^A0N,17,17^FD" + "NO AFILIADO" + "^FS^FO370,160^A0N,32,17^FDUS.$ " + enterosNoAfi + "." + decimalNoAfi + "^FS";
			linea = linea + "^PQ" + Integer.valueOf(cantEtq).toString() + ",0,0,N,Y^XZ";

		}
		return linea;
	}

	/**
	 * Función que imprime etiquetas para código de barra de supervisor.
	 * 
	 * @param codEan
	 * @return
	 */
	public String imprimeBarraSupZebra(String codEan, String nombre) {
		String linea = "^XA^FO0,50^BY2^BCN,100,N,N,N^FD"+codEan+"^FS^FO0,160^A0N,30,30^FD"
						+ nombre + "^FS^XZ";
		return linea;
	}

	/**
	 * Función que imprime etiquetas para la caja.
	 * 
	 * @param descripcion
	 * @param ean
	 * @param referencia
	 * @param cantEtq
	 * @return
	 */
	public String imprimeCajaSato(String descripcion, String ean, String referencia, int cantEtq) {
		int residuo;
		String linea = "";
		String nombre;
		if (cantEtq > 0) {
			residuo = cantEtq % 2;
			cantEtq = cantEtq / 2;
			ean = "1" + ean + Integer.valueOf(checkDigitoEan(ean));
			nombre = nombreArticulo(descripcion + " " + referencia);
			String cDot = "02";
			String cHeight = "110";
			// **** IMPRIME DE DOS EN DOS ******
			if (cantEtq > 0) {
				linea = "|,A,H030,V5,FW0707H335V120" + ",H065,V10,B2" + cDot + cHeight + ean + ",H065,V125,M" + ean + ",H065,V147,M"
						+ nombre + ",H440,V5,FW0707H335V120" + ",H470,V10,B2" + cDot + cHeight + ean + ",H470,V125,M" + ean
						+ ",H470,V147,M" + nombre + ",Q" + Integer.valueOf(cantEtq).toString() + ",Z~";
			}
			if (residuo > 0) {// ***** SI HUBO RESIDUO IMPRIME UNA LINEA MAS
								// *****
				linea = linea + "|,A,H030,V5,FW0707H335V120" + ",H065,V10,B2" + cDot + cHeight + ean + ",H065,V125,M" + ean
						+ ",H065,V147,M" + nombre + ",Q1,Z~";
			}
		}
		return linea;
	}
	
	/**
	 * Función que imprime etiquetas para la caja.
	 * 
	 * @param descripcion
	 * @param ean
	 * @param referencia
	 * @param cantEtq
	 * @return
	 */
	public String imprimeCajaZebra(String descripcion, String ean, String referencia, int cantEtq) {
		//^XA^FO10,20^A0N,30,30^BY2^BCN,80,Y,N,N^FD110009497001050^FS^FO10,130^A0N,30,30^FDCERVEZA 330CC CL 8689^FS^FO450,20^A0N,30,30^BY2^BCN,80,Y,N,N^FD110009497001050^FS^FO450,130^A0N,30,30^FDCERVEZA 330CC CL 8689^FS^PQ1,0,0,N,Y^XZ
		int residuo;
		String linea = "";
		String nombre;
		if (cantEtq > 0) {
			residuo = cantEtq % 2;
			cantEtq = cantEtq / 2;
			ean = "1" + ean + Integer.valueOf(checkDigitoEan(ean));
			nombre = nombreArticulo(descripcion + " " + referencia);
			// **** IMPRIME DE DOS EN DOS ******
			if (cantEtq > 0) {
				linea = "^XA^FO40,20^A0N,30,30^BY2^BCN,80,Y,N,N^FD" + ean + "^FS^FO40,130^A0N,30,30^FD"
						+ nombre + "^FS^FO480,20^A0N,30,30^BY2^BCN,80,Y,N,N^FD" + ean + "^FS^FO480,130^A0N,30,30^FD" 
						+ nombre + "^FS^PQ" + Integer.valueOf(cantEtq).toString() + ",0,0,N,Y^XZ";
			}
			if (residuo > 0) {// ***** SI HUBO RESIDUO IMPRIME UNA LINEA MAS
								// *****
				linea = linea + "^XA^FO40,20^A0N,30,30^BY2^BCN,80,Y,N,N^FD" + ean + "^FS^FO40,130^A0N,30,30^FD" + nombre + "^FS^PQ1,0,0,N,Y^XZ";
				
			}
		}
		return linea;
	}

	/**
	 * 
	 * @param descripcion
	 * @param precio
	 * @param precNoAfil
	 * @param leyendaIva
	 * @param cantEtq
	 * @return
	 */
	public String imprimeHabladorSato(String descripcion, String precio, String precNoAfil, String leyendaIva, int cantEtq) {
		int numSpa;
		String titulo1, titulo2, titulo3, titulo4;
		String linea = "";
		String spaces, raya;
		if (cantEtq > 0) {
			descripcion = descripcion + "                              ";
			titulo1 = descripcion.substring(0, 30);
			titulo2 = precio.replace("0", "O");
			numSpa = (10 - titulo2.length()) / 2;
			spaces = new String();
			for (int i = 0; i < numSpa; i++)
				spaces += ' ';
			raya = new String();
			for (int i = 0; i < 82; i++)
				raya += '-';
			titulo2 = spaces + titulo2;
			titulo3 = leyendaIva;
			titulo4 = precNoAfil;
			linea = "|,A" + ",%1,H192,V1730,L0101," + FONT_M + "+" + raya + ",%1,H400,V1730,L0101," + FONT_M + "l"
					+ ",%1,H600,V1730,L0101," + FONT_M + "l" + ",%1,H750,V1730,L0101," + FONT_M + "l" + ",%1,H250,V1700,L0404," + FONT_M
					+ titulo1 + ",%1,H450,V1660,L0606,WB1" + titulo2 + ",%1,H550,V550,L0202," + FONT_M + titulo3 + ",%1,H720,V1590,L0202,"
					+ FONT_M + titulo4 + ",Q" + Integer.valueOf(cantEtq).toString().trim() + ",Z~";
		}
		return linea;
	}
	
	/**
	 * 
	 * @param descripcion
	 * @param precio
	 * @param precNoAfil
	 * @param leyendaIva	
	 * @param cantEtq
	 * @return
	 */
	public String imprimeHabladorZebra(String descripcion, String precio, String precNoAfil, String leyendaIva, int cantEtq) {
		//^XA^LL1800^FWR^FO620,0^A0,50,60^FDl^FS^FO420,0^A0,50,60^FDl^FS^FO200,0^A0,50,60^FDl^FS^FO35,0^A0,50,60^FDl^FS^FO625,0^FD________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________^FS^FO440,30^A0,120,140^FDPANTALON GABARDINA S/PIN^FS^FO160,70^A0,210,240^FD$ 99910.99^FS^FO220,1190^A0,60,80^FDINCLUIDO IVA 12%^FS^FO50,40^A0,60,80^FDNO AFILIADO US.$ 11.76^FS^PQ1,0,0,N,Y^XZ
		int numSpa;
		String titulo1, titulo2, titulo3, titulo4;
		String linea = "";
		String spaces, raya;
		if (cantEtq > 0) {
			descripcion = descripcion + "                              ";
			titulo1 = descripcion.substring(0, 30);
			titulo2 = precio.replace("0", "O");
			numSpa = (10 - titulo2.length()) / 2;
			spaces = new String();
			for (int i = 0; i < numSpa; i++)
				spaces += ' ';
			raya = new String();
			for (int i = 0; i < 320; i++)
				raya += '_';
			titulo2 = spaces + titulo2;
			titulo3 = leyendaIva;
			titulo4 = precNoAfil;
			linea = "^XA^LL1800^FWR^FO620,0^A0,50,60^FDl^FS^FO420,0^A0,50,60^FDl^FS^FO200,0^A0,50,60^FDl^FS^FO35,0^A0,50,60^FDl^FS^FO625,0^FD" + raya 
					+ "^FS^FO440,30^A0,120,140^FD" + titulo1 + "^FS^FO160,70^A0,210,240^FD" + titulo2 
					+ "^FS^FO220,1190^A0,60,80^FD" + titulo3 + "^FS^FO50,40^A0,60,80^FD"
					+ titulo4 + "^FS^PQ" + Integer.valueOf(cantEtq).toString().trim() + ",0,0,N,Y^XZ";
		}
		return linea;
	}

	/**
	 * @param tipoTec
	 * @param puerto
	 * @param fecha
	 * @param medida
	 * @param pesoUni
	 * @param leyeIva
	 * @param totalAfi
	 * @param totalNoAfi
	 * @param codBarra
	 * @param descripcion
	 * @param tipoEtiq
	 * @param platos
	 * @return
	 */
	public boolean imprimeBarraBalanza(String tipoTec, String puerto, String fecha, String medida, String pesoUni, String leyeIva,
			String totalAfi, String totalNoAfi, String codBarra, String descripcion, char tipoEtiq, boolean platos) {
		String cadena = null, temp;
		boolean funcion = false;
		switch (tipoEtiq) {
			case 'A':
				cadena = imprimeActualBalanza(tipoTec, fecha, medida, pesoUni, leyeIva, totalAfi, totalNoAfi, codBarra, descripcion, platos);
				break;
		}
		if (cadena.length() > 0 && prn_tty_ok(puerto, 9600, 'n', '8', '1', 'N')) {
			// TODO: TERMINAR CON SERIE
		}
		return funcion;
	}

	/**
	 * @param tipoTec
	 * @param fecha
	 * @param medida
	 * @param pesoUni
	 * @param leyeIva
	 * @param totalAfi
	 * @param totalNoAfi
	 * @param codBarra
	 * @param descripcion
	 * @param platos
	 * @return
	 */
	public String imprimeActualBalanza(String tipoTec, String fecha, String medida, String pesoUni, String leyeIva, String totalAfi,
			String totalNoAfi, String codBarra, String descripcion, boolean platos) {
		String linea, totales;
		char etq[] = new char[1024];
		for (int i = 0; i < 1024; i++)
			etq[i] = ' ';
		totalAfi = "  $ " + totalAfi;

		if (totalNoAfi.equals(""))
			totales = "";
		else {
			totales = "  Afiliado     No Afiliado";
			totalNoAfi = "$ " + totalNoAfi;
		}

		if (tipoTec.equals("TEC BSV4D"))
			linea = "{C|}+" + "{PC001;0040,0015,06,06,O,00,B|}+" + // CANT
					"{PC002;0040,0040,06,06,O,00,B|}+" + // B Afiliado
					"{PC003;0040,0065,06,06,O,00,B|}+" + // $ 6.31
					"{PC004;0245,0065,06,06,O,00,B|}+" + // $ 6.53
					"{PC005;0060,0235,06,06,O,33,B|}+" + // Fecha estaba en 215
					"{PC006;0098,0190,06,06,O,00,B|}+" + // 10303067
															// (0030000078)
					"{PC007;0065,0215,06,06,O,00,B|}+" + // MANZANA STARKING
					"{XB01;0070,0070,2,1,02,01,04,04,00,0,0100=" + codBarra.substring(0, 20) + "|}+" + "{RC001;" + medida + pesoUni
					+ leyeIva + "|}+" + "{RC002;" + totales + "|}+" + "{RC003;" + totalAfi + "|}+" + "{RC004;" + totalNoAfi + "|}+"
					+ "{RC005;" + fecha + "|}+" + "{RC006;" + codBarra.substring(codBarra.length() - 22, codBarra.length()) + "|}+"
					+ "{RC007;" + descripcion + "|}+" + "{XS;I,0001,0001C5000|}";
		else
			linea = "{U2;0080|}+" + "{PC02;0600,0132,1,1,A,22|}+" + // Kilos: 8.888 incluido IVA 12%
					"{PC03;0600,0102,1,1,A,22|}+" + // $ 6.31
					"{PC08;0580,0086,1,1,A,22|}+" + "{PC09;0470,0102,1,1,A,22|}+" + // $ 6.75
					"{PC10;0470,0086,1,1,A,22|}+" + "{PC11;0595,0118,1,1,A,22|}+" + // Afiliado No Afiliado
					"{PC04;0560,0022,1,1,A,22|}+" + // 10000847 (0001000004)
					"{PC05;0578,0005,1,1,A,22|}+" + // descripcion centrada en
													// 26 caraceters
					"{PB06;0580,0042,132,061,2,0,0|}+" + // ***BARRA***
					"{PC01;0585,0005,1,1,A,11|}+" + // 2004/07/13 estaba en 15
					"{PC07;0625,0005,1,1,A,22|}+" + "{rC02;" + medida + pesoUni + leyeIva + "|}+" + "{rC03;" + totalAfi + "|}+" + "{rC09;"
					+ totalNoAfi + "|}+" + "{rC10;|}+" + "{rC11;" + totales + "|}+" + "{rC04;"
					+ codBarra.substring(codBarra.length() - 22, codBarra.length()) + "|}+" + "{rC05;" + descripcion + "|}+" + "{rB06;"
					+ codBarra.substring(0, 20) + "|}+" + "{rC01;" + fecha + "|}+" + "{R|}+" + "{i1001C|}+" + "{U1;0080|}";

		for (int i = 0; i < linea.length(); i++) {
			switch (linea.charAt(i)) {
				case '+':
					etq[i - 1] = ENT;
					break;
				default:
					etq[i - 1] = linea.charAt(i);
					break;
			}
		}
		return new String(etq);
	}

	/**
	 * @param puerto
	 * @return
	 */
	public boolean configuraTecBsv4d(String puerto) {
		String cadena, temp;
		char etq[] = new char[1024];
		boolean blnFuncion = false;
		for (int i = 0; i < 1024; i++)
			etq[i] = ' ';
		cadena = "{D0250,0430,0250|}+{T10C30|}";
		for (int i = 0; i < cadena.length(); i++) {
			switch (cadena.charAt(i)) {
				case '+':
					etq[i - 1] = ENT;
					break;
				default:
					etq[i - 1] = cadena.charAt(i);
					break;
			}
		}
		cadena = new String(etq);
		if (cadena.length() > 0 && prn_tty_ok(puerto, 9600, 'n', '8', '1', 'N')) {
			// TODO: TERMINAR CON SERIE
		}
		return blnFuncion;
	}

	/**
	 * Función que imprime etiquetas grandes de percha.
	 * 
	 * @param linea1
	 * @param linea2
	 * @param cantEtq
	 * @return
	 */
	public String imprimeGrandePercha(String linea1, String linea2, int cantEtq) {
		String linea = "";
		if (cantEtq > 0) {
			if (linea2 == "") {
				linea = "|,A,Q" + Integer.valueOf(cantEtq).toString().trim() + ",%2,H760,V150,L0203," + FONT_WB + "1" + linea1 + ",Z~";
			} else {
				linea = "|,A,Q" + Integer.valueOf(cantEtq).toString().trim() + ",%2,H760,V130,L0200," + FONT_WB + "1" + linea1
						+ ",%2,H760,V087,L0100," + FONT_WB + "1" + linea2 + ",Z~";
			}
		}
		return linea;
	}
	
	/**
	 * Función que imprime etiquetas grandes de percha.
	 * 
	 * @param linea1
	 * @param linea2
	 * @param cantEtq
	 * @return
	 */
	public String imprimeGrandePerchaZebra(String linea1, String linea2, int cantEtq) {
		String linea = "";
		//^XA^FO20,30^ADN,30,11^FDPANTALON GABARDINA S/PIN^FS^FO20,80^ADN,30,11^FD8689^FS^PQ1,0,0,N,Y^XZ
		if (cantEtq > 0) {
			if (linea2 == "") {
				linea = "^XA^FO20,30^ADN,30,11^FD" + linea1 + "^FS^PQ" + Integer.valueOf(cantEtq).toString() + ",0,0,N,Y^XZ";
			} else {
				linea = "^XA^FO20,30^ADN,30,11^FD" + linea1 + "^FS^FO20,80^ADN,30,11^FD" + linea2 + "^FS^PQ" + Integer.valueOf(cantEtq).toString() + ",0,0,N,Y^XZ";
			}
		}
		return linea;
	}

	/**
	 * Función que imprime etiquetas redondas pequeñas.
	 * 
	 * @param liena1
	 * @param cantEtq
	 * @return
	 */
	public String imprimeRedondaPequena(String linea1, int cantEtq) {
		int residuo, distancia = 0;
		String linea = "";
		if (cantEtq > 0) {
			residuo = cantEtq % 5;
			if (residuo != 0) {
				residuo = 5 - residuo;
				cantEtq = cantEtq + residuo;
			}
			cantEtq = cantEtq / 5;
			if (linea1.length() == 1)
				distancia = -10;
			if (linea1.length() == 2)
				distancia = 0;
			if (linea1.length() == 3)
				distancia = 15;
			if (linea1.length() == 4)
				distancia = 25;

			linea = "|,A,Q" + Integer.valueOf(cantEtq).toString().trim() + ",%2,H" + Integer.valueOf(760 + distancia) + ",V090,L0101,"
					+ FONT_WB + "1" + linea1 + ",%2,H" + Integer.valueOf(625 + distancia) + ",V090,L0101," + FONT_WB + "1" + linea1
					+ ",%2,H" + Integer.valueOf(490 + distancia) + ",V090,L0101," + FONT_WB + "1" + linea1 + ",%2,H"
					+ Integer.valueOf(355 + distancia) + ",V090,L0101," + FONT_WB + "1" + linea1 + ",%2,H"
					+ Integer.valueOf(220 + distancia) + ",V090,L0101," + FONT_WB + "1" + linea1 + ",Z~";
		}
		return linea;
	}
	
	/**
	 * Función que imprime etiquetas redondas pequeñas.
	 * 
	 * @param liena1
	 * @param cantEtq
	 * @return
	 */
	public String imprimeRedondaPequenaZebra(String linea1, int cantEtq) {
		int residuo = 0;
		String linea = "";
		if (cantEtq > 0) {
			residuo = cantEtq % 5;
			if (residuo != 0) {
				residuo = 5 - residuo;
				cantEtq = cantEtq + residuo;
			}
			cantEtq = cantEtq / 5;
			//^XA^FO10,50^ADN,30,18^FDABC^FS^FO100,50^ADN,30,18^FDABC^FS^FO190,50^ADN,30,18^FDABC^FS^FO280,50^ADN,30,18^FDABC^FS^FO370,50^ADN,30,18^FDABC^FS^PQ1,0,0,N,Y^XZ
			linea = "^XA^FO10,50^ADN,30,18^FD" + linea1 + "^FS^FO100,50^ADN,30,18^FD" 
					+ linea1 + "^FS^FO190,50^ADN,30,18^FD" 
					+ linea1 + "^FS^FO280,50^ADN,30,18^FD" 
					+ linea1 + "^FS^FO370,50^ADN,30,18^FD" 
					+ linea1 + "^FS^PQ" + Integer.valueOf(cantEtq).toString() + ",0,0,N,Y^XZ";

		}
		return linea;
	}

	/**
	 * 
	 * @param linea1
	 * @param cantEtq
	 * @return
	 */
	public String imprimeInversaCaja(String linea1, int cantEtq) {
		String puntos = "..................................................";
		String linea = "";
		if (cantEtq > 0) {
			linea = "|,A,Q" + Integer.valueOf(cantEtq).toString().trim() + ",%1,H700,V220,L0102," + FONT_WB + "1" + linea1
					+ ",%1,H600,V220,L0101,U" + puntos + ",Z~";
		}
		return (linea);
	}

	public boolean imprimePanaderia(String puerto, String ean, String descripcion, String tamano, String marca, String referencia,
			int cantidad, int acumulado, String tipoTec) {
		String linea, totales, des1, des2, cadena, temp, cod;
		boolean retorna = false;
		char etq[] = new char[1024];
		for (int i = 0; i < 1024; i++)
			etq[i] = ' ';
		descripcion = descripcion + ' ' + marca;
		if (tamano.length() == 0)
			descripcion = nombreArticulo(descripcion);
		else {
			descripcion = nombreArticulo(descripcion.substring(0, 32) + ' ' + tamano.trim());
		}
		des1 = descripcion.substring(0, SIZE_DESC);
		des2 = descripcion.substring(SIZE_DESC, SIZE_DESC + SIZE_DESC);
		ean = ean.trim();
		cod = ean + "/" + Integer.valueOf(checkDigitoEan(ean));
		referencia = referencia.trim();
		if (esAlfabetico(referencia))
			referencia = "-" + referencia;

		if (tipoTec == "TEC BSV4D")
			linea = "{C|}" + "{PC001;0040,0015,06,06,O,00,B|}+" + "{PC002;0040,0040,06,06,O,00,B|}+" + "{PC003;0040,0065,06,06,O,00,B|}+"
					+ "{PC004;0245,0065,06,06,O,00,B|}+" + "{XB01;0070,0070,2,1,02,01,04,04,00,0,0100=" + ean.substring(0, 20) + "|}+"
					+ "{RC001;" + cod + "|}+" + "{RC002;" + des1 + "|}+" + "{RC003;" + des2 + "|}+" + "{RC004;" + referencia + "|}+"
					+ "{XS;I,0001,0001C5000|}";
		else
			linea = "{U2;0080|}+" + "{PC02;0570,0045,1,1,A,22|}+" + "{PC03;0570,0025,1,1,A,22|}+" + "{PC04;0570,0010,1,1,A,22|}+"
					+ "{PB05;0578,0065,132,061,2,0,0|}+" + "{PC01;0585,0015,1,1,A,11|}+" + "{PC07;0630,0005,1,1,A,22|}+" + "{rC02;" + cod
					+ "|}+" + "{rC03;" + des1 + "|}+" + "{rC04;" + des2 + "|}+" + "{rB05;" + ean + "|}+" + "{rC01;" + referencia + "|}+"
					+ "{R|}+" + "{i1001C|}+" + "{U1;0080|}";

		for (int i = 0; i < linea.length(); i++) {
			switch (linea.charAt(i)) {
				case '+':
					etq[i - 1] = ENT;
					break;

				default:
					etq[i - 1] = linea.charAt(i);
					break;
			}
		}
		cadena = new String(etq);

		for (int i = 0; i < cantidad; i++) {
			if (cadena.length() > 0 && prn_tty_ok(puerto, 9600, 'n', '8', '1', 'N')) {
				acumulado = acumulado + 1;
				// TODO: TERMINAR CON SERIE
			}
		}
		return retorna;
	}

	public void imprimeRegistroSanitario(String desPro, String regSan, int cantEtq, String printPar) {
		int residuo;
		String cadena = "";
		int impre;
		char etq[] = new char[1024];
		for (int i = 0; i < 1024; i++) {
			etq[i] = ' ';
			if (cantEtq > 0) {
				if (cantEtq < ETQ_X_ROLLO)
					cantEtq = ETQ_X_ROLLO;
				residuo = cantEtq % ETQ_X_ROLLO;
				cantEtq = cantEtq / ETQ_X_ROLLO;
				cadena = "|,A" + ",%2,H886,V136,P2,SImp.El Rosado C.Ltda." + ",%2,H886,V106,P2,S" + desPro
						+ ",%2,H886,V076,P3,SReg.Sanitario:" + ",%2,H886,V046,P3,S" + regSan + ",%2,H649,V136,P2,SImp.El Rosado C.Ltda."
						+ ",%2,H649,V106,P2,S" + desPro + ",%2,H649,V076,P3,SReg.Sanitario:" + ",%2,H649,V046,P3,S" + regSan
						+ ",%2,H409,V136,P2,SImp.El Rosado C.Ltda." + ",%2,H409,V106,P2,S" + desPro + ",%2,H409,V076,P3,SReg.Sanitario:"
						+ ",%2,H409,V046,P3,S" + regSan + ",Q" + Integer.valueOf(cantEtq) + ",Z~";

				if (residuo == 2) {
					cadena = cadena + "|,A" + ",%2,H886,V136,P2,SImp.El Rosado C.Ltda." + ",%2,H886,V106,P2,S" + desPro
							+ ",%2,H886,V076,P3,SReg.Sanitario:" + ",%2,H886,V046,P3,S" + regSan
							+ ",%2,H649,V136,P2,SImp.El Rosado C.Ltda." + ",%2,H649,V106,P2,S" + desPro
							+ ",%2,H649,V076,P3,SReg.Sanitario:" + ",%2,H649,V046,P3,S" + regSan + ",Q1,Z~";
				}

				if (residuo == 1) {
					cadena = cadena + "|,A" + ",%2,H886,V136,P2,SImp.El Rosado C.Ltda." + ",%2,H886,V106,P2,S" + desPro
							+ ",%2,H886,V076,P3,SReg.Sanitario:" + ",%2,H886,V046,P3,S" + regSan + ",Q1,Z~";
				}
			}
			// TODO: TERMINAR CON SERIE
		}
	}

	/**
	 * Función que imprime etiquetas para ventas al por mayor, en formato etiquetas de cartón mi comisariato troqueladas de 1.
	 * 
	 * @param ean
	 * @param articulo
	 * @param precio
	 * @param des1
	 * @param des2
	 * @param uxc
	 * @param proveedor
	 * @param leyendaIva
	 * @param cantEtq
	 * @return
	 */
	public String imprimePorMayor(String ean, String articulo, String precio, String des1, String des2, String uxc, String proveedor,
			String leyendaIva, int cantEtq) {
		String linea = "";
		if (cantEtq > 0) {
			linea = "|,A,Q" + Integer.valueOf(cantEtq) + ",%2,H783,V200,L0102," + FONT_M + des1;
			if (des2 != null && !des2.equals(""))
				linea = linea + ",%2,H783,V150," + FONT_M + des2.trim();

			int pos = precio.indexOf(".");
			String enteros = precio.substring(0, pos);
			String decimal = "00";
			if (precio.length() > pos + 2)
				decimal = precio.substring(pos + 1, pos + 3);
			else if (precio.length() > pos + 1)
				decimal = precio.substring(pos + 1, pos + 2) + "0";

			linea = linea + ",%2,H750,V100,L0101,S" + leyendaIva + ",%2,H750,V080,L0101,S" + ean + "-" + articulo.substring(0, 4)
					+ ",%2,H760,V050,L0102,S" + "AL POR MAYOR (" + (proveedor.equalsIgnoreCase("C") ? "CAJA)" : "UNID)")
					+ ",%2,H480,V210,L0101,SAFILIADO" + ",%2,H390,V210,PS,P2,L0102,S" + "US.$" + ",%2,H";

			if (enteros.length() == 1)
				linea = linea + "410";
			else if (enteros.length() == 2)
				linea = linea + "450";
			else if (enteros.length() == 3)
				linea = linea + "500";
			else
				linea = linea + "540";

			linea = linea + ",V180,PS,L0203," + FONT_WB + "1" + enteros + "." + ",%2,H350,V180,PS,L0102," + FONT_WB + "1" + decimal
					+ ",%2,H530,V050,L0101,S" + "UNIDADES X CAJA: " + uxc + ",Z~";
		}
		return linea;
	}
	

	/**
	 * Función que imprime etiquetas para ventas al por mayor, en formato etiquetas de cartón mi comisariato troqueladas de 1.
	 * 
	 * @param ean
	 * @param articulo
	 * @param precio
	 * @param des1
	 * @param des2
	 * @param uxc
	 * @param proveedor
	 * @param leyendaIva
	 * @param cantEtq
	 * @return
	 */
	public String imprimePorMayorZebra(String ean, String articulo, String precio, String des1, String des2, String uxc, String proveedor,
			String leyendaIva, int cantEtq) {
		// ^XA^FWN^FO10,05^A0,50,34^FDCERVEZA 330CC^FS^FO10,55^A0,50,34^FDCLUB CLUB 330CC^FS^FO10,105^A0N,20,20^FDINCLUIDO IVA 19.0%^FS^FO10,125^A0N,20,20^FD750102311010-0034^FS^FO25,160^A0N,30,18^FDAL POR MAYOR (UNID)^FS^FO340,0^A0N,20,20^FDAFILIADO^FS^FO430,0^A0N,30,20^FDUS.$^FS^FO400,35^A0N,130,85^FD4.^FS^FO450,35^A0N,100,45^FD14^FS^FO330,160^A0N,30,18^FDUNIDADES X CAJA: 24^FS^PQ1,0,0,N,Y^XZ
		String linea = "";
		if (cantEtq > 0) {
			linea = "^XA^FWN^FO10,05^A0,50,34^FD" + des1;
			if (des2 != null && !des2.equals(""))
				linea = linea + "^FS^FO10,55^A0,50,34^FD" + des2.trim();

			int pos = precio.indexOf(".");
			String enteros = precio.substring(0, pos);
			String decimal = "00";
			if (precio.length() > pos + 2)
				decimal = precio.substring(pos + 1, pos + 3);
			else if (precio.length() > pos + 1)
				decimal = precio.substring(pos + 1, pos + 2) + "0";

			linea = linea + "^FS^FO20,105^A0N,20,20^FD" + leyendaIva + "^FS^FO20,125^A0N,20,20^FD" + ean + "-" + articulo.trim().substring(articulo.length()-4, articulo.length())
					+ "^FS^FO25,160^A0N,30,18^FD" + "AL POR MAYOR (" + (proveedor.trim().equalsIgnoreCase("MC") ? "CAJA)" : "UNID)")
					+ "^FS^FO340,0^A0N,20,20^FDAFILIADO" + "^FS^FO430,0^A0N,30,20^FD" + "US.$" + "^FS^FO";

			if (enteros.length() == 1)
				linea = linea + "375";
			else if (enteros.length() == 2)
				linea = linea + "355";
			else if (enteros.length() == 3)
				linea = linea + "310";
			else if (enteros.length() == 4)
				linea = linea + "265";
			else
				linea = linea + "250";

			linea = linea + ",32^A0N,142,85^FD" + enteros + "." + "^FS^FO450,35^A0N,100,45^FD" + decimal
					+ "^FS^FO300,160^A0N,30,18^FD" + "UNIDADES X CAJA: " + Integer.valueOf(uxc) + "^FS^PQ"+ Integer.valueOf(cantEtq) +",0,0,N,Y^XZ";
		}
		return linea;
	}

	/**
	 * Función que imprime etiquetas con precios de venta diferidos a 3,6,9 meses, en formato etiquetas de cartón mi comisariato troqueladas
	 * de 1.
	 * 
	 * @param ean
	 * @param articulo
	 * @param precio
	 * @param mjeNoAfiliado
	 * @param pvpNoAfiliado
	 * @param des1
	 * @param des2
	 * @param uxc
	 * @param referencia
	 * @param proveedor
	 * @param tamano
	 * @param marca
	 * @param leyendaIva
	 * @param cantEtq
	 * @return
	 */
	public String imprimeDiferido(String ean, String articulo, String precio, String mjeNoAfiliado, String pvpNoAfiliado, String des1,
			String des2, String uxc, String referencia, String proveedor, String tamano, String marca, String leyendaIva, int cantEtq) {
		String linea = "";
		if (cantEtq > 0) {
			linea = "|,A,Q" + Integer.valueOf(cantEtq) + ",%2,H783,V200,L0102," + FONT_M + des1;
			if (des2 != null && !des2.equals(""))
				linea = linea + ",%2,H783,V150," + FONT_M + des2.trim();
			Float precioTemp = Float.valueOf(precio);
			Integer proveedorTemp = Integer.valueOf(proveedor);
			Float precioCuotaTemp = precioTemp / proveedorTemp;
			String precioCuota = precioCuotaTemp.toString();
			int pos = precioCuota.indexOf(".");
			String enteros = precioCuota.substring(0, pos);
			String decimal = "00";
			if (precio.length() > pos + 2)
				decimal = precio.substring(pos + 1, pos + 3);
			else if (precio.length() > pos + 1)
				decimal = precio.substring(pos + 1, pos + 2) + "0";

			linea = linea + ",%2,H750,V100,L0101,S" + leyendaIva + ",%2,H750,V080,L0101,S" + ean + "-" + articulo.substring(0, 4)
					+ ",%2,H480,V210,L0101,S" + proveedor + " CUOTAS DE" + ",%2,H350,V210,PS,PS,L0102,S" + "US.$" + ",%2,H";

			if (enteros.length() == 1)
				linea = linea + "410";
			else if (enteros.length() == 2)
				linea = linea + "450";
			else if (enteros.length() == 3)
				linea = linea + "500";
			else
				linea = linea + "540";

			linea = linea + ",V180,PS,L0203," + FONT_WB + "1" + enteros + "." + ",%2,H350,V180,PS,L0102," + FONT_WB + "1" + decimal
					+ ",%2,H480,V050,L0101,S" + "PRECIO TOTAL" + ",%2,H450,V030,PS,P2,L0102,SUS.$ " + precio + ",Z~";
		}
		return linea;
	}
	
	/**
	 * Función que imprime etiquetas con precios de venta diferidos a 3,6,9 meses, en formato etiquetas de cartón mi comisariato troqueladas
	 * de 1.
	 * 
	 * @param ean
	 * @param articulo
	 * @param precio
	 * @param mjeNoAfiliado
	 * @param pvpNoAfiliado
	 * @param des1
	 * @param des2
	 * @param uxc
	 * @param referencia
	 * @param proveedor
	 * @param tamano
	 * @param marca
	 * @param leyendaIva
	 * @param cantEtq
	 * @return
	 */
	public String imprimeDiferidoZebra(String ean, String articulo, String precio, String mjeNoAfiliado, String pvpNoAfiliado, String des1,
			String des2, String uxc, String referencia, String proveedor, String tamano, String marca, String leyendaIva, int cantEtq) {
		String linea = "";
		//^XA^FO20,20^A0N,55,30^FDMESA PROVENSAL^FS^FO20,75^A0N,55,30^FD139X85X72CM V^FS^FO30,120^A0N,20,15^FDINCLUIDO IVA 19.0%^FS^FO30,140^A0N,20,15^FD750102311010-0034^FS^FO280,7^A0N,20,20^FD6 CUOTAS DE^FS^FO400,7^A0N,36,20^FDUS.$^FS^FO260,25^A0N,140,70^FD4113.^FS^FO400,50^A0N,80,45^FD33^FS^FO320,138^A0N,20,14^FDPRECIO TOTAL^FS^FO350,158^A0N,50,25^FDUS.$ 79.99^FS^PQ1,0,0,N,Y^XZ
		if (cantEtq > 0) {
			linea = "^XA^FWN^FO10,25^A0,55,37^FD" + des1 + "^FS";
			if (des2 != null && !des2.equals(""))
				linea = linea + "^FO10,75^A0,55,37^FD" + des2.trim()+"^FS";
			Double precioTemp = Double.valueOf(precio);
			Double interes = Double.valueOf(proveedor.substring(10).trim()) / 100;
			log.info("Interes: "+interes);
			Double cuotas = Double.valueOf(proveedor.substring(0, 10).trim());
			log.info("Coutas: "+cuotas);
			Double precioCuotaTemp = (precioTemp * (100+interes) / 100) / cuotas;
			log.info("Precio Cuota: "+precioCuotaTemp);
			String precioCuota = Double.valueOf(roundUp(precioCuotaTemp,100)).toString();
			log.info("Precio Cuota Real: "+precioCuota);
			int pos = precioCuota.indexOf(".");
			String enteros = precioCuota.substring(0, pos);
			String decimal = "00";
			if (precioCuota.length() > pos + 2)
				decimal = precioCuota.substring(pos + 1, pos + 3);
			else if (precioCuota.length() > pos + 1)
				decimal = precioCuota.substring(pos + 1, pos + 2) + "0";

			linea = linea + "^FO10,125^A0N,18,18^FD" + leyendaIva + "^FS^FO10,145^A0N,18,18^FD" + ean + "-" + articulo.substring(0, 4)
					+ "^FS^FO320,0^A0N,17,17^FD" + cuotas.intValue() + " CUOTAS DE" + "^FS^FO420,0^A0N,30,20^FD" + "US.$" + "^FS^FO";
			if (enteros.length() == 1)
				linea = linea + "390";
			else if (enteros.length() == 2)
				linea = linea + "350";
			else if (enteros.length() == 3)
				linea = linea + "310";
			else if (enteros.length() == 4)
				linea = linea + "260";
			else
				linea = linea + "250";
			
			int pos1 = precio.indexOf(".");
			String enteros1 = precio.substring(0, pos1);
			String decimal1 = "00";
			if (precio.length() > pos1 + 2)
				decimal1 = precio.substring(pos1 + 1, pos1 + 3);
			else if (precio.length() > pos1 + 1)
				decimal1 = precio.substring(pos1 + 1, pos1 + 2) + "0";

			linea = linea + ",25^A0N,140,90^FD" + enteros + "." + "^FS^FO450,25^A0N,100,45^FD" + decimal
					+ "^FS^FO340,140^A0N,17,17^FD" + "PRECIO TOTAL" + "^FS^FO370,160^A0N,32,17^FDUS.$ " + enteros1 + "." + decimal1 + "^FS^PQ" + Integer.valueOf(cantEtq) + ",0,0,N,Y^XZ";
		}
		return linea;
	}
}
