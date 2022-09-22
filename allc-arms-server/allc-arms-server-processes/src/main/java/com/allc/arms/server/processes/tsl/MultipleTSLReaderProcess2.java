package com.allc.arms.server.processes.tsl;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.RetailStoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.arms.utils.tsl.TSLRecordXMLGenerator;
import com.allc.arms.utils.tsl.TSLUtility;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.comm.frame.Frame;
import com.allc.core.process.AbstractProcess;
import com.allc.entities.RetailStore;
import com.allc.entities.Transaction;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import java.io.FileFilter;

import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.ParseException;
import org.hibernate.Session;

public class MultipleTSLReaderProcess2 extends AbstractProcess {

    private Logger log = Logger.getLogger(MultipleTSLReaderProcess2.class);
    private PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    private RandomAccessFile randSeekRead = null;
    private RandomAccessFile randFileRead = null;

    private String tslSeekFileName;
    private long timeSleep;
    private String valorEnCero;
    private Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
    private boolean endTSLProcess = false;
    private boolean finished = false;
    private String storeNumber = "";
    private String tslFileName = null;
    private String businessDateDay;

    private SimpleDateFormat sdfBusinessDate = new SimpleDateFormat("yyyyMMdd");
    private final String path = "/home/ArchivosLGs/3";
    private final String pathProcesados = "/home/ArchivosLGs/procesados/3";

    private boolean init() {
        boolean result = false;
        try {
            log.info("Iniciando poceso");
            valorEnCero = Util.rpad(ArmsServerConstants.Communication.CERO, ArmsServerConstants.Communication.SPACE, 20)
                    + ArmsServerConstants.Communication.CRLF;

            timeSleep = Long.parseLong(properties.getObject("tslReader.timeSleep"));
            result = true;
            log.info("Version compilada en: 01/02/2022.");
            iniciarSesion("Arts");
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    FileFilter directoriFilefilter = (File file) -> {
        //if the file extension is .log return true, else false
        return file.isDirectory();
    };

    @Override
    public void run() {
        try {
            if (init()) {

                while (!endTSLProcess) {

                    File dir = new File(path);
                    File[] d = dir.listFiles(directoriFilefilter);
                    for (File directory : d) {
                        String[] filesProcesses = directory.list((File dir1, String name1) -> !name1.toLowerCase().endsWith("dat"));
                        log.info("Procesando directorio " + directory.getAbsolutePath());
                        for (String filesProcesse : filesProcesses) {
                            valorEnCero = Util.rpad(ArmsServerConstants.Communication.CERO, ArmsServerConstants.Communication.SPACE, 20)
                                    + ArmsServerConstants.Communication.CRLF;
                            log.info("Procesando directorios " + d.length + filesProcesse);

                            storeNumber = directory.getName();
                            tslSeekFileName = path + File.separator + storeNumber + File.separator + "tslseek.dat";
                            log.info("Seek file name --- " + tslSeekFileName);
                            Files.creaEscribeDataArchivo(getEyesFileName(), "TSL_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|STR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando proceso de lectura de TSL.\n", true);
                            File file = new File(path + File.separator + directory.getName() + File.separator + filesProcesse);
                            log.info("Store number " + storeNumber + file.getAbsolutePath());
                            if (file.exists()) {
                                String archivos = archivosProcesados() == null ? "" : archivosProcesados();
                                String[] procesados = archivos.split(",");
                                boolean procesar = true, exist = false;

                                if (new File(tslSeekFileName).exists()) {
                                    tslFileName = obtieneNombreSeek();
                                    if (tslFileName.equals(file.getAbsolutePath())) {
                                        log.info("Archivo a procesar igual al tsl -----");
                                        long pos = obtieneOffsetSeek();
                                        try {
                                            randFileRead = new RandomAccessFile(tslFileName, "r");
                                            log.info("position ---- " + pos + " ++++ " + randFileRead.length());
                                            if (pos < randFileRead.length()) {
                                                procesar = true;
                                                log.info("Se procesa true");
                                                valorEnCero = Util.rpad(pos + "", ArmsServerConstants.Communication.SPACE, 20)
                                                        + ArmsServerConstants.Communication.CRLF;
                                            } else {
                                                procesar = false;
                                                log.info("Se procesa false");
                                            }
                                        } catch (Exception e) {
                                            log.error(e.getMessage());
                                        }
                                        exist = true;
                                    } else {
                                        for (String procesado : procesados) {
                                            if (procesado.equals(file.getAbsolutePath())) {
                                                try {
                                                    procesar = false;
                                                    exist = true;
                                                } catch (Exception e) {
                                                    log.error(e.getMessage(), e);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    procesar = true;
                                }
                                try {
                                    randFileRead.close();
                                } catch (Exception e) {

                                }
                                log.info("Se procesara: ---" + procesar);
                                if (procesar) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
                                    String date = file.getName().replaceAll("(?i)LG", "").split("\\.")[0];
                                    Date fecha = sdf.parse(date);
                                    businessDateDay = sdfBusinessDate.format(fecha);
                                    Files.creaEscribeDataArchivo(tslSeekFileName, valorEnCero + "", false);
                                    Files.creaEscribeDataArchivo(tslSeekFileName, file.getAbsolutePath() + ArmsServerConstants.Communication.CRLF, true);
                                    String sr;
                                    if (!exist) {
                                        sr = file.getAbsolutePath() + "," + archivos + ArmsServerConstants.Communication.CRLF;
                                    } else {
                                        sr = archivos + ArmsServerConstants.Communication.CRLF;
                                    }
                                    Files.creaEscribeDataArchivo(tslSeekFileName, sr, true);
                                    log.info("Archivo a procesar " + file.getAbsolutePath() + " " + obtieneNombreSeek());
                                    if (!readTSLReg()) {
                                        if (!endTSLProcess) {
                                            Thread.sleep(timeSleep * 2);
                                        }
                                    } else {
                                        if (!(new File(pathProcesados + File.separator + directory.getName())).exists()) {
                                            new File(pathProcesados + File.separator + directory.getName()).mkdirs();
                                        }
                                        file.renameTo(new File(pathProcesados + File.separator + directory.getName() + File.separator + filesProcesse));
                                    }
                                } else {
                                    if (!(new File(pathProcesados + File.separator + directory.getName())).exists()) {
                                        new File(pathProcesados + File.separator + directory.getName()).mkdirs();
                                    }
                                    file.renameTo(new File(pathProcesados + File.separator + directory.getName() + File.separator + filesProcesse));
                                }
                            }
                        }
                        new File(tslSeekFileName).renameTo(new File(pathProcesados + File.separator + directory.getName() + File.separator + "tslseek.dat"));
                        directory.delete();
                    }
                    Thread.sleep(timeSleep * 600);
                }
                if (session != null) {
                    session.close();
                    session = null;
                }
                Files.creaEscribeDataArchivo(getEyesFileName(), "TSL_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Finalizó el proceso de lectura de TSL.\n", true);
            } else {
                Files.creaEscribeDataArchivo(getEyesFileName(), "TSL_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al iniciar el proceso de lectura de TSL.\n", true);
            }
        } catch (InterruptedException | ParseException e) {
            try {
                Files.creaEscribeDataArchivo(getEyesFileName(), "TSL_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error en el proceso de lectura de TSL.\n", true);
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
            log.error(e.getMessage(), e);
        }
        finished = true;
    }

    private String getEyesFileName() {
        return properties.getObject("eyes.ups.file.name") + "_" + ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    @Override
    public boolean shutdown(long timeToWait) {
        endTSLProcess = true;
        long startTime = Calendar.getInstance().getTimeInMillis();
        log.info("Deteniendo TSLReader...");
        while (!finished) {
            try {
                long dif = Calendar.getInstance().getTimeInMillis() - startTime;
                if (dif >= timeToWait) {
                    return false;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        return true;
    }

    private boolean readTSLReg() {
        boolean isStoreClosed = false;
        long tmp = 0;
        String valorPosicion;
        long punteroFile = 0;
        boolean sent = true;
        StringBuffer data = new StringBuffer();
        String reg = "";
        String linea = "";
        String decod = "";
        String value = "";
        List list;
        Frame frame;
        try {
            tslFileName = obtieneNombreSeek();
            randSeekRead = new RandomAccessFile(tslSeekFileName, "r");

            data.append(ArmsServerConstants.Communication.SOCKET_CHANNEL).append(ArmsServerConstants.Communication.FRAME_SEP).append(ArmsServerConstants.Process.TSL_PROCESS)
                    .append(ArmsServerConstants.Communication.FRAME_SEP).append("000").append(ArmsServerConstants.Communication.FRAME_SEP)
                    .append(storeNumber).append(ArmsServerConstants.Communication.FRAME_SEP)
                    .append(ArmsServerConstants.Communication.PERM_CONN).append(ArmsServerConstants.Communication.FRAME_SEP);

            punteroFile = obtieneOffsetSeek();
            log.info("puntero:" + punteroFile);
            if (punteroFile >= 0) {
                randFileRead = new RandomAccessFile(tslFileName, "r");
                boolean errorReadTSL = false;
                //utilizamos este contador para que si no avanza el puntero loguee cada 10 ciclos
                int logguerCount = 10;
                while (!endTSLProcess && !isStoreClosed && !errorReadTSL) {
                    if (logguerCount == 10) {
                        log.info("Leyendo tsl:" + (punteroFile));
                        log.info("Fecha Contable: " + businessDateDay);
                    } else if (logguerCount == 0) {
                        logguerCount = 10;
                    }
                    try {
                        linea = Files.readLineByBytesPositionOfFile(randFileRead, punteroFile);
                        log.info("Linea: " + linea);
                        //linea = Files.readLineByBytesPositionOfFile4690(randFileRead, punteroFile);
                        //log.info("Linea: " + linea);
                    } catch (Exception e) {
                        //agregamos esto para que cuando hagan un cierre "forzado" no quede en un bucle infinito
                        linea = null;
                        errorReadTSL = true;
                        log.error(e.getMessage(), e);
                        Files.creaEscribeDataArchivo(getEyesFileName(), "TSL_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al leer el archivo: " + tslFileName + ". Se volverá a crear el Puntero.\n", true);
                        Files.deleteFile(tslSeekFileName);
                    }

                    if (businessDateDay != null && !businessDateDay.equals("00000000") && null != linea && !linea.trim().equals("") && businessDateDay.length() == 8) {

                        reg = linea.substring(1, linea.length()) + "," + "\"";
                        decod = ident(reg).replace("'|'", "");

                        if (decod.length() > 0) {

//                            String[] sp = decod.split("\\|");
//                            for (String string : sp) {
//                                log.info("date decod " + string);
//                            }
//                            String date ;
//                            if(sp[0].equals("21"))
//                            {
//                                date = sp[1];
//                            }else{
//                                date = sp[3];
//                            }
//                            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
//                            Date newDate = sdf.parse(date);
                            value = data.toString() + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")
                                    + ArmsServerConstants.Communication.FRAME_SEP + decod + ArmsServerConstants.Communication.FRAME_SEP
                                    + businessDateDay;
                            list = Arrays.asList(p.split(value));
                            String[] decodArray = decod.split("\\|");
                            log.debug("TYPE: " + decodArray[0]);
                            /**
                             * StoreClosing *
                             */
                            if (decodArray[0].equals(ArmsServerConstants.Tsl.STORE_CLOSING)) {
                                isStoreClosed = true;
                                // Files.creaEscribeDataArchivo(getSyscardFileName(properties), "CIERRE" + "\n", true);
                            }

                            frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                                    ArmsServerConstants.Communication.FRAME_SEP);
                            if (frame.loadData()) {
                                sent = sendTSLRecord(frame);
                                if (!sent) {
                                    isStoreClosed = false;
                                    Thread.sleep(30000);
                                }
                            }

                        }
                        if (!isStoreClosed && sent) {
                            punteroFile = randFileRead.getFilePointer();
                            tmp = punteroFile;
                            valorPosicion = Util.rpad(String.valueOf(tmp), ArmsServerConstants.Communication.SPACE, 20)
                                    + ArmsServerConstants.Communication.CRLF;
                            Files.creaEscribeDataArchivoByPos(tslSeekFileName, valorPosicion, 0);
                        }
                    } else if ((null == linea || linea.trim().equals("")) && punteroFile < randFileRead.getFilePointer()) {
                        //si el puntero cambio es porque shay dos 0D0A seguidos y tenemos que avanzar el puntero para que continue leyendo
                        log.info("Punteros diferentes: punteroFile: " + punteroFile + " filePointer: " + randFileRead.getFilePointer());
                        punteroFile = randFileRead.getFilePointer();
                        tmp = punteroFile;
                        valorPosicion = Util.rpad(String.valueOf(tmp), ArmsServerConstants.Communication.SPACE, 20)
                                + ArmsServerConstants.Communication.CRLF;
                        Files.creaEscribeDataArchivoByPos(tslSeekFileName, valorPosicion, 0);

                    } else if ((null == linea || linea.trim().equals("")) && punteroFile == randFileRead.length()) {
                        log.info("Linea null o vacia punteros iguales y diferentes EamTram");
                        isStoreClosed = true;
                        break;
                    } else {
                        logguerCount--;
                        if (!errorReadTSL) {
                            Thread.sleep(timeSleep);
                        }
                    }
                    if (isStoreClosed) {
                        punteroFile = randFileRead.length();
                        tmp = punteroFile;
                        valorPosicion = Util.rpad(String.valueOf(tmp), ArmsServerConstants.Communication.SPACE, 20)
                                + ArmsServerConstants.Communication.CRLF;
                        Files.creaEscribeDataArchivoByPos(tslSeekFileName, valorPosicion, 0);
                    }
                    Thread.sleep(500);

                }
                try {
                    randFileRead.close();
                    randSeekRead.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                try {
                    randFileRead.close();
                    randSeekRead.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return isStoreClosed;
    }

    private String storeCode = null;
    private String syncPath = null;
    private BigInteger idBsnGp = null;
    private static String tslDefaultLocalRepositoryToStore;
    RetailStoreDAO retailStoreDAO = new RetailStoreDAO();
    private Session session = null;

    private void iniciarSesion(String name) {
        while (session == null) {
            try {
                session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            if (session == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private static final SimpleDateFormat SDFTIME = new SimpleDateFormat("yyyyMMddHHmmss");

    private boolean sendTSLRecord(Frame frame) {
        int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
        String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength);
        log.info("Trama a procesar : " + trama);
        //agregar codigo grupo
        storeCode = properties.getObject("eyes.store.code") == null ? "0" : properties.getObject("eyes.store.code");
        syncPath = properties.getObject("searchEbil.sync.folder.path");
        idBsnGp = properties.getObject("eyes.store.code.group") == null ? BigInteger.ZERO : new BigInteger(properties.getObject("eyes.store.code.group"));
        try {
            UtilityFile.createWriteDataFile(getEyesFileName(), "TSL_READ_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|STR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando procesamiento de TSL.\n", true);

            tslDefaultLocalRepositoryToStore = properties.getObject("TSL.defaultLocalRepositoryToStore");

            if (StringUtils.isNotBlank(tslDefaultLocalRepositoryToStore)) {
                UtilityFile.createDir(tslDefaultLocalRepositoryToStore);
            }
            log.info("TSL.defaultLocalRepositoryToStore: " + tslDefaultLocalRepositoryToStore);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            /**
             * Instantiate from TSLRecordXMLGenerator *
             */
            TSLRecordXMLGenerator tslRecordXMLGenerator = new TSLRecordXMLGenerator();
            /**
             * pass the TSLRecord and the path to save it as a xml file *
             */

            int tienda = (new Integer(((String) frame.getHeader().get(3))));
            RetailStore retailStore = retailStoreDAO.getRetailStoreByCode(session, tienda);
            //crar nuevo metodo generate que reciba codigo de grupo...
            // el metodo solo debe distinguir si es central o no
            Transaction trx = tslRecordXMLGenerator.generate(frame,
                    tslDefaultLocalRepositoryToStore,
                    true,
                    retailStore,
                    (idBsnGp.compareTo(BigInteger.ZERO) == 0 && Integer.valueOf(storeCode) == 0), //esCentral? 
                    syncPath);

            if (trx != null) {
                log.info("----- trx no null " + properties.getObject("google.storage.file.path"));

                //HABILITAR EN CASO DE REQUERIR SUBIR AL STORAGE DE GOOGLE
//                try {
//                    String fecha = SDFTIME.format(trx.getBeginDateTime());
//
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    JsonUtil ju = new JsonUtil();
//                    ju.setStoreId(trx.getRetailStoreCode());
//                    ju.setTimestamp(new Timestamp(trx.getBeginDateTime().getTime()));
//                    List<Item> items = new LinkedList<>();
//                    if (trx.getRetailTransaction() != null && trx.getRetailTransaction().getLineItems() != null) {
//                        for (Object lineItem : trx.getRetailTransaction().getLineItems()) {
//                            RetailTransactionLineItem rtli = ((RetailTransactionLineItem) lineItem);
//                            if (rtli != null) {
//                                SaleReturnLineItem itm = rtli.getSaleLI();
//                                if (itm != null) {
//                                    Item i = new Item(itm.getItemCode(), itm.getQuantity());
//                                    items.add(i);
//                                }
//                            }
//                        }
//                    }
//                    ju.setItems(items);
//                    String fileName = properties.getObject("google.storage.file.path") + File.separator + trx.getRetailStoreCode() + "-"
//                            + trx.getWorkstationCode() + "-" + trx.getSequenceNumber() + "-" + fecha + ".json";
//                    log.info("creando archivo json " + fileName);
//                    objectMapper.writeValue(new File(fileName), ju);
//                } catch (ParseException | IOException e) {
//                    log.error(e.getMessage(), e);
//                }
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
            try {
                UtilityFile.createWriteDataFile(getEyesFileName(), "TSL_READ_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al procesar la línea del TSL.\n", true);
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
            return false;

        }
    }

    /**
     * Obtiene la posicion del archivo store, desde la que se debe de enviar la
     * informacion.
     *
     * @param nombreFileSeek Nombre del archivo seek
     * @return La posicion de envio
     * @throws IOException Si el archivo seek no contiene un numero.
     */
    private long obtieneOffsetSeek() {
        long punteroFile;
        String data;
        try {
            randSeekRead = new RandomAccessFile(tslSeekFileName, "r");
            data = randSeekRead.readLine();
            randSeekRead.seek(0);
            if (null == data) {
                punteroFile = 0;
            } else {
                punteroFile = Long.parseLong(data.replaceAll(" ", ""));
            }
            randSeekRead.close();
        } catch (IOException | NumberFormatException e) {
            log.error(e.getMessage(), e);
            punteroFile = -1;
        }
        return punteroFile;
    }

    /**
     * Obtiene el nombre del archivo store, desde la que se debe de enviar la
     * informacion.
     *
     * @return El nombre del archivo
     * @throws IOException Si el archivo seek no contiene un numero.
     */
    private String obtieneNombreSeek() {
        String data = null;
        try {
            randSeekRead = new RandomAccessFile(tslSeekFileName, "r");
            randSeekRead.readLine();
            //la linea que contiene el nombre es la segunda
            data = randSeekRead.readLine();
            randSeekRead.seek(0);
            randSeekRead.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return data;
    }

    private String archivosProcesados() {
        String data = null;
        try {
            if (new File(tslSeekFileName).exists()) {
                randSeekRead = new RandomAccessFile(tslSeekFileName, "r");
                randSeekRead.readLine();
                randSeekRead.readLine();
                //la linea que contiene el nombre es la segunda
                data = randSeekRead.readLine();
                randSeekRead.seek(0);
                randSeekRead.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return data;
    }

    private String ident(String dato) throws Exception {
        byte[] arreglop, arregloj;
        arreglop = dato.substring(0, 1).getBytes("ISO-8859-1");
        String sal1, salp;
        String salida = "";
        String cadena;
        int tipo;
        int tcadena;
        List list, lista, listb;
        String REGEX = "\",\"";
        String CAMP = "\":\"";
        String SCAMP = ":";
        Pattern p = Pattern.compile(REGEX);
        Pattern q = Pattern.compile(CAMP);
        Pattern r = Pattern.compile(SCAMP);
        try {
            tipo = Integer.parseInt(TSLUtility.unpack(arreglop));
            log.info("Tipo: " + tipo);
            switch (tipo) {
                case 0:
                    list = Arrays.asList(p.split(dato));
                    for (int x = 0; x <= list.size() - 1; x++) {
                        cadena = list.get(x).toString();

                        arregloj = cadena.substring(0, 1).getBytes("ISO-8859-1");
                        tcadena = Integer.parseInt(TSLUtility.unpack(arregloj));

                        lista = Arrays.asList(r.split(cadena));
                        if (x == list.size() - 1) {
                            salp = TSLUtility.parseatlog(lista, tcadena);
                        } else {
                            salp = TSLUtility.parseatlog(lista, tcadena) + ",";
                        }

                        salida = salida + salp;

                    }
                    break;
                case 20:
                    lista = Arrays.asList(q.split(dato.substring(2)));
                    sal1 = TSLUtility.obtiene(lista, 3, 1);
                    salida = "20" + "|" + TSLUtility.parseaexecpt(lista, sal1);
                    // TODO esto se hace hasta que agreguemos el procesamiento de los excepton log en el server (sin header00)
                    salida = "";
                    break;
                case 21:
                    listb = Arrays.asList(r.split(dato.substring(2)));
                    salida = "21" + "|" + TSLUtility.obtiene(listb, 0, 1) + "|" + TSLUtility.obtiene(listb, 1, 1) + "|"
                            + TSLUtility.obtiene(listb, 2, 1) + "|" + TSLUtility.obtiene(listb, 3, 1) + "|";
                    break;
                default:
                    break;
            }
        } catch (UnsupportedEncodingException | NumberFormatException e) {
            log.error(e.getMessage(), e);
            salida = "";
        }
        return salida;
    }

    private String getSyscardFileName(PropFile properties) {
        return properties.getObject("syscard.file.name") + ".DAT";
    }

}
