package nl_mario;

import engine.core.MarioLevelModel;
import java.util.Random;

public class FlowStrategy implements GeneratorStrategy {

    private final Random random = new Random();

    @Override
    public void generate(MarioLevelModel model, LevelConfig config) {
        // Typical dimensions
        int width = model.getWidth();
        int floorHeight = model.getHeight() - 4; // The floor is at height 12 approx

        // 1. Clear the map (all air)
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                model.setBlock(x, y, MarioLevelModel.EMPTY);
            }
        }

        // 2. Create safe start and end platforms
        createSafeZone(model, 0, 10, floorHeight);
        createSafeZone(model, width - 10, width, floorHeight);

        // 3. The Constructive Cursor
        int currentX = 10;
        int currentHeight = floorHeight;

        while (currentX < width - 10) {
            // A. Decide if we make a gap
            // Difficulty increases the probability of gaps
            if (random.nextDouble() < config.difficulty * 0.5) {
                int gapSize = 2 + random.nextInt((int)(config.difficulty * 3) + 1); // Gaps from 2 to 5 blocks
                currentX += gapSize; // Jump the gap (leave air)
            }

            // B. Decide length of the next platform
            int platformLength = 3 + random.nextInt(6);

            // C. Decide height (Verticality)
            // If verticality is HIGH, change height more often
            if ("HIGH".equalsIgnoreCase(config.verticality) && random.nextDouble() < 0.6) {
                int change = random.nextBoolean() ? -2 : 2; // Go up or down
                currentHeight += change;
                // Keep within reasonable limits
                currentHeight = Math.max(8, Math.min(model.getHeight() - 2, currentHeight));
            } else {
                // If LOW, tend to return to base ground
                if (currentHeight != floorHeight && random.nextDouble() < 0.5) {
                    currentHeight = floorHeight;
                }
            }

            // D. Build the platform
            int endX = Math.min(currentX + platformLength, width - 10);
            for (int x = currentX; x < endX; x++) {
                // Solid ground
                model.setBlock(x, currentHeight, MarioLevelModel.GROUND);
                // Fill underneath so it doesn't float awkwardly
                for (int y = currentHeight + 1; y < model.getHeight(); y++) {
                    model.setBlock(x, y, MarioLevelModel.GROUND);
                }

                // E. Enemies and Coins
                decorate(model, x, currentHeight, config);
            }

            currentX = endX;
        }

        // Place the exit
        model.setBlock(width - 2, floorHeight - 3, MarioLevelModel.MARIO_EXIT);
    }

    private void createSafeZone(MarioLevelModel model, int start, int end, int height) {
        for (int x = start; x < end; x++) {
            for (int y = height; y < model.getHeight(); y++) {
                model.setBlock(x, y, MarioLevelModel.GROUND);
            }
        }
    }

    private void decorate(MarioLevelModel model, int x, int groundY, LevelConfig config) {
        // Enemies
        if (random.nextDouble() < config.enemyDensity * 0.3) { // 0.3 is a balancing factor
            // Choose enemy type
            char enemy = MarioLevelModel.GOOMBA;
            if (config.difficulty > 0.6 && random.nextDouble() < 0.3) {
                enemy = MarioLevelModel.GREEN_KOOPA; // Harder
            }
            model.setBlock(x, groundY - 1, enemy);
            return; // If there is an enemy, don't put a coin in the same spot
        }

        // Coins
        if (random.nextDouble() < config.coinDensity * 0.4) {
            // Coin floating a bit above
            model.setBlock(x, groundY - 3, MarioLevelModel.COIN);
        }
    }
}