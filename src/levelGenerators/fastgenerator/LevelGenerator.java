package levelGenerators.fastgenerator;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

import java.util.Random;

public class LevelGenerator implements MarioLevelGenerator {

    private static final int GROUND_Y = 13;            // player stands at y=GROUND_Y-1
    private static final int START_WIDTH = 6;

    //enemies
    private static final int PACK_MIN = 5;
    private static final int PACK_MAX = 6;
    private static final int PACK_SPACING_MIN = 7;     // tiles between packs
    private static final int PACK_SPACING_MAX = 8;
    private static final int ENEMIES_MIN_TOTAL = 120;   // ensure >=20 hazards comfortably

    //coins
    private static final int COIN_BLOCK_MIN_W = 4;
    private static final int COIN_BLOCK_MAX_W = 8;
    private static final int COIN_BLOCK_MIN_H = 1;
    private static final int COIN_BLOCK_MAX_H = 3;
    private static final int COIN_BLOCK_GAP_MIN = 3;   // horizontal gap between coin blocks
    private static final int COIN_BLOCK_GAP_MAX = 8;

    private final Random rnd = new Random();

    private int enemiesCount = 0;
    private int coinsPlaced = 0;

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {
        model.clearMap();
        layFlatGround(model);
        placeStart(model);
        placeGoal(model);

        placeEnemies(model);
        placeCoins(model);

        return model.getMap();
    }

    @Override
    public String getGeneratorName() {
        return "FastGenerator";
    }

    // ------------------------------------------------------------
    // ground, start and goal
    // ------------------------------------------------------------
    private void layFlatGround(MarioLevelModel m) {
        for (int x = 0; x < m.getWidth(); x++) {
            for (int y = GROUND_Y; y < m.getHeight(); y++) {
                m.setBlock(x, y, MarioLevelModel.GROUND);
            }
        }
    }

    private void placeStart(MarioLevelModel m) {
        m.setBlock(1, GROUND_Y - 1, MarioLevelModel.MARIO_START);
    }

    private void placeGoal(MarioLevelModel m) {
        int gx = m.getWidth() - 2;
        m.setBlock(gx, GROUND_Y - 1, MarioLevelModel.MARIO_EXIT);
    }

    // ------------------------------------------------------------
    //enemies
    // ------------------------------------------------------------
    private void placeEnemies(MarioLevelModel m) {
        enemiesCount = 0;

        int x = START_WIDTH + 4;
        int rightLimit = m.getWidth() - 3;

        while (x < rightLimit && enemiesCount < ENEMIES_MIN_TOTAL) {
            // spacing before next pack
            x += rndRange(PACK_SPACING_MIN, PACK_SPACING_MAX);
            if (x >= rightLimit) break;

            //poner un bloque antes de enemigos para que el collector no se los encuentre de frente
            m.setBlock(x, GROUND_Y-1, MarioLevelModel.PYRAMID_BLOCK);
            x+=1;

            int pack = rndRange(PACK_MIN, PACK_MAX);
            int placed = 0;
            int px = x;

            while (placed < pack && px < rightLimit) {
                m.setBlock(px, GROUND_Y-1, MarioLevelModel.GOOMBA);
                enemiesCount++;
                placed++;
                px += 1;
            }
            x = px;
        }
    }


    // ------------------------------------------------------------
    // coin blocks (rectangules) from 1–3 tiles of the ground
    // ------------------------------------------------------------
    private void placeCoins(MarioLevelModel m) {
        coinsPlaced = 0;

        int x = START_WIDTH + 6;
        int rightLimit = m.getWidth() - 6;

        while (x < rightLimit) {
            x += rndRange(COIN_BLOCK_GAP_MIN, COIN_BLOCK_GAP_MAX);
            if (x >= rightLimit) break;

            int w = rndRange(COIN_BLOCK_MIN_W, COIN_BLOCK_MAX_W);
            int h = rndRange(COIN_BLOCK_MIN_H, COIN_BLOCK_MAX_H);
            int yTop = GROUND_Y - rndRange(1, 3); // de 1 a 3 por encima del suelo

            //dibuja bloque w×h de monedas, evitando chocar con enemigos ya puestos
            for (int dx = 0; dx < w; dx++) {
                for (int dy = 0; dy < h; dy++) {
                    int cx = x + dx;
                    int cy = yTop - dy;
                    if (! (x >= 0 && cx < m.getWidth() && cy >= 0 && cy < m.getHeight())  )continue;  // is not inside the maps
                    if (m.getBlock(cx, cy) == MarioLevelModel.EMPTY) {  //that position is empty
                            m.setBlock(cx, cy, MarioLevelModel.COIN);
                            coinsPlaced++;
                    }
                }
            }
            x += w;
        }
    }

    //random
    private int rndRange(int a, int b) {
        return a + rnd.nextInt(b - a + 1);
    }
}
