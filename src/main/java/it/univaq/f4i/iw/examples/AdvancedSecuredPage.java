package it.univaq.f4i.iw.examples;

import it.univaq.f4i.iw.framework.result.HTMLResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import it.univaq.f4i.iw.framework.utils.ServletHelpers;
import java.io.IOException;
import java.net.URLEncoder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author Giuseppe Della Penna
 */
public class AdvancedSecuredPage extends HttpServlet {

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HTMLResult result = new HTMLResult(getServletContext());
        result.setTitle("Secured page");
        result.setBody("<h1>Secured page</h1><p>Only logged-in users can access this content</p>");
        result.appendToBody("<p>VERY CONFIDENTIAL TEXT!!</p>");
        result.appendToBody("<p><a href=\"homepage\">Return to the Homepage</a></p>");
        result.activate(request, response);
    }

    private void action_something(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //controllo permessi specifici per questa funzione        
        //function-specific permission checks
        if (SecurityHelpers.canAccess(request, "action_something")) {
            HTMLResult result = new HTMLResult(getServletContext());
            result.setTitle("Secured page");
            result.setBody("<h1>Secured page</h1><p>Only <em>admin</em> users can access this content</p>");
            result.appendToBody("<p>VERY VERY CONFIDENTIAL TEXT!!</p>");
            result.appendToBody("<p><a href=\"homepage\">Return to the Homepage</a></p>");
            result.activate(request, response);
        } else {
            ServletHelpers.handleError("Access denied", request, response, getServletContext());
        }
    }

    private void action_loginredirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //ridirezione diretta verso la pagina di login
        //direct redirect to the login page

        //notare come passiamo alla servlet di login la nostra URL come referrer
        //note how we pass to the login servlet our URL as the referrer        
        String completeRequestURL = request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        response.sendRedirect("login?referrer=" + URLEncoder.encode(completeRequestURL, "UTF-8"));
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
            if (s != null) {
                if (request.getParameter("function") != null) {
                    action_something(request, response);
                } else {
                    action_default(request, response);
                }
            } else {
                action_loginredirect(request, response);
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
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
