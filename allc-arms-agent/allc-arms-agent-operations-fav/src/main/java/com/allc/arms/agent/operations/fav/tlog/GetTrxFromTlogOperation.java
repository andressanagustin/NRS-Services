package com.allc.arms.agent.operations.fav.tlog;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.tsl.TSLUtility;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.core.receiver.ReceiverPipe;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.RandomAccessFile4690;

/**
 * Operación encargada de retornar la línea (decodificada) del tsl correspondiente a la transacción solicitada.
 * Operación ID: 62
 * 
 * @author gustavo
 *
 */
public class GetTrxFromTlogOperation extends AbstractOperation{
	
	private Logger logger = Logger.getLogger(GetTrxFromTlogOperation.class);
	private static Pattern c = Pattern.compile(",");
	private static Pattern p = Pattern.compile("\\|");
	protected String storeCode;

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		return false;
	}
	
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		logger.info("Iniciando GetTrxFromTlogOperation...");
		try {
			//liberamos pipeServer
			ReceiverPipe.waitAvailable = false;
			String path = (String) frame.getBody().get(0);
			String nombreTlog = (String) frame.getBody().get(1);
			String tienda = (String) frame.getBody().get(2);
			while(tienda.length() < 3)
				tienda = "0"+tienda;
			short terminal = Short.valueOf((String) frame.getBody().get(3)).shortValue();
			short numTrans = Short.valueOf((String) frame.getBody().get(4)).shortValue();
			String resp = null;
			try {
				boolean isRespaldo = nombreTlog.startsWith("LG");
				String nombreIdx = isRespaldo ? "LI"+nombreTlog.substring(2,8) + "." + tienda : nombreTlog + ".IDX";
				logger.info("IDX a leer: " + path + File4690.separatorChar + nombreIdx);
				File4690 idxFile = new File4690(path + File4690.separatorChar + nombreIdx);
				if(!idxFile.exists()){
					createIdxFile(path, nombreIdx, nombreTlog+"."+tienda, terminal, numTrans, frame, properties, pipe);
				} else {
					RandomAccessFile4690 indexFile = new RandomAccessFile4690(path+ File4690.separatorChar +nombreIdx, "r");
					byte[] trans = new byte[64];
					int offset = -1;
					long indexFileSize = indexFile.length();
					while(indexFileSize > 0){
						indexFile.read(trans);
						indexFileSize -= 64;
						offset = extrae4(trans, 0);
						logger.info("offset:"+offset);
						short term = extrae2(trans, 4);
						logger.info("term:"+term);
						short trx = extrae2(trans, 6);
						logger.info("trx:"+trx);
						if(terminal == term && numTrans == trx){
							break;
						}
					}
					indexFile.close();
					String linea = null;
					String decod = null;
					logger.info("offset: " + offset);
					if(offset > 0){
						logger.info("TSL a leer: " + path + File4690.separatorChar + nombreTlog + (isRespaldo ? "."+tienda: ".DAT"));
						RandomAccessFile4690 randFileRead = new RandomAccessFile4690(path + File4690.separatorChar + nombreTlog + (isRespaldo ? "."+tienda: ".DAT"), "r");
						linea = Files.readLineByBytesPositionOfFile4690(randFileRead, offset);
						randFileRead.close();
						decod = ident(linea.substring(1, linea.length()) + "," + "\"");
						logger.info("decod: " + decod);
					}
					resp = Util.addLengthStartOfString(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + "1" + ArmsAgentConstants.Communication.FRAME_SEP + decod, properties.getInt("serverSocket.quantityBytesLength"));
					logger.info("Respuesta a enviar: " + resp);
					pipe.sendData((String) frame.getHeader().get(Frame.RESPONSE_CHANNEL), (String) frame.getHeader().get(Frame.POS_SOURCE), resp);
				}
			} catch(Exception ex){
				logger.error(ex.getMessage(), ex);
				resp = Util.addLengthStartOfString(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + "0", properties.getInt("serverSocket.quantityBytesLength"));
				logger.info("Respuesta a enviar: " + resp);
				pipe.sendData((String) frame.getHeader().get(Frame.RESPONSE_CHANNEL), (String) frame.getHeader().get(Frame.POS_SOURCE), resp);
			}
			
			logger.info("Finaliza GetTrxFromTlogOperation...");			
		} catch (Exception e){
			logger.error(e.getMessage(), e);
		}
		
		return false;
	}
	
	private void createIdxFile(String path, String idxName, String tslName, short terminal, short numTrans, Frame frame, PropFile properties, ConnPipeServer pipe) throws Exception{
		File4690 pathFile = new File4690(path);
		pathFile.mkdir();
		
		RandomAccessFile4690 randFileRead = new RandomAccessFile4690(path + File4690.separatorChar + tslName, "r");
		long offset = 0;
		String linea = Files.readLineByBytesPositionOfFile4690(randFileRead, offset);
		while(linea != null){
			String decod = ident(linea.substring(1, linea.length()) + "," + "\"");
			if(decod != null && !decod.isEmpty()){
				List list = Arrays.asList(c.split(decod));
				List stringTypesList = Arrays.asList(p.split((String)list.get(0)));
				short term = Short.valueOf(stringTypesList.get(1).toString()).shortValue();
				short transNum = Short.valueOf(stringTypesList.get(2).toString()).shortValue();
				if(terminal == term && numTrans == transNum){
					String resp = Util.addLengthStartOfString(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + "1" + ArmsAgentConstants.Communication.FRAME_SEP + decod, properties.getInt("serverSocket.quantityBytesLength"));
					logger.info("Respuesta a enviar: " + resp);

					pipe.sendData((String) frame.getHeader().get(Frame.RESPONSE_CHANNEL), (String) frame.getHeader().get(Frame.POS_SOURCE), resp);
				}
				byte[] reg = new byte[64];
				inserta4(reg, (int)offset, 0);
				inserta2(reg, term, 4);
				inserta2(reg, transNum, 6);
				String data = new String(reg);
				Files.creaEscribeDataArchivo4690(path + File4690.separatorChar + idxName, data, true);
			}
			offset = randFileRead.getFilePointer();
			linea = Files.readLineByBytesPositionOfFile4690(randFileRead, offset);
		}
		randFileRead.close();
	}
	
	/**
    * Extrae un entero de 4 bytes desde un vector de bytes
    * @param b vector de bytes
    * @param p posición inicial a extraer
    * @return un entero entre 0 y 4.294.967.295
    */
   public int extrae4(byte[] b, int p) {
      int n, v;
      n = b.length - p;
      if(n < 4) {
         return(0);
      }
      v = b[p] & 0xff;
      v = v | ((b[p+1] & 0xff) << 8);
      v = v | ((b[p+2] & 0xff) << 16);
      v = v | ((b[p+3] & 0xff) << 24);
      return(v);
   }

   /**
    * Inserta un entero de 4 bytes en un vector de bytes
    * @param b vector de bytes
    * @param v valor a insertar
    * @param p posición inicial donde insertar
    */
   public void inserta4(byte[] b, int v, int p) {
      int n;
      n = b.length - p;
      if(n < 4) return;
      b[p] = (byte) (v & 0xff);
      b[p+1] = (byte) ( (v & 0xff00) >> 8);
      b[p+2] = (byte) ( (v & 0xff0000) >> 16);
      b[p+3] = (byte) ( (v & 0xff000000) >> 24);
   }

   /**
    * Extrae un entero de 2 bytes desde un vector de bytes
    * @param b vector de bytes
    * @param p posición inicial a extraer
    * @return un entero entre 0 y 65.535
    */
   public short extrae2(byte[] b, int p) {
      int n;
      short v;
      n = b.length - p;
      if(n < 2) return(0);
      v = (short) (b[p] & 0xff);
      v = (short) (v | ((b[p+1] & 0xff) << 8));
      if(v == -32768) v = 0;
      return(v);
   }

   /**
    * Inserta un entero de 2 bytes en un vector de bytes
    * @param b vector de bytes
    * @param v valor a insertar
    * @param p posición inicial donde insertar
    */
   public void inserta2(byte[] b, short v, int p) {
      int n;
      n = b.length - p;
      if(n < 2) return;
      b[p] = (byte) (v & 0xff);
      b[p+1] = (byte) ( (v & 0xff00) >> 8);
   }

	   
	protected String ident(String dato) throws Exception {
		byte[] arreglop, arregloj;
		arreglop = dato.substring(0, 1).getBytes("ISO-8859-1");
		String sal1 = "", salp = "";
		String salida = "";
		String cadena = "";
		int tipo = 0;
		int tcadena = 0;
		List list, lista, listb;
		String REGEX = "\",\"";
		String CAMP = "\":\"";
		String SCAMP = ":";
		Pattern p = Pattern.compile(REGEX);
		Pattern q = Pattern.compile(CAMP);
		Pattern r = Pattern.compile(SCAMP);
		try {
			tipo = Integer.parseInt(TSLUtility.unpack(arreglop));
			logger.info("Tipo: " + tipo);
			if (tipo == 0) {

				list = Arrays.asList(p.split(dato));
				for (int x = 0; x <= list.size() - 1; x++) {
					cadena = list.get(x).toString();

					arregloj = cadena.substring(0, 1).getBytes("ISO-8859-1");
					tcadena = Integer.parseInt(TSLUtility.unpack(arregloj));

					lista = Arrays.asList(r.split(cadena));
					if (x == list.size() - 1)
						salp = TSLUtility.parseatlog(lista, tcadena);
					else
						salp = TSLUtility.parseatlog(lista, tcadena) + ",";

					salida = salida + salp;

				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			salida = "";
		}
		return salida;
	}

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
