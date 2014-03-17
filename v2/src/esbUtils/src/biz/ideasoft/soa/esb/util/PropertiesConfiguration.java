package biz.ideasoft.soa.esb.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jboss.internal.soa.esb.util.StreamUtils;
import org.jboss.soa.esb.ConfigurationException;

public class PropertiesConfiguration {
	
	protected static Logger _logger = Logger.getLogger(PropertiesConfiguration.class);

	private static Properties properties;

	public static String getProperty(String name) {
		return getProperty(name, null);
	}

	public static String getProperty(String name, String defaultValue) {
		String value = PropertiesUtilities.replaceProperties(name, getProperties());
		if (value != null && value.trim().length() > 0) {
			return value; 
		} else {
			return defaultValue;
		}
	}

	public synchronized static void resetProperties() {
		properties = null;		
	}

	private synchronized static Properties getProperties() {
		if (properties == null) {
			try {
				InputStream stream = StreamUtils.getResource("/config.properties");
				if (stream != null) {
					properties = new Properties();
					try {
						properties.load(stream);
					} catch (Exception e) {
						_logger.error("Cannot read config.properties on esbUtils.esb", e);
						properties = null;
					}
				} else {
					_logger.warn("Cannot read config.properties on esbUtils.esb");
				}
			} catch (ConfigurationException ce) {
				_logger.warn("Cannot read config.properties: " + ce.getMessage());
				_logger.debug(ce);
			}
		}
		return properties;
	}
	
}
