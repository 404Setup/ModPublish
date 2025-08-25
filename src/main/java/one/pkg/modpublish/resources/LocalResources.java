package one.pkg.modpublish.resources;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.data.local.SupportedInfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class LocalResources {
    private static final Gson gson = new Gson();
    private static final TypeToken<List<MinecraftVersion>> mvType = new TypeToken<>() {
    };
    private static final TypeToken<List<LauncherInfo>> liType = new TypeToken<>() {
    };

    private LocalResources() {
    }

    public static SupportedInfo getSupportedInfo() {
        try (InputStream supportedStream = LocalResources.class.getResourceAsStream("/META-INF/supported.info.json")) {
            if (supportedStream == null) {
                throw new Exception("supported.info.json not found");
            }
            try (InputStreamReader reader = new InputStreamReader(supportedStream)) {
                return gson.fromJson(reader, SupportedInfo.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<LauncherInfo> getLauncherInfo() {
        try (InputStream launcherStream = LocalResources.class.getResourceAsStream("/META-INF/launcher.info.json")) {
            if (launcherStream == null) {
                throw new Exception("launcher.info.json not found");
            }
            try (InputStreamReader reader = new InputStreamReader(launcherStream)) {
                return gson.fromJson(reader, liType.getType());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<MinecraftVersion> getMinecraftVersions() {
        try (InputStream stream = LocalResources.class.getResourceAsStream("/META-INF/minecraft.version.json")) {
            if (stream == null) {
                throw new Exception("minecraft.version.json not found");
            }
            try (InputStreamReader reader = new InputStreamReader(stream)) {
                return new Gson().fromJson(reader, mvType.getType());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
