/**
 * 
 */
package com.allc.util;

import java.text.SimpleDateFormat;

/**
 * @author GUSTAVOK
 *
 */
public final class ConstantsUtil {

	public static final String IXRETAIL_DATE_FORMAT = "yyyy-MM-dd";

    public static final String IXRETAIL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
  
    public static final String TLOG_SUFIX_DATE_TIME_FORMAT = "yyyyMMddHHmmss";
    
    public static final String DDMMYYYY_SUFIX_DATE_TIME_FORMAT = "ddMMyyyy";
    
    public static class Formatters {
    	
    	public static final	SimpleDateFormat IXRETAIL_DATE_FORMATTER = new SimpleDateFormat(ConstantsUtil.IXRETAIL_DATE_FORMAT);
    	
    	public static final	SimpleDateFormat IXRETAIL_DATE_TIME_FORMATTER = new SimpleDateFormat(ConstantsUtil.IXRETAIL_DATE_TIME_FORMAT);
    	
    	public static final	SimpleDateFormat TLOG_SUFIX_DATE_TIME_FORMATTER = new SimpleDateFormat(ConstantsUtil.TLOG_SUFIX_DATE_TIME_FORMAT);
    	
    	public static final	SimpleDateFormat DDMMYYYY_SUFIX_DATE_TIME_FORMATTER = new SimpleDateFormat(ConstantsUtil.DDMMYYYY_SUFIX_DATE_TIME_FORMAT);
    }
}
