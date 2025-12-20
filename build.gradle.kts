plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.wire)
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq)
    application
}

application {
    mainClass.set("xyz.block.buildersyndicate.app.BuilderSyndicateServiceKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(platform(libs.misk.bom))
    implementation(libs.misk)
    implementation(libs.misk.actions)
    implementation(libs.misk.config)
    implementation(libs.misk.core)
    implementation(libs.misk.service)
    implementation(libs.misk.inject)
    implementation(libs.misk.prometheus)
    implementation(libs.wire.runtime)

    flyway(libs.flyway.core)
    flyway(libs.flyway.mysql)
    flyway(libs.mysql.connector)

    jooqCodegen(libs.mysql.connector)

    testImplementation(kotlin("test"))
    testImplementation(libs.misk.testing)
}

flyway {
    url.set("jdbc:mysql://localhost:3307/buildersyndicate")
    user.set("root")
    password.set("root")
    migrationLocations.from("src/main/resources/db/migration")
}

jooq {
    configuration {
        jdbc {
            driver = "com.mysql.cj.jdbc.Driver"
            url = "jdbc:mysql://localhost:3307/buildersyndicate"
            user = "root"
            password = "root"
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.mysql.MySQLDatabase"
                inputSchema = "buildersyndicate"
            }
            generate {
                isDeprecated = false
                isRecords = true
                isImmutablePojos = true
                isFluentSetters = true
            }
            target {
                packageName = "xyz.block.buildersyndicate.adapters.db.jooq"
                directory = "build/generated-sources/jooq"
            }
        }
    }
}

sourceSets {
    main {
        kotlin {
            srcDir("build/generated-sources/jooq")
        }
    }
}

tasks.named("jooqCodegen") {
    onlyIf {
        gradle.startParameter.taskNames.any { it.contains("jooqCodegen") || it.contains("codegen") }
    }
}

wire {
    kotlin {
        javaInterop = true
    }
    sourcePath {
        srcDir("src/main/proto")
    }
}

tasks.test {
    useJUnitPlatform()
}
