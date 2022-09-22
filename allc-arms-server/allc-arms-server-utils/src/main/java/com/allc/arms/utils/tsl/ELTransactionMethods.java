package com.allc.arms.utils.tsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import com.allc.entities.Operator;
import com.allc.entities.RetailStore;
import com.allc.entities.TenderControlTransaction;
import com.allc.entities.TenderControlTransactionLineItem;
import com.allc.entities.TenderLoanTransaction;
import com.allc.entities.TenderPickupTransaction;
import com.allc.entities.Transaction;
import com.allc.entities.Workstation;
import com.allc.util.ConstantsUtil;

public class ELTransactionMethods {

	static Logger log = Logger.getLogger(ELTransactionMethods.class);
	static Pattern p = Pattern.compile("\\|");
	static int sequence;

	@SuppressWarnings("rawtypes")
	public static void transactionProcess(Transaction transaction, String stringTypeData) {
		try {
			List stringTypesList = Arrays.asList(p.split(stringTypeData));

			switch (new Integer(stringTypesList.get(ELConstants.Positions.TRANTYPE).toString()).intValue()) {
				case ELConstants.ELTypeCode.CASHIER_LOAN_FROM_CONTROLLER: {
					ELTransactionMethods.transactionHeader(transaction, stringTypesList);
					ELTransactionMethods.loan(transaction, stringTypesList);
					ELTransactionMethods.tender(transaction, stringTypesList);
					break;
				}
				case ELConstants.ELTypeCode.CASHIER_PICKUP_FROM_CONTROLLER: {
					ELTransactionMethods.transactionHeader(transaction, stringTypesList);
					ELTransactionMethods.pickup(transaction, stringTypesList);
					ELTransactionMethods.tender(transaction, stringTypesList);
					break;
				}
				case ELConstants.ELTypeCode.TRANSFER_TENDER_FROM_CONTROLLER: {
					ELTransactionMethods.transactionHeader(transaction, stringTypesList);
					ELTransactionMethods.pickup(transaction, stringTypesList);
					ELTransactionMethods.tender(transaction, stringTypesList);
					break;
				}
				case ELConstants.ELTypeCode.CARRY_FORWARD_FROM_CONTROLLER: {
					ELTransactionMethods.transactionHeader(transaction, stringTypesList);
					break;
				}
				default: {
					System.out.println("String type not recognized " + stringTypesList.get(0));
					break;
				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public static void transactionHeader(Transaction transaction, List stringTypeList) {
		try {
			sequence = 0;

			Integer ringElapsedTime = null;
			Integer tenderElapsedTime = null;

			Operator operator = new Operator();
			Workstation workstation = new Workstation();
			RetailStore retailStore = new RetailStore();

			transaction.setOperator(operator);
			transaction.setWorkstation(workstation);
			transaction.setRetailStore(retailStore);
			transaction.setTransactionTypeCode(3);
			transaction.setVoidedFlag(Boolean.FALSE);
			transaction.setCancelFlag(Boolean.FALSE);
			transaction.setKeyedOfflineFlag(Boolean.FALSE);
			transaction.setTrainingModeFlag(Boolean.FALSE);
			String trantype = stringTypeList.get(ELConstants.Positions.TRANTYPE).toString();
			if (trantype.equals(String.valueOf(ELConstants.ELTypeCode.CARRY_FORWARD_FROM_CONTROLLER))) {
				transaction.setOperatorCode(stringTypeList.get(ELConstants.Positions.OPERATOR).toString());
			} else
				transaction.setOperatorCode(stringTypeList.get(ELConstants.Positions.OPERATOR_TERMINAL).toString());
			transaction.setWorkstationCode(ELConstants.TerminalCode.CONTROLLER_TERM);
			/** dateTimeEndOfTransaction **/
			Date dateTimeEndOfTransaction = TSLConstants.Formatters.TLOG_SUFIX_DATE_TIME_FORMATTER_ACE.parse((String) stringTypeList
					.get(ELConstants.Positions.DATETIME));

			transaction.setBeginDateTimeString(ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(DateUtils.addSeconds(
					dateTimeEndOfTransaction,
					-(ObjectUtils.defaultIfNull(ringElapsedTime, 0) + ObjectUtils.defaultIfNull(tenderElapsedTime, 0)))));
			/** endDateTimeString **/
			transaction.setEndDateTimeString(ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(dateTimeEndOfTransaction));

			/** sequenceNumber **/
			transaction.setSequenceNumber(new Integer(stringTypeList.get(ELConstants.Positions.TRANSNUM).toString()));

			/** get the object related to TRANTYPE **/
			ELTransactionHeaderTypesMethods.MethodProcess(transaction, stringTypeList);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static void pickup(Transaction transaction, List list) {
		try {
			Object object = getTypeOfTransaction(transaction);
			if (object instanceof TenderControlTransaction) {
				TenderControlTransaction tct = (TenderControlTransaction) object;
				TenderPickupTransaction pickup = tct.getPickup();
				tct.setTenderControlTypeCode(TSLConstants.Type13.PICKUP);
				pickup.setOperatorCode(list.get(ELConstants.Positions.OPERATOR_TERMINAL).toString());
//				pickup.setOutbound(list.get(ELConstants.Positions.TERMINAL).toString());
				pickup.setInbound(transaction.getRetailStoreCode());
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public static void loan(Transaction transaction, List list) {
		try {
			Object object = getTypeOfTransaction(transaction);
			if (object instanceof TenderControlTransaction) {
				TenderControlTransaction tct = (TenderControlTransaction) object;
				TenderLoanTransaction loan = tct.getLoan();
				tct.setTenderControlTypeCode(TSLConstants.Type13.LOAN);
				loan.setOperatorCode(list.get(ELConstants.Positions.OPERATOR_TERMINAL).toString());
				loan.setOutbound(transaction.getRetailStoreCode());
//				loan.setInbound(list.get(ELConstants.Positions.TERMINAL).toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void tender(Transaction transaction, List list) {
		try {
			Object object = getTypeOfTransaction(transaction);
			if (object instanceof TenderControlTransaction) {
				TenderControlTransactionLineItem tenderControlTransactionLineItem = new TenderControlTransactionLineItem();
				tenderControlTransactionLineItem.setSequenceNumber(++sequence);
				tenderControlTransactionLineItem.setTenderTypeCode("11");
				tenderControlTransactionLineItem.setAmount((new Double(list.get(ELConstants.Positions.CASH).toString())));

				if (null == transaction.getTenderControlTransaction().getLineItems())
					transaction.getTenderControlTransaction().setLineItems(new ArrayList());
				transaction.getTenderControlTransaction().getLineItems().add(tenderControlTransactionLineItem);

				TenderControlTransactionLineItem tenderControlTransactionLineItemCheq = new TenderControlTransactionLineItem();
				tenderControlTransactionLineItemCheq.setSequenceNumber(++sequence);
				tenderControlTransactionLineItemCheq.setTenderTypeCode("21");
				tenderControlTransactionLineItemCheq.setAmount((new Double(list.get(ELConstants.Positions.CHECKS).toString())));

				if (null == transaction.getTenderControlTransaction().getLineItems())
					transaction.getTenderControlTransaction().setLineItems(new ArrayList());
				transaction.getTenderControlTransaction().getLineItems().add(tenderControlTransactionLineItemCheq);

			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static Object getTypeOfTransaction(Transaction transaction) {
		Object tmp = null;
		try {
			if (null != transaction.getControlTransaction()) {
				tmp = transaction.getControlTransaction();
			} else if (null != transaction.getRetailTransaction()) {
				tmp = transaction.getRetailTransaction();
			} else if (null != transaction.getTenderControlTransaction()) {
				tmp = transaction.getTenderControlTransaction();
			} else {
				log.error("type of transaction not defined");
				tmp = null;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return tmp;
	}

}
