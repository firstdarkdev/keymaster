## KeyMaster

A Simple Gradle plugin to help you sign your jars.

***

### Getting Started

To get started, you will need keystore. If you already have this, you can skip this part.

In a terminal, or in command line, run the following command:

```bash
keytool -genkey -alias YOUR_ALIAS_HERE -keyalg RSA -keysize 2048 -keystore keystore.jks
```

Answer the required questions, and your file will be generated once completed.

Copy this file to a safe directory, somewhere you can access it easily. KEEP IT OUT OF YOUR GIT REPO

### Installing the plugin

<details open="open"><summary>Groovy DSL</summary>
To use this plugin inside your project, first you have to add our maven.

To do this, open up `settings.gradle` and add the following:

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url "https://maven.firstdark.dev/releases"
        }
    }
}
```

Next, in your `build.gradle` add:

![badge](https://maven.firstdarkdev.xyz/api/badge/latest/releases/dev/firstdark/keymaster/keymaster?color=40c14a&name=keymaster)

```groovy
plugins {
    id "dev.firstdark.keymaster" version "VERSION"
}
```

Replace VERSION with the version above.

Finally, add the following to `build.gradle` file:

```groovy
import dev.firstdark.keymaster.tasks.SignJarTask

// Register a custom task to sign your jar
tasks.register('signJar', SignJarTask) {
    // Depend on the task used to build your project
    dependsOn jar
    
    // The input artifact. This can be a Task, File or File Name
    artifactInput = jar
    
    // Optional. Set the output name of the signed jar. This defaults to the artifactInput file name, and will overwrite it
    outputFileName = "testsign"

    // The password of your key
    keyPass = "123456"
    
    // Your key alias
    keyStoreAlias = "testalias"
    
    // Your keystore password
    keyStorePass = "123456"
    
    // Your keystore location
    keyStore = "/home/hypherionsa/dummystore.jks"
}

// Example of signing another jar
tasks.register('signDummyJar', SignJarTask) {
    dependsOn createDummyJar
    artifactInput = createDummyJar

    keyPass = "123456"
    keyStoreAlias = "testalias"
    keyStorePass = "123456"
    keyStore = "/home/hypherionsa/dummystore.jks"
}
```

That's all there is to it. You can use this custom task as an input for maven publishing, [modpublisher](https://github.com/firstdarkdev/modpublisher) or any other task that takes in a file
</details>

<details><summary>Kotlin DSL</summary>
To use this plugin inside your project, first you have to add our maven.

To do this, open up `settings.gradle.kts` and add the following:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri("https://maven.firstdark.dev/releases")
        }
    }
}
```

Next, in your `build.gradle.kts` add:

![badge](https://maven.firstdarkdev.xyz/api/badge/latest/releases/dev/firstdark/keymaster/keymaster?color=40c14a&name=keymaster)

```kotlin
plugins {
    id("com.hypherionmc.modutils.modpublisher") version "VERSION"
}
```

Replace VERSION with the version above.

Finally, add the following to `build.gradle.kts` file:

```kotlin
import dev.firstdark.keymaster.tasks.SignJarTask
import org.gradle.kotlin.dsl.register

// Register a custom task to sign your jar
val signJar by tasks.register<SignJarTask>("signJar") {
    // Depend on the task used to build your project
    dependsOn(tasks.jar)

    // The input artifact. This can be a Task, File or File Name
    artifactInput = tasks.jar

    // Optional. Set the output name of the signed jar. This defaults to the artifactInput file name, and will overwrite it
    outputFileName = "testsign"

    // The password of your key
    keyPass = "123456"

    // Your key alias
    keyStoreAlias = "testalias"

    // Your keystore password
    keyStorePass = "123456"

    // Your keystore location
    keyStore = "/home/hypherionsa/dummystore.jks"
}

// Example of signing another jar
val signDummyJar by tasks.register<SignJarTask>("signDummyJar") {
    dependsOn(tasks.createDummyJar)
    artifactInput = tasks.createDummyJar

    keyPass = "123456"
    keyStoreAlias = "testalias"
    keyStorePass = "123456"
    keyStore = "/home/hypherionsa/dummystore.jks"
}
```

That's all there is to it. You can use this custom task as an input for maven publishing, [modpublisher](https://github.com/firstdarkdev/modpublisher) or any other task that takes in a file
</details>

***

If you need any other help, open an issue, or visit us on [Discord](https://discord.firstdark.dev)