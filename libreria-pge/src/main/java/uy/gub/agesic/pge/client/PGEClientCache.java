package uy.gub.agesic.pge.client;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uy.gub.agesic.pge.beans.SAMLAssertion;
import uy.gub.agesic.pge.beans.STSResponse;
import uy.gub.agesic.pge.cache.ICache;
import uy.gub.agesic.pge.cache.InMemoryCacheWithDelayQueue;
import uy.gub.agesic.pge.core.config.ConfigProperties;
import uy.gub.agesic.pge.core.config.PGEConfiguration;
import uy.gub.agesic.pge.enums.SamlVersion;
import uy.gub.agesic.pge.exceptions.ConfigurationException;
import uy.gub.agesic.pge.exceptions.RequestSecurityTokenException;

import java.util.Date;

/**
 * Created by adriancur on 20/10/17.
 */
@Service(value = "pgecache")
public class PGEClientCache implements PGEClient {

    private final static Logger LOGGGER = Logger.getLogger(PGEClientCache.class);

    private static final int ERROR_MARGIN = 1;
    @Autowired
    protected PGEClient pgeClientBasic;
    @Autowired
    private ICache cache;

    public PGEClientCache(final PGEClient pgeClientBasic) {
        this.pgeClientBasic = pgeClientBasic;
        this.cache = new InMemoryCacheWithDelayQueue();
    }

    @Override
    public STSResponse requestSecurityToken(final PGEConfiguration config) throws RequestSecurityTokenException, ConfigurationException {
        final String cacheTokenEnabled = config.getSTSPropValue(ConfigProperties.CACHE_TOKEN_ENABLED);
        if (cacheTokenEnabled.equals("1")) {
            LOGGGER.debug("cache is enabled");
            return requestSecurityTokenCacheable(config);
        } else {
            LOGGGER.debug("cache is disabled");
            return pgeClientBasic.requestSecurityToken(config);
        }
    }

    private STSResponse requestSecurityTokenCacheable(final PGEConfiguration config) throws RequestSecurityTokenException, ConfigurationException {
        final String serviceName = config.getSTSPropValue(ConfigProperties.WSA_TO);
        final String samlVersion = config.getSTSPropValue(ConfigProperties.SAML_VERSION);
        final SAMLAssertion token = (SAMLAssertion) this.cache.get(serviceName);
        STSResponse response = new STSResponse(0, token);

        if (token == null) {
            LOGGGER.debug("token not found for service " + serviceName);
            response = pgeClientBasic.requestSecurityToken(config);
            final SAMLAssertion newToken = response.getAssertion();
            cache.add(serviceName, newToken, expiryTimeInMillis(newToken, samlVersion));
            LOGGGER.debug("token added to cache for service " + serviceName);
        } else {
            LOGGGER.debug("token from cache is valid to consume service " + serviceName);
        }
        return response;
    }

    private long expiryTimeInMillis(final SAMLAssertion token, final String SAMLVersion) {
        /*
         * el token tiene una vida util que esta definida en
         * getNotOnOrAfter. Si comparamos tokenLifeTime <= today(), el
         * problema que podemos tener es que el token este valido por unos
         * pocos milisegundos, p.ej: 100. En estos casos, el pedido al
         * servicio (p.ej: DNIC) puede llevar mas de este tiempo, por lo que
         * puede ser que tomes un token del cache que al llegar a la pdi
         * este vencido. Por esto mismo, se suma 1 a la fecha de expiracion para
         * tener un margen de tiempo para contemplar estos casos.
         *
         */
        Date tokenLifeTime = token.getAssertionSaml1().getConditions().getNotOnOrAfter().toDate();
        if (SAMLVersion.equals(SamlVersion.V2_0.getName())) {
            tokenLifeTime = token.getAssertionSaml2().getConditions().getNotOnOrAfter().toDate();
        }

        return DateUtils.addMinutes(tokenLifeTime, ERROR_MARGIN).getTime();
    }
}

