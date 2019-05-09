/**
 *
 */
package gub.agesic.connector.integration.actions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * This processor parses the {@link InputStream} and generates a Messsage with a
 * String payload
 *
 * @author guzman.llambias
 *
 */
public class StringInputMessageProcessor implements MessageProcessor<InputStream, String> {

    @Override
    public Message<String> process(final Message<InputStream> message)
            throws MessageProcessorException {
        try {
            final InputStream inputStream = message.getPayload();
            final String newPayload = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            return MessageBuilder.createMessage(newPayload, message.getHeaders());
        } catch (final IOException exception) {
            throw new MessageProcessorException("Internal error processing soap request",
                    exception);
        }
    }

}
