package com.google.code.infusion.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class HmacSha1Base64 {
  public static String sign(String text, String key) {
    try {
      Mac mac = Mac.getInstance("HmacSHA1");
      SecretKeySpec secret = new SecretKeySpec(key.getBytes(),"HmacSHA1");
      mac.init(secret);
      byte[] digest = mac.doFinal(text.getBytes());
      return new String(Base64.encodeBase64(digest), "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
