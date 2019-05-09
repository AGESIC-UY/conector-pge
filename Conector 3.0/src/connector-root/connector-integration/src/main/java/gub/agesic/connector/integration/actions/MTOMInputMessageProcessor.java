/**
 *
 */
package gub.agesic.connector.integration.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.UUID;

import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * This processor parses MTOM Messages, saves binary data into temporary files
 * and creates a {@link Message} with only the soap part of the MTOM Message as
 * a String payload. It saves into the {@link Message} headers the information
 * to retrieve the binary parts. Temporary files are saved into
 * {@link MTOMInputMessageProcessor#tempFolderLocation} location
 *
 * @author guzman.llambias
 *
 */
public class MTOMInputMessageProcessor implements MessageProcessor<InputStream, String> {

    private static final String MULTIPART_RELATED = "multipart/related";

    private final String tempFolderLocation;

    public MTOMInputMessageProcessor(final String tempFolderLocation) {
        this.tempFolderLocation = tempFolderLocation + "/";
    }

    @Override
    public Message<String> process(final Message<InputStream> message)
            throws MessageProcessorException {
        try {
            final MimeMultipart mtomMessage = new MimeMultipart(new ByteArrayDataSource(
                    IOUtils.toByteArray(message.getPayload()), MULTIPART_RELATED));

            final String folderId = UUID.randomUUID().toString();
            final String soapMessage = processMtomMessage(mtomMessage, folderId);

            return MessageBuilder.withPayload(soapMessage).copyHeaders(message.getHeaders())
                    .setHeader(MTOMMessageProcessorConstants.FOLDER_ID_HEADER, folderId)
                    .setHeader(MTOMMessageProcessorConstants.MTOM_PARTS_HEADER,
                            mtomMessage.getCount())
                    .build();
        } catch (final MessagingException | IOException exception) {
            throw new MessageProcessorException("Internal error while parsing mtom message",
                    exception);
        }
    }

    /**
     * Parses the mtom message, saves binary data into temporary files and
     * returns the soap message part
     *
     * @param mtomMessage
     * @param folderId
     * @return
     * @throws IOException
     * @throws MessagingException
     */
    private String processMtomMessage(final MimeMultipart mtomMessage, final String folderId)
            throws IOException, MessagingException {

        String soapMessage = "";

        final int parts = mtomMessage.getCount();
        for (int index = 0; index < parts; index++) {
            final BodyPart bodyPart = mtomMessage.getBodyPart(index);

            // Don't save soap message. The first part of an mtom message is the
            // soap message.
            final boolean isSoapPart = index == 0;
            if (isSoapPart) {
                // Soap message is allways the first part
                soapMessage = getSoapPart(bodyPart);
            } else {
                final String binaryFileName = this.tempFolderLocation + folderId + "/"
                        + MTOMMessageProcessorConstants.TEMP_FILE_PREFIX + index;
                saveBody(binaryFileName, bodyPart);
            }

            // Always save headers of an mtom part as this must then be sent to
            // the service later on.
            final String headerFileName = this.tempFolderLocation + folderId + "/"
                    + MTOMMessageProcessorConstants.TEMP_HEADER_PREFIX + index;
            saveHeader(headerFileName, bodyPart);
        }
        return soapMessage;
    }

    private String getSoapPart(final BodyPart bodyPart) throws IOException, MessagingException {
        final InputStream soapInputStream = bodyPart.getInputStream();
        return IOUtils.toString(soapInputStream, StandardCharsets.UTF_8.name());
    }

    private void saveHeader(final String headerFileName, final BodyPart bodyPart)
            throws MessagingException, IOException {
        final StringBuilder builder = new StringBuilder();
        final Enumeration<?> headers = bodyPart.getAllHeaders();
        while (headers.hasMoreElements()) {
            final Header header = (Header) headers.nextElement();
            final String headerAsString = header.getName() + ": " + header.getValue()
                    + System.lineSeparator();
            builder.append(headerAsString);
        }
        final File targetFileHeader = new File(headerFileName);
        FileUtils.writeStringToFile(targetFileHeader, builder.toString(), StandardCharsets.UTF_8);
    }

    private void saveBody(final String fileName, final BodyPart bodyPart)
            throws IOException, MessagingException {
        final InputStream initialStream = bodyPart.getInputStream();
        final File targetFile = new File(fileName);
        FileUtils.copyInputStreamToFile(initialStream, targetFile);
        initialStream.close();
    }

}
