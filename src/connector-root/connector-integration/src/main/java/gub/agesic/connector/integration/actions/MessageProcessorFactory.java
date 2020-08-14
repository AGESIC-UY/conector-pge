/**
 *
 */
package gub.agesic.connector.integration.actions;

import java.io.InputStream;

import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

/**
 * @author guzman.llambias
 *
 */
public class MessageProcessorFactory {

    private final MessageProcessor<InputStream, String> stringInputMessageProcessor;

    private final MessageProcessor<String, byte[]> stringOutputMessageProcessor;

    private final MessageProcessor<InputStream, String> mtomInputMessageProcessor;

    private final MessageProcessor<String, byte[]> mtomOutputMessageProcessor;

    public MessageProcessorFactory(
            final MessageProcessor<InputStream, String> stringInputMessageProcessor,
            final MessageProcessor<String, byte[]> stringOutputMessageProcessor,
            final MessageProcessor<InputStream, String> mtomInputMessageProcessor,
            final MessageProcessor<String, byte[]> mtomOutputMessageProcessor) {

        this.mtomInputMessageProcessor = mtomInputMessageProcessor;
        this.mtomOutputMessageProcessor = mtomOutputMessageProcessor;
        this.stringInputMessageProcessor = stringInputMessageProcessor;
        this.stringOutputMessageProcessor = stringOutputMessageProcessor;
    }

    public MessageProcessor<InputStream, String> getInputProcessor(final MediaType type)
            throws MessageProcessorException {
        if (type.includes(MediaType.TEXT_XML) || type.includes(MimeType.valueOf("application/soap+xml"))) {
            return stringInputMessageProcessor;
        } else if (type.getType().equals("multipart")) {
            return mtomInputMessageProcessor;
        } else {
            throw new MessageProcessorException("Unknown mediatype " + type);
        }
    }

    public MessageProcessor<String, byte[]> getOutputProcessor(final MediaType type)
            throws MessageProcessorException {
        if (type.includes(MediaType.TEXT_XML) || type.includes(MimeType.valueOf("application/soap+xml"))) {
            return stringOutputMessageProcessor;
        } else if (type.getType().equals("multipart")) {
            return mtomOutputMessageProcessor;
        } else {
            throw new MessageProcessorException("Unkonwn mediatype " + type);
        }
    }
}
