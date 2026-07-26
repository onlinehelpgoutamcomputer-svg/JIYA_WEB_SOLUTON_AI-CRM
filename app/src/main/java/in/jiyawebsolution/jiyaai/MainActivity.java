package in.jiyawebsolution.jiyaai;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView status, transcript;
    private HoloCoreView core;
    private TextToSpeech tts;
    private AiClient ai;

    private final ActivityResultLauncher<Intent> speechLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                core.setListening(false);
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) handle(matches.get(0));
                } else setStatus("STANDBY");
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        ai = new AiClient(this);
        tts = new TextToSpeech(this, code -> {
            if (code == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("bn", "IN"));
        });
        buildUi();
        greet();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(28, 20, 28, 20);
        root.setBackgroundColor(0xFF050811);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = label("JIYA AI", 25, Color.WHITE);
        TextView security = label("●  PRIVATE MODE", 12, 0xFF65FFB1);
        top.addView(brand, new LinearLayout.LayoutParams(0, 55, 1));
        top.addView(security);
        root.addView(top, new LinearLayout.LayoutParams(-1, 60));

        status = label("SYSTEM ONLINE", 13, 0xFF35F4FF);
        status.setGravity(Gravity.CENTER);
        root.addView(status, new LinearLayout.LayoutParams(-1, 38));

        core = new HoloCoreView(this);
        root.addView(core, new LinearLayout.LayoutParams(-1, 360));

        transcript = label("Tap the core and speak.\nবাংলা • हिन्दी • English", 16, 0xFFCFD8E8);
        transcript.setGravity(Gravity.CENTER);
        transcript.setMaxLines(5);
        root.addView(transcript, new LinearLayout.LayoutParams(-1, 95));

        Button talk = neonButton("TAP TO SPEAK", 0xFF35F4FF);
        talk.setOnClickListener(v -> startListening());
        core.setOnClickListener(v -> startListening());
        root.addView(talk, params(64));

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        Button settingsButton = neonButton("SETTINGS", 0xFFA96BFF);
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        Button stop = neonButton("EMERGENCY STOP", 0xFFFF5577);
        stop.setOnClickListener(v -> {
            if (tts != null) tts.stop(); core.setListening(false);
            transcript.setText("Automation stopped."); setStatus("SAFE STOP");
        });
        actions.addView(settingsButton, new LinearLayout.LayoutParams(0, 58, 1));
        actions.addView(stop, new LinearLayout.LayoutParams(0, 58, 1));
        root.addView(actions, params(62));

        TextView footer = label("Local permission control  •  Encrypted API keys  •  v1.0", 11, 0xFF718098);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, params(40));
        setContentView(root);
    }

    private void greet() {
        transcript.setText("Hello Boss! JIYA AI is ready. আজ কী কাজ করব?");
    }

    private void startListening() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 30);
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Boss, বলুন...");
        try {
            core.setListening(true); setStatus("LISTENING");
            speechLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Install Google Speech Services", Toast.LENGTH_LONG).show();
        }
    }

    private void handle(String command) {
        transcript.setText("YOU: " + command);
        String c = command.toLowerCase(Locale.ROOT);
        if (contains(c, "সেটিং", "setting")) {
            act("Open system settings?", () -> startActivity(new Intent(Settings.ACTION_SETTINGS)));
        } else if (contains(c, "ক্যামেরা", "camera")) {
            act("Open camera?", () -> startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)));
        } else if (contains(c, "হোয়াটসঅ্যাপ", "whatsapp")) {
            act("Open WhatsApp?", () -> openPackage("com.whatsapp"));
        } else if (contains(c, "ইউটিউব", "youtube")) {
            act("Open YouTube?", () -> openPackage("com.google.android.youtube"));
        } else if (contains(c, "বাড়ি", "home")) {
            if (!JiyaAccessibilityService.goHome()) accessibilityHint();
        } else if (contains(c, "পিছনে", "back")) {
            if (!JiyaAccessibilityService.goBack()) accessibilityHint();
        } else if (contains(c, "রিসেন্ট", "recent")) {
            if (!JiyaAccessibilityService.openRecents()) accessibilityHint();
        } else if (contains(c, "কল কর", "call")) {
            String number = c.replaceAll("[^0-9+]", "");
            if (number.length() >= 7) act("Prepare call to " + number + "?",
                    () -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number))));
            else reply("নম্বরটি বলুন বা লিখুন—আমি dial screen খুলব, কল আপনি confirm করবেন।");
        } else askAi(command);
    }

    private void askAi(String command) {
        if (!ai.isConfigured()) {
            reply("Boss, Settings-এ আপনার AI provider এবং API key যোগ করুন।");
            return;
        }
        setStatus("AI THINKING");
        ai.ask(command, (answer, error) -> runOnUiThread(() -> {
            if (error != null) reply("Connection problem: " + error.getMessage());
            else reply(answer);
        }));
    }

    private void act(String question, Runnable action) {
        new AlertDialog.Builder(this).setTitle("JIYA AI confirmation")
                .setMessage(question).setNegativeButton("Cancel", null)
                .setPositiveButton("Allow once", (d, w) -> {
                    try { action.run(); reply("Done, Boss."); }
                    catch (Exception e) { reply("I could not complete that action."); }
                }).show();
    }

    private void openPackage(String packageName) {
        Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch == null) throw new IllegalStateException("App not installed");
        startActivity(launch);
    }
    private void accessibilityHint() {
        reply("Navigation permission is off. Open Settings and enable JIYA AI Accessibility.");
    }
    private void reply(String value) {
        transcript.setText("JIYA: " + value); setStatus("SYSTEM ONLINE");
        if (tts != null) tts.speak(value, TextToSpeech.QUEUE_FLUSH, null, "jiya_reply");
    }
    private void setStatus(String value) { status.setText(value); }
    private boolean contains(String text, String... options) {
        for (String option : options) if (text.contains(option)) return true;
        return false;
    }
    private TextView label(String value, int size, int color) {
        TextView v = new TextView(this); v.setText(value); v.setTextSize(size);
        v.setTextColor(color); v.setGravity(Gravity.CENTER_VERTICAL); return v;
    }
    private Button neonButton(String value, int color) {
        Button b = new Button(this); b.setText(value); b.setTextColor(Color.BLACK);
        b.setTextSize(13); b.setBackgroundColor(color); return b;
    }
    private LinearLayout.LayoutParams params(int height) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, height);
        p.setMargins(4, 4, 4, 4); return p;
    }
    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
