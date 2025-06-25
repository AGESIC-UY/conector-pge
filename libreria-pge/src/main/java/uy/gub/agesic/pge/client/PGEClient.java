package uy.gub.agesic.pge.client;


import uy.gub.agesic.pge.beans.STSResponse;
import uy.gub.agesic.pge.core.config.PGEConfiguration;
import uy.gub.agesic.pge.exceptions.ConfigurationException;
import uy.gub.agesic.pge.exceptions.RequestSecurityTokenException;

/**
 * Created by adriancur on 20/10/17.
 */
public interface PGEClient {

    STSResponse requestSecurityToken(final PGEConfiguration config) throws RequestSecurityTokenException, ConfigurationException;
}
