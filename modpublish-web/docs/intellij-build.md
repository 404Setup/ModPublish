# IntelliJ Edition Build Instructions

If you prefer to compile the IntelliJ plugin from source, follow these instructions.

## Prerequisites

* JDK 21
* Gradle 9
* Python 3

## Build Steps

1. Clone the repository and go to the `modpublish-intellij` subproject directory.
2. Run the version processor in the root directory to query and format Minecraft version lists:
   ```bash
   python ../version_processor.py
   ```
3. Copy the generated `minecraft.version.json` file into the resource path:
   ```bash
   cp ../minecraft.version.json ./src/main/resources/META-INF/
   ```
4. Compile and build the plugin zip via Gradle:
   ```bash
   ./gradlew buildPlugin
   ```
5. The compiled zip file will be generated at `./build/distributions/`. You can install it manually in IntelliJ via "Install Plugin from Disk".
