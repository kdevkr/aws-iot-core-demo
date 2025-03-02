plugins {
    id("java")
}

group = "kr.kdev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("software.amazon.awssdk:bom:2.30.28"))
    implementation("software.amazon.awssdk:iot")
    implementation("software.amazon.awssdk.iotdevicesdk:aws-iot-device-sdk:1.23.0")
    implementation("software.amazon.awssdk:arns")
    implementation("ch.qos.logback:logback-core:1.5.17")
    implementation("ch.qos.logback:logback-classic:1.5.17")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}