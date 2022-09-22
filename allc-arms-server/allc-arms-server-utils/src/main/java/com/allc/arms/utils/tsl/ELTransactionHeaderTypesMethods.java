package com.allc.arms.utils.tsl;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.allc.entities.CarryForwardTransaction;
import com.allc.entities.ControlTransaction;
import com.allc.entities.TenderControlTransaction;
import com.allc.entities.TenderLoanTransaction;
import com.allc.entities.TenderPickupTransaction;
import com.allc.entities.Transaction;

public class ELTransactionHeaderTypesMethods {

	static Logger log = Logger.getLogger(ELTransactionHeaderTypesMethods.class);
	static Pattern p = Pattern.compile("\\|");

	@SuppressWarnings("rawtypes")
	public static Object MethodProcess(Transaction transaction, List list) {
		Object object = null;
		try {
			String trantype = list.get(ELConstants.Positions.TRANTYPE).toString();
			if (trantype.equals(String.valueOf(ELConstants.ELTypeCode.CASHIER_LOAN_FROM_CONTROLLER))) {
				object = new TenderControlTransaction();
				cashierLoan(transaction, list, object);
			} else if (trantype.equals(String.valueOf(ELConstants.ELTypeCode.CASHIER_PICKUP_FROM_CONTROLLER))
					|| trantype.equals(String.valueOf(ELConstants.ELTypeCode.TRANSFER_TENDER_FROM_CONTROLLER))) {
				object = new TenderControlTransaction();
				cashierPickup(transaction, list, object);
			} else if (trantype.equals(String.valueOf(ELConstants.ELTypeCode.CARRY_FORWARD_FROM_CONTROLLER))) {
				object = new ControlTransaction();
				carryForward(transaction, list, object);
			} 
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return object;
	}

	@SuppressWarnings("rawtypes")
	public static void cashierLoan(Transaction transaction, List list, Object object) {
		try {
			TenderControlTransaction tct = (TenderControlTransaction) object;
			tct.setLoan(new TenderLoanTransaction());
			transaction.setTenderControlTransaction(tct);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("rawtypes")
	public static void cashierPickup(Transaction transaction, List list, Object object) {
		try {
			TenderControlTransaction tct = (TenderControlTransaction) object;
			tct.setPickup(new TenderPickupTransaction());
			transaction.setTenderControlTransaction(tct);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void carryForward(Transaction transaction, List list, Object object) {
		try {
			ControlTransaction ct = (ControlTransaction) object;
			ct.setCarryForward(new CarryForwardTransaction());
			transaction.setControlTransaction(ct);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
