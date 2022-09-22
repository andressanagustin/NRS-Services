/**
 * 
 */
package com.allc.files.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileInputStream4690;
import com.ibm.OS4690.FileOutputStream4690;

/**
 * @author GUSTAVOK
 * 
 */
public class FilesHelper {
	private static final int BUFFER_SIZE = 8192;

	public static String replaceFileSeparator(String path) {
		String newPath = path.replace("/", File.separator);
		return newPath.replace("\\", File.separator);
	}

	public static String removeFileName(String filePath) {
		int index = filePath.lastIndexOf(File.separator);
		return filePath.substring(0, index);
	}

	/**
	 * M�todo que retorna los directorios incluidos dentro del directorio recibido como par�metro.
	 * 
	 * @param directory
	 */
	public static File[] getSubDirectories(String directory) {
		File folder = new File(directory);
		// obtenemos los directorios
		File[] listOfDir = folder.listFiles((new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		}));
		return listOfDir;
	}

	public static String readFirstLine(String filename) throws Exception {
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(filename));
			line = br.readLine();
		} catch (Exception e) {
			throw e;
		} finally {
			br.close();
		}
		return line;
	}

	public static boolean copyFileAndRemoveSpecificLine(String originDir, String destinationDir, String originName, String destName, int pos) throws Exception{
		boolean retorno = false;
		try {
			File inFile = new File(originDir + File.separator + originName);
			File tempFile = new File(destinationDir);
			tempFile.mkdirs();
			BufferedReader br = new BufferedReader(new FileReader(inFile));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile.getAbsolutePath() + File.separator + destName));

			String line = null;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (i != pos) {
					pw.println(line);
					pw.flush();
					retorno = true;
				}
				i++;
			}
			pw.close();
			br.close();
		} catch (Exception e) {
			throw e;
		}
		return retorno;
	}
	public static void copyFile(String originDir, String destinationDir, String originName, String destName) throws Exception {
		BufferedInputStream bis = null;
		BufferedOutputStream dest = null;
		try {
			File destDirectory = new File(destinationDir);
			destDirectory.mkdirs();
			// Write the file to the file system
			FileOutputStream fos1 = new FileOutputStream(destinationDir + File.separator + destName);
			dest = new BufferedOutputStream(fos1, BUFFER_SIZE);
			int count;
			byte data[] = new byte[BUFFER_SIZE];
			FileInputStream fis = new FileInputStream(originDir + File.separator + originName);
			bis = new BufferedInputStream(fis);
			while ((count = bis.read(data, 0, BUFFER_SIZE)) != -1) {
				dest.write(data, 0, count);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if(bis!=null)
					bis.close();
				if(dest!=null) {
					dest.flush();
					dest.close();
				}
			} catch (Exception e) {
				throw e;
			}
		}
	}
	
	public static void copyFile4690(String originDir, String destinationDir, String originName, String destName) throws Exception {
		BufferedInputStream bis = null;
		BufferedOutputStream dest = null;
		try {
			File4690 destDirectory = new File4690(destinationDir);
			destDirectory.mkdirs();
			// Write the file to the file system
			FileOutputStream4690 fos1 = new FileOutputStream4690(destinationDir + File.separator + destName, true);
			dest = new BufferedOutputStream(fos1, BUFFER_SIZE);
			int count;
			byte data[] = new byte[BUFFER_SIZE];
			FileInputStream4690 fis = new FileInputStream4690(originDir + File.separator + originName);
			bis = new BufferedInputStream(fis);
			while ((count = bis.read(data, 0, BUFFER_SIZE)) != -1) {
				dest.write(data, 0, count);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if(bis!=null)
					bis.close();
				if(dest!=null) {
					dest.flush();
					dest.close();
				}
			} catch (Exception e) {
				throw e;
			}
		}
	}

	public static boolean removeDir(File dir) {
		File[] ficheros = dir.listFiles();
		if (ficheros != null)
			for (int x = 0; x < ficheros.length; x++) {
				if (ficheros[x].isDirectory()) {
					removeDir(ficheros[x]);
				}
				ficheros[x].delete();
			}
		if (dir.delete())
			return true;
		else
			return false;
	}
}
