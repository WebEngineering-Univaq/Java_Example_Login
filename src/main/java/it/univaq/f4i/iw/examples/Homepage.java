/*
 * Homepage.java
 *
 * Questo esempio mostra come utilizzare le sessioni per autenticare un utente
 * 
 * This example shows how to use sessions to authenticate the user
 *
 */
package it.univaq.f4i.iw.examples;

import it.univaq.f4i.iw.framework.result.HTMLResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import it.univaq.f4i.iw.framework.utils.ServletHelpers;
import java.io.*;
import java.net.URLEncoder;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 *
 * @author Ingegneria del Web
 * @version
 */
public class Homepage extends HttpServlet {

    SimpleDateFormat f = new SimpleDateFormat();

    private void action_anonymous(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HTMLResult result = new HTMLResult(getServletContext());
        String completeRequestURL = request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        result.setTitle("Welcome");
        result.appendToBody("<h1>Authentication required</h1>");
        //notare come passiamo alla servlet di login la nostra URL come referrer
        //note how we pass to the login servlet our URL as the referrer
        result.appendToBody("<p>Please <a href=\"login?referrer=" + URLEncoder.encode(completeRequestURL, "UTF-8") + "\">Login</a></p>");
        result.appendToBody("<p>...or try to access</p><ul>");
        result.appendToBody("<li>the <a href='secured'>secured page</a></li>");
        result.appendToBody("<li>the <a href='public'>public page</a></li>");
        result.appendToBody("</ul>");

        result.activate(request, response);
    }

    private void action_logged(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //acquisiamo un riferimento alla sessione. Avendo già eseguito un checksession siamo sicuri che sia attiva e valida
        HttpSession s = request.getSession(false);
        HTMLResult result = new HTMLResult(getServletContext());
        result.setTitle("Welcome back");
        result.appendToBody("<h1>Welcome back, " + (String) s.getAttribute("username") + "</h1>");
        result.appendToBody("<p>You IP address is: " + (String) s.getAttribute("ip") + "</p>");
        result.appendToBody("<p>Your connection started on: " + ((LocalDateTime) s.getAttribute("session-start-ts")).format(DateTimeFormatter.ISO_DATE_TIME) + "</p>");
        //notare come passiamo alla servlet di logout la nostra URL come referrer
        //note how we pass to the logout servlet our URL as the referrer       
        result.appendToBody("<p><a href=\"logout?referrer=" + URLEncoder.encode(request.getRequestURI(), "UTF-8") + "\">Logout</a></p>");
        result.appendToBody("<p>Now you can access both</p><ul>");
        result.appendToBody("<li>the <a href='secured'>secured page</a></li>");
        result.appendToBody("<li>the <a href='public'>public page</a></li>");
        result.appendToBody("</ul>");

        result.activate(request, response);
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws jakarta.servlet.ServletException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            HttpSession s = SecurityHelpers.checkSession(request);
            if (s == null) {
                action_anonymous(request, response);
            } else {
                action_logged(request, response);
            }
        } catch (IOException ex) {
            ServletHelpers.handleError(ex, request, response, getServletContext());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
