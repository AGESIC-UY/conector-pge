package biz.ideasoft.soa.esb.saml;

import java.util.concurrent.ConcurrentHashMap;

public class SamlTokenCache {

	private static SamlTokenCache instance = new SamlTokenCache();
	private ConcurrentHashMap<String, CachedSamlToken> tokenCache;
	
	/** Este hashmap se utiliza para sincronizar el pedido de tokens para un mismo conector.
		El primer String es el nombre del conector, y el segundo string es el objeto que lo representa. Dicho objeto se usa para 
		sincronizar en RequestCachedSignedTokenAction
	**/  
	private ConcurrentHashMap<String, String> connectorNameCache;
	
	private SamlTokenCache() {
		tokenCache = new ConcurrentHashMap<String, CachedSamlToken>();
		connectorNameCache = new ConcurrentHashMap<String, String>();
	}
	
	public static SamlTokenCache getInstance() {
		return instance;
	}
	
	public CachedSamlToken getSamlToken(String connectorName) {
		return tokenCache.get(connectorName);
	}
	
	public void addSamlToken(String connectorName, CachedSamlToken token) {
		if (tokenCache.containsKey(connectorName)) {
			tokenCache.remove(connectorName);
		}
		tokenCache.put(connectorName, token);
	}

	public synchronized String getConnectorName(String name) {		
		if (!connectorNameCache.contains(name)) {
			connectorNameCache.put(name, name);
		}
		return connectorNameCache.get(name);
	}

}
