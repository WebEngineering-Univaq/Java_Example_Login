package it.univaq.f4i.iw.framework.security;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SecurityHelpers {

    //--------- SESSION SECURITY ------------    
    //questa funzione esegue una serie di controlli di sicurezza
    //sulla sessione corrente. Se la sessione non è valida, la cancella
    //e ritorna null, altrimenti la aggiorna e la restituisce
    //this method executed a set of standard chacks on the current session.
    //If the session exists and is valid, it is returned, otherwise
    //the session is invalidated and the method returns null
    public static HttpSession checkSession(HttpServletRequest r) {
        return checkSession(r, false);
    }

    public static HttpSession checkSession(HttpServletRequest r, boolean loginAgeCheck) {
        boolean check = true;

        HttpSession s = r.getSession(false);
        //per prima cosa vediamo se la sessione è attiva
        //first, let's see is the sessione is active
        if (s == null) {
            return null;
        }

        //data/ora correnti
        //current timestamp
        LocalDateTime now_ts = LocalDateTime.now();
        //inizio sessione
        //session start timestamp
        LocalDateTime start_ts = (LocalDateTime) s.getAttribute("session-start-ts");
        //ultima azione
        //last action timestamp
        LocalDateTime action_ts = (LocalDateTime) s.getAttribute("last-action-ts");
        if (action_ts == null) {
            action_ts = now_ts;
        }
        //ultima rigenerazione dell'ID
        //last session ID regeneration timestamp
        LocalDateTime refresh_ts = (LocalDateTime) s.getAttribute("session-refresh-ts");
        if (refresh_ts == null) {
            refresh_ts = start_ts;
        }
        //secondi trascorsi dall'inizio della sessione
        //seconds from the session start           
        long seconds_from_start = start_ts != null ? Duration.between(start_ts, now_ts).abs().getSeconds() : 0;
        //secondi trascorsi dall'ultima azione
        //seconds from the last valid action
        long seconds_from_action = Duration.between(action_ts, now_ts).abs().getSeconds();
        //secondi trascorsi dall'ultimo refresh della sessione
        //seconds from the last session refresh
        long seconds_from_refresh = Duration.between(refresh_ts, now_ts).abs().getSeconds();
        //
        if (s.getAttribute("userid") == null || start_ts == null) {
            //check sulla validità della sessione
            //second, check is the session contains valid data
            //nota: oltre a controllare se la sessione contiene un userid, 
            //dovremmo anche controllere che lo userid sia valido, probabilmente 
            //consultando il database utenti
            //note: besides checking if the session contains an userid, we should 
            //check if the userid is valid, possibly querying the user database
            check = false;
        } else if ((s.getAttribute("ip") == null) || !((String) s.getAttribute("ip")).equals(r.getRemoteHost())) {
            //check sull'ip del client
            //check if the client ip chaged
            check = false;
        } else if (seconds_from_start > 3 * 60 * 60) {
            //dopo tre ore la sessione scade
            //after three hours the session is invalidated
            check = false;
        } else if (seconds_from_action > 30 * 60) {
            //dopo trenta minuti dall'ultima operazione la sessione è invalidata
            //after 30 minutes since the last action the session is invalidated                    
            check = false;
        }
        //
        if (!check) {
            s.invalidate();
            return null;
        } else {
            //ogni 120 secondi, rigeneriamo la sessione per cambiarne l'ID
            //every 120 seconds, we regenerate the session to change its ID
            if (seconds_from_refresh >= 120) {
                s = regenerateSession(r);
                s.setAttribute("session-refresh-ts", now_ts);
            }
            //reimpostiamo la data/ora dell'ultima azione
            //if che checks are ok, update the last action timestamp
            s.setAttribute("last-action-ts", now_ts);
            return s;
        }
    }

    public static HttpSession createSession(HttpServletRequest request, String username, int userid) {
        //se una sessione è già attiva, rimuoviamola e creiamone una nuova
        //if a session already exists, remove it and recreate a new one
        disposeSession(request);
        HttpSession s = request.getSession(true);
        s.setAttribute("username", username);
        s.setAttribute("userid", userid);
        //
        s.setAttribute("ip", request.getRemoteHost());
        //
        s.setAttribute("session-start-ts", LocalDateTime.now());
        return s;
    }

    public static void disposeSession(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s != null) {
            s.invalidate();
        }
    }

    //questo metodo rigenera la sessione invalidando quella corrente e
    //creandone una nuova con gli stessi attributi. Può essere utile per 
    //prevenire il session hijacking, perchè modifica il session identifier
    //this method regenerates the session by invalidating the current one
    //and creating a new one with the same attributes. It may be useful
    //to prevent session hijacking, since it changes the session identifier
    public static HttpSession regenerateSession(HttpServletRequest request) {
        Map<String, Object> attributes = new HashMap<>();
        HttpSession s = request.getSession(false);
        if (s != null) {
            Enumeration<String> attributeNames = s.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String key = attributeNames.nextElement();
                Object value = s.getAttribute(key);
                attributes.put(key, value);
            }
            s.invalidate();
        }
        s = request.getSession(true);
        for (String key : attributes.keySet()) {
            Object value = attributes.get(key);
            s.setAttribute(key, value);
        }
        return s;
    }

    //--------- CONNECTION SECURITY ------------
    public static String checkHttps(HttpServletRequest request) {
        //possiamo usare questa tecnica per controllare se la richiesta è
        //stata effettuata in https e, in caso contrario, costruire la URL
        //necessaria a ridirezionare il browser verso l'https
        //we can use this technique to check if the request was made in https 
        //and, if not, build the URL needed to redirect the browser to https
        if (request.getScheme().equals("http")) {
            String url = "https://" + request.getServerName()
                    + ":" + request.getServerPort()
                    + request.getRequestURI() //request.getContextPath() + request.getServletPath() +  (request.getPathInfo() != null) ? request.getPathInfo() : ""
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            return url;
        } else {
            return null;
        }
    }

    //--------- DATA SECURITY ------------
    //questa funzione aggiunge un backslash davanti a
    //tutti i caratteri "pericolosi", usati per eseguire
    //SQL injection attraverso i parametri delle form
    //this function adds backslashes in front of
    //all the "malicious" charcaters, usually exploited
    //to perform SQL injection through form parameters
    public static String addSlashes(String s) {
        return s.replaceAll("(['\"\\\\])", "\\\\$1");
    }

    //questa funzione rimuove gli slash aggiunti da addSlashes
    //this function removes the slashes added by addSlashes
    public static String stripSlashes(String s) {
        return s.replaceAll("\\\\(['\"\\\\])", "$1");
    }

    public static int checkNumeric(String s) throws NumberFormatException {
        //convertiamo la stringa in numero, ma assicuriamoci prima che sia valida
        //convert the string to a number, ensuring its validity
        if (s != null) {
            //se la conversione fallisce, viene generata un'eccezione
            //if the conversion fails, an exception is raised
            return Integer.parseInt(s);
        } else {
            throw new NumberFormatException("String argument is null");
        }
    }

    public static String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

}
