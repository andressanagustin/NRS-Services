/**
 * 
 */
package com.allc.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.allc.entities.Transaction;
import com.thoughtworks.xstream.XStream;

/**
 * @author GUSTAVOK
 *
 */
public class ConverterFromXML {
	private static XStream xstream;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		xstream = new XStream();
		
		File file = new File("C:/ALLC/tlogRetiro.xml");
		StringBuffer xml = new StringBuffer();
		FileInputStream fis = null;
		try  {
			fis = new FileInputStream(file);
			System.out.println("Total file size to read (in bytes) : "+ fis.available());
 
			int content;
			while ((content = fis.read()) != -1) {
				// convert to char and display it
				xml.append((char) content);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fis!=null)
					fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Transaction trx = (Transaction) xstream.fromXML(xml.toString());
		String xml1 = xstream.toXML(trx);
		int i = 0;
		i++;
	}

	public ConverterFromXML(){
		XStreamConfig xStreamConfig = new XStreamConfig();
		xstream = xStreamConfig.getXstream();
	}
	
	public Transaction getTransaction(File tlog) throws Exception{
		FileInputStream fis = null;
		try  {
			fis = new FileInputStream(tlog);
			Transaction trx = (Transaction) xstream.fromXML(fis);
			return trx;
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if(fis!=null)
					fis.close();
			} catch (Exception e) {
				throw e;
			}
		}
	}
}
