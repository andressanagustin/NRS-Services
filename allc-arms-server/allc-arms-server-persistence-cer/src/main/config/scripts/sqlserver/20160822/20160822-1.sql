	 CREATE TABLE CO_FERRI_ALM
   (	
	ID_TRN 		int NOT NULL, 
	TRN_NBR 	VARCHAR(20), 
	ALM_NBR 	VARCHAR(4),
	PRIMARY KEY (ID_TRN)
   )
   
   ALTER TABLE CO_FERRI_ALM ADD FOREIGN KEY (ID_TRN) REFERENCES TR_RTL (ID_TRN)
   
     CREATE TABLE CO_DEDUCIBLE
   (	
	ID_TRN 			int NOT NULL, 
	DEDUC_COMEST 	VARCHAR(9), 
	DEDUC_ROPA 		VARCHAR(9), 
	DEDUC_ESC 		VARCHAR(9),
	PRIMARY KEY (ID_TRN)
   )
   
   ALTER TABLE CO_DEDUCIBLE ADD FOREIGN KEY (ID_TRN) REFERENCES TR_RTL (ID_TRN)