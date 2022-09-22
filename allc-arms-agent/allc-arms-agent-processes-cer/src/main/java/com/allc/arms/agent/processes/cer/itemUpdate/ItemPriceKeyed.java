package com.allc.arms.agent.processes.cer.itemUpdate;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.keyed.KeyedFileBean;
import com.allc.arms.utils.keyed.KeyedFileMethods;
import com.allc.arms.utils.keyed.Util4690;
import com.ibm.OS4690.KeyedFile;

public class ItemPriceKeyed {
	public static final String ACTION_ADD = "A";
	public static final String ACTION_DELETE = "B";
	public static final String ACTION_UPDATE = "M";
	static Logger log = Logger.getLogger(ItemPriceKeyed.class);
	static KeyedFileBean keyedFileBean = new KeyedFileBean();
	protected Map stringToInt;

	public Object init(String file) {
		Boolean result = Boolean.FALSE;
		try {
			loadHashCharToInt();
			keyedFileBean.setPathAndFileName(file);
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
			keyedFileBean.setKeyLength(7);
			keyedFileBean.setRecordSize(508);
			if (KeyedFileMethods.openFile(keyedFileBean))
				result = Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public Object initACE(String file) {
		Boolean result = Boolean.FALSE;
		try {
			loadHashCharToInt();
			keyedFileBean.setPathAndFileName(file);
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
			keyedFileBean.setKeyLength(6);
			keyedFileBean.setRecordSize(169);
			if (KeyedFileMethods.openFile(keyedFileBean))
				result = Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public Object initSuperMarket(String file) {
		Boolean result = Boolean.FALSE;
		try {
			loadHashCharToInt();
			keyedFileBean.setPathAndFileName(file);
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
			keyedFileBean.setKeyLength(6);
			keyedFileBean.setRecordSize(46);
			if (KeyedFileMethods.openFile(keyedFileBean))
				result = Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	private void loadHashCharToInt() {
		stringToInt = new HashMap();
		stringToInt.put("A", "10");
		stringToInt.put("B", "11");
		stringToInt.put("C", "12");
		stringToInt.put("D", "13");
		stringToInt.put("E", "14");
		stringToInt.put("F", "15");
		stringToInt.put("G", "16");
		stringToInt.put("H", "17");
		stringToInt.put("I", "18");
		stringToInt.put("J", "19");
		stringToInt.put("K", "20");
		stringToInt.put("L", "21");
		stringToInt.put("M", "22");
		stringToInt.put("N", "23");
		stringToInt.put("O", "24");
		stringToInt.put("P", "25");
		stringToInt.put("Q", "26");
		stringToInt.put("R", "27");
		stringToInt.put("S", "28");
		stringToInt.put("T", "29");
		stringToInt.put("U", "30");
		stringToInt.put("V", "31");
		stringToInt.put("W", "32");
		stringToInt.put("X", "33");
		stringToInt.put("Y", "34");
		stringToInt.put("Z", "35");
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

	public Object writeItem(String itemData, String tipoImp) {
		Boolean result = Boolean.FALSE;
		int pos = 0;
		int numberBytesWritten = 0;
		byte[] field;
		try {
			byte[] itemRecord = new byte[keyedFileBean.getKeyedFile().getRecordSize()];
			for (int i = 0; i < itemRecord.length; i++) {
				itemRecord[i] = 0x00;
			}
			if (ACTION_DELETE.equalsIgnoreCase(itemData.substring(0, 1))) {
				// log.debug("Borrando item: "+itemData.substring(1, 13));
				keyedFileBean.getKeyedFile().delete(Util4690.pack(
						StringUtils.leftPad(itemData.substring(1, 13), 14, ArmsAgentConstants.Communication.CERO)));
			} else {
				// log.debug("Actualizando item: "+itemData.substring(1, 13));
				// item code
				field = Util4690.pack(
						StringUtils.leftPad(itemData.substring(1, 13), 14, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// indicat0
				field = processIndicat0(itemData);
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// indicat1
				field = processIndicat1(itemData);
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// indicat1A
				field = processIndicat1A(itemData);
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// union
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// deparment
				field = Util4690.pack(
						StringUtils.leftPad(itemData.substring(31, 35), 4, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// families
				field = Util4690
						.pack(StringUtils
								.rightPad(
										StringUtils.leftPad(itemData.substring(35, 38), 3,
												ArmsAgentConstants.Communication.CERO).toString(),
										6, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, pos, 3);
				pos += field.length;
				// log.info("pos:"+pos);
				// mpgroup
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// unionpinfo
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// quantity
				field = Util4690.pack(itemData.substring(46, 48));
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// sale price
				field = Util4690.pack(
						StringUtils.leftPad(itemData.substring(38, 46), 10, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// linked to
				field = Util4690.pack("0000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// item name
				field = StringUtils.rightPad(itemData.substring(13, 31), 18).getBytes("UTF-8");
				System.arraycopy(field, 0, itemRecord, pos, 18);
				pos += 18;
				// log.info("pos:"+pos);
				// user exit 1
				field = "00".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// user exit 2
				field = "00".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// restricted sale type
				// field = "0".getBytes();
				field = new byte[1];
				field[0] = (new Integer(itemData.substring(250, 252))).byteValue();
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// reporting code
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// union indicator 5
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// commodity
				field = Util4690.pack("0000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// subcommodity
				field = Util4690.pack("000000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// msinumber
				field = Util4690.pack("00000000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// cost
				field = "0000".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// lqd limit qty
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// lqd deal qty
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// union host data (9 pd + 1 int)
				field = Util4690.pack("000000000000000000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length + 1;
				// log.info("pos:"+pos);
				// struct com price data
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length + 8;
				field = Util4690.pack("000000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// alternate discount by dept
				field = Util4690.pack("0000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// vat code
				if (tipoImp != null && tipoImp.equalsIgnoreCase("I"))
					field = processImpuestos(itemData);
				else {
					field = new byte[1];
					field[0] = (new Integer(0)).byteValue();

				}
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// union host price change log
				field = Util4690.pack("0000000000000000000000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// secondary points club
				field = Util4690.pack("000000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// large linket to
				field = Util4690.pack("00000000000000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// value card type
				if (itemData.substring(248, 250).equals("03")) {
					field = new byte[1];
					field[0] = (new Integer(61)).byteValue();
				} else {
					field = new byte[1];
					field[0] = (new Integer(0)).byteValue();

				}
				//log.info("pos:"+pos);
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				//field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// coupon level
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// coupon copay
				field = "00".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// eastype
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// reserved4 y pricing method 2
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// sale quantity 2
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// unioprice 2
				field = Util4690.pack("0000000000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// department 2
				field = Util4690.pack("0000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// mpgroup 2
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// lqd limit qty 2
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// lqd deal qty 2
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// scunion0
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// item subtype
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// union indicator 4
				field = "0000".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// ifps code
				field = Util4690.pack("000000");
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// tender restriction group
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// reserved 5
				if (itemRecord.length == 169) {
					field = StringUtils.leftPad("0", 30, ArmsAgentConstants.Communication.CERO).getBytes();
					System.arraycopy(field, 0, itemRecord, pos, field.length);
				} else {
					field = StringUtils.leftPad("0", 223, ArmsAgentConstants.Communication.CERO).getBytes();
					System.arraycopy(field, 0, itemRecord, pos, field.length);
					pos += field.length;
					// completamos el resto con 0
					// byte[] relleno = new byte[itemRecord.length-pos-145];
					// for(int i=0; i < relleno.length; i++){
					// relleno[i]=0x00;
					// }
					// field = relleno;
					// System.arraycopy(field, 0, itemRecord, pos,
					// field.length);
					// pos += field.length;
					// log.info("offset: "+pos);
					// log.info("Procesando fields del rosado");
					// EL Rosado fields
					field = Util4690.pack(StringUtils.leftPad(itemData.substring(248, 250), 2,
							ArmsAgentConstants.Communication.CERO));
					System.arraycopy(field, 0, itemRecord, pos, field.length);
					pos += field.length;
					// SAP code
					field = Util4690.pack(
							StringUtils.leftPad(itemData.substring(68, 86), 18, ArmsAgentConstants.Communication.CERO));
					System.arraycopy(field, 0, itemRecord, pos, field.length);
					pos += field.length;
					// log.info("pos:"+pos);
					// descripcion larga
					field = StringUtils.rightPad(itemData.substring(86, 126), 40).getBytes("UTF-8");
					System.arraycopy(field, 0, itemRecord, pos, 40);
					pos += 40;
					// log.info("pos:"+pos);
					// presentacion
					field = StringUtils.rightPad(itemData.substring(126, 136), 10).getBytes("UTF-8");
					System.arraycopy(field, 0, itemRecord, pos, 10);
					pos += 10;
					// log.info("pos:"+pos);
					// jerarquia
					field = StringUtils.rightPad(itemData.substring(136, 146), 10).getBytes("UTF-8");
					System.arraycopy(field, 0, itemRecord, pos, 10);
					pos += 10;
					// log.info("pos:"+pos);
					// proveedor
					field = Util4690.pack(
							StringUtils.leftPad(itemData.substring(146, 164), 18, ArmsAgentConstants.Communication.CERO));
					System.arraycopy(field, 0, itemRecord, pos, 9);
					pos += 9;
					// log.info("pos:"+pos);
					// fecha novedad
					field = Util4690.pack(StringUtils.leftPad(itemData.substring(164, 172), 8,
							ArmsAgentConstants.Communication.CERO));
					System.arraycopy(field, 0, itemRecord, pos, field.length);
					pos += field.length;
					// log.info("pos:"+pos);
					// fecha aplicacion
					field = Util4690.pack(StringUtils.leftPad(itemData.substring(172, 180), 8,
							ArmsAgentConstants.Communication.CERO));
					System.arraycopy(field, 0, itemRecord, pos, field.length);
					pos += field.length;
					// log.info("pos:"+pos);
					// deducible
					field = Util4690.pack(StringUtils.leftPad(itemData.substring(180, 183), 4,
							ArmsAgentConstants.Communication.CERO));
					System.arraycopy(field, 0, itemRecord, pos, field.length);
					pos += field.length;
					// log.info("pos:"+pos);
					// referencia
					field = StringUtils.rightPad(itemData.substring(183, 198), 15).getBytes("UTF-8");
					System.arraycopy(field, 0, itemRecord, pos, 15);
					pos += 15;
					// log.info("pos:"+pos);
					// color
					field = StringUtils.rightPad(itemData.substring(198, 208), 10).getBytes("UTF-8");
					System.arraycopy(field, 0, itemRecord, pos, 10);
					pos += 10;
					// log.info("pos:"+pos);
					// medida
					field = StringUtils.rightPad(itemData.substring(208, 212), 4).getBytes("UTF-8");
					System.arraycopy(field, 0, itemRecord, pos, 4);
					pos += 4;
					// log.info("pos:"+pos);
					// disenio
					field = StringUtils.rightPad(itemData.substring(212, 216), 4).getBytes("UTF-8");
					System.arraycopy(field, 0, itemRecord, pos, 4);
					pos += 4;
					// log.info("pos:"+pos);
					// marca
					field = StringUtils.rightPad(itemData.substring(216, 231), 15).getBytes("UTF-8");
					System.arraycopy(field, 0, itemRecord, pos, 15);
					pos += 15;
					// log.info("pos:"+pos);
					// unidades
					field = Util4690.pack(StringUtils.leftPad(itemData.substring(231, 237), 6,
							ArmsAgentConstants.Communication.CERO));
					System.arraycopy(field, 0, itemRecord, pos, field.length);
					pos += field.length;
					// log.info("pos:"+pos);
					// precio caja
					field = Util4690.pack(StringUtils.leftPad(itemData.substring(237, 247), 10,
							ArmsAgentConstants.Communication.CERO));
					System.arraycopy(field, 0, itemRecord, pos, field.length);
					pos += field.length;
					// log.info("pos:"+pos);
					// indicador venta por caja
					field = StringUtils.rightPad(itemData.substring(247, 248).equalsIgnoreCase("Y") ? "1" : "0", 1).getBytes("UTF-8");
					System.arraycopy(field, 0, itemRecord, pos, 1);
					pos += 1;
					// log.info("pos:"+pos);

				}
				// if(log.isTraceEnabled()){
				// log.trace("itemRecord: " + new String(itemRecord)+" size:
				// "+itemRecord.length);
				// }

				numberBytesWritten = keyedFileBean.getKeyedFile().write(itemRecord, KeyedFile.NO_UNLOCK,
						KeyedFile.NO_HOLD);

				if (numberBytesWritten == keyedFileBean.getKeyedFile().getRecordSize())
					result = Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public Object writeItemACE(String itemData, String tipoImp) {
		Boolean result = Boolean.FALSE;
		int pos = 0;
		int numberBytesWritten = 0;
		byte[] field;
		try {
			byte[] itemRecord = new byte[keyedFileBean.getKeyedFile().getRecordSize()];
			for (int i = 0; i < itemRecord.length; i++) {
				itemRecord[i] = 0x00;
			}
			if (ACTION_DELETE.equalsIgnoreCase(itemData.substring(0, 1))) {
				// log.debug("Borrando item: "+itemData.substring(1, 13));
				keyedFileBean.getKeyedFile().delete(Util4690.pack(
						StringUtils.leftPad(itemData.substring(1, 13), 12, ArmsAgentConstants.Communication.CERO)));
			} else {
				// log.debug("Actualizando item: "+itemData.substring(1, 13));
				// TODO REVISAR QUE CAMPOS DEBEMOS TRUNCAR (IZQ O DER)
				// item code
				field = Util4690.pack(
						StringUtils.leftPad(itemData.substring(1, 13), 12, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, 0, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// indicat0
				field = processIndicat0(itemData);
				System.arraycopy(field, 0, itemRecord, 6, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// indicat1
				field = processIndicat1(itemData);
				System.arraycopy(field, 0, itemRecord, 7, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// indicat1A
				field = processIndicat1A(itemData);
				System.arraycopy(field, 0, itemRecord, 8, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// union
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 9, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// deparment
				field = Util4690.pack(
						StringUtils.leftPad(itemData.substring(31, 35), 4, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, 10, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// families
				field = Util4690.pack(
						StringUtils.rightPad(itemData.substring(35, 38), 6, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, 12, 3);
				pos += field.length;
				// log.info("pos:"+pos);
				// mpgroup
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 15, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// unionpinfo
				// quantity
				field = Util4690.pack(itemData.substring(46, 48));
				System.arraycopy(field, 0, itemRecord, 16, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// sale price
				field = Util4690.pack(
						StringUtils.leftPad(itemData.substring(38, 46), 10, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, 17, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// linked to
				field = Util4690.pack("0000");
				System.arraycopy(field, 0, itemRecord, 22, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// item name
				field = StringUtils.rightPad(itemData.substring(13, 31), 18).getBytes("UTF-8");
				System.arraycopy(field, 0, itemRecord, 24, 18);
				pos += 18;
				// log.info("pos:"+pos);
				// user exit 1
				field = "00".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// user exit 2
				field = "00".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;

				// TODO HASTA ACA LLEGA EN SUPERMARKET

				// log.info("pos:"+pos);
				// restricted sale type
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// reporting code
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 47, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// union indicator 5
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// commodity
				field = Util4690.pack("0000");
				System.arraycopy(field, 0, itemRecord, 49, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// subcommodity
				field = Util4690.pack("000000");
				System.arraycopy(field, 0, itemRecord, 51, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// msinumber
				field = Util4690.pack("00000000");
				System.arraycopy(field, 0, itemRecord, 54, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// cost
				field = "0000".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// lqd limit qty
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 62, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// lqd deal qty
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 63, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// host quantity
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 64, field.length);
				pos += field.length;
				// union host data
				field = Util4690.pack("0000000000");
				System.arraycopy(field, 0, itemRecord, 65, field.length);
				pos += field.length;
				// host LQD Limit
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 70, field.length);
				pos += field.length;
				// host LQD Deal
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 71, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// struct com price data
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 72, field.length);
				pos += field.length + 10;
				// price date
				field = Util4690.pack("000000");
				System.arraycopy(field, 0, itemRecord, 83, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// alternate discount by dept
				field = Util4690.pack("0000");
				System.arraycopy(field, 0, itemRecord, 86, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// vat code
				if (tipoImp != null && tipoImp.equalsIgnoreCase("I"))
					field = processImpuestos(itemData);
				else {
					field = new byte[1];
					field[0] = (new Integer(0)).byteValue();

				}
				System.arraycopy(field, 0, itemRecord, 88, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// union host price change log
				field = Util4690.pack("0000000000000000000000");
				System.arraycopy(field, 0, itemRecord, 89, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// secondary points club
				field = Util4690.pack("000000");
				System.arraycopy(field, 0, itemRecord, 100, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// large linket to
				field = Util4690.pack("000000000000");
				System.arraycopy(field, 0, itemRecord, 103, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// value card type
				if (itemData.substring(248, 250).equals("03")) {
					field = new byte[1];
					field[0] = (new Integer(61)).byteValue();
				} else {
					field = new byte[1];
					field[0] = (new Integer(0)).byteValue();

				}
				log.info("pos:"+pos);
				System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// coupon level
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 110, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// coupon copay
				field = "00".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// eastype
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// reserved4 y pricing method 2
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 114, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// sale quantity 2
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 115, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// unioprice 2
				field = Util4690.pack("0000000000");
				System.arraycopy(field, 0, itemRecord, 116, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// department 2
				field = Util4690.pack("0000");
				System.arraycopy(field, 0, itemRecord, 121, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// mpgroup 2
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 123, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// lqd limit qty 2
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 124, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// lqd deal qty 2
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 125, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// scunion0
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// item subtype
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// union indicator 4
				field = "0000".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// ifps code
				field = Util4690.pack("000000");
				System.arraycopy(field, 0, itemRecord, 132, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// tender restriction group
				field = "0".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// reserved 5
				field = StringUtils.leftPad("0", 33, ArmsAgentConstants.Communication.CERO).getBytes();
				System.arraycopy(field, 0, itemRecord, 136, field.length);
				pos += field.length;

				// if(log.isTraceEnabled()){
				// log.trace("itemRecord: " + new String(itemRecord)+" size:
				// "+itemRecord.length);
				// }
				numberBytesWritten = keyedFileBean.getKeyedFile().write(itemRecord, KeyedFile.NO_UNLOCK,
						KeyedFile.NO_HOLD);

				if (numberBytesWritten == keyedFileBean.getKeyedFile().getRecordSize())
					result = Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public Object writeItemSuperMarket(String itemData, String tipoImp) {
		Boolean result = Boolean.FALSE;
		int pos = 0;
		int numberBytesWritten = 0;
		byte[] field;
		try {
			byte[] itemRecord = new byte[keyedFileBean.getKeyedFile().getRecordSize()];
			for (int i = 0; i < itemRecord.length; i++) {
				itemRecord[i] = 0x00;
			}
			if (ACTION_DELETE.equalsIgnoreCase(itemData.substring(0, 1))) {
				// log.debug("Borrando item: "+itemData.substring(1, 13));
				keyedFileBean.getKeyedFile().delete(Util4690.pack(
						StringUtils.leftPad(itemData.substring(1, 13), 12, ArmsAgentConstants.Communication.CERO)));
			} else {
				// log.debug("Actualizando item: "+itemData.substring(1, 13));
				// item code
				field = Util4690.pack(
						StringUtils.leftPad(itemData.substring(1, 13), 12, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, 0, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// indicat0
				field = processIndicat0(itemData);
				System.arraycopy(field, 0, itemRecord, 6, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// indicat1
				field = processIndicat1(itemData);
				System.arraycopy(field, 0, itemRecord, 7, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// indicat1A
				field = processIndicat1A(itemData);
				System.arraycopy(field, 0, itemRecord, 8, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// union
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 9, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// deparment
				field = Util4690.pack(
						StringUtils.leftPad(itemData.substring(31, 35), 4, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, 10, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// families
				field = Util4690.pack(
						StringUtils.rightPad(itemData.substring(35, 38), 6, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, 12, 3);
				pos += field.length;
				// log.info("pos:"+pos);
				// mpgroup
				field = Util4690.pack("00");
				System.arraycopy(field, 0, itemRecord, 15, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// unionpinfo
				// quantity
				field = Util4690.pack(itemData.substring(46, 48));
				System.arraycopy(field, 0, itemRecord, 16, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// sale price
				field = Util4690.pack(
						StringUtils.leftPad(itemData.substring(38, 46), 10, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(field, 0, itemRecord, 17, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// linked to
				field = Util4690.pack("0000");
				System.arraycopy(field, 0, itemRecord, 22, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// item name
				field = StringUtils.rightPad(itemData.substring(13, 31), 18).getBytes("UTF-8");
				System.arraycopy(field, 0, itemRecord, 24, 18);
				pos += 18;
				// log.info("pos:"+pos);
				// user exit 1
				field = "00".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;
				// log.info("pos:"+pos);
				// user exit 2
				field = "00".getBytes();
				// System.arraycopy(field, 0, itemRecord, pos, field.length);
				pos += field.length;

				numberBytesWritten = keyedFileBean.getKeyedFile().write(itemRecord, KeyedFile.NO_UNLOCK,
						KeyedFile.NO_HOLD);

				if (numberBytesWritten == keyedFileBean.getKeyedFile().getRecordSize())
					result = Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	private byte[] processIndicat0(String data) {
		byte byteValue = 0x00;
		// keep item movement
		byteValue = updateFlag(byteValue, reverse(data.substring(48, 49)), 0x01);
		// weight price required
		byteValue = updateFlag(byteValue, data.substring(49, 50), 0x40);
		// price required
		byteValue = updateFlag(byteValue, data.substring(50, 51), 0x10);
		// exception log item sale
		byteValue = updateFlag(byteValue, data.substring(54, 55), 0x08);
		// quantity allowed
		byteValue = updateFlag(byteValue, reverse(data.substring(55, 56)), 0x80);
		// quantity required
		byteValue = updateFlag(byteValue, data.substring(56, 57), 0x20);
		// authorized for sale
		byteValue = updateFlag(byteValue, reverse(data.substring(57, 58)), 0x04);
		// restricted for sale
		byteValue = updateFlag(byteValue, data.substring(62, 63), 0x02);
		byte[] retorno = new byte[1];
		retorno[0] = byteValue;
		return retorno;
	}

	private byte[] processIndicat1(String data) {
		byte byteValue = 0x00;
		// food stamp item
		byteValue = updateFlag(byteValue, data.substring(51, 52), 0x08);
		// discountable item
		byteValue = updateFlag(byteValue, reverse(data.substring(52, 53)), 0x02);
		// coupon multiplication
		byteValue = updateFlag(byteValue, reverse(data.substring(53, 54)), 0x01);
		// points apply to item
		byteValue = updateFlag(byteValue, data.substring(60, 61), 0x04);
		// byteValue = updateFlag(byteValue, "Y", 0x04);
		// tax A
		byteValue = updateFlag(byteValue, data.substring(64, 65), 0x80);
		// tax B
		byteValue = updateFlag(byteValue, data.substring(65, 66), 0x40);
		// tax C
		byteValue = updateFlag(byteValue, data.substring(66, 67), 0x20);
		// tax D
		byteValue = updateFlag(byteValue, data.substring(67, 68), 0x10);
		byte[] retorno = new byte[1];
		retorno[0] = byteValue;
		return retorno;
	}

	private byte[] processImpuestos(String data) {
		Integer vat = new Integer(0);
		if (data.substring(64, 65).equalsIgnoreCase("Y"))
			// tax A
			vat = new Integer(1);
		if (data.substring(65, 66).equalsIgnoreCase("Y"))
			// tax B
			vat = new Integer(2);
		if (data.substring(66, 67).equalsIgnoreCase("Y"))
			// tax C
			vat = new Integer(3);
		if (data.substring(67, 68).equalsIgnoreCase("Y"))
			// tax D
			vat = new Integer(4);
		byte[] retorno = new byte[1];
		retorno[0] = vat.byteValue();
		return retorno;
	}

	private byte[] processIndicat1A(String data) {
		byte byteValue = 0x00;
		// log to change file
		byteValue = updateFlag(byteValue, data.substring(58, 59), 0x80);
		// point only item coupon
		byteValue = updateFlag(byteValue, data.substring(59, 60), 0x20);
		// item links to deposit
		byteValue = updateFlag(byteValue, data.substring(61, 62), 0x10);
		// fuel volume required
		byteValue = updateFlag(byteValue, data.substring(63, 64), 0x40);
		byte[] retorno = new byte[1];
		retorno[0] = byteValue;
		return retorno;
	}

	private String reverse(final String value) {
		String res = "Y";
		if (value.equalsIgnoreCase("Y")) {
			res = "N";
		}
		return res;
	}

	/**
	 * Update the bit with the <code>maskValue</code> value inside the
	 * <code>byteValue</code> group, turn the bit on if the
	 * <code>strValue</code> is YES or turn it off otherwise.
	 * 
	 * @param byteValue
	 * @param strValue
	 * @param maskValue
	 * @return the updated byte
	 */
	protected byte updateFlag(byte byteValue, String strValue, int maskValue) {
		byte mask = (byte) (maskValue);
		// para activarlo |
		// para negarlo &not
		if (strValue.equals("Y"))
			return (byte) (byteValue | mask);
		else
			return byteValue;

	}

}
