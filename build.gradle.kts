plugins {
    id("java")
}

allprojects {
    group = "dev.sorokin"

    repositories {
        mavenCentral()
    }

}

subprojects {
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }
    }
}
