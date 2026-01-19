package nl_mario;

import com.google.gson.annotations.SerializedName;

public class LevelConfig {
    // Define the fields we expect to receive from the AI

    @SerializedName("generationStrategy")
    public String generationStrategy; // "FLOW" or "PUZZLE"

    @SerializedName("difficulty")
    public double difficulty; // 0.0 to 1.0

    @SerializedName("enemyDensity")
    public double enemyDensity; // 0.0 to 1.0

    @SerializedName("coinDensity")
    public double coinDensity; // 0.0 to 1.0

    @SerializedName("puzzleComplexity")
    public int puzzleComplexity; // 1 or 2

    @SerializedName("verticality")
    public String verticality; // "LOW" or "HIGH"

    // Empty constructor
    public LevelConfig() {}

    // toString method to verify it works
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