package gub.agesic.connector.dataaccess.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;

/**
 * Created by adriancur on 09/10/17.
 */
public interface GlobalConfigurationRepository
        extends JpaRepository<ConnectorGlobalConfiguration, Long> {

    /* Mejorar esto para buscar LA configuracion global */
    @Query("select c from ConnectorGlobalConfiguration c")
    Optional<ConnectorGlobalConfiguration> findFirst();

    @Query("select c from ConnectorGlobalConfiguration c where c.type =:type")
    Optional<ConnectorGlobalConfiguration> findGlobalConfiguration(@Param("type") String type);
}
