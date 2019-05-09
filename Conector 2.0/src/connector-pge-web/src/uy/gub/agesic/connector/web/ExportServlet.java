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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.exceptions.ConnectorException;
import uy.gub.agesic.connector.session.api.ConnectorManager;
import uy.gub.agesic.connector.util.Constants;

public class ExportServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	private static Log log = LogFactory.getLog(ExportServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	String idConnector = request.getParameter("id");
    	
    	InitialContext ctx;
		Connector connector;
		try {
			ctx = new InitialContext();
			ConnectorManager connectorManager = (ConnectorManager) ctx.lookup(Constants.CONNECTOR_MANAGER_REMOTE); //"connector-pge-ear/ConnectorManagerSession/remote");
			
			connector = connectorManager.getConnectorToExport(new Long(idConnector));
			
			connector.setVersion(ConnectorBean.version); // se actualiza la version en el caso de que se exporte un conector de una version anterior
			
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
			log.error(e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		} catch (JAXBException e) {
			log.error(e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		} catch (NumberFormatException e) {
			log.error(e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		} catch (ConnectorException e) {
			log.error(e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		}
    }
}
