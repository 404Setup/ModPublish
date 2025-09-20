/*
 * Copyright (C) 2025 404Setup (https://github.com/404Setup)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package one.pkg.modpublish.util.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import okhttp3.Request;
import okhttp3.Response;
import one.pkg.modpublish.api.API;
import one.pkg.modpublish.api.NetworkUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VersionProcessor {
    private static final Logger LOG = Logger.getInstance(VersionProcessor.class);
    private static final String MOJANG_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String CURSEFORGE_URL = "https://api.curseforge.com/v1/minecraft/version";

    private static List<Map<String, Object>> fetchMinecraftVersions() {

        try {
            LOG.info("Fetching Minecraft version data from Mojang API..");
            Request request = API.getBaseRequestBuilder().url(MOJANG_URL).build();
            try (Response response = NetworkUtil.getClient().newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    LOG.error("❌ Network request error: " + response.code());
                    return null;
                }

                String body = response.body().string();
                JsonObject data = JsonParser.fromJson(body).getAsJsonObject();
                List<Map<String, Object>> processedVersions = new ArrayList<>();

                JsonArray versions = data.getAsJsonArray("versions");
                for (JsonElement element : versions) {
                    JsonObject version = element.getAsJsonObject();
                    String releaseTime = version.get("releaseTime").getAsString();
                    if (releaseTime.endsWith("+00:00")) {
                        releaseTime = releaseTime.substring(0, releaseTime.length() - 6) + "Z";
                    }

                    Map<String, Object> processedVersion = new LinkedHashMap<>();
                    processedVersion.put("v", version.get("id").getAsString());
                    processedVersion.put("t", version.get("type").getAsString());
                    processedVersion.put("d", releaseTime);
                    processedVersions.add(processedVersion);
                }

                LOG.info("✅ Successfully processed " + processedVersions.size() + " versions");

                JsonObject latestInfo = data.getAsJsonObject("latest");
                LOG.info("📋 Latest version info:");
                LOG.info("   Release: " + latestInfo.get("release").getAsString());
                LOG.info("   Snapshot: " + latestInfo.get("snapshot").getAsString());

                return processedVersions;
            }
        } catch (IOException e) {
            LOG.error("❌ Network request error", e);
        } catch (JsonParseException e) {
            LOG.error("❌ JSON parsing error", e);
        } catch (Exception e) {
            LOG.error("❌ Unknown error", e);
        }
        return null;
    }

    private static List<Map<String, Object>> fetchCurseforgeVersions() {
        try {
            LOG.info("Fetching version data from CurseForge API...");
            Request request = API.getBaseRequestBuilder().url(CURSEFORGE_URL).build();
            try (Response response = NetworkUtil.getClient().newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    LOG.error("❌ Network request error: " + response.code());
                    return null;
                }

                String body = response.body().string();
                JsonObject data = JsonParser.fromJson(body).getAsJsonObject();
                JsonArray versions = data.has("data") ? data.getAsJsonArray("data") : new JsonArray();

                Type listType = new TypeToken<List<Map<String, Object>>>() {
                }.getType();
                List<Map<String, Object>> parsed = JsonParser.fromJson(versions, listType);

                LOG.info("✅ Successfully got CurseForge API data with " + parsed.size() + " versions");
                return parsed;
            }

        } catch (IOException e) {
            LOG.error("❌ Network request error", e);
        } catch (JsonParseException e) {
            LOG.error("❌ JSON parsing error", e);
        } catch (Exception e) {
            LOG.error("❌ Unknown error getting CurseForge data", e);
        }
        return null;
    }

    private static Map<String, Integer> createVersionMapping(List<Map<String, Object>> curseforgeVersions) {
        Map<String, Integer> versionMapping = new LinkedHashMap<>();
        for (Map<String, Object> version : curseforgeVersions) {
            String versionString = (String) version.getOrDefault("versionString", "");
            Number idNum = (Number) version.getOrDefault("gameVersionId", -1);
            int gameVersionId = idNum != null ? idNum.intValue() : -1;
            if (!versionString.isEmpty()) {
                versionMapping.put(versionString, gameVersionId);
            }
        }
        LOG.info("✅ Created version mapping table with " + versionMapping.size() + " entries");
        return versionMapping;
    }

    private static List<Map<String, Object>> mergeCurseforgeIds(List<Map<String, Object>> minecraftVersions, Map<String, Integer> versionMapping) {
        List<Map<String, Object>> updatedVersions = new ArrayList<>();
        int matchedCount = 0;
        LOG.info("Merging version data...");

        for (Map<String, Object> version : minecraftVersions) {
            String versionString = (String) version.getOrDefault("v", "");
            int gameVersionId = versionMapping.getOrDefault(versionString, -1);

            Map<String, Object> updated = new LinkedHashMap<>();
            updated.put("v", versionString);
            updated.put("t", version.getOrDefault("t", ""));
            updated.put("i", gameVersionId);
            updated.put("d", version.getOrDefault("d", ""));

            if (gameVersionId != -1) {
                matchedCount++;
            }

            updatedVersions.add(updated);
        }

        LOG.info("✅ Version update complete: " + matchedCount + "/" + minecraftVersions.size() + " versions found matching game version IDs");
        return updatedVersions;
    }

    private static boolean saveVersions(@NotNull List<Map<String, Object>> versions, @NotNull File outputFile) {
        try {
            if (outputFile.exists()) outputFile.delete();
            outputFile.createNewFile();
            try (FileWriter writer = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
                JsonParser.toJson(versions, writer);
                LOG.info("✅ Successfully saved updated data to " + outputFile);
                return true;
            }
        } catch (IOException e) {
            LOG.error("❌ Error saving file, ", e);
            return false;
        }
    }

    private static boolean saveVersions(List<Map<String, Object>> versions, String outputFile) {
        return saveVersions(versions, new File(outputFile));
    }

    private static void showPreview(List<Map<String, Object>> updatedVersions, int count) {
        LOG.info("📊 Update Result Preview (First " + count + " items):");
        LOG.info("------------------------------------------------------------");

        for (int i = 0; i < Math.min(count, updatedVersions.size()); i++) {
            Map<String, Object> version = updatedVersions.get(i);
            int gameVersionId = ((Number) version.get("i")).intValue();
            String status = gameVersionId != -1 ? "✅ Matched" : "❌ Unmatched";
            LOG.info("   " + (i + 1) + ". " + version.get("v") + " (" + version.get("t") + ") - ID: " + gameVersionId + " - " + status);
        }

        if (updatedVersions.size() > count) {
            LOG.info("   ... " + (updatedVersions.size() - count) + " more versions");
        }

        long matchedCount = updatedVersions.stream().filter(v -> ((Number) v.get("i")).intValue() != -1).count();
        int totalCount = updatedVersions.size();
        double matchRate = totalCount > 0 ? (matchedCount * 100.0 / totalCount) : 0;

        LOG.info("📈 Match Statistics:");
        LOG.info("   Total Versions: " + totalCount);
        LOG.info("   Matched Versions: " + matchedCount);
        LOG.info("   Unmatched Versions: " + (totalCount - matchedCount));
        LOG.info("   Match Rate: " + String.format("%.1f", matchRate) + "%");
    }

    public static boolean updateVersions() {
        List<Map<String, Object>> mojang = fetchMinecraftVersions();
        if (mojang == null) return false;
        List<Map<String, Object>> curseforge = fetchCurseforgeVersions();
        if (curseforge == null) return false;
        Map<String, Integer> mapping = createVersionMapping(curseforge);
        List<Map<String, Object>> merged = mergeCurseforgeIds(mojang, mapping);
        return saveVersions(merged, FileAPI.getUserDataFile("minecraft.version.json"));
        //showPreview(merged, 5);
    }
}
