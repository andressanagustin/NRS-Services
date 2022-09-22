package com.allc.os4690.event;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.allc.conexion.Trama;
import com.allc.files.Files;
import com.allc.main.Main;
import com.allc.main.constants.Constants;
import com.allc.main.properties.Properties4690EQ;
import com.allc.saf.SAF;
import com.allc.saf.SAFProcess;
import com.allc.util.Util2;

public class Event4690Process {

		static Logger log = Logger.getLogger(Event4690Process.class);
		/**
		 * Obtain the message queue events and save it into the file waiting for the saf sender
		 */
		
		public static void stored4690Event(Event event){

			/**Instantiate from Event4690 class who contains the methods to extract 4690 event's messages**/
			Event4690 event4690 = new Event4690();
			/**Set the attributes. the template messages**/
			event4690.setApplTxtFileName(event.getApplTxtFileName());
			event4690.setCntrTxtFileName(event.getCntrTxtFileName());
			event4690.setTermTxtFileName(event.getTermTxtFileName());

			try {
				String nodoCtrl = event.getControllerId();
				String storeNumber = event.getStoreNumber();				

				/**Instantiate from SAF class**/
				SAF saf = new SAF();
				/**Set the attributes needed to do Stored process**/
				saf.setFileStore(event.getFileStore());
				saf.setCrlf(event.getCrlf());
				
				int m = 0;
				int n = 0;
				int messageNumber = 0;
				int sourceNumber = -1;
				int eventNumber = -1;
				char[] arrayOfChar = new char[2];
				StringBuffer localStringBuffer = new StringBuffer();
				//StringBuffer stringBufferTmp = new StringBuffer();
				boolean obtainEvent;
				StringBuffer mess = new StringBuffer("");
				List list;
				String nodoCtrlRead;
				String numHour;
				Trama trama;
				String msgToSend;
				String fechaTrama;
				int i4;
				int severity;
				String uniqueData;
				String dateE = "";
				String hourE = "";
				int byteAlto;
				int byteBajo;
				String nroFecc;
				int year;
				int month;
				int day;
				int hora;
				int minuto;
				int segundo;
				int i2;
				int i3;
				String terminalID;
				char messageGroup;
				char[] arrayOf18BytesUniqueData;
				while (!Main.isFinE4690Q()) {
					try{
						obtainEvent = false;
						//registro = Constants.Comunicacion.VACIO;
						/**************************************************
						 * Verifies if exists some requirement coming from pipe
						 *************************************************/
						//byte[] arrayOf32BytesReadPI2 = new byte[pi2DAO.getPi2DTO().getCantidadBytesLeer()];
						byte[] arrayOf32BytesReadPI2 = new byte[event.getCantBytesLeePipe()];
						if(event.getPi24690().readPipe(arrayOf32BytesReadPI2) > 0 ){
							//InputStream inputStream = 
							char[] arrayC = new char[event.getCantBytesLeePipe()];
							for(int o = 1 ; o < event.getCantBytesLeePipe(); o++){
								arrayC[o] = (char)(arrayOf32BytesReadPI2[o] & 0xFF);
							}
							
							//log.info(">>>>>>>Inicio>>>>>>>");
							log.debug("read: \"" + event4690.buffer2HexString(arrayC) + "\"");
							log.info("read: \"" + new String(arrayC) + "\"");
							//log.info("leyo: " + new String(arrayOf32BytesReadPI2,"UTF8"));
							
							if (arrayOf32BytesReadPI2 != null){
								/**only if we need to redirect the data to another pipe**/
								if((event.getRedirectedEvents() == Constants.Redirection.CREATE_PIPE_REDIRECTION ) ||
								   (event.getRedirectedEvents() == Constants.Redirection.OPEN_PIPE_REDIRECTION_IN_WRITE_MODE ) ||
								   (event.getRedirectedEvents() == Constants.Redirection.OPEN_PIPE_REDIRECTION_IN_WRITE_MODE_IF_FAIL_THEN_CREATE_PIPE_REDIRECTION ) )
									if(null != event.getPi2Redirected()){
										event.getPi2Redirected().write(arrayOf32BytesReadPI2);
									}

								if ((arrayOf32BytesReadPI2[0] == 0) && (arrayOf32BytesReadPI2[1] == 0)) {
									if(log.isDebugEnabled())
										log.debug("4690EQ -> Inside Dummy Record");
						            m = 0xFF & arrayOf32BytesReadPI2[2];
						            n = 0xFF & arrayOf32BytesReadPI2[3];
						            i4 = n << 8 | m;

						            if(log.isDebugEnabled())
										log.debug("4690EQ -> lost events - " + i4);
								}
								
								byteAlto = (short)((0xFF & (short)arrayOf32BytesReadPI2[1]) << 8);
								byteBajo = (short)(0xFF & (short)arrayOf32BytesReadPI2[0]);
								nroFecc = String.valueOf(byteAlto + byteBajo);
								if(log.isDebugEnabled())
									log.debug("numDate: " + nroFecc);
								
								year = event4690.getData(byteAlto + byteBajo, 16, 9, 7);
								month = event4690.getData(byteAlto + byteBajo, 16, 5, 4);
								day = event4690.getData(byteAlto + byteBajo, 16, 0, 5);
								
								dateE = String.valueOf(1980 + year) + Util2.lpad(String.valueOf(month), "0", 2) + Util2.lpad(String.valueOf(day), "0", 2);

								if(log.isDebugEnabled())
									log.debug("date: " + dateE);

								byteAlto = 0;
								byteBajo = 0;

								byteAlto = (int)((0xFF & (short)arrayOf32BytesReadPI2[3]) << 8);
								byteBajo = (int)(0xFF & (short)arrayOf32BytesReadPI2[2]);		

								numHour = String.valueOf(byteAlto + byteBajo);

								if(log.isDebugEnabled())
									log.debug("numHour: " + numHour);

								hora    = event4690.getData(byteAlto + byteBajo, 16, 11, 5);
								minuto  = event4690.getData(byteAlto + byteBajo, 16, 5, 6);
								segundo = event4690.getData(byteAlto + byteBajo, 16, 0, 5) * 2;

								hourE =  Util2.lpad(String.valueOf(hora),"0",2) + Util2.lpad(String.valueOf(minuto),"0",2) + Util2.lpad(String.valueOf(segundo),"0",2);

								if(log.isDebugEnabled())
									log.debug("hourE: " + hourE);

								arrayOfChar[0] = ((char)arrayOf32BytesReadPI2[5]);
								arrayOfChar[1] = ((char)arrayOf32BytesReadPI2[4]);
								nodoCtrlRead = new String(arrayOfChar);
								
								/**it's filtered**/
								if(Properties4690EQ.Param4690.FILTER_BY_CONTROLLER.equals("S")){
									if (nodoCtrl.equals(nodoCtrlRead)) {
										/**get the event 'cause is filtered and it's the same controller node**/
										obtainEvent = true;
									}
								}else
									/**get the event anyway 'cause is not filtered**/
									obtainEvent = true;
								
								/**if the data is filtered**/
								if (obtainEvent) {
									if(log.isDebugEnabled())
										log.debug("4690EQ -> Parsing message for Controller " + nodoCtrlRead);

									i2 = (short)((0xFF & (short)arrayOf32BytesReadPI2[7]) << 8);
									i3 = (short)(0xFF & (short)arrayOf32BytesReadPI2[6]);
									terminalID = String.valueOf(i2 + i3);

									if(log.isDebugEnabled())
										log.debug("4690EQ -> Terminal Id = " + terminalID);
									
									log.info("4690EQ -> Terminal Id = " +  Util2.lpad(String.valueOf(terminalID),"0",3) + ", Store Number: " + Util2.lpad(event.getStoreNumber(),"0",4));
									String ipPos = event.getIp();
									// buscar solo si terminalid es distinto de 000
									if(Integer.parseInt(terminalID) != 0)
									{
										//leer archivo host split(spacio)
										String findPos = 'T' + Util2.lpad(event.getStoreNumber(),"0",4) + Util2.lpad(String.valueOf(terminalID),"0",3) ;
										//
										Files file4690 = new Files();
										String resultFind = file4690.readLineOfFile4690FindString("C:/ADX_SDT1/HOSTS.BAK",findPos);
										log.info("REsultado de buscar en hosts: " + resultFind);
										if(resultFind != null) {
											String[] result = resultFind.split(" ");
											log.info("Nueva Ip caja: " + result[0]);
											//event.setIp(result[0]);
											ipPos = result[0];
										}
									}
									
									
									sourceNumber = arrayOf32BytesReadPI2[8];

									if (arrayOf32BytesReadPI2[9] == 0)
									    arrayOf32BytesReadPI2[9] = 32;
									
									messageGroup = (char)arrayOf32BytesReadPI2[9];
									if(log.isDebugEnabled())
										log.debug("4690EQ -> Message Group = " + messageGroup);

									m = 0xFF & arrayOf32BytesReadPI2[10];
									n = 0xFF & arrayOf32BytesReadPI2[11];
									messageNumber = n << 8 | m;

									if(log.isDebugEnabled())
										log.debug("4690EQ -> Message Number = " + messageNumber);

									severity = arrayOf32BytesReadPI2[12];
									if(log.isDebugEnabled())
										log.debug("4690EQ -> Severity = " + severity);

									eventNumber = arrayOf32BytesReadPI2[13];

									arrayOf18BytesUniqueData = new char[18];
									for (int i5 = 0; i5 < arrayOf18BytesUniqueData.length; i5++){
										arrayOf18BytesUniqueData[i5] = arrayC[(i5 + 14)];
									    //arrayOf18BytesUniqueData[i5] = (char)(arrayOf32BytesReadPI2[(i5 + 14)] &  0xFF);
									    //arrayOf18BytesUniqueData[i5] = (byte) (arrayOf18BytesUniqueData[i5]  &  0xFF) ;
									}
									if(log.isDebugEnabled())
										log.debug("4690EQ -> uniqueData: \"" + event4690.buffer2HexString(arrayOf18BytesUniqueData) + "\"");
									
									log.info("4690EQ -> Terminal Id = " + terminalID + " " + "Message Group = " + messageGroup + " " + "Message Number = " + messageNumber + " " + "Severity = " + severity + " " + "uniqueData: \"" + event4690.buffer2HexString(arrayOf18BytesUniqueData) + "\"" );
									localStringBuffer.setLength(0);

									if (messageGroup != ' ')// W por ejemplo : 
									   localStringBuffer.append(messageGroup);
									else {
										localStringBuffer.append('?');
									}

									String messaNumber = event4690.formatInt(messageNumber);
									localStringBuffer.append(messaNumber);
									String sourNumber = event4690.formatInt(sourceNumber);
									localStringBuffer.append(" S").append(sourNumber);
									localStringBuffer.append('/');
									String evenNumber = event4690.formatInt(eventNumber);
									localStringBuffer.append("E").append(evenNumber);
									localStringBuffer.append(' ');
									uniqueData = (Util2.removeChar(event4690.getMsgs(messageGroup, messageNumber, arrayOf18BytesUniqueData),'|')).trim();
									localStringBuffer.append(uniqueData);

									//Este es el mensaje decodificado final
									log.info("Notification message: " + localStringBuffer.toString());
									mess.setLength(0);
									mess.append(messageGroup).append(event.getCar()).append(messaNumber).append(event.getCar()).append("S").append(sourNumber).append(event.getCar()).append("E").append(evenNumber).append(event.getCar()).append(severity).append(event.getCar()).append(uniqueData);
									/**si no se filtra el mensaje**/
									if(isMessageAllowed(event.getHash(), mess.toString())){
										fechaTrama =  Util2.fechaFormato(Calendar.getInstance().getTime());
										
										list = Arrays.asList(new String[]{"S", String.valueOf(Constants.ProcessConstants.SAVE_4690_EVENT_QUEUE_PROCESS), nodoCtrl, event.getDesCadena(), storeNumber, fechaTrama, dateE + hourE, event.getProgramSource(), ipPos, nodoCtrlRead, terminalID, String.valueOf(messageGroup), messaNumber, "S" + sourNumber, "E" + evenNumber, String.valueOf(severity), uniqueData});
										//se carga la trama
										trama = new Trama(list, event.getCantDatosHeader(), event.getCar());
										if(trama.loadData()){
											log.info(trama.toString());
											msgToSend = trama.getHeaderStr() + trama.getSeparationCar() + trama.getBodyStr();
											SAFProcess.stored(msgToSend, Constants.Comunicacion.CRLF);
										}else
											log.error(trama.getError());
									}
								}
							}
						}else{
							Thread.sleep(event.getTimeOutSleep());	
						}

					}catch(Exception e){
						log.error("stored4690Event: " + e);
					}
				}
				if(event.getPi24690().closePi2Read())
					log.info("pipe: " + event.getPi24690().getPi2DTO().getName() + " closed");
				else
					log.info("pipe: " + event.getPi24690().getPi2DTO().getName() + " not closed");

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if(null != event.getPi2Redirected()){
					if(event.getPi2Redirected().closePi2Write()){
						log.info("pipe: " + event.getPi2Redirected().getPi2DTO().getName() + " closed.");
					}else{
						log.info("pipe: " + event.getPi2Redirected().getPi2DTO().getName() + " not closed.");
					}
				}
				log.info("4690 message queue event finished");
			}

		}  
		
		
		private static boolean isMessageAllowed(Hashtable hash, String key){
			boolean result = true;
			try {
				if(hash.containsKey(key))
					result = false;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return result;
		}
}
