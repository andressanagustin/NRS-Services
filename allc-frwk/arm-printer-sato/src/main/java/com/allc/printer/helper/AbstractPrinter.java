/**
 * 
 */
package com.allc.printer.helper;

/**
 * @author gustavo
 *
 */
public abstract class AbstractPrinter {

	public abstract void print(String articulo, String ean, String descripcion, String tamano, String marca, String referencia,
			String indImpto, String proveedor, String uxc, double precio, int cantEtq, char tipoEtiq, double porcRecargo, double porcIva,
			String modelBarra, boolean flMsjNoAfiliado);

	public abstract void printSup(String codEan, String nombre);
}
