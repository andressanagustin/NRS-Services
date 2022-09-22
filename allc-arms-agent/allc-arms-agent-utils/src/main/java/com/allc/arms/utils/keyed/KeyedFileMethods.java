package com.allc.arms.utils.keyed;

import java.io.File;

import org.apache.log4j.Logger;

import com.ibm.OS4690.File4690;
import com.ibm.OS4690.KeyedFile;

public class KeyedFileMethods {
	static Logger log = Logger.getLogger(KeyedFileMethods.class);

	/**
	 * Open an existing keyed file
	 * 
	 * @param keyedFileBean
	 *            The Bean with the parameters to open/create a keyed file.
	 * @return true if the keyed File was open
	 */
	public static boolean openFile(KeyedFileBean keyedFileBean) {
		boolean resultado = false;
		try {
			File4690 fl = new File4690(keyedFileBean.getPathAndFileName());
			if (fl.exists()) {

				KeyedFile tmpKF = new KeyedFile(keyedFileBean.getPathAndFileName(), keyedFileBean.getMode(), keyedFileBean.getAccess());
				keyedFileBean.setKeyedFile(tmpKF);
				resultado = true;
			} else {
				log.error("No existe el archivo: " + keyedFileBean.getPathAndFileName());
			}
		} catch (Exception e) {
			keyedFileBean.setKeyedFile(null);
			log.error(e.getMessage(), e);
		}
		return resultado;
	}

	/**
	 * Method to create an open a Distributed Keyed File
	 * 
	 * @param keyedFileBean
	 *            The Bean with the parameters to open/create a keyed file.
	 * @return true if the keyed File was created an open
	 */
	public static boolean createKeyedFile(KeyedFileBean keyedFileBean) {
		boolean result = false;
		try {

			File fl = new File(keyedFileBean.getPathAndFileName());
			if (!fl.exists()) {
				keyedFileBean.setKeyedFile(new KeyedFile(fl, keyedFileBean.getMode(), keyedFileBean.getAccess(), keyedFileBean
						.getFileType(), keyedFileBean.getDistributionMethod(), keyedFileBean.getKeyLength(), keyedFileBean.getRecordSize(),
						keyedFileBean.getNumberOfRecords(), keyedFileBean.getRandomizingDivisor(), keyedFileBean.getChainingThreshold()));
				result = true;
			}
		} catch (Exception e) {
			keyedFileBean.setKeyedFile(null);
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Close a Keyed File, close the file stream and release any system resources associated with the stream
	 * 
	 * @return true if the file was closed. false otherwise
	 */
	public static boolean closeFile(KeyedFileBean keyedFileBean) {
		boolean result = false;
		try {
			if (null != keyedFileBean.getKeyedFile()) {
				keyedFileBean.getKeyedFile().closeFull();
				result = true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Method to fisrt at all open a Keyed File, if it exists, otherwise create a Keyed File
	 * 
	 * @param keyedFileBean
	 *            The Bean with the parameters to open/create a keyed file.
	 * @return true if the file is opened or created false otherwise
	 */
	public static boolean openCreateKeyedFile(KeyedFileBean keyedFileBean) {
		boolean result = false;
		try {
			if (!openFile(keyedFileBean)) {
				// log.error("openCreateKeyedFile: Keyed file " + keyedFileBean.getPathAndFileName() + " doesn't exist");
				// if(createKeyedFile(keyedFileBean)){
				// log.info("openCreateKeyedFile: Keyed File " + keyedFileBean.getPathAndFileName() + " created");
				// result = true;
				// }
			} else {
				log.info("openCreateKeyedFile: Keyed File " + keyedFileBean.getPathAndFileName() + " opened");
				result = true;
			}
		} catch (Exception e) {
			keyedFileBean.setKeyedFile(null);
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Delete a record From a Keyed File
	 * 
	 * @param keyedFileBean
	 *            The Bean with the parameters of the Keyed File
	 * @param id
	 *            Key to identify the record
	 * @param packed
	 *            indicate if the key is packed or not.
	 * @return true if the record was deleted. false otherwise
	 */
	public static boolean deleteRecord(KeyedFileBean keyedFileBean, String id, boolean packed) {
		boolean result = false;
		byte[] recordToDelete = null;
		byte[] key = null;
		try {
			key = new byte[keyedFileBean.getKeyedFile().getKeyLength()];
			recordToDelete = new byte[keyedFileBean.getKeyedFile().getRecordSize()];
			if (packed) {
				BCD.packUPD(key, 0, id);
			} else {
				/** fill the record key **/
				for (int j = 0; j < key.length; j++) {
					key[j] = (byte) id.charAt(j);
				}
			}
			System.arraycopy(key, 0, recordToDelete, 0, key.length);
			keyedFileBean.getKeyedFile().delete(recordToDelete);
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
}
