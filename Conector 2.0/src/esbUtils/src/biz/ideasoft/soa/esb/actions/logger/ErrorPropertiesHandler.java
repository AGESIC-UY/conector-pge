package biz.ideasoft.soa.esb.actions.logger;

import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;


public class ErrorPropertiesHandler {

	private Logger logger = Logger.getLogger(ErrorPropertiesHandler.class);

	private static ErrorPropertiesHandler instance = null;
	private Properties prop;

	private static final String ERROR_CODES_PROPERTIES = "/agesic/errorCodes.properties";

	private ErrorPropertiesHandler() {
		cargarConfiguracion();
	}

	public static ErrorPropertiesHandler getInstance() {
		if (instance == null) {
			instance = new ErrorPropertiesHandler();
		}
		return instance;
	}

	private void cargarConfiguracion() {
		if (prop != null) {
			return;
		}
		
		InputStream in = ErrorPropertiesHandler.class.getResourceAsStream(ERROR_CODES_PROPERTIES);
		if (in != null) {
			prop = new Properties();
			try {
				prop.load(in);
				in.close();
			} catch (Exception exc) {
				logger.fatal("No se pudieron cargar las propiedades del archivo de configuracion", exc);
				return;
			}
		} else {
			logger.fatal("No se encontro el archivo de configuracion del sistema");
		}
	}

	public String getProperty(String name) {
		return prop.getProperty(name);
	}



}
