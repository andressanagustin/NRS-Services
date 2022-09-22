package com.allc.arms.server.processes.monitor;

public class OperacionesBD {
	private String id;
	private float monto;
	private int cantidad;
	
	
	public OperacionesBD(String id, float monto, int cantidad) {
		super();
		this.id = id;
		this.monto = monto;
		this.cantidad = cantidad;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public float getMonto() {
		return monto;
	}


	public void setMonto(float monto) {
		this.monto = monto;
	}


	public int getCantidad() {
		return cantidad;
	}


	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}


	@Override
	public String toString() {
		return "OperacionesBD [id=" + id + ", monto=" + monto + ", cantidad=" + cantidad + "]";
	}
	
	
	
	
}
