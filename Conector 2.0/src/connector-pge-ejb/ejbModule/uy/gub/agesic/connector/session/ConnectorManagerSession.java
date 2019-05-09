package uy.gub.agesic.connector.session;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.entity.ConnectorPaths;
import uy.gub.agesic.connector.entity.FullConnector;
import uy.gub.agesic.connector.entity.GlobalConfiguration;
import uy.gub.agesic.connector.exceptions.ConnectorException;
import uy.gub.agesic.connector.session.api.ConnectorManager;
import uy.gub.agesic.connector.session.api.GlobalConfigurationManager;
import uy.gub.agesic.connector.util.ConnectorFileManager;
import uy.gub.agesic.connector.util.Constants;
import uy.gub.agesic.connector.util.Props;

@Stateless
public class ConnectorManagerSession implements ConnectorManager {
	
	private static Log log = LogFactory.getLog(ConnectorManagerSession.class);
	
	@PersistenceContext
	private EntityManager em;
	
	@EJB
	private GlobalConfigurationManager globalConfigService;
	
	public Long createConnector(Connector connector) throws ConnectorException {
		try{
			Pattern p = Pattern.compile("[^A-Za-z0-9_-]"); 
			if (p.matcher(connector.getName()).find()) {
				throw new ConnectorException("Tiene caracteres incorrectos en el nombre del conector.");
			}
			
			this.checkConnectorValidation(connector);
			
			em.persist(connector);
			log.debug("persist connector: "+ connector.getId());
			
			return connector.getId();
		}
		catch (Exception e){
			if (e instanceof ConnectorException) {
				throw (ConnectorException) e;
			}
			throw new ConnectorException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void editConnector(Connector connector) throws ConnectorException {
		try {
			Query queryPath = em.createNamedQuery("connector.bypath");
			queryPath.setParameter("path", connector.getPath());
			queryPath.setParameter("type", connector.getType());
			List<Connector> connectorsPaths = queryPath.getResultList();
			if (connectorsPaths != null && connectorsPaths.size() > 0) {
				Connector conPath = connectorsPaths.get(0);
				if (conPath.getId().longValue() != connector.getId().longValue()){
					log.error("Existe un connector con el mismo 'path' y 'tipo'");
					throw new ConnectorException("Existe un Conector con el mismo 'path' y 'tipo'");
				}
			}
		
			Query queryName = em.createNamedQuery("connector.byname");
			queryName.setParameter("name", connector.getName());
			queryName.setParameter("type", connector.getType());
			List<Connector> connectorsNames = queryName.getResultList();
			if (connectorsNames != null && connectorsNames.size() > 0) {
				Connector conName = connectorsNames.get(0);
				if (conName.getId().longValue() != connector.getId().longValue()){
					log.error("Existe un connector con el mismo 'nombre' y 'tipo'");
					throw new ConnectorException("Existe un Conector con el mismo 'nombre' y 'tipo'");
				}
			}
			
			em.merge(connector);
			log.debug("merge connector: "+ connector.getId());

		}
		catch (Exception e){
			log.error("Error on editConnector", e);
			throw new ConnectorException(e);
		}
	}

	public void deleteConnector(Long id) throws ConnectorException{
		Connector connector = this.getConnector(id);
		
		// Remove associated connector, in case it exists
		Connector linkedConnector = connector.getConnectorAssociated();
		if (linkedConnector != null) {
			linkedConnector.setConnectorAssociated(null);
		}
		connector.setConnectorAssociated(null);
		
		// en caso que el conector sea de tipo testeo, se deben quitar las asociaciones que los conectores de prod puedan tener
		if(connector.getType().equals(Connector.TYPE_TEST)) {
			removeConnectorLinks(connector);
		}
		
		em.remove(connector);
		
		//Delete Folder
		String path = ConnectorFileManager.getFilePath(connector, this);
		File folder = new File(path);
		deleteDirectory(folder);
		
		log.debug("remove connector: "+ id);
	}
	
	private boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         } else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }


	@SuppressWarnings("unchecked")
	public List<Connector> getConnectors(String type, String tag) throws ConnectorException {
		Query q = null;
		if (type!= null) {
			if (tag!= null && !tag.equals("")){
				q = em.createNamedQuery("connector.allbytypeAndtag");
				q.setParameter("type", type);
				q.setParameter("tag", "%" + tag + "%");
			}else{
				q = em.createNamedQuery("connector.allbytype");
				q.setParameter("type", type);
			}
		} else {
			q = em.createNamedQuery("connector.all");
		}
		
		return q.getResultList();
	}

	public Connector getConnector(Long id) {
		return em.find(Connector.class, id);
	}	

	public Connector getConnectorToExport(Long id) throws ConnectorException {
		Connector connector = em.find(Connector.class, id);
		
		ConnectorFileManager.loadFilesInfo(connector, this);
		
		ConnectorFileManager.loadFilesData(connector, this);
		return connector;
	}


	public FullConnector getConnectorByPath(String path, Boolean production) throws ConnectorException {
		Query q = em.createNamedQuery("connector.bypath");
		q.setParameter("path", path);
		String connectorType = null;
		
		if (production) {
			q.setParameter("type", Connector.TYPE_PROD);
			connectorType = Connector.TYPE_PROD;
		} else {
			q.setParameter("type", Connector.TYPE_TEST);
			connectorType = Connector.TYPE_TEST;
		}
		
		Connector originalConnector = null;
		Connector resultConnector = null;
		
		try {
			originalConnector = (Connector) q.getSingleResult();
		} catch (NoResultException e) {			
			log.error(e.getMessage(), e);
			throw new ConnectorException("El path " + path + " no se encuentra asociado a ning\u00FAn conector");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ConnectorException("Error al intentar obtener el Conector de la base de datos");
		}
		
		if (!originalConnector.isEnableLocalConf()) {
			// Genero una copia de Connector, ya que voy a tener que modificar algunos de sus campos.
			// O sea, simulo un em.detach(con) ya que en esta version de JPA no tengo el metodo
			resultConnector = new Connector(originalConnector);
			GlobalConfiguration globalConf = globalConfigService.getGlobalConfiguration(connectorType);
			
			if (globalConf == null) {
				throw new ConnectorException("Configuracion Global no cargada");
			}
			
			resultConnector.setAliasKeystore(globalConf.getAliasKeystore());
			resultConnector.setPasswordKeystoreOrg(globalConf.getPasswordKeystoreOrg());
			resultConnector.setPasswordKeystore(globalConf.getPasswordKeystore());
			resultConnector.setPasswordKeystoreSsl(globalConf.getPasswordKeystoreSsl());
		
		} else {
			resultConnector = originalConnector;
		}
		
		FullConnector fc = new FullConnector();
		fc.setConnector(resultConnector);
		fc.setConnectorPaths(this.getConnectorPaths(resultConnector));
		log.debug("getConnectorByPath: "+ resultConnector);
		return fc;
	}
	
	public FullConnector getConnectorByName(String name, boolean production) throws ConnectorException {
		Query q = em.createNamedQuery("connector.byname");
		q.setParameter("name", name);
		if (production) {
			q.setParameter("type", Connector.TYPE_PROD);
		} else {
			q.setParameter("type", Connector.TYPE_TEST);
		}
		Connector con = null;
		try {
			con = (Connector)q.getSingleResult();
		} catch (Exception e) {			
		}
		
		FullConnector fc = new FullConnector();
		fc.setConnector(con);
		fc.setConnectorPaths(this.getConnectorPaths(con));
		log.debug("getConnectorByName: "+ con);
		return fc;
	}
	
	private ConnectorPaths getConnectorPaths(Connector connector) throws ConnectorException{
		
		if (connector != null){
			ConnectorPaths cp = new ConnectorPaths();
			
			ConnectorFileManager.loadWsdlPath(this, cp, connector);
			
			if (connector.isEnableLocalConf()) {
				ConnectorFileManager.loadConnectorPaths(this, cp, connector);
			} else {
				ConnectorFileManager.loadConnectorPaths(globalConfigService, cp, connector.getType());
			}
			
			return cp;
		}
		return null;
		
	}

	public String getProperty(String prop, String filepath, String defaultValue) {
		return Props.getInstance().getProp(prop, filepath, defaultValue);
	}

	public String getBasicPath() {
		try {
			String basic_path = this.getProperty("BASIC_PATH", Constants.PATH_PROPERTIES_FILE, Constants.DEFAULT_BASICPATH); //"connector-pge/connector-pge.properties");
			File dirBasicPath = new File(basic_path);
			if (!dirBasicPath.exists()) {
				dirBasicPath.mkdir();
			}
			if (!basic_path.endsWith("/")) {
				basic_path = basic_path + "/";
			}
			return basic_path;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public boolean checkConnectorValidation(Connector connector) throws ConnectorException {
		Query queryPath = em.createNamedQuery("connector.bypath");
		queryPath.setParameter("path", connector.getPath());
		queryPath.setParameter("type", connector.getType());
		List<Connector> connectorsPaths = queryPath.getResultList();
		if (connectorsPaths != null && connectorsPaths.size() > 0) {
			log.error("Existe un connector con el mismo 'path' y 'tipo'");
			throw new ConnectorException("Existe un conector con el mismo 'path' y 'tipo'");
		}
	
		Query queryName = em.createNamedQuery("connector.byname");
		queryName.setParameter("name", connector.getName());
		queryName.setParameter("type", connector.getType());
		List<Connector> connectorsNames = queryName.getResultList();
		if (connectorsNames != null && connectorsNames.size() > 0) {
			log.error("Existe un connector con el mismo 'nombre' y 'tipo'");
			throw new ConnectorException("Existe un conector con el mismo 'nombre' y 'tipo'");
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	/** Quita la relacion entre los conectores de produccion asociados al conector que se pasa por parametro **/
	private void removeConnectorLinks(Connector connector) {
		Query queryName = em.createNamedQuery("connector.byAssociatedConnector");
		queryName.setParameter("id", connector.getId());
		queryName.setParameter("type", Connector.TYPE_PROD);
		
		List<Connector> prodConnectors = queryName.getResultList();
		for(Connector prodConnector : prodConnectors) {
			log.debug("removing connector association for " + prodConnector.getName());
			prodConnector.setConnectorAssociated(null);
		}
	}
	
}
