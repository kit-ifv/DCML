plugins {
    kotlin("jvm") version "2.1.20"
    id("maven-publish")
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "edu.kit.ifv.mobitopp"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
version = "1.0.1"
//version = project.findProperty("version")?.toString() ?: "1.0.0"
publishing {
    publications {
        register("mavenData", MavenPublication::class) {
            from(components["kotlin"])
        }
    }
    repositories {
        maven {
            url = uri("https://nexus.ifv.kit.edu/repository/maven-releases/")

            credentials {
                username = project.findProperty("nexusUsername") as String?
                password = project.findProperty("nexusPassword") as String?
            }
        }
    }
}
