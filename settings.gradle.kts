rootProject.name = "coffeebot"

sourceControl {
    gitRepository(uri("https://github.com/gabesaba/coffeelisp.git")) {
        producesModule("org.gabe.coffee:coffeelisp")
    }
}
