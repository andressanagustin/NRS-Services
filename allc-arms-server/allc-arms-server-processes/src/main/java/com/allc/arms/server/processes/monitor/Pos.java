package com.allc.arms.server.processes.monitor;

import com.google.gson.annotations.SerializedName;

public class Pos {
	@SerializedName("pv")
    private String codigo;
	@SerializedName("can_operaciones")
    private int cant_operaciones;
    private float monto;
    
    
    
    public String getCodigo() {
		return codigo;
	}



	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}



	public int getCant_operaciones() {
		return cant_operaciones;
	}



	public void setCant_operaciones(int cant_operaciones) {
		this.cant_operaciones = cant_operaciones;
	}



	public float getMonto() {
		return monto;
	}



	public void setMonto(float monto) {
		this.monto = monto;
	}



	@Override
    public String toString() {
        return "Pos{" +
                "codigo='" + codigo + '\'' +
                ", cant_operaciones=" + cant_operaciones +
                ", monto=" + monto +
                '}';
    }
}
