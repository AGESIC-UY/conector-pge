package uy.gub.agesic.pge.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uy.gub.agesic.pge.AgesicConstants;
import uy.gub.agesic.pge.AssertionManager;
import uy.gub.agesic.pge.PGEFactory;
import uy.gub.agesic.pge.XMLUtils;
import uy.gub.agesic.pge.beans.ClientCredential;
import uy.gub.agesic.pge.beans.SAMLAssertion;
import uy.gub.agesic.pge.beans.STSResponse;
import uy.gub.agesic.pge.core.config.ConfigProperties;
import uy.gub.agesic.pge.core.config.PGEConfiguration;
import uy.gub.agesic.pge.enums.SamlVersion;
import uy.gub.agesic.pge.enums.SoapVersion;
import uy.gub.agesic.pge.exceptions.*;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.*;

@Service
@Primary
public class PGEClientBasic implements PGEClient {
    private static final Logger log = LoggerFactory.getLogger(PGEClientBasic.class);
    private static final String CONFIG_FILE = "pge-config.xml";
    private final Map<String, PoolingHttpClientConnectionManager> mapPoolConnectionManager = new HashMap<>();

    @Override
    public STSResponse requestSecurityToken(PGEConfiguration config)
            throws RequestSecurityTokenException, ConfigurationException {

        if (config == null) {
            config = new PGEConfiguration(CONFIG_FILE);
        }
        final String alias = config.getKeyStoreAuthValue(ConfigProperties.KEY_STORE_ALIAS);
        final String storeFilePath = config.getKeyStoreAuthValue(ConfigProperties.KEY_STORE_FILE_URL);
        final String password = config.getKeyStoreAuthValue(ConfigProperties.KEY_STORE_PASS);

        final SAMLAssertion samlAssertion;
        final AssertionManager generator = PGEFactory.getAssertionManager();
        final ClientCredential credential;
        try {
            credential = generator.getCredential(password, storeFilePath, alias);
        } catch (final NoSuchAlgorithmException | UnrecoverableKeyException var25) {
            log.error(var25.getMessage(), var25);
            throw new RequestSecurityTokenException(var25, 902);
        } catch (final CertificateException var26) {
            log.error(var26.getMessage(), var26);
            throw new RequestSecurityTokenException(var26, 900);
        } catch (final IOException var28) {
            log.error(var28.getMessage(), var28);
            throw new RequestSecurityTokenException(var28, 901);
        } catch (final Exception var24) {
            log.error(var24.getMessage(), var24);
            throw new RequestSecurityTokenException(var24, 903);
        }

        try {
            samlAssertion = generator.generateSignedAssertion(credential, config);
        } catch (final AssertionException var23) {
            log.error(var23.getMessage(), var23);
            throw new RequestSecurityTokenException(var23);
        }

        String sslKeyStoreUrl = config.getKeyStoreAuthValue(ConfigProperties.SSL_KEY_STORE_URL);
        String sslKeyStorePwd = config.getKeyStoreAuthValue(ConfigProperties.SSL_KEY_STORE_PASS);
        String trustStoreUrl = config.getKeyStoreAuthValue(ConfigProperties.TRUST_STORE_URL);
        String trustStorePwd = config.getKeyStoreAuthValue(ConfigProperties.TRUST_STORE_PASS);
        String stsUrl = config.getSTSURL();

        KeyStore keystore;
        KeyStore truststoreSSL;
        try {
            keystore = XMLUtils.prepareKeystore(sslKeyStoreUrl, sslKeyStorePwd);
            truststoreSSL = XMLUtils.prepareKeystore(trustStoreUrl, trustStorePwd);
        } catch (final KeyStoreException | IOException e) {
            throw new RequestSecurityTokenException(e, 901);
        }
        CloseableHttpClient httpClient = prepareClient(keystore, sslKeyStorePwd, truststoreSSL, stsUrl, config);

        final String requestSecurityTokenMessage = createRequestSecurityTokenMessage(config, samlAssertion);
        final RSTRBean requestSecurityTokenResponse = requestStsHttpComponents(requestSecurityTokenMessage, httpClient, stsUrl);
        log.debug("Building Assertion from RequestSecurityTokenResponse message");

        final SAMLAssertion assertionResponse;
        try {
            final String samlVersion = config.getSTSPropValue(ConfigProperties.SAML_VERSION);
            assertionResponse = generator
                    .getAssertionFromSOAP(requestSecurityTokenResponse.getStsResponse(), samlVersion);
            log.debug("Assertion was built successfully");
            log.debug(assertionResponse.toString());
        } catch (final ParserException var20) {
            log.error(var20.getMessage(), var20);
            throw new RequestSecurityTokenException(
                    "Unable to parse RequestSecurityTokenResponse message", 905);
        } catch (final NoAssertionFoundException var21) {
            log.error(var21.getMessage(), var21);
            throw new RequestSecurityTokenException("No assertion was found", 906);
        } catch (final UnmarshalException var22) {
            log.error(var22.getMessage(), var22);
            throw new RequestSecurityTokenException(
                    "Unmarshal error: Cannot build assertion from RequestSecurityTokenResponse message", 907);
        }

        log.warn("SAML Signature of RSTR not validated yet!");
        return new STSResponse(requestSecurityTokenResponse.getResponseTime(), assertionResponse);
    }

    private String createRequestSecurityTokenMessage(final PGEConfiguration config,
                                                     final SAMLAssertion assertion)
            throws RequestSecurityTokenException {

        final String samlVersion = config.getSTSPropValue(ConfigProperties.SAML_VERSION);
        final String soapVersion = config.getSTSPropValue(ConfigProperties.SOAP_VERSION);
        final String policyName = config.getSTSPropValue(ConfigProperties.POLICY);
        final String role = config.getSTSPropValue(ConfigProperties.ROLE);
        final String service = config.getSTSPropValue(ConfigProperties.WSA_TO);

        final String messageID = UUID.randomUUID().toString();
        final Element elem = assertion.getDOM(samlVersion);

        if (samlVersion.equals(SamlVersion.V2_0.getName())) {
            NodeList inclusiveNamespaces = elem.getElementsByTagName("ec:InclusiveNamespaces");
            if (inclusiveNamespaces.getLength() > 0) {
                inclusiveNamespaces.item(0).getAttributes().getNamedItem("PrefixList").setNodeValue("saml2");
            }
        }

        final String strSaml;
        try {
            strSaml = XMLUtils.xmlToString(elem);
        } catch (final MarshalException var11) {
            log.error(var11.getMessage(), var11);
            throw new RequestSecurityTokenException("Could not create RST message.", 904);
        }

        final String xmlns = soapVersion.equals(SoapVersion.V1_2.getName()) ?
                AgesicConstants.NAMESPACE_SOAP_1_2 : AgesicConstants.NAMESPACE_SOAP_1_1;

        final String soapMessageSaml1PartOne =
                "<s:Envelope xmlns:a=\"http://www.w3.org/2005/08/addressing\" xmlns:s=\"" + xmlns + "\">" +

                        "<s:Header>" +
                        "<a:Action s:mustUnderstand=\"1\">http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</a:Action>" +
                        "<a:MessageID>urn:uuid:" + messageID + "</a:MessageID>" +
                        "</s:Header>" +

                        "<s:Body>" +
                        "<RequestSecurityToken xmlns=\"http://schemas.xmlsoap.org/ws/2005/02/trust\">" +
                        "<TokenType>http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1</TokenType>" +

                        "<AppliesTo xmlns=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">" +
                        "<a:EndpointReference>" +
                        "<a:Address>" + service + "</a:Address>" +
                        "</a:EndpointReference>" +
                        "</AppliesTo>" +

                        "<RequestType>http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</RequestType>" +

                        "<Issuer>" +
                        "<a:Address>" + policyName + "</a:Address>" +
                        "</Issuer>" +

                        "<Base>";

        final String soapMessageSaml1PartTwo = "</Base>" +
                "<SecondaryParameters>" +
                "<Rol>" + role + "</Rol>" +
                "</SecondaryParameters>" +

                "</RequestSecurityToken>" +

                "</s:Body>" +

                "</s:Envelope>";

        final String soapMessageSaml2PartOne =
                "<soapenv:Envelope xmlns:soapenv=\"" + xmlns + "\" xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +

                        "<soapenv:Header>" +
                            "<wsa:MessageID>uuid-" + messageID + "</wsa:MessageID>" +
                            "<wsa:Action>http://docs.oasis-open.org/ws-sx/ws-trust/200512/RequestSecurityToken</wsa:Action>" +
                        "</soapenv:Header>" +

                        "<soapenv:Body>" +
                        "<wst:RequestSecurityToken >" +
                        "<wst:TokenType>http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0</wst:TokenType>" +
                        "<wsp:AppliesTo xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">" +
                            "<wsa:EndpointReference>" +
                                "<wsa:Address>" + service + "</wsa:Address>" +
                            "</wsa:EndpointReference>" +
                        "</wsp:AppliesTo>" +

                        "<wst:RequestType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue</wst:RequestType>" +

                        "<wst:Issuer>" +
                            "<wsa:Address>" + policyName + "</wsa:Address>" +
                        "</wst:Issuer>" +

                        "<wst:Base>";

        final String soapMessageSaml2PartTwo =
                "</wst:Base>" +
                        "</wst:RequestSecurityToken>" +
                        "</soapenv:Body>" +
                        "</soapenv:Envelope>";

        final String soapMessagePartOne = samlVersion.equals(SamlVersion.V2_0.getName()) ? soapMessageSaml2PartOne : soapMessageSaml1PartOne;
        final String soapMessagePartTwo = samlVersion.equals(SamlVersion.V2_0.getName()) ? soapMessageSaml2PartTwo : soapMessageSaml1PartTwo;
        return soapMessagePartOne + strSaml + soapMessagePartTwo;
    }

    private RSTRBean requestStsHttpComponents(final String requestSecurityTokenMessage,
                                              final CloseableHttpClient httpClient,
                                              final String stsUrl)
            throws RequestSecurityTokenException {
        final InputStream stream;
        final long stsRequestTimestamp;
        final long stsResponseTimestamp;

        try (CloseableHttpClient httpclient = httpClient) {
            final String requestSecurityTokenResponseMessage;
            try {
                final RequestConfig requestConfig = RequestConfig.custom()
                        .setSocketTimeout(AgesicConstants.TIME_OUT_MILLIS)
                        .setConnectTimeout(AgesicConstants.TIME_OUT_MILLIS)
                        .setConnectionRequestTimeout(AgesicConstants.TIME_OUT_MILLIS)
                        .build();

                final HttpPost httpPost = new HttpPost(stsUrl);

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
                final StringBuilder stringBuilder = new StringBuilder();

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }

                in.close();
                requestSecurityTokenResponseMessage = stringBuilder.toString();
                log.debug(requestSecurityTokenResponseMessage);
                final int result = statusLine.getStatusCode();
                if (result != 200 && result != 202) {
                    throw new RequestSecurityTokenException(requestSecurityTokenResponseMessage, 908);
                }
            } catch (final IOException var78) {
                log.error(var78.getMessage(), var78);
                throw new RequestSecurityTokenException(var78, 901);
            } catch (final Exception var83) {
                log.error(var83.getMessage(), var83);
                throw new RequestSecurityTokenException(var83, 908);
            } finally {
                // Cierro la conexion.
                httpclient.close();
            }
            return new RSTRBean(requestSecurityTokenResponseMessage,
                    stsResponseTimestamp - stsRequestTimestamp);

        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new RequestSecurityTokenException(e, 909);
        }
    }

    private CloseableHttpClient prepareClient(final KeyStore keystore, final String passwordKeystoreSsl, final KeyStore keyStoreSSL, final String stsUrl, final PGEConfiguration config) {
        try {
            final SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(keyStoreSSL, (TrustStrategy) (chain, authType) -> true)
                    .loadKeyMaterial(keystore, passwordKeystoreSsl.toCharArray())
                    .build();
            final HttpClientBuilder builder = HttpClientBuilder.create();
            final SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(
                    sslContext.getSocketFactory(), new DefaultHostnameVerifier());

            builder.setSSLSocketFactory(sslConnectionFactory);
            final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslConnectionFactory).build();

            builder.setConnectionManager(getPoolConnectionManagerByUrl(stsUrl, registry, config));
            builder.setConnectionManagerShared(true);
            return builder.build();
        } catch (final Exception ex) {
            log.error(String.format("couldn't create httpClient!! %s", ex.getMessage()));
            return null;
        }
    }

    private Optional<PoolingHttpClientConnectionManager> getPoolConnectionManager(final String code) {
        if (mapPoolConnectionManager.containsKey(code)) {
            return Optional.of(mapPoolConnectionManager.get(code));
        } else {
            return Optional.empty();
        }
    }

    private PoolingHttpClientConnectionManager createPoolConnectionManager(final String host,
                                                                           final String port,
                                                                           final Registry<ConnectionSocketFactory> registry,
                                                                           final PGEConfiguration config) {
        PoolingHttpClientConnectionManager poolingConnManager;
        if (Objects.isNull(registry)) {
            poolingConnManager = new PoolingHttpClientConnectionManager();
        } else {
            poolingConnManager = new PoolingHttpClientConnectionManager(registry);
        }

        poolingConnManager.setMaxTotal(Integer.parseInt(config.getSAMLPropValue(ConfigProperties.MAX_TOTAL_OPEN_CONNECTIONS)));
        poolingConnManager.setDefaultMaxPerRoute(Integer.parseInt(config.getSAMLPropValue(ConfigProperties.MAX_CONNECTIONS_PER_ROUTE)));
        final HttpHost httpHostName = new HttpHost(host, Integer.parseInt(port));
        poolingConnManager.setMaxPerRoute(new HttpRoute(httpHostName), Integer.parseInt(config.getSAMLPropValue(ConfigProperties.MAX_CONNECTIONS_HOSTNAME_PORT)));

        mapPoolConnectionManager.put(host + port, poolingConnManager);

        return poolingConnManager;
    }

    public PoolingHttpClientConnectionManager getPoolConnectionManagerByUrl(final String url,
                                                                            final Registry<ConnectionSocketFactory> registry,
                                                                            final PGEConfiguration config) throws Exception {
        final String port = getPortFromUrl(url);
        final String host = getDomainName(url);

        final Optional<PoolingHttpClientConnectionManager> optionalPoolingHttpClientConnectionManager = getPoolConnectionManager(
                host + port);
        return optionalPoolingHttpClientConnectionManager
                .orElseGet(() -> createPoolConnectionManager(host, port, registry, config));

    }

    private String getPortFromUrl(final String url) throws Exception {
        try {
            // Getting the port from url string
            return String.valueOf(new URL(url).getPort());
        } catch (final MalformedURLException e) {
            // Throw business exception
            final String portError = "No se pudo obtener correctamente el puerto de la URL.";
            log.error(portError);
            throw new Exception(portError, e);
        }
    }

    public String getDomainName(final String url) throws Exception {
        URI uri;
        try {
            uri = new URI(url);
            return uri.getHost();
        } catch (final URISyntaxException e) {
            // Throw business exception
            final String hostError = "No se pudo obtener correctamente el host de la URL.";
            log.error(hostError);
            throw new Exception(hostError, e);
        }

    }

    private static class RSTRBean {
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

