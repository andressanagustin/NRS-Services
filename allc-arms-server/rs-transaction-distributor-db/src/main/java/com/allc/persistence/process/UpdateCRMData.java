/**
 * 
 */
package com.allc.persistence.process;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.persistence.dao.entities.Activity;
import com.allc.persistence.dao.entities.RegistroPtos;
import com.allc.persistence.util.HibernateCRMUtil;
import com.allc.entities.PreferredCustomerData;

/**
 * @author gustavo
 *
 */
public class UpdateCRMData {
	private Session sesion;
	private Transaction tx; 
	private static Logger log = Logger.getLogger(UpdateCRMData.class);
	private SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");
	
	public UpdateCRMData() {
		super();
		iniciaOperacion();
	}

	public void update(com.allc.entities.Transaction transaction) {
		try {
			Activity activity = null;
			RegistroPtos regPtos = null;
			iniciaOperacion();
			if (transaction.getRetailTransaction() != null && transaction.getRetailTransaction().getPreferredCustData() != null) {
				PreferredCustomerData pcd = transaction.getRetailTransaction().getPreferredCustData();
				log.debug("Actualizando información de puntos del cliente: "+pcd.getCustomerAccountID());
				activity = (Activity) sesion.get(Activity.class, pcd.getCustomerAccountID());
				if (activity == null)
					activity = new Activity();
				activity.setCustomerId(pcd.getCustomerAccountID());
				Integer newPoints = pcd.getPoints().intValue()+ pcd.getBonusPoints().intValue();
				activity.setTotalPoints(activity.getTotalPoints() + newPoints);
				activity.setTotalTrans(activity.getTotalTrans() + 1);
				activity.setRedeemedPoints(activity.getRedeemedPoints() + pcd.getRedeemedPoints().intValue());
				activity.setAutoCouponAmount(activity.getAutoCouponAmount()	+ pcd.getCouponAmount().intValue());

				if (activity.getLastDate()==null || activity.getLastDate().compareTo(transaction.getEndDateTime()) < 0) {
					activity.setLastDate(transaction.getEndDateTime());
					activity.setLastPoints(pcd.getPoints().intValue());
					activity.setMessageNum(pcd.getMessageCount().intValue());
				}

				regPtos = new RegistroPtos();
				regPtos.setCustomerId(pcd.getCustomerAccountID());
				regPtos.setTipoTrans("1");
				regPtos.setFechaTrans(transaction.getBusinessDayDate());
				regPtos.setHoraTrans(sdfHora.format(transaction.getEndDateTime()));
				regPtos.setNumTerminal(new Integer(transaction.getWorkstationCode()));
				regPtos.setNroTrans(transaction.getSequenceNumber());
				regPtos.setOperador(new Integer(transaction.getOperatorCode()));
				regPtos.setSumPtos(newPoints);
				regPtos.setResPtos(pcd.getRedeemedPoints().intValue());
				regPtos.setCodTienda(new Integer(transaction.getRetailStoreCode()));
			}
			if (activity != null && regPtos!=null) {
				sesion.save(regPtos);
				sesion.save(activity);
				tx.commit();
				log.debug("Información de puntos actualizada!");
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			tx.rollback();
		} finally {
			sesion.close();
			sesion = null;
    		tx = null;
		} 
	}
	
    private void iniciaOperacion() throws HibernateException {
    	while(sesion==null || tx == null) {
	    	try {
	    	    sesion = HibernateCRMUtil.getSessionFactory().openSession(); 
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
