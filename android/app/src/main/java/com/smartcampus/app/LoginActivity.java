package com.smartcampus.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

public class LoginActivity extends Activity {

    private EditText username;
    private EditText password;
    private EditText serverUrl;
    private Button loginBtn;
    private ProgressBar progress;
    private TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Session.restore(this)) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        serverUrl = findViewById(R.id.server_url);
        loginBtn = findViewById(R.id.login_btn);
        progress = findViewById(R.id.login_progress);
        error = findViewById(R.id.login_error);
        serverUrl.setText(Session.serverUrl(this));
        loginBtn.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String user = username.getText().toString().trim();
        String pass = password.getText().toString();
        if (user.isEmpty() || pass.isEmpty()) {
            showError("Username aur password dono required hain");
            return;
        }
        String url = serverUrl.getText().toString().trim();
        if (url.isEmpty()) {
            showError("Server URL daalo: PC pe scripts\\start-tunnel.bat chalao, jo https://xxx.trycloudflare.com URL mile woh yahan paste karo");
            return;
        }
        if (!Session.isEmulator() && url.contains("10.0.2.2")) {
            showError("10.0.2.2 sirf emulator ke liye hai. Real phone pe tunnel URL daalo (PC pe start-tunnel.bat) ya PC ka LAN IP jaise http://192.168.1.7:8080");
            return;
        }
        Session.saveServerUrl(this, url);
        setBusy(true);
        new Thread(() -> {
            try {
                JSONObject resp = ApiClient.login(user, pass);
                JSONObject data = resp.getJSONObject("data");
                JSONObject account = data.getJSONObject("user");
                String token = data.getString("token");
                runOnUiThread(() -> {
                    Session.save(this, token, account);
                    setBusy(false);
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setBusy(false);
                    showError(e.getMessage());
                });
            }
        }).start();
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        loginBtn.setEnabled(!busy);
        error.setVisibility(View.GONE);
    }

    private void showError(String message) {
        error.setText(message);
        error.setVisibility(View.VISIBLE);
    }
}
