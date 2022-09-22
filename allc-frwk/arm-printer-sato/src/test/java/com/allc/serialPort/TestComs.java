package com.allc.serialPort;

import gnu.io.CommPortIdentifier;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.allc.printer.helper.Runner;

/**
 * 
 */

/**
 * @author gustavo
 *
 */
public class TestComs {
	private static Logger log;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			 String path = System.getProperty("java.library.path");
			 System.out.println("java.library.path = " + path);
			Properties prop = new Properties();
			FileInputStream fis = new FileInputStream("log4jPrinterTester.properties");
			prop.load(fis);
			fis.close();
			PropertyConfigurator.configure(prop);
			log = Logger.getLogger(Runner.class);
			listPorts();
//	        SerialPort serialPort = new SerialPort();
//	        List<String> portsFree;
//			portsFree = serialPort.getFreeSerialPort();
//			log.info("Puertos serie abiertos:");
//			for (String free : portsFree) { 
//	            log.info(free); 
//	        }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} 
          
	}
    static void listPorts()
    {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            log.info(portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType()) );
        }        
    }
    
    static String getPortTypeName ( int portType )
    {
        switch ( portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

}
