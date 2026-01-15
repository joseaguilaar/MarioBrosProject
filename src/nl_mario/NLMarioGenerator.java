package levelGenerators;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;
import nl_mario.AiClient;
import nl_mario.FlowStrategy;
import nl_mario.GeneratorStrategy;
import nl_mario.LevelConfig;

public class NLMarioGenerator implements MarioLevelGenerator {

    private final AiClient aiClient;
    private LevelConfig currentConfig;
    // Texto por defecto por si no se especifica nada
    private String userPrompt = "A standard mario level";

    public NLMarioGenerator() {
        this.aiClient = new AiClient();
        // Configuración por defecto inicial
        this.currentConfig = new LevelConfig();
        this.currentConfig.generationStrategy = "FLOW";
    }

    // Método para inyectar el prompt desde fuera (lo usaremos en el Main)
    public void setUserPrompt(String prompt) {
        this.userPrompt = prompt;
        System.out.println(">> Consultando a Gemini para: \"" + prompt + "\"...");
        this.currentConfig = aiClient.getLevelParameters(prompt);
        System.out.println(">> Configuración recibida: " + currentConfig.toString());
    }

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {
        // 1. Seleccionar Estrategia
        GeneratorStrategy strategy;

        if ("PUZZLE".equalsIgnoreCase(currentConfig.generationStrategy)) {
            // strategy = new MazeStrategy(); // AÚN NO CREADA, usaremos Flow por ahora como fallback
            System.out.println("Strategy PUZZLE seleccionada (Usando Flow temporalmente hasta implementar Maze)");
            strategy = new FlowStrategy();
        } else {
            strategy = new FlowStrategy();
        }

        // 2. Ejecutar generación
        strategy.generate(model, currentConfig);

        // 3. Devolver el nivel en formato String (el framework lo pide así)
        return model.getMap();
    }

    @Override
    public String getGeneratorName() {
        return "NL-MarioGenerator";
    }
}