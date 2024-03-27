package com.WithSecure.jsolar.util;

public class Strings {

    // I Have literally no idea wtf this class does
    // i gotchu fam - yaykenyay
    static {
        System.loadLibrary("mstring");
    }

    public static native String get(String path);
}
