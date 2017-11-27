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
class ProguardJarTaskTests extends AbstractFunctionalTest {

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
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
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
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
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
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
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
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
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
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('BUILD SUCCESSFUL')
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

