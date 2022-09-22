package com.allc.arms.server.processes.operator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.operator.AuthorizesOPC;
import com.allc.arms.server.persistence.operator.IndicatOPC;
import com.allc.arms.server.persistence.operator.LevelAuthorizes;
import com.allc.arms.server.persistence.operator.Operator;
import com.allc.arms.server.persistence.operator.OperatorDAO;
import com.allc.arms.server.persistence.operator.OperatorStore;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;

public class ProcessFileOperator extends AbstractProcess{
	protected static Logger log = Logger.getLogger(ProcessFileOperator.class.getName());
	protected PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	protected Session session = null;
	private Session sessionSaadmin = null;
	protected String descriptorProceso = "OP_LOAD_P";
	private final String[] character = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "Ñ", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
	private final boolean cargaModelos = prop.getInt("iniLoadOperator.cargaModelos") != 0;
	private StoreDAO storeDao;
	private OperatorDAO opDao;
	private static final int DOWNLOAD = 1;
	private static final int STATUS = 5;
	private static final int SUBSCRIBE = 1;
	public static final int STATUS_ACTUALIZADO = 3;
	public static final int STATUS_ERROR = 9;
	
	private int storeKey;
	private String pathFile;
	
	public ProcessFileOperator(int storeKey, String pathFile) {
		this.storeKey = storeKey;
		this.pathFile = pathFile;
		this.storeDao = new StoreDAO();
		this.opDao = new OperatorDAO();
	}
	
	public void run() {
		try {
			iniciarSesion();
			iniciarSaadminSesion();
			//Vuelvo a obtener la tienda porque cuando cambio de estado no me deja guardarlo
			Store store = storeDao.getStoreByCode(sessionSaadmin, this.storeKey);
			String storeNumber = store.getKey().toString();;
			while(storeNumber.length() < 3)
				storeNumber = "0" + storeNumber;
			log.info("ENTRA A PROCESAR ARCHIVO DE TIENDA " + store.getKey());
			boolean respProceso = procesaArchivos(storeNumber, store.getStoreId());
			//cambiar estado de tienda procesada
			if(respProceso)
				store.setEstIniLoadOpe(STATUS_ACTUALIZADO);
			else
				store.setEstIniLoadOpe(STATUS_ERROR);
			storeDao.updateStore(sessionSaadmin, store);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			finalizaSesion();
			log.info("Finalizó el Proceso de archivo para operador tienda " + storeKey + ".");
		}
	}
	
	public boolean procesaArchivos(String pStoreNumber, Integer idTienda) {
		Integer codTienda = Integer.parseInt(pStoreNumber);
		String file1 = null;
		String file2 = null;
		boolean respuesta = false;
		
	    UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso+"|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|"+codTienda+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando proceso de carga de Operadores.\n", true);

		file1 = pStoreNumber+"-1-OperaLoadData";
		file2 = pStoreNumber+"-2-OperaLoadData";
		
		try {
			int totalLineas = 0;
			int lineasProcesadas = 0;
			//leemos archivo 1 para saber cantidad de lineas
			FileReader fr1 = new FileReader(pathFile + '/' + file1);
			BufferedReader br1 = new BufferedReader(fr1);
			String lineaFile1;
			while((lineaFile1 = br1.readLine()) != null) {
				totalLineas++;
			}
			fr1.close();
			br1.close();
			
			//leemos archivo 2 y guardamos en un hashmap
			log.info("Leemos ARCHIVO 2 (Niveles de autorizacion), Tienda: "+codTienda);
			FileReader fr2 = new FileReader(pathFile + '/' + file2);
			BufferedReader br2Mod = new BufferedReader(fr2);
			List<String> fileNvlAutoMod = new ArrayList<String>();
			String lineaFileMOD;			
			// INICIAMOS UN HASHMAP CON LOS MODELOS PARA PODER ASIGNAR DESPUES A LOS OPERADORES
			while((lineaFileMOD = br2Mod.readLine()) != null)
			{		
				totalLineas++;
				String tipoMod = lineaFileMOD.substring(lineaFileMOD.length() - 3);
				if(tipoMod.equalsIgnoreCase("MOD"))
				{
					fileNvlAutoMod.add(lineaFileMOD);
				}
			}
			br2Mod.close();
			fr2.close();
			log.info("Cantidad de lineas a procesar: " + totalLineas);
			// procesa archivo 2 datos de SISTEMA OPERATIVO
			fr2 = new FileReader(pathFile + '/' + file2);
			BufferedReader br2 = new BufferedReader(fr2);
			String lineaFile2;
            int idReg = consultaUsUsuarios_cuentaAdmin().intValue();	// 1102 buscar cuenta = NUOADM
            while ((lineaFile2 = br2.readLine()) != null) {
                //remplazar 00000000*9 --> 9999999999 por los modelos
                String codigoTxt = lineaFile2.substring(0, 10);
                log.info("Log reempla " + codigoTxt);
                for (int i = 0; i < character.length; i++) {
                    String string = character[i];
                    codigoTxt = codigoTxt.replaceAll("(?i)" + string, i + "");
                    log.info("Log reempla 2" + codigoTxt);
                }
                log.info("Log reempla 3" + codigoTxt);
                //cabiar letras a numeros
                String tipo = lineaFile2.substring(lineaFile2.length() - 3);
                log.info(tipo);
                boolean isModel = tipo.equalsIgnoreCase("MOD");
                
                String IdentitySO =  lineaFile2.substring(0, 10);
                while(IdentitySO.startsWith("0")){
                	IdentitySO = IdentitySO.replaceFirst("0", "");
                }
                log.info("Carga modelos: " + cargaModelos);
                log.info("isModel " + isModel);
                if (isModel) {
                	long IdModelSO = Long.parseLong(codigoTxt);
                    if (cargaModelos) {
                        this.insertarModeloSO(IdModelSO,IdentitySO, lineaFile2, codTienda, idReg);
                    }else {
                        // validar si los modelos estan en op_opertnd un registro del modelo a la tienda
                        this.insertarModeloOperadorTnd(IdentitySO, codTienda, idReg, 2);
                    }
                }else {
                    if (cargaModelos) {
                        //insertar operador con permisos
                        this.insertarOperadorSO(IdentitySO, storeDao, lineaFile2, codTienda, idReg,fileNvlAutoMod);
                    } else {
                        //int nivelAuto = Integer.parseInt(lineaFile2.substring(18, 20));
                        int nivelAuto = Integer.parseInt(lineaFile2.substring(lineaFile2.length() - 5,lineaFile2.length()-3));
                        if (nivelAuto == 0) {
                            //insertar operador con permisos
                            this.insertarOperadorSO(IdentitySO, storeDao, lineaFile2, codTienda, idReg,fileNvlAutoMod);
                        } else {
                            //insertar operador copiando los permisos de los modelos de la base de datos
                            this.insertarOperadorSinPermisosSO(IdentitySO, storeDao, lineaFile2, codTienda, idReg,fileNvlAutoMod);
                        }
                    }
                }
                lineasProcesadas++;
                this.updateProgress(storeDao, codTienda, lineasProcesadas * 100.00f / totalLineas);
            }
			//log.info("Finaliza lectura ARCHIVO 2(Niveles de autorizacion)");
			fr2.close();
			br2.close();
				
			log.info("Inicia a leer y procesar ARCHIVO 1 (Operador y Indicat), Tienda: "+codTienda);
			FileReader fr = new FileReader(pathFile + '/' + file1);
			BufferedReader br = new BufferedReader(fr);
			
			// procesa archivo 1 datos de SUPERMARKET
			String linea;
			while((linea = br.readLine()) != null)
			{
                //proceso archivo 1
                String identityDocument = Long.toString(Long.parseLong(linea.substring(0, 10)));
                ///ver si es modelo o operador
                boolean isModel = Integer.parseInt(linea.substring(0, 9)) == 999999999;

                log.info("registro : " + identityDocument + ", Es modelo: " + isModel);
                // vemos si el registro es un modelo o no
                if (isModel) {
                    // ver si se carga modelos o no si esta variable es falsa el modelo no se carga de los archivos
                    if (cargaModelos) {
                        // insertar modelo
                        this.insertarModelo(linea, codTienda, idReg);
                    } else {
                        // validar si los modelos estan en op_opertnd un registro del modelo a la tienda
                        this.insertarModeloOperadorTnd(identityDocument, codTienda, idReg,1);
                    }
                } else {
                    if (cargaModelos) {
                        //insertar operador con permisos
                        this.insertarOperador(storeDao, linea, codTienda, idReg);
                    } else {
                        int nivelAuto = Integer.parseInt(linea.substring(18, 20));
                        if (nivelAuto == 0) {
                            //insertar operador con permisos
                            this.insertarOperador(storeDao, linea, codTienda, idReg);
                        } else {
                            //insertar operador copiando los permisos de los modelos de la base de datos
                            this.insertarOperadorSinPermisos(storeDao, linea, codTienda, idReg);
                        }
                    }
                }
                lineasProcesadas++;
                this.updateProgress(storeDao, codTienda, lineasProcesadas * 100.00f / totalLineas);
			} 
			
			List operators = opDao.getOperatorsByOperatorStore(session, idTienda);
			if (operators != null && !operators.isEmpty()) {
				Iterator itOperators = operators.iterator();
				while (itOperators.hasNext()) {
                    Operator operador = null;
                    operador = (Operator) itOperators.next();
                    operador.setDownload(DOWNLOAD);
                    opDao.updateOperator(session, operador);
				}
			} else {
				log.info("No se encontraron operadores a descargar");
			}
			UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso+"|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|"+codTienda+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|FinalizÃ³ el proceso de carga de operadores.\n", true);
			log.info("PROCESO DE ACTUALIZACION DE OPERADORES A LA BASE DE DATOS FINALIZO DE FORMA CORRECTA.");			
			fr.close();
			br.close();
			// Borrar dos archivos ya procesados. 
			File archivo1 = new File(pathFile + '/' + file1);
			File archivo2 = new File(pathFile + '/' + file2);
	        archivo1.delete();
	        archivo2.delete();
	        respuesta = true;
	    }
	    catch(Exception e) {
	    	UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso+"|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|"+codTienda+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al iniciar el proceso de carga de operadores.\n", true);
	    	log.error(e.getMessage(), e);
	    	// ver de agregar codigo de error en la tienda para mostrar msj
	    	respuesta = false;
	    }
		
		return respuesta;
	}
	
    //Inserta para supermarket
	private void insertarModelo(String linea,Integer codTienda, int idReg) throws Exception {
		//String identityDocument = linea.substring(0, 10).trim();
		String identityDocument = Long.toString(Long.parseLong(linea.substring(0, 10)));
		
		int idModopera = Integer.parseInt(linea.substring(9, 10) + "0");
		log.info("Modelo id: " + idModopera);
		// validar si ya esta cargado el modelo. agregar log para probar.
		if(!consultaOpModopera(idModopera, 1))
		{		
			log.info("agregamos modelo");
			//OP_MODOPERA
			String descripcion = linea.substring(196, 216);
			//grupo estan en el archivo 2
			//usuario
			int nivelAuto = Integer.parseInt(linea.substring(18, 20));
			//idReg
			Date fecha = new Date();
			int estado = 1;
			int usSuite = 0;
			//int codigoCat = Integer.parseInt(linea.substring(18, 20));// seguridad mejorada 
			int segMejorada = 	idModopera == 40 ? 1 :
								idModopera == 50 ? 1 : 
								idModopera == 60 ? 1 :
								idModopera == 70 ? 1 :	
								idModopera == 80 ? 1 : 
								idModopera == 90 ? 1 : 0;
			
			// modelo 7/8/9 y sin modelo capas.
			boolean isAdmin = 	idModopera == 70 ? true :	
								idModopera == 80 ? true : 
								idModopera == 90 ? true : false;		
			
			//OP_MODMDA
			//comentar en el metodfo nuevo
			List<AuthorizesOPC> authoModels = generarIndicat(linea);
			
			for(AuthorizesOPC itemMod : authoModels)
			{
				// agregamos autorizaciones con indicat a Modelos OP_MODMDA
				if(!insertOpModMda(idModopera,itemMod.getIdIndicat(),itemMod.getIndicatOPC().getIdIndicatOPC(),itemMod.getValue())) {
					throw new Exception("Error insertando Indicat en los Modelos. En la base de datos OPERAC_EC tabla OP_MODMDA.");
				}
				
			}
	
			// usuario y grupo esta asociado a modelos de SO
			int grupo = 0;
			int usuario = 0; 
			
	        if (!insertOpModOpera(idModopera, descripcion, grupo, usuario, nivelAuto, idReg, fecha, estado, usSuite, segMejorada, isAdmin, 1)) {
	            throw new Exception("Error insertando Datos de Modelos. En la base de datos OPERAC_EC tabla OP_MODOPERA.");
	        }
	                    
			/////////////////// AGREGAMOS EN OPERADOR el MODELO para que despues haga las actualizaiones correspondientes
			log.info("agregamos modelo en la tabla de operadores.");
			Operator opeAux = opDao.getOperatorsByIdentityDocument(session,identityDocument);
			if(opeAux == null)
			{
				OperatorStore OperadorTienda = new OperatorStore();
				List<OperatorStore> tiendas = new ArrayList<>();
				
				OperadorTienda.setStatus(STATUS);
				OperadorTienda.setDownload(DOWNLOAD);
				OperadorTienda.setSubscribe(SUBSCRIBE);
				OperadorTienda.setFecha(new Date());
				
				//buscar datos de la tienda id e ip con el codTienda
				Store store = storeDao.getStoreByCode(sessionSaadmin,codTienda);
				if(store != null) {
					OperadorTienda.setCodTienda(store.getStoreId());
					OperadorTienda.setIpTienda(store.getIp());
					OperadorTienda.setTipoModelo(1);
					tiendas.add(OperadorTienda);
							
					Operator operador = new Operator();
					
					operador.setNameAce(descripcion);
					operador.setName(descripcion);
					//operador.setApellidoP(names[1]);
					//operador.setApellidoM(names[2]);
					operador.setIniciales("MOD");	
					operador.setStatus(STATUS);
					operador.setSubscribe(SUBSCRIBE);
					operador.setNivelAut(nivelAuto);
					operador.setIdReg(idReg);
					operador.setIndSegMejorada(segMejorada);
					operador.setIdentityDocument(identityDocument);
					operador.setIdModOpera((long)idModopera); 
					// agregamos tiendas asociadas al operador.
					operador.setTiendas(tiendas);
					//add operador modelo permisos de Supermarquet
					operador.setAuthorizations(authoModels);
					//operador.setGrupo(grupo);
					//operador.setUsuario(usuario);
		
					//System.out.println("Operador: " + operador.toString());
					opDao.updateOperator(session, operador);
					log.info("EL OPERADOR: "+operador.getIdentityDocument() +", SE CREO DE FORMA CORRECTA.");
				
					// agregar movimiento de add operator
					if(!insertOpOperaMov(operador.getIdentityDocument(),codTienda,idReg,store.getIp())) {
						throw new Exception("Error insertando Movimientos de Operadores en la base de datos OPERAC_EC.");
					}
					
				}else {
					log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
				}
			}else {
				log.info("El modelo operador supermarket: "+ identityDocument.toString() +", YA ESTA CARGADO EN LA SUITE.");
			}
			///////////////////
			
		}else {
			log.info("Ya existe el modelo");
			this.insertarModeloOperadorTnd(identityDocument, codTienda, idReg,1);
		}
	
	}
    
	private void insertarModeloSO(Long idModopera, String IdentitySo, String linea,Integer codTienda, int idReg) throws Exception {
		log.info("Modelo id: " + idModopera);
		// validar si ya esta cargado el modelo. agregar log para probar.
		if(!consultaOpModopera(idModopera, 2))
		{		
	        log.info("agregamos modelo");
	        //OP_MODOPERAlinea.substring(0, 9)
	        String descripcion = linea.substring(0, 10);
	        while(descripcion.startsWith("0")){
	            descripcion = descripcion.replaceFirst("0", "");
	        }
	        //grupo estan en el archivo 2
	        //usuario
	        int nivelAuto = Integer.parseInt(linea.substring(linea.length() - 5,linea.length()-3));
	        //idReg
	        Date fecha = new Date();
	        int estado = 1;
	        int usSuite = 0;
	        //int codigoCat = Integer.parseInt(linea.substring(18, 20));// seguridad mejorada 
	        int segMejorada = 1; //idModopera == 40 ? 1 : idModopera == 50 ? 1 : idModopera == 60 ? 1 : idModopera == 70 ? 1 : idModopera == 80 ? 1 : idModopera == 90 ? 1 : 0;
	        boolean isAdmin = true;
	
	        int grupo = 0;
	        int usuario = 0;
	
	        List<LevelAuthorizes> levelAuthorizations = new ArrayList<>();
	        List<String> nvlAuto = new ArrayList<>();
	        for (int i = 1; i <= 104; i++) {
	            nvlAuto.add(linea.substring(i + 9, i + 10));
	        }
	
	        int idNvlAuto = 1;
	        for (String item : nvlAuto) {
	        	LevelAuthorizes operadorNvlAuto = new LevelAuthorizes();
	            if (!insertOpModNva(idModopera, idNvlAuto, item)) {
	                throw new Exception("Error insertando Niveles de autorizacion en los Modelos. En la base de datos OPERAC_EC tabla OP_MODNVA.");
	            }
	            operadorNvlAuto.setIdNvautoriza(idNvlAuto);
	            operadorNvlAuto.setValue(item);
	            levelAuthorizations.add(operadorNvlAuto);
	            idNvlAuto++;
	            //System.out.println("operadorNvlAuto: " + operadorNvlAuto.toString());
	        }            
	
	        grupo = Integer.parseInt(linea.substring(114, 117));
	        usuario = Integer.parseInt(linea.substring(117, 120));
	
	        // Agregamos los modelos
	        if (!insertOpModOpera(idModopera, descripcion, grupo, usuario, nivelAuto, idReg, fecha, estado, usSuite, segMejorada, isAdmin,2)) {
	            throw new Exception("Error insertando Datos de Modelos. En la base de datos OPERAC_EC tabla OP_MODOPERA.");
	        }
	        
	        /////////////////// AGREGAMOS EN OPERADOR el MODELO para que despues haga las actualizaiones correspondientes
	        // como saber que es un modelo, porque tiene el mismo id_modeloSO que cc_operador
			
			Operator opeAux = opDao.getOperatorsByIdentityDocument(session,IdentitySo);
			if(opeAux == null)
			{
				OperatorStore OperadorTienda = new OperatorStore();
				List<OperatorStore> tiendas = new ArrayList<>();
				
				OperadorTienda.setTipoModelo(2);
				OperadorTienda.setStatus(STATUS);
				OperadorTienda.setDownload(DOWNLOAD);
				OperadorTienda.setSubscribe(SUBSCRIBE);
				OperadorTienda.setFecha(new Date());
								
				//buscar datos de la tienda id e ip con el codTienda
				Store store = storeDao.getStoreByCode(sessionSaadmin,codTienda);
				if(store != null) {
					OperadorTienda.setCodTienda(store.getStoreId());
					OperadorTienda.setIpTienda(store.getIp());
					
					tiendas.add(OperadorTienda);
							
					Operator operador = new Operator();
					
					operador.setNameAce(descripcion);
					operador.setName(descripcion);
					//operador.setApellidoP(names[1]);
					//operador.setApellidoM(names[2]);
					operador.setIniciales("MOD");	
					operador.setStatus(STATUS);
					operador.setSubscribe(SUBSCRIBE);
					operador.setNivelAutSO(nivelAuto);
					operador.setIdReg(idReg);
					operador.setIndSegMejorada(segMejorada);
					operador.setIdentityDocument(IdentitySo);
					operador.setIdModOperaSO((long)idModopera); 
					// agregamos tiendas asociadas al operador.
					operador.setTiendas(tiendas);
					//add operador modelo permisos de Supermarquet
					//operador.setAuthorizations(authoModels);
					
					operador.setGrupo(grupo);
					operador.setUsuario(usuario);
					operador.setLevelAuthorizations(levelAuthorizations);
					
					//System.out.println("Operador: " + operador.toString());
					opDao.updateOperator(session, operador);
					log.info("EL OPERADOR: "+operador.getIdentityDocument() +", SE CREO DE FORMA CORRECTA.");
				
					// agregar movimiento de add operator
					if(!insertOpOperaMov(operador.getIdentityDocument(),codTienda,idReg,store.getIp())) {
						throw new Exception("Error insertando Movimientos de Operadores en la base de datos OPERAC_EC.");
					}
					
				}else {
					log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
				}
			}else {
				log.info("El modelo operador so: "+ IdentitySo +", YA ESTA CARGADO EN LA SUITE.");
			}
			///////////////////
		
	
		}else {
			log.info("Ya existe el modelo");
			this.insertarModeloOperadorTnd(IdentitySo, codTienda, idReg,2);
		}
	
	}
	
	private void insertarModeloOperadorTnd(String identityDocument,Integer codTienda, int idReg, int tipoModelo) throws Exception {
		/////////////////// AGREGAMOS EN OPERADOR el MODELO para que despues haga las actualizaiones correspondientes
		log.info("agregamos modelo en la tabla de operadores.");
		Operator operador = opDao.getOperatorsByIdentityDocument(session,identityDocument);
		if(operador != null)
		{
			//buscar datos de la tienda id e ip con el codTienda
			Store store = storeDao.getStoreByCode(sessionSaadmin,codTienda);
			if(store != null) {
				if(!opDao.getOperatorsByIdentityDocumentAndStore(session,operador.getOperadorId(),store.getStoreId(),tipoModelo))
				{
	
					OperatorStore operadorTiendaNew = new OperatorStore();
					operadorTiendaNew.setStatus(STATUS);
					operadorTiendaNew.setDownload(DOWNLOAD);
					operadorTiendaNew.setSubscribe(SUBSCRIBE);
					operadorTiendaNew.setFecha(new Date());
					operadorTiendaNew.setCodTienda(store.getStoreId());
					operadorTiendaNew.setIpTienda(store.getIp());
					operadorTiendaNew.setOperadorId(operador.getOperadorId());
					operadorTiendaNew.setTipoModelo(tipoModelo);
					
					// Agregar OPETND
					opDao.updateOperatorStore(session, operadorTiendaNew);
					//ACTUALIZAR ESTADO DEL OPERADOR PARA MANDAR AL REGIONAL
					operador.setStatus(STATUS);
					opDao.updateOperator(session, operador);
					// agregar movimiento de add operator
					if(!insertOpOperaMov(operador.getIdentityDocument(),codTienda,idReg,store.getIp())) {
						throw new Exception("Error insertando Movimientos de Operadores en la base de datos OPERAC_EC.");
					}
					log.info("EL MODELO-OPERADOR: "+operador.getIdentityDocument() +", SE AGREGO A LA TIENDA: "+ codTienda +"DE FORMA CORRECTA.");
				}else {
					// si esta agregado y es la misma tienda no agregar.
					log.info("EL MODELO-OPERADOR: "+ identityDocument.toString() +", YA ESTABA CARGADO, en la tienda:"+ codTienda +".");
				}
				
			}else {
				log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
			}
		}else {
			log.info("El modelo operador tnd: "+ identityDocument.toString() +", YA ESTA CARGADO EN LA SUITE.");
		}
		///////////////////		
	}
	
	private void insertarOperador(StoreDAO storeDao, String linea, int codTienda, int idReg) throws Exception {
	    log.info("inserta operador " + idReg);
		//Long identityDocument = Long.parseLong(linea.substring(0, 10));
		String identityDocument = Long.toString(Long.parseLong(linea.substring(0, 10)));
		int nivelAuto = Integer.parseInt(linea.substring(18, 20));
	    log.info("Nivel Auto --- " + nivelAuto);
		//1 = Cajero Linea/operador pos; 2 = Cajero Perfumeria/operador pos; 3 = Devoluciones; 4 = Asistentes; 5 = Administrador; 6 = Autorizador
		//7 = Consultas; 8 = Sistemas Tecnico; 9 = Sistemas Admin
		long idModOpera = nivelAuto == 1 ? 10 :
				 nivelAuto == 2 ? 20 : 
				 nivelAuto == 3 ? 30 :
				 nivelAuto == 4 ? 40 :
				 nivelAuto == 5 ? 50 :
				 nivelAuto == 6 ? 60 :
				 nivelAuto == 7 ? 70 :
				 nivelAuto == 8 ? 80 :
				 nivelAuto == 9 ? 90 : 0;
	
		// validamos que el operador no este cargado si esta cargado no lo procesamos
		Operator opeAux = opDao.getOperatorsByIdentityDocument(session,identityDocument);
		if(opeAux == null)
		{
	        log.info("No Existe ope Aux" );
			// agregamos registro en la tienda por el operador que vamos a crear
			OperatorStore OperadorTienda = new OperatorStore();
			List<OperatorStore> tiendas = new ArrayList<OperatorStore>();
			
			OperadorTienda.setStatus(STATUS);
			OperadorTienda.setDownload(DOWNLOAD);
			OperadorTienda.setSubscribe(SUBSCRIBE);
			OperadorTienda.setFecha(new Date());
			//buscar datos de la tienda id e ip con el codTienda
			Store store = storeDao.getStoreByCode(sessionSaadmin,codTienda);
			if(store != null) {
				OperadorTienda.setCodTienda(store.getStoreId());
				OperadorTienda.setIpTienda(store.getIp());
				OperadorTienda.setTipoModelo(1);
				tiendas.add(OperadorTienda);
				
				Operator operador = new Operator();
				// agregamos tiendas asociadas al operador.
				operador.setTiendas(tiendas);
				
				operador.setStatus(STATUS);
				operador.setSubscribe(SUBSCRIBE);
				
				operador.setIdReg(idReg);
				
				String claveUsu = Integer.toString((Integer.parseInt(linea.substring(10, 18)))); // para sacar los 0 de adelante
				operador.setIdentityDocument(identityDocument);
				operador.setNivelAut(nivelAuto);
				operador.setIdModOpera(idModOpera); // 30 Operador pos
	            log.info("idModOpera Auto --- " + idModOpera);
	
				int segMejorada =   idModOpera == 40 ? 1 :
									idModOpera == 50 ? 1 : 
									idModOpera == 60 ? 1 :
									idModOpera == 70 ? 1 :	
									idModOpera == 80 ? 1 : 
									idModOpera == 90 ? 1 : 0;
				
				operador.setIndSegMejorada(segMejorada);  // debe tomar de op_modopera.codigo_cat_det si es 0 o 1
				
				String NameFull = linea.substring(196, 216);
				log.info("NameFull: " + NameFull);
				String[] names = NameFull.split(" ");
				String iniciales = null;
				
				operador.setNameAce(NameFull);
				names = fixName(names);
				
				operador.setName(names[0]);
				iniciales = names[0].substring(0,1);
				// REVISAR OPERADORES CON DOBLE ESPACIO
				if(names.length >= 2)
				{
					operador.setApellidoP(names[1]);
					iniciales += names[1].substring(0,1);
				}
				if(names.length >= 3)
				{
					operador.setName(names[0]+names[1]);
					operador.setApellidoP(names[2]);
					//operador.setApellidoM(names[2]);
					iniciales += names[2].substring(0,1);
				}
				operador.setIniciales(iniciales);
				
				//List<AuthorizesOPC> authorizations = generarIndicat(linea);
				// agregamos autorizaciones con indicat a operadores
				operador.setAuthorizations(generarIndicat(linea));
				
				// AGREGAR USUARIO validar que no exista
				if(!buscarUsUsuarios(operador.getIdentityDocument(),operador.getIdentityDocument())) {
					if(!insertUsUsuarios(operador.getNameAce(), operador.getIdentityDocument(), operador.getIdentityDocument(), idReg, claveUsu)) {
						log.error("Error insertando Usuarios en la base de datos de SAADMIN.");
						//throw new Exception("Error insertando Usuarios en la base de datos de SAADMIN.");
					}
				} else {
					//ACTUALIZAR USUARIO CON EL NOMBRE
					log.info("ACTUALIZA NOMBRE DEL USUARIO " + operador.getIdentityDocument());
					if(!updateUsUsuarios(operador.getNameAce(), operador.getIdentityDocument())) {
						log.error("Error actualizando Usuarios en la base de datos de SAADMIN.");
						//throw new Exception("Error insertando Usuarios en la base de datos de SAADMIN.");
					}	
				}
				
				// agregar movimiento de add operator
				if(!insertOpOperaMov(operador.getIdentityDocument(),codTienda,idReg,store.getIp())) {
					throw new Exception("Error insertando Movimientos de Operadores en la base de datos OPERAC_EC.");
				}
				
				//System.out.println("Operador: " + operador.toString());
				opDao.updateOperator(session, operador);
				log.info("EL OPERADOR: "+operador.getIdentityDocument() +", SE CREO DE FORMA CORRECTA.");
			}else {
				log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
			}
			
		}else {
			log.info("Existe operador valido tiendas y permisos.");
	        // si esta cargado verificar que este en la tienda actual y que tenga los permisos de supermarket
	        // verificar que tenga permisos de supermarket
	        if(!consultaOpMDABool(opeAux.getOperadorId())) {
	        	log.info("Agrego permisoso y modelo Supermarket,Operador:"+opeAux.getIdentityDocument().toString()+",Modelo:"+idModOpera+",linea:"+linea);
				// si no tiene permisos los agregamos
	        	opeAux.setIdModOpera(idModOpera);
	        	opeAux.setAuthorizations(generarIndicat(linea));
	        	opeAux.setNivelAut(nivelAuto);
	        	
	        	String NameFull = linea.substring(196, 216);
				String[] names = NameFull.split(" ");
				String iniciales = null;
				
				opeAux.setNameAce(NameFull);
				names = fixName(names);
				opeAux.setName(names[0]);
				iniciales = names[0].substring(0,1);
				// REVISAR OPERADORES CON DOBLE ESPACIO
				if(names.length >= 2)
				{
					opeAux.setApellidoP(names[1]);
					iniciales += names[1].substring(0,1);
				}
				if(names.length >= 3)
				{
					opeAux.setName(names[0]+names[1]);
					opeAux.setApellidoP(names[2]);
					//operador.setApellidoM(names[2]);
					iniciales += names[2].substring(0,1);
				}
				opeAux.setIniciales(iniciales);
				//ACTUALIZAR ESTADO DEL OPERADOR PARA MANDAR AL REGIONAL
				opeAux.setStatus(STATUS);
	
	        	opDao.updateOperator(session, opeAux);
			}
	        //ACTUALIZAR USUARIO CON EL NOMBRE
			if(buscarUsUsuarios(opeAux.getIdentityDocument(),opeAux.getIdentityDocument())) {
				log.info("ACTUALIZA NOMBRE DEL USUARIO " + opeAux.getIdentityDocument());
				if(!updateUsUsuarios(opeAux.getNameAce(), opeAux.getIdentityDocument())) {
					log.error("Error actualizando Usuarios en la base de datos de SAADMIN.");
					//throw new Exception("Error insertando Usuarios en la base de datos de SAADMIN.");
				}
			}
	        
			// verificar que sea en la misma tienda 
			Store store = storeDao.getStoreByCode(sessionSaadmin,codTienda);
			if(store != null) {
				//log.info("DATOS ID OPERADOR: " + opeAux.getOperadorId());
				//log.info("DATOS CODIGO TIENDA: " + codTienda);
				if(!opDao.getOperatorsByIdentityDocumentAndStore(session,opeAux.getOperadorId(),store.getStoreId(),1))
				{
					log.info("Agrego tienda:"+store.getKey().toString()+",Operador:"+opeAux.getIdentityDocument().toString());
					//sino agregar solo en tiendas tablas(US_USUTND, OP_OPETND y OP_OPERAMOV).
					
					OperatorStore operadorTiendaNew = new OperatorStore();
					operadorTiendaNew.setStatus(STATUS);
					operadorTiendaNew.setDownload(DOWNLOAD);
					operadorTiendaNew.setSubscribe(SUBSCRIBE);
					operadorTiendaNew.setFecha(new Date());
					operadorTiendaNew.setCodTienda(store.getStoreId());
					operadorTiendaNew.setIpTienda(store.getIp());
					operadorTiendaNew.setOperadorId(opeAux.getOperadorId());
					operadorTiendaNew.setTipoModelo(1);
					// Agregar OPETND
					opDao.updateOperatorStore(session, operadorTiendaNew);
					
					//ACTUALIZAR ESTADO DEL OPERADOR PARA MANDAR AL REGIONAL
					opeAux.setStatus(STATUS);
					opDao.updateOperator(session, opeAux);
					// agregar movimiento de add operator
					if(!insertOpOperaMov(opeAux.getIdentityDocument(),codTienda,idReg,store.getIp())) {
						throw new Exception("Error insertando Movimientos de Operadores en la base de datos OPERAC_EC.");
					}
					log.info("EL OPERADOR: "+opeAux.getIdentityDocument() +", SE AGREGO A LA TIENDA: "+ codTienda +"DE FORMA CORRECTA.");
				}else {
					// si esta agregado y es la misma tienda no agregar.
					log.info("EL OPERADOR: "+ identityDocument.toString() +", YA ESTABA CARGADO.");
				}
			}else {
				log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
			}
		} // else existe operador
	}
	    
	private void insertarOperadorSO(String identityDocument,StoreDAO storeDao, String linea, int codTienda, int idReg,List<String> fileNvlAutoMod) throws Exception {
		log.info("inserta operador " + identityDocument);
		
		// validamos que el operador no este cargado si esta cargado no lo procesamos
		Operator opeAux = opDao.getOperatorsByIdentityDocument(session,identityDocument);
		int nivelAuto = Integer.parseInt(linea.substring(linea.length() - 5,linea.length()-3));
		Long idModOpera = buscaModelo(fileNvlAutoMod,linea);
		if(opeAux == null)
		{
	        log.info("No Existe ope Aux" );
			// agregamos registro en la tienda por el operador que vamos a crear
			OperatorStore OperadorTienda = new OperatorStore();
			List<OperatorStore> tiendas = new ArrayList<>();
			
			OperadorTienda.setStatus(STATUS);
			OperadorTienda.setDownload(DOWNLOAD);
			OperadorTienda.setSubscribe(SUBSCRIBE);
			OperadorTienda.setFecha(new Date());
			OperadorTienda.setTipoModelo(2);
			//buscar datos de la tienda id e ip con el codTienda
			Store store = storeDao.getStoreByCode(sessionSaadmin,codTienda);
			if(store != null) {
				OperadorTienda.setCodTienda(store.getStoreId());
				OperadorTienda.setIpTienda(store.getIp());
				tiendas.add(OperadorTienda);
				
				Operator operador = new Operator();
	
				// agregamos tiendas asociadas al operador.
				operador.setTiendas(tiendas);
				operador.setStatus(STATUS);
				operador.setSubscribe(SUBSCRIBE);
				operador.setIdReg(idReg);
				
	            //revisar no llega en documento
				String claveUsu = ""; // para sacar los 0 de adelante
				operador.setIdentityDocument(identityDocument); //cc_operador
	            log.info("Nivel Auto --- " + nivelAuto);
				operador.setNivelAutSO(nivelAuto);
				
				
				operador.setIdModOperaSO(idModOpera); // ACA VA ID MODELO SO
	            log.info("idModOperaSO Auto --- " + idModOpera);
				int segMejorada =   1;
				
				operador.setIndSegMejorada(segMejorada);  // debe tomar de op_modopera.codigo_cat_det si es 0 o 1
				
	            String descripcion = linea.substring(0, 10);
	            while(descripcion.startsWith("0")){
	                descripcion = descripcion.replaceFirst("0", "");
	            }
								
				String iniciales = descripcion.substring(0,1);
				
				operador.setNameAce(descripcion);
				operador.setName(descripcion);
				
				// REVISAR OPERADORES CON DOBLE ESPACIO
				operador.setIniciales(iniciales);
				
				List<LevelAuthorizes> levelAuthorizations = new ArrayList<>();
				List<String> nvlAuto = new ArrayList<>();
	            for (int i = 1; i <= 104; i++) {
	                nvlAuto.add(linea.substring(i + 9, i + 10));
	            }
	
	            int idNvlAuto = 1;
	            for (String item : nvlAuto) {
	                LevelAuthorizes operadorNvlAuto = new LevelAuthorizes();
	                operadorNvlAuto.setIdNvautoriza(idNvlAuto);
	                operadorNvlAuto.setValue(item);
	                levelAuthorizations.add(operadorNvlAuto);
	                idNvlAuto++;
	                //System.out.println("operadorNvlAuto: " + operadorNvlAuto.toString());
	            }
	            operador.setGrupo(Integer.parseInt(linea.substring(114, 117)));
				operador.setUsuario(Integer.parseInt(linea.substring(117, 120)));
				// agregamos autorizaciones con indicat a operadores
				operador.setLevelAuthorizations(levelAuthorizations);
				
				
				
				// AGREGAR USUARIO
				if(!buscarUsUsuarios(operador.getIdentityDocument(),operador.getIdentityDocument())) {
					if(!insertUsUsuarios(operador.getNameAce(), operador.getIdentityDocument().toString(), operador.getIdentityDocument(), idReg, claveUsu)) {
						log.error("Error insertando Usuarios en la base de datos de SAADMIN.");
						//throw new Exception("Error insertando Usuarios en la base de datos de SAADMIN.");
					}
				}
				
				// agregar movimiento de add operator
				if(!insertOpOperaMov(operador.getIdentityDocument(),codTienda,idReg,store.getIp())) {
					throw new Exception("Error insertando Movimientos de Operadores en la base de datos OPERAC_EC.");
				}
				
				//System.out.println("Operador: " + operador.toString());
				opDao.updateOperator(session, operador);
				log.info("EL OPERADOR: "+operador.getIdentityDocument() +", SE CREO DE FORMA CORRECTA.");
			}else {
				log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
			}
			
		}else {
	        log.info("Existe operador valido tiendas y permisos.");
	        // si esta cargado verificar que este en la tienda actual y que tenga los permisos de supermarket
	        // verificar que tenga permisos de SO
	        if(!consultaOpNVABool(opeAux.getOperadorId())) {
	        	log.info("Agrego permisoso y modelo Supermarket,Operador:"+opeAux.getIdentityDocument().toString()+",Modelo:"+idModOpera+",linea:"+linea);
	        	// si no tiene permisos los agregamos
	        	opeAux.setIdModOperaSO(idModOpera);
	        	opeAux.setNivelAutSO(nivelAuto);
	        	opeAux.setIndSegMejorada(1);
	        	List<LevelAuthorizes> levelAuthorizations = new ArrayList<>();
				List<String> nvlAuto = new ArrayList<>();
	            for (int i = 1; i <= 104; i++) {
	                nvlAuto.add(linea.substring(i + 9, i + 10));
	            }
	
	            int idNvlAuto = 1;
	            for (String item : nvlAuto) {
	                LevelAuthorizes operadorNvlAuto = new LevelAuthorizes();
	                operadorNvlAuto.setIdNvautoriza(idNvlAuto);
	                operadorNvlAuto.setValue(item);
	                levelAuthorizations.add(operadorNvlAuto);
	                idNvlAuto++;
	                //System.out.println("operadorNvlAuto: " + operadorNvlAuto.toString());
	            }
	            opeAux.setGrupo(Integer.parseInt(linea.substring(114, 117)));
	            opeAux.setUsuario(Integer.parseInt(linea.substring(117, 120)));
				// agregamos autorizaciones con indicat a operadores
	            opeAux.setLevelAuthorizations(levelAuthorizations);
	        	
	        	opDao.updateOperator(session, opeAux);
			}
	        
			// verificar que sea en la misma tienda 
			Store store = storeDao.getStoreByCode(sessionSaadmin,codTienda);
			if(store != null) {
				//log.info("DATOS ID OPERADOR: " + opeAux.getOperadorId());
				//log.info("DATOS CODIGO TIENDA: " + codTienda);
				if(!opDao.getOperatorsByIdentityDocumentAndStore(session,opeAux.getOperadorId(),store.getStoreId(),2))
				{
					log.info("Agrego tienda:"+store.getKey().toString()+",Operador:"+opeAux.getIdentityDocument().toString());
					//sino agregar solo en tiendas tablas(US_USUTND, OP_OPETND y OP_OPERAMOV).
					
					OperatorStore operadorTiendaNew = new OperatorStore();
					operadorTiendaNew.setStatus(STATUS);
					operadorTiendaNew.setDownload(DOWNLOAD);
					operadorTiendaNew.setSubscribe(SUBSCRIBE);
					operadorTiendaNew.setFecha(new Date());
					operadorTiendaNew.setCodTienda(store.getStoreId());
					operadorTiendaNew.setIpTienda(store.getIp());
					operadorTiendaNew.setOperadorId(opeAux.getOperadorId());
					operadorTiendaNew.setTipoModelo(2);
					
					// Agregar OPETND 
					opDao.updateOperatorStore(session, operadorTiendaNew);
					
					//ACTUALIZAR ESTADO DEL OPERADOR PARA MANDAR AL REGIONAL
					opeAux.setStatus(STATUS);
					opDao.updateOperator(session, opeAux);
					// agregar movimiento de add operator
					if(!insertOpOperaMov(opeAux.getIdentityDocument(),codTienda,idReg,store.getIp())) {
						throw new Exception("Error insertando Movimientos de Operadores en la base de datos OPERAC_EC.");
					}
					log.info("EL OPERADOR: "+opeAux.getIdentityDocument() +", SE AGREGO A LA TIENDA: "+ codTienda +"DE FORMA CORRECTA.");
				}else {
					// si esta agregado y es la misma tienda no agregar.
					log.info("EL OPERADOR: "+ opeAux.getIdentityDocument() +", YA ESTABA CARGADO.");
				}
			}else {
				log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
			}
		} // else existe operador
	}
	
	private Long buscaModelo(List<String> fileNvlAutoMod,String linea) {
		Long idModelo = (long) 0;
		
		// se puede mejorar perfonmance con un while y una bandera para cuando lo encontro
		for(String item: fileNvlAutoMod) {
			int nivelAutoMod = Integer.parseInt(item.substring(item.length() - 5,item.length()-3));
			int nivelAuto = Integer.parseInt(linea.substring(linea.length() - 5,linea.length()-3));
			
			if(nivelAutoMod == nivelAuto) {
				//10 ,114
				String permisosMod = item.substring(10,114); 
				String permisos = linea.substring(10,114); 
				if(permisosMod.equals(permisos)) {
					String codigoTxt = item.substring(0, 10);
	                for (int i = 0; i < character.length; i++) {
	                    String string = character[i];
	                    codigoTxt = codigoTxt.replaceAll("(?i)" + string, i + "");
	                    log.info("Log reempla 2" + codigoTxt);
	                }    				
					idModelo = Long.parseLong(codigoTxt);
				}
					
			}
		}
		    	
		return idModelo;
	}
	
	        
	// validar que el modelo este cargado en la tabla de operadores
	// si no esta insertar modelo en la tabla de operadores.
	// si esta solo agregar en op_opertnd un registro del modelo a la tienda
	private void insertarOperadorSinPermisosSO(String identityDocument,StoreDAO storeDao, String linea, int codTienda, int idReg,List<String> fileNvlAutoMod) throws Exception {
	    log.info("Inserta sin permisos SO");
				
		// validamos que el operador no este cargado si esta cargado verificamos que se la misma tienda.
		Operator opeAux = opDao.getOperatorsByIdentityDocument(session,identityDocument);
		int nivelAuto = Integer.parseInt(linea.substring(linea.length() - 5,linea.length()-3));
		Long idModOpera = buscaModelo(fileNvlAutoMod,linea);
		log.info("idModOpera:" + idModOpera);
		//if(idModOpera == null) idModOpera = buscaModeloSinPermisos(fileNvlAutoMod,linea);
		
		if(opeAux == null)
		{
	        log.info("NO existe opeAux" );
			// agregamos registro en la tienda por el operador que vamos a crear
			OperatorStore OperadorTienda = new OperatorStore();
			List<OperatorStore> tiendas = new ArrayList<OperatorStore>();
			
			OperadorTienda.setStatus(STATUS);
			OperadorTienda.setDownload(DOWNLOAD);
			OperadorTienda.setSubscribe(SUBSCRIBE);
			OperadorTienda.setFecha(new Date());
			//buscar datos de la tienda id e ip con el codTienda
			Store store = storeDao.getStoreByCode(sessionSaadmin,codTienda);
			if(store != null) {
				OperadorTienda.setCodTienda(store.getStoreId());
				OperadorTienda.setTipoModelo(2);
				OperadorTienda.setIpTienda(store.getIp());
				tiendas.add(OperadorTienda);
				
				Operator operador = new Operator();
				
				operador.setIdentityDocument(identityDocument);
				// agregamos tiendas asociadas al operador.
				operador.setTiendas(tiendas);
				
				operador.setStatus(STATUS);
				operador.setSubscribe(SUBSCRIBE);
				
				operador.setIdReg(idReg); 	
				
				String claveUsu = "";
				//String claveUsu = Integer.toString((Integer.parseInt(linea.substring(10, 18)))); // para sacar los 0 de adelante
	
	            log.info("Nivel Auto --- " + nivelAuto);
				operador.setNivelAutSO(nivelAuto);
				operador.setIdModOperaSO(idModOpera); 				
				operador.setIndSegMejorada(1);  // debe tomar de op_modopera.codigo_cat_det si es 0 o 1			
				
				
				String descripcion = linea.substring(0, 10);
	            while(descripcion.startsWith("0")){
	                descripcion = descripcion.replaceFirst("0", "");
	            }
								
				String iniciales = descripcion.substring(0,1);
				
				operador.setNameAce(descripcion);
				operador.setName(descripcion);
				
				// REVISAR OPERADORES CON DOBLE ESPACIO
				operador.setIniciales(iniciales);					
				
				/**************** Tomar de los modelos de la base de datos **********************/   
				List<LevelAuthorizes> levelAuthorizations = new ArrayList<LevelAuthorizes>();
				// SI NO EXISTE EN LA BASE Y VIENE CON ID LO CREA
				if(idModOpera != 0) {
					List<Object[]> nivelesAutoModelosExists = consultaOpModNVA(idModOpera);
					if (nivelesAutoModelosExists == null) idModOpera = (long) 0; 
				}
				
				if(idModOpera == 0)
				{
					List<String> nvlAuto = new ArrayList<>();
	                for (int i = 1; i <= 104; i++) {
	                    nvlAuto.add(linea.substring(i + 9, i + 10));
	                }
	
	                int idNvlAuto = 1;
	                for (String item : nvlAuto) {
	                    LevelAuthorizes operadorNvlAuto = new LevelAuthorizes();
	                    operadorNvlAuto.setIdNvautoriza(idNvlAuto);
	                    operadorNvlAuto.setValue(item);
	                    levelAuthorizations.add(operadorNvlAuto);
	                    idNvlAuto++;
	                    //System.out.println("operadorNvlAuto: " + operadorNvlAuto.toString());
	                }
	                operador.setGrupo(Integer.parseInt(linea.substring(114, 117)));
					operador.setUsuario(Integer.parseInt(linea.substring(117, 120)));
				}else {
					//proceso archivo 2				
					//solo procesamos el registro correspondiente al mismo identityDocument
					//buscar Niveles de autorizacion del modelo 0/ID_MODOPERA,1/ID_NVLAUTO,2/VALUE
					List<Object[]> nivelesAutoModelos = consultaOpModNVA(idModOpera);
					for(Object[] nivelesAuto : nivelesAutoModelos) {
						int idNvlAuto = Integer.parseInt(nivelesAuto[1].toString());
						String nivValue = nivelesAuto[2].toString();
						
						LevelAuthorizes operadorNvlAuto = new LevelAuthorizes();
						operadorNvlAuto.setIdNvautoriza(idNvlAuto);
						operadorNvlAuto.setValue(nivValue);
						
						levelAuthorizations.add(operadorNvlAuto);
					}
					//buscar en modelos grupo y usuario 0/id_modopera,1/des_modopera,2/nva_grupo,3/nva_usuario,4/nivel_aut 
					Object[] modelos = consultaOpMod(idModOpera);
					operador.setGrupo(Integer.parseInt(modelos[2].toString()));
					operador.setUsuario(Integer.parseInt(modelos[3].toString()));
				}
				
				// agregagos niveles de autorizacion al operador
				operador.setLevelAuthorizations(levelAuthorizations);				
					
				// AGREGAR USUARIO validar que ya este
				if(!buscarUsUsuarios(operador.getIdentityDocument(),operador.getIdentityDocument())) {
					if(!insertUsUsuarios(operador.getNameAce(), operador.getIdentityDocument().toString(), operador.getIdentityDocument(), idReg, claveUsu)) {
						log.error("Error insertando Usuarios en la base de datos de SAADMIN.");
						//throw new Exception("Error insertando Usuarios en la base de datos de SAADMIN.");
					}
				}
				
				// agregar movimiento de add operator
				if(!insertOpOperaMov(operador.getIdentityDocument(),codTienda,idReg,store.getIp())) {
					throw new Exception("Error insertando Movimientos de Operadores en la base de datos OPERAC_EC.");
				}
				
				//System.out.println("Operador: " + operador.toString());
				opDao.updateOperator(session, operador);
				log.info("EL OPERADOR: "+operador.getIdentityDocument() +", SE CREO DE FORMA CORRECTA.");
			}else {
				log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
			}
			
		}else {
			
			log.info("Existe operador valido tiendas y permisos.");
			// si esta cargado verificar que este en la tienda actual y que tenga los permisos de supermarket
	        // verificar que tenga permisos de SO
			if(!consultaOpNVABool(opeAux.getOperadorId())) {
				log.info("Agrego permisoso y modelo SO,Operador:"+opeAux.getIdentityDocument().toString()+",Modelo:"+idModOpera+",linea:"+linea);
				// si no tiene permisos los agregamos
	        	opeAux.setIdModOperaSO(idModOpera);
	        	opeAux.setNivelAutSO(nivelAuto);
	        	opeAux.setIndSegMejorada(1);
	        	List<LevelAuthorizes> levelAuthorizations = new ArrayList<LevelAuthorizes>();
	        	if(idModOpera == 0) //SI no tiene modelo asignado toma los datos que viene en el archivo
				{
					List<String> nvlAuto = new ArrayList<>();
	                for (int i = 1; i <= 104; i++) {
	                    nvlAuto.add(linea.substring(i + 9, i + 10));
	                }
	
	                int idNvlAuto = 1;
	                for (String item : nvlAuto) {
	                    LevelAuthorizes operadorNvlAuto = new LevelAuthorizes();
	                    operadorNvlAuto.setIdNvautoriza(idNvlAuto);
	                    operadorNvlAuto.setValue(item);
	                    levelAuthorizations.add(operadorNvlAuto);
	                    idNvlAuto++;
	                    //System.out.println("operadorNvlAuto: " + operadorNvlAuto.toString());
	                }
	                opeAux.setGrupo(Integer.parseInt(linea.substring(114, 117)));
	                opeAux.setUsuario(Integer.parseInt(linea.substring(117, 120)));
	
				} else {
					List<Object[]> nivelesAutoModelos = consultaOpModNVA(idModOpera);
					for(Object[] nivelesAuto : nivelesAutoModelos) {
						int idNvlAuto = Integer.parseInt(nivelesAuto[1].toString());
						String nivValue = nivelesAuto[2].toString();
						
						LevelAuthorizes operadorNvlAuto = new LevelAuthorizes();
						operadorNvlAuto.setIdNvautoriza(idNvlAuto);
						operadorNvlAuto.setValue(nivValue);
						
						levelAuthorizations.add(operadorNvlAuto);
					}
					//buscar en modelos grupo y usuario 0/id_modopera,1/des_modopera,2/nva_grupo,3/nva_usuario,4/nivel_aut 
					Object[] modelos = consultaOpMod(idModOpera);
					opeAux.setGrupo(Integer.parseInt(modelos[2].toString()));
					opeAux.setUsuario(Integer.parseInt(modelos[3].toString()));
				}
				
				// agregagos niveles de autorizacion al operador
				opeAux.setLevelAuthorizations(levelAuthorizations);
				
	        	
	        	opDao.updateOperator(session, opeAux);
			}
			
			
			// verificar que sea en la misma tienda 
			Store store = storeDao.getStoreByCode(sessionSaadmin,codTienda);
			if(store != null) {
				//log.info("DATOS ID OPERADOR: " + opeAux.getOperadorId());
				//log.info("DATOS CODIGO TIENDA: " + codTienda)
				OperatorStore opeStore = opDao.getOperatorStoreByCodStore(session, opeAux.getOperadorId(), store.getStoreId(),2);
				
				if(opeStore == null)
				{
					log.info("Agrego tienda:"+store.getKey().toString()+",Operador:"+opeAux.getIdentityDocument().toString());
					//sino agregar solo en tiendas tablas(US_USUTND, OP_OPETND y OP_OPERAMOV).
					
					OperatorStore operadorTiendaNew = new OperatorStore();
					operadorTiendaNew.setStatus(STATUS);
					operadorTiendaNew.setDownload(DOWNLOAD);
					operadorTiendaNew.setSubscribe(SUBSCRIBE);
					operadorTiendaNew.setFecha(new Date());
					operadorTiendaNew.setCodTienda(store.getStoreId());
					operadorTiendaNew.setIpTienda(store.getIp());
					operadorTiendaNew.setOperadorId(opeAux.getOperadorId());
					operadorTiendaNew.setTipoModelo(2);
					
					// Agregar OPETND
					opDao.updateOperatorStore(session, operadorTiendaNew);
					
					// AGREGAR USUARIO validar que ya este
					String claveUsu = "";
					if(!buscarUsUsuarios(opeAux.getIdentityDocument(),opeAux.getIdentityDocument())) {
						if(!insertUsUsuarios(opeAux.getNameAce(), opeAux.getIdentityDocument().toString(), opeAux.getIdentityDocument(), idReg, claveUsu)) {
							log.error("Error insertando Usuarios en la base de datos de SAADMIN.");
							//throw new Exception("Error insertando Usuarios en la base de datos de SAADMIN.");
						}
					}
					
					//ACTUALIZAR ESTADO DEL OPERADOR PARA MANDAR AL REGIONAL
					opeAux.setStatus(STATUS);
					opDao.updateOperator(session, opeAux);
					
					// agregar movimiento de add operator
					if(!insertOpOperaMov(opeAux.getIdentityDocument(),codTienda,idReg,store.getIp())) {
						throw new Exception("Error insertando Movimientos de Operadores en la base de datos OPERAC_EC.");
					}
					log.info("EL OPERADOR: "+opeAux.getIdentityDocument() +", SE AGREGO A LA TIENDA: "+ codTienda +"DE FORMA CORRECTA.");
				}else {
					// si esta agregado y es la misma tienda editar.
					opeStore.setStatus(STATUS);
					opeStore.setDownload(DOWNLOAD);
					opeStore.setSubscribe(SUBSCRIBE);
					opeStore.setFecha(new Date());
					opeStore.setCodTienda(store.getStoreId());
					opeStore.setIpTienda(store.getIp());
					opeStore.setOperadorId(opeAux.getOperadorId());
					
					// Agregar OPETND
					opDao.updateOperatorStore(session, opeStore);
					//ACTUALIZAR ESTADO DEL OPERADOR PARA MANDAR AL REGIONAL
					opeAux.setStatus(STATUS);
					opDao.updateOperator(session, opeAux);
					
					log.info("EL OPERADOR: "+ opeAux.getIdentityDocument() +", SE ACTUALIZO, EN LA TIENDA: "+codTienda+".");
				}
			}else {
				log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
			}
		} // else existe operador
	}
	
	private void insertarOperadorSinPermisos(StoreDAO storeDao, String linea, int codTienda, int idReg) throws Exception {
	    log.info("Inserta sin permisos");
		//String identityDocument = linea.substring(0, 10).trim();
		String identityDocument = Long.toString(Long.parseLong(linea.substring(0, 10)));
		int nivelAuto = Integer.parseInt(linea.substring(18, 20));
		
		//1 = Cajero Linea/operador pos; 2 = Cajero Perfumeria/operador pos; 3 = Devoluciones; 4 = Asistentes; 5 = Administrador; 6 = Autorizador
		//7 = Consultas; 8 = Sistemas Tecnico; 9 = Sistemas Admin
		long idModOpera = nivelAuto == 1 ? 10 :
						 nivelAuto == 2 ? 20 : 
						 nivelAuto == 3 ? 30 :
						 nivelAuto == 4 ? 40 :
						 nivelAuto == 5 ? 50 :
						 nivelAuto == 6 ? 60 :
						 nivelAuto == 7 ? 70 :
						 nivelAuto == 8 ? 80 :
						 nivelAuto == 9 ? 90 : 0;
		
		// validamos que el operador no este cargado si esta cargado verificamos que se la misma tienda.
		Operator opeAux = opDao.getOperatorsByIdentityDocument(session,identityDocument);
		if(opeAux == null)
		{
	        log.info("Existe opeAux" );
			// agregamos registro en la tienda por el operador que vamos a crear
			OperatorStore OperadorTienda = new OperatorStore();
			List<OperatorStore> tiendas = new ArrayList<OperatorStore>();
			
			OperadorTienda.setStatus(STATUS);
			OperadorTienda.setDownload(DOWNLOAD);
			OperadorTienda.setSubscribe(SUBSCRIBE);
			OperadorTienda.setFecha(new Date());
			//buscar datos de la tienda id e ip con el codTienda
			Store store = storeDao.getStoreByCode(sessionSaadmin,codTienda);
			if(store != null) {
				OperadorTienda.setCodTienda(store.getStoreId());
				OperadorTienda.setIpTienda(store.getIp());
				OperadorTienda.setTipoModelo(1);
				tiendas.add(OperadorTienda);
				
				Operator operador = new Operator();
				
				// agregamos tiendas asociadas al operador.
				operador.setTiendas(tiendas);
				
				operador.setStatus(STATUS);
				operador.setSubscribe(SUBSCRIBE);
				operador.setIdentityDocument(identityDocument);
				operador.setIdReg(idReg); 	
				
				String claveUsu = Integer.toString((Integer.parseInt(linea.substring(10, 18)))); // para sacar los 0 de adelante	
				//operador.setIdentityDocument(Long.parseLong(linea.substring(0, 10)));
				operador.setNivelAut(nivelAuto);
				operador.setIdModOpera(idModOpera); 
				
				// tendria que tomarlo del modelo
				int segMejorada = 	idModOpera == 40 ? 1 :
									idModOpera == 50 ? 1 : 
									idModOpera == 60 ? 1 :
									idModOpera == 70 ? 1 :	
									idModOpera == 80 ? 1 : 
									idModOpera == 90 ? 1 : 0;
				
				operador.setIndSegMejorada(segMejorada);  // debe tomar de op_modopera.codigo_cat_det si es 0 o 1
				
				String NameFull = linea.substring(196, 216);	
				
				String[] names = NameFull.split(" ");
				String iniciales = null;
				
				operador.setNameAce(NameFull);
				try {
					names = fixName(names);
					if(names.length >= 1)
					{
						operador.setName(names[0]);
						iniciales = names[0].substring(0,1);
					}else {
						operador.setNameAce("OPERADOR SIN NOMBRE");
						operador.setName("OPERADOR SIN NOMBRE");
					}
					
					// REVISAR OPERADORES CON DOBLE ESPACIO
					if(names.length >= 2)
					{
						operador.setApellidoP(names[1]);
						iniciales += names[1].substring(0,1);
					}
					if(names.length >= 3)
					{
						operador.setApellidoM(names[2]);
						iniciales += names[2].substring(0,1);
					}
					operador.setIniciales(iniciales);						
				}catch(Exception e1) {
					log.error(e1.getMessage(), e1);
					log.info("Operador Con Error en el nombre"+Arrays.toString(names) );
				}
				/**************** Tomar de los modelos de la base de datos **********************/
				// SI NO EXISTE EN LA BASE Y VIENE CON ID LO CREA
				if(idModOpera != 0) {
					List<Object[]> indicatModelosExists = consultaOpModMDA(idModOpera);
					if (indicatModelosExists == null) idModOpera = (long) 0; 
				}					
				if(idModOpera == 0)
				{
					operador.setAuthorizations(generarIndicat(linea));
				}else {
					List<AuthorizesOPC> authorizations = new ArrayList<AuthorizesOPC>();
					//List<String> indicats = new ArrayList<String>();
					//buscar indicat del modelo
					List<Object[]> indicatModelos = consultaOpModMDA(idModOpera);
					for(Object[] indicat : indicatModelos) {
						//0ID_MODOPERA,1ID_INDICAT,2ID_INDICATOPC,3VALUE
						int idIndicat = Integer.parseInt(indicat[1].toString());
						int idIndicatOPC = Integer.parseInt(indicat[2].toString());
						int indivalue = Integer.parseInt(indicat[3].toString());
						
						IndicatOPC indOpc = opDao.getIndicatOPCByIdOPC(session,idIndicatOPC);
						
						AuthorizesOPC operadorAuto = new AuthorizesOPC();
						operadorAuto.setIdIndicat(idIndicat);
						operadorAuto.setIndicatOPC(indOpc);
						operadorAuto.setValue(indivalue);
						authorizations.add(operadorAuto);
					}	
					// agregamos autorizaciones con indicat a operadores
					operador.setAuthorizations(authorizations);
				}
								
				// AGREGAR USUARIO validar que ya este
				if(!buscarUsUsuarios(operador.getIdentityDocument(),operador.getIdentityDocument())) {
					if(!insertUsUsuarios(operador.getNameAce(), operador.getIdentityDocument().toString(), operador.getIdentityDocument(), idReg, claveUsu)) {
						log.error("Error insertando Usuarios en la base de datos de SAADMIN.");
						//throw new Exception("Error insertando Usuarios en la base de datos de SAADMIN.");
					}
				} else {
					//ACTUALIZAR USUARIO CON EL NOMBRE
					log.info("ACTUALIZA NOMBRE DEL USUARIO " + operador.getIdentityDocument());
					if(!updateUsUsuarios(operador.getNameAce(), operador.getIdentityDocument())) {
						log.error("Error actualizando Usuarios en la base de datos de SAADMIN.");
						//throw new Exception("Error insertando Usuarios en la base de datos de SAADMIN.");
					}	
				}
				
				// agregar movimiento de add operator
				if(!insertOpOperaMov(operador.getIdentityDocument(),codTienda,idReg,store.getIp())) {
					throw new Exception("Error insertando Movimientos de Operadores en la base de datos OPERAC_EC.");
				}
				
				//System.out.println("Operador: " + operador.toString());
				opDao.updateOperator(session, operador);
				log.info("EL OPERADOR: "+operador.getIdentityDocument() +", SE CREO DE FORMA CORRECTA.");
			}else {
				log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
			}
			
		}else {
			log.info("Existe operador valido tiendas y permisos.");
			// si esta cargado verificar que este en la tienda actual y que tenga los permisos de supermarket
	        // verificar que tenga permisos de supermarket	
			if(!consultaOpMDABool(opeAux.getOperadorId())) {
	        	log.info("Agrego permisoso y modelo Supermarket,Operador:"+opeAux.getIdentityDocument().toString()+",Modelo:"+idModOpera+",linea:"+linea);
				// si no tiene permisos los agregamos
	        	opeAux.setIdModOpera(idModOpera);
	        	opeAux.setNivelAut(nivelAuto);
	        	opeAux.setAuthorizations(generarIndicat(linea));
	        	
	        	String NameFull = linea.substring(196, 216);	
				String[] names = NameFull.split(" ");
				String iniciales = null;
				
				opeAux.setNameAce(NameFull);
				names = fixName(names);
				
				if(names.length >= 1)
				{
					opeAux.setName(names[0]);
					iniciales = names[0].substring(0,1);
				}else {
					opeAux.setNameAce("OPERADOR SIN NOMBRE");
					opeAux.setName("OPERADOR SIN NOMBRE");
				}
				
				// REVISAR OPERADORES CON DOBLE ESPACIO
				if(names.length >= 2)
				{
					opeAux.setApellidoP(names[1]);
					iniciales += names[1].substring(0,1);
				}
				if(names.length >= 3)
				{
					opeAux.setApellidoM(names[2]);
					iniciales += names[2].substring(0,1);
				}
				opeAux.setIniciales(iniciales);
	        	opDao.updateOperator(session, opeAux);
			}
			//ACTUALIZAR USUARIO CON EL NOMBRE
			if(buscarUsUsuarios(opeAux.getIdentityDocument(),opeAux.getIdentityDocument())) {
				log.info("ACTUALIZA NOMBRE DEL USUARIO " + opeAux.getIdentityDocument());
				if(!updateUsUsuarios(opeAux.getNameAce(), opeAux.getIdentityDocument())) {
					log.error("Error actualizando Usuarios en la base de datos de SAADMIN.");
					//throw new Exception("Error insertando Usuarios en la base de datos de SAADMIN.");
				}
			}
	        
	        
			// verificar que sea en la misma tienda 
			Store store = storeDao.getStoreByCode(sessionSaadmin,codTienda);
			if(store != null) {
				//log.info("DATOS ID OPERADOR: " + opeAux.getOperadorId());
				//log.info("DATOS CODIGO TIENDA: " + codTienda);
				OperatorStore opeStore = opDao.getOperatorStoreByCodStore(session, opeAux.getOperadorId(), store.getStoreId(),1);
				if(opeStore == null)
				{
					log.info("Agrego tienda:"+store.getKey().toString()+",Operador:"+opeAux.getIdentityDocument().toString());
					//sino agregar solo en tiendas tablas(US_USUTND, OP_OPETND y OP_OPERAMOV).
					
					OperatorStore operadorTiendaNew = new OperatorStore();
					operadorTiendaNew.setStatus(STATUS);
					operadorTiendaNew.setDownload(DOWNLOAD);
					operadorTiendaNew.setSubscribe(SUBSCRIBE);
					operadorTiendaNew.setFecha(new Date());
					operadorTiendaNew.setCodTienda(store.getStoreId());
					operadorTiendaNew.setIpTienda(store.getIp());
					operadorTiendaNew.setOperadorId(opeAux.getOperadorId());
					operadorTiendaNew.setTipoModelo(1);
					
					// Agregar OPETND
					opDao.updateOperatorStore(session, operadorTiendaNew);
					
					// AGREGAR USUTND validar que ya este
					/*if(!insertUsUsuTnd(idUsu,store.getStoreId(),idReg)) {
						throw new Exception("Error insertando Usuarios por Tienda en la base de datos de SAADMIN.");
					}*/
					//ACTUALIZAR ESTADO DEL OPERADOR PARA MANDAR AL REGIONAL
					opeAux.setStatus(STATUS);
					opDao.updateOperator(session, opeAux);
					// agregar movimiento de add operator
					if(!insertOpOperaMov(opeAux.getIdentityDocument(),codTienda,idReg,store.getIp())) {
						throw new Exception("Error insertando Movimientos de Operadores en la base de datos OPERAC_EC.");
					}
					log.info("EL OPERADOR: "+opeAux.getIdentityDocument() +", SE AGREGO A LA TIENDA: "+ codTienda +"DE FORMA CORRECTA.");
				}else {
					// si esta agregado y es la misma tienda editar.
					opeStore.setStatus(STATUS);
					opeStore.setDownload(DOWNLOAD);
					opeStore.setSubscribe(SUBSCRIBE);
					opeStore.setFecha(new Date());
					opeStore.setCodTienda(store.getStoreId());
					opeStore.setIpTienda(store.getIp());
					opeStore.setOperadorId(opeAux.getOperadorId());
					
					// Agregar OPETND
					opDao.updateOperatorStore(session, opeStore);
					//ACTUALIZAR ESTADO DEL OPERADOR PARA MANDAR AL REGIONAL
					opeAux.setStatus(STATUS);
					opDao.updateOperator(session, opeAux);
					
					log.info("EL OPERADOR: "+ identityDocument.toString() +", SE ACTUALIZO, EN LA TIENDA: "+codTienda+".");
				}
				
			}else {
				log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
			}
		} // else existe operador
	}
	
	private List<AuthorizesOPC> processIndicat(String indicat,int idIndicat,int idIndicatOpc,OperatorDAO opDao) throws Exception {
		
		List<AuthorizesOPC> authorizations = new ArrayList<AuthorizesOPC>();		
		
		for(int i=0;i<indicat.length();i++) {
			
			IndicatOPC indOpc = opDao.getIndicatOPCById(session,idIndicat,i);
			//System.out.println("IndicatOPC: " + indOpc.toString());
			if(indOpc != null)
			{
				AuthorizesOPC operadorAuto = new AuthorizesOPC();
				operadorAuto.setIdIndicat(idIndicat);
				operadorAuto.setIndicatOPC(indOpc);
				operadorAuto.setValue(Integer.parseInt(indicat.substring(i, i+1)));
				//System.out.println("AuthorizesOPC: " + operadorAuto.toString());
				
				authorizations.add(operadorAuto);
			}
			idIndicatOpc++;
			//bitPos--;
		}
		
		return authorizations;
	}
	
	private List<AuthorizesOPC> generarIndicat(String linea) throws Exception {		
		
		List<AuthorizesOPC> authoModels = new ArrayList<AuthorizesOPC>();
		List<String> indiModels = new ArrayList<String>();
						
		indiModels.add(linea.substring(20, 36));	// 0 -> 16 bites		
		indiModels.add(linea.substring(36, 52));	// 1 -> 16 bites
		indiModels.add(linea.substring(52, 60));	// 2 -> 8 bites
		indiModels.add(linea.substring(60, 68));	// 3 -> 8 bites
		indiModels.add(linea.substring(68, 76));	// 4 -> 8 bites
		indiModels.add(linea.substring(76, 84));	// 5 -> 8 bites
		indiModels.add(linea.substring(84, 92));	// 6 -> 8 bites
		indiModels.add(linea.substring(92, 100));	// 7 -> 8 bites
		indiModels.add(linea.substring(100, 108));	// 8 -> 8 bites
		indiModels.add(linea.substring(108, 116));	// 9 -> 8 bites
		indiModels.add(linea.substring(116, 124));	// 10 -> 8 bites
		indiModels.add(linea.substring(124, 132));	// 11 -> 8 bites
		indiModels.add(linea.substring(132, 140));	// 12 -> 8 bites
		indiModels.add(linea.substring(140, 148));	// 13 -> 8 bites
		indiModels.add(linea.substring(148, 156));	// 14 -> 8 bites
		indiModels.add(linea.substring(156, 164));	// 15 -> 8 bites
		indiModels.add(linea.substring(164, 172));	// 16 -> 8 bites
		indiModels.add(linea.substring(172, 180));	// 17 -> 8 bites
		indiModels.add(linea.substring(180, 188));	// 18 -> 8 bites
		indiModels.add(linea.substring(188, 196));	// 19 -> 8 bites
		
		int idIndicatOpc = 1;
		int idIndicat = 0;
		for(String itemIndiM : indiModels) {
			authoModels.addAll(processIndicat(itemIndiM,idIndicat,idIndicatOpc,opDao));		
			idIndicatOpc += itemIndiM.length();
			idIndicat++;
		}
		
		return authoModels;
	}	
	
	private boolean insertOpOperaMov(String ccOperador,int codTienda, int idReg, String ipTienda) {
		Transaction tx = null;
		Calendar calendario = Calendar.getInstance();
		Integer hora = calendario.get(Calendar.HOUR_OF_DAY);
		Integer minutos = calendario.get(Calendar.MINUTE);
		Integer segundos = calendario.get(Calendar.SECOND);
		String sHora = hora.toString()+':'+minutos.toString()+':'+segundos.toString();
		
		try {
			tx = this.session.beginTransaction();
			Query query = this.session.createSQLQuery("INSERT INTO OP_OPERAMOV (STR_ESTADO, REG_ESTADO, CC_OPERADOR, COD_TIENDA, IDREG, HORA, IP_CLIENTE) "
																	+ " VALUES (3,1,:valor1, :valor2, :valor3, :valor4, :valor5) ");
			query.setParameter("valor1", ccOperador);
			query.setParameter("valor2", codTienda);
			query.setParameter("valor3", idReg);
			query.setParameter("valor4", sHora);
			query.setParameter("valor5", ipTienda);
			
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	private boolean buscarUsUsuarios(String cuenta, String cc_opera) {
		try {		
			Query query = this.sessionSaadmin.createSQLQuery("select * from US_USUARIOS where CUENTA = '" + cuenta + "' AND CC_OPERADOR = '"+cc_opera+"' ");
			List<BigInteger> rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return true; 
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}
	
	private boolean insertUsUsuarios(String nombre,String cuenta, String cc_opera, int idReg, String clave) {
		Transaction tx = null;
		try {
			tx = this.sessionSaadmin.beginTransaction();
			Query query = this.sessionSaadmin.createSQLQuery("INSERT INTO US_USUARIOS(NOMBRE, CUENTA, CLAVE,CC_OPERADOR, IDREG, FL_PASS) "
																	+ "VALUES (:valor1, :valor2, :valor3, :valor4, :valor5, 1)");
			query.setParameter("valor1", nombre);
			query.setParameter("valor2", cuenta);
			query.setParameter("valor3", clave);
			query.setParameter("valor4", cc_opera);
			query.setParameter("valor5", idReg);
			
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	private boolean updateUsUsuarios(String nombre,String cuenta) {
		//ACTUALIZA NOMBRE DE USUARIO
		Transaction tx = null;
		try {
			tx = this.sessionSaadmin.beginTransaction();
			Query query = this.sessionSaadmin.createSQLQuery("UPDATE saadmin.us_usuarios"
															+ "	SET nombre=:valor1"
															+ "	WHERE cuenta=:valor2");
			query.setParameter("valor1", nombre);
			query.setParameter("valor2", cuenta);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	private boolean insertOpModMda(int id_modopera,int id_indicat,int id_indicatopc, int value) {
		Transaction tx = null;
		try {
			tx = this.session.beginTransaction();
			Query query = this.session.createSQLQuery("INSERT INTO OP_MODMDA (id_modopera,id_indicat,id_indicatopc,value)"
																	+ "VALUES (:valor1, :valor2, :valor3, :valor4)");
			query.setParameter("valor1", id_modopera);
			query.setParameter("valor2", id_indicat);
			query.setParameter("valor3", id_indicatopc);
			query.setParameter("valor4", value);
	
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	private boolean insertOpModNva(long id_modopera,int id_nvlauto, String value) {
		Transaction tx = null;
		try {
			tx = this.session.beginTransaction();
			Query query = this.session.createSQLQuery("INSERT INTO OP_MODNVA(id_modopera,id_nvlauto,value)"
																	+ "VALUES (:valor1, :valor2, :valor3)");
			query.setParameter("valor1", id_modopera);
			query.setParameter("valor2", id_nvlauto);
			query.setParameter("valor3", value);
	
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	private boolean insertOpModOpera(long id_modopera,String des_modopera,int nva_grupo, int nva_usuario,int nivel_aut,int idreg,Date fecha,int est_modopera,int us_suite,int codigo_cat_det,boolean isAdmin, int tipo_modelo) {
		Transaction tx = null;
		try {
			tx = this.session.beginTransaction();
			Query query = this.session.createSQLQuery("INSERT INTO OP_MODOPERA(id_modopera,des_modopera,nva_grupo,nva_usuario,nivel_aut,idreg,fecha,est_modopera,us_suite,codigo_cat_det,isAdmin, tipo_modelo)"
																	+ "VALUES (:valor1,:valor2,:valor3,:valor4,:valor5,:valor6,:valor7,:valor8,:valor9,:valor10,:valor11,:valor12)");
			query.setParameter("valor1", id_modopera);
			query.setParameter("valor2", des_modopera);
			query.setParameter("valor3", nva_grupo);
			query.setParameter("valor4", nva_usuario);
			query.setParameter("valor5", nivel_aut);
			query.setParameter("valor6", idreg);
			query.setParameter("valor7", fecha);
			query.setParameter("valor8", est_modopera);
			query.setParameter("valor9", us_suite);
			query.setParameter("valor10", codigo_cat_det);
			query.setParameter("valor11", isAdmin);
			query.setParameter("valor12", tipo_modelo);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	private void updateProgress(StoreDAO storeDao, int codTienda, float porcentaje) {
		try {
			log.info("Actualiza estado a " + porcentaje);
			Store store = storeDao.getStoreByCode(sessionSaadmin, codTienda);
			if (store != null) {
				store.setProgressIniLoadOpe(porcentaje);
				storeDao.updateStore(sessionSaadmin, store);
				store = storeDao.getStoreByCode(sessionSaadmin, codTienda);
			} else {
				log.info("La tienda: "+ codTienda +", NO ESTA CARGADA EN LA SUITE.");
			}	
		} catch (Exception e) {
			 log.error(e.getMessage(), e);
		}
	}
	
	public boolean consultaOpModopera(long id_modopera, int tipo) {
		try {		
			Query query = this.session.createSQLQuery("select * from operac_ec.OP_MODOPERA where id_modopera = '" + id_modopera + "' and tipo_modelo = " + tipo);
			List<BigInteger> rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return true; 
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}
	
	public Object[] consultaOpMod(long id_modopera) {
		try {		
			Query query = this.session.createSQLQuery("select id_modopera,des_modopera,nva_grupo,nva_usuario,nivel_aut from operac_ec.OP_MODOPERA where id_modopera = '" + id_modopera + "'");
			List<Object[]> rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows.get(0); 
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public List<Object[]> consultaOpModMDA(long id_modopera) {
		try {		
			Query query = this.session.createSQLQuery("select ID_MODOPERA,ID_INDICAT,ID_INDICATOPC,VALUE from operac_ec.OP_MODMDA where id_modopera = '" + id_modopera + "'");
			List<Object[]> rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows; 
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public boolean consultaOpModMDABool(long id_modopera) {
		try {		
			Query query = this.session.createSQLQuery("select * from operac_ec.OP_MODMDA where id_modopera = '" + id_modopera + "'");
			List<Object[]> rows = query.list();
			return rows != null && !rows.isEmpty();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
	
	public boolean consultaOpMDABool(long id_operador) {
		try {		
			Query query = this.session.createSQLQuery("select * from operac_ec.op_operamda where id_operador = '" + id_operador + "'");
			List<Object[]> rows = query.list();
			return rows != null && !rows.isEmpty();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
	
	public List<Object[]> consultaOpModNVA(long id_modopera) {
		try {		
			log.info("consultaOpModNVA " + id_modopera);
			Query query = this.session.createSQLQuery("select ID_MODOPERA,ID_NVLAUTO,VALUE from operac_ec.OP_MODNVA where id_modopera = '" + id_modopera + "'");
			List<Object[]> rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows; 
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public boolean consultaOpModNVABool(long id_modopera) {
		try {		
			Query query = this.session.createSQLQuery("select * from operac_ec.OP_MODNVA where id_modopera = '" + id_modopera + "'");
			List<Object[]> rows = query.list();
			return rows != null && !rows.isEmpty();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
	
	public boolean consultaOpNVABool(long id_operador) {
		try {		
			Query query = this.session.createSQLQuery("select * from operac_ec.op_operanva where id_operador = '" + id_operador + "'");
			List<Object[]> rows = query.list();
			return rows != null && !rows.isEmpty();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
	
	public BigInteger consultaUsUsuarios(String cc_opera) {
		try {		
			Query query = this.sessionSaadmin.createSQLQuery("select idusu from SAADMIN.US_USUARIOS where cc_operador = '" + cc_opera + "'");
			List<BigInteger> rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows.get(0); 
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public BigInteger consultaUsUsuarios_cuentaAdmin() {
		try {		
			Query query = this.sessionSaadmin.createSQLQuery("select idusu from SAADMIN.US_USUARIOS where cuenta = 'NUOADM'");
			List<BigInteger> rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows.get(0); 
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	protected String[] fixName(String[] names) {
		//POR SI VIENE MAS DE UN ESPACIO EN BLANCO PARA QUE NO LO TOME EN EL ARRAY
		List<String> aux = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            if (names[i].length() != 0) {
                aux.add(names[i]);
            }
        }
        return aux.toArray(new String[0]);
	}
	
	protected void iniciarSesion() {
		while (session == null && !isEnd) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Opera").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private void iniciarSaadminSesion() {
		while (sessionSaadmin == null && !isEnd) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionSaadmin == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	protected void finalizaSesion() {
		if (session != null) {
			session.close();
            session = null;
        }
		if (sessionSaadmin != null) {
            sessionSaadmin.close();
            sessionSaadmin = null;
        }
	}
	
	private String getEyesFileName() {
		return prop.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}


	@Override
	public boolean shutdown(long arg0) {
		isEnd = true;
		log.info("Finalizó el Proceso de archivo para operador tienda " + storeKey + ".");
		return true;
	}

}
