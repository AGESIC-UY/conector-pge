package gub.agesic.connector.integration.pgeclient.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import gub.agesic.connector.dataaccess.entity.Configuration;
import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.integration.actions.ConectorRestTemplate;
//import gub.agesic.connector.integration.pgeclient.AgesicConstants;
import gub.agesic.connector.integration.pgeclient.AssertionManager;
import gub.agesic.connector.integration.pgeclient.PGEFactory;
import gub.agesic.connector.integration.pgeclient.XMLUtils;
import gub.agesic.connector.integration.pgeclient.beans.ClientCredential;
import gub.agesic.connector.integration.pgeclient.beans.SAMLAssertion;
import gub.agesic.connector.integration.pgeclient.beans.STSResponse;
import gub.agesic.connector.integration.pgeclient.exceptions.AssertionException;
import gub.agesic.connector.integration.pgeclient.exceptions.MarshalException;
import gub.agesic.connector.integration.pgeclient.exceptions.NoAssertionFoundException;
import gub.agesic.connector.integration.pgeclient.exceptions.ParserException;
import gub.agesic.connector.integration.pgeclient.exceptions.RequestSecurityTokenException;
import gub.agesic.connector.integration.pgeclient.exceptions.UnmarshalException;
import gub.agesic.connector.integration.services.PoolConnectionService;
import gub.agesic.connector.services.dbaccess.ConnectorService;

@Service
@Primary
@PropertySource({ "classpath:config.properties" })
public class PGEClientBasic implements PGEClient {

    private static final Logger log = LoggerFactory.getLogger(PGEClientBasic.class);

    @Autowired
    private ConnectorService connectorService;
    @Autowired
    private PoolConnectionService poolConnectionService;
    @Autowired
    private ConectorRestTemplate conectortemplate;

    @Override
    public STSResponse requestSecurityToken(final Configuration configuration,
            final Connector connector, final String policyName)
            throws RequestSecurityTokenException {
        final SAMLAssertion samlAssertion;
        final String alias = configuration.getAliasKeystore();
        final String storeFilePath = configuration.getDirKeystoreOrg();
        final String password = configuration.getPasswordKeystoreOrg();
        final AssertionManager generator = PGEFactory.getAssertionManager();

        final ClientCredential credential;
        try {
            credential = generator.getCredential(password, storeFilePath, alias);
        } catch (final KeyStoreException var24) {
            log.error(var24.getMessage(), var24);
            throw new RequestSecurityTokenException(var24, Integer.valueOf(903));
        } catch (final NoSuchAlgorithmException var25) {
            log.error(var25.getMessage(), var25);
            throw new RequestSecurityTokenException(var25, Integer.valueOf(902));
        } catch (final CertificateException var26) {
            log.error(var26.getMessage(), var26);
            throw new RequestSecurityTokenException(var26, Integer.valueOf(900));
        } catch (final UnrecoverableKeyException var27) {
            log.error(var27.getMessage(), var27);
            throw new RequestSecurityTokenException(var27, Integer.valueOf(902));
        } catch (final IOException var28) {
            log.error(var28.getMessage(), var28);
            throw new RequestSecurityTokenException(var28, Integer.valueOf(901));
        } catch (final Exception var29) {
            log.error(var29.getMessage(), var29);
            throw new RequestSecurityTokenException(var29, Integer.valueOf(903));
        }

        try {
            samlAssertion = generator.generateSignedAssertion(credential, connector, policyName);
        } catch (final AssertionException var23) {
            log.error(var23.getMessage(), var23);
            throw new RequestSecurityTokenException(var23);
        }

        final String requestSecurityTokenMessage = createRequestSecurityTokenMessage(connector,
                samlAssertion, policyName);
        final RSTRBean requestSecurityTokenResponse = requestStsHttpComponents(connector,
                configuration, requestSecurityTokenMessage);

        log.debug("Building Assertion from RequestSecurityTokenResponse message");

        final SAMLAssertion assertionResponse;

        try {
            assertionResponse = generator
                    .getAssertionFromSOAP(requestSecurityTokenResponse.getStsResponse());
            log.debug("Assertion was built successfully");
            log.debug(assertionResponse.toString());
        } catch (final ParserException var20) {
            log.error(var20.getMessage(), var20);
            throw new RequestSecurityTokenException(
                    "Unable to parse RequestSecurityTokenResponse message", Integer.valueOf(905));
        } catch (final NoAssertionFoundException var21) {
            log.error(var21.getMessage(), var21);
            throw new RequestSecurityTokenException("No assertion was found", Integer.valueOf(906));
        } catch (final UnmarshalException var22) {
            log.error(var22.getMessage(), var22);
            throw new RequestSecurityTokenException(
                    "Unmarshal error: Cannot build assertion from RequestSecurityTokenResponse message",
                    Integer.valueOf(907));
        }

        log.warn("SAML Signature of RSTR not validated yet!");
        return new STSResponse(requestSecurityTokenResponse.getResponseTime(), assertionResponse);
    }

    private String createRequestSecurityTokenMessage(final Connector connector,
            final SAMLAssertion assertion, final String policyName)
            throws RequestSecurityTokenException {
        final String messageID = UUID.randomUUID().toString();
        final String role = connector.getActualRoleOperation().getRole();
        final String service = connector.getWsaTo();
        final Element elem = assertion.getDOM();

        final String strSaml;
        try {
            strSaml = XMLUtils.xmlToString(elem);
        } catch (final MarshalException var11) {
            log.error(var11.getMessage(), var11);
            throw new RequestSecurityTokenException("Could not create RST message.",
                    Integer.valueOf(904));
        }

        final String soapMessagePartOne = "<s:Envelope xmlns:a=\"http://www.w3.org/2005/08/addressing\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Header><a:Action s:mustUnderstand=\"1\">http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</a:Action><a:MessageID>urn:uuid:"
                + messageID + "</a:MessageID>"
                + "</s:Header><s:Body><RequestSecurityToken xmlns=\"http://schemas.xmlsoap.org/ws/2005/02/trust\">"
                + "<TokenType>http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1</TokenType>"
                + "<AppliesTo xmlns=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"><a:EndpointReference>"
                + "<a:Address>" + service + "</a:Address></a:EndpointReference></AppliesTo>"
                + "<RequestType>http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</RequestType><Issuer>"
                + "<a:Address>" + policyName + "</a:Address></Issuer><Base>";
        final String soapMessagePartTwo = "</Base><SecondaryParameters><Rol>" + role
                + "</Rol></SecondaryParameters></RequestSecurityToken></s:Body></s:Envelope>";
        return soapMessagePartOne + strSaml + soapMessagePartTwo;
    }

    private RSTRBean requestStsHttpComponents(final Connector connector,
            final Configuration configuration, final String requestSecurityTokenMessage)
            throws RequestSecurityTokenException {
        final InputStream stream;

        KeyStore keystore = null;
        KeyStore truststoreSSL = null;
        final long stsRequestTimestamp;
        final long stsResponseTimestamp;
        try {
            keystore = prepareKeystore(configuration.getDirKeystoreSsl(),
                    configuration.getPasswordKeystoreSsl());
            truststoreSSL = prepareKeystore(configuration.getDirKeystore(),
                    configuration.getPasswordKeystore());
        } catch (final KeyStoreException e) {
            throw new RequestSecurityTokenException(e, Integer.valueOf(901));
        } catch (final IOException e) {
            throw new RequestSecurityTokenException(e, Integer.valueOf(901));
        }
        final String url;
        if (connector.isEnableSTSLocal()) {
            url = connector.getStsLocalUrl();
        } else {
            final ConnectorGlobalConfiguration globalConfiguration = connectorService
                    .getGlobalConfigurationByType(connector.getType());
            url = globalConfiguration.getStsGlobalUrl();
        }
        try (CloseableHttpClient httpclient = prepareClient(keystore, configuration, truststoreSSL,
                url)) {

            final String requestSecurityTokenResponseMessage;
            try {
            	
                /*final RequestConfig requestConfig = RequestConfig.custom()
                        .setSocketTimeout(Integer.valueOf(AgesicConstants.TIME_OUT_MILLIS))
                        .setConnectTimeout(Integer.valueOf(AgesicConstants.TIME_OUT_MILLIS))
                        .setConnectionRequestTimeout(
                                Integer.valueOf(AgesicConstants.TIME_OUT_MILLIS))
                        .build();*/
            	final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(Integer.valueOf(conectortemplate.connectTimeout))
                .setConnectTimeout(Integer.valueOf(conectortemplate.connectTimeout))
                .setConnectionRequestTimeout(
                        Integer.valueOf(conectortemplate.connectTimeout))
                .build();
            	//System.out.print("****************************ConectorTimeout:" + conectortemplate.connectTimeout);
                final HttpPost httpPost = new HttpPost(url);

                httpPost.setEntity(new StringEntity(requestSecurityTokenMessage));
                httpPost.setConfig(requestConfig);

                stsRequestTimestamp = System.currentTimeMillis();
                final HttpResponse httpResponse = httpclient.execute(httpPost);
                stsResponseTimestamp = System.currentTimeMillis();

                final HttpEntity entity = httpResponse.getEntity();
                final StatusLine statusLine = httpResponse.getStatusLine();
                log.debug("STS response: " + statusLine);
                stream = entity.getContent();

                final InputStreamReader isr1 = new InputStreamReader(stream);
                final BufferedReader in = new BufferedReader(isr1);
                final StringBuffer stringBuffer = new StringBuffer();

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    stringBuffer.append(inputLine);
                }

                in.close();
                requestSecurityTokenResponseMessage = stringBuffer.toString();
                log.debug(requestSecurityTokenResponseMessage);
                final int result = statusLine.getStatusCode();
                if (result != 200 && result != 202) {
                    throw new RequestSecurityTokenException(requestSecurityTokenResponseMessage,
                            Integer.valueOf(908));
                }
            } catch (final IOException var78) {
                log.error(var78.getMessage(), var78);
                throw new RequestSecurityTokenException(var78, Integer.valueOf(901));
            } catch (final Exception var83) {
                log.error(var83.getMessage(), var83);
                throw new RequestSecurityTokenException(var83, Integer.valueOf(908));
            } finally {
                // Cierro la conexion.
                httpclient.close();
            }
            return new RSTRBean(requestSecurityTokenResponseMessage,
                    stsResponseTimestamp - stsRequestTimestamp);

        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new RequestSecurityTokenException(e, Integer.valueOf(909));
        }
    }

    private CloseableHttpClient prepareClient(final KeyStore keystore,
            final Configuration configuration, final KeyStore keyStoreSSL, final String url) {
        try {
            final SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(keyStoreSSL, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(final X509Certificate[] chain,
                                final String authType) throws CertificateException {
                            return true;
                        }
                    })
                    .loadKeyMaterial(keystore, configuration.getPasswordKeystoreSsl().toCharArray())
                    .build();
            final HttpClientBuilder builder = HttpClientBuilder.create();
            final SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(
                    sslContext.getSocketFactory(), new DefaultHostnameVerifier());

            builder.setSSLSocketFactory(sslConnectionFactory);
            final Registry<ConnectionSocketFactory> registry = RegistryBuilder
                    .<ConnectionSocketFactory> create().register("https", sslConnectionFactory)
                    .build();

            builder.setConnectionManager(
                    poolConnectionService.getPoolConnectionManagerByUrl(url, registry));
            builder.setConnectionManagerShared(true);
            return builder.build();
        } catch (final Exception ex) {
            log.error("couldn't create httpClient!! {}", ex.getMessage(), ex);
            return null;
        }
    }

    private KeyStore prepareKeystore(final String keystorePath, final String keystorePassword)
            throws KeyStoreException, IOException, RequestSecurityTokenException {
        log.debug("STS - SSL config: keystorePath=" + keystorePath + ", keystorePassword="
                + keystorePassword);

        final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(keystorePath));
            keystore.load(inputStream, keystorePassword.toCharArray());
            return keystore;
        } catch (final NoSuchAlgorithmException exception) {
            log.error(exception.getMessage(), exception);
            throw new RequestSecurityTokenException(exception, Integer.valueOf(902));
        } catch (final CertificateException exception) {
            log.error(exception.getMessage(), exception);
            throw new RequestSecurityTokenException(exception, Integer.valueOf(900));
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

    private class RSTRBean {
        private final String stsResponse;

        private final long responseTime;

        public RSTRBean(final String stsResponse, final long responseTime) {
            super();
            this.stsResponse = stsResponse;
            this.responseTime = responseTime;
        }

        public String getStsResponse() {
            return stsResponse;
        }

        public long getResponseTime() {
            return responseTime;
        }
    }
}
