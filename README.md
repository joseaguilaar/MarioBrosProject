# Project Report: NL-Mario
### Natural Language Driven Hybrid Level Generation for Super Mario Bros

**Team:** Jose Aguilar (Leader), Jose Carral
**Date:** 05.02.2026

---

## 1. Introduction and Justification

Making video game levels automatically is usually so difficult. On one hand, you have the "old school" way (using code and algorithms), which creates levels that work perfectly but is very boring to use because you have to tweak a thousand numbers. On the other hand, you have modern AI (like ChatGPT or Gemini), which is very creative but often makes broken levels that are impossible to play.

**The Problem:**
For a game designer or a casual player, having to manually change settings like `coins_density` or `enemy_density = 0.8` is tedious and confusing. Ideally, you just want to ask for what you want. But if you ask an LLM directly to "Make a Mario level," it often creates geometric nonsense or puts blocks in places where Mario can't even move.

**Our Solution:**
**NL-Mario** is a hybrid system that combines the best of both worlds. We use Google Gemini as a "translator" to understand what the user wants in plain English (e.g., *"I want a hard puzzle dungeon"*). Then, instead of letting the AI draw the level itself, our system uses that information to control a reliable code generator. This ensures the level matches with description but is also 100% playable and bug-free.

## 2. Background and Literature

Our project is built on top of the **Mario AI Framework**, which is the standard tool used in this course and in many research papers for testing Mario AI.

We combined three main ideas to make this work:

* **LLMs as "Translators" (Not Builders):**
    We decided not to ask the AI (Gemini) to build the level block by block, because LLMs often make mistakes with geometry. Instead, we use the AI to **understand the player's intent**. We read that "hybrid" systems (combining AI text understanding with solid code generation) usually work better than just letting the AI do everything random.

* **Metroidvania Logic (Graph-Based):**
    For our "MazeMaster" strategy, we took inspiration from the logic used in games like *Metroid* or *Zelda*. We used a simple **Graph-Based** approach. Basically, this ensures that the level structure always places the "Key" (the Mushroom) in an accessible area *before* the player reaches the "Lock" (the wall). This guarantees the puzzle is solvable.

* ***The A* Agent (Robin Baumgarten):**
    To test our levels, we employed the **Robin Baumgarten Agent**. This is the standard "bot" used in the Mario AI community. We used it initially to check if our levels were passable, and later we realized it was a great tool to benchmark how "confusing" a level is for a machine versus a human.

## 3. Methodology

Our system operates on a four-stage pipeline:

### 3.1. Intent Extraction 
The user inputs a prompt (e.g., *"Make a hard maze with a lock mechanism"*). We send this to **Google Gemini**, instructing it to map the fuzzy description into a structured JSON `LevelConfig` object containing:
* **Strategy:** `FLOW` (Platforming) vs. `PUZZLE` (Logic).
* **Difficulty:** 0.0 to 1.0.
* **Verticality:** `LOW` vs. `HIGH`.
* **Decorations/Enemy Density:** Numeric values.

### 3.2. Structural Generation
Based on the extracted Strategy, the system selects one of two algorithms:

1.  **FlowStrategy:** Focuses on rhythm. It generates linear chunks with dynamic gap sizes calculated based on Mario's jump physics. It ensures smooth traversing.
2.  **MazeStrategy:** A graph-based approach (`Start -> Key -> Obstacle -> Lock -> Goal`).
    * **The Key:** We implemented a "Shrine" chunk that forces the player to collect a Mushroom (Power-Up).
    * **The Lock:** We designed a specific geometry (an inverted "L" shape of bricks blocking a wall) that acts as a physical gate. It exploits the game mechanic that *only Big Mario can break bricks*. If the player loses the Mushroom (takes damage) before reaching the wall, the level becomes impassable.

### 3.3. Chunk Assembly
Levels are not generated block-by-block (which is error-prone) but assembled using pre-designed, parametrically adjustable "Chunks" (e.g., `buildTower`, `buildGap`, `buildCombatZone`). This ensures local coherence while maintaining global variety.

### 3.4. Selective Optimization (Evolution of Approach)
Initially, we planned to use the A* Agent (Robin Baumgarten) to validate *all* levels. However, we encountered a significant finding during development:
* **Failed Attempt:** The A* agent consistently failed `MazeStrategy` levels.
* **Analysis:** The A* algorithm optimizes for short-term survival and speed. It lacks "long-term memory" or planning. It does not understand that it *must* keep the Mushroom to break a wall 100 blocks later. It would intentionally take damage to save time, rendering the level impossible.
* **Refined Approach:** We implemented **Selective Optimization**. The system now detects the strategy:
    * **If FLOW:** The Agent validates and repairs jumps (since it's good at platforming).
    * **If PUZZLE:** Automated validation is disabled to preserve the logical trap, relying on human intelligence for completion.

## 4. Experiments and Results

We conducted qualitative testing to verify the distinctiveness of the generated levels.

### Experiment A: Prompt Responsiveness
| User Prompt | Resulting Configuration | Visual Result |
| :--- | :--- | :--- |
| *"A fast level to run"* | Strategy: FLOW, Verticality: LOW, Enemies: Low | Flat terrain, wide gaps, few obstacles. |
| *"A vertical dungeon"* | Strategy: MAZE, Verticality: HIGH, Difficulty: High | High stone towers, required backtracking. |

### Experiment B: The Lock-and-Key Mechanic
We verified the `MazeStrategy` logic manually.
1.  **Scenario 1:** Player collects Mushroom -> avoids enemies -> reaches Wall -> breaks bricks -> **Success**.
2.  **Scenario 2:** Player collects Mushroom -> hits enemy (becomes Small) -> reaches Wall -> cannot break bricks -> **Soft-lock (Intended Failure)**.
*Result:* This confirms the generator successfully implements logical gameplay constraints via geometry, not just random tiles.

## 5. How to Run the Code

1.  **Prerequisites:** Java Development Kit (JDK).
2.  **API Key:** You need a valid Google Gemini API Key.
    * Open `src/nl_mario/AiClient.java`.
    * Replace the placeholder string with your key: `private static final String API_KEY = "YOUR_KEY_HERE";`.
3.  **Execution:**
    * Run the `PlayNLMario.java` file.
    * The console will ask: `Describe the level you want to play:`.
    * Type your description (e.g., *"I want a hard puzzle maze"*).
    * The game window will launch automatically.

If the API fails or returns Error 429, the system includes a fail-safe mode that generates a default level based on keywords).

## 6. Conclusions and Future Work

### Overview
We successfully delivered a working **Text-to-Level** system. The architecture is robust, utilizing the Strategy Pattern to easily swap between linear generation and graph-based puzzle generation. The integration with Gemini allows for fuzzy, informal inputs to result in concrete, playable game stages.

### Differences from Initial Plan
1.  **Provider Switch:** We switched from OpenAI to Google Gemini due to cost/accessibility constraints. This required rewriting the API client but maintained functionality.
2.  **Conversational Co-Creation:** The initial proposal included an iterative loop where the user could refine the level ("Make it harder") repeatedly. While the architecture supports regenerating levels, a full "context-aware conversation" (where the AI remembers the *previous* level's map) proved too complex for the timeframe. We focused instead on perfecting the "One-Shot" generation quality and the Lock-and-Key logic.
3.  **Agent Logic:** We underestimated the difficulty A* agents have with puzzle logic. This led to the interesting discovery that "unpassable by AI" does not mean "broken level" in the context of puzzles.

### Future Work
* **Smarter Agents:** Implementing a GOAP (Goal-Oriented Action Planning) agent that understands inventory/power-up management to validate Puzzle levels.
* **Grammar-Based Expansion:** Replacing the hard-coded chunks with a formal grammar (L-System) to allow for even more varied structures.
