# Minecraft Alpha a1.2.6 — Deobfuscated Source

Decompiled and deobfuscated source code of Minecraft Alpha a1.2.6.
Builds with Gradle — no official Minecraft Launcher required.

---

## Requirements

| Tool   | Version                          |
| ------ | --------------------------------- |
| JDK    | 8 (required — won't run on newer) |
| Gradle | 8.7+ (via wrapper, no install needed) |

> ⚠️ Minecraft Alpha is not compatible with newer Java versions due to legacy APIs (`sun.*`, AWT, LWJGL 2).

---

## Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/minecraft-alpha-a1.2.6.git
cd minecraft-alpha-a1.2.6
```

### 2. Run the game

**Linux / macOS:**
```bash
./gradlew run
```

**Windows:**
```bash
gradlew.bat run
```

Gradle will automatically download dependencies (LWJGL, Paulscode, etc.) and extract native libraries before launching.

---

## Build a JAR

```bash
./gradlew jar
```

Output: `build/libs/minecraft-alpha-a1.2.6.jar` (fat jar, all dependencies included).

Run it manually:
```bash
java -Djava.library.path=build/natives \
     -jar build/libs/minecraft-alpha-a1.2.6.jar \
     --username Player --uuid - --session - \
     --version a1.2.6 --gameDir ./game \
     --assetsDir ./game/assets --assetIndex a1.2.0 \
     --accessToken - --userProperties {} \
     --userType legacy --versionType release \
     --skinProxy pre-b1.9-pre4
```

---

## Dependencies

| Library                         | Version        | Purpose                |
| -------------------------------- | --------------- | ----------------------- |
| `org.mcphackers:launchwrapper`   | 1.2.4           | Launch entry point       |
| `org.lwjgl.lwjgl:lwjgl`          | 2.9.4-nightly   | OpenGL, window, input    |
| `net.java.jinput:jinput`         | 2.0.5           | Gamepad/joystick input   |
| `com.paulscode:soundsystem`      | 20120107        | Sound engine              |
| `org.ow2.asm:asm`                | 9.9             | Bytecode (LaunchWrapper)  |
| `org.json:json`                  | 20230311        | Resource JSON parsing     |

---

## Project Structure

```
minecraft-alpha/
├── src/main/java/net/minecraft/
│   ├── client/      # Entry point (Minecraft.java)
│   ├── src/         # All game code (~455 classes)
│   └── isom/        # Isometric preview applet
├── src/main/resources/   # Bundled textures/icons (pack.png, gui/, mob/, etc.)
├── game/assets/          # External sound assets (indexes + hashed objects)
├── build.gradle
└── settings.gradle
```

---

## Important Note on Resources

This repository contains **only the decompiled code** — not Mojang's original assets, for copyright reasons. To get a fully working game, you need to add:

- **Textures & icons** (`pack.png`, `gui/`, `mob/`, `terrain.png`, `font/`, etc.) → extract from an original `client.jar` and place under `src/main/resources/`.
- **Sounds & music** (`.ogg` / `.mus`) → place under `game/assets/objects/<hash[0:2]>/<hash>`, matching the index at `game/assets/indexes/a1.2.0.json`.

Without these, the game will compile and launch, but crash on startup (`ImageIO.read input == null`) or run silently without audio.

---

## Troubleshooting

**`UnsupportedClassVersionError`** — you're using a JDK newer than 8.
→ Install JDK 8 and set it as `JAVA_HOME`.

**Crash at startup (`pack.png` / `gui/logo.png` not found)** — texture resources are missing.
→ See [Resources](#important-note-on-resources) above.

**No sound** — asset objects are missing from `game/assets/objects/`.
→ Populate them from the asset index, or run through a launcher (e.g. Betacraft) that fetches them automatically.

---

## License

This repository contains deobfuscated Minecraft code. All rights belong to Mojang / Microsoft.
Provided for **educational purposes only**.
