package com.allc.arms.agent.operations.customer;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.customer.Customer;
import com.allc.arms.utils.keyed.KeyedFileBean;
import com.allc.arms.utils.keyed.KeyedFileMethods;
import com.allc.arms.utils.keyed.Util4690;
import com.allc.properties.PropFile;
import com.ibm.OS4690.KeyedFile;


public class CustomerEnrollmentKeyed  {

	private Logger log = Logger.getLogger(CustomerEnrollmentKeyed.class);
	private KeyedFileBean keyedFileBean = new KeyedFileBean();
	private String customerEnrollmentKeyedFileName;
	
	public Object init(PropFile properties){
		Boolean result = Boolean.FALSE;
		try {
			customerEnrollmentKeyedFileName = (String)properties.getObject("customer.enrollment.keyedFile.name");
			keyedFileBean.setPathAndFileName(customerEnrollmentKeyedFileName);
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.DISTRIBUTE_ON_UPDATE);
			keyedFileBean.setKeyLength(9);
			keyedFileBean.setRecordSize(254);
			if(KeyedFileMethods.openFile(keyedFileBean))
				result = Boolean.TRUE;
		} catch (Exception e) {
			log.error(e);
		}
		return result;
	}
	
	public Object closure(){
		Boolean result = Boolean.FALSE;
		try {
			KeyedFileMethods.closeFile(keyedFileBean);
			result = Boolean.TRUE;
		} catch (Exception e) {
			log.error(e);
		}
		return result;
	}
	
	public Object process(Object object){
		Boolean result = Boolean.FALSE;
		int pos = 0;
		int numberBytesWritten = 0;

		byte[] field;
		try {
			if (!(object instanceof Customer)) {
				result = Boolean.FALSE;
			}
			
			int RecordSize = keyedFileBean.getKeyedFile().getRecordSize();

			byte[] customerRecord = new byte[RecordSize];
			if(log.isTraceEnabled())  
			   log.trace("RecordSize: " + RecordSize);
			
			field = Util4690.pack(StringUtils.right(StringUtils.leftPad( ((Customer)object).getCustomerId(), 18, ArmsAgentConstants.Communication.CERO),18)) ;
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.leftPad(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getRecordStatus(),ArmsAgentConstants.Communication.CERO).toString(), 1, ArmsAgentConstants.Communication.CERO).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left((ObjectUtils.defaultIfNull(((Customer)object).getLastName1(), ArmsAgentConstants.Communication.SPACE).toString().trim() + 
														   ArmsAgentConstants.Communication.SPACE + 
														   ObjectUtils.defaultIfNull(((Customer)object).getLastName2(), ArmsAgentConstants.Communication.SPACE).toString().trim()).trim()
														  , 17), 17, ArmsAgentConstants.Communication.SPACE ).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;			
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getFirstName(), ArmsAgentConstants.Communication.SPACE).toString(), 13), 13, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getAddressLine1(), ArmsAgentConstants.Communication.SPACE).toString(), 25), 25, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getAddressLine2(), ArmsAgentConstants.Communication.SPACE).toString(), 25), 25, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);

			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getCityName(), ArmsAgentConstants.Communication.SPACE).toString(), 20), 20, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getStateCode(), ArmsAgentConstants.Communication.SPACE).toString(), 2), 2, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);

			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getZipCode(), ArmsAgentConstants.Communication.CERO).toString(), 10), 10, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getHomePhoneAreaCode(), ArmsAgentConstants.Communication.CERO).toString(), 4), 4, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getHomePhone(), ArmsAgentConstants.Communication.CERO).toString(), 8), 8, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getBusPhoneAreaCode(), ArmsAgentConstants.Communication.CERO).toString(), 4), 4, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getBusPhone(), ArmsAgentConstants.Communication.CERO).toString(), 8), 8, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getDriverLicenseNum(), ArmsAgentConstants.Communication.SPACE).toString(), 20), 20, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getHomeStoreNumber(), ArmsAgentConstants.Communication.CERO).toString(), 6), 6, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getCustomerDemo(), ArmsAgentConstants.Communication.CERO).toString(), 6), 6, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(ArmsAgentConstants.Communication.CERO + ArmsAgentConstants.Communication.CERO); //Reserved
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getSsNumberExt(), ArmsAgentConstants.Communication.CERO).toString(), 4), 4, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getSsNumber(), ArmsAgentConstants.Communication.CERO).toString(), 10), 10, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getGender(), ArmsAgentConstants.Communication.SPACE).toString(), 1), 1, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getIncome(), ArmsAgentConstants.Communication.SPACE).toString(), 1), 1, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			/**temporal comment**/
			//field = Util4690.pack(ObjectUtils.equals(((Customer)object).getBirthDate(), null) ? "000000" : (DateFormatUtils.format(((Customer)object).getBirthDate(), "yyMMdd")).toString());
			field = Util4690.pack("      ");
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(ObjectUtils.equals(((Customer)object).getCustomerData().getEnrollDate(), null) ? "000000" : (DateFormatUtils.format(((Customer)object).getCustomerData().getEnrollDate(), "yyMMdd")).toString());
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;			
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getFamiliSize(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getChildAge1(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getChildAge2(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getChildAge3(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getChildAge4(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getChildAge5(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getChildAge6(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getChildAge7(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getTotalAdjustCount(), ArmsAgentConstants.Communication.CERO).toString(), 4), 4, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getTotalReedemCont(), ArmsAgentConstants.Communication.CERO).toString(), 4), 4, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getTotalPointsAdjPlus(), ArmsAgentConstants.Communication.CERO).toString(), 8), 8, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getTotalPointsAdjMinus(), ArmsAgentConstants.Communication.CERO).toString(), 8), 8, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getLastAdjustFormNum(), ArmsAgentConstants.Communication.CERO).toString(), 6), 6, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getLastReedemFormNum(), ArmsAgentConstants.Communication.CERO).toString(), 6), 6, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(ObjectUtils.equals(((Customer)object).getCustomerData().getLastAdjustDate(), null) ? "000000" : (DateFormatUtils.format(((Customer)object).getCustomerData().getLastAdjustDate(), "yyMMdd")).toString());
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(ObjectUtils.equals(((Customer)object).getCustomerData().getLastReedemDate(), null) ? "000000" : (DateFormatUtils.format(((Customer)object).getCustomerData().getLastReedemDate(), "yyMMdd")).toString());
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(ObjectUtils.equals(((Customer)object).getCustomerData().getChangeDate(), null) ? "000000" : (DateFormatUtils.format(((Customer)object).getCustomerData().getChangeDate(), "yyMMdd")).toString());
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(ObjectUtils.equals(((Customer)object).getCustomerData().getLastRainCheckDate(), null) ? "000000" : (DateFormatUtils.format(((Customer)object).getCustomerData().getLastRainCheckDate(), "yyMMdd")).toString());
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getCustomerAuthFlag(), ArmsAgentConstants.Communication.SPACE).toString(), 1), 1, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getAliasFlag(), ArmsAgentConstants.Communication.SPACE).toString(), 1), 1, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getReferenceAccount(), ArmsAgentConstants.Communication.CERO).toString(), 18), 18, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(((Customer)object).getCustomerData().getLastRainCheckAmount(), ArmsAgentConstants.Communication.CERO).toString(), 6), 6, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = ArmsAgentConstants.Communication.SPACE.getBytes(); //State Extension
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getZipAlphanumeric(), ArmsAgentConstants.Communication.SPACE).toString(), 9), 9, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(StringUtils.left(ObjectUtils.defaultIfNull(((Customer)object).getDriverLicenseNumExt(), ArmsAgentConstants.Communication.SPACE).toString(), 5), 5, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = StringUtils.rightPad(ArmsAgentConstants.Communication.SPACE, 16).getBytes();
			System.arraycopy(field, 0, customerRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled()){
				log.trace("pos: " + pos);
				log.trace("customerRecord: " + new String(customerRecord));
			}
			numberBytesWritten =   keyedFileBean.getKeyedFile().write(customerRecord, KeyedFile.NO_UNLOCK, KeyedFile.NO_HOLD);
			
			if(numberBytesWritten == RecordSize)
				result = Boolean.TRUE;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	
}
