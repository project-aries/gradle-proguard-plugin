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

import org.codehaus.groovy.runtime.GStringImpl
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.internal.file.copy.CopySpecInternal
import org.gradle.api.internal.file.copy.DestinationRootCopySpec
import org.gradle.api.internal.file.copy.FileCopyAction
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.internal.reflect.Instantiator
import proguard.gradle.ProGuardTask

/**
 * Custom Proguard (sub-classing to be exact) task to make things 
 * more gradle-like.
 *
 * @author cdancy
 */
class ProguardJarTask extends ProGuardTask {

    @Input
    @Optional
    public String classifier = 'pro'

    // file to convert to proguard
    @InputFile
    @Optional
    public File inputFile

    // lazily resolves to build/libs
    @InputFile
    public File destinationDir

    // whether to use java libraries for proguard generation
    @Internal
    private boolean withJavaLibs = false

    @Override
    @TaskAction
    void proguard(){

        // 1.) Only proceed if we have something to process otherwise output `NO-SOURCE` message
        def proguardInputFileFound = setProguardInputFile()
        if (proguardInputFileFound) {
            
            // 2.) Add OOTB java libs as library jars if requested.
            if (withJavaLibs) {
                ProguardConstants.JAVA_LIBS.each {
                    this.libraryjars(it)    
                }
            }

            // 3.) Set outputFile if it doesn't already exist. Very low
            //     likelyhood of getting a duplicate here but do a check
            //     just to be on the safe side.
            def outputFilePath = getOutputFile().path
            if (!this.getOutJarFiles().contains(outputFilePath)) {
                this.outjars(outputFilePath)
            }

            // 4.) Execute super version of proguard method to create our jar.
            super.proguard()

        } else {
            logger.quiet 'No jars to process. Was this expected?'
        }
    }

    // use java libraries as inputs for proguard library jars
    void withJavaLibs() {
        withJavaLibs = true
    }

    // helper method to configure proguard for library generation
    void withLibraryConfiguration() {
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
    }

    // find a file to use as input for proguard and set it
    private boolean setProguardInputFile() {

        boolean inputFileFound = false

        // 1.) If no 'injars' were specified then proceed to find one
        if (this.getInJarFiles().isEmpty()) {
            
            // 2.) Check if user passed in main inputFile
            if (inputFile) {
                this.injars(inputFile.path)
                inputFileFound = true
            } else {

                // 3.) Check if user passed in jar through task inputs.
                if(this.getInputs().getHasInputs() && !this.getInputs().getFiles().isEmpty()) {
                    this.injars(this.getInputs().getFiles().getSingleFile().path)
                    inputFileFound = true
                } else {

                    // 4.) If nothing was passed in assume user wanted us to use
                    //     output of jar task.
                    def jarOutputFile = project.tasks.findByName('jar')?.outputs?.files?.singleFile
                    if (jarOutputFile) {
                        this.injars(jarOutputFile.path)
                        inputFileFound = true
                    }
                }
            }
        } else {
            inputFileFound = true
        }
        
        inputFileFound
    }

    @OutputFile
    public File getOutputFile() {

        def localDestinationDir = destinationDir ?: project.file("${project.buildDir}/libs")
        def generatedFileName = "${localDestinationDir.path}/${project.name}"

        def localVersion = project.findProperty('version')
        if (localVersion && localVersion != 'unspecified') {
            generatedFileName += "-${localVersion}"
        }

        if (classifier) {
            generatedFileName += "-${classifier}"
        }

        return project.file("${generatedFileName}.jar")
    }

    void inputFile(final File inputFile) {
        if (inputFile) {
            this.inputFile = inputFile
        } else {
            throw new GradleException('Cannot set NULL inputFile')
        }
    }
}

