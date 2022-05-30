package com.example.servermaintenance;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

@Service
public class AlertService {
    public void addAlertToResponse(HttpServletResponse response, String alertType, String message) {
        response.setHeader("HX-Trigger", String.format("{\"%s\":\"%s\"}", alertType, message));
    }
}
