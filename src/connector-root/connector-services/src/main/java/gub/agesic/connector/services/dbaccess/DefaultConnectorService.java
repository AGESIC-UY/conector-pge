package gub.agesic.connector.services.dbaccess;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.dataaccess.entity.RoleOperation;
import gub.agesic.connector.dataaccess.repository.ConfigurationRepository;
import gub.agesic.connector.dataaccess.repository.ConnectorRepository;
import gub.agesic.connector.dataaccess.repository.ConnectorType;
import gub.agesic.connector.dataaccess.repository.ConnectorTypeHolder;
import gub.agesic.connector.dataaccess.repository.GlobalConfigurationRepository;
import gub.agesic.connector.exceptions.ConnectorException;

@PropertySource("classpath:connector-pge.properties")
@Service
public class DefaultConnectorService implements ConnectorService {

    public static final String NAMESPACE_SOAP_1_1 = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String NAMESPACE_SOAP_1_2 = "http://www.w3.org/2003/05/soap-envelope";
    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String ERROR_ALIAS_DEL_KEYSTORE_ORGANISMO_NO_PUEDE_SER_VACIO = "Error: Alias del Keystore Organismo no puede ser vacío.";
    public static final String ERROR_PASSWORD_KEYSTORE_ORGANISMO_NO_PUEDE_SER_VACIO = "Error: Password Keystore Organismo no puede ser vacío.";
    public static final String ERROR_PASSWORD_KEYSTORE_SSL_NO_PUEDE_SER_VACIO = "Error: Password Keystore SSL no puede ser vacío.";
    public static final String ERROR_PASSWORD_TRUSTSTORE_NO_PUEDE_SER_VACIO = "Error: Password Truststore no puede ser vacío.";
    public static final String ERROR_TIPO_DE_TOKEN_NO_PUEDE_SER_VACIO = "Error: Tipo de token no puede ser vacío.";
    public static final String ERROR_URL_STS_GLOBAL_NO_PUEDE_SER_VACIO = "Error: URL STS Global no puede ser vacío.";

    @Autowired
    private Environment environment;

    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    private ConnectorRepository connectorRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Override
    public boolean existsConnectorByName(final String name, final String type) {
        final Optional<Boolean> connector = connectorRepository.existsConnectorByNameAndType(name,
                type);
        return connector.get();
    }

    @Override
    public ConnectorGlobalConfiguration getGlobalConfigurationByType(final String type)
            throws NoSuchElementException {
        final Optional<ConnectorGlobalConfiguration> gConfig = globalConfigurationRepository
                .findGlobalConfiguration(type);
        if (gConfig.isPresent()) {
            return gConfig.get();
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public Connector getConnector(final Long id) {
        return connectorRepository.findOne(id);
    }

    @Override
    public Optional<Connector> getConnectorByPathAndPort(final String path,
            final ConnectorTypeHolder connectorTypeHolder) throws ConnectorException {

        final Optional<Connector> connector = connectorRepository.getConnectorByPathAndType(path,
                connectorTypeHolder.getConnectorType().getEnvironment());
        if (connector.isPresent()) {
            if (connector.get().isEnableSsl() == connectorTypeHolder.isSslEnabled()) {
                return connector;
            } else {
                throw new ConnectorException("Esta tratando de consultar un conector "
                        + (connectorTypeHolder.isSslEnabled() == true ? "con" : "sin")
                        + " SSL, pero el conector está configurado para "
                        + (connectorTypeHolder.isSslEnabled() == true ? "no aceptar" : "aceptar")
                        + " SSL");
            }
        } else {
            throw new ConnectorException(
                    "No se encontro un conector con el path y tipo de puerto proporcionado: "
                            + "Path: " + path + " Tipo: " + connectorTypeHolder.getConnectorType());
        }
    }

    @Override
    public List<Connector> getConnectorList() {
        return connectorRepository.findAll();
    }

    @Override
    public List<Connector> getFilteredConnectorList(final String type, final String tag) {
        if (tag == null || tag.isEmpty()) {
            return connectorRepository.getFilteredConnectorsByType(type);
        } else {
            return connectorRepository.getFilteredConnectorsByTypeAndTag(type, tag);
        }
    }

    @Override
    public void checkConnectorPathAndTypeAvailabilityForType(final String name, final String path,
            final String type) throws ConnectorException {
        if (existsConnectorByName(name, type)) {
            final String errorMessage = "ERROR: Ya existe un Conector con ese nombre (" + name
                    + ") para el ambiente " + type;
            throw new ConnectorException(errorMessage);
        } else if (connectorRepository.getConnectorByPathAndType(path, type).isPresent()) {
            final String errorMessage = "ERROR: Ya existe un Conector con ese path (" + path
                    + ") para el ambiente " + type;
            throw new ConnectorException(errorMessage);
        }
    }

    @Override
    public void saveConnector(final Connector connector) throws ConnectorException {
        connectorRepository.save(connector);
    }

    @Override
    public void saveGlobalConfig(final ConnectorGlobalConfiguration globalConfig)
            throws ConnectorException {
        final StringBuilder errorsMsgs = new StringBuilder();
        if (StringUtils.isEmpty(globalConfig.getAliasKeystore())) {
            errorsMsgs.append(ERROR_ALIAS_DEL_KEYSTORE_ORGANISMO_NO_PUEDE_SER_VACIO);
            errorsMsgs.append("\n");
        }
        if (StringUtils.isEmpty(globalConfig.getPasswordKeystoreOrg())) {
            errorsMsgs.append(ERROR_PASSWORD_KEYSTORE_ORGANISMO_NO_PUEDE_SER_VACIO);
            errorsMsgs.append("\n");
        }
        if (StringUtils.isEmpty(globalConfig.getPasswordKeystoreSsl())) {
            errorsMsgs.append(ERROR_PASSWORD_KEYSTORE_SSL_NO_PUEDE_SER_VACIO);
            errorsMsgs.append("\n");
        }

        if (StringUtils.isEmpty(globalConfig.getPasswordKeystore())) {
            errorsMsgs.append(ERROR_PASSWORD_TRUSTSTORE_NO_PUEDE_SER_VACIO);
            errorsMsgs.append("\n");
        }
        if (StringUtils.isEmpty(globalConfig.getPolicyName())) {
            errorsMsgs.append(ERROR_TIPO_DE_TOKEN_NO_PUEDE_SER_VACIO);
            errorsMsgs.append("\n");
        }
        if (StringUtils.isEmpty(globalConfig.getStsGlobalUrl())) {
            errorsMsgs.append(ERROR_URL_STS_GLOBAL_NO_PUEDE_SER_VACIO);
            errorsMsgs.append("\n");
        }
        if (errorsMsgs.length() > 0) {
            throw new ConnectorException(errorsMsgs.toString());
        } else {
            configurationRepository.save(globalConfig);
        }

    }

    @Override
    public void updateConnectorPath(Connector connector) {
        //Adicionar '/' al inicio en caso de que se alla omitido
        String connectorPath = connector.getPath();
        String slash = "/";
        if (!slash.equals(connectorPath.substring(0, 1))) {
            connectorPath = slash.concat(connectorPath);
            connector.setPath(connectorPath);
        }
    }

    @Override
    public void deleteConnector(final Long id) {
        connectorRepository.delete(id);
    }

    @Override
    public List<RoleOperation> getRoleoperationsOperationFromWSDL(final Connector connector,
            final String operation, final String soapVersion) {
        return connectorRepository.getRoleoperationsOperationFromWSDL(connector, operation, soapVersion);
    }

    @Override
    public ConnectorTypeHolder getConnectorTypeByPort(final String port) throws ConnectorException {

        final String PORT_PROD = environment.getProperty("connector.http.client.port.prod");
        final String PORT_PROD_SSL = environment.getProperty("connector.http.client.port.prod.ssl");
        final String PORT_TEST = environment.getProperty("connector.http.client.port.test");
        final String PORT_TEST_SSL = environment.getProperty("connector.http.client.port.test.ssl");

        if (PORT_PROD.equals(port)) {
            return new ConnectorTypeHolder(ConnectorType.PRODUCCION, false);
        } else if (PORT_PROD_SSL.equals(port)) {
            return new ConnectorTypeHolder(ConnectorType.PRODUCCION, true);
        } else if (PORT_TEST.equals(port)) {
            return new ConnectorTypeHolder(ConnectorType.TEST, false);
        } else if (PORT_TEST_SSL.equals(port)) {
            return new ConnectorTypeHolder(ConnectorType.TEST, true);
        } else {
            throw new ConnectorException("Puerto no disponible: " + port);
        }

    }

    @Override
    public String getPortByConnector(final Connector connector) {
        final String PORT_PROD = environment.getProperty("connector.http.client.port.prod");
        final String PORT_PROD_SSL = environment.getProperty("connector.http.client.port.prod.ssl");
        final String PORT_TEST = environment.getProperty("connector.http.client.port.test");
        final String PORT_TEST_SSL = environment.getProperty("connector.http.client.port.test.ssl");

        if (connector.getType().equals(ConnectorType.PRODUCCION.getEnvironment())) {
            if (connector.isEnableSsl()) {
                return PORT_PROD_SSL;
            } else {
                return PORT_PROD;
            }
        } else {
            if (connector.isEnableSsl()) {
                return PORT_TEST_SSL;
            } else {
                return PORT_TEST;
            }
        }
    }

    @Override
    public String getLocationBasedOnConnector(final Connector connector) throws ConnectorException {
        final String port = getPortByConnector(connector);
        String protocol = PROTOCOL_HTTP;
        final String path = connector.getPath();
        final InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            throw new ConnectorException("Ip local no disponible");
        }

        if (connector.isEnableSsl()) {
            protocol = PROTOCOL_HTTPS;
        }
        return protocol + "://" + ip.getHostAddress() + ":" + port + path;
    }

    @Override
    public int getMaxUploadSize() throws ConnectorException{
        try {
            return Integer.parseInt(environment.getProperty("connector.max.upload.size")) * 1024 * 1024;
        } catch (Exception e) {
            throw new ConnectorException("No se pudo leer la propiedad 'connector.max.upload.size'");
        }
    }
}
