package levelGenerators.constructive;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

import java.util.Random;

public class LevelGenerator implements MarioLevelGenerator {

    // Design constants
    private static final int GROUND_Y = 13;
    private final Random rnd = new Random();

    // Global variables for internal statistics (optional)
    private int enemiesPlaced = 0;

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {

        model.clearMap();
        enemiesPlaced = 0;

        // 1. Generate Base Terrain (Ground, Gaps, Hills)
        createTerrain(model);

        // 2. Add Structures (Pipes and Cannons)
        addStructures(model);

        // 3. Add Platforms and Floating Blocks
        addBlocksAndCoins(model);

        // 4. Add Varied Enemies
        addEnemies(model);

        // 5. Ensure Safe Start and End Zones
        cleanStartAndEnd(model);

        return model.getMap();
    }

    @Override
    public String getGeneratorName() {
        return "Constructive";
    }

    // ------------------------------------------------------------
    //TERRAIN: Flat ground, Hills and Gaps
    private void createTerrain(MarioLevelModel model) {
        int x = 0;
        while (x < model.getWidth()) {
            // Terrain change probabilities
            double roll = rnd.nextDouble();
            int length = rndRange(3, 8);

            if (x < 10 || x > model.getWidth() - 10) {
                // Start and end always flat
                fillGround(model, x, x + 1, GROUND_Y);
                x++;
            }
            else if (roll < 0.1) {
                // GAP - Max 1-2 blocks so agents can jump over
                int gapWidth = rndRange(1,2 );
                // Do not fill anything -> remains air (Gap)
                x += gapWidth;
            }
            else if (roll < 0.4) {
                // HILL (Elevation)
                int height = rndRange(2, 3); // Hill height
                int hillY = GROUND_Y - height;
                fillGround(model, x, x + length, hillY);
                // Fill under the hill to make it solid
                for(int k=x; k<x+length; k++) {
                    for(int j=hillY; j<GROUND_Y; j++) {
                        model.setBlock(k, j, MarioLevelModel.GROUND);
                    }
                }
                x += length;
            }
            else {
                // NORMAL FLAT GROUND
                fillGround(model, x, x + length, GROUND_Y);
                x += length;
            }
        }
    }

    private void fillGround(MarioLevelModel model, int xStart, int xEnd, int ySurface) {
        for (int x = xStart; x < xEnd; x++) {
            if (x >= model.getWidth()) break;
            for (int y = ySurface; y < model.getHeight(); y++) {
                model.setBlock(x, y, MarioLevelModel.GROUND);
            }
        }
    }

    // ------------------------------------------------------------
    // STRUCTURES: Pipes and Cannons
    private void addStructures(MarioLevelModel model) {
        for (int x = 10; x < model.getWidth() - 10; x++) {
            // Only place if there is solid ground at GROUND_Y
            if (isGround(model, x, GROUND_Y) && rnd.nextDouble() < 0.05) {

                if (rnd.nextDouble() < 0.8) {
                    // PIPE
                    int height = rndRange(2, 4); // Safe height
                    // Check space (width of 2)
                    if (isGround(model, x+1, GROUND_Y)) {
                        // Place pipe (Character varies by library, using general)
                        // Pipe occupies width of 2.
                        for(int h=0; h<height; h++) {
                            model.setBlock(x, GROUND_Y - 1 - h, MarioLevelModel.PIPE); // Left
                            model.setBlock(x+1, GROUND_Y - 1 - h, MarioLevelModel.PIPE); // Right
                        }
                        x += 2; // Skip occupied space
                    }
                } else {
                    // CANNON (Bullet Bill)
                    int height = rndRange(1, 3);
                    model.setBlock(x, GROUND_Y - 1 - height, MarioLevelModel.BULLET_BILL);
                    for(int h=0; h<height; h++) {
                        model.setBlock(x, GROUND_Y - 1 - h, MarioLevelModel.BULLET_BILL);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------
    // BLOCKS AND COINS
    private void addBlocksAndCoins(MarioLevelModel model) {
        for (int x = 10; x < model.getWidth() - 10; x++) {
            // Floating blocks
            if (rnd.nextDouble() < 0.4) {
                int length = rndRange(3, 5);
                int yHeight = GROUND_Y - rndRange(2,6); // Standard jump height

                for (int i = 0; i < length; i++) {
                    if (x + i >= model.getWidth()) break;

                    double typeRoll = rnd.nextDouble();
                    if (typeRoll < 0.2) model.setBlock(x+i, yHeight, MarioLevelModel.COIN_BRICK);
                    else if (typeRoll < 0.4) model.setBlock(x+i, yHeight, MarioLevelModel.COIN_HIDDEN_BLOCK);
                    else model.setBlock(x+i, yHeight, MarioLevelModel.COIN); // Pure coin

                    // Sometimes place enemy on top of the block
                    if (typeRoll < 0.4) {
                        if (rnd.nextDouble() < 0.1) {
                            model.setBlock(x + i, yHeight - 1, MarioLevelModel.GOOMBA);
                        }
                    }
                }
                x += length;
            }
        }
    }

    // ------------------------------------------------------------
    // VARIED ENEMIES
    private void addEnemies(MarioLevelModel model) {
        for (int x = 10; x < model.getWidth() - 10; x++) {
            // Only place enemy if there is ground below and it is air
            if (isGround(model, x, GROUND_Y) && model.getBlock(x, GROUND_Y-1) == MarioLevelModel.EMPTY) {

                if (rnd.nextDouble() < 0.2) { // Enemy probability
                    char enemyType;
                    double roll = rnd.nextDouble();

                    if (roll < 0.55) enemyType = MarioLevelModel.GOOMBA; // 55% Goomba
                    else if (roll < 0.8) enemyType = MarioLevelModel.GREEN_KOOPA; // 25% Green
                    else  enemyType = MarioLevelModel.RED_KOOPA; // 20% Red

                    model.setBlock(x, GROUND_Y - 1, enemyType);

                }
            }
        }
    }

    // ------------------------------------------------------------
    // UTILITIES
    // Cleans start and end zones so the agent doesn't die on spawn or arrival
    private void cleanStartAndEnd(MarioLevelModel m) {
        // Start
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < m.getHeight(); y++) {
                if (y >= GROUND_Y) m.setBlock(x, y, MarioLevelModel.GROUND);
                else m.setBlock(x, y, MarioLevelModel.EMPTY);
            }
        }
        m.setBlock(1, GROUND_Y - 1, MarioLevelModel.MARIO_START);

        // Goal
        int endX = m.getWidth() - 2;
        for (int x = endX - 5; x < m.getWidth(); x++) {
            for (int y = 0; y < m.getHeight(); y++) {
                if (y >= GROUND_Y) m.setBlock(x, y, MarioLevelModel.GROUND);
                else m.setBlock(x, y, MarioLevelModel.EMPTY);
            }
        }
        m.setBlock(endX, GROUND_Y - 1, MarioLevelModel.MARIO_EXIT);
    }

    private boolean isGround(MarioLevelModel model, int x, int y) {
        if (x < 0 || x >= model.getWidth() || y < 0 || y >= model.getHeight()) return false;
        return model.getBlock(x, y) == MarioLevelModel.GROUND;
    }

    private int rndRange(int min, int max) {
        return min + rnd.nextInt(max - min + 1);
    }
}