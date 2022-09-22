/**
 * 
 */
package com.allc.converters;

import com.allc.entities.AliasElecCouponItemCode;
import com.allc.entities.AssociatedCoupon;
import com.allc.entities.AssociatedData;
import com.allc.entities.AutomaticCouponData;
import com.allc.entities.BillData;
import com.allc.entities.BonoSolidario;
import com.allc.entities.BonusRedemptionPoints;
import com.allc.entities.CardData;
import com.allc.entities.CheckData;
import com.allc.entities.ControlTransaction;
import com.allc.entities.CouponData;
import com.allc.entities.CouponPromotionData;
import com.allc.entities.CouponToRedemption;
import com.allc.entities.DatosEmpleados;
import com.allc.entities.DeducibleData;
import com.allc.entities.DescuentoEmpleados;
import com.allc.entities.DescuentoEmpleadosTotal;
import com.allc.entities.EcommerceData;
import com.allc.entities.ExceptionLog;
import com.allc.entities.FacturaElec;
import com.allc.entities.FerricardData;
import com.allc.entities.GastoEfectivo;
import com.allc.entities.GiftcardData;
import com.allc.entities.IlimitadaData;
import com.allc.entities.InvoiceData;
import com.allc.entities.ItemReservaInfo;
import com.allc.entities.ManagerOverride;
import com.allc.entities.MiscellaneousFeeLineItem;
import com.allc.entities.MotoData;
import com.allc.entities.PasswTemporal;
import com.allc.entities.PointsPromotionData;
import com.allc.entities.PointsRedemptionData;
import com.allc.entities.PreferredCustomerData;
import com.allc.entities.PreferredCustomerSecPtsData;
import com.allc.entities.PriceModificationLineItem;
import com.allc.entities.PromotionDiscount;
import com.allc.entities.RecargaElec;
import com.allc.entities.Recaudos;
import com.allc.entities.RedeemedCouponData;
import com.allc.entities.RetailTransaction;
import com.allc.entities.RetailTransactionLineItem;
import com.allc.entities.RetailTransactionTotal;
import com.allc.entities.RetencionData;
import com.allc.entities.ReturnLineItem;
import com.allc.entities.ReturnTransaction;
import com.allc.entities.ReverseLineItemInfo;
import com.allc.entities.SaleReturnLineItem;
import com.allc.entities.StringUsuario;
import com.allc.entities.TaxLineItem;
import com.allc.entities.TenderControlTransaction;
import com.allc.entities.TenderControlTransactionLineItem;
import com.allc.entities.TenderDepositTransaction;
import com.allc.entities.TenderLineItem;
import com.allc.entities.TenderLoanTransaction;
import com.allc.entities.TenderPickupTransaction;
import com.allc.entities.TenderPinpadInfo;
import com.allc.entities.TenderReceiptTransaction;
import com.allc.entities.TenderReturnLineItem;
import com.allc.entities.TicketPromotionData;
import com.allc.entities.Transaction;
import com.allc.entities.TransferenciaBancaria;
import com.allc.entities.ValeEmpleado;
import com.allc.entities.VentaMayoreoItem;
import com.thoughtworks.xstream.XStream;

/**
 * @author GUSTAVOK
 * 
 */
public class XStreamConfig {
	private XStream xstream;

	public XStreamConfig() {
		xstream = new XStream();
		xstream.alias("Transaction", Transaction.class);
		xstream.aliasAttribute(Transaction.class, "cancelFlag", "CancelFlag");
		xstream.aliasAttribute(Transaction.class, "voidedFlag", "VoidedFlag");
		xstream.aliasAttribute(Transaction.class, "keyedOfflineFlag",
				"OfflineFlag");
		xstream.aliasAttribute(Transaction.class, "trainingModeFlag",
				"TrainingModeFlag");
		xstream.omitField(Transaction.class, "workstation");
		xstream.omitField(Transaction.class, "operator");
		xstream.omitField(Transaction.class, "retailStore");
		xstream.aliasField("RetailStoreID", Transaction.class,
				"retailStoreCode");
		xstream.aliasField("WorkstationID", Transaction.class,
				"workstationCode");
		xstream.aliasField("SequenceNumber", Transaction.class,
				"sequenceNumber");
		xstream.aliasField("BusinessDayDate", Transaction.class,
				"businessDayDateString");
		xstream.aliasField("BeginDateTime", Transaction.class,
				"beginDateTimeString");
                xstream.omitField(Transaction.class, "beginDateTime");
		xstream.aliasField("EndDateTime", Transaction.class,
				"endDateTimeString");
		xstream.omitField(Transaction.class, "endDateTime");
		xstream.aliasField("Period", Transaction.class, "period");
		xstream.aliasField("SubPeriod", Transaction.class, "subperiod");
		xstream.aliasField("OperatorID", Transaction.class, "operatorCode");
		xstream.aliasField("TransactionTypeCode", Transaction.class, "transactionTypeCode");
		// configuracion para RetailTransaction
		xstream.aliasField("RetailTransaction", Transaction.class,
				"retailTransaction");
		xstream.aliasField("RingElapsedTime", RetailTransaction.class,
				"ringElapsedTime");
		xstream.aliasField("TenderElapsedTime", RetailTransaction.class,
				"tenderElapsedTime");
		xstream.aliasField("IdleElapsedTime", RetailTransaction.class,
				"idleElapsedTime");
		xstream.aliasField("LockElapsedTime", RetailTransaction.class,
				"lockElapsedTime");
		xstream.aliasField("LineItemsScannedCount", RetailTransaction.class,
				"lineItemsScannedCount");
		xstream.aliasField("LineItemsKeyedCount", RetailTransaction.class,
				"lineItemsKeyedCount");
		xstream.aliasField("IncludeTax", RetailTransaction.class,
				"includeTax");
		xstream.aliasField("ItemCount", RetailTransaction.class, "unitCount");
		xstream.aliasField("RetailTransactionTypeCode", RetailTransaction.class, "retailTransactionTypeCode");
		xstream.aliasField("Totals", RetailTransaction.class, "totalItems");
		xstream.alias("Total", RetailTransactionTotal.class);
		xstream.aliasAttribute(RetailTransactionTotal.class,
				"transactionTotalTypeCode", "TotalType");
		xstream.aliasAttribute(RetailTransactionTotal.class, "cancelFlag",
				"CancelFlag");
		xstream.aliasField("Amount", RetailTransactionTotal.class, "amount");
		xstream.aliasField("MotosData", RetailTransaction.class,
				"motosData");
		xstream.alias("MotoData", MotoData.class);
		xstream.aliasField("IsVoid", MotoData.class, "isVoid");
		xstream.aliasField("ItemCode", MotoData.class, "itemCode");
		xstream.aliasField("SerialNumber", MotoData.class, "serialNumber");
		xstream.aliasField("SequenceNumber", MotoData.class, "sequenceNumber");
		xstream.aliasField("CodGerente", MotoData.class, "codGerente");
		xstream.aliasField("Direccion", MotoData.class, "direccion");
		xstream.aliasField("Ciudad", MotoData.class, "ciudad");
		xstream.aliasField("Telefono", MotoData.class, "telefono");
		
		xstream.aliasField("CardsData", RetailTransaction.class,
				"cardData");
		xstream.alias("CardData", CardData.class);
		xstream.aliasField("SequenceNumber", CardData.class, "sequenceNumber");
		xstream.aliasField("CardNumber", CardData.class, "cardNumber");
		xstream.aliasField("CardType", CardData.class, "cardType");
		xstream.aliasField("SyscardResponse", CardData.class, "rtaSyscard");
		xstream.aliasField("ActivationCode", CardData.class, "activationCode");
		xstream.aliasField("SequenceTrx", CardData.class, "sequenceTrx");
		xstream.aliasField("Hora", CardData.class, "hora");
		xstream.aliasField("IdTitular", CardData.class, "idTitular");
		xstream.aliasField("Monto", CardData.class, "monto");
		xstream.aliasField("CupoDisponible", CardData.class, "cupoDisponible");
		xstream.aliasField("StatusTarjeta", CardData.class, "stsTarjeta");
		xstream.aliasField("CodigoRespuesta", CardData.class, "codRta");
		
		xstream.aliasField("Recaudos", RetailTransaction.class,
				"recaudos");
		xstream.alias("Recaudo", Recaudos.class);
		xstream.aliasField("SequenceNumber", Recaudos.class, "sequenceNumber");
		xstream.aliasField("TipoTienda", Recaudos.class, "tipoTienda");
		xstream.aliasField("TipoRecaudo", Recaudos.class, "tipoRecaudo");
		xstream.aliasField("CodigoArticulo", Recaudos.class, "codArticulo");
		xstream.aliasField("Monto", Recaudos.class, "monto");
		
		xstream.aliasField("TransferenciasBancarias", RetailTransaction.class,
				"transferenciasBancarias");
		xstream.alias("TransferenciaBancaria", TransferenciaBancaria.class);
		xstream.aliasField("SequenceNumber", TransferenciaBancaria.class, "sequenceNumber");
		xstream.aliasField("IDBanco", TransferenciaBancaria.class, "idBanco");
		xstream.aliasField("NumeroCuenta", TransferenciaBancaria.class, "numCuenta");
		xstream.aliasField("Monto", TransferenciaBancaria.class, "monto");
		xstream.aliasField("FlagTender", TransferenciaBancaria.class, "flagTender");
		
		xstream.aliasField("ChecksData", RetailTransaction.class,
				"checkData");
		xstream.alias("CheckData", CheckData.class);
		xstream.aliasField("SequenceNumber", CheckData.class, "sequenceNumber");
		xstream.aliasField("Bank", CheckData.class, "bank");
		xstream.aliasField("CheckAccountNumber", CheckData.class, "checkAccountNumber");
		xstream.aliasField("CheckNumber", CheckData.class, "checkNumber");
		xstream.aliasField("CheckDate", CheckData.class, "checkDate");
		xstream.aliasField("CheckAmount", CheckData.class, "checkAmount");
		
		xstream.aliasField("BillData", RetailTransaction.class,
				"billData");
		xstream.alias("BillData", BillData.class);
		xstream.aliasField("SequenceNumber", BillData.class, "sequenceNumber");
		xstream.aliasField("Denomination", BillData.class, "denomination");
		xstream.aliasField("Number", BillData.class, "number");
		
		xstream.aliasField("BonoSolidario", RetailTransaction.class,
				"bonoSolidario");
		xstream.alias("BonoSolidario", BonoSolidario.class);
		xstream.aliasField("SequenceNumber", BonoSolidario.class, "sequenceNumber");
		xstream.aliasField("NumeroTransccion", BonoSolidario.class, "numTrx");
		xstream.aliasField("IDCliente", BonoSolidario.class, "idCliente");
		xstream.aliasField("Cupo", BonoSolidario.class, "cupo");
		xstream.aliasField("MontoDescuento", BonoSolidario.class, "montoDescuento");
		
		xstream.aliasField("DescuentoEmpleadosTotal", RetailTransaction.class,
				"dscEmpTotal");
		xstream.alias("DescuentoEmpleadosTotal", DescuentoEmpleadosTotal.class);
		xstream.aliasField("TrxNumber", DescuentoEmpleadosTotal.class,
				"trxNum");
		xstream.aliasField("MontoTotalDescuento", DescuentoEmpleadosTotal.class,
				"montoTotalDescuento");
		
		xstream.aliasField("DescuentosEmpleado", RetailTransaction.class,
				"dscEmpleados");
		xstream.alias("DescuentoEmpleado", DescuentoEmpleados.class);
		xstream.aliasField("SequenceNumber", DescuentoEmpleados.class, "sequenceNumber");
		xstream.aliasField("CodigoArticulo", DescuentoEmpleados.class, "codArticulo");
		xstream.aliasField("MontoDescuento", DescuentoEmpleados.class, "montoDescuento");
		
		xstream.aliasField("ItemReservaInfo", RetailTransaction.class,
				"itemRsvInfo");
		xstream.alias("ItemReservaInfo", ItemReservaInfo.class);
		xstream.aliasField("TrxNumber", ItemReservaInfo.class,
				"trxNum");
		xstream.aliasField("CodTienda", ItemReservaInfo.class,
				"codTienda");
		xstream.aliasField("ItemCode", ItemReservaInfo.class,
				"itemCode");
		xstream.aliasField("NumReserva", ItemReservaInfo.class,
				"numReserva");
		xstream.aliasField("NumSerie", ItemReservaInfo.class,
				"numSerie");
		xstream.aliasField("Fecha", ItemReservaInfo.class,
				"fecha");
		xstream.aliasField("Monto", ItemReservaInfo.class,
				"monto");
		
		xstream.aliasField("DatosEmpleados", RetailTransaction.class,
				"datosEmpleados");
		xstream.alias("DatosEmpleado", DatosEmpleados.class);
		xstream.aliasField("SequenceNumber", DatosEmpleados.class, "sequenceNumber");
		xstream.aliasField("NumeroTerminal", DatosEmpleados.class, "numTerminal");
		xstream.aliasField("NumeroTransaccion", DatosEmpleados.class, "numTrx");
		xstream.aliasField("NumeroEmpleado", DatosEmpleados.class, "numEmpleado");
		xstream.aliasField("IDPortador", DatosEmpleados.class, "idPortador");
		xstream.aliasField("NombrePortador", DatosEmpleados.class, "nombrePortador");
		xstream.aliasField("TarjetaPistaA", DatosEmpleados.class, "tarjetaPistaA");
		xstream.aliasField("TarjetaPistaB", DatosEmpleados.class, "tarjetaPistaB");
		
		xstream.aliasField("AssociatedData", RetailTransaction.class,
				"associatedData");
		xstream.alias("AssociatedData", AssociatedData.class);
		xstream.aliasField("StoreType", AssociatedData.class,
				"storeType");
		xstream.aliasField("PersonType", AssociatedData.class,
				"personType");
		xstream.aliasField("CountNumber", AssociatedData.class,
				"ctaNumber");
		xstream.aliasField("FlagRedepPoint", AssociatedData.class,
				"flRedemPoint");
		xstream.aliasField("CustomerID", AssociatedData.class,
				"customerID");
		xstream.aliasField("PortadorID", AssociatedData.class,
				"portadorID");
		xstream.aliasField("NamePortador", AssociatedData.class,
				"namePortador");

		xstream.aliasField("PromotionDiscounts", RetailTransaction.class,
				"promotionDiscs");
		xstream.alias("PromotionDiscount", PromotionDiscount.class);
		xstream.aliasField("SequenceNumber", PromotionDiscount.class, "sequenceNumber");
		xstream.aliasField("PromotionSequenceNumber", PromotionDiscount.class, "promotionSequenceNumber");
		xstream.aliasField("Amount", PromotionDiscount.class, "amount");
		xstream.aliasField("PromotionCode", PromotionDiscount.class, "promotionCode");
		xstream.aliasField("PromotionState", PromotionDiscount.class, "flvd");
		xstream.aliasField("OrdinalNumber", PromotionDiscount.class, "ordinalNumber");
		
		xstream.aliasField("CouponsPromotionData", RetailTransaction.class,
				"couponsPromotionData");
		xstream.alias("CouponPromotionData", CouponPromotionData.class);
		xstream.aliasField("SequenceNumber", CouponPromotionData.class, "sequenceNumber");
		xstream.aliasField("Coupon", CouponPromotionData.class, "cupon");
		xstream.aliasField("Cantidad", CouponPromotionData.class, "cantidad");
		
		xstream.aliasField("PointsPromotionData", RetailTransaction.class,
				"pointsPromotionData");
		xstream.alias("PointPromotionData", PointsPromotionData.class);
		xstream.aliasField("SequenceNumber", PointsPromotionData.class, "sequenceNumber");
		xstream.aliasField("LineaDeNegocio", PointsPromotionData.class, "lineaNegocio");
		xstream.aliasField("Cantidad", PointsPromotionData.class, "cantidad");
		
		xstream.aliasField("TicketsPromotionData", RetailTransaction.class,
				"ticketsPromotionData");
		xstream.alias("TicketPromotionData", TicketPromotionData.class);
		xstream.aliasField("SequenceNumber", TicketPromotionData.class, "sequenceNumber");
		xstream.aliasField("CodigoTicket", TicketPromotionData.class, "codTicket");
		xstream.aliasField("TipoTicket", TicketPromotionData.class, "tipoTicket");
		xstream.aliasField("CantidadTicket", TicketPromotionData.class, "cantidadTicket");
		xstream.aliasField("OrdinalNumbers", TicketPromotionData.class, "ordinalNumber");
		
		xstream.aliasField("VentaMayoreoItems", RetailTransaction.class,
				"itemsVentaMayoreo");
		xstream.alias("VentaMayoreoItem", VentaMayoreoItem.class);
		xstream.aliasField("SequenceNumber", VentaMayoreoItem.class, "sequenceNumber");
		
		xstream.aliasField("ReturnLineList", RetailTransaction.class,
				"returnLineList");
		xstream.alias("ReturnLineItem", ReturnLineItem.class);
		xstream.aliasField("SequenceNumber", ReturnLineItem.class, "sequenceNumber");
		xstream.aliasField("OriginalSequenceNumber", ReturnLineItem.class, "originalSequenceNumber");
		
		xstream.aliasField("RecargaElectronica", RetailTransaction.class,
				"recargaElectronica");
		xstream.alias("RecargaElectronica", RecargaElec.class);
		xstream.aliasField("SequenceNumber", RecargaElec.class, "sequenceNumber");
		xstream.aliasField("CodArticulo", RecargaElec.class, "codigoArt");
		xstream.aliasField("Tienda", RecargaElec.class, "tienda");
		xstream.aliasField("Terminal", RecargaElec.class, "terminal");
		xstream.aliasField("Referencia", RecargaElec.class, "referencia");
		xstream.aliasField("Tipo", RecargaElec.class, "tipo");
		xstream.aliasField("Numero", RecargaElec.class, "numero");
		xstream.aliasField("Monto", RecargaElec.class, "monto");
		xstream.aliasField("Autorizacion", RecargaElec.class, "autorizacion");
		xstream.aliasField("HoraTrx", RecargaElec.class, "horaTrx");
		xstream.aliasField("Cancelacion", RecargaElec.class, "cancelacion");
		
		xstream.aliasField("TenderPinpad", RetailTransaction.class,
				"pagosConPinpad");
		xstream.alias("TenderPinpad", TenderPinpadInfo.class);
		xstream.aliasField("SequenceNumber", TenderPinpadInfo.class, "sequenceNumber");
		xstream.aliasField("CodigoAdquiriente", TenderPinpadInfo.class, "codAdquiriente");
		xstream.aliasField("CodDiferido", TenderPinpadInfo.class, "codDiferido");
		xstream.aliasField("PlazoDiferido", TenderPinpadInfo.class, "plazoDiferido");
		xstream.aliasField("MesesGracia", TenderPinpadInfo.class, "mesesGracia");
		xstream.aliasField("Monto", TenderPinpadInfo.class, "montoTrx");
		xstream.aliasField("MontoBaseGrabamenIva", TenderPinpadInfo.class, "montoBaseGrabaIva");
		xstream.aliasField("MontoBaseNoGrabaIva", TenderPinpadInfo.class, "montoBaseNoGrabaIva");
		xstream.aliasField("Interes", TenderPinpadInfo.class, "interes");
		xstream.aliasField("IvaTrx", TenderPinpadInfo.class, "ivaTrx");
		xstream.aliasField("SecuenciaTrx", TenderPinpadInfo.class, "seqTrx");
		xstream.aliasField("HoraTrx", TenderPinpadInfo.class, "horaTrx");
		xstream.aliasField("FechaTrx", TenderPinpadInfo.class, "fechaTrx");
		xstream.aliasField("NumAutorizacion", TenderPinpadInfo.class, "numAutorizacion");
		xstream.aliasField("Mid", TenderPinpadInfo.class, "mid");
		xstream.aliasField("Tid", TenderPinpadInfo.class, "tid");
		xstream.aliasField("Cid", TenderPinpadInfo.class, "cid");
		xstream.aliasField("ValorDif", TenderPinpadInfo.class, "valorDif");
		xstream.aliasField("Arqc", TenderPinpadInfo.class, "arqc");
		xstream.aliasField("Aid", TenderPinpadInfo.class, "aid");
		xstream.aliasField("BankName", TenderPinpadInfo.class, "bankName");
		xstream.aliasField("BrandName", TenderPinpadInfo.class, "brandName");
		
		xstream.aliasField("FerricardData", RetailTransaction.class,
				"ferricardData");
		xstream.alias("FerricardData", FerricardData.class);
		xstream.aliasField("TransactionNumber", FerricardData.class,
				"transactionNumber");
		xstream.aliasField("AlmacenNumber", FerricardData.class,
				"almacenNumber");

		xstream.aliasField("EcommerceData", RetailTransaction.class,
				"ecommerceData");
		xstream.alias("EcommerceData", EcommerceData.class);
		xstream.aliasField("Orden", EcommerceData.class,
				"orden");
		xstream.aliasField("Factura", EcommerceData.class,
				"factura");
		xstream.aliasField("Hora", EcommerceData.class,
				"hora");
		
		xstream.aliasField("DeducibleData", RetailTransaction.class,
				"deducibleData");
		xstream.alias("DeducibleData", DeducibleData.class);
		xstream.aliasField("Comestible", DeducibleData.class,
				"comestible");
		xstream.aliasField("Ropa", DeducibleData.class,
				"ropa");
		xstream.aliasField("Escolar", DeducibleData.class,
				"escolar");
		
		xstream.aliasField("IlimitadaData", RetailTransaction.class,
				"ilimitadaData");
		xstream.alias("IlimitadaData", IlimitadaData.class);
		xstream.aliasField("SequenceNumber", IlimitadaData.class,
				"sequenceNumber");
		xstream.aliasField("Monto", IlimitadaData.class,
				"monto");
		xstream.aliasField("RefNum", IlimitadaData.class,
				"refNum");
		xstream.aliasField("TerminalId", IlimitadaData.class,
				"terminalId");	
		xstream.aliasField("MerchantId", IlimitadaData.class,
				"merchantId");
		xstream.aliasField("AutNum", IlimitadaData.class,
				"autNum");
		xstream.aliasField("IlimData", IlimitadaData.class,
				"ilimData");
		xstream.aliasField("Time", IlimitadaData.class,
				"time");	
		xstream.aliasField("FlVoid", IlimitadaData.class,
				"flVoid");			
		xstream.aliasField("PasswTemporal", RetailTransaction.class,
				"passwTemporal");
		xstream.alias("PasswTemporal", PasswTemporal.class);
		xstream.aliasField("Usuario", PasswTemporal.class,
				"usuario");
		xstream.aliasField("Password", PasswTemporal.class,
				"password");
		
		xstream.aliasField("RetencionData", TenderControlTransaction.class,
				"retencionData");		
		xstream.aliasField("RetencionData", RetailTransaction.class,
				"retencionData");
		xstream.alias("RetencionData", RetencionData.class);
		xstream.aliasField("SequenceNumber", RetencionData.class, "sequenceNumber");
		xstream.aliasField("Indicador", RetencionData.class, "indicador");
		xstream.aliasField("Terminal", RetencionData.class, "terminal");
		xstream.aliasField("Tiquete", RetencionData.class, "tiquete");
		xstream.aliasField("Fecha", RetencionData.class, "fecha");
		xstream.aliasField("Voucher", RetencionData.class, "voucher");
		xstream.aliasField("NumeroSRI", RetencionData.class, "numeroSRI");
		xstream.aliasField("Monto", RetencionData.class, "monto");
		xstream.aliasField("BaseImp", RetencionData.class, "baseImp");
		xstream.aliasField("Porcentaje", RetencionData.class, "porcentaje");
		xstream.aliasField("IdCliente", RetencionData.class, "idCliente");
		xstream.aliasField("Nombre", RetencionData.class, "nombre");
		xstream.aliasField("Tipo", RetencionData.class, "tipo");
		
		xstream.aliasField("RetencionData", TenderControlTransaction.class,
				"retencionData");		
		xstream.aliasField("RetencionData", RetailTransaction.class,
				"retencionData");
		xstream.alias("RetencionData", RetencionData.class);
		xstream.aliasField("SequenceNumber", RetencionData.class, "sequenceNumber");
		xstream.aliasField("Indicador", RetencionData.class, "indicador");
		xstream.aliasField("Terminal", RetencionData.class, "terminal");
		xstream.aliasField("Tiquete", RetencionData.class, "tiquete");
		xstream.aliasField("Fecha", RetencionData.class, "fecha");
		xstream.aliasField("Voucher", RetencionData.class, "voucher");
		xstream.aliasField("NumeroSRI", RetencionData.class, "numeroSRI");
		xstream.aliasField("Monto", RetencionData.class, "monto");
		xstream.aliasField("BaseImp", RetencionData.class, "baseImp");
		xstream.aliasField("Porcentaje", RetencionData.class, "porcentaje");
		xstream.aliasField("IdCliente", RetencionData.class, "idCliente");
		xstream.aliasField("Nombre", RetencionData.class, "nombre");
		xstream.aliasField("Tipo", RetencionData.class, "tipo");
		
		xstream.aliasField("GiftcardDataList", RetailTransaction.class,
				"giftcardData");
		xstream.alias("GiftcardData", GiftcardData.class);
		xstream.aliasField("SequenceNumber", GiftcardData.class, "sequenceNumber");
		xstream.aliasField("Time", GiftcardData.class, "time");
		xstream.aliasField("CardNumber", GiftcardData.class, "cardNumber");
		xstream.aliasField("Amount", GiftcardData.class, "amount");
		xstream.aliasField("ReferenceNbr", GiftcardData.class, "referenceNbr");
		xstream.aliasField("AuthorizationNbr", GiftcardData.class, "authorizationNbr");
		xstream.aliasField("ResponseCode", GiftcardData.class, "responseCode");
		xstream.aliasField("Status", GiftcardData.class, "status");
		
		xstream.aliasField("PointsRedemptionDataList", RetailTransaction.class,
				"pointsRedemptionData");
		xstream.alias("PointsRedemptionData", PointsRedemptionData.class);
		xstream.aliasField("SequenceNumber", PointsRedemptionData.class, "sequenceNumber");
		xstream.aliasField("Time", PointsRedemptionData.class, "time");
		xstream.aliasField("Discount", PointsRedemptionData.class, "discount");
		xstream.aliasField("CodNegocio", PointsRedemptionData.class, "codNegocio");
		xstream.aliasField("ReferenceNbr", PointsRedemptionData.class, "referenceNbr");
		xstream.aliasField("AuthorizationNbr", PointsRedemptionData.class, "authorizationNbr");
		xstream.aliasField("Bin", PointsRedemptionData.class, "bin");
		xstream.aliasField("Puntos", PointsRedemptionData.class, "puntos");
		xstream.aliasField("CodigoItem", PointsRedemptionData.class, "codItem");
		
		xstream.aliasField("ManagerOverrides", RetailTransaction.class,
				"managerOverrides");
		xstream.aliasField("ManagerOverrides", ControlTransaction.class,
				"managerOverrides");
		xstream.alias("ManagerOverride", ManagerOverride.class);
		xstream.aliasField("SequenceNumber", ManagerOverride.class,
				"sequenceNumber");
		xstream.aliasField("Number", ManagerOverride.class, "number");
		xstream.aliasField("Reason", ManagerOverride.class, "reason");
		xstream.aliasField("Index", ManagerOverride.class, "index");
		xstream.aliasField("initials", ManagerOverride.class, "initials");
		xstream.aliasField("InvoiceData", RetailTransaction.class,
				"invoiceData");
		xstream.alias("InvoiceData", InvoiceData.class);
		xstream.aliasField("CustomerID", InvoiceData.class,
				"customerID");
		xstream.aliasField("CustomerName", InvoiceData.class,
				"customerName");
		xstream.aliasField("Address", InvoiceData.class,
				"address");
		xstream.aliasField("Telephone", InvoiceData.class,
				"telephone");
		xstream.aliasField("FacturaElec", RetailTransaction.class,
				"facturaElec");
		xstream.alias("FacturaElec", FacturaElec.class);
		xstream.aliasField("Fecha", FacturaElec.class,
				"fecha");
		xstream.aliasField("Hora", FacturaElec.class,
				"hora");
		xstream.aliasField("NumeroDoc", FacturaElec.class,
				"numeroDoc");
		xstream.aliasField("TipoDoc", FacturaElec.class,
				"tipoDoc");
		xstream.aliasField("Estado", FacturaElec.class,
				"estado");
		xstream.aliasField("NumeroFac", FacturaElec.class,
				"numeroFac");
		xstream.aliasField("SubTotal", FacturaElec.class,
				"subTotal");
		xstream.aliasField("Total", FacturaElec.class,
				"total");
		xstream.aliasField("NotaCredito", RetailTransaction.class,
				"notaCredito");
		xstream.alias("NotaCredito", ReturnTransaction.class);
		xstream.aliasField("NumeroNotaCredito", ReturnTransaction.class,
				"numeroNotaCredito");
		xstream.aliasField("NumeroDocOriginal", ReturnTransaction.class,
				"numeroDocOriginal");
		xstream.aliasField("NumeroFacOriginal", ReturnTransaction.class,
				"numeroFac");
		xstream.aliasField("NroTiendaOriginal", ReturnTransaction.class,
				"nroTiendaOriginal");
		xstream.aliasField("Tipo", ReturnTransaction.class,
				"tipo");
		xstream.aliasField("FechaContOrig", ReturnTransaction.class,
				"fechaContOrig");
		xstream.aliasField("PosOrig", ReturnTransaction.class,
				"posOrig");
		xstream.aliasField("OperadorSuite", ReturnTransaction.class,
				"operadorSuite");
		xstream.aliasField("Supervisor", ReturnTransaction.class,
				"supervisor");
		xstream.aliasField("GastoEfectivo", TenderControlTransaction.class,
				"gastoEfectivo");
		xstream.alias("GastoEfectivo", GastoEfectivo.class);
		xstream.aliasField("Cedula", GastoEfectivo.class,
				"cedula");
		xstream.aliasField("Almacen", GastoEfectivo.class,
				"almacen");
		xstream.aliasField("Fecha", GastoEfectivo.class,
				"fecha");
		xstream.aliasField("Comprobante", GastoEfectivo.class,
				"comprobante");
		xstream.aliasField("Seccion", GastoEfectivo.class,
				"seccion");
		xstream.aliasField("Codigo", GastoEfectivo.class,
				"codigo");
		xstream.aliasField("Tipo", GastoEfectivo.class,
				"tipo");
		xstream.aliasField("Cantidad", GastoEfectivo.class,
				"cantidad");
		xstream.aliasField("Valor", GastoEfectivo.class,
				"valor");
		xstream.aliasField("Observacion", GastoEfectivo.class,
				"observacion");
		
		xstream.aliasField("ReverseLineItemInfo", TenderControlTransaction.class,
				"rvItemInfo");
		xstream.alias("ReverseLineItemInfo", ReverseLineItemInfo.class);
		xstream.aliasField("NotaCredito", ReverseLineItemInfo.class,
				"numNc");
		xstream.aliasField("LineItem", ReverseLineItemInfo.class,
				"lineItem");
		xstream.aliasField("Monto", ReverseLineItemInfo.class,
				"monto");
		xstream.aliasField("Autorizacion", ReverseLineItemInfo.class,
				"numAuto");
		
		xstream.aliasField("ValeEmpleado", TenderControlTransaction.class,
				"valeEmpleado");
		xstream.alias("ValeEmpleado", ValeEmpleado.class);
		xstream.aliasField("Cedula", ValeEmpleado.class,
				"cedula");
		xstream.aliasField("Comprobante", ValeEmpleado.class,
				"comprobante");
		xstream.aliasField("CodSociedadSAP", ValeEmpleado.class,
				"codSocSap");
		xstream.aliasField("Codigo", ValeEmpleado.class,
				"codigo");
		xstream.aliasField("Cuotas", ValeEmpleado.class,
				"cuotas");
		xstream.aliasField("Valor", ValeEmpleado.class,
				"valor");
		xstream.aliasField("Observacion", ValeEmpleado.class,
				"observacion");
		xstream.aliasField("ExceptionLogs", RetailTransaction.class,
				"exceptionLogs");
		xstream.alias("ExceptionLog", ExceptionLog.class);
		xstream.aliasField("SequenceNumber", ExceptionLog.class,
				"sequenceNumber");
		xstream.aliasField("Data", ExceptionLog.class, "data");
		xstream.aliasField("AssociatedCouponsToRedemptions", RetailTransaction.class,
				"associatedCouponsToRedemptions");
		xstream.alias("AssociatedCoupon", AssociatedCoupon.class);
		xstream.aliasField("SequenceNumber", AssociatedCoupon.class,
				"sequenceNumber");
		xstream.aliasField("Coupon", AssociatedCoupon.class,
				"coupon");
		xstream.alias("CouponToRedemption", CouponToRedemption.class);
		xstream.aliasField("CouponCode", CouponToRedemption.class, "couponCode");
		xstream.aliasField("TimeStamp", CouponToRedemption.class, "timeStamp");
		xstream.aliasField("Status", CouponToRedemption.class, "status");
		xstream.aliasField("FechaInicial", CouponToRedemption.class, "fechaInicial");
		xstream.aliasField("FechaExpiracion", CouponToRedemption.class, "fechaExpiracion");
		xstream.aliasField("MaxRedemptions", CouponToRedemption.class, "maxRedemptions");
		xstream.aliasField("NRedemptions", CouponToRedemption.class, "nRedemptions");
		xstream.aliasField("LastRedemption", CouponToRedemption.class, "lastRedemption");
		xstream.aliasField("PercentOff", CouponToRedemption.class, "percentOff");
		xstream.addImplicitCollection(RetailTransaction.class, "lineItems");
		xstream.alias("LineItem", RetailTransactionLineItem.class);
		xstream.aliasAttribute(RetailTransactionLineItem.class, "voidFlag",
				"VoidFlag");
		xstream.aliasAttribute(RetailTransactionLineItem.class, "itemTypeCode",
				"TypeCode");
		xstream.aliasField("SequenceNumber", RetailTransactionLineItem.class,
				"sequenceNumber");
		xstream.aliasField("ItemLink", RetailTransactionLineItem.class,
				"voidLine");
		xstream.aliasField("BeginDateTime", RetailTransactionLineItem.class,
				"beginDateTimeString");
		xstream.aliasField("EndDateTime", RetailTransactionLineItem.class,
				"endDateTimeString");
		xstream.aliasField("Sale", RetailTransactionLineItem.class, "saleLI");
		xstream.aliasField("Return", RetailTransactionLineItem.class,
				"returnLI");
		xstream.aliasField("POSItemID", SaleReturnLineItem.class, "posItemID");
		xstream.aliasField("ItemCode", SaleReturnLineItem.class, "itemCode");
		xstream.aliasField("ItemType", SaleReturnLineItem.class, "itemType");
		xstream.aliasField("MerchandiseHierarchyGroup", SaleReturnLineItem.class,
				"merchandiseHierarchyGroupCode");
		xstream.aliasField("POSDepartment", SaleReturnLineItem.class,
				"posDepartmentCode");
		xstream.aliasField("TaxType", SaleReturnLineItem.class,
				"taxType");
		xstream.aliasField("RegularSalesUnitPrice", SaleReturnLineItem.class,
				"regularSalesUnitPrice");
		xstream.aliasField("ExtendedAmount", SaleReturnLineItem.class,
				"extendedAmount");
		xstream.aliasField("Quantity", SaleReturnLineItem.class, "quantity");
		xstream.aliasField("EntryMethodCode", SaleReturnLineItem.class,
				"entryMethodCode");
		xstream.aliasField("ActualSalesUnitPrice", SaleReturnLineItem.class,
				"actualUnitPrice");
		xstream.aliasField("PriceEntered", SaleReturnLineItem.class,
				"priceEntered");
		xstream.aliasField("IsPromo", SaleReturnLineItem.class,
				"isPromo");
		xstream.aliasField("OrdinalNumber", SaleReturnLineItem.class,
				"ordinalNumber");
		xstream.aliasField("IsPorMayor", SaleReturnLineItem.class,
				"isPorMayor");
		xstream.aliasField("IsPorRedencion", SaleReturnLineItem.class,
				"isPorRedencion");
		xstream.aliasField("Tender", RetailTransactionLineItem.class, "tender");
		xstream.aliasAttribute(TenderLineItem.class, "tenderTypeCode",
				"TenderType");
		xstream.aliasAttribute(TenderLineItem.class, "typeCode", "TypeCode");
		xstream.aliasAttribute(TenderLineItem.class, "isChangeFlag", "ChangeFlag");
		xstream.aliasField("TenderAccountNumber", TenderLineItem.class,
				"tenderAccountNumber");
		xstream.aliasField("Amount", TenderLineItem.class, "amount");
		xstream.aliasField("ForeignCurrencyID", TenderLineItem.class,
				"foreignCurrencyID");
		xstream.aliasField("ForeignCurrencyAmount", TenderLineItem.class,
				"foreignCurrencyAmount");
		xstream.aliasField("ExchangeRate", TenderLineItem.class, "exchangeRate");
		xstream.aliasField("FeeAmount", TenderLineItem.class, "feeAmount");
		xstream.aliasField("Status", TenderLineItem.class, "status");
		xstream.aliasField("TenderReturnLineItem", TenderLineItem.class, "tenderReturnLineItem");

		xstream.aliasField("SequenceNumber", TenderReturnLineItem.class,
				"sequenceNumber");
		xstream.aliasField("Status", TenderReturnLineItem.class, "status");
		
		xstream.aliasField("TenderChange", RetailTransactionLineItem.class,
				"tenderChange");
		xstream.aliasField("Discount", RetailTransactionLineItem.class,
				"priceModification");
		xstream.aliasField("Percentage", PriceModificationLineItem.class,
				"percentage");
		xstream.aliasField("Amount", PriceModificationLineItem.class, "amount");
		xstream.aliasAttribute(PriceModificationLineItem.class,
				"priceModificationTypeCode", "DiscountTypeCode");
		xstream.aliasField("Tax", RetailTransactionLineItem.class, "tax");
		xstream.aliasField("TaxableAmount", TaxLineItem.class, "taxableAmount");
		xstream.aliasField("Amount", TaxLineItem.class, "taxAmount");
		xstream.aliasAttribute(TaxLineItem.class, "taxType", "TaxType");
		xstream.aliasField("MiscellaneousFee", RetailTransactionLineItem.class,
				"miscellaneousFee");
		xstream.aliasField("ItemID", MiscellaneousFeeLineItem.class, "itemID");
		xstream.aliasField("Amount", MiscellaneousFeeLineItem.class, "amount");
		// configuracion de TenderControlTransaction
		xstream.aliasField("TenderControlTransaction", Transaction.class,
				"tenderControlTransaction");
		xstream.aliasAttribute(TenderControlTransaction.class,
				"tenderControlTypeCode", "TenderControlTypeCode");
		xstream.aliasField("TenderPickup", TenderControlTransaction.class,
				"pickup");
		xstream.aliasField("TenderLoan", TenderControlTransaction.class, "loan");
		xstream.aliasField("TenderDeposit", TenderControlTransaction.class,
				"deposit");
		xstream.aliasField("TenderReceipt", TenderControlTransaction.class,
				"receipt");
		xstream.aliasField("TenderCount", TenderControlTransaction.class,
				"count");
		xstream.addImplicitCollection(TenderControlTransaction.class,
				"lineItems");
		xstream.aliasField("Inbound", TenderPickupTransaction.class, "inbound");
		xstream.aliasField("Outbound", TenderPickupTransaction.class,
				"outbound");
		xstream.aliasField("OperatorCode", TenderPickupTransaction.class,
				"operatorCode");
		xstream.aliasField("Outbound", TenderLoanTransaction.class, "outbound");
		xstream.aliasField("Inbound", TenderLoanTransaction.class, "inbound");
		xstream.aliasField("OperatorCode", TenderLoanTransaction.class,
				"operatorCode");
		xstream.alias("TenderControlTransactionTenderLineItem",
				TenderControlTransactionLineItem.class);
		xstream.aliasField("SafeID", TenderReceiptTransaction.class, "safeID");
		xstream.aliasField("ExternalDepositoryID",
				TenderReceiptTransaction.class, "externalDepositoryID");
		xstream.aliasField("SafeID", TenderDepositTransaction.class, "safeID");
		xstream.aliasField("ExternalDepositoryID",
				TenderDepositTransaction.class, "externalDepositoryID");
		xstream.aliasField("SequenceNumber",
				TenderControlTransactionLineItem.class, "sequenceNumber");
		xstream.aliasField("Amount", TenderControlTransactionLineItem.class,
				"amount");
		xstream.aliasField("TenderTypeCode",
				TenderControlTransactionLineItem.class, "tenderTypeCode");
		xstream.aliasField("Count", TenderControlTransactionLineItem.class,
				"count");
		xstream.aliasField("Denomination",
				TenderControlTransactionLineItem.class, "denominationID");
		// configuracion de ControlTransaction
		xstream.aliasField("ControlTransaction", Transaction.class,
				"controlTransaction");
		xstream.aliasAttribute(ControlTransaction.class, "typeCode", "TypeCode");
		xstream.aliasField("SignOn", ControlTransaction.class, "signOn");
		xstream.aliasField("SignOff", ControlTransaction.class, "signOff");
		xstream.aliasField("BusinessEOD", ControlTransaction.class, "businessEOD");
		xstream.aliasField("CarryForward", ControlTransaction.class, "carryForward");

		xstream.aliasField("AutomaticCouponData", RetailTransaction.class,
				"automaticCouponData");
		xstream.aliasField("MfgCouponAmount", AutomaticCouponData.class,
				"mfgCouponAmount");
		xstream.aliasField("StoreCouponAmount", AutomaticCouponData.class,
				"storeCouponAmount");
		xstream.aliasField("MfgCouponCount", AutomaticCouponData.class,
				"mfgCouponCount");
		xstream.aliasField("StoreCouponCount", AutomaticCouponData.class,
				"storeCouponCount");
		xstream.aliasField("BonusRedemptionPointsList",
				RetailTransaction.class, "bonusRedempPtsList");
		xstream.alias("BonusRedemptionPoints", BonusRedemptionPoints.class);
		xstream.aliasField("SequenceNumber", BonusRedemptionPoints.class,
				"sequenceNumber");
		xstream.aliasField("ItemCode", BonusRedemptionPoints.class, "itemCode");
		xstream.aliasField("Value", BonusRedemptionPoints.class, "value");
		xstream.aliasField("MoreFlags", BonusRedemptionPoints.class,
				"moreFlags");
		xstream.aliasField("Families", BonusRedemptionPoints.class, "families");
		xstream.aliasField("UsedTargetedCoupons", RetailTransaction.class,
				"usedTargetedCoupons");
		xstream.aliasField("CustomerAccountID", AutomaticCouponData.class,
				"customerAccountID");
		xstream.aliasField("TargetedCoupon", AutomaticCouponData.class,
				"targetedCoupon");
		xstream.aliasField("CouponTracking", RetailTransaction.class,
				"couponTracking");
		xstream.aliasField("LogFlags", AutomaticCouponData.class, "logFlags");
		xstream.aliasField("CampaignNumber", AutomaticCouponData.class,
				"campaignNumber");
		xstream.aliasField("MfgNumber", AutomaticCouponData.class, "mfgNumber");
		xstream.aliasField("PromotionCode", AutomaticCouponData.class,
				"promotionCode");
		xstream.aliasField("PreferredCustomerSecPtsData",
				RetailTransaction.class, "preferredCustSecPtsData");
		xstream.aliasField("ClubNumber", PreferredCustomerSecPtsData.class,
				"clubNumber");
		xstream.aliasField("Points", PreferredCustomerSecPtsData.class,
				"points");
		xstream.aliasField("RedeemedPoints", PreferredCustomerSecPtsData.class,
				"redeemedPoints");
		xstream.aliasField("BonusPoints", PreferredCustomerSecPtsData.class,
				"bonusPoints");
		xstream.aliasField("Sales", PreferredCustomerSecPtsData.class, "sales");
		xstream.aliasField("PreferredCustomerData", RetailTransaction.class,
				"preferredCustData");
		xstream.aliasField("CustomerAccountID", PreferredCustomerData.class,
				"customerAccountID");
		xstream.aliasField("Points", PreferredCustomerData.class, "points");
		xstream.aliasField("CouponAmount", PreferredCustomerData.class,
				"couponAmount");
		xstream.aliasField("CouponCount", PreferredCustomerData.class,
				"couponCount");
		xstream.aliasField("MessageCount", PreferredCustomerData.class,
				"messageCount");
		xstream.aliasField("TransferredTransCount",
				PreferredCustomerData.class, "transferredTransCount");
		xstream.aliasField("BonusPoints", PreferredCustomerData.class,
				"bonusPoints");
		xstream.aliasField("RedeemedPoints", PreferredCustomerData.class,
				"redeemedPoints");
		xstream.aliasField("EntryMethod", PreferredCustomerData.class,
				"entryMethod");
		xstream.aliasField("AliasElecCoupICList", RetailTransaction.class,
				"aliasElecCoupICList");
		xstream.alias("AliasElecCouponItemCode", AliasElecCouponItemCode.class);
		xstream.aliasField("SequenceNumber", AliasElecCouponItemCode.class,
				"sequenceNumber");
		xstream.aliasField("ItemCode", AliasElecCouponItemCode.class,
				"itemCode");
		xstream.aliasField("CouponsDataList", RetailTransaction.class,
				"couponsDataList");
		xstream.alias("StringUsuario", StringUsuario.class);
		xstream.aliasField("SequenceNumber", StringUsuario.class,
				"sequenceNumber");
		xstream.aliasField("Cadena", StringUsuario.class,
				"cadena");
		xstream.aliasField("StringUsuariosDataList", RetailTransaction.class,
				"stringsUsuario");
		xstream.alias("CouponData", CouponData.class);
		xstream.aliasField("PromoID", CouponData.class,
				"promoID");
		xstream.aliasField("CustomerID", CouponData.class,
				"customerID");
		xstream.aliasField("FormatID", CouponData.class,
				"formatID");
		xstream.aliasField("Impreso", CouponData.class,
				"impreso");
		xstream.aliasField("ItemCode", CouponData.class,
				"itemCode");
		xstream.aliasField("Barcode", CouponData.class,
				"barcode");
		xstream.aliasField("ValueDisc", CouponData.class,
				"valueDisc");
		xstream.aliasField("Apply", CouponData.class,
				"apply");
		xstream.aliasField("InitDate", CouponData.class,
				"initDateString");
		xstream.aliasField("ExpDate", CouponData.class,
				"expDateString");
		xstream.aliasField("Separator", CouponData.class,
				"separator");
		xstream.aliasField("LogoID", CouponData.class,
				"logoID");
		xstream.aliasField("Estado", CouponData.class,
				"estado");
		xstream.aliasField("RedeemedCouponsDataList", RetailTransaction.class,
				"redeemedcouponsDataList");
		xstream.alias("RedeemedCouponData", RedeemedCouponData.class);
		xstream.aliasField("Barcode", RedeemedCouponData.class,
				"barcode");
		xstream.aliasField("ValueDisc", RedeemedCouponData.class,
				"valueDisc");
		xstream.aliasField("Timestamp", RedeemedCouponData.class,
				"timestamp");
		xstream.aliasField("TransactionNumber", RedeemedCouponData.class,
				"transactionNumber");
		xstream.aliasField("Workstation", RedeemedCouponData.class,
				"workstation");
		xstream.aliasField("Store", RedeemedCouponData.class,
				"store");

	}

	/**
	 * @return the xstream
	 */
	public XStream getXstream() {
		return xstream;
	}

	/**
	 * @param xstream
	 *            the xstream to set
	 */
	public void setXstream(XStream xstream) {
		this.xstream = xstream;
	}

}
