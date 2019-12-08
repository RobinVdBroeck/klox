import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.61"
    application
}

group = "com.robinvdb"
version = "0.0.1-SNAPSHOT"
val junitVersion = "5.5.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.5.2")
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.contracts.ExperimentalContracts"
}

tasks.withType<JavaExec>().configureEach {
    standardInput = System.`in`
}

tasks.register("examples") {
    group = "verification"
    description = "Runs all examples."

    fileTree("src/test/resources/examples")
        .forEach { file ->
            logger.lifecycle("Running example $file")

            javaexec {
                classpath = sourceSets["main"].runtimeClasspath
                main = "com.robinvdb.klox.KloxKt"
                args = listOf(file.path)
            }
        }
}

application {
    mainClassName = "com.robinvdb.klox.KloxKt"
}