# Project: Turn-Based Dungeon Crawler (libGDX)

## Tech Stack

- **Framework**: libGDX 1.14.0
- **Language**: Java 17 (configurable via `javaVersion` in root build.gradle)
- **Build**: Gradle multi-module (core, desktop, teavm)
- **Desktop backend**: LWJGL3
- **Web backend**: gdx-teavm 1.4.0 (TeaVM → JavaScript, served via Gretty/Jetty)
- **Package**: `com.mygdx.game`

## Project Structure

```
core/src/com/mygdx/game/    — All shared game logic (platform-agnostic)
desktop/src/com/mygdx/game/ — Desktop launcher (LWJGL3)
teavm/src/com/mygdx/game/   — Web launcher (TeaVMLauncher, TeaVMBuilder)
assets/                     — Shared assets (textures, sounds, fonts)
```

Source directories use `src/` (not `src/main/java/`). Assets live in root `assets/` and are referenced via `Gdx.files.internal()`.

## Architecture

The game follows a turn-based board game pattern:

- `MyGdxGame` extends `ApplicationAdapter` — main game loop (create/render/dispose); dual `OrthographicCamera` + `ExtendViewport` (world **play area** min 1280×538, HUD full screen min 1280×720); constructor takes optional `boolean mobileWebPlay` flag (enables `MOBILE_DUNGEON_ZOOM = 2f`)
- `GameBoard` holds `Square[][]` grid, manages turns, monsters, items, exploration; constants: `BOARD_SQUARE_WIDTH = 32`, `BOARD_SQUARE_HEIGHT = 32`, `SQUARE_SIZE = 64`, `ROOMS_WIDE = 8`, `ROOMS_TALL = 8`, `TOTAL_FLOORS = 3`; use `setToastNotifier(StringCallback)` to wire toast messages to the UI; key query methods: `isHeroTurn()`, `getRound()`, `getCurrentFloor()`, `getMonstersKilled()`, `isVictory()`; item lifecycle: `addItem(Item)`, `removeItem(Item)`, `restoreFloorItemAt(Position)`; combat log: `logCombat(String)` / `showToast(String)` both append to `CombatLog`; click-to-move entry point: `heroClickOnTile(int tileX, int tileY)`
- `CombatLog` — rolling log (max 32 entries) of combat messages and toasts; obtained via `gameBoard.getCombatLog()`; rendered in the HUD log strip; deduplicates consecutive identical messages
- `Creature` is the base class for `Hero` and `Monster`; textures loaded via `TextureCache.get(image)`
- `Hero` — player character; `MAX_WEAPON_INVENTORY = 3` slots; weapon methods: `addWeapon(Weapon)`, `switchWeapon()` (cycles inventory), `swapEquippedWeapon(Weapon)` (replaces current, returns displaced), `setCurrentWeapon(Weapon)`, `getCurrentWeapon()`; movement consumes `speed` (reset to `MAX_SPEED = 8` each turn); `heal(int amount)` clamps to `maxHealth`
- `Monster` — two constructors: `Monster(image, health, board)` (defaults) and `Monster(image, health, damage, maxSpeed, board)` (custom stats); activate/move/attack logic runs via `startTurn(Hero)` called by `GameBoard.endHeroTurn()`
- `Weapon` is abstract — constructor `Weapon(float chanceToHit, int damage, String name)`; subclasses: `Sword`, `Axe`, `Mace`, `BigClub`, `Bite`, `Hammer`, `RustyBlade`
- `Item` is abstract base for collectibles (extends `Creature`; implement `use(Hero)`); has `moveTo(Position, boolean occupySquare)` for repositioning without always claiming the square
- `HealPotion` — Item subclass; texture `potion-red.png`; restores 1 HP on pickup
- `GreaterHealPotion` — Item subclass; texture `potion-blue.png`; restores 2 HP on pickup
- `WeaponPickup` — Item subclass; texture `weapon.png`; on pickup: adds weapon to hero inventory if space available, otherwise calls `swapEquippedWeapon` and drops displaced weapon at same tile
- `AudioConfig` — holds single constant `VOLUME = 0.5f`; used by Hero, Monster, and MyGdxGame for all sound/music playback volume
- `Position` is an immutable 2D coordinate value object; `Position.isNear(a, b)` checks adjacency
- `MyInputAdapter` extends `InputAdapter` for keyboard controls
- `RoomMazeGenerator` handles procedural dungeon generation
- `RandomMonsterFactory` creates monsters via factory methods: `createTroll`, `createOrc`, `createWerewolf`, `createOgre`, `createSkeleton`, `createRandomMonster`
- `Toast` handles in-game notification popups
- `TextureCache` — static cache for textures; `get(filename)` and `getOrCreateSolid(key, r, g, b, a, size)`; call `TextureCache.dispose()` in game `dispose()`
- `SoundCache` — static cache for sounds; `get(filename)`; call `SoundCache.dispose()` in game `dispose()`
- `Square` — represents a single board tile; holds a `Texture`, an optional `Creature` occupant, and an `explored` flag
- `StringCallback` — interface `call(String value)` used to notify UI (e.g. toasts) from game logic

## Code Conventions

- All game logic goes in `core/` module — NEVER put game logic in platform modules
- Platform launchers (desktop, teavm) are thin wrappers that only configure and start the game; no game logic in platform modules
- Use libGDX APIs, not raw Java AWT/Swing/JavaFX — everything must be cross-platform
- Textures: use `TextureCache.get("filename.png")` or `TextureCache.getOrCreateSolid(...)` for procedural solids; the cache is disposed in the main game `dispose()`
- Sounds: use `SoundCache.get("file.mp3")` (or `.wav`); the cache is disposed in the main game `dispose()`
- Dispose all disposable resources (SpriteBatch, Music, BitmapFont, ShapeRenderer, plus `TextureCache.dispose()` and `SoundCache.dispose()`) in `dispose()`
- Use `Gdx.files.internal()` for all file access — never use `java.io.File` or `java.nio.file`
- Prefer `com.badlogic.gdx.utils` collections (Array, ObjectMap) over java.util when performance matters
- Coordinate system: origin (0,0) is bottom-left, Y-axis points up
- Board uses `SQUARE_SIZE = 64` pixels per tile, grid is `BOARD_SQUARE_WIDTH x BOARD_SQUARE_HEIGHT`

## Rendering Rules

- All rendering happens between `batch.begin()` and `batch.end()` — never nest or overlap
- ShapeRenderer has its own begin/end cycle — do not mix with SpriteBatch
- Use `OrthographicCamera` for world view and a separate `hudCamera` for UI overlay
- Call `camera.update()` before drawing with the camera’s combined matrix (world camera is updated after follow logic each frame; viewports update cameras in `apply()` as well)
- Clear screen with `ScreenUtils.clear()` at the start of render
- Font rendering uses `BitmapFont` — for styled text use libGDX's `Label` with `Scene2D`

## Game Development Best Practices

These sections mix **what this project already does** (caches, `dispose()`, two cameras, `ExtendViewport` for world and HUD in `MyGdxGame`) with **general libGDX guidance**. The codebase does not use a Scene2D `Stage` for input or a turn-phase enum; it uses `MyInputAdapter` and a `heroTurn` boolean in `GameBoard`. Use the guidance below when refactoring or growing the project.

### Resource Management
- Load textures/sounds via `TextureCache.get()` and `SoundCache.get()` — they are cached and reused; call `TextureCache.dispose()` and `SoundCache.dispose()` in the main game `dispose()`
- Always call `.dispose()` on Music, SpriteBatch, ShapeRenderer, BitmapFont, and the static caches
- For many assets or async loading, consider `AssetManager`; for current scale the static caches are used
- Use `TextureAtlas` (packed spritesheets) instead of individual textures when asset count grows

### Performance
- Avoid allocating objects in `render()` — preallocate and reuse (Vector2, Rectangle, etc.)
- Use object pools (`com.badlogic.gdx.utils.Pool` / `DefaultPool`) for frequently created/destroyed objects
- SpriteBatch draw calls are batched automatically — avoid switching textures mid-batch
- `ShapeRenderer` is expensive — prefer textured sprites for production UI
- Delta time: always use `Gdx.graphics.getDeltaTime()` for time-based updates, never frame counting

### Game State
- Keep game state in `core/` data classes, separate from rendering
- The render loop should read state and draw — avoid mixing mutation and drawing
- Turn-based logic: clearly separate player turn, AI turn, and animation phases
- Use state machine or enum for game phases (PLAYER_TURN, MONSTER_TURN, ANIMATING, GAME_OVER)

### Input
- Use `InputAdapter` or `InputMultiplexer` for input — never poll in render unless needed
- For UI + game input, use `InputMultiplexer`: Stage first, then game input adapter
- Map controls to actions, not raw keys — makes rebinding easy

### Scaling & Cameras
- **This project**: Split viewports per ADR 0002. World **play area** uses `ExtendViewport` with minimum **1280×538** (720 minus ~182px HUD strip); HUD uses full-screen minimum **1280×720**. The world glViewport is shifted above the HUD strip on resize (`ExtendViewport.update()` resets bounds—re-apply Y offset). World draws are scissor-clipped to the play area. **Camera follow** centers the Hero in the play area; at the south map edge the camera clamps to the grid (no empty space below y=0). Viewports **extend** width or height on ultrawide/tall aspects so there is no letterboxing in the play area; pure **16:9** still maps 1:1 at minimum logical size.
- Override `resize(int width, int height)` and call `updateViewportsIfSized` (HUD first, then world with play-area height + Y offset; world uses `centerCamera` false so follow logic is not reset). **Web (TeaVM)**: the launcher can report **0×0** before the canvas has CSS layout; `MyGdxGame` skips viewport updates until width/height are positive and retries from `create`/`render` using `Gdx.graphics.getBackBufferWidth/Height()` so `glViewport` is never called with zero size (which would show a black screen).
- Separate world camera from HUD camera — HUD layout is bottom-anchored in HUD logical space (`hudCamera.viewportWidth` / `viewportHeight` reflect extended size). HUD hits unproject through `hudViewport` and test `y <= HUD_PANEL_Y_TOP`; play-area touches unproject through the world viewport.

### Audio
- Use `Sound` for short effects (< 5 seconds), `Music` for background tracks
- `Music` streams from disk — only one active instance needed
- All volume is centralised in `AudioConfig.VOLUME` (currently `0.5f`) — pass it to every `sound.play(AudioConfig.VOLUME)` and `music.setVolume(AudioConfig.VOLUME)` call
- Background music: `atmosphere.mp3` (looping, started in `MyGdxGame.create()`); sfx: `sword.wav` (hit), `death.mp3` (hero death)

## Gradle Commands

```bash
./gradlew :desktop:run          # Run the game (desktop)
./gradlew :desktop:dist         # Build distributable JAR
./gradlew :teavm:run            # Build JS and run web server (http://localhost:8080/)
./gradlew :teavm:buildJavaScript # Transpile to JavaScript only (output in teavm/build/dist)
./gradlew :core:compileJava     # Compile core module only
./gradlew clean                 # Clean build artifacts
```

## Adding New Content

### New Monster
1. Add the monster texture to `assets/`
2. Add a `createXxx(GameBoard board)` factory method in `RandomMonsterFactory` — use `Monster(image, health, damage, maxSpeed, board)` and call `monster.setWeapon(new YourWeapon())`
3. Add the image name to `MONSTER_IMAGES[]` and wire it in `createForImage()`
4. No changes needed in platform modules

### New Weapon
1. Extend `Weapon` — constructor `super(chanceToHit, damage, "Name")`
2. Assign to a monster in `RandomMonsterFactory`, or place as a `WeaponPickup` item on the board

### New Item (consumable)
1. Extend `Item` in `core/` — call `super("texture.png", board)` in the constructor
2. Implement `use(Hero hero)`: apply effect, call `board.showToast(...)`, call `board.removeItem(this)`, and null the square via `board.getSquare(position.x, position.y).setCreature(null)`
3. See `HealPotion` (restores 1 HP) and `GreaterHealPotion` (restores 2 HP) as reference implementations
4. Add the texture to `assets/`
5. Spawn the item in `GameBoard` (search for where `HealPotion` is instantiated for placement patterns)

### New Weapon Pickup (droppable weapon on the floor)
1. Create the `Weapon` subclass (step above)
2. Instantiate `new WeaponPickup(weapon, board)` and call `pickup.moveTo(position, true)` then `board.addItem(pickup)`
3. `WeaponPickup` handles the full pick-up/swap/drop logic automatically

### Existing Assets
Textures in `assets/`: `hero.png`, `troll.png`, `orc.png`, `werewolf.png`, `ogre.png`, `skeleton.png`, `weapon.png`, `potion-red.png`, `potion-blue.png`, `rock.png`, `tile.jpeg`, `tile-small.jpeg`, `logo.png`, `startup-logo.png`
Sounds: `sword.wav`, `death.mp3`, `atmosphere.mp3` (background), `music.mp3`
