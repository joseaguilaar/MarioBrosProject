package nl_mario;

import engine.core.MarioGame;
import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import levelGenerators.NLMarioGenerator;
import java.util.Scanner;

public class PlayNLMario {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 1. Instanciar tu generador
        NLMarioGenerator generator = new NLMarioGenerator();

        // 2. Pedir al usuario qué quiere jugar
        System.out.println("------------------------------------------------");
        System.out.println("   BIENVENIDO A NL-MARIO (AI POWERED)   ");
        System.out.println("------------------------------------------------");
        System.out.println("Describe el nivel que quieres jugar:");
        System.out.print("> ");
        String prompt = scanner.nextLine();

        // 3. Configurar el generador con la IA
        generator.setUserPrompt(prompt);

        // 4. Lanzar el juego
        System.out.println("Generando nivel y lanzando juego...");
        MarioGame game = new MarioGame();

        // Ejecutamos el juego en modo visual (true)
        // El último parámetro (200) es el límite de tiempo del temporizador de Mario
        game.runGame(new agents.robinBaumgarten.Agent(), generator.getGeneratedLevel(new MarioLevelModel(150, 16), null), 200, 0, true);
    }
}