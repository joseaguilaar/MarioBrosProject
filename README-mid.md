# README-MID.md

**Project Title:** NL-Mario: Natural Language Driven Hybrid Level Generation
**Team:** Jose Aguilar (Leader), Jose Carral
**Date:** 19.01.2026

## 1. Progress Overview
At this mid-term stage, we have made an initial implementation of the project's **Core Pipeline**. The system is now capable of receiving
a natural language prompt from the user, translating it into technical parameters using a generative AI, generating
a playable Super Mario Bros level using the first of our planned algorithmic strategies ("FlowRunner").

The structural foundations (Strategy Pattern) and the AI integration are fully functional, providing a stable base
to implement the remaining complexity in the second half of the project.

NOTE: to execute you have to create and google gemini API key in this link: https://aistudio.google.com/api-keys,  or just copy and paste
the key that I will send you maybe by email. As I can not upload the key tu github without them blocking me that key.
You have to add it in the AiClient.java file, changing "add_api_key_here" for the key.


## 2. What was already done

### A. AI Integration and Intent Extraction
* **Implemented Module:** `AiClient.java` and `LevelConfig.java`.
    * **Functionality:** We have established a secure connection with the **Google Gemini API**. The system correctly analyzes
    * the user's informal text (e.g., "I want a difficult level with many enemies") and extracts the standard JSON parameters
    * defined in our initial plan:
        * `generationStrategy` (Flow/Puzzle)
        * `difficulty`, `enemyDensity`, `coinDensity`
        * `verticality`, `puzzleComplexity`

### B. Constructive Generators (Strategy A)
* **Strategy Pattern:** We created the `GeneratorStrategy.java` interface to allow dynamic switching between generation algorithms.
    * **Flow Strategy Implemented:** The `FlowStrategy.java` class, although we still need to improve it to make levels more entertaining, currently
    * is fully operational:
        * Adjusts gap sizes and platform heights dynamically according to the parameters extracted by the AI (`difficulty` and `verticality`).
        * Places decoration and enemies according to the requested density.

### C. Architecture and Integration
* **Framework Integration:** The `NLMarioGenerator.java` class successfully bridges our custom logic with the standard *Mario AI Framework*.
    * **Interactive Testing:** We implemented `PlayNLMario.java` to allow immediate testing of the "text-to-level" flow by human users.

## 3. Changes from the Initial Plan

* **AI Provider Switch:** We migrated from OpenAI to **Google Gemini** as it allowed us to create an API key for free.

## 4. Most Important Things Left to Do

### A. Strategy B: "The MazeMaster"
The most immediate task is implementing `MazeStrategy.java`. Unlike the linear Flow strategy, this will require developing
a sort of graph-based logic to handle "Lock-and-Key" mechanics and backtracking structures
using chunks (level fragments).

### B. Search-Based Optimization (The "Repair" Phase)
We need to implement the `LevelRepairer` module using the RobinBaumgarten Agent. This module will validate the generated levels
and correct and improve them automatically.

### C. The Comparative Experiment
We will implement a "Pure LLM" baseline mode. This involves asking the AI to generate the level grid
directly (in ASCII) to compare its success rate against our Hybrid method in the final evaluation.