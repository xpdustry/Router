import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import fr.xpdustry.toxopid.ModPlatform
import fr.xpdustry.toxopid.task.GitHubArtifact
import fr.xpdustry.toxopid.task.GitHubDownload
import fr.xpdustry.toxopid.util.ModMetadata
import fr.xpdustry.toxopid.util.anukenJitpack
import fr.xpdustry.toxopid.util.mindustryDependencies
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("com.diffplug.spotless") version "6.11.0"
    id("net.kyori.indra") version "3.0.1"
    id("net.kyori.indra.publishing") version "3.0.1"
    id("net.kyori.indra.git") version "3.0.1"
    id("net.kyori.indra.licenser.spotless") version "3.0.1"
    id("net.ltgt.errorprone") version "2.0.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("fr.xpdustry.toxopid") version "2.1.1"
}

val metadata = ModMetadata.fromJson(file("plugin.json").readText())
if (indraGit.headTag() == null) {
    metadata.version += "-SNAPSHOT"
}
group = "fr.xpdustry"
version = metadata.version
description = metadata.description

toxopid {
    compileVersion.set("v" + metadata.minGameVersion)
    platforms.add(ModPlatform.HEADLESS)
}

repositories {
    mavenCentral()
    anukenJitpack()
    maven("https://maven.xpdustry.fr/releases") {
        name = "xpdustry-releases"
        mavenContent { releasesOnly() }
    }
}

dependencies {
    mindustryDependencies()
    implementation("com.alibaba.fastjson2:fastjson2:2.0.20")
    compileOnly("fr.xpdustry:distributor-api:3.0.0-rc.3")
    annotationProcessor("fr.xpdustry:distributor-api:3.0.0-rc.3")

    val junit = "5.9.0"
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit")

    // Static analysis
    annotationProcessor("com.uber.nullaway:nullaway:0.10.5")
    errorprone("com.google.errorprone:error_prone_core:2.16")
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        disable(
            "MissingSummary",
            "FutureReturnValueIgnored",
            "InlineMeSuggester",
            "EmptyCatch"
        )
        if (!name.contains("test", true)) {
            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", "fr.xpdustry.router")
            option("NullAway:TreatGeneratedAsUnannotated", true)
        }
    }
}

val downloadModDependencies = tasks.register<GitHubDownload>("downloadModDependencies") {
    artifacts.add(
        GitHubArtifact.release("Xpdustry", "Distributor", "v3.0.0-rc.3", "Distributor.jar")
    )
}

tasks.runMindustryServer {
    mods.setFrom(tasks.shadowJar, downloadModDependencies)
}

// It does not need the plugin
tasks.runMindustryClient {
    mods.setFrom()
}

// Required by the GitHub actions
tasks.register("getArtifactPath") {
    doLast { println(tasks.shadowJar.get().archiveFile.get().toString()) }
}

val relocate = tasks.create<ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks.shadowJar.get()
    prefix = "fr.xpdustry.router.shadow"
}

tasks.shadowJar {
    dependsOn(relocate)
    minimize()
    doFirst {
        val temp = temporaryDir.resolve("plugin.json")
        temp.writeText(metadata.toJson(true))
        from(temp)
    }
    from(rootProject.file("LICENSE.md")) {
        into("META-INF")
    }
    archiveFileName.set("RouterPlugin.jar")
}

tasks.build {
    dependsOn(tasks.shadowJar)
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

    publishSnapshotsTo("xpdustry", "https://maven.xpdustry.fr/snapshots")
    publishReleasesTo("xpdustry", "https://maven.xpdustry.fr/releases")

    gpl3OnlyLicense()

    val repo = metadata.repo.split("/")
    github(repo[0], repo[1]) {
        ci(true)
        issues(true)
        scm(true)
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
                    timezone.set("Europe/Brussels")
                }

                developer {
                    id.set("Router")
                }
            }
        }
    }
}

spotless {
    java {
        palantirJavaFormat()
        formatAnnotations()
        custom("noWildcardImports") {
            if (it.contains("*;\n")) {
                throw Error("No wildcard imports allowed")
            }
            it
        }
        bumpThisNumberIfACustomStepChanges(1)
    }
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("LICENSE_HEADER.md"))
}
