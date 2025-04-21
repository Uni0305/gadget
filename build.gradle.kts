plugins {
	id("fabric-loom") version "1.10-SNAPSHOT"
	id("maven-publish")
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
		vendor = JvmVendorSpec.ADOPTIUM
	}
}

base {
	archivesName = property("archives_base_name").toString()
}

version = "${property("mod_version")}+${property("minecraft_base_version")}"
group = property("maven_group").toString()

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
	maven("https://api.modrinth.com/maven") {
		content {
			includeGroup("maven.modrinth")
		}
	}
	mavenLocal()
	maven("https://maven.wispforest.io/releases/")
	maven("https://maven.terraformersmc.com/releases")
	maven("https://maven.blamejared.com")
	maven("https://maven.shedaniel.me/")
	maven("https://maven.ladysnake.org/releases")
	maven("https://maven.quiltmc.org/repository/release")
	maven("https://jitpack.io")
	maven("https://maven.andante.dev/releases/")
	maven("https://maven.jamieswhiteshirt.com/libs-release") {
		content {
			includeGroup("com.jamieswhiteshirt")
		}
	}
}

sourceSets {
	register("testmod") {
		runtimeClasspath += main.get().runtimeClasspath
		compileClasspath += main.get().compileClasspath
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
	modImplementation(annotationProcessor("io.wispforest:owo-lib:${property("owo_version")}")!!)
	include("io.wispforest:owo-sentinel:${property("owo_version")}")

	modImplementation(include("me.lucko:fabric-permissions-api:${property("fabric_perms_api")}")!!)

	implementation(include("net.fabricmc:mapping-io:${property("mapping_io")}")!!)
	implementation(include("net.auoeke:result:${property("auoeke_result")}")!!)
	implementation(include("net.auoeke:unsafe:${property("auoeke_unsafe")}")!!)
	implementation(include("net.auoeke:reflect:${property("auoeke_reflect")}")!!)

	modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${property("rei_version")}")

	modCompileOnly("org.vineflower:vineflower:${property("vineflower")}")

	modLocalRuntime("com.terraformersmc:modmenu:${property("modmenu_version")}")

//	modLocalRuntime "vazkii.patchouli:Patchouli:1.19.2-76-FABRIC"
//	modLocalRuntime "dev.emi:trinkets:3.4.0"
//	modLocalRuntime "me.shedaniel:RoughlyEnoughItems-fabric:9.1.530"
//	modLocalRuntime "dev.architectury:architectury-fabric:6.3.49"
//	modLocalRuntime "com.glisco:things:0.2.20+1.19"
	modLocalRuntime("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config")}")
//	modLocalRuntime "maven.modrinth:auth-me:${property("auth_me}"

	"testmodImplementation"(sourceSets.main.map { it.output })
}

loom {
	runs {
		register("testmodClient") {
			client()
			name = "Testmod Client"
			source(sourceSets.getByName("testmod"))
		}
		register("testmodServer") {
			server()
			name = "Testmod Server"
			source(sourceSets.getByName("testmod"))
		}
	}

	accessWidenerPath = file("src/main/resources/gadget.accesswidener")
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand("version" to project.version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	// Minecraft 1.18 (1.18-pre2) upwards uses Java 17.
	options.release = 21
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()
}

abstract class WriteVersionFileTask : DefaultTask() {
	@get:Input
	abstract val version: Property<String>

	@get:OutputFile
	abstract val versionFile: RegularFileProperty

	init {
		versionFile.convention { temporaryDir.resolve("VERSION") }
	}

	@TaskAction
	fun run() {
		versionFile.get().asFile.writeText(version.get())
	}
}

val writeVersionFile = tasks.register<WriteVersionFileTask>("writeVersionFile") {
	version.set(project.version.toString())
}

tasks.assemble {
	dependsOn(writeVersionFile)
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${base.archivesName.get()}" }
	}
}

// configure the maven publication
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
		val ENV = System.getenv()

		if (ENV.containsKey("MAVEN_URL")) {
			repositories.maven(ENV["MAVEN_URL"]!!) {
				credentials {
					username = (ENV["MAVEN_USERNAME"]!!.toString())
					password = (ENV["MAVEN_PASSWORD"]!!.toString())
				}
			}
		}
	}
}
