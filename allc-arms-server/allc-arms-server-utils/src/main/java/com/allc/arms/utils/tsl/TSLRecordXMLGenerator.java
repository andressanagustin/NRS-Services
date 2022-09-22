package com.allc.arms.utils.tsl;

import java.io.File;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.converters.ConverterToXML;
import com.allc.entities.RetailStore;
import com.allc.entities.Transaction;
import com.allc.util.ConstantsUtil;

public class TSLRecordXMLGenerator {

	private static Logger log = Logger.getLogger(TSLRecordXMLGenerator.class);
	private static Pattern c = Pattern.compile(",");
	private static Pattern p = Pattern.compile("\\|");
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	String nameSync = null;

	//cambiar, poner bandera para identificar si es central o no...
	public Transaction generate(Frame trama, String pathToSave, boolean saveTlog, RetailStore retailStore,
			boolean esCentral, String syncPath) {
		String value = null;
		String fecha = null;
		String name = null;
		String data = null;
		try {

			data = trama.getBody().get(TSLConstants.TSL_RECORD_POSITION_BODY).toString();
			log.debug("generate receipt " + data);
			List<String> list = Arrays.asList(c.split(data));

			ConverterToXML converterToXML = new ConverterToXML();
			Transaction transaction = new Transaction();
			/** position three has the store number **/
			transaction.setRetailStoreCode(trama.getHeader().get(3).toString());
			transaction.setRetailStore(retailStore);
			/** BusinessDayDateString **/
			String datetime = trama.getBody().get(TSLConstants.BUSINESS_DATE_DAY_POSITION_BODY).toString();
			int year = Integer.valueOf(datetime.substring(0, 4));
			int month = Integer.valueOf(datetime.substring(4, 6));
			int day = Integer.valueOf(datetime.substring(6, 8));
			Calendar calendar = new GregorianCalendar(year, month-1, day, 0, 0);
			Date businessDateDay = calendar.getTime();
			transaction.setBusinessDayDateString(
					ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(businessDateDay));
			/**
			 * this list is used just in case the original transaction need to
			 * add a new transaction, like sign on
			 **/
			List<Transaction> additionalTransactionsList = new ArrayList<Transaction>();
			TSLTransactionMethods tslTransactionMethods = new TSLTransactionMethods();
			for (int i = 0; i < list.size(); i++) {
				if (StringUtils.isNotBlank(list.get(i).toString()))
					tslTransactionMethods.transactionProcess(transaction, list.get(i).toString(),
							additionalTransactionsList);
			}
			/**
			 * after all, when the strings are processed add the original
			 * transaction to the list
			 **/
			additionalTransactionsList.add(transaction);

			try {
				if (saveTlog) {
					for (int i = 0; i < additionalTransactionsList.size(); i++) {
						value = converterToXML.getXmlByTransaction(((Transaction) additionalTransactionsList.get(i)));
                                                if(value != null && transaction.getBeginDateTime() != null){
                                                    fecha = sdf.format(transaction.getBeginDateTime());
                                                    String extraDir = "";
                                                    if(retailStore != null)
                                                    {
                                                            extraDir = retailStore.getDistDir() != null && !retailStore.getDistDir().isEmpty() ? File.separator + retailStore.getDistDir() : "";
                                                            log.info("storeCode: " + retailStore.getCode() + "extraDir: " + extraDir);
                                                    }
                                                    //storeCode usado como bandera si es central o no
                                                    //if (Integer.valueOf(storeCode) > 0) {
                                                    if (!esCentral) {
                                                            log.info("Es Regional, guarda en out.");
                                                            nameSync = pathToSave + File.separator + syncPath + extraDir + File.separator
                                                                            + transaction.getRetailStoreCode() + "-" + transaction.getWorkstationCode() + "-"
                                                                            + transaction.getSequenceNumber() + "-" + fecha + "-" + i;

                                                            boolean nameExist = true;
                                                            int seq = 0;
                                                            while (nameExist) {
                                                                    File fileAux = new File(nameSync.substring(0, nameSync.lastIndexOf(File.separator) + 1));
                                                                    log.info("File: " + fileAux.getPath());
                                                                    fileAux.mkdirs();								
                                                                    File file = new File(nameSync + (seq > 0 ? "-" + seq + ".xml" : ".xml"));
                                                                    //file.mkdirs();
                                                                    if (!file.exists()) {
                                                                            nameSync = nameSync + "-" + seq + ".xml";
                                                                            nameExist = false;
                                                                    } else
                                                                            seq++;
                                                            }
                                                            UtilityFile.createWriteDataFile(nameSync, value, false);
                                                    }
                                                    boolean nameExist = true;
                                                    int seq = 0;


                                                    name = pathToSave + extraDir + File.separator + transaction.getRetailStoreCode() + "-"
                                                                    + transaction.getWorkstationCode() + "-" + transaction.getSequenceNumber() + "-" + fecha
                                                                    + "-" + i;
                                                    while (nameExist) {
                                                            File file = new File(name + (seq > 0 ? "-" + seq + ".xml" : ".xml"));
                                                            if (!file.exists()) {
                                                                    name = name + "-" + seq + ".xml";
                                                                    nameExist = false;
                                                            } else
                                                                    seq++;
                                                    }

                                                    UtilityFile.createWriteDataFile(name, value, false);

                                                    log.info("Tlog generado: " + name);
                                                    log.debug(value);
                                                }
					}
				}
				return transaction;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public Transaction generateByEL(Frame trama, String pathToSave, RetailStore retailStore, Date oldBusinessDate,
			String storeCode, String syncPath) {
		String value = null;
		String fecha = null;
		String name = null;
		String data = null;
		int sequenceNumber = Integer.valueOf((String) trama.getBody().get(2));
		try {

			data = trama.getBody().get(TSLConstants.TSL_RECORD_POSITION_BODY).toString();

			List stringTypesList = Arrays.asList(p.split(data));
			int indicat0 = Integer.valueOf(stringTypesList.get(ELConstants.Positions.INDICAT0).toString()).intValue();

			ConverterToXML converterToXML = new ConverterToXML();
			Transaction transaction = new Transaction();
			transaction.setRetailStoreCode(trama.getHeader().get(3).toString());
			transaction.setRetailStore(retailStore);
			
			Date businessDateDay = TSLConstants.Formatters.TLOG_SUFIX_DATE_TIME_FORMATTER_ACE_2
					.parse(trama.getBody().get(TSLConstants.BUSINESS_DATE_DAY_POSITION_BODY).toString());
			if (indicat0 > 2)
				transaction.setBusinessDayDateString(
						ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(oldBusinessDate).toString());
			else
				transaction.setBusinessDayDateString(
						ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(businessDateDay).toString());
			ELTransactionMethods.transactionProcess(transaction, data);
			transaction.setSequenceNumber(sequenceNumber);
			try {
				value = converterToXML.getXmlByTransaction(transaction);
				fecha = sdf.format(transaction.getBusinessDayDate()).toString();

				if (Integer.valueOf(storeCode) > 0) {
					nameSync = pathToSave + File.separator + syncPath + File.separator + transaction.getRetailStoreCode() + "-"
							+ transaction.getWorkstationCode() + "-" + transaction.getSequenceNumber() + "-" + fecha;
					boolean nameExist = true;
					int seq = 0;
					while (nameExist) {
						//File file = new File(nameSync + (seq > 0 ? "-" + seq + ".xml" : ".xml"));
						File file = new File(name + "-" + seq + ".xml");
						if (!file.exists()) {
							nameSync = nameSync + "-" + seq + ".xml";
							nameExist = false;
						} else
							seq++;
					}
					UtilityFile.createWriteDataFile(nameSync, value, false);
				}

				name = pathToSave + File.separator + transaction.getRetailStoreCode() + "-"
						+ transaction.getWorkstationCode() + "-" + transaction.getSequenceNumber() + "-" + fecha;
				
				boolean nameExist = true;
				int seq = 0;
				while (nameExist) {
					//File file = new File(name + (seq > 0 ? "-" + seq + ".xml" : ".xml"));
					File file = new File(name + "-" + seq + ".xml");
					if (!file.exists()) {
						name = name + "-" + seq + ".xml";
						nameExist = false;
					} else
						seq++;
				}
				UtilityFile.createWriteDataFile(name, value, false);
				log.info("Tlog generado: " + name);
				return transaction;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

}
