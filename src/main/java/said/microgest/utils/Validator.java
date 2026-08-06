package said.microgest.utils;

import java.util.regex.Pattern;

public final class Validator {

    private Validator() {
    }

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isEmail(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    public static boolean isPhone(String phone) {
        return phone != null && phone.matches("^[0-9]{9,15}$");
    }

}