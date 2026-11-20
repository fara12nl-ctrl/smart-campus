package com.smartcampus.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DemoActivity extends Activity {

    private EditText threadsInput;
    private EditText startInput;
    private EditText endInput;
    private ProgressBar progress;
    private TextView result;

    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        threadsInput = findViewById(R.id.threads);
        startInput = findViewById(R.id.start_time);
        endInput = findViewById(R.id.end_time);
        progress = findViewById(R.id.demo_progress);
        result = findViewById(R.id.result);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 10);
        tomorrow.set(Calendar.MINUTE, 0);
        startInput.setText(fmt.format(tomorrow.getTime()));
        tomorrow.set(Calendar.HOUR_OF_DAY, 11);
        endInput.setText(fmt.format(tomorrow.getTime()));

        findViewById(R.id.run_btn).setOnClickListener(v -> run());
    }

    private void run() {
        int threads;
        try {
            threads = Integer.parseInt(threadsInput.getText().toString().trim());
        } catch (NumberFormatException e) {
            threads = 5;
        }
        if (threads < 2 || threads > 20) {
            Toast.makeText(this, "Threads 2 se 20 ke beech hone chahiye", Toast.LENGTH_SHORT).show();
            return;
        }
        String start = startInput.getText().toString().trim();
        String end = endInput.getText().toString().trim();
        progress.setVisibility(View.VISIBLE);
        result.setVisibility(View.GONE);
        final int n = threads;
        new Thread(() -> {
            try {
                String path = "/api/demo/race?type=ROOM&id=1&threads=" + n
                        + "&start=" + URLEncoder.encode(start, "UTF-8")
                        + "&end=" + URLEncoder.encode(end, "UTF-8");
                JSONObject resp = ApiClient.get(path);
                JSONObject data = resp.getJSONObject("data");
                JSONArray winners = data.optJSONArray("winners");
                StringBuilder sb = new StringBuilder();
                sb.append("Attempts: ").append(data.optInt("attempts")).append("\n");
                sb.append("Successful bookings: ").append(data.optInt("successes")).append("\n");
                sb.append("Conflicts (rejected): ").append(data.optInt("conflicts")).append("\n");
                sb.append("Duration: ").append(data.optLong("durationMs")).append(" ms\n");
                if (winners != null && winners.length() > 0) {
                    sb.append("Winner: ");
                    for (int i = 0; i < winners.length(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(winners.optString(i));
                    }
                }
                String text = sb.toString();
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    result.setText(text);
                    result.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
