package com.allc.comm.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Calendar;

import org.apache.log4j.Logger;

/**
 * @author gustavo
 *
 */
public class ConnSocketServer {

    static Logger logger = Logger.getLogger(ConnSocketServer.class);
    private Socket client;
    private DataOutputStream dos;
    private DataInputStream dis;
    private int quantityBytesLength;
    private String data;
    private long timeOutConnection;
    private long timeOutSleep;

    /**
     * Constructor que inicializa la clase
     *
     * @param cliente Socket que se utiliza para el cliente en particular
     * @param quantityBytesLength Cantidad de bytes usados para indicar la
     * longitud de la trama ( primeros X bytes )
     * @param timeOutConnection Tiempo usado como limite para esperar por los
     * datos en el socket
     * @param timeOutSleep Tiempo de respiro utilizado entre los loops de las
     * lecturas
     */
    public ConnSocketServer(Socket cliente, int quantityBytesLength, long timeOutConnection, long timeOutSleep) {
        try {
            setClient(cliente);
            setQuantityBytesLength(quantityBytesLength);
            setDos(new DataOutputStream(getClient().getOutputStream()));
            setDis(new DataInputStream(getClient().getInputStream()));
            setTimeOutConnection(timeOutConnection);
            setTimeOutSleep(timeOutSleep);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            setClient(null);
            setDis(null);
            setDos(null);
        }
    }
    
    public synchronized int bytesToInt(byte[] bytes) {
        return (bytes[0] << 8) & 0x0000ff00 | (bytes[1] << 0) & 0x000000ff;
    }
    
    public synchronized int leeLongitudDataHexaSocket() {
        int cantBytesAleer = getQuantityBytesLength();
        byte[] cbufHeader;
        int totalleido = 0;
        int totalleidos = 0;
        int totbytesaleer = 0;
        try {
            cbufHeader = new byte[cantBytesAleer];
            if (!timeOutSocket(getQuantityBytesLength())) {
                while ((getQuantityBytesLength() - totalleidos) > 0) {
                    totalleido = getDis().read(cbufHeader, totalleidos, getQuantityBytesLength() - totalleidos);
                    if (totalleido == -1) {
                        return 0;
                    }
                    totalleidos = totalleidos + totalleido;
                }
                totbytesaleer = bytesToInt(cbufHeader);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            totbytesaleer = -1;
        }
        return totbytesaleer;
    }

    /**
     * Lee la longitud de una trama
     *
     * @return la cantidad de bytes que tiene la trama. -1 si ocurrio un error o
     * si ocurrio timeout al leer la longitud de la trama
     */
    public int readLengthDataSocket() {
        int cantBytesAleer = getQuantityBytesLength();
        byte[] cbufHeader;
        int totbytesaleer = 0;
        try {
            cbufHeader = new byte[cantBytesAleer];
            if (!timeOutSocket(cantBytesAleer)) {
                getDis().read(cbufHeader, 0, cantBytesAleer);
                logger.debug("Cant a leer: " + new String(cbufHeader));
                totbytesaleer = Integer.parseInt(new String(cbufHeader));
            } else {
                totbytesaleer = -1;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            totbytesaleer = -1;
        }
        return totbytesaleer;
    }

    /**
     * Método encargado de verificar que hay disponible para leer del socket
     * quantityBytesToWait bytes.
     *
     * @param quantityBytesToWait
     * @return
     */
    public boolean timeOutSocket(int quantityBytesToWait) {
        boolean timeExpired = false;
        long startTime = Calendar.getInstance().getTimeInMillis();
        try {
            int available = getDis().available();
            while (available < quantityBytesToWait) {
                long dif = Calendar.getInstance().getTimeInMillis() - startTime;
                if (dif >= getTimeOutConnection()) {
                    if (available != 65536) {
                        timeExpired = true;
                    } else {
                        logger.info("available:" + available);
                    }
                    break;
                }
                Thread.sleep(getTimeOutSleep());
                available = getDis().available();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (timeExpired) {
            logger.error("timeOutSocket: Proceso despreciado por TimeOut. Tiempo Maximo: " + timeOutConnection + " Tiempo Transcurrido: "
                    + String.valueOf(Calendar.getInstance().getTimeInMillis() - startTime));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Funcion que lee una cantidad de bytes = quantityBytesToRead
     *
     * @param quantityBytesToRead
     * @return null si ocurrio un error al leer del socket
     */
    public String readDataSocket(int quantityBytesToRead) {
        String dato = null;
        try {
            byte[] bufbyte = new byte[quantityBytesToRead];
            int totalleido = 0;
            int totalleidos = 0;
            
            if (!timeOutSocket(quantityBytesToRead)) {
                while ((quantityBytesToRead - totalleidos) > 0) {
                    totalleido = getDis().read(bufbyte, totalleidos, quantityBytesToRead - totalleidos);
                    if (totalleido == -1) {
                        return null;
                    }
                    totalleidos = totalleidos + totalleido;
                }
                dato = new String(bufbyte);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            dato = null;
        }
        return dato;
    }

    /**
     * Funcion que lee una cantidad de bytes = quantityBytesToRead
     *
     * @param quantityBytesToRead
     * @return null si ocurrio un error al leer del socket
     */
    public byte[] readDataSocketToBytesArray(int quantityBytesToRead) {
        try {
            byte[] bufbyte = new byte[quantityBytesToRead];
            int totalleido = 0;
            int totalleidos = 0;
            
            if (!timeOutSocket(quantityBytesToRead)) {
                while ((quantityBytesToRead - totalleidos) > 0) {
                    totalleido = getDis().read(bufbyte, totalleidos, quantityBytesToRead - totalleidos);
                    if (totalleido == -1) {
                        return null;
                    }
                    totalleidos = totalleidos + totalleido;
                }
                return bufbyte;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Funcion Utilizada para escribir data a un Socket
     *
     * @param data Dato que se envia por socket
     * @return true si se logro escribir la data al socket, false si no se logro
     * escribir la data al socket
     */
    public boolean writeDataSocket(String data) {
        return writeDataSocketDOS(data);
    }

    /**
     * Funcion utilizada para escribir data a un socket
     *
     * @param data Dato que se envia por socket
     * @return true si se logro escribir la data al socket false si no se logro
     * escribir la data al socket
     */
    private boolean writeDataSocketDOS(String data) {
        try {
            getDos().writeBytes(data);
            getDos().flush();
            logger.debug("Data enviada al socket: " + data);
            //logger.info("client.getInetAddress() " + client.getInetAddress());
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Funcion Utilizada para escribir un arreglo de bytes a un Socket
     *
     * @param data Dato que se envia por socket
     * @return true si se logro escribir la data al socket false si no se logro
     * escribir la data al socket
     */
    public synchronized boolean writeByteArraySocket(byte[] data) {
        return writeByteArraySocketDOS(data);
    }

    /**
     * Funcion utilizada para escribir un arreglo de bytes a un socket UTILIZA
     * DataOutputStream
     *
     * @param data Dato que se envia por socket
     * @return true si se logro escribir la data al socket false si no se logro
     * escribir la data al socket
     */
    private synchronized boolean writeByteArraySocketDOS(byte[] data) {
        try {
            
            getDos().write(data);
            getDos().flush();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Método encargado de cerrar la conexión al socket.
     */
    public void closeConnectionServer() {
        try {
            closeDos();
            closeDis();
            closeClient();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    private void closeDos() {
        try {
            if (getDos() != null) {
                getDos().close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    private void closeDis() {
        try {
            if (getDis() != null) {
                getDis().close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    private void closeClient() {
        try {
            if (getClient() != null && !getClient().isClosed()) {
                getClient().close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @return the client
     */
    public Socket getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(Socket client) {
        this.client = client;
    }

    /**
     * @return the dos
     */
    public DataOutputStream getDos() {
        return dos;
    }

    /**
     * @param dos the dos to set
     */
    public void setDos(DataOutputStream dos) {
        this.dos = dos;
    }

    /**
     * @return the dis
     */
    public DataInputStream getDis() {
        return dis;
    }

    /**
     * @param dis the dis to set
     */
    public void setDis(DataInputStream dis) {
        this.dis = dis;
    }

    /**
     * @return the quantityBytesLength
     */
    public int getQuantityBytesLength() {
        return quantityBytesLength;
    }

    /**
     * @param quantityBytesLength the quantityBytesLength to set
     */
    public void setQuantityBytesLength(int quantityBytesLength) {
        this.quantityBytesLength = quantityBytesLength;
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @return the timeOutConnection
     */
    public long getTimeOutConnection() {
        return timeOutConnection;
    }

    /**
     * @param timeOutConnection the timeOutConnection to set
     */
    public void setTimeOutConnection(long timeOutConnection) {
        this.timeOutConnection = timeOutConnection;
    }

    /**
     * @return the timeOutSleep
     */
    public long getTimeOutSleep() {
        return timeOutSleep;
    }

    /**
     * @param timeOutSleep the timeOutSleep to set
     */
    public void setTimeOutSleep(long timeOutSleep) {
        this.timeOutSleep = timeOutSleep;
    }
    
}
