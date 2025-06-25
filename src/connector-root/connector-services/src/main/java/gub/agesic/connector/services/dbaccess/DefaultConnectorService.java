package gub.agesic.connector.services.dbaccess;

import com.sun.tools.doclint.Env;
import gub.agesic.connector.dataaccess.entity.*;
import gub.agesic.connector.dataaccess.enums.EnvironmentType;
import gub.agesic.connector.dataaccess.enums.ExpirationStatus;
import gub.agesic.connector.dataaccess.repository.ConfigurationRepository;
import gub.agesic.connector.dataaccess.repository.ConnectorRepository;
import gub.agesic.connector.dataaccess.repository.ConnectorTypeHolder;
import gub.agesic.connector.dataaccess.repository.GlobalConfigurationRepository;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.keystoremanager.KeystoreManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.*;

@PropertySource("file:${connector.integration.configLocation}/connector.properties")
@Service
public class DefaultConnectorService implements ConnectorService {

    public static final String NAMESPACE_SOAP_1_1 = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String NAMESPACE_SOAP_1_2 = "http://www.w3.org/2003/05/soap-envelope";
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_PORT_PROD = "9700";
    public static final String DEFAULT_PORT_TEST = "9800";
    public static final String DEFAULT_PORT_PROD_SSL = "8443";
    public static final String DEFAULT_PORT_TEST_SSL = "8553";
    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String ERROR_ALIAS_DEL_KEYSTORE_ORGANISMO_NO_PUEDE_SER_VACIO = "Error: Alias del Keystore Organismo no puede ser vacío.";
    public static final String ERROR_ALIAS_DEL_KEYSTORE_SSL_NO_PUEDE_SER_VACIO = "Error: Alias del Keystore SSL no puede ser vacío.";
    public static final String ERROR_PASSWORD_KEYSTORE_ORGANISMO_NO_PUEDE_SER_VACIO = "Error: Password Keystore Organismo no puede ser vacío.";
    public static final String ERROR_PASSWORD_KEYSTORE_SSL_NO_PUEDE_SER_VACIO = "Error: Password Keystore SSL no puede ser vacío.";
    public static final String ERROR_PASSWORD_TRUSTSTORE_NO_PUEDE_SER_VACIO = "Error: Password Truststore no puede ser vacío.";
    public static final String ERROR_TIPO_DE_TOKEN_NO_PUEDE_SER_VACIO = "Error: Tipo de token no puede ser vacío.";
    public static final String ERROR_SERVICIO_TIMEOUT_NO_PUEDE_SER_VACIO = "Error: Timeout en milisegundos no puede ser vacío.";
    public static final String ERROR_URL_STS_GLOBAL_NO_PUEDE_SER_VACIO = "Error: URL STS Global no puede ser vacío.";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectorService.class);

    private final Environment environment;
    private final GlobalConfigurationRepository globalConfigurationRepository;
    private final ConnectorRepository connectorRepository;
    private final ConfigurationRepository configurationRepository;
    private final KeystoreManagerService keystoreManagerService;

    @Autowired
    public DefaultConnectorService(Environment environment, GlobalConfigurationRepository globalConfigurationRepository, ConnectorRepository connectorRepository,
                                   ConfigurationRepository configurationRepository, KeystoreManagerService keystoreManagerService) {
        this.environment = environment;
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.connectorRepository = connectorRepository;
        this.configurationRepository = configurationRepository;
        this.keystoreManagerService = keystoreManagerService;
    }

    @Override
    public boolean existsConnectorByName(final String name, final String type) {
        return connectorRepository.existsConnectorByNameAndType(name, type);
    }

    @Override
    public boolean existsConnectorByName(final String name, final String type, final long id) {
        return connectorRepository.existsConnectorByNameAndType(name, type, id);
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
                connectorTypeHolder.getConnectorType().getName());
        if (connector.isPresent()) {
            if (connector.get().isEnableSsl() == connectorTypeHolder.isSslEnabled()) {
                return connector;
            } else {
                throw new ConnectorException("Esta tratando de consultar un servicio "
                        + (connectorTypeHolder.isSslEnabled() ? "con" : "sin")
                        + " SSL, pero el servicio está configurado para "
                        + (connectorTypeHolder.isSslEnabled() ? "no aceptar" : "aceptar")
                        + " SSL");
            }
        } else {
            throw new ConnectorException(
                    "No se encontró un servicio con el path y tipo de puerto proporcionado: "
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
            final String errorMessage = "ERROR: Ya existe un Servicio con ese nombre (" + name
                    + ") para el ambiente " + type;
            throw new ConnectorException(errorMessage);
        } else if (connectorRepository.getConnectorByPathAndType(path, type).isPresent()) {
            final String errorMessage = "ERROR: Ya existe un Servicio con ese path (" + path
                    + ") para el ambiente " + type;
            throw new ConnectorException(errorMessage);
        }
    }

    @Override
    public void checkConnectorPathAndTypeAvailabilityForType(final String name, final String path,
                                                             final String type, final long id) throws ConnectorException {
        if (existsConnectorByName(name, type, id)) {
            final String errorMessage = "ERROR: Ya existe un Servicio con ese nombre (" + name
                    + ") para el ambiente " + type;
            throw new ConnectorException(errorMessage);
        } else if (connectorRepository.getConnectorByPathAndType(path, type, id).isPresent()) {
            final String errorMessage = "ERROR: Ya existe un Servicio con ese path (" + path
                    + ") para el ambiente " + type;
            throw new ConnectorException(errorMessage);
        }
    }

    @Override
    public void saveConnector(final Connector connector) {
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
        if (StringUtils.isEmpty(globalConfig.getAliasKeystoreSSL())) {
            errorsMsgs.append(ERROR_ALIAS_DEL_KEYSTORE_SSL_NO_PUEDE_SER_VACIO);
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
        if (StringUtils.isEmpty(globalConfig.getServiceTimeOut())) {
            errorsMsgs.append(ERROR_SERVICIO_TIMEOUT_NO_PUEDE_SER_VACIO);
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
    public List<RoleOperation> getRoleOperationsFromWSDL(final Connector connector,
                                                         final String operation, final String soapVersion) {
        return connectorRepository.getRoleOperationsFromWSDL(connector, operation, soapVersion);
    }

    @Override
    public String getHost() {
        String host = environment.getProperty("connector.http.client.ip");
        if (host.isEmpty()) {
            final InetAddress ip;
            try {
                ip = InetAddress.getLocalHost();
                host = ip.getHostAddress();
            } catch (final UnknownHostException e) {
                LOGGER.error("Ip local no disponible", e);
                host = DEFAULT_HOST;
            }
        }
        return host;
    }

    @Override
    public String getPortByTypeAndProtocol(String type, boolean isSslEnabled) {
        final String PORT_PROD = environment.getProperty("connector.http.client.port.prod");
        final String PORT_PROD_SSL = environment.getProperty("connector.http.client.port.prod.ssl");
        final String PORT_TEST = environment.getProperty("connector.http.client.port.test");
        final String PORT_TEST_SSL = environment.getProperty("connector.http.client.port.test.ssl");

        if (type.equals(EnvironmentType.PRODUCTION.getName())) {
            return isSslEnabled
                    ? PORT_PROD_SSL.isEmpty() ? DEFAULT_PORT_PROD_SSL : PORT_PROD_SSL
                    : PORT_PROD.isEmpty() ? DEFAULT_PORT_PROD : PORT_PROD;
        } else {
            return isSslEnabled
                    ? PORT_TEST_SSL.isEmpty() ? DEFAULT_PORT_TEST_SSL : PORT_TEST_SSL
                    : PORT_TEST.isEmpty() ? DEFAULT_PORT_TEST : PORT_TEST;
        }
    }

    @Override
    public ConnectorTypeHolder getConnectorTypeByPort(final String port) throws ConnectorException {
        final String PORT_PROD = environment.getProperty("connector.http.client.port.prod");
        final String PORT_PROD_SSL = environment.getProperty("connector.http.client.port.prod.ssl");
        final String PORT_TEST = environment.getProperty("connector.http.client.port.test");
        final String PORT_TEST_SSL = environment.getProperty("connector.http.client.port.test.ssl");

        if (PORT_PROD.equals(port)) {
            return new ConnectorTypeHolder(EnvironmentType.PRODUCTION, false);
        } else if (PORT_PROD_SSL.equals(port)) {
            return new ConnectorTypeHolder(EnvironmentType.PRODUCTION, true);
        } else if (PORT_TEST.equals(port)) {
            return new ConnectorTypeHolder(EnvironmentType.TESTING, false);
        } else if (PORT_TEST_SSL.equals(port)) {
            return new ConnectorTypeHolder(EnvironmentType.TESTING, true);
        } else {
            throw new ConnectorException("Puerto no disponible: " + port);
        }
    }

    @Override
    public String getPortByConnector(final Connector connector) {
        return getPortByTypeAndProtocol(connector.getType(), connector.isEnableSsl());
    }

    @Override
    public String getLocationBasedOnConnector(final Connector connector) {
        final String protocol = connector.isEnableSsl() ? PROTOCOL_HTTPS : PROTOCOL_HTTP;
        final String host = getHost();
        final String port = getPortByConnector(connector);
        final String path = connector.getPath();

        return protocol + "://" + host + ":" + port + path;
    }

    @Override
    public int getMaxUploadSize() throws ConnectorException {
        try {
            return Integer.parseInt(environment.getProperty("connector.max.upload.size")) * 1024 * 1024;
        } catch (Exception e) {
            throw new ConnectorException("No se pudo leer la propiedad 'connector.max.upload.size'", e);
        }
    }

    @Override
    public ExpirationStatus getGlobalCertificateStatus() {
        ExpirationStatus testingStatus = getGlobalCertificateStatusByType(EnvironmentType.TESTING.getName());
        ExpirationStatus productionStatus = getGlobalCertificateStatusByType(EnvironmentType.PRODUCTION.getName());
        if (testingStatus == ExpirationStatus.EXPIRED || productionStatus == ExpirationStatus.EXPIRED) {
            return ExpirationStatus.EXPIRED;
        } else if (testingStatus == ExpirationStatus.EXPIRE_SOON || productionStatus == ExpirationStatus.EXPIRE_SOON) {
            return ExpirationStatus.EXPIRE_SOON;
        } else if (testingStatus == ExpirationStatus.UNKNOWN && productionStatus == ExpirationStatus.UNKNOWN) {
            return ExpirationStatus.UNKNOWN;
        }
        return ExpirationStatus.VALID;
    }

    private ExpirationStatus getGlobalCertificateStatusByType(String type) {
        try {
            ConnectorGlobalConfiguration globalConfig = getGlobalConfigurationByType(type);
            if (globalConfig.isEnableExpirationNotification()) {
                int expirationNoticeDays = globalConfig.getExpirationNoticeDays();
                ExpirationStatus orgExpirationStatus = calculateExpirationStatusKeyStoreOrg(globalConfig, expirationNoticeDays);
                ExpirationStatus sslExpirationStatus = calculateExpirationStatusKeyStoreSSL(globalConfig, expirationNoticeDays);
                ExpirationStatus truststoreExpirationStatus = calculateExpirationStatusTruststore(globalConfig, expirationNoticeDays, type);

                if (orgExpirationStatus == ExpirationStatus.EXPIRED
                        || sslExpirationStatus == ExpirationStatus.EXPIRED
                        || truststoreExpirationStatus == ExpirationStatus.EXPIRED) {
                    return ExpirationStatus.EXPIRED;
                } else if (orgExpirationStatus == ExpirationStatus.EXPIRE_SOON
                        || sslExpirationStatus == ExpirationStatus.EXPIRE_SOON
                        || truststoreExpirationStatus == ExpirationStatus.EXPIRE_SOON) {
                    return ExpirationStatus.EXPIRE_SOON;
                } else {
                    return ExpirationStatus.VALID;
                }
            }
        } catch (final Exception e) {
            LOGGER.info(e.getMessage(), e);
            return ExpirationStatus.UNKNOWN;
        }

        return ExpirationStatus.UNKNOWN;
    }

    @Override
    public ExpirationStatus calculateExpirationStatus(Connector connector) {
        ExpirationStatus expKeyOrg = ExpirationStatus.valueOf(connector.getExpKeystoreOrgStatus());
        ExpirationStatus expKeySSL = ExpirationStatus.valueOf(connector.getExpKeystoreSSLStatus());
        ExpirationStatus expTruststore = ExpirationStatus.valueOf(connector.getExpKeystoreTruststoreStatus());

        if (expKeyOrg.equals(ExpirationStatus.EXPIRED) || expKeySSL.equals(ExpirationStatus.EXPIRED) || expTruststore.equals(ExpirationStatus.EXPIRED)) {
            return ExpirationStatus.EXPIRED;
        } else if (expKeyOrg.equals(ExpirationStatus.EXPIRE_SOON) || expKeySSL.equals(ExpirationStatus.EXPIRE_SOON) || expTruststore.equals(ExpirationStatus.EXPIRE_SOON)) {
            return ExpirationStatus.EXPIRE_SOON;
        } else if (expKeyOrg.equals(ExpirationStatus.UNKNOWN) && expKeySSL.equals(ExpirationStatus.UNKNOWN) && expTruststore.equals(ExpirationStatus.UNKNOWN)) {
            return ExpirationStatus.UNKNOWN;
        }
        return ExpirationStatus.VALID;
    }

    @Override
    public ExpirationStatus calculateExpirationStatusKeyStoreOrg(Configuration configuration, int expirationNoticeDays) {
        String aliasKeystore = configuration.getAliasKeystore();

        Date keystoreOrgExpDate;
        try {
            keystoreOrgExpDate = keystoreManagerService.getCertificateExpirationDate(Paths.get(configuration.getDirKeystoreOrg()), aliasKeystore, configuration.getPasswordKeystoreOrg());
        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
            return ExpirationStatus.UNKNOWN;
        }

        return calculateExpirationStatus(keystoreOrgExpDate, expirationNoticeDays);
    }

    @Override
    public ExpirationStatus calculateExpirationStatusKeyStoreSSL(Configuration configuration, int expirationNoticeDays) {
        String aliasKeystoreSSL = configuration.getAliasKeystoreSSL();

        Date keystoreSSLExpDate;
        try {
            keystoreSSLExpDate = keystoreManagerService.getCertificateExpirationDate(Paths.get(configuration.getDirKeystoreSsl()), aliasKeystoreSSL, configuration.getPasswordKeystoreSsl());
        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
            return ExpirationStatus.UNKNOWN;
        }

        return calculateExpirationStatus(keystoreSSLExpDate, expirationNoticeDays);
    }

    @Override
    public ExpirationStatus calculateExpirationStatusTruststore(Configuration configuration, int expirationNoticeDays, String envType) {
        // Obtener el alias del truststore de las properties
        String aliasKeystore;
        if (envType.equals(EnvironmentType.TESTING.getName())) {
            aliasKeystore = environment.getProperty("connector.truststore.alias.test");
        } else {
            aliasKeystore = environment.getProperty("connector.truststore.alias.prod");
        }

        if (aliasKeystore.isEmpty()) {
            return ExpirationStatus.UNKNOWN;
        }

        Date keystoreTruststoreExpDate;
        try {
            keystoreTruststoreExpDate = keystoreManagerService.getCertificateExpirationDate(Paths.get(configuration.getDirKeystore()), aliasKeystore, configuration.getPasswordKeystore());
        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
            return ExpirationStatus.UNKNOWN;
        }

        return calculateExpirationStatus(keystoreTruststoreExpDate, expirationNoticeDays);
    }

    private ExpirationStatus calculateExpirationStatus(Date expirationDate, int expirationNoticeDays) {
        if (expirationDate == null) {
            return ExpirationStatus.UNKNOWN;
        }

        if (expirationDate.before(new Date())) {
            return ExpirationStatus.EXPIRED;
        }

        Calendar keystoreOrgEstimatedExpDate = Calendar.getInstance();
        keystoreOrgEstimatedExpDate.setTime(expirationDate);
        keystoreOrgEstimatedExpDate.add(Calendar.DATE, -expirationNoticeDays);

        if (new Date().after(keystoreOrgEstimatedExpDate.getTime())) {
            return ExpirationStatus.EXPIRE_SOON;
        }

        return ExpirationStatus.VALID;
    }

    public void setAllCertsExpirationStatus(Connector connector) {
        String envType = connector.getType();
        ConnectorGlobalConfiguration globalConfig = null;
        try {
            globalConfig = getGlobalConfigurationByType(envType);
        } catch (NoSuchElementException e) {
            LOGGER.info("No existe ninguna configuración global para el ambiente seleccionado.", e);
        }

        ExpirationStatus orgExpirationStatus = ExpirationStatus.UNKNOWN;
        ExpirationStatus sslExpirationStatus = ExpirationStatus.UNKNOWN;
        ExpirationStatus truststoreExpirationStatus = ExpirationStatus.UNKNOWN;

        int expirationNoticeDays;
        if (connector.isEnableLocalExpirationNotification()) {
            expirationNoticeDays = connector.getLocalExpirationNoticeDays();
            if (connector.isEnableLocalConfiguration()) {
                ConnectorLocalConfiguration localConfiguration = connector.getLocalConfiguration();
                if (localConfiguration != null) {
                    orgExpirationStatus = calculateExpirationStatusKeyStoreOrg(localConfiguration, expirationNoticeDays);
                    sslExpirationStatus = calculateExpirationStatusKeyStoreSSL(localConfiguration, expirationNoticeDays);
                    truststoreExpirationStatus = calculateExpirationStatusTruststore(localConfiguration, expirationNoticeDays, envType);
                }
            } else {
                if (globalConfig != null) {
                    orgExpirationStatus = calculateExpirationStatusKeyStoreOrg(globalConfig, expirationNoticeDays);
                    sslExpirationStatus = calculateExpirationStatusKeyStoreSSL(globalConfig, expirationNoticeDays);
                    truststoreExpirationStatus = calculateExpirationStatusTruststore(globalConfig, expirationNoticeDays, envType);
                }
            }
        } else {
            if (globalConfig != null) {
                if (globalConfig.isEnableExpirationNotification()) {
                    expirationNoticeDays = globalConfig.getExpirationNoticeDays();
                    if (connector.isEnableLocalConfiguration()) {
                        ConnectorLocalConfiguration localConfiguration = connector.getLocalConfiguration();
                        if (localConfiguration != null) {
                            orgExpirationStatus = calculateExpirationStatusKeyStoreOrg(localConfiguration, expirationNoticeDays);
                            sslExpirationStatus = calculateExpirationStatusKeyStoreSSL(localConfiguration, expirationNoticeDays);
                            truststoreExpirationStatus = calculateExpirationStatusTruststore(localConfiguration, expirationNoticeDays, envType);
                        }
                    } else {
                        orgExpirationStatus = calculateExpirationStatusKeyStoreOrg(globalConfig, expirationNoticeDays);
                        sslExpirationStatus = calculateExpirationStatusKeyStoreSSL(globalConfig, expirationNoticeDays);
                        truststoreExpirationStatus = calculateExpirationStatusTruststore(globalConfig, expirationNoticeDays, envType);
                    }
                }
            }
        }

        connector.setExpKeystoreOrgStatus(orgExpirationStatus.name());
        connector.setExpKeystoreSSLStatus(sslExpirationStatus.name());
        connector.setExpKeystoreTruststoreStatus(truststoreExpirationStatus.name());
    }
}
