package nl_mario;

import engine.core.MarioGame;
import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import levelGenerators.NLMarioGenerator;
import java.util.Scanner;

public class PlayNLMario {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 1. Instantiate the generator
        NLMarioGenerator generator = new NLMarioGenerator();

        // 2. Ask the user what they want to play
        System.out.println("------------------------------------------------");
        System.out.println("   WELCOME TO MARIO BROS GENERATOR (AI POWERED)   ");
        System.out.println("------------------------------------------------");
        System.out.println("Describe the level you want to play:");
        System.out.print("> ");
        String prompt = scanner.nextLine();

        // 3. Configure the generator with the AI
        generator.setUserPrompt(prompt);

        // 4. Launch the game
        System.out.println("Generating level and launching game...");
        MarioGame game = new MarioGame();

        // 5. Run the game in visual mode true
        game.runGame(new agents.robinBaumgarten.Agent(), generator.getGeneratedLevel(new MarioLevelModel(150, 16), null), 200, 0, true);
//        game.playGame(generator.getGeneratedLevel(new MarioLevelModel(150, 16), null), 200, 0);
    }
}