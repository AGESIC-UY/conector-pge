package gub.agesic.connector.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import gub.agesic.connector.exceptions.ConnectorException;

/**
 * Created by adriancur on 06/12/17.
 */
@Configuration
@EnableWebSecurity
@PropertySource("file:${connector.web.configLocation}/connector-web.properties")
public class LoginSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String USERNAME = "conector";

    public static final String USERROLE = "USER";

    private final String userPassword;

    public LoginSecurityConfig(@Value("${userPassword}") final String userPassword) {
        this.userPassword = userPassword;
    }

    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder authenticationMgr)
            throws Exception {
        authenticationMgr.inMemoryAuthentication().withUser(USERNAME).password(userPassword)
                .roles(USERROLE);
    }

    @Override
    protected void configure(final HttpSecurity http) throws ConnectorException {
        try {
            http.authorizeRequests()
                    .antMatchers("/resources/**", "/head", "/loginPage", "/connectors/**/wsdl",
                            "/connectors/**/*.xsd")
                    .permitAll().anyRequest().authenticated().and().formLogin()
                    .loginPage("/loginPage").loginProcessingUrl("/appLogin")
                    .defaultSuccessUrl("/connectors", true).failureUrl("/loginPage?error")
                    .usernameParameter("username").passwordParameter("password").and().logout()
                    .logoutSuccessUrl("/loginPage?logout").and().csrf().disable();
        } catch (final Exception e) {
            throw new ConnectorException(e.getMessage());
        }

    }
}
