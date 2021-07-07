package uy.gub.agesic.soa.wsa.esb.actions.sts.v2.opensaml;

import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;

public class OpenSamlBootstrap extends DefaultBootstrap {
	
	private static boolean initialized = false;
	
	private static synchronized boolean isInitialized() {
		if (!initialized) {
			initialized = true;
			return false;
		}
		return true;
	}
	
	public static synchronized void bootstrap() throws ConfigurationException {
		if (!isInitialized()) {
			DefaultBootstrap.bootstrap();
		}
    }
	
	protected static void initializeGlobalSecurityConfiguration() {
//		DefaultBootstrap.initializeGlobalSecurityConfiguration();
	}
	
	protected static void initializeXMLSecurity() throws ConfigurationException {
//		DefaultBootstrap.initializeXMLSecurity();
	}
	
	protected static void initializeVelocity() throws ConfigurationException {
//		DefaultBootstrap.initializeVelocity();
	}
	
	protected static void initializeXMLTooling(String[] providerConfigs) throws ConfigurationException {
		DefaultBootstrap.initializeXMLTooling(providerConfigs);
	}
	
	protected static void initializeArtifactBuilderFactories() throws ConfigurationException {
//		DefaultBootstrap.initializeArtifactBuilderFactories();
	}
}
