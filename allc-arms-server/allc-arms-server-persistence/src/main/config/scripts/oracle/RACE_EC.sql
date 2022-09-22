--------------------------------------------------------
--  DDL for Sequence DEMO_CUST_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "RACE_EC"."DEMO_CUST_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 21 CACHE 20 NOORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence DEMO_ORDER_ITEMS_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "RACE_EC"."DEMO_ORDER_ITEMS_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 61 CACHE 20 NOORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence DEMO_ORD_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "RACE_EC"."DEMO_ORD_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 11 CACHE 20 NOORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence DEMO_PROD_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "RACE_EC"."DEMO_PROD_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 21 CACHE 20 NOORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence DEMO_USERS_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "RACE_EC"."DEMO_USERS_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 21 CACHE 20 NOORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Table RA_NVL1
--------------------------------------------------------

  CREATE TABLE "RACE_EC"."RA_NVL1" 
   (	"COD_NVL1" NUMBER, 
	"DES_ES" VARCHAR2(250 BYTE), 
	"DES_EN" VARCHAR2(250 BYTE), 
	"COD_ACE" NUMBER DEFAULT 0
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Table RA_NVL2
--------------------------------------------------------

  CREATE TABLE "RACE_EC"."RA_NVL2" 
   (	"COD_NVL2" NUMBER, 
	"COD_NVL1" NUMBER, 
	"DES_ES" VARCHAR2(250 BYTE), 
	"DES_EN" VARCHAR2(250 BYTE), 
	"COD_ACE" NUMBER DEFAULT 0, 
	"DES_ACE" VARCHAR2(250 BYTE), 
	"ACTIVO" NUMBER DEFAULT 0
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Table RA_PRMT
--------------------------------------------------------

  CREATE TABLE "RACE_EC"."RA_PRMT" 
   (	"COD_PRMT" NUMBER, 
	"COD_NVL2" NUMBER, 
	"DES_PRMT" VARCHAR2(250 BYTE)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;

REM INSERTING into RACE_EC.RA_NVL1
SET DEFINE OFF;
Insert into RACE_EC.RA_NVL1 (COD_NVL1,DES_ES,DES_EN,COD_ACE) values ('1','Informes de contabilidad',null,'0');
Insert into RACE_EC.RA_NVL1 (COD_NVL1,DES_ES,DES_EN,COD_ACE) values ('2','Informes de ventas',null,'0');
Insert into RACE_EC.RA_NVL1 (COD_NVL1,DES_ES,DES_EN,COD_ACE) values ('3','Informes de listas y anotaciones',null,'0');
Insert into RACE_EC.RA_NVL1 (COD_NVL1,DES_ES,DES_EN,COD_ACE) values ('4','Informes de rendimiento',null,'0');
Insert into RACE_EC.RA_NVL1 (COD_NVL1,DES_ES,DES_EN,COD_ACE) values ('5','Informes de mantenimiento de datos',null,'0');
Insert into RACE_EC.RA_NVL1 (COD_NVL1,DES_ES,DES_EN,COD_ACE) values ('6','Informes de seguridad',null,'0');

REM INSERTING into RACE_EC.RA_NVL2
SET DEFINE OFF;
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('1','1','Informe caja operador/terminal',null,'0','Operator/Terminal Cash','1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('2','1','Informe caja oficina',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('3','1','Informe de arqueo del cajÃ³n',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('4','1','Informe diferencias arqueo',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('5','1','Informe resumen totales tienda',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('6','1','Informe estado f lin terminal',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('7','1','Informe lÃ­mite multiplicaciÃ³n vales',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('8','1','Informe resumen transacciones varias',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('9','1','F pago chq/var - Informe resumen tda',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('10','2','Informe ventas operador',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('11','2','Informe totales departamento',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('12','2','Informe totales horarios dpto',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('13','2','Informe movimiento artÃ­culos',null,'0',null,'0');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('14','2','Informe variaciÃ³n departamento',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('15','3','Informe lista formas de pago',null,'0',null,'0');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('16','3','Anotaciones de transacciones',null,'0',null,'0');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('17','3','Anotaciones de excepciones',null,'0',null,'0');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('18','4','Informe rendimiento operador',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('19','4','Informe productividad terminal',null,'0',null,'0');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('20','5','Detalle datos artÃ­culo',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('21','5','Resumen datos artÃ­culo',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('22','5','AutorizaciÃ³n operador',null,'0',null,'0');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('23','5','VerificaciÃ³n forma de pago',null,'0',null,'0');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('24','6','Informe ventas negativas',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('25','6','Informe excepciÃ³n ventas artÃ­culo',null,'0',null,'0');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('26','6','Informe anulaciÃ³n',null,'0',null,'1');
Insert into RACE_EC.RA_NVL2 (COD_NVL2,COD_NVL1,DES_ES,DES_EN,COD_ACE,DES_ACE,ACTIVO) values ('27','6','Informe devoluciÃ³n',null,'0',null,'1');

REM INSERTING into RACE_EC.RA_PRMT
SET DEFINE OFF;
Insert into RACE_EC.RA_PRMT (COD_PRMT,COD_NVL2,DES_PRMT) values ('1','1','ReportName');
Insert into RACE_EC.RA_PRMT (COD_PRMT,COD_NVL2,DES_PRMT) values ('2','1','OperTermId');
Insert into RACE_EC.RA_PRMT (COD_PRMT,COD_NVL2,DES_PRMT) values ('3','1','FileName');
Insert into RACE_EC.RA_PRMT (COD_PRMT,COD_NVL2,DES_PRMT) values ('4','1','OverwriteFile');
Insert into RACE_EC.RA_PRMT (COD_PRMT,COD_NVL2,DES_PRMT) values ('5','1','ExtendedPeriod');
Insert into RACE_EC.RA_PRMT (COD_PRMT,COD_NVL2,DES_PRMT) values ('6','1','Scope');
--------------------------------------------------------
--  DDL for Index RA_NVL1_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "RACE_EC"."RA_NVL1_PK" ON "RACE_EC"."RA_NVL1" ("COD_NVL1") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index RA_NVL2_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "RACE_EC"."RA_NVL2_PK" ON "RACE_EC"."RA_NVL2" ("COD_NVL2") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index RA_PMTRPT_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "RACE_EC"."RA_PMTRPT_PK" ON "RACE_EC"."RA_PRMT" ("COD_PRMT") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table RA_NVL1
--------------------------------------------------------

  ALTER TABLE "RACE_EC"."RA_NVL1" ADD CONSTRAINT "RA_NVL1_PK" PRIMARY KEY ("COD_NVL1")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE;
  ALTER TABLE "RACE_EC"."RA_NVL1" MODIFY ("COD_NVL1" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table RA_NVL2
--------------------------------------------------------

  ALTER TABLE "RACE_EC"."RA_NVL2" ADD CONSTRAINT "RA_NVL2_PK" PRIMARY KEY ("COD_NVL2")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE;
  ALTER TABLE "RACE_EC"."RA_NVL2" MODIFY ("COD_NVL2" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table RA_PRMT
--------------------------------------------------------

  ALTER TABLE "RACE_EC"."RA_PRMT" ADD CONSTRAINT "RA_PMTRPT_PK" PRIMARY KEY ("COD_PRMT")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE;
  ALTER TABLE "RACE_EC"."RA_PRMT" MODIFY ("COD_PRMT" NOT NULL ENABLE);
