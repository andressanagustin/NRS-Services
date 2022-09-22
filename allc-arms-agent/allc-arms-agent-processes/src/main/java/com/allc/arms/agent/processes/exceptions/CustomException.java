/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.agent.processes.exceptions;

/**
 *
 * @author Tyrone Lopez
 */
public class CustomException extends Exception {

    private CustomException(String message) {
        super(message);
    }

    public static CustomException customMessage(String mensaje) {
        return new CustomException(mensaje);
    }
    
    public static CustomException credencialesInvalidas() {
        return new CustomException("No existen credenciales asociadas a este cliente");
    }

    public static CustomException credencialesNoDatos() {
        return new CustomException("No existen datos asociados");
    }
}
