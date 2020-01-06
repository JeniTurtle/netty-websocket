package com.weds.socket.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class AuthCheck {
  public static String genAuthKey(String secretKey) {
    Long milliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
    return Encryption.encrypt(milliSecond + "", secretKey);
  }

  public static boolean validateAuthKey(String secretKey, String validateKey) {
    Long milliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
    String decodeValue = Encryption.decrypt(validateKey, secretKey);
    if (decodeValue == null) {
      return false;
    }
    Long authTime = Long.valueOf(decodeValue);
    return milliSecond - authTime < 5 * 60 * 1000;
  }
}
