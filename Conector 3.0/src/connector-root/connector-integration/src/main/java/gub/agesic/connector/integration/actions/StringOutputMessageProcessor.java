/**
 *
 */
package gub.agesic.connector.integration.actions;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Transforms the payload of a {@link Message} from a String to a byte[]
 *
 * @author guzman.llambias
 *
 */
public class StringOutputMessageProcessor implements MessageProcessor<String, byte[]> {

    @Override
    public Message<byte[]> process(final Message<String> message) throws MessageProcessorException {
        return MessageBuilder.createMessage(message.getPayload().getBytes(), message.getHeaders());
    }

}
