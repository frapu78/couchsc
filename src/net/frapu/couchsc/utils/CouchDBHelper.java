package net.frapu.couchsc.utils;

import java.util.Base64;

public class CouchDBHelper {

    /**
     * Returns a Basic Authentication String according to RF7617
     * @param user
     * @param password
     * @return
     */
    public static String getBasicAuthString(String user, String password) throws Exception {

        if (user.contains(":")) throw new Exception("Username must not contain ':' according to RF7617!");

        String plain = user+":"+password;

        return "Basic "+Base64.getEncoder().encodeToString(plain.getBytes());
    }

}
