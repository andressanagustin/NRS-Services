/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.interfaces;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.item.ItemTmp;
import static com.allc.arms.server.processes.interfaces.InicioProceso.log;
import com.allc.arms.server.processes.utils.AbstractProcessPrincipal;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.properties.PropFile;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 *
 * @author Tyrone Lopez
 */
public class UpdateItemTemporalProceso extends AbstractProcessPrincipal {

    private static final Logger LOGGER = Logger.getLogger(UpdateItemTemporalProceso.class.getName());

    private final PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    private Session sessionArtsEc = null;
    private boolean finished = false;
    private boolean isEnd = false;

    private void iniciarArtsEcSesion() {
        while (sessionArtsEc == null && !isEnd) {
            try {
                sessionArtsEc = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            if (sessionArtsEc == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void run() {

        while (!isEnd) {
            if (isPrincipal()) {
                finished = false;
                LOGGER.info("Consultando temporales ---- ");
                iniciarArtsEcSesion();
                RepositorioSQL sql = new RepositorioSQL(sessionArtsEc);

                List<ItemTmp> consulta_as_itm_tmp = sql.consulta_all_as_itm_tmp();
                consulta_as_itm_tmp.stream().map((itemTmp) -> {
                    LOGGER.info("Registro Temporal --- " + itemTmp.getIdItm());
                    return itemTmp;
                }).map((itemTmp) -> {
                    if (itemTmp.getStock() != null) {
                        try {
                            sql.update_as_itm_str_stock_tmp(itemTmp.getIdItm(), itemTmp.getIdBsnUn(), itemTmp.getStock().floatValue());
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                    return itemTmp;
                }).filter((itemTmp) -> (StringUtils.isNotBlank(itemTmp.getImagen()) && StringUtils.isNotBlank(itemTmp.getImagenSm()))).forEachOrdered((itemTmp) -> {
                    try {
                        sql.borra_as_itm_imagen_registro(itemTmp.getIdItm());
                        if (!sql.inserta_as_itm_imagen2(1, itemTmp.getIdItm(), itemTmp.getImagen(), itemTmp.getImagenSm(), 1, 1)) {
                            throw new Exception("Error al ingresar la imagen del item en la tabla AS_ITM_IMAGEN");
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
                sessionArtsEc.close();
                sessionArtsEc = null;
            } else {
                LOGGER.info("No Consulo temporales ---- No es principal");
            }
            try {
                LOGGER.info("Duermo: " + this.prop.getLong("interface.item.update.timesleep"));
                Thread.sleep(this.prop.getLong("interface.item.update.timesleep"));
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
            finished = true;
        }
    }

    @Override
    public boolean shutdown(long timeToWait) {
        isEnd = true;
        //closeConnection();
        long startTime = Calendar.getInstance().getTimeInMillis();
        log.info("Deteniendo Interfaces de carga de maestro de items ...");
        while (!finished) {
            try {
                long dif = Calendar.getInstance().getTimeInMillis() - startTime;
                if (dif >= timeToWait) {
                    return false;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        log.info("Finalizó el Proceso de actualizacion de items temporales.");
        return true;
    }

}
