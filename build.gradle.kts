plugins {
    id("java")
    id("version-catalog")
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

group = "me.racci"
version = "1.0.0"

dependencies {
    compileOnly(rootProject.libs.racciCore)
    compileOnly("me.racci:SylphEvents:1.0.0")
    compileOnly(rootProject.libs.plugin.placeholderAPI)
    compileOnly(rootProject.libs.plugin.multiverseCore)
    compileOnly(rootProject.libs.plugin.ecoEnchants)
    compileOnly(files("../API/GoldenCrates.jar"))
    compileOnly(files("../API/NexEngine.jar"))
    implementation("de.eldoria", "eldo-util", "1.9.6-DEV")
}

tasks {

    compileKotlin {
        kotlinOptions.suppressWarnings = true
        kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    build {
        dependsOn(shadowJar)
        dependsOn(publishToMavenLocal)
    }

    val devServer by registering(Jar::class) {
        dependsOn(shadowJar)
        destinationDirectory.set(File("${System.getProperty("user.home")}/Desktop/Minecraft/Sylph/Development/plugins/"))
        archiveClassifier.set("")
        from(shadowJar)
    }

    val sourcesJar by registering(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    val javadocJar by registering(Jar::class) {
        dependsOn("dokkaJavadoc")
        archiveClassifier.set("javadoc")
        from(dokkaJavadoc.get().outputDirectory)
    }

    dokkaGfm {
        outputDirectory.set(File("$buildDir/../docs"))
    }

    artifacts {
        sourceArtifacts(sourcesJar)
        archives(javadocJar)
        archives(jar)
    }

    getByName<Test>("test") {
        useJUnitPlatform()
    }

}

tasks.processResources {
    from(sourceSets.main.get().resources.srcDirs) {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/DaRacci/Sylph-BloodNights")
            credentials {
                password = System.getenv("TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            //artifactId = project.name.toLowerCase()
        }
    }
}