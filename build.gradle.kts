import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

val mainPackage = "com.starsep.myepisodes"
val mainClass = "$mainPackage.MainKt"
group = mainPackage
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("myepisodes_kt")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to mainClass))
        }
    }
    build {
        dependsOn(named<ShadowJar>("shadowJar"))
    }
}

task("run", JavaExec::class) {
    main = mainClass
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}
