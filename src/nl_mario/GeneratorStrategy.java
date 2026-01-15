package nl_mario;

import engine.core.MarioLevelModel;

public interface GeneratorStrategy {
    /**
     * Genera un nivel basado en la configuración recibida.
     * @param model El modelo del nivel (donde escribiremos los bloques)
     * @param config La configuración que vino de la IA (Gemini)
     */
    void generate(MarioLevelModel model, LevelConfig config);
}