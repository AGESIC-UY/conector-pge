package gub.agesic.connector.web.servlet3;

import java.io.File;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import gub.agesic.connector.web.config.SpringRootConfig;
import gub.agesic.connector.web.config.SpringWebConfig;

public class MyWebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    private static final int MAX_UPLOAD_SIZE_IN_MB = 5 * 1024 * 1024; // 5 MB

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { SpringRootConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { SpringWebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    protected void customizeRegistration(final ServletRegistration.Dynamic registration) {

        // upload temp file will put here
        final File uploadDirectory = new File(System.getProperty("java.io.tmpdir"));

        // register a MultipartConfigElement
        final MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
                uploadDirectory.getAbsolutePath(), MAX_UPLOAD_SIZE_IN_MB, MAX_UPLOAD_SIZE_IN_MB * 2,
                MAX_UPLOAD_SIZE_IN_MB / 2);

        registration.setMultipartConfig(multipartConfigElement);

    }

}