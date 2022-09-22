USE [EYES_EC]
GO
SET IDENTITY_INSERT [dbo].[FM_TIP_ESTADO] ON 

INSERT [dbo].[FM_TIP_ESTADO] ([ID_TIP_ESTADO], [VAL_TIP_ESTADO], [DES_TIP_ESTADO], [ABR_TIP_ESTADO], [BGCOLOR_TIP_ESTADO], [CSSFONT_TIP_ESTADO], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (1, 20, N'Proceso Iniciado', N'STR', N'#060', N'color:#FFF;text-shadow: 0px -1px 0px rgba(0, 0, 0, 0.75);', 0, CAST(N'2013-05-30 00:00:00.000' AS DateTime))
INSERT [dbo].[FM_TIP_ESTADO] ([ID_TIP_ESTADO], [VAL_TIP_ESTADO], [DES_TIP_ESTADO], [ABR_TIP_ESTADO], [BGCOLOR_TIP_ESTADO], [CSSFONT_TIP_ESTADO], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (2, 10, N'Proceso Finalizado', N'END', N'#F1F1F1', N'color:#000;text-shadow: 0px 1px 0px rgba(255, 255, 255, 0.75);', 0, CAST(N'2013-05-30 00:00:00.000' AS DateTime))
INSERT [dbo].[FM_TIP_ESTADO] ([ID_TIP_ESTADO], [VAL_TIP_ESTADO], [DES_TIP_ESTADO], [ABR_TIP_ESTADO], [BGCOLOR_TIP_ESTADO], [CSSFONT_TIP_ESTADO], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (3, 30, N'Proceso Finalizado con Alerta', N'WAR', N'#FFCC33', N'color:#111;text-shadow: 0px 1px 0px rgba(255, 255, 255, 0.75);', 0, CAST(N'2013-05-29 00:00:00.000' AS DateTime))
INSERT [dbo].[FM_TIP_ESTADO] ([ID_TIP_ESTADO], [VAL_TIP_ESTADO], [DES_TIP_ESTADO], [ABR_TIP_ESTADO], [BGCOLOR_TIP_ESTADO], [CSSFONT_TIP_ESTADO], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (4, 40, N'Proceso Finalizado con Error', N'ERR', N'#C00', N'color:#FFF;text-shadow: 0px -1px 0px rgba(0, 0, 0, 0.75);', 0, CAST(N'2013-05-29 00:00:00.000' AS DateTime))
INSERT [dbo].[FM_TIP_ESTADO] ([ID_TIP_ESTADO], [VAL_TIP_ESTADO], [DES_TIP_ESTADO], [ABR_TIP_ESTADO], [BGCOLOR_TIP_ESTADO], [CSSFONT_TIP_ESTADO], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (5, 50, N'en Curso', N'PRC', N'#2962FF', N'color:#FFF', 0, CAST(N'2015-10-27 00:00:00.000' AS DateTime))
SET IDENTITY_INSERT [dbo].[FM_TIP_ESTADO] OFF
SET IDENTITY_INSERT [dbo].[FM_TIP_SEVER] ON 

INSERT [dbo].[FM_TIP_SEVER] ([ID_TIP_SEVER], [VAL_TIP_SEVER], [DES_TIP_SEVER], [ABR_TIP_SEVER], [BGCOLOR_TIP_SEVER], [CSSFONT_TIP_SEVER], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (1, 1, N'Severidad 1', N'1', N'#F44336', N'color:#FFF', 0, CAST(N'2015-10-29 00:00:00.000' AS DateTime))
INSERT [dbo].[FM_TIP_SEVER] ([ID_TIP_SEVER], [VAL_TIP_SEVER], [DES_TIP_SEVER], [ABR_TIP_SEVER], [BGCOLOR_TIP_SEVER], [CSSFONT_TIP_SEVER], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (2, 2, N'Severidad 2', N'2', N'#EF6C00', N'color:#FFF', 0, CAST(N'2015-10-29 00:00:00.000' AS DateTime))
INSERT [dbo].[FM_TIP_SEVER] ([ID_TIP_SEVER], [VAL_TIP_SEVER], [DES_TIP_SEVER], [ABR_TIP_SEVER], [BGCOLOR_TIP_SEVER], [CSSFONT_TIP_SEVER], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (3, 3, N'Severidad 3', N'3', N'#FFC400', N'color:#333', 0, CAST(N'2015-10-29 00:00:00.000' AS DateTime))
INSERT [dbo].[FM_TIP_SEVER] ([ID_TIP_SEVER], [VAL_TIP_SEVER], [DES_TIP_SEVER], [ABR_TIP_SEVER], [BGCOLOR_TIP_SEVER], [CSSFONT_TIP_SEVER], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (4, 4, N'Severidad 4', N'4', N'#2962FF', N'color:#FFF', 0, CAST(N'2015-10-29 00:00:00.000' AS DateTime))
INSERT [dbo].[FM_TIP_SEVER] ([ID_TIP_SEVER], [VAL_TIP_SEVER], [DES_TIP_SEVER], [ABR_TIP_SEVER], [BGCOLOR_TIP_SEVER], [CSSFONT_TIP_SEVER], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (5, 5, N'Severidad 5', N'5', N'#00695C', N'color:#FFF', 0, CAST(N'2015-10-29 00:00:00.000' AS DateTime))
SET IDENTITY_INSERT [dbo].[FM_TIP_SEVER] OFF
SET IDENTITY_INSERT [dbo].[FM_TIPO_EQUIPO] ON 

INSERT [dbo].[FM_TIPO_EQUIPO] ([ID_TIPO], [COD_TIPO], [DES_TIPO], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (1, N'CNT', N'CONTROLADOR', 0, CAST(N'2013-04-22 00:00:00.000' AS DateTime))
INSERT [dbo].[FM_TIPO_EQUIPO] ([ID_TIPO], [COD_TIPO], [DES_TIPO], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (2, N'POS', N'POS', 0, CAST(N'2013-04-23 00:00:00.000' AS DateTime))
INSERT [dbo].[FM_TIPO_EQUIPO] ([ID_TIPO], [COD_TIPO], [DES_TIPO], [COD_USUARIO], [FEC_ACTUALIZACION]) VALUES (3, N'SRV', N'SVR.', 0, CAST(N'2015-08-24 00:00:00.000' AS DateTime))
SET IDENTITY_INSERT [dbo].[FM_TIPO_EQUIPO] OFF
SET IDENTITY_INSERT [dbo].[FP_PROCESO] ON 

INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (4, N'CONS_CPR_O', N'Consulta Cedula, Padron o Ruc', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Con.CedPadRuc.(O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (5, N'CONS_CURE_O', N'Consulta Cupon Redimible', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Con.CpnRed.(O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (6, N'CONS_CUST_O', N'Consulta Customer', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Con.Cust.(O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (7, N'TSL_READ_O', N'Lectura de TSL', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Lec.TSL(O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (8, N'CONS_DEV_O', N'Consulta Devolucion', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Con.Dev.(O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (9, N'UPD_DEV_O', N'Actualizacion de Devolucion', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Act.Dev.(O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (10, N'UPD_ITEM_O', N'Actualizacion de  Items', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Act.Item.(O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (11, N'UPD_MOTO_O', N'Actualizacion de Motos', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Act.Moto.(O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (12, N'CONS_RET_O', N'Consulta Factura para Retencion', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Con.FactRet.(O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (13, N'GEN_SUTRX_O', N'Generador de Transacciones Suspendidas', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Gen.TrxSusp.(O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (14, N'OPER_UPD_P', N'Actualizacion de Operador', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Act.Opera.(P)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (15, N'UPD_CPR_P', N'Actualizacion de Cedula, Padron o Ruc', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Act.CedPadRuc.(P)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (16, N'ACT_GFC_P', N'Activacion de Giftcard', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Act.Gift.(P)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (17, N'SRCH_ITEM_P', N'Busqueda de Archivos Item', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Bsc.Item.(P)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (18, N'SRCH_MOTO_P', N'Busqueda de Archivos Moto', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Bsc.Moto.(P)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (19, N'SRCH_PRNT_P', N'Busqueda de Archivos de Impreson', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Bsc.ArcImpr.(P)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (20, N'OPER_UPD_O', N'Actualizacion de Operador', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Act.Opera (O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (21, N'TSL_READ_P', N'Lectura de TSL', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Lec.TSL.(P)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (22, N'UPD_ITEM_P', N'Actualizacion de Items', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Act.Item.(P)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (23, N'UPD_MOTO_P', N'Actualizacion de Motos', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Act.Moto.(P)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (24, N'GEN_SUTRX_P', N'Generador de Transacciones Suspendidas', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Gen.TrxSusp.(P)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (25, N'CONS_CUDA_O', N'Consulta Customer Data', 0, CAST(N'2015-08-26 00:00:00.000' AS DateTime), N'Con.CustData.(O)')
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (92744, N'CH_ITEM_P', NULL, 0, CAST(N'2015-12-04 00:00:00.000' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117078, N'L_READ_P', NULL, 0, CAST(N'2015-12-10 00:00:00.000' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117079, N'LOAD_PARAMS_P', NULL, 0, CAST(N'2016-11-02 11:58:55.603' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117080, N'STR_STS_P', NULL, 0, CAST(N'2016-11-02 11:58:57.430' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117081, N'ACC_TOT_P', NULL, 0, CAST(N'2016-11-02 11:58:59.430' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117082, N'LOAD_PARAMS_O', NULL, 0, CAST(N'2016-11-02 12:00:20.710' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117083, N'FILE_UPD_U_P', NULL, 0, CAST(N'2016-11-02 13:30:43.873' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117084, N'P', NULL, 0, CAST(N'2016-11-02 13:37:48.080' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117085, N'SYS_READ_P', NULL, 0, CAST(N'2016-11-02 13:47:27.707' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117086, N'EL_READ_P', NULL, 0, CAST(N'2016-11-02 13:47:39.893' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117087, N'UPD_EBIL_O', NULL, 0, CAST(N'2016-11-02 18:59:43.607' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117088, N'CONS_SQ_SYS_TND', NULL, 0, CAST(N'2016-11-02 19:08:51.280' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117091, N'OPER_SUP_UPD_O', NULL, 0, CAST(N'2016-11-02 20:18:40.907' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117092, N'UPD_CODOPERA_P', NULL, 0, CAST(N'2016-11-02 20:18:48.547' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117093, N'CONS_PINP_OP', NULL, 0, CAST(N'2016-11-02 20:25:21.187' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117094, N'CONS_SOC_O', NULL, 0, CAST(N'2016-11-02 21:16:25.477' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117134, N'UPD_CODOPERA_O', NULL, 0, CAST(N'2016-11-04 19:51:25.257' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117135, N'OPER_SUP_UPD_P', NULL, 0, CAST(N'2016-11-04 19:51:46.617' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117142, N'SRCH_EBIL_P', NULL, 0, CAST(N'2016-11-06 17:07:09.577' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117147, N'REG_MSG_SYS_O', NULL, 0, CAST(N'2016-11-07 14:07:41.627' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117153, N'LE_UPD_U_P', NULL, 0, CAST(N'2016-11-07 17:44:03.543' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117170, N'FILE_UPD_D_P', NULL, 0, CAST(N'2016-11-08 03:50:43.020' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117262, N'SEND_CLS_PP_O', NULL, 0, CAST(N'2016-11-09 17:27:00.360' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117285, N'_P', NULL, 0, CAST(N'2016-11-10 12:18:45.527' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117288, N'CN_MSG_SYS_O', NULL, 0, CAST(N'2016-11-10 12:34:54.327' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117432, N'CONS_MOTO_O', NULL, 0, CAST(N'2016-11-15 11:45:58.743' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117492, N'U_P', NULL, 0, CAST(N'2016-11-16 19:17:34.467' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117550, N'UPD_FILE_O', NULL, 0, CAST(N'2016-11-18 11:20:27.257' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (117703, N'_UPD_U_P', NULL, 0, CAST(N'2016-11-23 17:59:37.437' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118013, N'ILE_UPD_U_P', NULL, 0, CAST(N'2016-11-26 14:56:32.203' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118044, N'INIDIA_P', NULL, 0, CAST(N'2016-11-27 12:23:53.270' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118077, N'TRXSNCR_P', NULL, 0, CAST(N'2016-11-28 00:10:45.473' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118090, N'_U_P', NULL, 0, CAST(N'2016-11-28 20:00:11.783' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118165, N'CONS_SQ_ADQ_TND', NULL, 0, CAST(N'2016-11-30 19:15:24.683' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118173, N'UPD_SECSUBSEC_P', NULL, 0, CAST(N'2016-12-01 08:42:28.560' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118174, N'UPD_U_P', NULL, 0, CAST(N'2016-12-01 09:34:49.813' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118188, N'RCV_RSP_SYS_O', NULL, 0, CAST(N'2016-12-02 03:59:37.880' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118189, N'STR_STS_UPD_O', NULL, 0, CAST(N'2016-12-02 05:22:56.987' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118201, N'RVS_CON_P', NULL, 0, CAST(N'2016-12-03 04:42:34.827' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118218, N'RCV_PTOS_O', NULL, 0, CAST(N'2016-12-04 12:46:05.977' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118261, N'FILE_SEND_O', NULL, 0, CAST(N'2016-12-07 02:32:06.030' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118425, N'REG_TR_FRM_POS_P', NULL, 0, CAST(N'2017-01-09 16:25:34.990' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118426, N'AD_PARAMS_P', NULL, 0, CAST(N'2017-01-09 17:16:48.817' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118486, N'SL_READ_P', NULL, 0, CAST(N'2017-01-16 13:04:07.433' AS DateTime), NULL)
INSERT [dbo].[FP_PROCESO] ([ID_PROCESO], [DES_CLAVE], [DES_PROCESO], [COD_USUARIO], [FEC_ACTUALIZACION], [ABR_PROCESO]) VALUES (118515, N'MS_P', NULL, 0, CAST(N'2017-01-20 13:57:15.287' AS DateTime), NULL)
SET IDENTITY_INSERT [dbo].[FP_PROCESO] OFF
;