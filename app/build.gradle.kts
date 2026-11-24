plugins {
    id("bms2mania.java-conventions")
    application
    alias(libs.plugins.javafx)
}

dependencies {
    implementation(project(":core"))
}

javafx {
    version = libs.versions.javafx.get()
    modules("javafx.controls")
}

application {
    mainClass = "io.github.kl35805.bms2mania.app.Launcher"
}