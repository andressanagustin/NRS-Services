/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.utils.stockpile;

import com.allc.arms.utils.keyed.KeyedFileBean;
import com.allc.arms.utils.keyed.KeyedFileMethods;
import com.allc.properties.PropFile;
import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.KeyedFile;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Tyrone Lopez
 */
public class ItemKeyed {

    protected static Logger log = Logger.getLogger(ItemKeyed.class);
    protected KeyedFileBean keyedFileBean = new KeyedFileBean();
    protected PropFile properties;

    public boolean init(PropFile properties) {
        boolean result = false;
        try {
            this.properties = properties;
            keyedFileBean.setPathAndFileName("C:/ADX_IDT1/MAEITEMS.DAT");
            keyedFileBean.setMode("r");
            keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
            keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
            keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
            keyedFileBean.setKeyLength(7);
            keyedFileBean.setRecordSize(39);
            if (KeyedFileMethods.openFile(keyedFileBean)) {
                log.info("MAEITEMS -- Abierto");
                result = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public List readItems() {
        if (keyedFileBean.getKeyedFile() == null) {
            return null;
        }
        try {
            log.info("Leyendo items");
            int recordSize = keyedFileBean.getKeyedFile().getRecordSize();

            List<ItemBean> values = new ArrayList();
            byte[] operatorRecord = new byte[recordSize];
            for (byte b : operatorRecord) {
                log.info(b);

            }
            return values;
        } catch (FlexosException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public boolean closure() {
        boolean result = false;
        try {
            KeyedFileMethods.closeFile(keyedFileBean);
            result = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;

    }
}
