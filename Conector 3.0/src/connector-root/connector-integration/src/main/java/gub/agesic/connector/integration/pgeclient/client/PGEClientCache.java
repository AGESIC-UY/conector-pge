package gub.agesic.connector.integration.pgeclient.client;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gub.agesic.connector.dataaccess.entity.Configuration;
import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.integration.pgeclient.beans.SAMLAssertion;
import gub.agesic.connector.integration.pgeclient.beans.STSResponse;
import gub.agesic.connector.integration.pgeclient.exceptions.RequestSecurityTokenException;

/**
 * Created by adriancur on 20/10/17.
 */
@Service
public class PGEClientCache implements PGEClient {

    private final static Logger LOGGGER = Logger.getLogger(PGEClientCache.class);

    private static final int ERROR_MARGIN = -1;

    @Autowired
    protected PGEClient pgeClientBasic;

    private final Map<String, SAMLAssertion> cache;

    public PGEClientCache(final PGEClient pgeClientBasic) {
        this.pgeClientBasic = pgeClientBasic;
        this.cache = new ConcurrentHashMap<String, SAMLAssertion>();
    }

    @Override
    public STSResponse requestSecurityToken(final Configuration configuration,
            final Connector connector, final String policyName)
            throws RequestSecurityTokenException {
        if (connector.isEnableCacheTokens()) {
            LOGGGER.debug("cache is enabled");
            return requestSecurityTokenCacheable(configuration, connector, policyName);
        } else {
            LOGGGER.debug("cache is disabled");
            return pgeClientBasic.requestSecurityToken(configuration, connector, policyName);

        }
    }

    private STSResponse requestSecurityTokenCacheable(final Configuration configuration,
            final Connector connector, final String policyName)
            throws RequestSecurityTokenException {
        final String serviceName = connector.getWsaTo();
        final SAMLAssertion token = this.cache.get(serviceName);
        STSResponse response = new STSResponse(0, token);
        if (unknownTokenOrHasExpired(token)) {
            LOGGGER.debug("token not found for service " + serviceName);
            response = pgeClientBasic.requestSecurityToken(configuration, connector, policyName);
            cache.put(serviceName, response.getAssertion());
            LOGGGER.debug("token added to cache for service " + serviceName);
        } else {
            LOGGGER.debug("token from cache is valid to consume service " + serviceName);
        }
        return response;
    }

    private boolean unknownTokenOrHasExpired(final SAMLAssertion token) {
        if (token == null) {
            return true;
        } else {
            /*
             * el token tiene una vida util que esta definida en
             * getNotOnOrAfter. Si comparamos tokenLifeTime <= today(), el
             * problema que podemos tener es que el token este valido por unos
             * pocos milisegundos, p.ej: 100. En estos casos, el pedido al
             * servicio (p.ej: DNIC) puede llevar mas de este tiempo, por lo que
             * puede ser que tomes un token del cache que al llegar a la pdi
             * este vencido. Por esto mismo, se resta 1 a la fecha de hoy para
             * tener un margen de tiempo para contemplar estos casos.
             *
             */
            final Date tokenLifeTime = token.getAssertion().getConditions().getNotOnOrAfter()
                    .toDate();
            final Date maximumDateTimeTokenUsage = DateUtils.addMinutes(new Date(), ERROR_MARGIN);
            return maximumDateTimeTokenUsage.compareTo(tokenLifeTime) >= 0;
        }
    }
}
