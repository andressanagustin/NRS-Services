package com.allc.arms.server.persistence.server;

import java.math.BigInteger;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ServerDAO {

    static Logger log = Logger.getLogger(ServerDAO.class);

    public Server getServer(Session session, BigInteger code) throws Exception {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.server.Server where id_bsn_un_gp = '" + code + "' ");
            List rows = query.list();
            return (rows != null && !rows.isEmpty()) ? (Server) rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Server getServerxNodo(Session session, String strNodo) throws Exception {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.server.Server where nodo = '" + strNodo + "' ");
            List rows = query.list();
            return (rows != null && !rows.isEmpty()) ? (Server) rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Server getServerxNodoPrincipal(Session session, int nodo) throws Exception {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.server.Server where idNodo = " + nodo + " and primario_app is true ");
            List rows = query.list();
            return (rows != null && !rows.isEmpty()) ? (Server) rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Server getServerxIp(Session session, String ip) throws Exception {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.server.Server where ip = '" + ip + "'");
            List rows = query.list();
            return (rows != null && !rows.isEmpty()) ? (Server) rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void updateServer(Session session, Server server) throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(server);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
}
