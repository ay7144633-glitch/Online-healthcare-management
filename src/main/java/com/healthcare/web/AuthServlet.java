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

@WebServlet(urlPatterns = {"/login", "/doctor/login", "/admin/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {
    private final UserRepository users = new UserRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        request.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp")
            .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        try {
            if (path.equals("/register")) {
                String name = value(request, "fullName");
                String email = value(request, "email");
                String password = value(request, "password");
                if (name.length() < 2 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || password.length() < 8) {
                        showError(request, response, true, false, false,
                            "Enter a name, valid email, and password with at least 8 characters.");
                    return;
                }
                users.registerPatient(name, email, password);
                signIn(request, users.authenticate(email, password));
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
                if (path.equals("/login") || path.equals("/doctor/login") || path.equals("/admin/login")) {
                User user = users.authenticate(value(request, "email"), value(request, "password"));
                boolean doctorPortal = path.equals("/doctor/login");
                boolean adminPortal = path.equals("/admin/login");
                if (user == null || (doctorPortal && !user.role().equals("DOCTOR"))
                    || (adminPortal && !user.role().equals("ADMIN"))) {
                    showError(request, response, false, doctorPortal, adminPortal,
                        doctorPortal ? "Doctor sign-in failed. Check your credentials and try again."
                            : adminPortal ? "Admin sign-in failed. Check your credentials and try again."
                                : "Email or password is incorrect, or this account is inactive.");
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

    private void signIn(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute("user", user);
    }

        private void showError(HttpServletRequest request, HttpServletResponse response, boolean register,
            boolean doctorMode, boolean adminMode, String message)
            throws ServletException, IOException {
        request.setAttribute("registerMode", register);
        request.setAttribute("doctorMode", doctorMode);
        request.setAttribute("adminMode", adminMode);
        request.setAttribute("error", message);
        String view = doctorMode ? "doctor-login" : adminMode ? "admin-login" : "auth";
        request.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp")
            .forward(request, response);
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}