# Tetris Game - Coursework Documentation

## GitHub Repository
https://github.com/aleihalawa/CW2025

## Compilation Instructions

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Apache Maven 3.6 or higher
- IntelliJ IDEA (recommended IDE)
- JavaFX SDK (included in project dependencies)

### Running the Application

**Important**: The game can only be run through IntelliJ IDEA's integrated terminal using the following command:

```bash
mvn -U clean javafx:run
```

**Steps:**
1. Open the project in IntelliJ IDEA
2. Open the integrated terminal (View → Tool Windows → Terminal, or Alt+F12)
3. Navigate to the project root directory (if not already there)
4. Run the command: `mvn -U clean javafx:run`

The `-U` flag forces Maven to update dependencies, `clean` removes previous build artifacts, and `javafx:run` compiles and executes the JavaFX application.

### Dependencies
All dependencies are managed through Maven and specified in `pom.xml`. The project uses:
- JavaFX for the GUI framework
- Maven for build management

**Note**: Running the application outside of IntelliJ's terminal may result in classpath or JavaFX module path issues. The recommended and tested method is using IntelliJ's integrated terminal.

---

## Implemented and Working Properly

### 1. Game Modes
- **Classic Mode**: Standard Tetris gameplay with traditional controls and mechanics
- **Mirror Mode**: All controls are reversed (left/right swapped, rotation reversed) with visual mirroring effects
- **Power-Ups Mode (Arcade)**: Enhanced gameplay with power-ups, bedrock corruption mechanic, and faster starting speed

**Location**: `src/main/java/com/comp2042/model/GameMode.java`, `src/main/java/com/comp2042/controller/GameController.java`

### 2. Power-Up System
- **Freeze Power-Up**: Temporarily freezes the game, allowing strategic piece placement
- **Bomb Power-Up**: Click-to-target explosion that destroys blocks in a 3x3 radius with gravity animation
- **Drill Power-Up**: Destroys blocks in a column as it falls, with real-time gravity effects

**Location**: 
- Power-up logic: `src/main/java/com/comp2042/model/SimpleBoard.java`
- Power-up activation: `src/main/java/com/comp2042/controller/GameController.java`
- Power-up visuals: `src/main/java/com/comp2042/view/GuiController.java`

### 3. Bedrock Corruption Mechanic
- Rising tide system where the lowest playable row turns into indestructible bedrock every 15 seconds
- Visual danger timer showing countdown
- Bedrock cannot be destroyed by drills or bombs
- Rows containing bedrock cannot be cleared
- Game over when corruption reaches the top

**Location**: 
- Core logic: `src/main/java/com/comp2042/model/SimpleBoard.java` (lines 422-449)
- Timer management: `src/main/java/com/comp2042/controller/GameController.java` (lines 493-530)
- UI display: `src/main/java/com/comp2042/view/GuiController.java` (lines 640-656)

### 4. Gravity Animation System
- Column collapse animation for blocks above destroyed areas
- Applied after bomb explosions and drill usage
- Blocks fall row-by-row with visual cascade effect
- Respects bedrock as solid barriers

**Location**: 
- Gravity logic: `src/main/java/com/comp2042/model/SimpleBoard.java` (lines 510-551)
- Animation controller: `src/main/java/com/comp2042/controller/GameController.java` (lines 905-930)

### 5. Leaderboard System
- Separate leaderboards for each game mode (Classic, Mirror, Power-Ups)
- Mode switching interface in leaderboard panel
- Persistent storage using file serialization
- High score tracking and display

**Location**: 
- Leaderboard management: `src/main/java/com/comp2042/model/HighScoreManager.java`
- UI: `src/main/java/com/comp2042/view/LeaderboardPanel.java`

### 6. Settings Management
- Persistent settings storage (ghost mode, volume, player name, game mode)
- Settings loaded on application startup
- Settings saved automatically on changes

**Location**: `src/main/java/com/comp2042/model/GameSettings.java`

### 7. Visual Enhancements
- Cyberpunk/retro arcade color palette for bricks
- Sharp square brick shapes (no rounded corners)
- Power-up notification system
- Bedrock corruption notifications
- Freeze effect with visual feedback
- Mirror mode particle effects
- Explosion animations with particle effects
- Drill particle sparks

**Location**: `src/main/java/com/comp2042/view/GuiController.java`

### 8. Contextual Help System
- Info buttons on mode selection screen
- Modal pop-ups displaying rules for each game mode
- Clean, non-intrusive UI design

**Location**: `src/main/java/com/comp2042/view/GameModeSelectionPanel.java`

### 9. Game Over Panel
- Displays current score and high score
- Mode-specific leaderboard integration
- Clean, centered layout

**Location**: `src/main/java/com/comp2042/view/GameOverPanel.java`

---

## Implemented but Not Working Properly

### None
All implemented features are functioning correctly. During development, several issues were encountered and resolved:

1. **Bedrock Corruption Timer Not Counting Down**: Fixed by ensuring corruption loop initialization in both constructor and `createNewGame()` method.

2. **Gravity Logic on Normal Bricks**: Initially, custom gravity was being applied to normal line clears, which contradicted standard Tetris behavior. Fixed by ensuring custom gravity only applies to power-up usage.

3. **Bedrock Blocks Disappearing/Changing Color**: Fixed by rewriting `clearRows()` method to correctly preserve bedrock positions during line clearing operations.

4. **Performance Issues in Power-Ups Mode**: Resolved through multiple optimizations:
   - Reduced particle counts
   - Optimized board refresh logic
   - Changed drill particle timer from `AnimationTimer` to `Timeline`
   - Reduced screen shake frequency and intensity
   - Stopped unused animations properly

**Location of Fixes**: 
- `src/main/java/com/comp2042/controller/GameController.java`
- `src/main/java/com/comp2042/model/SimpleBoard.java`
- `src/main/java/com/comp2042/view/GuiController.java`

---

## Features Not Implemented

### None
All required features have been successfully implemented and are working properly.

---

## New Java Classes

### 1. `NotificationPanel.java`
**Location**: `src/main/java/com/comp2042/view/NotificationPanel.java`

**Purpose**: Displays temporary notifications for power-up activations, power-up earnings, and bedrock corruption events. Provides fade-in/fade-out animations and auto-dismiss functionality.

**Key Features**:
- Animated appearance and disappearance
- Customizable message display
- Non-intrusive positioning

### 2. `GameOverPanel.java`
**Location**: `src/main/java/com/comp2042/view/GameOverPanel.java`

**Purpose**: Displays the game over screen with current score, high score, and action buttons (New Game, Leaderboard, Quit). Integrates with the leaderboard system.

**Key Features**:
- Score display (current and high score)
- Mode-specific leaderboard integration
- Centered, visually appealing layout

### 3. `LeaderboardPanel.java`
**Location**: `src/main/java/com/comp2042/view/LeaderboardPanel.java`

**Purpose**: Displays leaderboards for all game modes with the ability to switch between modes. Shows top scores with player names.

**Key Features**:
- Mode switching interface
- Persistent leaderboard storage
- Clean, organized display

### 4. `GameModeSelectionPanel.java`
**Location**: `src/main/java/com/comp2042/view/GameModeSelectionPanel.java`

**Purpose**: Provides the game mode selection interface with contextual help system. Allows players to choose between Classic, Mirror, and Power-Ups modes.

**Key Features**:
- Mode selection cards
- Info buttons with modal help system
- Visual mode indicators

### 5. `NameEntryPanel.java` / `NameEntryController.java`
**Location**: 
- `src/main/java/com/comp2042/view/NameEntryPanel.java`
- `src/main/java/com/comp2042/view/NameEntryController.java`

**Purpose**: Handles player name entry before starting a game. Validates input and stores the name in game settings.

**Key Features**:
- Input validation
- Integration with settings system

### 6. `HighScoreNotificationPanel.java`
**Location**: `src/main/java/com/comp2042/view/HighScoreNotificationPanel.java`

**Purpose**: Displays special notifications when the player achieves a new high score or beats their personal best.

**Key Features**:
- Animated high score celebrations
- Personal best tracking

### 7. `SettingsController.java`
**Location**: `src/main/java/com/comp2042/view/SettingsController.java`

**Purpose**: Manages the settings menu UI, allowing players to adjust volume, ghost mode, and other preferences.

**Key Features**:
- Volume controls
- Ghost mode toggle
- Settings persistence

### 8. `LeaderboardController.java`
**Location**: `src/main/java/com/comp2042/view/LeaderboardController.java`

**Purpose**: Controls the leaderboard view, handling mode switching and score display logic.

**Key Features**:
- Mode-specific leaderboard loading
- Score sorting and display

---

## Modified Java Classes

### 1. `GameController.java`
**Location**: `src/main/java/com/comp2042/controller/GameController.java`

**Major Modifications**:
- **Power-Up System Integration**: Added methods for handling Freeze, Bomb, and Drill power-ups
  - `activateFreeze()`: Implements freeze power-up with timeline pausing
  - `enterBombTargetingMode()`: Enables mouse targeting for bomb placement
  - `handleMouseClick()`: Converts mouse coordinates to grid coordinates for bomb targeting
  - `activateDrill()`: Spawns drill brick and manages drill mechanics
  - `startGravityAnimation()`: Handles cascading gravity animation after power-up usage

- **Bedrock Corruption System**: Implemented the rising tide mechanic
  - `initializeCorruptionLoop()`: Sets up 15-second corruption timer
  - `pauseCorruptionLoop()` / `resumeCorruptionLoop()`: Manages corruption during pause
  - Corruption countdown management and game over detection

- **Game Mode Management**: Enhanced mode switching and initialization
  - Power-Ups mode starts at level 5 for faster pace
  - Mode-specific initialization logic

- **Score and Leaderboard Integration**: 
  - `checkPowerUpEarning()`: Awards power-ups every 100 points
  - `checkPersonalHighScore()`: Tracks personal best scores
  - `handleGameOver()`: Saves scores to appropriate leaderboards

- **Constants Extraction**: Replaced magic numbers with named constants
  - `INITIAL_POWER_UP_THRESHOLD = 100`
  - `POWER_UP_THRESHOLD_INCREMENT = 100`
  - `CORRUPTION_INTERVAL_SECONDS = 15`
  - `BASE_SPEED_MS = 400.0`
  - `SPEED_DECREMENT_MS = 35.0`
  - `MIN_SPEED_MS = 75.0`
  - `POWERUPS_START_LEVEL = 5`

**Rationale**: Central controller needed to coordinate power-ups, game modes, and special mechanics. Constants improve maintainability.

### 2. `SimpleBoard.java`
**Location**: `src/main/java/com/comp2042/model/SimpleBoard.java`

**Major Modifications**:
- **Bedrock Corruption Logic**: 
  - `corruptNextRow()`: Converts existing blocks to bedrock (ID 9)
  - `currentCorruptionRow` tracking
  - Bedrock immunity in `clearRows()`, `moveDrillDown()`, `explodeAt()`

- **Drill Mechanics**:
  - `moveDrillDown()`: Independent drill movement with block destruction
  - `isDrillActive()`: Checks if current brick is a drill
  - `spawnDrill()`: Creates drill brick
  - Drill movement in `moveBrickLeft()` and `moveBrickRight()`

- **Bomb Explosion**:
  - `explodeAt(int row, int col)`: Destroys blocks in 3x3 radius
  - Bedrock immunity checks

- **Gravity System**:
  - `hasFloatingBlocks()`: Detects blocks with empty space below
  - `applyGravityStep()`: Moves blocks down one row, respecting bedrock

- **Helper Methods** (Refactoring):
  - `isBedrock(int row, int col)`: Centralized bedrock checking
  - `destroyBlockIfNotBedrock(int row, int col)`: Centralized block destruction

- **Constants**:
  - `BEDROCK_ID = 9`
  - `DRILL_ID = 11`

**Rationale**: Board model needed to support new power-up mechanics and bedrock corruption. Helper methods reduce code duplication.

### 3. `GuiController.java`
**Location**: `src/main/java/com/comp2042/view/GuiController.java`

**Major Modifications**:
- **Power-Up Visuals**:
  - `showPowerUpActivation()`: Displays power-up activation notifications
  - `showPowerUpEarned()`: Shows when power-ups are earned
  - `refreshInventory()`: Updates inventory display with power-up logos
  - `setGameCursor()`: Changes cursor for bomb targeting mode

- **Bedrock Corruption UI**:
  - `updateCorruptionTimer(int seconds)`: Updates danger timer display
  - `getCorruptionTimerContainer()`: Returns timer container for visibility control
  - `showBedrockCorruptionNotification()`: Shows notification when bedrock forms
  - Bedrock visual rendering (dark gray, ID 9)

- **Explosion Animation**:
  - `playExplosionAnimation(int row, int col)`: Creates particle effects for bomb explosions
  - Particle system with fade-out animations

- **Drill Visuals**:
  - Drill texture loading and rendering
  - Drill particle sparks during movement
  - Real-time drill position tracking

- **Freeze Effect**:
  - `setFreezeEffect(boolean active)`: Activates/deactivates freeze visuals
  - Glacier color scheme for frozen blocks
  - Snow particle effects

- **Mirror Mode Visuals**:
  - `setMirrorMode(boolean active)`: Applies vertical flip transformation
  - Particle effects for mirror mode

- **Color Palette Update**:
  - Cyberpunk/retro arcade color scheme
  - Sharp square shapes (no rounded corners)
  - Updated `getFillColor()` method

- **Performance Optimizations**:
  - Reduced particle counts
  - Optimized `refreshGameBackground()` to only update changed cells
  - Changed drill particle timer from `AnimationTimer` to `Timeline`
  - Reduced screen shake frequency

**Rationale**: View layer needed extensive updates to support new power-ups, bedrock corruption, and visual enhancements. Performance optimizations were critical for smooth gameplay.

### 4. `GameSettings.java`
**Location**: `src/main/java/com/comp2042/model/GameSettings.java`

**Major Modifications**:
- **Game Mode Storage**: Added `selectedMode` field and getter/setter methods
- **Import Cleanup**: Removed fully qualified class names, using proper imports

**Rationale**: Settings system needed to persist game mode selection across sessions.

### 5. `HighScoreManager.java`
**Location**: `src/main/java/com/comp2042/model/HighScoreManager.java`

**Major Modifications**:
- **Mode-Specific Leaderboards**: 
  - `loadLeaderboard(GameMode mode)`: Loads leaderboard for specific mode
  - `saveEntry(GameMode mode, ScoreEntry entry)`: Saves entry to mode-specific file
  - `resetLeaderboard(GameMode mode)`: Resets leaderboard for specific mode
  - `resetHighScore()`: Now resets all leaderboards for all modes

**Rationale**: Leaderboard system needed to support separate tracking for each game mode.

### 6. `MainMenuController.java`
**Location**: `src/main/java/com/comp2042/view/MainMenuController.java`

**Major Modifications**:
- **Debug Output Removal**: Removed `System.out.println` debug statements
- **Video Loading**: Enhanced video loading with retry logic

**Rationale**: Clean up debug output for production code.

### 7. `Main.java`
**Location**: `src/main/java/com/comp2042/app/Main.java`

**Major Modifications**:
- **Debug Output Removal**: Removed font loading success message

**Rationale**: Clean up debug output for production code.

### 8. `Board.java` (Interface)
**Location**: `src/main/java/com/comp2042/model/Board.java`

**Major Modifications**:
- **Power-Up Methods**: Added interface methods for power-up management
  - `addPowerUp(PowerUp type)`
  - `usePowerUp(int index)`
  - `getInventory()`
  - `explodeAt(int row, int col)`

**Rationale**: Interface needed to define power-up contract for board implementations.

---

## Unexpected Problems

### 1. Bedrock Corruption Timer Not Initializing
**Problem**: The corruption timer was stuck at 15 and not counting down, preventing bedrock from spawning.

**Root Cause**: The `corruptionLoop` timeline was only initialized in `createNewGame()`, but not in the constructor when the game mode was set to Power-Ups.

**Solution**: Added corruption loop initialization in both the constructor and `createNewGame()` method to ensure it starts regardless of when the game mode is set.

**Location**: `src/main/java/com/comp2042/controller/GameController.java` (lines 121-160, 493-530)

### 2. Gravity Logic Affecting Normal Line Clears
**Problem**: Custom gravity animation was being applied to normal line clears, causing blocks to change shape and behave differently from standard Tetris.

**Root Cause**: The `clearRows()` method was using custom column-by-column gravity logic instead of the standard immediate gravity.

**Solution**: Reverted `clearRows()` to use standard `lineClearService.clearFullLines()` for immediate gravity, while preserving bedrock positions explicitly. Custom iterative gravity is now only used for power-up effects.

**Location**: `src/main/java/com/comp2042/model/SimpleBoard.java` (lines 347-406)

### 3. Bedrock Blocks Disappearing or Changing Color
**Problem**: After line clears, bedrock blocks would sometimes disappear or change to different colors.

**Root Cause**: The `clearRows()` method was not correctly preserving bedrock positions during the gravity application after line clears.

**Solution**: Rewrote `clearRows()` to:
1. Identify bedrock positions before clearing
2. Apply standard line clearing
3. Reconstruct matrix by dropping non-bedrock blocks while preserving bedrock as solid barriers

**Location**: `src/main/java/com/comp2042/model/SimpleBoard.java` (lines 347-406)

### 4. Performance Issues in Power-Ups Mode
**Problem**: Power-Ups mode was experiencing significant lag, stuttering, and occasional glitches, making it unplayable.

**Root Cause**: Multiple performance bottlenecks:
- Excessive particle generation
- Inefficient board refresh logic (updating all cells every frame)
- Multiple gravity timelines running simultaneously
- High-frequency animation timers
- Excessive console logging

**Solution**: Implemented comprehensive performance optimizations:
- Reduced particle counts (drill: 1-2 particles, explosions: 4-5 particles)
- Optimized `refreshGameBackground()` to only update cells that changed
- Ensured only one gravity timeline runs at a time
- Changed drill particle timer from `AnimationTimer` to `Timeline` (50ms interval)
- Reduced screen shake frequency (20% chance) and intensity (1.5px)
- Removed all `System.out.println` statements
- Limited concurrent particles to 15 max

**Location**: 
- `src/main/java/com/comp2042/view/GuiController.java` (lines 1621-1815)
- `src/main/java/com/comp2042/controller/GameController.java` (lines 905-930)

### 5. Explosion Particles Disappearing
**Problem**: Explosion particles were being cleared immediately because they were added to a pane that gets refreshed.

**Root Cause**: Particles were added to `gameBoard` pane, which gets cleared during board refreshes.

**Solution**: Changed particle container to scene root (StackPane) to ensure persistence above the board.

**Location**: `src/main/java/com/comp2042/view/GuiController.java` (lines 832-900)

### 6. Lambda Variable Scope Issues
**Problem**: Compilation errors due to local variables referenced in lambda expressions not being effectively final.

**Root Cause**: Variables were being modified after declaration but before use in lambda expressions.

**Solution**: Restructured code to ensure variables are declared as `final` and assigned only once, or used final array wrappers for mutable values.

**Location**: Multiple locations in `GuiController.java` and `GameController.java`

### 7. Wildcard Import Issues
**Problem**: Code quality issues with wildcard imports making dependencies unclear.

**Root Cause**: `import com.comp2042.model.*;` was used instead of explicit imports.

**Solution**: Replaced wildcard import with explicit imports for all model classes.

**Location**: `src/main/java/com/comp2042/controller/GameController.java` (lines 6-18)

---

## Code Quality Improvements

### Refactoring Work Completed

1. **Magic Numbers → Named Constants**: Replaced all magic numbers with meaningful constants throughout the codebase
2. **Code Duplication Reduction**: Extracted helper methods to reduce duplication (e.g., `isBedrock()`, `destroyBlockIfNotBedrock()`)
3. **Import Organization**: Removed fully qualified class names and wildcard imports
4. **Documentation**: Added comprehensive JavaDoc comments to key classes and methods
5. **Debug Output Cleanup**: Removed all debug print statements
6. **Unused Code Removal**: Removed unused imports, fields, methods, and variables

**Location**: All modified files listed above

---

## Testing Notes

- All game modes have been tested and are functioning correctly
- Power-ups work as expected in Power-Ups mode
- Bedrock corruption mechanic functions properly
- Leaderboards save and load correctly for all modes
- Performance optimizations have resolved lag issues
- No known bugs or issues remain

---

## Additional Notes

- The game uses a custom "Public Pixel" font for retro aesthetics
- All resources (images, sounds) are located in `src/main/resources/`
- Settings are persisted to `settings.dat` in the project root
- Leaderboard files are stored per mode: `leaderboard_classic.dat`, `leaderboard_mirror.dat`, `leaderboard_powerups.dat`

---

## Conclusion

This coursework successfully implements all required features including multiple game modes, power-up system, bedrock corruption mechanic, and comprehensive UI enhancements. All features are working properly, and the codebase has been significantly improved through refactoring and optimization efforts.

