//version: 1707058017

plugins {
    id("com.gtnewhorizons.gtnhconvention")
    id("idea")
}

version = "1.10.2"
group = "kamkeel.customnpc-plus"
base.archivesName.set("CustomNPC-Plus")

val embedMixin = !project.hasProperty("nomixin")
if (!embedMixin) {
    version = version.toString() + "-nomixin"
}

tasks.register<Exec>("updateAPI") {
    description = "Updates (and Inits) git submodules"
    commandLine("git", "submodule", "update", "--init", "--recursive")
    group = "CustomNPC+"
}

val gradleWrapper = if (System.getProperty("os.name").lowercase().contains("windows")) "gradlew.bat" else "./gradlew"

tasks.register<Exec>("buildNoMixin") {
    description = "Builds mod without embed"
    group = "CustomNPC+"
    workingDir(project.rootDir)
    commandLine(gradleWrapper, "build", "-Pnomixin")
}

sourceSets {
    named("main") {
        java {
            srcDirs("src/api/java")
        }
    }
}
