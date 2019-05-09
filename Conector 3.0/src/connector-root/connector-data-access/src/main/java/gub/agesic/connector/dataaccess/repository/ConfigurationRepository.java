package gub.agesic.connector.dataaccess.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import gub.agesic.connector.dataaccess.entity.Configuration;

/**
 * Created by adriancur on 09/10/17.
 */
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

}
