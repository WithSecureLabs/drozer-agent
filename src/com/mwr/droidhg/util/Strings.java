package com.mwr.droidhg.util;

public class Strings {

  static {
    System.loadLibrary("mstring");
  }
    
  public static native String get(String path);

}
