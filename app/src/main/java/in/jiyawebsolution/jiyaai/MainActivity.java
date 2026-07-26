package in.jiyawebsolution.jiyaai;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ScrollView;
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
    private TextView status, transcript, languageBadge;
    private HoloCoreView core;
    private TextToSpeech tts;
    private AiClient ai;
    private SharedPreferences prefs;
    private String uiLanguage;
    private int uiThemeIndex;
    private ThemeCatalog.Theme theme;

    private final ActivityResultLauncher<Intent> speechLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                core.setListening(false);
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) handle(matches.get(0));
                } else setStatus(words("ONLINE", "অনলাইন", "ऑनलाइन"));
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = SecurePrefs.get(this);
        uiLanguage = language();
        uiThemeIndex = prefs.getInt("theme_index", 0);
        theme = ThemeCatalog.get(uiThemeIndex);
        ai = new AiClient(this);
        tts = new TextToSpeech(this, code -> {
            if (code == TextToSpeech.SUCCESS) applyVoiceLanguage();
        });
        buildUi();
    }

    @Override protected void onResume() {
        super.onResume();
        if (uiLanguage != null && (!uiLanguage.equals(language())
                || uiThemeIndex != prefs.getInt("theme_index", 0))) recreate();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(theme.background);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(14), dp(18), dp(24));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = label("JIYA AI", 27, 0xFFF4F7FC);
        brand.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams brandParams = new LinearLayout.LayoutParams(0, dp(58), 1);
        top.addView(brand, brandParams);

        TextView shield = chip("●  " + words("SECURE", "সুরক্ষিত", "सुरक्षित"), theme.success);
        top.addView(shield, new LinearLayout.LayoutParams(-2, dp(36)));
        root.addView(top);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(dp(12), dp(10), dp(12), dp(14));
        hero.setBackground(UiKit.outlined(theme.panel, withAlpha(theme.primary, 105), theme.radius, this));

        LinearLayout heroMeta = new LinearLayout(this);
        heroMeta.setGravity(Gravity.CENTER_VERTICAL);
        status = chip(words("ONLINE", "অনলাইন", "ऑनलाइन"), theme.primary);
        languageBadge = chip(theme.name, theme.accent);
        heroMeta.addView(status, new LinearLayout.LayoutParams(0, dp(34), 1));
        heroMeta.addView(languageBadge, new LinearLayout.LayoutParams(-2, dp(34)));
        hero.addView(heroMeta, new LinearLayout.LayoutParams(-1, dp(40)));

        core = new HoloCoreView(this);
        core.setTheme(theme);
        core.setOnClickListener(v -> startListening());
        hero.addView(core, new LinearLayout.LayoutParams(-1, dp(310)));

        TextView coreTitle = label(words("NEURAL VOICE CORE", "নিউরাল ভয়েস কোর", "न्यूरल वॉइस कोर"),
                12, theme.primary);
        coreTitle.setGravity(Gravity.CENTER);
        coreTitle.setLetterSpacing(.12f);
        hero.addView(coreTitle, new LinearLayout.LayoutParams(-1, dp(32)));
        root.addView(hero, blockParams(8));

        LinearLayout responseCard = new LinearLayout(this);
        responseCard.setOrientation(LinearLayout.VERTICAL);
        responseCard.setPadding(dp(16), dp(12), dp(16), dp(14));
        responseCard.setBackground(UiKit.outlined(theme.panel, withAlpha(theme.accent, 80), theme.radius, this));
        TextView responseTitle = label(words("JIYA RESPONSE", "জিয়া উত্তর", "जिया उत्तर"), 11, theme.accent);
        responseTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        responseCard.addView(responseTitle, new LinearLayout.LayoutParams(-1, dp(27)));
        transcript = label(words("Ready when you are, Boss. Tap the core and speak.",
                "প্রস্তুত আছি, Boss। কোরে ট্যাপ করে বলুন।",
                "मैं तैयार हूँ, Boss। कोर पर टैप करके बोलिए।"), 16, 0xFFF4F7FC);
        transcript.setGravity(Gravity.CENTER_VERTICAL);
        transcript.setMaxLines(5);
        responseCard.addView(transcript, new LinearLayout.LayoutParams(-1, -2));
        root.addView(responseCard, blockParams(14));

        Button talk = actionButton("✦  " + words("Talk to JIYA", "JIYA-কে বলুন", "JIYA से बात करें"),
                theme.primary, theme.background);
        talk.setOnClickListener(v -> startListening());
        root.addView(talk, buttonBlockParams(14));

        LinearLayout quick = new LinearLayout(this);
        quick.setGravity(Gravity.CENTER);
        Button settingsButton = actionButton("⚙  " + words("Settings", "সেটিংস", "सेटिंग्स"),
                theme.panelAlt, theme.primary);
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        Button stop = actionButton("■  " + words("Stop", "বন্ধ করুন", "रोकें"),
                0xFF26101A, 0xFFFF5577);
        stop.setOnClickListener(v -> stopAutomation());
        LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(0, dp(56), 1);
        left.setMargins(0, 0, dp(6), 0);
        LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(0, dp(56), 1);
        right.setMargins(dp(6), 0, 0, 0);
        quick.addView(settingsButton, left);
        quick.addView(stop, right);
        root.addView(quick, blockParams(12));

        LinearLayout stats = new LinearLayout(this);
        stats.setGravity(Gravity.CENTER);
        stats.addView(stat("VOICE", words("READY", "প্রস্তুত", "तैयार"), theme.primary),
                new LinearLayout.LayoutParams(0, dp(72), 1));
        stats.addView(stat("AI", ai.isConfigured() ? words("LINKED", "যুক্ত", "जुड़ा") :
                        words("SETUP", "সেটআপ", "सेटअप"), theme.accent),
                new LinearLayout.LayoutParams(0, dp(72), 1));
        stats.addView(stat("ACCESS", JiyaAccessibilityService.isRunning() ?
                        words("ON", "চালু", "चालू") : words("OFF", "বন্ধ", "बंद"), theme.success),
                new LinearLayout.LayoutParams(0, dp(72), 1));
        root.addView(stats, blockParams(12));

        TextView footer = label("PRIVATE • ENCRYPTED • " + theme.name.toUpperCase(Locale.ROOT) + "  |  v1.2",
                10, 0xFF8D9AAF);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, new LinearLayout.LayoutParams(-1, dp(44)));
        setContentView(scroll);
    }

    private void startListening() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 30);
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLocale());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                words("Speak now, Boss…", "Boss, বলুন…", "Boss, बोलिए…"));
        try {
            core.setListening(true);
            setStatus(words("LISTENING", "শুনছি", "सुन रही हूँ"));
            speechLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Install Google Speech Services", Toast.LENGTH_LONG).show();
        }
    }

    private void handle(String command) {
        transcript.setText(words("YOU: ", "আপনি: ", "आप: ") + command);
        String value = command.toLowerCase(Locale.ROOT);
        if (contains(value, "সেটিং", "setting", "सेटिंग")) {
            confirm(words("Open system settings?", "System settings খুলব?", "System settings खोलूँ?"),
                    () -> startActivity(new Intent(Settings.ACTION_SETTINGS)));
        } else if (contains(value, "ক্যামেরা", "camera", "कैमरा")) {
            confirm(words("Open camera?", "Camera খুলব?", "Camera खोलूँ?"),
                    () -> startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)));
        } else if (contains(value, "হোয়াটসঅ্যাপ", "whatsapp", "व्हाट्सऐप")) {
            confirm(words("Open WhatsApp?", "WhatsApp খুলব?", "WhatsApp खोलूँ?"),
                    () -> openPackage("com.whatsapp"));
        } else if (contains(value, "ইউটিউব", "youtube", "यूट्यूब")) {
            confirm(words("Open YouTube?", "YouTube খুলব?", "YouTube खोलूँ?"),
                    () -> openPackage("com.google.android.youtube"));
        } else if (contains(value, "বাড়ি", "home", "होम")) {
            if (!JiyaAccessibilityService.goHome()) accessibilityHint();
        } else if (contains(value, "পিছনে", "back", "पीछे")) {
            if (!JiyaAccessibilityService.goBack()) accessibilityHint();
        } else if (contains(value, "রিসেন্ট", "recent", "रीसेंट")) {
            if (!JiyaAccessibilityService.openRecents()) accessibilityHint();
        } else if (contains(value, "কল কর", "call", "कॉल")) {
            String number = value.replaceAll("[^0-9+]", "");
            if (number.length() >= 7) confirm(words("Prepare call to ", "Call প্রস্তুত করব: ", "Call तैयार करूँ: ") + number + "?",
                    () -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number))));
            else reply(words("Tell me the phone number. I will open the dial screen for your confirmation.",
                    "ফোন নম্বরটি বলুন। আপনার confirmation-এর জন্য dial screen খুলব।",
                    "फ़ोन नंबर बताइए। Confirmation के लिए dial screen खोलूँगी।"));
        } else askAi(command);
    }

    private void askAi(String command) {
        if (!ai.isConfigured()) {
            reply(words("Open Settings and add your AI provider and API key.",
                    "Settings-এ AI provider এবং API key যোগ করুন।",
                    "Settings में AI provider और API key जोड़ें।"));
            return;
        }
        setStatus(words("THINKING", "ভাবছি", "सोच रही हूँ"));
        ai.ask(command, (answer, error) -> runOnUiThread(() -> {
            if (error != null) reply("Connection problem: " + error.getMessage());
            else reply(answer);
        }));
    }

    private void confirm(String question, Runnable action) {
        new AlertDialog.Builder(this)
                .setTitle("JIYA AI")
                .setMessage(question)
                .setNegativeButton(words("Cancel", "বাতিল", "रद्द करें"), null)
                .setPositiveButton(words("Allow once", "একবার অনুমতি", "एक बार अनुमति"),
                        (dialog, which) -> {
                            try {
                                action.run();
                                reply(words("Done, Boss.", "হয়ে গেছে, Boss।", "हो गया, Boss।"));
                            } catch (Exception e) {
                                reply(words("I could not complete that action.",
                                        "কাজটি সম্পন্ন করা যায়নি।", "यह काम पूरा नहीं हो सका।"));
                            }
                        }).show();
    }

    private void stopAutomation() {
        if (tts != null) tts.stop();
        core.setListening(false);
        transcript.setText(words("All active JIYA actions have stopped.",
                "JIYA-এর সব চলমান কাজ বন্ধ হয়েছে।",
                "JIYA की सभी चल रही कार्रवाइयाँ रुक गई हैं।"));
        setStatus(words("SAFE STOP", "নিরাপদ বন্ধ", "सुरक्षित बंद"));
    }

    private void openPackage(String packageName) {
        Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch == null) throw new IllegalStateException("App not installed");
        startActivity(launch);
    }

    private void accessibilityHint() {
        reply(words("Navigation access is off. Enable JIYA AI in Accessibility settings.",
                "Navigation access বন্ধ। Accessibility settings-এ JIYA AI চালু করুন।",
                "Navigation access बंद है। Accessibility settings में JIYA AI चालू करें।"));
    }

    private void reply(String value) {
        transcript.setText("JIYA: " + value);
        setStatus(words("ONLINE", "অনলাইন", "ऑनलाइन"));
        if (tts != null) tts.speak(value, TextToSpeech.QUEUE_FLUSH, null, "jiya_reply");
    }

    private LinearLayout stat(String title, String value, int color) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setBackground(UiKit.outlined(theme.panel, withAlpha(theme.primary, 55),
                Math.max(3, theme.radius - 5), this));
        TextView first = label(title, 9, 0xFF8D9AAF);
        first.setGravity(Gravity.CENTER);
        TextView second = label(value, 11, color);
        second.setGravity(Gravity.CENTER);
        second.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        box.addView(first, new LinearLayout.LayoutParams(-1, dp(28)));
        box.addView(second, new LinearLayout.LayoutParams(-1, dp(26)));
        LinearLayout.LayoutParams margin = new LinearLayout.LayoutParams(-1, -1);
        margin.setMargins(dp(3), 0, dp(3), 0);
        box.setLayoutParams(margin);
        return box;
    }

    private TextView chip(String value, int color) {
        TextView view = label(value, 11, color);
        view.setGravity(Gravity.CENTER);
        view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        view.setBackground(UiKit.outlined(theme.panelAlt, withAlpha(color, 160),
                Math.max(8, theme.radius), this));
        view.setPadding(dp(13), 0, dp(13), 0);
        return view;
    }

    private Button actionButton(String value, int background, int text) {
        Button button = new Button(this);
        button.setText(value);
        UiKit.styleButton(button, background, text, this);
        UiKit.pressEffect(button);
        return button;
    }

    private TextView label(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    private LinearLayout.LayoutParams blockParams(int top) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(top), 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams buttonBlockParams(int top) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(58));
        params.setMargins(0, dp(top), 0, 0);
        return params;
    }

    private void setStatus(String value) { status.setText(value); }
    private boolean contains(String value, String... options) {
        for (String option : options) if (value.contains(option)) return true;
        return false;
    }

    private String language() { return prefs.getString("language", "English"); }
    private String words(String english, String bengali, String hindi) {
        if ("বাংলা".equals(language())) return bengali;
        if ("हिन्दी".equals(language())) return hindi;
        return english;
    }
    private String speechLocale() {
        if ("বাংলা".equals(language())) return "bn-IN";
        if ("हिन्दी".equals(language())) return "hi-IN";
        return "en-IN";
    }
    private int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (Math.max(0, Math.min(255, alpha)) << 24);
    }
    private void applyVoiceLanguage() {
        Locale locale = "বাংলা".equals(language()) ? new Locale("bn", "IN")
                : "हिन्दी".equals(language()) ? new Locale("hi", "IN")
                : new Locale("en", "IN");
        tts.setLanguage(locale);
        tts.setPitch(1.08f);
        tts.setSpeechRate(.94f);
    }
    private int dp(int value) { return UiKit.dp(this, value); }

    @Override protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
