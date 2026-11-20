package com.smartcampus.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class DashboardActivity extends Activity {

    private LinearLayout statsGrid;
    private LinearLayout quickGrid;
    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Session.restore(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_dashboard);
        statsGrid = findViewById(R.id.stats_grid);
        quickGrid = findViewById(R.id.quick_grid);
        userName = findViewById(R.id.user_name);
        userName.setText(Session.displayName(this));

        findViewById(R.id.logout_btn).setOnClickListener(v -> {
            Session.clear(this);
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        String[] keys = {"students", "faculty", "courses", "resources", "equipment", "events"};
        String[] labels = {"Students", "Faculty", "Courses", "Rooms", "Equipment", "Events"};
        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            addQuickButton(labels[i], v -> {
                Intent intent = new Intent(this, ListActivity.class);
                intent.putExtra("entity", key);
                startActivity(intent);
            });
        }
        addQuickButton("Resource Booking", v -> {
            Intent intent = new Intent(this, ListActivity.class);
            intent.putExtra("entity", "bookings");
            startActivity(intent);
        });
        addQuickButton("Thread Demo", v -> startActivity(new Intent(this, DemoActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        new Thread(() -> {
            try {
                JSONObject resp = ApiClient.get("/api/dashboard");
                JSONObject data = resp.getJSONObject("data");
                runOnUiThread(() -> renderStats(data));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void renderStats(JSONObject data) {
        statsGrid.removeAllViews();
        int[] values = {
                data.optInt("students"), data.optInt("faculty"), data.optInt("courses"),
                data.optInt("rooms"), data.optInt("equipment"), data.optInt("events"),
                data.optInt("todaysBookings"), data.optInt("attendancePercent")
        };
        String[] labels = {"Students", "Faculty", "Courses", "Rooms", "Equipment", "Events",
                "Today's Bookings", "Attendance %"};
        LinearLayout row = null;
        for (int i = 0; i < values.length; i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, dp(4), 0, dp(4));
                statsGrid.addView(row, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            row.addView(createStatCard(labels[i], String.valueOf(values[i]) + (i == 7 ? "%" : "")),
                    weightedParams(dp(4)));
        }
    }

    private View createStatCard(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        int p = dp(14);
        card.setPadding(p, dp(12), p, dp(12));

        TextView l = new TextView(this);
        l.setText(label.toUpperCase());
        l.setTextSize(10);
        l.setTextColor(getColor(R.color.muted));
        l.setLetterSpacing(0.08f);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(24);
        v.setTextColor(getColor(R.color.accent2));
        v.setTypeface(null, Typeface.BOLD);

        card.addView(l);
        card.addView(v);
        return card;
    }

    private interface Click {
        void onClick(android.view.View v);
    }

    private void addQuickButton(String label, Click click) {
        View parent = quickGrid.getChildAt(quickGrid.getChildCount() - 1);
        LinearLayout row;
        if (parent instanceof LinearLayout && ((LinearLayout) parent).getChildCount() < 2) {
            row = (LinearLayout) parent;
        } else {
            row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(4), 0, dp(4));
            quickGrid.addView(row, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(13);
        btn.setAllCaps(false);
        btn.setTextColor(getColor(R.color.text));
        btn.setBackgroundResource(R.drawable.bg_card);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(12), dp(16), dp(12), dp(16));
        btn.setOnClickListener(click::onClick);
        LinearLayout.LayoutParams lp = weightedParams(dp(4));
        lp.height = dp(64);
        row.addView(btn, lp);
    }

    private LinearLayout.LayoutParams weightedParams(int margin) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(margin, 0, margin, 0);
        return lp;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
