package com.rbs.service;

import com.rbs.model.User;

public final class Session {
    private Session() {}
    private static volatile User currentUser;
    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User u) { currentUser = u; }
}
