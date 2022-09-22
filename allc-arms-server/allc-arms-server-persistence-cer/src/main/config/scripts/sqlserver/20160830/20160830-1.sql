CREATE TABLE CO_RESERVA
   (	
	ID_RSV				int IDENTITY (1, 1) NOT NULL,
	ID_ITM 				int NOT NULL, 
	ID_BSN_UN 			int NOT NULL, 
	CD_RSV 				int NOT NULL, 
	FECHA_DSD 			datetime, 
	FECHA_HST			datetime, 
	NUM_SERIE			bigint NULL,
	FL_RSV_USED			bit NULL DEFAULT 0, 
	
	PRIMARY KEY (ID_RSV, ID_ITM, ID_BSN_UN)
   )

    ALTER TABLE CO_RESERVA ADD FOREIGN KEY (ID_ITM,ID_BSN_UN) REFERENCES AS_ITM_STR (ID_ITM,ID_BSN_UN)
    
update PM_PARVAL set VAL_PARAM='5:com.allc.arms.server.operations.customer.ConsultaCustomerOperation,10:com.allc.arms.server.operations.tsl.TSLReaderOperation,11:com.allc.arms.server.operations.el.ELReaderOperation,13:com.allc.arms.server.operations.cer.cedpadruc.ConsultaCedPadRucOperation,14:com.allc.arms.server.operations.cer.retencion.ConsultaRetencionOperation,15:com.allc.arms.server.operations.cer.itemUpdate.UpdateItemOperation,16:com.allc.arms.server.operations.cer.suspTrans.GenerateSuspTransOperation,18:com.allc.arms.server.operations.cer.cuponRedimible.ConsultaCuponRedimibleOperation,19:com.allc.arms.server.operations.cer.devolucion.ConsultaDevolucionOperation,20:com.allc.arms.server.operations.cer.devolucion.UpdateDevolucionOperation,21:com.allc.arms.server.operations.customer.data.ConsultaCustomerDataOperation,22:com.allc.arms.server.operations.cer.moto.ConsultaMotoOperation,23:com.allc.arms.server.operations.cer.moto.ConsultaGerentesTiendaOperation,24:com.allc.arms.server.operations.params.LoadParamsOperation,25:com.allc.arms.server.operations.cer.sociedad.ConsultaClaveSociedadOperation,27:com.allc.arms.server.operations.cer.store.ConsultaStoreOperation,28:com.allc.arms.server.operations.cer.arqueo.ConsultaArqueoPorMedioDePago,29:com.allc.arms.server.operations.cer.codopera.UpdateCodeOperaOperation,30:com.allc.arms.server.operations.cer.epslog.EPSLogReaderOperation,33:com.allc.arms.server.operations.cer.reserva.ConsultaReservaOperation,34:com.allc.arms.server.operations.cer.terminal.ConsultaUltimoNumFacturaOperation,35:com.allc.arms.server.operations.fileUpdate.FileUpdaterDownOperation' 
where ID_PARVAL='14';

update PM_PARVAL set VAL_PARAM='5:com.allc.arms.agent.operations.customer.ConsultaCustomerOperation,7:com.allc.arms.agent.operations.operator.OperatorUpdateOperation,13:com.allc.arms.agent.operations.cer.cedpadruc.ConsultaCedPadRucOperation,14:com.allc.arms.agent.operations.cer.retencion.ConsultaRetencionOperation,16:com.allc.arms.agent.processes.cer.suspTrans.GenerateSuspTransProcess,18:com.allc.arms.agent.operations.cer.cuponRedimible.ConsultaCuponRedimibleOperation,19:com.allc.arms.agent.operations.cer.devolucion.ConsultaDevolucionOperation,20:com.allc.arms.agent.operations.cer.devolucion.UpdateDevolucionOperation,21:com.allc.arms.agent.operations.customer.data.ConsultaCustomerDataOperation,22:com.allc.arms.agent.operations.cer.moto.ConsultaMotoOperation,23:com.allc.arms.agent.operations.cer.moto.ConsultaGerentesTiendaOperation,25:com.allc.arms.agent.operations.cer.sociedad.ConsultaClaveSociedadOperation,26:com.allc.arms.agent.operations.operator.OperatorPassWordUpdateOperation,27:com.allc.arms.agent.operations.cer.store.ConsultaStoreOperation,28:com.allc.arms.agent.operations.cer.arqueo.ConsultaArqueoPorMedioDePago,31:com.allc.arms.agent.operations.cer.syscard.WriteTramaSyscardOperation,32:com.allc.arms.agent.operations.cer.ebil.UpdateEbilFileOperation,33:com.allc.arms.agent.operations.cer.reserva.ConsultaReservaOperation,34:com.allc.arms.agent.operations.cer.terminal.ConsultaUltimoNumFacturaOperation,36:com.allc.arms.agent.operations.cer.syscard.ConsultaSeqSyscardOperation' 
where ID_PARVAL='74';