package gub.agesic.connector.web.controller;

import static gub.agesic.connector.web.controller.ConnectorController.CSS;
import static gub.agesic.connector.web.controller.ConnectorController.DANGER;
import static gub.agesic.connector.web.controller.ConnectorController.MSG;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Created by abrusco on 13/12/17.
 */
@Controller
@SessionAttributes(value = { "type", "tag" })
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
            final RedirectAttributes redirectAttributes) {
        String type = PRODUCCION;
        String tag = "";
        if (model.asMap().containsKey(TYPE_CONNECTOR)) {
            type = model.asMap().get(TYPE_CONNECTOR).toString();
        }
        if (model.asMap().containsKey(TAG_CONNECTOR)) {
            tag = model.asMap().get(TAG_CONNECTOR).toString();
        }
        return showFilteredConnectors(model, type, tag, redirectAttributes);
    }

    @GetMapping("/connectors/{pageId}")
    public ModelAndView showConnectorsByPage(final Model model,
            @PathVariable("pageId") final int pageId, final RedirectAttributes redirectAttributes) {
        final List<Connector> connectorList = connectorService.getConnectorList();
        return showConnectors(model, pageId, connectorList, redirectAttributes);
    }

    public ModelAndView showConnectors(final Model model, final int pageId,
            final List<Connector> connectorList, final RedirectAttributes redirectAttributes) {

        final int firstConnector = (pageId - 1) * CONNECTORS_PER_PAGE;
        int lastConnector = pageId * CONNECTORS_PER_PAGE;
        if (connectorList.size() < lastConnector) {
            lastConnector = connectorList.size();
        }
        final double connectorPerPage = CONNECTORS_PER_PAGE;
        int totalPages = (int) Math.ceil(connectorList.size() / connectorPerPage);
        if (totalPages == 0) {
            totalPages = 1;
        }

        if (pageId <= 0 || pageId > totalPages) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, "ERROR: No existe la p�gina " + pageId);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        }
        final List<Connector> resultConnectorList = new ArrayList<>();
        for (int i = firstConnector; i < lastConnector; i++) {
            resultConnectorList.add(connectorList.get(i));
        }
        model.addAttribute("connectors", resultConnectorList);
        model.addAttribute("actual_page", pageId);
        model.addAttribute("total_pages", totalPages);
        return new ModelAndView("index");
    }

    @GetMapping("/connectors/filtered")
    public ModelAndView showFilteredConnectors(final Model model,
            @RequestParam(value = "type", required = false) final String type,
            @RequestParam(value = "tag", required = false) final String tag,
            final RedirectAttributes redirectAttributes) {

        return showFilteredConnectorsByPage(model, 1, type, tag, redirectAttributes);
    }

    @GetMapping("/connectors/filtered/{pageId}")
    public ModelAndView showFilteredConnectorsByPage(final Model model,
            @PathVariable("pageId") final int pageId,
            @RequestParam(value = "type", required = false) final String type,
            @RequestParam(value = "tag", required = false) final String tag,
            final RedirectAttributes redirectAttributes) {

        if (type == null) {
            return showFirstConnectors(model, redirectAttributes);
        } else {
            final List<Connector> connectorList = connectorService.getFilteredConnectorList(type,
                    tag);
            model.addAttribute("filtered", true);
            model.addAttribute(TYPE_CONNECTOR, type);
            model.addAttribute(TAG_CONNECTOR, tag);
            return showConnectors(model, pageId, connectorList, redirectAttributes);
        }
    }
}
