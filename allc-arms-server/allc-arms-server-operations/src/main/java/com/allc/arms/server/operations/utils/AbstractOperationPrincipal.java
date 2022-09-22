package com.allc.arms.server.operations.utils;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.server.Server;
import com.allc.arms.server.persistence.server.ServerDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import java.math.BigInteger;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

public abstract class AbstractOperationPrincipal extends AbstractOperation{
	private static final Logger LOG = Logger.getLogger(AbstractOperationPrincipal.class.getName());
    
    private final PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    
    private Session sessionSaadmin = null;
    
    private void iniciarSaadminSesion() {
        while (sessionSaadmin == null) {
            try {
                sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
            } catch (HibernateException e) {
                LOG.error(e.getMessage(), e);
            }
            if (sessionSaadmin == null) {
                try {
                    LOG.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }
    
    public boolean isPrincipal() {
        try {
            iniciarSaadminSesion();
            BigInteger idBsnGp = prop.getObject("eyes.store.code.group") == null ? BigInteger.ZERO
                    : new BigInteger(prop.getObject("eyes.store.code.group")); // server regional

            ServerDAO serverDAO = new ServerDAO();
            Server serverActual = serverDAO.getServer(sessionSaadmin, idBsnGp);
            if (serverActual != null) {
                return serverActual.isPrimario_app();
            }
            
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            sessionSaadmin.close();
            sessionSaadmin = null;
        }
        LOG.info("No -server actual");
        return false;
    }

    public String hayServidorRegional(Integer codTienda) {
        try {
            iniciarSaadminSesion();
            Query query = this.sessionSaadmin.createSQLQuery("select ph.ip from mn_srv_str sl join MN_SRV ph on sl.id_nodo = ph.id_nodo where ph.estado = 1 and ph.primario_app is true and ph.regional is true and id_local = '" + codTienda + "'");
            List<String> rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows.get(0);
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            sessionSaadmin.close();
            sessionSaadmin = null;
        }
        return null;
        
    }
}
