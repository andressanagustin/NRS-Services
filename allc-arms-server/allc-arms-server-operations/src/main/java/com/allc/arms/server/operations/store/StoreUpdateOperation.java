/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.operations.store;

import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;

import com.allc.arms.server.operations.utils.AbstractOperationPrincipal;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.equipo.Equipo;
import com.allc.arms.server.persistence.equipo.EquipoDAO;
import com.allc.arms.server.persistence.store.BusinessStore;
import com.allc.arms.server.persistence.store.RetailStoreDAO;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreUtil;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.allc.comm.frame.Frame;
import com.allc.entities.RetailStore;

/**
 *
 * @author ruben.gomez
 */
public class StoreUpdateOperation extends AbstractOperationPrincipal{
	
	private final Logger logger = Logger.getLogger(StoreUpdateOperation.class);
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private Session sessionArts = null;
	private Session sessionEyes = null;
	private Session sessionSaadmin = null;
        public final Integer STATUS_PROCESADO = 3;
        private StoreDAO storeDAO = new StoreDAO();
        private EquipoDAO equipoDAO = new EquipoDAO();
	protected ConnSocketClient socketClient = null;
	
	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Iniciando StoreUpdateOperation...");
                iniciarSesion("Arts", "Eyes", "Saadmin");
		String msg = "1";
                Integer storeKey = null;
                RetailStoreDAO retailStoreDAO = new RetailStoreDAO();
		
		try {					
			
			XStream xstreamAux = new XStream();
			String StoreXML = (String) frame.getBody().get(0);
			
                        xstreamAux.alias("StoreUtil", StoreUtil.class);                                
                        StoreUtil storeUtil = (StoreUtil) xstreamAux.fromXML(StoreXML);
                        
                        Store newStore = storeUtil.getStore();
                        List<Equipo> equipos = storeUtil.getEquipos();
                        RetailStore retailStore = storeUtil.getRetailStore();
                        List<BusinessStore> businessStores = storeUtil.getBusinessStores();
                        
                        logger.info("store: " + newStore.toString());
                        logger.info("equipos: " + equipos.toString());
                        logger.info("businessStores: " + businessStores);
                        logger.info("retailStore: " + retailStore.toString());
                        
                        equipos.forEach((t) -> {logger.info("equipos: " + t);});
                        businessStores.forEach((t) -> {logger.info("businessStore: " + t);});
                                
                        storeKey = newStore.getKey();
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "STORE_UPD_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Actualizando Tienda: "+storeKey+".\n", true);

                        Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, storeKey);
                        RetailStore retailStoreToSend = retailStoreDAO.getRetailStoreByCode(sessionArts, storeKey);
                        
                        storeToSend = createStoreToSend(storeToSend, newStore);
			retailStoreToSend = createRetailStoreToSend(retailStoreToSend, retailStore);
                        
                        storeToSend.setStatusDownload(STATUS_PROCESADO);
			storeDAO.updateStore(sessionSaadmin, storeToSend);
			retailStoreDAO.updateRetailStore(sessionArts, retailStoreToSend);
                        
                        updateEquipos(equipos);
                        updateBusinessStores(businessStores, storeKey);                        
                        
                        msg = "0";
			logger.info("Tienda: " + storeToSend.getKey() + ", actualizado.");
                        UtilityFile.createWriteDataFile(getEyesFileName(properties), "STORE_UPD_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Tienda: "+storeKey+" procesado.\n", true);
			
		} catch (Exception e) {
			try {
                                msg = "1";
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "STORE_UPD_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al actualizar la Tienda: "+storeKey+".\n", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
			logger.error(e.getMessage(), e);
		}
                
                StringBuffer sb = new StringBuffer(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + msg);
                socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")));
                
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                
                return false;
	}

	protected Store createStoreToSend(Store originStore, Store store) {

            if(originStore == null){
		originStore = new Store();
            }
            
            originStore.setName(store.getName());
            originStore.setKey(store.getKey());
            originStore.setAddress(store.getAddress());
            originStore.setRegionCode(store.getRegionCode());
            originStore.setCityCode(store.getCityCode());
            originStore.setIp(store.getIp());
            originStore.setStatus(store.getStatus());
            originStore.setIdReg(store.getIdReg());
            originStore.setLastUpdate(store.getLastUpdate());
            originStore.setCodCanton(store.getCodCanton());
            originStore.setCodProvincia(store.getCodProvincia());
            originStore.setRucTienda(store.getRucTienda());
            originStore.setCodSri(store.getCodSri());
            originStore.setLocalServer(store.getLocalServer());
            originStore.setPaDivision(store.getPaDivision());
            originStore.setPaCiudad(store.getPaCiudad());
            originStore.setLatitud(store.getLatitud());
            originStore.setLongitud(store.getLongitud());
            originStore.setIdBsnUnGp(store.getIdBsnUnGp());
            originStore.setIpWsNotifSuperv(store.getIpWsNotifSuperv());
            originStore.setEstIniLoadOpe(store.getEstIniLoadOpe());
            originStore.setDownloadLog(store.getDownloadLog());
            originStore.setDesClaveDownload(store.getDesClaveDownload());
            originStore.setPgSync(store.getPgSync());
            originStore.setFecEstIniLoadOpe(store.getFecEstIniLoadOpe());
            originStore.setProgressIniLoadOpe(store.getProgressIniLoadOpe());
            originStore.setStatusDownload(store.getStatusDownload());

            return originStore;
	}
        
        protected RetailStore createRetailStoreToSend(RetailStore originRetailStore, RetailStore retailStore) {

            if(originRetailStore == null){
		originRetailStore = new RetailStore();
            }
            
            originRetailStore.setCode(retailStore.getCode());
            originRetailStore.setDescription(retailStore.getDescription());
            originRetailStore.setIncludeTax(retailStore.getIncludeTax());
            originRetailStore.setIvatax(retailStore.getIvatax());
            originRetailStore.setTax1(retailStore.getTax1());
            originRetailStore.setTax2(retailStore.getTax2());
            originRetailStore.setTax3(retailStore.getTax3());
            originRetailStore.setTax4(retailStore.getTax4());
            originRetailStore.setTax5(retailStore.getTax5());
            originRetailStore.setTax6(retailStore.getTax6());
            originRetailStore.setTax7(retailStore.getTax7());
            originRetailStore.setTax8(retailStore.getTax8());
            originRetailStore.setCeCobe(retailStore.getCeCobe());
            originRetailStore.setNoAfiliadoFlag(retailStore.getNoAfiliadoFlag());
            originRetailStore.setIdCtab(retailStore.getIdCtab());
            originRetailStore.setDistDir(retailStore.getDistDir());
            originRetailStore.setFlagStockLoad(retailStore.getFlagStockLoad());
            originRetailStore.setIdRetailStoreGroup(retailStore.getIdRetailStoreGroup());
            originRetailStore.setStatus(retailStore.getStatus());
            
            return originRetailStore;
	}
        
        protected void updateEquipos(List<Equipo> equipos) throws Exception {

            if(equipos.isEmpty()){
                return;
            }
            
            for (Equipo equipo : equipos) {
                Equipo originEquipo = equipoDAO.findOnlineByIdStoreAndDesClave(sessionEyes, equipo.getIdLocal(), equipo.getDesClave());
                
                if(originEquipo == null){
                    originEquipo = new Equipo();
                }

                originEquipo.setDesClave(equipo.getDesClave());
                originEquipo.setDesEquipo(equipo.getDesEquipo());
                originEquipo.setIp(equipo.getIp());
                originEquipo.setIndOnline(equipo.getIndOnline());
                originEquipo.setIndActivo(equipo.getIndActivo());
                originEquipo.setIdTipo(equipo.getIdTipo());
                originEquipo.setIdLocal(equipo.getIdLocal());
                originEquipo.setCodUsuario(equipo.getCodUsuario());
                originEquipo.setFecActualizacion(equipo.getFecActualizacion());
                originEquipo.setIdNegocio(equipo.getIdNegocio());
                originEquipo.setIdTipoTerminal(equipo.getIdTipoTerminal());
  
                equipoDAO.updateEquipo(sessionEyes, originEquipo);
                
            };
       }
        
        protected void updateBusinessStores(List<BusinessStore> businessStores, Integer storeKey) throws Exception {

            if(businessStores.isEmpty() ||  storeKey == null){
                return;
            }
            
            Store store = storeDAO.getStoreByCode(sessionSaadmin, storeKey);
            
            if (store == null){
               return;
            }

            storeDAO.deleteBusinessStore(sessionSaadmin, store.getStoreId());
            
            for (BusinessStore businessStore : businessStores) {
                storeDAO.insertBusinessStore(sessionSaadmin, store.getStoreId(), businessStore.getBusinessId());
            };
       }
	
	protected void iniciarSesion(String name1, String name2, String name3) {
		while (sessionArts == null) {
			try {
				sessionArts = HibernateSessionFactoryContainer.getSessionFactory(name1).openSession(); 
                       } catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionArts == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
          
		while (sessionEyes == null) {
			try {
				sessionEyes = HibernateSessionFactoryContainer.getSessionFactory(name2).openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionEyes == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
                
		while (sessionSaadmin == null) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory(name3).openSession();
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionSaadmin == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}
	
	protected boolean connectClient(String ip) {
		if (socketClient == null || !socketClient.isConnected()) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(ip);
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		//logger.info("IP: " + socketClient.getIpServer() +", puerto: " + socketClient.getPortServer());
		return socketClient.connectSocketUsingRetries();
	}
	
	protected boolean connectClient(PropFile properties, Store tienda) {
		if (socketClient == null || !socketClient.isConnected()) {
			logger.info("Store IP: " + tienda.getIp());
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(tienda.getIp());
			socketClient.setPortServer(properties.getInt("serverSocket.port"));
			socketClient.setRetries(2);
			socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}
	
	protected void closeConnection() {
		if (socketClient != null)
			socketClient.closeConnection();
	}

	@Override
	public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	private String getEyesFileName(PropFile properties ){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	protected boolean sendToLocal(StringBuffer data, Store store){
		List list = Arrays.asList(p.split(data.toString()));
		Frame frame = new Frame(list,
				ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
				ArmsServerConstants.Communication.FRAME_SEP);
		if (frame.loadData()) {
			boolean send = sendFrameToLocal(frame, store);
			if (send) {
				logger.info("Archivo enviado correctamente.");
				return true;
			} else {
				logger.error("Error al enviar al server.");
			}
		}
		return false;
	}
	protected boolean sendFrameToLocal(Frame frame, Store tienda) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient(properties, tienda);
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength);
			logger.info("Trama a enviar: " + trama);
			if (socketClient.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0) {
					numberOfBytes = socketClient.readLengthDataSocket();
					if (timeOutCycles == 5) {
						// cada 5 timeouts escribimos una trama vacía para
						// asegurarnos que el socket esté activo
						String mje = Util.addLengthStartOfString("", qtyBytesLength);
						if (!socketClient.writeDataSocket(mje)) {
							socketClient.setConnected(false);
							return false;
						}
						timeOutCycles = 0;
					}
					timeOutCycles++;
				}
				if (numberOfBytes > 0) {
					str = socketClient.readDataSocket(numberOfBytes);
					if (StringUtils.isNotBlank(str)) {
						list = Arrays.asList(p.split(str));
						frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsServerConstants.Communication.FRAME_SEP);
						logger.info("Respuesta recibida: " + frameRpta.toString());
						if (frameRpta.getStatusTrama() == 0) {
							return true;
						}
					}
				}
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
	}
}
