package uy.gub.agesic.connector.session;

import java.io.File;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uy.gub.agesic.connector.entity.GlobalConfiguration;
import uy.gub.agesic.connector.exceptions.GlobalConfigurationException;
import uy.gub.agesic.connector.session.api.GlobalConfigurationManager;
import uy.gub.agesic.connector.util.Constants;
import uy.gub.agesic.connector.util.Props;

@Stateless
public class GlobalConfigurationManagerSession implements GlobalConfigurationManager {
	
	private static Log log = LogFactory.getLog(GlobalConfigurationManagerSession.class);
	
	@PersistenceContext
	private EntityManager em;
	
	public void createGlobalConfiguration(GlobalConfiguration globalConf) throws GlobalConfigurationException {
		try{
			
			em.persist(globalConf);
			log.debug("persist globalConf");
			
		}
		catch (Exception e){
			if (e instanceof GlobalConfigurationException) {
				throw (GlobalConfigurationException) e;
			}
			throw new GlobalConfigurationException(e);
		}
	}
	

	public void editGlobalConfiguration(GlobalConfiguration globalConf) throws GlobalConfigurationException {
		try {
			em.merge(globalConf);
			log.debug("merge globalConfiguration");
		}
		catch (Exception e){
			log.error("Error on merge GlobalConfiguration", e);
			throw new GlobalConfigurationException(e);
		}
	}
	
	public String getProperty(String prop, String filepath, String defaultValue) {
		return Props.getInstance().getProp(prop, filepath, defaultValue);
	}


	public String getBasicPath() {
		try {
			String basic_path = this.getProperty("BASIC_PATH", Constants.PATH_PROPERTIES_FILE, Constants.DEFAULT_BASICPATH); //"connector-pge/connector-pge.properties");
			File dirBasicPath = new File(basic_path);
			if (!dirBasicPath.exists()) {
				dirBasicPath.mkdir();
			}
			if (!basic_path.endsWith("/")) {
				basic_path = basic_path + "/";
			}
			return basic_path;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public GlobalConfiguration getGlobalConfiguration(String connectorType) {
		Query queryPath = em.createNamedQuery("globalConfiguration.all");
		queryPath.setParameter("type", connectorType);
		
		List<GlobalConfiguration> globsPaths = queryPath.getResultList();
		if (globsPaths != null && globsPaths.size() > 0) {
			return globsPaths.get(0);
		}
		
		return null;
	}

	
}
