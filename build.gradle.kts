plugins {
    java
    alias(libs.plugins.shadow)
}

group = "me.char321"
version = "1.0.0"

// folia-api 26.2 ships Java 25 bytecode, so the plugin must be built with JDK 25+.
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

dependencies {
    compileOnly(libs.folia.api)
    compileOnly(libs.slimefun4) { isTransitive = false }
    compileOnly(libs.just.enough.guide) { isTransitive = false }
    compileOnly(libs.guizhan.lib.plugin)

    implementation(libs.bstats.bukkit)
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.property("version", project.version.toString())
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveClassifier = "unshaded"
}

tasks.shadowJar {
    archiveClassifier = ""
    relocate("org.bstats", "me.char321.sfadvancements.libs.bstats")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.test {
    useJUnitPlatform()
}
