package biz.ideasoft.soa.esb.actions;

import java.util.List;

import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.Service;
import org.jboss.soa.esb.actions.ContentBasedWiretap;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.ListenerTagNames;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.services.registry.RegistryException;
import org.jboss.soa.esb.services.routing.MessageRouterException;

public class ContentBasedWiretapExtended extends ContentBasedWiretap {
	private Service defaultRouteService;
	
	public ContentBasedWiretapExtended(ConfigTree tree) throws ConfigurationException, RegistryException, MessageRouterException {
		super(tree);
		ConfigTree[] destList = _config.getChildren(ContentBasedSyncInvoker.DEFAULT_ROUTE_TAG);
		if (destList != null && destList.length > 0) {
			ConfigTree ct = destList[0];
			String category = ct.getAttribute(ListenerTagNames.SERVICE_CATEGORY_NAME_TAG, "");
			String name = ct.getRequiredAttribute(ListenerTagNames.SERVICE_NAME_TAG);
			defaultRouteService = new Service(category, name);
            try {
				messageMulticaster.addRecipient(defaultRouteService);
			} catch (MessageDeliverException e) {
				throw new ConfigurationException("Problems with destination list: " + e, e);
			}
		}
	}

	@Override
	protected List<Service> executeRules(Message msg) throws MessageRouterException {
		List<Service> services = super.executeRules(msg);
		if (services.isEmpty() && defaultRouteService != null) {
			services.add(defaultRouteService);
		}
		return services;
	}
}
