package com.smartcampus.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class FormActivity extends Activity {

    private Entities.Entity entity;
    private JSONObject existing;
    private LinearLayout container;
    private final Map<String, View> inputs = new LinkedHashMap<>();
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entity = Entities.get(getIntent().getStringExtra("entity"));
        String rowJson = getIntent().getStringExtra("row");
        try {
            existing = rowJson == null ? null : new JSONObject(rowJson);
        } catch (Exception e) {
            existing = null;
        }
        if (entity == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_form);
        container = findViewById(R.id.form_container);
        ((TextView) findViewById(R.id.form_title))
                .setText((existing == null ? "Add " : "Edit ") + singular(entity.title));
        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        findViewById(R.id.save_btn).setOnClickListener(v -> save());

        for (Entities.Field field : entity.fields) {
            buildInput(field);
        }
    }

    private String singular(String title) {
        switch (entity.key) {
            case "students": return "Student";
            case "faculty": return "Faculty";
            case "courses": return "Course";
            case "resources": return "Room / Lab";
            case "equipment": return "Equipment";
            case "events": return "Event";
            case "bookings": return "Booking";
            default: return title;
        }
    }

    private String initialValue(String key) {
        if (existing == null) return "";
        String v = existing.optString(key, "");
        if ("null".equals(v)) return "";
        return v;
    }

    private void buildInput(Entities.Field field) {
        TextView label = new TextView(this);
        label.setText(field.label);
        label.setTextSize(12);
        label.setTextColor(getColor(R.color.muted));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(14);
        container.addView(label, lp);

        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        ilp.topMargin = dp(6);

        View input;
        if (field.type == Entities.SELECT) {
            Spinner spinner = new Spinner(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, field.options);
            spinner.setAdapter(adapter);
            String current = initialValue(field.key);
            for (int i = 0; i < field.options.length; i++) {
                if (field.options[i].equals(current)) {
                    spinner.setSelection(i);
                    break;
                }
            }
            input = spinner;
        } else {
            EditText edit = new EditText(this);
            edit.setBackgroundResource(R.drawable.bg_input);
            edit.setPadding(dp(16), 0, dp(16), 0);
            edit.setTextColor(getColor(R.color.text));
            edit.setHintTextColor(getColor(R.color.muted));
            edit.setSingleLine(true);
            String value = initialValue(field.key);
            if (field.type == Entities.NUMBER) {
                edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                edit.setHint("number");
                edit.setText(value);
            } else if (field.type == Entities.DATETIME) {
                edit.setHint("yyyy-MM-dd HH:mm");
                edit.setText(pretty(value));
                edit.setFocusable(false);
                edit.setOnClickListener(v -> pickDateTime(edit));
            } else {
                edit.setText(value);
            }
            input = edit;
        }
        container.addView(input, ilp);
        inputs.put(field.key, input);
    }

    private String pretty(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        String v = iso.replace('T', ' ');
        return v.length() > 16 ? v.substring(0, 16) : v;
    }

    private void pickDateTime(EditText edit) {
        Calendar cal = Calendar.getInstance();
        String current = edit.getText().toString().trim();
        if (!current.isEmpty()) {
            try {
                cal.setTime(fmt.parse(current));
            } catch (Exception ignored) { }
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            new TimePickerDialog(this, (tv, hour, minute) -> {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                edit.setText(fmt.format(cal.getTime()));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        JSONObject body = new JSONObject();
        try {
            for (Entities.Field field : entity.fields) {
                View input = inputs.get(field.key);
                if (input instanceof Spinner) {
                    String val = ((Spinner) input).getSelectedItem().toString();
                    body.put(field.key, val);
                    continue;
                }
                EditText edit = (EditText) input;
                String value = edit.getText().toString().trim();
                if (value.isEmpty()) {
                    if (field.required) {
                        Toast.makeText(this, field.label + " required hai", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    continue;
                }
                if (field.type == Entities.NUMBER) {
                    try {
                        body.put(field.key, Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, field.label + " number hona chahiye", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else if (field.type == Entities.DATETIME) {
                    body.put(field.key, value.replace(' ', 'T'));
                } else {
                    body.put(field.key, value);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        findViewById(R.id.save_btn).setEnabled(false);
        new Thread(() -> {
            try {
                if (existing != null && entity.editable) {
                    ApiClient.put(entity.api + "/" + existing.optLong("id"), body);
                } else {
                    ApiClient.post(entity.api, body);
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    findViewById(R.id.save_btn).setEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
