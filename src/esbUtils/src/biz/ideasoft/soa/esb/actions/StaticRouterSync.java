package biz.ideasoft.soa.esb.actions;

import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.services.registry.RegistryException;

public class StaticRouterSync extends StaticWiretapSync {
	
    public StaticRouterSync(ConfigTree config) throws ConfigurationException, RegistryException {
    	super(config);
    }
    
	public Message process(Message message) throws ActionProcessingException {
		return super.process(message);
	}

}
