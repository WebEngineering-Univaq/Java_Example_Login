package it.univaq.f4i.iw.examples;

import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giuseppe Della Penna
 */
public class UserData {

    private int userID;
    private String userName;
    private String hashedPassword;
    private Set<String> roles;

    public UserData() {
        this.userID = 0;
        this.userName = "";
        this.hashedPassword = "";
        this.roles = new HashSet<>();
    }

    /**
     * @return the userID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * @param userID the userID to set
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the hashedPassword
     */
    public String getHashedPassword() {
        return hashedPassword;
    }

    /**
     * @param hashedPassword the hashedPassword to set
     */
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    //creiamo un utente fasullo solo per scopi di test
    //create a fake user for testing purposes
    static UserData forUsername(String username) {
        UserData u = new UserData();
        u.setUserID(1);
        u.setUserName(username);

        if ("a".equalsIgnoreCase(username)) {
            u.addRole("admin");
            u.addRole("user");
        } else {
            u.addRole("user");
        }

        try {
            //solo per test: la password hashed deve essere letta dal database degli utenti!            
            //test-only: hashed password must be read from the user database!
            u.setHashedPassword(SecurityHelpers.getPasswordHashPBKDF2("p"));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(UserData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return u;
    }
}
