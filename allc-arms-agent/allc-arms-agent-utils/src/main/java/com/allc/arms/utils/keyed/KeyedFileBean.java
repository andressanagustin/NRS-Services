package com.allc.arms.utils.keyed;

import com.ibm.OS4690.KeyedFile;

public class KeyedFileBean {

	private String  pathAndFileName;
	private String  mode;
	private int     access;
	private byte	fileType;
	private byte	distributionMethod;
	private int 	keyLength;
	private int		recordSize;
	private int		numberOfRecords;
	private int		randomizingDivisor;
	private int 	chainingThreshold;
	private KeyedFile KeyedFile;
	

	public String getMode() {
		return mode;
	}
	public int getAccess() {
		return access;
	}
	public byte getFileType() {
		return fileType;
	}
	public byte getDistributionMethod() {
		return distributionMethod;
	}
	public int getKeyLength() {
		return keyLength;
	}
	public int getRecordSize() {
		return recordSize;
	}
	public int getNumberOfRecords() {
		return numberOfRecords;
	}
	public int getRandomizingDivisor() {
		return randomizingDivisor;
	}
	public int getChainingThreshold() {
		return chainingThreshold;
	}
	public KeyedFile getKeyedFile() {
		return KeyedFile;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	public void setAccess(int access) {
		this.access = access;
	}
	public void setFileType(byte fileType) {
		this.fileType = fileType;
	}
	public void setDistributionMethod(byte distributionMethod) {
		this.distributionMethod = distributionMethod;
	}
	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}
	public void setRecordSize(int recordSize) {
		this.recordSize = recordSize;
	}
	public void setNumberOfRecords(int numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}
	public void setRandomizingDivisor(int randomizingDivisor) {
		this.randomizingDivisor = randomizingDivisor;
	}
	public void setChainingThreshold(int chainingThreshold) {
		this.chainingThreshold = chainingThreshold;
	}
	public void setKeyedFile(KeyedFile keyedFile) {
		KeyedFile = keyedFile;
	}
	public String getPathAndFileName() {
		return pathAndFileName;
	}
	public void setPathAndFileName(String pathAndFileName) {
		this.pathAndFileName = pathAndFileName;
	}	
	

	
}
