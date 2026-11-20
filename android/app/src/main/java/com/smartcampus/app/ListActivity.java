package com.smartcampus.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends Activity {

    private Entities.Entity entity;
    private final JSONArray rows = new JSONArray();
    private final List<JSONObject> shown = new ArrayList<>();
    private RowAdapter adapter;
    private EditText search;
    private ProgressBar progress;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entity = Entities.get(getIntent().getStringExtra("entity"));
        if (entity == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_list);
        ((TextView) findViewById(R.id.title)).setText(entity.title);
        search = findViewById(R.id.search);
        progress = findViewById(R.id.progress);
        empty = findViewById(R.id.empty);
        findViewById(R.id.back_btn).setOnClickListener(v -> finish());

        Button addBtn = findViewById(R.id.add_btn);
        addBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, FormActivity.class);
            i.putExtra("entity", entity.key);
            startActivity(i);
        });

        ListView list = findViewById(R.id.list);
        adapter = new RowAdapter();
        list.setAdapter(adapter);

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { }
            public void afterTextChanged(Editable s) { filter(s.toString()); }
        });

        list.setOnItemClickListener((parent, view, position, id) -> openItem(shown.get(position)));
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(shown.get(position));
            return true;
        });

        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (entity != null) load();
    }

    private void load() {
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                JSONObject resp = ApiClient.get(entity.api);
                JSONArray data = resp.getJSONArray("data");
                runOnUiThread(() -> {
                    rows.length(); // keep reference alive
                    rowsClean();
                    for (int i = 0; i < data.length(); i++) {
                        rows.put(data.optJSONObject(i));
                    }
                    progress.setVisibility(View.GONE);
                    filter(search.getText().toString());
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void rowsClean() {
        while (rows.length() > 0) rows.remove(0);
    }

    private void filter(String q) {
        shown.clear();
        String needle = q.trim().toLowerCase();
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.optJSONObject(i);
            if (row == null) continue;
            if (needle.isEmpty() || row.toString().toLowerCase().contains(needle)) {
                shown.add(row);
            }
        }
        adapter.notifyDataSetChanged();
        empty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openItem(JSONObject row) {
        if (entity.key.equals("bookings")) {
            String status = row.optString("status", "");
            if ("CONFIRMED".equalsIgnoreCase(status)) {
                new AlertDialog.Builder(this)
                        .setTitle("Booking #" + row.optLong("id"))
                        .setItems(new String[]{"Cancel booking", "Delete", "Close"}, (d, which) -> {
                            if (which == 0) cancelBooking(row.optLong("id"));
                            else if (which == 1) confirmDelete(row);
                        })
                        .show();
            } else {
                confirmDelete(row);
            }
            return;
        }
        if (!entity.editable) return;
        Intent i = new Intent(this, FormActivity.class);
        i.putExtra("entity", entity.key);
        i.putExtra("row", row.toString());
        startActivity(i);
    }

    private void cancelBooking(long id) {
        new AlertDialog.Builder(this)
                .setMessage("Cancel this booking?")
                .setPositiveButton("Yes", (d, w) -> new Thread(() -> {
                    try {
                        ApiClient.put(entity.api + "/" + id + "/cancel", new JSONObject());
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                            load();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }).start())
                .setNegativeButton("No", null)
                .show();
    }

    private void confirmDelete(JSONObject row) {
        new AlertDialog.Builder(this)
                .setMessage("Delete this record?")
                .setPositiveButton("Delete", (d, w) -> new Thread(() -> {
                    try {
                        ApiClient.delete(entity.api + "/" + row.optLong("id"));
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                            load();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }).start())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String pretty(String value) {
        if (value == null) return "";
        return value.replace('T', ' ').replace(".000", "");
    }

    private class RowAdapter extends BaseAdapter {

        @Override
        public int getCount() { return shown.size(); }

        @Override
        public Object getItem(int position) { return shown.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(ListActivity.this).inflate(R.layout.item_row, parent, false);
            }
            JSONObject row = shown.get(position);
            TextView title = v.findViewById(R.id.row_title);
            TextView sub = v.findViewById(R.id.row_sub);
            TextView badge = v.findViewById(R.id.row_badge);

            String titleText = row.optString(entity.titleField, "");
            if (titleText.isEmpty()) titleText = entity.title + " #" + row.optLong("id");
            title.setText(titleText);

            StringBuilder sb = new StringBuilder();
            for (String key : entity.subFields) {
                String val = pretty(row.optString(key, ""));
                if (val.isEmpty() || "null".equals(val)) continue;
                if (sb.length() > 0) sb.append("  •  ");
                sb.append(val);
            }
            sub.setText(sb.toString());

            String status = row.optString("status", "");
            if (!status.isEmpty() && !"null".equals(status)) {
                badge.setText(status);
                badge.setVisibility(View.VISIBLE);
                int color;
                if (status.equals("CONFIRMED") || status.equals("AVAILABLE")) color = R.color.good;
                else if (status.equals("CANCELLED") || status.equals("MAINTENANCE")) color = R.color.bad;
                else color = R.color.warn;
                badge.setTextColor(getColor(color));
            } else {
                badge.setVisibility(View.GONE);
            }
            return v;
        }
    }
}
