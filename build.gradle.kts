import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import fr.xpdustry.toxopid.ModPlatform
import fr.xpdustry.toxopid.util.ModMetadata
import fr.xpdustry.toxopid.util.anukenJitpack
import fr.xpdustry.toxopid.util.mindustryDependencies
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("net.kyori.indra") version "2.1.1"
    id("net.kyori.indra.publishing") version "2.1.1"
    id("net.kyori.indra.license-header") version "2.1.1"
    id("net.ltgt.errorprone") version "2.0.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("fr.xpdustry.toxopid") version "2.0.0"
}

val metadata = ModMetadata.fromJson(file("plugin.json"))
group = property("props.project-group").toString()
description = metadata.description
version = metadata.version

toxopid {
    compileVersion.set("v" + metadata.minGameVersion)
    platforms.add(ModPlatform.HEADLESS)
}

repositories {
    mavenCentral()
    anukenJitpack()
}

dependencies {
    mindustryDependencies()

    val junit = "5.8.2"
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit")

    val jetbrains = "23.0.0"
    compileOnly("org.jetbrains:annotations:$jetbrains")
    testCompileOnly("org.jetbrains:annotations:$jetbrains")

    // Static analysis
    annotationProcessor("com.uber.nullaway:nullaway:0.9.7")
    errorprone("com.google.errorprone:error_prone_core:2.13.1")
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        disable("MissingSummary")
        if (!name.contains("test", true)) {
            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", project.property("props.root-package").toString())
        }
    }
}

// Required by the GitHub actions
tasks.create("getArtifactPath") {
    doLast { println(tasks.shadowJar.get().archiveFile.get().toString()) }
}

val relocate = tasks.create<ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks.shadowJar.get()
    prefix = project.property("props.root-package").toString() + ".shadow"
}

tasks.shadowJar {
    from(project.file("plugin.json"))
    dependsOn(relocate)
    minimize()
    from(rootProject.file("LICENSE.md")) {
        into("META-INF")
    }
}

tasks.build.get().dependsOn(tasks.shadowJar)

license {
    header(rootProject.file("LICENSE_HEADER.md"))
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}

indra {
    javaVersions {
        target(17)
        minimumToolchain(17)
    }

    publishSnapshotsTo("xpdustry", "https://repo.xpdustry.fr/snapshots")
    publishReleasesTo("xpdustry", "https://repo.xpdustry.fr/releases")

    gpl3OnlyLicense()

    if (metadata.repo.isNotBlank()) {
        val repo = metadata.repo.split("/")
        github(repo[0], repo[1]) {
            ci(true)
            issues(true)
            scm(true)
        }
    }

    configurePublications {
        pom {
            organization {
                name.set("Xpdustry")
                url.set("https://www.xpdustry.fr")
            }

            developers {
                developer {
                    id.set("Phinner")
                }

                developer {
                    id.set("Router")
                }
            }
        }
    }
}
