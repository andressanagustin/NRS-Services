/**
 * 
 */
package com.allc.arms.server.persistence;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;

/**
 * @author gustavo
 *
 */
public class HibernateSessionFactoryContainer {
	private static Map sessionFactories;

	/**
	 * 
	 */
	public HibernateSessionFactoryContainer() {

	}

	public static SessionFactory getSessionFactory(String name) {
		if (sessionFactories == null) {
			sessionFactories = new HashMap();
		}
		HibernateSessionFactory sf = (HibernateSessionFactory) sessionFactories.get(name);
		if (sf == null) {
			sf = new HibernateSessionFactory(name);
			sessionFactories.put(name, sf);
		}
		return sf.getSessionFactory();
	}
}
