/*
 * Copyright (C) 2022, 2023, THL A29 Limited, a Tencent company. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
    withJavadocJar()
}

sourceSets.create("jmh") {
    java.setSrcDirs(listOf("src/jmh/java"))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.encoding = "UTF-8"
    }

    val testOnCurrent = register("testOnCurrent", CommonTest::class) {
        systemProperty("test.classpath", classpath.joinToString(separator = ":"))

        doFirst {
            println("Testing JDK: " + javaLauncher.get().metadata.installationPath)
        }
    }

    register("testOnAdop8", CommonTest::class) {
        systemProperty("test.classpath", classpath.joinToString(separator = ":"))

        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(8))
            vendor.set(JvmVendorSpec.ADOPTIUM)
        })

        doFirst {
            println("Testing JDK: " + javaLauncher.get().metadata.installationPath)
        }
    }

    register("testOnAdop11", CommonTest::class) {
        systemProperty("test.classpath", classpath.joinToString(separator = ":"))

        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(11))
            vendor.set(JvmVendorSpec.ADOPTIUM)
        })

        doFirst {
            println("Testing JDK: " + javaLauncher.get().metadata.installationPath)
        }
    }

    register("testOnAdop17", CommonTest::class) {
        systemProperty("test.classpath", classpath.joinToString(separator = ":"))

        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
            vendor.set(JvmVendorSpec.ADOPTIUM)
        })

        doFirst {
            println("Testing JDK: " + javaLauncher.get().metadata.installationPath)
        }
    }

    test {
        dependsOn(testOnCurrent)
    }

    javadoc {
        options.locale = "en_US"
        isFailOnError = false
    }

    register("jmh", type=JavaExec::class) {
        mainClass.set("org.openjdk.jmh.Main")
        classpath(sourceSets["jmh"].runtimeClasspath)
    }
}

publishing {
    publications {
        create<MavenPublication>("kona") {
            from(components["java"])

            val pomName: String?
            val pomDescription: String?
            if (project.name.contains("crypto")) {
                pomName = "Tencent Kona Crypto Provider"
                pomDescription = "A Java security provider for supporting ShangMi algorithms SM2, SM3 and SM4."
            } else if (project.name.contains("pkix")) {
                pomName = "Tencent Kona PKIX Provider"
                pomDescription = "A Java security provider for supporting ShangMi algorithms in public key infrastructure"
            } else if (project.name.contains("ssl")) {
                pomName = "Tencent Kona SSL Provider"
                pomDescription = "A Java security provider for supporting protocols TLCP, TLS 1.3 (RFC 8998) and TLS 1.2"
            } else {
                pomName = "Tencent Kona Provider"
                pomDescription = "A Java security provider for supporting ShangMi features"
            }

            pom {
                name.set(pomName)
                description.set(pomDescription)
                url.set("https://github.com/Tencent/TencentKonaSMSuite/tree/master/${project.name}")
                licenses {
                    license {
                        name.set("GNU GPL v2.0 license with classpath exception")
                        url.set("https://github.com/Tencent/TencentKonaSMSuite/blob/master/LICENSE.txt")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            val snapshotRepoURL = uri("https://oss.sonatype.org/content/repositories/snapshots")
            val releaseRepoURL = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")

            url = if (version.toString().endsWith("-SNAPSHOT")) snapshotRepoURL else releaseRepoURL

            // gradle.properties contains the below properties:
            // ossrhUsername=<OSSRH User Name>
            // ossrhPassword=<OSSRH Password>
            name = "ossrh"
            credentials(PasswordCredentials::class)
        }
    }
}

signing {
    sign(publishing.publications["kona"])
}

task<Exec>("signJar") {
    var javaHome = System.getProperty("java.home")

    // java.home is <JAVA_HOME>/jre for JDK 8
    if(JavaVersion.current() == JavaVersion.VERSION_1_8) {
        javaHome = "$javaHome/.."
    }

    val type = System.getProperty("ks.type", "PKCS12")
    val keystore = System.getProperty("ks.path")
    val storepass = System.getProperty("ks.storepass")
    val keypass = System.getProperty("ks.keypass")
    val alias = System.getProperty("ks.alias")

    if (keystore != null) {
        commandLine(
            "${javaHome}/bin/jarsigner",
            "-J-Duser.language=en_US",
            "-storetype", type,
            "-keystore", keystore,
            "-storepass", storepass,
            "-keypass", keypass,
            "build/libs/${project.name}-${project.version}.jar",
            alias
        )
    }
}
