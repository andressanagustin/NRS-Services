package com.allc.arms.server.processes.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.Session;

import com.allc.ftp.client.FTPClient;
import com.allc.properties.PropFile;

public class ProcesoDat extends Proceso {

    BufferedReader reader;
    String cd_str_rt;

    public ProcesoDat(Session sessionParam, PropFile pProp) throws IOException {
        super(sessionParam, pProp, 1);
        reader = null;
        this.cd_str_rt = "";
    }

    public void descargaArchivosFtp() throws Exception {
        String stringFecha = "";
        String ftpFileNombre = "";
        String ftpFileLocal = "";
        String archivoLocal = "";
        String archivoRemoto = "";

        // Buscamos CODIPDV DEL DIA DE HOY
        //Date myDate = subtractDay(new Date());
        Date myDate = new Date();
        stringFecha = new SimpleDateFormat("yyyyMMdd").format(myDate);

        log.info("---------------------------- inicio Descarga archivos de stock ---------------------------");

        String ip = this.prop.getObject("interfaceMaestroItem.receiveFile.ftp.codipdv.host");
        String user = this.prop.getObject("interfaceMaestroItem.receiveFile.ftp.codipdv.user");
        String pass = this.prop.getObject("interfaceMaestroItem.receiveFile.ftp.codipdv.password");
        FTPClient cliFtp = new FTPClient(ip, user, pass);

        String paramTienda = this.prop.getObject("interfaceMaestroItem.process.codipdv");
        String[] listTien = paramTienda.split(",");
        ArrayList<Long> findStr = new ArrayList<>();
        for (String nroTienda : listTien) {
            findStr.add(Long.parseLong(nroTienda));
        }

        log.info("Lista de tiendas a procesar: " + findStr.toString());
        //findStr.add((long)124);
        //findStr.add((long)191);
        //findStr.add((long)194);

        //buscar en tabla de store, tiendas que tenemos cargadas	
        // 0 id_bsn_un, 1 de_str_rt, 2 cd_str_rt, 3 fl_stock_load
        List<Object[]> rsIDBSNUN = this.sql.consulta_id_bsn_un_stock_load(); // tiendas cargada en la tabla de pa_str_rtl.
        if (cliFtp.connectToServer()) {
            for (Object[] row : rsIDBSNUN) {
                if (!cliFtp.checkConnection()) {
                    throw new Exception("Se perdio la conexion con el FTP");
                }

                long id_bsn_un = Long.parseLong(row[0].toString());
                long cd_str_rt = Long.parseLong(row[2].toString());
                String de_str_rt = row[1].toString();

                //CODIP124.CODIP124				
                ftpFileNombre = "CODIP" + Long.toString(cd_str_rt) + ".CODIP" + Long.toString(cd_str_rt);
                ftpFileLocal = "CODIPDV" + Long.toString(cd_str_rt) + "_" + stringFecha + ".dat";
                //archivoRemoto   = "/var/smx/archivos/Max/Novedades/" ;		    		
                archivoRemoto = "";

                //ftpFileNombreLocal  = Long.toString(cd_str_rt) + "-" + carpeta + ".csv";
                archivoLocal = this.prop.getObject("interfaceMaestroItem.var.FileOrigen") + "/";

                try {
                    //***********************************************************************************//
                    // 								PRUEBA PARA PRODUCCION SACAR IF						 //
                    //***********************************************************************************//
                    if (findStr.contains(cd_str_rt)) {
                        //descargar archivo
                        log.info("Procesamos la tienda " + de_str_rt);

                        // consultar si ya esta cargado el archivo en la base de datos! si ya esta no hay que bajar.		
                        if (!this.sql.consulta_as_itm_load_file(ftpFileLocal)) {
                            log.info("Descargamos archivo ftp de CODIPDV de la tienda " + cd_str_rt);
                            if (cliFtp.downloadFtpFile(archivoRemoto, archivoLocal, ftpFileNombre, ftpFileLocal)) {
                                // Buscar si descargo archivo o no!
                                String sCarpAct = archivoLocal + ftpFileLocal;
                                //log.info("sCarpAct: " + sCarpAct);
                                File fileDownload = new File(sCarpAct);
                                // buscar en carpeta el archivo
                                // si esta hacer =>
                                if (fileDownload.exists() && fileDownload.isFile()) {
                                    log.info("Se descargo el archivo " + ftpFileLocal + " De forma correcta");
                                    log.info("Insertamos registro del archivo en la tabla 'AS_ITM_LOAD_FILE'");
                                    if (!this.sql.inserta_as_itm_load_file(ftpFileLocal, archivoLocal, true)) {
                                        throw new Exception("Error al insertar registro en tabla AS_ITM_LOAD_FILE.");
                                    }

                                } else {
                                    log.info("NO hay archivos para descargar del FTP en el dia de hoy.");
                                }

                            } else {
                                throw new Exception("No se pudo descargar archivos del repositorio FTP");
                            }
                        } else {
                            log.info("No descargamos archivo porque ya esta cargado. para la tienda " + de_str_rt);
                        }
                    }
                } catch (Exception e) {
                    log.info("El archivo: " + ftpFileLocal + " Tubo un problema para ser cargado.");
                    log.error(e.getMessage(), e);
                }
            }
            cliFtp.disconnectToServer();
        } else {
            //sin conexion ftp
            //log.info("NO SE PUDO CONECTAR AL FTP");
            throw new Exception("NO SE PUDO CONECTAR AL FTP");
        }
        //return true;
    }

    @Override
    public void procesaArchivo(String directorioFile, String descripcion, int idFile) throws IOException {
        log.info("obteniedo token");
        totalProcesados = 0;
        totalArchivo = 0;
        novedadesArchivo = "";
        log.info("CODIPDV - PROCESA INICIO=> lee archivo");
        // leer archivo dat
        List<Item> listItems = this.leerArchivoDAT(directorioFile, descripcion);
        total = listItems.size();
        log.info("CODIPDV - PROCESA FIN=> lee archivo");

        log.info("CODIPDV - PROCESA INICIO => guarda en bd");
        for (Item item : listItems) {
            try {

                Object[] rsITM = this.sql.consulta_as_itm_x_codigo(item.getAs_itm_cd_itm());
                // si no existe el item lo agregamos
                if (rsITM == null) { // AGREGA ITM

                    boolean fl_itm_dsc = false;
                    if (item.getAs_itm_fl_itm_dsc().toUpperCase().equals("SI")) {
                        fl_itm_dsc = true;
                    }

                    boolean fl_rp_rq = false;
                    if (item.getAs_itm_fl_rp_rq().toUpperCase().equals("SI")) {
                        fl_rp_rq = true;
                    }

                    int fl_wm_rq = 0;
                    if (item.getAs_itm_fl_wm_rq().toUpperCase().equals("SI")) {
                        fl_wm_rq = 1;
                    }

                    boolean fl_qy_rq = false;
                    if (item.getAs_itm_fl_qy_rq().toUpperCase().equals("SI")) {
                        fl_qy_rq = true;
                    }

                    boolean fl_qy_alw = false;
                    if (item.getAs_itm_fl_qy_alw().toUpperCase().equals("SI")) {
                        fl_qy_alw = true;
                    }

                    Integer rsITM_mrhrc = this.sql.consulta_co_mrhrc_gp_x_cd_mrhrc_gp_id_dpt_ps(Integer.parseInt(item.getAs_itm_cd_mrhrc_gp()), Integer.parseInt(item.getAs_itm_id_dpt_ps()));
                    // si no existe el la familia agregamos articulo sin familia.
                    if (rsITM_mrhrc == null) {
                        if (crearFamiliaDepto(Integer.parseInt(item.getAs_itm_cd_mrhrc_gp()), Integer.parseInt(item.getAs_itm_id_dpt_ps()), idFile)) {
                            rsITM_mrhrc = this.sql.consulta_co_mrhrc_gp_x_cd_mrhrc_gp_id_dpt_ps(Integer.parseInt(item.getAs_itm_cd_mrhrc_gp()), Integer.parseInt(item.getAs_itm_id_dpt_ps()));
                        } else {
                            // si hay un error, marca en la tabla del archivo que tienen errores
                            this.sql.actualiza_error_as_itm_load_file(idFile);
                            throw new Exception("al crear familias o departamentos");
                        }
                    }  // si existe esa familia agregamos el articulo con familia 

                    long id_mrhrc_gp = (int) rsITM_mrhrc;
                    //log.info("inserto articulo con familia, codFlia:"+id_mrhrc_gp);
                    if (!this.sql.inserta_as_itm_mrhrc(
                            item.getAs_itm_cd_itm(),
                            item.getAs_itm_nm_itm(),
                            id_mrhrc_gp,
                            fl_itm_dsc,
                            fl_rp_rq,
                            fl_wm_rq,
                            fl_qy_rq,
                            fl_qy_alw,
                            item.getAs_itm_str_de_itm()
                    )) {
                        // si hay un error, marca en la tabla del archivo que tienen errores
                        this.sql.actualiza_error_as_itm_load_file(idFile);
                        throw new Exception("Error al ingresar item en la tabla AS_ITM");
                    }

                    Object[] rsIDITM = this.sql.consulta_as_itm_x_codigo(item.getAs_itm_cd_itm());
                    // Valida qeu se inserto de forma correcta
                    if (rsIDITM == null) {
                        this.sql.actualiza_error_as_itm_load_file(idFile);
                        throw new Exception("El ítem " + item.getAs_itm_cd_itm() + " no existe");
                    }
                    long id_itm = Integer.parseInt(rsIDITM[0].toString());

                    /**
                     * ************************************************************************************************
                     */
                    /**
                     * ***************************** Inserta registro en
                     * AS_ITM_IMAGEN ********************************
                     */
                    // Lo vamos a ingresar por otro lado
                    /**
                     * ************************************************************************************************
                     */
                    /**
                     * ******************************************* AZUCAR
                     * *********************************************
                     */
                    // NO USAN
                    /**
                     * ******************************************* SAL
                     * ******************************************************
                     */
                    // NO USAN
                    /**
                     * ****************************************** GRASA
                     * ******************************************************
                     */
                    // NO USAN
                    /**
                     * *********************************************************************************************************
                     */
                    // buscamos el id de las distintas tiendas cd_str_rt 
                    List<Integer> rsIDBSNUN = this.sql.consulta_id_bsn_un_x_codigo(Long.parseLong(this.cd_str_rt));

                    if (rsIDBSNUN != null) {
                        // Recorremos por cada tienda TENDRIA QUE TRAER SOLO UNA
                        for (int row : rsIDBSNUN) {
                            long id_bsn_un = row;

                            boolean fl_azn_fr_sls = false;
                            if (item.getAs_itm_fl_azn_fr_sls().toUpperCase().equals("SI")) {
                                fl_azn_fr_sls = true;
                            }

                            int tx_a = 0;
                            if (item.getAs_itm_tx_a().toUpperCase().equals("SI")) {
                                tx_a = 1;
                            }

                            int flg_may = 0;
                            if (item.getAs_itm_flg_may().toUpperCase().equals("SI")) {
                                flg_may = 1;
                            }

                            // Inserta un registro en AS_ITM_STR por cada tienda
                            if (!this.sql.inserta_as_itm_str(
                                    id_itm,
                                    id_bsn_un,
                                    (Float.parseFloat(item.getAs_itm_sls_prc()) / 100),
                                    fl_azn_fr_sls,
                                    tx_a,
                                    flg_may,
                                    (int) (Float.parseFloat(item.getAs_itm_qty_may())),
                                    Float.parseFloat(item.getAs_itm_prc_may()),
                                    item.getAs_itm_color(),
                                    item.getAs_itm_diseno(),
                                    item.getAs_itm_marca(),
                                    item.getAs_itm_presentacion(),
                                    Float.parseFloat(item.getAs_itm_stock())
                            )) {
                                this.sql.actualiza_error_as_itm_load_file(idFile);
                                throw new Exception("Error al ingresar los items en la tabla AS_ITM_STR");

                            } else {
                                totalProcesados++;
                            }

                            // preguntar si ya existe no agregar ni editar
                            String rsIDPS = this.sql.consulta_id_ps(id_itm, id_bsn_un);
                            if (rsIDPS == null) {
                                // Inserta un registro en ID_PS para los codigos de barra por cada item en cada tienda
                                if (!this.sql.inserta_id_ps(
                                        item.getId_ps_id_itm_ps(),
                                        id_itm,
                                        id_bsn_un
                                )) {
                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                    throw new Exception("Error al ingresar los c�digos de barra en la tabla ID_PS");
                                }
                            } else {
                                if (!this.sql.actualiza_id_ps(
                                        item.getId_ps_id_itm_ps(),
                                        id_itm,
                                        id_bsn_un
                                )) ;
                            }
                        }
                    } else {
                        this.sql.actualiza_error_as_itm_load_file(idFile);
                        throw new Exception("No se encontro la tienda cargada en la tabla AS_ITM_STR, cod_tienda: " + this.cd_str_rt);
                    }

                } else {  // UPDATE
                    long id_itm = Integer.parseInt(rsITM[0].toString());

                    boolean fl_rp_rq = false;
                    if (item.getAs_itm_fl_rp_rq().toUpperCase().equals("SI")) {
                        fl_rp_rq = true;
                    }

                    int fl_wm_rq = 0;
                    if (item.getAs_itm_fl_wm_rq().toUpperCase().equals("SI")) {
                        fl_wm_rq = 1;
                    }

                    Integer rsITM_mrhrc = this.sql.consulta_co_mrhrc_gp_x_cd_mrhrc_gp_id_dpt_ps(Integer.parseInt(item.getAs_itm_cd_mrhrc_gp()), Integer.parseInt(item.getAs_itm_id_dpt_ps()));
                    // si no existe el la familia la creamos
                    if (rsITM_mrhrc == null) {
                        if (crearFamiliaDepto(Integer.parseInt(item.getAs_itm_cd_mrhrc_gp()), Integer.parseInt(item.getAs_itm_id_dpt_ps()), idFile)) {
                            rsITM_mrhrc = this.sql.consulta_co_mrhrc_gp_x_cd_mrhrc_gp_id_dpt_ps(Integer.parseInt(item.getAs_itm_cd_mrhrc_gp()), Integer.parseInt(item.getAs_itm_id_dpt_ps()));
                        } else {
                            // si hay un error, marca en la tabla del archivo que tienen errores
                            this.sql.actualiza_error_as_itm_load_file(idFile);
                            throw new Exception("al crear familias o departamentos");
                        }
                    }

                    long id_mrhrc_gp = (int) rsITM_mrhrc;
                    //log.info("Actualizo articulo con familia, codFlia:"+id_mrhrc_gp);
                    if (!this.sql.actualiza_as_itm_mrhrc(
                            item.getAs_itm_cd_itm(),
                            item.getAs_itm_nm_itm(),
                            id_mrhrc_gp,
                            fl_rp_rq,
                            fl_wm_rq,
                            id_itm,
                            item.getAs_itm_str_de_itm()
                    )) {
                        // si hay un error, marca en la tabla del archivo que tienen errores
                        this.sql.actualiza_error_as_itm_load_file(idFile);
                        throw new Exception("Error al ingresar item en la tabla AS_ITM");
                    }

                    //*********************************** TIENDAS ************************************************************//
                    // buscamos el id de las distintas tiendas cd_str_rt
                    List<Integer> rsIDBSNUN = this.sql.consulta_id_bsn_un_x_codigo(Long.parseLong(this.cd_str_rt));
                    if (rsIDBSNUN != null) {
                        // Recorremos por cada tienda TENDRIA QUE TRAER SOLO UNA
                        for (int row : rsIDBSNUN) {
                            long id_bsn_un = row;

                            boolean fl_azn_fr_sls = false;
                            if (item.getAs_itm_fl_azn_fr_sls().toUpperCase().equals("SI")) {
                                fl_azn_fr_sls = true;
                            }

                            int tx_a = 0;
                            if (item.getAs_itm_tx_a().toUpperCase().equals("SI")) {
                                tx_a = 1;
                            }

                            int flg_may = 0;
                            if (item.getAs_itm_flg_may().toUpperCase().equals("SI")) {
                                flg_may = 1;
                            }

                            // BUSCAR SI EL ARTICULO EXISTE EN ESA TIENDA
                            Object[] rsASITMSTR = this.sql.consulta_as_itm_str_codigos(id_itm, id_bsn_un);
                            // si no existe el registro lo agregamos agregamos.
                            if (rsASITMSTR == null) {
                                // Inserta un registro en AS_ITM_STR por cada tienda
                                if (!this.sql.inserta_as_itm_str(
                                        id_itm,
                                        id_bsn_un,
                                        (Float.parseFloat(item.getAs_itm_sls_prc()) / 100),
                                        fl_azn_fr_sls,
                                        tx_a,
                                        flg_may,
                                        (int) (Float.parseFloat(item.getAs_itm_qty_may())),
                                        Float.parseFloat(item.getAs_itm_prc_may()),
                                        item.getAs_itm_color(),
                                        item.getAs_itm_diseno(),
                                        item.getAs_itm_marca(),
                                        item.getAs_itm_presentacion(),
                                        Float.parseFloat(item.getAs_itm_stock())
                                )) {
                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                    throw new Exception("Error al ingresar los items en la tabla AS_ITM_STR");
                                } else {
                                    totalProcesados++;
                                }

                            } else {
                                // SI EXISTE LO ACTUALIZAMOS

                                // ver si ya tienen marca no cambiar
                                String marcaAux = rsASITMSTR[2] == null ? "" : rsASITMSTR[2].toString();
                                String marca = marcaAux.isEmpty() ? item.getAs_itm_marca() : marcaAux;

                                // Inserta un registro en AS_ITM_STR por cada tienda
                                if (!this.sql.actualiza_as_itm_str(
                                        (Float.parseFloat(item.getAs_itm_sls_prc()) / 100),
                                        tx_a,
                                        marca,
                                        item.getAs_itm_presentacion(),
                                        id_itm,
                                        id_bsn_un
                                )) {
                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                    throw new Exception("Error al ingresar los items en la tabla AS_ITM_STR");
                                } else {
                                    totalProcesados++;
                                }

                            }

                            //*********************************** CODIGO DE BARRA ************************************************************//
                            // preguntar si ya existe no agregar ni editar
                            String rsIDPS = this.sql.consulta_id_ps(id_itm, id_bsn_un);
                            if (rsIDPS == null) {
                                // Inserta un registro en ID_PS para los codigos de barra por cada item en cada tienda
                                if (!this.sql.inserta_id_ps(
                                        item.getId_ps_id_itm_ps(),
                                        id_itm,
                                        id_bsn_un
                                )) {
                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                    throw new Exception("Error al ingresar los códigos de barra en la tabla ID_PS");
                                }

                            } else {
                                // NO ACTUALIZA SI EXISTE QUEDA EL DE STOCK
                                if (!this.sql.actualiza_id_ps(
                                        item.getId_ps_id_itm_ps(),
                                        id_itm,
                                        id_bsn_un
                                )) {
                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                    throw new Exception("Error al ingresar los c�digos de barra en la tabla ID_PS");
                                }
                            }

                        }// fin for tiendas		
                    } else {
                        this.sql.actualiza_error_as_itm_load_file(idFile);
                        throw new Exception("No se encontro la tienda cargada en la tabla AS_ITM_STR, cod_tienda: " + this.cd_str_rt);
                    }
                }  //// FIN UPDATE

            } catch (Exception ex) {
                log.info("ERROR guardando en db - Proceso codipdv, cd. Items: " + item.getAs_itm_cd_itm());
                log.error(ex.getMessage(), ex);
                novedadesArchivo = novedadesArchivo.concat(ex.getMessage() + " & " + item.getAs_itm_cd_itm() + " | ");
            }
        }
        log.info("CODIPDV - PROCESA FIN => guarda en bd");
    }

    private boolean crearFamiliaDepto(Integer idFlia, Integer idDepto, Integer idFile) throws Exception {
        boolean result = false;

        if (this.sql.consulta_id_dpt_ps(idDepto) == null) {
            //log.info("inserto departamento, codDepto:"+idDepto);
            if (!this.sql.inserta_id_dpt_ps(idDepto, "Insertado por proceso CODIPDV, codigo:" + idDepto)) {
                this.sql.actualiza_error_as_itm_load_file(idFile);
                throw new Exception("Error al inserta departamento en la tabla ID_DPT_PS");
            }
        }
        //log.info("inserto familia, codFlia:"+idFlia);
        if (!this.sql.inserta_co_mrhrc_gp(idFlia, "Insertado por proceso CODIPDV, codigo:" + idFlia, idDepto)) {
            this.sql.actualiza_error_as_itm_load_file(idFile);
            throw new Exception("Error al ingresar familia en la tabla ID_DPT_PS");
        } else {
            result = true;
        }

        return result;
    }

    public List<Item> leerArchivoDAT(String directorioFile, String descripcion) throws IOException {
        String line = "";
        //int cantMax = 1000000; //cantidad maxima?
        int cantProc = 0;

        File fileToProcess = new File(directorioFile, descripcion);
        // Creamos instancia del archio a procesar.
        reader = new BufferedReader(new FileReader(fileToProcess));

        //traemos el primer registro QUE ES UN ENCABEZADO solo sacamos el cd_str_rt de la tienda
        line = reader.readLine();
        this.cd_str_rt = line.substring(1, 4);

        //traemos el segundo registro ESTE YA ES UN ITEMS
        line = reader.readLine();

        List<Item> listItems = new ArrayList<Item>();

        // iteramos por cada registro 'row'
        //while (line != null && cantMax > 0 ) {
        while (line != null) {
            if (!line.trim().isEmpty()) {
                try {
                    // crea un items para asignar los valores del archivo
                    Item itemAux = new Item();

                    //Cod_art "ITM" + sacarle un digito el ultimo es el digito verificador
                    // pedido de rodrigo si es con 26 sacar los ultimos 6 ceros aca y en el codigo de barra
                    //itemAux.setAs_itm_cd_itm(line.substring(1, 14));
                    String cd_itm = line.substring(1, 13);
                    if (cd_itm.substring(0, 2).equals("26") && Long.parseLong(cd_itm) >= Long.parseLong("260000000000")
                            && Long.parseLong(cd_itm) < Long.parseLong("270000000000") && cd_itm.substring(6, 12).equals("000000")) {
                        cd_itm = cd_itm.substring(0, 6);
                    }
                    itemAux.setAs_itm_cd_itm(cd_itm);

                    //NOMBRE_ARTICULO (50)
                    itemAux.setAs_itm_nm_itm(line.substring(17, 18) + line.substring(18, 57).trim().toLowerCase());

                    //FLAG_PERMITE_DESCUENTO (SI / NO)	
                    itemAux.setAs_itm_fl_itm_dsc("SI");

                    //REQUIERE_PRECIO (SI / NO)	
                    itemAux.setAs_itm_fl_rp_rq(line.charAt(227) == '1' ? "SI" : "NO");

                    //ES_PESABLE (SI / NO)	
                    itemAux.setAs_itm_fl_wm_rq(line.charAt(231) == '3' ? "SI" : "NO");

                    //REQUIERE_CANTIDAD (SI / NO)	
                    itemAux.setAs_itm_fl_qy_rq("NO");

                    //PERMITE_CANTIDAD (SI / NO)	
                    itemAux.setAs_itm_fl_qy_alw("SI");

                    //DESCRIPCION_ITEM (45)	
                    itemAux.setAs_itm_str_de_itm(line.substring(17, 18) + line.substring(18, 57).trim().toLowerCase());

                    //LINK_IMAGEN (250)	 NO REGISTRAR
                    itemAux.setAs_itm_img("");

                    //PRECIO (14,2)	
                    itemAux.setAs_itm_sls_prc(line.substring(82, 90));

                    //ES_AUTORIZADO_VENTA (SI / NO)	
                    itemAux.setAs_itm_fl_azn_fr_sls("SI");

                    //TIENE_IVA (SI / NO)	
                    int tx_aAux = Integer.parseInt(line.substring(142, 146).trim());
                    itemAux.setAs_itm_tx_a(tx_aAux == 0 ? "NO" : "SI");

                    //TIENE_MAYOREO (SI / NO)	
                    itemAux.setAs_itm_flg_may("NO");

                    //CANTIDAD_MAYOREO_DESDE (11)	
                    itemAux.setAs_itm_qty_may("0");

                    //PRECIO_MAYOREO (14,2)	
                    itemAux.setAs_itm_prc_may("0");

                    //COLOR (10)	
                    itemAux.setAs_itm_color("");

                    //DISENO (4)	
                    itemAux.setAs_itm_diseno("");

                    //MARCA (15)	
                    itemAux.setAs_itm_marca(line.substring(166, 181).trim());

                    //PRESENTACION (15)	
                    itemAux.setAs_itm_presentacion(line.substring(151, 166).trim());

                    //STOCK (11)	
                    itemAux.setAs_itm_stock("0");

                    //CODIGO_BARRAS (CB + EAN13)	 // misma regla
                    String id_ps_itm = line.substring(1, 14);
                    if (id_ps_itm.substring(0, 2).equals("26") && Long.parseLong(id_ps_itm) >= Long.parseLong("2600000000000")
                            && Long.parseLong(id_ps_itm) < Long.parseLong("2700000000000") && id_ps_itm.substring(6, 12).equals("000000")) {
                        id_ps_itm = id_ps_itm.substring(0, 6) + id_ps_itm.substring(12, 13);
                    }
                    itemAux.setId_ps_id_itm_ps(id_ps_itm);

                    //AZUCAR (ALTO / MEDIO / BAJO / NO CONTIENE)	
                    itemAux.setAs_itm_semaforo_azucar("");

                    //SAL (ALTO / MEDIO / BAJO / NO CONTIENE)	
                    itemAux.setAs_itm_semaforo_sal("");

                    //GRASA (ALTO / MEDIO / BAJO / NO CONTIENE)
                    itemAux.setAs_itm_semaforo_grasa("");

                    /// CLASIFICACION 
                    itemAux.setAs_itm_id_dpt_ps(line.substring(66, 70));

                    /// SUB CLASIFICACION 
                    itemAux.setAs_itm_cd_mrhrc_gp(line.substring(70, 74));

                    // Agrega el item en una lista de items
                    listItems.add(itemAux);
                    //log.info("Item añadido a la lista");

                } catch (Exception ex) {
                    log.info("ERROR leyendo archivo - Proceso codipdv, Items: " + cantProc);
                    log.error(ex.getMessage(), ex);
                    novedadesArchivo = novedadesArchivo.concat(ex.getMessage() + " & " + cantProc + " | ");
                }

            }
            cantProc++;
            totalArchivo++;
            line = reader.readLine();
        }

        reader.close();
        return listItems;
    }

}
