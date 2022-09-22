package com.allc.arms.utils.eyes;

import java.util.Date;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;

public class Eyes {
	private static PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private static Logger log = Logger.getLogger(Eyes.class.getName());
	
	public static void write(String descriptorPro, String idLocal, int tipoEq, String abrevTipoEstado, String descripcion) {
		// VER --> REVISAR DATOS QUE NO SEAN NULL
		try {
			log.info("Guardando eyes");
			UtilityFile.createWriteDataFile(getEyesFileName(), descriptorPro + "|" + prop.getHostName() + "|" + tipoEq + "|" + prop.getHostAddress() + "|" + 
					idLocal + "|" + abrevTipoEstado + "|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|" + descripcion
					, true);	
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
	}
	
	private static String getEyesFileName() {
		return prop.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

}
