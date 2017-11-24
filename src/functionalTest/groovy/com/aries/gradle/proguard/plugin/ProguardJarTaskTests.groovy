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

    def "Hello World"() {

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
                dontwarn
                target '7'
                overloadaggressively
                repackageclasses ''
                keepparameternames
                renamesourcefileattribute 'SourceFile'
                keepattributes 'Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,EnclosingMethod,*Annotation*'
                keep 'public class * { \
                    public protected *; \
                }'
                keepclassmembernames 'class * { \
                    java.lang.Class class\$(java.lang.String); \
                    java.lang.Class class\$(java.lang.String, boolean); \
                }'
                keepclasseswithmembernames includedescriptorclasses:true, 'class * { \
                    native <methods>; \
                }'
                keepclassmembers allowshrinking:true, 'enum * { \
                    public static **[] values(); \
                    public static ** valueOf(java.lang.String); \
                }'
                keepclassmembers 'class * implements java.io.Serializable { \
                    static final long serialVersionUID; \
                    static final java.io.ObjectStreamField[] serialPersistentFields; \
                    private void writeObject(java.io.ObjectOutputStream); \
                    private void readObject(java.io.ObjectInputStream); \
                    java.lang.Object writeReplace(); \
                    java.lang.Object readResolve(); \
                }'

                inputs.files configurations.findByName('customConfig').files.first()
            }

            task workflow {
                dependsOn proguardJar
                doLast {
                    def output = ["ls", "-alh", "${projectDir}/build/libs"].execute().text
                    println "Output: \${output}"

                    output = ["unzip", "-vl", "${projectDir}/build/libs/helloWorld.jar"].execute().text
                    println "Output: \${output}"
                }
            }
        """

        when:
        BuildResult result = build('workflow')

        then:
        result.output.contains('Hello World')
    }
}

