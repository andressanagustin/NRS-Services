package com.allc.arms.server.persistence.sociedad;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.allc.arms.server.persistence.moto.Moto;



public class SociedadDAO {
	
	static Logger log = Logger.getLogger(SociedadDAO.class);
	
	public Sociedad getClaveBySociedad(Session sesion, String sociedad){
    
	    	Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.sociedad.Sociedad G WHERE G.idSociedad = '"+sociedad+"'");
	    	Iterator iterator = query.iterate();
	    	if(iterator.hasNext())
	    		return (Sociedad) iterator.next();
	    	return null;
    }

}
