package nl_mario;

import engine.core.MarioGame;
import engine.core.MarioLevelModel;
import engine.core.MarioResult;
import engine.core.MarioAgent;
import engine.helper.GameStatus;

public class LevelRepairer {

    private static final int MAX_ITERATIONS = 5; // Intentos máximos de reparación

    /**
     * Intenta reparar el nivel simulando partidas con un agente.
     * @param model El modelo del nivel generado.
     * @return true si el nivel es jugable, false si sigue fallando tras los intentos.
     */
    public boolean repairLevel(MarioLevelModel model) {
        MarioGame game = new MarioGame();
        // Usamos el agente RobinBaumgarten (A*) porque es el mejor jugando
        MarioAgent agent = new agents.robinBaumgarten.Agent();

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            System.out.println("   [Reparador] Simulación #" + (i + 1) + "...");

            // Simular (Timer 20 fps * 200 segundos de juego aprox, depende del framework)
            // Ponemos un tiempo generoso para asegurar
            MarioResult result = game.runGame(agent, model.getMap(), 20, 0, false);

            if (result.getGameStatus() == GameStatus.WIN) {
                System.out.println("   ✅ [Reparador] El nivel es completable. ¡Listo!");
                return true;
            }

            // --- CORRECCIÓN AQUÍ ---
            // Calculamos dónde murió basándonos en el porcentaje completado
            float percent = result.getCompletionPercentage();
            int deathX = (int) (percent * model.getWidth());
            // -----------------------

            System.out.println("   ❌ [Reparador] Agente murió al " + (percent*100) + "% (Bloque X=" + deathX + ")");

            // Aplicar arreglo un poco antes de donde murió (para asegurar)
            applyFix(model, Math.max(5, deathX - 2));
        }

        System.err.println("   ⚠️ [Reparador] No se pudo arreglar al 100%.");
        return false;
    }

    private void applyFix(MarioLevelModel model, int x) {
        int width = model.getWidth();
        int height = model.getHeight();

        // Definimos la zona a reparar (donde murió el agente + un poco adelante)
        int startX = Math.max(0, x);
        int endX = Math.min(width, x + 5);

        System.out.println("      -> Construyendo puente de emergencia en X: " + startX + "-" + endX);

        // ESTRATEGIA DE REPARACIÓN BÁSICA:
        // 1. Rellenar agujeros con suelo.
        // 2. Eliminar enemigos en esa zona (por si era un salto imposible por enemigos).

        int floorY = height - 2;

        for (int currX = startX; currX < endX; currX++) {
            // Poner suelo sólido
            model.setBlock(currX, floorY, MarioLevelModel.GROUND);
            model.setBlock(currX, floorY + 1, MarioLevelModel.GROUND); // Base

            // Borrar obstáculos justo encima del suelo (limpieza de zona)
            model.setBlock(currX, floorY - 1, MarioLevelModel.EMPTY);
            model.setBlock(currX, floorY - 2, MarioLevelModel.EMPTY);
            model.setBlock(currX, floorY - 3, MarioLevelModel.EMPTY);
        }
    }
}