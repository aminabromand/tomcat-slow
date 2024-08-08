plugins {
	id("java")
}

group = "de.voelkel"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

tasks.withType<JavaCompile> {
	project.extensions.configure<JavaPluginExtension>("java") {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(17))
		}
	}
	options.encoding = "UTF-8"
}



dependencies {

//	val tomcatVersion = "11.0.0-M24" // slow
//	val tomcatVersion = "11.0.0-M22" // slow
	val tomcatVersion = "11.0.0-M21" // fast
//	val tomcatVersion = "11.0.0-M20" // fast
//	val tomcatVersion = "10.1.25" // slow
//	val tomcatVersion = "10.1.24" // fast
//	val tomcatVersion = "10.1.19" // fast

	implementation("org.apache.tomcat.embed:tomcat-embed-core:$tomcatVersion")
	implementation("org.apache.tomcat.embed:tomcat-embed-websocket:$tomcatVersion")
	implementation("org.apache.tomcat:tomcat-jasper:$tomcatVersion")

	implementation("org.glassfish.jersey.core:jersey-server:3.1.5")
	implementation("org.glassfish.jersey.media:jersey-media-jaxb:3.1.5")
	implementation("org.glassfish.jersey.containers:jersey-container-servlet:3.1.5")
	implementation("org.glassfish.jersey.inject:jersey-hk2:3.1.5")

	implementation("org.glassfish.jersey.core:jersey-client:3.1.5")

	implementation("org.glassfish.jersey.media:jersey-media-jaxb:3.1.5")
	implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
	implementation("org.jvnet.jaxb2_commons:jaxb2-basics-runtime:1.11.1")

//	val testImplementation by configurations
	testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.assertj:assertj-core:3.24.2")
//	testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}