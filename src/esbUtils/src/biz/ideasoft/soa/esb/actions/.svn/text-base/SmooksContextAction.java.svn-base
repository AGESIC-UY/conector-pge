package biz.ideasoft.soa.esb.actions;

import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.smooks.SmooksAction;

public class SmooksContextAction extends SmooksAction {
	
	private static ThreadLocal<Message> currentMessage = new ThreadLocal<Message>();

	private static ThreadLocal<ConfigTree> currentTree = new ThreadLocal<ConfigTree>();
	
	private ConfigTree configTree;

	public SmooksContextAction(ConfigTree tree) throws ConfigurationException {
		super(tree);
		configTree = tree;
	}

	public Message process(Message message) throws ActionProcessingException {
		currentMessage.set(message);
		currentTree.set(configTree);
		try {
			return super.process(message);
		} finally {
			currentMessage.remove();
			currentTree.remove();
		}
	}
	
	public static Message getCurrentThreadMessage() {
		return currentMessage.get();
	}

	public static ConfigTree getConfigTree() {
		return currentTree.get();
	}
}
