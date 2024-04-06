## KeyMaster

A Simple Gradle plugin to help you sign your jars.

***

### Getting Started

To get started, you will a GPG key. If you already have this, you can skip it.

In a terminal, or in command line, run the following commands:

```bash
# generate the keys
gpg --gen-key

#export the private key with the specified id to a file
gpg --output {private key file name and path} --armor --export-secret-keys {key-id}

#export the public key with the specified id to a file
gpg --output {public key file name and path} --armor --export {key-id}
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

// This is optional. These values can be configured on the task
keymaster {
    // GPG Password
    gpgPassword = "123456"
    // GPG Key file, or String.
    gpgKey = System.getenv("GPG_KEY")
    // Generate a .sig file for signed jars, to be used for verification
    generateSignature = true
}

// Register a custom task to sign your jar
tasks.register('signJar', SignJarTask) {
    // Depend on the task used to build your project
    dependsOn jar

    // The input artifact. This can be a Task, File or File Name
    artifactInput = jar

    // Optional. Set the output name of the signed jar. This defaults to the artifactInput file name, and will overwrite it
    outputFileName = "testsign"

    // GPG Private key file or string. Not required when the extension is used
    gpgKey = System.getenv("GPG_KEY")

    // GPG Private Key password. Not required when extension is used
    gpgPassword = "123456"

    // Should the task generate a .sig file. Defaults to true, and not required when extension is used
    generateSignature = false
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

// This is optional. These values can be configured on the task
extensions.configure<KeymasterExtension>("keymaster") {
    // GPG Password
    gpgPassword = "123456"
    // GPG Key file, or String.
    gpgKey = System.getenv("GPG_KEY")
    // Generate a .sig file for signed jars, to be used for verification
    generateSignature = true
}

// Register a custom task to sign your jar
tasks.register("signJar", SignJarTask::class) {
    // Depend on the task used to build your project
    dependsOn(tasks.jar)

    // The input artifact. This can be a Task, File or File Name
    artifactInput = tasks.jar

    // Optional. Set the output name of the signed jar. This defaults to the artifactInput file name, and will overwrite it
    outputFileName = "testsign"

    // GPG Private key file or string. Not required when the extension is used
    gpgKey = System.getenv("GPG_KEY")

    // GPG Private Key password. Not required when extension is used
    gpgPassword = "123456"

    // Should the task generate a .sig file. Defaults to true, and not required when extension is used
    generateSignature = false
}
```

That's all there is to it. You can use this custom task as an input for maven publishing, [modpublisher](https://github.com/firstdarkdev/modpublisher) or any other task that takes in a file
</details>

***

If you need any other help, open an issue, or visit us on [Discord](https://discord.firstdark.dev)