package it.univaq.f4i.iw.framework.security;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SecurityLayer {

    //--------- SESSION SECURITY ------------    
    //questa funzione esegue una serie di controlli di sicurezza
    //sulla sessione corrente. Se la sessione non è valida, la cancella
    //e ritorna null, altrimenti la aggiorna e la restituisce
    //this method executed a set of standard chacks on the current session.
    //If the session exists and is valid, it is returned, otherwise
    //the session is invalidated and the method returns null
    public static HttpSession checkSession(HttpServletRequest r) {
        boolean check = true;

        HttpSession s = r.getSession(false);
        //per prima cosa vediamo se la sessione è attiva
        //first, let's see is the sessione is active
        if (s == null) {
            return null;
        }

        //data/ora correnti
        //current timestamp
        Calendar now = Calendar.getInstance();
        //inizio sessione
        //session start timestamp
        Calendar begin = (Calendar) s.getAttribute("inizio-sessione");
        //ultima azione
        //last action timestamp
        Calendar last = (Calendar) s.getAttribute("ultima-azione");
        //ultima rigenerazione dell'ID
        //last session ID regeneration timestamp
        Calendar refresh = (Calendar) s.getAttribute("ultima-rigenerazione");

        //check sulla validità della sessione
        //second, check is the session contains valid data
        //nota: oltre a controllare se la sessione contiene un userid, 
        //dovremmo anche controllere che lo userid sia valido, probabilmente 
        //consultando il database utenti
        //note: besides checking if the session contains an userid, we should 
        //check if the userid is valid, possibly querying the user database
        if (s.getAttribute("userid") == null) {
            check = false;
            //check sull'ip del client
            //check if the client ip chaged
        } else if ((s.getAttribute("ip") == null) || !((String) s.getAttribute("ip")).equals(r.getRemoteHost())) {
            check = false;
            //check sulle date
            //check if the session is timed out
        } else {
            if (begin == null) {
                check = false;
            } else {
                //secondi trascorsi dall'inizio della sessione
                //seconds from the session start
                long secondsfrombegin = (now.getTimeInMillis() - begin.getTimeInMillis()) / 1000;
                //dopo tre ore la sessione scade
                //after three hours the session is invalidated
                if (secondsfrombegin > 3 * 60 * 60) {
                    check = false;
                } else if (last != null) {
                    //secondi trascorsi dall'ultima azione
                    //seconds from the last valid action
                    long secondsfromlast = (now.getTimeInMillis() - last.getTimeInMillis()) / 1000;
                    //dopo trenta minuti dall'ultima operazione la sessione è invalidata
                    //after 30 minutes since the last action the session is invalidated                    
                    if (secondsfromlast > 30 * 60) {
                        check = false;
                    }
                }
            }
        }
        if (!check) {
            s.invalidate();
            return null;
        } else {
            //ogni 120 secondi, rigeneriamo la sessione per cambiarne l'ID
            //every 120 seconds, we regenerate the session to change its ID
            if (refresh == null) {
                refresh = begin;
            }
            long secondsfromlastregen = (now.getTimeInMillis() - refresh.getTimeInMillis()) / 1000;
            if (secondsfromlastregen >= 120) {
                s = regenerateSession(r);
                s.setAttribute("ultima-rigenerazione", now);
            }
            
            //reimpostiamo la data/ora dell'ultima azione
            //if che checks are ok, update the last action timestamp
            s.setAttribute("ultima-azione", now);
            return s;
        }
    }

    public static HttpSession createSession(HttpServletRequest request, String username, int userid) {
        //se una sessione è già attiva, rimuoviamola e creiamone una nuova
        //if a session already exists, remove it and recreate a new one
        disposeSession(request);
        HttpSession s = request.getSession(true);
        s.setAttribute("username", username);
        s.setAttribute("ip", request.getRemoteHost());
        s.setAttribute("inizio-sessione", Calendar.getInstance());
        s.setAttribute("userid", userid);
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
    //prevenire il session hijacking, perchè modifica il session identidier
    //this method regenerates the session by invalidating the current one
    //and creating a new one with the same attributes. It may be useful
    //to prevent session hijacking, since it changes the session identifier
    public static HttpSession regenerateSession(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        Enumeration<String> attributeNames = s.getAttributeNames();
        Map<String, Object> attributes = new HashMap<>();
        while (attributeNames.hasMoreElements()) {
            String key = attributeNames.nextElement();
            Object value = s.getAttribute(key);
            attributes.put(key, value);
        }
        s.invalidate();
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
                    + request.getContextPath()
                    + request.getServletPath();
            if (request.getPathInfo() != null) {
                url += request.getPathInfo();
            }
            if (request.getQueryString() != null) {
                url += "?" + request.getQueryString();
            }

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
}
