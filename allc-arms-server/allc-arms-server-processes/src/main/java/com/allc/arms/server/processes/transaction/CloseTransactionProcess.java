package com.allc.arms.server.processes.transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.processes.utils.AbstractProcessPrincipal;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.mail.EmailSender;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import java.util.*;

public class CloseTransactionProcess extends AbstractProcessPrincipal {

    protected static Logger log = Logger.getLogger(CloseTransactionProcess.class);
    protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    private boolean threadEnable = true;
    protected Session session = null;
    private Session sessionSaadmin = null;

    private void iniciarSaadminSesion() {
		while (sessionSaadmin == null) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionSaadmin == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
    
    @Override
    public void run() {
        while (threadEnable) {
            if(isPrincipal()){
            try {
                int hourProperties = properties.getInt("hour.proccess.closeTX");
                int hour = GregorianCalendar.getInstance().get(Calendar.HOUR_OF_DAY);

                //between hour.proccess.closeTX and hour.proccess.closeTX + 1
                if (hour >= hourProperties && hour < hourProperties + 1 && isPrincipal()) {


                    log.info("Obteniendo Tiendas sin TX final.");
                    List<Object[]> tiendasSinTxFinal = consultarTiendas();

                    log.info("TIENDAS SIN TX FINAL: " + tiendasSinTxFinal.size());

                    StringJoiner joiner = new StringJoiner("\n");

                    tiendasSinTxFinal.stream().map((tienda) -> {
                        joiner.add(tienda[2] + " - " + (String) tienda[1]);
                        return tienda;
                    }).forEachOrdered((tienda) -> {
                        //Notificacion EYES
                        UtilityFile.createWriteDataFile(getEyesFileName(),
                                "OPER_ALERTA_TX_CIERRE|" +
                                        properties.getHostName() + "|3|" +
                                        properties.getHostAddress()
                                        + "|" + tienda[0] + "|STR|"
                                        + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
                                        + "| Alerta Tx Cierre Tienda: " + tienda[1] + " .\n",
                                true);
                    });


                    if (!tiendasSinTxFinal.isEmpty()) {

                        EmailSender.sendMail(
                                properties.getObject("smtp.user"),
                                properties.getObject("smtp.password"),
                                properties.getObject("smtp.server"),
                                properties.getObject("smtp.user"),
                                properties.getObject("administrator.alert.mail.to"),
                                "Alerta Transacción cierre tienda.",
                                "Las tiendas listadas a continuación no registraron transacción de cierre" +
                                        " en el dia de la fecha:  \n" + joiner.toString()
                        );
                    }

                    Thread.sleep(1000 * 60 * 60); //1 minute

                } else {
                    Thread.sleep(1000 * 60 * 60); //1 minute
                    log.info("CloseTransactionProcess sleepTime... ");
                }
            } catch (Exception ex) {
                log.error("Error al Verificar Transacciones finales de tienda:", ex);
            }
        }
        }
    }
    
    private String getEyesFileName() {
        return properties.getObject("eyes.ups.file.name") + "_"
                + ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    public List<Object[]> consultarTiendas() {


        iniciarSesion();

        String consulta =
                "SELECT " +
                        "        cod_tienda, " +
                        "        des_tienda,  " +
                        "        des_clave  " +
                        "FROM saadmin.mn_tienda " +
                        "WHERE cod_tienda NOT IN ( " +

//                "--RECUPERO LAS QUE TIENEN TX FINAL DEL DIA ANTERIOR " +

                        "    SELECT distinct cod_tienda " +
                        "    FROM arts_ec.tr_bsn_eod TED " +
                        "             INNER JOIN arts_ec.tr_trn Tx on TED.id_trn = Tx.id_trn " +
                        "             INNER JOIN arts_ec.pa_str_rtl Pa on Tx.id_bsn_un = Pa.id_bsn_un " +
                        "             INNER JOIN saadmin.mn_tienda Ti on Pa.cd_str_rt = Ti.des_clave " +
                        "    WHERE ind_activo = 1 " +
                        "      AND CAST(Tx.ts_trn_bgn AS DATE) = CAST(current_timestamp - interval '24 hours' AS DATE)) " +
                        "AND ind_activo =1";

        try {

            session.beginTransaction();
            SQLQuery query = session.createSQLQuery(consulta);
            List<Object[]> rows = query.list();
            session.getTransaction().commit();

            return rows;

        } catch (Exception ex) {
            log.error("Error al Verificar Transacciones finales de tienda:", ex);
            if (session != null && session.getTransaction() != null &&
                    session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }

        } finally {

            if (session != null) {
                session.close();
                session = null;
            }

        }

        return null;


    }


    protected void iniciarSesion() {

        while (session == null && threadEnable) {

            try {
                session = HibernateSessionFactoryContainer
                        .getSessionFactory("Saadmin")
                        .openSession();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            }
            if (session == null)
                try {
                    log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
        }
    }

    @Override
    public boolean shutdown(long l) {
        threadEnable = false;
        return false;
    }
}
