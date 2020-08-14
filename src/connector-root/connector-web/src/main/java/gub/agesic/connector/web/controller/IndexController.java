package gub.agesic.connector.web.controller;

import static gub.agesic.connector.web.controller.ConnectorController.CSS;
import static gub.agesic.connector.web.controller.ConnectorController.DANGER;
import static gub.agesic.connector.web.controller.ConnectorController.MSG;

import java.util.ArrayList;
import java.util.List;

import gub.agesic.connector.web.servlet3.MyWebInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.dbaccess.ConnectorService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by abrusco on 13/12/17.
 */
@Controller
@SessionAttributes(value = {"type", "tag"})
public class IndexController {

    public static final Integer CONNECTORS_PER_PAGE = 10;

    public static final String REDIRECT_TO_CONNECTORS = "redirect:/connectors/";

    public static final String PRODUCCION = "Produccion";

    public static final String TESTING = "Testing";

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    public static final String TYPE_CONNECTOR = "type";

    public static final String TAG_CONNECTOR = "tag";

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

        String type = PRODUCCION;
        if (model.asMap().containsKey(TYPE_CONNECTOR)) {
            type = model.asMap().get(TYPE_CONNECTOR).toString();
        }
        return showFilteredConnectors(model, type, redirectAttributes, request);
    }

    @GetMapping("/connectors/{pageId}")
    public ModelAndView showConnectorsByPage(final Model model,
                                             @PathVariable("pageId") final int pageId, final RedirectAttributes redirectAttributes, HttpServletRequest request) {
        final List<Connector> connectorList = connectorService.getConnectorList();

        return showConnectors(model, pageId, connectorList, redirectAttributes, request);
    }

    public ModelAndView showConnectors(final Model model, final int pageId,
                                       final List<Connector> connectorList, final RedirectAttributes redirectAttributes, HttpServletRequest request) {
        final String urlBase = request.getScheme() + "://" + request.getServerName();
        final List<Connector> resultConnectorList = new ArrayList<>();

        for (final Connector connector : connectorList) {
            final String port = connectorService.getPortByConnector(connector);
            final String wsdlURL = urlBase + ":" + port + connector.getPath() + "?wsdl";
            connector.setWsdlUrlForUI(wsdlURL);
            resultConnectorList.add(connector);
        }

        int maxUploadSize = MyWebInitializer.DEFAULT_MAX_UPLOAD_SIZE * 1024 * 1024;
        try {
            maxUploadSize = connectorService.getMaxUploadSize();
        } catch (ConnectorException e) {
            LOGGER.info(e.getMessage() + "Se utiliza el valor por defecto: "+MyWebInitializer.DEFAULT_MAX_UPLOAD_SIZE);
        }

        model.addAttribute("max_upload_size", maxUploadSize);
        model.addAttribute("connectors", resultConnectorList);

        return new ModelAndView("index");
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
            final List<Connector> connectorList = connectorService.getFilteredConnectorList(type,
                    tag);
            model.addAttribute("filtered", true);
            model.addAttribute(TYPE_CONNECTOR, type);
            model.addAttribute(TAG_CONNECTOR, tag);
            return showConnectors(model, pageId, connectorList, redirectAttributes, request);
        }
    }
}
