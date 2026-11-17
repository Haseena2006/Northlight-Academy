package com.northlight.academy.util;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HexFormat;
public final class Hashing {
  private static final SecureRandom RNG = new SecureRandom();
  private Hashing(){}
  public static String sha256(String s){
    try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8))); }
    catch (NoSuchAlgorithmException e){ throw new IllegalStateException(e); }
  }
  public static String randomToken(){ byte[] b = new byte[32]; RNG.nextBytes(b); return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b); }
  public static String certificateCode(){
    String a = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; StringBuilder sb = new StringBuilder("NLA-");
    for (int i=0;i<12;i++){ if(i>0 && i%4==0) sb.append('-'); sb.append(a.charAt(RNG.nextInt(a.length()))); }
    return sb.toString();
  }
}
