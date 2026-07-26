package in.jiyawebsolution.jiyaai;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class AiClient {
    public interface Callback { void done(String answer, Exception error); }

    private final SharedPreferences prefs;
    public AiClient(Context context) { prefs = SecurePrefs.get(context); }

    public boolean isConfigured() {
        return !prefs.getString("api_key", "").trim().isEmpty();
    }

    public void ask(String userText, Callback callback) {
        new Thread(() -> {
            try { callback.done(request(userText), null); }
            catch (Exception e) { callback.done(null, e); }
        }).start();
    }

    private String request(String text) throws Exception {
        String provider = prefs.getString("provider", "OpenAI");
        String key = prefs.getString("api_key", "").trim();
        String model = prefs.getString("model", defaultModel(provider)).trim();
        if (key.isEmpty()) throw new IllegalStateException("Add an API key in Settings");
        if ("Gemini".equals(provider)) return gemini(key, model, text);
        if ("Claude".equals(provider)) return claude(key, model, text);
        if ("Custom".equals(provider)) return custom(key, model, text);
        return openAi(key, model, text);
    }

    private String openAi(String key, String model, String text) throws Exception {
        JSONObject body = new JSONObject()
                .put("model", model)
                .put("input", "You are JIYA AI, a warm, witty female phone assistant. Reply briefly in the user's language. Never request passwords, OTPs, banking PINs or private credentials.\nUser: " + text);
        JSONObject json = post("https://api.openai.com/v1/responses", body,
                "Authorization", "Bearer " + key);
        if (json.has("output_text")) return json.optString("output_text");
        JSONArray output = json.optJSONArray("output");
        if (output != null) for (int i = 0; i < output.length(); i++) {
            JSONArray content = output.getJSONObject(i).optJSONArray("content");
            if (content != null) for (int j = 0; j < content.length(); j++) {
                String value = content.getJSONObject(j).optString("text");
                if (!value.isEmpty()) return value;
            }
        }
        throw new IllegalStateException("The AI returned no readable text");
    }

    private String gemini(String key, String model, String text) throws Exception {
        JSONObject part = new JSONObject().put("text",
                "You are JIYA AI, a warm and witty female phone assistant. Reply briefly in the user's language. User: " + text);
        JSONObject body = new JSONObject().put("contents",
                new JSONArray().put(new JSONObject().put("parts", new JSONArray().put(part))));
        JSONObject json = post("https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + key, body, null, null);
        return json.getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
    }

    private String claude(String key, String model, String text) throws Exception {
        JSONObject body = new JSONObject().put("model", model).put("max_tokens", 500)
                .put("system", "You are JIYA AI, a warm, witty female phone assistant. Reply briefly in the user's language.")
                .put("messages", new JSONArray().put(
                        new JSONObject().put("role", "user").put("content", text)));
        JSONObject json = post("https://api.anthropic.com/v1/messages", body,
                "x-api-key", key, "anthropic-version", "2023-06-01");
        return json.getJSONArray("content").getJSONObject(0).getString("text");
    }

    private String custom(String key, String model, String text) throws Exception {
        String endpoint = prefs.getString("endpoint", "").trim();
        if (!endpoint.startsWith("https://")) throw new IllegalStateException("Custom endpoint must use HTTPS");
        JSONObject body = new JSONObject().put("model", model)
                .put("messages", new JSONArray().put(new JSONObject()
                        .put("role", "user").put("content", text)));
        JSONObject json = post(endpoint, body, "Authorization", "Bearer " + key);
        return json.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content");
    }

    private JSONObject post(String endpoint, JSONObject body, String... headers) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(45000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        for (int i = 0; i + 1 < headers.length; i += 2)
            if (headers[i] != null) connection.setRequestProperty(headers[i], headers[i + 1]);
        try (OutputStream out = connection.getOutputStream()) {
            out.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int code = connection.getResponseCode();
        InputStream stream = code >= 200 && code < 300
                ? connection.getInputStream() : connection.getErrorStream();
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line; while ((line = reader.readLine()) != null) response.append(line);
        }
        if (code < 200 || code >= 300)
            throw new IllegalStateException("API error " + code + ": " + response);
        return new JSONObject(response.toString());
    }

    public static String defaultModel(String provider) {
        if ("Gemini".equals(provider)) return "gemini-2.5-flash";
        if ("Claude".equals(provider)) return "claude-sonnet-4-20250514";
        if ("Custom".equals(provider)) return "your-model";
        return "gpt-5.4";
    }
}
