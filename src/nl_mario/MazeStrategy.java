package nl_mario;

import engine.core.MarioLevelModel;
import java.util.Random;

public class MazeStrategy implements GeneratorStrategy {

    private final Random random = new Random();

    @Override
    public void generate(MarioLevelModel model, LevelConfig config) {
        System.out.println("--- INICIANDO GENERACIÓN DE MAZE ---"); // <--- CHIVATO 1
        int width = model.getWidth();
        int height = model.getHeight();

        // 1. Llenar todo de aire primero
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                model.setBlock(x, y, MarioLevelModel.EMPTY);
            }
        }

        // 2. Definir el suelo base
        int floorY = height - 2;
        createFloor(model, 0, width, floorY);

        // 3. GENERACIÓN DE "HABITACIONES" (CHUNKS)
        int currentX = 10;

        int loopSafety = 0; // <--- PROTECCIÓN CONTRA BUCLES INFINITOS

        while (currentX < width - 15) {
            loopSafety++;
            if (loopSafety > 1000) { // Si llevamos 1000 vueltas, algo va mal
                System.err.println("¡SOCORRO! Bucle infinito detectado. Saliendo a la fuerza.");
                break;
            }

            System.out.println("Generando habitación en X: " + currentX); // <--- CHIVATO 2

            int roomWidth = 8 + random.nextInt(10);

            // ... (resto de tu código de habitaciones) ...
            if (config.puzzleComplexity > 1 && random.nextDouble() < 0.6) {
                buildComplexRoom(model, currentX, roomWidth, floorY, config);
            } else {
                buildSimpleRoom(model, currentX, roomWidth, floorY, config);
            }

            // ... (código de muros) ...

            currentX += roomWidth + 1;
        }

        System.out.println("--- GENERACIÓN COMPLETADA ---"); // <--- CHIVATO 3
        model.setBlock(width - 2, floorY - 1, MarioLevelModel.MARIO_EXIT);
    }

    // Crea suelo plano
    private void createFloor(MarioLevelModel model, int start, int end, int yLevel) {
        for (int x = start; x < end; x++) {
            model.setBlock(x, yLevel, MarioLevelModel.GROUND);
        }
    }

    // Habitación simple: suelo y algún enemigo
    private void buildSimpleRoom(MarioLevelModel model, int xStart, int width, int floorY, LevelConfig config) {
        // Probabilidad de enemigos según config
        for (int x = xStart; x < xStart + width; x++) {
            if (random.nextDouble() < config.enemyDensity) {
                model.setBlock(x, floorY - 1, MarioLevelModel.GOOMBA);
            }
        }
    }

    // Habitación compleja: Plataformas en el aire (Verticalidad)
    private void buildComplexRoom(MarioLevelModel model, int xStart, int width, int floorY, LevelConfig config) {
        int platforms = 1 + random.nextInt(3); // 1 a 3 plataformas

        for (int i = 0; i < platforms; i++) {
            int platX = xStart + random.nextInt(width - 2);
            int platY = floorY - 4 - (i * 3); // Subiendo altura

            // Dibujar plataforma de 3 bloques
            if (platY > 2 && platX + 3 < model.getWidth()) {
                model.setBlock(platX, platY, MarioLevelModel.NORMAL_BRICK);
                model.setBlock(platX + 1, platY, MarioLevelModel.NORMAL_BRICK);
                model.setBlock(platX + 2, platY, MarioLevelModel.NORMAL_BRICK);

                // Poner moneda o enemigo encima
                if (random.nextDouble() < config.coinDensity) {
                    model.setBlock(platX + 1, platY - 1, MarioLevelModel.COIN);
                } else if (random.nextDouble() < config.enemyDensity) {
                    model.setBlock(platX + 1, platY - 1, MarioLevelModel.RED_KOOPA);
                }
            }
        }
    }
}