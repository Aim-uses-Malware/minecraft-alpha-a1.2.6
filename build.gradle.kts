plugins {
    id("java-library")
    application
}

group = "net.minecraft"
version = "rd-161807"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

val lwjglVersion = "3.4.1"

// Detect the LWJGL natives classifier for the machine running Gradle.
// (Only affects the `run` task; the fat jar bundles all platforms — see fatJar below.)
val osName = System.getProperty("os.name")!!.lowercase()
val osArch = System.getProperty("os.arch")!!.lowercase()
val is64Arm = osArch.startsWith("aarch64") || osArch.startsWith("arm64")

val lwjglNatives = when {
    osName.contains("win") -> if (is64Arm) "natives-windows-arm64" else "natives-windows"
    osName.contains("mac") || osName.contains("darwin") -> if (is64Arm) "natives-macos-arm64" else "natives-macos"
    else -> when {
        is64Arm -> "natives-linux-arm64"
        osArch.startsWith("arm") -> "natives-linux-arm32"
        else -> "natives-linux"
    }
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")

    // Natives — LWJGL 3 auto-extracts these from the classpath at runtime,
    // no manual extractNatives task needed anymore.
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
}

application {
    mainClass.set("com.mojang.minecraft.Minecraft")
}

// Add manifest to the default jar
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.mojang.minecraft.Minecraft"
        attributes["Implementation-Version"] = project.version
    }
}

// Fat jar — bundles all dependencies (LWJGL classes + natives for every platform) into one runnable jar
task("fatJar", Jar::class) {
    group = "build"
    description = "Assembles a runnable jar with all dependencies (all-platform natives) included"
    archiveClassifier.set("all")
    manifest {
        attributes["Main-Class"] = "com.mojang.minecraft.Minecraft"
        attributes["Implementation-Version"] = project.version
    }
    from(sourceSets["main"].output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val runDir = File(projectDir, "run")

tasks.named<JavaExec>("run") {
    workingDir(runDir)
    doFirst {
        runDir.mkdirs()
    }
    // Required by GLFW on macOS — must run on the first thread.
    if (osName.contains("mac") || osName.contains("darwin")) {
        jvmArgs("-XstartOnFirstThread")
    }
}
