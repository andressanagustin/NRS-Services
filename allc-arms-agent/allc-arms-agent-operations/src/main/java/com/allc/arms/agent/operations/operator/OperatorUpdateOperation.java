package com.allc.arms.agent.operations.operator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.ParamsUtils;
import com.allc.arms.utils.operator.OperatorWrapper;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.FileInputStream4690;
import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.security.AuthorizationRecord;
import com.ibm.OS4690.security.EnhancedSecurity;
import com.ibm.OS4690.security.EnhancedSecuritySession;
import com.thoughtworks.xstream.XStream;

public class OperatorUpdateOperation extends AbstractOperation {

    protected static Logger log = Logger.getLogger(OperatorUpdateOperation.class);

    public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
        log.info("Operator Update Operation iniciada.");
        OperatorKeyed operatorKeyed = new OperatorKeyed();
        String operatorID = null;
        try {
            String message;
            String OperatorXML = (String) frame.getBody().get(0);
            XStream xstream = new XStream();
            xstream.alias("OperatorWrapper", OperatorWrapper.class);
            OperatorWrapper operatorWrapper = (OperatorWrapper) xstream.fromXML(OperatorXML);
            operatorID = operatorWrapper.getIdentityDocument();
            Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "OPER_UPD_O|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|STR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Actualizando operador: " + operatorID + ".\n", true);

            if (ObjectUtils.notEqual(operatorWrapper, null)) {
            	int tipoModelo = operatorWrapper.getTipoModelo();
            	//boolean TienelevelAuto = operatorWrapper.getLevelAuthorizations().isEmpty();
            	// supermarket
            	if(tipoModelo == 1) {
            		if (operatorKeyed.init(properties) && operatorKeyed.process(operatorWrapper).equals(Boolean.TRUE) && updateLevelAuthorizationsModeloSupermarket(operatorWrapper, properties)) {
            			message = "0";
            			log.info("record Update supermarket, codigo operador: " + operatorWrapper.getIdentityDocument());
            		} else {
            			log.info("record Error supermarket, codigo operador: " + operatorWrapper.getIdentityDocument());
            			message = "1";
                    }
            	}else if(tipoModelo == 2) {
            		// Sistema Operativo TODOS TIENEN SEGURIDAD MEJORADA van por updateLevelAuthorizations.
            		if ((operatorWrapper.getIndSegMejorada().compareTo(0) == 0 && updateLevelAuthorizationsWithoutEnhSec(operatorWrapper, properties)
                            || updateLevelAuthorizations(operatorWrapper, properties, operatorWrapper.getIndSegMejorada()))) {
            			log.info("record Update SO, codigo operador: " + operatorWrapper.getIdentityDocument());
            			message = "0";
                    } else {
                    	log.info("record Error SO, codigo operador: " + operatorWrapper.getIdentityDocument());
                        message = "1";
                    }
            	}else {
            		log.info("Error variable tipoModelo: "+tipoModelo+", codigo operador: " + operatorWrapper.getIdentityDocument());
            		message = "1";
            	}
                operatorKeyed.closure();
            } else {
                message = "1";
            }

            StringBuffer sb = new StringBuffer(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + message);
            socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")));
            Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "OPER_UPD_O|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|END|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Operador: " + operatorID + " actualizado.\n", true);
        } catch (Exception e) {
            try {
                Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "OPER_UPD_O|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al actualizar el operador: " + operatorID + ".\n", true);
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
            log.error(e.getMessage(), e);
        }
        return false;
    }

    private String getEyesFileName(PropFile properties) {
        return properties.getObject("eyes.ups.file.name") + "_" + ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    public boolean shutdown(long timeToWait) {
        // TODO Auto-generated method stub
        return false;
    }
    // Modifica el ADXCSOUF

    private boolean updateLevelAuthorizationsWithoutEnhSec(OperatorWrapper operatorWrapper, PropFile properties) {
        log.info("Inicia sin seguridad mejorada");
        BufferedReader br = null;
        try {
            int length = operatorWrapper.getLevelAuthorizations().length();
            log.info(length + " " + operatorWrapper.getLevelAuthorizations());
            //int opGroup = Integer.valueOf(operatorWrapper.getLevelAuthorizations().substring(length-3, length)).intValue();
            int opGroup = Integer.valueOf(operatorWrapper.getLevelAuthorizations().substring(length - 6, length - 3)).intValue();
            log.info("opGroup: " + opGroup);
            String operatorId = StringUtils.rightPad(operatorWrapper.getIdentityDocument(), 9, "");
            log.info("operatorId: " + operatorId);
            boolean modelo = false;
            if (operatorId.contains("999999999")) {
                modelo = true;
                String newOp = "*".concat(operatorId.substring(9));
                log.info("nuevo operatorId: " + newOp);
                operatorId = StringUtils.rightPad(newOp, 9, "");
                log.info("nuevo operatorId: " + operatorId);
            }
            String modeloId = StringUtils.rightPad(("*" + operatorWrapper.getNivelAutSO().trim()), 9, "");

            log.info("modeloId: " + modeloId);
            br = new BufferedReader(new InputStreamReader(new FileInputStream4690(properties.getObject("operator.levelAuthorization.filename"))));
            char[] reg = new char[34];
            char[] regToCopy = new char[34];
            String regModelo = null;
            String regOperadorOld = null;
            int offset = 0;
            int posOld = 0;
            int posOldMod = 0;
            int pos = 0;
            char[] regOld = null;
            while (br.read(reg, offset, 34) > 0) {

                String regStr = new String(reg);
                String opId = regStr.substring(0, 9);
                log.info("OpId: " + opId);
                if (modeloId.equalsIgnoreCase(opId)) {
                    regToCopy = reg;
                    posOldMod = pos * 34;
                    regModelo = new String(regToCopy);
                    log.info("Encontro modelo. Reg:" + regModelo);
                }
                if (operatorId.equalsIgnoreCase(opId)) {
                    regOld = reg;
                    posOld = pos * 34;
                    regOperadorOld = new String(regOld);
                    log.info("Encontro el registro." + regOperadorOld);
                }
                //log.info("Reg: "+ regStr + ", En la posicion: " + pos); 
                pos++;
            }
            try {
                if (br != null) {
                    br.close();
                    br = null;
                }
            } catch (IOException e) {
            }
            String changePass = null;
            String operatorPass = null;
            boolean operatorDelete = false;
            String operatorReg = regOperadorOld == null ? regModelo : regOperadorOld; //new String(regToCopy);

            // si subscribe es 0 es porque se quiere bloquear
            if (operatorWrapper.getSubscribe().equals("0")) {
                changePass = StringUtils.rightPad(operatorWrapper.getPassword(), 9, "");
                log.info("Bloquea, pass: " + changePass);
                operatorReg = (changePass != null ? operatorId + changePass : operatorReg.substring(0, 18)) + operatorReg.substring(18);
                //log.info("Operador Reg bloquea: "+ operatorReg);
            } else if (operatorWrapper.getSubscribe().equals("1")) {
                // si subscribe es igual a 1 es alta o activar
                if (properties.getInt("operator.password.encripted.flag") != 0) {
                    operatorPass = StringUtils.rightPad(properties.getObject("operator.password.encripted"), 9, "");
                } else {
                    operatorPass = StringUtils.rightPad(properties.getObject("operator.password.noEncripted"), 9, "");
                }
                //log.info("Reg momento 3:" + operatorReg); 
                log.info("Activa, pass: " + operatorPass);
                //log.info("operatorId: "+operatorId);
                log.info("operatorReg: " + operatorReg);
                if (!modelo) {
                    log.info("No Es modelo");
                    operatorReg = operatorId + operatorPass + operatorReg.substring(18);
                } else {
                    log.info("Es modelo");
                }
                //log.info("Reg momento 4:" + operatorReg); 
            } else if (operatorWrapper.getSubscribe().equals("2")) {
                // si es 2 deberiamos eliminar
                operatorDelete = true;
                log.info("Elimina sin seguridad");
            }

            log.info("Operator Old: " + regOperadorOld + ", PositionOld: " + posOld);
            log.info("operatorReg: " + operatorReg);
            if (regOperadorOld != null) { // edicion
                if (operatorDelete) {	//Eliminar
                    Files.eliminaRegistroArchivo4690ByPos(properties.getObject("operator.levelAuthorization.filename"), posOld, 34);
                } else { // editar
                    if (!modelo) {// Editar modelo
                        log.info("No Es modelo edita");
                        Files.creaEscribeDataArchivo4690(properties.getObject("operator.levelAuthorization.filename"), operatorReg, true);
                    }
                }
            } else {
                if (modelo) {// Editar modelo
                    log.info("Es modelo Edita");
                    Files.creaEscribeDataArchivo4690ByPos(properties.getObject("operator.levelAuthorization.filename"), operatorReg, posOldMod);
                } else {// nuevo operador
                    log.info("No Es modelo edita");
                    Files.creaEscribeDataArchivo4690(properties.getObject("operator.levelAuthorization.filename"), operatorReg, true);
                }
            }
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
            }
        }
        //buscar en adxcsouf un registro con mismo grupo

        return true;
    }

    /* este metodo solo lo usamos para insertar cuando solo esta en el modelo de supermarket y no en sistema operativo
     * pero necesitamos que este en adxsouf tb porqeu el controlador lo agrega aca en este archivo tambien */
    private boolean updateLevelAuthorizationsModeloSupermarket(OperatorWrapper operatorWrapper, PropFile properties) {
        log.info("Inicia sin seguridad mejorada");
        if(operatorWrapper.getLevelAuthorizations().isEmpty() || operatorWrapper.getLevelAuthorizations() == null)
			operatorWrapper.setLevelAuthorizations("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN003001");      
        
        BufferedReader br = null;
        try {
            int length = operatorWrapper.getLevelAuthorizations().length();
            log.info(length + " " + operatorWrapper.getLevelAuthorizations());
            //int opGroup = Integer.valueOf(operatorWrapper.getLevelAuthorizations().substring(length-3, length)).intValue();
            int opGroup = Integer.valueOf(operatorWrapper.getLevelAuthorizations().substring(length - 6, length - 3)).intValue();
            log.info("opGroup: " + opGroup);
            String operatorId = StringUtils.rightPad(operatorWrapper.getIdentityDocument(), 9, "");
            log.info("operatorId: " + operatorId);
            boolean modelo = false;
            if (operatorId.contains("999999999")) {
                modelo = true;
                String newOp = "*".concat(operatorId.substring(9));
                log.info("nuevo operatorId: " + newOp);
                operatorId = StringUtils.rightPad(newOp, 9, "");
                log.info("nuevo operatorId: " + operatorId);
            }
            String modeloId = StringUtils.rightPad(("*" + operatorWrapper.getNivelAutSO().trim()), 9, "");

            log.info("modeloId: " + modeloId);
            br = new BufferedReader(new InputStreamReader(new FileInputStream4690(properties.getObject("operator.levelAuthorization.filename"))));
            char[] reg = new char[34];
            char[] regToCopy = new char[34];
            String regModelo = null;
            String regOperadorOld = null;
            int offset = 0;
            int posOld = 0;
            int posOldMod = 0;
            int pos = 0;
            char[] regOld = null;
            while (br.read(reg, offset, 34) > 0) {

                String regStr = new String(reg);
                String opId = regStr.substring(0, 9);
                log.info("OpId: " + opId);
                if (modeloId.equalsIgnoreCase(opId)) {
                    regToCopy = reg;
                    posOldMod = pos * 34;
                    regModelo = new String(regToCopy);
                    log.info("Encontro modelo. Reg:" + regModelo);
                }
                if (operatorId.equalsIgnoreCase(opId)) {
                    regOld = reg;
                    posOld = pos * 34;
                    regOperadorOld = new String(regOld);
                    log.info("Encontro el registro." + regOperadorOld);
                }
                //log.info("Reg: "+ regStr + ", En la posicion: " + pos); 
                pos++;
            }
            try {
                if (br != null) {
                    br.close();
                    br = null;
                }
            } catch (IOException e) {
            }
            String changePass = null;
            String operatorPass = null;
            boolean operatorDelete = false;
            String operatorReg = regOperadorOld == null ? regModelo : regOperadorOld; //new String(regToCopy);

           if (operatorWrapper.getSubscribe().equals("1")) {
                // si subscribe es igual a 1 es alta o activar
                if (properties.getInt("operator.password.encripted.flag") != 0) {
                    operatorPass = StringUtils.rightPad(properties.getObject("operator.password.encripted"), 9, "");
                } else {
                    operatorPass = StringUtils.rightPad(properties.getObject("operator.password.noEncripted"), 9, "");
                }
                //log.info("Reg momento 3:" + operatorReg); 
                log.info("Activa, pass: " + operatorPass);
                //log.info("operatorId: "+operatorId);
                log.info("operatorReg: " + operatorReg);
                if (!modelo) {
                    //log.info("No Es modelo,operatorId:"+operatorId+",operatorPass:"+operatorPass+",operatorReg.substring(18):"+operatorReg.substring(18));
                    if(operatorReg==null)
                    	operatorReg = operatorId + operatorPass;
                    else
                    	operatorReg = operatorId + operatorPass + operatorReg.substring(18);
                } else {
                    log.info("Es modelo");
                } 
            } 

            log.info("Operator Old: " + regOperadorOld + ", PositionOld: " + posOld);
            log.info("operatorReg: " + operatorReg);
            if (regOperadorOld == null) { // edicion
                if (!modelo) {// Editar modelo
                    log.info("No Es modelo edita");
                    Files.creaEscribeDataArchivo4690(properties.getObject("operator.levelAuthorization.filename"), operatorReg, true);
                }
            }
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
            }
        }
        //buscar en adxcsouf un registro con mismo grupo

        return true;
    }

    
    private static boolean updateLevelAuthorizations(OperatorWrapper operatorWrapper, PropFile properties, int indSegMejorada) {
        log.info("Inicia con seguridad mejorada");
        EnhancedSecuritySession ess = null;
        boolean isModel = false;
        boolean isMaster = false;
        try {
            String identyDocument = operatorWrapper.getIdentityDocument();
            EnhancedSecurity ass = new EnhancedSecurity();
            if (ass.isAvailable()) {
                //log.info("user: "+properties.getObject("operator.master.user"));
                //log.info("clave: "+properties.getObject("operator.master.password"));
                //log.info("clave desencriptada: "+ParamsUtils.desencriptar(properties.getObject("operator.master.password")) );
                String pass = ParamsUtils.desencriptar(properties.getObject("operator.master.password"));
                log.info("pass - " + pass);
                ess = ass.startSession(properties.getObject("operator.master.user"), pass);
                AuthorizationRecord ar = null;
                /*eliminar y salir*/
                if (operatorWrapper.getSubscribe().equals("2")) {
                	try {
                        log.info(identyDocument);
                        ar = ess.getRecord(identyDocument);
                        if(ar != null) {
    	                    log.info("Encontro el registro.");
    	                    ar.lock();
    	                    ess.delete(identyDocument);
    	                    log.info("Elimina con seguridad.");
    	                    ess.commit();
    	                    ess = null;
    	                }else {
    	                	log.info("No encontro el registro");
    	                	ess.cancel();
    	                	ess = null;
    	                }
                        return true;
                    } catch (FlexosException fe) {
                    	log.error(fe.getMessage(), fe);
                    	ess.cancel();
                    	ess = null;
                    }
                	
                }else {
                	try {
	                	try {
	                        log.info(identyDocument);
	                        ar = ess.getRecord(identyDocument);
	                        if(ar != null) {
	                        	isModel = ar.isModelRecord();
	                        	if(!isModel) {
		                        	try { // si no puede convertir en numerico lo cacheo para que no me salga y le pongo en false el master.
		                        		isMaster = Long.parseLong(identyDocument) == 99999999;
		                        	} catch (NumberFormatException ne) {
		    	                        log.error(ne.getMessage(), ne);
		    	                        isMaster=false;
		    	                    }
	                        	}
	    	                    log.info("Attributes before: " + ar.getAttributes());
	    	                    ar.lock();
	    	                }else {
	    	                	String passw = properties.getObject("operator.password.4690");
	                            ar = ess.createRecord(identyDocument, passw, true, false);
	                            log.info("Attributes doesn`t exist.");
	    	                }
	                    } catch (FlexosException fe) {
	                    	log.info("Error code:"+fe.getReturnCode());
	                        log.error(fe.getMessage(), fe);
	                        if(fe.getReturnCode() == -2132344831) //|| fe.getReturnCode() == -2132344812
							{	
								log.info("Exception metodo ess, CREAMOS EL USUARIO SO");
								String passw = properties.getObject("operator.password.4690");
	                            ar = ess.createRecord(identyDocument, passw, true, false);
	                            //log.info("Attributes doesn`t exist.");
	                            //ar.lock();
							}
							else
							{
								throw fe;	
							}
	                    }
	                	
	                	log.info("Update to: " + operatorWrapper.getLevelAuthorizations());
	                	// operador master no cambiamos permisos porque si los cambiamos a todos a N no deja mas hacer cambios en ningun lado
	                    if (!operatorWrapper.getLevelAuthorizations().isEmpty() && !isMaster) {
	                        if (!ar.getAttributes().equals(operatorWrapper.getLevelAuthorizations())) {
	                        	ar.setAttributes("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" + ar.getAttributes().substring(104,110));
	                	    	log.info("seteamos atributos en N, con valor:"+ar.getAttributes());
	                	    	ess.commit();
	                	    	ess = ass.startSession(properties.getObject("operator.master.user"), pass);
	                	    	try {
	    	                        log.info(identyDocument);
	    	                        ar = ess.getRecord(identyDocument);
	    	                        if(ar != null) {
	    	    	                    log.info("Attributes before: " + ar.getAttributes());
	    	    	                    ar.lock();
	    	    	                }else {
	    	    	                	String passw = properties.getObject("operator.password.4690");
	    	                            ar = ess.createRecord(identyDocument, passw, true, false);
	    	                            log.info("Attributes doesn`t exist.");
	    	    	                }
	    	                    } catch (FlexosException fe) {
	    	                        log.error(fe.getMessage(), fe);
	    	                    }
	                	    	
	                        	// agregar algo de logica de como actualiza.
	                            //ar.setAttributes(operatorWrapper.getLevelAuthorizations()); // ver si actualizar si son iguales
	                            if(actualizaPermisos(operatorWrapper.getLevelAuthorizations(),ar,properties)) {
	                            	log.info("Se actualizo correctamente Level authorizations");
	                            }
	                            
	                        } else {
	                            log.info("Son iguales no actualizo Level authorizations");
	                        }
	                    }
	                    log.info("Update authorization level to: " + operatorWrapper.getNivelAutSO());
	                    if (!operatorWrapper.getNivelAutSO().isEmpty()) {
	                    	short autorizationLevelWrapper = (new Integer(operatorWrapper.getNivelAutSO())).shortValue();
	                        if (ar.getAuthorizationLevel() != autorizationLevelWrapper) {
	                        	ar.setAuthorizationLevel(autorizationLevelWrapper);
	                        } else {
	                            log.info("Son iguales no actualizo Nivel de autorizacion");
	                        }
	                    }
	                    
	                    ess.commit();
	                    ess = null;
                	} catch (Exception e1) {
                        log.error(e1.getMessage(), e1);
                        ess.cancel();
                        ess = null;
                        return false;
                    }
                	// si es modelo no cambiar password
                    if (!operatorWrapper.getStatus().equals("4") && !isModel && !isMaster) {
                        log.info("Inicia el proceso de actualizacion de password");
                        for (int i = 0; i < 6; i++) {
                            try {
                                ess = ass.startSession(properties.getObject("operator.master.user"),
                                        ParamsUtils.desencriptar(properties.getObject("operator.master.password")));
                                ar = null;
                                String passw = "Ab" + Integer.valueOf(i).toString();
                                ar = ess.getRecord(identyDocument);
                            	ar.lock();
                                log.info("Password:" + passw);
                                ar.setPassword(passw, true);
                                ess.commit();
                                ess = null;
                                log.info("Password actualizada");
                            } catch (FlexosException e) {
                                log.error(e.getMessage(), e);
                                if (ess != null) {
                                    try {
                                        log.info("Cancelando session.");
                                        ess.cancel();
                                        ess = null;
                                    } catch (Exception ex) {
                                        log.error(ex.getMessage(), ex);
                                    }
                                }
                            }
                        }

                        try {
                            log.info("Inicia el proceso de actualizacion de password definitiva");
                            ess = ass.startSession(properties.getObject("operator.master.user"),
                                    ParamsUtils.desencriptar(properties.getObject("operator.master.password")));
                            ar = null;
                            String passw = operatorWrapper.getPassword();
                            if (passw == null || passw.length() == 0) {
                                passw = properties.getObject("operator.password.4690");
//    							if(indSegMejorada == 2)
//    								passw = properties.getObject("operator.password.noEncripted");
                            }
                            ar = ess.getRecord(identyDocument);
                            ar.lock();
                            log.info("Password definitiva:" + passw);
                            ar.setPassword(passw, true);
                            ess.commit();
                            ess = null;
                            log.info("Password actualizada");
                        } catch (FlexosException e) {
                            log.error(e.getMessage(), e);
                            if (ess != null) {
                                try {
                                    log.info("Cancelando session.");
                                    ess.cancel();
                                    ess = null;
                                } catch (Exception ex) {
                                    log.error(ex.getMessage(), ex);
                                }
                            }
                        }
                    }
                       
                }                
                //verifico si quedo abierta la session y la cierro.
                if (ess != null) {
                    try {
                        log.info("cerrando session sin controlar.NO GUARDA");
                        ess.cancel();
                        ess = null;
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            } else {
                log.error("EnhancedSecurity No Available");
            }
        } catch (FlexosException e) {
            log.error(e.getMessage(), e);
            if (ess != null) {
                try {
                    log.info("Cancelando session.");
                    ess.cancel();
                    ess = null;
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
            return false;
        }

        return true;
    }
    
    private static boolean actualizaPermisos(String atributosNew, AuthorizationRecord arActual,PropFile properties) throws Exception {
    	//obtenemos los permisos por niveles
    	String sistemaOperativo = properties.getObject("sistemaOperativo.version").trim(); // TCxSky, V6R5
    	log.info("Sistema Operativo:"+sistemaOperativo);
        boolean resp = false;
    	try {
	    	
	    	//String atributosActual = arActual.getAttributes();
	    	String newString = new String();
	    	// nvl 1 (1,2,3,4,5,6) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(0, 1).equals(atributosNew.substring(0, 1)) ||
	    	!arActual.getAttributes().substring(1, 2).equals(atributosNew.substring(1, 2)) ||
	    	!arActual.getAttributes().substring(2, 3).equals(atributosNew.substring(2, 3)) ||
	    	!arActual.getAttributes().substring(3, 4).equals(atributosNew.substring(3, 4)) ||
	    	!arActual.getAttributes().substring(4, 5).equals(atributosNew.substring(4, 5)) ||
	    	!arActual.getAttributes().substring(5, 6).equals(atributosNew.substring(5, 6))) {
	    		newString = atributosNew.substring(0,6) + arActual.getAttributes().substring(6);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 1 (1,2,3,4,5,6), con valor:"+atributosNew.substring(0,6));
	    	}else {
	    		log.info("Lo atributos nivel 1 (1,2,3,4,5,6), estan iguales");
	    	}
	    	// nvl 2 (36,37,39,40,38,54) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(35, 36).equals(atributosNew.substring(35, 36)) ||
	    	!arActual.getAttributes().substring(36, 37).equals(atributosNew.substring(36, 37)) ||
	    	!arActual.getAttributes().substring(38, 39).equals(atributosNew.substring(38, 39)) ||
	    	!arActual.getAttributes().substring(39, 40).equals(atributosNew.substring(39, 40)) ||
	    	!arActual.getAttributes().substring(37, 38).equals(atributosNew.substring(37, 38)) ||
	    	!arActual.getAttributes().substring(53, 54).equals(atributosNew.substring(53, 54))) {
	    		
	    		newString = arActual.getAttributes().substring(0,35)+atributosNew.substring(35,40)+arActual.getAttributes().substring(40,53)+atributosNew.substring(53, 54)+arActual.getAttributes().substring(54);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 2 (36,37,39,40,38,54), con valor:"+newString);
	    	}else {
	    		log.info("Lo atributos nivel 2 (36,37,39,40,38,54), estan iguales");
	    	}
	    	// nvl 3 (44,41,42,45,46,47,48,57,60) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(43, 44).equals(atributosNew.substring(43, 44)) ||
	    	!arActual.getAttributes().substring(40, 41).equals(atributosNew.substring(40, 41)) ||
	    	!arActual.getAttributes().substring(41, 42).equals(atributosNew.substring(41, 42)) ||
	    	!arActual.getAttributes().substring(44, 45).equals(atributosNew.substring(44, 45)) ||
	    	!arActual.getAttributes().substring(45, 46).equals(atributosNew.substring(45, 46)) ||
	    	!arActual.getAttributes().substring(46, 47).equals(atributosNew.substring(46, 47)) ||
	    	!arActual.getAttributes().substring(47, 48).equals(atributosNew.substring(47, 48)) ||
	    	!arActual.getAttributes().substring(56, 57).equals(atributosNew.substring(56, 57)) ||
	    	!arActual.getAttributes().substring(59, 60).equals(atributosNew.substring(59, 60))) {
	    		newString = arActual.getAttributes().substring(0,40)+atributosNew.substring(40,48)+arActual.getAttributes().substring(48,56)+
	    				atributosNew.substring(56, 57)+arActual.getAttributes().substring(57,59)+atributosNew.substring(59, 60)+
	    				arActual.getAttributes().substring(60);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 3 (44,41,42,45,46,47,48,57,60), con valor:"+newString);
	    	}else {
	    		log.info("Lo atributos nivel 3 (44,41,42,45,46,47,48,57,60), estan iguales");
	    	}
	    	// nvl 4 (50,25,26,35,58) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(49, 50).equals(atributosNew.substring(49, 50)) ||
	    	!arActual.getAttributes().substring(24, 25).equals(atributosNew.substring(24, 25)) ||
	    	!arActual.getAttributes().substring(25, 26).equals(atributosNew.substring(25, 26)) ||
	    	!arActual.getAttributes().substring(34, 35).equals(atributosNew.substring(34, 35)) ||
	    	!arActual.getAttributes().substring(57, 58).equals(atributosNew.substring(57, 58))) {
	    		newString = arActual.getAttributes().substring(0,24)+atributosNew.substring(24,26)+arActual.getAttributes().substring(26,34)+
	    				atributosNew.substring(34,35)+arActual.getAttributes().substring(35,49)+atributosNew.substring(49, 50) +
	    				arActual.getAttributes().substring(50,57)+atributosNew.substring(57, 58)+ arActual.getAttributes().substring(58);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 4 (50,25,26,35,58), con valor:"+newString);
	    	}else {
	    		log.info("Lo atributos nivel 4 (50,25,26,35,58), estan iguales");
	    	}
	    	// nvl 5 (51,52,53) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(50, 51).equals(atributosNew.substring(50, 51)) ||
	    	!arActual.getAttributes().substring(51, 52).equals(atributosNew.substring(51, 52)) ||
	    	!arActual.getAttributes().substring(52, 53).equals(atributosNew.substring(52, 53)) ) {
	    		newString = arActual.getAttributes().substring(0,50) + atributosNew.substring(50,53) +arActual.getAttributes().substring(53);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 5 (51,52,53), con valor:"+newString);
	    	}else {
	    		log.info("Lo atributos nivel 5 (51,52,53), estan iguales");
	    	}
	    	// nvl 6 (55,56,33,34) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(54, 55).equals(atributosNew.substring(54, 55)) ||
	    	!arActual.getAttributes().substring(55, 56).equals(atributosNew.substring(55, 56)) ||
	    	!arActual.getAttributes().substring(32, 33).equals(atributosNew.substring(32, 33)) ||
	    	!arActual.getAttributes().substring(33, 34).equals(atributosNew.substring(33, 34)) ) {
	    		newString = arActual.getAttributes().substring(0,32) + atributosNew.substring(32,34) + arActual.getAttributes().substring(34,54) + 
	    				atributosNew.substring(54,56)+arActual.getAttributes().substring(56);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 6 (55,56,33,34), con valor:"+newString);
	    	}else {
	    		log.info("Lo atributos nivel 6 (55,56,33,34), estan iguales");
	    	}
	    	// nvl 7 (43,59) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(42, 43).equals(atributosNew.substring(42, 43)) ||
	    	!arActual.getAttributes().substring(58, 59).equals(atributosNew.substring(58, 59)) ) {
	    		newString = arActual.getAttributes().substring(0,42) + atributosNew.substring(42,43) + arActual.getAttributes().substring(43,58) + 
	    				atributosNew.substring(58,59)+arActual.getAttributes().substring(59);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 7 (43,59), con valor:"+newString);
	    	}else {
	    		log.info("Lo atributos nivel 7 (43,59), estan iguales");
	    	}
	    	
	    	// nvl 8 (78,79) SKY --- (9,10,11,12,13,14,15,78,79) NNNNNN NNN TCxSky, 
	    	if(sistemaOperativo.equals("V6R5")) {
	    		log.info("nivel 8 V6R5 (9,10,11,12,13,14,15,78,79), actual:"+arActual.getAttributes().substring(77, 79)+",New:"+atributosNew.substring(77, 79));
		    	if(!arActual.getAttributes().substring(8, 9).equals(atributosNew.substring(8, 9))  ||
    			!arActual.getAttributes().substring(9, 10).equals(atributosNew.substring(9, 10))   ||
    			!arActual.getAttributes().substring(10, 11).equals(atributosNew.substring(10, 11)) ||
    			!arActual.getAttributes().substring(11, 12).equals(atributosNew.substring(11, 12)) ||
    			!arActual.getAttributes().substring(12, 13).equals(atributosNew.substring(12, 13)) ||
    			!arActual.getAttributes().substring(13, 14).equals(atributosNew.substring(13, 14)) ||
    			!arActual.getAttributes().substring(14, 15).equals(atributosNew.substring(14, 15)) ||
    			!arActual.getAttributes().substring(77, 78).equals(atributosNew.substring(77, 78)) ||
    			!arActual.getAttributes().substring(78, 79).equals(atributosNew.substring(78, 79)) ) {
		    		newString = arActual.getAttributes().substring(0,8)+ atributosNew.substring(8,15) +arActual.getAttributes().substring(15,77) + 
		    				atributosNew.substring(77,79) +arActual.getAttributes().substring(79);
		    		arActual.setAttributes(newString); 
		    		log.info("seteamos atributos nivel 8 V6R5 (9,10,11,12,13,14,15,78,79), con valor:"+newString);
		    	}else {
		    		log.info("Lo atributos nivel 8 V6R5 (9,10,11,12,13,14,15,78,79), estan iguales");
		    	}
	    	}else {
	    		log.info("nivel 8 TCxSky (78,79), actual:"+arActual.getAttributes().substring(77, 79)+",New:"+atributosNew.substring(77, 79));
		    	if(!arActual.getAttributes().substring(77, 78).equals(atributosNew.substring(77, 78)) ||
    			!arActual.getAttributes().substring(78, 79).equals(atributosNew.substring(78, 79)) ) {
		    		newString = arActual.getAttributes().substring(0,77) + atributosNew.substring(77,79) +arActual.getAttributes().substring(79);
		    		arActual.setAttributes(newString); 
		    		log.info("seteamos atributos nivel 8 (78,79) TCxSky, con valor:"+newString);
		    	}else {
		    		log.info("Lo atributos nivel 8 TCxSky (78,79), estan iguales");
		    	}
	    	}
	    	
	    	// nvl 9 (18,19,20,21,22,1000,1001) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(17, 18).equals(atributosNew.substring(17, 18)) ||
	    	!arActual.getAttributes().substring(18, 19).equals(atributosNew.substring(18, 19)) ||
	    	!arActual.getAttributes().substring(19, 20).equals(atributosNew.substring(19, 20)) ||
	    	!arActual.getAttributes().substring(20, 21).equals(atributosNew.substring(20, 21)) ||
	    	!arActual.getAttributes().substring(21, 22).equals(atributosNew.substring(21, 22)) ) {
	    		newString = arActual.getAttributes().substring(0,17) + atributosNew.substring(17,22) +arActual.getAttributes().substring(22);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 1 (1,2,3,4,5,6), con valor:"+newString);
	    	}else {
	    		log.info("Lo atributos nivel 9 (18,19,20,21,22), estan iguales");
	    	}
	    	// nvl 10 (65,66,69,70) sky -- (65,66,69,70,71,72) 6r5 NNNNNN NNN
	    	if(sistemaOperativo.equals("V6R5")) {
		    	if(!arActual.getAttributes().substring(64,65).equals(atributosNew.substring(64,65)) ||
		    	!arActual.getAttributes().substring(65, 66).equals(atributosNew.substring(65, 66)) ||
		    	!arActual.getAttributes().substring(68, 69).equals(atributosNew.substring(68, 69)) ||
		    	!arActual.getAttributes().substring(69, 70).equals(atributosNew.substring(69, 70)) ||
		    	!arActual.getAttributes().substring(69, 70).equals(atributosNew.substring(70, 71)) ||
		    	!arActual.getAttributes().substring(69, 70).equals(atributosNew.substring(71, 72))) {
		    		newString = arActual.getAttributes().substring(0,64) + atributosNew.substring(64,66) + arActual.getAttributes().substring(66,68) + 
		    				atributosNew.substring(68,72)+arActual.getAttributes().substring(72);
		    		arActual.setAttributes(newString); 
		    		log.info("seteamos atributos nivel 10 (65,66,69,70,71,72) V6R5, con valor:"+newString);
		    	}else {
		    		log.info("Lo atributos nivel 10 (65,66,69,70,71,72), estan iguales");
		    	}
	    	}else {
	    		if(!arActual.getAttributes().substring(64,65).equals(atributosNew.substring(64,65)) ||
		    	!arActual.getAttributes().substring(65, 66).equals(atributosNew.substring(65, 66)) ||
		    	!arActual.getAttributes().substring(68, 69).equals(atributosNew.substring(68, 69)) ||
		    	!arActual.getAttributes().substring(69, 70).equals(atributosNew.substring(69, 70)) ) {
		    		newString = arActual.getAttributes().substring(0,64) + atributosNew.substring(64,66) + arActual.getAttributes().substring(66,68) + 
		    				atributosNew.substring(68,70)+arActual.getAttributes().substring(70);
		    		arActual.setAttributes(newString); 
		    		log.info("seteamos atributos nivel 10 (65,66,69,70) TCxSky, con valor:"+newString);
		    	}else {
		    		log.info("Lo atributos nivel 10 (65,66,69,70), estan iguales");
		    	}
	    	}
	    	// nvl 11 (81,82,83,84,85,86,87,77,80) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(80, 81).equals(atributosNew.substring(80, 81)) ||
	    	!arActual.getAttributes().substring(81, 82).equals(atributosNew.substring(81, 82)) ||
	    	!arActual.getAttributes().substring(82, 83).equals(atributosNew.substring(82, 83)) ||
	    	!arActual.getAttributes().substring(83, 84).equals(atributosNew.substring(83, 84)) ||
	    	!arActual.getAttributes().substring(84, 85).equals(atributosNew.substring(84, 85)) ||
	    	!arActual.getAttributes().substring(85, 86).equals(atributosNew.substring(85, 86)) ||
	    	!arActual.getAttributes().substring(85, 87).equals(atributosNew.substring(85, 87)) ||
	    	!arActual.getAttributes().substring(76, 77).equals(atributosNew.substring(76, 77)) ||
	    	!arActual.getAttributes().substring(79, 80).equals(atributosNew.substring(79, 80))) {
	    		newString = arActual.getAttributes().substring(0,76)+atributosNew.substring(76,77)+arActual.getAttributes().substring(77,79)+
	    				atributosNew.substring(79,87) + arActual.getAttributes().substring(87);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 11 (81,82,83,84,85,86,87,77,80), con valor:"+newString);
	    	}else {
	    		log.info("Lo atributos nivel 1 (1,2,3,4,5,6), estan iguales");
	    	}
	    	// nvl 12 (73,75,74,76) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(72, 73).equals(atributosNew.substring(72, 73)) ||
	    	!arActual.getAttributes().substring(73, 74).equals(atributosNew.substring(73, 74)) ||
	    	!arActual.getAttributes().substring(74, 75).equals(atributosNew.substring(74, 75)) ||
	    	!arActual.getAttributes().substring(75, 76).equals(atributosNew.substring(75, 76))) {
	    		newString = arActual.getAttributes().substring(0,72)+atributosNew.substring(72,76)+arActual.getAttributes().substring(76);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 12 (73,75,74,76), con valor:"+newString);
	    	}else {
	    		log.info("Lo atributos nivel 12 (73,75,74,76), estan iguales");
	    	}
	    	// nvl 13 (97,98,99,100) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(96, 97).equals(atributosNew.substring(96, 97)) ||
	    	!arActual.getAttributes().substring(97, 98).equals(atributosNew.substring(97, 98)) ||
	    	!arActual.getAttributes().substring(98, 99).equals(atributosNew.substring(98, 99)) ||
	    	!arActual.getAttributes().substring(99, 100).equals(atributosNew.substring(99, 100))) {
	    		newString = arActual.getAttributes().substring(0,96)+atributosNew.substring(96,100)+arActual.getAttributes().substring(100);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 13 (97,98,99,100), con valor:"+newString);
	    	}else {
	    		log.info("Lo atributos nivel 13 (97,98,99,100), estan iguales");
	    	}
	    	// nvl 14 (89,90,91,92,93) NNNNNN NNN
	    	if(!arActual.getAttributes().substring(88, 89).equals(atributosNew.substring(88, 89)) ||
	    	!arActual.getAttributes().substring(89, 90).equals(atributosNew.substring(89, 90)) ||
	    	!arActual.getAttributes().substring(90, 91).equals(atributosNew.substring(90, 91)) ||
	    	!arActual.getAttributes().substring(91, 92).equals(atributosNew.substring(91, 92)) ||
	    	!arActual.getAttributes().substring(92, 93).equals(atributosNew.substring(92, 93))) {
	    		newString = arActual.getAttributes().substring(0,96)+atributosNew.substring(96,100)+arActual.getAttributes().substring(100);
	    		arActual.setAttributes(newString); 
	    		log.info("seteamos atributos nivel 14 (89,90,91,92,93) , con valor:"+newString);
	    	}else {
	    		log.info("Lo atributos nivel 14 (89,90,91,92,93), estan iguales");
	    	}
	    	resp = true;
    	}catch(Exception ex) {
    		log.error(ex.getMessage(), ex);
    		resp = false;
    		throw new Exception();
    	}
    	return resp;
    }
    
    
    public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
        // TODO Auto-generated method stub
        return false;
    }
}
