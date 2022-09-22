package com.allc.arms.utils.tsl;

public class ELConstants {

	/** String Types of the Transaction summary log **/
	public static class ELTypeCode {
		public static final int CASHIER_LOAN_FROM_CONTROLLER = 20;
		public static final int CASHIER_PICKUP_FROM_CONTROLLER = 21;
		public static final int TRANSFER_TENDER_FROM_CONTROLLER = 23;
		public static final int CARRY_FORWARD_FROM_CONTROLLER = 24;
	}
	
	/**Detail of the String Type 00 = TRANSACTION_HEADER**/
	public static class Positions {
		public static final int TERMINAL = 0;
		public static final int TRANSNUM = 1;
		public static final int DATETIME = 2;
		public static final int TRANTYPE = 3;
		public static final int OPERATOR = 4;
		public static final int OPERATOR_TERMINAL = 5;
		public static final int INDICAT0 = 6;
		public static final int CASH = 7;
		public static final int CHECKS = 8;
		public static final int FOODSTAMP = 9;
		public static final int MISC1 = 10;
		public static final int MISC2 = 11;
		public static final int MISC3 = 12;
		public static final int MFRCPN = 13;
		public static final int STRCPN = 14;
		
	}
	
	public static class TerminalCode {
		public static final String CONTROLLER_TERM = "0";
	}
}
