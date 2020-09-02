import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.serialization") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("com.github.ben-manes.versions") version "0.29.0"
}

val mainPackage = "com.starsep.myepisodes_kt"
val mainClassName = "$mainPackage.MainKt"
group = mainPackage
version = "0.0.1"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
}

val koinVersion = "2.1.5"
val konfVersion = "0.22.1"
val ktorVersion = "1.4.0"
val jsoupVersion = "1.13.1"

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("org.koin:koin-core:$koinVersion")
    implementation("org.jsoup:jsoup:$jsoupVersion")
    implementation("com.uchuhimo:konf:$konfVersion")
    implementation("me.tongfei:progressbar:0.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc-218")
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
            attributes(mapOf("Main-Class" to mainClassName))
        }
    }
    build {
        dependsOn(named<ShadowJar>("shadowJar"))
    }
}

task("run", JavaExec::class) {
    main = mainClassName
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}
