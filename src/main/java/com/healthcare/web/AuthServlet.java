package com.healthcare.web;

import java.io.IOException;
import java.sql.SQLException;

import com.healthcare.data.UserRepository;
import com.healthcare.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Serves sign-in and registration forms and manages authentication sessions. */
@WebServlet(urlPatterns = {
        "/login",
        "/doctor/login",
        "/admin/login",
        "/register",
        "/logout"
})
public class AuthServlet extends HttpServlet {
    private final UserRepository users = new UserRepository();

    /** Displays the requested authentication form or ends the current session on logout. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if (path.equals("/logout")) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.setAttribute("registerMode", path.equals("/register"));
        boolean doctorMode = path.equals("/doctor/login");
        boolean adminMode = path.equals("/admin/login");
        request.setAttribute("doctorMode", doctorMode);
        request.setAttribute("adminMode", adminMode);
        String view = doctorMode ? "doctor-login" : adminMode ? "admin-login" : "auth";
        forwardToView(request, response, view);
    }

    /** Registers a patient or authenticates a user, then routes them to their workspace. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        try {
            if (path.equals("/register")) {
                String name = value(request, "fullName");
                String email = value(request, "email");
                String password = value(request, "password");
                if (!isValidRegistration(name, email, password)) {
                    showError(request, response, true, false, false,
                            "Enter a name, valid email, and password with at least 8 characters.");
                    return;
                }
                users.registerPatient(name, email, password);
                signIn(request, users.authenticate(email, password));
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
            if (isLoginPath(path)) {
                User user = users.authenticate(value(request, "email"), value(request, "password"));
                boolean doctorPortal = path.equals("/doctor/login");
                boolean adminPortal = path.equals("/admin/login");
                if (!canUseLoginPortal(user, doctorPortal, adminPortal)) {
                    showError(request, response, false, doctorPortal, adminPortal,
                            loginErrorMessage(doctorPortal, adminPortal));
                    return;
                }
                signIn(request, user);
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } catch (SQLException exception) {
            getServletContext().log("Authentication request failed.", exception);
            showError(request, response, path.equals("/register"), path.equals("/doctor/login"),
                    path.equals("/admin/login"),
                    "Could not access the database. Check MySQL settings and try again.");
        }
    }

    /** Stores the authenticated user after rotating the session ID. */
    private void signIn(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        // Rotate the session ID after authentication to prevent session fixation.
        request.changeSessionId();
        session.setAttribute("user", user);
    }

    /** Re-displays the appropriate form with an authentication or registration error. */
    private void showError(HttpServletRequest request, HttpServletResponse response, boolean register,
            boolean doctorMode, boolean adminMode, String message)
            throws ServletException, IOException {
        request.setAttribute("registerMode", register);
        request.setAttribute("doctorMode", doctorMode);
        request.setAttribute("adminMode", adminMode);
        request.setAttribute("error", message);
        String view = loginView(doctorMode, adminMode);
        forwardToView(request, response, view);
    }

    /** Checks the required name, email, and password rules for patient registration. */
    private boolean isValidRegistration(String name, String email, String password) {
        return name.length() >= 2
                && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                && password.length() >= 8;
    }

    /** Identifies the three paths that accept sign-in submissions. */
    private boolean isLoginPath(String path) {
        return path.equals("/login") || path.equals("/doctor/login") || path.equals("/admin/login");
    }

    /** Confirms that the authenticated user's role matches the requested sign-in portal. */
    private boolean canUseLoginPortal(User user, boolean doctorPortal, boolean adminPortal) {
        return user != null
                && (!doctorPortal || user.role().equals("DOCTOR"))
                && (!adminPortal || user.role().equals("ADMIN"));
    }

    /** Selects a sign-in error message that matches the portal being used. */
    private String loginErrorMessage(boolean doctorPortal, boolean adminPortal) {
        if (doctorPortal) {
            return "Doctor sign-in failed. Check your credentials and try again.";
        }
        if (adminPortal) {
            return "Admin sign-in failed. Check your credentials and try again.";
        }
        return "Email or password is incorrect, or this account is inactive.";
    }

    /** Chooses the JSP view for the requested login portal. */
    private String loginView(boolean doctorMode, boolean adminMode) {
        if (doctorMode) {
            return "doctor-login";
        }
        if (adminMode) {
            return "admin-login";
        }
        return "auth";
    }

    /** Forwards the request to a JSP that is kept inside WEB-INF. */
    private void forwardToView(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(request, response);
    }

    /** Reads a trimmed request parameter, using an empty string when it is missing. */
    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}