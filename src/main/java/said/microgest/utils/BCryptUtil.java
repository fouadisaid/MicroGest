package said.microgest.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class BCryptUtil {

    private BCryptUtil() {
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}