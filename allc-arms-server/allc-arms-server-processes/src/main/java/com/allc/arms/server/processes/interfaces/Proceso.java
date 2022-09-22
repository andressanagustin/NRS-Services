package com.allc.arms.server.processes.interfaces;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.apache.log4j.Logger;
import org.hibernate.Session;
//import org.hibernate.mapping.Formula;
//import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.allc.properties.PropFile;

public abstract class Proceso {

    protected static Logger log = Logger.getLogger(Proceso.class.getName());
    protected PropFile prop = null;
    private Session sessionArtsEc = null;
    protected boolean finished = false;
    public int cantProcesada;
    public int total;
    public int totalProcesados;
    public int totalArchivo;
    public String novedadesArchivo;
    public String urlImagen;
    //public int cantOK;

    RepositorioSQL sql;
    int varAmbiente;
    int varIdItm;
    int tipo; // 1 DAT(CODIPDV), 2 CSV(Stock), 3 Excel(Jugueton) y 4 Excel(Moblart)
    List<String> fileExtend;
    String urlFolder;

    public Proceso(Session sessionParam, PropFile pProp, int tipo) throws IOException {
        this.prop = pProp;
        this.tipo = tipo;
        this.sessionArtsEc = sessionParam;
        this.sql = new RepositorioSQL(sessionArtsEc);
        this.cantProcesada = 0;
        this.fileExtend = new ArrayList<String>();
        this.urlImagen = "https://www.jugueton.com.ec/nrs_suite/nrs/images/ecom/"; // --> produccion

        switch (this.tipo) {
            case 1:
                this.urlFolder = this.prop.getObject("interfaceMaestroItem.var.FileOrigen");
                fileExtend.add(".DAT");
                break;
            case 2:
                this.urlFolder = this.prop.getObject("interfaceMaestroItem.receiveFile.ftp.FileDownload");
                fileExtend.add(".CSV");
                break;
            case 3:
                this.urlFolder = this.prop.getObject("interfaceMaestroItem.var.FileOrigen.Jugueton");
                fileExtend.add(".XLS");
                fileExtend.add(".XLSX");
                break;
            case 4:
                this.urlFolder = this.prop.getObject("interfaceMaestroItem.var.FileOrigen.Moblart");
                fileExtend.add(".XLS");
                fileExtend.add(".XLSX");
                break;
            default:
                this.urlFolder = this.prop.getObject("interfaceMaestroItem.var.FileOrigen");
                fileExtend.add(".DAT");
                break;
        }
        //this.cantOK = 0;
    }

    //Busca y carga archivos para procesar en la tabla
    public void cargaArchivosDB() {

        //int tipoProceso = this.tipo; // se inicializa en el constructor es importante para saber que proceso
        //String urlFolder = null;
        log.info("Buscamos archivos en la carpeta: " + urlFolder);
        File carpeta = new File(this.urlFolder);
        if (carpeta.exists() && carpeta.isDirectory()) {
            File[] lisFile = carpeta.listFiles();

            for (File file : lisFile) {
                try {
                    if (file.getName().toUpperCase().endsWith(fileExtend.get(0))
                            || (fileExtend.size() > 1 && file.getName().toUpperCase().endsWith(fileExtend.get(1)))) {
                        log.info(file.getName());
                        //verificar que no exista
                        if (!this.sql.consulta_as_itm_load_file(file.getName())) {
                            if (this.tipo == 2) // es stock
                            {
                                if (!this.sql.inserta_as_itm_load_file_stock(file.getName(), urlFolder)) {
                                    throw new Exception("Error al insertar registro en tabla AS_ITM_LOAD_FILE_STOCK.");
                                }
                            } else {
                                //agregar a la base de datos				
                                if (!sql.inserta_as_itm_load_file(file.getName(), urlFolder, true)) {
                                    throw new Exception("Error al insertar registro en tabla AS_ITM_LOAD_FILE.");
                                }
                            }
                            log.info("Se guardo en la tabla el archivo: " + file.getName() + ", para ser procesado.");
                        } else {
                            log.info("El archivo: " + file.getName() + " YA ESTABA CARGADO en la tabla 'AS_ITM_LOAD_FILE'.");
                        }
                    }
                } catch (Exception e) {
                    log.info("El archivo: " + file.getName() + " Tubo un problema para ser cargado.");
                    log.error(e.getMessage(), e);
                }
            }
        }
        //return true;
    }

    public void procesaArhivos() {
        //String fileExtend = this.fileExtend; // se inicializa en el constructor es importante para saber que proceso
        log.info("Procesa Archivo --> " + fileExtend);
        //Busca en 'AS_ITM_LOAD_FILE' si hay un archivo pendiente para procesar .DAT
        List<Object[]> loadFile = sql.consulta_ejecutando_all();

        if (loadFile != null) {
            for (Object[] row : loadFile) {
                //0 ID_FILE, 1 DESCRIPCION, 2 ESTADO, 3 EJECUTADO, 4 VAR_PROCESADO, 5 DIRECTORIO_FILE, 6 VALIDA_PHP, 7 FECHA
                String descripcion = row[1].toString();
                log.info(" Procesa Archivo: " + descripcion);
                try {
                    //if(descripcion.toUpperCase().endsWith(fileExtend))
                    if (descripcion.toUpperCase().endsWith(fileExtend.get(0))
                            || (fileExtend.size() > 1 && descripcion.toUpperCase().endsWith(fileExtend.get(1)))) {
                        //trae datos del archivo
                        int idFile = Integer.parseInt(row[0].toString());
                        String directorioFile = row[5].toString();

                        //log.info("ID_FILE --> " + idFile);
                        //log.info("DESCRIPCION --> " + descripcion);
                        // Actualiza estado de 'AS_ITM_LOAD_FILE' a true
                        if (!sql.actualiza_estado_as_itm_load_file(idFile)) {
                            //log.error("Error al actualizar estado ejecutando AS_ITM_LOAD_FILE..");
                            throw new Exception("Error al actualizar estado ejecutando AS_ITM_LOAD_FILE..");
                        }

                        this.procesaArchivo(directorioFile, descripcion, idFile);

                        //Actualizar la cantidad de registros procesados
                        if (!sql.actualiza_info_as_item_load_file(idFile, total, totalProcesados, totalArchivo, novedadesArchivo)) {
                            //log.error("Error al actualizar estado ejecutando AS_ITM_LOAD_FILE..");
                            throw new Exception("Error al actualizar estado ejecutando AS_ITM_LOAD_FILE..");
                        }

                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                log.info("FIN Procesa Archivo: " + descripcion);
            }
        } else {
            log.info("No hay ningun archivo esperando se procesado en la tabla 'AS_ITM_LOAD_FILE'");
        }
    }

    public abstract void procesaArchivo(String directorioFile, String descripcion, int idFile) throws Exception;

    // verificamos si el formato de la cell ingresada es numerico o string
    public String leerCell(Cell aux) {
        String result = "";
        int cellType = aux.getCellType();
        //log.info("type: " +cellType);
        switch (cellType) {
            case 0:
                // VER VIENE COMO EXPONENCIAL.
                //result = Double.toString(aux.getNumericCellValue()).trim();
                result = Long.toString((long) aux.getNumericCellValue()).trim();
                break;
            case 1:
                result = aux.getStringCellValue().trim();
                break;

            default:
                //log.info("type: " +cellType);
                result = aux.getStringCellValue().trim();
                break;

        }
        return result;
    }

    public String leerCellPorcentaje(Cell aux) {
        String result = "";
        DataFormatter formatter = new DataFormatter();
        result = formatter.formatCellValue(aux);
        result = result.replace("%", "");

        return result;
    }

    public String leerCellFecha(Cell aux) {
        String result = "";
        DataFormatter formatter = new DataFormatter();
        result = formatter.formatCellValue(aux);

        return result;
    }

    public String leerCellNum(Cell aux, Workbook wb) {
        String result = "";
        DataFormatter formatter = new DataFormatter();
//		org.apache.poi.xssf.usermodel.XSSFWorkbook
        FormulaEvaluator objFormulaEvaluator = new XSSFFormulaEvaluator((XSSFWorkbook) wb);
        objFormulaEvaluator.evaluate(aux);
        //FormulaEvaluator evaluator; 
        result = formatter.formatCellValue(aux, objFormulaEvaluator);
        result = result.replace(",", ".").replace("$ ", "").replace("$", "");

        return result;
    }

    public String limpiarCell(Cell aux) {
        String result = leerCell(aux)
                .replaceAll("(?i)cm.", "")
                .replaceAll("(?i)cm", "")
                .replaceAll("(?i)cn.", "")
                .replaceAll("(?i)cn", "")
                .replaceAll("(?i)kg.", "")
                .replaceAll("(?i)kg", "")
                .replaceAll("(?i)d", "")
                .replaceAll("(?i)m", "")
                .replaceAll("(?i)md", "")
                .replace("�", "");
        return result.trim();
    }

    public String limpiarCellNum(Cell aux) {
        String result = leerCell(aux)
                .replaceAll("(?i)cm.", "")
                .replaceAll("(?i)cm", "")
                .replaceAll("(?i)cn.", "")
                .replaceAll("(?i)cn", "")
                .replaceAll("(?i)kg.", "")
                .replaceAll("(?i)kg", "")
                .replaceAll("(?i)d", "")
                .replaceAll("(?i)m", "")
                .replaceAll("(?i)md", "")
                .replace("�", "")
                .replace(",", ".");
        return result.trim();
    }

    public static Date subtractDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }
}
