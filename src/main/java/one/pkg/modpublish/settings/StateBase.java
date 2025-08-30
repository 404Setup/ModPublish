package one.pkg.modpublish.settings;

import one.pkg.modpublish.data.internel.Info;
import one.pkg.modpublish.protect.HardwareFingerprint;
import one.pkg.modpublish.protect.Protect;
import org.jetbrains.annotations.NotNull;

public class StateBase {
    @NotNull
    Info getDecryptedToken(@NotNull String encryptedToken) {
        if (encryptedToken.isEmpty()) return Info.INSTANCE;
        var v = Protect.decryptString(encryptedToken, HardwareFingerprint.generateSecureProjectKey());
        if (encryptedToken.equals(v)) return Info.of(v, true, true);
        return Info.of(v, false, true);
    }

    @NotNull
    String encryptToken(String token) {
        return token == null || token.isBlank() ? "" : Protect.encryptString(token, HardwareFingerprint.generateSecureProjectKey());
    }
}
