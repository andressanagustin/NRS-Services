package com.allc.arms.utils.tsl;


import com.allc.entities.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TSLTransactionMethodsTest {


    @Test
    public void testString99() {

        Transaction transaction = new Transaction();
        RetailTransaction retailTransaction = new RetailTransaction();
        RetailTransactionLineItem item = new RetailTransactionLineItem();
        SaleReturnLineItem sItem = new SaleReturnLineItem();


        sItem.setPosItemID(786210203086L);

        item.setSequenceNumber(123123);
        item.setSaleLI(sItem);

        retailTransaction.setLineItems(Arrays.asList(item));

        transaction.setRetailTransaction(retailTransaction);

        //test one line and one item
        //99|9021|786210203086|1|239602
        // 00000000012592170000000001259220000000000125922800000000012601010000000001,
        //99|9021|000000260445|1|
        // 100706
        // 000005 / 100
        //
        //
        // 0001

        String data = "99|9021|786210203086|1|23960200000000012592170000000001259220000000000125922800000000012601010000000001";

        TSLTransactionMethods tslTransactionMethods = new TSLTransactionMethods();

        tslTransactionMethods.transactionProcess(
                transaction,
                data,
                null
        );

        Assertions.assertEquals(5, transaction.getRetailTransaction().getPromotionDiscs().size());

    }


    @Test
    public void testString99_bad_barcode() {

        Transaction transaction = new Transaction();
        RetailTransaction retailTransaction = new RetailTransaction();
        RetailTransactionLineItem item = new RetailTransactionLineItem();
        SaleReturnLineItem sItem = new SaleReturnLineItem();

        sItem.setPosItemID(786210203086L);
        item.setSequenceNumber(123123);
        item.setSaleLI(sItem);

        retailTransaction.setLineItems(Arrays.asList(item));

        transaction.setRetailTransaction(retailTransaction);

        String data = "99|9021|000000260445|1|1007060000050001";

        TSLTransactionMethods tslTransactionMethods = new TSLTransactionMethods();

        tslTransactionMethods.transactionProcess(
                transaction,
                data,
                null
        );

        Assertions.assertEquals(0, transaction.getRetailTransaction().getPromotionDiscs().size());

    }

    @Test
    public void testString99_singleLine() {

        Transaction transaction = new Transaction();
        RetailTransaction retailTransaction = new RetailTransaction();
        RetailTransactionLineItem item = new RetailTransactionLineItem();
        SaleReturnLineItem sItem = new SaleReturnLineItem();

        sItem.setPosItemID(260445L);
        item.setSequenceNumber(123123);
        item.setSaleLI(sItem);

        retailTransaction.setLineItems(Arrays.asList(item));

        transaction.setRetailTransaction(retailTransaction);

        String data = "99|9021|000000260445|1|1007060000050001";

        TSLTransactionMethods tslTransactionMethods = new TSLTransactionMethods();

        tslTransactionMethods.transactionProcess(
                transaction,
                data,
                null
        );

        Assertions.assertEquals(1, transaction.getRetailTransaction().getPromotionDiscs().size());
        Assertions.assertEquals(5.0D,
                ((PromotionDiscount) transaction.getRetailTransaction().getPromotionDiscs().get(0)).getAmount());
        Assertions.assertEquals("100706",
                ((PromotionDiscount) transaction.getRetailTransaction().getPromotionDiscs().get(0)).getPromotionCode());
        Assertions.assertFalse(
                ((PromotionDiscount) transaction.getRetailTransaction().getPromotionDiscs().get(0)).getFlvd());
        Assertions.assertEquals(1,
                ((PromotionDiscount) transaction.getRetailTransaction().getPromotionDiscs().get(0)).getPromotionSequenceNumber());

    }
}