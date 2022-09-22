package com.allc.arms.agent.processes.fileUpdate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.files.helper.FilesHelper;
import com.allc.properties.PropFile;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileFilter4690;

/**
 * Proceso encargado de actualizar los archivos que provienen del controlador. Es decir, que actualiza desde el disco C hacia el F.
 * 
 * @author gustavo
 *
 */
public class FileUpdaterUpProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(FileUpdaterUpProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected long timeSleep;
	protected File4690 outFolder;
	protected List originFolders;
	protected boolean isEnd = false;
	protected boolean finished = false;
	private String store;
	
	protected boolean init() {
		boolean result = false;
		try {
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			store = (new Integer(controllerStatusData.getStoreNumber())).toString();
			outFolder = new File4690(properties.getObject("fileUpdaterUp.out.folder.path"));
			outFolder.mkdir();
			originFolders = loadOriginFolders(properties.getObject("fileUpdaterUp.origin.folders"));
			timeSleep = Long.parseLong(properties.getObject("fileUpdaterUp.timeSleep").toString());
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	protected List loadOriginFolders(String folders){
		List folderList = new ArrayList();
		String [] foldersArray = folders.split("\\;");
		for(int i = 0; i < foldersArray.length; i++){
			folderList.add(foldersArray[i]);
		}
		return folderList;
	}

	public void run() {
		log.info("Iniciando File Updater Up Process...");
		init();
		while (!isEnd) {
			File4690 updateFile = null;
			try {
				Iterator itFolders = originFolders.iterator();
				while(itFolders.hasNext()){
					String[] reg = ((String) itFolders.next()).split("\\,");
					String folderName = reg[0];
					String[] filters = null;
					if(reg.length > 1){
						filters = new String[reg.length-1];
						for(int i = 1; i < reg.length; i++)
							filters[i-1] = reg[i];
					}
					File4690 originFolder = new File4690("C:/"+folderName);
					File4690[] list = getFilesList(originFolder, filters);
					String outDir = outFolder.getAbsolutePath() + File.separator + folderName;
					List oldFiles = new ArrayList(Arrays.asList((new File4690(outDir)).listFiles()));
					int size = list.length;
					for(int i = 0; i < size; i++){
						updateFile = list[i];
						oldFiles = removeFileOfList(updateFile.getName(), oldFiles);
						File4690 outFile = new File4690(outDir+File.separator+updateFile.getName());
						boolean needUpdate = !comparator(outFile, updateFile);
						if(needUpdate){
							try {
								Files.creaEscribeDataArchivo4690(getEyesFileName(), "FILE_UPD_U_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Actualizando archivo: "+ updateFile.getName()+".\n", true);
								outFile.delete();
								FilesHelper.copyFile4690(originFolder.getAbsolutePath(), outFolder.getAbsolutePath() + File.separator + folderName , updateFile.getName(), outFile.getName());
								Files.creaEscribeDataArchivo4690(getEyesFileName(), "FILE_UPD_U_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Finalizó el procesamiento del archivo: "+ updateFile.getName()+".\n", true);
							} catch (Exception e){
								log.error(e.getMessage(), e);
								Files.creaEscribeDataArchivo4690(getEyesFileName(), "FILE_UPD_U_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo actualizar el archivo: "+ updateFile.getName()+".\n", true);
							}
						}
					}
					deleteFiles(oldFiles);
				}
				try {
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			} catch (Exception e) {
				try {
					Files.creaEscribeDataArchivo4690(getEyesFileName(), "FILE_UPD_U_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo: "+ updateFile.getName()+".\n", true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
				log.error(e.getMessage(), e);
			}
		}
		finished = true;
	}
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	private File4690[] getFilesList(File4690 originFolder, final String[] filters) {
//		log.info("Buscando archivos para actualizar...");
		File4690[] files = originFolder.listFiles(new FileFilter4690() {
			public boolean accept(File4690 pathname) {
				boolean filtersOK = false;
				if(filters != null && filters.length > 0){
					for(int i = 0; i < filters.length; i++){
						if(pathname.getName().matches(filters[i])){
							filtersOK = true;
							break;
						}	
					}
				}
				return filtersOK && pathname.isFile();
			}
		});
		return files;
	}
	
	private List removeFileOfList(String fileName, List files){
		int size = files.size();
		for(int i = 0; i < size; i++){
			if(fileName.equalsIgnoreCase(((File4690) files.get(i)).getName())){
				files.remove(i);
				size = size - 1;
			}
		}
		return files;
	}
	
	private boolean deleteFiles(List files){
		Iterator itFiles = files.iterator();
		while(itFiles.hasNext()){
			File4690 file = (File4690) itFiles.next();
			try {
//				log.info("Borrando: "+file);
				return file.delete();
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	private boolean comparator(File4690 originFile, File4690 updateFile){
//		log.info("OriginN: "+originFile.getName());
//		log.info("UpdateN: "+updateFile.getName());
//		log.info("OriginP: "+originFile.getAbsolutePath());
//		log.info("UpdateP: "+updateFile.getAbsolutePath());
//		log.info("OriginL: "+originFile.length());
//		log.info("UpdateL: "+updateFile.length());
//		log.info("OriginLM: "+originFile.lastModified());
//		log.info("UpdateLM: "+updateFile.lastModified());
		if(originFile.length() != updateFile.length() || originFile.lastModified() < updateFile.lastModified())
			return false;
		return true;
	}
	
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo FileUpdaterUpProcess...");
		while (!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					return false;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		return true;
	}


}
