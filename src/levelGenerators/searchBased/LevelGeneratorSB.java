package levelGenerators.searchBased;

import java.util.ArrayList;
import java.util.List;
import java.util.*;

import engine.core.*;
import engine.helper.GameStatus;

public class LevelGeneratorSB implements MarioLevelGenerator {

    private static final int POPULATION_SIZE = 20;
    private static final int GENERATIONS = 15;

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {

        String objetiveFunction = "1";
        if (model.getWidth() == 149 ) {
            objetiveFunction = "2";
        }
        else if( model.getWidth() == 148 ) {
            objetiveFunction = "3";
        }
        return (getGeneratedLevelSB(new MarioLevelModel(150, 16), timer, objetiveFunction));
    }

    public String getGeneratedLevelSB(MarioLevelModel model, MarioTimer timer, String objetiveFunction) {
        // Create a list for the population
        List<String> population = new ArrayList<>();
        MarioLevelGenerator generator = new levelGenerators.constructive.LevelGenerator();

        //generate the initial levels with the constructive generator
        for(int i = 0; i < POPULATION_SIZE; i++) {
            String nivelInicial = generator.getGeneratedLevel(model, timer);
            population.add(nivelInicial);
        }

        //Search-Based Algorithm
        String bestLevel = runEvolutionaryAlgorithm(population, timer, objetiveFunction);

        // best level found
        return bestLevel;
    }

    public String getGeneratorName() {
        return "search-based";
    }

    private List<Integer> evaluationFunction(List<String> population, String objetiveFunction ){
        List<Integer> resultados = new ArrayList<>();

        for ( String level : population ) {

            // Run level
            MarioGame game = new MarioGame();

            if (objetiveFunction.equals("1")) {  //task1
                MarioResult results = game.runGame(new agents.robinBaumgarten.Agent(), level, 30, 0, false);

                double fitness = ((results.getGameStatus() == GameStatus.WIN ? 5000 : 0) + results.getKillsTotal()*3 + results.getCurrentCoins() + results.getNumDestroyedBricks()*2);
                resultados.add((int) fitness);

            }
            else{ //task2 or 3

                //I added multi-execution for the same level, as killer is a non-deterministic agent, so I will se the average of the results
                List<MarioResult> resultsK = new ArrayList<>();
                for( int i=0; i<5; i++) {
                    resultsK.add(game.runGame(new agents.killer.Agent(), level, 30, 0, false));
                }

                Boolean always_winK = true;
                double totalKillsk = 0;
                for(int i=0; i<5; i++) {
                    if( resultsK.get(i).getGameStatus() != GameStatus.WIN && always_winK) {
                        always_winK = false;
                    }
                    totalKillsk += resultsK.get(i).getKillsTotal();
                }

                double average_killsk = totalKillsk/5.0;

                if (objetiveFunction.equals("2")) { //task2
                    double fitnessK = ((always_winK) ? 5000 : 0) + average_killsk*10.0;
                    resultados.add((int) fitnessK);
                }
                else { //task3

                    //I added multi-execution for the same level, as collector is a non-deterministic agent, so I will see the average of the results
                    List<MarioResult> resultsC = new ArrayList<>();
                    for( int i=0; i<5; i++) {
                        resultsC.add(game.runGame(new agents.collector.Agent(), level, 30, 0, false));
                    }

                    Boolean always_winC = true;
                    double totalKillsc = 0;
                    double totalCoinsc = 0;
                    for(int i=0; i<5; i++) {
                        if( resultsC.get(i).getGameStatus() != GameStatus.WIN && always_winC) {
                            always_winC = false;
                        }
                        totalKillsc += resultsC.get(i).getKillsTotal();
                        totalCoinsc += resultsC.get(i).getCurrentCoins();
                    }

                    double average_killsc = totalKillsc/5.0;
                    double average_coinsc = totalCoinsc/5.0;

                    double passBonus = 0;
                    if (always_winK && always_winC ) {
                        passBonus = 10000;
                    } else if (always_winK || always_winC) {
                        passBonus = 2000;
                    }

                    double fitnessMulti = passBonus + (average_killsc * 10.0) + (average_coinsc * 10.0);
                    resultados.add((int) fitnessMulti);
                }
            }
        }

        return resultados;
    }

    private String runEvolutionaryAlgorithm(List<String> population, MarioTimer timer, String objetiveFunction) {
        int currentGeneration = 0;
        Random random = new Random();
        double mutationProb = 0.15; // 15% probability of mutating a block

        while (currentGeneration < GENERATIONS ) {

            // A. Evaluation
            List<Integer> resultados = evaluationFunction(population, objetiveFunction);

            // -- Print statistics for charts (Professor's requirement) --
            int currentBest = Collections.max(resultados);
            double currentAvg = resultados.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            System.out.println("Gen " + currentGeneration + " | Best: " + currentBest + " | Avg: " + currentAvg);

            // B. Selection (Keep the 3 best)
            List<String> newPopulation = new ArrayList<>();
            List<String> top3Levels = new ArrayList<>();

            // Temporary copy of results to avoid destroying the original if needed
            List<Integer> tempResultados = new ArrayList<>(resultados);

            for (int k = 0; k < 3; k++) {
                int maxValor = Integer.MIN_VALUE;
                int bestIndex = -1;
                for (int i = 0; i < tempResultados.size(); i++) {
                    if (tempResultados.get(i) > maxValor) {
                        maxValor = tempResultados.get(i);
                        bestIndex = i;
                    }
                }
                if (bestIndex != -1) {
                    top3Levels.add(population.get(bestIndex));
                    newPopulation.add(population.get(bestIndex)); // Elitism: pass directly
                    tempResultados.set(bestIndex, Integer.MIN_VALUE);
                }
            }

            // C. Reproduction (Fill the rest of the population up to POPULATION_SIZE)
            while (newPopulation.size() < POPULATION_SIZE) {
                // Select 2 parents randomly from the top 3 (Simple tournament)
                String parent1 = top3Levels.get(random.nextInt(top3Levels.size()));
                String parent2 = top3Levels.get(random.nextInt(top3Levels.size()));

                // CROSSOVER
                String child = crossover(parent1, parent2);
                // MUTATION
                child = mutate(child, mutationProb);
                newPopulation.add(child);
            }

            // Update the population and advance
            population = newPopulation;
            currentGeneration++;
        }

        // Return the best of the last population (re-evaluate or keep the best historical)
        return population.get(0);
    }

    //---------------------------
    //CROSSOVER AND MUTATION

    // 1. One-point crossover (Cuts the map vertically and joins the halves)
    private String crossover(String parent1, String parent2) {
        String[] rows1 = parent1.split("\n");
        String[] rows2 = parent2.split("\n");

        // Safety: if they have different heights, return one as is
        if (rows1.length != rows2.length) return parent1;

        int width = rows1[0].length();
        int cutPoint = new Random().nextInt(width - 2) + 1; // Cut at an intermediate point

        StringBuilder child = new StringBuilder();
        for (int i = 0; i < rows1.length; i++) {
            // Left part of parent 1 + Right part of parent 2
            String left = rows1[i].substring(0, cutPoint);
            String right = rows2[i].substring(cutPoint);
            child.append(left).append(right).append("\n");
        }
        return child.toString();
    }

    // 2. Safe mutation (Avoids breaking pipes)
//    private String mutate(String level, double probability) {
//        char[] tiles = level.toCharArray();
//        Random rnd = new Random();
//
//        for (int i = 0; i < tiles.length; i++) {
//            if (rnd.nextDouble() < probability) {
//                // Only mutate safe things to maintain "Visual Correctness"
//                // Do not touch pipes, base ground, or cannons.
//
//                // Mutation 1: Add/Remove Enemies (g = goomba) in the air (-)
//                if (tiles[i] == '-') {
//                    if (rnd.nextDouble() < 0.05) tiles[i] = 'g'; // Add enemy (low probability)
//                    else if (rnd.nextDouble() < 0.02) tiles[i] = 'o'; // Add coin
//                }
//                else if (tiles[i] == 'g' || tiles[i] == 'r' || tiles[i] == 'k') {
//                    tiles[i] = '-'; // Delete enemy
//                }
//
//                // Mutation 2: Change breakable blocks (S) for ? (?) or coins (o)
//                else if (tiles[i] == 'S') { // S is usually Brick Block
//                    if (rnd.nextDouble() < 0.1) tiles[i] = '?';
//                }
//                else if (tiles[i] == '?') {
//                    if (rnd.nextDouble() < 0.1) tiles[i] = 'S';
//                }
//            }
//        }
//        return new String(tiles);
//    }

    private String mutate(String level, double probability) {
        String[] rows = level.split("\n");
        char[][] map = new char[rows.length][];
        for(int i=0; i<rows.length; i++) map[i] = rows[i].toCharArray();

        Random rnd = new Random();
        int height = map.length;
        int width = map[0].length;

        for (int y = 0; y < height - 1; y++) { // -1 to not touch the last line if it is a border
            for (int x = 0; x < width; x++) {

                if (rnd.nextDouble() < probability) {
                    char current = map[y][x];

                    // 1. Enemy Mutation (DIVERSITY + PHYSICS)
                    // Only add enemy if it is air and THERE IS GROUND BELOW
                    if (current == '-') {
                        // Check y+1 (ground). Avoid placing floating enemies.
                        if (y + 1 < height && isSolid(map[y+1][x])) {
                            double roll = rnd.nextDouble();
                            if (roll < 0.02) map[y][x] = 'g'; // Goomba
                            else if (roll < 0.03) map[y][x] = 'k'; // Green Koopa
                            else if (roll < 0.035) map[y][x] = 'r'; // Red Koopa
                        }
                        // Floating coins are allowed
                        else if (rnd.nextDouble() < 0.01) {
                            map[y][x] = 'o'; // Coin
                        }
                    }

                    // 2. Delete existing enemies (to avoid saturation)
                    else if (isEnemy(current)) {
                        if(rnd.nextDouble() < 0.3) map[y][x] = '-';
                    }

                    // 3. Block Mutation (Type exchange)
                    else if (current == 'S') { // Brick
                        if (rnd.nextDouble() < 0.2) map[y][x] = '?'; // Question
                    }
                    else if (current == '?') {
                        if (rnd.nextDouble() < 0.2) map[y][x] = 'S'; // Brick
                    }
                }
            }
        }

        // Reconstruct String
        StringBuilder sb = new StringBuilder();
        for(char[] row : map) {
            sb.append(row).append("\n");
        }
        return sb.toString();
    }

    // Helpers for safe mutation
    private boolean isSolid(char c) {
        // X=Ground, #=Pyramid, S=Brick, ?=Question, t=Pipe, B=CannonBase
        return "X#S?t%B".indexOf(c) != -1;
    }

    private boolean isEnemy(char c) {
        return "gkr".indexOf(c) != -1;
    }

}