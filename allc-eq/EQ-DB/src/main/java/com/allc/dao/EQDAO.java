/**
 * 
 */
package com.allc.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.entities.Ejecucion;
import com.allc.entities.Equipo;
import com.allc.entities.Message;
import com.allc.entities.Proceso;
import com.allc.entities.TipoEstado;
import com.allc.util.HibernateUtil;

/**
 * @author gustavo
 *
 */
public class EQDAO {
	private Session sesion; 
    private Transaction tx;  
    private static Logger log = Logger.getLogger(EQDAO.class);

    public EQDAO() {
		super();
		iniciaOperacion();
	}


	public boolean saveMessage(Message message) { 
        try {

        	message.setData(message.getData().replaceAll("\u0000", ""));
            sesion.save(message); 
            tx.commit();
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        	try {
        		tx.rollback();
        	} catch (Exception ex){
        		log.error(e.getMessage(), e);
        	}
        	return false;
        } 
        return true; 
    }  
	
	public boolean saveEjecucion(Ejecucion ejecucion) { 
        try {
            sesion.save(ejecucion); 
            tx.commit();
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        	try {
        		tx.rollback();
        	} catch (Exception ex){
        		log.error(e.getMessage(), e);
        	}
        	return false;
        } 
        return true; 
    }  

    
    public Equipo getEquipo(String desClave, String codLocal){
    	Query query = sesion.createQuery("from com.allc.entities.Equipo where desClave = '"+desClave+"' and idLocal = '"+codLocal+"'");
    	List list = query.list();
    	if(list!=null && !list.isEmpty())
    		return (Equipo) list.get(0);
    	return null;
    }
    
    public Proceso getProceso(String desClave){
    	Query query = sesion.createQuery("from com.allc.entities.Proceso where clave = '"+desClave+"'");
    	List list = query.list();
    	if(list!=null && !list.isEmpty())
    		return (Proceso) list.get(0);
    	return null;
    }
    
    public TipoEstado getTipoEstado(String abrev){
    	Query query = sesion.createQuery("from com.allc.entities.TipoEstado where abreviatura = '"+abrev+"'");
    	List list = query.list();
    	if(list!=null && !list.isEmpty())
    		return (TipoEstado) list.get(0);
    	return null;
    }
    
    public void closeSession(){
    	try {
	    	if(sesion!=null && sesion.isOpen())
	    		sesion.close();
	        sesion = null;
	        tx = null;
    	} catch (Exception e) {
    		
    	}
    }

    public void iniciaOperacion() { 
    	while(sesion==null || tx == null) {
    	try {
    	    sesion = HibernateUtil.getSessionFactory().openSession(); 
	        tx = sesion.beginTransaction(); 
    	} catch (Exception e){
    		log.error(e.getMessage(), e);
    		sesion = null;
    		tx = null;
    	}
		if(sesion == null || tx == null)
			try {
				log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
				Thread.sleep(3000);
			} catch (InterruptedException e) {
	    		log.error(e.getMessage(), e);
	    	}
    	}
    }  
}
