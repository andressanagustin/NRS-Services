
USE ARTS_EC
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[PI_CERRAR_ENTREGA_CAJAS] 
AS
DECLARE @tienda int,
		@fecha_cierre date

BEGIN

	SET NOCOUNT ON;

	DECLARE cTiendas CURSOR FOR

	SELECT TIENDA, FECHA_CIERRE
		FROM PI_CONTROL
		WHERE RECEPCION_CAJAS = '0' ORDER BY FECHA_CIERRE, TIENDA

	OPEN cTiendas

	FETCH cTiendas INTO  @tienda,@fecha_cierre

	WHILE (@@FETCH_STATUS = 0 )
	BEGIN

		UPDATE c SET c.RECEPCION_CAJAS = '1'
		FROM PI_CONTROL c INNER JOIN LE_STR_REC_TOT l   
		ON c.TIENDA = l.STR_CD AND c.FECHA_CIERRE = CONVERT(DATE, l.TM_STP,103)
		WHERE c.TIENDA = @tienda AND c.FECHA_CIERRE = @fecha_cierre
		AND l.FL_PRNTD = 1 AND l.FL_RCLD = 1

		FETCH cTiendas INTO  @tienda,@fecha_cierre
	END
	CLOSE cTiendas
	DEALLOCATE cTiendas
END


GO
/****** Object:  StoredProcedure [dbo].[PI_LEE_DEPOSITO_EFECTIVO]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_LEE_DEPOSITO_EFECTIVO]
AS
BEGIN

DECLARE @vs_moneda varchar(4);
declare @v_control_fecha_cierre date;
declare @registros int;
declare @tbl_tiendas table (tienda int)

	SET NOCOUNT ON;

	SELECT @vs_moneda = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';
	SELECT TOP 1  @v_control_fecha_cierre=fecha_cierre FROM pi_control WHERE EFC = '0' ORDER BY fecha_cierre;
	insert into @tbl_tiendas select tienda from pi_control where EFC = '0' and fecha_cierre = @v_control_fecha_cierre;

	SELECT '1001' SOCIEDAD, CONVERT(VARCHAR(8), TI.TS_DEP,112) AS FECHA, @vs_moneda as MONEDA, TI.MO_DEP AS VALOR, TI.DE_DEP AS DETALLE,
	RIGHT('000000' + cast(STR.CD_STR_RT as varchar(3)),3) AS ALMACEN, TI.ID_BNC_DEP AS CUENTA_DEBITO, 
	'501010100100' as CUENTA_CREDITO, SEC.CICLO_EFC CICLO, SEC.SECUENCIA_EFC SECUENCIA, '/' SEPARADOR
	FROM TDVAL_EC.dbo.DEP_TICKET TI, ARTS_EC.dbo.PA_STR_RTL STR, TDVAL_EC.dbo.DEP_TCKTND TND, PI_CONTROL CON, PI_SECUENCIA_INICIAL SEC
	WHERE STR.CD_STR_RT = CON.COD_TIENDA AND CON.COD_TIENDA = SEC.cod_tienda AND
	TND.ID_TND = 11 --CORRESPONDE AL MEDIO DE PAGO EFECTIVO
	OR (TND.ID_TND = 21 OR TND.ID_TND = 22 OR TND.ID_TND = 23 OR TND.ID_TND = 24) --CORRESPONDE A MEDIOS DE PAGO CHEQUE
	AND  CONVERT(DATE, TI.TS_DEP,103)=@v_control_fecha_cierre AND STR.CD_STR_RT IN (select tienda from @tbl_tiendas)

	/*SELECT  CONVERT(VARCHAR(8), t.DC_DY_BSN,112) as FECHA, @vs_moneda as MONEDA, '43.00' as VALOR, 'DEPOSITO' as DETALLE,
	RIGHT('000000' + cast(c.TIENDA as varchar(3)),3) as ALMACEN, '0000000000' as CUENTA_DEBITO, '0000000000' as CUENTA_CREDITO,
	'3' as CICLO, '234' as SECUENCIA, '/'
	FROM TR_TRN t 
	INNER JOIN PI_CONTROL c ON t.ID_BSN_UN = c.COD_TIENDA AND CONVERT(DATE, t.DC_DY_BSN,103) = c.FECHA_CIERRE
	WHERE CONVERT(DATE, t.DC_DY_BSN,103)=@v_control_fecha_cierre
	and c.tienda IN (select tienda from @tbl_tiendas)*/


	UPDATE pi_control SET EFC = '1' 
	WHERE FECHA_CIERRE=@v_control_fecha_cierre and tienda IN (select tienda from @tbl_tiendas)


END


GO
/****** Object:  StoredProcedure [dbo].[PI_LEE_DEPOSITO_TARJETAS]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_LEE_DEPOSITO_TARJETAS]
AS
BEGIN

DECLARE @vs_moneda varchar(4);
declare @v_control_fecha_cierre date;
declare @registros int;
declare @tbl_tiendas table (tienda int)

	SET NOCOUNT ON;

	SELECT @vs_moneda = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';
	SELECT TOP 1  @v_control_fecha_cierre=fecha_cierre FROM pi_control WHERE TRJ = '0' ORDER BY fecha_cierre;
	insert into @tbl_tiendas select tienda from pi_control where TRJ = '0' and fecha_cierre = @v_control_fecha_cierre;

	SELECT '1001' SOCIEDAD, RIGHT('000' + cast(STR.CD_STR_RT as varchar(3)),3) as ALMACEN, BNC.COD_SAP_BNC AS HBKID,
	BNC.COD_SAP_CTA HKTID, CPID.CD_CARDPID IDTC ,PP.PLZ_DIF PLAZO, CPID.ORD_RECAP RECAP, (PP.MONTO+PP.INTERES) IMPORTE,
	PP.IVA_TRX IMPUESTO, CONVERT(VARCHAR(8), TI.TS_DEP,112) AS DATCONT, SEC.SECUENCIA_TRJ SECUENCIA
	FROM TDVAL_EC.dbo.DEP_TICKET TI, ARTS_EC.dbo.PA_STR_RTL STR, TDVAL_EC.dbo.DEP_TCKTND TND, SAADMIN.dbo.MN_TNDBNC BNC,
	ARTS_EC.dbo.PA_BIN_CPID CPID, ARTS_EC.dbo.CO_TND_PINPAD PP, ARTS_EC.dbo.TR_LTM_TND LTM, ARTS_EC.dbo.TR_TRN TRN,
	PI_CONTROL CON, PI_SECUENCIA_INICIAL SEC
	WHERE STR.CD_STR_RT = CON.COD_TIENDA AND CON.COD_TIENDA = SEC.cod_tienda
	AND TI.ID_BSN_DEP = STR.ID_BSN_UN
	AND STR.ID_BSN_UN = TRN.ID_BSN_UN
	AND TRN.ID_TRN = LTM.ID_TRN
	AND LTM.ID_TRN = PP.ID_TRN
	AND SUBSTRING (LTM.ID_ACNT_TND, 1, 6) = CPID.BIN_ENV
	AND LTM.ID_ACNT_TND IS NOT NULL
	AND LTM.MO_ITM_LN_TND = PP.MONTO
	AND CONVERT(INT, CPID.CD_BNC_DEP) = BNC.COD_BANCO
	AND TI.ID_DEP = TND.ID_DEP
	AND TND.ID_TND < 50 AND TND.ID_TND > 40 -- LOS MEDIOS DE PAGO DE TARJ DE CREDITO SON ENTRE 40 Y 50
	AND  CONVERT(DATE, TI.TS_DEP,103)=@v_control_fecha_cierre AND STR.CD_STR_RT IN (select tienda from @tbl_tiendas)

	UPDATE pi_control SET TRJ = '1' 
	WHERE FECHA_CIERRE=@v_control_fecha_cierre and tienda IN (select tienda from @tbl_tiendas)


END


GO
/****** Object:  StoredProcedure [dbo].[PI_LEE_DESCUENTOS_PROV]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_LEE_DESCUENTOS_PROV] 
AS
BEGIN

	SET NOCOUNT ON;

SELECT TIENDA,FECHA,COD_PROMOCION,COD_PROVEEDOR,MATERIAL,CANTIDAD,TOTAL,NOM_PROMOCION,CEBE
FROM PI_DESCUENTOS_PROV
ORDER BY TIENDA, COD_PROMOCION, MATERIAL

END

GO
/****** Object:  StoredProcedure [dbo].[PI_LEE_METODOS_PAGO]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_LEE_METODOS_PAGO] 
AS
BEGIN

DECLARE @VS_MONEDA VARCHAR(4);
DECLARE @V_CONTROL_FECHA_CIERRE DATE;
DECLARE @TBL_TIENDAS TABLE (TIENDA INT)

	SET NOCOUNT ON;

	SELECT @VS_MONEDA = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';
	SELECT TOP 1  @V_CONTROL_FECHA_CIERRE=FECHA_CIERRE FROM PI_CONTROL WHERE FOP = '0' ORDER BY FECHA_CIERRE;
	--SET @V_CONTROL_FECHA_CIERRE = '2016-11-23';
	INSERT INTO @TBL_TIENDAS SELECT TIENDA FROM PI_CONTROL WHERE FOP = '0' AND FECHA_CIERRE = @V_CONTROL_FECHA_CIERRE; 

	SELECT RIGHT('000000' + CAST(P.CD_STR_RT AS VARCHAR(3)),3) AS TIENDA, 		
		@V_CONTROL_FECHA_CIERRE  AS BELEGDATUM, 
		M.ID_SAP AS ZAHLART, 
		CONVERT(NUMERIC(10,2),SUM(L.MO_ITM_LN_TND/100)) SUMME, 'USD' AS WAEHRUNG 
	FROM TR_LTM_TND L 
	    INNER JOIN AS_TND A ON L.ID_TND=A.ID_TND AND A.TY_TND NOT IN ('51','62','63')
		INNER JOIN PI_METODOS_PAGO_EQUIVALENCIA M ON A.TY_TND = M.TY_TND 
		INNER JOIN TR_TRN T ON L.ID_TRN = T.ID_TRN
		INNER JOIN PA_STR_RTL P ON T.ID_BSN_UN = P.ID_BSN_UN
	WHERE CONVERT(DATE, T.DC_DY_BSN,103)=@V_CONTROL_FECHA_CIERRE
		AND P.CD_STR_RT IN (SELECT TIENDA FROM @TBL_TIENDAS)
	GROUP BY P.CD_STR_RT,M.ID_SAP 
	
	UNION ALL
	SELECT RIGHT('000000' + CAST(P.CD_STR_RT AS VARCHAR(3)),3) AS TIENDA, 		
		@V_CONTROL_FECHA_CIERRE  AS BELEGDATUM, 
		'ZCE1' AS ZAHLART, 
		CONVERT(NUMERIC(10,2),SUM(L.MO_ITM_LN_TND/100)) SUMME, 'USD' AS WAEHRUNG 
	FROM TR_LTM_TND L 
		INNER JOIN AS_TND A ON L.ID_TND=A.ID_TND AND A.TY_TND IN ('51')
		INNER JOIN TR_INVC I ON L.ID_TRN = I.ID_TRN
		INNER JOIN PI_METODOS_PAGO_EQUIVALENCIA M ON A.TY_TND = M.TY_TND 
		INNER JOIN TR_TRN T ON L.ID_TRN = T.ID_TRN
		INNER JOIN PA_STR_RTL P ON T.ID_BSN_UN = P.ID_BSN_UN
	WHERE CONVERT(DATE, T.DC_DY_BSN,103)=@V_CONTROL_FECHA_CIERRE
		AND SUBSTRING (L.ID_ACNT_TND, 1, 6) = '821824' AND I.ID_CST <> '0990004196001'
		AND P.CD_STR_RT IN (SELECT TIENDA FROM @TBL_TIENDAS)
	GROUP BY P.CD_STR_RT

	UNION ALL
	SELECT RIGHT('000000' + CAST(P.CD_STR_RT AS VARCHAR(3)),3) AS TIENDA, 		
		@V_CONTROL_FECHA_CIERRE  AS BELEGDATUM, 
		'ZD02' AS ZAHLART, 
		CONVERT(NUMERIC(10,2),SUM(L.MO_ITM_LN_TND/100)) SUMME, 'USD' AS WAEHRUNG 
	FROM TR_LTM_TND L 
		INNER JOIN AS_TND A ON L.ID_TND=A.ID_TND AND A.TY_TND IN ('63')
		INNER JOIN CO_RTNC_CER R ON L.ID_TRN = R.ID_TRN
		INNER JOIN PI_METODOS_PAGO_EQUIVALENCIA M ON A.TY_TND = M.TY_TND 
		INNER JOIN TR_TRN T ON L.ID_TRN = T.ID_TRN
		INNER JOIN PA_STR_RTL P ON T.ID_BSN_UN = P.ID_BSN_UN
	WHERE CONVERT(DATE, T.DC_DY_BSN,103)=@V_CONTROL_FECHA_CIERRE
		AND A.TY_TND = R.TY_TND AND L.ID_TRN = R.ID_TRN AND R.TIPO<>3 AND R.TIPO_PAG=1
		AND P.CD_STR_RT IN (SELECT TIENDA FROM @TBL_TIENDAS)
	GROUP BY P.CD_STR_RT

	UNION ALL
	SELECT RIGHT('000000' + CAST(P.CD_STR_RT AS VARCHAR(3)),3) AS TIENDA, 		
		@V_CONTROL_FECHA_CIERRE  AS BELEGDATUM, 
		'ZD03' AS ZAHLART, 
		CONVERT(NUMERIC(10,2),SUM(L.MO_ITM_LN_TND/100)) SUMME, 'USD' AS WAEHRUNG 
	FROM TR_LTM_TND L 
		INNER JOIN AS_TND A ON L.ID_TND=A.ID_TND AND A.TY_TND IN ('63')
		INNER JOIN CO_RTNC_CER R ON L.ID_TRN = R.ID_TRN
		INNER JOIN PI_METODOS_PAGO_EQUIVALENCIA M ON A.TY_TND = M.TY_TND 
		INNER JOIN TR_TRN T ON L.ID_TRN = T.ID_TRN
		INNER JOIN PA_STR_RTL P ON T.ID_BSN_UN = P.ID_BSN_UN
	WHERE CONVERT(DATE, T.DC_DY_BSN,103)=@V_CONTROL_FECHA_CIERRE
		AND A.TY_TND = R.TY_TND AND L.ID_TRN = R.ID_TRN AND R.TIPO<>3 AND R.TIPO_PAG=2
		AND P.CD_STR_RT IN (SELECT TIENDA FROM @TBL_TIENDAS)
	GROUP BY P.CD_STR_RT
	
	UNION ALL
	SELECT RIGHT('000000' + CAST(P.CD_STR_RT AS VARCHAR(3)),3) AS TIENDA, 		
		@V_CONTROL_FECHA_CIERRE  AS BELEGDATUM, 
		'ZD04' AS ZAHLART, 
		CONVERT(NUMERIC(10,2),SUM(L.MO_ITM_LN_TND/100)) SUMME, 'USD' AS WAEHRUNG 
	FROM TR_LTM_TND L 
		INNER JOIN AS_TND A ON L.ID_TND=A.ID_TND AND A.TY_TND IN ('63')
		INNER JOIN CO_RTNC_CER R ON L.ID_TRN = R.ID_TRN
		INNER JOIN PI_METODOS_PAGO_EQUIVALENCIA M ON A.TY_TND = M.TY_TND 
		INNER JOIN TR_TRN T ON L.ID_TRN = T.ID_TRN
		INNER JOIN PA_STR_RTL P ON T.ID_BSN_UN = P.ID_BSN_UN
	WHERE CONVERT(DATE, T.DC_DY_BSN,103)=@V_CONTROL_FECHA_CIERRE
		AND A.TY_TND = R.TY_TND AND L.ID_TRN = R.ID_TRN AND R.TIPO=3
		AND P.CD_STR_RT IN (SELECT TIENDA FROM @TBL_TIENDAS)
	GROUP BY P.CD_STR_RT

	UNION ALL
	SELECT RIGHT('000000' + CAST(P.CD_STR_RT AS VARCHAR(3)),3) AS TIENDA, 		
		@V_CONTROL_FECHA_CIERRE  AS BELEGDATUM, 
		'ZWTX' AS ZAHLART, 
		CONVERT(NUMERIC(10,2),SUM(L.MO_ITM_LN_TND/100)) SUMME, 'USD' AS WAEHRUNG 
	FROM TR_LTM_TND L 
		INNER JOIN AS_TND A ON L.ID_TND=A.ID_TND AND A.TY_TND IN ('62')
		INNER JOIN CO_RTNC_CER R ON L.ID_TRN = R.ID_TRN
		INNER JOIN PI_METODOS_PAGO_EQUIVALENCIA M ON A.TY_TND = M.TY_TND 
		INNER JOIN TR_TRN T ON L.ID_TRN = T.ID_TRN
		INNER JOIN PA_STR_RTL P ON T.ID_BSN_UN = P.ID_BSN_UN
	WHERE CONVERT(DATE, T.DC_DY_BSN,103)=@V_CONTROL_FECHA_CIERRE
		AND A.TY_TND = R.TY_TND AND L.ID_TRN = R.ID_TRN AND R.TIPO_PAG=1
		AND P.CD_STR_RT IN (SELECT TIENDA FROM @TBL_TIENDAS)
	GROUP BY P.CD_STR_RT
	
	UNION ALL
	SELECT RIGHT('000000' + CAST(P.CD_STR_RT AS VARCHAR(3)),3) AS TIENDA, 		
		@V_CONTROL_FECHA_CIERRE  AS BELEGDATUM, 
		'ZD05' AS ZAHLART, 
		CONVERT(NUMERIC(10,2),SUM(L.MO_ITM_LN_TND/100)) SUMME, 'USD' AS WAEHRUNG 
	FROM TR_LTM_TND L 
		INNER JOIN AS_TND A ON L.ID_TND=A.ID_TND AND A.TY_TND IN ('62')
		INNER JOIN CO_RTNC_CER R ON L.ID_TRN = R.ID_TRN
		INNER JOIN PI_METODOS_PAGO_EQUIVALENCIA M ON A.TY_TND = M.TY_TND 
		INNER JOIN TR_TRN T ON L.ID_TRN = T.ID_TRN
		INNER JOIN PA_STR_RTL P ON T.ID_BSN_UN = P.ID_BSN_UN
	WHERE CONVERT(DATE, T.DC_DY_BSN,103)=@V_CONTROL_FECHA_CIERRE
		AND A.TY_TND = R.TY_TND AND L.ID_TRN = R.ID_TRN AND R.TIPO_PAG=2
		AND P.CD_STR_RT IN (SELECT TIENDA FROM @TBL_TIENDAS)
	GROUP BY P.CD_STR_RT

	ORDER BY TIENDA, ZAHLART

	UPDATE PI_CONTROL SET FOP = '1' 
		WHERE FECHA_CIERRE=@V_CONTROL_FECHA_CIERRE AND TIENDA IN (SELECT TIENDA FROM @TBL_TIENDAS)

END



GO
/****** Object:  StoredProcedure [dbo].[PI_LEE_RETENCIONES]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_LEE_RETENCIONES]
AS
BEGIN

	DECLARE @vs_moneda varchar(4);
	declare @v_control_fecha_cierre date;
	declare @tbl_tiendas table (tienda int)

	SET NOCOUNT ON;

	SELECT @vs_moneda = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';
	SELECT TOP 1  @v_control_fecha_cierre=fecha_cierre FROM pi_control WHERE RET = '0' ORDER BY fecha_cierre;
	insert into @tbl_tiendas select tienda from pi_control where ret = '0' and fecha_cierre = @v_control_fecha_cierre;

	SELECT RIGHT('000' + cast(c.TIENDA as varchar(3)),3) as TIENDA, r.CLNT_ID, r.CLNT_NM, r.FECHA, 
	SUBSTRING(r.TIQUETE,1,3) + SUBSTRING(r.TIQUETE,4,3) + '-' + SUBSTRING(r.TIQUETE,7,9) TIQUETE, 
	r.BASE_IMP, r.PORCENTAJE, r.MONTO, 
	SUBSTRING(r.VOUCHER,1,3) + SUBSTRING(r.VOUCHER,4,3) + '-' + SUBSTRING(r.VOUCHER,7,9) VOUCHER, 
	r.NUMERO_SRI, CONVERT(VARCHAR(8), t.DC_DY_BSN,112) FECHA_PROCESO
	FROM PI_CONTROL c 
	INNER JOIN TR_TRN t ON c.COD_TIENDA = t.ID_BSN_UN AND c.FECHA_CIERRE = CONVERT(DATE, t.DC_DY_BSN)
	INNER JOIN CO_RTNC_CER r ON r.ID_TRN = t.ID_TRN
	WHERE CONVERT(DATE, t.DC_DY_BSN) = CONVERT(DATE, @v_control_fecha_cierre)
	AND c.tienda IN (select tienda from @tbl_tiendas)
	ORDER BY t.ID_BSN_UN

	UPDATE pi_control SET RET = '1' 
	WHERE FECHA_CIERRE=@v_control_fecha_cierre and tienda IN (select tienda from @tbl_tiendas)


END



GO
/****** Object:  StoredProcedure [dbo].[PI_LEE_TIENDAS_CERRADAS]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[PI_LEE_TIENDAS_CERRADAS] AS 
 
DECLARE @cod_tienda int,
		@tienda int,
		@fecha_cierre date,
		@id_trx_cierre int



BEGIN
	SET NOCOUNT ON;

	DECLARE cTiendas CURSOR FOR

	SELECT b.ID_BSN_UN,d.cd_str_rt,CONVERT(DATETIME, b.DC_DY_BSN,103),a.ID_TRN
		FROM TR_BSN_EOD a,TR_TRN b, PA_STR_RTL d
		WHERE a.ID_TRN = b.ID_TRN
    AND b.ID_BSN_UN=d.ID_BSN_UN
    AND CONVERT(DATETIME, b.DC_DY_BSN,103) <= getdate()
    AND CONVERT(DATETIME, b.DC_DY_BSN,103) >= getdate()-30
	ORDER BY b.DC_DY_BSN ASC

	OPEN cTiendas

	FETCH cTiendas INTO  @cod_tienda,@tienda,@fecha_cierre,@id_trx_cierre 

	WHILE (@@FETCH_STATUS = 0 )
	BEGIN

		IF ( SELECT count(*) FROM pi_control where cod_tienda = @cod_tienda and fecha_cierre = @fecha_cierre ) = 0
		BEGIN

		insert into pi_control(cod_tienda, tienda,fecha_cierre,fecha_registro,id_trx_cierre,AGR,FER,MOT,REC,GIF,ILI,BOL,FOP,VAL,
		CTC,SOA,CUP,GAE,CJC,SOB,EFC,TRJ,RET,VAE,SECUENCIA_EFC,CICLO_EFC,SECUENCIA_TRJ,RECEPCION_CAJAS,CONTABILIZAR) VALUES (@cod_tienda,@tienda,@fecha_cierre,getdate(),
		@id_trx_cierre,0, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		CASE WHEN (Select SECUENCIA_EFC FROM PI_CONTROL WHERE cod_tienda = @cod_tienda AND FECHA_CIERRE=(Select MAX(FECHA_CIERRE) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda)) > 0 
		THEN 
			CASE WHEN (Select SECUENCIA_EFC FROM PI_CONTROL WHERE cod_tienda = @cod_tienda AND FECHA_CIERRE=(Select MAX(FECHA_CIERRE) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda)) = 999
			THEN 
				1
			ELSE
				(Select (SECUENCIA_EFC+1) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda AND FECHA_CIERRE=(Select MAX(FECHA_CIERRE) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda))
			END
		ELSE (Select SECUENCIA_EFC FROM PI_SECUENCIA_INICIAL WHERE cod_tienda = @cod_tienda) END,
		CASE WHEN (Select CICLO_EFC FROM PI_CONTROL WHERE cod_tienda = @cod_tienda AND FECHA_CIERRE=(Select MAX(FECHA_CIERRE) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda)) > 0 
		THEN 
			CASE WHEN (Select SECUENCIA_EFC FROM PI_CONTROL WHERE cod_tienda = @cod_tienda AND FECHA_CIERRE=(Select MAX(FECHA_CIERRE) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda)) = 999
			THEN 
				(Select (CICLO_EFC+1) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda AND FECHA_CIERRE=(Select MAX(FECHA_CIERRE) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda))
			ELSE
				(Select CICLO_EFC FROM PI_CONTROL WHERE cod_tienda = @cod_tienda AND FECHA_CIERRE=(Select MAX(FECHA_CIERRE) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda))
			END
		ELSE (Select CICLO_EFC FROM PI_SECUENCIA_INICIAL WHERE cod_tienda = @cod_tienda) END,
		CASE WHEN (Select SECUENCIA_TRJ FROM PI_CONTROL WHERE cod_tienda = @cod_tienda AND FECHA_CIERRE=(Select MAX(FECHA_CIERRE) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda)) > 0 
		THEN 
			CASE WHEN (Select SECUENCIA_TRJ FROM PI_CONTROL WHERE cod_tienda = @cod_tienda AND FECHA_CIERRE=(Select MAX(FECHA_CIERRE) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda)) = 999
			THEN 
				1
			ELSE
				(Select (SECUENCIA_TRJ+1) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda AND FECHA_CIERRE=(Select MAX(FECHA_CIERRE) FROM PI_CONTROL WHERE cod_tienda = @cod_tienda))
			END				 
		ELSE (Select SECUENCIA_TRJ FROM PI_SECUENCIA_INICIAL WHERE cod_tienda = @cod_tienda) END,0,0);
		
		EXEC PI_OBTIENE_DATOS @tienda, @fecha_cierre, @cod_tienda

		END

		FETCH cTiendas INTO  @cod_tienda,@tienda,@fecha_cierre,@id_trx_cierre	

		

	END
	
	CLOSE cTiendas
	DEALLOCATE cTiendas

	


  
END; 


GO
/****** Object:  StoredProcedure [dbo].[PI_LEE_VALES_EMPLEADOS]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[PI_LEE_VALES_EMPLEADOS] 
AS
BEGIN
	SET NOCOUNT ON;

	DECLARE @VD_FECHA_CIERRE  DATE;
	DECLARE @TBL_TIENDAS TABLE (TIENDA INT)

	SELECT TOP 1  @VD_FECHA_CIERRE=FECHA_CIERRE FROM PI_CONTROL WHERE VAE = '0' ORDER BY FECHA_CIERRE;
	--SET @VD_FECHA_CIERRE = '2016-10-05';
	INSERT INTO @TBL_TIENDAS SELECT TIENDA FROM PI_CONTROL WHERE VAE = '0' AND FECHA_CIERRE = @VD_FECHA_CIERRE;
		
    SELECT	V.CED CEDULA, 
			E.ID_SAP COD_TRAN, 
			@VD_FECHA_CIERRE FECHA_INICIO, 
			@VD_FECHA_CIERRE FECHA_FINAL, 
			--CONVERT(NUMERIC(10,2),CAST(V.VALOR AS DECIMAL)/100) VALOR, 
			CONVERT(NUMERIC(10,2),CONVERT(NUMERIC,V.VALOR)/100) VALOR, 
			V.CUOTAS NUM_CUOTA,
			@VD_FECHA_CIERRE FECHA_PROCESO, 
			RIGHT('000' + CAST(S.CD_STR_RT AS VARCHAR(3)),3) COD_ALMACEN,
			CONVERT(NUMERIC(10,2),CONVERT(NUMERIC,V.VALOR)/V.CUOTAS/100) VALOR_CUOTA, 
			CASE WHEN DAY(@VD_FECHA_CIERRE)>20 THEN EOMONTH(@VD_FECHA_CIERRE,1) ELSE EOMONTH(@VD_FECHA_CIERRE)END FECHA_VENCIMIENTO 
	FROM CO_VALE_EMP_CER V 
	INNER JOIN TR_TRN T ON T.ID_TRN = V.ID_TRN 
	INNER JOIN PA_STR_RTL S ON S.ID_BSN_UN = T.ID_BSN_UN 
	INNER JOIN PI_VALES_EMPLEADOS_EQUIVALENCIA E ON E.ID_TOS = V.COD
	WHERE CONVERT(DATE, T.DC_DY_BSN) = @VD_FECHA_CIERRE
	      AND S.CD_STR_RT IN (SELECT TIENDA FROM @TBL_TIENDAS)

	UPDATE PI_CONTROL SET VAE = '1' WHERE FECHA_CIERRE=@VD_FECHA_CIERRE AND TIENDA IN (SELECT TIENDA FROM @TBL_TIENDAS)
END




GO
/****** Object:  StoredProcedure [dbo].[PI_LEE_VENTAS_GIFTCARDS]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_LEE_VENTAS_GIFTCARDS]	
AS
BEGIN

DECLARE @vs_moneda varchar(4);
declare @v_control_fecha_cierre date;
declare @tbl_tiendas table (tienda int)

	SET NOCOUNT ON;

	SELECT @vs_moneda = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';
	SELECT TOP 1  @v_control_fecha_cierre=fecha_cierre FROM pi_control WHERE gif = '0' ORDER BY fecha_cierre;
	--SET @v_control_fecha_cierre = '2016-11-22';
	insert into @tbl_tiendas select tienda from pi_control where gif = '0' and fecha_cierre = @v_control_fecha_cierre;

    SELECT RIGHT('000000' + cast(p.cd_str_rt as varchar(3)),3) as TIENDA,CONVERT(DATE, c.DC_DY_BSN,103) VORGDATUM,@vs_moneda BELEGWAERS,'ZGVS' VORGANGART,'ARTN' QUALARTNR,
	RIGHT('000000000000000000' + cast(i.CD_SAP as varchar(18)),18) ARTNR,CONVERT(numeric(10,3),
	SUM(CASE WHEN h.QU_UN > 0 THEN CASE WHEN h.MO_EXTND < 0 THEN (h.QU_UN/1000)*-1 ELSE h.QU_UN/1000 END
	    ELSE CASE WHEN h.MO_EXTND < 0 THEN h.QU_ITM_LM_RTN_SLS*-1 ELSE h.QU_ITM_LM_RTN_SLS END END))  MENGE,
	CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) IVA,
	CONVERT(numeric(10,2),sum(h.MO_EXTND)/100) + CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) KONDVALUE
    from TR_TRN c, TR_LTM_SLS_RTN h, AS_ITM i, PA_STR_RTL p
    where c.ID_TRN = h.ID_TRN and h.ID_ITM = i.ID_ITM and c.ID_BSN_UN = p.ID_BSN_UN
    AND CONVERT(DATE, c.DC_DY_BSN,103)=@v_control_fecha_cierre
    and p.cd_str_rt IN (select tienda from @tbl_tiendas)
    and i.CD_SAP <> 0 and c.FL_CNCL <> '1' and i.JERARQUIA IN ('SE6001001')
    group by CONVERT(DATE, c.DC_DY_BSN,103),p.cd_str_rt,i.CD_SAP,i.CD_ITM	
	ORDER BY p.cd_str_rt,i.CD_SAP

	UPDATE pi_control SET GIF = '1' 
	WHERE FECHA_CIERRE=@v_control_fecha_cierre and tienda IN (select tienda from @tbl_tiendas)

END


GO
/****** Object:  StoredProcedure [dbo].[PI_LEE_VENTAS_MOTOS]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_LEE_VENTAS_MOTOS]
AS
BEGIN

	SET NOCOUNT ON;

select	RIGHT('000000' + cast(p.TIENDA as varchar(3)),3) as TIENDA
		,CONVERT(VARCHAR(8), c.DC_DY_BSN,112) VORGDATUM
        ,o.CD_OPR  KASSIERER
        ,r.NOMBRE + ' ' + r.APELLIDO_P + ' ' +r.APELLIDO_M as CSHNAME
		,'USD' BELEGWAERS
        ,i.CD_SAP ARTNR
        ,h.QU_ITM_LM_RTN_SLS MENGE
        --segmento 3
        ,h.MO_EXTND KONDVALUE
        ,h.mo_tx IVA
        ,a.srl_nbr FLDVAL
        ,null identificador
        ,'R' estado
            from TR_TRN c INNER JOIN TR_LTM_SLS_RTN h ON c.ID_TRN = h.ID_TRN
			INNER JOIN TR_LTM_MOTO_DT a ON h.ID_TRN = a.ID_TRN AND h.ai_ln_itm=a.ai_ln_itm
			INNER JOIN AS_ITM i ON h.ID_ITM = i.ID_ITM
			INNER JOIN PI_CONTROL p ON  c.ID_BSN_UN = p.COD_TIENDA
			INNER JOIN PA_OPR o ON c.ID_OPR = o.ID_OPR
			INNER JOIN OPERAC_EC.dbo.OP_OPERADOR r ON c.ID_OPR = r.ID_OPERADOR
            where CONVERT(DATE, p.FECHA_CIERRE)=CONVERT(DATE, '2016-06-21')

            and i.CD_SAP <> 0;
END



GO
/****** Object:  StoredProcedure [dbo].[PI_LEE_VENTAS_RECARGAS]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_LEE_VENTAS_RECARGAS]	
AS
BEGIN

DECLARE @vs_moneda varchar(4);
declare @v_control_fecha_cierre date;
declare @tbl_tiendas table (tienda int)

	SET NOCOUNT ON;

	SELECT @vs_moneda = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';
	SELECT TOP 1  @v_control_fecha_cierre=fecha_cierre FROM pi_control WHERE REC = '0' ORDER BY fecha_cierre;
	insert into @tbl_tiendas select tienda from pi_control where rec = '0' and fecha_cierre = @v_control_fecha_cierre;

    SELECT RIGHT('000000' + cast(p.cd_str_rt as varchar(3)),3) as TIENDA,CONVERT(DATE, c.DC_DY_BSN,103) VORGDATUM,@vs_moneda BELEGWAERS,'ZREC' VORGANGART,'ARTN' QUALARTNR,
	RIGHT('000000000000000000' + cast(i.CD_SAP as varchar(18)),18) ARTNR,CONVERT(numeric(10,3),
	SUM(CASE WHEN h.QU_UN > 0 THEN CASE WHEN h.MO_EXTND < 0 THEN (h.QU_UN/1000)*-1 ELSE h.QU_UN/1000 END
	    ELSE CASE WHEN h.MO_EXTND < 0 THEN h.QU_ITM_LM_RTN_SLS*-1 ELSE h.QU_ITM_LM_RTN_SLS END END))  MENGE,
	CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) IVA,
	CONVERT(numeric(10,2),sum(h.MO_EXTND)/100) + CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) KONDVALUE
    from TR_TRN c, TR_LTM_SLS_RTN h, AS_ITM i, PA_STR_RTL p
    where c.ID_TRN = h.ID_TRN and h.ID_ITM = i.ID_ITM and c.ID_BSN_UN = p.ID_BSN_UN
    AND CONVERT(DATE, c.DC_DY_BSN,103)=@v_control_fecha_cierre
    and p.cd_str_rt IN (select tienda from @tbl_tiendas)
    and i.CD_SAP <> 0 and c.FL_CNCL <> '1' and i.JERARQUIA IN ('SB8003004')
    group by CONVERT(DATE, c.DC_DY_BSN,103),p.cd_str_rt,i.CD_SAP,i.CD_ITM	
	ORDER BY p.cd_str_rt,i.CD_SAP

	UPDATE pi_control SET rec = '1' 
	WHERE FECHA_CIERRE=@v_control_fecha_cierre and tienda IN (select tienda from @tbl_tiendas)

END


GO
/****** Object:  StoredProcedure [dbo].[PI_LEE_WPUUMS]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[PI_LEE_WPUUMS]
AS
BEGIN 

DECLARE @vd_fecha_cierre  datetime;
DECLARE @vs_identificador varchar(21); 
DECLARE @vn_contador      int;
DECLARE @vn_agr           int; 
DECLARE @vn_tienda        decimal(38);
declare @tbl_tiendas table (tienda int)

-- El procedimiento que obtiene los datos de las ventas es PI_OBTIENE_DATOS
 
SET NOCOUNT ON;

BEGIN TRANSACTION

    select TOP 1 @vd_fecha_cierre = fecha_cierre from pi_control where agr = '0' order by fecha_cierre
	insert into @tbl_tiendas select tienda from pi_control where agr = '0' and fecha_cierre = @vd_fecha_cierre;
    
	SET @vn_contador =@vn_agr+1;
	SET @vs_identificador=substring(convert(varchar, @vn_tienda),0,6)+'_'+REPLACE(CONVERT(DATE, @vd_fecha_cierre,103),'-','')+'_'+rtrim(ltrim(RIGHT('000000' + cast(@vn_contador as varchar(6)),6)));
  
   select  
    RIGHT('000' + cast(TIENDA as varchar(3)),3) TIENDA,BELEGDATUM,BELEGWAERS,QUALARTNR,ARTNR,

    --MR 24-11-2016 
	--UMSMENGE,MONTO,convert(numeric(10,6),KONDVALUE) KONDVALUE,
	ROUND(sum(CONVERT(numeric(10,3),umsmenge)),3) UMSMENGE,
	ROUND(sum(CONVERT(numeric(10,2),monto)-CONVERT(numeric(10,2),descuento)),2) MONTO,
	ROUND(sum(CONVERT(numeric(10,6),kondvalue)),2) KONDVALUE,
	--MR 24-11-2016 

	IDENTIFICADOR, ESTADO
    from pi_wpuums
    where belegdatum=@vd_fecha_cierre and tienda IN (select tienda from @tbl_tiendas)

    --MR 24-11-2016 
	group by TIENDA,BELEGDATUM,BELEGWAERS,QUALARTNR,ARTNR,IDENTIFICADOR,ESTADO
	--MR 24-11-2016 

	order by TIENDA ASC,ESTADO DESC ,ARTNR ASC;
  
  update pi_control
  set agr='1'
  where tienda IN (select tienda from @tbl_tiendas)
  and fecha_cierre=@vd_fecha_cierre;

  COMMIT 
  
END;


GO
/****** Object:  StoredProcedure [dbo].[PI_OBTIENE_DATOS]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[PI_OBTIENE_DATOS] (@v_control_tienda int, @v_control_fecha_cierre date, @v_control_cod_tienda int) AS
 BEGIN 
 
DECLARE @vs_moneda       varchar(4);
declare @wpuums table (TIENDA varchar(3),ID_TRN int, AI_LN_ITM int, BELEGDATUM date, BELEGWAERS varchar(4), ARTNR varchar(20),
COD_ITM bigint,KONDVALUE varchar(20),UMSMENGE varchar(20),MONTO varchar(20),DESCUENTO varchar(20),
IDENTIFICADOR varchar(20),ESTADO varchar(1))

SET NOCOUNT ON;

  SELECT @vs_moneda = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';

	/* 
	tabla TR_TRN transacciones
	tabla TR_LTM_SLS_RTN items de la venta
	tabla AS_ITM mestro de articulos
	tabla TR_LTM_RTL_TRN estado de articulos devueltos
	tabla TR_RTN indica que transacción es una nota de credito
	tabla TR_LTM_PRM para conocer que item tiene descuento y restar el decsuento
	campo FL_VD_LN_ITM indica si el item esta anulado en ese caso se debe restar
	campo FL_CNCL indica que la factura esta anulada para que no se considere en el Idoc
	Campo FL_VD indica que se anulo el descuento sobre el item
	Un select para facturas u otro la NC unidos por un UNION para traer todas las transacciones por almacen

	Este procedimiento llena la tabla PI_WPUUMS que luego es leida por el procedimiento PI_LEE_WPUUMS
	*/

	  INSERT INTO @wpuums(TIENDA,ID_TRN,AI_LN_ITM,BELEGDATUM,BELEGWAERS,ARTNR,COD_ITM,KONDVALUE,UMSMENGE,MONTO,DESCUENTO,IDENTIFICADOR,ESTADO)
      SELECT @v_control_tienda,c.ID_TRN,t.AI_LN_ITM,CONVERT(DATE, c.DC_DY_BSN,103),@vs_moneda,RIGHT('000000000000000000' + cast(i.CD_SAP as varchar(18)),18) CD_SAP,i.CD_ITM,	  
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CONVERT(numeric(10,3),(CONVERT(numeric(10,3),h.mo_tx))/1000) 
	  ELSE CONVERT(numeric(10,3),(CONVERT(numeric(10,3),h.mo_tx*-1))/1000) 
	  END MO_TX,
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN 
		CASE WHEN h.QU_UN = '0.00' THEN	CONVERT(numeric(10,3),h.QU_ITM_LM_RTN_SLS)
		ELSE CONVERT(numeric(10,3),h.QU_UN/1000)
		END
	  ELSE 
		CASE WHEN h.QU_UN = '0.00' THEN	CONVERT(numeric(10,3),h.QU_ITM_LM_RTN_SLS*-1)
		ELSE CONVERT(numeric(10,3),(h.QU_UN/1000)*-1)
		END
	  END QU_ITM_LM_RTN_SLS,
	  CASE WHEN t.FL_VD_LN_ITM = 0 THEN
		    CONVERT(numeric(10,3),h.MO_EXTND)/100+ CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx)/1000) 
	  ELSE  
		   CONVERT(numeric(10,3),h.MO_EXTND*-1)/100+ CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx*-1)/1000) 
	  END MO_EXTND,
	  CASE 
	    WHEN (p.FL_VD = 0) THEN (p.MO_PRM)/100
		WHEN (p.FL_VD = 1) THEN (p.MO_PRM*-1)/100
		ELSE '0.00'
	  END MO_PRM,
	  null,'-' 
      FROM TR_TRN c INNER JOIN TR_LTM_SLS_RTN h ON c.ID_TRN = h.ID_TRN AND c.FL_CNCL <> '1'
	  INNER JOIN AS_ITM i ON h.ID_ITM = i.ID_ITM
	  INNER JOIN TR_LTM_RTL_TRN t ON h.ID_TRN = t.ID_TRN AND h.AI_LN_ITM = t.AI_LN_ITM
	  LEFT JOIN TR_RTN r ON h.ID_TRN = r.ID_TRN
	  INNER JOIN TR_LTM_PRM p ON h.ID_TRN = p.ID_TRN and h.AI_LN_ITM = p.AI_LN_ITM 
	  WHERE CONVERT(DATE, c.DC_DY_BSN,103)=@v_control_fecha_cierre
      AND c.ID_BSN_UN=@v_control_cod_tienda
      AND i.CD_ITM != '811099999998' AND r.ID_TRN is null AND 
	  i.CD_SAP <> 0 and i.JERARQUIA NOT IN ('FA3001001','SB8003004','SE6001001','SE6003003','SE6004004','S96001001')

	  UNION ALL

      SELECT @v_control_tienda,c.ID_TRN,t.AI_LN_ITM,CONVERT(DATE, c.DC_DY_BSN,103),@vs_moneda,RIGHT('000000000000000000' + cast(i.CD_SAP as varchar(18)),18) CD_SAP,i.CD_ITM,	  
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CONVERT(numeric(10,3),(CONVERT(numeric(10,3),h.mo_tx))/1000,2) 
	  ELSE CONVERT(numeric(10,3),(CONVERT(numeric(10,3),h.mo_tx*-1))/1000,2) 
	  END MO_TX,
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CONVERT(numeric(10,3),h.QU_ITM_LM_RTN_SLS)
	  ELSE CONVERT(numeric(10,3),h.QU_ITM_LM_RTN_SLS*-1) 
	  END QU_ITM_LM_RTN_SLS,
	  CASE WHEN t.FL_VD_LN_ITM = 0 THEN
		    CONVERT(numeric(10,3),h.MO_EXTND)/100+ CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx)/1000) 
	  ELSE  
		   CONVERT(numeric(10,3),h.MO_EXTND*-1)/100+ CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx*-1)/1000) 
	  END MO_EXTND,
	  CASE 
	    WHEN (p.FL_VD = 0) THEN (p.MO_PRM)/100
		WHEN (p.FL_VD = 1) THEN (p.MO_PRM*-1)/100
		ELSE '0.00'
	  END MO_PRM,
	  null,'+' 
      FROM TR_TRN c INNER JOIN TR_LTM_SLS_RTN h ON c.ID_TRN = h.ID_TRN AND c.FL_CNCL <> '1'
	  INNER JOIN AS_ITM i ON h.ID_ITM = i.ID_ITM
	  INNER JOIN TR_LTM_RTL_TRN t ON h.ID_TRN = t.ID_TRN AND h.AI_LN_ITM = t.AI_LN_ITM
	  LEFT JOIN TR_RTN r ON h.ID_TRN = r.ID_TRN
	  LEFT JOIN TR_LTM_PRM p ON h.ID_TRN = p.ID_TRN and h.AI_LN_ITM = p.AI_LN_ITM 
	  WHERE CONVERT(DATE, c.DC_DY_BSN,103)=@v_control_fecha_cierre
      AND c.ID_BSN_UN=@v_control_cod_tienda
      AND i.CD_ITM != '811099999998' AND r.ID_TRN is not null AND 
	  i.CD_SAP <> 0 and i.JERARQUIA NOT IN ('FA3001001','SB8003004','SE6001001','SE6003003','SE6004004','S96001001')


	  INSERT INTO pi_wpuums(TIENDA,BELEGDATUM,BELEGWAERS,QUALARTNR,ARTNR,COD_ITM,KONDVALUE,UMSMENGE,MONTO,DESCUENTO,IDENTIFICADOR,ESTADO,FECHA_LECTURA)
	  SELECT TIENDA,BELEGDATUM,BELEGWAERS,'ARTN',ARTNR,COD_ITM,KONDVALUE,UMSMENGE,MONTO,SUM(CONVERT(numeric(10,3),DESCUENTO)),null,ESTADO,GETDATE() 
	  FROM @wpuums
	  GROUP BY TIENDA,ID_TRN,AI_LN_ITM,BELEGDATUM,BELEGWAERS,ARTNR,COD_ITM,KONDVALUE,UMSMENGE,MONTO,ESTADO


	  
END; 
    


GO
/****** Object:  StoredProcedure [dbo].[PI_OBTIENE_DATOS_BACKUP]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[PI_OBTIENE_DATOS_BACKUP] (@v_control_tienda int, @v_control_fecha_cierre date, @v_control_cod_tienda int) AS
 BEGIN 

--tiendas cerradas
--DECLARE control CURSOR LOCAL for SELECT cod_tienda,tienda,fecha_cierre FROM pi_control where AGR='0' ORDER BY fecha_cierre;

    
DECLARE @vs_moneda       varchar(4);


 
SET NOCOUNT ON;

  --OPEN control;
  SELECT @vs_moneda = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';
  
    --FETCH control INTO @v_control_cod_tienda, @v_control_tienda, @v_control_fecha_cierre;  
    
    --WHILE (@@FETCH_STATUS = 0 )
	--BEGIN

      insert into pi_wpuums(TIENDA,BELEGDATUM,BELEGWAERS,QUALARTNR,ARTNR,COD_ITM,KONDVALUE,UMSMENGE,MONTO,IDENTIFICADOR,ESTADO,FECHA_LECTURA)
      select @v_control_tienda,CONVERT(DATE, c.DC_DY_BSN,103),@vs_moneda,'ARTN',RIGHT('000000000000000000' + cast(i.CD_SAP as varchar(18)),18) CD_SAP,i.CD_ITM,
	  CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) MO_TX,
	  CONVERT(numeric(10,3),SUM(CASE WHEN h.QU_UN > 0 THEN CASE WHEN h.MO_EXTND < 0 THEN (h.QU_UN/1000)*-1 ELSE h.QU_UN/1000 END
	  ELSE CASE WHEN h.MO_EXTND < 0 THEN h.QU_ITM_LM_RTN_SLS*-1 ELSE h.QU_ITM_LM_RTN_SLS END END))  QU_ITM_LM_RTN_SLS,
	  CONVERT(numeric(10,2),sum(h.MO_EXTND)/100) + CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) MO_EXTND,null,
	  '+',GETDATE() 
      from TR_TRN c, TR_LTM_SLS_RTN h, AS_ITM i
      where c.ID_TRN = h.ID_TRN
      and h.ID_ITM = i.ID_ITM
      AND CONVERT(DATE, c.DC_DY_BSN,103)=@v_control_fecha_cierre
      and c.ID_BSN_UN=@v_control_cod_tienda
      and i.CD_SAP <> 0 and c.FL_CNCL <> '1' and i.JERARQUIA NOT IN ('FA3001001','SB8003004','SE6001001','SE6003003','SE6004004','S96001001')
      group by CONVERT(DATE, c.DC_DY_BSN,103),i.CD_SAP,i.CD_ITM
	  having SUM(CASE WHEN h.MO_EXTND < 0 THEN h.QU_ITM_LM_RTN_SLS*-1 ELSE h.QU_ITM_LM_RTN_SLS END ) > 0

      --FETCH control INTO @v_control_cod_tienda, @v_control_tienda, @v_control_fecha_cierre;
    --END;
    --CLOSE control;
    --DEALLOCATE control;

END; 
    


GO
/****** Object:  StoredProcedure [dbo].[PI_OBTIENE_DATOS_BACKUP _III]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[PI_OBTIENE_DATOS_BACKUP _III] (@v_control_tienda int, @v_control_fecha_cierre date, @v_control_cod_tienda int) AS
 BEGIN 
 
DECLARE @vs_moneda       varchar(4);

SET NOCOUNT ON;

  SELECT @vs_moneda = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';

	/* 
	tabla TR_TRN transacciones
	tabla TR_LTM_SLS_RTN items de la venta
	tabla AS_ITM mestro de articulos
	tabla TR_LTM_RTL_TRN estado de articulos devueltos
	tabla TR_RTN indica que transacción es una nota de credito
	tabla TR_LTM_PRM para conocer que item tiene descuento y restar el decsuento
	campo FL_VD_LN_ITM indica si el item esta anulado en ese caso se debe restar
	campo FL_CNCL indica que la factura esta anulada para que no se considere en el Idoc
	Campo FL_VD indica que se anulo el descuento sobre el item
	Un select para facturas u otro la NC unidos por un UNION para traer todas las transacciones por almacen

	Este procedimiento llena la tabla PI_WPUUMS que luego es leida por el procedimiento PI_LEE_WPUUMS
	*/

	  INSERT INTO pi_wpuums(TIENDA,BELEGDATUM,BELEGWAERS,QUALARTNR,ARTNR,COD_ITM,KONDVALUE,UMSMENGE,MONTO,IDENTIFICADOR,ESTADO,FECHA_LECTURA)
      SELECT @v_control_tienda,CONVERT(DATE, c.DC_DY_BSN,103),@vs_moneda,'ARTN',RIGHT('000000000000000000' + cast(i.CD_SAP as varchar(18)),18) CD_SAP,i.CD_ITM,	  
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CONVERT(numeric(10,3),(CONVERT(numeric(10,3),h.mo_tx))/1000,2) 
	  ELSE CONVERT(numeric(10,3),(CONVERT(numeric(10,3),h.mo_tx*-1))/1000,2) 
	  END MO_TX,
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CONVERT(numeric(10,3),h.QU_ITM_LM_RTN_SLS)
	  ELSE CONVERT(numeric(10,3),h.QU_ITM_LM_RTN_SLS*-1) 
	  END QU_ITM_LM_RTN_SLS,
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CASE WHEN (p.MO_PRM is NULL) 
		   THEN CONVERT(numeric(10,3),h.MO_EXTND)/100+ CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx)/1000) 
		   ELSE CONVERT(numeric(10,3),(h.MO_EXTND)/100 - (p.MO_PRM)/100) + CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx)/1000) 
		   END 
	  ELSE CASE WHEN (p.MO_PRM is NULL) 
		   THEN CONVERT(numeric(10,3),h.MO_EXTND*-1)/100+ CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx*-1)/1000) 
		   ELSE CONVERT(numeric(10,3),(h.MO_EXTND*-1)/100 - (p.MO_PRM*-1)/100) + CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx*-1)/1000,2) 
		   END 
	  END MO_EXTND,
	  null,'-',GETDATE() 
      FROM TR_TRN c INNER JOIN TR_LTM_SLS_RTN h ON c.ID_TRN = h.ID_TRN AND c.FL_CNCL <> '1'
	  INNER JOIN AS_ITM i ON h.ID_ITM = i.ID_ITM
	  INNER JOIN TR_LTM_RTL_TRN t ON h.ID_TRN = t.ID_TRN AND h.AI_LN_ITM = t.AI_LN_ITM
	  LEFT JOIN TR_RTN r ON h.ID_TRN = r.ID_TRN
	  LEFT JOIN TR_LTM_PRM p ON h.ID_TRN = p.ID_TRN and h.AI_LN_ITM = p.AI_LN_ITM AND p.FL_VD ='0'
	  WHERE CONVERT(DATE, c.DC_DY_BSN,103)=@v_control_fecha_cierre
      AND c.ID_BSN_UN=@v_control_cod_tienda
      AND i.CD_ITM != '811099999998' AND r.ID_TRN is null AND
	  i.CD_SAP <> 0 and i.JERARQUIA NOT IN ('FA3001001','SB8003004','SE6001001','SE6003003','SE6004004','S96001001')

	  UNION ALL

      SELECT @v_control_tienda,CONVERT(DATE, c.DC_DY_BSN,103),@vs_moneda,'ARTN',RIGHT('000000000000000000' + cast(i.CD_SAP as varchar(18)),18) CD_SAP,i.CD_ITM,	  
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CONVERT(numeric(10,3),(CONVERT(numeric(10,3),h.mo_tx))/1000,2) 
	  ELSE CONVERT(numeric(10,3),(CONVERT(numeric(10,3),h.mo_tx*-1))/1000,2) 
	  END MO_TX,
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CONVERT(numeric(10,3),h.QU_ITM_LM_RTN_SLS)
	  ELSE CONVERT(numeric(10,3),h.QU_ITM_LM_RTN_SLS*-1) 
	  END QU_ITM_LM_RTN_SLS,
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CASE WHEN (p.MO_PRM is NULL) 
		   THEN CONVERT(numeric(10,3),h.MO_EXTND)/100+ CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx)/1000) 
		   ELSE CONVERT(numeric(10,3),(h.MO_EXTND)/100 - (p.MO_PRM)/100) + CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx)/1000) 
		   END 
	  ELSE CASE WHEN (p.MO_PRM is NULL) 
		   THEN CONVERT(numeric(10,3),h.MO_EXTND*-1)/100+ CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx*-1)/1000) 
		   ELSE CONVERT(numeric(10,3),(h.MO_EXTND*-1)/100 - (p.MO_PRM*-1)/100) + CONVERT(numeric(10,3),CONVERT(numeric(10,3),h.mo_tx*-1)/1000,2) 
		   END 
	  END MO_EXTND,
	  null,'+',GETDATE() 
      FROM TR_TRN c INNER JOIN TR_LTM_SLS_RTN h ON c.ID_TRN = h.ID_TRN AND c.FL_CNCL <> '1'
	  INNER JOIN AS_ITM i ON h.ID_ITM = i.ID_ITM
	  INNER JOIN TR_LTM_RTL_TRN t ON h.ID_TRN = t.ID_TRN AND h.AI_LN_ITM = t.AI_LN_ITM
	  LEFT JOIN TR_RTN r ON h.ID_TRN = r.ID_TRN
	  LEFT JOIN TR_LTM_PRM p ON h.ID_TRN = p.ID_TRN and h.AI_LN_ITM = p.AI_LN_ITM AND p.FL_VD ='0'
	  WHERE CONVERT(DATE, c.DC_DY_BSN,103)=@v_control_fecha_cierre
      AND c.ID_BSN_UN=@v_control_cod_tienda
      AND i.CD_ITM != '811099999998' AND r.ID_TRN is not null AND
	  i.CD_SAP <> 0 and i.JERARQUIA NOT IN ('FA3001001','SB8003004','SE6001001','SE6003003','SE6004004','S96001001')
END; 
    


GO
/****** Object:  StoredProcedure [dbo].[PI_OBTIENE_DATOS_BACKUP_II]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[PI_OBTIENE_DATOS_BACKUP_II] (@v_control_tienda int, @v_control_fecha_cierre date, @v_control_cod_tienda int) AS
 BEGIN 

--tiendas cerradas
--DECLARE control CURSOR LOCAL for SELECT cod_tienda,tienda,fecha_cierre FROM pi_control where AGR='0' ORDER BY fecha_cierre;

    
DECLARE @vs_moneda       varchar(4);


 
SET NOCOUNT ON;

  --OPEN control;
  SELECT @vs_moneda = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';
  
    --FETCH control INTO @v_control_cod_tienda, @v_control_tienda, @v_control_fecha_cierre;  
    
    --WHILE (@@FETCH_STATUS = 0 )
	--BEGIN
	/* 
	tabla TR_TRN transacciones
	tabla TR_LTM_SLS_RTN items de la venta
	tabla AS_ITM mestro de articulos
	tabla TR_LTM_RTL_TRN estado de articulos devueltos
	tabla TR_RTN indica que transacción es una nota de credito
	tabla TR_LTM_PRM para conocer que item tiene descuento y restar el decsuento
	campo FL_VD_LN_ITM indica si el item esta anulado en ese caso se debe restar
	campo FL_CNCL indica que la factura esta anulada para que no se considere en el Idoc
	Campo FL_VD indica que se anulo el descuento sobre el item
	Un select para facturas u otro la NC unidos por un UNION para traer todas las transacciones por almacen

	Este procedimiento llena la tabla PI_WPUUMS que luego es leida por el procedimiento PI_LEE_WPUUMS



	
	*/
	  INSERT INTO pi_wpuums(TIENDA,BELEGDATUM,BELEGWAERS,QUALARTNR,ARTNR,COD_ITM,KONDVALUE,UMSMENGE,MONTO,IDENTIFICADOR,ESTADO,FECHA_LECTURA)
      SELECT @v_control_tienda,CONVERT(DATE, c.DC_DY_BSN,103),@vs_moneda,'ARTN',RIGHT('000000000000000000' + cast(i.CD_SAP as varchar(18)),18) CD_SAP,i.CD_ITM,	  
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) 
	  ELSE CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx*-1))/1000,2)) 
	  END MO_TX,
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CONVERT(numeric(10,3),SUM(h.QU_ITM_LM_RTN_SLS)) 
	  ELSE CONVERT(numeric(10,3),SUM(h.QU_ITM_LM_RTN_SLS*-1)) 
	  END QU_ITM_LM_RTN_SLS,
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CASE WHEN (p.MO_PRM is NULL) 
		   THEN CONVERT(numeric(10,2),sum(h.MO_EXTND)/100)+ CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) 
		   ELSE CONVERT(numeric(10,2),(sum(h.MO_EXTND)/100) - (SUM(p.MO_PRM)/100)) + CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) 
		   END 
	  ELSE CASE WHEN (p.MO_PRM is NULL) 
		   THEN CONVERT(numeric(10,2),sum(h.MO_EXTND)/100)+ CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) 
		   ELSE CONVERT(numeric(10,2),(sum(h.MO_EXTND*-1)/100) - (SUM(p.MO_PRM*-1)/100)) + CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx*-1))/1000,2)) 
		   END 
	  END MO_EXTND,
	  null,'-',GETDATE() 
      FROM TR_TRN c INNER JOIN TR_LTM_SLS_RTN h ON c.ID_TRN = h.ID_TRN AND c.FL_CNCL <> '1'
	  INNER JOIN AS_ITM i ON h.ID_ITM = i.ID_ITM
	  INNER JOIN TR_LTM_RTL_TRN t ON h.ID_TRN = t.ID_TRN AND h.AI_LN_ITM = t.AI_LN_ITM
	  LEFT JOIN TR_RTN r ON h.ID_TRN = r.ID_TRN
	  LEFT JOIN TR_LTM_PRM p ON h.ID_TRN = p.ID_TRN and h.AI_LN_ITM = p.AI_LN_ITM AND p.FL_VD ='0'
	  WHERE CONVERT(DATE, c.DC_DY_BSN,103)=@v_control_fecha_cierre
      AND c.ID_BSN_UN=@v_control_cod_tienda
      AND i.CD_ITM != '811099999998' AND r.ID_TRN is null AND
	  i.CD_SAP <> 0 and i.JERARQUIA NOT IN ('FA3001001','SB8003004','SE6001001','SE6003003','SE6004004','S96001001')
      group by CONVERT(DATE, c.DC_DY_BSN,103),i.CD_SAP,i.CD_ITM,p.MO_PRM,t.FL_VD_LN_ITM
	  having SUM(CASE WHEN t.FL_VD_LN_ITM = 0 THEN h.QU_ITM_LM_RTN_SLS ELSE h.QU_ITM_LM_RTN_SLS*-1 END ) > 0
	  UNION
      SELECT @v_control_tienda,CONVERT(DATE, c.DC_DY_BSN,103),@vs_moneda,'ARTN',RIGHT('000000000000000000' + cast(i.CD_SAP as varchar(18)),18) CD_SAP,i.CD_ITM,	  
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) 
	  ELSE CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx*-1))/1000,2)) 
	  END MO_TX,
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CONVERT(numeric(10,3),SUM(h.QU_ITM_LM_RTN_SLS)) 
	  ELSE CONVERT(numeric(10,3),SUM(h.QU_ITM_LM_RTN_SLS*-1)) 
	  END QU_ITM_LM_RTN_SLS,
	  CASE WHEN t.FL_VD_LN_ITM = 0 
	  THEN CASE WHEN (p.MO_PRM is NULL) 
		   THEN CONVERT(numeric(10,2),sum(h.MO_EXTND)/100)+ CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) 
		   ELSE CONVERT(numeric(10,2),(sum(h.MO_EXTND)/100) - (SUM(p.MO_PRM)/100)) + CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) 
		   END 
	  ELSE CASE WHEN (p.MO_PRM is NULL) 
		   THEN CONVERT(numeric(10,2),sum(h.MO_EXTND)/100)+ CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx))/1000,2)) 
		   ELSE CONVERT(numeric(10,2),(sum(h.MO_EXTND*-1)/100) - (SUM(p.MO_PRM*-1)/100)) + CONVERT(numeric(10,2),ROUND(sum(CONVERT(numeric(10,2),h.mo_tx*-1))/1000,2)) 
		   END 
	  END MO_EXTND,
	  null,'+',GETDATE() 
      FROM TR_TRN c INNER JOIN TR_LTM_SLS_RTN h ON c.ID_TRN = h.ID_TRN AND c.FL_CNCL <> '1'
	  INNER JOIN AS_ITM i ON h.ID_ITM = i.ID_ITM
	  INNER JOIN TR_LTM_RTL_TRN t ON h.ID_TRN = t.ID_TRN AND h.AI_LN_ITM = t.AI_LN_ITM
	  LEFT JOIN TR_RTN r ON h.ID_TRN = r.ID_TRN
	  LEFT JOIN TR_LTM_PRM p ON h.ID_TRN = p.ID_TRN and h.AI_LN_ITM = p.AI_LN_ITM AND p.FL_VD ='0'
	  WHERE CONVERT(DATE, c.DC_DY_BSN,103)=@v_control_fecha_cierre
      AND c.ID_BSN_UN=@v_control_cod_tienda
      AND i.CD_ITM != '811099999998' AND r.ID_TRN is not null AND
	  i.CD_SAP <> 0 and i.JERARQUIA NOT IN ('FA3001001','SB8003004','SE6001001','SE6003003','SE6004004','S96001001')
      group by CONVERT(DATE, c.DC_DY_BSN,103),i.CD_SAP,i.CD_ITM,p.MO_PRM,t.FL_VD_LN_ITM
	  having SUM(CASE WHEN t.FL_VD_LN_ITM = 0 THEN h.QU_ITM_LM_RTN_SLS ELSE h.QU_ITM_LM_RTN_SLS*-1 END ) > 0

    --FETCH control INTO @v_control_cod_tienda, @v_control_tienda, @v_control_fecha_cierre;
    --END;
    --CLOSE control;
    --DEALLOCATE control;

END; 
    


GO
/****** Object:  StoredProcedure [dbo].[PI_OBTIENE_DESCUENTOS_PROV]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[PI_OBTIENE_DESCUENTOS_PROV] (@v_control_tienda int, @v_control_fecha_cierre date, @v_control_cod_tienda int) AS
--ALTER PROCEDURE [dbo].[PI_OBTIENE_DESCUENTOS_PROV] AS
 BEGIN 
 
    SET NOCOUNT ON;

	--DECLARE @v_control_fecha_cierre  DATE;
	DECLARE @promociones table (TIENDA varchar(3), FECHA date, COD_PROMOCION varchar(20), COD_PROVEEDOR varchar(20),
	MATERIAL varchar(18), CANTIDAD varchar(7), TOTAL varchar(20), NOM_PROMOCION varchar(255),CEBE varchar(10))

	--SET @v_control_fecha_cierre = '2016-11-22';

	  --INSERT INTO PI_DESCUENTOS_PROV  
	  INSERT INTO @promociones(TIENDA,FECHA,COD_PROMOCION,COD_PROVEEDOR,MATERIAL,CANTIDAD,TOTAL,NOM_PROMOCION,CEBE)    
	  SELECT @v_control_tienda tienda, CONVERT(DATE, c.DC_DY_BSN,103) fecha, p.cd_prm, pp.cd_prv prov,
	  RIGHT('000000000000000000' + cast(i.CD_SAP as varchar(18)),18) material, 
	  CASE 
	    WHEN (p.FL_VD = 0) THEN 1
		WHEN (p.FL_VD = 1) THEN -1
	  END cant,
	  CASE 
	    WHEN (p.FL_VD = 0) THEN (p.MO_PRM)/100
		WHEN (p.FL_VD = 1) THEN (p.MO_PRM*-1)/100
	  END total, 
	  pp.DES_PRM promo,a.ce_cobe cebe

      FROM TR_TRN c 
	  INNER JOIN TR_LTM_SLS_RTN h ON c.ID_TRN = h.ID_TRN AND c.FL_CNCL <> '1'
	  INNER JOIN AS_ITM i ON h.ID_ITM = i.ID_ITM
	  INNER JOIN TR_LTM_RTL_TRN t ON h.ID_TRN = t.ID_TRN AND h.AI_LN_ITM = t.AI_LN_ITM
	  LEFT JOIN TR_RTN r ON h.ID_TRN = r.ID_TRN
	  LEFT JOIN TR_LTM_PRM p ON h.ID_TRN = p.ID_TRN and h.AI_LN_ITM = p.AI_LN_ITM
	  INNER JOIN PA_STR_RTL a ON a.ID_BSN_UN = c.ID_BSN_UN 
	  INNER JOIN CO_PRM_PRV pp ON p.CD_PRM=pp.CD_PRM
  
	  WHERE CONVERT(DATE, c.DC_DY_BSN,103)=@v_control_fecha_cierre
	  AND c.ID_BSN_UN=@v_control_cod_tienda
      AND i.CD_ITM != '811099999998' AND p.CD_PRM is not null AND
	  i.CD_SAP <> 0 and i.JERARQUIA NOT IN ('FA3001001','SB8003004','SE6001001','SE6003003','SE6004004','S96001001')
	  order by i.CD_ITM 
	  --validación de promocion proveedor

--      group by CONVERT(DATE, c.DC_DY_BSN,103),c.ID_BSN_UN,a.ce_cobe, p.CD_PRM,i.CD_SAP,h.id_itm,pp.cd_prv,pp.DES_PRM --,i.CD_ITM,p.MO_PRM,t.FL_VD_LN_ITM
	  --having SUM(CASE WHEN t.FL_VD_LN_ITM = 0 THEN h.QU_ITM_LM_RTN_SLS ELSE h.QU_ITM_LM_RTN_SLS*-1 END ) > 0

	  INSERT INTO PI_DESCUENTOS_PROV(TIENDA,FECHA,COD_PROMOCION,COD_PROVEEDOR,MATERIAL,CANTIDAD,TOTAL,NOM_PROMOCION,CEBE,FECHA_LECTURA)
	  SELECT TIENDA,FECHA,COD_PROMOCION,COD_PROVEEDOR,MATERIAL,SUM(CONVERT(numeric(10,2),CANTIDAD)) CANTIDAD,
	  SUM(CONVERT(numeric(10,2),TOTAL)) TOTAL,NOM_PROMOCION,CEBE,GETDATE() 
	  FROM @promociones
	  GROUP BY TIENDA,FECHA,COD_PROMOCION,COD_PROVEEDOR,MATERIAL,NOM_PROMOCION,CEBE 
	
	
	
	
	  
END; 
GO
/****** Object:  StoredProcedure [dbo].[PI_PAGO_CAJA_CHICA]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_PAGO_CAJA_CHICA] 
AS
BEGIN
	SET NOCOUNT ON;

	DECLARE @vd_fecha_cierre  date;

	SET @vd_fecha_cierre = '2016-05-23';

	SELECT	right('000' + cast(a.cd_str_rt as varchar(3)),3) TIENDA,
			@vd_fecha_cierre VORGDATUM,
			'CCCA' VORGANGART,
		    '11' POSNR,
			'02' POSNR2,
			g.COMP ZUONR, 
			r.mo_tot_rtl_trn WRBTR,
			'USD' WAERS 
    FROM CO_GASTO_EFE_CER g
	INNER JOIN TR_TRN t ON t.ID_TRN = g.ID_TRN 
	INNER JOIN PA_STR_RTL a ON a.ID_BSN_UN = t.ID_BSN_UN 
	INNER JOIN TR_TOT_RTL r ON r.ID_TRN = g.ID_TRN AND r.ID_TR_TOT_TYP = 1
	WHERE CONVERT(DATE, t.DC_DY_BSN) = @vd_fecha_cierre

END



GO
/****** Object:  StoredProcedure [dbo].[PI_PAGO_CUENTAS_CORRIENTES]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_PAGO_CUENTAS_CORRIENTES] 
AS
BEGIN
	SET NOCOUNT ON;

	DECLARE @vd_fecha_cierre  date;

	SET @vd_fecha_cierre = '2016-05-23';

	SELECT	right('000' + cast(a.cd_str_rt as varchar(3)),3) TIENDA,
			@vd_fecha_cierre VORGDATUM,
			'INGV' VORGANGART,
		    '01' POSNR,
			'03' POSNR2,
			r.mo_tot_rtl_trn WRBTR,
			'USD' WAERS 
    FROM CO_RECAUDOS g
	INNER JOIN TR_TRN t ON t.ID_TRN = g.ID_TRN 
	INNER JOIN PA_STR_RTL a ON a.ID_BSN_UN = t.ID_BSN_UN 
	INNER JOIN TR_TOT_RTL r ON r.ID_TRN = g.ID_TRN AND r.ID_TR_TOT_TYP = 1
	WHERE CONVERT(DATE, t.DC_DY_BSN) = @vd_fecha_cierre	
	
END



GO
/****** Object:  StoredProcedure [dbo].[PI_PAGO_GASTOS_EFECTIVO]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_PAGO_GASTOS_EFECTIVO] 
AS
BEGIN
	SET NOCOUNT ON;

	DECLARE @VS_MONEDA VARCHAR(4);
	DECLARE @VD_FECHA_CIERRE  DATE;
	DECLARE @TBL_TIENDAS TABLE (TIENDA INT)

	SELECT @VS_MONEDA = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';
	SELECT TOP 1  @VD_FECHA_CIERRE=FECHA_CIERRE FROM PI_CONTROL WHERE GAE = '0' ORDER BY FECHA_CIERRE;
	--SET @VD_FECHA_CIERRE = '2016-10-28';
	INSERT INTO @TBL_TIENDAS SELECT TIENDA FROM PI_CONTROL WHERE GAE = '0' AND FECHA_CIERRE = @VD_FECHA_CIERRE;

	SELECT	RIGHT('000' + CAST(A.CD_STR_RT AS VARCHAR(3)),3) TIENDA,
			@VD_FECHA_CIERRE VORGDATUM,
			'CEXP' VORGANGART,
		    '21' POSNR,--RIGHT('00' + G.COD,2)  
			'02' POSNR2,
			--ZUONR
			--KNTOBJECT
			A.CE_COBE KOSTL,
			CONVERT(NUMERIC(10,2),SUM(CONVERT(NUMERIC,G.VALOR)) / 100) WRBTR, 
			@VS_MONEDA WAERS 
    FROM CO_GASTO_EFE_CER G
	INNER JOIN TR_TRN T ON T.ID_TRN = G.ID_TRN 
	INNER JOIN PA_STR_RTL A ON A.ID_BSN_UN = T.ID_BSN_UN 
	WHERE CONVERT(DATE, T.DC_DY_BSN) = @VD_FECHA_CIERRE
	      AND A.CD_STR_RT IN (SELECT TIENDA FROM @TBL_TIENDAS)
	GROUP BY  A.CD_STR_RT, A.CE_COBE;

    UPDATE PI_CONTROL SET GAE = '1' WHERE FECHA_CIERRE=@VD_FECHA_CIERRE AND TIENDA IN (SELECT TIENDA FROM @TBL_TIENDAS)

END



GO
/****** Object:  StoredProcedure [dbo].[PI_PAGO_SOBRANTES_CAJA]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[PI_PAGO_SOBRANTES_CAJA] 
AS
BEGIN
	SET NOCOUNT ON;

/*
SELECT TI.ID_DEP AS ID_DEPOSITO, STR.CD_STR_RT AS COD_ALMACEN, TI.TS_DEP AS FECHA, 
CASE WHEN SF.TY_SYF = 1 THEN SF.MO_SYF END SOBRANTE, 
CASE WHEN SF.TY_SYF = 2 THEN SF.MO_SYF END FALTANTE
FROM TDVAL_EC.dbo.DEP_TICKET TI, ARTS_EC.dbo.PA_STR_RTL STR, TDVAL_EC.dbo.DEP_TCKSYF SF
WHERE TI.ID_DEP = SF.ID_DEP
AND TI.ID_BSN_DEP = STR.ID_BSN_UN

select * FROM TDVAL_EC.dbo.DEP_TICKET
select * FROM TDVAL_EC.dbo.DEP_TCKSYF
*/

	DECLARE @VS_MONEDA VARCHAR(4);
	DECLARE @VD_FECHA_CIERRE  DATE;
	DECLARE @TBL_TIENDAS TABLE (TIENDA INT)

	SELECT @VS_MONEDA = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';
	--SELECT TOP 1  @VD_FECHA_CIERRE=FECHA_CIERRE FROM PI_CONTROL WHERE SOB = '0' ORDER BY FECHA_CIERRE;
	SET @VD_FECHA_CIERRE = '2016-10-05';
	--INSERT INTO @TBL_TIENDAS SELECT TIENDA FROM PI_CONTROL WHERE SOB = '0' AND FECHA_CIERRE = @VD_FECHA_CIERRE;

	SELECT	RIGHT('000' + CAST(A.CD_STR_RT AS VARCHAR(3)),3) TIENDA,
			@VD_FECHA_CIERRE VORGDATUM,
			'SOBR' VORGANGART,
		    '13' POSNR, 
			'12' POSNR2,
			A.CE_COBE ZUONR,
			CONCAT('R',RIGHT('000' + CAST(A.CD_STR_RT AS VARCHAR(3)),3)) KNTOBJECT, 
			--KOSTL
			CONVERT(NUMERIC(10,2),SUM(CONVERT(NUMERIC,CASE WHEN SF.TY_SYF = 1 THEN SF.MO_SYF END)) / 100) WRBTR, 
			@VS_MONEDA WAERS 
    FROM TDVAL_EC.dbo.DEP_TICKET TI
	INNER JOIN TDVAL_EC.dbo.DEP_TCKSYF SF ON TI.ID_DEP = SF.ID_DEP
	INNER JOIN PA_STR_RTL A ON A.ID_BSN_UN =  TI.ID_BSN_DEP
	WHERE CONVERT(DATE, TI.TS_DEP) = @VD_FECHA_CIERRE
	  -- AND A.CD_STR_RT IN (SELECT TIENDA FROM @TBL_TIENDAS)
	GROUP BY  A.CD_STR_RT, A.CE_COBE;

    --UPDATE PI_CONTROL SET SOB = '1' WHERE FECHA_CIERRE=@VD_FECHA_CIERRE AND TIENDA IN (SELECT TIENDA FROM @TBL_TIENDAS)
	
END



GO
/****** Object:  StoredProcedure [dbo].[PI_PAGO_VALES_EMPLEADOS]    Script Date: 12/19/2016 6:14:46 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Manuel Ramirez
-- Create date: 20160622
-- Description:	
-- =============================================

CREATE PROCEDURE [dbo].[PI_PAGO_VALES_EMPLEADOS] 
AS
BEGIN
	SET NOCOUNT ON;

	DECLARE @VS_MONEDA VARCHAR(4);
	DECLARE @VD_FECHA_CIERRE  DATE;
	DECLARE @TBL_TIENDAS TABLE (TIENDA INT)

	SELECT @VS_MONEDA = DESCRIPCION FROM PI_PARAMETROS WHERE ID='MONEDA';
	SELECT TOP 1  @VD_FECHA_CIERRE=FECHA_CIERRE FROM PI_CONTROL WHERE VAL = '0' ORDER BY FECHA_CIERRE;
	--SET @VD_FECHA_CIERRE = '2016-10-05';
	INSERT INTO @TBL_TIENDAS SELECT TIENDA FROM PI_CONTROL WHERE VAL = '0' AND FECHA_CIERRE = @VD_FECHA_CIERRE;

	SELECT	RIGHT('000' + CAST(A.CD_STR_RT AS VARCHAR(3)),3) TIENDA,
			@VD_FECHA_CIERRE VORGDATUM,
			'APEM' VORGANGART,
		    G.COD_SOC_SAP POSNR, --S.CLAVE_CONT POSNR,
			'02' POSNR2,
			--ZUONR
			--KNTOBJECT
			--KOSTL
			CONVERT(NUMERIC(10,2),SUM(CONVERT(NUMERIC,VALOR)) / 100) WRBTR, 
			@VS_MONEDA WAERS 
    FROM CO_VALE_EMP_CER G
	INNER JOIN TR_TRN T ON T.ID_TRN = G.ID_TRN 
	INNER JOIN PA_STR_RTL A ON A.ID_BSN_UN = T.ID_BSN_UN 
	--INNER JOIN CO_SOCIEDAD_CER S ON S.COD_SOC = G.COD_SOC_SAP	
	WHERE CONVERT(DATE, T.DC_DY_BSN) = @VD_FECHA_CIERRE
	      AND A.CD_STR_RT IN (SELECT TIENDA FROM @TBL_TIENDAS)
	GROUP BY  A.CD_STR_RT, G.COD_SOC_SAP;

    UPDATE PI_CONTROL SET VAL = '1' WHERE FECHA_CIERRE=@VD_FECHA_CIERRE AND TIENDA IN (SELECT TIENDA FROM @TBL_TIENDAS)

END
;
