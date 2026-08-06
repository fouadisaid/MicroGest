package said.microgest.utils;

import said.microgest.entities.User;

public class SessionContext {

    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }

    public static String getCurrentUsername() {
        return currentUser != null
                ? currentUser.getUsername()
                : System.getProperty("user.name", "system");
    }
}