package one.pkg.modpublish.settings;

import one.pkg.modpublish.protect.HardwareFingerprint;
import one.pkg.modpublish.protect.Protect;

public class StateBase {
    String getDecryptedToken(String encryptedToken) {
        if (encryptedToken == null || encryptedToken.isEmpty()) return "";
        return Protect.decryptString(encryptedToken, HardwareFingerprint.generateSecureProjectKey());
    }

    String encryptToken(String token) {
        return token == null || token.isBlank() ? "" : Protect.encryptString(token, HardwareFingerprint.generateSecureProjectKey());
    }
}
