package gub.agesic.connector.web.servlet3;

import gub.agesic.connector.web.config.SpringRootConfig;
import gub.agesic.connector.web.config.SpringWebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import java.io.File;

public class MyWebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    public static final int DEFAULT_MAX_UPLOAD_SIZE = 5; // 5 MB
    private static final Logger LOGGER = LoggerFactory.getLogger(MyWebInitializer.class);

    @Autowired
    private Environment environment;

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{SpringRootConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{SpringWebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected void customizeRegistration(final ServletRegistration.Dynamic registration) {
        int maxUploadSize = DEFAULT_MAX_UPLOAD_SIZE;
        try {
            maxUploadSize = Integer.parseInt(environment.getProperty("connector.max.upload.size"));
        } catch (Exception e) {
            LOGGER.info("No se pudo leer la propiedad 'connector.max.upload.size'. Se utiliza el valor por defecto: " + DEFAULT_MAX_UPLOAD_SIZE);
        }
        final int MAX_UPLOAD_SIZE_IN_MB = maxUploadSize * 1024 * 1024;

        // upload temp file will put here
        final File uploadDirectory = new File(System.getProperty("java.io.tmpdir"));

        // register a MultipartConfigElement
        final MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
                uploadDirectory.getAbsolutePath(), MAX_UPLOAD_SIZE_IN_MB, MAX_UPLOAD_SIZE_IN_MB * 2,
                MAX_UPLOAD_SIZE_IN_MB / 2);

        registration.setMultipartConfig(multipartConfigElement);

    }
}