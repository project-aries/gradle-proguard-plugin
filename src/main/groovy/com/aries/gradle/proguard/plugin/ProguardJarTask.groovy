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
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.internal.file.copy.CopySpecInternal
import org.gradle.api.internal.file.copy.DestinationRootCopySpec
import org.gradle.api.internal.file.copy.FileCopyAction
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.internal.reflect.Instantiator
import proguard.gradle.ProGuardTask

/**
 * Custom Proguard task to make things more gradle-like. 
 *
 * @author cdancy
 */
class ProguardJarTask extends ProGuardTask {

    @Input
    @Optional
    public String classifier
    
    @Input
    @Optional
    public File destinationDir
    
    @Internal
    private boolean useJreLibsAsLibraryJars = false

    @Override
    @TaskAction
    void proguard(){
        logger.quiet 'Creating Proguard jar...'

        // 1.) Add any inputs the user has passed in.
        if (this.getInputs().getHasInputs()) { 
            for (def possibleFile : this.getInputs().files.files) {
                def possibleFilePath = possibleFile.path
                if (!getInJarFiles().contains(possibleFilePath)) {
                    this.injars(possibleFilePath)
                }
            }
        }

        // 2.) Add OOTB java libs as library jars.
        if (useJreLibsAsLibraryJars) {
            ProguardConstants.JAVA_LIBS.each {
                this.libraryjars(it)    
            }
        }
        
        for (def possibleInJar : getInJarFiles()) {
            logger.quiet "In Jar File: ${possibleInJar}"
        }
        
        for (def possibleFile : getLibraryJarFileCollection().files) {
            logger.quiet "Found lib jar ${possibleFile}"
        }

        this.outjars("${destinationDir.path}/helloWorld.jar")
        super.proguard()
    }
    
    void useJreLibsAsLibraryJars() {
        useJreLibsAsLibraryJars = true
    }
    
    void configureForLibraryGeneration() {
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
}

