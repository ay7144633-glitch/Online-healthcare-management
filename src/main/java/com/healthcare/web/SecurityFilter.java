package com.healthcare.web;

import java.io.IOException;
import java.util.UUID;

import com.healthcare.model.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Adds response protections, validates CSRF tokens, and requires sign-in for private routes. */
@WebFilter("/*")
public class SecurityFilter implements Filter {
    /** Applies common request security checks before handing control to a servlet. */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getServletPath();
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(true);
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }
        // Binding POST requests to this session prevents another site from forging an action as the signed-in user.
        if (request.getMethod().equalsIgnoreCase("POST")
                && !session.getAttribute("csrfToken").equals(request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Request validation failed.");
            return;
        }

        boolean publicPath = path.isEmpty()
                || path.equals("/")
                || path.equals("/login")
                || path.equals("/doctor/login")
                || path.equals("/admin/login")
                || path.equals("/register")
                || path.equals("/for-patients")
                || path.equals("/for-doctors")
                || path.equals("/for-clinics")
                || path.equals("/how-it-works")
                || path.startsWith("/assets/");
        User user = (User) session.getAttribute("user");
        if (!publicPath && user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        chain.doFilter(request, response);
    }
}