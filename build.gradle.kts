import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import fr.xpdustry.toxopid.extension.MindustryRepository
import fr.xpdustry.toxopid.extension.ModTarget
import fr.xpdustry.toxopid.extension.ModDependency
import fr.xpdustry.toxopid.util.ModMetadata
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.cadixdev.gradle.licenser.header.HeaderStyle

plugins {
    id("net.kyori.indra") version "2.1.1"
    id("net.kyori.indra.publishing") version "2.1.1"
    id("net.kyori.indra.license-header") version "2.1.1"
    id("net.ltgt.errorprone") version "2.0.2"
    id("fr.xpdustry.toxopid") version "1.3.2"
    id("com.github.ben-manes.versions") version "0.42.0"
}

val metadata = ModMetadata(file("${rootProject.rootDir}/plugin.json"))
group = property("props.project-group").toString()
version = metadata.version

toxopid {
    modTarget.set(ModTarget.HEADLESS)
    arcCompileVersion.set("v" + metadata.minGameVersion)
    mindustryCompileVersion.set("v" + metadata.minGameVersion)

    /*
    mindustryRepository.set(MindustryRepository.BE)
    mindustryRuntimeVersion.set("22390")

    modDependencies.set(
        listOf(
            ModDependency("Xpdustry/Javelin", "v0.3.1", "Javelin.jar"),
            ModDependency("Xpdustry/Distributor", "v2.6.1", "distributor-core.jar"),
            ModDependency("Xpdustry/KotlinRuntimePlugin", "v1.0.0", "xpdustry-kotlin-stdlib.jar")
        )
    )
     */
}

repositories {
    mavenCentral()
    maven("https://repo.xpdustry.fr/releases") {
        name = "xpdustry-releases"
        mavenContent { releasesOnly() }
    }
}

dependencies {
    // compileOnly("fr.xpdustry:javelin:0.3.1")
    // compileOnly("fr.xpdustry:distributor-core:2.6.1" )
    // implementation("net.mindustry_ddns:file-store:2.1.0")
    // implementation("org.aeonbits.owner:owner-java8:1.0.12")

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
    // Run relocation before shadow
    dependsOn(relocate)
    // Reduces shadow jar size by removing unused classes
    minimize()
}

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

    if (metadata.repo != null) {
        val repo = metadata.repo!!.split("/")
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
                    id.set(metadata.author)
                }
            }
        }
    }
}
