package com.allc.arms.utils.tsl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.allc.entities.ControlTransaction;
import com.allc.entities.RetailTransaction;
import com.allc.entities.RetailTransactionTotal;
import com.allc.entities.SignOffTransaction;
import com.allc.entities.TenderControlTransaction;
import com.allc.entities.Transaction;

public class TSLTransactionHeaderTypesMethods {
	
	static Logger log = Logger.getLogger(TSLTransactionHeaderTypesMethods.class);
	static Pattern p = Pattern.compile("\\|");
	
	@SuppressWarnings("rawtypes")
	public Object MethodProcess(Transaction transaction, List list){
		Object object = null;
		try {
				String trantype = list.get(TSLConstants.Type00.TRANTYPE).toString();
				
				if(trantype.equals(TSLConstants.TransactionHeaderTranType.CHECKOUT_TRANSACTION)){
					object = new RetailTransaction();
					checkoutTransaction(transaction, list, object);
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.TENDER_CASHING)){
					object = new TenderControlTransaction();
					tenderCashing(transaction, list, object);
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.TENDER_EXCHANGE)){
					object = new TenderControlTransaction();
					tenderExchange(transaction, list, object);
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.CASHIER_LOAN)){
					object = new TenderControlTransaction();
					cashierLoan(transaction, list, object);
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.CASHIER_PICKUP)){
					object = new TenderControlTransaction();
					cashierPickup(transaction, list, object);
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.TENDER_LISTING)){
					object = new TenderControlTransaction();
					tenderListing(transaction, list, object);
				/*else if(trantype.equals(TSLConstants.TransactionHeaderTranType.PRICE_VERIFY_CHANGE)){
					object = new ();*/
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.TRAINING_SESSION)){
					object = new RetailTransaction();
					trainingSession(transaction, list, object);
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.TERMINAL_TRANSFER)){
					object = new TenderControlTransaction();
					terminalTransfer(transaction, list, object);
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.TERMINAL_MONITOR)){
					object = new TenderControlTransaction();
					terminalMonitor(transaction, list, object);
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.TENDER_COUNT)){
					object = new TenderControlTransaction();
					tenderCount(transaction, list, object);
				/*}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.RESERVED1)){
					object = new ();*/
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.RETURN_ITEM_TRANSACTION)){
					object = new RetailTransaction();
					returnItemTransaction(transaction, list, object);
					/*}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.WIC_TRANSACTION)){
					object = new RetailTransaction();
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.RESERVED2)){
					object = new ();*/
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.REPRINT_TENDER_RECEIPT )){
					object = new ControlTransaction();
					reprintTenderReceipt(transaction, list, object);
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.VOIDED_CHECKOUT_TRANSACTION )){
					object = new RetailTransaction();
					voidedCheckoutTransaction(transaction, list, object);
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.OPERATOR_SIGN_OFF )){
					object = new ControlTransaction();
					OperatorSignOff(transaction, list, object);
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.STANDALONE_SESSION )){
					object = new RetailTransaction();
					standaloneSession(transaction, list, object);
				/*}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.EBT_BALANCE_INQUIRY )){
					object = new ();
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.VALUE_CARD_BALANCE_INQUIRY )){
					object = new ();
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.WIC_EBT_BALANCE_INQUIRY )){
					object = new ();
				}else if(trantype.equals(TSLConstants.TransactionHeaderTranType.DEPARTMENT_TOTALS_REPORT )){
					object = new TenderControlTransaction();*/
				}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return object;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void checkoutTransaction(Transaction transaction, List list, Object object){
		int sign = 1;
		try {

			
			RetailTransactionTotal retailTransactionTotal = null;
			
			if(null == ((RetailTransaction)object).getTotalItems())
				((RetailTransaction)object).setTotalItems(new ArrayList());

			/**TAX**/
			retailTransactionTotal = new RetailTransactionTotal();
			retailTransactionTotal.setAmount(new Double(0));
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_TAX_AMOUNT);
			((RetailTransaction)object).getTotalItems().add(retailTransactionTotal);

			
			/**GROSSPOS**/
			retailTransactionTotal = new RetailTransactionTotal();
			String grossPos = "".equalsIgnoreCase(list.get(TSLConstants.Type00.GROSSPOS).toString()) ? "0" : list.get(TSLConstants.Type00.GROSSPOS).toString();
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_GROSS_POSITIVE_AMOUNT);
			sign = 1;
			if(list.size() > TSLConstants.Type00.INDICAT1 && isBitOn(Long.parseLong(list.get(TSLConstants.Type00.INDICAT1).toString()), TSLConstants.Type00.INDICAT1_GROSS_POSITIVE_IS_NEGATIVE_BIT_POSITION)){
				sign = -1;
			}
			retailTransactionTotal.setAmount(new Double(grossPos) * sign);
			((RetailTransaction)object).getTotalItems().add(retailTransactionTotal);

			
			/**GROSSNEG**/
			retailTransactionTotal = new RetailTransactionTotal();
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_GROSS_NEGATIVE_AMOUNT);
			sign = 1;
			if(list.size() > TSLConstants.Type00.INDICAT1 && isBitOn(Long.parseLong(list.get(TSLConstants.Type00.INDICAT1).toString()), TSLConstants.Type00.INDICAT1_GROSS_NEGATIVE_IS_NEGATIVE_BIT_POSITION)){
				sign = -1;
			}
			String grossNeg = "".equalsIgnoreCase(list.get(TSLConstants.Type00.GROSSNEG).toString()) ? "0" : list.get(TSLConstants.Type00.GROSSNEG).toString();
			retailTransactionTotal.setAmount(new Double(grossNeg) * sign);
			((RetailTransaction)object).getTotalItems().add(retailTransactionTotal);			

			retailTransactionTotal = new RetailTransactionTotal();
			retailTransactionTotal.setAmount(new Double(0));
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_PROMO_POSITIVE_DISCOUNT);
			((RetailTransaction)object).getTotalItems().add(retailTransactionTotal);

			retailTransactionTotal = new RetailTransactionTotal();
			retailTransactionTotal.setAmount(new Double(0));
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_PROMO_NEGATIVE_DISCOUNT);
			((RetailTransaction)object).getTotalItems().add(retailTransactionTotal);
			
			retailTransactionTotal = new RetailTransactionTotal();
			retailTransactionTotal.setAmount(new Double(0));
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_POSITIVE_DISCOUNT);
			((RetailTransaction)object).getTotalItems().add(retailTransactionTotal);

			retailTransactionTotal = new RetailTransactionTotal();
			retailTransactionTotal.setAmount(new Double(0));
			retailTransactionTotal.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_NEGATIVE_DISCOUNT);
			((RetailTransaction)object).getTotalItems().add(retailTransactionTotal);
			//por defecto usamos 0=Ticket
			((RetailTransaction)object).setRetailTransactionTypeCode(0);
			transaction.setRetailTransaction((RetailTransaction)object);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	
	@SuppressWarnings("rawtypes")
	public void tenderCashing(Transaction transaction, List list, Object object){
		try {

			
			
			
			

			transaction.setTenderControlTransaction((TenderControlTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void tenderExchange(Transaction transaction, List list, Object object){
		try {

			
			
			
			

			transaction.setTenderControlTransaction((TenderControlTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void cashierLoan(Transaction transaction, List list, Object object){
		try {

			
			
			
			

			transaction.setTenderControlTransaction((TenderControlTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("rawtypes")
	public void cashierPickup(Transaction transaction, List list, Object object){
		try {

			
			
			
			

			transaction.setTenderControlTransaction((TenderControlTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void tenderListing(Transaction transaction, List list, Object object){
		try {

			
			
			
			

			transaction.setTenderControlTransaction((TenderControlTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void trainingSession(Transaction transaction, List list, Object object){
		try {

			transaction.setTrainingModeFlag(Boolean.TRUE);
			

			transaction.setRetailTransaction((RetailTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}	
	
	@SuppressWarnings("rawtypes")
	public void terminalTransfer(Transaction transaction, List list, Object object){
		try {

			
			
			
			

			transaction.setTenderControlTransaction((TenderControlTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void terminalMonitor(Transaction transaction, List list, Object object){
		try {

			
			
			
			

			transaction.setTenderControlTransaction((TenderControlTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}	
	
	@SuppressWarnings("rawtypes")
	public void tenderCount(Transaction transaction, List list, Object object){
		try {

			
			
			
			

			transaction.setTenderControlTransaction((TenderControlTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}	
	
	@SuppressWarnings("rawtypes")
	public void returnItemTransaction(Transaction transaction, List list, Object object){
		try {

			
			
			
			

			transaction.setRetailTransaction((RetailTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}		
	
	@SuppressWarnings("rawtypes")
	public void reprintTenderReceipt(Transaction transaction, List list, Object object){
		try {

			
			
			
			

			transaction.setControlTransaction((ControlTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}		
	
	@SuppressWarnings("rawtypes")
	public void voidedCheckoutTransaction(Transaction transaction, List list, Object object){
		try {
			transaction.setCancelFlag(Boolean.TRUE);

			transaction.setRetailTransaction((RetailTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}	
		
	@SuppressWarnings("rawtypes")
	public void OperatorSignOff(Transaction transaction, List list, Object object){
		try {
			
			if(list.size() > TSLConstants.Type00.INDICAT1 && isBitOn(Long.valueOf(list.get(TSLConstants.Type00.INDICAT1).toString()), TSLConstants.Type00.INDICAT1_SIGN_OFF_IS_FALSE_BIT_POSITION)){
				log.info("SignOffIsFalse");
				return;
			}
			
			SignOffTransaction signOffTransaction = new SignOffTransaction();
			
			signOffTransaction.setQuantityOfTransactions(new Integer(list.get(TSLConstants.Type00.NUMSTRING).toString().length() > 0 ? list.get(TSLConstants.Type00.NUMSTRING).toString() : "0"));
			
			((ControlTransaction)object).setSignOff(signOffTransaction);
			
			transaction.setControlTransaction((ControlTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void standaloneSession(Transaction transaction, List list, Object object){
		try {

			transaction.setKeyedOfflineFlag(Boolean.TRUE);
			
			
			

			transaction.setRetailTransaction((RetailTransaction)object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Used to know if the bit is On
	 * 
	 * @param value
	 *            value to check if some bit is On
	 * @param position
	 *            position to verify the bit, start with bit 0
	 * @return true if the bit is on, otherwise return false
	 */
	public Boolean isBitOn(long value, int position) {
		Boolean result = Boolean.FALSE;
		try {
			long mask = 1 << position;
			long tmp = value & mask;

			if (tmp != 0)
				result = Boolean.TRUE;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
}
