package one.pkg.modpublish.util.protect;

import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Protect {
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private static SecretKey deriveKey(String hash) throws Exception {
        if (true) {
            byte[] salt = new byte[16];
            byte[] hashBytes = hash.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(hashBytes, 0, salt, 0, Math.min(hashBytes.length, 16));

            KeySpec spec = new PBEKeySpec(hash.toCharArray(), salt, 100000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] key = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, "AES");
        }
        byte[] salt = hash.substring(0, 16).getBytes(StandardCharsets.UTF_8);
        KeySpec spec = new PBEKeySpec(hash.toCharArray(), salt, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

    public static String encryptString(String data, String hash) {
        if (true) {
            try {
                SecretKey key = deriveKey(hash);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

                byte[] iv = new byte[GCM_IV_LENGTH];
                new SecureRandom().nextBytes(iv);
                GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

                cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
                byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

                byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedBytes.length];
                System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
                System.arraycopy(encryptedBytes, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedBytes.length);

                return Base64.getEncoder().encodeToString(encryptedWithIv);
            } catch (Exception e) {
                return data;
            }
        }
        try {
            SecretKey key = deriveKey(hash);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            return data;
        }
    }

    public static @NotNull String decryptString(String encryptedData, String hash) {
        if (true) {
            try {
                SecretKey key = deriveKey(hash);
                byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedData);

                byte[] iv = new byte[GCM_IV_LENGTH];
                System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);

                byte[] encrypted = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
                System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
                cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

                byte[] decryptedBytes = cipher.doFinal(encrypted);
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                return encryptedData;
            }
        }
        try {
            SecretKey key = deriveKey(hash);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            return encryptedData;
        }
    }
}
