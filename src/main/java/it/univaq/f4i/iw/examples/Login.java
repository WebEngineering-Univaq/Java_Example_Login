/*
 * Login.java
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

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Ingegneria del Web
 * @version
 */
public class Login extends HttpServlet {

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HTMLResult result = new HTMLResult(getServletContext());
        result.setTitle("Welcome");
        result.appendToBody("<h1>Please Login</h1>");
        //
        if (request.getAttribute("https-redirect") != null) {
            result.appendToBody("<p>WARNING! you are not using a secured (https) connection! "
                    + "Please follow <a href=\"" + request.getAttribute("https-redirect") + "\">this url</a> to login securely!</p>");
        }
        //
        result.appendToBody("<form method=\"post\" action=\"login\">");
        result.appendToBody("<p>Username: <input name=\"u\" type=\"text\"/></p>");
        result.appendToBody("<p>Password: <input name=\"p\" type=\"password\"/></p>");
        if (request.getParameter("referrer") != null) {
            result.appendToBody("<input name=\"referrer\" type=\"hidden\" value=\"" + request.getParameter("referrer") + "\"/></p>");
        }
        result.appendToBody("<p><input value=\"login\" name=\"login\" type=\"submit\"/></p>");
        result.appendToBody("</form>");
        result.activate(request, response);
    }

    private void action_login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("u");
        String password = request.getParameter("p");
        //... VALIDAZIONE IDENTITA'...
        //... IDENTITY CHECKS ...

        if (!username.isEmpty() && !password.isEmpty()) {
            //se la validazione ha successo
            //if the identity validation succeeds
            //carichiamo lo userid dal database utenti
            //load userid from user database
            int userid = 1;
            SecurityHelpers.createSession(request, username, userid);
            //se è stato trasmesso un URL di origine, torniamo a quell'indirizzo
            //if an origin URL has been transmitted, return to it
            if (request.getParameter("referrer") != null) {
                response.sendRedirect(request.getParameter("referrer"));
            } else {
                response.sendRedirect("homepage");
            }
        } else {
            ServletHelpers.handleError("Login failed", request, response, getServletContext());
        }
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
            if (request.getParameter("login") != null) {
                action_login(request, response);
            } else {
                //tecnica base per forzare la ridirezione su HTTPS
                //andrebbe posta ad esempio in un filtro per poterla usare
                //su ogni richiesta senza replicare codice
                //basic technique to force redirection on HTTPS 
                //It should be placed, e.g., in a filter to easily use it 
                //on every request without replicating the code

                String https_redirect_url = SecurityHelpers.checkHttps(request);
                request.setAttribute("https-redirect", https_redirect_url);
                //non eseguiamo la ridirezione, altrimenti sui vostri server di prova non funzionerebbe
                //we do not redirect, otherwise it would not work on your test servers                
                //if (https_redirect_url != null) {
                //    response.sendRedirect(https_redirect_url);
                //} else {
                action_default(request, response);
                //}
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
