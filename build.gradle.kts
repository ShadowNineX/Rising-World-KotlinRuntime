import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.0"
    `maven-publish`
}

group = "com.ShadowNine.KotlinRuntime"
version = "1.0.2"

// ---------------------------
// Java Toolchain
// ---------------------------
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

// ---------------------------
// Kotlin Compiler Options
// ---------------------------
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget("20"))
    }
}

// ---------------------------
// Repositories
// ---------------------------
repositories {
    mavenCentral()
}

// ---------------------------
// Dependencies
// ---------------------------

// Read from gradle.properties
val pluginApiPath: String = findProperty("PLUGINAPIJAR")?.toString()
    ?: error("❌ Gradle property 'PLUGINAPIJAR' not set in gradle.properties")

val pluginApiFile = file(pluginApiPath)
if (!pluginApiFile.exists()) {
    error("❌ PluginAPI.jar not found at: $pluginApiPath")
}

dependencies {
    // KotlinRuntime bundles kotlin-stdlib so other plugins can depend on this single runtime JAR.
    implementation(kotlin("stdlib"))
    compileOnly(files(pluginApiFile))
}

// ---------------------------
// Kotlin SourceSets
// ---------------------------
kotlin {
    sourceSets.main {
        kotlin.srcDirs("src/main/kotlin")
    }
}

// ---------------------------
// ModInfo Generation Task
// ---------------------------
val generatedSourcesDir = layout.buildDirectory.dir("generated/sources/modinfo/kotlin/main")
val modInfoName = project.name
val modInfoVersion = project.version.toString()

val generateModInfo by tasks.registering(Copy::class) {
    group = "build"
    description = "Generates ModInfo.kt from the project metadata template."
    inputs.property("modInfoName", modInfoName)
    inputs.property("modInfoVersion", modInfoVersion)

    from("src/main/resources") {
        include("ModInfo.kt.template")
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "project.name" to modInfoName,
                "project.version" to modInfoVersion
            )
        )
        rename { it.removeSuffix(".template") }
    }
    into(generatedSourcesDir)
}

kotlin.sourceSets["main"].kotlin.srcDir(generatedSourcesDir)
tasks.compileKotlin {
    description = "Compiles Kotlin sources after generating plugin metadata."
    dependsOn(generateModInfo)
}

// ---------------------------
// Fat JAR (includes only Kotlin stdlib, excludes PluginAPI)
// ---------------------------
val fatJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Builds the Kotlin runtime plugin JAR with Kotlin runtime dependencies bundled."

    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)

    // Only bundle runtimeClasspath dependencies (Kotlin stdlib)
    // Excludes compileOnly dependencies like PluginAPI
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .filter { it != pluginApiFile }  // Explicitly exclude PluginAPI
            .map { zipTree(it) }
    })

    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}
val fatJarFile = fatJar.flatMap { it.archiveFile }

tasks.build {
    description = "Builds the Kotlin runtime JAR and distribution artifacts."
    dependsOn(fatJar)
}

// ---------------------------
// Copy to dist folder
// ---------------------------
val copyToDist by tasks.registering(Copy::class) {
    group = "build"
    description = "Copies the Kotlin runtime JAR into dist/Kotlin-Runtime."

    dependsOn(fatJar)

    val distDir = layout.projectDirectory.dir("dist/Kotlin-Runtime")
    into(distDir)
    from(fatJarFile)
}

tasks.build {
    finalizedBy(copyToDist)
}

// ---------------------------
// Zip fat JAR into KotlinRuntime-<version>.zip
// ---------------------------
val zipDist by tasks.registering(Zip::class) {
    group = "build"
    description = "Packages the Kotlin runtime plugin distribution as a versioned ZIP file."

    dependsOn(fatJar)

    // inside the zip, create a folder named KotlinRuntime/
    from(fatJarFile) {
        into("KotlinRuntime")
        rename(".*\\.jar", "KotlinRuntime-$modInfoVersion.jar")
    }

    archiveFileName.set("KotlinRuntime-$modInfoVersion.zip")
    destinationDirectory.set(layout.projectDirectory.dir("dist"))
}

tasks.build {
    finalizedBy(zipDist)
}



// ---------------------------
// Clean dist folder
// ---------------------------
tasks.clean {
    description = "Deletes generated build outputs and the plugin dist folders."
    delete(layout.projectDirectory.dir("dist"))
    delete(layout.projectDirectory.dir("target"))
}
