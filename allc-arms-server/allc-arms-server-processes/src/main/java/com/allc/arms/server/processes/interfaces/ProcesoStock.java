package com.allc.arms.server.processes.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

//import com.nuo.ftp.ClienteFTP;
public class ProcesoStock extends Proceso {

    String cd_str;
    //int id_file;

    public ProcesoStock(Session sessionParam, PropFile pProp) throws IOException {
        super(sessionParam, pProp, 2);
        this.cd_str = null;
    }

    public void descargaArchivosFtp() throws Exception {
        String carpeta = "";
        String ftpFileNombre = "";
        //String ftpFileNombreRemoto = ""; //124-20201106-total.csv 
        //String ftpFileNombreLocal = ""; //124-20201106.csv 
        String archivoLocal = "";
        String archivoRemoto = "";

        // Buscamos stock del dia de ayer!
        //Date myDate = subtractDay(new Date());
        Date myDate = new Date();
        carpeta = new SimpleDateFormat("yyyyMMdd").format(myDate);

        log.info(" ---------------------------- inicio Descarga archivos de stock ---------------------------");

        //FTPClient cliFtp = new FTPClient();
        String ip = this.prop.getObject("interfaceMaestroItem.receiveFile.ftp.host");
        String user = this.prop.getObject("interfaceMaestroItem.receiveFile.ftp.user");
        String pass = this.prop.getObject("interfaceMaestroItem.receiveFile.ftp.password");
        FTPClient cliFtp = new FTPClient(ip, user, pass);

        String paramTienda = this.prop.getObject("interfaceMaestroItem.process.stock");
        String[] listTien = paramTienda.split(",");

        ArrayList<Long> findStr = new ArrayList<Long>();
        for (String nroTienda : listTien) {
            findStr.add(Long.parseLong(nroTienda));
        }

        //buscar en tabla de store, tiendas que tenemos cargadas	
        // 0 id_bsn_un, 1 de_str_rt, 2 cd_str_rt, 3 fl_stock_load
        List<Object[]> rsIDBSNUN = this.sql.consulta_id_bsn_un_stock_load(); // tiendas habilitadas para ecommerce.
        if (cliFtp.connectToServer()) {
            for (Object[] row : rsIDBSNUN) {
                if (!cliFtp.checkConnection()) {
                    throw new Exception("Se perdio la conexion con el FTP");
                }

                long id_bsn_un = Long.parseLong(row[0].toString());
                long cd_str_rt = Long.parseLong(row[2].toString());
                String de_str_rt = row[1].toString();
                boolean fl_stock_load = Boolean.parseBoolean(row[3].toString());
                if (fl_stock_load) {
                    // ya se realizo la carga inicial
                    //ftpFileNombreRemoto	= Long.toString(cd_str_rt) + "-" + carpeta + "-parcial.csv";
                    ftpFileNombre = Long.toString(cd_str_rt) + "-" + carpeta + "-parcial.csv";
                    archivoRemoto = carpeta + "/parcial/";
                } else {
                    // Es la primer carga
                    //ftpFileNombreRemoto	= Long.toString(cd_str_rt) + "-" + carpeta + "-total.csv";
                    ftpFileNombre = Long.toString(cd_str_rt) + "-" + carpeta + "-total.csv";
                    archivoRemoto = carpeta + "/total/";
                }
                //ftpFileNombreLocal  = Long.toString(cd_str_rt) + "-" + carpeta + ".csv";
                archivoLocal = this.prop.getObject("interfaceMaestroItem.receiveFile.ftp.FileDownload") + "/" + carpeta + "/";

                try {

                    if (findStr.contains(cd_str_rt)) {
                        //descargar archivo
                        log.info("Procesamos la tienda " + de_str_rt);

                        // consultar si ya esta cargado el archivo en la base de datos! si ya esta no hay que bajar.		
                        if (!this.sql.consulta_as_itm_load_file_stock(ftpFileNombre)) {

                            log.info("Descargamos archivo ftp de stock de la tienda " + cd_str_rt);
                            if (cliFtp.downloadFtpFile(archivoRemoto, archivoLocal, ftpFileNombre)) {
                                // Buscar si descargo archivo o no!
                                String sCarpAct = archivoLocal + ftpFileNombre;
                                //log.info("sCarpAct: " + sCarpAct);
                                File fileDownload = new File(sCarpAct);
                                // buscar en carpeta el archivo
                                // si esta hacer =>
                                if (fileDownload.exists() && fileDownload.isFile()) {
                                    log.info("Se descargo el archivo " + ftpFileNombre + " De forma correcta");
                                    log.info("Insertamos registro del archivo en la tabla 'AS_ITM_LOAD_FILE_STOCK'");
                                    if (!this.sql.inserta_as_itm_load_file_stock(ftpFileNombre, archivoLocal)) {
                                        throw new Exception("Error al insertar registro en tabla AS_ITM_LOAD_FILE_STOCK.");
                                    }

                                    if (!fl_stock_load) {
                                        // pasamos a true la tabla de tiendas si es total
                                        if (!this.sql.actualiza_pa_str_rtl(id_bsn_un)) {
                                            throw new Exception("Error al modificar la tabla 'PA_STR_RTL'");
                                        }
                                    }
                                } else {
                                    log.info("NO hay archivos para descargar del FTP en el dia de ayer.");
                                }

                            } else {
                                throw new Exception("No se pudo descargar archivos del repositorio FTP");
                            }
                        } else {
                            log.info("No descargamos archivo porque ya esta cargado. para la tienda " + de_str_rt);
                        }
                    }
                } catch (Exception e) {
                    log.info("El archivo: " + ftpFileNombre + " Tubo un problema para ser cargado.");
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

    public void procesaArchivos() throws Exception {

        log.info("Procesa Archivo --> " + fileExtend);

        // busco si hay archivos que procesar en la tabla 'AS_ITM_LOAD_FILE_STOCK'
        //0 ID_FILE, 1 DESCRIPCION, 2 PROCESADO, 3 DIRECTORIO_FILE, 4 ERROR, 5 FECHA 
        List<Object[]> rs = this.sql.consulta_as_itm_load_file_stock_procesar();
        if (rs != null) {
            for (Object[] row : rs) {
                // procesar
                int id_file = Integer.parseInt(row[0].toString());
                //int id_file 			= Integer.parseInt(row[0].toString()); 
                String descripcion = row[1].toString();
                String directorioFile = row[3].toString();
                log.info(" Procesa Archivo: " + descripcion);
                try {

                    this.procesaArchivo(descripcion, directorioFile, id_file);

                    // modificamos el archivo a procesado
                    if (!this.sql.actualiza_estado_itm_load_file_stock_ok(id_file)) {
                        this.sql.actualiza_estado_itm_load_file_stock_error(id_file);
                        throw new Exception("Error al Actualizar tabla de archivos de stock");
                    }

                    if (!sql.actualiza_info_as_item_load_file_stock(id_file, total, totalProcesados, totalArchivo, novedadesArchivo)) {
                        if (!sql.actualiza_estado_itm_load_file_stock_error(id_file)) {
                            //log.error("Error al actualizar estado ejecutando AS_ITM_LOAD_FILE..");
                            throw new Exception("Error al actualizar estado ejecutando AS_ITM_LOAD_FILE..");
                        }
                    }

                    log.info("--------------------------------------------------------------");
                    log.info("EL ARCHIVO: " + descripcion + " SE PROCESO CORRECTAMENTE");
                    log.info("--------------------------------------------------------------");

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                log.info("FIN Procesa Archivo: " + descripcion);

                log.info("--------------------------------------------------------------");
                log.info("FINALIZO LA CARGA DE TODOS LOS ARCHIVOS DE FORMA CORRECTA.");
                log.info("--------------------------------------------------------------");
            }
        } else {
            log.info("No hay archivos pendientes de stock por procesar.");
        }

    }

    public void procesaArchivo(String descripcion, String directorioFile, int idFile) throws Exception {
        //codigo de tienda
        String[] str = descripcion.split("-");
        this.cd_str = str[0];
        totalProcesados = 0;
        totalArchivo = 0;
        novedadesArchivo = "";
        // VALIDAR QUE ESTE LA TIENDA SINO NO AGREGAR	
        List<Integer> rsIDBSNUN = this.sql.consulta_id_bsn_un_x_codigo(Long.parseLong(this.cd_str));
        if (rsIDBSNUN == null) {
            // si no existe la tienda
            log.info("LA tienda (cd_str: " + cd_str + ") no esta cargada en la base de datos");
        } else {
            // Recorremos por cada tienda TENDRIA QUE TRAER SOLO UNA
            for (int row : rsIDBSNUN) {
                long id_bsn_un = row;
                String archivoMIF = directorioFile + "/" + descripcion;
                log.info("TIENDA (cd_str: " + cd_str + ")");
                log.info("STOCK - PROCESA INICIO=> lee archivo");
                // leer archivo CVS
                List<Item> listItems = this.leerArchivoCSV(archivoMIF);
                log.info("STOCK - PROCESA FIN=> lee archivo");

                log.info("STOCK - PROCESA INICIO => guarda en bd");
                if (listItems == null) {
                    log.info("El archivo de la tienda: " + cd_str + " No se proceso.");
                } else {

                    total = listItems.size();

                    for (Item item : listItems) {
                        try {
                            // buscamos si el articulo esta en el maestro de itm
                            Object[] rsITM = this.sql.consulta_as_itm_x_codigo(item.getAs_itm_cd_itm());

                            // si no existe el item no se puede agregar stock
                            if (rsITM == null) {
                                // si no existe el itm
                                //log.info("El item (cd_itm: " + item.getAs_itm_cd_itm() + ") no esta cargado en la base de datos");
                            } else {
                                long id_itm = Integer.parseInt(rsITM[0].toString());
                                // BUSCAR SI EL ARTICULO EXISTE EN ESA TIENDA
                                // si no existe el registro lo agregamos agregamos.
                                Object[] rsASITMSTR = this.sql.consulta_as_itm_str_codigos(id_itm, id_bsn_un);
                                if (rsASITMSTR == null) {
                                    // Tiene que existir
                                    log.info("No existe el item en la tienda seleccionada, item: " + item.getAs_itm_cd_itm() + ", tienda: " + cd_str);
                                } else {
                                    // si existe lo actualizamos
                                    if (!this.sql.actualiza_as_itm_str_stock((int) (Float.parseFloat(item.getAs_itm_stock())),
                                            id_itm,
                                            id_bsn_un
                                    )) {
                                        this.sql.actualiza_estado_itm_load_file_stock_error(idFile);
                                        throw new Exception("Error al ingresar los items en la tabla AS_ITM_STR");

                                    } else {
                                        totalProcesados++;
                                    }

                                    // Actualizar codigo de barra ver si sacamos de codipdv
                                    if (item.getId_ps_id_itm_ps() != null && !item.getId_ps_id_itm_ps().isEmpty()) {
                                        // vemos si existe 
                                        String rsIDPS = this.sql.consulta_id_ps(id_itm, id_bsn_un);
                                        if (rsIDPS == null) {
                                            // si no existe lo creo
                                            if (!this.sql.inserta_id_ps(
                                                    item.getId_ps_id_itm_ps(),
                                                    id_itm,
                                                    id_bsn_un)) {
                                                this.sql.actualiza_estado_itm_load_file_stock_error(idFile);
                                                throw new Exception("Error al ingresar los códigos de barra en la tabla ID_PS");
                                            }
                                        } else {
                                            // si existe actualizo
                                            if (!this.sql.actualiza_id_ps(
                                                    item.getId_ps_id_itm_ps(),
                                                    id_itm,
                                                    id_bsn_un)) {
                                                this.sql.actualiza_estado_itm_load_file_stock_error(idFile);
                                                throw new Exception("Error al ingresar los códigos de barra en la tabla ID_PS");
                                            }
                                        }

                                    }

                                    // Actualizar Imagen ver si sacamos de codipdv
                                    if (item.getAs_itm_img() != null && !item.getAs_itm_img().isEmpty()) {
                                        //actualizo idem a codigo de barra.
                                    }
                                }
                            }
                            cantProcesada++;
                        } catch (Exception ex) {
                            log.info("ERROR guardando en db - Proceso de stock, cd. Items: " + item.getAs_itm_cd_itm());
                            log.error(ex.getMessage(), ex);
                            novedadesArchivo = novedadesArchivo.concat(ex.getMessage() + " & " + item.getAs_itm_cd_itm() + " | ");
                        }

                    }
                }
                log.info("STOCK - PROCESA FIN => guarda en bd");
            }
        }

    }
    
    public List<Item> leerArchivoCSV(String archivoMIF) {
        int cantProc = 0;
        BufferedReader br = null;
        String line = "";
        //Se define separador ";"
        String cvsSplitBy = ";";

        List<Item> listItems = new ArrayList<Item>();

        try {
            br = new BufferedReader(new FileReader(archivoMIF));
            while ((line = br.readLine()) != null) {
                // crea un items para asignar los valores del archivo
                try {
                    Item itemAux = new Item();

                    String[] datos = line.split(cvsSplitBy);
                    //Imprime datos.
                    //datos[0]; //codigo de tienda
                    //datos[1]; //codigo de barra
                    //datos[2]; //codigo de barra pos (cod interno) 
                    //datos[3]; //stock
                    //datos[4]; //url imagen
                    int sizeDatos = datos.length;
                    int cantDigitos = datos[1].length();
                    if (cantDigitos > 1) {
                        // Codigo Barra
                        itemAux.setId_ps_id_itm_ps(datos[1]);
                        //itemAux.setAs_itm_cd_itm(datos[2]);
                        itemAux.setAs_itm_cd_itm(datos[1].substring(0, cantDigitos - 1));
                        // Stock
                        if (sizeDatos > 3) {
                            itemAux.setAs_itm_stock(datos[3]);
                        }
                        // Imagen
                        if (sizeDatos > 4) {
                            itemAux.setAs_itm_img(datos[4]);
                        }
                        // Agrega el item en una lista de items
                        listItems.add(itemAux);

                        cantProc++;
                    }
                } catch (Exception ex) {
                    log.info("ERROR leyendo archivos - Proceso Stock, Items: " + cantProc);
                    log.error(ex.getMessage(), ex);
                    novedadesArchivo = novedadesArchivo.concat(ex.getMessage() + " & " + cantProc + " | ");
                }
                totalArchivo++;
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            return null;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    return null;
                }
            }
        }

        return listItems;

    }

}
