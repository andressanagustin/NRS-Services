package com.allc.util;


import java.io.IOException;
import java.util.Timer;

import com.allc.main.Main;








public class Interrupcion extends Thread{

	//static Logger log = Logger.getLogger(Interrupcion.class);
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
		} catch (IOException e2) {

			System.out.println("Inter1" + e2.fillInStackTrace());
		} catch (InterruptedException e) {

			System.out.println("Inter2" + e.fillInStackTrace());
		}


	m_oTimer.cancel();
	}
	
}
