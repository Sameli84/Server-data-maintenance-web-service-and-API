package com.example.servermaintenance.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {
    @Override
    protected AccessDecisionManager accessDecisionManager() {
        var manager = (AffirmativeBased)super.accessDecisionManager();
        manager.getDecisionVoters().clear();
        manager.getDecisionVoters().add(SecurityConfiguration.roleVoter());
        return manager;
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        var handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(SecurityConfiguration.roleHierarchy());
        return handler;
//        return (MethodSecurityExpressionHandler) SecurityConfiguration.roleHierarchy();
    }
}
