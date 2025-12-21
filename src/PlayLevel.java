import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import engine.core.MarioGame;
import engine.core.MarioResult;

public class PlayLevel {
    public static void printResults(MarioResult result) {
        System.out.println("****************************************************************");
        System.out.println("Game Status: " + result.getGameStatus().toString() +
                " Percentage Completion: " + result.getCompletionPercentage());
        System.out.println("Lives: " + result.getCurrentLives() + " Coins: " + result.getCurrentCoins() +
                " Remaining Time: " + (int) Math.ceil(result.getRemainingTime() / 1000f));
        System.out.println("Mario State: " + result.getMarioMode() +
                " (Mushrooms: " + result.getNumCollectedMushrooms() + " Fire Flowers: " + result.getNumCollectedFireflower() + ")");
        System.out.println("Total Kills: " + result.getKillsTotal() + " (Stomps: " + result.getKillsByStomp() +
                " Fireballs: " + result.getKillsByFire() + " Shells: " + result.getKillsByShell() +
                " Falls: " + result.getKillsByFall() + ")");
        System.out.println("Bricks: " + result.getNumDestroyedBricks() + " Jumps: " + result.getNumJumps() +
                " Max X Jump: " + result.getMaxXJump() + " Max Air Time: " + result.getMaxJumpAirTime());
        System.out.println("****************************************************************");
    }

    public static String getLevel(String filepath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
        }
        return content;
    }

    public static void main(String[] args) {
        MarioGame game = new MarioGame();
        GenerateLevel generator = new GenerateLevel();
        //printResults(game.playGame(getLevel("./levels/MyMaps/lvl-5.txt"), 200, 0));

        //ASSIGNMENT 1

        //------------------------------------------------------------------------------------------
        //1
        //printResults(game.runGame(new agents.sergeyKarakovskiy.Agent(), getLevel("./levels/MyMaps/lvl-1.txt"), 20, 0, true));

        //------------------------------------------------------------------------------------------
        //2
        //printResults(game.runGame(new agents.spencerSchumann.Agent(), getLevel("./levels/MyMaps/lvl-2.txt"), 20, 0, true));
        //printResults(game.runGame(new agents.trondEllingsen.Agent(), getLevel("./levels/MyMaps/lvl-2.txt"), 20, 0, true));

        //------------------------------------------------------------------------------------------
        //3
        //printResults(game.runGame(new agents.collector.Agent(), getLevel("./levels/MyMaps/lvl-3.txt"), 20, 0, true));

        //------------------------------------------------------------------------------------------
        //4. I force the agent to hit the murshroom upgrade block, and the force him
        // to wait until the upgrade, what enables him to break a block and continue, then I do the same other two times,
        // but first forcing the agent to be attacked by any enemy, to reduce from upgraded to normal mario.
        //printResults(game.runGame(new agents.sergeyKarakovskiy.Agent(), getLevel("./levels/MyMaps/lvl-4&6.txt"), 500, 0, true));


        //------------------------------------------------------------------------------------------
        //5. To pass the level, is needed a so smart agent, or maybe a not so smart one. Because the agent must be attacked
        // by an enemy, to be able to continue. So I let me being attacked, to enable me to continue.
        printResults(game.runGame(new agents.killer.Agent(), getLevel("./levels/MyMaps/prueba.txt"), 500, 0, true));
        //printResults(game.playGame(getLevel("./levels/MyMaps/lvl-5.txt"), 200, 0));

        //------------------------------------------------------------------------------------------
        //6. Same logic as in point 5, the agent must be attacked by a green kopa, reduce to normal Mario and
        // be able to pass through the hole in the wall of just one block.
        //So sergeKarakovsky is attacked, what enable him to continue.
        //printResults(game.runGame(new agents.robinBaumgarten.Agent(), getLevel("./levels/MyMaps/lvl-4&6.txt"), 500, 0, true));
        //printResults(game.runGame(new agents.sergeyKarakovskiy.Agent(), getLevel("./levels/MyMaps/lvl-4&6.txt"), 500, 0, true));



    }
}
