import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.serialization") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

val mainPackage = "com.starsep.myepisodes_kt"
val mainClass = "$mainPackage.MainKt"
group = mainPackage
version = "0.0.1"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
}

val koinVersion = "2.1.5"
val konfVersion = "0.22.1"
val ktorVersion = "1.3.1"
val jsoupVersion = "1.13.1"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("org.koin:koin-core:$koinVersion")
    implementation("org.jsoup:jsoup:$jsoupVersion")
    implementation("com.uchuhimo:konf:$konfVersion")
    implementation("me.tongfei:progressbar:0.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
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
