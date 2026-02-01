package nl_mario;

import engine.core.MarioGame;
import engine.core.MarioLevelModel;
import engine.core.MarioResult;
import engine.core.MarioAgent;
import engine.helper.GameStatus;

public class LevelRepairer {

    // We will attempt to fix the level up to 5 times
    private static final int MAX_ATTEMPTS = 5;

    public void repairLevel(MarioLevelModel model) {
        System.out.println("   [Optimization] Starting validation with A* Agent...");

        MarioGame game = new MarioGame();
        // We use the best available agent for validation
        MarioAgent agent = new agents.robinBaumgarten.Agent();

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            // 1. Simulate game
            // We limit the time to 200 ticks of simulated gameplay
            MarioResult result = game.runGame(agent, model.getMap(), 200, 0, false);

            // 2. Check result
            if (result.getGameStatus() == GameStatus.WIN) {
                System.out.println("   [Optimization] ✅ The level is PASSABLE. Iteration: " + (i+1));
                return; // Work done
            }

            // 3. If lost, calculate where (approximately)
            float percent = result.getCompletionPercentage();
            int failedX = (int) (percent * model.getWidth());

            System.out.println("   [Optimization] Failure detected at X=" + failedX + " (" + (int)(percent*100) + "%). Applying patch...");

            // 4. APPLY REPAIR (Mutation)
            // Strategy: Build a safe bridge in the failure zone
            fixZone(model, failedX);
        }

        System.out.println("   [Optimization]  Attempt limit reached. The level might be too difficult.");
    }

    // Creates safe ground around the death zone
    private void fixZone(MarioLevelModel model, int centerX) {
        int startX = Math.max(0, centerX - 2);
        int endX = Math.min(model.getWidth(), centerX + 4);
        int floorY = model.getHeight() - 2;

        for (int x = startX; x < endX; x++) {
            // Fill gaps with solid ground
            model.setBlock(x, floorY, MarioLevelModel.GROUND);
            model.setBlock(x, floorY + 1, MarioLevelModel.GROUND);

            // Remove enemies or obstacles right above that might block the path
            model.setBlock(x, floorY - 1, MarioLevelModel.EMPTY);
            model.setBlock(x, floorY - 2, MarioLevelModel.EMPTY);
        }
    }
}