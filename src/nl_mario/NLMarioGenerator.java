package levelGenerators;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;
import nl_mario.AiClient;
import nl_mario.FlowStrategy;
import nl_mario.MazeStrategy;
import nl_mario.GeneratorStrategy;
import nl_mario.LevelConfig;
import nl_mario.LevelRepairer;

public class NLMarioGenerator implements MarioLevelGenerator {

    private final AiClient aiClient;
    private LevelConfig currentConfig;
    // Default text in case nothing is specified
    private String userPrompt = "A standard mario level";

    public NLMarioGenerator() {
        this.aiClient = new AiClient();
        // Initial default configuration
        this.currentConfig = new LevelConfig();
        this.currentConfig.generationStrategy = "FLOW";
    }

    // Method to inject the prompt from outside (we will use it in Main)
    public void setUserPrompt(String prompt) {
        this.userPrompt = prompt;
        System.out.println(">> Consulting Gemini for: \"" + prompt + "\"...");
        this.currentConfig = aiClient.getLevelParameters(prompt);
        System.out.println(">> Configuration received: " + currentConfig.toString());
    }

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {
        // 1. Select Strategy
        GeneratorStrategy strategy;

        if ("PUZZLE".equalsIgnoreCase(currentConfig.generationStrategy)) {
            System.out.println(">> Generating PUZZLE mode (MazeStrategy)...");
            strategy = new MazeStrategy();
        } else {
            System.out.println(">> Generating FLOW mode (FlowStrategy)...");
            strategy = new FlowStrategy();
        }

        // 2. Execute generation
        strategy.generate(model, currentConfig);

        // 3. OPTIMIZATION (Search-Based Repair)
        // We only execute it if the level is complex to ensure passability
        if (currentConfig.difficulty > 0.3 && "FLOW".equalsIgnoreCase(currentConfig.generationStrategy)) {
            LevelRepairer repairer = new LevelRepairer();
            repairer.repairLevel(model);
        }

        // 4. Return the level in String format (the framework requires it)
        return model.getMap();
    }
    @Override
    public String getGeneratorName() {
        return "NL-MarioGenerator";
    }
}