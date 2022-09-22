USE [RACE_EC]
GO
SET IDENTITY_INSERT [dbo].[RA_NVL1] ON 

INSERT [dbo].[RA_NVL1] ([COD_NVL1], [DES_ES], [DES_EN], [COD_ACE]) VALUES (1, N'Informes de contabilidad', NULL, 0)
INSERT [dbo].[RA_NVL1] ([COD_NVL1], [DES_ES], [DES_EN], [COD_ACE]) VALUES (2, N'Informes de ventas', NULL, 0)
INSERT [dbo].[RA_NVL1] ([COD_NVL1], [DES_ES], [DES_EN], [COD_ACE]) VALUES (3, N'Informes de listas y anotaciones', NULL, 0)
INSERT [dbo].[RA_NVL1] ([COD_NVL1], [DES_ES], [DES_EN], [COD_ACE]) VALUES (4, N'Informes de rendimiento', NULL, 0)
INSERT [dbo].[RA_NVL1] ([COD_NVL1], [DES_ES], [DES_EN], [COD_ACE]) VALUES (5, N'Informes de mantenimiento de datos', NULL, 0)
INSERT [dbo].[RA_NVL1] ([COD_NVL1], [DES_ES], [DES_EN], [COD_ACE]) VALUES (6, N'Informes de seguridad', NULL, 0)
SET IDENTITY_INSERT [dbo].[RA_NVL1] OFF
SET IDENTITY_INSERT [dbo].[RA_NVL2] ON 

INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (1, 1, N'Informe caja operador/terminal', NULL, 0, N'Operator/Terminal Cash', 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (2, 1, N'Informe caja oficina', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (3, 1, N'Informe de arqueo del cajon', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (4, 1, N'Informe diferencias arqueo', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (5, 1, N'Informe resumen totales tienda', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (6, 1, N'Informe estado f lin terminal', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (7, 1, N'Informe límite multiplicacion vales', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (8, 1, N'Informe resumen transacciones varias', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (9, 1, N'F pago chq/var - Informe resumen tda', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (10, 2, N'Informe ventas operador', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (11, 2, N'Informe totales departamento', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (12, 2, N'Informe totales horarios dpto', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (13, 2, N'Informe movimiento articulos', NULL, 0, NULL, 0)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (14, 2, N'Informe variacion departamento', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (15, 3, N'Informe lista formas de pago', NULL, 0, NULL, 0)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (16, 3, N'Anotaciones de transacciones', NULL, 0, NULL, 0)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (17, 3, N'Anotaciones de excepciones', NULL, 0, NULL, 0)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (18, 4, N'Informe rendimiento operador', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (19, 4, N'Informe productividad terminal', NULL, 0, NULL, 0)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (20, 5, N'Detalle datos articulo', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (21, 5, N'Resumen datos articulo', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (22, 5, N'Autorizacion operador', NULL, 0, NULL, 0)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (23, 5, N'Verificacion forma de pago', NULL, 0, NULL, 0)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (24, 6, N'Informe ventas negativas', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (25, 6, N'Informe excepcion ventas articulo', NULL, 0, NULL, 0)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (26, 6, N'Informe anulacion', NULL, 0, NULL, 1)
INSERT [dbo].[RA_NVL2] ([COD_NVL2], [COD_NVL1], [DES_ES], [DES_EN], [COD_ACE], [DES_ACE], [ACTIVO]) VALUES (27, 6, N'Informe devolucion', NULL, 0, NULL, 1)
SET IDENTITY_INSERT [dbo].[RA_NVL2] OFF
SET IDENTITY_INSERT [dbo].[RA_PRMT] ON 

INSERT [dbo].[RA_PRMT] ([COD_PRMT], [COD_NVL2], [DES_PRMT]) VALUES (1, 1, N'ReportName')
INSERT [dbo].[RA_PRMT] ([COD_PRMT], [COD_NVL2], [DES_PRMT]) VALUES (2, 1, N'OperTermId')
INSERT [dbo].[RA_PRMT] ([COD_PRMT], [COD_NVL2], [DES_PRMT]) VALUES (3, 1, N'FileName')
INSERT [dbo].[RA_PRMT] ([COD_PRMT], [COD_NVL2], [DES_PRMT]) VALUES (4, 1, N'OverwriteFile')
INSERT [dbo].[RA_PRMT] ([COD_PRMT], [COD_NVL2], [DES_PRMT]) VALUES (5, 1, N'ExtendedPeriod')
INSERT [dbo].[RA_PRMT] ([COD_PRMT], [COD_NVL2], [DES_PRMT]) VALUES (6, 1, N'Scope')
SET IDENTITY_INSERT [dbo].[RA_PRMT] OFF
;