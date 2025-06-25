package uy.gub.agesic.pge;

import uy.gub.agesic.pge.beans.ClientCredential;
import uy.gub.agesic.pge.beans.RSTBean;
import uy.gub.agesic.pge.beans.SAMLAssertion;
import uy.gub.agesic.pge.core.config.PGEConfiguration;
import uy.gub.agesic.pge.exceptions.AssertionException;
import uy.gub.agesic.pge.exceptions.NoAssertionFoundException;
import uy.gub.agesic.pge.exceptions.ParserException;
import uy.gub.agesic.pge.exceptions.UnmarshalException;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public interface AssertionManager {

    ClientCredential getCredentialFromAssertion(SAMLAssertion assertion, PGEConfiguration config);

    /**
     * Generates a signed SAML Assertion using a ClientCredential
     *
     * @param signingCredential A {@link ClientCredential} to sign the SAML Assertion
     * @param config A {@link PGEConfiguration} with PGE configurations
     * @return A {@link SAMLAssertion} signed with the signingCredential
     * @throws AssertionException if an error occurs during the creation of the signed assertion
     */
    SAMLAssertion generateSignedAssertion(ClientCredential signingCredential,
                                          PGEConfiguration config) throws AssertionException;

    /**
     * Gets the first {@link SAMLAssertion} found in a SOAP message
     *
     * @param string String representation of the SOAP message
     * @return The first {@link SAMLAssertion} found in a SOAP message
     * @throws ParserException           if it cannot parse the SOAP message
     * @throws UnmarshalException        if it cannot build an {@link SAMLAssertion} from the SOAP
     *                                   message
     * @throws NoAssertionFoundException if no {@link SAMLAssertion} is found in the SOAP message
     */
    SAMLAssertion getAssertionFromSOAP(String string, String samlVersion)
            throws ParserException, NoAssertionFoundException, UnmarshalException;

    /**
     * Gets a ClientCredential from a Keystore
     *
     * @param keyStorePwd      keystore password
     * @param keyStoreFilePath keystore file path
     * @param alias            certificate alias used in the keystore
     * @return A {@link ClientCredential} from a keystore with the required
     * attributes
     * @throws KeyStoreException         if the requested keystore type is not available in the
     *                                   default provider package or any of the other provider
     *                                   packages that were searched or it wasn't loaded.
     * @throws NoSuchAlgorithmException  if the algorithm used to check the integrity of the keystore
     *                                   or the algorithm for recovering the key cannot be found.
     * @throws CertificateException      if any of the certificates in the keystore could not be
     *                                   loaded.
     * @throws IOException               if the keystore file does not exist, is a directory rather
     *                                   than a regular file, or for some other reason cannot be
     *                                   opened for reading.
     * @throws UnrecoverableKeyException if the key cannot be recovered (e.g., the given password is
     *                                   wrong).
     */
    ClientCredential getCredential(String keyStorePwd, String keyStoreFilePath, String alias)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
            UnrecoverableKeyException;

}

