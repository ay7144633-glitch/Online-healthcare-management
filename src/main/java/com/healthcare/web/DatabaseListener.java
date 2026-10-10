package com.healthcare.web;

import java.sql.SQLException;

import com.healthcare.data.Database;
import com.healthcare.data.UserRepository;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Initializes the database schema and optionally creates the first administrator at startup. */
@WebListener
public class DatabaseListener implements ServletContextListener {
    /** Creates missing tables and bootstraps an admin only when credentials are configured and absent. */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            Database.initialize();
            String adminEmail = System.getenv("BOOTSTRAP_ADMIN_EMAIL");
            String adminPassword = System.getenv("BOOTSTRAP_ADMIN_PASSWORD");
            boolean hasBootstrapCredentials = adminEmail != null && !adminEmail.isBlank()
                    && adminPassword != null && adminPassword.length() >= 8;
            if (hasBootstrapCredentials) {
                UserRepository users = new UserRepository();
                // Bootstrap only a missing account; restarting the app must never reset an existing admin password.
                if (!users.existsByEmail(adminEmail)) {
                    users.createUser("System Administrator", adminEmail, adminPassword, "ADMIN", null);
                }
            }
            event.getServletContext().log("Healthcare database initialized.");
        } catch (SQLException | IllegalStateException exception) {
            event.getServletContext().setAttribute(
                    "databaseError",
                    "Database is unavailable. Check MySQL and the DB_* settings.");
            event.getServletContext().log("Healthcare database initialization failed.", exception);
        }
    }
}