package com.allc.files.helper;

public class ControllerFiles {
	
	public static String unpack(byte abyte0[]) { // System.setProperty( "file.encoding", "UTF-8" );
		String salida = "";
		StringBuffer stringbuffer = new StringBuffer();
		for (int i = 0; i < abyte0.length; i++) {
			char c1 = mapToChar((byte) ((abyte0[i] & 0xf0) >> 4));
			char c3 = mapToChar((byte) (abyte0[i] & 0xf));
			stringbuffer.append(c1);
			stringbuffer.append(c3);

		}
		salida = stringbuffer.toString();
		salida = salida.replaceAll("F", "0");
		salida = salida.replaceAll("D", "-");
		return salida;
	}

	protected static char mapToChar(byte byte0) {
		char c;
		if (byte0 < 10)
			c = (char) (48 + byte0);
		else
			c = (char) (65 + (byte0 - 10));
		return c;
	}

}
