/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.interfaces.webapi;

import com.allc.arms.server.processes.interfaces.RepositorioSQL;
import com.allc.arms.server.processes.interfaces.webapi.utils.Feature;
import com.allc.arms.server.processes.interfaces.webapi.utils.Image;
import com.allc.arms.server.processes.interfaces.webapi.utils.ItemList;
import com.allc.arms.server.processes.interfaces.webapi.utils.RootItems;
import com.allc.arms.server.processes.interfaces.webapi.utils.Token;
import com.allc.properties.PropFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;

/**
 *
 * @author Tyrone Lopez
 * com.allc.arms.server.processes.interfaces.webapi.ProcesoWebApi
 */
public class ProcesoWebApi {

    private static final Logger LOGGER = Logger.getLogger(ProcesoWebApi.class.getName());

    private Token token;

    private final PropFile prop;

    private String idClient;

    private String clientSecret;

    private String tiendaActual;

    private final Calendar tokenExpired = Calendar.getInstance();

    private final RepositorioSQL sql;

    private Integer totalArchivo;
    private Integer totalProcesados;
    private Integer total;
    private String novedades = "";
    private boolean prcTotal;

    public ProcesoWebApi(PropFile pProp, Session session) {
        this.prop = pProp;
        sql = new RepositorioSQL(session);
    }

    public void procesarDatos() throws Exception {
        //String paramTienda = this.prop.getObject("interfaceMaestroItem.process.codipdv");
        List<String> tiendas = new LinkedList<>();
        Long tiendaJugeton = null;
        Long tiendaMoblart = null;
        String propJugueton = this.prop.getObject("interfaceMaestroItem.api.process.jugueton");
        String propMoblart = this.prop.getObject("interfaceMaestroItem.api.process.moblart");
        if (StringUtils.isNotBlank(propJugueton) && !"null".equals(propJugueton)) {
            tiendaJugeton = Long.parseLong(propJugueton);
            tiendas.add(tiendaJugeton.toString());
        }
        if (StringUtils.isNotBlank(propMoblart) && !"null".equals(propMoblart)) {
            tiendaMoblart = Long.parseLong(propMoblart);
            tiendas.add(tiendaMoblart.toString());
        }
        if (!tiendas.isEmpty()) {
            tiendaActual = tiendas.get(0);
            for (String tienda : tiendas) {
                Integer idFile = null;
                try {
                    totalProcesados = 0;
                    prcTotal = this.sql.consulta_id_bsn_un_stock_load(tienda); // tiendas habilitadas para ecommerce.

                    String totalParcial = prcTotal ? "-parcial" : "-total";
                    Object[] consulta_fila_as_itm_load_file = sql.consulta_fila_as_itm_load_file(tienda + totalParcial);
                    if (consulta_fila_as_itm_load_file == null) {
                        if (sql.inserta_as_itm_load_file(tienda + totalParcial, null, false)) {
                            consulta_fila_as_itm_load_file = sql.consulta_fila_as_itm_load_file(tienda + totalParcial);
                            if (consulta_fila_as_itm_load_file == null) {
                                throw new Exception("No se crea as_itm_load_file");
                            }
                        }
                    }
                    idFile = (Integer) consulta_fila_as_itm_load_file[0];
                    if (Integer.parseInt(consulta_fila_as_itm_load_file[4].toString()) != 1) {

                        RootItems root = null;
                        try {
                            root = getItemByWork(tienda);
                        } catch (Exception e) {
                            if (idFile != null) {
                                if (!this.sql.actualiza_error_as_itm_load_file(idFile, e.getMessage())) {
                                    LOGGER.error("No se pudo actualizar as_itm_load_file");
                                }
                            }
                            throw new Exception("No se pudo conectar al host");
                        }
                        if (root == null || root.getItemList().isEmpty()) {
                            if (!this.sql.actualiza_error_as_itm_load_file(idFile, "No se pudo conectar al host")) {
                                throw new Exception("No se pudo actualizar estado as_itm_load_file");
                            }
                        } else {
                            String fileNameJson = prop.getObject("webapi.utils.auditoria.file") + File.separator + idFile.toString() + totalParcial + ".json";
                            ObjectMapper objectMapper = new ObjectMapper();
                            objectMapper.writeValue(new File(fileNameJson), root);
                            if (!this.sql.actualiza_estado_as_itm_load_file(idFile)) {
                                throw new Exception("No se pudo actualizar estado as_itm_load_file");
                            }
                            totalArchivo = root.getTotalItems();
                            total = root.getItemList().size();
                            Workbook workbook = new HSSFWorkbook();
                            //Crea hoja nueva
                            Sheet sheet = workbook.createSheet("Hoja de datos");
                            Map<String, Object[]> datos = new TreeMap<>();
                            datos.put("0", new Object[]{"Bar code", "Nombre Item", "Descripcion 1", "Descripcion 2", "Imagen sm", "Imagen lg", "Stock", "Precio", "Tiene oferta", "precio oferta", "Caracteristicas", "Se publica",});
                            for (ItemList item : root.getItemList()) {
                                try {
                                    int fl_wm_rq = 0;
                                    if (item.getBulkSale()) {
                                        fl_wm_rq = 1;
                                    }
                                    boolean fl_rp_rq = false;
                                    boolean fl_qy_alw = true;
                                    boolean fl_itm_dsc = true;
                                    boolean fl_qy_rq = false;
                                    long id_itm;
                                    Object[] rsITM = sql.consulta_as_itm_x_codigo(item.getPosBarcode());
                                    String id_ps_itm = item.getBarcode();
                                    if (id_ps_itm.substring(0, 2).equals("26") && Long.parseLong(id_ps_itm) >= Long.parseLong("2600000000000")
                                            && Long.parseLong(id_ps_itm) < Long.parseLong("2700000000000") && id_ps_itm.substring(6, 12).equals("000000")) {
                                        id_ps_itm = id_ps_itm.substring(0, 6) + id_ps_itm.substring(12, 13);
                                    }
                                    Integer id_mrhrc_gp;
                                    if (item.getSubclassificationCode() != null && item.getCategoryCode() != null) {
                                        id_mrhrc_gp = this.sql.consulta_co_mrhrc_gp_x_cd_mrhrc_gp_id_dpt_ps(Integer.parseInt(item.getSubclassificationCode()), Integer.parseInt(item.getCategoryCode()));
                                        if (id_mrhrc_gp == null) {
                                            if (this.sql.consulta_id_dpt_ps(Integer.parseInt(item.getCategoryCode())) == null) {
                                                if (!this.sql.inserta_id_dpt_ps(Integer.parseInt(item.getCategoryCode()), item.getCategory())) {
                                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                                    throw new Exception("Error al ingresar los items en la tabla ID_DPT_PS");
                                                }

                                            }
                                            if (this.sql.inserta_co_mrhrc_gp(Integer.parseInt(item.getSubclassificationCode()), item.getSubclassification(), Integer.parseInt(item.getCategoryCode()))) {
                                                id_mrhrc_gp = this.sql.consulta_co_mrhrc_gp_x_cd_mrhrc_gp_id_dpt_ps(Integer.parseInt(item.getSubclassificationCode()), Integer.parseInt(item.getCategoryCode()));
                                            } else {
                                                this.sql.actualiza_error_as_itm_load_file(idFile);
                                                throw new Exception("Error al ingresar los items en la tabla ID_DPT_PS");
                                            }
                                        }

                                        //Actualizacion o insercion de item
                                        String brand = item.getBrand().replaceAll("'", "");
                                        String nm_itm = "";
                                        String nm_ecomm = "";
                                        String de_descripcion = "";
                                        String de_itm = "";
                                        String imagenLg = "";
                                        String imagenSm = "";
                                        Integer stock = 0;
                                        String complejidad_armado = "";
                                        String diseniador = "";
                                        String color = "";
                                        double alto = 0.00;
                                        double ancho = 0.00;
                                        double largo = 0.00;
                                        double peso = 0.00;
                                        String categoria = "";
                                        String subCategoria = "";
                                        if (item.getFeatures() != null) {
                                            for (Feature feature : item.getFeatures()) {
                                                switch (feature.getTypeName().toUpperCase()) {
                                                    case "ECOMMERCE COLOR":
                                                        color = feature.getDescription();
                                                        break;
                                                    case "ECOMMERCE ALTO":
                                                        alto = Double.parseDouble(feature.getDescription());
                                                        break;
                                                    case "ECOMMERCE ANCHO":
                                                        ancho = Double.parseDouble(feature.getDescription());
                                                        break;
                                                    case "ECOMMERCE PROFUNDIDAD":
                                                        largo = Double.parseDouble(feature.getDescription());
                                                        break;
                                                    case "ECOMMERCE PESO":
                                                        peso = Double.parseDouble(feature.getDescription());
                                                        break;
                                                    case "ECOMMERCE NOMBRE":
                                                        nm_ecomm = feature.getDescription();
                                                        break;
                                                    case "ECOMMERCE DESCRIPCION":
                                                        de_descripcion = de_descripcion.concat(" ").concat(feature.getDescription());
                                                        break;
                                                    case "ECOMMERCE COLECCION":
                                                        diseniador = feature.getDescription();
                                                        break;
                                                    case "ECOMMERCE ARMADO":
                                                        complejidad_armado = feature.getDescription();
                                                        break;
                                                    case "ECOMMERCE CATEGORIA":
                                                        categoria = feature.getDescription();
                                                        break;
                                                    case "ECOMMERCE SUBCATEGORIA":
                                                        subCategoria = feature.getDescription();
                                                        break;
                                                }
                                            }
                                        } else {
                                            LOGGER.info("no features " + item.getBarcode());
                                        }
                                        boolean reqArmado = StringUtils.isNotBlank(complejidad_armado);
                                        if (rsITM == null) { // AGREGA ITM
                                            if (!this.sql.inserta_as_itm_api(
                                                    item.getPosBarcode(),
                                                    item.getDescription(),
                                                    fl_itm_dsc,
                                                    fl_rp_rq,
                                                    fl_wm_rq,
                                                    fl_qy_rq,
                                                    fl_qy_alw,
                                                    StringUtils.isBlank(nm_ecomm) ? item.getDescription() : nm_ecomm,
                                                    de_descripcion,
                                                    id_mrhrc_gp,
                                                    alto,
                                                    ancho,
                                                    largo,
                                                    color,
                                                    peso,
                                                    diseniador,
                                                    reqArmado
                                            )) {
                                                this.sql.actualiza_error_as_itm_load_file(idFile);
                                                throw new Exception("Error al ingresar los items en la tabla ID_DPT_PS");
                                            }
                                            rsITM = this.sql.consulta_as_itm_x_codigo(item.getPosBarcode());

                                        } else { //Actualiza item
                                            id_itm = Integer.parseInt(rsITM[0].toString());
                                            nm_itm = "".equals(nm_itm) ? item.getDescription() : nm_itm;
                                            de_itm = item.getDescription() == null ? rsITM[1].toString() : item.getDescription();
                                            String de_aux = rsITM[2] == null ? "" : rsITM[2].toString();
                                            de_descripcion = StringUtils.isBlank(de_descripcion) ? de_aux : de_descripcion;
                                            String edad = rsITM[3] != null ? rsITM[3].toString() : "";
                                            String genero = rsITM[8] != null ? rsITM[8].toString() : "";
                                            String diseAux = rsITM[10] != null ? rsITM[10].toString() : "";
                                            diseniador = StringUtils.isBlank(diseniador) ? diseAux : diseniador;
                                            alto = alto == 0.0 ? Double.parseDouble(rsITM[4] == null ? "0.0" : rsITM[4].toString()) : alto;
                                            ancho = ancho == 0.0 ? Double.parseDouble(rsITM[5] == null ? "0.0" : rsITM[5].toString()) : ancho;
                                            largo = largo == 0.0 ? Double.parseDouble(rsITM[6] == null ? "0.0" : rsITM[6].toString()) : largo;
                                            peso = peso == 0.0 ? Double.parseDouble(rsITM[7] == null ? "0.0" : rsITM[7].toString()) : peso;

                                            Long idSpr = null;
                                            BigInteger rsCoSPR = this.sql.consulta_co_spr_detalle(brand);
                                            // si no existe el proveedor lo agregamos
                                            if (rsCoSPR == null) {
                                                // Inserta registro en tabla 'co_spr'
                                                if (!this.sql.inserta_co_spr(brand)) {
                                                    // si hay un error, marca en la tabla del archivo que tienen errores
                                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                                    throw new Exception("Error al ingresar item en la tabla AS_ITM");
                                                }

                                                // verifico que se creeo y busco el id
                                                BigInteger rsCoSPR2 = this.sql.consulta_co_spr_detalle(brand);
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
                                            color = rsITM[9] != null ? rsITM[9].toString() : "";
                                            if (!this.sql.actualiza_as_itm_api(
                                                    nm_itm,
                                                    nm_ecomm,
                                                    de_descripcion,
                                                    alto,
                                                    ancho,
                                                    largo,
                                                    color,
                                                    diseniador,
                                                    reqArmado,
                                                    idSpr,
                                                    edad,
                                                    peso,
                                                    genero,
                                                    item.getPosBarcode(),
                                                    fl_rp_rq,
                                                    fl_wm_rq,
                                                    id_mrhrc_gp,
                                                    id_itm)) {
                                                this.sql.actualiza_error_as_itm_load_file(idFile);
                                                throw new Exception("Error al actualizar item en la tabla AS_ITM");
                                            }
                                        }

                                        List<Integer> rsIDBSNUN = this.sql.consulta_id_bsn_un_x_codigo(Long.parseLong(tienda));
                                        id_itm = Integer.parseInt(rsITM[0].toString());

                                        for (Integer row : rsIDBSNUN) {
                                            long id_bsn_un = row;

                                            boolean fl_azn_fr_sls = true;

                                            int tx_a = item.getValueAddedTax() ? 1 : 0;

                                            int flg_may = 0;

                                            int fl_promocion = 0;
                                            double prm_prc = 0;
                                            Date fechaInicio = null;
                                            Date fechaFin = null;
                                            String prm_dsc_lbl = null;
                                            if (item.getOfferPercentage() != null && item.getOfferPercentage() > 0.0) {
                                                fl_promocion = 1;
                                                prm_prc = item.getOfferPriceWithTax();
                                                fechaInicio = item.getOfferStartDate();
                                                fechaFin = item.getOfferEndDate();
                                                prm_dsc_lbl = item.getOfferPercentage().toString();
                                            }
                                            // Inserta un registro en AS_ITM_STR por cada tienda
                                            Integer stockAux = item.getStock() == null ? 0 : item.getStock();
                                            Integer stockAux2 = item.getStockDistributionCenter() == null ? 0 : item.getStockDistributionCenter();
                                            stock = stockAux + stockAux2;
                                            Object[] rsASITMSTR = this.sql.consulta_as_itm_str_codigos(id_itm, id_bsn_un);
                                            if (rsASITMSTR == null) {
                                                if (!this.sql.inserta_as_itm_str_api(
                                                        id_itm,
                                                        id_bsn_un,
                                                        (float) (item.getAffiliatePriceWithTax()),
                                                        fl_azn_fr_sls,
                                                        tx_a,
                                                        flg_may,
                                                        0,
                                                        0,
                                                        color,
                                                        "",
                                                        brand,
                                                        "",
                                                        stock
                                                )) {
                                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                                    throw new Exception("Error al ingresar los items en la tabla AS_ITM_STR" + id_itm + color);
                                                }
                                            } else {
                                                // SI EXISTE LO ACTUALIZAMOS

                                                // ver si ya tienen marca no cambiar
                                                // Inserta un registro en AS_ITM_STR por cada tienda
                                                String marca = rsASITMSTR[2] == null ? brand : rsASITMSTR[2].toString();

                                                if (!this.sql.actualiza_as_itm_str_api(
                                                        (float) (item.getAffiliatePriceWithTax()),
                                                        tx_a,
                                                        marca,
                                                        stock.floatValue(),
                                                        fl_promocion,
                                                        prm_dsc_lbl,
                                                        fechaInicio,
                                                        fechaFin,
                                                        prm_prc,
                                                        id_itm,
                                                        id_bsn_un
                                                )) {
                                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                                    throw new Exception("Error al ingresar los items en la tabla AS_ITM_STR");
                                                }
                                            }

                                            // preguntar si ya existe no agregar ni editar
                                            String rsIDPS = this.sql.consulta_id_ps(id_itm, id_bsn_un);

                                            // Actualizar codigo de barra ver si sacamos de codipdv
                                            // vemos si existe 
                                            if (rsIDPS == null) {
                                                // si no existe lo creo
                                                if (!this.sql.inserta_id_ps(
                                                        id_ps_itm,
                                                        id_itm,
                                                        id_bsn_un)) {
                                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                                    throw new Exception("Error al ingresar los códigos de barra en la tabla ID_PS");
                                                }
                                            } else {
                                                // si existe actualizo
                                                if (!this.sql.actualiza_id_ps(
                                                        id_ps_itm,
                                                        id_itm,
                                                        id_bsn_un)) {
                                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                                    throw new Exception("Error al ingresar los códigos de barra en la tabla ID_PS");
                                                }
                                            }

                                            //
                                            if ((tiendaJugeton != null && tiendaJugeton == Long.parseLong(tienda)) || (tiendaMoblart != null && tiendaMoblart == Long.parseLong(tienda))) {
                                                // insertar en tabla de categorias y subcategorias para el ecommerce
                                                if (!categoria.isEmpty()) {
                                                    // primero validamos que tengamos listado de moblar
                                                    String nombreTienda = "MOBLART";
                                                    if (tiendaJugeton != null && tiendaJugeton == Long.parseLong(tienda)) {
                                                        nombreTienda = "JUGUETÓN";
                                                    }
                                                    Integer idListCatalogo = this.sql.consulta_id_listado_catalogo(nombreTienda);
                                                    if (idListCatalogo != null) {
                                                        //Buscamos categoria, si no existe la creamos
                                                        Integer idCatCategoria = this.sql.consulta_ecommerce_catalogo(categoria.trim(), idListCatalogo);
                                                        if (idCatCategoria == null) {
                                                            // No esta la categoria, la creamos 
                                                            Integer idCatPadre = this.sql.consulta_ecommerce_catalogo_nodo_padre(idListCatalogo);
                                                            // buscar ultimo numero idCatalogo
                                                            Integer proximoId = this.sql.consulta_ecommerce_catalogo_ultimo_ID(idListCatalogo) + 1;
                                                            //inserta_ecommerce_catalogo
                                                            if (!this.sql.inserta_ecommerce_catalogo(proximoId, idListCatalogo, idCatPadre, categoria.trim())) {
                                                                this.sql.actualiza_error_as_itm_load_file(idFile);
                                                                throw new Exception("Error al ingresar la CATEGORIA del item en la tabla ECOMMERCE_CATALOGO.");
                                                            }

                                                            idCatCategoria = this.sql.consulta_ecommerce_catalogo(categoria.trim(), idListCatalogo);

                                                        }

                                                        if (!subCategoria.isEmpty()) {
                                                            // mandaron subcategoria, buscar subcategoria arts_ec.ecommerce_catalogo para esta busqueda agregar idpadre
                                                            Integer idCatSubcategoria = this.sql.consulta_ecommerce_catalogo(subCategoria.trim(), idListCatalogo, idCatCategoria);
                                                            if (idCatSubcategoria == null) {
                                                                //cargamos la subcategoria				
                                                                // buscar ultimo numero idCatalogo
                                                                Integer proximoId = this.sql.consulta_ecommerce_catalogo_ultimo_ID(idListCatalogo) + 1;
                                                                //inserta_ecommerce_catalogo
                                                                if (!this.sql.inserta_ecommerce_catalogo(proximoId, idListCatalogo, idCatCategoria, subCategoria.trim())) {
                                                                    this.sql.actualiza_error_as_itm_load_file(idFile);
                                                                    throw new Exception("Error al ingresar la SUBCATEGORIA del item en la tabla ECOMMERCE_CATALOGO.");
                                                                }

                                                                idCatSubcategoria = this.sql.consulta_ecommerce_catalogo(subCategoria.trim(), idListCatalogo, idCatCategoria);
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
                                                        LOGGER.info(nombreTienda + " => NO se encuentra el listado " + nombreTienda + " en ECOMMERCE_CATALOGO_CATALOGO.");
                                                    }

                                                }
                                            }
                                            if (item.getImages() != null && !item.getImages().isEmpty()) {
                                                sql.borra_as_itm_imagen_registro(id_itm);
                                                List<String> imagenes = item.getImages().stream().map(img -> img.getName()).distinct().collect(Collectors.toList());

                                                for (String imagen : imagenes) {
                                                    List<Image> images = item.getImages().stream().filter(img -> img.getName().equals(imagen)).collect(Collectors.toList());

                                                    for (Image image : images) {
                                                        if (!StringUtils.isBlank(image.getDimension()) && image.getDimension().equals("300X300")) {
                                                            imagenSm = image.getUrl();
                                                        }
                                                        if (!StringUtils.isBlank(image.getDimension()) && image.getDimension().equals("1000X1000")) {
                                                            imagenLg = image.getUrl();
                                                        }
                                                    }
                                                    if (StringUtils.isNotBlank(imagenLg) && StringUtils.isNotBlank(imagenSm)) {

                                                        Object[] rsIMAGEN = this.sql.consulta_as_itm_imagen_registro(id_itm);
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
                                            }
                                        }
                                        totalProcesados++;
                                        Object[] rsIMAGEN = this.sql.consulta_as_itm_imagen_registro(id_itm);
                                        datos.put(item.getBarcode(), new Object[]{item.getBarcode(), nm_itm, de_itm, de_descripcion, imagenSm, imagenLg, stock, (Double) item.getAffiliatePriceWithTax(), item.getOfferPercentage() == null ? "No" : "Si", item.getOfferPriceWithTax(), item.getFeatures() != null ? "Si" : "No", stock >= 1 && rsIMAGEN != null ? "SI" : "NO - POR IMAGENES O STOCK"});
                                    } else {
                                        LOGGER.info("No entra codigos null --- " + tienda + " code " + item.getBarcode());
                                        datos.put(item.getBarcode(), new Object[]{item.getBarcode(), "", "", "", "", "", "", "", "", "NO - POR CATEGORIA O SUBCATEGORIA"});
                                    }
                                } catch (Exception e) {
                                    LOGGER.error(e.getMessage(), e);
                                }
                            }
                            Set<String> keyset = datos.keySet();
                            int numeroRenglon = 0;
                            for (String key : keyset) {
                                Row row = sheet.createRow(numeroRenglon++);
                                Object[] arregloObjetos = datos.get(key);
                                int numeroCelda = 0;
                                for (Object obj : arregloObjetos) {
                                    Cell cell = row.createCell(numeroCelda++);
                                    if (obj instanceof String) {
                                        cell.setCellValue((String) obj);
                                    } else if (obj instanceof Integer) {
                                        cell.setCellValue((Integer) obj);
                                    } else if (obj instanceof Double) {
                                        cell.setCellValue((Double) obj);
                                    }
                                }
                            }

                            String fileName = prop.getObject("webapi.utils.auditoria.file") + File.separator + idFile.toString() + totalParcial + ".xls";
                            try {
                                File f = new File(fileName);
                                //Se genera el documento
                                FileOutputStream out = new FileOutputStream(f);
                                workbook.write(out);
                                out.close();
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                            if (!sql.actualiza_info_as_item_load_file_API(idFile, total, totalProcesados, totalArchivo, null, fileName)) {
                                //log.error("Error al actualizar estado ejecutando AS_ITM_LOAD_FILE..");
                                throw new Exception("Error al actualizar estado ejecutando AS_ITM_LOAD_FILE..");
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                    if (idFile != null) {
                        this.sql.actualiza_error_as_itm_load_file(idFile, e.getMessage());
                    }
                }
            }
        }
    }

    private RootItems getItemByWork(String tienda) throws Exception {
        // Integer bloque = Integer.parseInt(prop.getObject("webapi.utils.bloque"));
        Integer bloque = 500;
        RootItems root = new RootItems();
        root.setTotalItems(bloque);
        //for (int i = 0; i < 500; i = i + bloque) {
        for (int i = 0; i < root.getTotalItems(); i = i + bloque) {
            LOGGER.info("Consultando tienda: " + tienda + " bloque: " + i + " de: " + root.getTotalItems());
            //"https://www.cfavorita.ec/capedServices/item/v2/findItems/"
            //https://www.cfavorita.ec/ecomServices/item/v2/findItems
            String urlString = prop.getObject("webapi.utils.url.items");
            URL url = new URL(urlString + tienda);
            //URL url = new URL("https://aplpre.favorita.ec/capedServices/item/findItems/" + code);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json");
            Token tok = getToken(tienda);
            if (tok != null) {
                con.setRequestProperty("Authorization", "Bearer " + tok.getAccess_token());
                /* Payload support */
                con.setDoOutput(true);
                try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
                    String cantidad = prcTotal ? "true" : "false";
                    out.writeBytes("{\"offset\":" + i + ",\"limit\":" + bloque + ",\"enableCount\":true,\"dailyInventoryMovement\":" + cantidad + "}");
                    out.flush();
                }

                int status = con.getResponseCode();
                if (status == 200) {
                    ObjectMapper om = new ObjectMapper();

                    try {
                        RootItems readValue = om.readValue(con.getInputStream(), RootItems.class);
                        //om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        root.getItemList().addAll(readValue.getItemList());
                        root.setTotalItems(readValue.getTotalItems());
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }

                } else {
                    LOGGER.error("error status + " + status);
                    return null;
                }
                con.disconnect();
            } else {
                return null;
            }
        }
        return root;
    }

    private Token getToken(String tienda) throws Exception {
//        idClient = prop.getObject("webapi.utils.security.idClient");
//        clientSecret = prop.getObject("webapi.utils.security.clientSecret");

        //jugueton
        if (tienda.equals("194")) {
            LOGGER.info("Token 194");
            idClient = "EcommerceTM";
            clientSecret = "e9c69813-24c6-44ff-b36b-57f1747be1a1";
        } else //moblart
        {
            LOGGER.info("Token 124");
            idClient = "EcomMOBLART";
            clientSecret = "bb9de4bb-bbc6-4bed-8151-74b8355224db";
        }
        if (!tiendaActual.equals(tienda)) {
            token = null;
        }
        try {
            if (token == null) {
                getToken(true);
                tokenExpired.add(Calendar.SECOND, token.getExpires_in());
            } else {
                if (new Date().after(tokenExpired.getTime())) {
                    tokenExpired.setTime(new Date());
                    tokenExpired.add(Calendar.SECOND, token.getExpires_in());
                    getToken(true);
                }
            }
            tiendaActual = tienda;
            return token;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private void getToken(boolean isNew) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (isNew) {
            sb.append("grant_type=client_credentials&client_secret=").append(clientSecret).append("&client_id=").append(idClient);
        } else {
            sb.append("grant_type=refresh_token&refresh_token=").append(token.getRefresh_token()).append("&client_id=").append(idClient).append("&client_secret=").append(clientSecret);
        }
        URL url = new URL(prop.getObject("webapi.utils.token"));
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        /* Payload support */
        con.setDoOutput(true);
        try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
            out.writeBytes(sb.toString());
            out.flush();
        } catch (Exception e) {

        }
        int status = con.getResponseCode();
        if (status == 200) {
            ObjectMapper om = new ObjectMapper();
            token = om.readValue(con.getInputStream(), Token.class);
        } else {
            LOGGER.error("error al obtener el token " + prop.getObject("webapi.utils.token"));
            throw new Exception("Error al obtener el token");
        }
        con.disconnect();
    }

}
