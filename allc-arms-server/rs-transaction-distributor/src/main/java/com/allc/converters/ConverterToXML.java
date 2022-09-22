/**
 * 
 */
package com.allc.converters;

import com.allc.entities.Transaction;
import com.thoughtworks.xstream.XStream;

/**
 * @author GUSTAVOK
 *
 */
public class ConverterToXML {
	private static XStream xstream;

	public ConverterToXML(){
		XStreamConfig xStreamConfig = new XStreamConfig();
		xstream = xStreamConfig.getXstream();
		

	}
	
	public String getXmlByTransaction(Transaction transaction) throws Exception {
		try  {
			return xstream.toXML(transaction);
		} catch (Exception e) {
			throw e;
		}
	}
}
