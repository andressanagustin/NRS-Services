package com.allc.arms.server.persistence;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateSessionFactory {
	static Logger log = Logger.getLogger(HibernateSessionFactory.class);
	private String name = null;
	private SessionFactory sessionFactory = null;

	public HibernateSessionFactory(String name) {
		this.name = name;
	}

	private SessionFactory buildSessionFactory() {
		try {
			Configuration configuration = new Configuration();
			String filename = "hibernate" + name + ".cfg.xml";
			configuration.configure(filename);
			return configuration.buildSessionFactory();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public SessionFactory getSessionFactory() {
		if (sessionFactory == null)
			sessionFactory = buildSessionFactory();
		return sessionFactory;
	}

	public boolean closeSessionFactory() {
		try {
			if (sessionFactory != null && !sessionFactory.isClosed()) {
				sessionFactory.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

}
