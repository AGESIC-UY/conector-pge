package uy.gub.agesic.connector.session.api;

import java.util.List;

import javax.ejb.Remote;

import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.entity.FullConnector;
import uy.gub.agesic.connector.exceptions.ConnectorException;

@Remote
public interface ConnectorManager {

	Long createConnector(Connector connector) throws ConnectorException;
	
	void deleteConnector(Long id) throws ConnectorException;

	List<Connector> getConnectors(String type) throws ConnectorException;

	//obtain the connector without byte[]
	Connector getConnector(Long id) throws ConnectorException;
	
	//obtain the connector with byte[]
	Connector getConnectorToExport(Long id) throws ConnectorException;
	
	//Connector importConnector(InputStream inputStream);

	FullConnector getConnectorByPath(String path, Boolean production) throws ConnectorException;
	
	FullConnector getConnectorByName(String name, boolean production) throws ConnectorException;

	void editConnector(Connector connector) throws ConnectorException;
	
	String getProperty(String prop, String filepath, String defaultValue);
	
	String getBasicPath();
	
	boolean checkConnectorValidation(Connector connector) throws ConnectorException;
}
