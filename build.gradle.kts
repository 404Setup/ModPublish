import org.jetbrains.intellij.platform.gradle.tasks.SignPluginTask

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.8.0"
}

group = "one.pkg"
version = "0.0.3"
val targetJavaVersion = 17

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.2")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.intellij.plugins.markdown")
    }
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

tasks.named<SignPluginTask>("signPlugin") {
    certificateChain.set(file("chain.crt").readText())
    privateKey.set(file("private.pem").readText())
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
}