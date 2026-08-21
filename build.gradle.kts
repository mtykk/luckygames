plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.0"
}

group = "io.github.mtykk"
version = "0.0.1"

repositories {
    mavenCentral()
    maven{
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven{
        name = "codemc"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("com.jeff-media:custom-block-data:2.2.8")
    compileOnly("commons-io:commons-io:2.22.0")
    compileOnly("me.zombie_striker:QualityArmory:2.1.3")
    implementation("fr.mrmicky:fastboard:2.2.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar{
    relocate("fr.mrmicky.fastboard","io.github.mtykk.luckygames.fastboard")
}