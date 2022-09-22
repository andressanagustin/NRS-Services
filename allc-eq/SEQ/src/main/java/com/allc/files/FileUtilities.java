package com.allc.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public final class FileUtilities
{
  private static final int DEFAULT_BUFFER_SIZE = 4096;

  private static String slashify(String _pathString, boolean _bDirectory)
  {
    String _path = _pathString;
    if (File.separatorChar != '/')
      _path = _path.replace(File.separatorChar, '/');
    if (!_path.startsWith("/"))
      _path = "/" + _path;
    if ((!_path.endsWith("/")) && (_bDirectory)) {
      _path = _path + "/";
    }

    return _path;
  }

  public static URL toURL(File _file)
    throws MalformedURLException
  {
    return new URL("file", "", slashify(_file.getAbsolutePath(), _file.isDirectory()));
  }

  public static void moveFile(File source, File destination)
    throws IOException
  {
    copyFile(source, destination);
    source.delete();
  }

  public static void copyFile(File source, File destination)
    throws IOException
  {
    copyFile(source, destination, true);
  }

  public static void copyFile(File source, File destination, boolean preserveFileDate)
    throws IOException
  {
    if (!source.exists()) {
      String message = "File " + source + " does not exist";
      throw new FileNotFoundException(message);
    }

    if ((destination.getParentFile() != null) && (!destination.getParentFile().exists())) {
      destination.getParentFile().mkdirs();
    }

    if ((destination.exists()) && (!destination.canWrite())) {
      String message = "Unable to open file " + destination + " for writing.";
      throw new IOException(message);
    }

    FileInputStream input = new FileInputStream(source);
    try {
      FileOutputStream output = new FileOutputStream(destination);
      try {
        copy(input, output);
      } finally {
      }
    }
    finally {
      closeQuietly(input);
    }

    if (source.length() != destination.length()) {
      String message = "Failed to copy full contents from " + source + " to " + destination;
      throw new IOException(message);
    }

    if (preserveFileDate)
    {
      destination.setLastModified(source.lastModified());
    }
  }

  public static int copy(InputStream input, OutputStream output)
    throws IOException
  {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    int count = 0;
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  public static void closeQuietly(OutputStream output)
  {
    if (output == null) {
      return;
    }
    try
    {
      output.close();
    }
    catch (IOException ioe)
    {
    }
  }

  public static void closeQuietly(InputStream input)
  {
    if (input == null) {
      return;
    }
    try
    {
      input.close();
    }
    catch (IOException ioe)
    {
    }
  }

  public static String getStringFromFile(String _fileName)
  {
    byte[] _b = null;
    try {
      File _file = new File(_fileName);

      BufferedInputStream _bis = new BufferedInputStream(new FileInputStream(_file));

      _b = new byte[_bis.available()];
      _bis.read(_b);

      _bis.close();
    }
    catch (Exception _e)
    {
      _e.printStackTrace();
      return "";
    }

    return new String(_b);
  }

  public static String relativizePaths(String filePath, String directory)
  {
    try
    {
      URI directoryURI = new File(directory).toURI();
      URI toRelativizeURI = new File(filePath).toURI();
      URI relativeURI = directoryURI.relativize(toRelativizeURI);
      return relativeURI.getPath(); } catch (Exception e) {
    }
    return filePath;
  }
}