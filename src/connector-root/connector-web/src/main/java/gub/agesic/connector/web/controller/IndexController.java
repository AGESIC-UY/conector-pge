package gub.agesic.connector.web.controller;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.dataaccess.enums.EnvironmentType;
import gub.agesic.connector.dataaccess.enums.ExpirationStatus;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import gub.agesic.connector.web.servlet3.MyWebInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static gub.agesic.connector.web.controller.ConnectorController.*;
import static gub.agesic.connector.web.controller.GlobalConfigurationController.GLOBAL_CERTIFICATE_STATUS;

/**
 * Created by abrusco on 13/12/17.
 */
@Controller
@SessionAttributes(value = {"type", "tag"})
public class IndexController {

    public static final Integer CONNECTORS_PER_PAGE = 10;
    public static final String REDIRECT_TO_CONNECTORS = "redirect:/connectors/";
    public static final String TYPE_CONNECTOR = "type";
    public static final String TAG_CONNECTOR = "tag";
    public static final String VIEW_INDEX = "index";

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private ConnectorService connectorService;

    @GetMapping("/error")
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView errorPage(final RedirectAttributes redirectAttributes) {
        final String errorMessage = "ERROR: Esa p�gina no existe";
        LOGGER.error(errorMessage);
        redirectAttributes.addFlashAttribute(CSS, DANGER);
        redirectAttributes.addFlashAttribute(MSG, errorMessage);
        return new ModelAndView(REDIRECT_TO_CONNECTORS);
    }

    @ExceptionHandler(ConnectorException.class)
    public ModelAndView connectorExceptionErrorPage(final RedirectAttributes redirectAttributes) {
        return errorPage(redirectAttributes);
    }

    @GetMapping("/")
    public ModelAndView index() {
        return new ModelAndView(REDIRECT_TO_CONNECTORS);
    }

    @GetMapping("/connectors")
    public ModelAndView showFirstConnectors(final Model model,
                                            final RedirectAttributes redirectAttributes,
                                            final HttpServletRequest request) {

        String type = EnvironmentType.PRODUCTION.getName();
            if (model.asMap().containsKey(TYPE_CONNECTOR)) {
            type = model.asMap().get(TYPE_CONNECTOR).toString();
        }
        return showFilteredConnectors(model, type, redirectAttributes, request);
    }

    @GetMapping("/connectors/{pageId}")
    public ModelAndView showConnectorsByPage(final Model model,
                                             @PathVariable("pageId") final int pageId,
                                             final RedirectAttributes redirectAttributes, HttpServletRequest request) {

        final List<Connector> connectorList = connectorService.getConnectorList();
        return showConnectors(model, pageId, connectorList, redirectAttributes, request);
    }

    //TODO: Probar paginado y verificar argumentos que no se usan
    public ModelAndView showConnectors(final Model model, final int pageId,
                                       final List<Connector> connectorList,
                                       final RedirectAttributes redirectAttributes, HttpServletRequest request) {
        final String urlBase = request.getScheme() + "://" + request.getServerName();
        final List<Connector> resultConnectorList = new ArrayList<>();

        for (final Connector connector : connectorList) {
            final String port = connectorService.getPortByConnector(connector);
            final String wsdlURL = urlBase + ":" + port + connector.getPath() + "?wsdl";

            //TODO: Verificar por que se chequea si el conector tiene tipo el tipo definido?
            String type = EnvironmentType.TESTING.getName();
            if (model.asMap().containsKey(TYPE_CONNECTOR)) {
                type = model.asMap().get(TYPE_CONNECTOR).toString();
            }
            if (connector.getType() == null) {
                connector.setType(type);
            }

            ConnectorGlobalConfiguration globalConfig = null;
            try {
                globalConfig = connectorService.getGlobalConfigurationByType(type);
            } catch (NoSuchElementException e) {
                LOGGER.info("No existe ninguna configuración global para el ambiente seleccionado.", e);
            }

            ExpirationStatus expirationStatus = ExpirationStatus.UNKNOWN;
            // En caso de estar habilitada la notificacion de aviso local
            if (connector.isEnableLocalExpirationNotification() || (globalConfig != null && globalConfig.isEnableExpirationNotification())) {
                connectorService.setAllCertsExpirationStatus(connector);
                expirationStatus = connectorService.calculateExpirationStatus(connector);
            }

            connector.setExpirationStatus(expirationStatus.name());
            connector.setWsdlUrlForUI(wsdlURL);
            resultConnectorList.add(connector);
        }

        int maxUploadSize = MyWebInitializer.DEFAULT_MAX_UPLOAD_SIZE * 1024 * 1024;
        try {
            maxUploadSize = connectorService.getMaxUploadSize();
        } catch (ConnectorException e) {
            LOGGER.info(e.getMessage() + "Se utiliza el valor por defecto: " + MyWebInitializer.DEFAULT_MAX_UPLOAD_SIZE);
        }

        model.addAttribute("max_upload_size", maxUploadSize);
        model.addAttribute("connectors", resultConnectorList);

        String status = connectorService.getGlobalCertificateStatus().name();
        model.addAttribute(GLOBAL_CERTIFICATE_STATUS, status);

        return new ModelAndView(VIEW_INDEX);
    }

    @GetMapping("/connectors/filtered")
    public ModelAndView showFilteredConnectors(final Model model,
                                               @RequestParam(value = "type", required = false) final String type,
                                               final RedirectAttributes redirectAttributes,
                                               final HttpServletRequest request) {

        return showFilteredConnectorsByPage(model, 1, type, redirectAttributes, request);
    }

    @GetMapping("/connectors/filtered/{pageId}")
    public ModelAndView showFilteredConnectorsByPage(final Model model,
                                                     @PathVariable("pageId") final int pageId,
                                                     @RequestParam(value = "type", required = false) final String type,
                                                     final RedirectAttributes redirectAttributes,
                                                     final HttpServletRequest request) {

        String tag = "";
        if (type == null) {
            return showFirstConnectors(model, redirectAttributes, request);
        } else {
            final List<Connector> connectorList = connectorService.getFilteredConnectorList(type, tag);
            model.addAttribute("filtered", true);
            model.addAttribute(TYPE_CONNECTOR, type);
            model.addAttribute(TAG_CONNECTOR, tag);

            return showConnectors(model, pageId, connectorList, redirectAttributes, request);
        }
    }
}
