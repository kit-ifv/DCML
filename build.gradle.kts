plugins {
    kotlin("jvm") version  "2.3.0"
    id("maven-publish")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("signing")
    id("me.champeau.jmh") version "0.7.3"
}

group = "edu.kit.ifv.mobitopp"
project.group = "edu.kit.ifv.mobitopp"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}


if (checkProperty("doPublish")) {
    /* mobiTopp publishing process (see .gitlab-ci.yml)
        * Parameters such as "doPublish" must be passed in gradle command:
        *  - ./gradlew <TASKS> publish -PdoPublish=true -Pparam=value...
        * Lookup of parameters doPublish and isRelease returns true if they are specified and their value reads "true".
        * Other required parameters must be specified, otherwise an error is thrown.
        *
        * The pipeline build version is used as the published artifacts version string.
        *  - uses parameter: "buildVersion"
        *
        * Every merge on main is published to local repo: see deploy-job
        *  - checks: doPublish=true, isRelease=false
        *  - requires parameters: "localUrl", "localRepoUser" and "localRepoPassword"
        *
        * Public releases must be published manually:
        *  - checks: doPublish=true, isRelease=true
        *  - requires parameters: "publicUrl", "publicRepoUser" and "publicRepoPassword"
        */

    project.version = requireProperty("buildVersion")
    println("Setup publishing configuration for ${group}:${project.name}:${version}.")

    publishing {

        publications {

            create<MavenPublication>("mavenData") {
                from(components["java"])
                groupId = group.toString()
                artifactId = project.name
                version = project.version.toString()

                pom {
                    name.set(project.name)
                    description.set("A kotlin DSL syntax to define discrete choice models.")
                    url.set("https://github.com/kit-ifv/DCML")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://mit-license.org")
                        }
                    }

                    developers {
                        developer {
                            id.set("Robin Andre")
                            name.set("Robin Andre")
                            email.set("robin.andre@kit.edu")
                        }
                        developer {
                            id.set("Jelle Kübler")
                            name.set("Jelle Kübler")
                            email.set("jelle.kuebler@kit.edu")
                        }
                    }

                    scm {
                        connection.set("scm:git:git:https://github.com/kit-ifv/DCML.git")
                        developerConnection.set("scm:git:ssh://git@github.com/kit-ifv/DCML.git")
                        url.set("https://github.com/kit-ifv/DCML")
                    }
                }
            }

        }

        repositories {
            if (checkProperty("isRelease")) {
                println("Activate: publish public release!")

                signing {
                    sign(publishing.publications)
                }

                nexusPublishing {
                    repositories {
                        // see https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
                        sonatype {
                            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
                            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
                            username = requireProperty("publicRepoUser")
                            password = requireProperty("publicRepoPassword")
                        }
                    }
                }

            } else {
                println("Activate: publish local build!")
                maven {
                    name = "LocalRepo"
                    url = uri(requireProperty("localUrl"))
                    credentials {
                        username = requireProperty("localRepoUser")
                        password = requireProperty("localRepoPassword")
                    }
                }
            }
        }

    }

}


fun requireProperty(property: String, orElse: String? = null): String =
    requireNotNull(project.findProperty(property) as? String ?: orElse) {
        "Could not find property '$property'. Please check the gradle command args. It should contain:\n" +
                "    ./gradlew ... -P$property=<VALUE> ..."
    }

fun checkProperty(property: String): Boolean = project.hasProperty(property) && project.property(property) == "true"
