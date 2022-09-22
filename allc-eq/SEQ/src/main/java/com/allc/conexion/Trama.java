package com.allc.conexion;

import java.util.ArrayList;
import java.util.List;


public class Trama {

	public Trama(List<String> list, int qtyHeaderElements, String separationCar ) {
		super();
		this.list = list;
		this.qtyHeaderElements = qtyHeaderElements;
		this.separationCar = separationCar;
		this.body = new ArrayList<String>();
		this.Header = new ArrayList<String>();
		this.bodyStr = "";
		this.headerStr = "";
	}
	
	private List<String> body;
	private List<String> Header;
	private String headerStr;
	private String bodyStr;
	private String separationCar;
	private int qtyHeaderElements;
	private List<String> list;
	private String error;
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getBodyStr() {
		return bodyStr;
	}
	public String getHeaderStr() {
		return headerStr;
	}
	public List<String> getList() {
		return list;
	}
	public int getQtyHeaderElements() {
		return qtyHeaderElements;
	}
	public String getSeparationCar() {
		return separationCar;
	}
	public List<String> getBody() {
		return body;
	}
	public List<String> getHeader() {
		return Header;
	}
	
	
	public void setBodyStr(String body) {
		this.bodyStr = body;
	}
	public void setHeaderStr(String header) {
		this.headerStr = header;
	}
	public void setList(List<String> list) {
		this.list = list;
	}
	public void setQtyHeaderElements(int cantDatosHeader) {
		this.qtyHeaderElements = cantDatosHeader;
	}
	public void setSeparationCar(String separationCar) {
		this.separationCar = separationCar;
	}
	public void setBody(List<String> body) {
		this.body = body;
	}
	public void setHeader(List<String> header) {
		Header = header;
	}
		
	/**
	 * Funcion que dividira la lista(previamente asignada) en header y body (previamente se indica la cantidad de datos del header)
	 * y el caracter que separa cada dato, eso se hace en el constructor de la clase: 
	 * Trama(List lista, int cantDatosHeader, String carSeparacion )
	 * 
	 * @return
	 * 		true si no tuvo inconvenientes en realizar la separacion.
	 * 		caso contrario devuelve false
	 */
	public boolean loadData(){
		boolean ok=false;
		try{
			if(validateTramaLength()){
				setHeaderStr("");
				setBodyStr("");
				//set header
				for (int b=0; b < getQtyHeaderElements() ;b++){
					setHeaderStr(getHeaderStr() + getSeparationCar() + getList().get(b));
					getHeader().add(getList().get(b));
				}
				setHeaderStr(getHeaderStr().substring(1));
				//set body
				for (int b=getQtyHeaderElements(); b < getList().size() ;b++){
					setBodyStr(getBodyStr() + getSeparationCar() + getList().get(b));
					getBody().add(getList().get(b));
				}
				/**if exist body**/
				if(!(getBodyStr().equals("")))
					setBodyStr(getBodyStr().substring(1));
				setError("");
				ok = true;
			}
		}catch(Exception e){
			setError("loadData: " + e);
		}
		return ok;
	}
	/**
	 * 
	 */

	public boolean validateTramaLength(){
		boolean ok=true;
		try{			
			if(getList().size()<getQtyHeaderElements()){
				ok = false;
				setError("La cantidad de elementos de la trama: " + getList().size() + " es menor que la cantidad de datos del header: " + getQtyHeaderElements());
			}
			setError("");
		}catch(Exception e){
			setError("validateTramaLength: " + e);
		}
		return ok;
	}

	/**
	 * Funcion que obtiene el codigo del estado de la trama de comunicacion
	 * 
	 * @return   Si es 0 es comunicacion correcta
	 * 			 <> de 0 en caso contrario.
	 */
	public int getStatusTrama(){
		return  Integer.parseInt((String)getList().get(getQtyHeaderElements())) ;
	}

	/**
	 * Obtain the Trama representation in String format
	 * @return   getHeader() + getSeparationCar() + getBodyStr()
	 */
	public String listToStr(){
		return getHeaderStr() + getSeparationCar() + getBodyStr();
	}

	public String toString() {
		final int maxLen = 14;
		return "Trama ["
				+ (Header != null ? "Header="
						+ Header.subList(0, Math.min(Header.size(), maxLen))
						+ ", " : "")
				+ (body != null ? "body="
						+ body.subList(0, Math.min(body.size(), maxLen)) + ", "
						: "")
				+ (headerStr != null ? "headerStr=" + headerStr + ", " : "")
				+ (bodyStr != null ? "bodyStr=" + bodyStr + ", " : "")
				+ (separationCar != null ? "carSeparacion=" + separationCar
						+ ", " : "")
				+ "cantDatosHeader=" + qtyHeaderElements
				+ ", "
				+ (list != null ? "lista="
						+ list.subList(0, Math.min(list.size(), maxLen)) : "")
				+ "]";
	}
	


	
}
