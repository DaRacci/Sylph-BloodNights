version = "ALPHA"

dependencies {
    compileOnly(project(":RacciCore"))
    compileOnly(project(":HollowsEve"))
    compileOnly(rootProject.libs.placeholderAPI)
    compileOnly(rootProject.libs.multiverseCore)
    compileOnly(rootProject.libs.ecoEnchants)
    compileOnly(files("../API/GoldenCrates.jar"))
    compileOnly(files("../API/NexEngine.jar"))
    implementation("de.eldoria", "eldo-util", "1.9.6-DEV")
}