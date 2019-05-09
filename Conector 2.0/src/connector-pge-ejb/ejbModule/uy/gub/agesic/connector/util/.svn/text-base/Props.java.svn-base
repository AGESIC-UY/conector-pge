package uy.gub.agesic.connector.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Props {

	private static Logger log = Logger.getLogger(Props.class);
	private static Props instance = null;
	
	Map<String, Properties> hashProps = new HashMap<String, Properties>();

	protected Props() {
	}

	public static Props getInstance() {
		if (instance == null) {
			instance = new Props();
		}
		return instance;
	}

	public String getProp(String name, String propfile, String defaultValue) {
		if (! hashProps.containsKey(propfile)){
			try {
				InputStream i = Props.class.getClassLoader().getResourceAsStream(propfile);
				Properties p = new Properties();
				p.load(i);
				i.close();
				
				hashProps.put(propfile, p);
			} catch (Exception e) {
				log.debug("No existe el archivo properties en la ruta '" + propfile + "'. Se crea un properties vaci­o.", e);
				hashProps.put(propfile, new Properties());
			}
		}
		
		try {
			if (hashProps.get(propfile).containsKey(name)){
				return hashProps.get(propfile).getProperty(name);
			} else {
				log.debug("No existe la propiedad '"+ name + "' para el archivo '" + propfile + "'. Se devuelve el valor por defecto: '" + defaultValue + "'");
				hashProps.get(propfile).setProperty(name, defaultValue);
				return defaultValue;
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
			return null;
		}

	}
}
