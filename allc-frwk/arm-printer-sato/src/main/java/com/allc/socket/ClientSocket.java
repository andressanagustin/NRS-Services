/**
 * 
 */
package com.allc.socket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.apache.log4j.Logger;

/**
 * @author gustavo
 *
 */
public class ClientSocket {

		private Logger logger = Logger.getLogger(ClientSocket.class);
		private Socket socket;
		private DataOutputStream dos;
		private BufferedReader in;
		private int length;
		private int timeout;
		private int timeoutSocket;
		private int timeoutSleep;

		public ClientSocket(Socket socket, BufferedReader in, DataOutputStream dos, int timeoutSocket, int length,
				int timeout, int timeoutSleep) {
			this.socket = socket;
			this.in = in;
			this.dos = dos;
			this.length = length;
			this.timeout = timeout;
			this.timeoutSocket = timeoutSocket;
			this.timeoutSleep = timeoutSleep;
		}

		/**
		 * Este m�todo env�a un mensaje al servidor configurado.
		 * 
		 * @param message
		 *            Mensaje a enviar al servidor
		 * 
		 * @return true si se envi� correctamente el mensaje, false en cualquier
		 *         otro caso
		 * @throws Exception
		 */
		public boolean sendMessage(String message) throws Exception {
			try {
				logger.info("Trama a enviar: " + message);
				logger.info("IP: " + socket.getInetAddress().getHostAddress() + " Puerto: " + socket.getPort()
						+ " Timeout: " + timeoutSocket);
				dos.writeBytes(message);
				return true;
			} catch (SocketTimeoutException e) {
				logger.error("Se agot� el tiempo de espera para obtener una respuesta del servidor.  (IP: "
						+ socket.getInetAddress().getHostAddress() + ", Puerto: " + socket.getPort() + ").", e);
				throw e;
			} catch (UnknownHostException e) {
				logger.error("No se pudo conectar al servidor. (IP: " + socket.getInetAddress().getHostAddress()
						+ ", Puerto: " + socket.getPort() + ").", e);
				throw e;
			} catch (IOException e) {
				logger.error("Hubo un problema de lectura/escritura en el socket ("
						+ socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ").", e);
				throw e;
			}
		}

		private String leerNCant(int msgLength) {
			try {
				int cantLeidos = 0;
				char[] msg = new char[msgLength];
				long tiempoInicial = Calendar.getInstance().getTimeInMillis();
				while (cantLeidos < msgLength) {
					if ((Calendar.getInstance().getTimeInMillis() - tiempoInicial) >= getTimeout()) {
						logger.error("Se super� el timeout de espera de respuesta al requerimiento.");
						return null;
					}
					if (in.ready()) {
						char[] leer = new char[msgLength - cantLeidos];
						in.read(leer);
						int cantLeidosCiclo = (new String(leer)).trim().length();
						System.arraycopy(leer, 0, msg, cantLeidos, cantLeidosCiclo);
						cantLeidos += cantLeidosCiclo;
					}
					try {
						Thread.sleep(getTimeoutSleep());
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				}
				return new String(msg);
			} catch (UnknownHostException e) {
				logger.error("Error al establecer conexi�n con el POS. (IP: " + socket.getInetAddress().getHostAddress()
						+ ").", e);
			} catch (IOException e) {
				logger.error("Hubo un problema de lectura/escritura en el socket ("
						+ socket.getInetAddress().getHostAddress() + ").", e);
			}
			return null;
		}

		/**
		 * Este m�todo env�a una petici�n al sistema correspondiente y devuelve una
		 * respuesta a dicha petici�n.
		 * 
		 * @param message
		 *            Requerimiento a ser evaluado por el sistema correspondiente.
		 * @param borrarPrimeraLinea
		 *            Se usa para establecer si se debe borrar la primera l�nea de
		 *            la respuesta
		 * @return La respuesta al requerimiento, luego de ser evaluada por el
		 *         sistema correspondiente.
		 */
		public String receiveMessage() {
			String cantALeer = leerNCant(this.length);
			if (cantALeer == null || cantALeer.trim().equals(""))
				return null;
			int msgLength = new Integer(cantALeer).intValue();
			String msg = leerNCant(msgLength);
			return msg;
		}

		/**
		 * @return the socket
		 */
		public Socket getSocket() {
			return socket;
		}

		/**
		 * @param socket
		 *            the socket to set
		 */
		public void setSocket(Socket socket) {
			this.socket = socket;
		}

		/**
		 * @return the in
		 */
		public BufferedReader getIn() {
			return in;
		}

		/**
		 * @param in
		 *            the in to set
		 */
		public void setIn(BufferedReader in) {
			this.in = in;
		}

		/**
		 * @return the length
		 */
		public int getLength() {
			return length;
		}

		/**
		 * @param length
		 *            the length to set
		 */
		public void setLength(int length) {
			this.length = length;
		}

		/**
		 * @return the timeout
		 */
		public int getTimeout() {
			return timeout;
		}

		/**
		 * @param timeout
		 *            the timeout to set
		 */
		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}

		/**
		 * @return the timeoutSleep
		 */
		public int getTimeoutSleep() {
			return timeoutSleep;
		}

		/**
		 * @param timeoutSleep
		 *            the timeoutSleep to set
		 */
		public void setTimeoutSleep(int timeoutSleep) {
			this.timeoutSleep = timeoutSleep;
		}

	}
