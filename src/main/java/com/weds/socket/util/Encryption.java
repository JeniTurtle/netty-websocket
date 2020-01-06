package com.weds.socket.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public class Encryption {

  private final static Logger LOGGER = LogManager.getLogger(Encryption.class);

  public static String encrypt(String word, String password) {
    String result = "";
    fixKeyLength();
    try {
      byte[] ivBytes;
      /*you can give whatever you want for password. This is for testing purpose*/
      SecureRandom random = new SecureRandom();
      byte bytes[] = new byte[20];
      random.nextBytes(bytes);
      byte[] saltBytes = bytes;
      // Derive the key
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65556, 256);
      SecretKey secretKey = factory.generateSecret(spec);
      SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
      //encrypting the word
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, secret);
      AlgorithmParameters params = cipher.getParameters();
      ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();
      byte[] encryptedTextBytes = cipher.doFinal(word.getBytes("UTF-8"));
      //prepend salt and vi
      byte[] buffer = new byte[saltBytes.length + ivBytes.length + encryptedTextBytes.length];
      System.arraycopy(saltBytes, 0, buffer, 0, saltBytes.length);
      System.arraycopy(ivBytes, 0, buffer, saltBytes.length, ivBytes.length);
      System.arraycopy(encryptedTextBytes, 0, buffer, saltBytes.length + ivBytes.length, encryptedTextBytes.length);
      //result =  new Base64().encodeToString(buffer);
      result = new String(java.util.Base64.getEncoder().encode(buffer));
    }catch (Exception e){
      e.printStackTrace();
      LOGGER.error("Encryption : " +e);
    }
    return result;

  }

  public static String decrypt(String encryptedText, String password) {
    String result = "";
    fixKeyLength();
    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      //strip off the salt and iv
      //ByteBuffer buffer = ByteBuffer.wrap(new Base64().decode(encryptedText));
      ByteBuffer buffer = ByteBuffer.wrap(java.util.Base64.getDecoder().decode(encryptedText));
      byte[] saltBytes = new byte[20];
      buffer.get(saltBytes, 0, saltBytes.length);
      byte[] ivBytes1 = new byte[cipher.getBlockSize()];
      buffer.get(ivBytes1, 0, ivBytes1.length);
      byte[] encryptedTextBytes = new byte[buffer.capacity() - saltBytes.length - ivBytes1.length];

      buffer.get(encryptedTextBytes);
      // Deriving the key
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65556, 256);
      SecretKey secretKey = factory.generateSecret(spec);
      SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
      cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes1));
      byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
      result = new String(decryptedTextBytes);
    }catch (Exception e){
      e.printStackTrace();
      LOGGER.error("Decryption : "+e);
    }
    return result;
  }

  public static void fixKeyLength() {
    String errorString = "Failed manually overriding key-length permissions.";
    int newMaxKeyLength;
    try {
      if ((newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES")) < 256) {
        Class c = Class.forName("javax.crypto.CryptoAllPermissionCollection");
        Constructor con = c.getDeclaredConstructor();
        con.setAccessible(true);
        Object allPermissionCollection = con.newInstance();
        Field f = c.getDeclaredField("all_allowed");
        f.setAccessible(true);
        f.setBoolean(allPermissionCollection, true);

        c = Class.forName("javax.crypto.CryptoPermissions");
        con = c.getDeclaredConstructor();
        con.setAccessible(true);
        Object allPermissions = con.newInstance();
        f = c.getDeclaredField("perms");
        f.setAccessible(true);
        ((Map) f.get(allPermissions)).put("*", allPermissionCollection);

        c = Class.forName("javax.crypto.JceSecurityManager");
        f = c.getDeclaredField("defaultPolicy");
        f.setAccessible(true);
        Field mf = Field.class.getDeclaredField("modifiers");
        mf.setAccessible(true);
        mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
        f.set(null, allPermissions);

        newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
      }
    } catch (Exception e) {
      throw new RuntimeException(errorString, e);
    }
    if (newMaxKeyLength < 256)
      throw new RuntimeException(errorString); // hack failed
  }
}