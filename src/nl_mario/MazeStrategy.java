package nl_mario;

import engine.core.MarioLevelModel;
import java.util.Random;

public class MazeStrategy implements GeneratorStrategy {

    private final Random random = new Random();
    private int currentX = 0;
    private int floorY;

    // Constants for code readability
    private static final char GROUND = MarioLevelModel.GROUND;
    private static final char ROCK = MarioLevelModel.PYRAMID_BLOCK;
    private static final char BRICK = MarioLevelModel.NORMAL_BRICK;
    private static final char ENEMY = MarioLevelModel.GOOMBA;
    private static final char WINGED_ENEMY = MarioLevelModel.GOOMBA_WINGED;

    @Override
    public void generate(MarioLevelModel model, LevelConfig config) {
        model.clearMap();
        currentX = 0;
        floorY = model.getHeight() - 3; // Standard floor level

        // LEVEL GRAPH EXECUTION PLAN

        // 1. SAFE START ZONE
        buildStartChunk(model);

        // 2. THE KEY SHRINE (Objective: Get the Mushroom)
        buildKeyShrineChunk(model, config);

        // 3. THE MAZE (PART 1) - Varied Obstacles
        buildVariedMaze(model, config, 16);

        // 4. THE FIRST GATE (Lock-and-Key Puzzle)
        // Requires Super Mario (Big) to break the bricks and pass.
        buildLockChunk(model);

        // 5. THE MAZE (PART 2) - Increased Difficulty
        buildVariedMaze(model, config, 12);

        // 6. THE SECOND GATE (Final Check)
        buildLockChunk(model);

        // 7. GOAL ZONE
        buildGoalChunk(model);
    }

    // CHUNK BUILDERS (Level Fragments)

    private void buildStartChunk(MarioLevelModel model) {
        createPlatform(model, currentX, 8, floorY);
        model.setBlock(1, floorY - 1, MarioLevelModel.MARIO_START);

        // Decoration: Small entrance arch
        model.setBlock(currentX + 2, floorY - 4, ROCK);
        model.setBlock(currentX + 3, floorY - 4, ROCK);
        model.setBlock(currentX + 2, floorY - 1, BRICK); // Test block for controls

        currentX += 8;
    }

    // A stepped "Shrine" holding the Key
    private void buildKeyShrineChunk(MarioLevelModel model, LevelConfig config) {
        int length = 14;
        createPlatform(model, currentX, length, floorY);

        int centerX = currentX + 7;

        // Build a stepped pyramid structure
        // Level 1
        for(int x = centerX - 3; x <= centerX + 3; x++) model.setBlock(x, floorY - 1, ROCK);
        // Level 2
        for(int x = centerX - 2; x <= centerX + 2; x++) model.setBlock(x, floorY - 2, ROCK);
        // Level 3 (Top)
        model.setBlock(centerX, floorY - 3, ROCK);

        // THE KEY (Mushroom PowerUp) placed at the summit
        // '@' represents a Question Block with a PowerUp
        model.setBlock(centerX, floorY - 6, '@');

        // Place a Guardian if difficulty is medium/high
        if (config.difficulty > 0.3) {
            model.setBlock(centerX - 4, floorY - 1, ENEMY);
            model.setBlock(centerX + 4, floorY - 1, ENEMY);
        }

        // Oppressive ceiling
        for (int x = 0; x < 30; x++) {
            model.setBlock(currentX + x, 0, ROCK);
            if(x % 4 == 0) model.setBlock(currentX + x, 1, ROCK); // Stalactites
        }

        currentX += length;
    }

    // Intelligent Obstacle Selector based on Logic
    private void buildVariedMaze(MarioLevelModel model, LevelConfig config, int totalLength) {
        int builtLength = 0;

        while (builtLength < totalLength) {
            double roll = random.nextDouble();
            int chunkLen = 0;

            // Select obstacle type based on configuration
            if (roll < 0.4) {
                // 40% -> Standard Combat Zone (Enemies)
                chunkLen = buildFlatEnemyZone(model, config);
            } else if (roll < 0.7) {
                // 30% -> High Wall (Requires precise jumping)
                chunkLen = buildHighWall(model);
            } else {
                // 30% -> Pipes or Cannons (Complex structures)
                if (config.difficulty > 0.6) {
                    chunkLen = buildCannonZone(model);
                } else {
                    chunkLen = buildPipeZone(model);
                }
            }
            builtLength += chunkLen;
        }
    }

    // THE LOCK MECHANIC (Inverted 'L' Shape)
    private void buildLockChunk(MarioLevelModel model) {
        int length = 8;
        createPlatform(model, currentX, length, floorY);

        int wallX = currentX + 4;
        int tunnelY = floorY - 3; // Jump height

        // 1. Solid Stone Wall (Decorated as a castle gate)
        for (int y = 0; y <= floorY; y++) {
            if (y < tunnelY) { // Leave bottom empty for entry
                model.setBlock(wallX, y, ROCK);
            } else if (y > tunnelY + 2) { // Solid upper part
                model.setBlock(wallX, y, ROCK);
            }
        }
        // Decorative battlements
        model.setBlock(wallX, floorY - 6, ROCK);
        model.setBlock(wallX, floorY - 8, ROCK);

        // 2. The "Inverted L" of Breakable Bricks
        // This shape forces the player to break through.
        model.setBlock(wallX, tunnelY, BRICK);
        model.setBlock(wallX + 1, tunnelY, BRICK);
        model.setBlock(wallX + 2, tunnelY, BRICK);
        model.setBlock(wallX + 3, tunnelY, BRICK);
        model.setBlock(wallX + 4, tunnelY, BRICK);
        model.setBlock(wallX + 5, tunnelY, BRICK);

        // Final stopper to force breaking blocks vertically
        model.setBlock(wallX + 5, tunnelY + 1, ROCK);
        model.setBlock(wallX + 5, tunnelY + 2, ROCK);

        // Helper step block just in case
        model.setBlock(wallX - 2, floorY - 1, BRICK);

        currentX += length;
    }

    private void buildGoalChunk(MarioLevelModel model) {
        createPlatform(model, currentX, 10, floorY);
        // Classic End Stairs
        for(int i=0; i<4; i++) {
            for(int h=0; h<=i; h++) model.setBlock(currentX + i, floorY - 1 - h, ROCK);
        }
        model.setBlock(currentX + 8, floorY - 1, MarioLevelModel.MARIO_EXIT);
        currentX += 10;
    }

    //MODULE BUILDERS

    private int buildFlatEnemyZone(MarioLevelModel model, LevelConfig config) {
        int len = 6;
        createPlatform(model, currentX, len, floorY);
        // Spawn enemies based on density
        if (random.nextDouble() < config.enemyDensity) {
            model.setBlock(currentX + 3, floorY - 1, ENEMY);
            // Winged enemies only on high difficulty
            if (config.difficulty > 0.7) model.setBlock(currentX + 4, floorY - 4, WINGED_ENEMY);
        }
        currentX += len;
        return len;
    }

    private int buildHighWall(MarioLevelModel model) {
        int len = 6;
        // Create a gap before the wall for tension
        createPlatform(model, currentX, 2, floorY);
        createPlatform(model, currentX + 4, 2, floorY);

        // The Wall
        int wallX = currentX + 3;
        for (int y = floorY; y > floorY - 5; y--) {
            model.setBlock(wallX, y, ROCK);
        }
        // Helper step
        model.setBlock(wallX - 1, floorY - 2, BRICK);

        currentX += len;
        return len;
    }

    private int buildPipeZone(MarioLevelModel model) {
        int len = 5;
        createPlatform(model, currentX, len, floorY);
        // Simple Pipe construction
        model.setBlock(currentX + 2, floorY - 1, '<'); // Pipe Left
        model.setBlock(currentX + 3, floorY - 1, '>'); // Pipe Right
        model.setBlock(currentX + 2, floorY - 2, '['); // Pipe Top Left
        model.setBlock(currentX + 3, floorY - 2, ']'); // Pipe Top Right
        // Note: 'T' is often used for pipes with Piranha Plants in the framework

        currentX += len;
        return len;
    }

    private int buildCannonZone(MarioLevelModel model) {
        int len = 6;
        createPlatform(model, currentX, len, floorY);
        // Bullet Bill Tower
        int h = 3;
        for(int i=1; i<h; i++) model.setBlock(currentX + 3, floorY - i, 'b'); // Cannon Body
        model.setBlock(currentX + 3, floorY - h, '*'); // Cannon Head

        currentX += len;
        return len;
    }

    // Helper: Creates safe ground with padding underneath
    private void createPlatform(MarioLevelModel model, int x, int width, int y) {
        for (int i = 0; i < width; i++) {
            if (x + i < model.getWidth()) {
                model.setBlock(x + i, y, GROUND);
                // Fill visual padding below floor
                for (int j = 1; j < 4; j++) {
                    if (y + j < model.getHeight()) model.setBlock(x + i, y + j, GROUND);
                }
            }
        }
    }
}