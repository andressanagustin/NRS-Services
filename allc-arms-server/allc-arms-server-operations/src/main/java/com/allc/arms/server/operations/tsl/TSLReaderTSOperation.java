package com.allc.arms.server.operations.tsl;

import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.file.UtilityFile;
import com.allc.arms.utils.tsl.TSLRecordXMLGenerator;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.entities.Transaction;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.allc.ticketserver.TicketserverApp;

public class TSLReaderTSOperation extends AbstractOperation {

	protected static Logger log = Logger.getLogger(TSLReaderTSOperation.class);
	protected static String tslDefaultLocalRepositoryToStore;
	private String storeCode = null;
	private String syncPath = null;

	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		
		storeCode = properties.getObject("eyes.store.code");
		syncPath = properties.getObject("searchEbil.sync.folder.path");
		
		try {
			tslDefaultLocalRepositoryToStore = properties.getObject("TSL.defaultLocalRepositoryToStore");

			if (StringUtils.isNotBlank(tslDefaultLocalRepositoryToStore))
				UtilityFile.createDir(tslDefaultLocalRepositoryToStore);
			log.info("TSL.defaultLocalRepositoryToStore: " + tslDefaultLocalRepositoryToStore);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		try {
			StringBuilder msg = new StringBuilder();
			/** Instantiate from TSLRecordXMLGenerator **/
			TSLRecordXMLGenerator tslRecordXMLGenerator = new TSLRecordXMLGenerator();
			/** pass the TSLRecord and the path to save it as a xml file **/
			//Transaction trx = tslRecordXMLGenerator.generate(frame, tslDefaultLocalRepositoryToStore, false, null, storeCode, syncPath);
			Transaction trx = tslRecordXMLGenerator.generate(frame, tslDefaultLocalRepositoryToStore, false, null, Integer.valueOf(storeCode) == 0, syncPath);
			if (trx != null) {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
			} else {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("-1");
			}

			String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
			socket.writeDataSocket(tmp);

			if (trx.getControlTransaction() != null && trx.getControlTransaction().getBusinessEOD() != null) {
				log.info("Starting Ticketserver...(IP: " + socket.getClient().getInetAddress().getHostAddress() + ")");
				TicketserverApp tcktSrvr = new TicketserverApp();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String fecha = sdf.format(trx.getBusinessDayDate()).toString();
				String zipName = trx.getRetailStoreCode() + fecha.substring(6, 8) + fecha.substring(4, 6) + fecha.substring(2, 4) + ".zip";

				tcktSrvr.process(socket.getClient().getInetAddress().getHostAddress(), zipName);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public static String formatMonth(String month) {
		try {
			new Integer(month);
		} catch (NumberFormatException e) {
		}
		if ("10".equalsIgnoreCase(month)) {
			return "A";
		} else if ("11".equalsIgnoreCase(month)) {
			return "B";
		} else if ("12".equalsIgnoreCase(month)) {
			return "C";
		}
		return month;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
