package com.allc.arms.server.operations.colas;

public class AutorizacionResponse {

	private boolean estado;
	private DsError error;
	public boolean isEstado() {
		return estado;
	}
	public void setEstado(boolean estado) {
		this.estado = estado;
	}
	public DsError getError() {
		return error;
	}
	public void setError(DsError error) {
		this.error = error;
	}
	
	
}
