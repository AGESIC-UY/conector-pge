package uy.gub.agesic.connector.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uy.gub.agesic.connector.session.api.ConnectorManager;
import uy.gub.agesic.connector.util.ConnectorFileManager;
import uy.gub.agesic.connector.util.Constants;


public class DownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	private static Log log = LogFactory.getLog(DownloadServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	//String idConnector = request.getParameter("id");
    	String typeConnector = request.getParameter("type");
    	String typeFile = request.getParameter("typeFile");
    	String fileName = request.getParameter("fileName");
    	///// Si esta condici�n en verdadera se cambia el type del conector para descargar el archivo del conector de testing que se esta pasando a producci�n
    	String editingToProduction = request.getParameter("editingToProduction");
    	
    	InitialContext ctx;
		try {
			String pathFile = null;
			if (typeFile.equals("connectorFile")){
				String nameConnector = request.getParameter("name");
				ctx = new InitialContext();
				ConnectorManager connectorManager = (ConnectorManager) ctx.lookup(Constants.CONNECTOR_MANAGER_REMOTE); //"connector-pge-ear/ConnectorManagerSession/remote");
				String basic_path = connectorManager.getBasicPath();
				
				if (Boolean.parseBoolean(editingToProduction)) {
					typeConnector = "Test";
				}
				
				pathFile = basic_path + nameConnector + "_" + typeConnector + "/" + fileName;
				
				File file = new File(pathFile);

		        if (!file.exists()) {
		            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
		            return;
		        }

		        // Get content type by filename.
		        String contentType = getServletContext().getMimeType(file.getName());

		        if (contentType == null) {
		            contentType = "application/octet-stream";
		        }

		        // Init servlet response.
		        response.reset();
		        response.setBufferSize(DEFAULT_BUFFER_SIZE);
		        response.setContentType(contentType);
		        response.setHeader("Content-Length", String.valueOf(file.length()));
		        response.setHeader("Content-Disposition", "attachment; filename=\"" + nameConnector + "_" + file.getName() + "\"");
		        
		        // Prepare streams.
		        BufferedInputStream input = null;
		        BufferedOutputStream output = null;

		        try {
		            // Open streams.
		            input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
		            output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

		             byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		            int length;
		            while ((length = input.read(buffer)) > 0) {
		                output.write(buffer, 0, length);
		            }
		        } finally {
		            close(output);
		            close(input);
		        }
			
		    	
			}
			if (typeFile.equals("globalFile")){
				ServletContext context = getServletContext();
			    InputStream is = context.getResourceAsStream("/docs/" + Constants.CONDICIONES_USO_FILENAME);
			    
			 // Get content type by filename.
		        String contentType = getServletContext().getMimeType(fileName);

		        if (contentType == null) {
		            contentType = "application/octet-stream";
		        }
		        
		     // Init servlet response.
		        response.reset();
		        response.setBufferSize(DEFAULT_BUFFER_SIZE);
		        response.setContentType(contentType);
		        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		        
		        BufferedInputStream input = null;
		        BufferedOutputStream output = null;

		        try {
		            // Open streams.
		            input = new BufferedInputStream(is , DEFAULT_BUFFER_SIZE);
		            output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

		            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		            int lengthFile=0; // Contador para calcular el largo del archivo 
		            int length;
		            while ((length = input.read(buffer)) > 0) {
		                output.write(buffer, 0, length);
		                lengthFile++;
		            }
		            response.setHeader("Content-Length", String.valueOf(lengthFile));
		        } finally {
		            close(output);
		            close(input);
		        }
		        
		    }
	    		
		} catch (NamingException e) {
			log.error(e.getLocalizedMessage(), e);
		}
    }
    
    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                // Do your thing with the exception. Print it, log it or mail it.
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }

}
