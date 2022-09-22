/**
 * 
 */
package com.allc.files.helper.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.allc.files.helper.FilesHelper;

/**
 * @author GUSTAVOK
 * 
 */
public class ZipHelper {

	static Logger logger = Logger.getLogger(ZipHelper.class);
	private static final int BUFFER_SIZE = 8192;

	public static void zipperFiles() {

	}

	public static void unzipperFiles(String originDir, String destDir) {
		try {
			logger.debug("Descomprimiendo archivos en: " + destDir);
			// Create a ZipInputStream to read the zip file
			BufferedOutputStream dest = null;

			File folder = new File(originDir);
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					String filename = listOfFiles[i].getName();
					logger.debug("Archivo a descomprimir: " + filename);
					FileInputStream fis = new FileInputStream(originDir + filename);
					ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
					// Loop over all of the entries in the zip file
					int count;
					byte data[] = new byte[BUFFER_SIZE];
					ZipEntry entry;
					while ((entry = zis.getNextEntry()) != null) {
						if (!entry.isDirectory()) {
							String destFN = destDir + entry.getName();
							File destDirectory = new File(FilesHelper.removeFileName(FilesHelper
									.replaceFileSeparator(destFN)));
							logger.debug("Generando directorios para el path: " + destDirectory.getPath());
							destDirectory.mkdirs();
							// Write the file to the file system
							FileOutputStream fos1 = new FileOutputStream(destFN);
							dest = new BufferedOutputStream(fos1, BUFFER_SIZE);
							while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
								dest.write(data, 0, count);
							}
							dest.flush();
							dest.close();
							logger.debug("Se ha generado el archivo: " + destFN);
						}
					}
					zis.close();
					logger.debug("El archivo " + filename + " se descomprimió correctamente.");
				}
			}
		} catch (Exception e) {
			logger.error("Error al descomprimir archivos.", e);
		}
	}
}
