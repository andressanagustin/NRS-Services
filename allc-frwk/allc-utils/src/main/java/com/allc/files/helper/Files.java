package com.allc.files.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileInputStream4690;
import com.ibm.OS4690.FileOutputStream4690;
import com.ibm.OS4690.RandomAccessFile4690;

public class Files {

    static Logger log = Logger.getLogger(Files.class);

    public static void creaArchivo(String nombreArchivo) {
        try {
            File f = new File(nombreArchivo);
            f.createNewFile();
        } catch (Exception ioe) {
            log.error(ioe);
        }
    }

    /**
     *
     * @param nombreArchivo nombreArchivo
     * @param data data
     * @param append true adiciona, false = como si creara el archivo y guardara
     * la data
     */
    public static boolean creaEscribeDataArchivo(String nombreArchivo, String data, boolean append) {
        try {

            PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(nombreArchivo, append)));
            fileaPos.write(data, 0, data.length());
            fileaPos.close();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     *
     * @param nombreArchivo nombreArchivo
     * @param data data
     * @param append true adiciona, false = como si creara el archivo y guardara
     * la data
     */
    public static boolean creaEscribeDataArchivo4690(String nombreArchivo, String data, boolean append) {
        FileOutputStream4690 fos = null;
        try {
            File4690 file = new File4690(nombreArchivo);
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream4690(nombreArchivo, append);
            fos.write(data.getBytes(), 0, data.length());
            fos.close();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     *
     * @param nombreArchivo nombreArchivo
     * @param data data
     * @param position posicion desde donde se va a escribir
     */    
    public static boolean creaEscribeDataArchivoByPos(String nombreArchivo, String data, int position) {
        RandomAccessFile file = null;
        try {
            //log.info("creaEscribeDataArchivo4690ByPos, FILE: " + nombreArchivo + ", Data: " + data + ", Position: "+ position);
            file = new RandomAccessFile(nombreArchivo, "rw");

            // Nos situamos en el byte 'position' del fichero.
            file.seek(position);

            // remplazamos el registro
            file.write(data.getBytes(), 0, data.length());

            file.close();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     *
     * @param nombreArchivo nombreArchivo
     * @param data data
     * @param position posicion desde donde se va a escribir
     */
    public static boolean creaEscribeDataArchivo4690ByPos(String nombreArchivo, String data, int position) {
        RandomAccessFile4690 file = null;
        try {
            //log.info("creaEscribeDataArchivo4690ByPos, FILE: " + nombreArchivo + ", Data: " + data + ", Position: "+ position);
            file = new RandomAccessFile4690(nombreArchivo, "rw");

            // Nos situamos en el byte 'position' del fichero.
            file.seek(position);

            // remplazamos el registro
            file.write(data.getBytes(), 0, data.length());

            file.close();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     *
     * @param nombreArchivo nombreArchivo
     * @param position posicion desde donde se va a escribir
     * @param length cantidad de bytes que se van a escribir
     */
    public static boolean eliminaRegistroArchivo4690ByPos(String nombreArchivo, int position, int length) {
        RandomAccessFile4690 file = null;
        try {
            log.info("eliminaRegistroArchivo4690ByPos, FILE: " + nombreArchivo + ", Position: " + position + ", Length: " + length);
            file = new RandomAccessFile4690(nombreArchivo, "rw");

            // Nos situamos en el byte 'position' del fichero.
            file.seek(position);

            byte[] arrayBytes = new byte[length];

            /*for(int i=0; i < arrayBytes.length ;i++) {
				arrayBytes[i] = new byte; 
			}*/
            // remplazamos el registro vacio
            file.write(arrayBytes, 0, length);

            file.close();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public static void adicionaDataArchivo(String nombreArchivo, String data) {
        try {

            PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(nombreArchivo, true)));
            fileaPos.write(data, 0, data.length());
            fileaPos.close();
            // log.info("Archivo: " + nombreArchivo + " Se escribio data: " +
            // data );

        } catch (Exception e1) {
            log.error("No se puede escribir en el archivo: " + nombreArchivo + " " + e1);
        }
    }

    public static boolean fileExists(String nombreArchivo) {
        File fichero = new File(nombreArchivo);
        return fichero.exists();
    }

    public static boolean fileExists4690(String nombreArchivo) {
        File4690 fichero = new File4690(nombreArchivo);
        return fichero.exists();
    }

    public static String readLineOfFile(String nombreArchivo) {

        String linea = null;
        try {

            FileReader fr = new FileReader(nombreArchivo);
            BufferedReader br = new BufferedReader(fr);

            linea = br.readLine();

            br.close();
            fr.close();

        } catch (Exception e1) {
            log.error("readLineOfFile: cannot open file: " + nombreArchivo + " " + e1);
        }
        return linea;
    }

    public static String readSpecifictLineOfFile(String fileName, long row) {

        BufferedReader br = null;
        String linea = "";
        long cont = 0;
        try {

            br = new BufferedReader(new FileReader(fileName));

            while (null != (linea = br.readLine())) {
                cont++;
                // log.info("leyo " + cont);
                if (cont == row) {
                    break;
                }
            }
            // log.info("linea: "+linea);
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);

        } finally {
            try {
                br.close();
            } catch (IOException e) {
            }
        }
        return linea;
    }

    public static String readSpecifictLineOfFile4690(String fileName, long row) {

        BufferedReader br = null;
        String linea = "";
        long cont = 0;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream4690(fileName)));
            while (null != (linea = br.readLine())) {
                cont++;
                if (cont == row) {
                    break;
                }
            }
            // log.info("linea: "+linea);
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
            linea = null;
        } finally {
            try {
                br.close();
            } catch (IOException e) {
            }
        }
        return linea;
    }

    

    /**
     * Lee la data de un archivo comenzando en la posicion recibida como
     * parametro.
     *
     * @param randomAccessFile
     * @param bytesPosition Posicion en bytes para comenzar la lectura
     * @return
     */
    public static String readDataByBytesPositionOfFile4690(RandomAccessFile4690 randomAccessFile, long bytesPosition, long countToRead) {
        String dataStr = "";
        try {
            if (randomAccessFile.length() <= bytesPosition) {
                return null;
            }
            randomAccessFile.seek(bytesPosition);
            byte[] data = new byte[(int) countToRead];
            randomAccessFile.read(data);
            dataStr = new String(data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            dataStr = null;
        }
        return dataStr;
    }

    
    /**
     * Lee una linea determinada comenzando en la posicion recibida como
     * parametro.
     *
     * @param randomAccessFile
     * @param bytesPosition Posicion en bytes para comenzar la lectura
     * @return
     */
    public static String readLineByBytesPositionOfFile4690(RandomAccessFile4690 randomAccessFile, long bytesPosition) {
        String linea = "";
        try {
            if (randomAccessFile.length() <= bytesPosition) {
                return null;
            }
            randomAccessFile.seek(bytesPosition);
            linea = randomAccessFile.readLine();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            linea = null;
        }
        return linea;
    }

    /**
     * Lee una linea determinada comenzando en la posicion recibida como
     * parametro. Metodo especifico para leer archivos de 4690, considerando
     * como fin de linea solo cuando hay \r\n.
     *
     * @param randomAccessFile
     * @param bytesPosition Posicion en bytes para comenzar la lectura
     * @return
     */
    public static String readLineByBytesPositionOfFileWriteIn4690(RandomAccessFile4690 randomAccessFile, long bytesPosition) {
        String linea = "";
        try {
            if (randomAccessFile.length() <= bytesPosition) {
                return null;
            }
            randomAccessFile.seek(bytesPosition);
            linea = Files.readLine(randomAccessFile);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            linea = null;
        }
        return linea;
    }

    public static String readLine(RandomAccessFile4690 randomAccessFile) throws IOException {
        StringBuffer input = new StringBuffer();
        int c = -1;
        boolean eol = false;

        while (!eol) {
            if (randomAccessFile.length() <= randomAccessFile.getFilePointer()) {
                return null;
            }
            switch (c = randomAccessFile.read()) {
                case -1:
                    eol = true;
                    break;
                case '\r':
                    long cur = randomAccessFile.getFilePointer();
                    int a = randomAccessFile.read();
                    if (a != '\n') {
                        randomAccessFile.seek(cur);
                    } else {
                        eol = true;
                    }
                    break;
                default:
                    input.append((char) c);
                    break;
            }
        }

        if ((c == -1) && (input.length() == 0)) {
            return null;
        }
        return input.toString();
    }

    public static String readLineByBytesPositionOfFile(RandomAccessFile randomAccessFile, long bytesPosition) {
        String linea = "";
        try {
            if (randomAccessFile.length() <= bytesPosition) {
                return null;
            }
            randomAccessFile.seek(bytesPosition);
            linea = randomAccessFile.readLine();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            linea = null;
        }
        return linea;
    }

    public static boolean leeDataArchivo(String rutaNombreFile, ArrayList array) {
        FileReader fr = null;
        BufferedReader br = null;
        String linea;
        boolean b_exito = false;
        try {
            fr = new FileReader(rutaNombreFile);
            br = new BufferedReader(fr);

            while ((linea = br.readLine()) != null) {
                if (linea.trim() != "") {
                    array.add(linea);
                }
            }
            b_exito = true;
        } catch (Exception e) {
            log.error("leeDataArchivo: " + e);
        } finally {
            try {
                if (null != br) {
                    br.close();
                }
                if (null != fr) {
                    fr.close();
                }
            } catch (Exception e2) {
                log.error("leeDataArchivo: " + e2);
            }
        }
        return b_exito;
    }

    public static void leeDataArchivo(String rutaNombreFile, String dato) {
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;

        try {
            // Apertura del fichero y creacion de BufferedReader para poder
            // hacer una lectura comoda (disponer del metodo readLine()).
            archivo = new File(rutaNombreFile);
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);

            // Lectura del fichero
            String linea;

            while ((linea = br.readLine()) != null) {
                log.info(linea);
                dato = dato + linea;
            }
        } catch (Exception e) {
            log.error("leeDataArchivo: " + e);
            dato = null;
        } finally {

            try {
                if (null != br) {
                    br.close();
                }
                if (null != fr) {
                    fr.close();
                }
            } catch (Exception e2) {
                log.error("leeDataArchivo: " + e2);
            }
        }
    }

    public static String leeDataArchivo(String rutaNombreFile) {

        String dato = "";

        DataInputStream dis = null;
        File file;
        try {

            file = new File(rutaNombreFile);
            // log.info("CHARSET: " + informacion[19] );
            // isr = new InputStreamReader(new FileInputStream(file),"UTF-8");
            // osw = new OutputStreamWriter(new FileOutputStream(file),
            // JAVA_ENCODING);
            dis = new DataInputStream(new FileInputStream(file));
            // InputStream inStream = clb.getAsciiStream();//
            // .getBinaryStream(); OutputStreamWriter

            // InputStreamReader isr = new InputStreamReader(inStream);
            /*
			 * int size = (int)isr.length(); char[] buffer = new char[size];
             */
            byte[] b = new byte[1];

            String a = "";
            while ((dis.read(b)) != -1) {
                a = a + (new String(b));
            }

            /*
			 * // Apertura del fichero y creacion de BufferedReader para poder
			 * // hacer una lectura comoda (disponer del metodo readLine()).
			 * archivo = new File (rutaNombreFile); fr = new FileReader
			 * (archivo); br = new BufferedReader(fr);
			 * 
			 * // Lectura del fichero int linea;
			 * 
			 * while((linea=br.read())!=null){ System.out.println(linea); dato =
			 * dato + linea; }
             */
        } catch (Exception e) {
            log.error("CreaEscribeDataArchivoError: " + e);
            dato = null;
        } finally {

            try {
                if (null != dis) {
                    dis.close();
                }
            } catch (Exception e2) {
                log.error("CreaEscribeDataArchivoError: " + e2);
            }
        }
        return dato;
    }

    public static boolean deleteFile(String nombreFile) {
        boolean b_exito = false;
        try {
            if (fileExists(nombreFile)) {
                if (deleteArchivo(nombreFile)) {
                    b_exito = true;
                }
            }
        } catch (Exception e) {
            log.error("borraArchivo: " + e);
        }
        return b_exito;
    }

    public static boolean deleteFile4690(String nombreFile) {
        boolean b_exito = false;
        try {
            if (fileExists4690(nombreFile)) {
                if (deleteArchivo4690(nombreFile)) {
                    b_exito = true;
                }
            }
        } catch (Exception e) {
            log.error("borraArchivo: " + e);
        }
        return b_exito;
    }

    public static boolean deleteArchivo(String nombreFile) {
        boolean b_exito = false;
        try {
            File fichero = new File(nombreFile);
            if (fichero.delete()) {
                b_exito = true;
            }
        } catch (Exception e) {
            log.error("deleteArchivo: " + e);
        }
        return b_exito;
    }

    public static boolean deleteArchivo4690(String nombreFile) {
        boolean b_exito = false;
        try {
            File4690 fichero = new File4690(nombreFile);
            if (fichero.delete()) {
                b_exito = true;
            }
        } catch (Exception e) {
            log.error("deleteArchivo: " + e);
        }
        return b_exito;
    }

    /**
     * crea una ruta de directorios
     *
     * @param ruta
     * @return true: si crea la ruta de directorios o el directorio ya existe
     * false: si no puede crearla.
     */
    public static boolean creaDirectorio(String ruta) {
        boolean b_exito = false;
        try {
            File folder = new File(ruta);
            if (folder.exists()) {
                b_exito = true;
            } else {
                folder.mkdirs();
                b_exito = true;
            }
        } catch (SecurityException e) {
            log.error("creaDirectorio: Verifique los permisos " + e);
        } catch (Exception e) {
            log.error("creaDirectorio: " + e);
        }
        return b_exito;
    }

    /**
     * obtiene el separador de archivos
     *
     * @return
     */
    public static String fileSeparator() {
        return File.separator;
    }

    /**
     * Se usa para obtener un separador de rutas pejem en windows seria ";"
     * c:/xxx/yyy;d:/abc/ddd
     *
     * @return
     */
    public static String pathSeparator() {
        return File.pathSeparator;
    }

    public static String leerArchivo(String nombreArchivo) {
        StringBuffer respuesta = new StringBuffer();
        FileInputStream fstream = null;
        DataInputStream entrada = null;
        BufferedReader buffer = null;
        try {
            fstream = new FileInputStream(nombreArchivo);
            entrada = new DataInputStream(fstream);
            buffer = new BufferedReader(new InputStreamReader(entrada));
            String strLinea;
            while ((strLinea = buffer.readLine()) != null) {
                respuesta.append(strLinea);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            respuesta = new StringBuffer();
        } finally {
            try {
                entrada.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                respuesta = new StringBuffer();
            }
        }

        return respuesta.toString();
    }

    public static void zippear(String fechaCont, String fechaCierre, File filesDir, String zipDir, String zipname) {
        int BUFFER_SIZE = 1024;
        String mesDia = null;
        try {
            String zipfilename = zipDir + zipname + ".zip";
            String[] files = filesDir.list();
            // Reference to the file we will be adding to the zipfile
            BufferedInputStream origin = null;
            // Reference to our zip file
            FileOutputStream dest = new FileOutputStream(zipfilename);
            // Wrap our destination zipfile with a ZipOutputStream
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            // Create a byte[] buffer that we will read data

            // from the source
            // files into and then transfer it to the zip file
            byte[] data = new byte[BUFFER_SIZE];
            Iterator itFechas = obtenerFechasIntermedias(fechaCont, fechaCierre).iterator();
            while (itFechas.hasNext()) {
                String fecha = (String) itFechas.next();
                mesDia = formatMonth(fecha.substring(2, 4)) + fecha.substring(4, 6);
                log.info("Buscando journals que comiencen con: " + "J" + mesDia);
                // Iterate over all of the files in our list
                for (int i = 0; i < files.length; i++) {
                    // Get a BufferedInputStream that we can use to read the
                    // source file
                    String filename = (String) files[i].toUpperCase();
                    if (filename.startsWith("J" + mesDia) || filename.equalsIgnoreCase("TSRPOS")) {
                        log.info("Adding: " + filename);
                        FileInputStream fi = new FileInputStream(filesDir + "\\" + filename);
                        origin = new BufferedInputStream(fi, BUFFER_SIZE);
                        // Setup the entry in the zip file
                        ZipEntry entry = new ZipEntry(zipname + "\\" + filename);
                        out.putNextEntry(entry);
                        // Read data from the source file and write it out to
                        // the zip file
                        int count;
                        while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                            out.write(data, 0, count);
                        }
                        // Close the source file
                        origin.close();
                    }
                }
            }
            // Close the zip file
            out.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void zippear4690(String fechaCont, String fechaCierre, File4690 filesDir, String zipDir,
            String zipname) {
        int BUFFER_SIZE = 1024;
        String mesDia = null;
        try {
            String zipfilename = zipDir + zipname + ".zip";
            String[] files = filesDir.list();
            // Reference to the file we will be adding to the zipfile
            BufferedInputStream origin = null;
            File4690 file = new File4690(zipfilename);
            if (!file.exists()) {
                file.createNewFile();
            }
            // Reference to our zip file
            FileOutputStream4690 dest = new FileOutputStream4690(zipfilename);
            // Wrap our destination zipfile with a ZipOutputStream
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            // Create a byte[] buffer that we will read data

            // from the source
            // files into and then transfer it to the zip file
            byte[] data = new byte[BUFFER_SIZE];
            Iterator itFechas = obtenerFechasIntermedias(fechaCont, fechaCierre).iterator();
            boolean tsrposAdded = false;
            while (itFechas.hasNext()) {
                String fecha = (String) itFechas.next();
                mesDia = formatMonth(fecha.substring(2, 4)) + fecha.substring(4, 6);
                log.info("Buscando journals que comiencen con: " + "J" + mesDia);
                // Iterate over all of the files in our list
                for (int i = 0; i < files.length; i++) {
                    // Get a BufferedInputStream that we can use to read the
                    // source file
                    String filename = (String) files[i].toUpperCase();
                    if (filename.startsWith("J" + mesDia)) {
                        log.info("Adding: " + filename);
                        FileInputStream4690 fi = new FileInputStream4690(filesDir + "\\" + filename);
                        origin = new BufferedInputStream(fi, BUFFER_SIZE);
                        // Setup the entry in the zip file
                        ZipEntry entry = new ZipEntry(zipname + "\\" + filename);
                        out.putNextEntry(entry);
                        // Read data from the source file and write it out to
                        // the zip file
                        int count;
                        while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                            out.write(data, 0, count);
                        }
                        // Close the source file
                        origin.close();
                    } else if (filename.equalsIgnoreCase("TSRPOS") && !tsrposAdded) {
                        log.info("Adding: " + filename);
                        FileInputStream4690 fi = new FileInputStream4690(filesDir + "\\" + filename);
                        origin = new BufferedInputStream(fi, BUFFER_SIZE);
                        // Setup the entry in the zip file
                        ZipEntry entry = new ZipEntry(zipname + "\\" + filename);
                        out.putNextEntry(entry);
                        // Read data from the source file and write it out to
                        // the zip file
                        int count;
                        while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                            out.write(data, 0, count);
                        }
                        // Close the source file
                        origin.close();
                        tsrposAdded = true;
                    }
                }
            }
            // Close the zip file
            out.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void zippear4690(String zipDir, String fileName, String zipname) {
        int BUFFER_SIZE = 1024;
        BufferedInputStream origin = null;
        ZipOutputStream out = null;
        FileOutputStream4690 dest = null;
        FileInputStream4690 fi = null;
        try {
            String zipfilename = zipDir + File4690.separator + zipname + ".zip";

            // Reference to the file we will be adding to the zipfile
            File4690 file = new File4690(zipfilename);
            if (!file.exists()) {
                file.createNewFile();
            }
            // Reference to our zip file
            dest = new FileOutputStream4690(zipfilename);
            // Wrap our destination zipfile with a ZipOutputStream
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            // Create a byte[] buffer that we will read data
            // from the source
            // files into and then transfer it to the zip file
            byte[] data = new byte[BUFFER_SIZE];

            // Get a BufferedInputStream that we can use to read the
            // source file
            log.info("Adding: " + fileName);
            log.info("Adding With Dir: " + zipDir + File4690.separator + fileName);
            fi = new FileInputStream4690(zipDir + File4690.separator + fileName);
            origin = new BufferedInputStream(fi, BUFFER_SIZE);
            // Setup the entry in the zip file
            ZipEntry entry = new ZipEntry(fileName);
            out.putNextEntry(entry);
            // Read data from the source file and write it out to the zip file
            int count;
            while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                out.write(data, 0, count);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                origin.close();
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
            try {
                out.close();
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
            try {
                dest.close();
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
            try {
                fi.close();
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    public static boolean copyFile4690(File4690 fileToCopy, File4690 newFile) {
        int BUFFER_SIZE = 1024;
        BufferedInputStream origin = null;
        FileOutputStream4690 fos = null;
        FileInputStream4690 fi = null;

        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            fi = new FileInputStream4690(fileToCopy);
            origin = new BufferedInputStream(fi, BUFFER_SIZE);
            fos = new FileOutputStream4690(newFile);

            byte[] data = new byte[BUFFER_SIZE];
            int count;
            while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                fos.write(data, 0, count);
            }
            return true;
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
            return false;
        } finally {
            try {
                origin.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            try {
                fi.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            try {
                fos.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @param zipDir
     * @param journalsDir
     * @param startWith
     * @param zipname
     */
    public static void zippearJournals4690(String zipDir, String journalsDir, String startWith, String zipname) {
        int BUFFER_SIZE = 1024;
        FileOutputStream4690 dest = null;
        try {
            String zipfilename = zipDir + File4690.separator + zipname + ".zip";

            // Reference to the file we will be adding to the zipfile
            BufferedInputStream origin;
            File4690 file = new File4690(zipfilename);
            if (!file.exists()) {
                file.createNewFile();
            }
            // Reference to our zip file
            dest = new FileOutputStream4690(zipfilename);
            // Create a byte[] buffer that we will read data
            // from the source
            // files into and then transfer it to the zip file
            byte[] data = new byte[BUFFER_SIZE];
            File4690 folder = new File4690(journalsDir);
            File4690[] listOfDir = folder.listFiles();
            for (File4690 listOfDir1 : listOfDir) {
                String journalName = listOfDir1.getName();
                if (journalName.toUpperCase().startsWith(startWith)) {
                    log.info("Adding: " + journalName);
                    try {
                        FileInputStream4690 fi = new FileInputStream4690(journalsDir + File4690.separator + journalName);
                        origin = new BufferedInputStream(fi, BUFFER_SIZE);
                        // Setup the entry in the zip file
                        ZipEntry entry = new ZipEntry(zipname + File4690.separator + journalName);
                        try  {
                             ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                            out.putNextEntry(entry);
                            // Read data from the source file and write it out to
                            // the zip file
                            int count;
                            while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                                out.write(data, 0, count);
                            }
                        }catch (Exception e){
                            
                        }
                        origin.close();
                    }
                    catch(Exception e)
                    {
                        
                    }
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                dest.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    private static List<String> fileList;

    public static void zippearDirectorio(String dir, String destino) throws IOException, IllegalArgumentException {
        fileList = new ArrayList<String>();
        generateFileList(dir, new File(dir));
        byte[] buffer = new byte[1024];
        String source = new File(dir).getName();
        FileOutputStream fos;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(destino + ".zip");
            zos = new ZipOutputStream(fos);

            log.info("Output to Zip : " + destino + ".zip");
            FileInputStream in = null;

            for (String file : fileList) {
                log.info("File Added : " + file);
                ZipEntry ze = new ZipEntry(source + File.separator + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(dir + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }

            zos.closeEntry();
            log.info("Folder successfully compressed");

        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);

        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static void generateFileList(String source, File node) {
        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(source, node.toString()));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(source, new File(node, filename));
            }
        }
    }

    private static String generateZipEntry(String source, String file) {
        return file.substring(source.length() + 1, file.length());
    }

    public static void zippear(String zipDir, String fileName, String zipname) {
        int BUFFER_SIZE = 1024;
        try {
            String zipfilename = zipDir + zipname + ".zip";

            // Reference to the file we will be adding to the zipfile
            BufferedInputStream origin = null;
            File file = new File(zipfilename);
            if (!file.exists()) {
                file.createNewFile();
            }
            // Reference to our zip file
            FileOutputStream dest = new FileOutputStream(zipfilename);
            // Wrap our destination zipfile with a ZipOutputStream
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            // Create a byte[] buffer that we will read data
            // from the source
            // files into and then transfer it to the zip file
            byte[] data = new byte[BUFFER_SIZE];

            // Get a BufferedInputStream that we can use to read the
            // source file
            log.info("Adding: " + fileName);
            FileInputStream fi = new FileInputStream(zipDir + "\\" + fileName);
            origin = new BufferedInputStream(fi, BUFFER_SIZE);
            // Setup the entry in the zip file
            ZipEntry entry = new ZipEntry(fileName);
            out.putNextEntry(entry);
            // Read data from the source file and write it out to the zip file
            int count;
            while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                out.write(data, 0, count);
            }
            // Close the source file
            origin.close();

            // Close the zip file
            out.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static List obtenerFechasIntermedias(String fechaIni, String fechaFin) {
        int ano1 = (new Integer(fechaIni.substring(0, 2))).intValue();
        int ano2 = (new Integer(fechaFin.substring(0, 2))).intValue();
        int mes1 = (new Integer(fechaIni.substring(2, 4))).intValue();
        int mes2 = (new Integer(fechaFin.substring(2, 4))).intValue();
        int dia1 = (new Integer(fechaIni.substring(4, 6))).intValue();
        int dia2 = (new Integer(fechaFin.substring(4, 6)).intValue());
        List fechas = new ArrayList();
        int diaMax = 31;
        int mesMax = 12;
        fechas.add(fechaIni);
        while (ano1 <= ano2) {
            while (mes1 <= mesMax && mes1 <= mes2) {
                while (dia1 <= diaMax && dia1 <= dia2) {
                    String dia = (new Integer(dia1).toString());
                    String mes = (new Integer(mes1).toString());
                    String ano = (new Integer(ano1).toString());
                    fechas.add((dia.length() < 2 ? "0" + dia : dia) + (mes.length() < 2 ? "0" + mes : mes)
                            + (ano.length() < 2 ? "0" + ano : ano));
                    dia1++;
                }
                mes1++;
            }
            ano1++;
        }
        return fechas;
    }

    public static String formatMonth(String month) {
        try {
            new Integer(month);
        } catch (NumberFormatException e) {
        }
        if ("10".equalsIgnoreCase(month)) {
            return "A";
        } else if ("11".equalsIgnoreCase(month)) {
            return "B";
        } else if ("12".equalsIgnoreCase(month)) {
            return "C";
        }
        return month.substring(1, 2);
    }
}
