USE COTZ_EC
GO

  CREATE TABLE CO_CLIENTE 
   (	
    COD_CLIENTE int IDENTITY (1, 1) NOT NULL, 
	IDENTIFICACION VARCHAR(18), 
	NOMBRE VARCHAR(75), 
	APELLIDO_P VARCHAR(50), 
	APELLIDO_M VARCHAR(50), 
	GENERO VARCHAR(1), 
	FEC_NACIMIENTO datetime DEFAULT SYSDATETIMEOFFSET(), 
	DIRECCION VARCHAR(200), 
	COD_REGION int NOT NULL DEFAULT 0, 
	COD_CIUDAD int NULL, 
	TELEFONO VARCHAR(50), 
	EMAIL VARCHAR(200), 
	ID_REG int NULL, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(), 
	TIPOID int NULL
   )
   
  CREATE TABLE CO_COTCLTE 
   (	
	[ID_COT] [int] NOT NULL,
	[ID_CPR] [bigint] NOT NULL
   )
   
  CREATE TABLE [IMP_COT](
	[ID_COT] [int] IDENTITY(1,1) NOT NULL,
	[COD_TIENDA] [int] NULL,
	[ESTADO] [int] NOT NULL CONSTRAINT [DF_IMP_COT_ESTADO]  DEFAULT ((0)),
	[ID_WS] [int] NOT NULL CONSTRAINT [DF_IMP_COT_ID_WS]  DEFAULT ((0)),
	[ID_REG] [int] NOT NULL CONSTRAINT [DF_IMP_COT_ID_REG]  DEFAULT ((0)),
	[FECHA] [datetime] NOT NULL CONSTRAINT [DF_IMP_COT_FECHA]  DEFAULT (sysdatetimeoffset()),
	[FECHA_ACT] [datetime] NOT NULL CONSTRAINT [DF_IMP_COT_FECHA_ACT]  DEFAULT (sysdatetimeoffset()),
	[F_AFILIADO] [int] NULL CONSTRAINT [DF__IMP_COT__ID_CPR__2D27B809]  DEFAULT ((1))
	) ON [PRIMARY]
   
  CREATE UNIQUE INDEX IMP_COT_PK ON IMP_COT (ID_COT) 

  CREATE TABLE IMP_COTART 
   (	
	[ID_COTITM] [int] IDENTITY(1,1) NOT NULL,
	[ID_COT] [int] NULL,
	[QN_ITM] DECIMAL(9,2) NULL,
	[TY_ITM] [varchar](1) NULL,
	[CD_ITM] [bigint] NULL,
	PRIMARY KEY (ID_COTITM)
   )
   
  CREATE UNIQUE INDEX IMP_COTART_PK ON IMP_COTART (ID_COTITM) 