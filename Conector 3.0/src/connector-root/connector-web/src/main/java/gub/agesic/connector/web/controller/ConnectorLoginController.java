package gub.agesic.connector.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ConnectorLoginController {

    public static final String CSS = "css";

    public static final String MSG = "msg";

    public static final String DANGER = "danger";

    public static final String SUCCESS = "success";

    @RequestMapping(value = "/loginPage", method = RequestMethod.GET)
    public ModelAndView loginPage(
            @RequestParam(value = "error", required = false) final String error,
            @RequestParam(value = "logout", required = false) final String logout) {
        final ModelAndView model = new ModelAndView();
        if (error != null) {
            model.addObject(CSS, DANGER);
            model.addObject(MSG, "Credenciales Inválidas.");
        }

        if (logout != null) {
            model.addObject(CSS, SUCCESS);
            model.addObject(MSG, "Te has deslogueado correctamente de la aplicación.");
        }

        model.setViewName("loginPage");
        return model;
    }
}