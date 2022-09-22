/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.interfaces.webapi;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.processes.interfaces.RepositorioSQL;
import com.allc.arms.server.processes.interfaces.webapi.utils.Feature;
import com.allc.arms.server.processes.interfaces.webapi.utils.ItemList;
import com.allc.arms.server.processes.interfaces.webapi.utils.RootItems;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.mail.EmailSender;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
 */
public class ReviewEcommerceProceso extends AbstractProcess {

    private static final Logger LOGGER = Logger.getLogger(ReviewEcommerceProceso.class.getName());
    private final PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    private Session sessionArtsEcom = null;
    private Session sessionArts = null;

    private boolean finished = false;
    private boolean isEnd = false;
    private RepositorioSQL sql;

    private void iniciarArtsEcomSesion() {
        while (sessionArtsEcom == null && !isEnd) {
            try {
                sessionArtsEcom = HibernateSessionFactoryContainer.getSessionFactory("ArtsEcomm").openSession();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (sessionArtsEcom == null) {
                try {
                    LOGGER.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    private void iniciarArtsSesion() {
        LOGGER.info(" ----Conectando con Arts----");
        while (sessionArts == null && !isEnd) {
            try {
                sessionArts = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (sessionArts == null) {
                try {
                    LOGGER.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void run() {

        LOGGER.info(" ---------------------------- PROCESO de carga Archivo Maestro de Items -------------------------------");
        while (!isEnd) {
            iniciarArtsEcomSesion();
            iniciarArtsSesion();
            sql = new RepositorioSQL(sessionArts);
            try {
                UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|" + prop.getHostName() + "|3|" + prop.getHostAddress() + "|0|STR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando proceso de carga de Maestros de Articulos.\n", true);
                SimpleDateFormat HourFormat = new SimpleDateFormat("HH:mm");
                Date StartTime = HourFormat.parse("08:00");
                Date CurrentTime = HourFormat.parse(HourFormat.format(new Date()));

                LOGGER.info(" ---------------------------- INICIO PROCESO REVIEW -------------------------------");
                if (CurrentTime.after(StartTime)) {
                    procesarRevision();
                }
                LOGGER.info(" ---------------------------- FIN PROCESO REVIEW -------------------------------");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            sessionArtsEcom.close();
            sessionArtsEcom = null;
            sessionArts.close();
            sessionArts = null;
            try {
                LOGGER.info("Duermo: " + this.prop.getLong("interfaceMaestroItem.timesleep.webapi"));
                Thread.sleep(this.prop.getLong("interfaceMaestroItem.timesleep.webapi"));
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        finished = true;
    }

    @Override
    public boolean shutdown(long timeToWait) {
        isEnd = true;
        //closeConnection();
        long startTime = Calendar.getInstance().getTimeInMillis();
        LOGGER.info("Deteniendo Interfaces de carga de maestro de items ...");
        while (!finished) {
            try {
                long dif = Calendar.getInstance().getTimeInMillis() - startTime;
                if (dif >= timeToWait) {
                    return false;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("Finalizó el Proceso de Interfaces de carga de maestro de items.");
        return true;
    }

    private String getEyesFileName() {
        return prop.getObject("eyes.ups.file.name") + "_" + ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    private void procesarRevision() throws IOException {
        List<Object[]> files = sql.consulta_file_as_itm_load_file();
        for (Object[] file : files) {
            Long tienda = Long.parseLong(file[1].toString().split("\\-")[0]);
            LOGGER.info("Store to work " + tienda);
            if (file[5] != null) {
                String fileName = file[5].toString().replace("xls", "json");
                File jsonFile = new File(fileName);
                int id_bsn_un = this.sql.consulta_id_bsn_un_x_tienda(tienda);
                if (jsonFile.exists()) {
                    LOGGER.info("File to work " + jsonFile);
                    ObjectMapper objectMapper = new ObjectMapper();
                    RootItems root = objectMapper.readValue(jsonFile, RootItems.class);
                    Workbook workbook = new HSSFWorkbook();
                    //Crea hoja nueva
                    Sheet sheet = workbook.createSheet("Hoja de datos");
                    Map<String, Object[]> datos = new TreeMap<>();
                    datos.put("0", new Object[]{"Barcode",
                        "Descripciones",
                        "Coincide Nombre Item",
                        "Coincide Descripcion 1",
                        "Coincide Descripcion 2",
                        "Coincide Stock",
                        "Coincide Precio",
                        "Coincide alto",
                        "Coincide ancho",
                        "Coincide largo",
                        "Coincide peso"});
                    for (ItemList item : root.getItemList()) {
                        try {
                            if (item.getFeatures() != null) {
                                LOGGER.info("BarCode " + item.getBarcode());
                                Object[] row = sql.consulta_as_itm_ecomm(sessionArtsEcom, item.getPosBarcode(), id_bsn_un);
                                Long id_itm = Long.parseLong(row[0].toString());
                                String nm_itm_ecom = row[1].toString();
                                Double precio_ecom = Double.parseDouble(row[2].toString());
                                String de_descripcion_ecom = row[3].toString();
                                String de_itm_ecom = row[4].toString();
                                Double alto_ecom = Double.parseDouble(row[5].toString());
                                Double largo_ecom = Double.parseDouble(row[6].toString());
                                Double ancho_ecom = Double.parseDouble(row[7].toString());
                                Double peso_ecom = Double.parseDouble(row[8].toString());
                                Integer stock_ecom = Integer.parseInt(row[9].toString());
                                String nm_itm = "";
                                String de_descripcion = "";
                                String de_itm = "";
                                String imagenLg = "";
                                String imagenSm = "";
                                Double alto_itm = 0.00;
                                Double ancho_itm = 0.00;
                                Double largo_itm = 0.00;
                                Double peso_itm = 0.00;
                                int s = item.getStock() == null ? 0 : item.getStock();
                                int sdc = item.getStockDistributionCenter() == null ? 0 : item.getStockDistributionCenter();
                                Integer stock_itm = s + sdc;
                                Double prc_itm = item.getAffiliatePriceWithTax();
                                Boolean nm_itm_bool, de_descripcion_bool, de_itm_bool, stock_bool, prc_bool, alto_bool, largo_bool, ancho_bool, peso_bool;
                                for (Feature feature : item.getFeatures()) {
                                    switch (feature.getTypeName().toUpperCase()) {
                                        case "ECOMMERCE COLOR":
                                            break;
                                        case "ECOMMERCE ALTO":
                                            alto_itm = Double.parseDouble(feature.getDescription());
                                            break;
                                        case "ECOMMERCE ANCHO":
                                            ancho_itm = Double.parseDouble(feature.getDescription());
                                            break;
                                        case "ECOMMERCE PROFUNDIDAD":
                                            largo_itm = Double.parseDouble(feature.getDescription());
                                            break;
                                        case "ECOMMERCE PESO":
                                            peso_itm = Double.parseDouble(feature.getDescription());
                                            break;
                                        case "ECOMMERCE NOMBRE":
                                            nm_itm = feature.getDescription();
                                            break;
                                        case "ECOMMERCE DESCRIPCION":
                                            de_descripcion = de_descripcion.concat(" ").concat(feature.getDescription());
                                            break;
                                    }
                                }
                                nm_itm_bool = nm_itm_ecom.equalsIgnoreCase(nm_itm);
                                de_descripcion_bool = de_descripcion_ecom.equalsIgnoreCase(de_descripcion);
                                de_itm_bool = de_itm_ecom.equalsIgnoreCase(de_itm);
                                stock_bool = stock_itm.equals(stock_ecom);
                                prc_bool = precio_ecom.equals(prc_itm);
                                alto_bool = alto_itm.equals(alto_ecom);
                                largo_bool = largo_ecom.equals(largo_itm);
                                ancho_bool = ancho_ecom.equals(ancho_itm);
                                peso_bool = peso_ecom.equals(peso_itm);
//                                if (item.getImages() != null && !item.getImages().isEmpty()) {
//                                    List<String> imagenes = item.getImages().stream().map(img -> img.getName()).distinct().collect(Collectors.toList());
//
//                                    for (String imagen : imagenes) {
//                                        List<Image> images = item.getImages().stream().filter(img -> img.getName().equals(imagen)).collect(Collectors.toList());
//
//                                        for (Image image : images) {
//                                            if (!StringUtils.isBlank(image.getDimension()) && image.getDimension().equals("300X300")) {
//                                                imagenSm = image.getUrl();
//                                            }
//                                            if (!StringUtils.isBlank(image.getDimension()) && image.getDimension().equals("1000X1000")) {
//                                                imagenLg = image.getUrl();
//                                            }
//                                        }
//                                        
//                                    }
//                                }

                                if (!nm_itm_bool || !de_descripcion_bool
                                        || !de_itm_bool || !stock_bool
                                        || !prc_bool || !alto_bool
                                        || !largo_bool || !ancho_bool
                                        || !peso_bool) {
                                    datos.put(item.getBarcode(), new Object[]{
                                        item.getBarcode(),
                                        "SI",
                                        !nm_itm_bool ? "NO" : "SI",
                                        !de_descripcion_bool ? "NO" : "SI",
                                        !de_itm_bool ? "NO" : "SI",
                                        !stock_bool ? "NO" : "SI",
                                        !prc_bool ? "NO" : "SI",
                                        !alto_bool ? "NO" : "SI",
                                        !largo_bool ? "NO" : "SI",
                                        !ancho_bool ? "NO" : "SI",
                                        !peso_bool ? "NO" : "SI",});
                                }
                            } else {
                                LOGGER.info("no features " + item.getBarcode());
                                datos.put(item.getBarcode(), new Object[]{item.getBarcode(), "NO"});
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

                    String fileNameResult = prop.getObject("webapi.utils.auditoria.file") + File.separator + "revisiones " + File.separator;
                    if (!(new File(fileNameResult)).exists()) {
                        new File(fileNameResult).mkdirs();
                    }

                    try {
                        File f = new File(fileNameResult + File.separator + file[0].toString() + ".xls");
                        //Se genera el documento
                        FileOutputStream out = new FileOutputStream(f);
                        workbook.write(out);
                        out.close();
                        String message = "Se encontraron <b><font color='red'>" + (datos.size() - 1) + "</font></b> novedades en la revision del api del dia de hoy";

                        EmailSender.sendMailWithAttachment(
                                prop.getObject("smtp.user"),
                                prop.getObject("smtp.password"),
                                prop.getObject("smtp.server"),
                                prop.getObject("smtp.user"),
                                prop.getObject("administrator.alert.mail.to"),
                                "Alertas tiems Ecomerce.",
                                message,
                                f
                        );

                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                Long id_load_file = Long.parseLong(file[0].toString());
                if (!sql.update_as_itm_load_file(id_load_file)) {
                    LOGGER.error("Error al procesar archivo " + id_load_file);
                }
            }
        }
    }
}
