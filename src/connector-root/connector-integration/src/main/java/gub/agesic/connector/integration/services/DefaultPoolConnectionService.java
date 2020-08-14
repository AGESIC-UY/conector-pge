package gub.agesic.connector.integration.services;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import gub.agesic.connector.exceptions.ConnectorException;

@Service
@PropertySource("file:${connector.web.configLocation}/connector.properties")
public class DefaultPoolConnectionService implements PoolConnectionService {

    private static final Logger log = LoggerFactory.getLogger(DefaultPoolConnectionService.class);

    private final Map<String, PoolingHttpClientConnectionManager> mapPoolConnectionManager;

    @Autowired
    private Environment environment;

    public DefaultPoolConnectionService() {
        mapPoolConnectionManager = new HashMap<>();
    }

    private Optional<PoolingHttpClientConnectionManager> getPoolConnectionManager(final String code)
            throws ConnectorException {

        if (Objects.nonNull(mapPoolConnectionManager)
                && mapPoolConnectionManager.containsKey(code)) {

            return Optional.of(mapPoolConnectionManager.get(code));
        } else {
            return Optional.empty();
        }
    }

    private PoolingHttpClientConnectionManager createPoolConnectionManager(final String host,
            final String port, final Registry<ConnectionSocketFactory> registry) {
        PoolingHttpClientConnectionManager poolingConnManager;
        if (Objects.isNull(registry)) {
            poolingConnManager = new PoolingHttpClientConnectionManager();
        } else {
            poolingConnManager = new PoolingHttpClientConnectionManager(registry);
        }

        poolingConnManager.setMaxTotal(Integer.parseInt(environment
                .getProperty("connector.pool_connection_manager.maxtotalopenconnections")));
        poolingConnManager.setDefaultMaxPerRoute(Integer.parseInt(environment
                .getProperty("connector.pool_connection_manager.defaultmaxconnectionsperroute")));
        final HttpHost httpHostName = new HttpHost(host, Integer.parseInt(port));
        poolingConnManager.setMaxPerRoute(new HttpRoute(httpHostName), Integer.parseInt(environment
                .getProperty("connector.pool_connection_manager.maxconnectionshostnameport")));

        mapPoolConnectionManager.put(host + port, poolingConnManager);

        return poolingConnManager;
    }

    @Override
    public PoolingHttpClientConnectionManager getPoolConnectionManagerByUrl(final String url,
            final Registry<ConnectionSocketFactory> registry) throws ConnectorException {
        final String port = getPortFromUrl(url);
        final String host = getDomainName(url);

        final Optional<PoolingHttpClientConnectionManager> optionalPoolingHttpClientConnectionManager = getPoolConnectionManager(
                host + port);
        return optionalPoolingHttpClientConnectionManager
                .orElseGet(() -> createPoolConnectionManager(host, port, registry));

    }

    private String getPortFromUrl(final String url) throws ConnectorException {
        try {
            // Getting the port from url string
            return String.valueOf(new URL(url).getPort());
        } catch (final MalformedURLException e) {
            // Throw business exception
            final String portError = "No se pudo obtener correctamente el puerto de la URL.";
            log.error(portError);
            throw new ConnectorException(portError, e);
        }
    }

    public String getDomainName(final String url) throws ConnectorException {
        URI uri;
        try {
            uri = new URI(url);
            return uri.getHost();
        } catch (final URISyntaxException e) {
            // Throw business exception
            final String hostError = "No se pudo obtener correctamente el host de la URL.";
            log.error(hostError);
            throw new ConnectorException(hostError, e);
        }

    }

}
