package biz.ideasoft.soa.esb.actions;

import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.services.registry.RegistryException;
import org.jboss.soa.esb.services.routing.MessageRouterException;

public class ContentBasedRouterExtended extends ContentBasedWiretapExtended {
	public ContentBasedRouterExtended(ConfigTree tree) throws ConfigurationException, RegistryException, MessageRouterException {
		super(tree);
	}

	@Override
	public Message process(Message msg) throws ActionProcessingException {
		super.process(msg);
		return null;
	}

}
