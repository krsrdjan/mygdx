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

- `MyGdxGame` extends `ApplicationAdapter` — main game loop (create/render/dispose); dual `OrthographicCamera` + `ExtendViewport` (world and HUD, min 1280×720 logical units)
- `GameBoard` holds `Square[][]` grid, manages turns, monsters, items, exploration; use `setToastNotifier(StringCallback)` to wire toast messages to the UI
- `Creature` is the base class for `Hero` and `Monster`; textures loaded via `TextureCache.get(image)`
- `Weapon` is abstract — subclasses: `Sword`, `Axe`, `Mace`, `BigClub`, `Bite`
- `Item` is abstract base for collectibles (extends `Creature`; implement `use(Hero)`)
- `Position` is an immutable 2D coordinate value object
- `MyInputAdapter` extends `InputAdapter` for keyboard controls
- `RoomMazeGenerator` handles procedural dungeon generation
- `RandomMonsterFactory` creates monsters via factory pattern
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
- **This project**: `ExtendViewport` with minimum world size **1280×720** (16:9) on both world and HUD cameras. The viewport **extends** width or height on taller or wider aspects (typical tall phones in landscape/portrait, ultrawide monitors) so there is **no letterboxing**; the player may see extra dungeon tiles at the edges. Pure **16:9** surfaces still see exactly 1280×720 world units (same as a fixed HD logical size).
- Override `resize(int width, int height)` and call `viewport.update(width, height)` for each viewport (world uses `centerCamera` false so the follow camera is not reset). **Web (TeaVM)**: the launcher can report **0×0** before the canvas has CSS layout; `MyGdxGame` skips viewport updates until width/height are positive and retries from `create`/`render` using `Gdx.graphics.getBackBufferWidth/Height()` so `glViewport` is never called with zero size (which would show a black screen).
- Separate world camera from UI camera — both use logical units; the HUD is laid out in the HUD camera space (bottom-anchored bars; `hudCamera.viewportWidth` / `viewportHeight` reflect extended size).

### Audio
- Use `Sound` for short effects (< 5 seconds), `Music` for background tracks
- `Music` streams from disk — only one active instance needed
- Set volume via a config object (like `AudioConfig`) so it's adjustable globally

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
2. Register the monster in `RandomMonsterFactory` (image name, stats via `Monster` constructor, `setWeapon(...)` with an existing `Weapon` or a new `Weapon` subclass only if you need a new attack profile)
3. No changes needed in platform modules

### New Weapon
1. Extend `Weapon` with `chanceToHit` and `damage` values
2. Assign to a monster or make available to the hero

### New Item
1. Extend `Item` abstract class in core/
2. Add pickup/use logic in `Hero` or `GameBoard`
3. Add texture to `assets/`
