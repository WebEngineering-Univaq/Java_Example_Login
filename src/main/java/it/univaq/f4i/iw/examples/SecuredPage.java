package it.univaq.f4i.iw.examples;

import it.univaq.f4i.iw.framework.result.HTMLResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import it.univaq.f4i.iw.framework.utils.ServletHelpers;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Giuseppe Della Penna
 */
public class SecuredPage extends HttpServlet {

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HTMLResult result = new HTMLResult(getServletContext());
        result.setTitle("Secured page");
        result.setBody("<h1>Secured page</h1><p>Only logged-in users can access this content</p>");
        result.appendToBody("<p>VERY CONFIDENTIAL TEXT!!</p>");
        result.appendToBody("<p><a href=\"homepage\">Return to the Homepage</a></p>");
        result.activate(request, response);
    }

    private void action_loginrequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //avviso di sicurezza, con l'opzione di accesso tramite login
        //security alert with login option

        HTMLResult result = new HTMLResult(getServletContext());
        result.setTitle("Secured page");
        result.setBody("<h1>Secured page</h1><p>Only logged-in users can access this content</p>");
        //notare come passiamo alla servlet di login la nostra URL come referrer
        //note how we pass to the login servlet our URL as the referrer        
        result.appendToBody("<p>Please <a href=\"login?referrer=" + URLEncoder.encode(request.getRequestURI(), "UTF-8") + "\">Login</a></p>");
        result.appendToBody("<p><a href=\"homepage\">Return to the Homepage</a></p>");
        result.activate(request, response);
    }

    private void action_loginredirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //ridirezione diretta verso la pagina di login
        //direct redirect to the login page

        //notare come passiamo alla servlet di login la nostra URL come referrer
        //note how we pass to the login servlet our URL as the referrer        
        String completeRequestURL = request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        response.sendRedirect("login?referrer=" + URLEncoder.encode(completeRequestURL, "UTF-8"));
    }

    private void action_accessdenied(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //errore di accesso
        //access error

        ServletHelpers.handleError("Access denied", request, response, getServletContext());
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            HttpSession s = SecurityHelpers.checkSession(request);
            if (s != null) {
                action_default(request, response);
            } else {
                //se la pagina non è accessibile come utente anonimo...
                //if this page cannot be accessed as anonymous user...
                //
                //1) ridirigiamo a quella di login
                //1) redirect to the login page               
                action_loginredirect(request, response);
                //
                //2) oppure dichiariamo che è richiesta la login, ma lasciamo all'utente la scelta
                //2) or declare that a login is required and let the user choose
                //action_loginrequest(request, response);
                //
                //3) o generiamo un errore
                //3) or output an error message
                //action_accessdenied(request, response);
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
