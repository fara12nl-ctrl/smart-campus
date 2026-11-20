package com.smartcampus.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONObject;

/** Stores the auth token + logged-in user in SharedPreferences. */
public final class Session {

    private static final String PREFS = "smart_campus";

    private Session() { }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.contains("emulator")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu");
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void save(Context c, String token, JSONObject user) {
        String displayName = user.optString("displayName", "User");
        String role = user.optString("role", "");
        prefs(c).edit()
                .putString("token", token)
                .putString("displayName", displayName)
                .putString("role", role)
                .apply();
        ApiClient.setToken(token);
    }

    /** Server URL set by the user on the login screen (survives logout). */
    public static String serverUrl(Context c) {
        String saved = prefs(c).getString("serverUrl", null);
        if (saved != null) return saved;
        return ApiClient.DEFAULT_BASE_URL;
    }

    /** Normalizes "host:port" / "https://x/" input into "http(s)://host[:port]". */
    public static String saveServerUrl(Context c, String raw) {
        String v = raw == null ? "" : raw.trim();
        if (v.isEmpty()) v = ApiClient.DEFAULT_BASE_URL;
        while (v.endsWith("/")) v = v.substring(0, v.length() - 1);
        if (!v.startsWith("http://") && !v.startsWith("https://")) v = "http://" + v;
        prefs(c).edit().putString("serverUrl", v).apply();
        ApiClient.setBaseUrl(v);
        return v;
    }

    /** Restores the token into ApiClient; returns false if not logged in. */
    public static boolean restore(Context c) {
        ApiClient.setBaseUrl(serverUrl(c));
        String token = prefs(c).getString("token", null);
        if (token == null) return false;
        ApiClient.setToken(token);
        return true;
    }

    public static String displayName(Context c) {
        return prefs(c).getString("displayName", "User");
    }

    public static String role(Context c) {
        return prefs(c).getString("role", "");
    }

    public static void clear(Context c) {
        prefs(c).edit().remove("token").remove("displayName").remove("role").apply();
        ApiClient.setToken(null);
    }
}
