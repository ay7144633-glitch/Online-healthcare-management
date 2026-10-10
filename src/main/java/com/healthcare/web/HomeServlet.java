package com.healthcare.web;

import java.io.IOException;
import java.sql.SQLException;

import com.healthcare.data.SettingRepository;
import com.healthcare.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Serves public-facing pages and supplies current clinic branding and contact settings. */
@WebServlet(urlPatterns = {"", "/for-patients", "/for-doctors", "/for-clinics", "/how-it-works"})
public class HomeServlet extends HttpServlet {
    private final SettingRepository settings = new SettingRepository();

    /** Selects the requested public page, loads clinic settings, and forwards to its JSP. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        String path = request.getServletPath();
        String page = switch (path) {
            case "/for-patients" -> "patients";
            case "/for-doctors" -> "doctors";
            case "/for-clinics" -> "clinics";
            case "/how-it-works" -> "how-it-works";
            default -> "home";
        };
        String title = switch (page) {
            case "patients" -> "For Patients";
            case "doctors" -> "For Doctors";
            case "clinics" -> "For Clinics";
            case "how-it-works" -> "How It Works";
            default -> "Carepoint | Healthcare, connected";
        };
        request.setAttribute("user", user);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageTitle", title);
        try {
            request.setAttribute("clinicSettings", settings.getAll());
        } catch (SQLException exception) {
            getServletContext().log("Could not load clinic contact settings.", exception);
            throw new ServletException("Could not load clinic contact settings.", exception);
        }
        String view = page.equals("home") ? "landing" : page;
        request.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(request, response);
    }
}