package gub.agesic.connector.dataaccess.repository;

import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Created by adriancur on 09/10/17.
 */
public interface GlobalConfigurationRepository extends JpaRepository<ConnectorGlobalConfiguration, Long> {

    // FIXME: Obtener configuracion global por ambiente
    // A tener en cuenta
    // En la tabla de configuracion se guarda la configuracion global para cada ambiente (type: Testing o Produccion)
    // Tambien se guarda la configuración local de cada servicio sin especificar el ambiente (type = null)
    // Si por algun motivo se guardara una configuracion local con el tipo de ambiente definido, se va a tratar como
    // si fuera una configuracion global, obteniendo resultados inesperados al ejecutar la consulta.
    @Query("select c from ConnectorGlobalConfiguration c where c.type =:type")
    Optional<ConnectorGlobalConfiguration> findGlobalConfiguration(@Param("type") String type);
}
