package gub.agesic.connector.dataaccess.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.RoleOperation;

/**
 * Created by adriancur on 09/10/17.
 */
public interface ConnectorRepository extends JpaRepository<Connector, Long> {

    @Query("select c from Connector c where c.path =:path and c.type =:type")
    Optional<Connector> getConnectorByPathAndType(@Param("path") String path,
            @Param("type") String type);

    @Query("select count(c)>0 from Connector c where c.name =:name and c.type =:type")
    Optional<Boolean> existsConnectorByNameAndType(@Param("name") String name,
            @Param("type") String type);

    @Query("select ro from Connector c inner join c.roleOperations ro where c =:connector and (ro.operationFromWSDL = :operation or ro.operationInputName = :operation)")
    Optional<RoleOperation> getRoleoperationsOperationFromWSDL(
            @Param("connector") Connector connector, @Param("operation") String operation);

    @Query("select c from Connector c where c.type =:type")
    List<Connector> getFilteredConnectorsByType(@Param("type") String type);

    @Query("select c from Connector c where c.type =:type and c.tag =:tag")
    List<Connector> getFilteredConnectorsByTypeAndTag(@Param("type") String type,
            @Param("tag") String tag);
}
