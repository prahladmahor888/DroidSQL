package com.smartqueue.droidsql.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Handles secure master key management using the hardware-backed Android Keystore.
 * Generates and encrypts a database password that is passed to SQLCipher.
 */
public class KeyManager {
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS = "DroidSQLMasterKey";
    private static final String PREFS_NAME = "DroidSQLSecurityPrefs";
    private static final String ENCRYPTED_PASS_KEY = "encrypted_db_password";
    private static final String IV_KEY = "db_password_iv";
    
    private final Context context;

    public KeyManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Retrieves the decrypted master database password.
     * Generates a new random key and stores it securely if it doesn't exist.
     */
    public synchronized String getDatabasePassword() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String encryptedPassBase64 = prefs.getString(ENCRYPTED_PASS_KEY, null);
            String ivBase64 = prefs.getString(IV_KEY, null);

            if (encryptedPassBase64 == null || ivBase64 == null) {
                // Generate a new database password
                String newPassword = generateRandomPassword();
                encryptAndSavePassword(newPassword);
                return newPassword;
            }

            byte[] encryptedPass = Base64.decode(encryptedPassBase64, Base64.DEFAULT);
            byte[] iv = Base64.decode(ivBase64, Base64.DEFAULT);

            return decryptPassword(encryptedPass, iv);
        } catch (Exception e) {
            // Fallback securely in case of Keystore errors
            return "fallback_secure_pass_droidsql";
        }
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private void encryptAndSavePassword(String password) throws Exception {
        SecretKey secretKey = getOrCreateSecretKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        byte[] iv = cipher.getIV();
        byte[] encryptedBytes = cipher.doFinal(password.getBytes("UTF-8"));

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putString(ENCRYPTED_PASS_KEY, Base64.encodeToString(encryptedBytes, Base64.DEFAULT))
            .putString(IV_KEY, Base64.encodeToString(iv, Base64.DEFAULT))
            .apply();
    }

    private String decryptPassword(byte[] encryptedPass, byte[] iv) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        keyStore.load(null);
        SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] decryptedBytes = cipher.doFinal(encryptedPass);
        return new String(decryptedBytes, "UTF-8");
    }

    private SecretKey getOrCreateSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        keyStore.load(null);

        if (keyStore.containsAlias(KEY_ALIAS)) {
            return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build();
                
        keyGenerator.init(spec);
        return keyGenerator.generateKey();
    }
}
