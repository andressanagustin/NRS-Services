package com.allc.arms.server.processes.interfaces;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.Session;

import com.allc.properties.PropFile;
import java.text.DecimalFormat;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class ProcesoJugueton extends Proceso {

    public ProcesoJugueton(Session sessionParam, PropFile pProp) throws IOException {
        //super(sessionParam,pProp,".XLS");
        super(sessionParam, pProp, 3);
    }

    @Override
    public void procesaArchivo(String directorioFile, String descripcion, int idFile) throws IOException, InvalidFormatException {
        // leer archivo xls
        String archivoMIF = directorioFile + "/" + descripcion;
        if (this.varAmbiente == 1) {
            archivoMIF = archivoMIF.replace("/", "\\").trim();
        }
        totalProcesados = 0;
        totalArchivo = 0;
        novedadesArchivo = "";

        log.info("JUGUETON - PROCESA INICIO=> lee archivo");
        List<Item> listItems = this.leerArchivoXLS(archivoMIF);

        total = listItems.size();
        log.info("JUGUETON - PROCESA FIN=> lee archivo");

        log.info("JUGUETON - PROCESA INICIO => guarda en bd");

        //buscar id tienda
        long id_bsn_un = 0;
        try {
            long tiendaJugeton = Long.parseLong(this.prop.getObject("interfaceMaestroItem.process.jugueton")); // ver si puede haber mas de una tienda
            List<Integer> rsID_BSN_UN = this.sql.consulta_id_bsn_un_x_codigo(tiendaJugeton);
            if (rsID_BSN_UN == null) {
                // si no existe no se creo y hay algo mal ERROR!
                this.sql.actualiza_error_as_itm_load_file(idFile);
                throw new Exception("Error buscando la tienda de jugueton, no se pudo encontrar en la base de datos.");
            } else {
                // si existe traemos el id
                id_bsn_un = (int) rsID_BSN_UN.get(0);
            }

            for (Item item : listItems) {
                try {
                    // 0 ID_ITM,1 DE_ITM,2 DE_DESCRIPCION,3 EDAD,4 ALTO,5 ANCHO,6 LARGO,7 PESO,8 GENERO
                    Object[] rsITM = this.sql.consulta_as_itm_x_codigo_jgt(item.getAs_itm_cd_itm());
                    // si no existe el item NO HACEMOS NADA TIENE QUE ESTAR CREADO
                    if (rsITM != null) {
                        // si existe lo actualizamos.
                        long id_itm = Long.parseLong(rsITM[0].toString());
                        String de_itm = rsITM[1] != null ? rsITM[1].toString() : "";
                        String de_descripcion = rsITM[2] != null ? rsITM[2].toString() : "";
                        String edad = rsITM[3] != null ? rsITM[3].toString() : "";
                        double alto = rsITM[4] != null ? Double.parseDouble(rsITM[4].toString()) : 0.00;
                        double ancho = rsITM[5] != null ? Double.parseDouble(rsITM[5].toString()) : 0.00;
                        double largo = rsITM[6] != null ? Double.parseDouble(rsITM[6].toString()) : 0.00;
                        double peso = rsITM[7] != null ? Double.parseDouble(rsITM[7].toString()) : 0.00;
                        double peso_serv = rsITM[11] != null ? Double.parseDouble(rsITM[11].toString()) : 0.00;
                        String genero = rsITM[8] != null ? rsITM[8].toString() : "";
                        String promoAux = item.getAs_itm_str_fl_promocion().replace("SI", "1").replace("Sí", "1").replace("Si", "1").replace("si", "1").replace("sI", "1").replace("NO", "0").replace("No", "0").replace("nO", "0").replace("no", "0");
                        int fl_promocion = Integer.parseInt(promoAux);
                        String prm_dsc_lbl = fl_promocion == 1 ? item.getAs_itm_str_prm_dsc_lbl() : null;
                        double prm_prc = fl_promocion == 1 ? Double.parseDouble(item.getAs_itm_str_prm_prc()) : 0;
                        boolean addImagen = item.getAs_itm_img() != null;
                        Date fechaInicio = fl_promocion == 1 ? item.getAs_itm_str_prm_dtf() : null;
                        Date fechaFin = fl_promocion == 1 ? item.getAs_itm_str_prm_dtt() : null;
                        
                        // agregar proveedor primero
                        long idSpr = 0;
                        BigInteger rsCoSPR = this.sql.consulta_co_spr_detalle(item.getAs_itm_prove_det());
                        // si no existe el proveedor lo agregamos
                        if (rsCoSPR == null) {
                            // Inserta registro en tabla 'co_spr'
                            if (!this.sql.inserta_co_spr(item.getAs_itm_prove_det())) {
                                // si hay un error, marca en la tabla del archivo que tienen errores
                                this.sql.actualiza_error_as_itm_load_file(idFile);
                                throw new Exception("Error al ingresar item en la tabla AS_ITM");
                            }

                            // verifico que se creeo y busco el id
                            BigInteger rsCoSPR2 = this.sql.consulta_co_spr_detalle(item.getAs_itm_prove_det());
                            if (rsCoSPR2 == null) {
                                // si no existe no se creo y hay algo mal ERROR!
                                this.sql.actualiza_error_as_itm_load_file(idFile);
                                throw new Exception("Error al ingresar el proveedor en la tabla 'co_spr'");
                            } else {
                                // si existe traemos el id
                                idSpr = rsCoSPR2.longValue();
                            }
                        } else {
                            idSpr = rsCoSPR.longValue();
                        }

                        // Actualiza registro en tabla 'AS_ITM'
                        if (!this.sql.actualiza_as_itm_gnr(
                                idSpr,
                                item.getAs_itm_str_de_itm().isEmpty() ? de_itm : item.getAs_itm_str_de_itm(),
                                item.getAs_itm_str_de_descripcion().isEmpty() ? de_descripcion : item.getAs_itm_str_de_descripcion(),
                                item.getAs_itm_edad().isEmpty() ? edad : item.getAs_itm_edad(),
                                item.getAs_itm_alto().isEmpty() ? alto : Double.parseDouble(item.getAs_itm_alto()),
                                item.getAs_itm_ancho().isEmpty() ? ancho : Double.parseDouble(item.getAs_itm_ancho()),
                                item.getAs_itm_largo().isEmpty() ? largo : Double.parseDouble(item.getAs_itm_largo()),
                                item.getAs_itm_peso().isEmpty() ? peso : Double.parseDouble(item.getAs_itm_peso()),
                                item.getAs_itm_genero().isEmpty() ? genero : item.getAs_itm_genero(),
                                id_itm,
                                item.getAs_itm_pesoServi().isEmpty() ? peso_serv : Double.parseDouble(item.getAs_itm_pesoServi().replaceAll("\"", ""))
                        )) {
                            this.sql.actualiza_error_as_itm_load_file(idFile);
                            throw new Exception("Error al actualizar item en la tabla AS_ITM");
                        }

                        // Actualiza registro en tabla 'AS_ITM_STR'
                        if (!item.getAs_itm_prove_det().isEmpty()) {
                            // Actualiza registro en tabla 'AS_ITM_str'
                            if (!this.sql.actualiza_as_itm_str_gnr_jugeton(item.getAs_itm_prove_det(), fl_promocion, prm_dsc_lbl, id_itm, id_bsn_un, prm_prc, fechaInicio, fechaFin)) {
                                this.sql.actualiza_error_as_itm_load_file(idFile);
                                throw new Exception("Error al actualizar item en la tabla AS_ITM_STR");
                            } else {
                                totalProcesados++;
                            }

                        } else {
                            // Actualiza registro en tabla 'AS_ITM_STR'
                            if (!this.sql.actualiza_as_itm_str_jugeton(fl_promocion, prm_dsc_lbl, id_itm, id_bsn_un, prm_prc, fechaInicio, fechaFin)) {
                                this.sql.actualiza_error_as_itm_load_file(idFile);
                                throw new Exception("Error al actualizar item en la tabla AS_ITM_STR");
                            } else {
                                totalProcesados++;
                            }

                        }

                        // Inserta registro en AS_ITM_IMAGEN
                        // valido si el excel trajo nombre de imagen
                        if (addImagen) {
                            String[] ListImagen = item.getAs_itm_img().split(",");
                            for (String img : ListImagen) {
                                String imagenLg = this.urlImagen + "productos_lg/" + img.trim();
                                String imagenSm = this.urlImagen + "productos_sm/" + img.trim();

                                // verifica que exista
                                Object[] rsIMAGEN = this.sql.consulta_as_itm_imagen_registro(id_itm);
                                // si no existe 
                                if (rsIMAGEN == null) {
                                    if (!this.sql.inserta_as_itm_imagen2(1, id_itm, imagenLg, imagenSm, 1, 1)) {
                                        this.sql.actualiza_error_as_itm_load_file(idFile);
                                        throw new Exception("Error al ingresar la imagen del item en la tabla AS_ITM_IMAGEN");
                                    }

                                    //log.info("Se creo el registro de la imagen del item " + item.getAs_itm_cd_itm() + " EXITOSAMENTE!!!");		
                                } else {
                                    Object[] rsIMAname = this.sql.consulta_as_itm_imagen_nombre1(id_itm, imagenLg);
                                    //Validamos que no este cargada la misma imagen
                                    if (rsIMAname == null) {
                                        int id_itm_imagen = Integer.parseInt(rsIMAGEN[0].toString());
                                        int principal = 0;//Integer.parseInt(rsIMAGEN[5].toString()) == 1 ? 0 : 1;
                                        int orden_presentacion = rsIMAGEN[7] == null ? 1 : Integer.parseInt(rsIMAGEN[7].toString());

                                        if (!this.sql.inserta_as_itm_imagen2(id_itm_imagen + 1, id_itm, imagenLg, imagenSm, orden_presentacion + 1, principal)) {
                                            this.sql.actualiza_error_as_itm_load_file(idFile);
                                            throw new Exception("Error al ingresar la imagen del item en la tabla AS_ITM_IMAGEN");
                                        }
                                    } else {
                                        // si es la misma imagen validar estado
                                        int id_itm_imagen = Integer.parseInt(rsIMAname[0].toString());
                                        int estado = Integer.parseInt(rsIMAname[3].toString());
                                        if (estado == 0) {
                                            if (!this.sql.actualiza_as_itm_imagen1(id_itm_imagen, id_itm)) {
                                                this.sql.actualiza_error_as_itm_load_file(idFile);
                                                throw new Exception("Error al cambiar el estado de la imagen del item en la tabla AS_ITM_IMAGEN, id_itm: " + id_itm + ", id_itm_imagen: " + id_itm_imagen);
                                            }
                                        }

                                    }
                                }

                            }
                        }

                        // insertar en tabla de categorias y subcategorias para el ecommerce
                        if (!item.getAs_itm_categoria().isEmpty()) {
                            // primero validamos que tengamos listado de JUGUETÓN
                            Integer idListCatalogo = this.sql.consulta_id_listado_catalogo("JUGUETÓN");
                            if (idListCatalogo != null) {
                                //Buscamos categoria, si no existe la creamos
                                Integer idCatCategoria = this.sql.consulta_ecommerce_catalogo(item.getAs_itm_categoria().trim(), idListCatalogo);
                                if (idCatCategoria == null) {
                                    // No esta la categoria, la creamos 
                                    Integer idCatPadre = this.sql.consulta_ecommerce_catalogo_nodo_padre(idListCatalogo);
                                    // buscar ultimo numero idCatalogo
                                    Integer proximoId = this.sql.consulta_ecommerce_catalogo_ultimo_ID(idListCatalogo) + 1;
                                    //inserta_ecommerce_catalogo
                                    if (!this.sql.inserta_ecommerce_catalogo(proximoId, idListCatalogo, idCatPadre, item.getAs_itm_categoria().trim())) {
                                        this.sql.actualiza_error_as_itm_load_file(idFile);
                                        throw new Exception("Error al ingresar la CATEGORIA del item en la tabla ECOMMERCE_CATALOGO.");
                                    }

                                    idCatCategoria = this.sql.consulta_ecommerce_catalogo(item.getAs_itm_categoria().trim(), idListCatalogo);

                                }

                                Short estado = this.sql.consulta_ecommerce_catalogo_items_estado(idListCatalogo, idCatCategoria, id_itm);
                                if (estado == null) {
                                    //primero borrar las asociaciones entre el item y las categorias anteriores
                                    if (!this.sql.borrar_ecommerce_catalogo_items(idListCatalogo, idCatCategoria, id_itm)) {
                                        this.sql.actualiza_error_as_itm_load_file(idFile);
                                        throw new Exception("Error al BORRAR los ITEMS asociados en la SUBCATEGORIA del item en la tabla ECOMMERCE_CATALOGO_ITEMS. idListCatalogo:" + idListCatalogo + ",idCatCategoria:" + idCatCategoria + ",id_itm:" + id_itm);
                                    }

                                    //insertamos items en categoria
                                    if (!this.sql.inserta_ecommerce_catalogo_items(idListCatalogo, idCatCategoria, id_itm)) {
                                        this.sql.actualiza_error_as_itm_load_file(idFile);
                                        throw new Exception("Error al ingresar el ITEMS en la CATEGORIA del item en la tabla ECOMMERCE_CATALOGO_ITEMS.");
                                    }
                                } else if (estado == 0) {
                                    // activamos
                                    this.sql.actualiza_ecommerce_catalogo_items_estado(idListCatalogo, idCatCategoria, id_itm);
                                }

                            } else {
                                log.info("JUGUETÓN => NO se encuentra el listado jugueton en ECOMMERCE_CATALOGO_CATALOGO.");
                            }

                        }

                        // contador de items bien procesados  
                        this.cantProcesada++;
                    }
                } catch (Exception ex) {
                    log.info("ERROR guardando en db - Proceso Proceso Excel Jugeton, cd. Items: " + item.getAs_itm_cd_itm());
                    log.error(ex.getMessage(), ex);
                    novedadesArchivo = novedadesArchivo.concat(ex.getMessage() + " & " + item.getAs_itm_cd_itm() + " | ");
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        log.info("JUGUETON - PROCESA FIN => guarda en bd");

    }

    public List<Item> leerArchivoXLS(String archivoMIF) throws IOException, InvalidFormatException {

        // Creamos instancia del archio a procesar.
        File file = new File(archivoMIF);
        // Obtenemos los bytes del archivo
        //FileInputStream fis = new FileInputStream(file);

        // Creamos una instancia del 'libro de trabajo' seria el archivo de extencion .xlsx
        //XSSFWorkbook wb = new XSSFWorkbook(fis);
        org.apache.poi.ss.usermodel.Workbook wb = WorkbookFactory.create(file);

        // Creamos un objeto hoja y recuperamos aqui la hoja del archivo original.
        //XSSFSheet sheet = wb.getSheetAt(0); 
        org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);

        // Iteramos sobre el archivo de excel
        Iterator<Row> itr = sheet.iterator();
        itr.next();

        int contFilas = 0;
        List<Item> listItems = new ArrayList<Item>();

        while (itr.hasNext()) {
            Row row = itr.next(); // ver como no procesar registros vacios
            try {
                // Iteramos sobre cada columna
                Iterator<Cell> cellIterator = row.cellIterator();

                // crea un items para asignar los valores del archivo
                Item itemAux = new Item();

                //1 Codigo barras padre 13 digitos ultimo de verificacion
                Cell cell = cellIterator.next();
                String aux = leerCell(cell);

                if (aux != null && !aux.isEmpty()) {
                    contFilas++;

                    int cantDigitos = aux.length();
                    //log.info("Leer Cell: " + aux + ", cantDigitos: " + cantDigitos);
                    itemAux.setAs_itm_cd_itm(aux.substring(0, cantDigitos - 1));

                    //2 precio NO VA
                    cell = cellIterator.next();

                    // 3 proveedor id_spr y deberia llenarse en esta tabla select * from arts_ec.co_spr;
                    // ademas si no tiene marca ponerle esta descripcion como marca
                    cell = cellIterator.next();
                    String prove = leerCell(cell);
                    itemAux.setAs_itm_prove_det(prove);

                    //4 Referencia
                    cell = cellIterator.next();

                    //5 Detalle Item	
                    cell = cellIterator.next();
                    cantDigitos = cell.getStringCellValue().trim().length();
                    itemAux.setAs_itm_str_de_itm(cell.getStringCellValue().trim().substring(0, (cantDigitos > 45 ? 45 : cantDigitos)));
                    //Descripcion Item	
                    itemAux.setAs_itm_str_de_descripcion(cell.getStringCellValue().trim());

                    //6 EDAD	
                    cell = cellIterator.next();
                    String edad = leerCell(cell);
                    itemAux.setAs_itm_edad(edad);

                    //7 TAMAï¿½O EMPAQUE LARGO	
                    cell = cellIterator.next();
                    String largo = limpiarCellNum(cell);
                    itemAux.setAs_itm_largo(largo);

                    //8 TAMAï¿½O EMPAQUE ANCHO	
                    cell = cellIterator.next();
                    String ancho = limpiarCellNum(cell);
                    itemAux.setAs_itm_ancho(ancho);

                    //09 TAMAï¿½O EMPAQUE ALTURA 	
                    cell = cellIterator.next();
                    String altura = limpiarCellNum(cell);
                    itemAux.setAs_itm_alto(altura);

                    //10 PESO	 
                    cell = cellIterator.next();
                    String peso = limpiarCellNum(cell);
                    itemAux.setAs_itm_peso(peso);

                    //11
                    cell = cellIterator.next();
                    DecimalFormat df = new DecimalFormat("#0.000");
                    String pesoServ = "";
                    try {
                        pesoServ = df.format(cell.getNumericCellValue()).replace(",", ".");
                    } catch (Exception e) {
                    }
                    itemAux.setAs_itm_pesoServi(pesoServ);

                    //12 GENERO 	
                    cell = cellIterator.next();
                    String genero = leerCell(cell);
                    itemAux.setAs_itm_genero(genero);

                    //13 CATEGORï¿½A
                    cell = cellIterator.next();
                    itemAux.setAs_itm_categoria(leerCell(cell));

                    //14 porcentaje de descuento
                    cell = cellIterator.next();
                    //itemAux.setAs_itm_str_prm_dsc_lbl(limpiarCell(cell));
                    //String desc = Integer.toString((int)(cell.getNumericCellValue()*100)).trim();
                    itemAux.setAs_itm_str_prm_dsc_lbl(leerCellPorcentaje(cell));

                    //15 Tiene Promocion
                    cell = cellIterator.next();
                    String promo = limpiarCell(cell);
                    promo = promo.isEmpty() || promo == null || promo == "" ? "NO" : promo;
                    itemAux.setAs_itm_str_fl_promocion(promo);

                    //16 Precio de Promocion NO ANDA BIEN
                    cell = cellIterator.next();
                    String prcPromo = leerCellNum(cell, wb);
                    itemAux.setAs_itm_str_prm_prc(prcPromo);

                    //17 fecha inicio 18/3/2021
                    cell = cellIterator.next();
                    String fecAuxI = leerCellFecha(cell);
                    if (fecAuxI != null && !fecAuxI.isEmpty() && fecAuxI != "") {
                        Date fecIni = new SimpleDateFormat("MM/dd/yy").parse(fecAuxI);
                        itemAux.setAs_itm_str_prm_dtf(fecIni);

                    } else {
                        itemAux.setAs_itm_str_prm_dtf(null);
                    }

                    // 18 fecha fin 30/6/2021
                    cell = cellIterator.next();
                    String fecAuxF = leerCellFecha(cell);
                    if (fecAuxF != null && !fecAuxF.isEmpty() && fecAuxF != "") {
                        Date fecFin = new SimpleDateFormat("MM/dd/yy").parse(fecAuxF);
                        itemAux.setAs_itm_str_prm_dtt(fecFin);

                    } else {
                        itemAux.setAs_itm_str_prm_dtt(null);
                    }
                    //19 Nombre imagen				
                    cell = cellIterator.next();
                    String imagen = leerCell(cell); // trae una lista de imagenes

                    if (imagen != null && !imagen.isEmpty()) {
                        itemAux.setAs_itm_img(imagen);
                    } else {
                        itemAux.setAs_itm_img(null);
                    }

                    // Agrega el item en una lista de items
                    listItems.add(itemAux);
                }
            } catch (Exception ex) {
                log.info("ERROR leyendo archivo - Proceso Excel Jugeton, Items: " + contFilas);
                log.error(ex.getMessage(), ex);
                novedadesArchivo = novedadesArchivo.concat(ex.getMessage() + " & " + contFilas + " | ");
            }
            totalArchivo++;

        }
        return listItems;
    }

}
