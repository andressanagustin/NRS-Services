USE PRCFLJ_EC
GO

  CREATE TABLE ARC_EXPRC 
   (	
    ID_ARCPRC int NULL, 
	NUM_LINLO int NULL, 
	EXCEPCION VARCHAR(1), 
	DECIDE VARCHAR(1), 
	IDREG int NULL, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(), 
	NUM_LINEX int NULL
   )
   
     CREATE TABLE ARTICBALANZA 
   (	
    ARTICULO 				varchar(7) NOT NULL,
	DES_CLAVE 				int NOT NULL,
	DESCRIPCION 			varchar(40) NULL,
	PRECIO_PUB 				decimal(10, 2) NULL DEFAULT 0,
	PRECIO_COM 				decimal(10, 2) NULL DEFAULT 0,
	PORC_RECARGO 			decimal(4, 2) NULL DEFAULT 0,
	IND_IVA 				char(1) NULL,
	IND_AL_PESO 			char(1) NULL DEFAULT 0,
	COD_SECCION 			varchar(4) NULL,
	COD_SUBSEC 				varchar(4) NULL,
	JERARQUIA_SAP 			varchar(10) NULL,
	NOMBRE_GRUPO 			varchar(60) NULL,
	FCH_ULT_CAMBIO 			date NULL,
	DIAS_REFRIGER 			int NULL,
	DIAS_CONGELAC 			int NULL,
	IND_EMPACADO 			char(1) NULL,
	PROCESADO 				char(1) NULL DEFAULT 'N',
	ID_ITM					int NULL,
	IND_FECHAEMISION 		char(1) NULL DEFAULT 'S',
	PRIMARY KEY (ARTICULO, DES_CLAVE)
   )
   
   
   CREATE TABLE T_USERBALANZA(
	LOGIN_I varchar(10) NOT NULL,
	NOMBRE_USER varchar(30) NULL,
	CLAVE  varchar(15) NULL,
	FCH_ULT_CAMBIO date NULL,
	PROCESADO char(1) NULL DEFAULT 'N',
	DES_CLAVE int NULL,
	PRIMARY KEY (LOGIN_I)
	)
   
  CREATE TABLE ARC_ITEMS 
   (	
    ID_ARCITEM int IDENTITY (1, 1) NOT NULL, 
	ID_ARCPRC int NOT NULL DEFAULT 0, 
	ESTADO int NOT NULL DEFAULT 0, 
	ARCHIVO VARCHAR(250),
	PRIMARY KEY (ID_ARCITEM)
   )
   
  CREATE UNIQUE INDEX ARC_ITEMS_PK ON ARC_ITEMS (ID_ARCITEM) 
   
  CREATE TABLE ARC_MOV 
   (	
    ID_MOV int IDENTITY (1, 1) NOT NULL, 
	ID_ARCPRC int NULL, 
	ID_ESTPRC int NULL, 
	FEC_MOV datetime DEFAULT SYSDATETIMEOFFSET(), 
	HOR_MOV VARCHAR(8), 
	IDREG int NOT NULL DEFAULT 0,
	PRIMARY KEY (ID_MOV)
   )
   
  CREATE UNIQUE INDEX ARC_MOV_PK ON ARC_MOV (ID_MOV) 
   
  CREATE TABLE ARC_PRC 
   (	
    ID_ARCPRC int IDENTITY (1, 1) NOT NULL, 
	NOM_ARCPRC VARCHAR(200), 
	NUM_ITEMS int NULL, 
	COD_TIENDA int NULL, 
	ID_ESTPRC int NOT NULL DEFAULT 0, 
	FEC_PROC datetime DEFAULT SYSDATETIMEOFFSET(), 
	NOM_ARCLOTE VARCHAR(200), 
	COD_NEGOCIO int NULL, 
	ID_ARCSAP int NOT NULL DEFAULT 0, 
	ID_DPT_PS int NULL,
	PRIMARY KEY (ID_ARCPRC)
   )
   
  CREATE UNIQUE INDEX ARC_PRC_PK ON ARC_PRC (ID_ARCPRC)
  
   CREATE TABLE MOTO_FILES 
   (	
    ID_MOTOFILE int IDENTITY (1, 1) NOT NULL, 
	NOM_MOTOFILE VARCHAR(200), 
	
	PRIMARY KEY (ID_MOTOFILE)
   )
   
  CREATE UNIQUE INDEX MOTO_FILES_PK ON MOTO_FILES (ID_MOTOFILE) 
   
  CREATE TABLE ARC_PRCEX 
   (	
    ID_ARCPRC int NULL, 
	NOM_ARCEX VARCHAR(200), 
	NUM_ITEMS int NULL
   )
   
  CREATE TABLE ARC_SAP 
   (	
    ID_ARCSAP int IDENTITY (1, 1) NOT NULL, 
	COD_TIENDA int NULL, 
	NUM_LOTE bigint NOT NULL, 
	ARCITEM VARCHAR(200), 
	NUM_ITEM int NOT NULL DEFAULT 0, 
	ARCEAN VARCHAR(200), 
	NUM_EAN int NOT NULL DEFAULT 0, 
	ARCERRI VARCHAR(200), 
	NUM_ERRI int NOT NULL DEFAULT 0, 
	ARCERRE VARCHAR(200), 
	NUM_ERRE int NOT NULL DEFAULT 0, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(), 
	ID_ESTPRC int NOT NULL DEFAULT 2
   )
   
  CREATE TABLE EST_PRC 
   (	
    ID_ESTPRC int IDENTITY (1, 1) NOT NULL, 
	NOM_ESTPRC VARCHAR(200), 
	COL_ESTPRC VARCHAR(7),
	CSF_ESTADO VARCHAR(250),
	PRIMARY KEY (ID_ESTPRC)
   )
   
  CREATE UNIQUE INDEX EST_PRC_PK ON EST_PRC (ID_ESTPRC) 
   
  CREATE TABLE FLJ_PRC 
   (	
    ID_FLJPRC int IDENTITY (1, 1) NOT NULL, 
	ID_FTIPO int NULL, 
	ID_ARCPRC int NULL, 
	NUM_FLEJES int NULL, 
	CD_ITM VARCHAR(25), 
	MO_ITM int NULL, 
	NM_ITM VARCHAR(50), 
	CB_ITM int NULL
   )
   
  CREATE TABLE FLJ_TIPO 
   (	
    ID_FTIPO int IDENTITY (1, 1) NOT NULL, 
	NOM_FTIPO VARCHAR(200), 
	EST_FTIPO int NULL, 
	COD_FTIPO VARCHAR(1),
	PRIMARY KEY (ID_FTIPO)
   )
   
  CREATE UNIQUE INDEX FLJ_TIPO_PK ON FLJ_TIPO (ID_FTIPO) 
   
  CREATE TABLE IMP_FLJ 
   (	
    ID_FLEJE int IDENTITY (1, 1) NOT NULL, 
	COD_TIENDA int NULL, 
	COD_FTIPO VARCHAR(1), 
	ARCHIVO VARCHAR(250), 
	IDREG int NULL, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(), 
	ESTADO int NOT NULL DEFAULT 0, 
	TY_DIF int NULL DEFAULT 0,
	[ID_FLJDIF] [int] NULL DEFAULT 0,
	PRIMARY KEY (ID_FLEJE)
   )
      
  CREATE UNIQUE INDEX IMP_FLJ_PK ON IMP_FLJ (ID_FLEJE) 
   
  CREATE TABLE IMP_FLJART 
   (	
    ID_FLJART int IDENTITY (1, 1) NOT NULL,
	ID_FLEJE int NULL, 
	CD_ITM bigint NULL, 
	QN_ITM int NULL, 
	PRIMARY KEY (ID_FLJART)
   )
   
  CREATE UNIQUE INDEX IMP_FLJART_PK ON IMP_FLJART (ID_FLJART) 
   
  CREATE TABLE PRC_PRC 
   (	
    ID_PRCPRC int IDENTITY (1, 1) NOT NULL, 
	ID_ARCPRC int NULL, 
	FEC_PRCPRC datetime
   )
   
  ALTER TABLE ARC_PRC ADD CONSTRAINT ARC_PRC_FK1 FOREIGN KEY (ID_ESTPRC) REFERENCES EST_PRC (ID_ESTPRC)
   
  -- NUEVA TABLA DEL REGISTRO DE DIFERIDOS (CUOTA E IMPUESTO) DE FLEJES --
  CREATE TABLE [dbo].[FLJ_DIF]
  (
	[ID_FLJDIF] [int] IDENTITY(1,1) NOT NULL, 
	[CD_FLJDIF] [int] NOT NULL CONSTRAINT [DF_FLJ_DIF_CD_FLJDIF]  DEFAULT ((0)), 
	[TX_FLJDIF] [int] NOT NULL CONSTRAINT [DF_FLJ_DIF_TX_FLJDIF]  DEFAULT ((0)), 
	[ID_REG] [int] NOT NULL CONSTRAINT [DF_FLJ_DIF_ID_REG]  DEFAULT ((0)), 
	[FECHA] [datetime] NOT NULL CONSTRAINT [DF_FLJ_DIF_FECHA]  DEFAULT (getdate()),
	MON_MIN bigint NULL DEFAULT 0,
	PRIMARY KEY (ID_FLJDIF)
 )
;
