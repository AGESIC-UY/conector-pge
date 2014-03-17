package uy.gub.agesic.connector.session.api;


import javax.ejb.Remote;
import uy.gub.agesic.connector.entity.GlobalConfiguration;
import uy.gub.agesic.connector.exceptions.GlobalConfigurationException;


@Remote
public interface GlobalConfigurationManager {

	void createGlobalConfiguration(GlobalConfiguration globalConf) throws GlobalConfigurationException;
	
	void editGlobalConfiguration(GlobalConfiguration globalConf) throws GlobalConfigurationException;
	
	String getBasicPath() ;
	
	GlobalConfiguration getGlobalConfiguration(String connectorType);
	
}
