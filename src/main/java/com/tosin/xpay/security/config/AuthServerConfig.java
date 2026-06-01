package com.tosin.xpay.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;

@Configuration
@EnableWebSecurity
public class AuthServerConfig {
	
@Autowired private SessionRegistry sessionRegistry;
	
	@Autowired private CustomAccessDeniedHandler customAccessDeniedHandler;	
	@Autowired private CustomSessionExpiredStrategy customSessionExpiredStrategy;
	@Autowired private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	@Autowired private RateLimitingFilter rateLimitingFilter;
	
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
  	
    	//CSRF & CORS 
    	//==========================================================================================================================================================================================
    	   	
    	// SKIP CSRF IF AUTHORIZATION HEADER CONTAINS BEARER TOKEN- SUPPORT STATELESS API CLIENTS (JWT) AND STATEFUL API CLIENTS (SESSION)
    	//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------   	    
        
        //CSRF & CORS
        httpSecurity.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults());           

		httpSecurity.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        
        //REQUEST AUTHORIZATION RULES
        //==========================================================================================================================================================================================
        
        httpSecurity.authorizeHttpRequests((request) -> {
        	
        			//NOTE: has MEANS SINGLE WHILE hasAny MEANS MULTIPLE 
			
		           	request.requestMatchers("/error").permitAll();  
		           	
		           	request.requestMatchers("/staff/create/**").permitAll();
		           	request.requestMatchers("/customer/create/**").permitAll();
		           	request.requestMatchers("/auth/login/**").permitAll();
		           	request.requestMatchers("/auth/logout/**").permitAll();
					request.requestMatchers("/auth/password-reset/**").permitAll();
		           	request.requestMatchers("/customer/find/**").permitAll();
		           		           
		           		           			           	
		           
		           	request.anyRequest().fullyAuthenticated();
                                                         
            }
        );
        
        //STORING THE SESSION
        //=========================================================================================================================================================================================     

        //SESSION MANAGEMENT
        httpSecurity.sessionManagement((session) -> {
        	
	            	session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
	                session.sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::newSession);
	                
	                session.maximumSessions(-1).maxSessionsPreventsLogin(false).sessionRegistry(sessionRegistry).expiredSessionStrategy(customSessionExpiredStrategy);
	                
            }
        );

	    //SPRING SECURITY SETS THE X-FRAME-OPTIONS RESPONSE HEADER TO DENY BY DEFAULT.
	    //THIS TELLS THE BROWSER THAT THE PAGE CANNOT BE DISPLAYED IN A FRAME, REGARDLESS OF THE SITE ATTEMPTING TO DO SO.
	    //IF YOU CHOOSE TO DISABLE THE X-FRAME-OPTIONS HEADER (NOT RECOMMENDED) BY SETTING THEN SPRING SECURITY WILL NOT ADD THE X-FRAME-OPTIONS HEADER TO THE RESPONSE.
	    //THIS MEANS YOUR APPLICATION COULD BE RENDERED IN A FRAME, AND ALSO COULD BE VULNERABLE TO CLICKJACKING ATTACKS.
        //httpSecurity.headers(httpSecuritySecurityHeadersConfigurer -> httpSecuritySecurityHeadersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        
        //CLEAR COOKIE WHEN LOGOUT
        //=========================================================================================================================================================================================
        
        httpSecurity.logout((logout) -> {
        	
        			 //THIS ENABLES THE DEFAULT LOGOUT MECHANISM WITH A SPECIFED URL
        			 //WE DO NOT NEED THIS BECAUSE OUR LOGIN AND LOGOUT URL IS A REST CONTROLLER
                     //logout.logoutUrl("/user-information/user-logout/v1.0");
        	
        			 //AFTER A SUCCESSFUL LOGOUT, SPRING SECURITY WILL REDIRECT THE USER TO A SPECIFIED PAGE.
        			 //WE DO NOT NEED THIS BECAUSE OUR LOGIN AND LOGOUT URL IS A REST CONTROLLER
                     //logout.logoutSuccessUrl("/user-information/user-login/v1.0");
                     
                     logout.addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.COOKIES)));
                     logout.logoutSuccessHandler((httpSecurityServletRequest, httpSecurityServletResponse, authentication) -> SecurityContextHolder.clearContext());
                     logout.clearAuthentication(true);
                     logout.invalidateHttpSession(true);
                     logout.deleteCookies("JSESSIONID");
                     logout.permitAll();
                     
            }
        );
        
        //FORM LOGIN
        //========================================================================================================================================================================================
        
        httpSecurity.formLogin((formLogin) -> {
        	
        			//SINCE WE ARE NOT USING THE FORM LOGIN MECHANISM, WE JUST COMMENT THEM OUT
        			
        			//IF WE CHOOSE TO USE FORM LOGIN IN THE FUTURE, WE NEED TO IMPLEMENT
        	
        			//1. authenticationFailureHandler() ------> IMPLEMENT THIS IF REQUIRED IN FUTURE
        			//2. authenticationSuccessHandler() ------> IMPLEMENT THIS IF REQUIRED IN FUTURE
        	
		        	//formLogin.loginPage("/login.html");
		        	//formLogin.failureHandler(authenticationFailureHandler());
		        	//formLogin.successHandler(authenticationSuccessHandler());
            
		   }
		);        
        
        //CUSTOM EXCEPTION
        //=======================================================================================================================================================================================
        
        httpSecurity.exceptionHandling((exception) -> {  
        	
        			//CUSTOM EXCEPTION HANDLING FOR ACCESS DENIED AND AUTHENTICATION ENTRYPOINT                   
					exception.accessDeniedHandler(customAccessDeniedHandler);  
					exception.authenticationEntryPoint(customAuthenticationEntryPoint);
                     
            }
        );        

        return httpSecurity.build();
        
    }       
    
}


