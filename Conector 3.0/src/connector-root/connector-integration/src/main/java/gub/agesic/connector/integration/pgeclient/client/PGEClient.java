package gub.agesic.connector.integration.pgeclient.client;

import gub.agesic.connector.dataaccess.entity.Configuration;
import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.integration.pgeclient.beans.STSResponse;
import gub.agesic.connector.integration.pgeclient.exceptions.RequestSecurityTokenException;

/**
 * Created by adriancur on 20/10/17.
 */
public interface PGEClient {

    STSResponse requestSecurityToken(final Configuration configuration, final Connector connector,
            final String policyName) throws RequestSecurityTokenException;
}
