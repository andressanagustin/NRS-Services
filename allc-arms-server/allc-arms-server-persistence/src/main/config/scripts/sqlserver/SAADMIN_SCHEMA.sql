USE SAADMIN
GO

  CREATE TABLE LG_EVENTO 
   (	
	COD_EVENTO int IDENTITY (1, 1) NOT NULL, 
	COD_TIPO_EVENTO int NULL, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(), 
	HORA VARCHAR(8), 
	IP_CLIENTE VARCHAR(200), 
	COD_USUARIO int NULL, 
	IDACC int NULL, 
	IDSISTEMA int NULL, 
	IDPERFIL int NULL,
	PRIMARY KEY (COD_EVENTO)
   )
   
  CREATE UNIQUE INDEX LG_EVENTO_PK ON LG_EVENTO (COD_EVENTO)
  
  CREATE TABLE LG_TIPO_EVENTO
   (	
    COD_TIPO_EVENTO int IDENTITY (1, 1) NOT NULL, 
	DES_TIPO_EVENTO VARCHAR(250),
	PRIMARY KEY (COD_TIPO_EVENTO)
   )
   
  CREATE UNIQUE INDEX LG_TIPO_EVENTO_PK ON LG_TIPO_EVENTO (COD_TIPO_EVENTO)
  
  CREATE TABLE MN_CANTON
   (
    COD_CANTON int IDENTITY (1, 1) NOT NULL, 
	NM_CANTON VARCHAR(250),
	PRIMARY KEY (COD_CANTON)
   ) 
   
  CREATE UNIQUE INDEX MN_CANTON_PK ON MN_CANTON (COD_CANTON) 
   
  CREATE TABLE MN_NEGOCIO 
   (	
    COD_NEGOCIO int IDENTITY (1, 1) NOT NULL, 
	DES_NEGOCIO VARCHAR(200), 
	IDREG int NULL, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(), 
	COD_LINEA VARCHAR(2), 
	COD_SAP VARCHAR(1),
	PRIMARY KEY (COD_NEGOCIO)
   )
   
  CREATE UNIQUE INDEX MN_NEGOCIO_PK ON MN_NEGOCIO (COD_NEGOCIO) 
   
  CREATE TABLE MN_NEGTND 
   (	
    COD_NEGOCIO int NOT NULL, 
	COD_TIENDA int NOT NULL,
	PRIMARY KEY (COD_NEGOCIO,COD_TIENDA)
   ) 
   
  CREATE TABLE MN_PROVINCIA
   (	
    COD_PROVINCIA int IDENTITY (1, 1) NOT NULL, 
	NM_PROVINCIA VARCHAR(250),
	PRIMARY KEY (COD_PROVINCIA)
   )
   
  CREATE UNIQUE INDEX MN_PROVINCIA_PK ON MN_PROVINCIA (COD_PROVINCIA) 
  
  CREATE TABLE MN_SOCIEDAD
   (	
    COD_SOC int IDENTITY (1, 1) NOT NULL, 
	DES_SOC int NOT NULL, 
	NM_SOC VARCHAR(250), 
	RUC_SOC VARCHAR(20),
	PRIMARY KEY (COD_SOC)
   )
   
  CREATE UNIQUE INDEX MN_SOCIEDAD_PK ON MN_SOCIEDAD (COD_SOC) 
   
  CREATE TABLE MN_TIENDA
   (	
    COD_TIENDA int IDENTITY (1, 1) NOT NULL, 
	DES_TIENDA VARCHAR(200), 
	DES_CLAVE int NOT NULL, 
	DIRECCION VARCHAR(200), 
	COD_REGION int NOT NULL DEFAULT 0, 
	COD_CIUDAD int NOT NULL DEFAULT 0, 
	IP VARCHAR(15),
	IND_ACTIVO int NOT NULL DEFAULT 0, 
	IDREG int NULL, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(),
	COD_CANTON int NOT NULL DEFAULT 0,
	COD_PROVINCIA int NOT NULL DEFAULT 0,
	RUC_TIENDA VARCHAR(14),
	COD_SRI VARCHAR(4),
	FL_LCL_SRVR int NOT NULL DEFAULT 0,
	PA_DIVISION VARCHAR(20) NULL,
	PA_CIUDAD VARCHAR(30) NULL,
	PRIMARY KEY (COD_TIENDA)
   )
   
  CREATE UNIQUE INDEX MN_TIENDA_PK ON MN_TIENDA (COD_TIENDA) 
      
  CREATE TABLE MN_TNDSOC
   (	
    COD_TIENDA int NOT NULL, 
	COD_SOC int NOT NULL,
	PRIMARY KEY (COD_TIENDA,COD_SOC)
   )
   
  CREATE UNIQUE INDEX MN_TNDSOC_PK ON MN_TNDSOC (COD_TIENDA, COD_SOC) 
   
  CREATE TABLE PM_CIUDAD 
   (	
    COD_CIUDAD int IDENTITY (1, 1) NOT NULL, 
	COD_REGION int NOT NULL DEFAULT 0, 
	DES_CIUDAD VARCHAR(200), 
	COD_PAIS int NOT NULL DEFAULT 0, 
	PRIMARY KEY (COD_CIUDAD)
   )
   
  CREATE UNIQUE INDEX PM_CIUDAD_PK ON PM_CIUDAD (COD_CIUDAD)
   
  CREATE TABLE PM_PAIS
   (	
    COD_PAIS int IDENTITY (1, 1) NOT NULL, 
	DES_PAIS VARCHAR(200), 
	DES_NIC VARCHAR(2), 
	MONEDA VARCHAR(3), 
	CENTAVOS int NOT NULL DEFAULT 0,
	DPEST int NOT NULL DEFAULT 0,
	PACTIV int NOT NULL DEFAULT 0,
	CEDPERS VARCHAR(6), 
	CEDEMP VARCHAR(6), 
	ESTADO int NOT NULL DEFAULT 0,
	DES_REGION VARCHAR(100),
	SEPMIL VARCHAR(1),
	SEPDEC VARCHAR(1),
	PRIMARY KEY (COD_PAIS)
   )
   
  CREATE UNIQUE INDEX PM_PAIS_PK ON PM_PAIS (COD_PAIS) 
     
  CREATE TABLE PM_PARAM
   (	
    COD_PARAM int IDENTITY (1, 1) NOT NULL, 
	DES_PARAM VARCHAR(200), 
	VAR_PARAM VARCHAR(200), 
	TIP_PARAM int NOT NULL DEFAULT 0,
	AMBITO int NOT NULL DEFAULT 0,
	PRIMARY KEY (COD_PARAM)
   )
   
  CREATE UNIQUE INDEX PM_PARAM_PK ON PM_PARAM (COD_PARAM) 
       
  CREATE TABLE PM_PARVAL
   (	
    ID_PARVAL int IDENTITY (1, 1) NOT NULL, 
	COD_PARAM int NOT NULL, 
	VAL_PARAM VARCHAR(2500), 
	IDREG int NOT NULL DEFAULT 0,
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(),
	ESTADO int NOT NULL DEFAULT 0,
	DES_CLAVE int NULL,
	PRIMARY KEY (ID_PARVAL)
   )
   
  CREATE UNIQUE INDEX PM_PARVAL_PK ON PM_PARVAL (ID_PARVAL) 
  
  CREATE TABLE PM_REGION
   (	
    COD_REGION int IDENTITY (1, 1) NOT NULL, 
	DES_REGION VARCHAR(200), 
	ABR_REGION VARCHAR(10), 
	COD_PAIS int NULL,
	PRIMARY KEY (COD_REGION)
   )
   
  CREATE UNIQUE INDEX PM_REGION_PK ON PM_REGION (COD_REGION) 
   
  CREATE TABLE US_ACCESO
   (	
    IDACC int IDENTITY (1, 1) NOT NULL, 
	NOMBRE VARCHAR(200), 
	ENLACE VARCHAR(200), 
	TIPO int NULL, 
	IDREG int NULL, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(), 
	IDSISTEMA int NOT NULL DEFAULT 0,
	PRIMARY KEY (IDACC)
   )
   
  CREATE UNIQUE INDEX US_ACCESO_PK ON US_ACCESO (IDACC) 
   
  CREATE TABLE US_PERFACC
   (	
    IDPERFIL int NOT NULL, 
	IDACC int NOT NULL, 
	INGRESO int NOT NULL DEFAULT 0,
	PRIMARY KEY (IDPERFIL,IDACC)
   )
   
  CREATE TABLE US_PERFIL
   (	
    IDPERFIL int IDENTITY (1, 1) NOT NULL, 
	NOMBRE VARCHAR(200), 
	EDITAR int NOT NULL DEFAULT 0, 
	WM int NOT NULL DEFAULT 0, 
	IDREG int NULL DEFAULT 0, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(), 
	IDSISTEMA int NOT NULL DEFAULT 0,
	FL_PRED int NULL DEFAULT 0,
	FL_SET int NOT NULL DEFAULT 1,
	PRIMARY KEY (IDPERFIL)
   )
   
  CREATE UNIQUE INDEX US_PERFIL_PK ON US_PERFIL (IDPERFIL) 
   
  CREATE TABLE US_SISTEMA
   (	
    IDSISTEMA int IDENTITY (1, 1) NOT NULL, 
	NOMBRE VARCHAR(200), 
	CARPETA VARCHAR(200), 
	BDIP VARCHAR(200), 
	BDUS VARCHAR(200), 
	BDPS VARCHAR(200),
	BDNM VARCHAR(200), 
	IDREG int NULL, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(),
	FL_SET int NOT NULL DEFAULT 0,
	PRIMARY KEY (IDSISTEMA)
   )
   
  CREATE UNIQUE INDEX US_SISTEMA_PK ON US_SISTEMA (IDSISTEMA) 
    
   CREATE TABLE US_USUARIOS
   (	
    IDUSU int IDENTITY (1, 1) NOT NULL, 
	NOMBRE VARCHAR(200), 
	CUENTA VARCHAR(200), 
	CLAVE VARCHAR(20), 
	EMAIL VARCHAR(200), 
	ESTADO int NOT NULL DEFAULT 0, 
	IDREG int NULL, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(),
	SU int NOT NULL DEFAULT 0, 
	CC_OPERADOR int NOT NULL DEFAULT 0,
	FL_PASS int NOT NULL DEFAULT 0, 
	PRIMARY KEY (IDUSU)
   )
   
  CREATE UNIQUE INDEX US_USUARIOS_PK ON US_USUARIOS (IDUSU) 
   
  CREATE TABLE US_USUPERF
   (	
    IDUSU int NOT NULL, 
	IDPERFIL int NOT NULL, 
	IDSISTEMA int NULL,
	PRIMARY KEY (IDUSU,IDPERFIL)
   )
   
  CREATE TABLE US_USUTND
   (	
    IDUSU int NOT NULL, 
	COD_TIENDA int NOT NULL, 
	IDREG int NULL, 
	FECHA datetime DEFAULT SYSDATETIMEOFFSET(), 
	COD_NEGOCIO int NOT NULL DEFAULT 0,
	PRIMARY KEY (IDUSU,COD_TIENDA,COD_NEGOCIO)
   )
   
  CREATE UNIQUE INDEX US_USUTND_PK ON US_USUTND (IDUSU, COD_TIENDA, COD_NEGOCIO) 
  
  
   
  CREATE TABLE CFG_CN_CNF
   (	
	ID_CN_CNF 		int IDENTITY (1, 1) NOT NULL, 
	SPR_NM 			VARCHAR(20) NOT NULL, 
	DES_CLAVE 		int NOT NULL,
	LST_CN_DATE     datetime NOT NULL,
	PRIMARY KEY (ID_CN_CNF)
   ) 
  
  
  ALTER TABLE LG_EVENTO ADD CONSTRAINT LG_EVENTO_FK1 FOREIGN KEY (COD_TIPO_EVENTO) REFERENCES LG_TIPO_EVENTO (COD_TIPO_EVENTO)
  ALTER TABLE MN_TNDSOC ADD CONSTRAINT MN_TNDSOC_FK1 FOREIGN KEY (COD_TIENDA) REFERENCES MN_TIENDA (COD_TIENDA)
  ALTER TABLE MN_TNDSOC ADD CONSTRAINT MN_TNDSOC_FK2 FOREIGN KEY (COD_SOC) REFERENCES MN_SOCIEDAD (COD_SOC)
  
  ALTER TABLE US_ACCESO ADD CONSTRAINT US_ACCESO_FK1 FOREIGN KEY (IDSISTEMA) REFERENCES US_SISTEMA (IDSISTEMA)
  ALTER TABLE US_PERFIL ADD CONSTRAINT US_PERFIL_FK1 FOREIGN KEY (IDSISTEMA) REFERENCES US_SISTEMA (IDSISTEMA);