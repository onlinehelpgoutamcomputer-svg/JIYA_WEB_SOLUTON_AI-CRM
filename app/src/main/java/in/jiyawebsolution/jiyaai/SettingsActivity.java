package in.jiyawebsolution.jiyaai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private Spinner provider, language;
    private EditText key, model, endpoint;
    private SharedPreferences prefs;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = SecurePrefs.get(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(UiKit.BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        TextView back = text("‹  Back", 15, UiKit.CYAN);
        back.setOnClickListener(v -> finish());
        root.addView(back, height(42));

        TextView title = text("System Control", 28, UiKit.TEXT);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(title);
        root.addView(text("Configure JIYA AI exactly the way you want.", 14, UiKit.MUTED));

        LinearLayout experience = card();
        experience.addView(sectionTitle("EXPERIENCE"));
        experience.addView(text("Interface & voice language", 14, UiKit.TEXT));
        language = spinner(new String[]{"English", "বাংলা", "हिन्दी"});
        String currentLanguage = prefs.getString("language", "English");
        language.setSelection(currentLanguage.equals("বাংলা") ? 1 : currentLanguage.equals("हिन्दी") ? 2 : 0);
        experience.addView(language, fieldParams());

        Switch confirmations = new Switch(this);
        confirmations.setText("Action confirmations (Required)");
        confirmations.setTextColor(UiKit.TEXT);
        confirmations.setChecked(true);
        confirmations.setEnabled(false);
        confirmations.setPadding(0, dp(6), 0, 0);
        experience.addView(confirmations, height(54));
        root.addView(experience, blockParams());

        LinearLayout aiCard = card();
        aiCard.addView(sectionTitle("AI CONNECTION"));
        aiCard.addView(text("Provider", 14, UiKit.TEXT));
        String[] providers = {"OpenAI", "Gemini", "Claude", "Custom"};
        provider = spinner(providers);
        provider.setSelection(Math.max(0, java.util.Arrays.asList(providers)
                .indexOf(prefs.getString("provider", "OpenAI"))));
        aiCard.addView(provider, fieldParams());

        aiCard.addView(text("API key", 14, UiKit.TEXT));
        key = field(prefs.getString("api_key", ""), "Paste API key");
        key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        aiCard.addView(key, fieldParams());

        aiCard.addView(text("Model ID", 14, UiKit.TEXT));
        model = field(prefs.getString("model", "gpt-5.4"), "Model ID");
        aiCard.addView(model, fieldParams());

        aiCard.addView(text("Custom HTTPS endpoint", 14, UiKit.TEXT));
        endpoint = field(prefs.getString("endpoint", ""), "Required only for Custom");
        aiCard.addView(endpoint, fieldParams());
        aiCard.addView(text("Your API key is encrypted and stored only on this phone.", 12, UiKit.GREEN));
        root.addView(aiCard, blockParams());

        LinearLayout permissionCard = card();
        permissionCard.addView(sectionTitle("DEVICE PERMISSIONS"));
        permissionCard.addView(text("Microphone enables voice commands. Accessibility enables only Home, Back and Recents navigation.", 13, UiKit.MUTED));

        Button access = button("Accessibility settings", UiKit.PANEL_2, UiKit.CYAN);
        access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        permissionCard.addView(access, buttonParams());

        Button permissions = button("App permissions", UiKit.PANEL_2, UiKit.CYAN);
        permissions.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.parse("package:" + getPackageName()))));
        permissionCard.addView(permissions, buttonParams());
        root.addView(permissionCard, blockParams());

        Button save = button("Save settings", UiKit.CYAN, UiKit.BG);
        save.setOnClickListener(v -> save());
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(-1, dp(58));
        saveParams.setMargins(0, dp(18), 0, 0);
        root.addView(save, saveParams);

        TextView privacy = text("SECURITY SHIELD ACTIVE\nNo OTP, UPI PIN, banking password or hidden remote access.",
                12, UiKit.GREEN);
        privacy.setGravity(Gravity.CENTER);
        privacy.setBackground(UiKit.outlined(0xFF07120F, UiKit.GREEN, 14, this));
        privacy.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams privacyParams = new LinearLayout.LayoutParams(-1, -2);
        privacyParams.setMargins(0, dp(16), 0, 0);
        root.addView(privacy, privacyParams);
        setContentView(scroll);
    }

    private void save() {
        String selectedProvider = provider.getSelectedItem().toString();
        String selectedModel = model.getText().toString().trim();
        if (selectedModel.isEmpty()) selectedModel = AiClient.defaultModel(selectedProvider);
        prefs.edit().putString("provider", selectedProvider)
                .putString("language", language.getSelectedItem().toString())
                .putString("api_key", key.getText().toString().trim())
                .putString("model", selectedModel)
                .putString("endpoint", endpoint.getText().toString().trim()).apply();
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private EditText field(String value, String hint) {
        EditText edit = new EditText(this);
        edit.setText(value);
        edit.setHint(hint);
        edit.setTextColor(UiKit.TEXT);
        edit.setTextSize(15);
        edit.setHintTextColor(0xFF65738A);
        edit.setSingleLine(true);
        edit.setPadding(dp(16), 0, dp(16), 0);
        edit.setBackground(UiKit.outlined(UiKit.PANEL_2, 0xFF26364D, 13, this));
        return edit;
    }

    private Spinner spinner(String[] values) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, values) {
            @Override public android.view.View getView(int position, android.view.View convertView,
                                                       android.view.ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(UiKit.TEXT);
                view.setTextSize(15);
                view.setPadding(dp(16), 0, dp(12), 0);
                return view;
            }
        };
        spinner.setAdapter(adapter);
        spinner.setBackground(UiKit.outlined(UiKit.PANEL_2, 0xFF26364D, 13, this));
        return spinner;
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(0, dp(7), 0, dp(7));
        return view;
    }

    private TextView sectionTitle(String value) {
        TextView view = text(value, 12, UiKit.CYAN);
        view.setLetterSpacing(.12f);
        view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return view;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(16));
        card.setBackground(UiKit.outlined(UiKit.PANEL, 0xFF1B2A40, 18, this));
        return card;
    }

    private Button button(String value, int background, int text) {
        Button button = new Button(this);
        button.setText(value);
        UiKit.styleButton(button, background, text, this);
        UiKit.pressEffect(button);
        return button;
    }

    private LinearLayout.LayoutParams fieldParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(54));
        params.setMargins(0, dp(3), 0, dp(12));
        return params;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(54));
        params.setMargins(0, dp(10), 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams blockParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(18), 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams height(int value) {
        return new LinearLayout.LayoutParams(-1, dp(value));
    }

    private int dp(int value) { return UiKit.dp(this, value); }
}
