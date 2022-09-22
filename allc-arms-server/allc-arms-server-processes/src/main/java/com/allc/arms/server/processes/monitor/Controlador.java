package com.allc.arms.server.processes.monitor;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Controlador {
	@SerializedName("codigo tienda")
	private int id_local;
	private int cant_operaciones;
    private float monto;
    @SerializedName("list")
    private List<Pos> posList;
    
    
    
    public int getId_local() {
		return id_local;
	}



	public void setId_local(int id_local) {
		this.id_local = id_local;
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



	public List<Pos> getPosList() {
		return posList;
	}



	public void setPosList(List<Pos> posList) {
		this.posList = posList;
	}



	@Override
    public String toString() {
        return "Controler{" +
        		"id_local=" + id_local +
                ",cant_operaciones=" + cant_operaciones +
                ", monto=" + monto +
                ", posList=" + posList +
                '}';
    }
}
