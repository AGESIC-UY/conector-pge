/**
 *
 */
package gub.agesic.connector.integration.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.internet.ParseException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import gub.agesic.connector.integration.support.ConnectorUtils;

/**
 * This processor fetches binary data previously saved into temporary files and
 * creates a {@link Message} with an MTOM message as a byte[] payload using the
 * original {@link Message}.
 *
 * @author guzman.llambias
 * @see {@link MTOMInputMessageProcessor} to know how binary data is saved
 */
@SuppressWarnings("PMD.AvoidReassigningParameters")
public class MTOMOutputMessageProcessor implements MessageProcessor<String, byte[]> {

    private static final String BOUNDARY_ATTRIBUTE = "boundary";

    private final String tempFolderLocation;

    public MTOMOutputMessageProcessor(final String tempFolderLocation) {
        this.tempFolderLocation = tempFolderLocation + "/";
    }

    @Override
    public Message<byte[]> process(final Message<String> message) throws MessageProcessorException {
        try {
            final String boundary = "--" + getBoundary(message);
            final String folderId = (String) message.getHeaders()
                    .get(MTOMMessageProcessorConstants.FOLDER_ID_HEADER);
            final int parts = (int) message.getHeaders()
                    .get(MTOMMessageProcessorConstants.MTOM_PARTS_HEADER);

            final byte[] mtomMessage = buildBinaryMtomMessage(message.getPayload(), folderId,
                    boundary, parts);

            cleanTempFiles(parts, folderId);

            return MessageBuilder.createMessage(mtomMessage, message.getHeaders());

        } catch (final MessagingException | IOException exception) {
            throw new MessageProcessorException("Internal error while building mtom message",
                    exception);
        }
    }

    private void cleanTempFiles(final int parts, final String folderId) {
        for (int index = 0; index < parts; index++) {
            final boolean isSoapPart = index == 0;
            final String headerFilePath = tempFolderLocation + folderId + "/"
                    + MTOMMessageProcessorConstants.TEMP_HEADER_PREFIX + index;

            new File(headerFilePath).delete();
            if (!isSoapPart) {
                final String filepath = tempFolderLocation + folderId + "/"
                        + MTOMMessageProcessorConstants.TEMP_FILE_PREFIX + index;
                new File(filepath).delete();
            }
        }
    }

    private byte[] buildBinaryMtomMessage(final String soapMessage, final String folderId,
            final String boundary, final int parts) throws IOException {
        byte[] result = {};

        for (int index = 0; index < parts; index++) {
            final String headerFilePathPrefix = tempFolderLocation + folderId + "/"
                    + MTOMMessageProcessorConstants.TEMP_HEADER_PREFIX;
            result = addHeader(result, boundary, headerFilePathPrefix + index);

            final boolean isSoapPart = index == 0;
            if (isSoapPart) {
                result = addString(soapMessage, result);
            } else {
                final String filepathPrefix = tempFolderLocation + folderId + "/"
                        + MTOMMessageProcessorConstants.TEMP_FILE_PREFIX;
                result = addBinaryPart(result, filepathPrefix + index);
            }
            result = addString(System.lineSeparator(), result);
        }
        return addString(boundary + "--", result);
    }

    private byte[] addBinaryPart(byte[] result, final String filepathPrefix) throws IOException {
        final InputStream initialStream = FileUtils.openInputStream(new File(filepathPrefix));
        final byte[] part = IOUtils.toByteArray(initialStream);
        final byte[] partialResult = result;
        result = new byte[partialResult.length + part.length];
        System.arraycopy(partialResult, 0, result, 0, partialResult.length);
        System.arraycopy(part, 0, result, partialResult.length, part.length);
        return result;
    }

    private byte[] addHeader(byte[] result, final String boundary, final String filePath)
            throws IOException {
        result = addString(boundary + System.lineSeparator(), result);
        final String headers = FileUtils.readFileToString(new File(filePath), "UTF-8");
        result = addString(headers, result);
        result = addString(System.lineSeparator(), result);
        return result;
    }

    private byte[] addString(final String headerAsString, byte[] result) {
        final byte[] headerAsByte = headerAsString.getBytes();
        final byte[] partialResult = result;
        result = new byte[result.length + headerAsByte.length];
        System.arraycopy(partialResult, 0, result, 0, partialResult.length);
        System.arraycopy(headerAsByte, 0, result, partialResult.length, headerAsByte.length);
        return result;
    }

    private String getBoundary(final Message<String> message)
            throws ParseException, MessageProcessorException {
        final MediaType value = ConnectorUtils.getContentType(message);
        final String boundaryWithQuotes = value.getParameter(BOUNDARY_ATTRIBUTE);
        return boundaryWithQuotes.substring(1, boundaryWithQuotes.length() - 1);
    }

}
