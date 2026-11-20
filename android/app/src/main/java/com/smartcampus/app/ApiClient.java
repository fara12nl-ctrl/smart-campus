package com.smartcampus.app;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Thin HTTP client over HttpURLConnection (no external libraries needed). */
public final class ApiClient {

    /** Default: 10.0.2.2 is the host machine's localhost as seen from the Android emulator. */
    public static final String DEFAULT_BASE_URL = "http://10.0.2.2:8080";

    private static String baseUrl = DEFAULT_BASE_URL;
    private static String token;

    private ApiClient() { }

    public static void setBaseUrl(String url) {
        String v = url == null ? "" : url.trim();
        if (!v.isEmpty()) baseUrl = v;
    }

    public static String getBaseUrl() { return baseUrl; }

    public static void setToken(String t) { token = t; }
    public static String getToken() { return token; }

    public static class ApiError extends Exception {
        public final int status;

        public ApiError(int status, String message) {
            super(message);
            this.status = status;
        }
    }

    public static JSONObject request(String method, String path, JSONObject body) throws ApiError {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setRequestProperty("Accept", "application/json");
            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            if (body != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = conn.getResponseCode();
            InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String text = readAll(is);
            if (text == null || text.isEmpty()) {
                if (status >= 400) throw new ApiError(status, "HTTP " + status);
                return new JSONObject();
            }
            JSONObject json = new JSONObject(text);
            if (status >= 400 || !json.optBoolean("success", true)) {
                throw new ApiError(status, json.optString("message", "HTTP " + status));
            }
            return json;
        } catch (ApiError e) {
            throw e;
        } catch (Exception e) {
            throw new ApiError(0, "Network error: " + e.getMessage()
                    + " (server reachable hai? Server URL check karo)");
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static JSONObject login(String username, String password) throws ApiError {
        JSONObject body = new JSONObject();
        try {
            body.put("username", username);
            body.put("password", password);
        } catch (Exception ignored) { }
        return request("POST", "/api/auth/login", body);
    }

    public static JSONObject get(String path) throws ApiError { return request("GET", path, null); }
    public static JSONObject post(String path, JSONObject body) throws ApiError { return request("POST", path, body); }
    public static JSONObject put(String path, JSONObject body) throws ApiError { return request("PUT", path, body); }
    public static JSONObject delete(String path) throws ApiError { return request("DELETE", path, null); }

    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }
}
