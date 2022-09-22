package com.allc.util;

import java.util.Timer;

import com.allc.main.Main;

public class Interrupcion extends Thread{
	private Timer m_oTimer;
	public Interrupcion(Timer p_oTimer)
	{
	super();
	m_oTimer = p_oTimer;
	}

	public void run()
	{
		try {
			 Main.setEndOfService(true);
			Thread.sleep(2000);
		} catch (Exception e) {
			System.out.println(e);
		}
		m_oTimer.cancel();
	}
	
}
