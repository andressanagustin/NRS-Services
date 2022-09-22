package com.allc.conexion;

import java.util.ArrayList;
import java.util.List;


public class Trama {

	public Trama(List list, int qtyHeaderElements, String separationCar ) {
		super();
		this.list = list;
		this.qtyHeaderElements = qtyHeaderElements;
		this.separationCar = separationCar;
		this.body = new ArrayList();
		this.Header = new ArrayList();
		this.bodyStr = new StringBuffer("");
		this.headerStr = new StringBuffer("");
	}
	
	private List body;
	private List Header;
	private StringBuffer headerStr;
	private StringBuffer bodyStr;
	private String separationCar;
	private int qtyHeaderElements;
	private List list;
	private String error;
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public StringBuffer getBodyStr() {
		return bodyStr;
	}
	public StringBuffer getHeaderStr() {
		return headerStr;
	}
	public List getList() {
		return list;
	}
	public int getQtyHeaderElements() {
		return qtyHeaderElements;
	}
	public String getSeparationCar() {
		return separationCar;
	}
	public List getBody() {
		return body;
	}
	public List getHeader() {
		return Header;
	}
	
	
	public void setBodyStr(StringBuffer body) {
		this.bodyStr = body;
	}
	public void setHeaderStr(StringBuffer header) {
		this.headerStr = header;
	}
	public void setList(List list) {
		this.list = list;
	}
	public void setQtyHeaderElements(int cantDatosHeader) {
		this.qtyHeaderElements = cantDatosHeader;
	}
	public void setSeparationCar(String separationCar) {
		this.separationCar = separationCar;
	}
	public void setBody(List body) {
		this.body = body;
	}
	public void setHeader(List header) {
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
				setHeaderStr(new StringBuffer(""));
				setBodyStr(new StringBuffer(""));
				//set header
				for (int b=0; b < getQtyHeaderElements() ;b++){
					setHeaderStr(getHeaderStr().append(getSeparationCar()).append(getList().get(b)));
					getHeader().add(getList().get(b));
				}
				setHeaderStr(getHeaderStr().delete(0, getSeparationCar().length()));
				//set body
				for (int b=getQtyHeaderElements(); b < getList().size() ;b++){
					setBodyStr(getBodyStr().append(getSeparationCar()).append(getList().get(b)));
					getBody().add(getList().get(b));
				}
				/**if exist body**/
				if(!(getBodyStr().toString().equals("")))
					setBodyStr(getBodyStr().delete(0, getSeparationCar().length()));
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
	public StringBuffer getString(){
		return getHeaderStr().append(getSeparationCar()).append(getBodyStr());
	}	
	
	public String toString() {
		StringBuffer builder = new StringBuffer();
		builder.append("Frame [Header=");
		builder.append(Header);
		builder.append(", body=");
		builder.append(body);
		builder.append(", headerStr=");
		builder.append(headerStr);
		builder.append(", bodyStr=");
		builder.append(bodyStr);
		builder.append(", separationCar=");
		builder.append(separationCar);
		builder.append(", qtyHeaderElements=");
		builder.append(qtyHeaderElements);
		builder.append(", list=");
		builder.append(list);
		builder.append(", error=");
		builder.append(error);
		builder.append("]");
		return builder.toString();
	}

}
