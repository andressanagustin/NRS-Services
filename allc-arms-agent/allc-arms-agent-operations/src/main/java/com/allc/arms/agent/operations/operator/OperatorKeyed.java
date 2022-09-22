package com.allc.arms.agent.operations.operator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.keyed.KeyedFileBean;
import com.allc.arms.utils.keyed.KeyedFileMethods;
import com.allc.arms.utils.keyed.Util4690;
import com.allc.arms.utils.operator.OperatorWrapper;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.KeyedFile;

public class OperatorKeyed {

	protected static Logger log = Logger.getLogger(OperatorKeyed.class);
	protected KeyedFileBean keyedFileBean = new KeyedFileBean();
	protected PropFile properties;
	protected String operatorKeyedFileName;

	public boolean init(PropFile properties) {
		boolean result = false;
		try {
			this.properties = properties;
			operatorKeyedFileName = (String) properties.getObject("operator.keyedFile.name");
			keyedFileBean.setPathAndFileName(operatorKeyedFileName);
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
			keyedFileBean.setKeyLength(5);
			keyedFileBean.setRecordSize(properties.getInt("operator.keyedFile.record.length"));
			if (KeyedFileMethods.openFile(keyedFileBean))
				result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public boolean closure() {
		boolean result = false;
		try {
			KeyedFileMethods.closeFile(keyedFileBean);
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;

	}

	public List readOperator(String documento) {
		try {
			if (keyedFileBean.getKeyedFile() == null)
				return null;
			int recordSize = keyedFileBean.getKeyedFile().getRecordSize();
			byte[] operatorRecord = new byte[recordSize];
			byte[] key = Util4690.pack(StringUtils.leftPad(documento, 10, ArmsAgentConstants.Communication.CERO));
			System.arraycopy(key, 0, operatorRecord, 0, key.length);
			keyedFileBean.getKeyedFile().read(operatorRecord, 1);
			String reg = new String(operatorRecord);
			log.debug("Registro leido: " + Util4690.unpack(operatorRecord, 0, 5) + reg.substring(5));
			List values = new ArrayList();
			values.add(reg.substring(5, 9));
			values.add(Util4690.unpack(reg.substring(29, 32)));
			for (int i = 0; i < values.size(); i++) {
				log.debug("REG leida: " + values.get(i));
			}
			return values;
		} catch (Exception e) {
			log.error("Error al leer el archivo " + keyedFileBean.getPathAndFileName(), e);
		}
		return null;
	}
	
	public String readOperatorData(String code) {
		try {
			if (keyedFileBean.getKeyedFile() == null)
				return null;
			int recordSize = keyedFileBean.getKeyedFile().getRecordSize();
			byte[] operatorRecord = new byte[recordSize];
			byte[] key = Util4690.pack(StringUtils.leftPad(code, 10, ArmsAgentConstants.Communication.CERO));
			log.info("Codigo a leer: " + code);
			System.arraycopy(key, 0, operatorRecord, 0, key.length);
			keyedFileBean.getKeyedFile().read(operatorRecord, 1);
			log.info("Operador Leido! " + code);
			String reg = new String(operatorRecord);
			String operaData;
			
			// Indicat0
			byte[] i01 = new byte[2];
			i01[0] = Util.shortToBytes((short)0)[0];
			i01[1] = reg.substring(10, 11).getBytes()[0];
			byte[] i02 = new byte[2];
			i02[0] = Util.shortToBytes((short)0)[0];
			i02[1] = reg.substring(11, 12).getBytes()[0];
			String indicat01 = Integer.toBinaryString(Short.valueOf(Util.bytesToShort(i01)).intValue());
			String indicat02 = Integer.toBinaryString(Short.valueOf(Util.bytesToShort(i02)).intValue());
			while(indicat01.length() < 8){
				indicat01 = "0" + indicat01;
			}
			while(indicat02.length() < 8){
				indicat02 = "0" + indicat02;
			}
			log.info("indicat0:"+indicat01.substring(0, 8)+indicat02.substring(0, 8));
			String indicat0 = (new StringBuffer(indicat01.substring(0, 8))).reverse().toString() + (new StringBuffer(indicat02.substring(0, 8))).reverse().toString();
			
			// Indicat1
			byte[] i11 = new byte[2];
			i11[0] = Util.shortToBytes((short)0)[0];
			i11[1] = reg.substring(12, 13).getBytes()[0];
			byte[] i12 = new byte[2];
			i12[0] = Util.shortToBytes((short)0)[0];
			i12[1] = reg.substring(13, 14).getBytes()[0];
			String indicat11 = Integer.toBinaryString(Short.valueOf(Util.bytesToShort(i11)).intValue());
			String indicat12 = Integer.toBinaryString(Short.valueOf(Util.bytesToShort(i12)).intValue());
			while(indicat11.length() < 8){
				indicat11 = "0" + indicat11;
			}
			while(indicat12.length() < 8){
				indicat12 = "0" + indicat12;
			}
			log.info("indicat1:"+indicat11.substring(0, 8)+indicat12.substring(0, 8));
			String indicat1 = (new StringBuffer(indicat11.substring(0, 8))).reverse().toString() + (new StringBuffer(indicat12.substring(0, 8))).reverse().toString();
			
			// Indicat >= 2
			String indicats = "";
			for(int i = 14; i < 32; i++){
				byte[] b = new byte[2];
				b[0] = Util.shortToBytes((short)0)[0];
				b[1] = reg.substring(i, i+1).getBytes()[0];
				String indicat = Integer.toBinaryString(Short.valueOf(Util.bytesToShort(b)).intValue());
				while(indicat.length() < 8){
					indicat = "0" + indicat;
				}
				log.info("Indicat: "+indicat);
				indicat = (new StringBuffer(indicat.substring(0, 8))).reverse().toString();
				indicats += indicat;
			}
			//operaData = Util4690.unpack(operatorRecord, 0, 5) + Util4690.unpack(operatorRecord, 5, 4) + Util4690.unpack(operatorRecord, 9, 1) + Util4690.unpack(operatorRecord, 10, 5) 
			//		+ indicat0 + indicat1 + indicats + reg.substring(32);
                        String indOpe = (indicat01.substring(0, 8) + indicat02.substring(0, 8)).equals("0000000000000000") && Util4690.unpack(operatorRecord, 9, 1).equals("01") ? "00" : Util4690.unpack(operatorRecord, 9, 1);
                        operaData = Util4690.unpack(operatorRecord, 0, 5) + Util4690.unpack(operatorRecord, 5, 4) + indOpe
                            + indicat0 + indicat1 + indicats + reg.substring(32);
                        log.info("Parete 1 " + Util4690.unpack(operatorRecord, 0, 5));
                        log.info("Parete 2 " + Util4690.unpack(operatorRecord, 5, 4));
                        log.info("Parete 3 " + Util4690.unpack(operatorRecord, 9, 1));
                        log.info("Parete 3-1 " + indOpe);
                        
			log.info("Registro leido: " + operaData);
			return operaData;
		} catch (Exception e) {
			if(e instanceof FlexosException)
				log.info("Error Code:"+((FlexosException)e).getReturnCode());
			log.error("Error al leer el archivo " + keyedFileBean.getPathAndFileName(), e);
		}
		return null;
	}

	public Object process(Object object) {
		if(properties.getInt("operator.keyedFile.record.length") == 72)
			return processSupermarket(object);
		else
			return processACE(object);
	}
	
	public Object processACE(Object object) {
		Boolean result = Boolean.FALSE;
		int pos = 0;
		int numberBytesWritten = 0;
		byte[] field;
		try {
			if (!(object instanceof OperatorWrapper)) {
				result = Boolean.FALSE;
			}

			int RecordSize = keyedFileBean.getKeyedFile().getRecordSize();

			byte[] operatorRecord = new byte[RecordSize];
			if (log.isTraceEnabled())
				log.trace("RecordSize: " + RecordSize);

			field = Util4690.pack(StringUtils.right(
					StringUtils.leftPad(((OperatorWrapper) object).getIdentityDocument(), 10, ArmsAgentConstants.Communication.CERO), 10));
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos += field.length;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);
			String passw = ((OperatorWrapper) object).getPassword();
			String passPacked = null;
			String passwordChangeDate = null;
			int ind14 = 0;
			if (0 != properties.getInt("operator.password.encripted.flag")) {
				if (passw == null || passw.length() == 0)
					passw = properties.getObject("operator.password.noEncripted");
				ind14 = properties.getInt("operator.password.encripted.indicat14");
			} else {
				if (passw == null || passw.length() == 0)
					passw = properties.getObject("operator.password.noEncripted");
				ind14 = properties.getInt("operator.password.noEncripted.indicat14");
			}
			log.info("Password:"+passw);
			if (((OperatorWrapper) object).getStatus().equals("4")) {
				List values = readOperator(((OperatorWrapper) object).getIdentityDocument());
				if (values != null && values.size() > 0) {
					passPacked = (String) values.get(0);
					passwordChangeDate = (String) values.get(1);
					log.info("Password packed:"+passw);
				}
			}

			if (passPacked != null)
				field = passPacked.getBytes();
			else
				field = Util4690.pack(StringUtils.right(StringUtils.leftPad(passw, 8, ArmsAgentConstants.Communication.CERO), 8));
			log.info("passw1:" + new String(field));
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos += field.length;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);
			field = Util4690.pack(StringUtils.right(StringUtils.leftPad(
					ObjectUtils.defaultIfNull(((OperatorWrapper) object).getNivelAut(), ArmsAgentConstants.Communication.CERO)
							.toString(), 2, ArmsAgentConstants.Communication.CERO), 2));
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos += field.length;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);

			String indicat;
			String indicatP1;
			String indicatP2;
			
			for (int i = 0; i < 2; i++) {
				indicatP1 = ((StringBuffer) ((OperatorWrapper) object).getIndicats().get(i)).substring(0, 8);
				StringBuffer indicatPart1 = new StringBuffer(indicatP1);
				indicatP2 = ((StringBuffer) ((OperatorWrapper) object).getIndicats().get(i)).substring(8, 16);
				StringBuffer indicatPart2 = new StringBuffer(indicatP2);
				//indicat = ((StringBuffer) ((OperatorWrapper) object).getIndicats().get(i)).reverse().toString();
				indicat = indicatPart1.reverse().toString() + indicatPart2.reverse().toString();
				log.info("indicat0: " + indicat);
				field = Util.shortToBytes((short) Util.binaryToDecimal(indicat));
				System.arraycopy(field, 0, operatorRecord, pos, field.length);
				pos += field.length;
				if (log.isTraceEnabled())
					log.trace("pos " + pos);
			}

			for (int i = 2; i < ((OperatorWrapper) object).getIndicats().size(); i++) {
				if (i == 14) {
					field = new byte[] { (byte) ind14 };
					indicat = (new Integer(ind14)).toString();
				} else {
					indicat = ((StringBuffer) ((OperatorWrapper) object).getIndicats().get(i)).reverse().toString();
					field = new byte[] { (byte) (int) Util.binaryToDecimal(indicat) };
				}
				log.info("indicat" + i + ": " + indicat + "field: " + field.toString());
				System.arraycopy(field, 0, operatorRecord, pos, field.length);
				pos += field.length;
				if (log.isTraceEnabled())
					log.trace("pos " + pos);

			}
//			log.info("PASS CHANGE DATE: "
//					+ (ObjectUtils.equals(passwordChangeDate, null) ? "000000" : StringUtils.right(
//							StringUtils.leftPad(passwordChangeDate, 6, ArmsAgentConstants.Communication.CERO), 6)));
			field = Util4690.pack(ObjectUtils.equals(passwordChangeDate, null) ? "000000" : StringUtils.right(
					StringUtils.leftPad(passwordChangeDate, 6, ArmsAgentConstants.Communication.CERO), 6));
//			log.info("PASS CHANGE DATE EMPAQUETADA: " + field);
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos += field.length;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);

			field = StringUtils.rightPad(
					StringUtils.left(ObjectUtils
							.defaultIfNull(((OperatorWrapper) object).getName(), ArmsAgentConstants.Communication.SPACE).toString(), 20),
					20, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos += field.length;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);

			field = StringUtils.rightPad(
					StringUtils.left(ObjectUtils.defaultIfNull("", ArmsAgentConstants.Communication.SPACE)
							.toString(), 20), 20, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos += field.length;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);
			//TODO HASTA ACA LLEGA SUPERMARKET
			field = Util4690.pack(ObjectUtils.equals(((OperatorWrapper) object).getOperatorBirthDate(), null) ? "00000000"
					: (DateFormatUtils.format(((OperatorWrapper) object).getOperatorBirthDate(), "yyyyMMdd")).toString());
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos += field.length;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);
			// PD 6 DNI
			field = Util4690.pack(StringUtils.rightPad(
					StringUtils.left(
							ObjectUtils.defaultIfNull(((OperatorWrapper) object).getIdentityDocument(),
									ArmsAgentConstants.Communication.SPACE).toString(), 12), 12, ArmsAgentConstants.Communication.SPACE)
					.toString());
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos += field.length;
			// PD 2 : CEROS
			field = Util4690.pack("0000");
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos += field.length;

			if (log.isTraceEnabled()) {
				log.trace("pos " + pos);
				log.trace("operatorRecord: " + new String(operatorRecord));
			}

			numberBytesWritten = keyedFileBean.getKeyedFile().write(operatorRecord, KeyedFile.NO_UNLOCK, KeyedFile.NO_HOLD);

			if (numberBytesWritten == RecordSize)
				result = Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	public Object processSupermarket(Object object) {
		Boolean result = Boolean.FALSE;
		int pos = 0;
		int numberBytesWritten = 0;
		byte[] field;
		try {
			if (!(object instanceof OperatorWrapper)) {
				result = Boolean.FALSE;
			}
			if (((OperatorWrapper) object).getSubscribe().equalsIgnoreCase("2")) {
				log.debug("Borrando Operador: "+((OperatorWrapper) object).getIdentityDocument());
				keyedFileBean.getKeyedFile().delete(Util4690.pack(StringUtils.right(
						StringUtils.leftPad(((OperatorWrapper) object).getIdentityDocument(), 10, ArmsAgentConstants.Communication.CERO), 10)));
				 return Boolean.TRUE;
			}
			int RecordSize = keyedFileBean.getKeyedFile().getRecordSize();

			byte[] operatorRecord = new byte[RecordSize];
			if (log.isTraceEnabled())
				log.trace("RecordSize: " + RecordSize);

			field = Util4690.pack(StringUtils.right(
					StringUtils.leftPad(((OperatorWrapper) object).getIdentityDocument(), 10, ArmsAgentConstants.Communication.CERO), 10));
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos += 5;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);
			String passw = ((OperatorWrapper) object).getPassword();
			String passPacked = null;
			String passwordChangeDate = null;
			int ind14 = 0;
			if (0 != properties.getInt("operator.password.encripted.flag")) {
				if (passw == null || passw.length() == 0)
					passw = properties.getObject("operator.password.noEncripted");
				ind14 = properties.getInt("operator.password.encripted.indicat14");
			} else {
				if (passw == null || passw.length() == 0)
					passw = properties.getObject("operator.password.noEncripted");
				ind14 = properties.getInt("operator.password.noEncripted.indicat14");
			}
			if (((OperatorWrapper) object).getStatus().equals("4")) {
				List values = readOperator(((OperatorWrapper) object).getIdentityDocument());
				if (values != null && values.size() > 0) {
					passPacked = (String) values.get(0);
					passwordChangeDate = (String) values.get(1);
				}
			}

			if (passPacked != null)
				field = passPacked.getBytes();
			else
				field = Util4690.pack(StringUtils.right(StringUtils.leftPad(passw, 8, ArmsAgentConstants.Communication.CERO), 8));
			log.info("passw1:" + field);
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos = 9;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);
			field = Util4690.pack(StringUtils.right(StringUtils.leftPad(
					ObjectUtils.defaultIfNull(((OperatorWrapper) object).getNivelAut(), ArmsAgentConstants.Communication.CERO)
							.toString(), 2, ArmsAgentConstants.Communication.CERO), 2));
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos = 10;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);


			String indicat;
			String indicatP1;
			String indicatP2;
			
			for (int i = 0; i < 2; i++) {
				indicatP1 = ((StringBuffer) ((OperatorWrapper) object).getIndicats().get(i)).substring(0, 8);
				StringBuffer indicatPart1 = new StringBuffer(indicatP1);
				indicatP2 = ((StringBuffer) ((OperatorWrapper) object).getIndicats().get(i)).substring(8, 16);
				StringBuffer indicatPart2 = new StringBuffer(indicatP2);
				//indicat = ((StringBuffer) ((OperatorWrapper) object).getIndicats().get(i)).reverse().toString();
				indicat = indicatPart1.reverse().toString() + indicatPart2.reverse().toString();
				log.info("indicat0: " + indicat);
				field = Util.shortToBytes((short) Util.binaryToDecimal(indicat));
				System.arraycopy(field, 0, operatorRecord, pos, field.length);
				pos += field.length;
				if (log.isTraceEnabled())
					log.trace("pos " + pos);
			}
			
//			String indicat;
//			for (int i = 0; i < 2; i++) {
//				indicat = ((StringBuffer) ((OperatorWrapper) object).getIndicats().get(i)).reverse().toString();
//				log.info("indicat0: " + indicat);
//				field = Util.shortToBytes((short) Util.binaryToDecimal(indicat));
//				System.arraycopy(field, 0, operatorRecord, pos, field.length);
//				pos += field.length;
//				if (log.isTraceEnabled())
//					log.trace("pos " + pos);
//			}

			for (int i = 2; i < ((OperatorWrapper) object).getIndicats().size(); i++) {
				/*if (i == 14) {
					field = new byte[] { (byte) ind14 };
					indicat = (new Integer(ind14)).toString();
				} else {*/
					indicat = ((StringBuffer) ((OperatorWrapper) object).getIndicats().get(i)).reverse().toString();
					field = new byte[] { (byte) (int) Util.binaryToDecimal(indicat) };
				//}
				log.info("indicat " + i + ": " + indicat + "field: " + field.toString());
				System.arraycopy(field, 0, operatorRecord, pos, field.length);
				pos += field.length;
				if (log.isTraceEnabled())
					log.trace("pos " + pos);

			}
			pos = 32;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);

			field = StringUtils.rightPad(
					StringUtils.left(ObjectUtils
							.defaultIfNull(((OperatorWrapper) object).getName(), ArmsAgentConstants.Communication.SPACE).toString(), 20),
					20, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos = 52;
			if (log.isTraceEnabled())
				log.trace("pos " + pos);

			field = StringUtils.rightPad(
					StringUtils.left(ObjectUtils.defaultIfNull("", ArmsAgentConstants.Communication.SPACE)
							.toString(), 20), 20, ArmsAgentConstants.Communication.SPACE).getBytes();
			System.arraycopy(field, 0, operatorRecord, pos, field.length);
			pos += field.length;
			
			if (log.isTraceEnabled()) {
				log.trace("pos " + pos);
				log.trace("operatorRecord: " + new String(operatorRecord));
			}

			numberBytesWritten = keyedFileBean.getKeyedFile().write(operatorRecord, KeyedFile.NO_UNLOCK, KeyedFile.NO_HOLD);

			if (numberBytesWritten == RecordSize)
				result = Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

}
