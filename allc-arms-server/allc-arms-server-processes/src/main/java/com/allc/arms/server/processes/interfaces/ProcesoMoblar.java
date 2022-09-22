package com.allc.arms.server.processes.interfaces;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;

import com.allc.properties.PropFile;

public class ProcesoMoblar extends Proceso {

    public ProcesoMoblar(Session sessionParam, PropFile pProp) throws IOException {
        //super(sessionParam,pProp,".XLSX");
        super(sessionParam, pProp, 4);
    }

    public void procesaArchivo(String directorioFile, String descripcion, int idFile) throws IOException {
        String archivoMIF = directorioFile + "/" + descripcion;
        if (this.varAmbiente == 1) {
            archivoMIF = archivoMIF.replace("/", "\\").trim();
        }

        totalProcesados = 0;
        totalArchivo = 0;
        novedadesArchivo = "";

        log.info("MOBLAR - PROCESA INICIO=> lee archivo");
        // leer archivo xlsx
        List<Item> listItems = this.leerArchivoXLSX(archivoMIF);
        log.info("MOBLAR - PROCESA FIN=> lee archivo");
        total = listItems.size();

        log.info("MOBLAR - PROCESA INICIO => guarda en bd");
        long id_bsn_un = 0;
        try {
            long tiendaMoblart = Long.parseLong(this.prop.getObject("interfaceMaestroItem.process.moblart")); // ver si puede haber mas de una tienda
            List<Integer> rsID_BSN_UN = this.sql.consulta_id_bsn_un_x_codigo(tiendaMoblart);
            if (rsID_BSN_UN == null) {
                // si no existe no se creo y hay algo mal ERROR!
                this.sql.actualiza_error_as_itm_load_file(idFile);
                throw new Exception("Error buscando la tienda de MOBLAR, no se pudo encontrar en la base de datos.");
            } else {
                // si existe traemos el id
                id_bsn_un = (int) rsID_BSN_UN.get(0);
            }

            for (Item item : listItems) {

                try {

                    // 0 ID_ITM,1 DE_ITM,2 DE_DESCRIPCION,3 EDAD,4 ALTO,5 ANCHO,6 LARGO,7 PESO,8 GENERO,9 COLOR,10 DISENIADOR
                    Object[] rsITM = this.sql.consulta_as_itm_x_codigo(item.getAs_itm_cd_itm());
                    // si no existe el item NO HACEMOS NADA TIENE QUE ESTAR CREADO
                    if (rsITM != null) {
                        // si existe lo actualizamos.
                        long id_itm = Integer.parseInt(rsITM[0].toString());
                        String de_itm = rsITM[1] != null ? rsITM[1].toString() : "";
                        String de_descripcion = rsITM[2] != null ? rsITM[2].toString() : "";
                        double alto = rsITM[4] != null ? Double.parseDouble(rsITM[4].toString()) : 0.00;
                        double ancho = rsITM[5] != null ? Double.parseDouble(rsITM[5].toString()) : 0.00;
                        double largo = rsITM[6] != null ? Double.parseDouble(rsITM[6].toString()) : 0.00;
                        String color = rsITM[9] != null ? rsITM[9].toString() : "";
                        String diseniador = rsITM[10] != null ? rsITM[10].toString() : "";
                        boolean addImagen = item.getAs_itm_img() != null ? true : false;
                        String reqArm = item.getAs_itm_requiereArmado().replace("SI", "1").replace("SÍ", "1").replace("Si", "1").replace("si", "1").replace("sI", "1").replace("NO", "0").replace("No", "0").replace("nO", "0").replace("no", "0");
                        boolean reqArmado = Integer.parseInt(reqArm) == 1 ? true : false;
                        String promoAux = item.getAs_itm_str_fl_promocion().replace("SI", "1").replace("SÍ", "1").replace("Si", "1").replace("si", "1").replace("sI", "1").replace("NO", "0").replace("No", "0").replace("nO", "0").replace("no", "0");
                        int fl_promocion = Integer.parseInt(promoAux);
                        String prm_dsc_lbl = fl_promocion == 1 ? item.getAs_itm_str_prm_dsc_lbl() : null;
                        //int prm_prc 			= fl_promocion == 1 ?  : null; todavia no mandan el precio de promocion
                        double prm_prc = fl_promocion == 1 ? Double.parseDouble(item.getAs_itm_str_prm_prc()) : 0;
                        Date fechaInicio = fl_promocion == 1 ? item.getAs_itm_str_prm_dtf() : null;
                        Date fechaFin = fl_promocion == 1 ? item.getAs_itm_str_prm_dtt() : null;

                        // Actualiza registro en tabla 'AS_ITM'
                        if (!this.sql.actualiza_as_itm_moblar(item.getAs_itm_str_de_itm().isEmpty() ? de_itm : item.getAs_itm_str_de_itm(),
                                item.getAs_itm_str_de_descripcion().isEmpty() ? de_descripcion : item.getAs_itm_str_de_descripcion(),
                                item.getAs_itm_alto().isEmpty() ? alto : Double.parseDouble(item.getAs_itm_alto()),
                                item.getAs_itm_ancho().isEmpty() ? ancho : Double.parseDouble(item.getAs_itm_ancho()),
                                item.getAs_itm_largo().isEmpty() ? largo : Double.parseDouble(item.getAs_itm_largo()),
                                item.getAs_itm_color().isEmpty() ? color : item.getAs_itm_color(),
                                item.getAs_itm_diseniador().isEmpty() ? diseniador : item.getAs_itm_diseniador(),
                                id_itm,
                                reqArmado)) {
                            this.sql.actualiza_error_as_itm_load_file(idFile);
                            throw new Exception("Error al actualizar item en la tabla AS_ITM");
                        }
                        // Actualiza registro en tabla 'AS_ITM_STR' 
                        if (!this.sql.actualiza_as_itm_str_moblar(fl_promocion, prm_dsc_lbl, id_itm, id_bsn_un, fechaInicio, fechaFin, prm_prc)) {
                            this.sql.actualiza_error_as_itm_load_file(idFile);
                            throw new Exception("Error al actualizar item en la tabla AS_ITM_STR  ");
                        } else {
                            totalProcesados++;
                        }

                        // Inserta registro en AS_ITM_IMAGEN
                        // valido si el excel trajo nombre de imagen
                        if (addImagen) {
                            String[] ListImagen = item.getAs_itm_img().split(",");
                            log.info("Agregando imagenes --- " + ListImagen.length);
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
                                    //ID_ITM_IMAGEN, ID_ITM, IMAGEN, ESTADO, FECHA_CREACION, PRINCIPAL, IMAGEN_SM, ORDEN_PRESENTACION
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
                        else{
                            log.info("No se agrega imagenes " + item.getAs_itm_img());
                        }

                        // insertar en tabla de categorias y subcategorias para el ecommerce
                        if (!item.getAs_itm_categoria().isEmpty()) {
                            // primero validamos que tengamos listado de moblar
                            Integer idListCatalogo = this.sql.consulta_id_listado_catalogo("MOBLART");
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

                                if (!item.getAs_itm_subcategoria().isEmpty()) {
                                    // mandaron subcategoria, buscar subcategoria arts_ec.ecommerce_catalogo para esta busqueda agregar idpadre
                                    Integer idCatSubcategoria = this.sql.consulta_ecommerce_catalogo(item.getAs_itm_subcategoria().trim(), idListCatalogo, idCatCategoria);
                                    if (idCatSubcategoria == null) {
                                        //cargamos la subcategoria
                                        //log.info("MOBLAR => NO se encuentra la SUBCATEGORIA en ECOMMERCE_CATALOGO. idListCatalogo: "+idListCatalogo);				
                                        // buscar ultimo numero idCatalogo
                                        Integer proximoId = this.sql.consulta_ecommerce_catalogo_ultimo_ID(idListCatalogo) + 1;
                                        //inserta_ecommerce_catalogo
                                        if (!this.sql.inserta_ecommerce_catalogo(proximoId, idListCatalogo, idCatCategoria, item.getAs_itm_subcategoria().trim())) {
                                            this.sql.actualiza_error_as_itm_load_file(idFile);
                                            throw new Exception("Error al ingresar la SUBCATEGORIA del item en la tabla ECOMMERCE_CATALOGO.");
                                        }

                                        idCatSubcategoria = this.sql.consulta_ecommerce_catalogo(item.getAs_itm_subcategoria().trim(), idListCatalogo, idCatCategoria);
                                    }

                                    Short estado = this.sql.consulta_ecommerce_catalogo_items_estado(idListCatalogo, idCatSubcategoria, id_itm);
                                    if (estado == null) {
                                        //primero borrar las sub categorias anteriores 
                                        if (!this.sql.borrar_ecommerce_catalogo_items(idListCatalogo, idCatSubcategoria, id_itm)) {
                                            this.sql.actualiza_error_as_itm_load_file(idFile);
                                            throw new Exception("Error al BORRAR los ITEMS asociados en la SUBCATEGORIA del item en la tabla ECOMMERCE_CATALOGO_ITEMS. idListCatalogo:" + idListCatalogo + ",idCatSubcategoria:" + idCatSubcategoria + ",id_itm:" + id_itm);
                                        }

                                        //ya esta cargada la subcategoria, solo agregamos un registro en ecommerce_catalogo_items
                                        if (!this.sql.inserta_ecommerce_catalogo_items(idListCatalogo, idCatSubcategoria, id_itm)) {
                                            this.sql.actualiza_error_as_itm_load_file(idFile);
                                            throw new Exception("Error al ingresar el ITEMS en la SUBCATEGORIA del item en la tabla ECOMMERCE_CATALOGO_ITEMS.");
                                        }
                                    } else if (estado == 0) {
                                        // activamos
                                        this.sql.actualiza_ecommerce_catalogo_items_estado(idListCatalogo, idCatSubcategoria, id_itm);
                                    }

                                } else {
                                    Short estado = this.sql.consulta_ecommerce_catalogo_items_estado(idListCatalogo, idCatCategoria, id_itm);
                                    if (estado == null) {
                                        //primero borrar las sub categorias anteriores 
                                        if (!this.sql.borrar_ecommerce_catalogo_items(idListCatalogo, idCatCategoria, id_itm)) {
                                            this.sql.actualiza_error_as_itm_load_file(idFile);
                                            throw new Exception("Error al BORRAR los ITEMS asociados en la SUBCATEGORIA del item en la tabla ECOMMERCE_CATALOGO_ITEMS. idListCatalogo:" + idListCatalogo + ",idCatCategoria:" + idCatCategoria + ",id_itm:" + id_itm);
                                        }

                                        //no mandaron subcategoria, insertamos items en categoria
                                        if (!this.sql.inserta_ecommerce_catalogo_items(idListCatalogo, idCatCategoria, id_itm)) {
                                            this.sql.actualiza_error_as_itm_load_file(idFile);
                                            throw new Exception("Error al ingresar el ITEMS en la CATEGORIA del item en la tabla ECOMMERCE_CATALOGO_ITEMS.");
                                        }
                                    } else if (estado == 0) {
                                        // activamos
                                        this.sql.actualiza_ecommerce_catalogo_items_estado(idListCatalogo, idCatCategoria, id_itm);
                                    }
                                }

                            } else {
                                log.info("MOBLAR => NO se encuentra el listado Moblart en ECOMMERCE_CATALOGO_CATALOGO.");
                            }

                        }
                        // contador de items bien procesados
                    }
                    this.cantProcesada++;
                } catch (Exception ex) {
                    log.info("ERROR guardando en db - Proceso Proceso Excel Moblar, cd. Items: " + item.getAs_itm_cd_itm());
                    log.error(ex.getMessage(), ex);
                    novedadesArchivo = novedadesArchivo.concat(ex.getMessage() + " & " + item.getAs_itm_cd_itm() + " | ");
                }
            }
        } catch (Exception ex) {
            //log.info("ERROR." );
            log.error(ex.getMessage(), ex);
        }
        log.info("MOBLAR - PROCESA FIN => guarda en bd");
    }

    public List<Item> leerArchivoXLSX(String archivoMIF) throws IOException {
        // Creamos instancia del archio a procesar.
        File file = new File(archivoMIF);
        // Obtenemos los bytes del archivo
        FileInputStream fis = new FileInputStream(file);

        // Creamos una instancia del 'libro de trabajo' seria el archivo de extencion .xlsx
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        // Creamos un objeto hoja y recuperamos aqui la hoja del archivo original.
        XSSFSheet sheet = wb.getSheetAt(0);
        // Iteramos sobre el archivo de excel
        Iterator<Row> itr = sheet.iterator();
        itr.next();

        int contFilas = 0;
        List<Item> listItems = new ArrayList<>();
        // iteramos por cada registro 'row'
        while (itr.hasNext()) {
            Row row = itr.next(); // ver como no tomar registros vacios
            try {
                // Iteramos sobre cada columna
                Iterator<Cell> cellIterator = row.cellIterator();
                // crea un items para asignar los valores del archivo
                Item itemAux = new Item();
                String codBarra = null;

                //Codigo barras padre 13 digitos ultimo de verificacion 1 COD_ARTICULO (ITM + COD)
                Cell cell = cellIterator.next();
                String aux = leerCell(cell);
                //log.info("codigo: "+ aux);
                if (aux != null && !aux.isEmpty()) {
                    contFilas++;
                    int cantDigitos = aux.length();
                    itemAux.setAs_itm_cd_itm(aux.substring(0, cantDigitos - 1));
                    codBarra = aux;
                    // Categoria LO SACAMOS
                    // cell = cellIterator.next();

                    //2 NOMBRE_ARTICULO (150)	
                    cell = cellIterator.next();
                    cantDigitos = cell.getStringCellValue().trim().length();
                    itemAux.setAs_itm_str_de_itm(cell.getStringCellValue().trim().substring(0, (cantDigitos > 150 ? 150 : cantDigitos)));

                    // Permite descuento LO SACAMOS
                    //cell = cellIterator.next();
                    // Requiere precio LO SACAMOS
                    //cell = cellIterator.next();
                    // 3 DESCRIPCION_ITEM 	
                    cell = cellIterator.next();
                    itemAux.setAs_itm_str_de_descripcion(leerCell(cell));

                    // 4 NOMBRE_IMAGEN (250)	
                    cell = cellIterator.next();
                    String imagen = leerCell(cell);  //trae una lista de imagenes
                    if (imagen != null && !imagen.isEmpty()) {
                        itemAux.setAs_itm_img(imagen);
                    } else {
                        itemAux.setAs_itm_img(null);
                    }

                    //Precio LO SACAMOS
                    //cell = cellIterator.next();
                    // 5 DIMENSIONES (tamaño) las tres alto x ancho X largo
                    cell = cellIterator.next();
                    String campo = limpiarCellNum(cell);
                    campo = campo.replace("x", "X").replace("(", "").replace(")", "");
                    // limpiamos si tiene dos medidas separadas por un espacio, usamos solo la primera
                    String[] campoAux = campo.split(" ");
                    campo = campoAux[0];

                    if (campo.isEmpty()) {
                        itemAux.setAs_itm_alto("");
                        itemAux.setAs_itm_ancho("");
                        itemAux.setAs_itm_largo("");
                    } else {
                        String[] dim = campo.split("X");

                        itemAux.setAs_itm_alto(dim[0].trim());

                        if (dim.length > 1) {
                            itemAux.setAs_itm_ancho(dim[1].trim());
                            if (dim.length > 2) {
                                itemAux.setAs_itm_largo(dim[2].trim());
                            } else {
                                itemAux.setAs_itm_largo("");
                            }
                        } else {
                            itemAux.setAs_itm_ancho("");
                            itemAux.setAs_itm_largo("");
                        }
                    }

                    //6 COLOR	
                    cell = cellIterator.next();
                    itemAux.setAs_itm_color(leerCell(cell));

                    //7 DISEÑADOR	
                    cell = cellIterator.next();
                    itemAux.setAs_itm_diseniador(leerCell(cell));

                    //8 CATEGORIA	
                    cell = cellIterator.next();
                    itemAux.setAs_itm_categoria(leerCell(cell));

                    //9 SUBCATEGORIA	
                    cell = cellIterator.next();
                    itemAux.setAs_itm_subcategoria(leerCell(cell));

                    //10 Peso Neto	
                    cell = cellIterator.next();
                    itemAux.setAs_itm_pesoNeto(limpiarCellNum(cell));

                    //11 Requiere Armado
                    cell = cellIterator.next();
                    String reqArmAux = limpiarCell(cell);
                    reqArmAux = reqArmAux.isEmpty() || reqArmAux == null || reqArmAux == "" ? "NO" : reqArmAux;
                    itemAux.setAs_itm_requiereArmado(reqArmAux);

                    //12 porcentaje de descuento
                    cell = cellIterator.next();
                    //itemAux.setAs_itm_str_prm_dsc_lbl(limpiarCell(cell));
                    //String desc = Integer.toString((int)(cell.getNumericCellValue()*100)).trim();
                    itemAux.setAs_itm_str_prm_dsc_lbl(leerCellPorcentaje(cell));

                    //13 Tiene Promocion
                    cell = cellIterator.next();
                    String promo = limpiarCell(cell);
                    promo = promo.isEmpty() || promo == null || promo == "" ? "NO" : promo;
                    itemAux.setAs_itm_str_fl_promocion(promo);
                    //log.info("Promo: "+ promo);

                    //14 Precio de Promocion NO ANDA BIEN
                    cell = cellIterator.next();
                    String prcPromo = leerCellNum(cell, wb);
                    itemAux.setAs_itm_str_prm_prc(prcPromo);

                    // 15 fecha inicio 18/3/2021
                    cell = cellIterator.next();
                    String fecAuxI = leerCellFecha(cell);
                    if (fecAuxI != null && !fecAuxI.isEmpty()) {
                        //log.info("Fecha Inicio: "+ fecAuxI);
                        Date fecIni = new SimpleDateFormat("MM/dd/yy").parse(fecAuxI);
                        itemAux.setAs_itm_str_prm_dtf(fecIni);

                    } else {
                        itemAux.setAs_itm_str_prm_dtf(null);
                    }

                    // 16 fecha fin 30/6/2021
                    cell = cellIterator.next();
                    String fecAuxF = leerCellFecha(cell);
                    if (fecAuxF != null && !fecAuxF.isEmpty() && fecAuxF != "") {
                        //log.info("Fecha Fin: "+ fecAuxF);
                        Date fecFin = new SimpleDateFormat("MM/dd/yy").parse(fecAuxF);
                        itemAux.setAs_itm_str_prm_dtt(fecFin);

                    } else {
                        itemAux.setAs_itm_str_prm_dtt(null);
                    }

                    // Agrega el item en una lista de items
                    listItems.add(itemAux);
                }
            } catch (Exception ex) {
                log.info("ERROR leyendo archivo - Proceso Excel Moblar, Items: " + contFilas);
                log.error(ex.getMessage(), ex);
                novedadesArchivo = novedadesArchivo.concat(ex.getMessage() + " & " + contFilas + " | ");
            }
            totalArchivo++;
        }
        return listItems;
    }

}
