package nl_mario;

import engine.core.MarioLevelModel;
import java.util.Random;

public class FlowStrategy implements GeneratorStrategy {

    private final Random random = new Random();

    // Official characters
    private static final char GROUND = MarioLevelModel.GROUND;
    private static final char BRICK = MarioLevelModel.NORMAL_BRICK;
    private static final char PLATFORM = MarioLevelModel.PLATFORM; // Jump through platform
    private static final char COIN = MarioLevelModel.COIN;
    private static final char ENEMY = MarioLevelModel.GOOMBA;
    private static final char PIPE = MarioLevelModel.PIPE;
    private static final char CANNON = MarioLevelModel.BULLET_BILL;

    @Override
    public void generate(MarioLevelModel model, LevelConfig config) {
        int width = model.getWidth();
        int floorHeight = model.getHeight() - 4;

        model.clearMap();

        // 1. INITIAL SAFE ZONE
        buildFloor(model, 0, 10, floorHeight);
        model.setBlock(1, floorHeight - 1, MarioLevelModel.MARIO_START);

        int currentX = 10;
        int currentFloor = floorHeight;

        // 2. MAIN GENERATION LOOP
        while (currentX < width - 10) {

            // A. Decide if we add a GAP
            // In Flow, gaps are for running jumps, not for stopping.
            if (random.nextDouble() < config.difficulty * 0.4) {
                int gapSize = 2 + random.nextInt(3 + (int)(config.difficulty * 2)); // width from 2 to 5

                // Put coin arc over the gap (Visual guide)
                drawCoinArc(model, currentX, gapSize, currentFloor - 2);

                currentX += gapSize; // Move forward leaving air
            }

            // B. Choose TERRAIN PATTERN
            // Instead of random ground, we pick a level "piece"
            double roll = random.nextDouble();
            int length = 0;

            if (roll < 0.2 && config.difficulty > 0.3) {
                // 20% -> Pipe Zone (Rhythmic jumps)
                length = buildPipeHurdles(model, currentX, currentFloor);
            } else if (roll < 0.4) {
                // 20% -> Sky Bridge - Fast upper route
                length = buildSkyBridge(model, currentX, currentFloor, config);
            } else if (roll < 0.5) {
                // 10% -> Cannon Zone
                length = buildCannonRun(model, currentX, currentFloor);
            } else {
                // 50% -> Standard Ground with Enemies (Classic Flow)
                length = 4 + random.nextInt(6);

                // Smooth height variation (Hills)
                if ("HIGH".equalsIgnoreCase(config.verticality) && random.nextDouble() < 0.4) {
                    int change = random.nextBoolean() ? -2 : 2;
                    currentFloor = Math.max(8, Math.min(model.getHeight() - 4, currentFloor + change));
                } else {
                    // Try to go back to base floor
                    if (currentFloor != floorHeight && random.nextDouble() < 0.5) currentFloor = floorHeight;
                }

                buildFloor(model, currentX, currentX + length, currentFloor);
                decorateClassic(model, currentX, length, currentFloor, config);
            }

            currentX += length;
        }

        // 3. FINAL ZONE
        buildFloor(model, width - 10, width, floorHeight);
        model.setBlock(width - 2, floorHeight - 1, MarioLevelModel.MARIO_EXIT);
    }

    // --- PATTERN BUILDER METHODS ---

    // Generates floating blocks in the air. If you are fast you go up.
    private int buildSkyBridge(MarioLevelModel model, int x, int y, LevelConfig config) {
        int length = 6 + random.nextInt(5); // 6 to 10 blocks

        // 1. Base floor (Slow/safe route)
        buildFloor(model, x, x + length, y);

        // 2. Air route (Fast route)
        // Height: 4 blocks up (perfect jump)
        int skyY = y - 4;

        for (int i = 1; i < length - 1; i++) {
            // Use pass-through platforms or bricks
            char block = (random.nextDouble() < 0.5) ? PLATFORM : BRICK;
            model.setBlock(x + i, skyY, block);

            // Coins or reward up there to motivate
            if (random.nextDouble() < 0.3) model.setBlock(x + i, skyY - 1, COIN);
        }

        // Enemy below to punish the slow player
        if (config.enemyDensity > 0.4) {
            model.setBlock(x + length / 2, y - 1, ENEMY);
        }

        return length;
    }

    // Generates 2 or 3 pipes in a row to jump without stopping
    private int buildPipeHurdles(MarioLevelModel model, int x, int y) {
        int pipes = 2 + random.nextInt(2); // 2 or 3 pipes
        int spacing = 3; // Space between them
        int totalLen = pipes * spacing + 2;

        buildFloor(model, x, x + totalLen, y);

        for (int i = 0; i < pipes; i++) {
            int px = x + 2 + (i * spacing);
            int pHeight = 2; // Low pipes to keep the rhythm

            // Draw pipe
            model.setBlock(px, y - 1, PIPE);
            model.setBlock(px + 1, y - 1, PIPE);
            model.setBlock(px, y - 2, PIPE);
            model.setBlock(px + 1, y - 2, PIPE);

            // Piranha Plant (optional, breaks flow, better empty for speed)
        }
        return totalLen;
    }

    // Flat zone with Bullet Bills shooting
    private int buildCannonRun(MarioLevelModel model, int x, int y) {
        int length = 8;
        buildFloor(model, x, x + length, y);

        // A cannon at the end or start
        int cannonX = x + 4;
        model.setBlock(cannonX, y - 1, CANNON);
        model.setBlock(cannonX, y - 2, CANNON); // Low height to force a jump

        return length;
    }

    // --- UTILS AND DECORATION ---

    private void decorateClassic(MarioLevelModel model, int startX, int length, int y, LevelConfig config) {
        for (int i = 1; i < length - 1; i++) {
            int cx = startX + i;

            // Enemies (Spaced out to avoid too many)
            if (random.nextDouble() < config.enemyDensity * 0.25) {
                // Koopas (k) are better for Flow because you can kick them
                char enemy = (random.nextBoolean()) ? 'k' : ENEMY;
                model.setBlock(cx, y - 1, enemy);
            }

            // ? Blocks (PowerUps)
            if (random.nextDouble() < 0.1) {
                model.setBlock(cx, y - 4, MarioLevelModel.SPECIAL_QUESTION_BLOCK);
            }
        }
    }

    // Draw coins in an arc over a gap
    private void drawCoinArc(MarioLevelModel model, int startX, int gapSize, int startY) {
        // Simple visual logic
        if (gapSize <= 2) {
            model.setBlock(startX + 1, startY - 1, COIN);
        } else if (gapSize == 3) {
            model.setBlock(startX + 1, startY - 1, COIN);
            model.setBlock(startX + 2, startY - 1, COIN);
        } else {
            // Arc for big gaps
            model.setBlock(startX + 1, startY - 1, COIN);
            model.setBlock(startX + 2, startY - 2, COIN); // The highest one
            model.setBlock(startX + 3, startY - 1, COIN);
        }
    }

    private void buildFloor(MarioLevelModel model, int start, int end, int y) {
        for (int x = start; x < end; x++) {
            if (x < model.getWidth()) {
                model.setBlock(x, y, GROUND);
                for (int dy = 1; y + dy < model.getHeight(); dy++) {
                    model.setBlock(x, y + dy, GROUND);
                }
            }
        }
    }
}