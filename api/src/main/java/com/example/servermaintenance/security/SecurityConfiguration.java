package com.example.servermaintenance.security;

import com.example.servermaintenance.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoderService passwordEncoderService;

    class LoginPageFilter extends GenericFilterBean {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            if (SecurityContextHolder.getContext().getAuthentication() != null
                    && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                    && (((HttpServletRequest)request).getRequestURI().equals("/login")
                    || ((HttpServletRequest)request).getRequestURI().equals("/register"))) {
                System.out.println("user is authenticated but trying to access login or register page, redirecting to /");
                ((HttpServletResponse)response).sendRedirect("/");
            }
            chain.doFilter(request, response);
        }
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.addFilterBefore(new LoginPageFilter(), DefaultLoginPageGeneratingFilter.class);

        http.authorizeRequests()
                .expressionHandler(webExpressionHandler())
                .antMatchers("/register", "/register/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/api", "/api/**").hasRole("TEACHER")
                .antMatchers("/admin-tools", "/admin-tools/**").hasRole("ADMIN").and()
                .authorizeRequests()
                .accessDecisionManager(accessDecisionManager())
                .anyRequest().authenticated().and()
                .httpBasic().and()
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
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService).passwordEncoder(passwordEncoderService.passwordEncoder());
    }

    private SecurityExpressionHandler<FilterInvocation> webExpressionHandler() {
        var defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
        defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchy());
        return defaultWebSecurityExpressionHandler;
    }

    @Bean
    public static RoleHierarchy roleHierarchy() {
        var roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_TEACHER\nROLE_TEACHER > ROLE_STUDENT");
        return roleHierarchy;
    }

    @Bean
    public static RoleHierarchyVoter roleVoter() {
        return new RoleHierarchyVoter(roleHierarchy());
    }

    @Bean
    public AffirmativeBased accessDecisionManager() {
        List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
        decisionVoters.add(webExpressionVoter());
        decisionVoters.add(roleVoter());
        return new AffirmativeBased(decisionVoters);
    }

    private WebExpressionVoter webExpressionVoter() {
        WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
        webExpressionVoter.setExpressionHandler(expressionHandler());
        return webExpressionVoter;
    }

    @Bean
    public DefaultWebSecurityExpressionHandler expressionHandler() {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }
}
