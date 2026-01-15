package nl_mario;

import java.util.Scanner;

public class TestConnection {
    public static void main(String[] args) {
        AiClient client = new AiClient();
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- NL-Mario Generator ---");
        System.out.println("Describe tu nivel ideal (ej: 'Quiero un laberinto super difícil lleno de enemigos'):");

        String input = scanner.nextLine();

        System.out.println("Consultando a la IA...");
        LevelConfig config = client.getLevelParameters(input);

        System.out.println("\n--- Parámetros Generados ---");
        System.out.println(config.toString());

        // Aquí es donde llamarías a tu generador en el futuro:
        // LevelGenerator generator = new LevelGenerator(config);
        // generator.createLevel();
    }
}