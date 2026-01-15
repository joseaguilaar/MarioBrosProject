package nl_mario;

import engine.core.MarioLevelModel;
import java.util.Random;

public class FlowStrategy implements GeneratorStrategy {

    private final Random random = new Random();

    @Override
    public void generate(MarioLevelModel model, LevelConfig config) {
        // Dimensiones típicas
        int width = model.getWidth();
        int floorHeight = model.getHeight() - 4; // El suelo está en la altura 12 aprox

        // 1. Limpiar el mapa (todo aire)
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                model.setBlock(x, y, MarioLevelModel.EMPTY);
            }
        }

        // 2. Crear plataformas de inicio y fin seguras
        createSafeZone(model, 0, 10, floorHeight);
        createSafeZone(model, width - 10, width, floorHeight);

        // 3. El Cursor Constructivo
        int currentX = 10;
        int currentHeight = floorHeight;

        while (currentX < width - 10) {
            // A. Decidir si hacemos un hueco (Gap)
            // La dificultad aumenta la probabilidad de huecos
            if (random.nextDouble() < config.difficulty * 0.5) {
                int gapSize = 2 + random.nextInt((int)(config.difficulty * 3) + 1); // Huecos de 2 a 5 bloques
                currentX += gapSize; // Saltamos el hueco (dejamos aire)
            }

            // B. Decidir longitud de la siguiente plataforma
            int platformLength = 3 + random.nextInt(6);

            // C. Decidir altura (Verticalidad)
            // Si verticality es HIGH, cambiamos la altura más a menudo
            if ("HIGH".equalsIgnoreCase(config.verticality) && random.nextDouble() < 0.6) {
                int change = random.nextBoolean() ? -2 : 2; // Subir o bajar
                currentHeight += change;
                // Mantener dentro de límites razonables
                currentHeight = Math.max(8, Math.min(model.getHeight() - 2, currentHeight));
            } else {
                // Si es LOW, tendemos a volver al suelo base
                if (currentHeight != floorHeight && random.nextDouble() < 0.5) {
                    currentHeight = floorHeight;
                }
            }

            // D. Construir la plataforma
            int endX = Math.min(currentX + platformLength, width - 10);
            for (int x = currentX; x < endX; x++) {
                // Suelo sólido
                model.setBlock(x, currentHeight, MarioLevelModel.GROUND);
                // Relleno por debajo para que no flote feo (opcional)
                for (int y = currentHeight + 1; y < model.getHeight(); y++) {
                    model.setBlock(x, y, MarioLevelModel.GROUND);
                }

                // E. Decoración (Enemigos y Monedas)
                decorate(model, x, currentHeight, config);
            }

            currentX = endX;
        }

        // Colocar la meta
        model.setBlock(width - 2, floorHeight - 3, MarioLevelModel.MARIO_EXIT);
    }

    private void createSafeZone(MarioLevelModel model, int start, int end, int height) {
        for (int x = start; x < end; x++) {
            for (int y = height; y < model.getHeight(); y++) {
                model.setBlock(x, y, MarioLevelModel.GROUND);
            }
        }
    }

    private void decorate(MarioLevelModel model, int x, int groundY, LevelConfig config) {
        // Enemigos
        if (random.nextDouble() < config.enemyDensity * 0.3) { // 0.3 es un factor de balanceo
            // Elegir tipo de enemigo (Goomba es el básico)
            char enemy = MarioLevelModel.GOOMBA;
            if (config.difficulty > 0.6 && random.nextDouble() < 0.3) {
                enemy = MarioLevelModel.GREEN_KOOPA; // Más difícil
            }
            model.setBlock(x, groundY - 1, enemy);
            return; // Si hay enemigo, no ponemos moneda en el mismo sitio
        }

        // Monedas
        if (random.nextDouble() < config.coinDensity * 0.4) {
            // Moneda flotando un poco arriba
            model.setBlock(x, groundY - 3, MarioLevelModel.COIN);
        }
    }
}