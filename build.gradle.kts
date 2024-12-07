plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.22"
    id("org.jetbrains.kotlin.kapt") version "1.7.22"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.22"
    id("groovy") 
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.7.10"
}

version = "0.1"
group = "module.checker"

val kotlinVersion=project.properties.get("kotlinVersion")
repositories {
    mavenCentral()
}

dependencies {
    kapt("info.picocli:picocli-codegen")
    kapt("io.micronaut:micronaut-http-validation")
    implementation("info.picocli:picocli")
    implementation(libs.micronaut.http.client)
    implementation(libs.micronaut.serde.jackson)
    implementation(libs.micronaut.reactor)
    implementation("io.micronaut.cache:micronaut-cache-caffeine")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.picocli:micronaut-picocli")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation(libs.jline.graal)
    implementation(libs.jline.terminal.jansi)
    implementation(libs.jline.terminal.jna)

    runtimeOnly("ch.qos.logback:logback-classic")
    implementation("io.micronaut:micronaut-validation")

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

}


application {
    mainClass.set("module.checker.ModuleCheckerCommand")
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
micronaut {
    version(libs.versions.micronaut.asProvider().get())
    testRuntime("spock2")
    processing {
        incremental(true)
        annotations("module.checker.*")
    }
}



