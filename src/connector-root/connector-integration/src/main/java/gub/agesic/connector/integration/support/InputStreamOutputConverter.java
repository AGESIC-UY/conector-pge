/**
 *
 */
package gub.agesic.connector.integration.support;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import gub.agesic.connector.integration.actions.MessageProcessor;
import gub.agesic.connector.integration.actions.MessageProcessorException;

/**
 * @author guzman.llambias
 *
 */
public class InputStreamOutputConverter implements MessageProcessor<String, InputStream> {

    @Override
    public Message<InputStream> process(final Message<String> message)
            throws MessageProcessorException {

        InputStream inputStream;
        try {
            inputStream = IOUtils.toInputStream(message.getPayload(), "UTF-8");
            return MessageBuilder.createMessage(inputStream, message.getHeaders());
        } catch (final IOException exception) {
            throw new MessageProcessorException("Error interno al procesar la respuesta",
                    exception);
        }
    }

}
