package nl_mario;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import okhttp3.*;

import java.io.IOException;

public class AiClient {
    // clave de google gemini que he generado (API KEY)
    private static final String API_KEY = "AIzaSyDHDAPbsuEHB36fM6OJjj38HnWDzkvyw7w";

    // Usamos Gemini 2.5 Flash que es rápido y soporta modo JSON
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
                "- puzzleComplexity: 1 (simple) or 2 (complex).\n" +
                "- verticality: 'LOW' (flat) or 'HIGH' (hilly).\n" +
                "User request: '" + userPrompt + "'\n" +
                "Output ONLY valid JSON parameters.";

        // 2. Construimos el JSON específico para GEMINI
        JsonObject jsonBody = new JsonObject();

        // Parte "contents"
        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject partObj = new JsonObject();
        partObj.addProperty("text", systemPrompt);
        parts.add(partObj);
        contentObj.add("parts", parts);
        contents.add(contentObj);
        jsonBody.add("contents", contents);

        // Parte "generationConfig" para forzar respuesta JSON
        JsonObject genConfig = new JsonObject();
        genConfig.addProperty("response_mime_type", "application/json");
        jsonBody.add("generationConfig", genConfig);

        // 3. Crear la petición HTTP
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        // 4. Ejecutar y parsear
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Error en Gemini API: " + response.code() + " " + response.message());
                if (response.body() != null) System.err.println(response.body().string());
                return getDefaultConfig();
            }

            String responseBody = response.body().string();

            // Parsear la respuesta de Gemini (Estructura: candidates[0].content.parts[0].text)
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
        config.difficulty = 0.5;
        config.enemyDensity = 0.5;
        config.coinDensity = 0.5;
        config.puzzleComplexity = 1;
        config.verticality = "LOW";
        return config;
    }
}