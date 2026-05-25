plugins {
    id("java")
    application
}

application {
    mainClass = "de.alniarez.Main"
}

group = "de.alniarez"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:7.2.2")
    implementation("com.h2database:h2:2.2.224")
    implementation("io.javalin:javalin-rendering-freemarker:7.2.2")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}