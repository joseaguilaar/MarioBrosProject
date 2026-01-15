package nl_mario;

import com.google.gson.annotations.SerializedName;

public class LevelConfig {
    // Definimos los campos que esperamos recibir de la IA

    @SerializedName("generationStrategy")
    public String generationStrategy; // "FLOW" o "PUZZLE"

    @SerializedName("difficulty")
    public double difficulty; // 0.0 a 1.0

    @SerializedName("enemyDensity")
    public double enemyDensity; // 0.0 a 1.0

    @SerializedName("coinDensity")
    public double coinDensity; // 0.0 a 1.0

    @SerializedName("puzzleComplexity")
    public int puzzleComplexity; // 1 o 2

    @SerializedName("verticality")
    public String verticality; // "LOW" o "HIGH"

    // Constructor vacío
    public LevelConfig() {}

    // Método toString para verificar que funciona
    @Override
    public String toString() {
        return "LevelConfig{" +
                "Strategy='" + generationStrategy + '\'' +
                ", Diff=" + difficulty +
                ", Enemies=" + enemyDensity +
                ", Coins=" + coinDensity +
                ", Puzzle=" + puzzleComplexity +
                ", Vertical=" + verticality +
                '}';
    }
}