package com.example.document_management.config;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;

@Configuration
public class SessionConfig {

    @Bean
    public HttpSessionListener sessionListener(SessionRegistry sessionRegistry) {
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {
                sessionRegistry.registerNewSession(se.getSession().getId(), null);
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
                sessionRegistry.removeSessionInformation(se.getSession().getId());
            }
        };
    }
}
