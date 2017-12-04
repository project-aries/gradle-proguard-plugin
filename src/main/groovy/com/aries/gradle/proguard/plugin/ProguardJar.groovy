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

import java.util.Date
import org.codehaus.groovy.runtime.GStringImpl
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import proguard.gradle.ProGuardTask

/**
 * Custom Proguard (sub-classing to be exact) task to make things 
 * more gradle-like.
 *
 * @author cdancy
 */
class ProguardJar extends ProGuardTask implements PublishArtifact {

    @Input
    @Optional
    public String baseName = project.name

    @Input
    @Optional
    public String classifier = 'pro'

    // file to convert to proguard
    @InputFile
    @Optional
    public File inputFile

    // file to write proguard conversion into
    @OutputFile
    @Optional
    public File outputFile

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
            def outputFilePath = getFile().path
            if (!this.getOutJarFiles().contains(outputFilePath)) {
                this.outjars(outputFilePath)
            }

            // 4.) Execute super version of proguard method to create our jar.
            super.proguard()

        } else {
            logger.quiet 'No file to process. Was this expected?'
        }
    }

    // use java libraries as inputs for proguard library jars
    void withJavaLibs() {
        withJavaLibs = true
    }

    // helper method to configure proguard for generic library generation
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

    // helper method to configure proguard for generic android generation
    void withAndroidConfiguration() {
        dontpreverify
        repackageclasses ''
        allowaccessmodification
        optimizations '!code/simplification/arithmetic'
        renamesourcefileattribute 'SourceFile'
        keepattributes 'SourceFile,LineNumberTable'
        keepattributes '*Annotation*'

        keep 'public class * extends android.app.Activity'
        keep 'public class * extends android.app.Application'
        keep 'public class * extends android.app.Service'
        keep 'public class * extends android.content.BroadcastReceiver'
        keep 'public class * extends android.content.ContentProvider'
        keep 'public class * extends android.view.View { \
            public <init>(android.content.Context); \
            public <init>(android.content.Context, android.util.AttributeSet); \
            public <init>(android.content.Context, android.util.AttributeSet, int); \
            public void set*(...); \
        }'
        keepclasseswithmembers 'class * { \
            public <init>(android.content.Context, android.util.AttributeSet); \
        }'
        keepclasseswithmembers 'class * { \
            public <init>(android.content.Context, android.util.AttributeSet, int); \
        }'
        keepclassmembers 'class * extends android.content.Context { \
           public void *(android.view.View); \
           public void *(android.view.MenuItem); \
        }'
        keepclassmembers 'class * implements android.os.Parcelable { \
            static android.os.Parcelable$Creator CREATOR; \
        }'
        keepclassmembers 'class **.R$* { \
            public static <fields>; \
        }'
        keepclassmembers 'class * { \
            @android.webkit.JavascriptInterface <methods>; \
        }'
        keep 'public interface com.android.vending.licensing.ILicensingService'
        dontnote 'com.android.vending.licensing.ILicensingService'
        dontwarn 'android.support.**'
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
            
            // 2.) Check if user passed in main input file
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
                    def jarOutputFile = project.tasks.findByName('jar')?.getArchivePath()
                    if (jarOutputFile) {
                        this.injars(jarOutputFile.path)
                        
                        // 5.) Add all runtime deps as library jars
                        project.configurations.runtime.resolve().each { libraryJarDep ->
                            this.injars(libraryJarDep.path)
                        }
                        inputFileFound = true
                    }
                }
            }
        } else {
            inputFileFound = true
        }
        
        inputFileFound
    }

    // helper method to set the baseName
    void baseName(final String baseName) {
        if (baseName) {
            this.baseName = baseName
        } else {
            throw new GradleException('Cannot set NULL baseName')
        }
    }

    // helper method to set the classifier
    void classifier(final String classifier) {
        if (classifier) {
            this.classifier = classifier
        } else {
            throw new GradleException('Cannot set NULL classifier')
        }
    }

    // helper method to set the inputFile
    void inputFile(final def inputFile) {
        if (inputFile) {
            this.inputFile = project.file(inputFile)
        } else {
            throw new GradleException('Cannot set NULL inputFile')
        }
    }

    // helper method to set the outputFile
    void outputFile(final def outputFile) {
        if (outputFile) {
            this.outputFile = project.file(outputFile)
        } else {
            throw new GradleException('Cannot set NULL outputFile')
        }
    }

    TaskDependency getBuildDependencies() {
        Task thisTask = this
        return new TaskDependency() {
            @Override
            Set<? extends Task> getDependencies(Task task) {
                return [thisTask] as Set
            }
        }
    }
    
    String getExtension() {
        Jar.DEFAULT_EXTENSION
    }

    String getType() {
        getExtension()
    }

    String getClassifier() {
        classifier
    }

    File getFile() {
        if (outputFile) {
            outputFile
        } else {
            def localDestinationDir = destinationDir ?: project.file("${project.buildDir}/libs")
            def generatedFileName = "${localDestinationDir.path}/${baseName}"

            def localVersion = project.findProperty('version')
            if (localVersion && localVersion != 'unspecified') {
                generatedFileName += "-${localVersion}"
            }

            def localClassifier = getClassifier()
            if (localClassifier) {
                generatedFileName += "-${localClassifier}"
            }

            project.file("${generatedFileName}.${getExtension()}")
        }
    }

    Date getDate() {
        def foundFile = getFile()
        foundFile.exists() ? new Date(foundFile.lastModified()) : null
    }
}

