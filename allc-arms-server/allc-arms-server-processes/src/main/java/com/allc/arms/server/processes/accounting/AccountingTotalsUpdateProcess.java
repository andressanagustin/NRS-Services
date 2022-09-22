package com.allc.arms.server.processes.accounting;

import java.io.File;
import java.io.FileFilter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.accounting.OperTermRecordTotals;
import com.allc.arms.server.persistence.accounting.RecordTotalsDAO;
import com.allc.arms.server.persistence.accounting.StoreRecordTotals;
import com.allc.arms.server.persistence.accounting.TenderTotalsVarietyRecord;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.ControllerFiles;
import com.allc.properties.PropFile;

public class AccountingTotalsUpdateProcess extends AbstractProcess {

	protected Logger log = Logger.getLogger(AccountingTotalsUpdateProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	protected Session session = null;
	protected Session sessionSaadmin = null;
	protected boolean finished = false;
	private String storeCode;
	private File inFolder;
	private int sleepTime;
	private Transaction tx;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmm");
	private SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String fechaContable = null;
	private ArrayList<String> tenderRecordPend = new ArrayList<String>();
	private ArrayList<String> indivRecordPend = new ArrayList<String>();
	private RecordTotalsDAO storeRecordDAO = new RecordTotalsDAO();
	protected StoreDAO storeDAO = new StoreDAO();
	protected List pathActiveStore = new ArrayList();
	private int storeCodeHasta;
	private Iterator filesToProcess = null;

	protected void inicializar() {
		isEnd = false;
		try {
			storeCode = properties.getObject("eyes.store.code");
			while (storeCode.length() < 3)
				storeCode = "0" + storeCode;
			iniciarSesion("Saadmin");

			sleepTime = properties.getInt("accountingTotals.sleeptime");

			ParamsDAO paramsDAO = new ParamsDAO();
			ParamValue paravalue = null;

			if (Integer.valueOf(storeCode) == 0) {
				List tiendasActivas = storeDAO.getAllActiveStore(sessionSaadmin);
				if (tiendasActivas != null && !tiendasActivas.isEmpty()) {

					Iterator itStores = tiendasActivas.iterator();

					while (itStores.hasNext()) {
						Store tienda = (Store) itStores.next();
						String codTienda = tienda.getKey().toString();
						while (codTienda.length() < 3)
							codTienda = "0" + codTienda;

						paravalue = paramsDAO.getParamByClave(sessionSaadmin, Integer.valueOf(codTienda).toString(),
								ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_OUT");
						String dirTemp = properties.getObject("SUITE_ROOT") + paravalue.getValor() + codTienda
								+ File.separator + properties.getObject("accountingTotals.in.folder.path");
						(new File(dirTemp)).mkdirs();
						pathActiveStore.add(dirTemp);

					}
				} else {
					log.info("No existen tiendas activas disponibles.");
				}
			} else {

				paravalue = paramsDAO.getParamByClave(sessionSaadmin, Integer.valueOf(storeCode).toString(),
						ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_OUT");
				pathActiveStore.add(properties.getObject("SUITE_ROOT") + paravalue.getValor() + storeCode
						+ File.separator + properties.getObject("accountingTotals.in.folder.path"));
			}
			storeCodeHasta = properties.getObject("accountingTotals.in.folder.path").length() + 1;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando AccountingTotalsUpdateProcess...");
		inicializar();

		while (!isEnd) {

			try {
				// File accountFileCTC = new File(inFolder, "EAMACCTC.DAT");
				// File accountFileCTP = new File(inFolder, "EAMACCTP.DAT");

				File fileToProcess = getNextFile();

				if (fileToProcess != null) {
					String path = fileToProcess.getAbsolutePath();
					String pathFinal = path.substring(0, path.lastIndexOf(File.separator));
					String tiendaCode = pathFinal.substring(pathFinal.length() - (storeCodeHasta + 3),
							pathFinal.length() - storeCodeHasta);
					if (fileToProcess.getName().equalsIgnoreCase("EAMACCTC.DAT")) {

						try {
							fechaContable = null;
							iniciarSesion();
							RandomAccessFile f = new RandomAccessFile(fileToProcess.getAbsolutePath(), "r");
							byte[] b = new byte[(int) f.length()];
							f.readFully(b);
							StringBuffer sbCTC = new StringBuffer(new String(b));
							f.close();
							procesarArchivo(sbCTC, tiendaCode, "A");
							UtilityFile.createWriteDataFile(getEyesFileName(),
									"ACC_TOT_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
											+ storeCode + "|PRC|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
											+ "|Procesamiento del Archivo EAMACCTC.DAT Tienda: "+tiendaCode+" finalizado.\n",
									true);
							log.info("Procesamiento del archivo EAMACCTC.DAT en la tienda: "+ tiendaCode +" finalizado.");
						} catch (Exception e) {
							log.error(e.getMessage(), e);
							try {
								UtilityFile.createWriteDataFile(getEyesFileName(),
										"ACC_TOT_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
												+ "|" + storeCode + "|ERR|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
														new Date())
												+ "|Error al procesar el archivo EAMACCTC.DAT Tienda: "+tiendaCode+".\n",
										true);
							} catch (Exception e1) {
								log.error(e1.getMessage(), e1);
							}
						} finally {
							session.close();
							session = null;
							tx = null;
						}

					}
					if (fileToProcess.getName().equalsIgnoreCase("EAMACCTP.DAT")) {
						try {
							fechaContable = null;
							iniciarSesion();
							RandomAccessFile f = new RandomAccessFile(fileToProcess.getAbsolutePath(), "r");
							byte[] b = new byte[(int) f.length()];
							f.readFully(b);
							StringBuffer sbCTP = new StringBuffer(new String(b));
							f.close();
							procesarArchivo(sbCTP, tiendaCode, "P");
							UtilityFile.createWriteDataFile(getEyesFileName(),
									"ACC_TOT_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
											+ storeCode + "|PRC|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
											+ "|Procesamiento del Archivo EAMACCTP.DAT Tienda: "+tiendaCode+" finalizado.\n",
									true);
							log.info("Procesamiento del archivo EAMACCTP.DAT en la tienda: "+ tiendaCode +" finalizado.");

						} catch (Exception e) {
							log.error(e.getMessage(), e);
							try {
								UtilityFile.createWriteDataFile(getEyesFileName(),
										"ACC_TOT_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
												+ "|" + storeCode + "|ERR|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
														new Date())
												+ "|Error al procesar el archivo EAMACCTP.DAT Tienda: "+tiendaCode+".\n",
										true);
							} catch (Exception e1) {
								log.error(e1.getMessage(), e1);
							}
						} finally {
							session.close();
							session = null;
							tx = null;
						}
					}
					
					if (!filesToProcess.hasNext()) {
						try {
							Thread.sleep(sleepTime);
							try {
								iniciarSesion();
								cleanTables();
							} catch (Exception e) {
								log.error("Se produjo un error al intentar borrar las tablas.");
								e.printStackTrace();
							} finally {
								session.close();
								session = null;
								tx = null;
							}
							
						} catch (InterruptedException e) {
							log.error(e.getMessage(), e);
						}
					}
				}

			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile.createWriteDataFile(getEyesFileName(),
							"ACC_TOT_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ storeCode + "|ERR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Error al procesar el archivo.\n",
							true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			}

		}
		finished = true;
	}

	private void procesarArchivo(StringBuffer sb, String tiendaCode, String typePeriod) {

		try {

			for (int i = 512; i < sb.length(); i = i + 512) {

				String bloque = sb.substring(i, i + 512);

				String recordType = bloque.substring(4, 5);

				byte nullByte = 0x00;
				if (recordType.getBytes()[0] != nullByte) {
					// log.info("RECORDTYPE: " + recordType);
					char type = recordType.charAt(0);
					switch (type) {

					case '1':
						procesaStoreRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case '2':
						procesaOperatorTerminalRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case '3':
						procesaOperatorTerminalRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case '4':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case '5':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case '6':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case '7':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case '8':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case 'A':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case 'B':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case 'C':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case 'D':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case 'E':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case 'F':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case 'G':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case 'H':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case 'I':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;
					case 'J':
						procesaTenderVarietyRecord(bloque.substring(4), tiendaCode, typePeriod);
						break;

					default:
						break;

					}
				}
			}
			if (indivRecordPend != null && !indivRecordPend.isEmpty() && fechaContable != null) {
				Iterator<String> it = indivRecordPend.iterator();

				while (it.hasNext()) {

					procesaOperatorTerminalRecord(it.next(), tiendaCode, typePeriod);

				}
			}
			if (tenderRecordPend != null && !tenderRecordPend.isEmpty() && fechaContable != null) {
				Iterator<String> it = tenderRecordPend.iterator();

				while (it.hasNext()) {

					procesaTenderVarietyRecord(it.next(), tiendaCode, typePeriod);

				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private void iniciarSesion() {
		while (session == null && !isEnd) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	protected void iniciarSesion(String name) {
		while (sessionSaadmin == null) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionSaadmin == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo StoreStatusUpdateProcess...");
		while (!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					return false;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		log.info("Finalizó el Proceso de Actualización de StoreStatus.");
		return true;
	}

	private Integer convertToInt(byte[] bytes) {
		final ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		if (bytes.length == 2)
			return new Integer(bb.getShort());
		else
			return new Integer(bb.getInt());
	}

	private void procesaStoreRecord(String bloque, String tiendaCode, String periodType) {

		try {
			StoreRecordTotals record = new StoreRecordTotals();
			byte[] arregloStr;
			arregloStr = bloque.substring(1, 6).getBytes();
			int store = Integer.parseInt(ControllerFiles.unpack(arregloStr));
			byte[] arregloDate;
			arregloDate = bloque.substring(6, 11).getBytes();

			Date fecha = formatter.parse(ControllerFiles.unpack(arregloDate).substring(0, 10));
			fechaContable = formatter2.format(fecha);
//			record = storeRecordDAO.getStoreRecordByStoreDate(session, Integer.valueOf(tiendaCode), fechaContable);

			initTx();
			record.setStoreCode(Integer.valueOf(tiendaCode));
			record.setRecordType(bloque.substring(0, 1));
			record.setStoreId(store);
			record.setTimeStamp(fecha);
			byte indicat = bloque.substring(11, 12).getBytes()[0];
			record.setFlReconciled(isSet(indicat, 7));
			record.setFlPrinted(isSet(indicat, 6));
			byte[] arregloLst;
			arregloLst = bloque.substring(12, 14).getBytes();
			int lstTerminal = Integer.parseInt(ControllerFiles.unpack(arregloLst));
			record.setLastTerminal(lstTerminal);
			record.setRestart(convertToInt(bloque.substring(14, 18).getBytes()));
			record.setGrossPlus(convertToInt(bloque.substring(18, 22).getBytes()));
			record.setGrossMinus(convertToInt(bloque.substring(22, 26).getBytes()));
			record.setSalesTransactionCount(convertToInt(bloque.substring(26, 30).getBytes()));
			record.setLonAmtCash(convertToInt(bloque.substring(30, 34).getBytes()));
			record.setLonAmtCheck(convertToInt(bloque.substring(34, 38).getBytes()));
			record.setLonAmtFoods(convertToInt(bloque.substring(38, 42).getBytes()));
			record.setLonAmtMisc1(convertToInt(bloque.substring(42, 46).getBytes()));
			record.setLonAmtMisc2(convertToInt(bloque.substring(46, 50).getBytes()));
			record.setLonAmtMisc3(convertToInt(bloque.substring(50, 54).getBytes()));
			record.setLonAmtManuf(convertToInt(bloque.substring(54, 58).getBytes()));
			record.setLonAmtStore(convertToInt(bloque.substring(58, 62).getBytes()));
			record.setPkpAmtCash(convertToInt(bloque.substring(62, 66).getBytes()));
			record.setPkpAmtCheck(convertToInt(bloque.substring(66, 70).getBytes()));
			record.setPkpAmtFoods(convertToInt(bloque.substring(70, 74).getBytes()));
			record.setPkpAmtMisc1(convertToInt(bloque.substring(74, 78).getBytes()));
			record.setPkpAmtMisc2(convertToInt(bloque.substring(78, 82).getBytes()));
			record.setPkpAmtMisc3(convertToInt(bloque.substring(82, 86).getBytes()));
			record.setPkpAmtManuf(convertToInt(bloque.substring(86, 90).getBytes()));
			record.setPkpAmtStore(convertToInt(bloque.substring(90, 94).getBytes()));
			record.setCntTndAmtCash(convertToInt(bloque.substring(94, 98).getBytes()));
			record.setCntTndAmtCheck(convertToInt(bloque.substring(98, 102).getBytes()));
			record.setCntTndAmtFoods(convertToInt(bloque.substring(102, 106).getBytes()));
			record.setCntTndAmtMisc1(convertToInt(bloque.substring(106, 110).getBytes()));
			record.setCntTndAmtMisc2(convertToInt(bloque.substring(110, 114).getBytes()));
			record.setCntTndAmtMisc3(convertToInt(bloque.substring(114, 118).getBytes()));
			record.setCntTndAmtManuf(convertToInt(bloque.substring(118, 122).getBytes()));
			record.setCntTndAmtStore(convertToInt(bloque.substring(122, 126).getBytes()));
			record.setNetTndAmtCash(convertToInt(bloque.substring(126, 130).getBytes()));
			record.setNetTndAmtCheck(convertToInt(bloque.substring(130, 134).getBytes()));
			record.setNetTndAmtFoods(convertToInt(bloque.substring(134, 138).getBytes()));
			record.setNetTndAmtMisc1(convertToInt(bloque.substring(138, 142).getBytes()));
			record.setNetTndAmtMisc2(convertToInt(bloque.substring(142, 146).getBytes()));
			record.setNetTndAmtMisc3(convertToInt(bloque.substring(146, 150).getBytes()));
			record.setNetTndAmtManuf(convertToInt(bloque.substring(150, 154).getBytes()));
			record.setNetTndAmtStore(convertToInt(bloque.substring(154, 158).getBytes()));
			record.setOpnTndAmtCash(convertToInt(bloque.substring(158, 162).getBytes()));
			record.setOpnTndAmtCheck(convertToInt(bloque.substring(162, 166).getBytes()));
			record.setOpnTndAmtFoods(convertToInt(bloque.substring(166, 170).getBytes()));
			record.setOpnTndAmtMisc1(convertToInt(bloque.substring(170, 174).getBytes()));
			record.setOpnTndAmtMisc2(convertToInt(bloque.substring(174, 178).getBytes()));
			record.setOpnTndAmtMisc3(convertToInt(bloque.substring(178, 182).getBytes()));
			record.setOpnTndAmtManuf(convertToInt(bloque.substring(182, 186).getBytes()));
			record.setOpnTndAmtStore(convertToInt(bloque.substring(186, 190).getBytes()));
			record.setMiscTransactionAmount(convertToInt(bloque.substring(190, 194).getBytes()));
			// record.setEFTData(String.valueOf(convertToInt(bloque.substring(194,
			// 294).getBytes())));
			byte[] arregloDate2;
			arregloDate2 = bloque.substring(294, 299).getBytes();
			Date fechaPeriod = formatter.parse(ControllerFiles.unpack(arregloDate2));
			record.setPeriodTimeStamp(fechaPeriod);
			record.setTaxableExemptAmount(convertToInt(bloque.substring(299, 303).getBytes()));
			record.setTaxExemptAmountA(convertToInt(bloque.substring(303, 307).getBytes()));
			record.setTaxExemptAmountB(convertToInt(bloque.substring(307, 311).getBytes()));
			record.setTaxExemptAmountC(convertToInt(bloque.substring(311, 315).getBytes()));
			record.setTaxExemptAmountD(convertToInt(bloque.substring(315, 319).getBytes()));
			record.setTaxableExemptAmountA(convertToInt(bloque.substring(319, 323).getBytes()));
			record.setTaxableExemptAmountB(convertToInt(bloque.substring(323, 327).getBytes()));
			record.setTaxableExemptAmountC(convertToInt(bloque.substring(327, 331).getBytes()));
			record.setTaxableExemptAmountD(convertToInt(bloque.substring(331, 335).getBytes()));
			record.setTaxExemptAmountE(convertToInt(bloque.substring(335, 339).getBytes()));
			record.setTaxExemptAmountF(convertToInt(bloque.substring(339, 343).getBytes()));
			record.setTaxExemptAmountG(convertToInt(bloque.substring(343, 347).getBytes()));
			record.setTaxExemptAmountH(convertToInt(bloque.substring(347, 351).getBytes()));
			record.setTaxableExemptAmountE(convertToInt(bloque.substring(351, 355).getBytes()));
			record.setTaxableExemptAmountF(convertToInt(bloque.substring(355, 359).getBytes()));
			record.setTaxableExemptAmountG(convertToInt(bloque.substring(359, 363).getBytes()));
			record.setTaxableExemptAmountH(convertToInt(bloque.substring(363, 367).getBytes()));
			// byte[] arregloRsv;
			// arregloRsv = bloque.substring(367,384).getBytes();
			// int reserved =
			// Integer.parseInt(ControllerFiles.unpack(arregloRsv));
			// record.setReserved(String.valueOf(reserved));
			record.setManAutoCouponAmount(convertToInt(bloque.substring(384, 388).getBytes()));
			record.setStoreAutoCouponAmount(convertToInt(bloque.substring(388, 392).getBytes()));
			record.setDoubledCouponAmount(convertToInt(bloque.substring(392, 396).getBytes()));
			record.setPCTransactionAmount(convertToInt(bloque.substring(396, 400).getBytes()));
			record.setPCTransactionCount(convertToInt(bloque.substring(400, 402).getBytes()));
			record.setPCAutoCouponCount(convertToInt(bloque.substring(402, 404).getBytes()));
			record.setPCAutoCouponAmount(convertToInt(bloque.substring(404, 408).getBytes()));
			record.setTypPrd(periodType);
			// record.setUsrInteger(String.valueOf(convertToInt(bloque.substring(408,
			// 468).getBytes())));
			// record.setUsrReserved(bloque.substring(468, 508));

			session.saveOrUpdate(record);
			tx.commit();
			// log.info(record.getRecordType() +" Registrado!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				tx.rollback();
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}

	}

	private void procesaOperatorTerminalRecord(String bloque, String tiendaCode, String periodType)
			throws ParseException {

		OperTermRecordTotals record = new OperTermRecordTotals();
		byte[] arregloAcc;
		arregloAcc = bloque.substring(1, 6).getBytes();
		int account = Integer.parseInt(ControllerFiles.unpack(arregloAcc));
		byte[] arregloDate;
		arregloDate = bloque.substring(6, 11).getBytes();

		if (fechaContable != null) {
			Date fecha = formatter.parse(ControllerFiles.unpack(arregloDate).substring(0, 10));
			String recordType = bloque.substring(0, 1);
//			record = storeRecordDAO.getOperTermRecordByAccountDate(session, Integer.valueOf(tiendaCode), recordType,
//					account, fechaContable);
			try {
				initTx();
				record.setStoreCode(Integer.valueOf(tiendaCode));
				record.setRecordType(recordType);
				record.setAccountId(account);
				record.setStoreTimeStamp(formatter2.parse(fechaContable));
				record.setTimeStamp(fecha);
				byte[] arregloTrxNum;
				arregloTrxNum = bloque.substring(12, 14).getBytes();
				int trxNum = Integer.parseInt(ControllerFiles.unpack(arregloTrxNum));
				record.setTrxNumber(trxNum);
				record.setRestart(convertToInt(bloque.substring(14, 18).getBytes()));
				byte[] gp = bloque.substring(18, 22).getBytes();
				record.setGrossPlus(convertToInt(gp));
				record.setGrossMinus(convertToInt(bloque.substring(22, 24).getBytes()));
				record.setSalesTransactionCount(convertToInt(bloque.substring(24, 30).getBytes()));
				record.setLonAmtCash(convertToInt(bloque.substring(30, 34).getBytes()));
				record.setLonAmtCheck(convertToInt(bloque.substring(34, 38).getBytes()));
				record.setLonAmtFoods(convertToInt(bloque.substring(38, 42).getBytes()));
				record.setLonAmtMisc1(convertToInt(bloque.substring(42, 46).getBytes()));
				record.setLonAmtMisc2(convertToInt(bloque.substring(46, 50).getBytes()));
				record.setLonAmtMisc3(convertToInt(bloque.substring(50, 54).getBytes()));
				record.setLonAmtManuf(convertToInt(bloque.substring(54, 58).getBytes()));
				record.setLonAmtStore(convertToInt(bloque.substring(58, 62).getBytes()));
				record.setPkpAmtCash(convertToInt(bloque.substring(62, 66).getBytes()));
				record.setPkpAmtCheck(convertToInt(bloque.substring(66, 70).getBytes()));
				record.setPkpAmtFoods(convertToInt(bloque.substring(70, 74).getBytes()));
				record.setPkpAmtMisc1(convertToInt(bloque.substring(74, 78).getBytes()));
				record.setPkpAmtMisc2(convertToInt(bloque.substring(78, 82).getBytes()));
				record.setPkpAmtMisc3(convertToInt(bloque.substring(82, 86).getBytes()));
				record.setPkpAmtManuf(convertToInt(bloque.substring(86, 90).getBytes()));
				record.setPkpAmtStore(convertToInt(bloque.substring(90, 94).getBytes()));
				record.setCntTndAmtCash(convertToInt(bloque.substring(94, 98).getBytes()));
				record.setCntTndAmtCheck(convertToInt(bloque.substring(98, 102).getBytes()));
				record.setCntTndAmtFoods(convertToInt(bloque.substring(102, 106).getBytes()));
				record.setCntTndAmtMisc1(convertToInt(bloque.substring(106, 110).getBytes()));
				record.setCntTndAmtMisc2(convertToInt(bloque.substring(110, 114).getBytes()));
				record.setCntTndAmtMisc3(convertToInt(bloque.substring(114, 118).getBytes()));
				record.setCntTndAmtManuf(convertToInt(bloque.substring(118, 122).getBytes()));
				record.setCntTndAmtStore(convertToInt(bloque.substring(122, 126).getBytes()));
				record.setNetTndAmtCash(convertToInt(bloque.substring(126, 130).getBytes()));
				record.setNetTndAmtCheck(convertToInt(bloque.substring(130, 134).getBytes()));
				record.setNetTndAmtFoods(convertToInt(bloque.substring(134, 138).getBytes()));
				record.setNetTndAmtMisc1(convertToInt(bloque.substring(138, 142).getBytes()));
				record.setNetTndAmtMisc2(convertToInt(bloque.substring(142, 146).getBytes()));
				record.setNetTndAmtMisc3(convertToInt(bloque.substring(146, 150).getBytes()));
				record.setNetTndAmtManuf(convertToInt(bloque.substring(150, 154).getBytes()));
				record.setNetTndAmtStore(convertToInt(bloque.substring(154, 158).getBytes()));
				record.setItemSalesAmount(convertToInt(bloque.substring(158, 162).getBytes()));
				record.setDepositAmount(convertToInt(bloque.substring(162, 166).getBytes()));
				record.setRefundAmount(convertToInt(bloque.substring(166, 170).getBytes()));
				record.setDepositReturnAmount(convertToInt(bloque.substring(170, 174).getBytes()));
				record.setMiscReceiptAmount(convertToInt(bloque.substring(174, 178).getBytes()));
				record.setMiscPayoutAmount(convertToInt(bloque.substring(178, 182).getBytes()));
				record.setDiscountAmount(convertToInt(bloque.substring(182, 186).getBytes()));
				record.setTaxableAmountExempt(convertToInt(bloque.substring(186, 190).getBytes()));
				record.setItemCancelAmount(convertToInt(bloque.substring(190, 194).getBytes()));
				record.setDepositCancelAmount(convertToInt(bloque.substring(194, 198).getBytes()));
				record.setCreditTransactionAmount(convertToInt(bloque.substring(198, 202).getBytes()));
				record.setTenderFeeAmount(convertToInt(bloque.substring(202, 206).getBytes()));
				record.setMiscTransactionAmount(convertToInt(bloque.substring(206, 210).getBytes()));
				record.setTenderCashingAmount(convertToInt(bloque.substring(210, 214).getBytes()));
				record.setTenderExchangeAmount(convertToInt(bloque.substring(214, 218).getBytes()));
				record.setTaxableAmountA(convertToInt(bloque.substring(218, 222).getBytes()));
				record.setTaxAmountA(convertToInt(bloque.substring(222, 226).getBytes()));
				record.setTaxableAmountB(convertToInt(bloque.substring(226, 230).getBytes()));
				record.setTaxAmountB(convertToInt(bloque.substring(230, 234).getBytes()));
				record.setTaxableAmountC(convertToInt(bloque.substring(234, 238).getBytes()));
				record.setTaxAmountC(convertToInt(bloque.substring(238, 242).getBytes()));
				record.setTaxableAmountD(convertToInt(bloque.substring(242, 246).getBytes()));
				record.setTaxAmountD(convertToInt(bloque.substring(246, 250).getBytes()));
				record.setStandaloneGrossPlus(convertToInt(bloque.substring(250, 254).getBytes()));
				record.setStandaloneGrossMinus(convertToInt(bloque.substring(254, 258).getBytes()));
				record.setVoidTransactionAmount(convertToInt(bloque.substring(258, 262).getBytes()));
				record.setTrainingTransactionAmount(convertToInt(bloque.substring(262, 266).getBytes()));
				record.setItemSalesCount(convertToInt(bloque.substring(266, 270).getBytes()));
				record.setItemSalesKeyedCount(convertToInt(bloque.substring(270, 274).getBytes()));
				record.setItemSalesLookupKeysCount(convertToInt(bloque.substring(274, 278).getBytes()));
				record.setTradingStamps(convertToInt(bloque.substring(278, 282).getBytes()));
				record.setDepositCount(convertToInt(bloque.substring(282, 284).getBytes()));
				record.setRefundCount(convertToInt(bloque.substring(284, 286).getBytes()));
				record.setDepositReturnCount(convertToInt(bloque.substring(286, 288).getBytes()));
				record.setMiscReceiptCount(convertToInt(bloque.substring(288, 290).getBytes()));
				record.setMiscPayoutCount(convertToInt(bloque.substring(290, 292).getBytes()));
				record.setDiscountCount(convertToInt(bloque.substring(292, 294).getBytes()));
				record.setTaxExemptionCount(convertToInt(bloque.substring(294, 296).getBytes()));
				record.setItemCancelCount(convertToInt(bloque.substring(296, 298).getBytes()));
				record.setDepositCancelCount(convertToInt(bloque.substring(298, 300).getBytes()));
				record.setCreditTransactionCount(convertToInt(bloque.substring(300, 302).getBytes()));
				record.setSpecialSignOffCount(convertToInt(bloque.substring(302, 304).getBytes()));
				record.setNoSaleTransactionCount(convertToInt(bloque.substring(304, 306).getBytes()));
				record.setNetTndNumCash(convertToInt(bloque.substring(306, 308).getBytes()));
				record.setNetTndNumCheck(convertToInt(bloque.substring(308, 310).getBytes()));
				record.setNetTndNumFoods(convertToInt(bloque.substring(310, 312).getBytes()));
				record.setNetTndNumMisc1(convertToInt(bloque.substring(312, 314).getBytes()));
				record.setNetTndNumMisc2(convertToInt(bloque.substring(314, 316).getBytes()));
				record.setNetTndNumMisc3(convertToInt(bloque.substring(316, 318).getBytes()));
				record.setNetTndNumManuf(convertToInt(bloque.substring(318, 320).getBytes()));
				record.setNetTndNumStore(convertToInt(bloque.substring(320, 322).getBytes()));
				record.setLoanCount(convertToInt(bloque.substring(322, 324).getBytes()));
				record.setPickupCount(convertToInt(bloque.substring(324, 326).getBytes()));
				record.setStandaloneTransactionCount(convertToInt(bloque.substring(326, 328).getBytes()));
				record.setVoidTransactionCount(convertToInt(bloque.substring(328, 330).getBytes()));
				record.setTrainingTransactionCount(convertToInt(bloque.substring(330, 332).getBytes()));
				record.setStandaloneTaxAmount(convertToInt(bloque.substring(332, 336).getBytes()));
				record.setTaxableAmountE(convertToInt(bloque.substring(336, 340).getBytes()));
				record.setTaxAmountE(convertToInt(bloque.substring(340, 344).getBytes()));
				record.setTaxableAmountF(convertToInt(bloque.substring(344, 348).getBytes()));
				record.setTaxAmountF(convertToInt(bloque.substring(348, 352).getBytes()));
				record.setTaxableAmountG(convertToInt(bloque.substring(352, 356).getBytes()));
				record.setTaxAmountG(convertToInt(bloque.substring(356, 360).getBytes()));
				record.setTaxableAmountH(convertToInt(bloque.substring(360, 364).getBytes()));
				record.setTaxAmountH(convertToInt(bloque.substring(364, 368).getBytes()));
				record.setTaxAmountE(convertToInt(bloque.substring(368, 372).getBytes()));
				record.setSalesPoints(convertToInt(bloque.substring(372, 376).getBytes()));
				record.setBonusPoints(convertToInt(bloque.substring(376, 380).getBytes()));
				record.setRedeemedPoints(convertToInt(bloque.substring(380, 384).getBytes()));
				record.setManAutoCouponAmount(convertToInt(bloque.substring(384, 388).getBytes()));
				record.setStoreAutoCouponAmount(convertToInt(bloque.substring(388, 392).getBytes()));
				record.setDoubledCouponAmount(convertToInt(bloque.substring(392, 396).getBytes()));
				record.setPCTransactionAmount(convertToInt(bloque.substring(396, 400).getBytes()));
				record.setPCTransactionCount(convertToInt(bloque.substring(400, 402).getBytes()));
				record.setPCAutoCouponCount(convertToInt(bloque.substring(402, 404).getBytes()));
				record.setPCAutoCouponAmount(convertToInt(bloque.substring(404, 408).getBytes()));
				record.setCouponTier1Amount(convertToInt(bloque.substring(408, 412).getBytes()));
				record.setCouponTier2Amount(convertToInt(bloque.substring(412, 416).getBytes()));
				record.setCouponTier3Amount(convertToInt(bloque.substring(416, 420).getBytes()));
				record.setCouponTier4Amount(convertToInt(bloque.substring(420, 424).getBytes()));
				record.setCouponTier5Amount(convertToInt(bloque.substring(424, 428).getBytes()));
				record.setCouponTier6Amount(convertToInt(bloque.substring(428, 432).getBytes()));
				byte[] arregloCoupon;
				arregloCoupon = bloque.substring(432, 444).getBytes();
				String couponCount = ControllerFiles.unpack(arregloCoupon);
				record.setCouponTier1Count(Integer.valueOf(couponCount.substring(0, 2)));
				record.setCouponTier2Count(Integer.valueOf(couponCount.substring(2, 4)));
				record.setCouponTier3Count(Integer.valueOf(couponCount.substring(4, 6)));
				record.setCouponTier4Count(Integer.valueOf(couponCount.substring(6, 8)));
				record.setCouponTier5Count(Integer.valueOf(couponCount.substring(8, 10)));
				record.setCouponTier6Count(Integer.valueOf(couponCount.substring(10, 12)));
				record.setTypPrd(periodType);
				session.saveOrUpdate(record);
				tx.commit();

				// log.info(record.getRecordType() +" Registrado!");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					tx.rollback();
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		} else {
			indivRecordPend.add(bloque);
		}
	}

	private void procesaTenderVarietyRecord(String bloque, String tiendaCode, String periodType) throws ParseException {

		TenderTotalsVarietyRecord record = new TenderTotalsVarietyRecord();
		byte[] arregloAcc;
		arregloAcc = bloque.substring(1, 6).getBytes();
		int account = Integer.parseInt(ControllerFiles.unpack(arregloAcc));

		if (fechaContable != null) {
			String recordType = bloque.substring(0, 1);
//			record = storeRecordDAO.getTenderVarietyRecordByAccount(session, Integer.valueOf(tiendaCode), recordType,
//					account, fechaContable);
			try {
				initTx();
				record.setStoreCode(Integer.valueOf(tiendaCode));
				record.setRecordType(recordType);
				record.setAccountId(account);
				record.setTimeStamp(formatter2.parse(fechaContable));
				byte[] arregloRestart;
				arregloRestart = bloque.substring(6, 10).getBytes();
				String restart = ControllerFiles.unpack(arregloRestart);
				record.setRestart(restart);
				record.setTnd1amtTND1(convertToInt(bloque.substring(10, 14).getBytes()));
				record.setTnd1amtTND2(convertToInt(bloque.substring(14, 18).getBytes()));
				record.setTnd1amtTND3(convertToInt(bloque.substring(18, 22).getBytes()));
				record.setTnd1amtTND4(convertToInt(bloque.substring(22, 26).getBytes()));
				record.setTnd1amtTND5(convertToInt(bloque.substring(26, 30).getBytes()));
				record.setTnd1amtTND6(convertToInt(bloque.substring(30, 34).getBytes()));
				record.setTnd2amtTND1(convertToInt(bloque.substring(34, 38).getBytes()));
				record.setTnd2amtTND2(convertToInt(bloque.substring(38, 42).getBytes()));
				record.setTnd2amtTND3(convertToInt(bloque.substring(42, 46).getBytes()));
				record.setTnd2amtTND4(convertToInt(bloque.substring(46, 50).getBytes()));
				record.setTnd2amtTND5(convertToInt(bloque.substring(50, 54).getBytes()));
				record.setTnd2amtTND6(convertToInt(bloque.substring(54, 58).getBytes()));
				record.setTnd3amtTND1(convertToInt(bloque.substring(58, 62).getBytes()));
				record.setTnd3amtTND2(convertToInt(bloque.substring(62, 66).getBytes()));
				record.setTnd3amtTND3(convertToInt(bloque.substring(66, 70).getBytes()));
				record.setTnd3amtTND4(convertToInt(bloque.substring(70, 74).getBytes()));
				record.setTnd3amtTND5(convertToInt(bloque.substring(74, 78).getBytes()));
				record.setTnd3amtTND6(convertToInt(bloque.substring(78, 82).getBytes()));
				record.setTnd4amtTND1(convertToInt(bloque.substring(82, 86).getBytes()));
				record.setTnd4amtTND2(convertToInt(bloque.substring(86, 90).getBytes()));
				record.setTnd4amtTND3(convertToInt(bloque.substring(90, 94).getBytes()));
				record.setTnd4amtTND4(convertToInt(bloque.substring(94, 98).getBytes()));
				record.setTnd4amtTND5(convertToInt(bloque.substring(98, 102).getBytes()));
				record.setTnd4amtTND6(convertToInt(bloque.substring(102, 106).getBytes()));
				record.setTnd5amtTND1(convertToInt(bloque.substring(106, 110).getBytes()));
				record.setTnd5amtTND2(convertToInt(bloque.substring(110, 114).getBytes()));
				record.setTnd5amtTND3(convertToInt(bloque.substring(114, 118).getBytes()));
				record.setTnd5amtTND4(convertToInt(bloque.substring(118, 122).getBytes()));
				record.setTnd5amtTND5(convertToInt(bloque.substring(122, 126).getBytes()));
				record.setTnd5amtTND6(convertToInt(bloque.substring(126, 130).getBytes()));
				record.setTnd6amtTND1(convertToInt(bloque.substring(130, 134).getBytes()));
				record.setTnd6amtTND2(convertToInt(bloque.substring(134, 138).getBytes()));
				record.setTnd6amtTND3(convertToInt(bloque.substring(138, 142).getBytes()));
				record.setTnd6amtTND4(convertToInt(bloque.substring(142, 146).getBytes()));
				record.setTnd6amtTND5(convertToInt(bloque.substring(146, 150).getBytes()));
				record.setTnd6amtTND6(convertToInt(bloque.substring(150, 154).getBytes()));
				record.setTnd7amtTND1(convertToInt(bloque.substring(154, 158).getBytes()));
				record.setTnd7amtTND2(convertToInt(bloque.substring(158, 162).getBytes()));
				record.setTnd7amtTND3(convertToInt(bloque.substring(162, 166).getBytes()));
				record.setTnd7amtTND4(convertToInt(bloque.substring(166, 170).getBytes()));
				record.setTnd7amtTND5(convertToInt(bloque.substring(170, 174).getBytes()));
				record.setTnd7amtTND6(convertToInt(bloque.substring(174, 178).getBytes()));
				record.setTnd8amtTND1(convertToInt(bloque.substring(178, 182).getBytes()));
				record.setTnd8amtTND2(convertToInt(bloque.substring(182, 186).getBytes()));
				record.setTnd8amtTND3(convertToInt(bloque.substring(186, 190).getBytes()));
				record.setTnd8amtTND4(convertToInt(bloque.substring(190, 194).getBytes()));
				record.setTnd8amtTND5(convertToInt(bloque.substring(194, 198).getBytes()));
				record.setTnd8amtTND6(convertToInt(bloque.substring(198, 202).getBytes()));
				record.setTnd1cntTND1(convertToInt(bloque.substring(202, 204).getBytes()));
				record.setTnd1cntTND2(convertToInt(bloque.substring(204, 206).getBytes()));
				record.setTnd1cntTND3(convertToInt(bloque.substring(206, 208).getBytes()));
				record.setTnd1cntTND4(convertToInt(bloque.substring(208, 210).getBytes()));
				record.setTnd1cntTND5(convertToInt(bloque.substring(210, 212).getBytes()));
				record.setTnd1cntTND6(convertToInt(bloque.substring(212, 214).getBytes()));
				record.setTnd2cntTND1(convertToInt(bloque.substring(214, 216).getBytes()));
				record.setTnd2cntTND2(convertToInt(bloque.substring(216, 218).getBytes()));
				record.setTnd2cntTND3(convertToInt(bloque.substring(218, 220).getBytes()));
				record.setTnd2cntTND4(convertToInt(bloque.substring(220, 222).getBytes()));
				record.setTnd2cntTND5(convertToInt(bloque.substring(222, 224).getBytes()));
				record.setTnd2cntTND6(convertToInt(bloque.substring(224, 226).getBytes()));
				record.setTnd3cntTND1(convertToInt(bloque.substring(226, 228).getBytes()));
				record.setTnd3cntTND2(convertToInt(bloque.substring(228, 230).getBytes()));
				record.setTnd3cntTND3(convertToInt(bloque.substring(230, 232).getBytes()));
				record.setTnd3cntTND4(convertToInt(bloque.substring(232, 234).getBytes()));
				record.setTnd3cntTND5(convertToInt(bloque.substring(234, 236).getBytes()));
				record.setTnd3cntTND6(convertToInt(bloque.substring(236, 238).getBytes()));
				record.setTnd4cntTND1(convertToInt(bloque.substring(238, 240).getBytes()));
				record.setTnd4cntTND2(convertToInt(bloque.substring(240, 242).getBytes()));
				record.setTnd4cntTND3(convertToInt(bloque.substring(242, 244).getBytes()));
				record.setTnd4cntTND4(convertToInt(bloque.substring(244, 246).getBytes()));
				record.setTnd4cntTND5(convertToInt(bloque.substring(246, 248).getBytes()));
				record.setTnd4cntTND6(convertToInt(bloque.substring(248, 250).getBytes()));
				record.setTnd5cntTND1(convertToInt(bloque.substring(250, 252).getBytes()));
				record.setTnd5cntTND2(convertToInt(bloque.substring(252, 254).getBytes()));
				record.setTnd5cntTND3(convertToInt(bloque.substring(254, 256).getBytes()));
				record.setTnd5cntTND4(convertToInt(bloque.substring(256, 258).getBytes()));
				record.setTnd5cntTND5(convertToInt(bloque.substring(258, 260).getBytes()));
				record.setTnd5cntTND6(convertToInt(bloque.substring(260, 262).getBytes()));
				record.setTnd6cntTND1(convertToInt(bloque.substring(262, 264).getBytes()));
				record.setTnd6cntTND2(convertToInt(bloque.substring(264, 266).getBytes()));
				record.setTnd6cntTND3(convertToInt(bloque.substring(266, 268).getBytes()));
				record.setTnd6cntTND4(convertToInt(bloque.substring(268, 270).getBytes()));
				record.setTnd6cntTND5(convertToInt(bloque.substring(270, 272).getBytes()));
				record.setTnd6cntTND6(convertToInt(bloque.substring(272, 274).getBytes()));
				record.setTnd7cntTND1(convertToInt(bloque.substring(274, 276).getBytes()));
				record.setTnd7cntTND2(convertToInt(bloque.substring(276, 278).getBytes()));
				record.setTnd7cntTND3(convertToInt(bloque.substring(278, 280).getBytes()));
				record.setTnd7cntTND4(convertToInt(bloque.substring(280, 282).getBytes()));
				record.setTnd7cntTND5(convertToInt(bloque.substring(282, 284).getBytes()));
				record.setTnd7cntTND6(convertToInt(bloque.substring(284, 286).getBytes()));
				record.setTnd8cntTND1(convertToInt(bloque.substring(286, 288).getBytes()));
				record.setTnd8cntTND2(convertToInt(bloque.substring(288, 290).getBytes()));
				record.setTnd8cntTND3(convertToInt(bloque.substring(290, 292).getBytes()));
				record.setTnd8cntTND4(convertToInt(bloque.substring(292, 294).getBytes()));
				record.setTnd8cntTND5(convertToInt(bloque.substring(294, 296).getBytes()));
				record.setTnd8cntTND6(convertToInt(bloque.substring(296, 298).getBytes()));
				record.setTnd1amtTND7(convertToInt(bloque.substring(298, 302).getBytes()));
				record.setTnd1amtTND8(convertToInt(bloque.substring(302, 306).getBytes()));
				record.setTnd1amtTND9(convertToInt(bloque.substring(306, 310).getBytes()));
				record.setTnd2amtTND7(convertToInt(bloque.substring(310, 314).getBytes()));
				record.setTnd2amtTND8(convertToInt(bloque.substring(314, 318).getBytes()));
				record.setTnd2amtTND9(convertToInt(bloque.substring(318, 322).getBytes()));
				record.setTnd3amtTND7(convertToInt(bloque.substring(322, 326).getBytes()));
				record.setTnd3amtTND8(convertToInt(bloque.substring(326, 330).getBytes()));
				record.setTnd3amtTND9(convertToInt(bloque.substring(330, 334).getBytes()));
				record.setTnd4amtTND7(convertToInt(bloque.substring(334, 338).getBytes()));
				record.setTnd4amtTND8(convertToInt(bloque.substring(338, 342).getBytes()));
				record.setTnd4amtTND9(convertToInt(bloque.substring(342, 346).getBytes()));
				record.setTnd5amtTND7(convertToInt(bloque.substring(346, 350).getBytes()));
				record.setTnd5amtTND8(convertToInt(bloque.substring(350, 354).getBytes()));
				record.setTnd5amtTND9(convertToInt(bloque.substring(354, 358).getBytes()));
				record.setTnd6amtTND7(convertToInt(bloque.substring(358, 362).getBytes()));
				record.setTnd6amtTND8(convertToInt(bloque.substring(362, 366).getBytes()));
				record.setTnd6amtTND9(convertToInt(bloque.substring(366, 370).getBytes()));
				record.setTnd7amtTND7(convertToInt(bloque.substring(370, 374).getBytes()));
				record.setTnd7amtTND8(convertToInt(bloque.substring(374, 378).getBytes()));
				record.setTnd7amtTND9(convertToInt(bloque.substring(378, 382).getBytes()));
				record.setTnd8amtTND7(convertToInt(bloque.substring(382, 386).getBytes()));
				record.setTnd8amtTND8(convertToInt(bloque.substring(386, 390).getBytes()));
				record.setTnd8amtTND9(convertToInt(bloque.substring(390, 394).getBytes()));
				record.setTnd1cntTND7(convertToInt(bloque.substring(394, 396).getBytes()));
				record.setTnd1cntTND8(convertToInt(bloque.substring(396, 398).getBytes()));
				record.setTnd1cntTND9(convertToInt(bloque.substring(398, 400).getBytes()));
				record.setTnd2cntTND7(convertToInt(bloque.substring(400, 402).getBytes()));
				record.setTnd2cntTND8(convertToInt(bloque.substring(402, 404).getBytes()));
				record.setTnd2cntTND9(convertToInt(bloque.substring(404, 406).getBytes()));
				record.setTnd3cntTND7(convertToInt(bloque.substring(406, 408).getBytes()));
				record.setTnd3cntTND8(convertToInt(bloque.substring(408, 410).getBytes()));
				record.setTnd3cntTND9(convertToInt(bloque.substring(410, 412).getBytes()));
				record.setTnd4cntTND7(convertToInt(bloque.substring(412, 414).getBytes()));
				record.setTnd4cntTND8(convertToInt(bloque.substring(414, 416).getBytes()));
				record.setTnd4cntTND9(convertToInt(bloque.substring(416, 418).getBytes()));
				record.setTnd5cntTND7(convertToInt(bloque.substring(418, 420).getBytes()));
				record.setTnd5cntTND8(convertToInt(bloque.substring(420, 422).getBytes()));
				record.setTnd5cntTND9(convertToInt(bloque.substring(422, 424).getBytes()));
				record.setTnd6cntTND7(convertToInt(bloque.substring(424, 426).getBytes()));
				record.setTnd6cntTND8(convertToInt(bloque.substring(426, 428).getBytes()));
				record.setTnd6cntTND9(convertToInt(bloque.substring(428, 430).getBytes()));
				record.setTnd7cntTND7(convertToInt(bloque.substring(430, 432).getBytes()));
				record.setTnd7cntTND8(convertToInt(bloque.substring(432, 434).getBytes()));
				record.setTnd7cntTND9(convertToInt(bloque.substring(434, 436).getBytes()));
				record.setTnd8cntTND7(convertToInt(bloque.substring(436, 438).getBytes()));
				record.setTnd8cntTND8(convertToInt(bloque.substring(438, 440).getBytes()));
				record.setTnd8cntTND9(convertToInt(bloque.substring(440, 442).getBytes()));
				record.setTypPrd(periodType);
				session.saveOrUpdate(record);
				tx.commit();

				// log.info(record.getRecordType() +" Registrado!");

			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					tx.rollback();
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		} else {
			tenderRecordPend.add(bloque);
		}
	}

	protected boolean isSet(byte value, int bit) {
		return (value & (1 << bit)) != 0;
	}

	public void initTx() {
		while (tx == null || !tx.isActive()) {
			try {
				tx = session.beginTransaction();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				tx = null;
			}
			if (tx == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA TRANSACCION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private File getNextFile() {

		File inFolder = null;

		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				Iterator itInFolders = pathActiveStore.iterator();
				File[] files = null;
				List filesStore = new ArrayList();
				int count = 0;
				while (itInFolders.hasNext()) {
					inFolder = new File((String) itInFolders.next());
					log.info("InFolder: " + inFolder.getAbsolutePath());
					File[] tempFiles = inFolder.listFiles(new FileFilter() {
						public boolean accept(File pathname) {
							return pathname.isFile() && (pathname.getName().equalsIgnoreCase("EAMACCTC.DAT")
									|| pathname.getName().equalsIgnoreCase("EAMACCTP.DAT"));
						}

					});
					if (tempFiles != null) {
						filesStore.add(tempFiles);
						count = count + tempFiles.length;
					}
				}
				if (count > 0) {
					files = new File[count];
					Iterator itFiles = filesStore.iterator();
					int countAux = 0;
					while (itFiles.hasNext()) {
						File[] temp = (File[]) itFiles.next();
						System.arraycopy(temp, 0, files, countAux, temp.length);
						countAux = countAux + temp.length;
					}
					this.filesToProcess = Arrays.asList(files).iterator();

				} else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				}

			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File) this.filesToProcess.next();
	}
	
	public boolean cleanTables() {
		Transaction tx = null;
		
		try {
			tx = session.beginTransaction();
			Query query = session.createSQLQuery("TRUNCATE TABLE LE_STR_REC_TOT");
			query.executeUpdate();
			Query query2 = session.createSQLQuery("TRUNCATE TABLE LE_TND_REC_TOT");
			query2.executeUpdate();
			Query query3 = session.createSQLQuery("TRUNCATE TABLE LE_IND_REC_TOT");
			query3.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}

}
