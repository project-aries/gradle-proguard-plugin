/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aries.gradle.proguard.plugin

import org.gradle.testkit.runner.BuildResult
import spock.lang.Requires

/*

    Tests to exercise using the 'proguardJar' task.

*/
class ProguardJarTests extends AbstractFunctionalTest {

    def "Build Proguard Jar using OOTB task inputs"() {

        buildFile << """
            configurations {
                customConfig
            }

            dependencies {
                customConfig (group: 'org.apache.ant', name: 'ant', version: '1.10.1') {
                    transitive = false
                }
            }

            proguardJar {
                withJavaLibs()
                withLibraryConfiguration()
                dontwarn()

                inputs.files configurations.findByName('customConfig').files.first()
            }

            task workflow {
                dependsOn proguardJar
                doLast {
                    def sourceLength = configurations.findByName('customConfig').files.first().length()
                    def targetLength = proguardJar.getFile().length()
                    logger.quiet "Found Lengths: sourceLength=\${sourceLength}, targetLength=\${targetLength}"

                    if (sourceLength > targetLength) {
                        logger.quiet "Source is bigger than Target"
                    }
                }
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
        result.output.contains('Source is bigger than Target')
        !result.output.contains('No jars to process')
    }

    def "Build Proguard Jar using inputFile parameter"() {

        buildFile << """
            configurations {
                customConfig
            }

            dependencies {
                customConfig (group: 'org.apache.ant', name: 'ant', version: '1.10.1') {
                    transitive = false
                }
            }

            proguardJar {
                withJavaLibs()
                withLibraryConfiguration()
                dontwarn()

                inputFile = configurations.findByName('customConfig').files.first()
            }

            task workflow {
                dependsOn proguardJar
                doLast {
                    def sourceLength = configurations.findByName('customConfig').files.first().length()
                    def targetLength = proguardJar.getFile().length()
                    logger.quiet "Found Lengths: sourceLength=\${sourceLength}, targetLength=\${targetLength}"

                    if (sourceLength > targetLength) {
                        logger.quiet "Source is bigger than Target"
                    }
                }
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
        result.output.contains('Source is bigger than Target')
        !result.output.contains('No jars to process')
    }

    def "Build Proguard Jar using inputFile method"() {

        buildFile << """
            configurations {
                customConfig
            }

            dependencies {
                customConfig (group: 'org.apache.ant', name: 'ant', version: '1.10.1') {
                    transitive = false
                }
            }

            proguardJar {
                withJavaLibs()
                withLibraryConfiguration()
                dontwarn()

                inputFile configurations.findByName('customConfig').files.first()
            }

            task workflow {
                dependsOn proguardJar
                doLast {
                    def sourceLength = configurations.findByName('customConfig').files.first().length()
                    def targetLength = proguardJar.getFile().length()
                    logger.quiet "Found Lengths: sourceLength=\${sourceLength}, targetLength=\${targetLength}"

                    if (sourceLength > targetLength) {
                        logger.quiet "Source is bigger than Target"
                    }
                }
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
        result.output.contains('Source is bigger than Target')
        !result.output.contains('No jars to process')
    }

    def "Build Proguard Jar using injars method"() {

        buildFile << """
            configurations {
                customConfig
            }

            dependencies {
                customConfig (group: 'org.apache.ant', name: 'ant', version: '1.10.1') {
                    transitive = false
                }
            }

            proguardJar {
                withJavaLibs()
                withLibraryConfiguration()
                dontwarn()

                injars configurations.findByName('customConfig').files.first().path
            }

            task workflow {
                dependsOn proguardJar
                doLast {
                    def sourceLength = configurations.findByName('customConfig').files.first().length()
                    def targetLength = proguardJar.getFile().length()
                    logger.quiet "Found Lengths: sourceLength=\${sourceLength}, targetLength=\${targetLength}"

                    if (sourceLength > targetLength) {
                        logger.quiet "Source is bigger than Target"
                    }
                }
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
        result.output.contains('Source is bigger than Target')
        !result.output.contains('No jars to process')
    }

    def "Build Proguard Jar with different baseName and classifier"() {

        buildFile << """
            configurations {
                customConfig
            }

            dependencies {
                customConfig (group: 'org.apache.ant', name: 'ant', version: '1.10.1') {
                    transitive = false
                }
            }

            proguardJar {
                withJavaLibs()
                withLibraryConfiguration()
                dontwarn()

                baseName = 'Hello'
                classifier = 'world'
                injars configurations.findByName('customConfig').files.first().path
            }

            task workflow {
                dependsOn proguardJar
                doLast {
                    def sourceLength = configurations.findByName('customConfig').files.first().length()
                    def targetLength = proguardJar.getFile().length()
                    logger.quiet "Found Lengths: sourceLength=\${sourceLength}, targetLength=\${targetLength}"

                    if (sourceLength > targetLength) {
                        logger.quiet "Source is bigger than Target"
                    }

                    println "FileName: \${proguardJar.getFile().getName()}"
                }
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
        result.output.contains('Source is bigger than Target')
        result.output.contains("FileName: Hello-world.jar")
        !result.output.contains('No jars to process')
    }

    def "Build Proguard Jar with different outputFile"() {

        buildFile << """
            configurations {
                customConfig
            }

            dependencies {
                customConfig (group: 'org.apache.ant', name: 'ant', version: '1.10.1') {
                    transitive = false
                }
            }

            proguardJar {
                withJavaLibs()
                withLibraryConfiguration()
                dontwarn()

                inputFile configurations.findByName('customConfig').files.first().path
                outputFile project.file("\${buildDir}/HelloWorld.jar")
            }

            task workflow {
                dependsOn proguardJar
                doLast {
                    def sourceLength = configurations.findByName('customConfig').files.first().length()
                    def targetLength = proguardJar.getFile().length()
                    logger.quiet "Found Lengths: sourceLength=\${sourceLength}, targetLength=\${targetLength}"

                    if (sourceLength > targetLength) {
                        logger.quiet "Source is bigger than Target"
                    }

                    println "FileName: \${proguardJar.getFile().getName()}"
                    println "New Location exists: " + project.file("\${buildDir}/HelloWorld.jar").exists()
                }
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
        result.output.contains('Source is bigger than Target')
        result.output.contains('New Location exists: true')
        !result.output.contains('No jars to process')
    }

    def "UP-TO-DATE when Proguard built twice"() {

        buildFile << """
            configurations {
                customConfig
            }

            dependencies {
                customConfig (group: 'org.apache.ant', name: 'ant', version: '1.10.1') {
                    transitive = false
                }
            }

            proguardJar {
                withJavaLibs()
                withLibraryConfiguration()
                dontwarn()

                inputFile configurations.findByName('customConfig').files.first()
            }

            task workflow {
                dependsOn proguardJar
                doLast {
                    def sourceLength = configurations.findByName('customConfig').files.first().length()
                    def targetLength = proguardJar.getFile().length()
                    logger.quiet "Found Lengths: sourceLength=\${sourceLength}, targetLength=\${targetLength}"

                    if (sourceLength > targetLength) {
                        logger.quiet "Source is bigger than Target"
                    }
                }
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
        result.output.contains('Source is bigger than Target')
        !result.output.contains('No jars to process')
        
        when:
        result = build('workflow')

        then:
        result.output.contains(':proguardJar UP-TO-DATE')
        !result.output.contains('No jars to process')
    }

    def "When no inputs are found print 'No file to process' message"() {

        buildFile << """
            proguardJar {
                withJavaLibs()
                withLibraryConfiguration()
                dontwarn()
            }

            task workflow {
                dependsOn proguardJar
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('No file to process')
    }
}

