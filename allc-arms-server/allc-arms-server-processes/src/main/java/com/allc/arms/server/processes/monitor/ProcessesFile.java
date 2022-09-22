package com.allc.arms.server.processes.monitor;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.monitor.Monitor;
import com.allc.arms.server.persistence.monitor.MonitorDAO;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;
import com.google.gson.Gson;

/**
 * Proceso encargado de leer el archivo enviado desde los controladores para el monitor de control de la suite
 * 
 *
 */

public class ProcessesFile extends AbstractProcess{
	protected Logger log = Logger.getLogger(ProcessesFile.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected long timeSleep;
	private static String path;
	private Controlador controler;
	public boolean isEnd = false;
	private Session sessionEyesmin = null;
	private Session sessionSaadmin = null;
	private MonitorDAO monitorDAO;
	private List<OperacionesBD> operacionesBD;
	
	
	public void run() {
		log.info("Incia el Proceso lectura archivo para monitor.");
		while (!isEnd) {
			iniciarEyesSesion();
			iniciarSaadminSesion();
			//OBTENER CADA TIENDA ACTIVA
			StoreDAO storeDAO = new StoreDAO(); //Obtengo tiendas
			List stores = storeDAO.getAllActiveStores(sessionSaadmin);
			if (stores != null && !stores.isEmpty()) {
				Iterator itStore = stores.iterator();
				while (itStore.hasNext()) {
					try {
						Store store = (Store) itStore.next();
						monitorDAO = new MonitorDAO();
						String storeNumber = store.getKey().toString();;
						while(storeNumber.length() < 3)
							storeNumber = "0" + storeNumber;
						path = properties.getObject("processesFile.path") + storeNumber + "/controlador/";
						log.info("Lee carpeta " + path);
				        String fileName = readPath(path);
				        if (fileName != null) {
				            log.info("Lee archivo " + fileName);
				            controler = jsonControladorToObject(path + "/" + fileName);
				            if (controler !=null) {
				            	getDataFromBD(controler.getId_local());
				            	saveMonitorControlador(controler);
				            	saveMonitorPos(controler.getPosList());
				            	deleteFile(path + "/" + fileName);
				            }
				        }
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
	        finalizarEyesSesion();
	        finalizarSaadminSesion();
	        try {
	        	Thread.sleep(properties.getLong("processesFile.timesleep"));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	private String readPath(String pathName) {
		try {
            File path = new File(pathName);
            File[] files = path.listFiles();
            if (files == null || files.length == 0){
                log.info("No hay archivos.");
                return null;
            }
            //Ordena por fecha Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            return files[0].getName();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
	}
	
    private Controlador jsonControladorToObject(String fileName){
        try {
            Reader reader = Files.newBufferedReader(Paths.get(fileName));
            return new Gson().fromJson(reader, Controlador.class);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }
    
    private void deleteFile(String fileName){
        File file = new File(fileName);
        if (file.delete()) {
			log.info("Borra archivo: " + fileName);
		}
        else {
        	log.info("NO borra archivo: " + fileName);
		}
    }
    
    private void getDataFromBD(int idLocal) {
    	final int CENTAVOS = 100; //Lo divide por 100 para que quede bien el monto
    	int cantidad = 0;
    	float monto = 0;
		
    	log.info("TRAE OPERACIONES EN LA BASE DE DATOS");
    	try {
    		List<Object[]> operaciones = monitorDAO.getOperacionByTienda(sessionSaadmin, idLocal);
        	if (operaciones != null && !operaciones.isEmpty()) operacionesBD = new ArrayList<OperacionesBD>();
        	for (Object[] operacion : operaciones) {
        		String id = Integer.toString(Integer.parseInt(operacion[1].toString())); //Lo paso 
        		log.info("CONTROLADOR " + operacion[1] + "(" + id + ")" + " - monto " + operacion[2] + " -  cantidad " + operacion[3]);
        		operacionesBD.add(new OperacionesBD(id, Float.parseFloat(operacion[2].toString()) / CENTAVOS, Integer.parseInt(operacion[3].toString())));
        		cantidad +=Integer.parseInt(operacion[3].toString());
        		monto += Float.parseFloat(operacion[2].toString()) / CENTAVOS;
        	}
        	if (operaciones != null && !operaciones.isEmpty()) operacionesBD.add(new OperacionesBD("CC", monto, cantidad));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
    }
    
    private void saveMonitorControlador(Controlador controlador) {
    	log.info("Guarda controlador " + controler.toString());
        //CONTROLAR SI EXISTE
    	Monitor monitor = monitorDAO.getMonitorById(sessionEyesmin, controler.getId_local(), "CC");
    	if (monitor == null) {
    		log.info("NO ENCUENTRA MONITOR, LO CREA");
    		monitor = new Monitor();
    		monitor.setId_local(controler.getId_local());
    		monitor.setDes_clave("CC");
    	}
    	else {
    		log.info("Encuentra monitor" + monitor.getId_local());
    	}
    	monitor.setCantidad(controler.getCant_operaciones());
		monitor.setMonto(controler.getMonto());
		monitor.setFec_actualizacion(new Date());
		monitor.setCantidad_bd(getCantidad(monitor.getDes_clave()));
		monitor.setMonto_bd(getMonto(monitor.getDes_clave()));
		monitorDAO.insertMonitor(sessionEyesmin, monitor);
    }
    
    private void saveMonitorPos(List<Pos> posList) {
    	for (Iterator iterator = posList.iterator(); iterator.hasNext();) {
			Pos pos = (Pos) iterator.next();
			log.info("Guarda POS " + pos.toString());
			Monitor monitor = monitorDAO.getMonitorById(sessionEyesmin, controler.getId_local(), pos.getCodigo());
        	if (monitor == null) {
        		log.info("NO ENCUENTRA MONITOR, LO CREA");
        		monitor = new Monitor();
        		monitor.setId_local(controler.getId_local());
        		monitor.setDes_clave(pos.getCodigo());
        	}
        	else {
        		log.info("Encuentra monitor id_local " + monitor.getId_local() + " des_clave " + monitor.getDes_clave());
        	}
        	monitor.setCantidad(pos.getCant_operaciones());
    		monitor.setMonto(pos.getMonto());
    		monitor.setFec_actualizacion(new Date());
    		monitor.setCantidad_bd(getCantidad(monitor.getDes_clave()));
    		monitor.setMonto_bd(getMonto(monitor.getDes_clave()));
    		monitorDAO.insertMonitor(sessionEyesmin, monitor);
		}
    }
    
    private int getCantidad(String id) {
    	try {
    		log.info("Busca cantidad id " + id);
        	if (operacionesBD != null) {
        		for (Iterator iterator = operacionesBD.iterator(); iterator.hasNext();) {
        			OperacionesBD operacionesBDAux = (OperacionesBD) iterator.next();
        			log.info(operacionesBDAux.toString());
        			if (operacionesBDAux.getId().equals(id)) {
        				log.info("Encuentra " + id);
        				return operacionesBDAux.getCantidad();
        			}
        		}
        		log.info("No encuentra cantidad id " + id);
			}
        	else log.info("OperacionesBD en null");
        	return 0;	
    	} catch (Exception e) {
			log.error(e.getMessage(), e);
			return 0;
		}
    }
    
    private float getMonto(String id) {
    	try {
    		if (operacionesBD != null) {
    			for (Iterator iterator = operacionesBD.iterator(); iterator.hasNext();) {
        			OperacionesBD operacionesBDAux = (OperacionesBD) iterator.next();
        			if (operacionesBDAux.getId().equals(id)) {
        				log.info("Encuentra " + id);
        				return operacionesBDAux.getMonto();
        			}
        		}	
    		}
    		else log.info("OperacionesBD en null");
        	return 0;	
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return 0;
		}
    }
    
    private void iniciarEyesSesion() {
        while (sessionEyesmin == null && !isEnd) {
            try {
                sessionEyesmin = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
            } catch (HibernateException e) {
                log.error(e.getMessage(), e);
            }
            if (sessionEyesmin == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
    private void finalizarEyesSesion() {
    	if (sessionEyesmin != null) {
            sessionEyesmin.close();
            sessionEyesmin = null;
        }
    }
    
    private void iniciarSaadminSesion() {
        while (sessionSaadmin == null && !isEnd) {
            try {
            	sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
            } catch (HibernateException e) {
                log.error(e.getMessage(), e);
            }
            if (sessionSaadmin == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
    private void finalizarSaadminSesion() {
    	if (sessionSaadmin != null) {
    		sessionSaadmin.close();
    		sessionSaadmin = null;
    	}
    }
    
	@Override
	public boolean shutdown(long arg0) {
		isEnd = true;
		finalizarEyesSesion();
		finalizarSaadminSesion();
		log.info("Finaliza el Proceso lectura archivo para monitor.");
		return true;
	}

}
