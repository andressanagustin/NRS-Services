package com.allc.arms.utils.tsl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.entities.AssociatedCoupon;
import com.allc.entities.BusinessEODTransaction;
import com.allc.entities.ControlTransaction;
import com.allc.entities.DeducibleData;
import com.allc.entities.EcommerceData;
import com.allc.entities.ExceptionLog;
import com.allc.entities.FacturaElec;
import com.allc.entities.InvoiceData;
import com.allc.entities.ManagerOverride;
import com.allc.entities.Operator;
import com.allc.entities.PointsRedemptionData;
import com.allc.entities.PreferredCustomerData;
import com.allc.entities.PriceModificationLineItem;
import com.allc.entities.PromotionDiscount;
import com.allc.entities.ResumenItem;
import com.allc.entities.RetailTransaction;
import com.allc.entities.RetailTransactionLineItem;
import com.allc.entities.RetailTransactionTotal;
import com.allc.entities.ReturnTransaction;
import com.allc.entities.SaleReturnLineItem;
import com.allc.entities.SignOnTransaction;
import com.allc.entities.StringUsuario;
import com.allc.entities.TaxLineItem;
import com.allc.entities.TenderControlTransaction;
import com.allc.entities.TenderControlTransactionLineItem;
import com.allc.entities.TenderLineItem;
import com.allc.entities.TenderLoanTransaction;
import com.allc.entities.TenderPickupTransaction;
import com.allc.entities.TenderPinpadInfo;
import com.allc.entities.TenderReturnLineItem;
import com.allc.entities.Transaction;
import com.allc.entities.Workstation;
import com.allc.util.ConstantsUtil;


public class TSLTransactionMethods {

	static Logger log = Logger.getLogger(TSLTransactionMethods.class);
	static Pattern p = Pattern.compile("\\|");
	protected int sequence;
	protected TenderPinpadInfo tenderPinpadTemp;
	protected double totalTax = 0;
	protected double currentTax = 0;
	protected long currentItemCode = 0;
	protected int currentOrdinalNumber = 0;
	protected double currentTaxPercent = 0;

        /**
     * @param transaction
     * @param stringTypeData
     * @param additionalTransactions
        */
	@SuppressWarnings("rawtypes")
	public void transactionProcess(Transaction transaction, String stringTypeData, List additionalTransactions) {
		try {

			List stringTypesList = Arrays.asList(p.split(stringTypeData));
			switch (new Integer(stringTypesList.get(0).toString())) {
				case TSLConstants.StringTypeCode.TRANSACTION_HEADER:
					transactionHeader(transaction, stringTypesList, additionalTransactions);
					break;
				case TSLConstants.StringTypeCode.ITEM_ENTRY:
					itemEntry(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.ITEM_ENTRY_EXTENSION:
					itemEntryExtension(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.DISCOUNT:
//					discount(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.VOIDED_DISCOUNT:
//					discount(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.TENDER:
					tender(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.TENDER_CORRECTION:
					tender(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.TAX:
					tax(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.TAX_REFUND:
					tax(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.CHANGE:
					change(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.MANAGER_OVERRIDE:
					managerOverride(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.DATA_ENTRY:
					dataEntry(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.TILL_CHANGE:
					tillChange(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.SUREPOS_ACE_EPS_TENDER:
					break;
				case TSLConstants.StringTypeCode.EXCEPTION_LOG:
					exceptionLog(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.STORE_CLOSING:
					storeClosing(transaction, stringTypesList);
					break;
				case TSLConstants.StringTypeCode.WIC_EBT_DATA:
					break;
				case TSLConstants.StringTypeCode.EXTRA_DATA:
					break;
				case TSLConstants.StringTypeCode.USER_DATA:
					userData(transaction, stringTypesList);
					break;
				default:
					System.out.println("String type not recognized " + stringTypesList.get(0));
					break;

			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// transaction.setBusinessDayDate(DateUtils.truncate(businessDayDate, Calendar.DAY_OF_MONTH));
	// Date c = DateUtils.addSeconds(datetimeTransaction, ringtime);
	@SuppressWarnings({ "rawtypes", "unchecked" })
	/**Type00: TRANSACTION_HEADER**/
	public void transactionHeader(Transaction transaction, List stringTypeList, List additionalTransactionsList) {
		try {
			sequence = 0;
			totalTax = 0;
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

			transaction.setOperatorCode(stringTypeList.get(TSLConstants.Type00.OPERATOR).toString());
			transaction.setWorkstationCode(stringTypeList.get(TSLConstants.Type00.TERMINAL).toString());
			/** dateTimeEndOfTransaction **/
			String datetime = (String) stringTypeList.get(TSLConstants.Type00.DATETIME);
			int year = Integer.valueOf("20" + datetime.substring(0, 2));
			int month = Integer.valueOf(datetime.substring(2, 4));
			int day = Integer.valueOf(datetime.substring(4, 6));
			int hour = Integer.valueOf(datetime.substring(6, 8));
			int minutes = Integer.valueOf(datetime.substring(8, 10));
			Calendar calendar = new GregorianCalendar(year, month-1, day, hour, minutes);
			Date dateTimeEndOfTransaction = calendar.getTime();
			/** beginDateTimeString **/
                        try{
			ringElapsedTime = ObjectUtils.equals(
					StringUtils.defaultIfBlank(stringTypeList.size() > TSLConstants.Type00.RINGTIME ? stringTypeList.get(TSLConstants.Type00.RINGTIME).toString() : null, null), null) ? null
					: new Integer(stringTypeList.get(TSLConstants.Type00.RINGTIME).toString());
			tenderElapsedTime = ObjectUtils.equals(
					StringUtils.defaultIfBlank(stringTypeList.size() > TSLConstants.Type00.TENDERTI ? stringTypeList.get(TSLConstants.Type00.TENDERTI).toString(): null, null), null) ? null
					: new Integer(stringTypeList.get(TSLConstants.Type00.TENDERTI).toString());
                        if(ringElapsedTime == null)
                        {
                            ringElapsedTime = 0;
                        }
                        if(tenderElapsedTime == null)
                        {
                            tenderElapsedTime = 0;
                        }
                        int elapsedTotal = ringElapsedTime + tenderElapsedTime;
                        calendar.add(Calendar.SECOND, -elapsedTotal);
                        }catch(Exception e)
                        {
                            log.error(e.getMessage(), e);
                            calendar = new GregorianCalendar(year, month-1, day, hour, minutes);
                        }
                        transaction.setBeginDateTimeString(ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(calendar.getTime()));
			/** endDateTimeString **/
			transaction.setEndDateTimeString(ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(dateTimeEndOfTransaction));

			/** sequenceNumber **/
			// Integer elapsedMinutes = getNumberOfElapsedMinutes(dateTimeEndOfTransaction);
			transaction.setSequenceNumber(new Integer(stringTypeList.get(TSLConstants.Type00.TRANSNUM).toString()) /* + elapsedMinutes */);

			Integer lockElapsedTime = ObjectUtils.equals(
					StringUtils.defaultIfBlank(stringTypeList.size() > TSLConstants.Type00.SPECIAL ? stringTypeList.get(TSLConstants.Type00.SPECIAL).toString() : null, null), null) ? null
					: new Integer(stringTypeList.get(TSLConstants.Type00.SPECIAL).toString());
			Integer idleElapsedTime = ObjectUtils.equals(
					StringUtils.defaultIfBlank(stringTypeList.size() > TSLConstants.Type00.INACTIVE ? stringTypeList.get(TSLConstants.Type00.INACTIVE).toString() : null, null), null) ? null
					: new Integer(stringTypeList.get(TSLConstants.Type00.INACTIVE).toString());

			/** There is a login operator transaction before **/
			if (stringTypeList.size() > TSLConstants.Type00.INDICAT1 && isBitOn(Long.parseLong(stringTypeList.get(TSLConstants.Type00.INDICAT1).toString()),
					TSLConstants.Type00.INDICAT1_OPERATOR_SIGN_ON_BIT_POSITION)) {

				Transaction additionalTransaction = new Transaction();

				additionalTransaction.setVoidedFlag(Boolean.FALSE);
				additionalTransaction.setCancelFlag(Boolean.FALSE);
				additionalTransaction.setKeyedOfflineFlag(Boolean.FALSE);
				additionalTransaction.setTrainingModeFlag(Boolean.FALSE);
				additionalTransaction.setTransactionTypeCode(2);
				additionalTransaction.setOperatorCode(transaction.getOperatorCode());
				additionalTransaction.setWorkstationCode(transaction.getWorkstationCode());
				additionalTransaction.setRetailStoreCode(transaction.getRetailStoreCode());
				additionalTransaction.setBusinessDayDateString(transaction.getBusinessDayDateString());
				additionalTransaction.setBeginDateTimeString(transaction.getBeginDateTimeString());
				additionalTransaction.setEndDateTimeString(transaction.getEndDateTimeString());
				additionalTransaction.setSequenceNumber(transaction.getSequenceNumber());

				ControlTransaction controlTransaction = new ControlTransaction();
				SignOnTransaction signOnTransaction = new SignOnTransaction();

				controlTransaction.setSignOn(signOnTransaction);
				additionalTransaction.setControlTransaction(controlTransaction);

				additionalTransactionsList.add(additionalTransaction);
			}

			/** get the object related to TRANTYPE **/
			// Object objectTransaction = new Object();
			TSLTransactionHeaderTypesMethods tslTransactionHeaderTypesMethods = new TSLTransactionHeaderTypesMethods();
			Object objectTransaction = tslTransactionHeaderTypesMethods.MethodProcess(transaction, stringTypeList);

			if (objectTransaction instanceof RetailTransaction) {
				transaction.setTransactionTypeCode(1);
				transaction.getRetailTransaction().setRingElapsedTime(ringElapsedTime);
				transaction.getRetailTransaction().setTenderElapsedTime(tenderElapsedTime);
				transaction.getRetailTransaction().setIdleElapsedTime(idleElapsedTime);
				transaction.getRetailTransaction().setLockElapsedTime(lockElapsedTime);
				log.info("Store:"+transaction.getRetailStore());
				if(transaction.getRetailStore().getIncludeTax() != null)
				{
					log.info("Include Tax: "+transaction.getRetailStore().getIncludeTax());
					transaction.getRetailTransaction().setIncludeTax(transaction.getRetailStore().getIncludeTax().equalsIgnoreCase("S") ? true : false);
				}else{
					transaction.getRetailTransaction().setIncludeTax(false);
				}
				
			}
			if (objectTransaction instanceof TenderControlTransaction) {
				transaction.setTransactionTypeCode(3);
			}
			if (objectTransaction instanceof ControlTransaction) {
				transaction.setTransactionTypeCode(2);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**Type01: ITEM_ENTRY**/
	public void itemEntry(Transaction transaction, List list) {
		int sign = 1;
		try {

			RetailTransactionLineItem retailTransactionLineItem = new RetailTransactionLineItem();
			retailTransactionLineItem.setSequenceNumber(++sequence);
			// � VoidFlag
			if ((!list.get(TSLConstants.Type01.INDICAT2).toString().isEmpty() && isBitOn(
					Long.parseLong(list.get(TSLConstants.Type01.INDICAT2).toString()),
					TSLConstants.Type01.INDICAT2_CANCEL_KEY_PRESSED_BIT_POSITION))
					&& (!list.get(TSLConstants.Type01.INDICAT3).toString().isEmpty() && (list.get(TSLConstants.Type01.INDICAT3).toString()
							.substring(0, 1).equals(TSLConstants.Type01.INDICAT3_T_ITEM_SALE_CANCEL_VALUE) || list
							.get(TSLConstants.Type01.INDICAT3).toString().substring(0, 1)
							.equals(TSLConstants.Type01.INDICAT3_T_DEPOSIT_CANCEL_VALUE))))
				retailTransactionLineItem.setVoidFlag(Boolean.TRUE);
			else
				retailTransactionLineItem.setVoidFlag(Boolean.FALSE);

			if (!list.get(TSLConstants.Type01.INDICAT3).toString().isEmpty()
					&& list.get(TSLConstants.Type01.INDICAT3).toString().substring(1)
							.equals(TSLConstants.Type01.INDICAT3_O_SCANNED_ITEM_CODE_VALUE)) {
				transaction.getRetailTransaction().setLineItemsScannedCount(
						transaction.getRetailTransaction().getLineItemsScannedCount() + 1);
			}

			if (!list.get(TSLConstants.Type01.INDICAT3).toString().isEmpty()
					&& list.get(TSLConstants.Type01.INDICAT3).toString().substring(1)
							.equals(TSLConstants.Type01.INDICAT3_O_KEYED_ITEM_CODE_VALUE)) {
				transaction.getRetailTransaction().setLineItemsKeyedCount(transaction.getRetailTransaction().getLineItemsKeyedCount() + 1);
			}

			SaleReturnLineItem saleReturnLineItem = new SaleReturnLineItem();
			saleReturnLineItem.setOrdinalNumber(currentOrdinalNumber != 0 ? currentOrdinalNumber : 0);
			saleReturnLineItem.setMerchandiseHierarchyGroupCode(new Integer(list.get(TSLConstants.Type01.FAMILYNU).toString()));
			saleReturnLineItem.setPosDepartmentCode(new Integer(list.get(TSLConstants.Type01.DEPARTME).toString()));
			saleReturnLineItem.setQuantity(1D); // Siempre que no exista un string 02 este valor es 1
			saleReturnLineItem.setExtendedAmount(new Double(list.get(TSLConstants.Type01.XPRICE).toString()));
			/** barcode, para colsubsidio va el codigo de producto **/
			saleReturnLineItem.setItemCode(currentItemCode != 0 ? currentItemCode : Long.valueOf(list.get(TSLConstants.Type01.ITEMCODE).toString()));
			saleReturnLineItem.setPosItemID(Long.valueOf(list.get(TSLConstants.Type01.ITEMCODE).toString()));
			currentOrdinalNumber = 0;
			currentItemCode = 0;
			/** ExtendedAmount sign **/
			if ((!list.get(TSLConstants.Type01.INDICAT2).toString().isEmpty() && (isBitOn(
					Long.parseLong(list.get(TSLConstants.Type01.INDICAT2).toString()),
					TSLConstants.Type01.INDICAT2_NEGATIVE_PRICE_DUE_TO_DEAL_BIT_POSITION) || isBitOn(
					Long.parseLong(list.get(TSLConstants.Type01.INDICAT2).toString()),
					TSLConstants.Type01.INDICAT2_CANCEL_KEY_PRESSED_BIT_POSITION)))
					|| (!list.get(TSLConstants.Type01.INDICAT3).toString().isEmpty() && (list.get(TSLConstants.Type01.INDICAT3).toString()
							.substring(0, 1).equals(TSLConstants.Type01.INDICAT3_T_REFUND_VALUE)
							|| list.get(TSLConstants.Type01.INDICAT3).toString().substring(0, 1)
									.equals(TSLConstants.Type01.INDICAT3_T_DEPOSIT_RETURN_VALUE)
							|| list.get(TSLConstants.Type01.INDICAT3).toString().substring(0, 1)
									.equals(TSLConstants.Type01.INDICAT3_T_MISC_TRANS_PAYOUT_REFUND_VALUE)
							|| list.get(TSLConstants.Type01.INDICAT3).toString().substring(0, 1)
									.equals(TSLConstants.Type01.INDICAT3_T_MANUFACTURER_COUPON_VALUE) || list
							.get(TSLConstants.Type01.INDICAT3).toString().substring(0, 1)
							.equals(TSLConstants.Type01.INDICAT3_T_STORE_COUPON_VALUE)))) {

//				saleReturnLineItem.setExtendedAmount(saleReturnLineItem.getExtendedAmount() * -1);
			}
			saleReturnLineItem.setRegularSalesUnitPrice(saleReturnLineItem.getExtendedAmount());
			String taxType = "";
			if (!list.get(TSLConstants.Type01.INDICAT1).toString().isEmpty()) {
				if (isBitOn(Long.parseLong(list.get(TSLConstants.Type01.INDICAT1).toString()),
						TSLConstants.Type01.INDICAT1_PRICE_ENTERED_BIT_POSITION)) {
					saleReturnLineItem.setPriceEntered(1);
				} else
					saleReturnLineItem.setPriceEntered(0);
				if (isBitOn(Long.parseLong(list.get(TSLConstants.Type01.INDICAT1).toString()),
						TSLConstants.Type01.INDICAT1_TAXABLE_A_BIT_POSITION)) {
					taxType = taxType + "A";
				} else if (isBitOn(Long.parseLong(list.get(TSLConstants.Type01.INDICAT1).toString()),
						TSLConstants.Type01.INDICAT1_TAXABLE_B_BIT_POSITION)) {
					taxType = taxType + "B";
				} else if (isBitOn(Long.parseLong(list.get(TSLConstants.Type01.INDICAT1).toString()),
						TSLConstants.Type01.INDICAT1_TAXABLE_C_BIT_POSITION)) {
					taxType = taxType + "C";
				} else if (isBitOn(Long.parseLong(list.get(TSLConstants.Type01.INDICAT1).toString()),
						TSLConstants.Type01.INDICAT1_TAXABLE_D_BIT_POSITION)) {
					taxType = taxType + "D";
				}
			}
			saleReturnLineItem.setTaxType(taxType);
			// TODO: AGREGAR MONTO DE IMPUESTO.
			if (!list.get(TSLConstants.Type01.INDICAT3).toString().isEmpty()) {
				/**
				 * T: 0 = normal, 1 = Deposit, 2 = refund, 3 = Deposit return envase, 4 = miscel�neo receipt sale, 5 = miscelaneo payout
				 * refund, 6 = manufacturer coupon, 7 = Store coupon, 8 = item sale cancel, 9 = deposit cancel
				 **/
				saleReturnLineItem.setItemType(new Integer(list.get(TSLConstants.Type01.INDICAT3).toString().substring(0, 1)));
				/**
				 * O: 0 = Scanned, 1 = Keyed, 2 = Lookup keyed, 3 = Linked, 4 = Reserved, 5 = Item created by service, 6-7 = Reserved, 8 =
				 * Redemption of points, 9 = Bonus points.
				 **/
				saleReturnLineItem.setEntryMethodCode(getEntryMethodName(list.get(TSLConstants.Type01.INDICAT3).toString().substring(1)));

				//agregado para que todos los cupones del ace base se registren como Service
				if(saleReturnLineItem.getItemType().equals(Integer.valueOf(TSLConstants.Type01.INDICAT3_T_STORE_COUPON_VALUE))){
					saleReturnLineItem.setEntryMethodCode(TSLConstants.Type01.INDICAT3_O_ITEM_CREATED_BY_SERVICE_NAME);
				}

				/** INDICAT3 del string 01 itemType T values 8 y 9 ==> Anulaciones **/
				if (list.get(TSLConstants.Type01.INDICAT3).toString().substring(0, 1)
						.equals(TSLConstants.Type01.INDICAT3_T_ITEM_SALE_CANCEL_VALUE)
						|| list.get(TSLConstants.Type01.INDICAT3).toString().substring(0, 1)
								.equals(TSLConstants.Type01.INDICAT3_T_DEPOSIT_CANCEL_VALUE)) {
					retailTransactionLineItem.setVoidFlag(Boolean.TRUE);
				}
			}
			/** Anulaciones **/
			if (!list.get(TSLConstants.Type01.INDICAT2).toString().isEmpty()
					&& isBitOn(Long.parseLong(list.get(TSLConstants.Type01.INDICAT2).toString()),
							TSLConstants.Type01.INDICAT2_CANCEL_KEY_PRESSED_BIT_POSITION)) {
				retailTransactionLineItem.setVoidFlag(Boolean.TRUE);
			}

			/*			*//** INDICAT3 del string 01 itemType T values 2, 3, 5 ==> Return **/
			/*
			 * if( list.get(TSLConstants.Type01.INDICAT3).toString().substring(0, 1).equals(TSLConstants.Type01.INDICAT3_T_REFUND_VALUE) ||
			 * list.get(TSLConstants.Type01.INDICAT3).toString().substring(0, 1).equals(TSLConstants.Type01.INDICAT3_T_DEPOSIT_RETURN_VALUE)
			 * || list.get(TSLConstants.Type01.INDICAT3).toString().substring(0,
			 * 1).equals(TSLConstants.Type01.INDICAT3_T_MISC_TRANS_PAYOUT_REFUND_VALUE) ){
			 * //retailTransactionLineItem.setReturnLI(saleReturnLineItem); retailTransactionLineItem.setSaleLI(saleReturnLineItem); }
			 *//** INDICAT3 del string 01 itemType T values 0, 1 y 4 ==> Sale **/
			/*
			 * if( list.get(TSLConstants.Type01.INDICAT3).toString().substring(0,
			 * 1).equals(TSLConstants.Type01.INDICAT3_T_NORMAL_ITEM_SALE_VALUE) ||
			 * list.get(TSLConstants.Type01.INDICAT3).toString().substring(0, 1).equals(TSLConstants.Type01.INDICAT3_T_DEPOSIT_VALUE) ||
			 * list.get(TSLConstants.Type01.INDICAT3).toString().substring(0,
			 * 1).equals(TSLConstants.Type01.INDICAT3_T_MISC_TRANS_RECEIPT_SALE_VALUE) ){
			 * retailTransactionLineItem.setSaleLI(saleReturnLineItem); }
			 */

			/** Devolucion **/
			if (!list.get(TSLConstants.Type01.INDICAT2).toString().isEmpty()
					&& (isBitOn(Long.parseLong(list.get(TSLConstants.Type01.INDICAT2).toString()),
							TSLConstants.Type01.INDICAT2_REFUND_KEY_PRESSED_BIT_POSITION) || isBitOn(Long.parseLong(list.get(TSLConstants.Type01.INDICAT2).toString()),
									TSLConstants.Type01.INDICAT2_NEGATIVE_PRICE_DUE_TO_DEAL_BIT_POSITION))) {
				retailTransactionLineItem.setReturnLI(saleReturnLineItem);
				retailTransactionLineItem.setItemTypeCode(TSLConstants.Type0708.RETURN);
			} else {
				retailTransactionLineItem.setSaleLI(saleReturnLineItem);
				retailTransactionLineItem.setItemTypeCode(TSLConstants.Type0708.SALE);
			}
			// if item entry extension is not coming
			if (list.get(TSLConstants.Type01.INDICAT2).toString().isEmpty()
					|| (!list.get(TSLConstants.Type01.INDICAT2).toString().isEmpty() && !isBitOn(
							Long.parseLong(list.get(TSLConstants.Type01.INDICAT2).toString()),
							TSLConstants.Type01.INDICAT2_EXTENSION_FOLLOWS_THIS_STRING_BIT_POSITION))) {
				if (retailTransactionLineItem.getVoidFlag())
					sign = -1;
				transaction.getRetailTransaction().setUnitCount(
						transaction.getRetailTransaction().getUnitCount() == null ? (sign * saleReturnLineItem.getQuantity()) : transaction
								.getRetailTransaction().getUnitCount() + (sign * saleReturnLineItem.getQuantity()));
			}

			if (null == transaction.getRetailTransaction().getLineItems())
				transaction.getRetailTransaction().setLineItems(new ArrayList());


			if (saleReturnLineItem!= null && (saleReturnLineItem.getItemCode().compareTo(Long.valueOf(811099999998L)) == 0 || saleReturnLineItem.getItemCode().compareTo(Long.valueOf(811099999996L)) == 0)) {
				saleReturnLineItem.setIsPromo(Boolean.TRUE);
				if(retailTransactionLineItem.getVoidFlag()!= null && !retailTransactionLineItem.getVoidFlag().booleanValue()){
					RetailTransactionTotal totalPromoPos = null;
					Iterator itTotalItems = transaction.getRetailTransaction().getTotalItems().iterator();
					while (itTotalItems.hasNext() && totalPromoPos == null) {
						RetailTransactionTotal totalItem = (RetailTransactionTotal) itTotalItems.next();
						if(totalItem.getTransactionTotalTypeCode() != null && TSLConstants.Type00.TRANSACTION_PROMO_POSITIVE_DISCOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
							totalPromoPos = totalItem;
						}
					}
					if(totalPromoPos==null){
						totalPromoPos = new RetailTransactionTotal();
						totalPromoPos.setAmount(saleReturnLineItem.getExtendedAmount());
						transaction.getRetailTransaction().getTotalItems().add(totalPromoPos);
					} else {
						Double temp = saleReturnLineItem.getExtendedAmount() + totalPromoPos.getAmount();
						totalPromoPos.setAmount(temp);
					}
				} else {
					RetailTransactionTotal totalPromoNeg = null;
					Iterator itTotalItems = transaction.getRetailTransaction().getTotalItems().iterator();
					while (itTotalItems.hasNext() && totalPromoNeg == null) {
						RetailTransactionTotal totalItem = (RetailTransactionTotal) itTotalItems.next();
						if(totalItem.getTransactionTotalTypeCode() != null && TSLConstants.Type00.TRANSACTION_PROMO_NEGATIVE_DISCOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
							totalPromoNeg = totalItem;
						}
					}
					if(totalPromoNeg==null){
						totalPromoNeg = new RetailTransactionTotal();
						totalPromoNeg.setAmount(saleReturnLineItem.getExtendedAmount());
						transaction.getRetailTransaction().getTotalItems().add(totalPromoNeg);
					} else {
						Double temp = saleReturnLineItem.getExtendedAmount() + totalPromoNeg.getAmount();
						totalPromoNeg.setAmount(temp);
					}
				}
			} else if (saleReturnLineItem!= null && saleReturnLineItem.getItemType().equals(Integer.valueOf(TSLConstants.Type01.INDICAT3_T_STORE_COUPON_VALUE))) {
				saleReturnLineItem.setIsPromo(Boolean.FALSE);
				if(retailTransactionLineItem.getVoidFlag()!= null && !retailTransactionLineItem.getVoidFlag().booleanValue()){
					RetailTransactionTotal totalDescPos = null;
					Iterator itTotalItems = transaction.getRetailTransaction().getTotalItems().iterator();
					while (itTotalItems.hasNext() && totalDescPos == null) {
						RetailTransactionTotal totalItem = (RetailTransactionTotal) itTotalItems.next();
						if(totalItem.getTransactionTotalTypeCode() != null && TSLConstants.Type00.TRANSACTION_POSITIVE_DISCOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
							totalDescPos = totalItem;
						}
					}
					if(totalDescPos==null){
						totalDescPos = new RetailTransactionTotal();
						totalDescPos.setAmount(saleReturnLineItem.getExtendedAmount());
						transaction.getRetailTransaction().getTotalItems().add(totalDescPos);
					} else {
						Double temp = saleReturnLineItem.getExtendedAmount() + totalDescPos.getAmount();
						totalDescPos.setAmount(temp);
					}
				} else {
					RetailTransactionTotal totalDescNeg = null;
					Iterator itTotalItems = transaction.getRetailTransaction().getTotalItems().iterator();
					while (itTotalItems.hasNext() && totalDescNeg == null) {
						RetailTransactionTotal totalItem = (RetailTransactionTotal) itTotalItems.next();
						if(totalItem.getTransactionTotalTypeCode() != null && TSLConstants.Type00.TRANSACTION_NEGATIVE_DISCOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
							totalDescNeg = totalItem;
						}
					}
					if(totalDescNeg==null){
						totalDescNeg = new RetailTransactionTotal();
						totalDescNeg.setAmount(saleReturnLineItem.getExtendedAmount());
						transaction.getRetailTransaction().getTotalItems().add(totalDescNeg);
					} else {
						Double temp = saleReturnLineItem.getExtendedAmount() + totalDescNeg.getAmount();
						totalDescNeg.setAmount(temp);
					}
				}
			}
			if(currentTax > 0){
				RetailTransactionTotal totalTax1 = null;
				Iterator itTotalItems = transaction.getRetailTransaction().getTotalItems().iterator();
				while (itTotalItems.hasNext() && totalTax1 == null) {
					RetailTransactionTotal totalItem = (RetailTransactionTotal) itTotalItems.next();
					if(totalItem.getTransactionTotalTypeCode() != null && TSLConstants.Type00.TRANSACTION_TAX_AMOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
						totalTax1 = totalItem;
						break;
					}
				}
				if(retailTransactionLineItem.getSaleLI() != null){
					saleReturnLineItem = retailTransactionLineItem.getSaleLI();
					if(retailTransactionLineItem.getVoidFlag())
						totalTax -= currentTax;
					else
						totalTax += currentTax;
				} else if(retailTransactionLineItem.getReturnLI() != null){
					saleReturnLineItem = retailTransactionLineItem.getReturnLI();
					if(retailTransactionLineItem.getVoidFlag())
						totalTax += currentTax;
					else
						totalTax -= currentTax;
				}
				if(saleReturnLineItem != null)
					saleReturnLineItem.setAppliedTax(currentTax);

				if(totalTax1!=null){
					totalTax1.setAmount(totalTax);
				}
				currentTax = 0;
			}
			transaction.getRetailTransaction().getLineItems().add(retailTransactionLineItem);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("rawtypes")
	/**Type02: ITEM_ENTRY_EXTENSION**/
	public void itemEntryExtension(Transaction transaction, List list) {
		int sign = 1;
		try {
			RetailTransactionLineItem retailTransactionLineItem = (RetailTransactionLineItem) transaction.getRetailTransaction()
					.getLineItems().get(transaction.getRetailTransaction().getLineItems().size() - 1);

			SaleReturnLineItem saleReturnLineItem = null;
			/** only one of them is not null: getSaleLI **/
			if (retailTransactionLineItem.getSaleLI() != null)
				saleReturnLineItem = retailTransactionLineItem.getSaleLI();
			if (retailTransactionLineItem.getReturnLI() != null)
				saleReturnLineItem = retailTransactionLineItem.getReturnLI();

			/** Para saber si un item es pesable **/
			if (isBitOn(Long.parseLong(list.get(TSLConstants.Type01.INDICAT1).toString()),
					TSLConstants.Type01.INDICAT1_WEIGHT_ITEM_BIT_POSITION)) {

			}


			// si se ha vendido por peso
			if (list.size() > TSLConstants.Type02.INDICAT1 && (isBitOn(Long.parseLong(list.get(TSLConstants.Type02.INDICAT1).toString()),
					TSLConstants.Type02.INDICAT1_SCALE_WEIGHT_BIT_POSITION)
					|| isBitOn(Long.parseLong(list.get(TSLConstants.Type02.INDICAT1).toString()),
							TSLConstants.Type02.INDICAT1_WEIGHT_OR_VOLUME_KEY_BIT_POSITION))) {

				saleReturnLineItem.setUnits(new Double(list.size() > TSLConstants.Type02.QTYORWGTORVOL ? list.get(TSLConstants.Type02.QTYORWGTORVOL).toString() : "0"));
				// saleReturnLineItem.setUnitOfMeasure(); siempre es en KG
				// se vendio por cantidad
			} else {
				Double quantity = new Double(list.size() > TSLConstants.Type02.QTYORWGTORVOL ? list.get(TSLConstants.Type02.QTYORWGTORVOL).toString() : "0");
				String method = list.size() > TSLConstants.Type02.METHOD ? list.get(TSLConstants.Type02.METHOD).toString() : "0";
				int pricingMethod = method != null && !method.isEmpty() ? Integer.valueOf(method).intValue() : 1;
				String dealStr = list.size() > TSLConstants.Type02.DEALQUAN ? list.get(TSLConstants.Type02.DEALQUAN).toString() : "0";
				Integer dealQty = Integer.valueOf(dealStr != null && !dealStr.isEmpty() ? dealStr : "0");
				String saleStr = list.size() > TSLConstants.Type02.SALEQUAN ? list.get(TSLConstants.Type02.SALEQUAN).toString() : "0";
				Integer saleQty = Integer.valueOf(saleStr != null && !saleStr.isEmpty() ? saleStr : "0");
				if(dealQty > saleQty && pricingMethod > 1){
					String salePrice = (list.size() > TSLConstants.Type02.SALEPRIC ? list.get(TSLConstants.Type02.SALEPRIC).toString(): "0")+"0000000000";
					Double unitPrice = Double.valueOf(salePrice.substring(0, 5));
					Double reducedPrice = Double.valueOf(salePrice.substring(5, 10));
					Double discount = (new BigDecimal((unitPrice - reducedPrice) * quantity)).divide(new BigDecimal(100), 0, BigDecimal.ROUND_HALF_UP).doubleValue();
					addManualDiscount(transaction.getRetailTransaction(), saleReturnLineItem, discount, true);
				}
				saleReturnLineItem.setQuantity(quantity);
				Double regularSalesUnitPrice = saleReturnLineItem.getExtendedAmount() / (saleReturnLineItem.getQuantity());
				saleReturnLineItem.setRegularSalesUnitPrice(regularSalesUnitPrice);
			}
			// unit count
			if (retailTransactionLineItem.getVoidFlag())
				sign = -1;
			transaction.getRetailTransaction().setUnitCount(
					transaction.getRetailTransaction().getUnitCount() == null ? (sign * saleReturnLineItem.getQuantity()) : transaction
							.getRetailTransaction().getUnitCount() + (sign * saleReturnLineItem.getQuantity()));


		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**Type03: DISCOUNT, Type04: VOIDED_DISCOUNT**/
	public void discount(Transaction transaction, List list) {
		try {
			RetailTransactionLineItem retailTransactionLineItem = new RetailTransactionLineItem();
			retailTransactionLineItem.setSequenceNumber(++sequence);
			if(TSLConstants.StringTypeCode.VOIDED_DISCOUNT ==  (new Integer(list.get(0).toString())).intValue())
				retailTransactionLineItem.setVoidFlag(Boolean.TRUE);
			else
				retailTransactionLineItem.setVoidFlag(Boolean.FALSE);

			PriceModificationLineItem priceModificationLineItem = new PriceModificationLineItem();

			priceModificationLineItem.setPercentage(new Double(list.get(TSLConstants.Type0304.DISRATE).toString()));
			priceModificationLineItem.setAmount(new Double(list.get(TSLConstants.Type0304.AMOUNT).toString()));

			/** DISGROUP = DiscountTypeCode **/
			retailTransactionLineItem.setItemTypeCode(new Integer(list.get(TSLConstants.Type0304.DISGROUP).toString()));
			retailTransactionLineItem.setPriceModification(priceModificationLineItem);

			if (null == transaction.getRetailTransaction().getLineItems())
				transaction.getRetailTransaction().setLineItems(new ArrayList());

			transaction.getRetailTransaction().getLineItems().add(retailTransactionLineItem);

			int numberOfTotalElements = transaction.getRetailTransaction().getTotalItems().size();
			for (int j = 0; j < numberOfTotalElements; j++) {
				/** is tax **/
				if (!retailTransactionLineItem.getVoidFlag() && ((RetailTransactionTotal) transaction.getRetailTransaction().getTotalItems().get(j))
						.getTransactionTotalTypeCode().equals(TSLConstants.Type00.TRANSACTION_POSITIVE_DISCOUNT)) {
					((RetailTransactionTotal) transaction.getRetailTransaction().getTotalItems().get(j))
							.setAmount(((RetailTransactionTotal) transaction.getRetailTransaction().getTotalItems().get(j))
									.getAmount() + retailTransactionLineItem.getPriceModification().getAmount());
					break;
				} else if (retailTransactionLineItem.getVoidFlag() && ((RetailTransactionTotal) transaction.getRetailTransaction().getTotalItems().get(j))
						.getTransactionTotalTypeCode().equals(TSLConstants.Type00.TRANSACTION_NEGATIVE_DISCOUNT)) {
					((RetailTransactionTotal) transaction.getRetailTransaction().getTotalItems().get(j))
							.setAmount(((RetailTransactionTotal) transaction.getRetailTransaction().getTotalItems().get(j))
									.getAmount() + retailTransactionLineItem.getPriceModification().getAmount());
					break;
				}

			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**Type05: TENDER, Type06: TENDER_CORRECTION**/
	public  void tender(Transaction transaction, List list) {
		int sign = 1;
		try {

			Object object = getTypeOfTransaction(transaction);
			if (object instanceof RetailTransaction) {
				TenderLineItem tenderLineItem = new TenderLineItem();

				tenderLineItem.setTenderTypeCode(list.get(TSLConstants.Type0506.TENDTYPE).toString().length() > 0 ? list.get(TSLConstants.Type0506.TENDTYPE).toString() : "0".toString());
				tenderLineItem.setAmount(new Double(list.get(TSLConstants.Type0506.AMTTENDE).toString().length() > 0 ? list.get(TSLConstants.Type0506.AMTTENDE).toString() : "0"));
				tenderLineItem.setFeeAmount(new Double(list.get(TSLConstants.Type0506.AMTTNFEE).toString().length() > 0 ? list.get(TSLConstants.Type0506.AMTTNFEE).toString() : "0"));
				tenderLineItem.setStatus(new Integer(list.get(TSLConstants.Type0506.STATUS).toString().length() > 0 ? list.get(TSLConstants.Type0506.STATUS).toString() : "0"));
				tenderLineItem.setTenderAccountNumber(list.get(TSLConstants.Type0506.CUSTOMER).toString());

				RetailTransactionLineItem retailTransactionLineItem = new RetailTransactionLineItem();
				retailTransactionLineItem.setSequenceNumber(++sequence);

				if(tenderLineItem.getTenderAccountNumber() != null && tenderLineItem.getTenderAccountNumber().trim().length() > 0 && tenderPinpadTemp != null) {
					if (transaction.getRetailTransaction().getPagosConPinpad() == null)
						transaction.getRetailTransaction().setPagosConPinpad(new ArrayList());
					tenderLineItem.setTenderAccountNumber(tenderPinpadTemp.getNumTarjeta());
					tenderPinpadTemp.setSequenceNumber(retailTransactionLineItem.getSequenceNumber());
					transaction.getRetailTransaction().getPagosConPinpad().add(tenderPinpadTemp);
				}
				tenderPinpadTemp = null;

				tenderLineItem.setIsChangeFlag(Boolean.FALSE);

				if (list.get(TSLConstants.Type0506.TYPE).toString().equals(TSLConstants.Type0506.TENDER_CORRECTION_06)) {
					retailTransactionLineItem.setVoidFlag(Boolean.TRUE);

					int seqNumVoided = getLastTenderLineItemVoidedSeqNum(transaction.getRetailTransaction().getLineItems(), tenderLineItem.getAmount(), tenderLineItem.getTenderTypeCode(), tenderLineItem.getTenderAccountNumber());
					tenderLineItem.setSequenceNumberVoided(seqNumVoided);
					deletePinpadDataIfExist(transaction.getRetailTransaction().getPagosConPinpad(), seqNumVoided);
				}

				if (list.get(TSLConstants.Type0506.TYPE).toString().equals(TSLConstants.Type0506.TENDER_05)) {
					retailTransactionLineItem.setVoidFlag(Boolean.FALSE);
				}

				retailTransactionLineItem.setTender(tenderLineItem);

				if (null == transaction.getRetailTransaction().getLineItems())
					transaction.getRetailTransaction().setLineItems(new ArrayList());
				transaction.getRetailTransaction().getLineItems().add(retailTransactionLineItem);

			}
			if (object instanceof TenderControlTransaction) {
				TenderControlTransactionLineItem tenderControlTransactionLineItem = new TenderControlTransactionLineItem();
				tenderControlTransactionLineItem.setSequenceNumber(++sequence);

//				if (list.get(TSLConstants.Type0506.TYPE).toString().equals(TSLConstants.Type0506.TENDER_CORRECTION_06)) {
//					sign = -1;
//				}

				tenderControlTransactionLineItem.setTenderTypeCode(list.get(TSLConstants.Type0506.TENDTYPE).toString());
				tenderControlTransactionLineItem.setAmount(sign * (new Double(list.get(TSLConstants.Type0506.AMTTENDE).toString())));
				tenderControlTransactionLineItem.setExchangeRate(new Double(list.get(TSLConstants.Type0506.AMTTNFEE).toString()));

				if (null == transaction.getTenderControlTransaction().getLineItems())
					transaction.getTenderControlTransaction().setLineItems(new ArrayList());
				transaction.getTenderControlTransaction().getLineItems().add(tenderControlTransactionLineItem);

			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**Type07: TAX, Type08: TAX_REFUND**/
	public  void tax(Transaction transaction, List list) {
		try {
			int sign = 1;
			/** TAX REFUND **/
//			if (new Integer(list.get(TSLConstants.Type0708.TYPE).toString()) == TSLConstants.StringTypeCode.TAX_REFUND) {
//				sign = -1;
//			}

			int numberOfElements = list.size();
			for (int i = 1; i < numberOfElements; i++) {
				String value = list.get(i).toString();
				if (!value.equals("")) {

					TaxLineItem taxLineItem = null;

					if (i == TSLConstants.Type0708.AMTTAXA) {
						taxLineItem = new TaxLineItem();
						taxLineItem.setTaxType(TSLConstants.Type0708.A);
						taxLineItem.setTaxAmount((new Double(list.get(TSLConstants.Type0708.AMTTAXA).toString())) * sign);
						taxLineItem.setTaxableAmount(new Double(list.get(TSLConstants.Type0708.AMTSALEA).toString()));
					} else if (i == TSLConstants.Type0708.AMTTAXB) {
						taxLineItem = new TaxLineItem();
						taxLineItem.setTaxType(TSLConstants.Type0708.B);
						taxLineItem.setTaxAmount((new Double(list.get(TSLConstants.Type0708.AMTTAXB).toString())) * sign);
						taxLineItem.setTaxableAmount(new Double(list.get(TSLConstants.Type0708.AMTSALEB).toString()));
					} else if (i == TSLConstants.Type0708.AMTTAXC) {
						taxLineItem = new TaxLineItem();
						taxLineItem.setTaxType(TSLConstants.Type0708.C);
						taxLineItem.setTaxAmount((new Double(list.get(TSLConstants.Type0708.AMTTAXC).toString())) * sign);
						taxLineItem.setTaxableAmount(new Double(list.get(TSLConstants.Type0708.AMTSALEC).toString()));
					} else if (i == TSLConstants.Type0708.AMTTAXD) {
						taxLineItem = new TaxLineItem();
						taxLineItem.setTaxType(TSLConstants.Type0708.D);
						taxLineItem.setTaxAmount((new Double(list.get(TSLConstants.Type0708.AMTTAXD).toString())) * sign);
						taxLineItem.setTaxableAmount(new Double(list.get(TSLConstants.Type0708.AMTSALED).toString()));
					}
					if (ObjectUtils.notEqual(taxLineItem, null)) {
						RetailTransactionLineItem retailTransactionLineItem = new RetailTransactionLineItem();
						retailTransactionLineItem.setSequenceNumber(++sequence);

						if (new Integer(list.get(TSLConstants.Type0708.TYPE).toString()) == TSLConstants.StringTypeCode.TAX) {
							retailTransactionLineItem.setItemTypeCode(TSLConstants.Type0708.SALE);
						}
						if (new Integer(list.get(TSLConstants.Type0708.TYPE).toString()) == TSLConstants.StringTypeCode.TAX_REFUND) {
							retailTransactionLineItem.setItemTypeCode(TSLConstants.Type0708.RETURN);
						}
						retailTransactionLineItem.setTax(taxLineItem);

						if (null == transaction.getRetailTransaction().getLineItems())
							transaction.getRetailTransaction().setLineItems(new ArrayList());
						transaction.getRetailTransaction().getLineItems().add(retailTransactionLineItem);

						/** To set the transaction total taxes **/
						int numberOfTotalElements = transaction.getRetailTransaction().getTotalItems().size();
						for (int j = 0; j < numberOfTotalElements; j++) {
							/** is tax **/
							if (((RetailTransactionTotal) transaction.getRetailTransaction().getTotalItems().get(j))
									.getTransactionTotalTypeCode().equals(TSLConstants.Type00.TRANSACTION_TAX_AMOUNT)) {
								((RetailTransactionTotal) transaction.getRetailTransaction().getTotalItems().get(j))
										.setAmount(((RetailTransactionTotal) transaction.getRetailTransaction().getTotalItems().get(j))
												.getAmount() + taxLineItem.getTaxAmount());

								// luego de recibir el total de tax, comparamos con la suma de los tax por item
								double taxTotalBase = ((RetailTransactionTotal) transaction.getRetailTransaction().getTotalItems().get(j)).getAmount();
								if(totalTax < 0)
									totalTax = - totalTax;
								if(taxTotalBase != totalTax && transaction.getRetailTransaction().getLineItems()!= null){
									//necesitamos cuadrar los detalles para que sean igual al total
									Iterator itLineItems = transaction.getRetailTransaction().getLineItems().iterator();
									while(itLineItems.hasNext()){
										log.debug("taxTotalBase: "+taxTotalBase+" totalTax: "+totalTax);
										double difTax = taxTotalBase - totalTax;
										int signo = 1;
										if(difTax < 0){
											signo = -1;
											difTax = - difTax;
										}
										RetailTransactionLineItem lineItem = (RetailTransactionLineItem) itLineItems.next();
										SaleReturnLineItem saleReturnLineItem = null;
										if(lineItem.getSaleLI() != null){
											saleReturnLineItem = lineItem.getSaleLI();
											if(saleReturnLineItem.getAppliedTax() > 0){
												if(signo > 0 && !lineItem.getVoidFlag().booleanValue()){
													saleReturnLineItem.setAppliedTax(saleReturnLineItem.getAppliedTax()+difTax);
													totalTax += difTax;
													log.debug("1 taxTotalBase: "+taxTotalBase+" totalTax: "+totalTax);

													break;
												} else {
													if(saleReturnLineItem.getAppliedTax() - difTax >= 0){
														saleReturnLineItem.setAppliedTax(saleReturnLineItem.getAppliedTax()-difTax);
														totalTax -= difTax;
														log.debug("2 taxTotalBase: "+taxTotalBase+" totalTax: "+totalTax);

														break;
													} else {
														totalTax -= saleReturnLineItem.getAppliedTax();
														saleReturnLineItem.setAppliedTax(new Double(0));
														log.debug("3 taxTotalBase: "+taxTotalBase+" totalTax: "+totalTax);

													}
												}
											}
										} else if(lineItem.getReturnLI() != null){
											saleReturnLineItem = lineItem.getReturnLI();
											if(saleReturnLineItem.getAppliedTax() > 0){
												if(signo < 0 && !lineItem.getVoidFlag().booleanValue()){
													saleReturnLineItem.setAppliedTax(saleReturnLineItem.getAppliedTax()+difTax);
													totalTax += difTax;
													break;
												} else {
													if(saleReturnLineItem.getAppliedTax() - difTax >= 0){
														saleReturnLineItem.setAppliedTax(saleReturnLineItem.getAppliedTax()-difTax);
														totalTax -= difTax;
														break;
													} else {
														totalTax -= saleReturnLineItem.getAppliedTax();
														saleReturnLineItem.setAppliedTax(new Double(0));
													}
												}
											}
										}
									}

								}
								break;
							}

						}
					}
				}
			}


		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**Type09: CHANGE**/
	public  void change(Transaction transaction, List list) {
		try {
			RetailTransactionLineItem retailTransactionLineItem = new RetailTransactionLineItem();
			retailTransactionLineItem.setSequenceNumber(++sequence);

			TenderLineItem tenderLineItem = new TenderLineItem();

			tenderLineItem.setAmount(new Double(list.get(TSLConstants.Type09.AMTCHANGE).toString()));
			tenderLineItem.setTenderTypeCode(list.get(TSLConstants.Type09.TENDTYPE).toString());
			tenderLineItem.setTenderAccountNumber("");
			tenderLineItem.setStatus(0);
			tenderLineItem.setIsChangeFlag(Boolean.TRUE);
			retailTransactionLineItem.setTender(tenderLineItem);

			retailTransactionLineItem.setVoidFlag(Boolean.FALSE);
			if(null != transaction.getRetailTransaction()){
				if(null == transaction.getRetailTransaction().getLineItems())
					transaction.getRetailTransaction().setLineItems(new ArrayList());
				transaction.getRetailTransaction().getLineItems().add(retailTransactionLineItem);
			}
			if(null != transaction.getTenderControlTransaction()){
				if(null == transaction.getTenderControlTransaction().getLineItems())
					transaction.getTenderControlTransaction().setLineItems(new ArrayList());
				transaction.getTenderControlTransaction().getLineItems().add(retailTransactionLineItem);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**Type10: MANAGER OVERRIDE**/
	public  void managerOverride(Transaction transaction, List list) {
		try {
			ManagerOverride managerOverride = new ManagerOverride();

			managerOverride.setNumber(list.get(TSLConstants.Type10.OVERRIDE).toString());
			managerOverride.setReason(list.get(TSLConstants.Type10.REASON).toString());

			if (null != transaction.getRetailTransaction()) {
				if (null == transaction.getRetailTransaction().getManagerOverrides()) {
					transaction.getRetailTransaction().setManagerOverrides(new ArrayList());
				}
				managerOverride.setSequenceNumber(transaction.getRetailTransaction().getManagerOverrides().size() + 1);
				transaction.getRetailTransaction().getManagerOverrides().add(managerOverride);
			} else if (null != transaction.getControlTransaction()) {
				if (null == transaction.getControlTransaction().getManagerOverrides()) {
					transaction.getControlTransaction().setManagerOverrides(new ArrayList());
				}
				managerOverride.setSequenceNumber(transaction.getControlTransaction().getManagerOverrides().size() + 1);
				transaction.getControlTransaction().getManagerOverrides().add(managerOverride);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	/**Type11: DATA ENTRY**/
	public  void dataEntry(Transaction transaction, List list) {
		try {
			/** identifier EE PREFERRED CUSTOMER TRANSACTION DATA STRING **/
			if (TSLConstants.Type11.IDENTIFIER_VALUE.equals(list.get(TSLConstants.Type11.IDENTIFIER).toString())) {
				PreferredCustomerData preferredCustomerData = new PreferredCustomerData();

				preferredCustomerData.setCustomerAccountID(list.get(TSLConstants.Type11.CUSTOMERACCOUNTID).toString());
				preferredCustomerData.setPoints(new Double(list.get(TSLConstants.Type11.POINTS).toString()));
				preferredCustomerData.setCouponAmount(new Double(list.get(TSLConstants.Type11.COUPONAMOUNT).toString()));
				preferredCustomerData.setCouponCount(new Double(list.get(TSLConstants.Type11.COUPONCOUNT).toString()));
				preferredCustomerData.setMessageCount(new Double(list.get(TSLConstants.Type11.MESSAGECOUNT).toString()));
				preferredCustomerData.setTransferredTransCount(new Double(list.get(TSLConstants.Type11.TRANSFERREDTRANSCOUNT).toString()));
				preferredCustomerData.setTranferredTransAmount(new Double(list.get(TSLConstants.Type11.TRANSFERREDTRANSAMOUNT).toString()));
				preferredCustomerData.setBonusPoints(new Double(list.get(TSLConstants.Type11.BONUSPOINTS).toString()));
				preferredCustomerData.setRedeemedPoints(new Double(list.get(TSLConstants.Type11.REEDEMEDPOINTS).toString()));
				preferredCustomerData.setEntryMethod(new Double(list.get(TSLConstants.Type11.ENTRYMETHOD).toString()));

				transaction.getRetailTransaction().setPreferredCustData(preferredCustomerData);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**Type13: TILL CHANGE**/
	public  void tillChange(Transaction transaction, List list) {
		try {

			TenderControlTransaction tenderControlTransaction = transaction.getTenderControlTransaction();

			TenderControlTransactionLineItem tenderControlTransactionLineItem;

			/** For Loans **/
			int numberOfLoans = new Integer(list.get(TSLConstants.Type13.NUMLOANS).toString());
			if (numberOfLoans > 0) {
				TenderLoanTransaction tenderLoanTransaction = new TenderLoanTransaction();

				tenderLoanTransaction.setOutbound(transaction.getRetailStoreCode());
				tenderLoanTransaction.setInbound(transaction.getWorkstationCode());
				tenderLoanTransaction.setOperatorCode(transaction.getOperatorCode());
				tenderControlTransaction.setLoan(tenderLoanTransaction);
				tenderControlTransaction.setTenderControlTypeCode(TSLConstants.Type13.LOAN);

				for (int i = 5; i < 5 + (numberOfLoans * 2); i = i + 2) {
					tenderControlTransactionLineItem = new TenderControlTransactionLineItem();
					tenderControlTransactionLineItem.setSequenceNumber(++sequence);

					tenderControlTransactionLineItem.setTenderTypeCode(list.get(i).toString());
					tenderControlTransactionLineItem.setAmount(new Double(list.get(i + 1).toString()));

					if (null == transaction.getTenderControlTransaction().getLineItems())
						transaction.getTenderControlTransaction().setLineItems(new ArrayList());

					transaction.getTenderControlTransaction().getLineItems().add(tenderControlTransactionLineItem);
				}
			}
			/** For Pickups **/
			int numberOfPickups = new Integer(list.get(TSLConstants.Type13.NUMPKUPS).toString());
			if (numberOfPickups > 0) {
				TenderPickupTransaction tenderPickupTransaction = new TenderPickupTransaction();

				tenderPickupTransaction.setOutbound(transaction.getWorkstationCode());
				tenderPickupTransaction.setInbound(transaction.getRetailStoreCode());
				tenderPickupTransaction.setOperatorCode(transaction.getOperatorCode());
				tenderControlTransaction.setPickup(tenderPickupTransaction);
				tenderControlTransaction.setTenderControlTypeCode(TSLConstants.Type13.PICKUP);
				for (int i = (numberOfLoans * 2) + 6; i < (numberOfLoans * 2) + 6 + (numberOfPickups * 2); i = i + 2) {
					tenderControlTransactionLineItem = new TenderControlTransactionLineItem();
					tenderControlTransactionLineItem.setSequenceNumber(++sequence);

					tenderControlTransactionLineItem.setTenderTypeCode(list.get(i).toString());
					tenderControlTransactionLineItem.setAmount(new Double(list.get(i + 1).toString()));

					if (null == transaction.getTenderControlTransaction().getLineItems())
						transaction.getTenderControlTransaction().setLineItems(new ArrayList());

					transaction.getTenderControlTransaction().getLineItems().add(tenderControlTransactionLineItem);
				}

			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**Type20: EXCEPTION LOG**/
	public  void exceptionLog(Transaction transaction, List list) {
		try {
			ExceptionLog exceptionLog = new ExceptionLog();

			exceptionLog.setData(list.get(TSLConstants.Type20.LOGDATA).toString());

			if (null == transaction.getRetailTransaction().getExceptionLogs())
				transaction.getRetailTransaction().setExceptionLogs(new ArrayList());

			exceptionLog.setSequenceNumber(transaction.getRetailTransaction().getExceptionLogs().size() + 1);

			transaction.getRetailTransaction().getExceptionLogs().add(exceptionLog);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	/**Type21: STORE CLOSING**/
	public  void storeClosing(Transaction transaction, List list) {
		try {
			transaction.setVoidedFlag(Boolean.FALSE);
			transaction.setCancelFlag(Boolean.FALSE);
			transaction.setKeyedOfflineFlag(Boolean.FALSE);
			transaction.setTrainingModeFlag(Boolean.FALSE);
			transaction.setSequenceNumber(0);
			transaction.setTransactionTypeCode(2);

			Operator operator = new Operator();
			Workstation workstation = new Workstation();

			transaction.setOperator(operator);
			transaction.setWorkstation(workstation);
			transaction.setWorkstationCode(ELConstants.TerminalCode.CONTROLLER_TERM);
			/** dateTimeEndOfTransaction **/
			String datetime = (String) list.get(TSLConstants.Type21.DATETIME);
			int year = Integer.valueOf("20" + datetime.substring(0, 2));
			int month = Integer.valueOf(datetime.substring(2, 4));
			int day = Integer.valueOf(datetime.substring(4, 6));
			int hour = Integer.valueOf(datetime.substring(6, 8));
			int minutes = Integer.valueOf(datetime.substring(8, 10));
			Calendar calendar = new GregorianCalendar(year, month-1, day, hour, minutes);
			Date dateTimeEndOfTransaction = calendar.getTime();
			transaction.setBeginDateTimeString(ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(dateTimeEndOfTransaction));

			ControlTransaction controlTransaction = new ControlTransaction();
			BusinessEODTransaction businessEODTransaction = new BusinessEODTransaction();

			controlTransaction.setTypeCode(TSLConstants.Type21.STORE_CLOSING);
			controlTransaction.setBusinessEOD(businessEODTransaction);

			transaction.setControlTransaction(controlTransaction);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/** Type99: USER DATA **/
	public  void userData(Transaction transaction, List list) {
		try {
			log.debug("TYPE: " + list.get(TSLConstants.Type99.TYPE).toString());
			Object object = getTypeOfTransaction(transaction);
//			if (list.get(TSLConstants.Type99.TYPE).toString().equals("189011")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("20070316")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("130506")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("20060813")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("0063")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("20060814")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("0042")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("20190815")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("20060816")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("2001")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("0066")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("0053")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("0128")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("20080227")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("0067")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("99007002")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("0623")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("0005")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("0077")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("20080225")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("0208")
//					|| list.get(TSLConstants.Type99.TYPE).toString().equals("0005")
//					)			{
//				log.info("Tipo desconocido");
//			} else {
				if (object instanceof RetailTransaction) {
					RetailTransaction retailTransaction = (RetailTransaction) object;
					StringUsuario stringUsuario = new StringUsuario();
					String sUsuario = "";
					Iterator<String> it = list.iterator();
					while (it.hasNext()) {
						sUsuario = sUsuario + it.next() + ArmsServerConstants.Communication.FRAME_SEP;
					}

					stringUsuario.setCadena(sUsuario);
					if (null == retailTransaction.getStringsUsuario())
						transaction.getRetailTransaction().setStringsUsuario(new ArrayList());
					stringUsuario.setSequenceNumber(retailTransaction.getStringsUsuario().size() + 1);
					retailTransaction.getStringsUsuario().add(stringUsuario);
				}
				if (list.get(TSLConstants.Type99.TYPE).toString().equals(TSLConstants.Type99.FACTURA_ELEC)) {

					if (list.size()>= 6 && object instanceof RetailTransaction) {
						RetailTransaction retailTransaction = (RetailTransaction) object;
						InvoiceData invoiceData = retailTransaction.getInvoiceData() != null ? retailTransaction.getInvoiceData() : new InvoiceData();
						invoiceData.setCustomerID(list.get(TSLConstants.Type99.SubType0029.RUC_CLIENTE).toString());
						invoiceData.setCustomerName(list.get(TSLConstants.Type99.SubType0029.NOMBRE_CLIENTE).toString());

						if (null == retailTransaction.getInvoiceData())
							transaction.getRetailTransaction().setInvoiceData(invoiceData);
						FacturaElec facElec = retailTransaction.getFacturaElec() != null ? retailTransaction.getFacturaElec() : new FacturaElec();
						facElec.setNumeroFac(list.get(TSLConstants.Type99.SubType0029.NUM_TRX_CAJA).toString());
						retailTransaction.setRetailTransactionTypeCode(1);
						retailTransaction.setFacturaElec(facElec);
					}
				} else if (list.get(TSLConstants.Type99.TYPE).toString().equals(TSLConstants.Type99.NOTA_CREDITO_ELEC) ||
						list.get(TSLConstants.Type99.TYPE).toString().equals(TSLConstants.Type99.NOTA_CRED_VALE_ELEC)) { 
					//cambiar por el 45, complementario puede emitir el vale electronico.
					if (list.size()>= 10 && object instanceof RetailTransaction) {
						RetailTransaction retailTransaction = (RetailTransaction) object;
						InvoiceData invoiceData = retailTransaction.getInvoiceData() != null ? retailTransaction.getInvoiceData() : new InvoiceData();
						invoiceData.setCustomerID(list.get(TSLConstants.Type99.SubType0028.RUC_CLIENTE).toString());
						invoiceData.setCustomerName(list.get(TSLConstants.Type99.SubType0028.NOMBRE_CLIENTE).toString());

						if (null == retailTransaction.getInvoiceData())
							transaction.getRetailTransaction().setInvoiceData(invoiceData);

						ReturnTransaction returnTransaction = retailTransaction.getNotaCredito() != null ? retailTransaction.getNotaCredito() : new ReturnTransaction();
						returnTransaction.setNumeroDocOriginal((String) list.get(TSLConstants.Type99.SubType0028.NRO_TRX_ORIGINAL));
						returnTransaction.setTipo(2);
						returnTransaction.setFechaContOrig(TSLConstants.Formatters.TLOG_SUFIX_DATE_TIME_FORMATTER_SM.parse((String)list.get(TSLConstants.Type99.SubType0028.FECHA_TRX_ORIGINAL).toString()));
						returnTransaction.setNumeroNotaCredito(list.get(TSLConstants.Type99.SubType0028.NUM_NOTA).toString());

						transaction.getRetailTransaction().setRetailTransactionTypeCode(2);
						transaction.getRetailTransaction().setNotaCredito(returnTransaction);
					}
				} else if (list.get(TSLConstants.Type99.TYPE).toString().equals(TSLConstants.Type99.TOTALES_TRX)) {

					if (list.size()>= 9 && object instanceof RetailTransaction) {
						RetailTransaction retailTransaction = (RetailTransaction) object;
						FacturaElec facElec = retailTransaction.getFacturaElec() != null ? retailTransaction.getFacturaElec() : new FacturaElec();
						facElec.setTotal(list.get(TSLConstants.Type99.SubType0034.TOTAL).toString());
						facElec.setSubTotal(list.get(TSLConstants.Type99.SubType0034.SUBTOTAL1).toString());
						retailTransaction.setRetailTransactionTypeCode(1);
						retailTransaction.setFacturaElec(facElec);
					}
				} else if (list.get(TSLConstants.Type99.TYPE).toString().equals(TSLConstants.Type99.DESC_EMP_DATA)) {
					RetailTransaction retailTransaction = (RetailTransaction) object;
					Long itemCode = Long.valueOf(list.get(TSLConstants.Type99.SubType98.COD_ITEM).toString());
					SaleReturnLineItem saleReturnLineItem = getFirstSaleReturnLineItemSeqNumWithoutDescEmp(retailTransaction.getLineItems(), itemCode);
					Double signo = Double.valueOf(list.get(TSLConstants.Type99.SubType98.SIGNO).toString());
					Double discount = (signo.doubleValue() > 0 ? -1 : 1)  * Double.valueOf(list.get(TSLConstants.Type99.SubType98.DESC_TOTAL).toString());
					addManualDiscount(retailTransaction, saleReturnLineItem, discount, false);

				} else if(list.get(TSLConstants.Type99.TYPE).toString().equals(TSLConstants.Type99.DATOS_ADIC_TRX2)) {
					String subtype = list.get(TSLConstants.Type99.SubType0203.SUBTYPE).toString();
					if (subtype.equals("03")) {
						RetailTransaction retailTransaction = (RetailTransaction) object;

						//feha y hora de FIN de la trx
						String date = list.get(TSLConstants.Type99.SubType0203.DATA_1).toString();
						String time = list.get(TSLConstants.Type99.SubType0203.DATA_2).toString();
						int year = Integer.valueOf("20" + date.substring(0, 2));
						int month = Integer.valueOf(date.substring(2, 4));
						int day = Integer.valueOf(date.substring(4, 6));
						int hour = Integer.valueOf(time.substring(0, 2));
						int minutes = Integer.valueOf(time.substring(2, 4));
						int seconds = Integer.valueOf(time.substring(4, 6));
						Calendar calendar = new GregorianCalendar(year, month - 1, day, hour, minutes, seconds);
						Date dateTimeEndOfTransaction = calendar.getTime();

						transaction.setBeginDateTimeString(ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(DateUtils.addSeconds(
								dateTimeEndOfTransaction,
								-(ObjectUtils.defaultIfNull(retailTransaction.getRingElapsedTime(), 0) + ObjectUtils.defaultIfNull(retailTransaction.getTenderElapsedTime(), 0)))));

						transaction.setEndDateTimeString(ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(dateTimeEndOfTransaction));
						transaction.setEndDateTime(dateTimeEndOfTransaction);
					} else if (subtype.equals("04")) {
						//datos de transaccion de ecommerce
						String ordenEc = list.get(TSLConstants.Type99.SubType0203.DATA_1).toString();
						String nroFact = list.get(TSLConstants.Type99.SubType0203.DATA_2).toString();
						String hora = list.get(TSLConstants.Type99.SubType0203.DATA_3).toString();
						EcommerceData ecommerceData = new EcommerceData();
						ecommerceData.setOrden(Integer.valueOf(ordenEc));
						ecommerceData.setFactura(nroFact);
						ecommerceData.setHora(hora);
						transaction.getRetailTransaction().setEcommerceData(ecommerceData);
					}
				}else if (list.get(TSLConstants.Type99.TYPE).toString()
							.equals(TSLConstants.Type99.DESC_ART_PROMO_LOYALTY)) {

						if (object instanceof RetailTransaction) {

							//Código de barras del producto
							//Indica el # de registro para el mismo artículo
							//Detalle de dsctos otorgados a este producto en esta transacción.
							// 6 cod promo, 6 valor Dcto, 4 Cant unidades que recibieron el dscto

							Long barCode = Long.valueOf((String) list.get(TSLConstants.Type99.SubType9021.BARCODE));

							String[] details = ((String) list.get(TSLConstants.Type99.SubType9021.DETAILS))
									.split("(?<=\\G.{16})");

							if (transaction.getRetailTransaction().getPromotionDiscs() == null) {
								transaction.getRetailTransaction().setPromotionDiscs(new ArrayList());
							}

							SaleReturnLineItem item = getFirstSaleReturnLineItemByItemCode(
									transaction.getRetailTransaction().getLineItems(), barCode);

							Integer seqNumber = item != null ? item.getSequenceNumber() : null;

							if (seqNumber != null) {
								for (String detail : details) {
									PromotionDiscount promotionDiscount = new PromotionDiscount();
									promotionDiscount.setSequenceNumber(seqNumber);
									promotionDiscount.setOrdinalNumber(seqNumber);
									promotionDiscount.setPromotionCode(detail.substring(0, 6));
									promotionDiscount.setAmount(new Double(detail.substring(6, 12)));
									promotionDiscount.setFlvd(false);
									promotionDiscount.setPromotionSequenceNumber(transaction
											.getRetailTransaction().getPromotionDiscs().size() + 1);

									transaction.getRetailTransaction().getPromotionDiscs().add(promotionDiscount);
								}
							}


						}
					}
//			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	/**
	 * Metodo utilizado para agregar al tlog los descuentos que no se originaron por Vector (Mayoreo y Descuento Empleado).
	 *
	 * @param retailTrx
	 * @param saleReturnLineItem
	 * @param discount
	 * @param isPorMayor
	 */
	private  void addManualDiscount(RetailTransaction retailTrx, SaleReturnLineItem saleReturnLineItem, Double discount, boolean isPorMayor){
		// Se crea item SERVICE para guardar en la TR_LTM_SLS_RTN el item donde se refleja el descuento por caja
		RetailTransactionLineItem retailTransactionLineItemService = new RetailTransactionLineItem();
		retailTransactionLineItemService.setSequenceNumber(++sequence);
		retailTransactionLineItemService.setVoidFlag(Boolean.FALSE);
		SaleReturnLineItem saleReturnLineItemService = new SaleReturnLineItem();
		saleReturnLineItemService.setQuantity(Double.valueOf("1"));
		saleReturnLineItemService.setItemType(7);
		saleReturnLineItemService.setItemCode(Long.valueOf("811099999998"));
		saleReturnLineItemService.setExtendedAmount(discount < 0 ? -discount : discount);
		saleReturnLineItemService.setOrdinalNumber(0);
		saleReturnLineItemService.setRegularSalesUnitPrice(discount < 0 ? -discount : discount);
		saleReturnLineItemService.setEntryMethodCode("SERVICE");
		saleReturnLineItemService.setPosDepartmentCode(0);
		saleReturnLineItemService.setMerchandiseHierarchyGroupCode(0);
		saleReturnLineItemService.setIsPromo(Boolean.TRUE);
		saleReturnLineItemService.setSequenceNumber(retailTransactionLineItemService.getSequenceNumber());
		retailTransactionLineItemService.setSaleLI(saleReturnLineItemService);
		if (null == retailTrx.getLineItems())
			retailTrx.setLineItems(new ArrayList());
		retailTrx.getLineItems().add(retailTransactionLineItemService);

		PromotionDiscount promoDisc = new PromotionDiscount();
		promoDisc.setSequenceNumber(saleReturnLineItem.getSequenceNumber());
		promoDisc.setPromotionSequenceNumber(saleReturnLineItemService.getSequenceNumber());
		promoDisc.setOrdinalNumber(saleReturnLineItem.getOrdinalNumber());
		promoDisc.setAmount(discount < 0 ? -discount : discount);
		promoDisc.setPromotionCode(isPorMayor ? "MAY999999" : "DE999999");
		promoDisc.setFlvd(Boolean.FALSE);

		// marcamos el articulo vendido por redencion
		if(isPorMayor)
			saleReturnLineItem.setIsPorMayor(isPorMayor);

		if (retailTrx.getPromotionDiscs() == null)
			retailTrx.setPromotionDiscs(new ArrayList());
		retailTrx.getPromotionDiscs().add(promoDisc);
		if(discount >= 0){
			RetailTransactionTotal totalPromoPos = null;
			RetailTransactionTotal totalGrossPos = null;
			RetailTransactionTotal totalGrossNeg = null;
			Iterator itTotalItems = retailTrx.getTotalItems().iterator();
			while (itTotalItems.hasNext() && (totalPromoPos == null || totalGrossPos == null || totalGrossNeg == null)) {
				RetailTransactionTotal totalItem = (RetailTransactionTotal) itTotalItems.next();
				if(totalItem.getTransactionTotalTypeCode() != null && TSLConstants.Type00.TRANSACTION_POSITIVE_DISCOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
					totalPromoPos = totalItem;
				} else if(TSLConstants.Type00.TRANSACTION_GROSS_POSITIVE_AMOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
					totalGrossPos = totalItem;
				} else if(TSLConstants.Type00.TRANSACTION_GROSS_NEGATIVE_AMOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
					totalGrossNeg = totalItem;
				}
			}
			if(totalPromoPos==null){
				totalPromoPos = new RetailTransactionTotal();
				totalPromoPos.setAmount(promoDisc.getAmount());
				totalPromoPos.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_POSITIVE_DISCOUNT);
				retailTrx.getTotalItems().add(totalPromoPos);
			} else {
				Double temp = promoDisc.getAmount() + totalPromoPos.getAmount();
				totalPromoPos.setAmount(temp);
			}
			if(isPorMayor)
				if(totalGrossPos == null){
					totalGrossPos = new RetailTransactionTotal();
					totalGrossPos.setAmount(promoDisc.getAmount());
					totalGrossPos.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_GROSS_POSITIVE_AMOUNT);
					retailTrx.getTotalItems().add(totalGrossPos);
				} else {
					totalGrossPos.setAmount(totalGrossPos.getAmount() + promoDisc.getAmount());
				}
			else
				if(totalGrossNeg == null){
					totalGrossNeg = new RetailTransactionTotal();
					totalGrossNeg.setAmount(-promoDisc.getAmount());
					totalGrossNeg.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_GROSS_NEGATIVE_AMOUNT);
					retailTrx.getTotalItems().add(totalGrossNeg);
				} else {
					totalGrossNeg.setAmount(totalGrossNeg.getAmount() - promoDisc.getAmount());
				}
		} else {
			RetailTransactionTotal totalPromoNeg = null;
			RetailTransactionTotal totalGrossPos = null;
			RetailTransactionTotal totalGrossNeg = null;
			Iterator itTotalItems = retailTrx.getTotalItems().iterator();
			while (itTotalItems.hasNext() && totalPromoNeg == null) {
				RetailTransactionTotal totalItem = (RetailTransactionTotal) itTotalItems.next();
				if(totalItem.getTransactionTotalTypeCode() != null && TSLConstants.Type00.TRANSACTION_NEGATIVE_DISCOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
					totalPromoNeg = totalItem;
				} else if(TSLConstants.Type00.TRANSACTION_GROSS_POSITIVE_AMOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
					totalGrossPos = totalItem;
				} else if(TSLConstants.Type00.TRANSACTION_GROSS_NEGATIVE_AMOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
					totalGrossNeg = totalItem;
				}
			}
			if(totalPromoNeg==null){
				totalPromoNeg = new RetailTransactionTotal();
				totalPromoNeg.setAmount(promoDisc.getAmount());
				totalPromoNeg.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_NEGATIVE_DISCOUNT);
				retailTrx.getTotalItems().add(totalPromoNeg);
			} else {
				Double temp = promoDisc.getAmount() + totalPromoNeg.getAmount();
				totalPromoNeg.setAmount(temp);
			}
			if(isPorMayor)
				if(totalGrossPos == null){
					totalGrossPos = new RetailTransactionTotal();
					totalGrossPos.setAmount(-promoDisc.getAmount());
					totalGrossPos.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_GROSS_POSITIVE_AMOUNT);
					retailTrx.getTotalItems().add(totalGrossPos);
				} else {
					totalGrossPos.setAmount(totalGrossPos.getAmount() - promoDisc.getAmount());
				}
			else
				if(totalGrossNeg == null){
					totalGrossNeg = new RetailTransactionTotal();
					totalGrossNeg.setAmount(promoDisc.getAmount());
					totalGrossNeg.setTransactionTotalTypeCode(TSLConstants.Type00.TRANSACTION_GROSS_NEGATIVE_AMOUNT);
					retailTrx.getTotalItems().add(totalGrossNeg);
				} else {
					totalGrossNeg.setAmount(totalGrossNeg.getAmount() + promoDisc.getAmount());
				}
		}

	}

	private  void addRedemptionDiscount(RetailTransaction retailTrx, PointsRedemptionData pointsRedemptionData){
		// Se crea item SERVICE para guardar en la TR_LTM_SLS_RTN el item donde se refleja el descuento por caja
		Double discount = new Double(pointsRedemptionData.getDiscount());
//		RetailTransactionLineItem retailTransactionLineItemService = new RetailTransactionLineItem();
//		retailTransactionLineItemService.setSequenceNumber(++sequence);
//		retailTransactionLineItemService.setVoidFlag(Boolean.FALSE);
//		SaleReturnLineItem saleReturnLineItemService = new SaleReturnLineItem();
//		saleReturnLineItemService.setQuantity(Double.valueOf("1"));
//		saleReturnLineItemService.setItemType(7);
//		saleReturnLineItemService.setItemCode(Long.valueOf("811099999998"));
//		saleReturnLineItemService.setExtendedAmount(discount < 0 ? -discount : discount);
//		saleReturnLineItemService.setOrdinalNumber(0);
//		saleReturnLineItemService.setRegularSalesUnitPrice(discount < 0 ? -discount : discount);
//		saleReturnLineItemService.setEntryMethodCode("SERVICE");
//		saleReturnLineItemService.setPosDepartmentCode(0);
//		saleReturnLineItemService.setMerchandiseHierarchyGroupCode(0);
//		saleReturnLineItemService.setIsPorMayor(false);
//		retailTransactionLineItemService.setSaleLI(saleReturnLineItemService);
//		if (null == retailTrx.getLineItems())
//			retailTrx.setLineItems(new ArrayList());
//		retailTrx.getLineItems().add(retailTransactionLineItemService);

		PromotionDiscount promoDisc = new PromotionDiscount();
		promoDisc.setOrdinalNumber(0);
		if(pointsRedemptionData.getOrdinalNumber() != null)
			promoDisc.setSequenceNumber(getLineItemByOrdNumSeq(retailTrx.getLineItems(), pointsRedemptionData.getOrdinalNumber()));
		else
			promoDisc.setSequenceNumber(getFirstSaleReturnLineItemSeqNumWithoutRedemption(retailTrx.getLineItems(), pointsRedemptionData.getCodItem()));
		promoDisc.setAmount(discount < 0 ? -discount : discount);
		promoDisc.setPromotionCode("RE9999999");
		promoDisc.setFlvd(Boolean.FALSE);

		// marcamos el articulo vendido por redencion
		SaleReturnLineItem saleReturnLineItem = getLineItemByOrdNum(retailTrx.getLineItems(), pointsRedemptionData.getOrdinalNumber());
		if (saleReturnLineItem!= null && saleReturnLineItem.getItemCode().compareTo(Long.valueOf(811099999996L)) != 0) {
			saleReturnLineItem.setIsPorRedencion(Boolean.TRUE);
		}
		//marcamos el cupon como de promocion (para asociacion con tabla TR_LTM_PRM)
		SaleReturnLineItem saleReturnLineItemPromo = getLineItemByOrdNum(retailTrx.getLineItems(), pointsRedemptionData.getOrdinalNumber()+1);
		if (saleReturnLineItemPromo!= null && saleReturnLineItemPromo.getItemCode().compareTo(Long.valueOf(811099999996L)) == 0) {
			saleReturnLineItemPromo.setIsPromo(Boolean.TRUE);
			promoDisc.setAmount(saleReturnLineItemPromo.getExtendedAmount());
			promoDisc.setPromotionSequenceNumber(saleReturnLineItemPromo.getSequenceNumber());
		}
		if (retailTrx.getPromotionDiscs() == null)
			retailTrx.setPromotionDiscs(new ArrayList());
		retailTrx.getPromotionDiscs().add(promoDisc);
//		if(discount >= 0){
//			RetailTransactionTotal totalPromoPos = null;
//			Iterator itTotalItems = retailTrx.getTotalItems().iterator(); 
//			while (itTotalItems.hasNext() && totalPromoPos == null) {
//				RetailTransactionTotal totalItem = (RetailTransactionTotal) itTotalItems.next();
//				if(totalItem.getTransactionTotalTypeCode() != null && TSLConstants.Type00.TRANSACTION_PROMO_POSITIVE_DISCOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
//					totalPromoPos = totalItem;
//				}
//			}
//			if(totalPromoPos==null){
//				totalPromoPos = new RetailTransactionTotal();
//				totalPromoPos.setAmount(promoDisc.getAmount());
//				retailTrx.getTotalItems().add(totalPromoPos);
//			} else {
//				Double temp = promoDisc.getAmount() + totalPromoPos.getAmount();
//				totalPromoPos.setAmount(temp);
//			}
//		} else {
//			RetailTransactionTotal totalPromoNeg = null;
//			Iterator itTotalItems = retailTrx.getTotalItems().iterator(); 
//			while (itTotalItems.hasNext() && totalPromoNeg == null) {
//				RetailTransactionTotal totalItem = (RetailTransactionTotal) itTotalItems.next();
//				if(totalItem.getTransactionTotalTypeCode() != null && TSLConstants.Type00.TRANSACTION_PROMO_NEGATIVE_DISCOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
//					totalPromoNeg = totalItem;
//				}
//			}
//			if(totalPromoNeg==null){
//				totalPromoNeg = new RetailTransactionTotal();
//				totalPromoNeg.setAmount(promoDisc.getAmount());
//				retailTrx.getTotalItems().add(totalPromoNeg);
//			} else {
//				Double temp = promoDisc.getAmount() + totalPromoNeg.getAmount();
//				totalPromoNeg.setAmount(temp);
//			}
//		}

		//modificamos el gross neg porque la caja lo considero como descuento y se desea que se registre en descuentos por promociones
//		RetailTransactionTotal totalGross = null;
//		Iterator itTotalItems = retailTrx.getTotalItems().iterator(); 
//		while (itTotalItems.hasNext() && totalGross == null) {
//			RetailTransactionTotal totalItem = (RetailTransactionTotal) itTotalItems.next();
//			if(discount >= 0 && totalItem.getTransactionTotalTypeCode() != null && TSLConstants.Type00.TRANSACTION_GROSS_NEGATIVE_AMOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
//				totalGross = totalItem;
//				break;
//			} else if(discount < 0 && totalItem.getTransactionTotalTypeCode() != null && TSLConstants.Type00.TRANSACTION_GROSS_POSITIVE_AMOUNT.equalsIgnoreCase(totalItem.getTransactionTotalTypeCode())){
//				totalGross = totalItem;
//				break;
//			}
//		}
//		if(totalGross!=null){
//			Double temp = totalGross.getAmount() - promoDisc.getAmount();
//			totalGross.setAmount(temp);
//		}
	}

	private  void loadTenderReturnLineItems(RetailTransaction retailTransaction){
		Iterator itLineItems = retailTransaction.getLineItems().iterator();
		while(itLineItems.hasNext()){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) itLineItems.next();
			if(lineItem.getTender() != null && lineItem.getTender().getIsChangeFlag()){
				TenderLineItem tenderLineItem = lineItem.getTender();
				TenderReturnLineItem tenderReturnLineItem = new TenderReturnLineItem();
				tenderReturnLineItem.setSequenceNumber(tenderLineItem.getSequenceNumber());
				tenderReturnLineItem.setStatus(0);
				tenderLineItem.setTenderReturnLineItem(tenderReturnLineItem);
				//obtenemos el pago en 0 anterior para obtener el AccountNumber
				RetailTransactionLineItem lineItemAux = (RetailTransactionLineItem) retailTransaction.getLineItems().get(tenderReturnLineItem.getSequenceNumber()-2);
				tenderLineItem.setTenderAccountNumber(lineItemAux.getTender().getTenderAccountNumber());
			}
		}
	}

	/**
	 * Metodo que busca el primer SaleReturnLineItem que contenga el itemCode
	 * recibido como parametro
	 *
	 * @param lineItems
	 * @return
	 */
	private SaleReturnLineItem getFirstSaleReturnLineItemByItemCode(List lineItems, Long itemCode) {
		int cant = lineItems.size();
		for (int i = 0; i < cant; i++) {
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) lineItems.get(i);
			SaleReturnLineItem saleReturnLineItem = null;
			if (lineItem.getSaleLI() != null) {
				saleReturnLineItem = lineItem.getSaleLI();
			} else if (lineItem.getReturnLI() != null) {
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if (saleReturnLineItem != null && saleReturnLineItem.getPosItemID() != null
					&& itemCode.compareTo(saleReturnLineItem.getPosItemID()) == 0) {
				return saleReturnLineItem;
			}
		}
		return null;
	}

	private  int getLineItemSeqNumByOrdNum(List lineItems, int ordinalNumber){
		Iterator itLineItems = lineItems.iterator();
		while(itLineItems.hasNext()){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) itLineItems.next();
			SaleReturnLineItem saleReturnLineItem = null;
			if(lineItem.getSaleLI() != null){
				saleReturnLineItem = lineItem.getSaleLI();
			} else if(lineItem.getReturnLI() != null){
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if (saleReturnLineItem != null && saleReturnLineItem.getOrdinalNumber().intValue() == ordinalNumber) {
				return saleReturnLineItem.getSequenceNumber();
			}
		}
		return 0;
	}

	/**
	 * Metodo que busca el primer sequence number correspondiente a un SaleReturnLineItem que no tenga asociado un resumen de ítem
	 * @param lineItems
	 * @return
	 */
	private  SaleReturnLineItem getFirstSaleReturnLineItemSeqNumWithoutResumenItem(List lineItems, Long itemCode){
		int cant = lineItems.size();
		for(int i = 0; i < cant; i++){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) lineItems.get(i);
			SaleReturnLineItem saleReturnLineItem = null;
			if(lineItem.getSaleLI() != null){
				saleReturnLineItem = lineItem.getSaleLI();
			} else if(lineItem.getReturnLI() != null){
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if (saleReturnLineItem != null && !"SERVICE".equalsIgnoreCase(saleReturnLineItem.getEntryMethodCode())
					&& !saleReturnLineItem.getItemType().equals(Integer.valueOf(TSLConstants.Type01.INDICAT3_T_STORE_COUPON_VALUE)) && saleReturnLineItem.getResumenItem() == null
					&& saleReturnLineItem.getPosItemID().compareTo(itemCode) == 0){
				return saleReturnLineItem;
			}
		}
		return null;
	}

	/**
	 * Metodo que busca el primer sequence number correspondiente a un SaleReturnLineItem que no tenga asociado un descuento por Redencion
	 * cuyo codigo de item coincida con el recibido como parametro
	 * Antes de retornar se marca como articulo vendido con redencion
	 * @param lineItems
	 * @return
	 */
	private  int getFirstSaleReturnLineItemSeqNumWithoutRedemption(List lineItems, Long itemCode){
		int cant = lineItems.size();
		for(int i = 0; i < cant; i++){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) lineItems.get(i);
			SaleReturnLineItem saleReturnLineItem = null;
			if(lineItem.getSaleLI() != null){
				saleReturnLineItem = lineItem.getSaleLI();
			} else if(lineItem.getReturnLI() != null){
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if (saleReturnLineItem != null && !"SERVICE".equalsIgnoreCase(saleReturnLineItem.getEntryMethodCode()) && !saleReturnLineItem.getItemType().equals(Integer.valueOf(TSLConstants.Type01.INDICAT3_T_STORE_COUPON_VALUE))
					&& !saleReturnLineItem.getIsPorRedencion()){
				saleReturnLineItem.setIsPorRedencion(Boolean.TRUE);
				return saleReturnLineItem.getSequenceNumber();
			}
		}
		return 0;
	}

	/**
	 * Metodo que busca el primer SaleReturnLineItem que no tenga asociado un descuento de Empleado
	 * cuyo codigo de item coincida con el recibido como parametro
	 * Antes de retornar se marca como articulo vendido con descuento de empleado
	 * @param lineItems
	 * @return
	 */
	private  SaleReturnLineItem getFirstSaleReturnLineItemSeqNumWithoutDescEmp(List lineItems, Long itemCode){
		int cant = lineItems.size();
		for(int i = 0; i < cant; i++){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) lineItems.get(i);
			SaleReturnLineItem saleReturnLineItem = null;
			if(lineItem.getSaleLI() != null){
				saleReturnLineItem = lineItem.getSaleLI();
			} else if(lineItem.getReturnLI() != null){
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if (saleReturnLineItem != null && !"SERVICE".equalsIgnoreCase(saleReturnLineItem.getEntryMethodCode()) && !saleReturnLineItem.getItemType().equals(Integer.valueOf(TSLConstants.Type01.INDICAT3_T_STORE_COUPON_VALUE))
					&& !saleReturnLineItem.getIsDescEmp() && saleReturnLineItem.getItemCode().compareTo(itemCode) == 0){
				saleReturnLineItem.setIsDescEmp(Boolean.TRUE);
				return saleReturnLineItem;
			}
		}
		return null;
	}


	/**
	 * Metodo que retorna el ultimo sequence number correspondiente a un SaleReturnLineItem
	 * @param lineItems
	 * @return
	 */
	private  int getLastSaleReturnLineItemSeqNum(List lineItems){
		int cant = lineItems.size();
		for(int i = cant-1; i >= 0; i--){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) lineItems.get(i);
			SaleReturnLineItem saleReturnLineItem = null;
			if(lineItem.getSaleLI() != null){
				saleReturnLineItem = lineItem.getSaleLI();
			} else if(lineItem.getReturnLI() != null){
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if (saleReturnLineItem != null && !"SERVICE".equalsIgnoreCase(saleReturnLineItem.getEntryMethodCode()) && !saleReturnLineItem.getItemType().equals(Integer.valueOf(TSLConstants.Type01.INDICAT3_T_STORE_COUPON_VALUE))){
				return saleReturnLineItem.getSequenceNumber();
			}
		}
		return 0;
	}

	/**
	 * Metodo que retorna el ultimo SaleReturnLineItem
	 * @param lineItems
	 * @return
	 */
	private  SaleReturnLineItem getLastSaleReturnLineItem(List lineItems){
		int cant = lineItems.size();
		for(int i = cant-1; i >= 0; i--){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) lineItems.get(i);
			SaleReturnLineItem saleReturnLineItem = null;
			if(lineItem.getSaleLI() != null){
				saleReturnLineItem = lineItem.getSaleLI();
			} else if(lineItem.getReturnLI() != null){
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if (saleReturnLineItem != null && !"SERVICE".equalsIgnoreCase(saleReturnLineItem.getEntryMethodCode()) && !saleReturnLineItem.getItemType().equals(Integer.valueOf(TSLConstants.Type01.INDICAT3_T_STORE_COUPON_VALUE))){
				return saleReturnLineItem;
			}
		}
		return null;
	}


	/**
	 * Metodo que retorna el ultimo sequence number correspondiente a un TenderLineItem
	 * @param lineItems
	 * @return
	 */
	private  int getLastTenderLineItemSeqNum(List lineItems){
		int cant = lineItems.size();
		for(int i = cant-1; i >= 0; i--){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) lineItems.get(i);
			return lineItem.getSequenceNumber()+1;
		}
		return 0;
	}

	/**
	 * Metodo que retorna el ultimo sequence number correspondiente a un TenderLineItem NO anulado
	 * cuyo monto y medio de pago coincida con lo recibido como parametro.
	 * @param lineItems
	 * @return
	 */
	private  int getLastTenderLineItemVoidedSeqNum(List lineItems, Double amount, String tenderType, String tenderAccountNumber){
		int cant = lineItems.size();
		for(int i = cant-1; i >= 0; i--){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) lineItems.get(i);
			if(lineItem!=null && lineItem.getTender()!=null && !lineItem.getTender().getIsVoided() && lineItem.getTender().getTenderTypeCode().equals(tenderType) && lineItem.getTender().getAmount().compareTo(amount)==0 && (lineItem.getTender().getTenderAccountNumber() == null || lineItem.getTender().getTenderAccountNumber().equalsIgnoreCase(tenderAccountNumber))){
				lineItem.getTender().setIsVoided(Boolean.TRUE);
				return lineItem.getSequenceNumber();
			}
		}
		return 0;
	}

	/**
	 * Metodo que borra los datos de Pinpad del pago con seqNum recibido.
	 * @param pagosPinpad
	 * @param seqNum
	 */
	private  void deletePinpadDataIfExist(List pagosPinpad, int seqNum){
		if(pagosPinpad != null){
			Iterator itPagos = pagosPinpad.iterator();
			while(itPagos.hasNext()){
				TenderPinpadInfo pago = (TenderPinpadInfo) itPagos.next();
				if(pago.getSequenceNumber().compareTo(seqNum) == 0){
					itPagos.remove();
					break;
				}
			}
		}
	}

	private  int getNextPromotionLineItemSeqNum(List lineItems, int promoDiscCount){
		Iterator itLineItems = lineItems.iterator();
		while(itLineItems.hasNext()){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) itLineItems.next();
			SaleReturnLineItem saleReturnLineItem = null;
			if(lineItem.getSaleLI() != null){
				saleReturnLineItem = lineItem.getSaleLI();
			} else if(lineItem.getReturnLI() != null){
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if (saleReturnLineItem != null && "SERVICE".equalsIgnoreCase(saleReturnLineItem.getEntryMethodCode()) && saleReturnLineItem.getItemCode().compareTo(Long.valueOf(811099999998L)) == 0) {
				if(promoDiscCount == 0){
					return saleReturnLineItem.getSequenceNumber();
				}
				promoDiscCount--;
			}
		}
		return 0;
	}

	private  RetailTransactionLineItem getLineItemToSetTax(List lineItems, Long itemCode){
		Iterator itLineItems = lineItems.iterator();
		while(itLineItems.hasNext()){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) itLineItems.next();
			SaleReturnLineItem saleReturnLineItem = null;
			if(lineItem.getSaleLI() != null){
				saleReturnLineItem = lineItem.getSaleLI();
			} else if(lineItem.getReturnLI() != null){
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if(saleReturnLineItem != null && itemCode.compareTo(saleReturnLineItem.getItemCode())==0 && saleReturnLineItem.getAppliedTax()==null){
				return lineItem;
			}
		}
		return null;
	}

	private  Integer getLineItemByOrdNumSeq(List lineItems, Integer ordinalNumber){
		Iterator itLineItems = lineItems.iterator();
		while(itLineItems.hasNext()){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) itLineItems.next();
			SaleReturnLineItem saleReturnLineItem = null;
			if(lineItem.getSaleLI() != null){
				saleReturnLineItem = lineItem.getSaleLI();
			} else if(lineItem.getReturnLI() != null){
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if(ordinalNumber.compareTo(saleReturnLineItem.getOrdinalNumber())==0){
				return saleReturnLineItem.getSequenceNumber();
			}
		}
		return 0;
	}

	private  SaleReturnLineItem getLineItemByOrdNum(List lineItems, Integer ordinalNumber){
		Iterator itLineItems = lineItems.iterator();
		while(itLineItems.hasNext()){
			RetailTransactionLineItem lineItem = (RetailTransactionLineItem) itLineItems.next();
			SaleReturnLineItem saleReturnLineItem = null;
			if(lineItem.getSaleLI() != null){
				saleReturnLineItem = lineItem.getSaleLI();
			} else if(lineItem.getReturnLI() != null){
				saleReturnLineItem = lineItem.getReturnLI();
			}
			if(ordinalNumber.compareTo(saleReturnLineItem.getOrdinalNumber())==0){
				return saleReturnLineItem;
			}
		}
		return null;
	}

	/**
	 * get the Entry Method Name
	 *
	 * @param entryMethodCodeValue
	 * @return
	 */
	public  String getEntryMethodName(String entryMethodCodeValue) {
		String value = "";
		try {
			if (entryMethodCodeValue.equals(TSLConstants.Type01.INDICAT3_O_SCANNED_ITEM_CODE_VALUE))
				value = TSLConstants.Type01.INDICAT3_O_SCANNED_ITEM_CODE_NAME;
			else if (entryMethodCodeValue.equals(TSLConstants.Type01.INDICAT3_O_KEYED_ITEM_CODE_VALUE))
				value = TSLConstants.Type01.INDICAT3_O_KEYED_ITEM_CODE_NAME;
			else if (entryMethodCodeValue.equals(TSLConstants.Type01.INDICAT3_O_ITEM_LOOKUP_KEY_USED_VALUE))
				value = TSLConstants.Type01.INDICAT3_O_ITEM_LOOKUP_KEYED_USED_NAME;
			else if (entryMethodCodeValue.equals(TSLConstants.Type01.INDICAT3_O_ITEM_CODE_LINKED_TO_VALUE))
				value = TSLConstants.Type01.INDICAT3_O_ITEM_CODE_LINKED_TO_NAME;
			else if (entryMethodCodeValue.equals(TSLConstants.Type01.INDICAT3_O_RESERVED1_VALUE))
				value = TSLConstants.Type01.INDICAT3_O_RESERVED1_NAME;
			else if (entryMethodCodeValue.equals(TSLConstants.Type01.INDICAT3_O_ITEM_CREATED_BY_SERVICE_VALUE))
				value = TSLConstants.Type01.INDICAT3_O_ITEM_CREATED_BY_SERVICE_NAME;
			else if (entryMethodCodeValue.equals(TSLConstants.Type01.INDICAT3_O_RESERVED2_VALUE))
				value = TSLConstants.Type01.INDICAT3_O_RESERVED2_NAME;
			else if (entryMethodCodeValue.equals(TSLConstants.Type01.INDICAT3_O_REEDEMPTION_OF_POINTS_VALUE))
				value = TSLConstants.Type01.INDICAT3_O_REEDEMPTION_POINTS_NAME;
			else if (entryMethodCodeValue.equals(TSLConstants.Type01.INDICAT3_O_BONUS_POINTS_VALUE))
				value = TSLConstants.Type01.INDICAT3_O_BONUS_POINTS_NAME;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return value;
	}

	public  Object getTypeOfTransaction(Transaction transaction) {
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

	private  AssociatedCoupon getAssociatedCoupon(RetailTransaction rt, String couponId) {
		if (rt.getAssociatedCouponsToRedemptions() != null) {
			int size = rt.getAssociatedCouponsToRedemptions().size();
			for (int i = 0; i < size; i++) {
				if (couponId.equalsIgnoreCase(((AssociatedCoupon) rt.getAssociatedCouponsToRedemptions().get(i)).getCoupon()
						.getCouponCode()))
					return (AssociatedCoupon) rt.getAssociatedCouponsToRedemptions().get(i);
			}
		}
		return null;
	}

	/**
	 * Get the elapsed minutes since Hour 00 minutes 00 until the hour and minutes of the transaction
	 *
	 * @param date
	 *            transaction date
	 * @return the elapsed minutes
	 */
	public  Integer getNumberOfElapsedMinutes(Date date) {
		Integer numberOfMinutes = new Integer(0);
		try {
			Date dateTrunc = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
			Calendar calFechaInicial = Calendar.getInstance();
			calFechaInicial.setTime(dateTrunc);

			Calendar calFechaFinal = Calendar.getInstance();
			calFechaFinal.setTime(date);
			numberOfMinutes = (((int) (calFechaFinal.getTimeInMillis() - calFechaInicial.getTimeInMillis())) / 1000 / 60);
		} catch (Exception e) {
			numberOfMinutes = 0;
		}
		return numberOfMinutes;
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
	public  Boolean isBitOn(long value, int position) {
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
