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
}

application {
    mainClassName = "coffeebot.MainKt"
}
