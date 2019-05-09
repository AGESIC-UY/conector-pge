/**
 *
 */
package gub.agesic.connector.integration.actions;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;

import gub.agesic.connector.integration.support.ConnectorUtils;

/**
 * @author guzman.llambias
 */
public class BasicMessageCoordinator implements MessageProcessor<InputStream, byte[]> {

    private final MessageProcessorFactory messageProcessorFactory;
    private final MessageProcessor<String, String> wsInvokeService;
    private static final Logger LOGGER = Logger.getLogger("connectorMessages");

    public BasicMessageCoordinator(final MessageProcessorFactory messageProcessorFactory,
            final MessageProcessor<String, String> wsInvokeService) {
        this.messageProcessorFactory = messageProcessorFactory;
        this.wsInvokeService = wsInvokeService;
    }

    @Override
    public Message<byte[]> process(final Message<InputStream> message)
            throws MessageProcessorException {

        final MediaType mediaType = ConnectorUtils.getContentType(message);
        final Message<String> preProcessedMessage = messageProcessorFactory
                .getInputProcessor(mediaType).process(message);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[RQ-C] " + preProcessedMessage);
        }
        final Message<String> request = wsInvokeService.process(preProcessedMessage);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[RQ-PDI] " + request);
        }
        return messageProcessorFactory.getOutputProcessor(mediaType).process(request);
    }

}
