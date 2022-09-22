/**
 * 
 */
package com.allc.comm.frame;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Entidad utilizada para representar una trama de comunicación.
 * 
 * @author gustavo
 *
 */
public class Frame {
	private static Logger logger = Logger.getLogger(Frame.class);
	/** Constants to identify the header's data **/
	public static final int COMMUNICATION_CHANNEL = 0;
	public static final int OPERATION_REQUEST = 1;
	public static final int RESPONSE_CHANNEL = 2;
	public static final int POS_SOURCE = 3;
	public static final int PERMANENT_CONN = 4;
	public static final int DATETIME_PROCESS = 5;

	private List body;
	private List Header;
	private StringBuffer headerStr;
	private StringBuffer bodyStr;
	private String separator;
	private int qtyHeaderElements;
	private List list;

	/**
	 * Constructor from Frame class.
	 * 
	 * @param list
	 *            list of elements to load in the frame.
	 * @param qtyHeaderElements
	 *            Quantity of Header's element
	 * @param separator
	 *            Character to separate the frame's element
	 */
	public Frame(List list, int qtyHeaderElements, String separator) {
		super();
		this.list = list;
		this.qtyHeaderElements = qtyHeaderElements;
		this.separator = separator;
		this.body = new ArrayList();
		this.Header = new ArrayList();
		this.bodyStr = new StringBuffer("");
		this.headerStr = new StringBuffer("");
	}

	/**
	 * Funcion que dividira la lista(previamente asignada) en header y body (previamente se indica la cantidad de datos del header) y el
	 * caracter que separa cada dato, eso se hace en el constructor de la clase: Trama(List lista, int cantDatosHeader, String carSeparacion
	 * )
	 * 
	 * @return true si no tuvo inconvenientes en realizar la separacion. caso contrario devuelve false
	 */
	public boolean loadData() {
		boolean ok = false;
		try {
			if (validateTramaLength()) {
				setHeaderStr(new StringBuffer(""));
				setBodyStr(new StringBuffer(""));
				// set header
				for (int b = 0; b < getQtyHeaderElements(); b++) {
					setHeaderStr(getHeaderStr().append(getSeparator()).append(getList().get(b)));
					getHeader().add(getList().get(b));
				}
				setHeaderStr(getHeaderStr().delete(0, getSeparator().length()));
				// set body
				for (int b = getQtyHeaderElements(); b < getList().size(); b++) {
					setBodyStr(getBodyStr().append(getSeparator()).append(getList().get(b)));
					getBody().add(getList().get(b));
				}
				/** if exist body **/
				if (!(getBodyStr().toString().equals("")))
					setBodyStr(getBodyStr().delete(0, getSeparator().length()));
				ok = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return ok;
	}

	/**
	 * 
	 */

	public boolean validateTramaLength() {
		boolean ok = true;
		try {
			if (getList().size() < getQtyHeaderElements()) {
				ok = false;
				logger.error("El formato de la trama no es correcto.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return ok;
	}

	/**
	 * Funcion que obtiene el codigo del estado de la trama de comunicacion
	 * 
	 * @return Si es 0 es comunicacion correcta <> de 0 en caso contrario.
	 */
	public int getStatusTrama() {
		return Integer.parseInt((String) getList().get(getQtyHeaderElements()));
	}

	/**
	 * Obtain the Trama representation in String format
	 * 
	 * @return getHeader() + getSeparationCar() + getBodyStr()
	 */
	public StringBuffer getString() {
		return getHeaderStr().append(getSeparator()).append(getBodyStr());
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Frame [=Header").append(Header).append(", body=").append(body).append(", headerStr=").append(headerStr)
				.append(", bodyStr=").append(bodyStr).append(", separator=").append(separator).append(", qtyHeaderElements=")
				.append(qtyHeaderElements).append(", list=").append(list).append("]");
		return buffer.toString();
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

	public String getSeparator() {
		return separator;
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

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public void setBody(List body) {
		this.body = body;
	}

	public void setHeader(List header) {
		Header = header;
	}

}
