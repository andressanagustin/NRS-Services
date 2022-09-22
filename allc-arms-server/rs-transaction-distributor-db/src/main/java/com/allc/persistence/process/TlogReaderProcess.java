/**
 * 
 */
package com.allc.persistence.process;

import java.io.File;
import java.io.FileFilter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.allc.persistence.dao.TransactionDAO;
import com.allc.persistence.util.TDprop;
import com.allc.util.ConstantsUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * @author GUSTAVOK
 *
 */


public class TlogReaderProcess {
	private SimpleDateFormat dateFormat;
	private TransactionDAO transactionDAO =  new TransactionDAO();
	private File currentTlog;
	private File tlogFolder;
	private File processedTlogFolder;
	private File duplicatedTlogFolder;
	private File errorTlogFolder;
	private Iterator filesToProcess = null;
	private static TDprop properties;
	private static Logger log;
	private boolean isEnd = false;
	
	public TlogReaderProcess() {
    		log = Logger.getLogger(TlogReaderProcess.class);
    		properties = TDprop.getInstance();
    		
			if(null == properties)
				throw new NullPointerException("cannot load the properties file TDprop.xml");
			else{
				if(properties.getFolderInPath().equals("")){
					log.error("The parameter folder: \'reader.folder.in.path\' inside of TDprop.xml cannot be empty");
					System.exit(1);
				}
			}	
    	
			//this.tlogFolder = new File("C:/ALLC/tlog");
			this.tlogFolder = new File(properties.getFolderInPath());
			
			//this.processedTlogFolder = new File(this.tlogFolder,"processedTlog");
			this.processedTlogFolder = new File(this.tlogFolder,properties.getProcessedFolderName());
			
			if (!this.processedTlogFolder.exists()) {
				processedTlogFolder.mkdirs();
			}
			//this.errorTlogFolder = new File(this.tlogFolder,"errorTlogs");
			this.errorTlogFolder = new File(this.tlogFolder,properties.getErrorFolderName());
			if (!this.errorTlogFolder.exists()) {
				errorTlogFolder.mkdirs();
			}
			this.duplicatedTlogFolder = new File(this.tlogFolder,properties.getDuplicatedFolderName());
			if (!this.duplicatedTlogFolder.exists()) {
				duplicatedTlogFolder.mkdirs();
			}
	}
    public ArrayList<File> getTransactionalFiles() {
            int numberTransToProcess = Integer.parseInt(properties.getNumberTransactionsToProcess());
            ArrayList<File> filesProcess = new ArrayList<File>();
                    do {
                            File[] files = tlogFolder.listFiles(new FileFilter() {
                                    public boolean accept(File pathname) {
                                            return pathname.isFile()&& pathname.getName().endsWith(".xml") && !pathname.getName().toUpperCase().startsWith("TMP");
                                    }
                            });
                            if (files.length == 0) {
                                    try {
                                            Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                            // Do nothing.
                                    }
                            } else {
                                    Arrays.sort(files, new Comparator() {
                                            public int compare(Object obj1, Object obj2) {
                                                    String[] parts = ((File) obj1).getName().split("\\-");
                                                    String fecha1 =  parts[3];
                                                    String[] parts2 = ((File) obj2).getName().split("\\-");
                                                    String fecha2 =  parts2[3];
                                                    long fechaF1 = Long.parseLong(fecha1);
                                                    long fechaF2 = Long.parseLong(fecha2);
                                                    if (fechaF1 == fechaF2) {
                                                            return 0;
                                                    }
                                                    if (fechaF1 < fechaF2) {
                                                            return -1;
                                                    }
                                                    return 1;
                                            }
                                    });
                                    if (files.length >= numberTransToProcess) {
                                            for (int i = 0; i < numberTransToProcess; i++) {
                                                    filesProcess.add(files[i]);
                                            }
                                    } else {
                                            filesProcess = new ArrayList<File>(Arrays.asList(files));
                                    }
                            }
                    } while (filesProcess.isEmpty() && !isEnd);
            if(isEnd) {
                    log.info("La búsqueda de tlogs se ha interrumpido.");
                    return null;
            }
            return filesProcess;
    }
    
    public File getNextTransactionalFile() {
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				File[] files = tlogFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile()&& pathname.getName().endsWith(".xml") && !pathname.getName().toUpperCase().startsWith("TMP");
					}
				});
				if (files.length == 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// Do nothing.
					}
				} else {
					Arrays.sort(files, new Comparator() {
						public int compare(Object obj1, Object obj2) {
							String[] parts = ((File) obj1).getName().split("\\-");
							String fecha1 =  parts[3];
							String[] parts2 = ((File) obj2).getName().split("\\-");
							String fecha2 =  parts2[3];
							long fechaF1 = Long.parseLong(fecha1);
							long fechaF2 = Long.parseLong(fecha2);
							if (fechaF1 == fechaF2) {
								return 0;
							}
							if (fechaF1 < fechaF2) {
								return -1;
							}
							return 1;
						}
					});
					this.filesToProcess = Arrays.asList(files).iterator();
				}
			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()) && !isEnd);
		}
		if(isEnd) {
			log.info("La búsqueda de tlogs se ha interrumpido.");
			return null;
		}
		currentTlog = (File) this.filesToProcess.next(); 
		return currentTlog;
	}
    
	public String getRenamedTlog(String currentName) {
		String rawFileName = currentName.substring(0, currentName.indexOf('.'));
		return rawFileName + "-" + getSuffixDateFormat().format(new Date()) + ".xml";
	}
    
    private SimpleDateFormat getSuffixDateFormat() {
		if (dateFormat == null) {
			dateFormat = ConstantsUtil.Formatters.TLOG_SUFIX_DATE_TIME_FORMATTER;
		}
		return dateFormat;
	}

	public boolean moveToErrorTlogFolders(File currentTlog ) throws SQLException {
		int indexOf = currentTlog.getName().indexOf("-");
		String store = currentTlog.getName().substring(0, indexOf);
		String nomArch= this.getRenamedTlog(currentTlog.getName());

                if(transactionDAO.existeArchivoEnBd(currentTlog.getName(), store))
			transactionDAO.deleteArchFromDb(currentTlog.getName(), store);
		transactionDAO.insertaArchTlog(nomArch, store, "E");
		
		return currentTlog.renameTo(new File(errorTlogFolder, nomArch));
	}
	
	public boolean moveToProcessedTlogFolders(File currentTlog, String nomArch) throws SQLException {
		int indexOf = currentTlog.getName().indexOf("-");
		String store = currentTlog.getName().substring(0, indexOf);
		
		if(transactionDAO.existeArchivoEnBd(currentTlog.getName(), store))
			transactionDAO.deleteArchFromDb(currentTlog.getName(), store);
			
		transactionDAO.insertaArchTlog(nomArch, store, "P");
		
		return currentTlog.renameTo(new File(processedTlogFolder, nomArch));
	}
	
	public boolean moveToDuplicatedTlogFolders(File currentTlog) throws SQLException {
		int indexOf = currentTlog.getName().indexOf("-");
		String store = currentTlog.getName().substring(0, indexOf);
		String nomArch= this.getRenamedTlog(currentTlog.getName());

		if(transactionDAO.existeArchivoEnBd(currentTlog.getName(), store))
			transactionDAO.deleteArchFromDb(currentTlog.getName(), store);
			
		transactionDAO.insertaArchTlog(nomArch, store, "D");
		
		return currentTlog.renameTo(new File(duplicatedTlogFolder, nomArch));
	}
	
	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}
        
        public void moveToExtra(List<String> listTlog){
            for (String tlog : listTlog) {                        
                    File currentTlog = new File(processedTlogFolder, tlog);
                    if (currentTlog.exists())
                        currentTlog.renameTo(new File(properties.getFolderInPath(), currentTlog.getName()));
            }
        }
}
