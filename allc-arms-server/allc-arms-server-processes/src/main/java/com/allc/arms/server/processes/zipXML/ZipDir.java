package com.allc.arms.server.processes.zipXML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class ZipDir extends SimpleFileVisitor<Path> {
	protected static Logger log = Logger.getLogger(ZipDir.class);
	private static ZipOutputStream zos;
	private Path sourceDir;

	public ZipDir(Path sourceDir) {
		this.sourceDir = sourceDir;
	}

	public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {

		try {
			Path targetFile = sourceDir.relativize(file);
			zos.putNextEntry(new ZipEntry(targetFile.toString()));
			byte[] bytes = Files.readAllBytes(file);
			zos.write(bytes, 0, bytes.length);
			zos.closeEntry();
		} catch (IOException ex) {
			log.error(ex);
		}
		return FileVisitResult.CONTINUE;
	}

	public static void zipearDir(String dirPath, String outDirPath, String zipName) throws IOException {
		Path sourceDir = Paths.get(dirPath);
                File folder = new File(outDirPath);
                
                if (!folder.exists()) 
                        folder.mkdirs();

                String zipFileName = outDirPath.concat(zipName);
                zos = new ZipOutputStream(new FileOutputStream(zipFileName));
                Files.walkFileTree(sourceDir, new ZipDir(sourceDir));
                zos.close();
		log.info("Directorio Zipeado Correctamente...");
	}

	public static void deleteDir(File folder) throws Exception {
                File[] files = folder.listFiles();
                if (files != null) {
                        for (File f : files) {
                                if (f.isDirectory()) {
                                        deleteDir(f);
                                } else {
                                        f.delete();
                                }
                        }
                }
                log.info("Archivos Eliminados del Directorio...");
	}
}
