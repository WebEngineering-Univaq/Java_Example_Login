/*
 * HTMLResult.java
 * 
 * Questa classe permette di generare facilmente output a partire da codice
 * HTML. 
 * 
 * This class supports direct HTML output
 * 
 */
package it.univaq.f4i.iw.framework.result;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Giuseppe Della Penna
 */
public class HTMLResult {

    public enum HTML_Version {

        XHTML1_STRICT, XHTML1_TRANSITIONAL, HTML5
    };
    protected ServletContext context;
    private String title;
    private String head;
    private String body;
    private HTML_Version htmlVersion;
    private String encoding;

    public HTMLResult(ServletContext context) {
        this.context = context;
        init();
    }

    private void init() {
        setTitle("Untitled page");
        setHead("");
        setBody("");
        setHtmlVersion(HTML_Version.HTML5);
        setEncoding("UTF-8");
    }

    public void printPageHeader(PrintWriter out) {
        if (getHtmlVersion() != HTML_Version.HTML5) {
            out.println("<?xml version=\"1.0\" encoding=\"" + getEncoding() + "\"?>");
        }

        switch (getHtmlVersion()) {
            case XHTML1_STRICT:
                out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
                break;
            case XHTML1_TRANSITIONAL:
                out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
                break;
            default:
            case HTML5:
                out.println("<!doctype html>");
        }

        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>");
        if (getHtmlVersion() == HTML_Version.HTML5) {
            out.println("<meta charset=\"" + getEncoding() + "\"/>");
        }
        out.println("<title>" + title + "</title>" + head + "</head>");
    }

    public void printPageBody(PrintWriter out) {
        out.println("<body>" + body + "</body>");
    }

    public void printPageFooter(PrintWriter out) {
        out.println("</html>");
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the htmlVersion
     */
    public HTML_Version getHtmlVersion() {
        return htmlVersion;
    }

    /**
     * @param htmlversion the htmlVersion to set
     */
    public void setHtmlVersion(HTML_Version htmlversion) {
        this.htmlVersion = htmlversion;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param charset the encoding to set
     */
    public void setEncoding(String charset) {
        this.encoding = charset;
    }

    public void activate(String title, String body, HttpServletRequest request, HttpServletResponse response) throws IOException {
        setTitle(title);
        setBody(body);
        activate(request, response);
    }

    public void activate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //impostiamo il content type, se specificato dall'utente, o usiamo il default
        //set the content type, if specified by the user, or use the default
        String contentType = (String) request.getAttribute("contentType");
        if (contentType == null) {
            contentType = "text/html";
        }
        response.setContentType(contentType);

        //impostiamo l'encoding
        //set the character encoding
        response.setCharacterEncoding(getEncoding());

        PrintWriter out = response.getWriter();
        try {
            printPageHeader(out);
            printPageBody(out);
            printPageFooter(out);
        } finally {
            out.close();
        }
    }

    /**
     * @return the head
     */
    public String getHead() {
        return head;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param head the head to set
     */
    public void setHead(String head) {
        this.head = head;
    }

    public void appendToHead(String html) {
        this.head += html;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    public void appendToBody(String html) {
        this.body += html;
    }

    ///////////////////
    public static String sanitizeHTMLOutput(String s) {
        return s.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("'", "&#039;")
                .replaceAll("\"", "&#034;");
    }

}
