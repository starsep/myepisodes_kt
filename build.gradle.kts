import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
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
    implementation(Ktor.client.core)
    implementation(Ktor.client.apache)
    implementation("org.koin:koin-core:_")
    implementation("org.jsoup:jsoup:_")
    implementation("com.uchuhimo:konf:_")
    implementation("me.tongfei:progressbar:_")
    implementation("io.ktor:ktor-client-json-jvm:_")
    implementation("io.ktor:ktor-client-serialization-jvm:_")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:_")
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
