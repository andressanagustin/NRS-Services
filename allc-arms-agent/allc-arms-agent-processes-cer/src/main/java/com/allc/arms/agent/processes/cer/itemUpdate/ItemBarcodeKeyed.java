package com.allc.arms.agent.processes.cer.itemUpdate;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.keyed.KeyedFileBean;
import com.allc.arms.utils.keyed.KeyedFileMethods;
import com.allc.arms.utils.keyed.Util4690;
import com.ibm.OS4690.KeyedFile;

public class ItemBarcodeKeyed { 

	static Logger log = Logger.getLogger(ItemBarcodeKeyed.class);
	static KeyedFileBean keyedFileBean = new KeyedFileBean();
	public static final String ACTION_DELETE = "B";
	
	public Object init(String file) {
		Boolean result = Boolean.FALSE;
		try {
			keyedFileBean.setPathAndFileName(file);
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
			keyedFileBean.setKeyLength(7);
			keyedFileBean.setRecordSize(13);
			if(KeyedFileMethods.openFile(keyedFileBean))
				result = Boolean.TRUE;
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
	
	public Object writeItem(String itemData) {
		Boolean result = Boolean.FALSE;
		int pos = 0;
		int numberBytesWritten = 0;
		byte[] field;
		try {
			byte[] itemRecord = new byte[keyedFileBean.getKeyedFile().getRecordSize()];
			
			if (ACTION_DELETE.equalsIgnoreCase(itemData.substring(0, 1))) {
				
				keyedFileBean.getKeyedFile().delete(Util4690.pack(
						StringUtils.leftPad(itemData.substring(13, 25), 14, ArmsAgentConstants.Communication.CERO)));
			} else {
			//barcode
			field = Util4690.pack(StringUtils.leftPad(itemData.substring(13, 25), 14, ArmsAgentConstants.Communication.CERO)) ;
			System.arraycopy(field, 0, itemRecord, pos, field.length);
			pos += field.length;
			//item code
			field = Util4690.pack(itemData.substring(1, 13)) ;
			System.arraycopy(field, 0, itemRecord, pos, field.length);
			pos += field.length;
			
//			if(log.isTraceEnabled()){
//				log.trace("itemRecord: " + new String(itemRecord));
//			}

			numberBytesWritten =   keyedFileBean.getKeyedFile().write(itemRecord, KeyedFile.NO_UNLOCK, KeyedFile.NO_HOLD);

			if(numberBytesWritten == keyedFileBean.getKeyedFile().getRecordSize())
				result = Boolean.TRUE;
		}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

}
