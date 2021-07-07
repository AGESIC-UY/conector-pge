package biz.ideasoft.soa.esb.actions;

import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import biz.ideasoft.soa.esb.util.SoapUtil;

/**
 * Extended to generate a soap fault when something fails
 * @author martin
 *
 */
public class EJBProcessor extends org.jboss.soa.esb.actions.EJBProcessor {

	public EJBProcessor(ConfigTree config) {
		super(config);
	}

	@Override
	public Message process(Message msg) throws ActionProcessingException, ConfigurationException {
		try {
			return super.process(msg);
		} catch (ActionProcessingException exc) {
			Throwable deeper = SoapUtil.getDeepestException(exc);
			
			if (deeper != null && deeper != exc) {
				SoapUtil.throwFaultException(deeper);
			}
			
			throw exc;
		}
	}

	
}
