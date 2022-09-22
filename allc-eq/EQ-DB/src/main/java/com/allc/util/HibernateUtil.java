/**
 * 
 */
package com.allc.util;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author GUSTAVOK
 * 
 */
public class HibernateUtil {
	private static SessionFactory sessionFactory;
	private static Logger log = Logger.getLogger(HibernateUtil.class);;

	static void initialize() {
		try {
			Configuration configuration = new Configuration();
			configuration.configure("hibernate.cfg.xml");
			sessionFactory = configuration.buildSessionFactory();
		} catch (HibernateException he) {
			log.error("Ocurri� un error en la inicializaci�n de la SessionFactory: "
					+ he.getMessage(), he);
			throw new ExceptionInInitializerError(he);
		} catch (Exception e) {
			log.error("Ocurri� un error en la inicializaci�n de la SessionFactory: "
					+ e.getMessage(), e);
		} catch (Throwable  ex) {
			log.error("Ocurri� un error en la inicializaci�n de la SessionFactory: "
					+ ex, ex);
		}
	}


	public static SessionFactory getSessionFactory() {
		if(sessionFactory==null)
			initialize();
		return sessionFactory;
	}
}
