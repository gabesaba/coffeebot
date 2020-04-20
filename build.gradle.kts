plugins {
    kotlin("jvm") version "1.3.70"
    application
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.discord4j:discord4j-core:3.0.13")

    implementation("org.gabe.coffee:coffeelisp:1.0") {
        version {
            branch = "coffeebot"
        }
    }

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "coffeebot.MainKt"
}
