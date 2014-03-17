package biz.ideasoft.soa.esb.actions.wsa;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionProcessingFaultException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.Properties;

import biz.ideasoft.soa.esb.util.SoapUtil;
import biz.ideasoft.soa.wsa.service.api.MapperDao;

/**
 * This action maps the logical EPR located in a message property (wsaTo by default) to a physical EPR, the result
 * is located in another message property (physicalWsaTo by default).
 * The mapping is performed using the Mapper entity (stored in the mapper table), the message processing fails if 
 * there's no mapping for the given logical EPR, at least that the copyIfNotFound property is set to true, in that
 * case the value of the logical EPR is set in the physical EPR
 * Configuration Example:
 *<pre>{@code
 *
 *<action class="biz.ideasoft.soa.wsa.esb.actions.MapWsaEpr" name="MapWsaEpr">
 *    <!-- all properties are optional -->
 *    <property name="logicalDestination" value="wsaTo" />
 *    <property name="physicalDestination" value="physicalWsaTo" />
 *    <property name="mapperJNDI" value="MapperDaoImpl/local" />
 *    <property name="copyIfNotFound" value="false"/>
 *    <property name="validate" value="false"/>
 *</action>
 *
 * }</pre>
*/
public class MapWsaEpr extends AbstractActionLifecycle {

	protected ConfigTree _config;
	protected static Logger _logger = Logger.getLogger(MapWsaEpr.class);
	
	public static final String LOGICAL_DESTINATION_PROPERTY = "logicalDestination";
	public static final String PHYSICAL_DESTINATION_PROPERTY = "physicalDestination";
	public static final String MAPPER_SESSION_JNDI_PROPERTY_NAME = "mapperJNDI";
	public static final String COPY_IF_NOT_FOUND_PROPERTY_NAME = "copyIfNotFound";
	public static final String VALIDATE_PROPERTY_NAME = "validate";

	public static final String DEFAULT_LOGICAL_PROPERTY_NAME = "logicalDestination";
	public static final String DEFAULT_PHYSICAL_PROPERTY_NAME = "physicalDestination";
	public static final boolean DEFAULT_COPY_IF_NOT_FOUND = false;
	public static final boolean DEFAULT_VALIDATE = false;
	
	private String logicalPropertyName = DEFAULT_LOGICAL_PROPERTY_NAME;
	private String physicalPropertyName = DEFAULT_PHYSICAL_PROPERTY_NAME;
	private String mapperJNDIName;
	private boolean copyIfNotFound;
	private boolean validate;

	public MapWsaEpr(ConfigTree config) {
		_config = config;
		logicalPropertyName = config.getAttribute(LOGICAL_DESTINATION_PROPERTY, DEFAULT_LOGICAL_PROPERTY_NAME);
		physicalPropertyName = config.getAttribute(PHYSICAL_DESTINATION_PROPERTY, DEFAULT_PHYSICAL_PROPERTY_NAME);
		mapperJNDIName = config.getAttribute(MAPPER_SESSION_JNDI_PROPERTY_NAME);
		
		copyIfNotFound = Boolean.parseBoolean(config.getAttribute(COPY_IF_NOT_FOUND_PROPERTY_NAME, String.valueOf(DEFAULT_COPY_IF_NOT_FOUND)));
		validate = Boolean.parseBoolean(config.getAttribute(VALIDATE_PROPERTY_NAME, String.valueOf(DEFAULT_VALIDATE)));
	}

   public Message process(Message message) throws Exception {
	   Properties prop = message.getProperties();
	   
	   String wsaTo = (String) prop.getProperty(logicalPropertyName);	   
	   
	   try {
		   InitialContext iniCtx = new InitialContext();
		   MapperDao dao = (MapperDao) iniCtx.lookup(mapperJNDIName);
		   String physicalEPR = dao.getPhysicalEPR(wsaTo);
		   if (physicalEPR == null) {
			   if (copyIfNotFound) {
				   _logger.debug("Logical EPR not mapped: " + wsaTo + ", assing logical EPR to physical EPR");
				   physicalEPR = wsaTo;
			   } else if (validate) {
				   String cause = "Logical EPR not mapped: " + wsaTo;
				   Message faultMessage = SoapUtil.getFaultMessage(SoapUtil.CLIENT_ERROR, cause, null, SoapUtil.SOAP_NAME_TYPE);
				   throw new ActionProcessingFaultException(faultMessage, cause);
			   }
		   }
		   if (physicalEPR != null) {
			   prop.setProperty(physicalPropertyName, physicalEPR);
		   }
	   } catch (Exception e) {
		   _logger.error("Error: ", e);
		   SoapUtil.throwFaultException(e);
	   }
	   return message;
   }
}
