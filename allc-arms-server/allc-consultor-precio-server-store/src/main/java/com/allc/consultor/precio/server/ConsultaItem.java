/**
 * 
 */
package com.allc.consultor.precio.server;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.allc.consultor.precio.server.dao.HibernateArtsUtil;
import com.allc.consultor.precio.server.dao.ItemDAO;

/**
 * @author gustavo
 *
 */
public class ConsultaItem {
	static Logger log;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConsultaItem consultaItem = new ConsultaItem();
		PropertyConfigurator.configure("log4jConf.properties");
		log = Logger.getLogger(ConsultaItem.class);
		log.info("Resultado: "+consultaItem.getItemData("200000000001"));
	}
	/**
	 * 
	 * @param ean
	 * @return
	 * La salida sera un String separado por (|) entre campos

		los campos son:
		
		descripción = String
		precio Afiliado = decimal
		precio No Afiliado = decimal
		precio Caja = decimal (devuelve 0.00 si no hay promoción para el articulo)
		iva = String (contiene S y N)
		promoción = String (contiene S y N)  S si tiene promoción. 
		
		Los campos numéricos usan siempre 2 decimales.
		
		Ej.
		
		VELAS PELOTA DE FUTBOLT C-1010 1 . .|2.46|2.64|0.00|S |N
	 */
	public String getItemData(String ean) {
		try {
			Properties props = new Properties();
			FileInputStream is = new FileInputStream("C:/ALLC/CPWS/ConsultorPrecioServerConf.properties");
			props.load(is);
			Integer storeNumber = new Integer(props.getProperty("consultaItem.store.number"));
			ItemDAO itemDAO = new ItemDAO();
			/** get a session **/
			SessionFactory sessionFactory = HibernateArtsUtil.getSessionFactory();
			/** open a session **/
			Session session = sessionFactory.openSession();
			List<String> list = itemDAO.getItem(session, new Long(ean.substring(0, ean.length()-1)), storeNumber);
			
			if(list!=null && list.size() >= 7){
				
				double porcRec = Double.valueOf(formatNumber(list.get(6).toString())).doubleValue() / 100;
				
				double porcImp = Double.valueOf(formatNumber(list.get(7).toString())).doubleValue() / 100;
				
				double precioBase = Double.valueOf(formatNumber(list.get(1).toString())).doubleValue();
				
				double precioUnitario = roundUp(precioBase + (precioBase * porcRec), 100);
								
				double precioAfiliado = roundUp("S".equalsIgnoreCase(list.get(4).toString()) ? (precioBase + (precioBase * porcImp)) : precioBase, 100);

				double precioNoAfiliado = roundUp("S".equalsIgnoreCase(list.get(4).toString()) ? (precioUnitario + (precioUnitario * porcImp)) : precioUnitario, 100);
				
				return list.get(0) +"|"+ precioAfiliado +"|"+ precioNoAfiliado +"|"+ precioNoAfiliado +"|"+ list.get(4) +"|"+ list.get(5);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Artículo no disponible";
	}
	
	public double roundUp(double n, int width) {
		return Math.rint(n*width)/width;
	}
	
	private String formatNumber(String nbr) {
		if(nbr != null) {
			String number = nbr.split("\\.")[0];
			while(number.length() < 3) {
				number = "0"+number;
			}
			return number.substring(0, number.length()-2)+"."+number.substring(number.length()-2);
		}
		return "0.00";
	}
}
