package com.example.servermaintenance.security;

import com.example.servermaintenance.KeyCloakAuthSuccessHandler;
import com.example.servermaintenance.account.AccountService;
import lombok.AllArgsConstructor;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfiguration {
    private final AccountService accountService;
    private final PasswordEncoderService passwordEncoderService;

    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**")
                    .authorizeRequests().anyRequest().hasRole("TEACHER")
                    .expressionHandler(webExpressionHandler())
                    .accessDecisionManager(accessDecisionManager())
                    .and()
                    .httpBasic().authenticationEntryPoint(authenticationEntryPoint());
        }

        @Bean
        public AuthenticationEntryPoint authenticationEntryPoint() {
            var entryPoint = new BasicAuthenticationEntryPoint();
            entryPoint.setRealmName("api realm");
            return entryPoint;
        }
    }
    @Configuration
    @Order(2)
    public static class FormWebSecurityConfigurationAdapter extends KeycloakWebSecurityConfigurerAdapter {

        @Autowired
        public void configureGlobal(
                AuthenticationManagerBuilder auth) throws Exception {

            KeycloakAuthenticationProvider keycloakAuthenticationProvider
                    = keycloakAuthenticationProvider();
            keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(
                    new SimpleAuthorityMapper());
            auth.authenticationProvider(keycloakAuthenticationProvider);
        }
        @Bean
        @Override
        protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
            return new RegisterSessionAuthenticationStrategy(
                    new SessionRegistryImpl());
        }

        @Bean
        @Override
        protected KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter() throws Exception {
            KeycloakAuthenticationProcessingFilter filter = new KeycloakAuthenticationProcessingFilter(authenticationManagerBean());
            filter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy());
            filter.setAuthenticationSuccessHandler(successHandler());
            return filter;
        }


        @NotNull
        @Bean
        public KeyCloakAuthSuccessHandler successHandler() {
            return new KeyCloakAuthSuccessHandler(new SavedRequestAwareAuthenticationSuccessHandler());
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests()
                    .antMatchers("/*")
                    .hasRole("STUDENT")
                    .anyRequest()
                    .permitAll();
        }
  /*      @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.authorizeRequests()
                    .expressionHandler(webExpressionHandler())
                    .antMatchers("/register", "/register/**", "/webjars/**").permitAll()
                    .antMatchers("/admin-tools", "/admin-tools/**").hasRole("ADMIN").and()
                    .authorizeRequests()
                    .accessDecisionManager(accessDecisionManager())
                    .anyRequest().authenticated().and()
                    .formLogin()
                    .usernameParameter("email")
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .permitAll()
                    .and()
                    .logout()
                    .logoutUrl("/logout")
                    .invalidateHttpSession(true)
                    .permitAll();
        }*/
    }

    private static SecurityExpressionHandler<FilterInvocation> webExpressionHandler() {
        var defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
        defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchy());
        return defaultWebSecurityExpressionHandler;
    }

    @Bean
    public static RoleHierarchy roleHierarchy() {
        var roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ADMIN > TEACHER\nTEACHER > STUDENT");
        return roleHierarchy;
    }

    @Bean
    public static RoleHierarchyVoter roleVoter() {
        return new RoleHierarchyVoter(roleHierarchy());
    }

    @Bean
    public static AffirmativeBased accessDecisionManager() {
        List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
        decisionVoters.add(webExpressionVoter());
        decisionVoters.add(roleVoter());
        return new AffirmativeBased(decisionVoters);
    }

    private static WebExpressionVoter webExpressionVoter() {
        WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
        webExpressionVoter.setExpressionHandler(expressionHandler());
        return webExpressionVoter;
    }

    @Bean
    public static DefaultWebSecurityExpressionHandler expressionHandler() {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }
}
