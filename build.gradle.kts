plugins {
    kotlin("jvm") version "1.3.72"
    application
}

repositories {
    jcenter()
}

val ktorVersion = "1.3.2"

dependencies {
    implementation(kotlin("stdlib"))

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
    implementation("org.jetbrains.exposed", "exposed-core", "0.28.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.28.1")
    implementation("org.xerial:sqlite-jdbc:3.32.3.2")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "coffeebot.MainKt"
}
