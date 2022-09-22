/**
 * 
 */
package com.allc.core.operation;

import java.util.Map;

import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public abstract class AbstractOperation extends Thread {
	
	public Map staticProperties;

	public abstract boolean shutdown(long timeToWait);

	public abstract boolean process(ConnSocketServer socket, Frame frame, PropFile properties);

	public abstract boolean process(ConnPipeServer pipe, Frame frame, PropFile properties);
}
