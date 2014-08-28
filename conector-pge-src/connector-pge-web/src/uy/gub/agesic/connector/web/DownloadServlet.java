package uy.gub.agesic.connector.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uy.gub.agesic.connector.session.api.ConnectorManager;
import uy.gub.agesic.connector.util.Constants;
import uy.gub.agesic.connector.util.Props;

public class DownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	//String idConnector = request.getParameter("id");
    	String nameConnector = request.getParameter("name");
    	String typeConnector = request.getParameter("type");
    	String fileName = request.getParameter("fileName");
    	
    	InitialContext ctx;
		try {
			ctx = new InitialContext();
			ConnectorManager connectorManager = (ConnectorManager) ctx.lookup(Constants.CONNECTOR_MANAGER_REMOTE); //"connector-pge-ear/ConnectorManagerSession/remote");
			String basic_path = connectorManager.getBasicPath();
			String pathFile = basic_path + nameConnector + "_" + typeConnector + "/" + fileName;
	    	
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
			
		} catch (NamingException e) {
			e.printStackTrace();
		}
    }
    
    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                // Do your thing with the exception. Print it, log it or mail it.
                e.printStackTrace();
            }
        }
    }
   

}
