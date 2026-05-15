package com.tosin.xpay.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityBeanConfig {	
	
	@Autowired private UserDetailsService userDetailsService;
    
	@Bean
	public SessionRegistry sessionRegistry() {
	    return new SessionRegistryImpl();
	}	
	
    @Bean
    public ConcurrentSessionFilter concurrentSessionFilter() {
        return new ConcurrentSessionFilter(sessionRegistry());
    }	
	
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    } 
    
    @Bean
    public SecurityContextRepository securityContextRepository() {
      return new HttpSessionSecurityContextRepository();
    } 		
    
    @Bean
    public SecurityContextHolderStrategy securityContextHolderStrategy() {
      return SecurityContextHolder.getContextHolderStrategy();
    }    
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }  
    
    @Bean
    public SessionFixationProtectionStrategy sessionFixationProtectionStrategy() {
        return new SessionFixationProtectionStrategy();
    }    

    @Bean
    public RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(sessionRegistry());
    }    
    
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }
    
}
