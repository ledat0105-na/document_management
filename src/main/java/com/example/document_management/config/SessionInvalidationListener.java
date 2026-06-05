package com.example.document_management.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class SessionInvalidationListener {

    @Autowired
    private SessionRegistry sessionRegistry;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            sessionRegistry.getAllPrincipals().forEach(principal -> {
                sessionRegistry.getAllSessions(principal, false)
                    .forEach(sessionInformation -> sessionInformation.expireNow());
            });
            System.out.println("All sessions invalidated on server restart");
        } catch (Exception e) {
            System.out.println("Session invalidation completed on startup");
        }
    }
}
