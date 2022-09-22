package com.allc.arms.server.processes.cer.saveTrx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.tsl.TSLConstants;
import com.allc.converters.ConverterToXML;
import com.allc.core.process.AbstractProcess;
import com.allc.entities.CouponPromotionData;
import com.allc.entities.DeducibleData;
import com.allc.entities.FacturaElec;
import com.allc.entities.InvoiceData;
import com.allc.entities.MotoData;
import com.allc.entities.Operator;
import com.allc.entities.PromotionDiscount;
import com.allc.entities.RetailTransaction;
import com.allc.entities.RetailTransactionLineItem;
import com.allc.entities.RetailTransactionTotal;
import com.allc.entities.SaleReturnLineItem;
import com.allc.entities.TaxLineItem;
import com.allc.entities.TenderLineItem;
import com.allc.entities.TenderPinpadInfo;
import com.allc.entities.Transaction;
import com.allc.entities.VentaMayoreoItem;
import com.allc.entities.Workstation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.util.ConstantsUtil;

public class SaveTransactionsProcess extends AbstractProcess {

	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private static Logger logger = Logger.getLogger(SaveTransactionsProcess.class);
	private boolean isEnd = false;
	protected boolean finished = false;
	private File inFolder;
	private File bkpFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	private BufferedReader reader;
	static int sequence;
	static TenderPinpadInfo tenderPinpadTemp;
	String value;
	String nameSync = null;
	String syncPath = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	protected SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
	protected static String tslDefaultLocalRepositoryToStore;
	int cantTrx;
	int seqPromo;
	private Session sesion;
	private org.hibernate.Transaction tx;
	SimpleDateFormat TLOG_SUFIX_DATE_TIME_FORMATTER = new SimpleDateFormat("yyyyMMddHHmm");

	protected void inicializar() {
		isEnd = false;
		try {
			value = null;
			// inFolder = new
			// File(properties.getObject("saveTransaction.in.folder"));
			inFolder = new File("C:/ALLC/WWW/EYES/ARSAP/IN");
			// bkpFolder = new
			// File(properties.getObject("saveTransaction.bkp.folder"));
			bkpFolder = new File("C:/ALLC/WWW/EYES/ARSAP/BKP");
			// sleepTime = properties.getInt("saveTransaction.sleeptime");
			sleepTime = 3000;

			tslDefaultLocalRepositoryToStore = properties.getObject("TSL.defaultLocalRepositoryToStore");

			if (StringUtils.isNotBlank(tslDefaultLocalRepositoryToStore))
				UtilityFile.createDir(tslDefaultLocalRepositoryToStore);
			logger.info("TSL.defaultLocalRepositoryToStore: " + tslDefaultLocalRepositoryToStore);

			syncPath = properties.getObject("searchEbil.sync.folder.path");

			cantTrx = 0;

			seqPromo = 1;

			openSession();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void run() {
		logger.info("Iniciando SaveTransactionsProcess...");
		inicializar();
		File fileToProcess = null;
		String storeCode = properties.getObject("eyes.store.code");

		com.allc.entities.Transaction transaction = null;
		while (!isEnd) {
			try {
				fileToProcess = getNextTrxFile();
				if (fileToProcess != null) {
					logger.info("Archivo a procesar: " + fileToProcess.getName().toUpperCase());
					UtilityFile.createWriteDataFile(getEyesFileName(),
							"SAVE_TRX_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ storeCode + "|STR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Archivo a Procesar: " + fileToProcess.getName() + ".\n",
							true);
					reader = new BufferedReader(new FileReader(fileToProcess));

					String line = null;
					while ((line = reader.readLine()) != null && !isEnd) {

						try {
							String[] lineParts = line.split(",");
							String trxHeader = lineParts[0];
							if (trxHeader.startsWith("00")) {
								transaction = new Transaction();
								transactionHeader(transaction, trxHeader);
								for (int i = 1; i <= lineParts.length - 1; i++) {
									String[] info = lineParts[i].split("\\|");
									switch (new Integer(info[0])) {
									case TSLConstants.StringTypeCode.ITEM_ENTRY:
										itemEntry(transaction, info);
										break;
									case TSLConstants.StringTypeCode.TENDER:
										tender(transaction, info);
										break;
									case TSLConstants.StringTypeCode.TAX:
										tax(transaction, info);
										break;
									case TSLConstants.StringTypeCode.CHANGE:
										change(transaction, info);
										break;
									case 10:
										infoFactura(transaction, info);
										break;
									case 11:
										promoInfo(transaction, info);
										break;
									case 12:
										invoiceDataInfo(transaction, info);
										break;
									case 13:
										webCouponInfo(transaction, info);
										break;
									case 14:
										extraPointsInfo(transaction, info);
										break;
									case 15:
										motoInfo(transaction, info);
										break;
									case 16:
										decuciblesInfo(transaction, info);
										break;
									case 17:
										cuposConsumoInfo(transaction, info);
										break;
									case 18:
										mayoreoInfo(transaction, info);
										break;
									default:
										System.out.println("String type not recognized " + info[0]);
										break;

									}
								}

							}
							if (transaction != null) {
								generateXMLToSave(transaction, storeCode);
								seqPromo = 1;
								cantTrx++;
							}

						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}

					}

					File outDel = new File(bkpFolder, fileToProcess.getName());
					if (outDel.exists())
						outDel.delete();
					File out = new File(bkpFolder, fileToProcess.getName());
					reader.close();

					if (fileToProcess.renameTo(out)) {
						logger.info("Archivo procesado correctamente.");
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"SAVE_TRX_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
										+ storeCode + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|Archivo: " + fileToProcess.getName() + " procesado correctamente.\n",
								true);

					} else
						logger.info("El archivo no se pudo mover.");

				}

			} catch (Exception e) {
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"SAVE_TRX_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode
								+ "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al procesar el Archivo: " + fileToProcess.getName().toUpperCase() + ".\n",
						true);
				logger.error(e.getMessage(), e);
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
		finished = true;
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public void transactionHeader(Transaction transaction, String trx) {
		try {
			sequence = 0;
			String[] trxInfo = trx.split("\\|");

			Integer ringElapsedTime = 0;
			Integer tenderElapsedTime = 0;

			Operator operator = new Operator();
			Workstation workstation = new Workstation();

			transaction.setOperator(operator);
			transaction.setWorkstation(workstation);

			transaction.setVoidedFlag(Boolean.FALSE);
			transaction.setCancelFlag(Boolean.FALSE);
			transaction.setKeyedOfflineFlag(Boolean.FALSE);
			transaction.setTrainingModeFlag(Boolean.FALSE);

			transaction.setWorkstationCode(trxInfo[1]);
			transaction.setRetailStoreCode(trxInfo[4]);
			transaction.setOperatorCode(trxInfo[5]);

			Date dateTimeBeginOfTransaction = TLOG_SUFIX_DATE_TIME_FORMATTER.parse(trxInfo[6]);
			Date dateTimeEndOfTransaction = TLOG_SUFIX_DATE_TIME_FORMATTER.parse(trxInfo[7]);

			transaction.setBeginDateTimeString(
					ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(dateTimeBeginOfTransaction));
			transaction.setEndDateTimeString(
					ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(dateTimeEndOfTransaction));

			transaction.setSequenceNumber(Integer.valueOf(trxInfo[2]));

			Date businessDateDay = TSLConstants.Formatters.TLOG_SUFIX_DATE_TIME_FORMATTER_ACE_2.parse(trxInfo[3]);
			transaction.setBusinessDayDateString(
					ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(businessDateDay).toString());

			Integer lockElapsedTime = 0;
			Integer idleElapsedTime = 0;

			transaction.setTransactionTypeCode(1);

			RetailTransaction retailTransaction = new RetailTransaction();

			transaction.setRetailTransaction(retailTransaction);

			transaction.getRetailTransaction().setRingElapsedTime(ringElapsedTime);
			transaction.getRetailTransaction().setTenderElapsedTime(tenderElapsedTime);
			transaction.getRetailTransaction().setIdleElapsedTime(idleElapsedTime);
			transaction.getRetailTransaction().setLockElapsedTime(lockElapsedTime);

			if (null == transaction.getRetailTransaction().getTotalItems())
				transaction.getRetailTransaction().setTotalItems(new ArrayList());

			/** GROSSPOS **/
			RetailTransactionTotal retailTransactionTotal = new RetailTransactionTotal();
			retailTransactionTotal.setAmount(new Double(trxInfo[8]));
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_GROSS_POSITIVE_AMOUNT);
			transaction.getRetailTransaction().getTotalItems().add(retailTransactionTotal);

			/** GROSSNEG **/
			retailTransactionTotal = new RetailTransactionTotal();
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_GROSS_NEGATIVE_AMOUNT);
			retailTransactionTotal.setAmount(new Double(trxInfo[9]));
			transaction.getRetailTransaction().getTotalItems().add(retailTransactionTotal);

			retailTransactionTotal = new RetailTransactionTotal();
			retailTransactionTotal.setAmount(new Double(0));
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_PROMO_POSITIVE_DISCOUNT);
			transaction.getRetailTransaction().getTotalItems().add(retailTransactionTotal);

			retailTransactionTotal = new RetailTransactionTotal();
			retailTransactionTotal.setAmount(new Double(0));
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_PROMO_NEGATIVE_DISCOUNT);
			transaction.getRetailTransaction().getTotalItems().add(retailTransactionTotal);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void itemEntry(Transaction transaction, String[] info) {

		try {
			int sign = 1;

			RetailTransactionLineItem retailTransactionLineItem = new RetailTransactionLineItem();
			retailTransactionLineItem.setSequenceNumber(++sequence);

			retailTransactionLineItem.setVoidFlag(Boolean.FALSE);

			SaleReturnLineItem saleReturnLineItem = new SaleReturnLineItem();

			saleReturnLineItem.setQuantity(Double.valueOf(info[5]));
			saleReturnLineItem.setUnits(Double.valueOf(info[6]));
			saleReturnLineItem.setExtendedAmount(new Double(info[3]));

			saleReturnLineItem.setItemCode(Long.valueOf(info[1]));
			saleReturnLineItem.setOrdinalNumber(Integer.valueOf(info[9]));

			saleReturnLineItem.setRegularSalesUnitPrice(new Double(info[2]));
			saleReturnLineItem.setTaxType(info[7]);
			saleReturnLineItem.setAppliedTax(Double.valueOf(info[8]));
			saleReturnLineItem.setEntryMethodCode(info[4]);
			
			saleReturnLineItem.setItemType(0);
			
			int mayoreo = Integer.valueOf(info[10]);
			
			saleReturnLineItem.setIsPorMayor(mayoreo == 0 ? false : true);

			retailTransactionLineItem.setSaleLI(saleReturnLineItem);
			retailTransactionLineItem.setItemTypeCode(TSLConstants.Type0708.SALE);
			if (null == transaction.getRetailTransaction().getLineItems())
				transaction.getRetailTransaction().setLineItems(new ArrayList());

			transaction.getRetailTransaction().getLineItems().add(retailTransactionLineItem);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public static void tender(Transaction transaction, String[] info) {
		try {

			TenderLineItem tenderLineItem = new TenderLineItem();

			tenderLineItem.setTenderTypeCode(info[1]);
			tenderLineItem.setAmount(Double.valueOf(info[2]));
			tenderLineItem.setFeeAmount(Double.valueOf("0"));
			tenderLineItem.setStatus(0);
			tenderLineItem.setTenderAccountNumber(info[3]);

			tenderLineItem.setIsChangeFlag(Boolean.FALSE);

			RetailTransactionLineItem retailTransactionLineItem = new RetailTransactionLineItem();
			retailTransactionLineItem.setSequenceNumber(++sequence);

			retailTransactionLineItem.setVoidFlag(Boolean.FALSE);

			retailTransactionLineItem.setTender(tenderLineItem);

			if (null == transaction.getRetailTransaction().getLineItems())
				transaction.getRetailTransaction().setLineItems(new ArrayList());
			transaction.getRetailTransaction().getLineItems().add(retailTransactionLineItem);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void tax(Transaction transaction, String[] info) {
		try {
			int sign = 1;
			TaxLineItem taxLineItem = null;
			taxLineItem = new TaxLineItem();
			taxLineItem.setTaxType(info[2]);
			taxLineItem.setTaxAmount((new Double(info[3])) * sign);
			taxLineItem.setTaxableAmount(new Double(info[4]));

			RetailTransactionLineItem retailTransactionLineItem = new RetailTransactionLineItem();
			retailTransactionLineItem.setSequenceNumber(++sequence);
			retailTransactionLineItem.setItemTypeCode(Integer.valueOf(info[1]));

			retailTransactionLineItem.setTax(taxLineItem);

			if (null == transaction.getRetailTransaction().getLineItems())
				transaction.getRetailTransaction().setLineItems(new ArrayList());
			transaction.getRetailTransaction().getLineItems().add(retailTransactionLineItem);

			RetailTransactionTotal retailTransactionTotal = new RetailTransactionTotal();
			retailTransactionTotal.setAmount(new Double(info[3]));
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_TAX_AMOUNT);
			transaction.getRetailTransaction().getTotalItems().add(retailTransactionTotal);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void change(Transaction transaction, String[] info) {
		try {
			RetailTransactionLineItem retailTransactionLineItem = new RetailTransactionLineItem();
			retailTransactionLineItem.setSequenceNumber(++sequence);

			TenderLineItem tenderLineItem = new TenderLineItem();

			tenderLineItem.setAmount(new Double(info[2]));
			tenderLineItem.setTenderTypeCode(info[1]);
			tenderLineItem.setTenderAccountNumber("");
			tenderLineItem.setIsChangeFlag(Boolean.TRUE);
			retailTransactionLineItem.setTender(tenderLineItem);

			retailTransactionLineItem.setVoidFlag(Boolean.FALSE);
			if (null != transaction.getRetailTransaction()) {
				if (null == transaction.getRetailTransaction().getLineItems())
					transaction.getRetailTransaction().setLineItems(new ArrayList());
				transaction.getRetailTransaction().getLineItems().add(retailTransactionLineItem);
			}
			if (null != transaction.getTenderControlTransaction()) {
				if (null == transaction.getTenderControlTransaction().getLineItems())
					transaction.getTenderControlTransaction().setLineItems(new ArrayList());
				transaction.getTenderControlTransaction().getLineItems().add(retailTransactionLineItem);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void infoFactura(Transaction transaction, String[] info) {

		try {

			FacturaElec facElec = new FacturaElec();
			facElec.setFecha(info[1]);
			facElec.setHora(info[2]);
			facElec.setNumeroFac(info[3]);
			facElec.setSubTotal(info[4]);
			facElec.setTotal(info[5]);

			if (null == transaction.getRetailTransaction().getFacturaElec()) {
				transaction.getRetailTransaction().setFacturaElec(facElec);
				transaction.getRetailTransaction().setRetailTransactionTypeCode(1);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public void promoInfo(Transaction transaction, String[] info) {

		try {

			PromotionDiscount promoDisc = new PromotionDiscount();
			promoDisc.setOrdinalNumber(Integer.valueOf(info[3]));
			promoDisc.setSequenceNumber(Integer.valueOf(info[3]));
			promoDisc.setAmount(Double.valueOf(info[2]));
			promoDisc.setPromotionCode(info[1]);
			promoDisc.setPromotionSequenceNumber(seqPromo);
			promoDisc.setFlvd(false);
			if (transaction.getRetailTransaction().getPromotionDiscs() == null)
				transaction.getRetailTransaction().setPromotionDiscs(new ArrayList());
			transaction.getRetailTransaction().getPromotionDiscs().add(promoDisc);
			seqPromo++;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public static void invoiceDataInfo(Transaction transaction, String[] info) {

		try {

			InvoiceData invoiceData = new InvoiceData();
			invoiceData.setCustomerID(info[1]);

			if ("1".equals(info[3]))
				invoiceData.setCustomerType(info[3]);
			else if ("0".equals(info[3]) && invoiceData.getCustomerID().length() > 10)
				invoiceData.setCustomerType("2");
			else
				invoiceData.setCustomerType("3");

			invoiceData.setCustomerName(info[2]);

			if (null == transaction.getRetailTransaction().getInvoiceData())
				transaction.getRetailTransaction().setInvoiceData(invoiceData);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public static void webCouponInfo(Transaction transaction, String[] info) {

		try {

			CouponPromotionData couponPrmData = new CouponPromotionData();
			couponPrmData.setSequenceNumber(transaction.getRetailTransaction().getCouponsPromotionData() == null ? 1
					: transaction.getRetailTransaction().getCouponsPromotionData().size() + 1);
			couponPrmData.setCupon(info[1]);
			couponPrmData.setCantidad(Integer.valueOf(info[2]));
			transaction.getRetailTransaction().getCouponsPromotionData().add(couponPrmData);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public void extraPointsInfo(Transaction transaction, String[] info) {

		try {
			updatePuntos(transaction, info);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public static void motoInfo(Transaction transaction, String[] info) {

		try {

			MotoData motoData = new MotoData();
			motoData.setItemCode(Long.valueOf(info[1]));
			motoData.setSerialNumber(info[2]);
			motoData.setCodGerente(new Integer(info[3]));
			motoData.setSequenceNumber(transaction.getRetailTransaction().getMotosData() == null ? 1
					: transaction.getRetailTransaction().getMotosData().size() + 1);
			if (transaction.getRetailTransaction().getMotosData() == null)
				transaction.getRetailTransaction().setMotosData(new ArrayList());
			transaction.getRetailTransaction().getMotosData().add(motoData);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public static void decuciblesInfo(Transaction transaction, String[] info) {

		try {

			DeducibleData deducibleData = new DeducibleData();
			deducibleData.setComestible(info[1]);
			deducibleData.setRopa(info[2]);
			deducibleData.setEscolar(info[3]);

			if (null == transaction.getRetailTransaction().getDeducibleData())
				transaction.getRetailTransaction().setDeducibleData(deducibleData);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public void cuposConsumoInfo(Transaction transaction, String[] info) {

		try {
			updateConsumos(transaction, info);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}
	
	public void mayoreoInfo(Transaction transaction, String[] info) {

		try {
			
			VentaMayoreoItem vtaMayoreo = new VentaMayoreoItem();
			
			int lastSRlineItem = getLastSaleReturnLineItemSeqNum(transaction.getRetailTransaction().getLineItems());
			vtaMayoreo.setSequenceNumber(lastSRlineItem);
			vtaMayoreo.setSequenceNumberMay(++sequence);
			
			// Se crea item SERVICE para guardar en la TR_LTM_SLS_RTN el item donde se refleja el descuento por caja
			
			RetailTransactionLineItem retailTransactionLineItemService = new RetailTransactionLineItem();
			retailTransactionLineItemService.setSequenceNumber(vtaMayoreo.getSequenceNumberMay());

			retailTransactionLineItemService.setVoidFlag(Boolean.FALSE);

			SaleReturnLineItem saleReturnLineItemService = new SaleReturnLineItem();

			saleReturnLineItemService.setQuantity(Double.valueOf("1"));
			
			saleReturnLineItemService.setItemType(7);
			
			saleReturnLineItemService.setItemCode(Long.valueOf("811099999997"));
			
			saleReturnLineItemService.setExtendedAmount(new Double(info[1]));
			
			saleReturnLineItemService.setOrdinalNumber(0);

			saleReturnLineItemService.setRegularSalesUnitPrice(new Double(info[1]));
			
			saleReturnLineItemService.setEntryMethodCode("SERVICE");
			
			saleReturnLineItemService.setIsPorMayor(false);

			retailTransactionLineItemService.setSaleLI(saleReturnLineItemService);
			
			if (null == transaction.getRetailTransaction().getLineItems())
				transaction.getRetailTransaction().setLineItems(new ArrayList());

			transaction.getRetailTransaction().getLineItems().add(retailTransactionLineItemService);
			
			if (transaction.getRetailTransaction().getItemsVentaMayoreo() == null)
				transaction.getRetailTransaction().setItemsVentaMayoreo(new ArrayList());
			transaction.getRetailTransaction().getItemsVentaMayoreo().add(vtaMayoreo);
			
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	private File getNextTrxFile() {
		logger.info("Buscando archivos para guardar Transacciones.");
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile() && (pathname.getName().toUpperCase().startsWith("TRX"));
					}
				});
				if (files.length == 0) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				} else {

					this.filesToProcess = Arrays.asList(files).iterator();
				}
			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File) this.filesToProcess.next();
	}

	private static int getNextPromotionLineItemSeqNum(List lineItems, int promoDiscCount) {
		Iterator itLineItems = lineItems.iterator();
		while (itLineItems.hasNext()) {
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) itLineItems.next();
			SaleReturnLineItem saleReturnLineItem = null;
			if (lineItem.getSaleLI() != null) {
				saleReturnLineItem = lineItem.getSaleLI();
			} else if (lineItem.getReturnLI() != null) {
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if (saleReturnLineItem != null && "SERVICE".equalsIgnoreCase(saleReturnLineItem.getEntryMethodCode())
					&& saleReturnLineItem.getItemCode().compareTo(Long.valueOf(811099999998L)) == 0) {
				if (promoDiscCount == 0) {
					return saleReturnLineItem.getSequenceNumber();
				}
				promoDiscCount--;
			}
		}
		return 0;
	}

	public void generateXMLToSave(Transaction transaction, String storeCode) {
		String value = null;
		String fecha = null;
		String name = null;
		String data = null;
		ConverterToXML converterToXML = new ConverterToXML();

		try {
			value = converterToXML.getXmlByTransaction(transaction);
			fecha = sdf.format(transaction.getBusinessDayDate()).toString();

			if (Integer.valueOf(storeCode) > 0) {
				nameSync = tslDefaultLocalRepositoryToStore + File.separator + syncPath + File.separator
						+ transaction.getRetailStoreCode() + "-" + transaction.getWorkstationCode() + "-"
						+ transaction.getSequenceNumber() + "-" + fecha + "-" + cantTrx + ".xml";
				boolean nameExist = true;
				int seq = 0;
				while (nameExist) {
					File file = new File(nameSync + (seq > 0 ? "-" + seq : ""));
					if (!file.exists())
						nameExist = false;
					else
						seq++;
				}
				UtilityFile.createWriteDataFile(nameSync, value, false);
			}
			boolean nameExist = true;
			int seq = 0;
			while (nameExist) {
				File file = new File(name + (seq > 0 ? "-" + seq : ""));
				if (!file.exists())
					nameExist = false;
				else
					seq++;
			}
			name = tslDefaultLocalRepositoryToStore + File.separator + transaction.getRetailStoreCode() + "-"
					+ transaction.getWorkstationCode() + "-" + transaction.getSequenceNumber() + "-" + fecha + "-"
					+ cantTrx + ".xml";
			UtilityFile.createWriteDataFile(name, value, false);
			logger.info("Tlog generado: " + name);
			logger.debug(value);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	private void updatePuntos(Transaction transaction, String[] data) throws Exception {
		try {
			initTx();
			Integer store = Integer.valueOf(transaction.getRetailStoreCode());
			String terminal = transaction.getWorkstationCode();
			while (terminal.length() < 4) {
				terminal = "0" + terminal;
			}
			Date fecha = transaction.getBusinessDayDate();
			Integer trxNum = Integer.valueOf(transaction.getSequenceNumber());
			Integer ordNum = Integer.valueOf(data[3]);
			Integer ptos = Integer.valueOf(data[2]);
			String cdPromo = data[1];

			// boolean ordinalFound = true;
			String itemCode = "";
			// while(ordinalFound){
			Iterator items = transaction.getRetailTransaction().getLineItems().iterator();
			while (items.hasNext()) {
				Object item = items.next();
				if (item instanceof RetailTransactionLineItem) {
					RetailTransactionLineItem lineItem = (RetailTransactionLineItem) item;
					if (lineItem.getSaleLI() != null) {
						SaleReturnLineItem saleItem = lineItem.getSaleLI();
						if (saleItem.getOrdinalNumber() == ordNum) {
							// ordinalFound = false;
							itemCode = String.valueOf(saleItem.getItemCode());
							break;
						}
					}
				}

			}
			// }
			Long item = Long.valueOf(itemCode);
			Query query = sesion.createSQLQuery(
					"INSERT INTO CO_PTS_DT (CD_STR_RT, CD_WS, DC_DY_BSN, AI_TRN, CD_ITM, ORDL_NBR, PTS, CD_PRM) VALUES (:valor1, :valor2, :valor3, :valor4, :valor5, :valor6, :valor7, :valor8)");
			query.setParameter("valor1", store);
			query.setParameter("valor2", terminal);
			query.setParameter("valor3", fecha);
			query.setParameter("valor4", trxNum);
			query.setParameter("valor5", item);
			query.setParameter("valor6", ordNum);
			query.setParameter("valor7", ptos);
			query.setParameter("valor8", cdPromo);
			logger.info("Query: " + query.getQueryString());
			query.executeUpdate();

			tx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try {
				tx.rollback();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	private void updateConsumos(Transaction transaction, String[] data) throws Exception {
		try {
			initTx();
			String accountNumber = data[1];
			String sequence = data[2];
			String autNum = data[3];
			String respCode = data[6];
			Integer monto1 = Integer.valueOf(data[4]);
			String factura = transaction.getRetailTransaction().getFacturaElec().getNumeroFac();
			Integer monto2 = Integer.valueOf(data[5]);
			Query query = sesion.createSQLQuery(
					"INSERT INTO CO_CSM_SYS_DT (INVC_NMB, ACNT_NMB, SEQ_TRX, AUT_NMB, AMNT_1, AMNT_2, RSP_CD) VALUES (:valor1, :valor2, :valor3, :valor4, :valor5, :valor6, :valor7)");
			query.setParameter("valor1", factura);
			query.setParameter("valor2", accountNumber);
			query.setParameter("valor3", sequence);
			query.setParameter("valor4", autNum);
			query.setParameter("valor5", monto1);
			query.setParameter("valor6", monto2);
			query.setParameter("valor7", respCode);
			logger.info("Query: " + query.getQueryString());
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try {
				tx.rollback();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	public void openSession() {
		while (sesion == null) {
			try {
				sesion = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				sesion = null;
			}
			if (sesion == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA SESION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}

	public void initTx() {
		while (tx == null || !tx.isActive()) {
			try {
				tx = sesion.beginTransaction();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				tx = null;
			}
			if (tx == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA TRANSACCION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}
	
	private static int getLastSaleReturnLineItemSeqNum(List lineItems){
		int cant = lineItems.size();
		for(int i = cant-1; i >= 0; i--){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) lineItems.get(i);
			SaleReturnLineItem saleReturnLineItem = null;
			if(lineItem.getSaleLI() != null){
				saleReturnLineItem = lineItem.getSaleLI();
			} else if(lineItem.getReturnLI() != null){
				saleReturnLineItem = lineItem.getReturnLI();
			}
			
			logger.info(saleReturnLineItem.getEntryMethodCode());
			logger.info(saleReturnLineItem.getItemType());
			logger.info(saleReturnLineItem.getSequenceNumber());
			
			if (saleReturnLineItem != null && !"SERVICE".equalsIgnoreCase(saleReturnLineItem.getEntryMethodCode()) && !saleReturnLineItem.getItemType().equals(Integer.valueOf(TSLConstants.Type01.INDICAT3_T_STORE_COUPON_VALUE))){
				return saleReturnLineItem.getSequenceNumber();
			}
		}
		return 0;
	}

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		logger.info("Deteniendo SaveTransactionsProcess...");
		while (!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					return false;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		logger.info("Finalizó el Proceso de Guardado de Transacciones en Base de Datos.");
		return true;
	}

}
