/**
 * 
 */
package com.allc.core.process;

import java.util.Timer;

import com.allc.core.receiver.ReceiverManager;

/**
 * @author GUSTAVOK
 * 
 */
public class Interrupcion extends Thread {
	protected Timer m_oTimer;
	protected LauncherProcess launcherProcess;
	protected ReceiverManager receiverManager;

	public Interrupcion(Timer p_oTimer, LauncherProcess launcherProcess) {
		super();
		m_oTimer = p_oTimer;
		this.launcherProcess = launcherProcess;
	}

	public Interrupcion(Timer p_oTimer, ReceiverManager receiverManager) {
		super();
		m_oTimer = p_oTimer;
		this.receiverManager = receiverManager;
	}

	public void run() {
		if (launcherProcess != null)
			launcherProcess.shutdown();
		if (receiverManager != null)
			receiverManager.shutdown();
		m_oTimer.cancel();
	}

}
