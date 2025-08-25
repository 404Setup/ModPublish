plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.7.2"
}

group = "one.pkg"
version = "0.0.1"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.2")
        bundledPlugin("com.intellij.java")
    }

    implementation("io.github.wasabithumb:jtoml:1.2.0")
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252"
        }
    }
    pluginVerification  {
        ides {
            recommended()
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("META-INF/plugin.xml") {
        expand(props)
    }
}
