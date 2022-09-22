--------------------------------------------------------
--  DDL for Sequence DEMO_CUST_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "CVMT_EC"."DEMO_CUST_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 21 CACHE 20 NOORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence DEMO_ORDER_ITEMS_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "CVMT_EC"."DEMO_ORDER_ITEMS_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 61 CACHE 20 NOORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence DEMO_ORD_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "CVMT_EC"."DEMO_ORD_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 11 CACHE 20 NOORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence DEMO_PROD_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "CVMT_EC"."DEMO_PROD_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 21 CACHE 20 NOORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence DEMO_USERS_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "CVMT_EC"."DEMO_USERS_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 21 CACHE 20 NOORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Table CM_CARTAS
--------------------------------------------------------

  CREATE TABLE "CVMT_EC"."CM_CARTAS" 
   (	"ID_CARTA" NUMBER(38,0), 
	"ID_TRN" NUMBER(38,0), 
	"FECHA_C" DATE DEFAULT SYSDATE, 
	"ESTADO" NUMBER DEFAULT 0, 
	"SRL_NBR" VARCHAR2(20 BYTE), 
	"CC_OPERADOR" NUMBER, 
	"DES_CLAVE" NUMBER, 
	"FECHA_E" DATE, 
	"NOMBXML" VARCHAR2(200 BYTE)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;

   COMMENT ON COLUMN "CVMT_EC"."CM_CARTAS"."ID_TRN" IS 'TRX';
   COMMENT ON COLUMN "CVMT_EC"."CM_CARTAS"."FECHA_C" IS 'FECHA DE CREACION';
   COMMENT ON COLUMN "CVMT_EC"."CM_CARTAS"."ESTADO" IS '0: PENDIENTE, 1:ACEPTADA, 2: IMPRESA Y ENVIADA, 3: RE-IMPRESA, 4: RECHAZADA';
   COMMENT ON COLUMN "CVMT_EC"."CM_CARTAS"."SRL_NBR" IS 'SERIE';
   COMMENT ON COLUMN "CVMT_EC"."CM_CARTAS"."CC_OPERADOR" IS 'GERENTE';
   COMMENT ON COLUMN "CVMT_EC"."CM_CARTAS"."DES_CLAVE" IS 'TIENDA';
   COMMENT ON COLUMN "CVMT_EC"."CM_CARTAS"."FECHA_E" IS 'FECHA DE ACEPTACION';
   COMMENT ON COLUMN "CVMT_EC"."CM_CARTAS"."NOMBXML" IS 'NOMBRE DEL ARCHIVO XML';

--------------------------------------------------------
--  DDL for Index CM_CARTAS_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "CVMT_EC"."CM_CARTAS_PK" ON "CVMT_EC"."CM_CARTAS" ("ID_CARTA") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table CM_CARTAS
--------------------------------------------------------

  ALTER TABLE "CVMT_EC"."CM_CARTAS" MODIFY ("ID_CARTA" NOT NULL ENABLE);
  ALTER TABLE "CVMT_EC"."CM_CARTAS" ADD CONSTRAINT "CM_CARTAS_PK" PRIMARY KEY ("ID_CARTA")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE;