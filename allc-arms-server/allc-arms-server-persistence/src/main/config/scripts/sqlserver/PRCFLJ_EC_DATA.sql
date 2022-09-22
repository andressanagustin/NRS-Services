USE [PRCFLJ_EC]
GO
SET IDENTITY_INSERT [dbo].[EST_PRC] ON 

INSERT [dbo].[EST_PRC] ([ID_ESTPRC], [NOM_ESTPRC], [COL_ESTPRC], [CSF_ESTADO]) VALUES (0, N'EN RECEPCION', N'#C8C8C8', N'color:#333')
INSERT [dbo].[EST_PRC] ([ID_ESTPRC], [NOM_ESTPRC], [COL_ESTPRC], [CSF_ESTADO]) VALUES (1, N'EXCEPCIONES', N'#FFB300', N'color:#FFF')
INSERT [dbo].[EST_PRC] ([ID_ESTPRC], [NOM_ESTPRC], [COL_ESTPRC], [CSF_ESTADO]) VALUES (2, N'PROCESAR', N'#7A2A9C', N'color:#FFF')
INSERT [dbo].[EST_PRC] ([ID_ESTPRC], [NOM_ESTPRC], [COL_ESTPRC], [CSF_ESTADO]) VALUES (3, N'RECHAZADO', N'#F44336', N'color:#FFF')
INSERT [dbo].[EST_PRC] ([ID_ESTPRC], [NOM_ESTPRC], [COL_ESTPRC], [CSF_ESTADO]) VALUES (4, N'GENERAR FLEJES', N'#8BC34A', N'color:#FFF')
INSERT [dbo].[EST_PRC] ([ID_ESTPRC], [NOM_ESTPRC], [COL_ESTPRC], [CSF_ESTADO]) VALUES (5, N'IMPRESION', N'#689F38', N'color:#FFF')
INSERT [dbo].[EST_PRC] ([ID_ESTPRC], [NOM_ESTPRC], [COL_ESTPRC], [CSF_ESTADO]) VALUES (6, N'ACTIVAR PRECIOS', N'#33691E', N'color:#FFF')
INSERT [dbo].[EST_PRC] ([ID_ESTPRC], [NOM_ESTPRC], [COL_ESTPRC], [CSF_ESTADO]) VALUES (7, N'EN PROCESO', N'#EC407A', N'color:#FFF')
INSERT [dbo].[EST_PRC] ([ID_ESTPRC], [NOM_ESTPRC], [COL_ESTPRC], [CSF_ESTADO]) VALUES (8, N'PROCESADO', N'#C8C8C8', N'color:#FFF')
SET IDENTITY_INSERT [dbo].[EST_PRC] OFF
SET IDENTITY_INSERT [dbo].[FLJ_TIPO] ON 

INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (1, N'INDIVIDUAL', 1, N'I')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (2, N'DE ROPA', 1, N'R')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (3, N'MAYORISTA', 0, N'M')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (4, N'PERCHA', 1, N'P')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (5, N'DIFERIDO', 1, N'D')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (6, N'CAJA', 1, N'V')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (7, N'HABLADOR', 1, N'H')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (8, N'GRANDE PERCHA', 0, N'G')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (9, N'REDONDA PEQUEÃ‘A', 0, N'D')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (10, N'INVERSA CAJA', 0, N'N')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (11, N'MAYORISTA CAJA', 1, N'Z')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (12, N'MAYORISTA UNIDAD', 1, N'U')
INSERT [dbo].[FLJ_TIPO] ([ID_FTIPO], [NOM_FTIPO], [EST_FTIPO], [COD_FTIPO]) VALUES (13, N'ETIQUETA CAJA', 0, N'C')
SET IDENTITY_INSERT [dbo].[FLJ_TIPO] OFF

-- REGISTROS, SE PUEDEN PREDETERMINAR EN SUITE LOCAL (SUITE CENTRAL BUSCA NUMERO DE CUOTA PARA ACTUALIZACIÓN) --
SET IDENTITY_INSERT [dbo].[FLJ_DIF] ON 
INSERT [dbo].[FLJ_DIF] ([ID_FLJDIF], [CD_FLJDIF], [TX_FLJDIF], [ID_REG], [FECHA]) VALUES (1, 3, 268, 0, CAST(N'2017-06-14 08:24:24.947' AS DateTime))
INSERT [dbo].[FLJ_DIF] ([ID_FLJDIF], [CD_FLJDIF], [TX_FLJDIF], [ID_REG], [FECHA]) VALUES (2, 6, 473, 0, CAST(N'2017-06-14 08:32:25.277' AS DateTime))
INSERT [dbo].[FLJ_DIF] ([ID_FLJDIF], [CD_FLJDIF], [TX_FLJDIF], [ID_REG], [FECHA]) VALUES (3, 9, 681, 0, CAST(N'2017-06-14 08:32:37.480' AS DateTime))
INSERT [dbo].[FLJ_DIF] ([ID_FLJDIF], [CD_FLJDIF], [TX_FLJDIF], [ID_REG], [FECHA]) VALUES (4, 12, 891, 0, CAST(N'2017-06-14 08:32:48.793' AS DateTime))
INSERT [dbo].[FLJ_DIF] ([ID_FLJDIF], [CD_FLJDIF], [TX_FLJDIF], [ID_REG], [FECHA]) VALUES (5, 15, 1103, 0, CAST(N'2017-06-14 08:33:00.293' AS DateTime))
INSERT [dbo].[FLJ_DIF] ([ID_FLJDIF], [CD_FLJDIF], [TX_FLJDIF], [ID_REG], [FECHA]) VALUES (6, 18, 1319, 0, CAST(N'2017-06-14 08:33:13.463' AS DateTime))
INSERT [dbo].[FLJ_DIF] ([ID_FLJDIF], [CD_FLJDIF], [TX_FLJDIF], [ID_REG], [FECHA]) VALUES (7, 24, 1758, 0, CAST(N'2017-06-14 08:33:24.480' AS DateTime))
SET IDENTITY_INSERT [dbo].[FLJ_DIF] OFF

;