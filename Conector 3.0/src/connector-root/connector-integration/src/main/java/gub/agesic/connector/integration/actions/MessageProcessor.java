/**
 *
 */
package gub.agesic.connector.integration.actions;

import org.springframework.messaging.Message;

/**
 * @author guzman.llambias
 *
 */
public interface MessageProcessor<T, K> {

    Message<K> process(Message<T> message) throws MessageProcessorException;

}
