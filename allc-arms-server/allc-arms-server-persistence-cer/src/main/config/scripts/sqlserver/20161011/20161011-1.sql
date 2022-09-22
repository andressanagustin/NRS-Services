-------------SOBRE LA BASE DE DATOS SAADMIN------------------------------------------------

SET IDENTITY_INSERT PM_PARAM ON;
Insert into PM_PARAM (COD_PARAM,DES_PARAM,VAR_PARAM,TIP_PARAM,AMBITO) values ('196','PORCENTAJE DE DESCUENTO EMPLEADOS POR DEFAULT','PORC_DSC_EMP_DEF','0','2');
Insert into PM_PARAM (COD_PARAM,DES_PARAM,VAR_PARAM,TIP_PARAM,AMBITO) values ('197','PORCENTAJE DE BONO SOLIDARIO POR DEFAULT','PORC_BON_SOL_DEF','0','2');
SET IDENTITY_INSERT PM_PARAM OFF;

SET IDENTITY_INSERT PM_PARVAL ON;
Insert into PM_PARVAL (ID_PARVAL,COD_PARAM,VAL_PARAM,IDREG,FECHA,ESTADO) values ('196','196','0150','1103',convert(datetime,'03/11/15',3),'1');
Insert into PM_PARVAL (ID_PARVAL,COD_PARAM,VAL_PARAM,IDREG,FECHA,ESTADO) values ('197','197','0300','1103',convert(datetime,'03/11/15',3),'1');
SET IDENTITY_INSERT PM_PARVAL OFF;

--------------------------------------------------------------------------------------------

-------------SOBRE LA BASE DE DATOS ARTS_EC-------------------------------------------------

ALTER TABLE ID_DPT_PS DELETE COLUMN CD_DPT_PS;
ALTER TABLE CO_MRHRC_GP DELETE COLUMN CD_MRHRC_GP;

--------------------------------------------------------------------------------------------