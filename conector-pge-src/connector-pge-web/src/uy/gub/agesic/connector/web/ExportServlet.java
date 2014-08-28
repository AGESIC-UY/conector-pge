package uy.gub.agesic.connector.web;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.exceptions.ConnectorException;
import uy.gub.agesic.connector.session.api.ConnectorManager;
import uy.gub.agesic.connector.util.Constants;

public class ExportServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	String idConnector = request.getParameter("id");
    	
    	InitialContext ctx;
		Connector connector;
		try {
			ctx = new InitialContext();
			ConnectorManager connectorManager = (ConnectorManager) ctx.lookup(Constants.CONNECTOR_MANAGER_REMOTE); //"connector-pge-ear/ConnectorManagerSession/remote");
			
			connector = connectorManager.getConnectorToExport(new Long(idConnector));			
			
			response.reset();
	        response.setBufferSize(DEFAULT_BUFFER_SIZE);
	        response.setContentType("application/xml");
	        //response.setHeader("Content-Length", String.valueOf(file.length()));
	        response.setHeader("Content-Disposition", "attachment; filename=\""+ connector.getName() +"_export.xml\"");
	        
			JAXBContext context = JAXBContext.newInstance(Connector.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(connector, response.getOutputStream());
			
		} catch (NamingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (ConnectorException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
}
