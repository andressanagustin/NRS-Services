package com.allc.arms.agent.operations.cer.syscard;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.keyed.KeyedFileBean;
import com.allc.arms.utils.keyed.KeyedFileMethods;
import com.allc.arms.utils.keyed.Util4690;
import com.ibm.OS4690.KeyedFile;

public class SyscardMsgKeyed { 

	static Logger log = Logger.getLogger(SyscardMsgKeyed.class);
	static KeyedFileBean keyedFileBean = new KeyedFileBean();
	
	public Object init(String file) {
		Boolean result = Boolean.FALSE;
		try {
			keyedFileBean.setPathAndFileName(file);
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
			keyedFileBean.setKeyLength(8);
			keyedFileBean.setRecordSize(14);
			if(KeyedFileMethods.openFile(keyedFileBean))
				result = Boolean.TRUE;
			log.info("Clave: "+keyedFileBean.getKeyLength() + " Length: "+keyedFileBean.getRecordSize());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public Object closure() {
		Boolean result = Boolean.FALSE;
		try {
			KeyedFileMethods.closeFile(keyedFileBean);
			result = Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
		
	}
	
	public Object writePointer(String key, String pointerData) {
		Boolean result = Boolean.FALSE;
		int numberBytesWritten = 0;
		try {
			byte[] pointerRecord = new byte[keyedFileBean.getRecordSize()];
			log.info("key: "+key);
			log.info("pointerData: "+pointerData + " tamaño: "+ pointerData.length());
			log.info("pointerRecord: "+pointerRecord.length);
			//pos + trx
			System.arraycopy(key.getBytes(), 0, pointerRecord, 0, 8);
			//msg
			System.arraycopy(pointerData.getBytes(), 0, pointerRecord, 8, 6);
			log.info("pointerRecord: "+pointerRecord.length);
			numberBytesWritten =   keyedFileBean.getKeyedFile().write(pointerRecord, KeyedFile.NO_UNLOCK, KeyedFile.NO_HOLD);

			if(numberBytesWritten == keyedFileBean.getKeyedFile().getRecordSize())
				result = Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	public String readPointer(String key) {
		try {
			int recordSize = keyedFileBean.getKeyedFile().getRecordSize();
			byte[] pointerRecord = new byte[recordSize];
			log.info("key: "+key);
			log.info("pointerRecord: "+pointerRecord.length);
			System.arraycopy(key.getBytes(), 0, pointerRecord, 0, key.length());
			keyedFileBean.getKeyedFile().read(pointerRecord, 1);
			log.info("Leido: "+ new String(pointerRecord));
			return (new String(pointerRecord)).substring(8);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}


}
