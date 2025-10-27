package com.rbs.util;

import java.util.regex.Pattern;

public final class Validators {
    private Validators() {}

    private static final Pattern EMAIL = Pattern.compile("^.+@.+\\..+$");
    private static final Pattern AADHAAR = Pattern.compile("^\\d{4} \\d{4} \\d{4}$");
    private static final Pattern PHONE = Pattern.compile("^\\+91 \\d{5} \\d{5}$");

    public static boolean isNameValid(String name) {
        return name != null && name.trim().length() >= 3 && name.trim().length() <= 16;
    }

    public static boolean isAdult(int age) {
        return age >= 18;
    }

    public static boolean isEmailValid(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    public static boolean isAadhaarValid(String aadhaar) {
        return aadhaar != null && AADHAAR.matcher(aadhaar).matches();
    }

    public static boolean isPhoneValid(String phone) {
        return phone != null && PHONE.matcher(phone).matches();
    }
}



