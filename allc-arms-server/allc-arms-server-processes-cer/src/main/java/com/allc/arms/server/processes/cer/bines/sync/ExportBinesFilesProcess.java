package com.allc.arms.server.processes.cer.bines.sync;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.FilesHelper;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;

public class ExportBinesFilesProcess extends AbstractProcess {

	protected static Logger logger = Logger.getLogger(ExportBinesFilesProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	private int sleepTime;
	protected ConnSocketClient socketClient = null;
	protected boolean finished = false;
	private int horaBines;
	private int minBines;
	Timer timerBines = new Timer();
	StoreDAO storeDAO = new StoreDAO();
	ParamsDAO paramsDAO = new ParamsDAO();
	Session sessionSaadmin = null;
	Session sessionArts = null;
	File secYsubFile;
	File secYsubFileCmd;
	File regBinesFile;
	File regBinesFileCmd;
	private File outFolderSecySub;
	protected SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
	protected SimpleDateFormat dfcompare = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected SimpleDateFormat formatterFile = new SimpleDateFormat("yyyyMMdd");
	File opcPagoFile;
	File opcPagoFileCmd;
	private String store = null;
	private String outFolderPart1;
	private String outFolderPart2;
	private File bkpFolder;
	private String binSec = null;
	private String binOpc = null;
	private String binArc = null;

	protected void inicializar() {
		try {

			sleepTime = properties.getInt("exportBines.sleeptime");
			horaBines = properties.getInt("exportBines.horaExportacion");
			minBines = properties.getInt("exportBines.minExportacion");
			sessionSaadmin = iniciarSesionSaadmin();
			sessionArts = iniciarSesionArts();
			// sleepTime = 30000;
			outFolderSecySub = new File("C:/ALLC");
			store = properties.getObject("eyes.store.code");
			ParamValue paravalue5 = paramsDAO.getParamByClave(sessionSaadmin, store,
					ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_IN");
			outFolderPart1 = properties.getObject("SUITE_ROOT") + File.separator + paravalue5.getValor()
					+ File.separator;
			outFolderPart2 = File.separator + properties.getObject("fileUpdaterDown.folder.name");
			bkpFolder = new File(
					properties.getObject("SUITE_ROOT") + File.separator + "EYES" + File.separator + "BINES_BKP");
			bkpFolder.mkdirs();
			ParamValue paravalue = paramsDAO.getParamByClave(sessionSaadmin, store,
					ArmsServerConstants.AmbitoParams.SUITE_PARAMS, "NOM_BINSEC");
			binSec = paravalue.getValor();
			ParamValue paravalue2 = paramsDAO.getParamByClave(sessionSaadmin, store,
					ArmsServerConstants.AmbitoParams.SUITE_PARAMS, "NOM_BINOPC");
			binOpc = paravalue2.getValor();
			ParamValue paravalue3 = paramsDAO.getParamByClave(sessionSaadmin, store,
					ArmsServerConstants.AmbitoParams.SUITE_PARAMS, "NOM_BINARC");
			binArc = paravalue3.getValor();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void run() {

		logger.info("Iniciando ExportBinesFilesProcess...");
		inicializar();
		final String storeCode = StringUtils.leftPad(properties.getObject("eyes.store.code"), 3, "0");

		try {

			Date horaBinesDate = new Date(System.currentTimeMillis());

			Calendar c = Calendar.getInstance();
			c.setTime(horaBinesDate);
			// Si la hora es posterior a las 10am se programa para el dia
			// siguiente
			if ((c.get(Calendar.HOUR_OF_DAY) > horaBines)|| (c.get(Calendar.HOUR_OF_DAY) == horaBines && c.get(Calendar.MINUTE) == minBines)) {
				c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 1);
			}

			c.set(Calendar.HOUR_OF_DAY, horaBines);
			c.set(Calendar.MINUTE, minBines);
			c.set(Calendar.SECOND, 0);

			horaBinesDate = c.getTime();
			long tiempoRepeticion = 86400000;

			TimerTask timerTaskExportBines = new TimerTask() {
				public void run() {

					sessionSaadmin = iniciarSesionSaadmin();
					sessionArts = iniciarSesionArts();

					try {
						crearArchivoGrupoSecciones();
						crearArchivoRegistroBinesPorTienda();
						crearArchivoOpcionesDePago();

					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					} finally {
						if (sessionArts != null && sessionSaadmin != null) {
							sessionArts.close();
							sessionSaadmin.close();
						}
					}
				}
			};

			timerBines.schedule(timerTaskExportBines, horaBinesDate, tiempoRepeticion);

		} catch (Exception e) {
			UtilityFile.createWriteDataFile(getEyesFileName(),
					"EXP_BIN_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode
							+ "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Error al exportar Bines.\n",
					true);
			logger.error(e.getMessage(), e);
		}
		while (!isEnd) {

			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}

			finished = true;
		}
	}

	public void crearArchivoGrupoSecciones() {

		logger.info("Inicia proceso de creación de Archivo de Secciones.");

		List<Object[]> dataPaBinGrpsec = getDataPaBinGrpsec();

		if (dataPaBinGrpsec != null && !dataPaBinGrpsec.isEmpty()) {
			try {
				String gSecySubFileName = binSec + ".DAT";
				String gSecySubCmdFileName = binSec + ".CMD";

				File gSecySubFileDel = new File(outFolderSecySub, gSecySubFileName);
				if (gSecySubFileDel.exists())
					gSecySubFileDel.delete();

				secYsubFile = new File(outFolderSecySub, gSecySubFileName);
				BufferedWriter bwr = new BufferedWriter(new FileWriter(secYsubFile, true));

				for (int i = 0; i < dataPaBinGrpsec.size(); i++) {
					Object[] row = dataPaBinGrpsec.get(i);

					String idGrpSec = row[0].toString();
					String FL_INCL = row[2].toString();

					List<Object[]> dataPaBinSec = getDataPaBinSec(idGrpSec);
					int CRRGRP = 0;
					for (int j = 0; j < dataPaBinSec.size(); j++) {
						Object[] row2 = dataPaBinSec.get(j);
						String ID_DPT_PS = StringUtils.leftPad(row2[1].toString(), 4, "0") + "0000";
						bwr.write(StringUtils.leftPad(idGrpSec, 3, "0")
								+ StringUtils.leftPad(String.valueOf(CRRGRP), 2, "0") + ID_DPT_PS + FL_INCL);
						bwr.newLine();
						CRRGRP++;
					}

				}

				File gSecySubFileCmdDel = new File(outFolderSecySub, gSecySubCmdFileName);
				if (gSecySubFileCmdDel.exists())
					gSecySubFileCmdDel.delete();

				secYsubFileCmd = new File(outFolderSecySub, gSecySubCmdFileName);
				BufferedWriter bwrcmd = new BufferedWriter(new FileWriter(secYsubFileCmd, true));
				bwrcmd.write("C:\\ADX_UDT1");
				bwrcmd.newLine();

				if (bwr != null) {
					bwr.close();
				}
				if (bwrcmd != null) {
					bwrcmd.close();
				}

				logger.info("Finaliza proceso de creación de Archivo de Secciones.");

				logger.info("Inicia copiado a folder de tiendas activas.");
				List activeStores = storeDAO.getAllActiveStores(sessionSaadmin);
				Iterator itStores = activeStores.iterator();
				while (itStores.hasNext()) {
					Store tienda = (Store) itStores.next();
					String codTienda = tienda.getKey().toString();

//					if (checkStoreClose(codTienda)) {
						while (codTienda.length() < 3)
							codTienda = "0" + codTienda;
						try {
							File pathTienda = new File(outFolderPart1 + codTienda + outFolderPart2);
							FilesHelper.copyFile(outFolderSecySub.getAbsolutePath(), pathTienda.getAbsolutePath(),
									secYsubFile.getName(), secYsubFile.getName());
							FilesHelper.copyFile(outFolderSecySub.getAbsolutePath(), pathTienda.getAbsolutePath(),
									secYsubFileCmd.getName(), secYsubFileCmd.getName());

						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
//					}

				}
				Date now = new Date();
				String strDate = formatterFile.format(now);
				FilesHelper.copyFile(outFolderSecySub.getAbsolutePath(), bkpFolder.getAbsolutePath(),
						secYsubFile.getName(), secYsubFile.getName() + "_" + strDate);
				secYsubFile.delete();
				secYsubFileCmd.delete();
				logger.info("Finaliza copiado a folder de tiendas activas.");

			} catch (Exception e) {
				logger.error("Error durante el proceso de creación de Archivo de Secciones.");
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void crearArchivoRegistroBinesPorTienda() {

		logger.info("Inicia proceso de creación de Archivo de Registro de Bines.");

		List activeStores = storeDAO.getAllActiveStores(sessionSaadmin);

		if (activeStores != null && !activeStores.isEmpty()) {
			try {
				String gRegBinesFileName = binArc + ".DAT";
				String gRegBinesCmdFileName = binArc + ".CMD";

				File gSecySubFileDel = new File(outFolderSecySub, gRegBinesFileName);
				if (gSecySubFileDel.exists())
					gSecySubFileDel.delete();

				File gRegBinesCmdDel = new File(outFolderSecySub, gRegBinesCmdFileName);
				if (gRegBinesCmdDel.exists())
					gRegBinesCmdDel.delete();

				regBinesFileCmd = new File(outFolderSecySub, gRegBinesCmdFileName);
				BufferedWriter bwrcmd = new BufferedWriter(new FileWriter(regBinesFileCmd, true));
				bwrcmd.write("C:\\ADX_UDT1");
				bwrcmd.newLine();
				bwrcmd.write("C:\\ADX_IPGM\\ACEBIRBL.386");
				bwrcmd.newLine();
				if (bwrcmd != null) {
					bwrcmd.close();
				}

				regBinesFile = new File(outFolderSecySub, gRegBinesFileName);

				Iterator itTienda = activeStores.iterator();

				// ARMADO DE HEADER (COMUN)
				// CANTIDAD DE BINES ACTIVOS

				int NUMBINES = getCountBines();

				// CANTIDAD DE RANGOS ACTIVOS

				int NUMRANGOS = getCountRangos();

				int NUMREGISTROS_Aux = NUMBINES + NUMRANGOS;
				String NUMREGISTROS = StringUtils.leftPad(String.valueOf(NUMREGISTROS_Aux), 7, "0");
				Date now = new Date();
				String strDate = df.format(now);
				String LNBIN_HEADER = "H1000001BIN FILE" + strDate + "R0400" + NUMREGISTROS + "\r\n";

				int SEQ_FOO = 0;
				// FIN HEADER

				while (itTienda.hasNext()) {
					Store tiendaAux = (Store) itTienda.next();
//					String codTienda = tiendaAux.getKey();
//					if (checkStoreClose(codTienda)) {
						try {
							if (regBinesFile.exists())
								regBinesFile.delete();
							regBinesFile = new File(outFolderSecySub, gRegBinesFileName);

							BufferedWriter bwrRB = new BufferedWriter(new FileWriter(regBinesFile, true));
							bwrRB.write(LNBIN_HEADER);

							String DES_TIENDA = StringUtils.leftPad(tiendaAux.getKey().toString(), 3, "0");
							int CONTADOR = 1;
							List<Object[]> dataPaBin = getPaBinData();

							if (dataPaBin != null && !dataPaBin.isEmpty()) {

								for (int i = 0; i < dataPaBin.size(); i++) {
									Object[] row = dataPaBin.get(i);
									String REC_ID = "D1";
									CONTADOR++;
									String ID_BIN = row[0].toString();
									String CD_BIN = row[1].toString();
									String SEQ_NUM = StringUtils.leftPad(String.valueOf(CONTADOR), 6, "0");
									String BIN_NUM = StringUtils.rightPad(row[1].toString(), 20, " ");
									String CD_CARD_TY = row[16].toString();
									String ACCT_CD = "A";
									String FL_PROC = StringUtils.leftPad(row[3].toString(), 4, "0");
									String USER_FL = "0000";
									String CD_CARDPID = row[24].toString();
									String LEN_ACNT_NUM = row[29].toString();

									// CONSULTA INICIAL DE DEPÓSITO
									String CD_INI_DEP = null;
									List<Object[]> dataIniDep = getIniDepData(CD_CARDPID);
									if (dataIniDep != null && !dataIniDep.isEmpty()) {
										for (int j = 0; j < dataIniDep.size(); j++) {
											Object[] row2 = dataIniDep.get(j);
											if (row2 != null && row2[3] != null)
												CD_INI_DEP = row2[3].toString();
											else
												CD_INI_DEP = CD_CARDPID;
										}

									} else {
										CD_INI_DEP = CD_CARDPID;
									}

									String CD_CARDACE = row[28].toString();
									String ID_OPCHDR = "0";
									String CD_OPCSLS = "0";

									// // 1. OPCION EN TIENDA - SELECCIONA
									// OPCIÓN
									// SEGÚN
									// // PRIORIDAD DE ASIGNACIÓN
									// int dataOpTienda =
									// getOpTiendaData(DES_TIENDA, CD_CARDPID);
									// if (dataOpTienda != 0) {
									// ID_OPCHDR = String.valueOf(dataOpTienda);
									// // 2. VERIFICAR QUE SE ENCUENTRE ACTIVO
									// // 2.A BINES CER - REGISTRADOS EN
									// // PA_BIN_OPCBIN (BANCARIAS NO REGISTRAN
									// EN
									// // ESTA TABLA)
									// List<Object[]> dataBin = null;
									// if (CD_CARDACE == "CR" || CD_CARDACE ==
									// "GF"
									// || CD_CARDACE == "OC") {
									// dataBin =
									// getDataBinCer(Integer.valueOf(ID_OPCHDR),
									// Integer.valueOf(CD_BIN));
									// } else {
									// // 2.B BINES TARJETAS BANCARIAS
									// dataBin =
									// getDataBinTarjBanc(Integer.valueOf(ID_OPCHDR));
									// }
									//
									// if (dataBin != null &&
									// !dataBin.isEmpty()) {
									// for (int k = 0; k < dataBin.size(); k++)
									// {
									// Object[] row3 = dataBin.get(k);
									// String IND_ACTIVO = row3[14].toString();
									// if (IND_ACTIVO.equals("1")) {
									// // OPCION DE PAGO
									// CD_OPCSLS = row3[2].toString();
									// } else {
									// // OPCION DE PAGO PREDETERMINADA
									// CD_OPCSLS = "0";
									// }
									// }
									// }
									//
									// }

									// 1. OPCION EN TIENDA - SELECCIONA OPCIÓN
									// SEGÚN
									// PRIORIDAD DE ASIGNACIÓN
									List<Object[]> dataBin = null;
									if (CD_CARDACE == "CR" || CD_CARDACE == "GF" || CD_CARDACE == "OC") {
										dataBin = getDataBinCer(Integer.valueOf(ID_OPCHDR), Integer.valueOf(CD_BIN));
									} else {
										// 2.B BINES TARJETAS BANCARIAS
										dataBin = getDataBinTarjBanc(Integer.valueOf(ID_OPCHDR));
									}

									if (dataBin != null && !dataBin.isEmpty()) {
										for (int q = 0; q < dataBin.size(); q++) {
											Object[] row6 = dataBin.get(q);
											ID_OPCHDR = row6[0].toString();
										}

										int dataOpTienda = getOpTiendaData(DES_TIENDA, CD_CARDPID);
										if (dataOpTienda != 0) {
											// 2. VERIFICAR QUE SE ENCUENTRE
											// ACTIVO
											List<Object[]> binOpcHdr = null;
											binOpcHdr = getDataBinTarjBanc(Integer.valueOf(ID_OPCHDR));
											if (binOpcHdr != null && !binOpcHdr.isEmpty()) {
												for (int k = 0; k < binOpcHdr.size(); k++) {
													Object[] row3 = binOpcHdr.get(k);
													String IND_ACTIVO = row3[14].toString();
													if (IND_ACTIVO.equals("1")) {
														// OPCION DE PAGO
														CD_OPCSLS = row3[2].toString();
														// if(FL_SETOPC ==
														// 0){CD_OPCSLS =
														// String.valueOf(0);}
													} else {
														// OPCION DE PAGO
														// PREDETERMINADA
														CD_OPCSLS = "0";
													}
												}
											}
										}
									}

									if (ID_OPCHDR.equals("0"))
										CD_OPCSLS = "0";
									String LENID_CARDTY = LEN_ACNT_NUM;
									String LEN_PAN = StringUtils.leftPad(LENID_CARDTY, 2, "0");
									String CARD_PID = CD_CARDACE;
									String NET_ID = "CC";
									String HOST_TY = row[6].toString();
									String FL_CRDA = row[7].toString();
									String FL_FTM = row[8].toString();
									String FL_MANA = row[9].toString();
									String FL_BILL = row[10].toString();
									String FL_POINTS = row[11].toString();
									String DEPARTM = StringUtils.rightPad(row[12].toString(), 6, "0");
									String DES_BIN = StringUtils.rightPad(row[13].toString(), 15, " ");
									String CD_FRANK = StringUtils.leftPad(row[14].toString(), 2, "0");
									String FL_BSOL = row[15].toString();
									String CARD_TY = StringUtils.leftPad(CD_CARD_TY, 2, "0");
									String FL_RET = row[17].toString();
									String SV_CPID = StringUtils.leftPad(CD_OPCSLS, 2, "0");
									String FL_PIN = row[18].toString();
									String FL_CVC = row[19].toString(); // RETIRADO
									String FL_CHANGE = row[20].toString(); // RETIRADO
									String INI_CARD = CD_INI_DEP;

									// ARMADO DE LINEA DE REGISTRO
									String LINE = REC_ID + SEQ_NUM + BIN_NUM + LEN_PAN + ACCT_CD + FL_PROC + USER_FL
											+ CARD_PID + NET_ID + HOST_TY + FL_CRDA + FL_FTM + FL_MANA + FL_BILL
											+ FL_POINTS;
									LINE = LINE + DEPARTM + DES_BIN + CD_FRANK + FL_BSOL + CARD_TY + FL_RET + SV_CPID
											+ FL_PIN + INI_CARD + "\r\n";
									SEQ_FOO = CONTADOR + 1;
									bwrRB.write(LINE);

								}
							}

							// CONCATENAR BINES GENÉRICOS (TARJETAS
							// INTERNACIONALES
							// - RANGOS)
							List<Object[]> dataBinGen = getDataBinGen();
							String BINGEN = "";
							String CD_RANGO = null;
							String CD_CARDPID = null;
							String DES_CARDPID = null;
							String CD_CARDACE = null;
							String CD_ADQ = null;
							String LEN_ACNT_NUM = null;
							String SEQ_PRED = null;
							int CTA_BINGEN = 0;
							if (dataBinGen != null && !dataBinGen.isEmpty()) {
								for (int m = 0; m < dataBinGen.size(); m++) {
									Object[] row4 = dataBinGen.get(m);
									CTA_BINGEN = CTA_BINGEN + 1;
									CD_RANGO = StringUtils.rightPad(row4[1].toString(), 20, " ");
									CD_CARDPID = row4[2].toString();

									List<Object[]> dataBinCpid = getIniDepData(CD_CARDPID);
									if (dataBinCpid != null && !dataBinCpid.isEmpty()) {
										for (int t = 0; t < dataBinCpid.size(); t++) {
											Object[] row5 = dataBinCpid.get(t);

											DES_CARDPID = StringUtils.rightPad(row5[2].toString(), 15, " ");
											CD_CARDACE = row5[11].toString();
											CD_ADQ = row5[5].toString();
											LEN_ACNT_NUM = StringUtils.leftPad(row5[9].toString(), 2, "0");
										}
									}

									CONTADOR = CONTADOR + 1;
									SEQ_PRED = StringUtils.leftPad(String.valueOf(CONTADOR), 6, "0");

									BINGEN = "D1" + SEQ_PRED + CD_RANGO + LEN_ACNT_NUM + "A00000000" + CD_CARDACE + "CC"
											+ CD_ADQ + "10010000000" + DES_CARDPID + "020000000" + CD_CARDPID + "\r\n";
									bwrRB.write(BINGEN);

								}
								SEQ_FOO = SEQ_FOO + CTA_BINGEN;
							}

							// FOOTER (COMUN)
							// RECORD ID "T1" CTE. 2 - 2
							// SECUENCIA "000000" CALC. 6 - 8 CANTIDAD DE
							// REGISTROS
							// DE DETALLE + 1
							// ARMADO DE FOOTER
							String LNBIN_FOO = "T1" + StringUtils.leftPad(String.valueOf(SEQ_FOO), 6, "0") + "\r\n";
							bwrRB.write(LNBIN_FOO);
							// FIN FOOTER

							if (bwrRB != null) {
								bwrRB.close();
							}

							try {
								logger.info("Copiado de archivo " + regBinesFile.getName() + "a tienda " + DES_TIENDA
										+ ".");
								File pathTienda = new File(outFolderPart1 + DES_TIENDA + outFolderPart2 + File.separator
										+ regBinesFile.getName());
								Date now2 = new Date();
								String strDate2 = formatterFile.format(now2);
								FilesHelper.copyFile(outFolderSecySub.getAbsolutePath(), bkpFolder.getAbsolutePath(),
										regBinesFile.getName(),
										regBinesFile.getName() + "_" + DES_TIENDA + "_" + strDate2);
								regBinesFile.renameTo(pathTienda);

								File pathTiendaCmd = new File(outFolderPart1 + DES_TIENDA + outFolderPart2);

								FilesHelper.copyFile(outFolderSecySub.getAbsolutePath(),
										pathTiendaCmd.getAbsolutePath(), regBinesFileCmd.getName(),
										regBinesFileCmd.getName());

							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}

						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
//					}
				}

				regBinesFileCmd.delete();

				logger.info("Finaliza proceso de creación de Archivo de Registro de Bines.");

			} catch (Exception e) {
				logger.error("Error durante el proceso de creación de Archivo de Registro de Bines.");
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void crearArchivoOpcionesDePago() {

		logger.info("Inicia proceso de creación de Archivo de Opciones de Pago.");

		List activeStores = storeDAO.getAllActiveStores(sessionSaadmin);

		if (activeStores != null && !activeStores.isEmpty()) {
			try {
				String gOpcPagoFileName = binOpc + ".DAT";
				String gOpcPagoCmdFileName = binOpc + ".CMD";

				File gOpcPagFileCmdDel = new File(outFolderSecySub, gOpcPagoCmdFileName);
				if (gOpcPagFileCmdDel.exists())
					gOpcPagFileCmdDel.delete();

				opcPagoFileCmd = new File(outFolderSecySub, gOpcPagoCmdFileName);
				BufferedWriter bwrcmdOpc = new BufferedWriter(new FileWriter(opcPagoFileCmd, true));
				bwrcmdOpc.write("C:\\ADX_UDT1");
				bwrcmdOpc.newLine();
				if (bwrcmdOpc != null) {
					bwrcmdOpc.close();
				}

				File gOpcPagoFileDel = new File(outFolderSecySub, gOpcPagoFileName);
				if (gOpcPagoFileDel.exists())
					gOpcPagoFileDel.delete();

				opcPagoFile = new File(outFolderSecySub, gOpcPagoFileName);

				Iterator itTienda = activeStores.iterator();

				while (itTienda.hasNext()) {
					Store tiendaAux = (Store) itTienda.next();
//					String codTienda = tiendaAux.getKey();
//					if (checkStoreClose(codTienda)) {

						try {
							if (opcPagoFile.exists())
								opcPagoFile.delete();
							opcPagoFile = new File(outFolderSecySub, gOpcPagoFileName);

							BufferedWriter bwrOpc = new BufferedWriter(new FileWriter(opcPagoFile, true));

							String DES_TIENDA = StringUtils.leftPad(tiendaAux.getKey().toString(), 3, "0");
							String OPC_DIFER = null;
							List<Object[]> dataOpcPag = getOpcPagoData();

							if (dataOpcPag != null && !dataOpcPag.isEmpty()) {

								for (int i = 0; i < dataOpcPag.size(); i++) {
									Object[] row = dataOpcPag.get(i);
									String ID_OPCHDR = row[0].toString();
									String CD_CARDACE = row[1].toString();
									String CARDPLAN = row[2].toString(); // 0=OBTIENE
																			// EL
																			// PREDETERMINADO
									String CD_OPCSLS = StringUtils.leftPad(row[2].toString(), 2, "0");
									String ESHEADER = "000";
									OPC_DIFER = StringUtils.leftPad(row[4].toString(), 2, "0");
									String MESGR_HDR = "00";
									String ID_GRPSEC = StringUtils.leftPad(row[3].toString(), 3, "0");
									String STR_DIFERIDO = "100000000000000000000000000000000000000000000000";

									if (CARDPLAN.equals("0")) {
										// LINEA DE OPCIÓN PREDETERMINADA
										String LN_OPHDR = CD_CARDACE + CD_OPCSLS + ESHEADER + OPC_DIFER + MESGR_HDR
												+ ID_GRPSEC + STR_DIFERIDO + "\r\n";
										bwrOpc.write(LN_OPHDR);
									}
									String REGISTRAR = "0";
									String FEC_INI = row[15].toString();
									String FEC_TER = row[16].toString();
									// logger.info("FEC_INI: " + FEC_INI);
									// logger.info("FEC_TER: " + FEC_TER);
									Date FEC_INI_D = dfcompare.parse(row[15].toString());
									Date FEC_TER_D = dfcompare.parse(row[16].toString());
									Date fechaActual = new Date();
									// logger.info("fechaActual: " +
									// fechaActual.toString());
									if (FEC_INI.isEmpty() && FEC_TER.isEmpty()) {
										// logger.info("ENTRA AL IF");
										REGISTRAR = "1";
									} else {
										// logger.info("ENTRA AL ELSE");
										if (fechaActual.after(FEC_INI_D) && fechaActual.before(FEC_TER_D)) {
											REGISTRAR = "1";
											// logger.info("ENTRA AL IF DEL
											// ELSE");
										}
									}

									// VALIDAR SI SE ENCUENTRA ASOCIADO A UNA
									// TIENDA
									String CTAVAL = "0";
									CTAVAL = String.valueOf(getCountOpcStr(Integer.valueOf(ID_OPCHDR)));

									// CONSULTA PARA ARMADO DE HEADER OPCION DE
									// PAGO
									if (Integer.valueOf(CTAVAL) > 0) {
										List<Object[]> dataPaBinOpcDet = getDataPaBinOpcDet(Integer.valueOf(ID_OPCHDR),
												Integer.valueOf(DES_TIENDA));
										int Diferido_1 = 0;
										int Diferido_2 = 0;
										int Diferido_4 = 0;
										int Diferido_8 = 0;
										int Diferido_16 = 0;
										if (dataPaBinOpcDet != null && !dataPaBinOpcDet.isEmpty()) {

											for (int j = 0; j < dataPaBinOpcDet.size(); j++) {
												Object[] row2 = dataPaBinOpcDet.get(j);
												String ID_OPCDET = row2[0].toString();
												String[] diferidos = { "1", "2", "4", "8", "16" };
												for (int q = 0; q < diferidos.length; q++) {
													// VERIFICAR SI DIF_x <> 0

													int DIF_x = Integer.valueOf(diferidos[q]);
													int DIF = 0;
													if (DIF_x == 1)
														DIF = Integer.valueOf(row2[3].toString());
													else if (DIF_x == 2)
														DIF = Integer.valueOf(row2[4].toString());
													else if (DIF_x == 4)
														DIF = Integer.valueOf(row2[5].toString());
													else if (DIF_x == 8)
														DIF = Integer.valueOf(row2[6].toString());
													else if (DIF_x == 16)
														DIF = Integer.valueOf(row2[7].toString());

													if (DIF != 0) {
														// VALIDAR VIGENCIA DEL
														// DETALLE
//														logger.info("ID_OPCDET: " + ID_OPCDET);
//														logger.info("DIF: " + DIF);
														String INICIO_VIG = null;
														String TERMINO_VIG = null;
														if (DIF_x == 1) {
															INICIO_VIG = row2[14] != null ? row2[14].toString() : null;
															TERMINO_VIG = row2[15] != null ? row2[15].toString() : null;
														} else if (DIF_x == 2) {
															INICIO_VIG = row2[16] != null ? row2[16].toString() : null;
															TERMINO_VIG = row2[17] != null ? row2[17].toString() : null;
														} else if (DIF_x == 4) {
															INICIO_VIG = row2[18] != null ? row2[18].toString() : null;
															TERMINO_VIG = row2[19] != null ? row2[19].toString() : null;
														} else if (DIF_x == 8) {
															INICIO_VIG = row2[20] != null ? row2[20].toString() : null;
															TERMINO_VIG = row2[21] != null ? row2[21].toString() : null;
//															logger.info("INICIO_VIG: " + INICIO_VIG);
//															logger.info("TERMINO_VIG: " + TERMINO_VIG);
														} else if (DIF_x == 16) {
															INICIO_VIG = row2[22] != null ? row2[22].toString() : null;
															TERMINO_VIG = row2[23] != null ? row2[23].toString() : null;
														}
														// String INICIO_VIG =
														// row2[14].toString();
														// //
														// INICIO
														// // VIGENCIA
														// String TERMINO_VIG =
														// row2[15].toString();
														// //
														// TERMINO
														// // VIGENCIA
														int SumaDiferido = 0;

														if (INICIO_VIG == null && TERMINO_VIG == null) { // SIN
																											// VIGENCIA
															SumaDiferido = 1;
														} else {
															// VALIDAR RANGO DE
															// FECHA
															// COMPARAR FECHA DE
															// INICIO IGUAL O
															// MENOR
															// A LA ACTUAL
															Date INICIO_VIG_D = dfcompare.parse(INICIO_VIG);
															Date TERMINO_VIG_D = dfcompare.parse(TERMINO_VIG);
															// FUERA O DENTRO DE
															// RANGO
															if (!fechaActual.after(INICIO_VIG_D)
																	&& !fechaActual.before(TERMINO_VIG_D)) {
																SumaDiferido = 1;
															}
														}

														if (SumaDiferido == 1) {
															if (row2[3].toString().equals("1")) {
																Diferido_1 = 1;
															}
															if (row2[4].toString().equals("2")) {
																Diferido_2 = 2;
															}
															if (row2[5].toString().equals("4")) {
																Diferido_4 = 4;
															}
															if (row2[6].toString().equals("8")) {
																Diferido_8 = 8;
															}
															if (row2[7].toString().equals("16")) {
																Diferido_16 = 16;
															}
														}

													}

												}
											}
										}

										int OPC_DIFER_int = Diferido_1 + Diferido_2 + Diferido_4 + Diferido_8
												+ Diferido_16;
										OPC_DIFER = StringUtils.leftPad(String.valueOf(OPC_DIFER_int), 2, "0");

										// ARMADO DE LINEA DE REGISTRO HEADER
										// OPCIOND E PAGO
										String LN_OPHDR = CD_CARDACE + CD_OPCSLS + ESHEADER + OPC_DIFER + MESGR_HDR
												+ ID_GRPSEC + STR_DIFERIDO + "\r\n";

										bwrOpc.write(LN_OPHDR);

									}

									// ARMADO DE DETALLE DE LA OPCION DE PAGO
									if (Integer.valueOf(REGISTRAR) == 1 && Integer.valueOf(CTAVAL) > 0) {

										// DETALLE PA_BIN_OPCDET
										// CARD PLAN ID 2 "CR"
										// PA_BIN_OPCHDR.ID_OPCHDR -> CD_CARDPID
										// VARIEDAD 2 "00"
										// PA_BIN_OPCHDR.ID_OPCHDR
										// -> CD_OPCSLS
										// ID DEL DIFERIDO (CUOTA) 2 "00"
										// NUM_DIFER
										// FLAG CON/SIN INTERES/PLUS 1 "0/1/2"
										// MESES DE GRACIA 2 "00" 01:CON, 00:SIN
										// MONTO MINIMO VENTA 6 "000000"
										// MNT_MIN_SLS
										// 9-14
										// IDGRUPOSECCION 3 "000" ID_GRPSEC
										// FILLER 44
										// "00000000000000000000000000000000000000000000"
										// 15-61
										// TOTAL= 62
										// CONSULTA DETALLE OPCION DE PAGO

										List<Object[]> dataDetalleOpcPag = getDetalleOpcPagoData(
												Integer.valueOf(ID_OPCHDR), Integer.valueOf(DES_TIENDA));

										if (dataDetalleOpcPag != null && !dataDetalleOpcPag.isEmpty()) {

											for (int t = 0; t < dataDetalleOpcPag.size(); t++) {
												Object[] row3 = dataDetalleOpcPag.get(t);
												String ID_OPCDET = row3[0].toString();
												String NUM_DIFER = row3[2].toString();
												// TRABAJAR CON EL ARREGLO DE
												// DIFS

												String[] diferidos = { "1", "2", "4", "8", "16" };
												for (int s = 0; s < diferidos.length; s++) {
													// VERIFICAR SI DIF_x <> 0

													int DIF_x = Integer.valueOf(diferidos[s]);
													int DIF = 0;

													if (DIF_x == 1)
														DIF = Integer.valueOf(row3[3].toString());
													else if (DIF_x == 2)
														DIF = Integer.valueOf(row3[4].toString());
													else if (DIF_x == 4)
														DIF = Integer.valueOf(row3[5].toString());
													else if (DIF_x == 8)
														DIF = Integer.valueOf(row3[6].toString());
													else if (DIF_x == 16)
														DIF = Integer.valueOf(row3[7].toString());

													if (DIF != 0) {
														// VALIDAR VIGENCIA DEL
														// DETALLE
														String INICIO_VIG = null;
														String TERMINO_VIG = null;
														if (DIF_x == 1) {
															INICIO_VIG = row3[14] != null ? row3[14].toString() : null;
															TERMINO_VIG = row3[15] != null ? row3[15].toString() : null;
														} else if (DIF_x == 2) {
															INICIO_VIG = row3[16] != null ? row3[16].toString() : null;
															TERMINO_VIG = row3[17] != null ? row3[17].toString() : null;
														} else if (DIF_x == 4) {
															INICIO_VIG = row3[18] != null ? row3[18].toString() : null;
															TERMINO_VIG = row3[19] != null ? row3[19].toString() : null;
														} else if (DIF_x == 8) {
															INICIO_VIG = row3[20] != null ? row3[20].toString() : null;
															TERMINO_VIG = row3[21] != null ? row3[21].toString() : null;
														} else if (DIF_x == 16) {
															INICIO_VIG = row3[22] != null ? row3[22].toString() : null;
															TERMINO_VIG = row3[23] != null ? row3[23].toString() : null;
														}

														int REGDETALLE = 0;

														if (INICIO_VIG == null && TERMINO_VIG == null) { // SIN
																											// VIGENCIA
															REGDETALLE = 1;
														} else {
															// VALIDAR RANGO DE
															// FECHA
															// COMPARAR FECHA DE
															// INICIO IGUAL O
															// MENOR
															// A LA ACTUAL
															Date INICIO_VIG_D = dfcompare.parse(INICIO_VIG);
															Date TERMINO_VIG_D = dfcompare.parse(TERMINO_VIG);
															// FUERA O DENTRO DE
															// RANGO
															if (!fechaActual.after(INICIO_VIG_D)
																	&& !fechaActual.before(TERMINO_VIG_D)) {
																REGDETALLE = 1;
															}
														}
														if (REGDETALLE == 1) {
															// CONSTRUIR LINEA
															// DE
															// DETALLE
															NUM_DIFER = StringUtils.leftPad(row3[2].toString(), 2, "0");
															String CodeDiferido = null;

															if (DIF_x == 1) {
																CodeDiferido = "000";
															}
															if (DIF_x == 2) {
																CodeDiferido = "100";
															}
															if (DIF_x == 4) {
																CodeDiferido = "001";
															}
															if (DIF_x == 8) {
																CodeDiferido = "101";
															}
															if (DIF_x == 16) {
																CodeDiferido = "200";
															}

															String RestricDiferido = null;
															String MontoDiferido = null;

															if (DIF_x == 1) {
																RestricDiferido = StringUtils
																		.leftPad(row3[9].toString(), 3, "0");
																MontoDiferido = StringUtils.leftPad(row3[24].toString(),
																		6, "0");
															} else if (DIF_x == 2) {
																RestricDiferido = StringUtils
																		.leftPad(row3[10].toString(), 3, "0");
																MontoDiferido = StringUtils.leftPad(row3[25].toString(),
																		6, "0");
															} else if (DIF_x == 4) {
																RestricDiferido = StringUtils
																		.leftPad(row3[11].toString(), 3, "0");
																MontoDiferido = StringUtils.leftPad(row3[26].toString(),
																		6, "0");
															} else if (DIF_x == 8) {
																RestricDiferido = StringUtils
																		.leftPad(row3[12].toString(), 3, "0");
																MontoDiferido = StringUtils.leftPad(row3[27].toString(),
																		6, "0");
															} else if (DIF_x == 16) {
																RestricDiferido = StringUtils
																		.leftPad(row3[13].toString(), 3, "0");
																MontoDiferido = StringUtils.leftPad(row3[28].toString(),
																		6, "0");
															}

															String FILLER_DET = "00000000000000000000000000000000000000000000";
															// ARMADO DE LINEA
															// DE
															// DETALLE OPCION DE
															// PAGO
															String LN_DETHDR = CD_CARDACE + CD_OPCSLS + NUM_DIFER
																	+ CodeDiferido + MontoDiferido + RestricDiferido
																	+ FILLER_DET + "\r\n";
															// CONCATENA LÍNEA
															// DE
															// HEADER CON
															// DETALLE
															// $LN_OPHDR=$LN_OPHDR.$LN_DETHDR;
															bwrOpc.write(LN_DETHDR);
														}
													}

												}

											}
										}

									}

								}

							}

							if (bwrOpc != null) {
								bwrOpc.close();
							}

							try {
								logger.info(
										"Copiado de archivo " + opcPagoFile.getName() + "a tienda " + DES_TIENDA + ".");
								File pathTienda = new File(outFolderPart1 + DES_TIENDA + outFolderPart2 + File.separator
										+ opcPagoFile.getName());
								Date now2 = new Date();
								String strDate2 = formatterFile.format(now2);
								FilesHelper.copyFile(outFolderSecySub.getAbsolutePath(), bkpFolder.getAbsolutePath(),
										opcPagoFile.getName(),
										opcPagoFile.getName() + "_" + DES_TIENDA + "_" + strDate2);
								opcPagoFile.renameTo(pathTienda);

								File pathTiendaCmd = new File(outFolderPart1 + DES_TIENDA + outFolderPart2);
								FilesHelper.copyFile(outFolderSecySub.getAbsolutePath(),
										pathTiendaCmd.getAbsolutePath(), opcPagoFileCmd.getName(),
										opcPagoFileCmd.getName());

							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}

						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
//					}

				}

				opcPagoFileCmd.delete();

				logger.info("Finaliza proceso de creación de Archivo de Opciones de Pago.");

			} catch (Exception e) {
				logger.error("Error durante el proceso de creación de Archivo de Opciones de Pago.");
				logger.error(e.getMessage(), e);
			}
		}
	}

	private List<Object[]> getDataPaBinGrpsec() {
		try {
			SQLQuery query = sessionArts
					.createSQLQuery("SELECT * FROM PA_BIN_GRPSEC WHERE ESTADO=1 ORDER BY ID_GRPSEC ASC");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getDataPaBinSec(String idGrpSec) {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT * FROM PA_BIN_SECC WHERE ID_GRPSEC='" + idGrpSec + "' ORDER BY ID_DPT_PS ASC");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public Integer getCountBines() {
		try {

			SQLQuery query = sessionArts.createSQLQuery("SELECT COUNT(ID_BIN) AS NUMREGS FROM PA_BIN WHERE EST_BIN=1");
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	public Integer getCountRangos() {
		try {

			SQLQuery query = sessionArts
					.createSQLQuery("SELECT COUNT(ID_RANGO) AS NUMREGS FROM PA_BIN_RANK WHERE IND_ACTIVO=1");
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	private List<Object[]> getPaBinData() {
		try {
			SQLQuery query = sessionArts
					.createSQLQuery("SELECT * FROM PA_BIN WHERE EST_BIN=1 ORDER BY CD_CARDACE ASC, CD_BIN ASC");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getIniDepData(String cardpid) {
		try {
			SQLQuery query = sessionArts.createSQLQuery("SELECT * FROM PA_BIN_CPID WHERE CD_CARDPID='" + cardpid + "'");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public Integer getOpTiendaData(String DES_TIENDA, String CD_CARDPID) {
		try {

			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT TOP 1 S.ID_OPCHDR FROM PA_BIN_OPCSTR S, PA_BIN_OPCCPID C WHERE S.ID_OPCHDR = C.ID_OPCHDR AND CD_STR ='"
							+ DES_TIENDA + "' AND CD_CARDPID='" + CD_CARDPID + "' ORDER BY PRIORITY DESC");
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	private List<Object[]> getDataBinCer(Integer ID_OPCHDR, Integer CD_BIN) {
		try {
			SQLQuery query = sessionArts
					.createSQLQuery("SELECT * FROM PA_BIN_OPCBIN WHERE ID_OPCHDR=" + ID_OPCHDR + " AND CD_BIN=" + CD_BIN
							+ " AND ID_OPCHDR IN(SELECT ID_OPCHDR FROM PA_BIN_OPCHDR WHERE PRIORITY>0)");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getDataBinTarjBanc(Integer ID_OPCHDR) {
		try {
			SQLQuery query = sessionArts.createSQLQuery("SELECT * FROM PA_BIN_OPCHDR WHERE ID_OPCHDR=" + ID_OPCHDR);
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getDataBinGen() {
		try {
			SQLQuery query = sessionArts
					.createSQLQuery("SELECT * FROM PA_BIN_RANK WHERE IND_ACTIVO=1 ORDER BY CD_RANGO DESC");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getOpcPagoData() {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT * FROM PA_BIN_OPCHDR WHERE IND_ACTIVO=1 ORDER BY CD_CARDACE ASC, CD_OPCSLS ASC");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private Integer getCountOpcStr(Integer ID_OPCHDR) {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT COUNT(ID_OPCHDR) AS CTAVAL FROM PA_BIN_OPCSTR WHERE ID_OPCHDR=" + ID_OPCHDR);
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	private List<Object[]> getDataPaBinOpcDet(Integer ID_OPCHDR, Integer DES_TIENDA) {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT D.ID_OPCDET,D.ID_OPCHDR ,D.NUM_DIFER ,D.DIF_1 ,D.DIF_2 ,D.DIF_4 ,D.DIF_8 ,D.DIF_16 ,D.MNT_MIN_SLS ,D.RS_DIF_1 ,D.RS_DIF_2 ,D.RS_DIF_4 ,D.RS_DIF_8 ,D.RS_DIF_16 ,D.FI_DIF_1 ,D.FT_DIF_1 ,D.FI_DIF_2 ,D.FT_DIF_2 ,D.FI_DIF_4 ,D.FT_DIF_4 ,D.FI_DIF_8 ,D.FT_DIF_8 ,D.FI_DIF_16 ,D.FT_DIF_16 ,D.MNT_DIF_1, D.MNT_DIF_2 ,D.MNT_DIF_4 ,D.MNT_DIF_8 ,D.MNT_DIF_16  FROM PA_BIN_OPCDET D, PA_BIN_OPCSTR S WHERE D.ID_OPCHDR="
							+ ID_OPCHDR + " AND D.ID_OPCDET=S.ID_OPCDET AND S.CD_STR=" + DES_TIENDA);
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getDetalleOpcPagoData(Integer ID_OPCHDR, Integer DES_TIENDA) {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT D.ID_OPCDET,D.ID_OPCHDR ,D.NUM_DIFER ,D.DIF_1 ,D.DIF_2 ,D.DIF_4 ,D.DIF_8 ,D.DIF_16 ,D.MNT_MIN_SLS ,D.RS_DIF_1 ,D.RS_DIF_2 ,D.RS_DIF_4 ,D.RS_DIF_8 ,D.RS_DIF_16 ,D.FI_DIF_1 ,D.FT_DIF_1 ,D.FI_DIF_2 ,D.FT_DIF_2 ,D.FI_DIF_4 ,D.FT_DIF_4 ,D.FI_DIF_8 ,D.FT_DIF_8 ,D.FI_DIF_16 ,D.FT_DIF_16 ,D.MNT_DIF_1, D.MNT_DIF_2 ,D.MNT_DIF_4 ,D.MNT_DIF_8 ,D.MNT_DIF_16 FROM PA_BIN_OPCDET D, PA_BIN_OPCSTR S WHERE D.ID_OPCHDR="
							+ ID_OPCHDR + " AND D.ID_OPCDET=S.ID_OPCDET AND S.CD_STR=" + DES_TIENDA
							+ " ORDER BY D.NUM_DIFER ASC");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected Session iniciarSesionSaadmin() {

		Session sessionSaadmin = null;

		while (sessionSaadmin == null) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionSaadmin == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}

		return sessionSaadmin;
	}

	protected Session iniciarSesionArts() {

		Session sessionArts = null;

		while (sessionArts == null) {
			try {
				sessionArts = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionArts == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}

		return sessionArts;
	}

	public boolean checkStoreClose(String codTienda) {
		
		logger.info("Verificación tienda cerrrada: " + codTienda);

		int bsnUnit = getIdBsnUnit(Integer.valueOf(codTienda));
		
		//logger.info("BSN UNIT: " + bsnUnit);

		if (bsnUnit > 0) {
			int maxIdTrn = getMaxIdTrn(bsnUnit);
			//logger.info("MAX ID_TRN: " + maxIdTrn);
			int maxIdTrnBsnUnit = getNextMaxIdTrn(bsnUnit, maxIdTrn);
			//logger.info("maxIdTrnBsnUnit: " + maxIdTrnBsnUnit);
			if (maxIdTrnBsnUnit == 0){
				//logger.info("RETURN: true");
				return true;
			}
		}
		//logger.info("RETURN: false");
		return false;
	}

	public Integer getIdBsnUnit(Integer codTienda) {
		try {

			SQLQuery query = sessionArts
					.createSQLQuery("SELECT ID_BSN_UN FROM PA_STR_RTL WHERE CD_STR_RT = " + codTienda);
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	public Integer getMaxIdTrn(Integer bsnUnit) {
		try {

			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT case  when MAX (TR_BSN_EOD.ID_TRN) IS NULL then 0 else MAX (TR_BSN_EOD.ID_TRN) end AS ID_TRN FROM TR_BSN_EOD WHERE ID_TRN IN(SELECT ID_TRN FROM TR_TRN WHERE ID_BSN_UN = "
							+ bsnUnit + ")");
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	public Integer getNextMaxIdTrn(Integer bsnUnit, Integer maxIdTrn) {
		try {

			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT ID_TRN FROM TR_TRN WHERE ID_TRN > " + maxIdTrn + " AND ID_BSN_UN = " + bsnUnit);
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		logger.info("Deteniendo ExportBinesFilesProcess...");
		while (!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					return false;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		timerBines.cancel();
		logger.info("Finalizó el Proceso de Exportacion de Bines.");
		return true;
	}

}
