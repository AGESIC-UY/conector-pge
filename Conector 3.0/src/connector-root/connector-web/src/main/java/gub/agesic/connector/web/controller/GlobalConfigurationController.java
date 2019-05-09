package gub.agesic.connector.web.controller;

import static gub.agesic.connector.services.keystoremanager.DefaultKeystoreManagerService.KEYSTORE_ORG_FILENAME;
import static gub.agesic.connector.services.keystoremanager.DefaultKeystoreManagerService.KEYSTORE_SSL_FILENAME;
import static gub.agesic.connector.services.keystoremanager.DefaultKeystoreManagerService.KEYSTORE_TRUSTSTORE_FILENAME;
import static gub.agesic.connector.web.controller.ConnectorController.CSS;
import static gub.agesic.connector.web.controller.ConnectorController.DANGER;
import static gub.agesic.connector.web.controller.ConnectorController.MSG;
import static gub.agesic.connector.web.controller.ConnectorController.SUCCESS;
import static gub.agesic.connector.web.controller.IndexController.TESTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import gub.agesic.connector.services.keystoremanager.KeystoreManagerService;

/**
 * Created by abrusco on 13/12/17.
 */
@Controller
public class GlobalConfigurationController {

    public static final String GLOBAL_CONFIGURATION = "globalConfiguration";

    public static final String REDIRECT_TO_CONNECTORS = "redirect:/connectors/";

    public static final String REDIRECT_GLOBAL_CONFIGURATION_TYPE = "redirect:/globalConfiguration?type=";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GlobalConfigurationController.class);
    @Autowired
    private ConnectorService connectorService;
    @Autowired
    private KeystoreManagerService keystoreManagerService;

    @GetMapping("/globalConfiguration")
    public ModelAndView getGlobalConfiguration(final Model model,
            @RequestParam(value = "type", required = false) String type) {

        if (type == null) {
            type = TESTING;
        }
        try {
            final ConnectorGlobalConfiguration globalConfig = connectorService
                    .getGlobalConfigurationByType(type);
            model.addAttribute(GLOBAL_CONFIGURATION, globalConfig);
            model.addAttribute("aliasKeystore", globalConfig.getAliasKeystore());
        } catch (final NoSuchElementException e) {
            model.addAttribute(GLOBAL_CONFIGURATION, new ConnectorGlobalConfiguration());
        } finally {
            model.addAttribute("type", type);
            return new ModelAndView(GLOBAL_CONFIGURATION);
        }
    }

    @PostMapping("/globalConfiguration")
    public ModelAndView saveGlobalConfiguration(
            final @ModelAttribute(GLOBAL_CONFIGURATION) ConnectorGlobalConfiguration globalConfiguration,
            @RequestParam(value = "keystoreOrgFile", required = false) final MultipartFile keystoreOrgFile,
            @RequestParam(value = "keystoreSSLFile", required = false) final MultipartFile keystoreSSLFile,
            @RequestParam(value = "keystoreTruststoreFile", required = false) final MultipartFile keystoreTrustoreFile,
            final RedirectAttributes redirectAttributes, final Model model)
            throws ConnectorException {

        if (globalConfiguration == null) {
            final String errorMessage = "ERROR: No se encontró una Configuración Global";
            LOGGER.error(errorMessage);
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, errorMessage);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        }
        try {
            keystoreManagerService.setGlobalConfigurationKeystoresFilePaths(globalConfiguration);
            keystoreManagerService.uploadKeystoresGlobalConfiguration(globalConfiguration,
                    keystoreOrgFile, keystoreSSLFile, keystoreTrustoreFile);
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
    public void getGCKeystoreOrg(@RequestParam("type") final String type,
            final HttpServletResponse response, final RedirectAttributes redirectAttributes)
            throws ConnectorException {

        getGCKeystore(type, response, redirectAttributes, KEYSTORE_ORG_FILENAME);
    }

    @GetMapping("/globalConfiguration/keystoreSsl")
    public void getGCKeystoreSsl(@RequestParam("type") final String type,
            final HttpServletResponse response, final RedirectAttributes redirectAttributes)
            throws ConnectorException {

        getGCKeystore(type, response, redirectAttributes, KEYSTORE_SSL_FILENAME);
    }

    @GetMapping("/globalConfiguration/truststore")
    public void getGCTruststore(@RequestParam("type") final String type,
            final HttpServletResponse response, final RedirectAttributes redirectAttributes)
            throws ConnectorException {

        getGCKeystore(type, response, redirectAttributes, KEYSTORE_TRUSTSTORE_FILENAME);
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
    public ModelAndView cancelGlobalConfiguration(final RedirectAttributes redirectAttributes)
            throws ConnectorException {
        return new ModelAndView(REDIRECT_TO_CONNECTORS);
    }

}
