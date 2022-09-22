package com.allc.arms.agent.processes.fileUpdate;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.RandomAccessFile4690;
import java.util.List;

/**
 * Proceso encargado de mantener actualizadas las copias de los archivos
 * EAMTRANA, EAMTRANB, EAMTRANC y en futuro exception log
 *
 * @author gustavo
 *
 */
public class FileUpdaterTSLELProcess extends AbstractProcess {

    protected Logger log = Logger.getLogger(FileUpdaterTSLELProcess.class);
    protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
    protected long timeSleep;
    protected File4690 outFolder;
    protected File4690 originFolder;
    protected boolean isEnd = false;
    protected boolean finished = false;
    private String store;

    protected boolean init() {
        boolean result = false;
        try {
            ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
            store = (new Integer(controllerStatusData.getStoreNumber())).toString();
            outFolder = new File4690(properties.getObject("fileUpdaterUp.out.folder.copy.path"));//new File4690(properties.getObject("fileUpdaterUp.out.folder.path"));
            outFolder.mkdir();
            //originFolder = loadOriginFolders(properties.getObject("fileUpdaterUp.origin.folders"));
            timeSleep = Long.parseLong(properties.getObject("fileUpdaterUp.timeSleep"));
            result = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public void run() {
        log.info("Iniciando File Updater TSL-EL Process...");
        init();
        while (!isEnd) {
            try {
                List<String> files = properties.getList("fileUpdaterUp.out.folder.copy.files");
                for (String file : files) {
                    updateFile(file);
                }
                Thread.sleep(timeSleep);

            } catch (Exception e) {
                try {
                    Files.creaEscribeDataArchivo4690(getEyesFileName(), "FILE_UPD_TSLEL_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + store + "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al procesar archivo.\n", true);
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
                log.error(e.getMessage(), e);
            }
        }
        finished = true;
    }

    private boolean updateFile(String fileName) {
        try {
            String outFileName = outFolder.getAbsolutePath() + File.separator + fileName;
            File4690 outFile = new File4690(outFileName);

            String originFileName = properties.getObject("TSL.path.eamterms") + File.separator + fileName;
            File4690 originFile = new File4690(originFileName);

            log.info("Archivos: " + originFileName + " - " + outFileName);
            if (!originFile.exists()) {
                log.info("Archivo orgen " + originFileName + " no existe");
                return false;
            }
            if (!outFile.exists()) {
                log.info("Archivo destino " + outFileName + " no existe");
                outFile.createNewFile();
            }
            long outFileLength = outFile.length();

            log.info("originFile " + originFile.length() + " bytes");
            log.info("outFile " + outFileLength + " bytes");

            if (originFile.length() > outFileLength) {
                log.info("Hay diferencias");
                long count = originFile.length() - outFileLength;
                log.info("Hay diferencias de " + count + " bytes");
                RandomAccessFile4690 raf = null;
                try {
                    raf = new RandomAccessFile4690(originFile, "r");
                    String data = Files.readDataByBytesPositionOfFile4690(raf, outFileLength, count);

                    Files.creaEscribeDataArchivo4690ByPos(outFileName, data, (int) outFileLength);
                    log.info("Archivo Actualizado");

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (Exception ex) {
                            log.error(ex.getMessage());
                        }
                    }
                }
            } else if (originFile.length() < outFile.length()) {
                outFile.delete();
                log.info("Borramos archivo " + outFileName + " porque es mas chico que el original");
            } else {
                log.info("NO hay diferencias");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    private String getEyesFileName() {
        return properties.getObject("eyes.ups.file.name") + "_" + ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    public boolean shutdown(long timeToWait) {
        isEnd = true;
        long startTime = Calendar.getInstance().getTimeInMillis();
        log.info("Deteniendo FileUpdaterTSLELProcess...");
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

}
