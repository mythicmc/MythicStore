import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
    id("net.kyori.blossom") version "2.1.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8" // IntelliJ + Blossom integration
    id("org.ajoberstar.grgit.service") version "5.2.0"
}

group = "org.mythicmc"
version = "1.2.0${getVersionMetadata()}"
description = "Utilities to help mythicmc/store run actions on the MythicMC server."

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.13.2-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.16-R0.5-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    implementation("redis.clients:jedis:3.6.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

sourceSets {
    main {
        blossom {
            resources {
                property("version", project.version.toString())
                property("description", project.description)
            }
            javaSources {
                property("version", project.version.toString())
                property("description", project.description)
            }
        }
    }
}

tasks.getByName<ShadowJar>("shadowJar") {
    minimize()
    archiveClassifier.set("")
    relocate("redis.clients", "org.mythicmc.mythicstore.shadow.redis.clients")
}

fun getVersionMetadata(): String {
    if (project.hasProperty("skipVersionMetadata")) return ""

    val grgit = try { grgitService.service.orNull?.grgit } catch (e: Exception) { null }
    if (grgit != null) {
        val head = grgit.head() ?: return "+unknown" // No head, fresh git repo
        var id = head.abbreviatedId
        val tag = grgit.tag.list().find { head.id == it.commit.id }

        // If we're on a tag and the tree is clean, don't put any metadata
        if (tag != null && grgit.status().isClean) {
            return ""
        }
        // Flag the build if the tree isn't clean
        if (!grgit.status().isClean) {
            id += "-dirty"
        }

        return "+git.$id"
    }

    return "+unknown"
}
