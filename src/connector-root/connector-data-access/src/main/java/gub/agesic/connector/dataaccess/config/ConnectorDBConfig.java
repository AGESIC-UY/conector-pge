package gub.agesic.connector.dataaccess.config;

import java.beans.PropertyVetoException;
import java.util.HashMap;

import javax.naming.NamingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@PropertySource({ "classpath:application.properties" })
@PropertySource("file:${connector.web.configLocation}/connector.properties")
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "entManagerFactory", transactionManagerRef = "transactionManager", basePackages = {
        "gub.agesic.connector.dataaccess.repository" })
public class ConnectorDBConfig {
    @Autowired
    private Environment environment;

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entManagerFactory() throws NamingException {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean
                .setPackagesToScan(new String[] { "gub.agesic.connector.dataaccess.entity" });

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        final HashMap<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto",
                environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        jpaProperties.put("hibernate.dialect",
                environment.getProperty("spring.jpa.properties.hibernate.dialect"));
        entityManagerFactoryBean.setJpaPropertyMap(jpaProperties);

        return entityManagerFactoryBean;
    }

    @Primary
    @Bean
    public ComboPooledDataSource dataSource() {
        final ComboPooledDataSource dataSource = new ComboPooledDataSource();
        try {
            dataSource
                    .setDriverClass(environment.getProperty("spring.datasource.driver-class-name"));
        } catch (final PropertyVetoException e) {
            e.printStackTrace();
        }
        dataSource.setJdbcUrl(environment.getProperty("spring.datasource.url"));
        dataSource.setUser(environment.getProperty("spring.datasource.username"));
        dataSource.setPassword(environment.getProperty("spring.datasource.password"));
        dataSource.setInitialPoolSize(Integer
                .parseInt(environment.getProperty("connector.datasource.initial-pool-size")));
        dataSource.setMinPoolSize(
                Integer.parseInt(environment.getProperty("connector.datasource.min-pool-size")));
        dataSource.setMaxPoolSize(
                Integer.parseInt(environment.getProperty("connector.datasource.max-pool-size")));
        dataSource.setMaxIdleTime(
                Integer.parseInt(environment.getProperty("connector.datasource.max-idle-time")));
        dataSource.setMaxStatements(
                Integer.parseInt(environment.getProperty("connector.datasource.max-statements")));
        return dataSource;
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager() throws NamingException {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entManagerFactory().getObject());
        return transactionManager;
    }

}
