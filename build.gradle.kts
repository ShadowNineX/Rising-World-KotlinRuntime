plugins {
    kotlin("jvm") version "2.2.20"
    `maven-publish`
}

group = "com.hedgehogform.KotlinRuntime"
version = "1.0"

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
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_20)
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
    implementation(kotlin("stdlib"))
    implementation(files(pluginApiFile))
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

val generateModInfo by tasks.registering(Copy::class) {
    from("src/main/resources") {
        include("ModInfo.kt.template")
        filter { line ->
            line.replace("@project.name@", project.name)
                .replace("@project.version@", project.version.toString())
        }
        rename { it.removeSuffix(".template") }
    }
    into(generatedSourcesDir)
}

kotlin.sourceSets["main"].kotlin.srcDir(generatedSourcesDir)
tasks.compileKotlin {
    dependsOn(generateModInfo)
}

// ---------------------------
// Fat JAR (includes all dependencies)
// ---------------------------
val fatJar by tasks.registering(Jar::class) {
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    })

    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}

tasks.build {
    dependsOn(fatJar)
}

// ---------------------------
// Copy to dist folder
// ---------------------------
val copyToDist by tasks.registering(Copy::class) {
    dependsOn(fatJar)

    val distDir = layout.projectDirectory.dir("dist/Kotlin-Runtime").asFile
    into(distDir)
    from(fatJar.map { it.archiveFile.get().asFile })
}

tasks.build {
    finalizedBy(copyToDist)
}

// ---------------------------
// Zip fat JAR into KotlinRuntime-<version>.zip
// ---------------------------
val zipDist by tasks.registering(Zip::class) {
    dependsOn(fatJar)

    val jarFile = fatJar.get().archiveFile.get().asFile

    // inside the zip, create a folder named KotlinRuntime/
    from(jarFile) {
        into("KotlinRuntime")
        rename { "KotlinRuntime-${project.version}.jar" } // rename inside the zip
    }

    archiveFileName.set("KotlinRuntime-${project.version}.zip")
    destinationDirectory.set(layout.projectDirectory.dir("dist"))
}

tasks.build {
    finalizedBy(zipDist)
}



// ---------------------------
// Clean dist folder
// ---------------------------
tasks.clean {
    delete(layout.projectDirectory.dir("dist"))
    delete(layout.projectDirectory.dir("target"))
}
