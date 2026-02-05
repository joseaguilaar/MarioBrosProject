package nl_mario;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import okhttp3.*;

import java.io.IOException;

public class AiClient {
    // Google Gemini key (API KEY)
    private static final String API_KEY = "ADD_API_KEY_HERE";

    // We use Gemini 2.5 Flash which is fast and supports JSON mode
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    private final OkHttpClient client;
    private final Gson gson;

    public AiClient() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public LevelConfig getLevelParameters(String userPrompt) {
        String systemPrompt = "You are a Mario Bros level design assistant. " +
                "Translate the user's natural language description into a JSON configuration. " +
                "Strictly follow this schema:\n" +
                "- generationStrategy: 'FLOW' (for speed/rhythm) or 'PUZZLE' (for exploration/keys).\n" +
                "- difficulty: 0.0 to 1.0.\n" +
                "- enemyDensity: 0.0 to 1.0.\n" +
                "- coinDensity: 0.0 to 1.0.\n" +
                "- verticality: 'LOW' (flat) or 'HIGH' (hilly).\n" +
                "User request: '" + userPrompt + "'\n" +
                "Output ONLY valid JSON parameters.";

        // 2. Build the specific JSON for GEMINI
        JsonObject jsonBody = new JsonObject();

        // contents part
        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject partObj = new JsonObject();
        partObj.addProperty("text", systemPrompt);
        parts.add(partObj);
        contentObj.add("parts", parts);
        contents.add(contentObj);
        jsonBody.add("contents", contents);

        // "generationConfig" part to force JSON response, not messages
        JsonObject genConfig = new JsonObject();
        genConfig.addProperty("response_mime_type", "application/json");
        jsonBody.add("generationConfig", genConfig);

        // 3. Create the HTTP request
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        // 4. Execute and parse
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Error in Gemini API: " + response.code() + " " + response.message());
                if (response.body() != null) System.err.println(response.body().string());
                return getDefaultConfig();
            }

            String responseBody = response.body().string();

            // Parse the Gemini response
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            String contentJsonString = responseJson.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .get("content").getAsJsonObject()
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            return gson.fromJson(contentJsonString, LevelConfig.class);

        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultConfig();
        }
    }

    private LevelConfig getDefaultConfig() {
        LevelConfig config = new LevelConfig();
        config.generationStrategy = "FLOW";
        config.difficulty = 0.4;
        config.enemyDensity = 0.5;
        config.coinDensity = 0.5;
        config.verticality = "HIGH";
        return config;
    }
}
