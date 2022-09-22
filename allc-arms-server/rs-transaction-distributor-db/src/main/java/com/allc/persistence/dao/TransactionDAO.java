/**
 * 
 */
package com.allc.persistence.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.type.StandardBasicTypes;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.moto.Moto;
import com.allc.entities.AssociatedCoupon;
import com.allc.entities.CedRuc;
import com.allc.entities.CouponToRedemption;
import com.allc.entities.Currency;
import com.allc.entities.ExternalDepository;
import com.allc.entities.Extranjero;
import com.allc.entities.InvoiceAssociated;
import com.allc.entities.Item;
import com.allc.entities.ItemReservaInfo;
import com.allc.entities.ItemStore;
import com.allc.entities.MerchandiseHierarchyGroup;
import com.allc.entities.MotoData;
import com.allc.entities.Operator;
import com.allc.entities.POSDepartment;
import com.allc.entities.RedeemedCouponData;
import com.allc.entities.RetailStore;
import com.allc.entities.RetailTransactionLineItem;
import com.allc.entities.RetailTransactionTotal;
import com.allc.entities.RetencionData;
import com.allc.entities.ReverseLineItemInfo;
import com.allc.entities.Safe;
import com.allc.entities.SaleReturnLineItem;
import com.allc.entities.StringUsuario;
import com.allc.entities.Tender;
import com.allc.entities.TenderControlTransactionLineItem;
import com.allc.entities.Till;
import com.allc.entities.TransactionTotalType;
import com.allc.entities.Workstation;
import com.allc.persistence.util.ClasificacionAlertas;
import com.allc.persistence.util.Files;
import com.allc.persistence.util.HibernateUtil;
import com.allc.persistence.util.SqlPostgres;
import com.allc.persistence.util.TDprop;
import com.allc.persistence.ws.devsu.AlertTecRequest;
import com.allc.persistence.ws.devsu.AutorizacionRequest;
import com.allc.persistence.ws.devsu.AutorizacionRequestIp;
import com.allc.persistence.ws.devsu.ResponseAutorizacionService;
import com.allc.util.ConstantsUtil;
import com.google.gson.Gson;

/**
 * @author GUSTAVOK
 *
 */
public class TransactionDAO {
	private Session sesion;
	protected Session sessionEyes;
	protected Session sessionDevs;
	public Transaction tx;
	private Transaction txEyes;
	private TDprop properties;
	private static Logger log = Logger.getLogger(TransactionDAO.class);

	public TransactionDAO() {
		super();
		properties = TDprop.getInstance();
		iniciaOperacion();
	}

	public boolean saveTransaction(com.allc.entities.Transaction transaction) throws Throwable {
		try {
			iniciaOperacion();
                        if (!validateDuplicatedTrx(transaction)) {
                                if (transaction.getRetailStore() == null) {
                                        transaction.setRetailStore(getRetailStoreByCode(transaction.getRetailStoreCode()));
                                }
                                if (transaction.getWorkstation() == null && transaction.getWorkstationCode() != null) {
                                        transaction.setWorkstation(
                                                        getWorkstationByCode(transaction.getWorkstationCode(), transaction.getRetailStore()));
                                }
                                if (transaction.getOperator() == null && transaction.getOperatorCode() != null) {
                                        transaction.setOperator(getOperatorByCode(transaction.getOperatorCode()));
                                }
                                if (transaction.getRetailTransaction() != null) {
                                        if (transaction.getRetailTransaction().getLineItems() != null
                                                        && !transaction.getRetailTransaction().getLineItems().isEmpty()) {
                                                Iterator itLineItems = transaction.getRetailTransaction().getLineItems().iterator();
                                                List items = new ArrayList();
                                                List tenders = new ArrayList();
                                                List currencys = new ArrayList();
                                                List posDeparments = new ArrayList();
                                                List merchHierarchyGroups = new ArrayList();
                                                while (itLineItems.hasNext()) {
                                                        RetailTransactionLineItem lineItem = (RetailTransactionLineItem) itLineItems.next();
                                                        if (lineItem.getSaleLI() != null && lineItem.getSaleLI().getItem() == null) {
                                                                Item item = getItemByCode(lineItem.getSaleLI().getItemCode(), items);
                                                                if(item.getItemType() == null)
                                                                        item.setItemType(lineItem.getSaleLI().getItemType());
                                                                lineItem.getSaleLI().setItem(item);
                                                                if(item.getItemID() != null)
                                                                        updateStock(item.getItemID(), transaction.getRetailStore().getRetailStoreID(), lineItem.getSaleLI().getQuantity().intValue());
                                                                // if(lineItem.getSaleLI().getUnits()!=null &&
                                                                // lineItem.getSaleLI().getUnits().compareTo(new
                                                                // Double(0))>0)
                                                                // lineItem.getSaleLI().getItem().setFlWorM(1);
                                                                // POSDepartment posDepartment =
                                                                // getPOSDepartmentByCode(lineItem.getSaleLI().getPosDepartmentCode(),
                                                                // posDeparments);
                                                                // lineItem.getSaleLI().getItem().setPosDepartment(posDepartment);
                                                                // MerchandiseHierarchyGroup merchHierarGp =
                                                                // getMerchandiseHierarchyGroupByCode(lineItem.getSaleLI().getMerchandiseHierarchyGroupCode(),
                                                                // posDepartment, merchHierarchyGroups);
                                                                // lineItem.getSaleLI().getItem().setMerchandiseHierarchyGroup(merchHierarGp);
                                                                loadTaxes(lineItem.getSaleLI(), transaction.getRetailStore(),
                                                                                transaction.getRetailTransaction().isIncludeTax());
                                                        } else if (lineItem.getReturnLI() != null && lineItem.getReturnLI().getItem() == null) {
                                                                Item item = getItemByCode(lineItem.getReturnLI().getItemCode(), items);
                                                                if(item.getItemType() == null)
                                                                        item.setItemType(lineItem.getReturnLI().getItemType());
                                                                lineItem.getReturnLI().setItem(item);
                                                                // if(lineItem.getReturnLI().getUnits()!=null &&
                                                                // lineItem.getReturnLI().getUnits().compareTo(new
                                                                // Double(0))>0)
                                                                // lineItem.getReturnLI().getItem().setFlWorM(1);
                                                                // POSDepartment posDepartment =
                                                                // getPOSDepartmentByCode(lineItem.getReturnLI().getPosDepartmentCode(),
                                                                // posDeparments);
                                                                // lineItem.getReturnLI().getItem().setPosDepartment(posDepartment);
                                                                // MerchandiseHierarchyGroup merchHierarGp =
                                                                // getMerchandiseHierarchyGroupByCode(lineItem.getReturnLI().getMerchandiseHierarchyGroupCode(),
                                                                // posDepartment, merchHierarchyGroups);
                                                                // lineItem.getReturnLI().getItem().setMerchandiseHierarchyGroup(merchHierarGp);
                                                                loadTaxes(lineItem.getReturnLI(), transaction.getRetailStore(),
                                                                                transaction.getRetailTransaction().isIncludeTax());
                                                        } else if (lineItem.getTender() != null) {
                                                                if (lineItem.getTender().getTender() == null
                                                                                && lineItem.getTender().getTenderTypeCode() != null) {
                                                                        Tender tender = getTenderByCode(lineItem.getTender().getTenderTypeCode(), tenders);
                                                                        lineItem.getTender().setTender(tender);
                                                                }
                                                                if (lineItem.getTender().getForeignCurrency() == null
                                                                                && lineItem.getTender().getForeignCurrencyID() != null) {
                                                                        Currency currency = getCurrencyByID(lineItem.getTender().getForeignCurrencyID(),
                                                                                        currencys);
                                                                        lineItem.getTender().setForeignCurrency(currency);
                                                                        ;
                                                                }
                                                        }
                                                }
                                        }
                                        if (transaction.getRetailTransaction().getTotalItems() != null
                                                        && !transaction.getRetailTransaction().getTotalItems().isEmpty()) {
                                                Iterator itTotalItems = transaction.getRetailTransaction().getTotalItems().iterator();
                                                while (itTotalItems.hasNext()) {
                                                        RetailTransactionTotal totalItem = (RetailTransactionTotal) itTotalItems.next();
                                                        if (totalItem.getTransactionTotalType() == null
                                                                        && totalItem.getTransactionTotalTypeCode() != null) {
                                                                TransactionTotalType totalType = getTransactionTotalTypeByCode(
                                                                                totalItem.getTransactionTotalTypeCode());
                                                                totalItem.setTransactionTotalType(totalType);
                                                        }

                                                }
                                        }
                                        if (transaction.getRetailTransaction().getRedeemedCouponsDataList() != null
                                                        && !transaction.getRetailTransaction().getRedeemedCouponsDataList().isEmpty()) {
                                                Iterator itRedCouponsData = transaction.getRetailTransaction().getRedeemedCouponsDataList()
                                                                .iterator();
                                                while (itRedCouponsData.hasNext()) {
                                                        RedeemedCouponData redCouponData = (RedeemedCouponData) itRedCouponsData.next();
                                                        setCouponRedeemed(redCouponData.getBarcode());
                                                }
                                        }
                                        if (transaction.getRetailTransaction().getFacturaElec() != null) {
                                                InvoiceAssociated invoice = new InvoiceAssociated();
                                                if (transaction.getRetailTransaction().getInvoiceData() != null) {
//							if ("1".equals(transaction.getRetailTransaction().getInvoiceData().getCustomerType())) {
//								// si el tipo es 1 es extranjero
//								Extranjero extranjero = getExtranjeroByID(
//										transaction.getRetailTransaction().getInvoiceData().getCustomerID());
//								if (extranjero == null)
//									extranjero = new Extranjero();
//								extranjero
//										.setCodigo(transaction.getRetailTransaction().getInvoiceData().getCustomerID());
//								extranjero.setNombre(
//										transaction.getRetailTransaction().getInvoiceData().getCustomerName());
//								extranjero
//										.setDireccion(transaction.getRetailTransaction().getInvoiceData().getAddress());
//								extranjero.setTelefono(
//										transaction.getRetailTransaction().getInvoiceData().getTelephone());
//								sesion.saveOrUpdate(extranjero);
//								invoice.setExtranjero(extranjero);
//								invoice.setCustomerType(1);
//								invoice.setNameCustomer(
//										transaction.getRetailTransaction().getInvoiceData().getCustomerName());
//								invoice.setIdentificacionCustomer(
//										transaction.getRetailTransaction().getInvoiceData().getCustomerID());
//							} else {
//								// si el tipo es 2 = ruc y 3 = cedula
//								CedRuc cedRuc = getCedRucByID(
//										transaction.getRetailTransaction().getInvoiceData().getCustomerID());
//								if (cedRuc == null) {
//									cedRuc = new CedRuc();
//									cedRuc.setId(Long.valueOf(
//											transaction.getRetailTransaction().getInvoiceData().getCustomerID()));
//									cedRuc.setCodigo(
//											transaction.getRetailTransaction().getInvoiceData().getCustomerID());
//									cedRuc.setNombre(
//											transaction.getRetailTransaction().getInvoiceData().getCustomerName());
//									cedRuc.setDireccion(
//											transaction.getRetailTransaction().getInvoiceData().getAddress());
//									cedRuc.setTelefono(
//											transaction.getRetailTransaction().getInvoiceData().getTelephone());
//									cedRuc.setRegInter(1);
//									if ("2".equals(
//											transaction.getRetailTransaction().getInvoiceData().getCustomerType()))
//										cedRuc.setTipo("R");
//									if ("3".equals(
//											transaction.getRetailTransaction().getInvoiceData().getCustomerType()))
//										cedRuc.setTipo("C");
//									sesion.saveOrUpdate(cedRuc);
//								}
//								invoice.setCedRuc(cedRuc);
                                                                invoice.setCustomerType(0);
                                                                //log.info("cod_sri:"+Integer.valueOf(transaction.getRetailTransaction().getInvoiceData().getCustomerType()));
                                                                //invoice.setCodigoSRI(Integer.valueOf(transaction.getRetailTransaction().getInvoiceData().getCustomerType()));								
                                                                invoice.setNameCustomer(
                                                                                transaction.getRetailTransaction().getInvoiceData().getCustomerName());
                                                                invoice.setIdentificacionCustomer(
                                                                                transaction.getRetailTransaction().getInvoiceData().getCustomerID());
//							}
                                                } else {
                                                        if (transaction.getRetailTransaction().getAssociatedData() != null) {
                                                                CedRuc cedRuc = getCedRucByID(
                                                                                transaction.getRetailTransaction().getAssociatedData().getCustomerID());
                                                                if (cedRuc != null) {
                                                                        invoice.setCedRuc(cedRuc);
                                                                }
                                                                invoice.setCustomerType(0);
                                                                invoice.setNameCustomer(
                                                                                transaction.getRetailTransaction().getAssociatedData().getNameCustomer());
                                                                invoice.setIdentificacionCustomer(
                                                                                transaction.getRetailTransaction().getAssociatedData().getCustomerID());
                                                        }
                                                }
                                                if (transaction.getRetailTransaction().getFacturaElec() != null) {
                                                        invoice.setInvoiceNumber(transaction.getRetailTransaction().getFacturaElec().getNumeroFac());
                                                        invoice.setSubTotal(transaction.getRetailTransaction().getFacturaElec().getSubTotal());
                                                        invoice.setTotal(transaction.getRetailTransaction().getFacturaElec().getTotal());
                                                        invoice.setTax(transaction.getRetailStore().getTax1());
                                                        invoice.setAuthorizationNumber(transaction.getRetailTransaction().getFacturaElec().getAuthorizationNumber());
                                                        invoice.setAmbiente(transaction.getRetailTransaction().getFacturaElec().getAmbiente());
                                                        invoice.setEmision(transaction.getRetailTransaction().getFacturaElec().getEmision());
                                                }
                                                transaction.getRetailTransaction().setInvoiceAssoc(invoice);
                                        } 
                                        if (transaction.getRetailTransaction().getStringsUsuario() !=null) { 
                                                Iterator isu = transaction.getRetailTransaction().getStringsUsuario().iterator();
                                                int aux = 0;
                                                while (isu.hasNext()) { 
                                                        StringUsuario us = (StringUsuario) isu.next();
                                                        String cadenatemoString = us.getCadena();
                                                        String newcadenaString  = "";
                                                        for (int i = 0; i< cadenatemoString.length();i++){
                                                    char b=cadenatemoString.charAt(i);
                                                    int ascii = (int) b;
                                                    if (32 <= ascii && ascii <= 254){
                                                         newcadenaString +=cadenatemoString.charAt(i);
                                                    }else{
                                                        newcadenaString += String.valueOf(ascii);
                                                    }
                                               }
                                                        us.setCadena(newcadenaString);
                                                        aux++; 
                                                } 
                                        }
                                        if (transaction.getRetailTransaction().getAssociatedCouponsToRedemptions() != null
                                                        && !transaction.getRetailTransaction().getAssociatedCouponsToRedemptions().isEmpty()) {
                                                int count = transaction.getRetailTransaction().getAssociatedCouponsToRedemptions().size();
                                                for (int i = 0; i < count; i++) {
                                                        AssociatedCoupon assocCoupon = (AssociatedCoupon) transaction.getRetailTransaction()
                                                                        .getAssociatedCouponsToRedemptions().get(i);
                                                        CouponToRedemption coupon = getCouponToRedemptionByID(
                                                                        assocCoupon.getCoupon().getCouponCode());
                                                        if (coupon == null) {
                                                                coupon = new CouponToRedemption();
                                                                coupon.setCouponCode(assocCoupon.getCoupon().getCouponCode());
                                                        }
                                                        coupon.setTimeStamp(assocCoupon.getCoupon().getTimeStamp());
                                                        coupon.setStatus(assocCoupon.getCoupon().getStatus());
                                                        coupon.setFechaInicial(assocCoupon.getCoupon().getFechaInicial());
                                                        coupon.setFechaExpiracion(assocCoupon.getCoupon().getFechaExpiracion());
                                                        coupon.setMaxRedemptions(assocCoupon.getCoupon().getMaxRedemptions());
                                                        coupon.setnRedemptions(assocCoupon.getCoupon().getnRedemptions());
                                                        if (assocCoupon.getCoupon().getLastRedemption() != null
                                                                        && !assocCoupon.getCoupon().getLastRedemption().trim().startsWith("-"))
                                                                coupon.setLastRedemption(assocCoupon.getCoupon().getLastRedemption());
                                                        coupon.setPercentOff(assocCoupon.getCoupon().getPercentOff());
                                                        sesion.saveOrUpdate(coupon);
                                                        assocCoupon.setCoupon(coupon);
                                                }
                                        }
                                        if (transaction.getRetailTransaction().getRetencionData() != null
                                                        && transaction.getRetailTransaction().getFacturaElec() != null) {
                                                RetencionData retenData = new RetencionData();
                                                String numFact = transaction.getRetailTransaction().getFacturaElec().getNumeroFac();
                                                String numFactFinal = null;
                                                Pattern p = Pattern.compile("[-]");
                                                Matcher m = p.matcher(numFact);
                                                if (m.find())
                                                        numFactFinal = m.replaceAll("");

                                                int countRec = transaction.getRetailTransaction().getRetencionData().size();

                                                for (int i = 0; i < countRec; i++) {
                                                        retenData = (RetencionData) transaction.getRetailTransaction().getRetencionData().get(i);
                                                        retenData.setTiquete(numFactFinal);
                                                        // sesion.saveOrUpdate(retenData);
                                                }

                                                Integer terminal = Integer.valueOf(transaction.getWorkstationCode());
                                                Integer storeCode = transaction.getRetailStore().getCode();
                                                try {
                                                        iniciarSesionEyes();
                                                        setInvoiceNumberToTerminalStatus(storeCode, terminal, numFactFinal);
                                                        txEyes.commit();
                                                } catch (Exception he) {
                                                        txEyes.rollback();
                                                        log.error(he.getMessage(), he);
                                                } finally {
                                                        sessionEyes.close();
                                                        sessionEyes = null;
                                                }
                                        }
//					if (transaction.getRetailTransaction().getNotaCredito() != null) {
//						setPagosCerStatus(transaction.getRetailTransaction().getLineItems(),
//								transaction.getRetailTransaction().getNotaCredito().getNumeroNotaCredito());
//						
//						int taxFacOri = getTaxFacturaOriginal(transaction.getRetailTransaction().getNotaCredito().getNumeroFac());
//						transaction.getRetailTransaction().getNotaCredito().setTax(taxFacOri);
//						Integer idTrxOri = getIdTrxOri(transaction.getRetailTransaction().getNotaCredito().getNumeroFac());
//						if(idTrxOri == null)
//							idTrxOri = getIdTicketOri(Integer.valueOf(transaction.getRetailTransaction().getNotaCredito().getNumeroDocOriginal()), transaction.getRetailTransaction().getNotaCredito().getFechaContOrig(), transaction.getRetailTransaction().getNotaCredito().getPosOrig(), transaction.getRetailTransaction().getNotaCredito().getNroTiendaOriginal());
//						setIlimitadaReverse(idTrxOri);
//						
//						if(existePagoTarjBanc(transaction.getRetailTransaction().getLineItems()))
//							setAllPagoPinpadReverse(idTrxOri);
//					}
                                        if (transaction.getRetailTransaction().getItemRsvInfo() != null) {
                                                ItemReservaInfo reserva = transaction.getRetailTransaction().getItemRsvInfo();
                                                String numRsv = reserva.getNumReserva();
                                                String numSerie = reserva.getNumSerie();
                                                setReservaUtilizada(numRsv, numSerie);

                                        }
                                        if (transaction.getRetailTransaction().getNotaCredito() != null) {
//						RetailTransactionLineItem lineItem = (RetailTransactionLineItem) transaction
//								.getRetailTransaction().getLineItems().get(0);
//						RetailTransactionLineItem lineItemNext = (RetailTransactionLineItem) transaction
//								.getRetailTransaction().getLineItems().get(1);
//						if(lineItemNext.getReturnLI() == null){
//							SaleReturnLineItem returnItem = lineItem.getReturnLI();
//							Item item = returnItem.getItem();
//							Moto moto = getMoto(item.getItemID());
//							if (moto != null) {
//								Integer idTrxOri = getIdTrxOri(
//									transaction.getRetailTransaction().getNotaCredito().getNumeroFac());
//								setReturnMoto(idTrxOri);
//							}
//						}
                                                if (transaction.getRetailTransaction().getMotosData() != null) {
                                                        MotoData motoData = (MotoData) transaction.getRetailTransaction().getMotosData().get(0);
                                                        log.info("Se devuelve MOTO: " + motoData.getSerialNumber().trim());
                                                        setReturnMoto(motoData.getSerialNumber().trim());
                                                }
                                        } 
                                } else if (transaction.getTenderControlTransaction() != null) {
                                        Iterator itLineItems = null;
                                        if (transaction.getTenderControlTransaction().getLineItems() != null
                                                        && !transaction.getTenderControlTransaction().getLineItems().isEmpty()) {
                                                itLineItems = transaction.getTenderControlTransaction().getLineItems().iterator();
                                        }
                                        if (transaction.getTenderControlTransaction().getDeposit() != null) {
                                                transaction.getTenderControlTransaction().getDeposit().setSafe(
                                                                getSafeByID(transaction.getTenderControlTransaction().getDeposit().getSafeID()));
                                                transaction.getTenderControlTransaction().getDeposit()
                                                                .setExternalDepository(getExternalDepositoryByID(transaction
                                                                                .getTenderControlTransaction().getDeposit().getExternalDepositoryID()));
                                        } else if (transaction.getTenderControlTransaction().getReceipt() != null) {
                                                transaction.getTenderControlTransaction().getReceipt().setSafe(
                                                                getSafeByID(transaction.getTenderControlTransaction().getReceipt().getSafeID()));
                                                transaction.getTenderControlTransaction().getReceipt()
                                                                .setExternalDepository(getExternalDepositoryByID(transaction
                                                                                .getTenderControlTransaction().getReceipt().getExternalDepositoryID()));
                                        }
                                        if (itLineItems != null) {
                                                List currencys = new ArrayList();
                                                List tenders = new ArrayList();
                                                while (itLineItems.hasNext()) {
                                                        TenderControlTransactionLineItem lineItem = (TenderControlTransactionLineItem) itLineItems
                                                                        .next();
                                                        if (lineItem.getTender() == null && lineItem.getTenderTypeCode() != null) {
                                                                Tender tender = getTenderByCode(lineItem.getTenderTypeCode(), tenders);
                                                                lineItem.setTender(tender);
                                                        }
                                                        if (lineItem.getCurrency() == null && lineItem.getCurrencyID() != null) {
                                                                Currency currency = getCurrencyByID(lineItem.getCurrencyID(), currencys);
                                                                lineItem.setCurrency(currency);
                                                        }
                                                }
                                        }

                                        if (transaction.getTenderControlTransaction().getRvItemInfo() != null) {
                                                ReverseLineItemInfo rvItem = transaction.getTenderControlTransaction().getRvItemInfo();
                                                List<String> dataTrxNC = getInfoTrxNc(rvItem.getNumNc());
                                                String facOri = dataTrxNC.get(1);
                                                Integer idTrxOri = getIdTrxOri(facOri);
                                                setPagoPinpadReverse(idTrxOri, rvItem.getNumAuto());
                                                Integer idTrxNc = Integer.valueOf(dataTrxNC.get(0));
                                                setRtnItemStatus(idTrxNc, rvItem.getLineItem());
                                        }
                                } 

                                else if (transaction.getControlTransaction() != null) {
                                        if (transaction.getControlTransaction().getCarryForward() != null) {
                                                Integer idTrn = getLastClose(transaction);
                                                log.info("Finalizo cierre id_trn: " + idTrn);
                                                if (idTrn != null) {
                                                        transaction.getControlTransaction().getCarryForward().setIdBusinessEOD(idTrn);
                                                        setCloseFinished(idTrn);
                                                }
                                        } else if (transaction.getControlTransaction().getBusinessEOD() != null) {
                                                for(int i = 0; i < 30; i++){
                                                        com.allc.entities.Transaction trxAux = new com.allc.entities.Transaction();
                                                        trxAux.setBusinessDayDate(transaction.getBusinessDayDate());
                                                        trxAux.setSequenceNumber(0);
                                                        trxAux.setBeginDateTime(transaction.getBeginDateTime());
                                                        trxAux.setCancelFlag(Boolean.TRUE);
                                                        trxAux.setEndDateTime(transaction.getEndDateTime());
                                                        trxAux.setRetailStore(transaction.getRetailStore());
                                                        trxAux.setTransactionTypeCode(2);
                                                        trxAux.setWorkstation(transaction.getWorkstation());
                                                        sesion.save(trxAux);
                                                }
//						try {
//							String dateString = transaction.getBeginDateTimeString().substring(0, 10);
//							String timeString = transaction.getBeginDateTimeString().substring(11, 19);						
//							insertaTposCierre(transaction.getRetailStoreCode(), dateString, timeString);
//						} catch (Exception exc){
//							log.error(exc.getMessage(), exc);
//						}
                                        } 

                                }
                                sesion.save(transaction);
                                log.info("id_trn generado --> "+transaction.getTransactionID() + " | tienda --> "+transaction.getRetailStore().getCode());

                                if (properties.getObject("proceso.alertas.supervisor.habilitado").toString().equals("1"))
                                {
                                        alertaItmsScaneadosDemoradosTrx(transaction);
                                }

                        } else
                                return false;
		} catch (Exception he) {
                        log.error(he.getMessage(), he);
			tx.rollback();
			throw he;
		}
		return true;
	}
	
	private void updateMonitor(String idLocal, String idControlador, Integer idTransaction) {
		try {
			log.info("Guarda en monitor id_trn " + idTransaction + " - id local: " + idLocal + " - id controlador " + idControlador);
			//Paso los codigos a enteros para que le saque los 0 de adelante
			int idLocalInt = Integer.parseInt(idLocal);
			int idControladorInt = Integer.parseInt(idControlador);
			Statement statement = sesion.connection().createStatement();
			int countRows = statement.executeUpdate("UPDATE eyes_ec.monitor	SET id_trn=" + idTransaction + ", fec_act_id_trn=CURRENT_TIMESTAMP WHERE id_local=" + idLocalInt + " and des_clave='" + idControladorInt + "';");
			log.info("Guardado monitor " + countRows);	
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

        public void commitTransaction() throws Exception{
                try{
                    tx.commit();
                    log.info("--- Bloque de XML Insertados a la BD ---");
                } catch (Exception e) {
                        log.error(e.getMessage(), e);
			tx.rollback();
                        throw e;
		} finally {
                        sesion.close();
                        sesion = null;
                        tx = null;
                }
	}
                
	private void loadTaxes(SaleReturnLineItem lineItem, RetailStore store, boolean includeTax) {
		if (lineItem.getAppliedTax() == null || lineItem.getAppliedTax().compareTo(new Double(0)) == 0
				|| "T".equalsIgnoreCase(properties.getCalculateTaxFlag())) {
			ItemStore itemStore = getItemStore(lineItem.getItem().getItemID(), store.getRetailStoreID());
			if (itemStore != null) {
				if (includeTax) {
					double extendedAm = lineItem.getExtendedAmount().doubleValue();
					double tax = store.getTax1().doubleValue() / 100;
					double taxAmount = itemStore.getTaxA().doubleValue();
					lineItem.setAppliedTax(
							formatTaxAmount((new Double(((extendedAm * tax) / (100 + tax)) * taxAmount)).toString()));
				} else {
					double extendedAm = lineItem.getExtendedAmount().doubleValue() / 100;
					double tax = store.getTax1().doubleValue() / 100;
					double taxAmount = itemStore.getTaxA().doubleValue();
					lineItem.setAppliedTax(
							formatTaxAmount((new Double(((extendedAm * tax) / 100) * taxAmount)).toString()));
				}
			}
		}
	}

	private Double formatTaxAmount(String taxAmount) {
		String[] parts = taxAmount.split("\\.");
		String decimal = "0000";
		if (parts.length > 1) {
			decimal = parts[1];
			while (decimal.length() < 4)
				decimal = decimal + "0";
			if (decimal.length() > 4)
				decimal = decimal.substring(0, 4);
		}
		return new Double(parts[0] + decimal);
	}

	private ItemStore getItemStore(Integer itemId, Integer retailStoreId) {
		if (itemId != null && retailStoreId != null) {
			Query query = sesion.createQuery("from com.allc.entities.ItemStore where itemID = '" + itemId
					+ "' and retailStoreID = '" + retailStoreId + "'");
			List list = query.list();
			if (list != null && !list.isEmpty())
				return (ItemStore) list.get(0);
		}
		return null;
	}

	private void updateStock(Integer itemId, Integer retailStoreId, Integer qty) throws SQLException {
		Statement statement = sesion.connection().createStatement();
		int countRows = statement.executeUpdate("UPDATE AS_ITM_STR SET STOCK = STOCK - " + qty + " WHERE STOCK > 0 AND ID_ITM = " + itemId + " AND ID_BSN_UN = " + retailStoreId);
	}

	/**
	 * Retorna true si la transacción ya está registrada en BD.
	 * 
	 * @param transaction
	 * @return
	 */
	private boolean validateDuplicatedTrx(com.allc.entities.Transaction transaction) throws Exception {
            Query query = null;
		try {
			String fecha = null;
			if (transaction.getControlTransaction() != null
					&& transaction.getControlTransaction().getBusinessEOD() != null) {
				if ("oracle".equalsIgnoreCase(HibernateUtil.getMotor()) || "postgresql".equalsIgnoreCase(HibernateUtil.getMotor())) {
					fecha = ConstantsUtil.Formatters.DDMMYYYY_SUFIX_DATE_TIME_FORMATTER
							.format(transaction.getBusinessDayDate());
					query = sesion.createSQLQuery(
							"SELECT COUNT(TRN.ID_TRN) FROM TR_TRN TRN,PA_STR_RTL STR, AS_WS WS WHERE TRN.ID_BSN_UN = STR.ID_BSN_UN AND TRN.ID_WS = WS.ID_WS AND WS.CD_WS = '"
									+ transaction.getWorkstationCode() + "' AND STR.CD_STR_RT = "
									+ new Integer(transaction.getRetailStoreCode()) + " AND TRN.DC_DY_BSN = TO_TIMESTAMP('"
									+ fecha + "','YYYYMMDDHH24MISS') AND TRN.AI_TRN = "
									+ transaction.getSequenceNumber()
									+ " AND TRN.ID_TRN NOT IN (SELECT ID_TRN FROM TR_SGN_ON)");
				} else if ("sqlserver".equalsIgnoreCase(HibernateUtil.getMotor())) {
					fecha = ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(transaction.getEndDateTime());
					// fecha =
					// ConstantsUtil.Formatters.DDMMYYYY_SUFIX_DATE_TIME_FORMATTER
					// .format(transaction.getBusinessDayDate());
					// fecha = fecha.substring(0, 2) + "-" + fecha.substring(2,
					// 4) + "-" + fecha.substring(4);
					log.info("BusinessDate:" + fecha);
					query = sesion.createSQLQuery(
							"SELECT COUNT(TRN.ID_TRN) FROM TR_TRN TRN,PA_STR_RTL STR, AS_WS WS WHERE TRN.ID_BSN_UN = STR.ID_BSN_UN AND TRN.ID_WS = WS.ID_WS AND WS.CD_WS = '"
									+ transaction.getWorkstationCode() + "' AND STR.CD_STR_RT = "
									+ new Integer(transaction.getRetailStoreCode())
									+ " AND TRN.TS_TRN_BGN = convert(datetime,'" + fecha + "',126) AND TRN.AI_TRN = "
									+ transaction.getSequenceNumber()
									+ " AND TRN.ID_TRN NOT IN (SELECT ID_TRN FROM TR_SGN_ON)");
				}
			}
			if (transaction.getTenderControlTransaction() != null
					&& transaction.getSequenceNumber().compareTo(0) == 0) {
				// si es una transaccion de Retiro o Dotacion desde el
				// Controlador se genera con seqNum=0
				// y puede haber mas de una con la misma fecha, por lo que no
				// debemos validar duplicidad
				return false;
			} else if (transaction.getControlTransaction() != null
					&& transaction.getControlTransaction().getCarryForward() != null
					&& transaction.getSequenceNumber().compareTo(0) == 0) {
				// si es una transaccion de Suma y Sigue desde el
				// Controlador se genera con seqNum=0
				// y puede haber otra con la misma fecha, por lo que no
				// debemos validar duplicidad
				return false;
			} else {
				if ("oracle".equalsIgnoreCase(HibernateUtil.getMotor()) || "postgresql".equalsIgnoreCase(HibernateUtil.getMotor())) {
					fecha = ConstantsUtil.Formatters.TLOG_SUFIX_DATE_TIME_FORMATTER
							.format(transaction.getBeginDateTime());
					query = sesion.createSQLQuery(
							"SELECT COUNT(TRN.ID_TRN) FROM TR_TRN TRN,PA_STR_RTL STR, AS_WS WS WHERE TRN.ID_BSN_UN = STR.ID_BSN_UN AND TRN.ID_WS = WS.ID_WS AND WS.CD_WS = '"
									+ transaction.getWorkstationCode() + "' AND STR.CD_STR_RT = "
									+ new Integer(transaction.getRetailStoreCode()) + " AND TRN.TS_TRN_BGN = TO_TIMESTAMP('"
									+ fecha + "','YYYYMMDDHH24MISS') AND TRN.AI_TRN = "
									+ transaction.getSequenceNumber()
									+ " AND TRN.ID_TRN NOT IN (SELECT ID_TRN FROM TR_SGN_ON)");
					// log.info("Query for validation: " + query);
				} else if ("sqlserver".equalsIgnoreCase(HibernateUtil.getMotor())) {
					fecha = ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER.format(transaction.getEndDateTime());
					query = sesion.createSQLQuery(
							"SELECT COUNT(TRN.ID_TRN) FROM TR_TRN TRN,PA_STR_RTL STR, AS_WS WS WHERE TRN.ID_BSN_UN = STR.ID_BSN_UN AND TRN.ID_WS = WS.ID_WS AND WS.CD_WS = '"
									+ transaction.getWorkstationCode() + "' AND STR.CD_STR_RT = "
									+ new Integer(transaction.getRetailStoreCode())
									+ " AND TRN.TS_TRN_END = convert(datetime,'" + fecha + "',126) AND TRN.AI_TRN = "
									+ transaction.getSequenceNumber()
									+ " AND TRN.ID_TRN NOT IN (SELECT ID_TRN FROM TR_SGN_ON)");
				}
			}
			List rows = query.list();
			if (rows != null && !rows.isEmpty() && Integer.valueOf(rows.get(0).toString()).intValue() > 0) {
				log.info("Transacción duplicada.");
				return true;
			}
		    } catch (Exception e) {
			log.error(e.getMessage(), e);
                        throw e;
		    }
            return false;
	}

	private Integer getLastClose(com.allc.entities.Transaction transaction) {
		Query query = null;
		try {
			query = sesion.createSQLQuery(
					"select max(b.id_trn) from tr_bsn_eod b, tr_trn t where b.id_trn=t.id_trn and t.id_bsn_un="
							+ transaction.getRetailStore().getRetailStoreID());
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return Integer.valueOf(rows.get(0).toString());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	private RetailStore getRetailStoreByCode(String retailStoreCode) {
		Integer code = new Integer(retailStoreCode);
		Query query = sesion.createQuery("from com.allc.entities.RetailStore where code = '" + code + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (RetailStore) list.get(0);
		RetailStore retailStore = new RetailStore();
		retailStore.setCode(code);
		return retailStore;
	}

	private Workstation getWorkstationByCode(String workstationCode, RetailStore retailStore) {
		Query query = sesion.createQuery("from com.allc.entities.Workstation where code = '" + workstationCode
				+ "'  and store.code = '" + retailStore.getCode() + "' ");
		List list = query.list();
		Workstation workstation = null;
		if (list != null && !list.isEmpty()) {
			workstation = (Workstation) list.get(0);
			if(workstation.getType() == null || workstation.getType() == 0) {
				workstation.setType(7);
			}
		}else{
			workstation = new Workstation();
			workstation.setCode(workstationCode);
			workstation.setStore(retailStore);
			workstation.setType(7);
		}
		return workstation;
	}

	private Operator getOperatorByCode(String operatorCode) {
		Query query = sesion.createQuery("from com.allc.entities.Operator where code = '" + operatorCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (Operator) list.get(0);
		Operator operator = new Operator();
		operator.setCode(operatorCode);
		return operator;
	}

	private Item getItemByCode(Long itemCode, List items) {
		Iterator itItems = items.iterator();
		while (itItems.hasNext()) {
			Item item = (Item) itItems.next();
			if (item.getItemCode().compareTo(itemCode) == 0)
				return item;
		}
		String itemCodeS =  ("0000000000"+itemCode.toString());
		itemCodeS =  itemCodeS.substring(itemCodeS.length()-12); //ultimos 12 caracteres.
		Query query = sesion.createQuery("from com.allc.entities.Item where itemCode = '"+ itemCode+"' or itemCode =  '" + itemCodeS + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (Item) list.get(0);
		Item item = new Item();
		item.setItemCode(itemCode);
		items.add(item);
		//writeItemInFileEx(itemCode);
		return item;
	}

	private POSDepartment getPOSDepartmentByCode(String posDepartmentCode, List posDepartments) {

		Iterator itPOSDepartments = posDepartments.iterator();
		while (itPOSDepartments.hasNext()) {
			POSDepartment posDepartment = (POSDepartment) itPOSDepartments.next();
			if (posDepartment.getId().compareTo(Integer.valueOf(posDepartmentCode)) == 0)
				return posDepartment;
		}
		Query query = sesion
				.createQuery("from com.allc.entities.POSDepartment where code = '" + posDepartmentCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (POSDepartment) list.get(0);
		POSDepartment posDepartment = new POSDepartment();
		posDepartment.setCodDptoCer(posDepartmentCode);
		posDepartment.setName("Departamento " + posDepartmentCode);
		posDepartments.add(posDepartment);
		return posDepartment;
	}

	private MerchandiseHierarchyGroup getMerchandiseHierarchyGroupByCode(String merchandiseHierarchyGroupCode,
			POSDepartment posDepartment, List merchHierarchyGroups) {

		if (posDepartment != null && posDepartment.getId() != null) {
			Iterator itMerchHierarGP = merchHierarchyGroups.iterator();
			while (itMerchHierarGP.hasNext()) {
				MerchandiseHierarchyGroup merchandiseHierarchyGroup = (MerchandiseHierarchyGroup) itMerchHierarGP
						.next();
				if (merchandiseHierarchyGroup.getId().compareTo(Integer.valueOf(merchandiseHierarchyGroupCode)) == 0)
					return merchandiseHierarchyGroup;
			}
			Query query = sesion.createQuery("from com.allc.entities.MerchandiseHierarchyGroup where code = '"
					+ merchandiseHierarchyGroupCode + "' and posDepartment.id = " + posDepartment.getId());
			List list = query.list();
			if (list != null && !list.isEmpty())
				return (MerchandiseHierarchyGroup) list.get(0);
		}
		MerchandiseHierarchyGroup merchandiseHierarchyGroup = new MerchandiseHierarchyGroup();
		merchandiseHierarchyGroup.setCodMRHCer(merchandiseHierarchyGroupCode);
		merchandiseHierarchyGroup.setName("Familia " + merchandiseHierarchyGroupCode);
		merchandiseHierarchyGroup.setDescription("Familia " + merchandiseHierarchyGroupCode);
		merchandiseHierarchyGroup.setPosDepartment(posDepartment);
		merchHierarchyGroups.add(merchandiseHierarchyGroup);
		return merchandiseHierarchyGroup;
	}

	private Extranjero getExtranjeroByID(String customerCode) {
		Query query = sesion.createQuery("from com.allc.entities.Extranjero where cd_cpr = '" + customerCode + "'");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (Extranjero) list.get(0);
		return null;
	}

	private CedRuc getCedRucByID(String customerCode) {
		Query query = sesion.createQuery("from com.allc.entities.CedRuc where cd_cpr = '" + customerCode + "'");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (CedRuc) list.get(0);
		return null;
	}

	private CouponToRedemption getCouponToRedemptionByID(String couponCode) {
		Query query = sesion
				.createQuery("from com.allc.entities.CouponToRedemption where cd_cpn = '" + couponCode + "'");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (CouponToRedemption) list.get(0);
		return null;
	}

	private Tender getTenderByCode(String tenderCode, List tenders) {
		Iterator itTenders = tenders.iterator();
		while (itTenders.hasNext()) {
			Tender tender = (Tender) itTenders.next();
			if (tender.getTenderTypeCode().equalsIgnoreCase(tenderCode))
				return tender;
		}
		Query query = sesion.createQuery("from com.allc.entities.Tender where tenderTypeCode = '" + tenderCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (Tender) list.get(0);
		Tender tender = new Tender();
		tender.setTenderTypeCode(tenderCode);
		tenders.add(tender);
		return tender;
	}

	private Safe getSafeByID(Integer safeID) {
		Query query = sesion.createQuery("from com.allc.entities.Safe where tenderRepositoryID = '" + safeID + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (Safe) list.get(0);
		Safe safe = new Safe();
		safe.setTenderRepositoryID(safeID);
		return safe;
	}

	private Till getTillByID(Integer tillID) {
		Query query = sesion.createQuery("from com.allc.entities.Till where tenderRepositoryID = '" + tillID + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (Till) list.get(0);
		Till till = new Till();
		till.setTenderRepositoryID(tillID);
		return till;
	}

	private ExternalDepository getExternalDepositoryByID(Integer externalDepositoryID) {
		Query query = sesion.createQuery(
				"from com.allc.entities.ExternalDepository where tenderRepositoryID = '" + externalDepositoryID + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (ExternalDepository) list.get(0);
		ExternalDepository externalDepository = new ExternalDepository();
		externalDepository.setTenderRepositoryID(externalDepositoryID);
		return externalDepository;
	}
	
	private TransactionTotalType getTransactionTotalTypeByCode(String totalTypeCode) {
		Query query = sesion
				.createQuery("from com.allc.entities.TransactionTotalType where transactionTotalTypeCode = '"
						+ totalTypeCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (TransactionTotalType) list.get(0);
		TransactionTotalType transactionTotalType = new TransactionTotalType();
		transactionTotalType.setTransactionTotalTypeCode(totalTypeCode);
		;
		return transactionTotalType;
	}

	private void setCouponRedeemed(String barcode) throws SQLException {
		Statement statement = sesion.connection().createStatement();
		int countRows = statement.executeUpdate("UPDATE CO_CPN_DT SET STS = 1 WHERE CPN_ID = " + barcode);
	}

	private Currency getCurrencyByID(Integer currencyID, List currencys) {
		Iterator itCurrencys = currencys.iterator();
		while (itCurrencys.hasNext()) {
			Currency currency = (Currency) itCurrencys.next();
			if (currency.getCurrencyID().equals(currencyID))
				return currency;
		}
		Query query = sesion.createQuery("from com.allc.entities.Currency where currencyID = '" + currencyID + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (Currency) list.get(0);
		Currency currency = new Currency();
		currency.setCurrencyID(currencyID);
		currencys.add(currency);
		return currency;
	}

	private void setPagosCerStatus(List lineItems, String numNC) {
		iniciarSesionDevs();
		SQLQuery query = sessionDevs.createSQLQuery(
				"SELECT P.SQ_NMB, P.ESTADO FROM DV_TICKET T, DV_PCER P WHERE T.ID_DEV = P.ID_DEV AND T.NOTA =  '"
						+ numNC + "'");
		List<Object[]> rows = query.list();
		Map pagos = new HashMap();
		for (Object[] row : rows) {
			pagos.put(Integer.valueOf(row[0].toString()), Integer.valueOf(row[1].toString()));
		}
		Integer seq = 0;
		Iterator itLineItems = lineItems.iterator();
		while (itLineItems.hasNext()) {
			RetailTransactionLineItem retLineItem = (RetailTransactionLineItem) itLineItems.next();
			if (retLineItem.getTender() != null
					&& ("51".equals(retLineItem.getTender().getTenderTypeCode())
							|| "52".equals(retLineItem.getTender().getTenderTypeCode())
							|| "53".equals(retLineItem.getTender().getTenderTypeCode())
							|| "54".equals(retLineItem.getTender().getTenderTypeCode()))
					&& retLineItem.getTender().getIsChangeFlag()) {
				seq++;
//				if (pagos.containsKey(seq)) {
//					retLineItem.getTender().getTenderReturnLineItem()
//							.setStatus(((Integer) pagos.get(seq)).compareTo(1) == 0 ? 1 : 0);
//				}
				retLineItem.getTender().getTenderReturnLineItem().setStatus(1);
			} else if(retLineItem.getTender() != null
					&& ("62".equals(retLineItem.getTender().getTenderTypeCode())
							|| "63".equals(retLineItem.getTender().getTenderTypeCode()))
					&& retLineItem.getTender().getIsChangeFlag())
				//si es retencion se marca como reversado automaticamente
				retLineItem.getTender().getTenderReturnLineItem().setStatus(1);
		}
		sessionDevs.close();
		sessionDevs = null;
	}
	
	private boolean existePagoTarjBanc(List lineItems) {
		Iterator itLineItems = lineItems.iterator();
		while (itLineItems.hasNext()) {
			RetailTransactionLineItem retLineItem = (RetailTransactionLineItem) itLineItems.next();
			if (retLineItem.getTender() != null
					&& "55".equals(retLineItem.getTender().getTenderTypeCode()) && (retLineItem.getTender().getTenderAccountNumber() != null && !retLineItem.getTender().getTenderAccountNumber().isEmpty())) {
				return true;
			} 
		}
		return false;
	}

	private void setInvoiceNumberToTerminalStatus(Integer storeCode, Integer terminal, String numFactura)
			throws SQLException {
		Statement statement = sessionEyes.connection().createStatement();
		int countRows = statement.executeUpdate("UPDATE WS_STS SET ULT_NUM_FACT = " + numFactura + " WHERE STR_CD = "
				+ storeCode + "AND TML = " + terminal);
	}

	public void cierraOperacion() {
		log.info("Cerrando Sesion");
		try {
			if(sesion != null){
				sesion.close();
				sesion = null;
                                tx = null;
			}			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void iniciaOperacion() {
                log.info("Abriendo Sesion");
		while (sesion == null || tx == null) {
			try {
				sesion = HibernateUtil.getSessionFactory().openSession();
				tx = sesion.beginTransaction();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sesion = null;
				tx = null;
			}
			if (sesion == null || tx == null){
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
                        } 
		}
	}

	private void iniciarSesionEyes() {
		while (sessionEyes == null) {
			try {
				sessionEyes = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
				txEyes = sessionEyes.beginTransaction();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sessionEyes = null;
				txEyes = null;
			}
			if (sessionEyes == null || txEyes == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private void iniciarSesionDevs() {
		while (sessionDevs == null) {
			try {
				sessionDevs = HibernateSessionFactoryContainer.getSessionFactory("Devs").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sessionDevs = null;
			}
			if (sessionDevs == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	public boolean insertaArchTlog(String nombTlog, String store, String status) throws SQLException {

		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			Query query = sesion.createSQLQuery(
					"INSERT INTO CO_TLOG (NOM_TLOG, DES_CLAVE, STATUS) VALUES (:valor1, :valor2, :valor3)");
			query.setParameter("valor1", nombTlog);
			query.setParameter("valor2", Integer.valueOf(store));
			query.setParameter("valor3", status);
			query.executeUpdate();

			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	public boolean insertaTposCierre(String storeCode, String date, String time) throws SQLException {

		try {
			log.info("insertando en TPOS_CIERRE: Store: "+storeCode+" date: "+date+" time: "+time);
			Query query = sesion.createSQLQuery(
					"INSERT INTO TPOS_CIERRE (STORE, FECHA, TIME) VALUES (:valor1, :valor2, :valor3)");
			query.setParameter("valor1", storeCode);
			query.setParameter("valor2", date);
			query.setParameter("valor3", time);
			query.executeUpdate();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	private List<String> getInfoTrxNc(String numNC) {
		try {

			SQLQuery query = sesion
					.createSQLQuery("Select TR_RTN.ID_TRN, TR_RTN.ORGL_INVC_NMB From TR_RTN, TR_TRN Where TR_TRN.ID_TRN = TR_RTN.ID_TRN AND TR_TRN.FL_CNCL <> 1  and TR_RTN.RTN_NMB = '" + numNC + "'");
			List<Object[]> rows = query.list();
			for (Object[] row : rows) {
				List<String> retorno = new ArrayList<String>();
				retorno.add(row[0] != null ? row[0].toString() : null);
				retorno.add(row[1] != null ? row[1].toString() : null);
				return retorno;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public Integer getIdTrxOri(String facturaOri) {
		try {
			SQLQuery query = sesion
					.createSQLQuery("Select TR_INVC.ID_TRN From TR_INVC, TR_TRN Where TR_TRN.ID_TRN = TR_INVC.ID_TRN AND TR_TRN.FL_CNCL <> 1 AND TR_INVC.INVC_NMB = '" + facturaOri + "'");
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public Integer getIdTicketOri(Integer nroTicket, Date fechaCont, Integer nroPos, Integer tienda) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			log.info("getIdTicketOri: Select TR_RTL.ID_TRN From TR_RTL, TR_TRN, AS_WS, PA_STR_RTL Where TR_TRN.ID_TRN = TR_RTL.ID_TRN AND TR_TRN.ID_WS = AS_WS.ID_WS AND TR_TRN.ID_BSN_UN = PA_STR_RTL.ID_BSN_UN AND TR_TRN.FL_CNCL <> 1 "
							+ "AND TR_TRN.AI_TRN = "+nroTicket+" AND TR_TRN.DC_DY_BSN = '"+sdf.format(fechaCont)+"' AND CAST(AS_WS.CD_WS AS INT) = "+nroPos+" AND PA_STR_RTL.CD_STR_RT = "+tienda);
			SQLQuery query = sesion
					.createSQLQuery("Select TR_RTL.ID_TRN From TR_RTL, TR_TRN, AS_WS, PA_STR_RTL Where TR_TRN.ID_TRN = TR_RTL.ID_TRN AND TR_TRN.ID_WS = AS_WS.ID_WS AND TR_TRN.ID_BSN_UN = PA_STR_RTL.ID_BSN_UN AND TR_TRN.FL_CNCL <> 1 "
							+ "AND TR_TRN.AI_TRN = "+nroTicket+" AND TR_TRN.DC_DY_BSN = '"+sdf.format(fechaCont)+"' AND CAST(AS_WS.CD_WS AS INT) = "+nroPos+" AND PA_STR_RTL.CD_STR_RT = "+tienda);
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	private void setIlimitadaReverse(Integer idTrx) throws SQLException {
		Statement statement = sesion.connection().createStatement();
		int countRows = statement.executeUpdate("UPDATE CO_ILIM_DT SET FL_VD = 1 WHERE ID_TRN = " + idTrx);
	}
	
	private void setAllPagoPinpadReverse(Integer idTrx) throws SQLException {
		Statement statement = sesion.connection().createStatement();
		int countRows = statement.executeUpdate("UPDATE CO_TND_PINPAD SET FL_RV = 1 WHERE ID_TRN = " + idTrx);
	}
	
	private void setPagoPinpadReverse(Integer idTrx, String autorizacion) throws SQLException {
		Statement statement = sesion.connection().createStatement();
		int countRows = statement.executeUpdate("UPDATE CO_TND_PINPAD SET FL_RV = 1 WHERE ID_TRN = " + idTrx
				+ " and NUM_AUTO = '" + autorizacion + "'");
	}

	private void setRtnItemStatus(Integer idTrx, Integer lineItem) throws SQLException {
		Statement statement = sesion.connection().createStatement();
		int countRows = statement.executeUpdate(
				"UPDATE TR_LTM_TND_RTN SET STS = 1 WHERE ID_TRN = " + idTrx + " and AI_LN_ITM = " + lineItem);
	}

	private void setCloseFinished(Integer idTrx) throws SQLException {
		Statement statement = sesion.connection().createStatement();
		int countRows = statement.executeUpdate("UPDATE tr_bsn_eod SET CL_END = 1 WHERE ID_TRN = " + idTrx);
	}

	private void setReservaUtilizada(String reserva, String numSerie) throws SQLException {
		Statement statement = sesion.connection().createStatement();
		int countRows = statement.executeUpdate(
				"UPDATE CO_RESERVA SET FL_RSV_USED = 1 WHERE CD_RSV = " + reserva + " and NUM_SERIE = " + numSerie);
	}

	public boolean existeArchivoEnBd(String archivo, String tienda) {

		try {
			Query query = sesion.createSQLQuery(
					"SELECT ID_TLOG FROM CO_TLOG WHERE NOM_TLOG = '" + archivo + "' and DES_CLAVE = '" + tienda + "'");
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;

	}

	public void deleteArchFromDb(String archivo, String tienda) throws SQLException {
		Statement statement = sesion.connection().createStatement();
		int countRows = statement.executeUpdate(
				"DELETE FROM CO_TLOG WHERE NOM_TLOG = '" + archivo + "' and DES_CLAVE = '" + tienda + "'");
	}

	public void writeItemInFileEx(Long itemCode) {

		File exItemFIle = new File("C:/ALLC", "ITEMEX");
		try {
			BufferedWriter bwr = new BufferedWriter(new FileWriter(exItemFIle, true));

			String code = String.valueOf(itemCode);
			bwr.write(code);
			bwr.newLine();
			if (bwr != null) {
				bwr.close();
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	public Item getItem(String codSAP) {
		try {
			Query query = sesion
					.createQuery(" FROM com.allc.entities.Item I WHERE I.codigoSAP = " + new Integer(codSAP));
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return (Item) rows.get(0);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public Moto getMoto(Integer idItem) {
		Query query = sesion.createQuery("from com.allc.arms.server.persistence.moto.Moto where itemID = " + idItem);
		Iterator iterator = query.iterate();
		if (iterator.hasNext())
			return (Moto) iterator.next();
		return null;
	}

	private void setReturnMoto(String serialNum) throws SQLException {
		Statement statement = sesion.connection().createStatement();
		int countRows = statement.executeUpdate("UPDATE TR_LTM_MOTO_DT SET FL_RV = 1 WHERE SRL_NBR = '" + serialNum + "'");
	}
	
	public Integer getTaxFacturaOriginal(String numFactOriginal) {
		try {
			SQLQuery query = sesion.createSQLQuery("Select IMP_1 From TR_INVC Where INVC_NMB = '" + numFactOriginal + "'");
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return 0;
	}
	
	private void marcarItmsScaneadosDemoradosTrx(com.allc.entities.Transaction trx) {
		try {
			log.info("************ Inicio marcarItmsScaneadosDemoradosTrx *************************");
			//ojjo configurar en td properties!!, preguntar gustavo como
			Integer disparadorItems = Integer.parseInt(properties.getObject("proceso.cantidad.articulos.minuto.cajero.items.disparador").toString());
			Integer disparadorItemsPorFreq = Integer.parseInt(properties.getObject("proceso.cantidad.articulos.minuto.cajero.items.frecuencia.segundos").toString());
			log.info("Disparador cantidad items x minuto minimo --> "+disparadorItems);
			log.info("Disparador tiempo items x freq (seg) --> "+disparadorItemsPorFreq);
			SQLQuery queryUpd;
			
			//iniciaOperacion();
			tx = sesion.beginTransaction();
			
			//log.info(SqlPostgres.OBTENER_OPERADORES_LENTOS_POR_TRX);
			
			SQLQuery query = sesion.createSQLQuery(SqlPostgres.OBTENER_OPERADORES_LENTOS_POR_TRX);
			query.setInteger(0, disparadorItemsPorFreq); 
			query.setInteger(1, trx.getTransactionID()); //idBsnUn
			//query.setInteger(0, trx.getRetailStore().getRetailStoreID()); //idBsnUn
			//query.setInteger(1, trx.getWorkstation().getWorkstationID()); //idWs
			//query.setInteger(2, trx.getSequenceNumber()); //aiTrn
			query.setInteger(2, disparadorItems); 
			query.setInteger(3, disparadorItems); 
			List<Object[]> rows = query.list();
			if (rows == null || rows.isEmpty())
			{
				log.info("No hay alerta que notificar...");
				log.info("desmarcar cajero");
				queryUpd = sesion.createSQLQuery(SqlPostgres.ACTUALIZAR_CAJERO_LENTO);
//				queryUpd.setInteger(0, 0);
//				queryUpd.setInteger(1, trx.getRetailStore().getRetailStoreID()); //idBsnUn
//				queryUpd.setInteger(2, trx.getWorkstation().getWorkstationID()); //idWs
//				queryUpd.setString(3, trx.getOperator().getCode()); //cd_opr
				
				
				queryUpd.setInteger(0, 0); //is slow
				queryUpd.setBigInteger(1, BigInteger.ZERO); // id trn
				queryUpd.setInteger(2, 0); //ai trn
				queryUpd.setInteger(3, trx.getRetailStore().getRetailStoreID()); //idBsnUn
				queryUpd.setInteger(4, trx.getWorkstation().getWorkstationID()); //idWs
				queryUpd.setString(5, trx.getOperator().getCode()); //cd_opr
				
				queryUpd.executeUpdate();
				tx.commit();
				return;
			}
			
			Object[] row = rows.get(0);
			
			//String ipWsTiendaSupervisor = row[1].toString();
			String codigoCaja = "000" + row[5].toString();
	        codigoCaja = codigoCaja.substring(codigoCaja.length() - 3);
	        
	        log.info("tienda --> "+row[2].toString() + " -----------------------------");
     		log.info("codigo tienda --> "+(Integer) row[1]);
     		log.info("COD_OPERADOR --> "+trx.getOperator().getCode());
     		log.info("Caja --> "+ codigoCaja);
     		log.info("Id trn --> "+ (BigInteger) row[6]);
     		log.info("ai_trn --> "+ (Integer) row[8]);
     		log.info("Disparador cantidad items x minuto minimo --> "+disparadorItems);
     		log.info("   Total lineas venta --> "+(Integer)row[22]);
 			log.info("   Total articulos --> "+(Integer)row[23]);
 			log.info("   Item x seg --> "+(BigDecimal)row[25]);
 			log.info("   Item x freq --> "+(BigDecimal)row[26]);
     		log.info("----------------------------------------------------------------");
     		log.info("");
     		log.info("marcar cajero con trx");
     		log.info("validando si existe cajero marcado..");
     		
     		query = sesion.createSQLQuery(SqlPostgres.VALIDA_EXISTE_CAJERO_LENTO);
     		query.setInteger(0, trx.getRetailStore().getRetailStoreID()); //idBsnUn
     		query.setInteger(1, trx.getWorkstation().getWorkstationID()); //idWs
     		query.setString(2, trx.getOperator().getCode()); //cd_opr
			
			//Object[] rowE = (Object[]) query.list().get(0);
			BigInteger existeCajero = (BigInteger) query.list().get(0);
			
			log.info("cant --> "+existeCajero);
			
			if (existeCajero.compareTo(BigInteger.ZERO) == 0)
			{
				log.info("insertar cajero lento");
				queryUpd = sesion.createSQLQuery(SqlPostgres.INSERTA_CAJERO_LENTO);
				queryUpd.setInteger(0, trx.getRetailStore().getRetailStoreID()); //idBsnUn
				queryUpd.setInteger(1, trx.getWorkstation().getWorkstationID()); //idWs
				queryUpd.setString(2, trx.getOperator().getCode()); //cd_opr
				queryUpd.setInteger(3, 1);  //IS SLOW
				queryUpd.setInteger(4, (Integer) row[8]); //ai trn
				queryUpd.setBigInteger(5, (BigInteger) row[6]); // id trn
				queryUpd.executeUpdate();
			}
			else
			{
				log.info("actualizar cajero lento");
				queryUpd = sesion.createSQLQuery(SqlPostgres.ACTUALIZAR_CAJERO_LENTO);
				queryUpd.setInteger(0, 1); //is slow
				queryUpd.setBigInteger(1, (BigInteger) row[6]); // id trn
				queryUpd.setInteger(2, (Integer) row[8]); //ai trn
				queryUpd.setInteger(3, trx.getRetailStore().getRetailStoreID()); //idBsnUn
				queryUpd.setInteger(4, trx.getWorkstation().getWorkstationID()); //idWs
				queryUpd.setString(5, trx.getOperator().getCode()); //cd_opr
				queryUpd.executeUpdate();
			}
			tx.commit();
     		
			
		} catch (Exception ex) {
			log.error("!!!!!Error Inesperado marcarItmsScaneadosDemoradosTrx!!!!!!!", ex);
			tx.rollback();
		}
		finally {
			log.info("************ Fin marcarItmsScaneadosDemoradosTrx *************************");
		}
	}
		
		
		private void alertaItmsScaneadosDemoradosTrx(com.allc.entities.Transaction trx) {
			String CODIGO_PROCESO = "2.1";
			String CODIGO_CATALOGO_PARAMETROS = "CONF_AL_NOT2.1";
			String TITULO_ALERTA = "Productividad por Transaccion";
//			if (!validarEjecucionProcesoAlerta(CODIGO_PROCESO))
//			{
//				return;
//			}
			
			try {
				
				if (!conexionDbLink())
				{
					return;
				}
				
				log.info("************ Inicio alertaItmsScaneadosDemoradosTrx *************************");
				Map<String,String> propDb = obtenerPropiedadesAlertasSupervisorBD(CODIGO_PROCESO);
				SQLQuery query = sesion.createSQLQuery(SqlPostgres.OBTENER_TITULO_ALERTA);
				query.setString(0, CODIGO_PROCESO);
				List<Object[]> rows = query.list();
				TITULO_ALERTA = rows.get(0)+"";

				//Integer disparadorItems = Integer.parseInt(propDb.get("proceso.cantidad.articulos.minuto.cajero.items.disparador").toString());
				Integer disparadorItemsPorFreq = Integer.parseInt(propDb.get("proceso.cantidad.articulos.minuto.cajero.items.frecuencia.segundos").toString());
				//log.info("Disparador cantidad items x minuto minimo --> "+disparadorItems);
				log.info("Disparador tiempo items x freq (seg) --> "+disparadorItemsPorFreq);
				SQLQuery queryUpd;
				LocalDateTime  fechaInicio = LocalDateTime .now();
				
				//iniciaOperacion();
				tx = sesion.beginTransaction();
				
				//log.info(SqlPostgres.OBTENER_OPERADORES_LENTOS_POR_TRX);
				
				query = sesion.createSQLQuery(SqlPostgres.OBTENER_OPERADORES_LENTOS_POR_TRX);
				query.setInteger(0, disparadorItemsPorFreq); 
				query.setInteger(1, trx.getTransactionID()); //idBsnUn
				rows = query.list();
				if (rows == null || rows.isEmpty())
				{
					log.info("No hay alerta que notificar...");
					return;
				}
				
				Object[] row = rows.get(0);
				
				String ipWsTiendaSupervisor = row[3].toString();
				String codigoCaja = "000" + row[5].toString();
		        codigoCaja = codigoCaja.substring(codigoCaja.length() - 3);
		        boolean notificarSupervisor = ((Integer) row[24]) == 1 ;
		        Integer disparadorItems = (Integer) row[30];
		        Integer tienda = (Integer) row[1];
		        Integer terminal = Integer.parseInt(codigoCaja); 
		        propDb = obtenerPropiedadesAlertasSupervisorBD(CODIGO_PROCESO,tienda,terminal);
		        
		        log.info("tienda --> "+row[2].toString() + " -----------------------------");
	     		log.info("codigo tienda --> "+tienda);
	     		log.info("COD_OPERADOR --> "+Integer.parseInt(trx.getOperator().getCode())+"");
	     		log.info("OPERADOR --> "+ row[25].toString());
	     		log.info("Caja --> "+ codigoCaja);
	     		log.info("Id trn --> "+ (BigInteger) row[6]);
	     		log.info("ai_trn --> "+ (Integer) row[8]);
	     		log.info("Disparador cantidad items x minuto minimo --> "+disparadorItems);
	     		log.info("   Total lineas venta --> "+(Integer)row[22]);
	 			log.info("   Total articulos --> "+(Integer)row[23]);
	 			log.info("   Item x seg --> "+(BigDecimal)row[27]);
	 			log.info("   Item x freq --> "+(BigDecimal)row[28]);
	 			log.info("   Tiempo Total Trx --> "+(String)row[29]);
	     		log.info("----------------------------------------------------------------");
	     		log.info("");
	     		
	     		AlertTecRequest alert = new AlertTecRequest();
	        	AutorizacionRequestIp alertIp = new AutorizacionRequestIp();
	        	
	        	alert.setCaja(Integer.parseInt(codigoCaja));
	        	alert.setIdLocal((Integer) row[1]);
	        	alert.setDescripcionAlerta("menos de "+disparadorItems+" articulos escaneados"
	        			+ " | cantidad de escaneos por minuto: "+(BigDecimal)row[28]
	        			+" | Operador: "+trx.getOperator().getCode() +" - "+row[25].toString()
	        	);
	        	
	        	alert.setDescripcionAlerta(propDb.get("proceso.cantidad.articulos.minuto.cajero.msj.notif")
						.replace("**MINIMO_ITM**", disparadorItems+"")
						.replace("**ITM_X_MIN**", (BigDecimal)row[28]+"")
						.replace("**OPERADOR**", trx.getOperator().getCode() +" - "+row[25].toString())
				);
	        	
	        	alert.setTituloAlerta(TITULO_ALERTA);
	        	alert.setTipoAlerta(ClasificacionAlertas.EFECTIVIDAD_CAJERO);
	        	alert.setValor(((BigDecimal)row[28]) + "");
	        	
	        	alertIp.setIp(ipWsTiendaSupervisor);
	        	alertIp.setAutorizacionRequest(alert);
	        	
	        	String auditInsert ="("+(Integer) row[1]+"," //codigo tienda
	    	     		+"'"+row[2].toString()+"'," //tienda
	    	     		+"'"+CODIGO_PROCESO+"'," //codigo alerta
	    	     		+"'"+TITULO_ALERTA+"',"//nombre alerta
	    	     		+"'"+row[5].toString()+"',"  //codigo caja
	    	     		+(Integer) row[8]+"," //secuencial trx
	    	     		+trx.getOperator().getCode()+"," //codigo operador
	    	     		+"100007,"
	    	     		+"'"+Files.toStringJson(alert)+"',"// json envio
	    	     		+"null,"
	    	     		+"null,"
	    	     		+"'{"+
	    	     			"\"minItmsPorMinuto\" : "+disparadorItems+
	    	     		"}'," //json parametros
	    	     		+"now()"	
	    	     		+")";
	        	
	        	//registrar alertas detectadas
		        registroAlertaNotifSupervAudit(auditInsert);
	        	
	        	//enviar notificacion
	        	//consumoServicio(strJson);
	        	String strJson = Files.toStringJson(alertIp.getAutorizacionRequest());
	        	
	        	//if (validarEnvioAlertaSupervisorLocal((Integer) row[1], CODIGO_PROCESO))
	        	if (notificarSupervisor)
	        	{
	        		notificaSupervisorTienda(strJson,alertIp.getIp());
	        	}
	     		
	     		
	     		
				
			} catch (Exception ex) {
				log.error("!!!!!Error Inesperado alertaItmsScaneadosDemoradosTrx!!!!!!!", ex);
				if (!desconexionDbLink())
				{
					return;
				}
			}
			finally {
				log.info("************ Fin alertaItmsScaneadosDemoradosTrx *************************");
			}
		
		
	}
		
		public void notificaSupervisorTienda(String jsonRequest, String host) throws Exception {
			
			try {
				
				//String u = "http://pos-favorita.devsu.us/favorita-pos-server/api/exposed/sendAlertTEC";
			    String u =  "http://"+host+":8080"+ properties.getObject("notificacion.supervisor.tienda.webservice.url").toString();
			    //
			    log.info("url consumo --> "+u);
			    log.info("json envio:");
			    log.info(jsonRequest);
			    
			    URL url = new URL(u);


			    //HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			    conn.setDoOutput(true);   
			    conn.setRequestMethod("POST");  
			    conn.setUseCaches(false);  
			    conn.setConnectTimeout(10000);  
			    conn.setReadTimeout(10000);  
			    conn.setRequestProperty("Content-Type", "application/json; utf-8");
			    conn.setRequestProperty("Accept", "application/json");
			    	String userpass = "supermaxi-admin:SUPERmaxi1";
			    String auth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			    //String auth = (String) properties.getObject("colas.alertas.tec.webservice.notificar.autorizacion");
			    log.info("auth --> "+auth);
			    conn.setRequestProperty ("Authorization", auth);

			    
			    	OutputStream os = conn.getOutputStream();
			        byte[] input = jsonRequest.getBytes("utf-8");
			        os.write(input, 0, input.length);	
			        os.close();
			    
			    int respCode = conn.getResponseCode();
			    
			    log.info("http status code --> "+respCode);
			    
			    
			    
			    //leer respuesta
			    StringBuilder response = new StringBuilder();
			    
			    InputStreamReader isr = new InputStreamReader(
						  (respCode != 200 && respCode != 201)
						  ? conn.getErrorStream()
						  : conn.getInputStream()
						  , "utf-8"
						  );
			    BufferedReader br = new BufferedReader(isr);
			    
			    /*
			    		  
			    		  )
			    */
			    String responseLine = null;
			    while ((responseLine = br.readLine()) != null) {
			        response.append(responseLine.trim());
			    }
			    br.close();
			    log.info(response.toString());
			    if (respCode != 200 && respCode != 201)
			    {
			    	
			    	log.info("error inesperado al notificar supervisor");
			    	//conn.getErrorStream();
			    	return;
			    }
			    
			    ResponseAutorizacionService jsonRespuesta = new Gson().fromJson(response.toString(), ResponseAutorizacionService.class);
			    
			    
			    if (!jsonRespuesta.getRespuesta().isEstado())
			    {
			    	log.info("Error al notificar alerta:");
			    	log.info("mensaje --> "+jsonRespuesta.getRespuesta().getError().getMensaje());
			    	return;
			    }
			    log.info("alerta enviada!");
		    
			} catch (Exception ex) {
				ex.printStackTrace();
				log.error("Error inesperado al consultar servicio:",ex);
			}
		    		
		}
		
		private boolean validarEjecucionProcesoAlerta(String codigoAlerta) {
			
			try {
		        
		        
		        SQLQuery query = sesion.createSQLQuery(SqlPostgres.VALIDA_EJECUCION_PROCESO_ALERTA_SUPERVISOR);
				query.setString(0, codigoAlerta);
				Integer result=0;
				result = ((BigInteger) query.list().get(0)).intValue();
				 
		        return result == 1;
		        
			} catch (Exception ex) {
				log.error("Error al verificar validacion ejecucion proceso alerta supervisor",ex);
				return false;
			}
			
		}
		
		private boolean validarEnvioAlertaSupervisorLocal(Integer codigoTienda, String codigoAlerta) {
			
			try {
				Integer result=0;
				SQLQuery query = sesion.createSQLQuery(SqlPostgres.VALIDA_NOTIFICAR_ALERTA_PROCESO_TIENDA);
				query.setInteger(0, codigoTienda);
				query.setString(1, codigoAlerta);
				result = ((BigInteger) query.list().get(0)).intValue();

		        return result == 1;
		        
			} catch (Exception ex) {
				log.error("Error al verificar validacion envio de notificacion al supervisor",ex);
				return false;
			}
			
		}
		
		private Map<String,String> obtenerPropiedadesAlertasSupervisorBD(String codigoCatalgo) {
			return obtenerPropiedadesAlertasSupervisorBD(codigoCatalgo,null,null);
		}

		private Map<String,String> obtenerPropiedadesAlertasSupervisorBD(String codigoCatalgo, Integer storeNumber,Integer terminalNumber) {
			Map<String,String> parametros = new HashMap<String, String>();
			
			try {
				SQLQuery query = sesion.createSQLQuery(SqlPostgres.OBTENER_PARAMETROS_ALERTAS_SUPERVISOR_EN_BD);
				query.setParameter(0, storeNumber, StandardBasicTypes.INTEGER);
				query.setParameter(1, terminalNumber, StandardBasicTypes.INTEGER);
				query.setString(2, codigoCatalgo);
				//query.setString(0, codigoCatalgo);

				List<Object[]> rows = query.list();

				
				if (rows.size() == 0)
				{
					log.error("No se pudo cargar los parametros para la alerta "+codigoCatalgo);
			    	return null;
				}
				
				for (Object[] row : rows)
				{
					parametros.put((String)row[0], (String)row[1]);
				}
				

				return parametros;

			} catch (Exception ex) {
				log.error("Error al obtener parametros de configuracion de la alerta en BD",ex);
				return null;
			}
					
		}
		
		private void registroAlertaNotifSupervAudit(String queryInsert) {
			
			Transaction tx = null;
			try {
				tx = sesion.beginTransaction();
				Query query = sesion.createSQLQuery(SqlPostgres.AL_NS_REGISTRO_AUDIT + queryInsert + ";");
				query.executeUpdate();

				tx.commit();
			} catch (Exception ex) {
				tx.rollback();
				log.error("Error al registrar alerta procesada",ex);
			}
		}
		
		public boolean conexionDbLink () {
	    	if (Integer.parseInt(properties.getObject("distributor.es.regional").toString()) == 0)
	        {
	        	return true;
	        }
	    	
	    	log.info("conexion a la central");
	    	try {

	    		SQLQuery query = sesion.createSQLQuery(SqlPostgres.CHECK_DBLINK);
	    		List<Object[]> rows = query.list();
	    		if (rows != null && !rows.isEmpty())
	    		{
	    			log.info("ya existe una conexion a la central");
	    			return true;
	    		}
	    			
	    		
	            
	            
	            query = sesion.createSQLQuery(SqlPostgres.CONEXION_DB_LINK
	            							.replace("**HOST**", properties.getObject("central.db.host").toString())
	            							.replace("**DBNAME**", properties.getObject("central.db.name").toString())
	            							.replace("**USUARIO**", properties.getObject("central.db.usuario").toString())
	            							.replace("**PASSWORD**", properties.getObject("central.db.password").toString())				
	            );
	            
	            rows = query.list();
	            
			} catch (Exception ex) {
				ex.printStackTrace();
				log.error("No se pudo establecer conexion a BD central!",ex);
				return false;
				// TODO: handle exception
			}
	        log.info("conexion establecida");
	        return true;
	    	
	    }
		
		public boolean desconexionDbLink () {
	    		    	
	    	log.info("desconexion a la central");
	    	try {

	    		SQLQuery query = sesion.createSQLQuery(SqlPostgres.DESCONEXION_DBLINK);
	    		List<Object[]> rows = query.list();    			
	            
			} catch (Exception ex) {
				ex.printStackTrace();
				log.error("No se pudo eliminar la conexion a BD central!",ex);
				return false;
				// TODO: handle exception
			}
	        log.info("conexion Eliminada");
	        return true;
	    	
	    }
		

}
