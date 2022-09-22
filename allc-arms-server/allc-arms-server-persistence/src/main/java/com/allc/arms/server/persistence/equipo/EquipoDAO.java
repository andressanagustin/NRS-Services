/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.persistence.equipo;

import java.beans.Statement;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Date;
import java.util.LinkedList;

/**
 *
 * @author Tyrone Lopez
 */
public class EquipoDAO {

    static Logger log = Logger.getLogger(EquipoDAO.class);

    public List getAllEquiposByIdStore(Session sesion, Integer idStore) {
    	try {
            Query query = sesion.createQuery("from com.allc.arms.server.persistence.equipo.Equipo e where e.idLocal = " + idStore );
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return new ArrayList();
    }
    
    public Equipo findOnlineByIdStoreAndDesClave(Session session, Integer idStore, String desClave) {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Equipo e where e.idLocal = " + idStore + " and e.desClave = '" + desClave + "'");
            log.info(query.getQueryString());
            List list = query.list();
            if (list != null && !list.isEmpty()) {
                Equipo equipo = (Equipo) list.get(0);
                return equipo;
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    public Equipo findOnlineByIdStore(Session session, Integer idStore) {
        try {
            log.info(idStore);
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Equipo e where e.idLocal = " + idStore + " and e.indActivo = 1 and e.ip is not null and (e.desClave = 'CC' or e.desClave = 'DD')");
            log.info(query.getQueryString());
            List list = query.list();
            if (list != null && !list.isEmpty()) {
                Equipo equipo = (Equipo) list.get(0);
                return equipo;
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Equipo findOnlineJetsonByIdStore(Session session, Integer idStore) {
        try {
            log.info(idStore);
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Equipo e where e.idLocal = " + idStore + " and e.indActivo = 1 and e.desClave = 'SRVJT'");
            log.info(query.getQueryString());
            List list = query.list();
            if (list != null && !list.isEmpty()) {
                Equipo equipo = (Equipo) list.get(0);
                return equipo;
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Equipo findOnlineByIdStore(Session session, Integer idStore, String desClave) {
        try {
            log.info(idStore);
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Equipo e where e.idLocal = " + idStore + " and e.indActivo = 1 and e.ip is not null and (e.desClave = '" + desClave + "')");
            log.info("Extraer logs " + query.getQueryString());
            List list = query.list();
            if (list != null && !list.isEmpty()) {
                Equipo equipo = (Equipo) list.get(0);
                return equipo;
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Equipo findOnlineByIdEquipo(Session session, Integer idEquipo) {
        try {
            log.info(idEquipo);
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Equipo e where e.idEquipo = " + idEquipo + " and e.indOnline = 1 and (e.desClave = 'CC' or e.desClave = 'DD')");
            log.info("query:" + query.getQueryString());
            return (Equipo) query.uniqueResult();

        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Equipo findOnlineJetsonByIdEquipo(Session session, Integer idEquipo) {
        try {
            log.info(idEquipo);
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Equipo e where e.idEquipo = " + idEquipo + " and e.indOnline = 1 and e.desClave = 'SRVJT'");
            log.info("query:" + query.getQueryString());
            return (Equipo) query.uniqueResult();

        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public List getJetsonSender(Session session, Integer status) throws Exception {

        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where estadoActivo = 1 AND status = " + status + " ");
            log.info("query:" + query.getQueryString());
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List getStoreProccess(Session session, Integer status) throws Exception {
        try {
            Query query = session.createSQLQuery("SELECT T.COD_TIENDA FROM SAADMIN.MN_TIENDA T  \n"
                    + "JOIN EYES_EC.FM_EQUIPO E ON  T.DES_CLAVE = E.id_local\n"
                    + "join CT_EQUIPOS.CT_EQUIPOS C on C.ID_EQUIPO = E.ID_EQUIPO\n"
                    + "WHERE C.EMT_ESTADO = " + status + " and now() BETWEEN C.FECHA_EMT_ESTADO_D and C.FECHA_EMT_ESTADO_H ;");
            log.info("SQL --- " + query.getQueryString());
            List<Object[]> stores = query.list();
            log.info("ResulSet " + stores.size());
            if (stores != null && !stores.isEmpty()) {
                List<Integer> ids = new LinkedList<Integer>();
                for (Object store : stores) {
                    ids.add(((BigInteger) store).intValue());
                }
                Query query1 = session.createQuery("from com.allc.arms.server.persistence.store.Store where storeId in (:storList)");
                query1.setParameterList("storList", ids);
                return query1.list();
            } else {
                log.info("No Existen tiendas para procesar");
            }
            return new LinkedList();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List getJetsonSenderToPing(Session session, Integer status) throws Exception {

        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where estadoActivo = 1 AND pingStatus = " + status + " ");
            log.info("query:" + query.getQueryString());
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            } else {
                return new LinkedList();
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List getJetsonSenderToPingUp(Session session, Integer status) throws Exception {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where pingStatusUp = " + status + " ");
            log.info("query:" + query.getQueryString());
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            } else {
                return new LinkedList();
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List getJetsonSenderToRegisterUp(Session session, Integer status) throws Exception {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where statusRegistrarUp = " + status + " ");
            log.info("query:" + query.getQueryString());
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            } else {
                return new LinkedList();
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List getJetsonSenderToUploadUp(Session session, Integer status) throws Exception {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where statusUploadUp = " + status + " ");
            log.info("query:" + query.getQueryString());
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            } else {
                return new LinkedList();
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List getJetsonSenderToRestart(Session session, Integer status) throws Exception {

        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where estadoActivo = 1 AND statusReiniciar = " + status + " ");
            log.info("query:" + query.getQueryString());
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                log.info("Lista de jetson" + rows.size());
                return rows;
            } else {
                return new LinkedList();
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List getJetsonSenderToApagar(Session session, Integer status) throws Exception {
        //Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where estadoActivo = 1 AND statusReiniciar = " + status + " ");
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where estadoActivo = 1 AND statusApagar = " + status + " ");
            log.info("query:" + query.getQueryString());
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            } else {
                return new LinkedList();
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List<Jetson> getJetsonXIdEquipo(Session session, Integer idEquipo) {

        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where estadoActivo = 1 AND idEquipo = " + idEquipo);
            log.info("query:" + query.getQueryString());
            return query.list();

        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return new LinkedList<Jetson>();
    }

    public List<Jetson> getJetsonXIdEquipoUpload(Session session, Integer idEquipo) {

        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where estadoActivo = 1 and status = 4 AND idEquipo = " + idEquipo);
            log.info("query:" + query.getQueryString());
            return query.list();

        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return new LinkedList<Jetson>();
    }

    public Jetson getJetsonXmac(Session session, String mac) {

        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where estadoActivo = 1 AND macEqu = '" + mac + "' ");
            log.info("query:" + query.getQueryString());
            return (Jetson) query.uniqueResult();

        } catch (HibernateException e) {
            log.error(e.getMessage(), e);

        }
        return null;
    }

    public Jetson getJetsonId(Session session, Integer id) {

        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.Jetson where estadoActivo = 1 AND codigoEqu = " + id + " ");
            log.info("query:" + query.getQueryString());
            return (Jetson) query.uniqueResult();

        } catch (HibernateException e) {
            log.error(e.getMessage(), e);

        }
        return null;
    }

    public void updateJetson(Session session, Jetson jetson) throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(jetson);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        }
    }

    public JetsonUploadFile insertJetsonUploadFile(Session session, Integer codEqui) throws Exception {
        JetsonUploadFile jetsonUploadFile = new JetsonUploadFile();
        jetsonUploadFile.setFecha(new Date());
        jetsonUploadFile.setCodigoEqu(codEqui);
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(jetsonUploadFile);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
                throw e;
            }
            return null;
        }
        return jetsonUploadFile;
    }

    public JetsonUploadFile insertJetsonUploadFile(Session session, Integer codEqui, String url) throws Exception {
        JetsonUploadFile jetsonUploadFile = new JetsonUploadFile();
        jetsonUploadFile.setUrlArchivosEquipoOrigenEnt(url);
        jetsonUploadFile.setFecha(new Date());
        jetsonUploadFile.setCodigoEqu(codEqui);
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(jetsonUploadFile);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
                throw e;
            }
            log.error(e.getMessage(), e);
            return null;
        }
        return jetsonUploadFile;
    }

    public boolean updateJetsonUploadFile(Session session, Integer id, Integer codEquipo) throws Exception {
        Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.JetsonUploadFile where idFileEquipo = " + id);

        JetsonUploadFile entrenamiento = (JetsonUploadFile) query.uniqueResult();
        if (entrenamiento != null) {
            entrenamiento.setUrlArchivosEquipoOrigenEnt(id + ".zip");
            entrenamiento.setCodigoEqu(codEquipo);
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(entrenamiento);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                    throw e;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    public List<JetsonEntrenamiento> getListEntrenamiento(Session session) {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.JetsonEntrenamiento");
            log.info("query:" + query.getQueryString());
            return query.list();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return new LinkedList<JetsonEntrenamiento>();
    }

    public void saveJetsonEntrenamiento(Session session, List<JetsonEntrenamiento> entrenamientos) throws Exception {
        for (JetsonEntrenamiento entrenamiento : entrenamientos) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(entrenamiento);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                    throw e;
                }
            }
        }
    }

    public List<JetsonVersionAprendizaje> getAprendizajes(Session session) {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.JetsonVersionAprendizaje");
            log.info("query:" + query.getQueryString());
            return query.list();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return new LinkedList<JetsonVersionAprendizaje>();
    }

    public JetsonVersionAprendizaje getAprendizajes(Session session, long codVap) {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.JetsonVersionAprendizaje where codigoVap =" + codVap);
            log.info("query:" + query.getQueryString());
            return (JetsonVersionAprendizaje) query.uniqueResult();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public void saveJetsonAprendizajes(Session session, JetsonVersionAprendizaje aprendizaje) throws Exception {
        JetsonVersionAprendizaje jva = getAprendizajes(session, aprendizaje.getCodigoVap());
        if (jva == null) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(aprendizaje);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                    throw e;
                }
            }
        }
    }

    public JetsonEntrenamiento getEntrenamiento(Session session, int codEnt) {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.equipo.JetsonEntrenamiento where codigoEnt =" + codEnt);
            log.info("query:" + query.getQueryString());
            return (JetsonEntrenamiento) query.uniqueResult();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public void saveJetsonEntrenamiento(Session session, JetsonEntrenamiento entrenamiento) throws Exception {
        JetsonEntrenamiento jva = getEntrenamiento(session, entrenamiento.getCodigoEnt());
        if (jva == null) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(entrenamiento);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                    throw e;
                }
            }
        }
    }

    public void saveJetsonAprendizajes(Session session, List<JetsonVersionAprendizaje> aprendizajes) throws Exception {
        for (JetsonVersionAprendizaje aprendizaje : aprendizajes) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(aprendizaje);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                    throw e;
                }
            }
        }
    }
    
    public boolean updateIndOnlineByIdStoreAndIp(Session session, int idStore, String ip, int status){
    	Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createSQLQuery("UPDATE fm_equipo SET ind_online = :valor1, fec_actualizacion=CURRENT_TIMESTAMP WHERE id_local=" + idStore + " and ip='" + ip + "'");
			query.setParameter("valor1", status);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
    }
    
    public void updateEquipo(Session session, Equipo equipo) throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(equipo);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
}
