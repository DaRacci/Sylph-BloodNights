version = "ALPHA"

dependencies {
    compileOnly(project(":RacciCore"))
    compileOnly(rootProject.libs.placeholderAPI)
    compileOnly(rootProject.libs.multiverseCore)
    implementation("de.eldoria", "eldo-util", "1.9.6-DEV")
}