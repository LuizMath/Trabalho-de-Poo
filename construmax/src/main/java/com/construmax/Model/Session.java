package com.construmax.Model;

public class Session {
  private static User loggedUser;
  public static void setUser(User user) {
        loggedUser = user;
    }

    public static User getUser() {
        return loggedUser;
    }

    public static void clear() {
        loggedUser = null;
    }
}
