import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

val mainPackage = "com.starsep.myepisodeskt"
val mainClassName = "$mainPackage.MainKt"
group = mainPackage
version = "0.0.1"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(Ktor.client.core)
    implementation(Ktor.client.apache)
    implementation(Ktor.client.contentNegotiation)
    implementation(Ktor.plugins.serialization.kotlinx.json)
    implementation(Koin.core)
    implementation("org.jsoup:jsoup:_")
    implementation("com.uchuhimo:konf:_")
    implementation("me.tongfei:progressbar:_")
    implementation(KotlinX.serialization.json)
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("myepisodeskt")
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
    mainClass.set(mainClassName)
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}
