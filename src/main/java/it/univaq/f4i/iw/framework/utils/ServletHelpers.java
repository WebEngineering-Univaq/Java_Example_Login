package it.univaq.f4i.iw.framework.utils;

import it.univaq.f4i.iw.framework.result.HTMLResult;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Didattica
 */
public class ServletHelpers {

    public static void handleError(String message, HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        //Scriviamo il messaggio di errore nel log del server
        //Log the error message in the server log
        System.err.println(message);
        // ATTENZIONE: in un ambiente di produzione, i messaggi di errore DEVONO essere limitati a informazioni generiche, non a stringhe di complete di eccezione
        //e.g., potremmo mappare solo la classe dell'eccezione (IOException, SQLException, ecc.) in messaggi come "Errore IO", "Errore database", ecc.
        //WARNING: in a production environment, error messages MUST be limited to generic information, not full exception strings
        //e.g., we may map the exception class only (IOException, SQLException, etc.) to messages like "IO Error", "Database Error", etc.
        HTMLResult result = new HTMLResult(context);
        result.setTitle("ERROR");
        result.setBody("<p>" + message + "</p>");
        try {
            result.activate(request, response);
        } catch (IOException ex) {
            //if error page cannot be sent, try a standard HTTP error message
            //se non possiamo inviare la pagina di errore, proviamo un messaggio di errore HTTP standard
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (IOException ex1) {
                //if ALSO this error status cannot be notified, write to the server log
                //se ANCHE questo stato di errore non può essere notificato, scriviamo sul log del server
                Logger.getLogger(ServletHelpers.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    public static void handleError(Exception exception, HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        String message = (exception != null) ? exception.getClass().getSimpleName() : "Unknown exception";
        if (exception != null && exception.getMessage() != null && !exception.getMessage().isEmpty()) {
            message += ": " + exception.getMessage();
        }
        handleError(message, request, response, context);
    }

    public static void handleError(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        //assumiamo che l'eccezione sia passata tramite gli attributi della request
        //ma per sicurezza controlliamo comunque il tipo effettivo dell'oggetto
        //we assume that the exception has been passed using the request attributes        
        //but we always check the real object type
        if (request.getAttribute("exception") instanceof Exception) {
            handleError((Exception) request.getAttribute("exception"),
                    request, response, context);
        } else {
            handleError("Unknown error", request, response, context);
        }

    }
}
