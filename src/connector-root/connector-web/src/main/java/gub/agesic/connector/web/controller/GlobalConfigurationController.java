package gub.agesic.connector.web.controller;

import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.dataaccess.enums.EnvironmentType;
import gub.agesic.connector.dataaccess.enums.ExpirationStatus;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import gub.agesic.connector.services.keystoremanager.KeystoreManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.NoSuchElementException;

import static gub.agesic.connector.web.controller.ConnectorController.*;

/**
 * Created by abrusco on 13/12/17.
 */
@Controller
public class GlobalConfigurationController {
    private static final String GLOBAL_TYPE = "type";
    private static final String GLOBAL_ALIAS_KEYSTORE_ORG = "aliasKeystore";
    private static final String GLOBAL_ALIAS_KEYSTORE_SSL = "aliasKeystoreSSL";

    private static final String GLOBAL_KEYSTORE_FILE = "keystoreFile";
    private static final String GLOBAL_KEYSTORE_ORG_FILE = "keystoreOrgFile";
    private static final String GLOBAL_KEYSTORE_SSL_FILE = "keystoreSSLFile";
    private static final String GLOBAL_KEYSTORE_TRUSTSTORE_FILE = "keystoreTruststoreFile";

    private static final String GLOBAL_DIR_KEYSTORE_ORG = "dirKeystoreOrg";
    private static final String GLOBAL_DIR_KEYSTORE_SSL = "dirKeystoreSsl";
    private static final String GLOBAL_DIR_KEYSTORE = "dirKeystore";

    private static final String GLOBAL_ENABLE_EXPIRATION_NOTIFICATION = "enableExpirationNotification";
    private static final String GLOBAL_EXPIRATION_NOTICE_DAYS = "expirationNoticeDays";

    private static final String GLOBAL_EXP_KEYSTORE_ORG_STATUS = "expKeystoreOrgStatus";
    private static final String GLOBAL_EXP_KEYSTORE_SSL_STATUS = "expKeystoreSSLStatus";
    private static final String GLOBAL_EXP_KEYSTORE_TRUSTSTORE_STATUS = "expKeystoreTruststoreStatus";

    private static final String EXP_DATE_FORMAT = "dd-MM-yyyy";
    private static final String GLOBAL_EXP_DATE_KEYSTORE_ORG = "expDateKeystoreOrg";
    private static final String GLOBAL_EXP_DATE_KEYSTORE_SSL = "expDateKeystoreSsl";
    private static final String GLOBAL_EXP_DATE_KEYSTORE_TRUSTSTORE = "expDateKeystoreTruststore";

    private static final String GLOBAL_SERVICE_TIMEOUT = "serviceTimeOut";
    private static final String GLOBAL_HOST = "host";
    private static final String GLOBAL_PORT = "port";
    private static final String GLOBAL_PORT_SSL = "portSsl";

    public static final String GLOBAL_CONFIGURATION = "globalConfiguration";
    public static final String GLOBAL_CERTIFICATE_STATUS = "globalCertificateStatus";
    public static final String REDIRECT_TO_CONNECTORS = "redirect:/connectors/";
    public static final String REDIRECT_GLOBAL_CONFIGURATION_TYPE = "redirect:/globalConfiguration?type=";

    private static final int GLOBAL_TIMEOUT_DEFAULT = 30000;
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalConfigurationController.class);

    @Autowired
    private Environment environment;
    @Autowired
    private ConnectorService connectorService;
    @Autowired
    private KeystoreManagerService keystoreManagerService;

    @GetMapping("/globalConfiguration")
    public ModelAndView getGlobalConfiguration(final Model model,
                                               @RequestParam(value = GLOBAL_TYPE, required = false) String type) {

        if (type == null) {
            type = EnvironmentType.TESTING.getName();
        }
        model.addAttribute(GLOBAL_TYPE, type);

        try {
            final ConnectorGlobalConfiguration globalConfig = connectorService.getGlobalConfigurationByType(type);
            model.addAttribute(GLOBAL_CONFIGURATION, globalConfig);
            model.addAttribute(GLOBAL_ALIAS_KEYSTORE_ORG, globalConfig.getAliasKeystore());
            model.addAttribute(GLOBAL_ALIAS_KEYSTORE_SSL, globalConfig.getAliasKeystoreSSL());
            model.addAttribute(GLOBAL_DIR_KEYSTORE_ORG, globalConfig.getDirKeystoreOrg());
            model.addAttribute(GLOBAL_DIR_KEYSTORE_SSL, globalConfig.getDirKeystoreSsl());
            model.addAttribute(GLOBAL_DIR_KEYSTORE, globalConfig.getDirKeystore());
            model.addAttribute(GLOBAL_ENABLE_EXPIRATION_NOTIFICATION, globalConfig.isEnableExpirationNotification());
            model.addAttribute(GLOBAL_EXPIRATION_NOTICE_DAYS, globalConfig.getExpirationNoticeDays());
            model.addAttribute(GLOBAL_SERVICE_TIMEOUT, globalConfig.getServiceTimeOut());

            if (globalConfig.isEnableExpirationNotification()) {
                model.addAttribute(GLOBAL_EXP_KEYSTORE_ORG_STATUS, connectorService.calculateExpirationStatusKeyStoreOrg(globalConfig, globalConfig.getExpirationNoticeDays()).name());
                model.addAttribute(GLOBAL_EXP_KEYSTORE_SSL_STATUS, connectorService.calculateExpirationStatusKeyStoreSSL(globalConfig, globalConfig.getExpirationNoticeDays()).name());
                model.addAttribute(GLOBAL_EXP_KEYSTORE_TRUSTSTORE_STATUS, connectorService.calculateExpirationStatusTruststore(globalConfig, globalConfig.getExpirationNoticeDays(), type).name());
            }

            String status = connectorService.getGlobalCertificateStatus().name();
            model.addAttribute(GLOBAL_CERTIFICATE_STATUS, status);

            loadCertsData(globalConfig, model);
        } catch (NoSuchElementException e) {
            model.addAttribute(GLOBAL_CONFIGURATION, new ConnectorGlobalConfiguration());
            model.addAttribute(GLOBAL_SERVICE_TIMEOUT, GLOBAL_TIMEOUT_DEFAULT);
            model.addAttribute(GLOBAL_CERTIFICATE_STATUS, ExpirationStatus.UNKNOWN.name());
        }

        model.addAttribute(GLOBAL_HOST, connectorService.getHost());
        model.addAttribute(GLOBAL_PORT, connectorService.getPortByTypeAndProtocol(type, false));
        model.addAttribute(GLOBAL_PORT_SSL, connectorService.getPortByTypeAndProtocol(type, true));

        return new ModelAndView(GLOBAL_CONFIGURATION);
    }

    private void loadCertsData(final ConnectorGlobalConfiguration globalConfig, final Model model) {
        String aliasKeystore = globalConfig.getAliasKeystore();
        String aliasKeystoreSSL = globalConfig.getAliasKeystoreSSL();
        String aliasTruststore;
        if (globalConfig.getType().equals(EnvironmentType.TESTING.getName())) {
            aliasTruststore = environment.getProperty("connector.truststore.alias.test");
        } else {
            aliasTruststore = environment.getProperty("connector.truststore.alias.prod");
        }
        Path keystorePath = Paths.get(globalConfig.getDirKeystoreOrg());
        Date expDate;

        try {
            expDate = keystoreManagerService.getCertificateExpirationDate(keystorePath, aliasKeystore, globalConfig.getPasswordKeystoreOrg());
            model.addAttribute(GLOBAL_EXP_DATE_KEYSTORE_ORG, new SimpleDateFormat(EXP_DATE_FORMAT).format(expDate));
        } catch (ConnectorException e) {
            model.addAttribute(GLOBAL_EXP_DATE_KEYSTORE_ORG, e.getMessage());
        }

        keystorePath = Paths.get(globalConfig.getDirKeystoreSsl());
        try {
            expDate = keystoreManagerService.getCertificateExpirationDate(keystorePath, aliasKeystoreSSL, globalConfig.getPasswordKeystoreSsl());
            model.addAttribute(GLOBAL_EXP_DATE_KEYSTORE_SSL, new SimpleDateFormat(EXP_DATE_FORMAT).format(expDate));
        } catch (ConnectorException e) {
            model.addAttribute(GLOBAL_EXP_DATE_KEYSTORE_SSL, e.getMessage());
        }

        keystorePath = Paths.get(globalConfig.getDirKeystore());
        try {
            expDate = keystoreManagerService.getCertificateExpirationDate(keystorePath, aliasTruststore, globalConfig.getPasswordKeystore());
            model.addAttribute(GLOBAL_EXP_DATE_KEYSTORE_TRUSTSTORE, new SimpleDateFormat(EXP_DATE_FORMAT).format(expDate) + " (" + aliasTruststore + ")");
        } catch (ConnectorException e) {
            model.addAttribute(GLOBAL_EXP_DATE_KEYSTORE_TRUSTSTORE, e.getMessage());
        }
    }

    @PostMapping("/globalConfiguration")
    public ModelAndView saveGlobalConfiguration(
            final @ModelAttribute(GLOBAL_CONFIGURATION) ConnectorGlobalConfiguration globalConfiguration,
            @RequestParam(value = GLOBAL_KEYSTORE_ORG_FILE, required = false) final MultipartFile keystoreOrgFile,
            @RequestParam(value = GLOBAL_KEYSTORE_SSL_FILE, required = false) final MultipartFile keystoreSSLFile,
            @RequestParam(value = GLOBAL_KEYSTORE_TRUSTSTORE_FILE, required = false) final MultipartFile keystoreTruststoreFile,
            final RedirectAttributes redirectAttributes) {

        if (globalConfiguration == null) {
            final String errorMessage = "ERROR: No se encontró una Configuración Global";
            LOGGER.error(errorMessage);
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, errorMessage);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        }
        try {
            keystoreManagerService.setGlobalConfigurationKeystoresFilePaths(globalConfiguration,
                    keystoreOrgFile, keystoreSSLFile, keystoreTruststoreFile);
            keystoreManagerService.uploadKeystoresByConfiguration(globalConfiguration,
                    keystoreOrgFile, keystoreSSLFile, keystoreTruststoreFile);
            connectorService.saveGlobalConfig(globalConfiguration);
            redirectAttributes.addFlashAttribute(CSS, SUCCESS);
            redirectAttributes.addFlashAttribute(MSG,
                    "Se actualizó la Configuración Global para el ambiente "
                            + globalConfiguration.getType());
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        } catch (final ConnectorException e) {
            final String[] arrayMsgs = e.getMessage().split("\n");
            if (arrayMsgs.length > 1) {
                redirectAttributes.addFlashAttribute(MSG, Arrays.asList(arrayMsgs));
            } else {
                redirectAttributes.addFlashAttribute(MSG, e.getMessage());
            }
            redirectAttributes.addFlashAttribute(CSS, DANGER);

            return new ModelAndView(
                    REDIRECT_GLOBAL_CONFIGURATION_TYPE + globalConfiguration.getType());
        }
    }

    @GetMapping("/globalConfiguration/keystoreOrg")
    public void getGCKeystoreOrg(@RequestParam(GLOBAL_TYPE) final String type,
                                 final HttpServletResponse response, final RedirectAttributes redirectAttributes) {
        try {
            getGCKeystore(type, response, redirectAttributes, GLOBAL_KEYSTORE_ORG_FILE);
        } catch (ConnectorException ce) {
            LOGGER.error(ce.getMessage(), ce);
        }

    }

    @GetMapping("/globalConfiguration/keystoreSsl")
    public void getGCKeystoreSsl(@RequestParam(GLOBAL_TYPE) final String type,
                                 final HttpServletResponse response, final RedirectAttributes redirectAttributes) {
        try {
            getGCKeystore(type, response, redirectAttributes, GLOBAL_KEYSTORE_SSL_FILE);
        } catch (ConnectorException ce) {
            LOGGER.error(ce.getMessage(), ce);
        }
    }

    @GetMapping("/globalConfiguration/truststore")
    public void getGCTruststore(@RequestParam(GLOBAL_TYPE) final String type,
                                final HttpServletResponse response, final RedirectAttributes redirectAttributes) {
        try {
            getGCKeystore(type, response, redirectAttributes, GLOBAL_KEYSTORE_FILE);
        } catch (ConnectorException ce) {
            LOGGER.error(ce.getMessage(), ce);
        }
    }

    public void getGCKeystore(final String type, final HttpServletResponse response,
                              final RedirectAttributes redirectAttributes, final String keystoreName)
            throws ConnectorException {
        final ConnectorGlobalConfiguration globalConfiguration = connectorService
                .getGlobalConfigurationByType(type);
        if (globalConfiguration == null) {
            final String errorMessage = "ERROR: No hay Configuración Global del ambiente " + type;
            LOGGER.error(errorMessage);
            throw new ConnectorException(errorMessage);
        } else {
            final Path keystorePath = keystoreManagerService.getGlobalConfigurationKeystore(type,
                    keystoreName);
            if (keystorePath.toFile().exists()) {
                response.setContentType("application/force-download");
                response.addHeader("Content-Disposition",
                        "attachment; filename=" + keystorePath.getFileName());
                try {
                    Files.copy(keystorePath, response.getOutputStream());
                    response.getOutputStream().flush();
                } catch (final IOException e) {
                    final String errorMessage = "ERROR: No se pudo encontrar el keystore "
                            + keystoreName + " para la Configuración Global del ambiente " + type;
                    LOGGER.error(errorMessage, e);
                    redirectAttributes.addFlashAttribute(CSS, DANGER);
                    redirectAttributes.addFlashAttribute(MSG, e.getMessage());
                    throw new ConnectorException(errorMessage, e);
                }
            }
        }
    }

    @PostMapping("/globalConfiguration/cancel")
    public ModelAndView cancelGlobalConfiguration() {
        return new ModelAndView(REDIRECT_TO_CONNECTORS);
    }

}
