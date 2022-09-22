package com.allc.arms.agent.operations.customer;

import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.customer.Activity;
import com.allc.arms.utils.customer.Customer;
import com.allc.arms.utils.keyed.KeyedFileBean;
import com.allc.arms.utils.keyed.KeyedFileMethods;
import com.allc.arms.utils.keyed.Util4690;
import com.allc.properties.PropFile;
import com.ibm.OS4690.KeyedFile;

public class CustomerActivityKeyed {
	private Logger log = Logger.getLogger(CustomerActivityKeyed.class);
	private KeyedFileBean keyedFileBean = new KeyedFileBean();	
	private String customerActivityKeyedFileName;

	public Object init(PropFile properties){
		Boolean result = Boolean.FALSE;
		try {
			customerActivityKeyedFileName = (String)properties.getObject("customer.activity.keyedFile.name");
			keyedFileBean.setPathAndFileName(customerActivityKeyedFileName);
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
			keyedFileBean.setKeyLength(9);
			keyedFileBean.setRecordSize(101);
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
		Integer optionFlags = null;
		byte[] field;
		try {

			Activity activity = null;
			Iterator iterator = ((Customer)object).getActivities().iterator(); 
		    /**the break is placed temporarily, because for the moment the customer has just one campaign **/
		    while (iterator.hasNext()){
		    	activity = (Activity)iterator.next();
		    	break;
		    }
		    if(ObjectUtils.equals(activity, null)){
				log.error("Activity class related to Customer is null, cannot complete the customer update");
				return Boolean.FALSE;
			}
		    log.info(activity);
			int RecordSize = keyedFileBean.getKeyedFile().getRecordSize();

			byte[] activityRecord = new byte[RecordSize];
			if(log.isTraceEnabled())  
			   log.trace("RecordSize: " + RecordSize);
			
			field = Util4690.pack(StringUtils.right(StringUtils.leftPad( ((Customer)object).getCustomerId(), 18, ArmsAgentConstants.Communication.CERO),18)) ;
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getTotalPoints(), ArmsAgentConstants.Communication.CERO).toString(), 8), 8, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getTotalTrans(), ArmsAgentConstants.Communication.CERO).toString(), 4), 4, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getRedeemedPoints(), ArmsAgentConstants.Communication.CERO).toString(), 8), 8, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getAutoCouponAmount(), ArmsAgentConstants.Communication.CERO).toString(), 6), 6, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(ObjectUtils.equals(activity.getLastDate(), null) ? "000000" : (DateFormatUtils.format(activity.getLastDate(), "yyMMdd")).toString());
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getLastPoints(), ArmsAgentConstants.Communication.CERO).toString(), 6), 6, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);			

			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getStatusLevel(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getMessageNum(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);			
			
			//field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getOptionFlags(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(optionFlags, ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getDiscountGroupId(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getMultiplier(), ArmsAgentConstants.Communication.CERO).toString(), 2), 2, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);			
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getTargetedCouponIds(), ArmsAgentConstants.Communication.CERO).toString(), 20), 20, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getAltCustomerNum(), ArmsAgentConstants.Communication.CERO).toString(), 18), 18, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getPeriodStartDate(), ArmsAgentConstants.Communication.CERO).toString(), 4), 4, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);			
			
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getPeriodPoints(), ArmsAgentConstants.Communication.CERO).toString(), 8), 8, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getPeriodTransCount(), ArmsAgentConstants.Communication.CERO).toString(), 4), 4, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getPeriodRedeemPoints(), ArmsAgentConstants.Communication.CERO).toString(), 8), 8, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);
			
			field = Util4690.pack(ObjectUtils.equals(activity.getLastRedeemDate(), null) ? "000000" : (DateFormatUtils.format(activity.getLastRedeemDate(), "yyMMdd")).toString());
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);			

			field = Util4690.pack(StringUtils.leftPad(StringUtils.right(ObjectUtils.defaultIfNull(activity.getCumSalesTotal(), ArmsAgentConstants.Communication.CERO).toString(), 8), 8, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled())  
				log.trace("pos " + pos);			

			field = StringUtils.rightPad(ArmsAgentConstants.Communication.SPACE, 30).getBytes();
			System.arraycopy(field, 0, activityRecord, pos, field.length);
			pos += field.length;
			if(log.isTraceEnabled()){
				log.trace("pos: " + pos);
				log.trace("ActivityRecord: " + new String(activityRecord));
			}

			numberBytesWritten =   keyedFileBean.getKeyedFile().write(activityRecord, KeyedFile.NO_UNLOCK, KeyedFile.NO_HOLD);

			if(numberBytesWritten == RecordSize)
				result = Boolean.TRUE;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	
	}
	
	
	
}
