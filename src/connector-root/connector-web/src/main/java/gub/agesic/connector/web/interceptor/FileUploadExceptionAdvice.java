package gub.agesic.connector.web.interceptor;

import gub.agesic.connector.web.servlet3.MyWebInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static gub.agesic.connector.web.controller.ConnectorController.*;

@ControllerAdvice
public class FileUploadExceptionAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadExceptionAdvice.class);

    @ExceptionHandler(MultipartException.class)
    public ModelAndView handleMultipartException(MultipartException exc, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.error(exc.getMessage());

        String viewName = request.getRequestURI().contains(PATH_UPLOAD_ADD) ? REDIRECT_TO_EDIT : REDIRECT_TO_CONNECTORS;
        FlashMap outputFlashMap = RequestContextUtils.getOutputFlashMap(request);

        if (outputFlashMap != null) {
            outputFlashMap.put(CSS, DANGER);
            outputFlashMap.put(MSG, "No se pudo subir el fichero. Formato no soportado.");
        }

        return new ModelAndView(viewName);
    }
}
