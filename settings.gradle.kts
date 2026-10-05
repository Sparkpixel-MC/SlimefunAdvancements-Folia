pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        // hosts JustEnoughGuide
        maven("https://repo.bacteriawa.com/repository/maven-public/")
    }
}

rootProject.name = "SlimefunAdvancements"
