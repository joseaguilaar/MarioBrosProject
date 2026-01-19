package nl_mario;

import engine.core.MarioLevelModel;

public interface GeneratorStrategy {
    /**
     * Generates a level based on the received configuration.
     * @param model The level model (where we will write the blocks)
     * @param config The configuration that came from the AI (Gemini)
     */
    void generate(MarioLevelModel model, LevelConfig config);
}