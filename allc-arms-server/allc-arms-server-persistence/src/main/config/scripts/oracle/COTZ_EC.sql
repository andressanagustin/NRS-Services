
--------------------------------------------------------
--  DDL for Sequence SQ_ID_COTITM
--------------------------------------------------------

   CREATE SEQUENCE  "COTZ_EC"."SQ_ID_COTITM"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 582 CACHE 20 NOORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Table CO_CLIENTE
--------------------------------------------------------

  CREATE TABLE "COTZ_EC"."CO_CLIENTE" 
   (	"COD_CLIENTE" NUMBER(18,0), 
	"IDENTIFICACION" VARCHAR2(18 BYTE), 
	"NOMBRE" VARCHAR2(75 BYTE), 
	"APELLIDO_P" VARCHAR2(50 BYTE), 
	"APELLIDO_M" VARCHAR2(50 BYTE), 
	"GENERO" CHAR(1 BYTE), 
	"FEC_NACIMIENTO" DATE DEFAULT SYSDATE, 
	"DIRECCION" VARCHAR2(200 BYTE), 
	"COD_REGION" NUMBER DEFAULT 0, 
	"COD_CIUDAD" NUMBER, 
	"TELEFONO" VARCHAR2(50 BYTE), 
	"EMAIL" VARCHAR2(200 BYTE), 
	"ID_REG" NUMBER, 
	"FECHA" DATE DEFAULT SYSDATE, 
	"TIPOID" NUMBER
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;

   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."COD_CLIENTE" IS 'IDENTIFICADOR';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."IDENTIFICACION" IS 'CEDULA';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."NOMBRE" IS 'NOMBRE';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."APELLIDO_P" IS 'APELLIDO PATERNO';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."APELLIDO_M" IS 'APELLIDO MATERNO';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."GENERO" IS 'M - F - E';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."FEC_NACIMIENTO" IS 'FECHA DE NACIMIENTO';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."DIRECCION" IS 'DIRECCION';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."COD_REGION" IS 'DEPARTAMENTO';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."COD_CIUDAD" IS 'CIUDAD';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."TELEFONO" IS 'TELEFONO';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."EMAIL" IS 'CORREO';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."ID_REG" IS 'USUARIO';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."FECHA" IS 'FECHA DE REGISTRO';
   COMMENT ON COLUMN "COTZ_EC"."CO_CLIENTE"."TIPOID" IS '1: PERS.NATURAL, 2: PERS.JURIDICA';
--------------------------------------------------------
--  DDL for Table CO_COTCLTE
--------------------------------------------------------

  CREATE TABLE "COTZ_EC"."CO_COTCLTE" 
   (	"ID_COT" NUMBER(38,0), 
	"COD_CLIENTE" NUMBER(18,0)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Table IMP_COT
--------------------------------------------------------

  CREATE TABLE "COTZ_EC"."IMP_COT" 
   (	"ID_COT" NUMBER, 
	"COD_TIENDA" NUMBER, 
	"COD_NEGOCIO" NUMBER, 
	"ESTADO" NUMBER DEFAULT 0, 
	"ID_WS" NUMBER, 
	"IDREG" NUMBER, 
	"FECHA" DATE DEFAULT SYSDATE, 
	"FECHA_ACT" DATE DEFAULT SYSDATE
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;

   COMMENT ON COLUMN "COTZ_EC"."IMP_COT"."ID_COT" IS 'IDENTIFICADOR';
   COMMENT ON COLUMN "COTZ_EC"."IMP_COT"."COD_TIENDA" IS 'TIENDA';
   COMMENT ON COLUMN "COTZ_EC"."IMP_COT"."COD_NEGOCIO" IS 'NEGOCIO';
   COMMENT ON COLUMN "COTZ_EC"."IMP_COT"."ESTADO" IS '0:TEMP, 1:REG, 2:TRXSUSP';
   COMMENT ON COLUMN "COTZ_EC"."IMP_COT"."ID_WS" IS 'POS PARA TRX SUSP.';
   COMMENT ON COLUMN "COTZ_EC"."IMP_COT"."IDREG" IS 'QUIEN REGISTRA';
   COMMENT ON COLUMN "COTZ_EC"."IMP_COT"."FECHA" IS 'REGISTRO';
   COMMENT ON COLUMN "COTZ_EC"."IMP_COT"."FECHA_ACT" IS 'ACTUALIZACION';
--------------------------------------------------------
--  DDL for Table IMP_COTART
--------------------------------------------------------

  CREATE TABLE "COTZ_EC"."IMP_COTART" 
   (	"ID_COTITM" NUMBER, 
	"ID_COT" NUMBER, 
	"QN_ITM" NUMBER, 
	"TY_ITM" CHAR(1 BYTE), 
	"CD_ITM" NUMBER
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;

   COMMENT ON COLUMN "COTZ_EC"."IMP_COTART"."ID_COTITM" IS 'IDENTIFICADOR';
   COMMENT ON COLUMN "COTZ_EC"."IMP_COTART"."ID_COT" IS 'COTIZACION';
   COMMENT ON COLUMN "COTZ_EC"."IMP_COTART"."QN_ITM" IS 'CANTIDAD';
   COMMENT ON COLUMN "COTZ_EC"."IMP_COTART"."TY_ITM" IS 'P:PESABLE, U:UNITARIO';
   COMMENT ON COLUMN "COTZ_EC"."IMP_COTART"."CD_ITM" IS 'CODIGO ACE ITEM';


  CREATE UNIQUE INDEX "COTZ_EC"."IMP_COTART_PK" ON "COTZ_EC"."IMP_COTART" ("ID_COTITM") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index IMP_COT_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "COTZ_EC"."IMP_COT_PK" ON "COTZ_EC"."IMP_COT" ("ID_COT") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table IMP_COT
--------------------------------------------------------

  ALTER TABLE "COTZ_EC"."IMP_COT" MODIFY ("ID_COT" NOT NULL ENABLE);
  ALTER TABLE "COTZ_EC"."IMP_COT" ADD CONSTRAINT "IMP_COT_PK" PRIMARY KEY ("ID_COT")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE;
--------------------------------------------------------
--  Constraints for Table IMP_COTART
--------------------------------------------------------

  ALTER TABLE "COTZ_EC"."IMP_COTART" MODIFY ("ID_COTITM" NOT NULL ENABLE);
  ALTER TABLE "COTZ_EC"."IMP_COTART" ADD CONSTRAINT "IMP_COTART_PK" PRIMARY KEY ("ID_COTITM")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE;
--------------------------------------------------------
--  DDL for Trigger TR_IMP_COTART
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "COTZ_EC"."TR_IMP_COTART" 
  before insert on COTZ_EC.IMP_COTART
  for each row
declare

begin
    SELECT SQ_ID_COTITM.NEXTVAL
      INTO :NEW.ID_COTITM
      FROM DUAL;
  
end TR_IMP_COTART;
/
ALTER TRIGGER "COTZ_EC"."TR_IMP_COTART" ENABLE;