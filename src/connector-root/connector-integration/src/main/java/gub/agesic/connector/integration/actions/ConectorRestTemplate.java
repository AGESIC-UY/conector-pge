/**
 *
 */
package gub.agesic.connector.integration.actions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.repository.ConnectorTypeHolder;
import gub.agesic.connector.dataaccess.repository.GlobalConfigurationRepository;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.client.RestTemplate;

import gub.agesic.connector.dataaccess.entity.Configuration;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.integration.services.PoolConnectionService;

import static gub.agesic.connector.integration.actions.WSInvokeService.REGEX_PATH;

/**
 * @author guzman.llambias
 */
public class ConectorRestTemplate implements MessageProcessor<byte[], InputStream> {

    private static final Logger log = LoggerFactory.getLogger(ConectorRestTemplate.class);

    private final HeaderMapper<HttpHeaders> mapper;

    private int readTimeout;

    @Autowired
    private ConnectorService connectorService;

    @Autowired
    private PoolConnectionService poolConnectionService;

    public ConectorRestTemplate(final HeaderMapper<HttpHeaders> mapper) {
        this.mapper = mapper;
    }

    @Override
    public Message<InputStream> process(final Message<byte[]> message)
            throws MessageProcessorException {

        final Optional<Connector> connector;
        final ConnectorTypeHolder connectorTypeHolder;

        try {
            // Search path and port from url
            final String path = getPathFromUrl(message);
            final String port = getPortFromUrl(message);

            connectorTypeHolder = connectorService.getConnectorTypeByPort(port);
            connector = connectorService.getConnectorByPathAndPort(path, connectorTypeHolder);
        } catch (final ConnectorException exception) {
            throw new MessageProcessorException("No se pudo obtener la configuracion del servicio",
                    exception);
        }

        if (connector.isPresent()) {
            Connector conn = connector.get();
            if (conn.isEnableLocalServiceTimeOut()) {
                this.readTimeout = conn.getLocalServiceTimeOut();
            } else {
                try {
                    this.readTimeout = connectorService.getGlobalConfigurationByType(conn.getType()).getServiceTimeOut();
                } catch (final NoSuchElementException e) {
                    throw new MessageProcessorException(
                            "No existe ninguna configuración global para el ambiente seleccionado. Es necesaria una para definir el timeout a utilizar.");
                }
            }
        }

        final HttpHeaders headers = new HttpHeaders();
        mapper.fromHeaders(message.getHeaders(), headers);

        final String serviceUrl = (String) message.getHeaders().get("serviceUrl");
        final CloseableHttpClient httpClient;
        if (serviceUrl.startsWith("https")) {
            httpClient = prepareClient(message);
        } else {
            final HttpClientBuilder builder = HttpClientBuilder.create();
            try {
                builder.setConnectionManager(
                        poolConnectionService.getPoolConnectionManagerByUrl(serviceUrl, null));
                builder.setConnectionManagerShared(true);
            } catch (final ConnectorException e) {

                throw new MessageProcessorException("Error al configurar pool connection Manager",
                        e);

            }
            httpClient = builder.build();

        }
        final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
                httpClient);
        factory.setHttpClient(httpClient);
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(readTimeout);

        final HttpEntity<byte[]> entity = new HttpEntity<byte[]>(message.getPayload(), headers);
        final RestTemplate restTemplate = new RestTemplate(factory);

        if (log.isDebugEnabled()) {
            log.debug("[OUT] " + new String(message.getPayload()), "UTF-8");
        }
        log.info("STS Execution time: "
                + message.getHeaders().get(WSInvokeService.HEADER_STS_EXECUTION_TIME) + "ms");
        final long connectorExecutionTime = System.currentTimeMillis()
                - ((long) message.getHeaders().get("initialTimestamp") + (long) message.getHeaders()
                        .get(WSInvokeService.HEADER_STS_EXECUTION_TIME));
        log.info("Connector request execution time: " + connectorExecutionTime + "ms");

        final ResponseEntity<byte[]> response = restTemplate.exchange(serviceUrl, HttpMethod.POST,
                entity, byte[].class);

        final InputStream inputStream = new ByteArrayInputStream(response.getBody());
        final Map<String, ?> responseHeaders = mapper.toHeaders(response.getHeaders());
        return MessageBuilder.withPayload(inputStream).copyHeaders(responseHeaders).build();
    }

    private CloseableHttpClient prepareClient(final Message<byte[]> message)
            throws MessageProcessorException {
        final Configuration config = (Configuration) message.getHeaders()
                .get(WSInvokeService.HEADER_CONFIGURATION);

        final KeyStore keystore = prepareKeystore(config.getDirKeystoreSsl(),
                config.getPasswordKeystoreSsl());
        final KeyStore truststore = prepareKeystore(config.getDirKeystore(),
                config.getPasswordKeystore());

        try {
            final SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(truststore, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(final X509Certificate[] chain,
                                final String authType) throws CertificateException {
                            return true;
                        }
                    }).loadKeyMaterial(keystore, config.getPasswordKeystoreSsl().toCharArray())
                    .build();

            final HttpClientBuilder builder = HttpClientBuilder.create();
            final SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(
                    sslContext.getSocketFactory(), new DefaultHostnameVerifier());

            builder.setSSLSocketFactory(sslConnectionFactory);
            final Registry<ConnectionSocketFactory> registry = RegistryBuilder
                    .<ConnectionSocketFactory> create().register("https", sslConnectionFactory)
                    .build();

            final String serviceUrl = (String) message.getHeaders().get("serviceUrl");
            builder.setConnectionManager(
                    poolConnectionService.getPoolConnectionManagerByUrl(serviceUrl, registry));
            builder.setConnectionManagerShared(true);
            return builder.build();
        } catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException
                | KeyStoreException | ConnectorException exception) {
            throw new MessageProcessorException("Error al configurar conexion ssl", exception);
        }

    }

    private String getPathFromUrl(final Message<byte[]> message) {
        final String url = getUrlFromMessageHeader(message);
        // Search by url after application context.
        return url.replaceFirst(REGEX_PATH, "$5");
    }
    private String getUrlFromMessageHeader(final Message<byte[]> message) {
        // Getting the path from message
        return (String) message.getHeaders().get(org.springframework.integration.http.HttpHeaders.REQUEST_URL);
    }

    private String getPortFromUrl(final Message<byte[]> message) throws MessageProcessorException {
        final String url = getUrlFromMessageHeader(message);

        try {
            // Getting the port from url string
            return String.valueOf(new URL(url).getPort());
        } catch (final MalformedURLException e) {
            // Throw business exception
            final String portError = "No se pudo obtener correctamente el puerto de la URL.";
            log.error(portError);
            throw new MessageProcessorException(portError, e);
        }
    }

    private KeyStore prepareKeystore(final String keystorePath, final String keystorePassword)
            throws MessageProcessorException {
        log.debug("Service - SSL config: keystorePath=" + keystorePath + ", keystorePassword="
                + keystorePassword);

        FileInputStream inputStream = null;
        try {
            final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            inputStream = new FileInputStream(new File(keystorePath));

            keystore.load(inputStream, keystorePassword.toCharArray());
            return keystore;
        } catch (final NoSuchAlgorithmException | CertificateException | IOException
                | KeyStoreException exception) {
            log.error("Error al intentar leer keystore", exception);
            throw new MessageProcessorException("Error al intentar leer keystore", exception);
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
