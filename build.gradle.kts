import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.0"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.20"
	kotlin("plugin.spring") version "1.9.20"
}

group = "com.aphisiit.com"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	
	implementation("org.bouncycastle:bcpkix-jdk18on:1.77")
	
	implementation("com.itextpdf:itext7-core:7.1.18")
	implementation("com.itextpdf:sign:8.0.2")
	implementation("com.itextpdf:bouncy-castle-adapter:8.0.2")
	implementation("com.google.guava:guava:32.1.3-jre")
	implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register("version") {
	print("${project.version}")
}

tasks.register<Copy>("copyJar") {
	from(layout.buildDirectory.dir("libs/${project.name}-${project.version}.jar"))
	into(layout.buildDirectory.dir("container"))
	rename { fileName: String -> fileName.replace("-${project.version}.jar", ".jar") }
}

tasks.named("bootJar") { finalizedBy("copyJar") }