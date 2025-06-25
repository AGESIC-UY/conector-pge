package uy.gub.agesic.pge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import uy.gub.agesic.pge.exceptions.MarshalException;
import uy.gub.agesic.pge.exceptions.RequestSecurityTokenException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class XMLUtils {
    private static final Logger log = LoggerFactory.getLogger(XMLUtils.class);

    /**
     * Creates a string representation of a {@link Node} instance. This method
     * does not introduce any character to the string representation of the
     * {@link Node} (eg. \n or \r characters)
     *
     * @param node A {@link Node} instance
     * @return A string representation of the node instance
     * @throws MarshalException if any MarshalException errors occur.
     */
    public static String xmlToString(final Node node) throws MarshalException {
        try {
            final Source source = new DOMSource(node);
            final StringWriter stringWriter = new StringWriter();
            final Result result = new StreamResult(stringWriter);
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();

        } catch (final TransformerException e) {
            e.printStackTrace();
            throw new MarshalException("Cannot build a string representation of the assertion.");
        }
    }

    public static KeyStore prepareKeystore(final String keystorePath, final String keystorePassword)
            throws KeyStoreException, IOException, RequestSecurityTokenException {
        log.debug("STS - SSL config: keystorePath=" + keystorePath + ", keystorePassword=" + keystorePassword);

        final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(keystorePath);
            keystore.load(inputStream, keystorePassword.toCharArray());
            return keystore;
        } catch (final NoSuchAlgorithmException exception) {
            log.error(exception.getMessage(), exception);
            throw new RequestSecurityTokenException(exception, 902);
        } catch (final CertificateException exception) {
            log.error(exception.getMessage(), exception);
            throw new RequestSecurityTokenException(exception, 900);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (final Exception exception) {
                log.error("Error al cerrar el stream al leer el keystore " + keystorePath);
            }
        }
    }
}

