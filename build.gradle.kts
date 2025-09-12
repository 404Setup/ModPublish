import org.jetbrains.intellij.platform.gradle.tasks.SignPluginTask

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.8.0"
}

group = "one.pkg"
version = "0.0.5"
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

    options.compilerArgs.add("-Xdiags:verbose")
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.2")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.intellij.plugins.markdown")
        bundledPlugin("Git4Idea")
    }

    implementation("one.tranic:t-proxy:1.0.1")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    compileOnly("com.vladsch.flexmark:flexmark:0.64.8")
    compileOnly("com.vladsch.flexmark:flexmark-html2md-converter:0.64.8")
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252"
        }
    }
    pluginVerification {
        ides {
            recommended()
        }
    }
}

task("generateChangeNotes") {
    description = "Generate change-notes from markdown files"

    doLast {
        val changeNotesDir = File(projectDir, "changelogs")
        val pluginXmlFile = File(projectDir, "src/main/resources/META-INF/plugin.xml")

        if (!changeNotesDir.exists()) {
            println("Change notes directory not found: ${changeNotesDir.absolutePath}")
            println("Creating example directory and file...")
            changeNotesDir.mkdirs()
            val exampleFile = File(changeNotesDir, "CHANGELOG.md")
            exampleFile.writeText(
                """
                # Changelog
            """.trimIndent()
            )
            println("Created example changelog at: ${exampleFile.absolutePath}")
        }

        if (pluginXmlFile.exists()) {
            val markdownFiles: List<File> = changeNotesDir.listFiles { _, name ->
                name.endsWith(".md") || name.endsWith(".markdown")
            }?.sortedByDescending { it.lastModified() } ?: arrayListOf()

            if (markdownFiles.isEmpty()) {
                println("No markdown files found in ${changeNotesDir.absolutePath}")
                return@doLast
            }

            val htmlContent = convertMarkdownToHtml(markdownFiles)
            val pluginXmlContent = pluginXmlFile.readText()

            val changeNotesRegex = Regex(
                """(\s*)<change-notes>\s*<!\[CDATA\[(.*?)\]\]>\s*</change-notes>""",
                RegexOption.DOT_MATCHES_ALL
            )

            val updatedContent = pluginXmlContent.replace(changeNotesRegex) { matchResult ->
                val leadingSpaces = matchResult.groupValues[1]
                """${leadingSpaces}<change-notes><![CDATA[
$leadingSpaces      $htmlContent
$leadingSpaces]]></change-notes>"""
            }

            pluginXmlFile.writeText(updatedContent)
            println("Updated plugin.xml with change-notes from ${markdownFiles.size} markdown file(s)")
        } else {
            println("plugin.xml not found at: ${pluginXmlFile.absolutePath}")
        }


    }
}

fun convertMarkdownToHtml(markdownFiles: List<File>): String {
    val htmlBuilder = StringBuilder()

    for ((index, file) in markdownFiles.withIndex()) {
        val content = file.readText()
        val html = markdownToHtml(content)
        htmlBuilder.append(html)

        if (index < markdownFiles.size - 1) {
            htmlBuilder.append("\n")
        }
    }

    return htmlBuilder.toString().trim()
}

fun markdownToHtml(markdown: String): String {
    var html = markdown

    html = html.replace(Regex("\\n\\s*\\n"), "\n")

    html = html.replace(Regex("^# (.+)$", RegexOption.MULTILINE), "<h1>$1</h1>")
    html = html.replace(Regex("^## (.+)$", RegexOption.MULTILINE), "<h2>$1</h2>")
    html = html.replace(Regex("^### (.+)$", RegexOption.MULTILINE), "<h3>$1</h3>")

    html = html.replace(Regex("^---+$", RegexOption.MULTILINE), "<hr>")

    html = html.replace(Regex("^- (.+)$", RegexOption.MULTILINE), "<li>$1</li>")
    html = html.replace(Regex("^\\d+\\. (.+)$", RegexOption.MULTILINE), "<li>$1</li>")

    html = html.replace(Regex("""\*\*(.+?)\*\*"""), "<strong>$1</strong>")
    html = html.replace(Regex("""\*(.+?)\*"""), "<em>$1</em>")

    html = html.replace(Regex("(<li>.*?</li>)(?:\\s*<li>.*?</li>)*", RegexOption.DOT_MATCHES_ALL)) { matchResult ->
        val fullMatch = matchResult.value
        val isOrderedList = markdown.contains(Regex("^\\d+\\. ", RegexOption.MULTILINE))

        if (isOrderedList) {
            "<ol>$fullMatch</ol>"
        } else {
            "<ul>$fullMatch</ul>"
        }
    }

    html = html.replace(Regex("^(?!<[/]?(?:h[1-6]|ul|ol|li|hr|p)>)(.+)$", RegexOption.MULTILINE), "<p>$1</p>")

    html = html.replace(Regex("\\s*\\n\\s*"), "")

    return html
}


tasks.named<ProcessResources>("processResources") {
    dependsOn("generateChangeNotes")

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