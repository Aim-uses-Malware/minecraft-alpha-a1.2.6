# Minecraft Alpha a1.2.6 — Deobfuscated Source

Декомпилированный и деобфусцированный исходный код Minecraft Alpha a1.2.6.  
Сборка через Gradle, без необходимости иметь официальный Minecraft Launcher.

---

## Структура проекта

```
minecraft-alpha/
├── src/
│   └── main/
│       └── java/
│           └── net/minecraft/
│               ├── client/          # Точка входа (Minecraft.java)
│               ├── src/             # Весь игровой код (~455 классов)
│               └── isom/            # Изометрический превью (апплет)
├── game/
│   └── assets/                      # Ресурсы (звуки, индексы)
│       ├── indexes/
│       │   └── a1.2.0.json
│       └── objects/                 # Хешированные файлы ресурсов
├── build.gradle
├── settings.gradle
└── gradle/
    └── wrapper/
        └── gradle-wrapper.properties
```

---

## Требования

| Инструмент | Версия        |
|------------|---------------|
| JDK        | 8 (обязательно, не выше) |
| Gradle     | 8.7+ (через wrapper, ставить не нужно) |

> ⚠️ Именно **JDK 8**. Minecraft Alpha не совместим с более новыми версиями Java из-за устаревших API (`sun.*`, AWT-специфика, LWJGL 2).

---

## Быстрый старт

### 1. Клонировать репозиторий

```bash
git clone https://github.com/YOUR_USERNAME/minecraft-alpha.git
cd minecraft-alpha
```

### 2. Запустить игру

**Linux / macOS:**
```bash
./gradlew run
```

**Windows:**
```cmd
gradlew.bat run
```

Gradle автоматически:
- скачает все зависимости (LWJGL, Paulscode и т.д.)
- распакует нативные библиотеки в `build/natives/`
- запустит игру через LaunchWrapper

---

## Сборка JAR

```bash
./gradlew jar
```

Результат: `build/libs/minecraft-alpha-a1.2.6.jar` (fat jar, все зависимости внутри).

Запуск JAR вручную:
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

## Настройка IDE

### IntelliJ IDEA

1. `File → Open` → выбрать папку проекта
2. IDEA автоматически импортирует Gradle
3. Run-конфигурация `run` уже настроена через `build.gradle`
4. Убедитесь что в `File → Project Structure → SDK` выбран JDK 8

### Eclipse

1. `File → Import → Gradle → Existing Gradle Project`
2. Выбрать папку проекта
3. Выбрать JDK 8 в настройках проекта

### VS Code

1. Установить расширения: `Extension Pack for Java`, `Gradle for Java`
2. Открыть папку проекта
3. В панели Gradle: `Tasks → application → run`

---

## Зависимости

| Библиотека | Версия | Назначение |
|---|---|---|
| `org.mcphackers:launchwrapper` | 1.2.4 | Шим запуска (точка входа) |
| `org.lwjgl.lwjgl:lwjgl` | 2.9.4-nightly | OpenGL, окно, ввод |
| `org.lwjgl.lwjgl:lwjgl_util` | 2.9.4-nightly | Утилиты LWJGL |
| `net.java.jinput:jinput` | 2.0.5 | Геймпад/джойстик |
| `com.paulscode:soundsystem` | 20120107 | Звуковая система |
| `com.paulscode:codecjorbis` | 20230120 | Декодер OGG Vorbis |
| `com.paulscode:codecwav` | 20101023 | Декодер WAV |
| `com.paulscode:libraryjavasound` | 20101123 | Java Sound backend |
| `com.paulscode:librarylwjglopenal` | 20100824 | OpenAL backend |
| `org.ow2.asm:asm` | 9.9 | Байткод (LaunchWrapper) |
| `org.json:json` | 20230311 | Разбор JSON ресурсов |

---

## Аргументы запуска (LaunchWrapper)

| Аргумент | Значение по умолчанию | Описание |
|---|---|---|
| `--username` | `Player` | Имя игрока |
| `--uuid` | `-` | UUID (не нужен в offline) |
| `--session` | `-` | Токен сессии |
| `--version` | `a1.2.6` | Версия клиента |
| `--gameDir` | `.` (папка `game/`) | Рабочая директория |
| `--assetsDir` | `assets` | Папка ресурсов |
| `--assetIndex` | `a1.2.0` | Индекс ресурсов |
| `--skinProxy` | `pre-b1.9-pre4` | Прокси для скинов |

Изменить можно в `build.gradle` в блоке `run { args = [...] }`.

---

## Как это работает

```
gradlew run
    └─► LaunchWrapper (org.mcphackers.launchwrapper.Launch)
            └─► net.minecraft.client.Minecraft (основной класс игры)
                    ├─► LWJGL 2 (окно OpenGL)
                    ├─► Paulscode (звук через OpenAL)
                    └─► game/assets/ (текстуры, звуки)
```

LaunchWrapper — это лёгкий шим, который имитирует поведение оригинального Minecraft Launcher, передавая нужные аргументы в основной класс.

---

## Возможные проблемы

**`UnsupportedClassVersionError`** — используется JDK новее 8.  
→ Установи JDK 8 и укажи его в `JAVA_HOME`.

**`SIGSEGV` / падение при старте** — нативные библиотеки не распакованы.  
→ Запусти `./gradlew extractNatives` вручную.

**Нет звука** — отсутствуют файлы в `game/assets/objects/`.  
→ Файлы ресурсов хранятся в хешированном виде согласно `game/assets/indexes/a1.2.0.json`. При необходимости можно восстановить их с помощью оригинального лаунчера.

**`Could not resolve` зависимости** — Maven репозиторий `repo.mcphackers.org` недоступен.  
→ Скачай JAR вручную с [mcphackers releases](https://github.com/MCPhackers/launchwrapper/releases) и добавь в `libs/`.

---

## Лицензия

Этот репозиторий содержит деобфусцированный код Minecraft, права на который принадлежат Mojang / Microsoft.  
Репозиторий предназначен **только для образовательных целей**.
