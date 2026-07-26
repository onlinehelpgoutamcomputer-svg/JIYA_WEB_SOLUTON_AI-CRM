package in.jiyawebsolution.jiyaai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private Spinner provider;
    private EditText key, model, endpoint;
    private SharedPreferences prefs;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = SecurePrefs.get(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 50, 36, 36);
        root.setBackgroundColor(Color.rgb(5, 8, 17));

        TextView title = text("JIYA AI  /  SYSTEM SETTINGS", 23, 0xFF35F4FF);
        root.addView(title);
        root.addView(text("AI provider", 13, Color.LTGRAY));
        provider = new Spinner(this);
        String[] values = {"OpenAI", "Gemini", "Claude", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, values);
        provider.setAdapter(adapter);
        provider.setSelection(Math.max(0, java.util.Arrays.asList(values)
                .indexOf(prefs.getString("provider", "OpenAI"))));
        root.addView(provider, full(58));

        root.addView(text("API key (encrypted on this device)", 13, Color.LTGRAY));
        key = field(prefs.getString("api_key", ""), "Paste API key");
        key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(key, full(58));

        root.addView(text("Model", 13, Color.LTGRAY));
        model = field(prefs.getString("model", "gpt-5.4"), "Model ID");
        root.addView(model, full(58));

        root.addView(text("Custom HTTPS endpoint (Custom provider only)", 13, Color.LTGRAY));
        endpoint = field(prefs.getString("endpoint", ""), "https://...");
        root.addView(endpoint, full(58));

        Button save = button("SAVE AI CONFIGURATION");
        save.setOnClickListener(v -> save());
        root.addView(save, full(62));

        Button access = button("OPEN ACCESSIBILITY PERMISSION");
        access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(access, full(62));

        Button mic = button("OPEN APP PERMISSIONS");
        mic.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.parse("package:" + getPackageName()))));
        root.addView(mic, full(62));
        root.addView(text("Privacy guard: JIYA AI never requests OTP, UPI PIN, banking password or unrestricted remote access.",
                13, 0xFF65FFB1));
        setContentView(root);
    }

    private void save() {
        String p = provider.getSelectedItem().toString();
        String selectedModel = model.getText().toString().trim();
        if (selectedModel.isEmpty()) selectedModel = AiClient.defaultModel(p);
        prefs.edit().putString("provider", p)
                .putString("api_key", key.getText().toString().trim())
                .putString("model", selectedModel)
                .putString("endpoint", endpoint.getText().toString().trim()).apply();
        Toast.makeText(this, "Secure settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private EditText field(String value, String hint) {
        EditText edit = new EditText(this);
        edit.setText(value); edit.setHint(hint); edit.setTextColor(Color.WHITE);
        edit.setHintTextColor(0xFF65738A); edit.setSingleLine(true);
        return edit;
    }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size);
        t.setTextColor(color); t.setPadding(0, 14, 0, 10); return t;
    }
    private Button button(String value) {
        Button b = new Button(this); b.setText(value); b.setTextColor(Color.BLACK);
        b.setBackgroundColor(0xFF35F4FF); return b;
    }
    private LinearLayout.LayoutParams full(int height) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, height);
        p.setMargins(0, 6, 0, 14); return p;
    }
}
