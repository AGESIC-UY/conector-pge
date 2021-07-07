package uy.gub.agesic.esb.action;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.Properties;
import org.jboss.soa.esb.message.ResponseHeader;
import org.jboss.soa.esb.services.registry.RegistryException;

import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.entity.ConnectorPaths;
import uy.gub.agesic.connector.entity.FullConnector;
import uy.gub.agesic.connector.exceptions.ConnectorException;

import org.jboss.internal.soa.esb.util.StreamUtils;

import biz.ideasoft.soa.esb.util.SoapUtil;

public class ResourceReplyAction extends AbstractActionPipelineProcessor {

	protected ConfigTree config;
	protected static Logger _logger = Logger.getLogger(ResourceReplyAction.class);
	
	public ResourceReplyAction(ConfigTree config) throws ConfigurationException, RegistryException {
		
		this.config = config;
	}
	
	public Message process(Message message) throws ActionProcessingException {
		
		Properties properties = message.getProperties();
		
		String location = "http://" + properties.getProperty("host") + properties.getProperty("Path");// + "/?" + properties.getProperty("Query");  
		properties.setProperty("location", location);
		properties.setProperty("Content-Type", new ResponseHeader("Content-Type", "application/xml;charset=UTF-8"));
		
		boolean soapFault = false;
		FullConnector fullConnector = (FullConnector) message.getBody().remove("fullConnector");  
		
		if (fullConnector != null) {
			Connector connectorConfig = fullConnector.getConnector();
			if (connectorConfig != null) {
				
				// busco el recurso solicitado
				String resourceName = null;
				String query = (String) properties.getProperty("Query");
				if (query.equalsIgnoreCase("wsdl")) {
					resourceName = Connector.NAME_FILE_WSDL;
				} else {
					resourceName = query;
				}
				
				ConnectorPaths connectorPaths = fullConnector.getConnectorPaths();
				String resourcePath = connectorPaths.getConnectorDirPath();
				resourcePath += resourceName;
				
				File resourceFile = new File(resourcePath);
				if (resourceFile.isFile()) {
					
					byte[] resourceBytes = null;
					
					try {
						FileInputStream is = new FileInputStream(resourceFile);
						resourceBytes = StreamUtils.readStream(is);
					} catch (Exception e) {
						_logger.error(e.getLocalizedMessage(), e);
						SoapUtil.throwFaultException(new ConnectorException(e));	
					}
					
					message.getBody().add(resourceBytes);
				
				} else {
					soapFault = true;
				}
				
			} else {
				soapFault = true;
			}
		}
		
		properties.setProperty("soapFault", new Boolean(soapFault));

		return message;
		
	}
}
