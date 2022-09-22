package com.allc.arms.agent.operations.colas;


import java.util.Date;
import java.util.regex.Pattern;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.core.receiver.ReceiverPipe;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.RandomAccessFile4690;
import org.apache.commons.lang.StringUtils;

public class RegistroPersonasEnColaOperationV3 extends AbstractOperation {

	protected Logger log = Logger.getLogger(RegistroPersonasEnColaOperationV3.class);
	protected String storeNumber = "";
	protected String terminal = "";
	protected String personasCola = "";
	protected String numeroTrx = "";
	protected String storeCode;
	protected String resp;
	protected String valorEnCero = Util.rpad(ArmsAgentConstants.Communication.CERO, ArmsAgentConstants.Communication.SPACE, 20)
			+ ArmsAgentConstants.Communication.CRLF;
	protected static Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected ConnSocketClient socketClient;
    protected RandomAccessFile4690 randSeekRead = null;
	
	protected String AlertSeekFileName;

	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		log.info("Inicio de operacion COLAS V3");
		resp = "2";
		
		try {
			AlertSeekFileName = properties.getObject("alertaPersonaCola.file.seek"); //Poner nombre de archivo en propiedad.
			String alertListFileName;
	        String newAlertListFileName = properties.getObject("alertaPersonaCola.file.listAlert");;
			
			if (!Files.fileExists4690(AlertSeekFileName)) {
				Files.creaEscribeDataArchivo4690(AlertSeekFileName, valorEnCero, false);
				Files.creaEscribeDataArchivo4690(AlertSeekFileName, newAlertListFileName  + ArmsAgentConstants.Communication.CRLF, true);
				alertListFileName = newAlertListFileName;
			} else {
				alertListFileName = Files.readSpecifictLineOfFile4690(AlertSeekFileName, 2);
			}
			
			ReceiverPipe.waitAvailable = false;
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			storeNumber = controllerStatusData.getStoreNumber();
			terminal = (String) frame.getHeader().get(3);
			numeroTrx = (String) frame.getBody().get(0);
			personasCola = (String) frame.getBody().get(1);

			log.info("ALERTA PERSONAS EN COLA --> storeNumber: " + storeNumber+ ", terminal: " + terminal+", numeroTrx: " + numeroTrx + ", personasCola: " + personasCola);
                        
                        if (StringUtils.isNotBlank(personasCola) && Integer.parseInt(personasCola) > 0) {
                            
                            StringBuffer data = getFrameHeader();
                            data.append(ArmsAgentConstants.Communication.FRAME_SEP).append(storeNumber)
                                            .append(ArmsAgentConstants.Communication.FRAME_SEP).append(terminal)
                                            .append(ArmsAgentConstants.Communication.FRAME_SEP).append(numeroTrx)
                                            .append(ArmsAgentConstants.Communication.FRAME_SEP).append(personasCola).append(ArmsAgentConstants.Communication.CRLF);

                            /// guardar en un archivo
                            Files.creaEscribeDataArchivo4690(newAlertListFileName,data.toString(),true);
                        }

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			//log.info("ERROR: " + e);
		}

		return false;
	}

	public boolean process(ConnSocketServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	protected StringBuffer getFrameHeader() {
		StringBuffer data = new StringBuffer();
		data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP).append("58")
				.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000")
				.append(ArmsAgentConstants.Communication.FRAME_SEP).append(storeCode)
				.append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Communication.TEMP_CONN)
				.append(ArmsAgentConstants.Communication.FRAME_SEP)
				.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
		return data;
	}

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}
    
    protected String addReg() {
        String data = null;
        try {
            randSeekRead = new RandomAccessFile4690(AlertSeekFileName, "r");
            randSeekRead.readLine();
            //la linea que contiene el nombre es la segunda
            data = randSeekRead.readLine();
            randSeekRead.seek(0);
            randSeekRead.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return data;
    }

}
