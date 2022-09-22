/**
 * 
 */
package com.allc.printer.helper;

import java.util.Timer;

/**
 * @author GUSTAVOK
 * 
 */
public class Interrupcion extends Thread {
	private Timer m_oTimer;
	private Process process;

	public Interrupcion(Timer p_oTimer, Process process) {
		super();
		m_oTimer = p_oTimer;
		this.process = process;
	}

	public void run() {
		process.shutdown();
		m_oTimer.cancel();
	}

}
