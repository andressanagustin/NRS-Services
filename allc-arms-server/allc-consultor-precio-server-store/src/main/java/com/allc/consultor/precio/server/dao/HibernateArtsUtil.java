package com.allc.consultor.precio.server.dao;


import java.io.File;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateArtsUtil {
	static Logger log = Logger.getLogger(HibernateArtsUtil.class);
	private static final SessionFactory sessionFactory = buildSessionFactory();

	private static SessionFactory buildSessionFactory() {

		try {
			Configuration configuration = new Configuration();
			configuration.configure(new File("C:/ALLC/CPWS/hibernateArts.cfg.xml"));
			return configuration.buildSessionFactory();
		} catch (HibernateException he) {
			he.printStackTrace();
//			log.error("Ocurri� un error en la inicializaci�n de la SessionFactory: "
//					+ he.getMessage(), he);
			throw new ExceptionInInitializerError(he);
		} catch (Exception e) {
			e.printStackTrace();
//			log.error("Ocurri� un error en la inicializaci�n de la SessionFactory: "
//					+ e.getMessage(), e);
		} catch (Throwable  ex) {
			ex.printStackTrace();
//			log.error("Ocurri� un error en la inicializaci�n de la SessionFactory: "
//					+ ex, ex);
		}
		return null;
	}

	public static SessionFactory getSessionFactory() {
	
		return sessionFactory;
	}	  
 
	public static boolean closeSessionFactory(){
		boolean result = false;
		try {
			if(!getSessionFactory().isClosed()){
				getSessionFactory().close();
				result = true;
			}else
				result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
