plugins {
    kotlin("jvm") version "1.7.21"
    application
}

repositories {
    mavenCentral()
}

val ktorVersion = "1.6.8"
val exposedVersion = "0.41.1"

dependencies {
    // Discord
    implementation("com.discord4j:discord4j-core:3.0.14")

    // Milton
    implementation("com.github.ajalt:clikt:2.7.1")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-gson:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")

    // Lisp
    implementation("org.gabe.coffee:coffeelisp:1.0") {
        version {
            branch = "master"
        }
    }

    // Database
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.xerial:sqlite-jdbc:3.32.3.2")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "coffeebot.MainKt"
}
