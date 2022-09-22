package com.allc.arms.server.operations.tsl.receiver;

import org.apache.log4j.Logger;

import com.allc.arms.server.operations.file.receiver.FileReceiverOperation;

public class TSLReceiverOperation extends FileReceiverOperation {

        @Override
	protected void initialize(){
		log = Logger.getLogger(TSLReceiverOperation.class);
	}


}
